-- =============================================================================
-- V3__contabilidad.sql - MS-Cobros
-- Modulo de contabilidad: cuentas + categorias + movimientos.
-- Los pagos de cobros ahora se vinculan a una cuenta y generan un movimiento
-- contable automatico de tipo INGRESO.
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS contabilidad_schema;

-- -----------------------------------------------------------------------------
-- 1) contabilidad_schema.cuentas
-- Donde vive el dinero: caja, bancos, tarjetas.
-- -----------------------------------------------------------------------------
CREATE TABLE contabilidad_schema.cuentas (
    id              BIGSERIAL     PRIMARY KEY,
    nombre          VARCHAR(80)   NOT NULL,
    tipo            VARCHAR(20)   NOT NULL,
    numero_cuenta   VARCHAR(60),                        -- requerido si tipo=BANCO/TARJETA
    saldo_inicial   NUMERIC(12,2) NOT NULL DEFAULT 0,
    activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    observaciones   TEXT,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    CONSTRAINT ck_cuentas_tipo CHECK (tipo IN ('EFECTIVO','BANCO','TARJETA'))
);

CREATE UNIQUE INDEX ux_cuentas_nombre ON contabilidad_schema.cuentas (LOWER(nombre));
CREATE INDEX ix_cuentas_activo ON contabilidad_schema.cuentas (activo);

-- -----------------------------------------------------------------------------
-- 2) contabilidad_schema.categorias_movimiento
-- Clasificacion de ingresos y gastos.
-- es_sistema=true marca las categorias predefinidas (no borrables).
-- -----------------------------------------------------------------------------
CREATE TABLE contabilidad_schema.categorias_movimiento (
    id           BIGSERIAL    PRIMARY KEY,
    codigo       VARCHAR(40)  NOT NULL,
    nombre       VARCHAR(80)  NOT NULL,
    tipo         VARCHAR(10)  NOT NULL,
    es_sistema   BOOLEAN      NOT NULL DEFAULT FALSE,
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    CONSTRAINT ck_categorias_tipo CHECK (tipo IN ('INGRESO','GASTO'))
);

CREATE UNIQUE INDEX ux_categorias_codigo ON contabilidad_schema.categorias_movimiento (codigo);
CREATE INDEX ix_categorias_tipo ON contabilidad_schema.categorias_movimiento (tipo, activo);

-- -----------------------------------------------------------------------------
-- 3) contabilidad_schema.movimientos_contables
-- Registro real de cada ingreso o gasto.
-- -----------------------------------------------------------------------------
CREATE TABLE contabilidad_schema.movimientos_contables (
    id             BIGSERIAL     PRIMARY KEY,
    fecha          DATE          NOT NULL,
    tipo           VARCHAR(10)   NOT NULL,
    monto          NUMERIC(12,2) NOT NULL,
    cuenta_id      BIGINT        NOT NULL,
    categoria_id   BIGINT        NOT NULL,
    descripcion    VARCHAR(255),
    referencia     VARCHAR(80),                          -- nro factura proveedor, comprobante, etc.
    pago_id        BIGINT,                               -- FK opcional al pago origen (cuando es auto-generado)
    anulado        BOOLEAN       NOT NULL DEFAULT FALSE,
    motivo_anulacion TEXT,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP,
    created_by     VARCHAR(50),
    updated_by     VARCHAR(50),
    CONSTRAINT ck_mov_tipo CHECK (tipo IN ('INGRESO','GASTO')),
    CONSTRAINT ck_mov_monto_positivo CHECK (monto > 0),
    CONSTRAINT fk_mov_cuenta FOREIGN KEY (cuenta_id)
        REFERENCES contabilidad_schema.cuentas (id),
    CONSTRAINT fk_mov_categoria FOREIGN KEY (categoria_id)
        REFERENCES contabilidad_schema.categorias_movimiento (id),
    CONSTRAINT fk_mov_pago FOREIGN KEY (pago_id)
        REFERENCES cobros_schema.pagos (id) ON DELETE SET NULL
);

CREATE INDEX ix_mov_cuenta_fecha ON contabilidad_schema.movimientos_contables (cuenta_id, fecha);
CREATE INDEX ix_mov_categoria ON contabilidad_schema.movimientos_contables (categoria_id);
CREATE INDEX ix_mov_pago ON contabilidad_schema.movimientos_contables (pago_id);
CREATE INDEX ix_mov_fecha ON contabilidad_schema.movimientos_contables (fecha);

-- -----------------------------------------------------------------------------
-- 4) Modificacion tabla cobros_schema.pagos
-- Agregar cuenta_id nullable (nullable para no romper pagos historicos).
-- Los pagos nuevos deben traer cuenta_id.
-- -----------------------------------------------------------------------------
ALTER TABLE cobros_schema.pagos
    ADD COLUMN cuenta_id BIGINT;

ALTER TABLE cobros_schema.pagos
    ADD CONSTRAINT fk_pagos_cuenta FOREIGN KEY (cuenta_id)
        REFERENCES contabilidad_schema.cuentas (id);

CREATE INDEX ix_pagos_cuenta ON cobros_schema.pagos (cuenta_id);

-- -----------------------------------------------------------------------------
-- 5) Seeds: categorias predefinidas
-- -----------------------------------------------------------------------------
INSERT INTO contabilidad_schema.categorias_movimiento (codigo, nombre, tipo, es_sistema)
VALUES
    -- INGRESOS
    ('COBRO_ESTUDIANTE',        'Cobro de estudiante',                  'INGRESO', TRUE),
    ('OTROS_INGRESO',           'Otros ingresos',                       'INGRESO', TRUE),
    -- GASTOS
    ('COMBUSTIBLE',             'Combustible',                          'GASTO',   TRUE),
    ('MANTENIMIENTO_VEHICULO',  'Mantenimiento vehiculos',              'GASTO',   TRUE),
    ('SEGURO_MATRICULA',        'Seguros / SOAT / RTV',                 'GASTO',   TRUE),
    ('SUELDO_INSTRUCTOR',       'Sueldos instructores',                 'GASTO',   TRUE),
    ('SUELDO_ADMINISTRATIVO',   'Sueldos administrativos',              'GASTO',   TRUE),
    ('ALQUILER',                'Alquiler',                             'GASTO',   TRUE),
    ('SERVICIOS_BASICOS',       'Servicios (luz / agua / internet)',    'GASTO',   TRUE),
    ('PUBLICIDAD',              'Publicidad',                           'GASTO',   TRUE),
    ('OTROS_GASTO',             'Otros gastos',                         'GASTO',   TRUE);

-- -----------------------------------------------------------------------------
-- 6) Seed: cuenta CAJA por defecto (para poder registrar movimientos desde el
-- inicio sin datos falsos; el admin la puede editar o crear mas cuentas).
-- -----------------------------------------------------------------------------
INSERT INTO contabilidad_schema.cuentas (nombre, tipo, saldo_inicial, activo, observaciones)
VALUES ('Caja', 'EFECTIVO', 0.00, TRUE, 'Cuenta de efectivo por defecto. Editable desde la seccion Saldo.');
