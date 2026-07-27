-- =============================================================================
-- V6__backfill_movimientos_historicos.sql - MS-Cobros
-- Backfill idempotente: crea los movimientos contables que faltan para
-- registros historicos (anteriores a que existiera la sincronizacion auto):
--   1) Pagos existentes sin movimiento vinculado (INGRESO).
--   2) Registros de combustible sin movimiento vinculado (GASTO).
--   3) Mantenimientos activos (no soft-deleted) sin movimiento (GASTO).
--
-- Idempotente: usa NOT EXISTS para no duplicar. Se puede re-ejecutar el SQL
-- (via psql o repitiendo la migracion en un entorno de prueba) sin efectos
-- colaterales.
--
-- Cuentas destino: se toman de la config de la escuela vigente al momento
-- del deploy. Si no hay cuenta default para un rubro, ese bloque no crea
-- nada y queda para backfill manual mas adelante.
--
-- Cross-schema safety: los MSs corren sus Flyway en paralelo contra la BD
-- compartida (no hay depends_on entre ellos). Si esta migracion corre antes
-- que ms-auth cree auth_schema o que ms-vehiculos cree vehiculos_schema,
-- las lecturas cross-schema fallarian con "relation does not exist" y
-- crashearia el startup de ms-cobros. Por eso cada bloque valida primero
-- que la tabla exista y, si no, lo saltea (RAISE NOTICE) sin abortar.
-- El backfill queda pendiente para la proxima ejecucion (Flyway solo corre
-- migraciones nuevas, asi que se ejecuta en el siguiente arranque cuando
-- las otras tablas ya existen — o se corre a mano via psql).
-- =============================================================================

DO $$
DECLARE
    v_cuenta_cobros        BIGINT;
    v_cuenta_combustible   BIGINT;
    v_cuenta_mantenimiento BIGINT;
    v_cat_cobro_id         BIGINT;
    v_cat_combustible_id   BIGINT;
    v_cat_mantenimiento_id BIGINT;
    v_pagos_creados        INT := 0;
    v_comb_creados         INT := 0;
    v_mant_creados         INT := 0;
    v_has_config           BOOLEAN;
    v_has_pagos            BOOLEAN;
    v_has_facturas         BOOLEAN;
    v_has_combustible      BOOLEAN;
    v_has_mantenimientos   BOOLEAN;
    v_has_vehiculos        BOOLEAN;
