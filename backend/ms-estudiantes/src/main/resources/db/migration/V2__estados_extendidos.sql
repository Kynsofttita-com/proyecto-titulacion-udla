-- =============================================================================
-- V2__estados_extendidos.sql - MS-Estudiantes
-- Estados academicos extendidos + situacion_pago derivada de cobros.
--
-- Estados (transiciones tipicas):
--   PRE_MATRICULADO ──pago──► MATRICULADO ──primera asignacion──► CURSANDO
--                                                                    │
--                                                       horas completadas
--                                                                    ▼
--                                                              COMPLETADO
--                          (cualquier estado puede pasar a RETIRADO manualmente)
--
-- situacion_pago se sincroniza desde MS-Cobros via evento pago.registrado.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1) Quitar el CHECK actual ANTES de hacer el UPDATE (porque MATRICULADO aun
--    no esta permitido por el CHECK viejo).
-- -----------------------------------------------------------------------------
ALTER TABLE estudiantes_schema.estudiantes DROP CONSTRAINT ck_estudiantes_estado;

-- -----------------------------------------------------------------------------
-- 2) Migrar valores existentes: ACTIVO → MATRICULADO.
-- -----------------------------------------------------------------------------
UPDATE estudiantes_schema.estudiantes
   SET estado = 'MATRICULADO'
 WHERE estado = 'ACTIVO';

-- -----------------------------------------------------------------------------
-- 3) Reañadir CHECK con los estados nuevos.
-- -----------------------------------------------------------------------------
ALTER TABLE estudiantes_schema.estudiantes
    ADD CONSTRAINT ck_estudiantes_estado
        CHECK (estado IN ('PRE_MATRICULADO', 'MATRICULADO', 'CURSANDO', 'COMPLETADO', 'RETIRADO'));

-- -----------------------------------------------------------------------------
-- 4) Agregar columna situacion_pago (estado financiero derivado)
-- -----------------------------------------------------------------------------
ALTER TABLE estudiantes_schema.estudiantes
    ADD COLUMN situacion_pago VARCHAR(20) NOT NULL DEFAULT 'SIN_DEUDA';

ALTER TABLE estudiantes_schema.estudiantes
    ADD CONSTRAINT ck_estudiantes_situacion_pago
        CHECK (situacion_pago IN ('SIN_DEUDA', 'PAGO_PARCIAL', 'AL_DIA', 'EN_MORA', 'PAGADO_TOTAL'));

CREATE INDEX idx_estudiantes_situacion_pago
    ON estudiantes_schema.estudiantes (situacion_pago)
 WHERE deleted_at IS NULL;

COMMENT ON COLUMN estudiantes_schema.estudiantes.estado IS
'Estado academico: PRE_MATRICULADO (registrado sin pagar), MATRICULADO (pago inicial), '
'CURSANDO (clases activas), COMPLETADO (curso terminado), RETIRADO (abandono).';

COMMENT ON COLUMN estudiantes_schema.estudiantes.situacion_pago IS
'Estado financiero derivado, sincronizado desde MS-Cobros via evento pago.registrado. '
'SIN_DEUDA (sin facturas), PAGO_PARCIAL (con saldo), AL_DIA (credito sin cuotas vencidas), '
'EN_MORA (cuota vencida), PAGADO_TOTAL (todas las facturas pagadas).';
