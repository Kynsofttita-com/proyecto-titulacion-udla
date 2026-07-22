-- =============================================================================
-- V4__seed_cuenta_cobros_escuela.sql - MS-Cobros
-- Seed idempotente de una cuenta EFECTIVO "Cobros Escuela" pensada para ser
-- la cuenta default de ingresos por cobros (se configura en /configuracion
-- de ms-auth). Coexiste con la cuenta "Caja" seedeada en V3.
--
-- Idempotente: NO inserta si ya existe una cuenta con el mismo nombre
-- (comparacion case-insensitive), evitando romper la migracion en reruns o
-- entornos donde ya la crearon manualmente.
-- =============================================================================

INSERT INTO contabilidad_schema.cuentas
    (nombre, tipo, saldo_inicial, activo, observaciones)
SELECT
    'Cobros Escuela', 'EFECTIVO', 0.00, TRUE,
    'Cuenta default para ingresos de cobros de estudiantes. Puede configurarse en Configuracion > Contabilidad.'
WHERE NOT EXISTS (
    SELECT 1 FROM contabilidad_schema.cuentas
    WHERE LOWER(nombre) = LOWER('Cobros Escuela')
);
