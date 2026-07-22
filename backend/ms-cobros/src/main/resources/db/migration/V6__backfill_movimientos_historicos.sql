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
BEGIN
    -- 1) Leer las cuentas default configuradas (single-tenant → una fila)
    SELECT cuenta_default_cobros_id,
           cuenta_default_combustible_id,
           cuenta_default_mantenimiento_id
      INTO v_cuenta_cobros, v_cuenta_combustible, v_cuenta_mantenimiento
      FROM auth_schema.configuracion_escuela
     LIMIT 1;

    -- 2) Leer las categorias de sistema
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
    -- 3) BACKFILL PAGOS → INGRESO
    -- Cuenta: la del pago si existe, sino la default de cobros.
    -- Solo pagos que no tengan ya un movimiento vinculado por pago_id.
    -- ---------------------------------------------------------------------
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

    -- ---------------------------------------------------------------------
    -- 4) BACKFILL COMBUSTIBLE → GASTO
    -- Requiere cuenta default de combustible; si no esta configurada, se
    -- salta el bloque completo.
    -- ---------------------------------------------------------------------
    IF v_cuenta_combustible IS NOT NULL THEN
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
    END IF;

    -- ---------------------------------------------------------------------
    -- 5) BACKFILL MANTENIMIENTO → GASTO
    -- Solo mantenimientos activos (deleted_at IS NULL).
    -- ---------------------------------------------------------------------
    IF v_cuenta_mantenimiento IS NOT NULL THEN
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
    END IF;

    RAISE NOTICE '[V6 backfill] Movimientos creados — pagos: %, combustible: %, mantenimiento: %',
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
