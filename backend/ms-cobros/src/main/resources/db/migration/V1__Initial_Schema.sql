-- =============================================================================
-- V1__Initial_Schema.sql - MS-Cobros
-- 3 tablas en cobros_schema (facturas con generated column saldo, pagos sin
-- soft delete, reconciliacion diaria)
-- Ver docs/database/schema.md sección 10
-- =============================================================================

-- -----------------------------------------------------------------------------
-- cobros_schema.facturas (con optimistic locking + generated column)
-- -----------------------------------------------------------------------------
CREATE TABLE cobros_schema.facturas (
    id                       BIGSERIAL    PRIMARY KEY,
    numero_factura           VARCHAR(20)  NOT NULL,
    estudiante_id            BIGINT       NOT NULL,                  -- ref estudiantes_schema.estudiantes
    concepto_facturacion_id  BIGINT       NOT NULL,                  -- ref auth_schema.conceptos_facturacion
    descripcion              VARCHAR(255),
    monto_original           NUMERIC(10,2) NOT NULL,
    monto_pagado             NUMERIC(10,2) NOT NULL DEFAULT 0,
    saldo                    NUMERIC(10,2) GENERATED ALWAYS AS (monto_original - monto_pagado) STORED,
    estado                   VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_emision            DATE         NOT NULL DEFAULT CURRENT_DATE,
    fecha_vencimiento        DATE         NOT NULL,
    motivo_anulacion         TEXT,
    version                  BIGINT       NOT NULL DEFAULT 0,        -- optimistic locking
    created_at               TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP,
    created_by               VARCHAR(50),
    updated_by               VARCHAR(50),
    deleted_at               TIMESTAMP,

    CONSTRAINT uq_facturas_numero       UNIQUE (numero_factura),
    CONSTRAINT ck_facturas_monto_orig   CHECK (monto_original > 0),
    CONSTRAINT ck_facturas_monto_pag    CHECK (monto_pagado >= 0 AND monto_pagado <= monto_original),
    CONSTRAINT ck_facturas_estado       CHECK (estado IN ('PENDIENTE', 'PARCIAL', 'PAGADA', 'VENCIDA', 'ANULADA'))
);

CREATE INDEX idx_facturas_numero               ON cobros_schema.facturas (numero_factura);
CREATE INDEX idx_facturas_estudiante           ON cobros_schema.facturas (estudiante_id);
CREATE INDEX idx_facturas_estado_vencimiento   ON cobros_schema.facturas (estado, fecha_vencimiento) WHERE deleted_at IS NULL;

COMMENT ON TABLE cobros_schema.facturas IS 'Facturas con saldo calculado automaticamente y soporte de pagos parciales';

-- -----------------------------------------------------------------------------
-- cobros_schema.pagos (NO tiene deleted_at - los pagos NO se borran)
-- -----------------------------------------------------------------------------
CREATE TABLE cobros_schema.pagos (
    id                     BIGSERIAL    PRIMARY KEY,
    factura_id             BIGINT       NOT NULL,
    monto                  NUMERIC(10,2) NOT NULL,
    fecha_pago             TIMESTAMP    NOT NULL DEFAULT NOW(),
    metodo_pago            VARCHAR(20)  NOT NULL,
    referencia_transaccion VARCHAR(100),
    observaciones          TEXT,
    usuario_registro_id    BIGINT       NOT NULL,                    -- ref auth_schema.usuarios
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by             VARCHAR(50),

    CONSTRAINT fk_pagos_factura      FOREIGN KEY (factura_id) REFERENCES cobros_schema.facturas (id),
    CONSTRAINT ck_pagos_monto        CHECK (monto > 0),
    CONSTRAINT ck_pagos_metodo       CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'CHEQUE'))
);

CREATE INDEX idx_pagos_factura ON cobros_schema.pagos (factura_id);
CREATE INDEX idx_pagos_fecha   ON cobros_schema.pagos (fecha_pago);

COMMENT ON TABLE cobros_schema.pagos IS 'Pagos registrados (append-only para auditoria)';

-- -----------------------------------------------------------------------------
-- cobros_schema.reconciliacion (1 fila por dia, total calculado)
-- -----------------------------------------------------------------------------
CREATE TABLE cobros_schema.reconciliacion (
    id                     BIGSERIAL    PRIMARY KEY,
    fecha                  DATE         NOT NULL,
    total_efectivo         NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_tarjeta          NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_transferencia    NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_cheque           NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_dia              NUMERIC(10,2) GENERATED ALWAYS AS (total_efectivo + total_tarjeta + total_transferencia + total_cheque) STORED,
    cantidad_pagos         INTEGER      NOT NULL DEFAULT 0,
    usuario_conciliador_id BIGINT,
    observaciones          TEXT,
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP,
    created_by             VARCHAR(50),
    updated_by             VARCHAR(50),

    CONSTRAINT uq_reconciliacion_fecha    UNIQUE (fecha),
    CONSTRAINT ck_reconciliacion_efectivo CHECK (total_efectivo >= 0),
    CONSTRAINT ck_reconciliacion_tarjeta  CHECK (total_tarjeta >= 0),
    CONSTRAINT ck_reconciliacion_transfer CHECK (total_transferencia >= 0),
    CONSTRAINT ck_reconciliacion_cheque   CHECK (total_cheque >= 0),
    CONSTRAINT ck_reconciliacion_pagos    CHECK (cantidad_pagos >= 0)
);
