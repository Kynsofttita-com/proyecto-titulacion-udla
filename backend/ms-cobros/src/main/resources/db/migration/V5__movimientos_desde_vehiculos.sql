-- =============================================================================
-- V5__movimientos_desde_vehiculos.sql - MS-Cobros
-- Vincula movimientos contables con registros de combustible y mantenimiento
-- de ms-vehiculos, para permitir origen "Vehiculo" en los movimientos y
-- sincronizacion CRUD desde ms-vehiculos.
--
-- Sin FK cross-MS (registros_combustible / mantenimientos viven en el schema
-- vehiculos_schema y son propiedad de ms-vehiculos). La consistencia se maneja
-- por app: al eliminar un registro en Vehiculos, ms-vehiculos anula el
-- movimiento asociado via Feign.
-- =============================================================================

ALTER TABLE contabilidad_schema.movimientos_contables
    ADD COLUMN registro_combustible_id  BIGINT,
    ADD COLUMN mantenimiento_id         BIGINT,
    ADD COLUMN vehiculo_id              BIGINT,
    ADD COLUMN placa_vehiculo           VARCHAR(20),
    ADD COLUMN kilometraje              INTEGER;

COMMENT ON COLUMN contabilidad_schema.movimientos_contables.registro_combustible_id IS
    'ID del registro de combustible que genero este movimiento (vehiculos_schema.registros_combustible). NULL si no aplica.';
COMMENT ON COLUMN contabilidad_schema.movimientos_contables.mantenimiento_id IS
    'ID del mantenimiento que genero este movimiento (vehiculos_schema.mantenimientos). NULL si no aplica.';
COMMENT ON COLUMN contabilidad_schema.movimientos_contables.vehiculo_id IS
    'Vehiculo al que corresponde el gasto (redundante con la referencia pero indexable). NULL si no aplica.';
COMMENT ON COLUMN contabilidad_schema.movimientos_contables.placa_vehiculo IS
    'Placa denormalizada al momento del registro. Se muestra en la tabla de Gastos como origen.';
COMMENT ON COLUMN contabilidad_schema.movimientos_contables.kilometraje IS
    'Kilometraje del vehiculo al momento del registro. Solo para gastos vinculados a vehiculo.';

-- Consultas frecuentes: buscar movimiento por registro externo, listar gastos de un vehiculo.
CREATE UNIQUE INDEX ux_mov_registro_combustible ON contabilidad_schema.movimientos_contables (registro_combustible_id)
    WHERE registro_combustible_id IS NOT NULL AND anulado = FALSE;
CREATE UNIQUE INDEX ux_mov_mantenimiento ON contabilidad_schema.movimientos_contables (mantenimiento_id)
    WHERE mantenimiento_id IS NOT NULL AND anulado = FALSE;
CREATE INDEX ix_mov_vehiculo ON contabilidad_schema.movimientos_contables (vehiculo_id)
    WHERE vehiculo_id IS NOT NULL;

-- Integridad: un movimiento puede venir de UN solo origen externo a la vez.
ALTER TABLE contabilidad_schema.movimientos_contables
    ADD CONSTRAINT ck_mov_origen_unico CHECK (
        (CASE WHEN pago_id                 IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN registro_combustible_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN mantenimiento_id        IS NOT NULL THEN 1 ELSE 0 END) <= 1
    );
