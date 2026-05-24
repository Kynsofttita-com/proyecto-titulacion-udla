-- =============================================================================
-- V2__credito_y_cuotas.sql - MS-Cobros
-- Soporte de pago a crédito dentro de la misma factura:
--   - Campos nuevos en facturas (tipo_pago, numero_cuotas, frecuencia, etc.)
--   - Tabla factura_cuotas (1 fila por cuota) como fuente de verdad
--   - Campo numero_cuota en pagos (vincula pago ↔ cuota)
-- Estados de factura ampliados: añade 'PAGADO' (estado existente histórico) y
-- mantiene compatibilidad con los estados previos.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1) Ampliar tabla facturas
-- -----------------------------------------------------------------------------
ALTER TABLE cobros_schema.facturas
    ADD COLUMN tipo_pago             VARCHAR(20)  NOT NULL DEFAULT 'CONTADO',
    ADD COLUMN numero_cuotas         INT          NOT NULL DEFAULT 1,
    ADD COLUMN cuotas_pagadas        INT          NOT NULL DEFAULT 0,
    ADD COLUMN frecuencia_cuota      VARCHAR(20),
    ADD COLUMN fecha_primera_cuota   DATE,
    ADD COLUMN valor_cuota           NUMERIC(10,2);

ALTER TABLE cobros_schema.facturas
    ADD CONSTRAINT ck_facturas_tipo_pago
        CHECK (tipo_pago IN ('CONTADO', 'CREDITO'));

ALTER TABLE cobros_schema.facturas
    ADD CONSTRAINT ck_facturas_numero_cuotas
        CHECK (numero_cuotas >= 1 AND numero_cuotas <= 24);

ALTER TABLE cobros_schema.facturas
    ADD CONSTRAINT ck_facturas_cuotas_pagadas
        CHECK (cuotas_pagadas >= 0 AND cuotas_pagadas <= numero_cuotas);

ALTER TABLE cobros_schema.facturas
    ADD CONSTRAINT ck_facturas_frecuencia_cuota
        CHECK (frecuencia_cuota IS NULL OR frecuencia_cuota IN ('MENSUAL', 'QUINCENAL', 'SEMANAL'));

-- Cuando es CONTADO: numero_cuotas debe ser 1 y frecuencia debe ser NULL.
-- Cuando es CREDITO: numero_cuotas >= 2 y frecuencia + fecha_primera_cuota NOT NULL.
ALTER TABLE cobros_schema.facturas
    ADD CONSTRAINT ck_facturas_credito_consistente
        CHECK (
            (tipo_pago = 'CONTADO'  AND numero_cuotas = 1)
         OR (tipo_pago = 'CREDITO'  AND numero_cuotas >= 2
                                    AND frecuencia_cuota IS NOT NULL
                                    AND fecha_primera_cuota IS NOT NULL)
        );

-- Ampliar el CHECK de estado para incluir el legado 'PAGADO' que usa el código
-- y el nuevo 'PAGADA' (consistente con doc). Mantengo ambos por compatibilidad.
ALTER TABLE cobros_schema.facturas DROP CONSTRAINT ck_facturas_estado;
ALTER TABLE cobros_schema.facturas
    ADD CONSTRAINT ck_facturas_estado
        CHECK (estado IN ('PENDIENTE', 'PARCIAL', 'PAGADA', 'PAGADO', 'VENCIDA', 'ANULADA'));

COMMENT ON COLUMN cobros_schema.facturas.tipo_pago        IS 'CONTADO o CREDITO. En crédito, las cuotas viven en factura_cuotas.';
COMMENT ON COLUMN cobros_schema.facturas.numero_cuotas    IS 'Cantidad de cuotas (1 si CONTADO, 2..24 si CREDITO).';
COMMENT ON COLUMN cobros_schema.facturas.cuotas_pagadas   IS 'Cuotas saldadas. Mantenido por trigger lógico desde PagoService.';
COMMENT ON COLUMN cobros_schema.facturas.frecuencia_cuota IS 'MENSUAL, QUINCENAL o SEMANAL. NULL si CONTADO.';
COMMENT ON COLUMN cobros_schema.facturas.valor_cuota      IS 'monto_original / numero_cuotas. Persistido para evitar recálculo.';


