-- =============================================================================
-- V3__pendiente_matricula.sql - MS-Estudiantes
-- Agrega el valor 'PENDIENTE_MATRICULA' a situacion_pago para distinguir:
--   - "Sin facturas porque ya pago todo" (SIN_DEUDA / PAGADO_TOTAL)
--   - "Sin facturas porque aun no se le emitio matricula" (PENDIENTE_MATRICULA)
--
-- Se aplica retroactivamente a los estudiantes PRE_MATRICULADO sin facturas
-- (asumiendo que estaban en SIN_DEUDA solo por falta de emision).
-- =============================================================================

-- 1) Quitar CHECK para poder hacer UPDATE
ALTER TABLE estudiantes_schema.estudiantes DROP CONSTRAINT ck_estudiantes_situacion_pago;

-- 2) Backfill: PRE_MATRICULADO + SIN_DEUDA → PENDIENTE_MATRICULA
-- (no podemos saber desde aqui si tiene facturas porque cobros vive en otro schema;
--  asumimos que si esta en PRE_MATRICULADO no le emitieron aun. El sync masivo
--  posterior corrige cualquier caso atipico via Feign a ms-cobros.)
UPDATE estudiantes_schema.estudiantes
   SET situacion_pago = 'PENDIENTE_MATRICULA'
 WHERE estado = 'PRE_MATRICULADO'
   AND situacion_pago = 'SIN_DEUDA';

-- 3) Reañadir CHECK con el valor nuevo
ALTER TABLE estudiantes_schema.estudiantes
    ADD CONSTRAINT ck_estudiantes_situacion_pago
        CHECK (situacion_pago IN (
            'SIN_DEUDA',
            'PENDIENTE_MATRICULA',
            'PAGO_PARCIAL',
            'AL_DIA',
            'EN_MORA',
            'PAGADO_TOTAL'
        ));

-- 4) Cambiar el DEFAULT para nuevos estudiantes (siempre arrancan en PRE_MATRICULADO
--    sin facturas, asi que su situacion natural es PENDIENTE_MATRICULA).
ALTER TABLE estudiantes_schema.estudiantes
    ALTER COLUMN situacion_pago SET DEFAULT 'PENDIENTE_MATRICULA';

COMMENT ON COLUMN estudiantes_schema.estudiantes.situacion_pago IS
'Estado financiero. PENDIENTE_MATRICULA = registrado sin factura aun (cobrarle). '
'SIN_DEUDA = sin facturas porque ya pago todo (raro fuera de COMPLETADO). '
'PAGO_PARCIAL = factura con saldo. AL_DIA = solo credito sin vencer. '
'EN_MORA = factura/cuota vencida sin pagar. PAGADO_TOTAL = todo saldado.';