BEGIN
    -- Existencia de tablas cross-schema (arranque paralelo de MSs).
    SELECT EXISTS(SELECT 1 FROM information_schema.tables
                  WHERE table_schema = 'auth_schema' AND table_name = 'configuracion_escuela')
      INTO v_has_config;
    SELECT EXISTS(SELECT 1 FROM information_schema.tables
                  WHERE table_schema = 'cobros_schema' AND table_name = 'pagos')
      INTO v_has_pagos;
    SELECT EXISTS(SELECT 1 FROM information_schema.tables
                  WHERE table_schema = 'cobros_schema' AND table_name = 'facturas')
      INTO v_has_facturas;
    SELECT EXISTS(SELECT 1 FROM information_schema.tables
                  WHERE table_schema = 'vehiculos_schema' AND table_name = 'registros_combustible')
      INTO v_has_combustible;
    SELECT EXISTS(SELECT 1 FROM information_schema.tables
                  WHERE table_schema = 'vehiculos_schema' AND table_name = 'mantenimientos')
      INTO v_has_mantenimientos;
    SELECT EXISTS(SELECT 1 FROM information_schema.tables
                  WHERE table_schema = 'vehiculos_schema' AND table_name = 'vehiculos')
      INTO v_has_vehiculos;

    -- 1) Leer cuentas default (solo si auth_schema.configuracion_escuela existe)
    IF v_has_config THEN
        SELECT cuenta_default_cobros_id,
               cuenta_default_combustible_id,
               cuenta_default_mantenimiento_id
          INTO v_cuenta_cobros, v_cuenta_combustible, v_cuenta_mantenimiento
          FROM auth_schema.configuracion_escuela
         LIMIT 1;
    ELSE
        RAISE NOTICE '[V6 backfill] auth_schema.configuracion_escuela aun no existe; se omite lectura de cuentas default.';
    END IF;

    -- 2) Leer las categorias de sistema (deben existir: seed en V3 misma migration chain)
    SELECT id INTO v_cat_cobro_id
      FROM contabilidad_schema.categorias_movimiento WHERE codigo = 'COBRO_ESTUDIANTE';
    SELECT id INTO v_cat_combustible_id
      FROM contabilidad_schema.categorias_movimiento WHERE codigo = 'COMBUSTIBLE';
    SELECT id INTO v_cat_mantenimiento_id
      FROM contabilidad_schema.categorias_movimiento WHERE codigo = 'MANTENIMIENTO_VEHICULO';

    IF v_cat_cobro_id IS NULL OR v_cat_combustible_id IS NULL OR v_cat_mantenimiento_id IS NULL THEN
        RAISE EXCEPTION 'Backfill V6 abortado: faltan categorias de sistema (COBRO_ESTUDIANTE / COMBUSTIBLE / MANTENIMIENTO_VEHICULO). Verifica seed V3.';
    END IF;

    -- ---------------------------------------------------------------------
    -- 3) BACKFILL PAGOS -> INGRESO
    -- Cuenta: la del pago si existe, sino la default de cobros.
    -- Solo pagos que no tengan ya un movimiento vinculado por pago_id.
    -- ---------------------------------------------------------------------
    IF v_has_pagos AND v_has_facturas THEN
        INSERT INTO contabilidad_schema.movimientos_contables (
            fecha, tipo, monto, cuenta_id, categoria_id,
            descripcion, referencia, pago_id, anulado, created_at
        )
        SELECT
            p.fecha_pago::date,
            'INGRESO',
            p.monto,
            COALESCE(p.cuenta_id, v_cuenta_cobros),
            v_cat_cobro_id,
            'Pago factura #' || COALESCE(f.numero_factura, p.factura_id::text)
                || ' (' || p.metodo_pago || ')' || ' [backfill]',
            f.numero_factura,
            p.id,
            FALSE,
            NOW()
        FROM cobros_schema.pagos p
        LEFT JOIN cobros_schema.facturas f ON f.id = p.factura_id
        WHERE COALESCE(p.cuenta_id, v_cuenta_cobros) IS NOT NULL
          AND NOT EXISTS (
              SELECT 1 FROM contabilidad_schema.movimientos_contables m
               WHERE m.pago_id = p.id
          );

        GET DIAGNOSTICS v_pagos_creados = ROW_COUNT;
    ELSE
        RAISE NOTICE '[V6 backfill] cobros_schema.pagos/facturas aun no existen; se omite backfill de pagos.';
    END IF;

    -- ---------------------------------------------------------------------
    -- 4) BACKFILL COMBUSTIBLE -> GASTO
    -- Requiere cuenta default de combustible; si no esta configurada, se
    -- salta el bloque completo.
    -- ---------------------------------------------------------------------
    IF v_cuenta_combustible IS NOT NULL AND v_has_combustible AND v_has_vehiculos THEN
        INSERT INTO contabilidad_schema.movimientos_contables (
            fecha, tipo, monto, cuenta_id, categoria_id,
            descripcion, referencia,
            registro_combustible_id, vehiculo_id, placa_vehiculo, kilometraje,
            anulado, created_at
        )
        SELECT
            rc.fecha::date,
            'GASTO',
            rc.costo_total,
            v_cuenta_combustible,
            v_cat_combustible_id,
            'Combustible: ' || rc.litros || ' L'
                || CASE WHEN rc.estacion IS NOT NULL AND rc.estacion <> ''
                        THEN ' en ' || rc.estacion ELSE '' END
                || ' [backfill]',
            v.placa,
            rc.id,
            v.id,
            v.placa,
            rc.kilometraje_actual,
            FALSE,
            NOW()
        FROM vehiculos_schema.registros_combustible rc
        JOIN vehiculos_schema.vehiculos v ON v.id = rc.vehiculo_id
        WHERE NOT EXISTS (
              SELECT 1 FROM contabilidad_schema.movimientos_contables m
               WHERE m.registro_combustible_id = rc.id
          );

        GET DIAGNOSTICS v_comb_creados = ROW_COUNT;
    ELSIF NOT v_has_combustible OR NOT v_has_vehiculos THEN
        RAISE NOTICE '[V6 backfill] vehiculos_schema.registros_combustible/vehiculos aun no existen; se omite backfill de combustible.';
    END IF;

    -- ---------------------------------------------------------------------
    -- 5) BACKFILL MANTENIMIENTO -> GASTO
    -- Solo mantenimientos activos (deleted_at IS NULL).
    -- ---------------------------------------------------------------------
    IF v_cuenta_mantenimiento IS NOT NULL AND v_has_mantenimientos AND v_has_vehiculos THEN
        INSERT INTO contabilidad_schema.movimientos_contables (
            fecha, tipo, monto, cuenta_id, categoria_id,
            descripcion, referencia,
            mantenimiento_id, vehiculo_id, placa_vehiculo, kilometraje,
            anulado, created_at
        )
        SELECT
            m.fecha,
            'GASTO',
            m.costo,
            v_cuenta_mantenimiento,
            v_cat_mantenimiento_id,
            m.tipo || ': ' || m.descripcion
                || CASE WHEN m.taller IS NOT NULL AND m.taller <> ''
                        THEN ' (' || m.taller || ')' ELSE '' END
                || ' [backfill]',
            v.placa,
            m.id,
            v.id,
            v.placa,
            m.kilometraje_servicio,
            FALSE,
            NOW()
        FROM vehiculos_schema.mantenimientos m
        JOIN vehiculos_schema.vehiculos v ON v.id = m.vehiculo_id
        WHERE m.deleted_at IS NULL
          AND NOT EXISTS (
              SELECT 1 FROM contabilidad_schema.movimientos_contables mc
               WHERE mc.mantenimiento_id = m.id
          );

        GET DIAGNOSTICS v_mant_creados = ROW_COUNT;
    ELSIF NOT v_has_mantenimientos OR NOT v_has_vehiculos THEN
        RAISE NOTICE '[V6 backfill] vehiculos_schema.mantenimientos/vehiculos aun no existen; se omite backfill de mantenimiento.';
    END IF;

    RAISE NOTICE '[V6 backfill] Movimientos creados -- pagos: %, combustible: %, mantenimiento: %',
        v_pagos_creados, v_comb_creados, v_mant_creados;

    IF v_cuenta_cobros IS NULL THEN
        RAISE NOTICE '[V6 backfill] AVISO: no hay cuenta_default_cobros_id configurada. Se saltearon pagos sin cuenta_id explicita.';
    END IF;
    IF v_cuenta_combustible IS NULL THEN
        RAISE NOTICE '[V6 backfill] AVISO: no hay cuenta_default_combustible_id configurada. NO se creo backfill de combustible.';
    END IF;
    IF v_cuenta_mantenimiento IS NULL THEN
        RAISE NOTICE '[V6 backfill] AVISO: no hay cuenta_default_mantenimiento_id configurada. NO se creo backfill de mantenimiento.';
    END IF;
END $$;
