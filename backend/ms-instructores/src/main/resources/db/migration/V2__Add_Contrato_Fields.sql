-- =============================================================================
-- V2__Add_Contrato_Fields.sql - MS-Instructores
-- Agrega informacion de contrato (tipo, horas semanales, tarifa hora).
-- =============================================================================

ALTER TABLE instructores_schema.instructores
    ADD COLUMN tipo_contrato            VARCHAR(20)   NOT NULL DEFAULT 'TIEMPO_COMPLETO',
    ADD COLUMN horas_contrato_semanales SMALLINT      NOT NULL DEFAULT 40,
    ADD COLUMN tarifa_hora              NUMERIC(8,2);

ALTER TABLE instructores_schema.instructores
    ADD CONSTRAINT ck_instructores_tipo_contrato
        CHECK (tipo_contrato IN ('TIEMPO_COMPLETO', 'MEDIO_TIEMPO', 'POR_HORAS'));

ALTER TABLE instructores_schema.instructores
    ADD CONSTRAINT ck_instructores_horas_contrato
        CHECK (horas_contrato_semanales > 0 AND horas_contrato_semanales <= 60);

ALTER TABLE instructores_schema.instructores
    ADD CONSTRAINT ck_instructores_tarifa_hora
        CHECK (tarifa_hora IS NULL OR tarifa_hora > 0);

-- Si el contrato es POR_HORAS, la tarifa_hora es obligatoria
ALTER TABLE instructores_schema.instructores
    ADD CONSTRAINT ck_instructores_tarifa_obligatoria_por_horas
        CHECK (tipo_contrato <> 'POR_HORAS' OR tarifa_hora IS NOT NULL);

CREATE INDEX idx_instructores_tipo_contrato
    ON instructores_schema.instructores (tipo_contrato)
    WHERE deleted_at IS NULL;

COMMENT ON COLUMN instructores_schema.instructores.tipo_contrato IS
    'TIEMPO_COMPLETO | MEDIO_TIEMPO | POR_HORAS';
COMMENT ON COLUMN instructores_schema.instructores.horas_contrato_semanales IS
    'TC=40 (def), MT=20, POR_HORAS=maximo permitido por semana';
COMMENT ON COLUMN instructores_schema.instructores.tarifa_hora IS
    'Pago por hora en USD. Obligatorio para POR_HORAS; opcional para TC/MT (horas extra)';
