-- =============================================================================
-- V9: Cuentas contables por defecto en configuracion_escuela
-- =============================================================================
-- Permite al admin fijar a que cuenta contable van, por default:
--   1) Los cobros de estudiantes (ingresos)
--   2) Los gastos de combustible generados desde Vehiculos
--   3) Los gastos de mantenimiento generados desde Vehiculos
--
-- Son opcionales (NULL = no configurado; el flujo pide/exige cuenta manual).
-- Las cuentas viven en contabilidad_schema.cuentas (ms-cobros) por lo que NO
-- se puede poner FK cross-MS; la validacion se hace al momento de usar.
-- =============================================================================

ALTER TABLE auth_schema.configuracion_escuela
    ADD COLUMN cuenta_default_cobros_id         BIGINT,
    ADD COLUMN cuenta_default_combustible_id    BIGINT,
    ADD COLUMN cuenta_default_mantenimiento_id  BIGINT;

COMMENT ON COLUMN auth_schema.configuracion_escuela.cuenta_default_cobros_id IS
    'Cuenta contable (contabilidad_schema.cuentas) donde se registran por default los cobros de estudiantes. NULL = requerir cuenta explicita en cada pago.';
COMMENT ON COLUMN auth_schema.configuracion_escuela.cuenta_default_combustible_id IS
    'Cuenta contable donde se registran por default los gastos de combustible generados desde Vehiculos.';
COMMENT ON COLUMN auth_schema.configuracion_escuela.cuenta_default_mantenimiento_id IS
    'Cuenta contable donde se registran por default los gastos de mantenimiento generados desde Vehiculos.';