-- -----------------------------------------------------------------------------
-- 2) Tabla factura_cuotas (1 fila por cuota, fuente de verdad para crédito)
-- -----------------------------------------------------------------------------
CREATE TABLE cobros_schema.factura_cuotas (
    id                  BIGSERIAL    PRIMARY KEY,
    factura_id          BIGINT       NOT NULL,
    numero_cuota        INT          NOT NULL,
    monto               NUMERIC(10,2) NOT NULL,
    monto_pagado        NUMERIC(10,2) NOT NULL DEFAULT 0,
    saldo               NUMERIC(10,2) GENERATED ALWAYS AS (monto - monto_pagado) STORED,
    fecha_vencimiento   DATE         NOT NULL,
    fecha_pago_completa TIMESTAMP,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,

    CONSTRAINT fk_factura_cuotas_factura
        FOREIGN KEY (factura_id) REFERENCES cobros_schema.facturas (id) ON DELETE CASCADE,
    CONSTRAINT uq_factura_cuotas_numero
        UNIQUE (factura_id, numero_cuota),
    CONSTRAINT ck_factura_cuotas_numero
        CHECK (numero_cuota >= 1),
    CONSTRAINT ck_factura_cuotas_monto
        CHECK (monto > 0),
    CONSTRAINT ck_factura_cuotas_pagado
        CHECK (monto_pagado >= 0 AND monto_pagado <= monto),
    CONSTRAINT ck_factura_cuotas_estado
        CHECK (estado IN ('PENDIENTE', 'PARCIAL', 'PAGADA', 'VENCIDA'))
);

CREATE INDEX idx_factura_cuotas_factura       ON cobros_schema.factura_cuotas (factura_id);
CREATE INDEX idx_factura_cuotas_estado_venc   ON cobros_schema.factura_cuotas (estado, fecha_vencimiento);
CREATE INDEX idx_factura_cuotas_pendientes    ON cobros_schema.factura_cuotas (factura_id, numero_cuota)
    WHERE estado IN ('PENDIENTE', 'PARCIAL');

COMMENT ON TABLE  cobros_schema.factura_cuotas IS 'Cuotas de una factura a crédito. Solo se pueblan cuando facturas.tipo_pago=CREDITO.';
COMMENT ON COLUMN cobros_schema.factura_cuotas.numero_cuota IS 'Orden de la cuota (1..numero_cuotas).';
COMMENT ON COLUMN cobros_schema.factura_cuotas.estado       IS 'PENDIENTE, PARCIAL, PAGADA o VENCIDA (job programado puede marcarla VENCIDA).';


-- -----------------------------------------------------------------------------
-- 3) Vincular pago ↔ cuota
-- -----------------------------------------------------------------------------
ALTER TABLE cobros_schema.pagos
    ADD COLUMN numero_cuota INT,
    ADD COLUMN factura_cuota_id BIGINT;

ALTER TABLE cobros_schema.pagos
    ADD CONSTRAINT fk_pagos_factura_cuota
        FOREIGN KEY (factura_cuota_id) REFERENCES cobros_schema.factura_cuotas (id);

CREATE INDEX idx_pagos_factura_cuota ON cobros_schema.pagos (factura_cuota_id);

COMMENT ON COLUMN cobros_schema.pagos.numero_cuota     IS 'Numero de la cuota que cubre este pago (NULL si CONTADO o pago libre).';
COMMENT ON COLUMN cobros_schema.pagos.factura_cuota_id IS 'FK a factura_cuotas (cuando aplica a una cuota especifica).';


-- -----------------------------------------------------------------------------
-- 4) Backfill: las facturas pre-existentes son CONTADO con valor_cuota=monto_original
-- -----------------------------------------------------------------------------
UPDATE cobros_schema.facturas
   SET valor_cuota = monto_original
 WHERE valor_cuota IS NULL;
