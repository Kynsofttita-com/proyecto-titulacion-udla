-- =============================================================================
-- V4__situacion_pago_simplificada.sql - MS-Estudiantes
-- =============================================================================
-- Simplifica el modelo de situacion_pago a 4 valores claros:
--
--   PENDIENTE_FACTURACION  No tiene factura emitida.
--   PENDIENTE_PAGO         Factura CONTADO emitida, $0 pagado.
--   PAGO_PARCIAL           Factura CONTADO con algo pagado, hay saldo.
--   PAGADO_TOTAL           Todo saldado, o factura CREDITO emitida
--                          (asume cobro automatico por tarjeta).
--
-- Cambios vs V3:
--   - Renombra PENDIENTE_MATRICULA -> PENDIENTE_FACTURACION (mas claro).
--   - Elimina SIN_DEUDA (ambiguo): si tenia facturas saldadas -> PAGADO_TOTAL,
--     sin facturas -> PENDIENTE_FACTURACION.
--   - Elimina AL_DIA (modelo nuevo asume cobro automatico, no hay distincion
--     credito al dia vs mora): si era AL_DIA -> PAGADO_TOTAL.
--   - Elimina EN_MORA (no existe mora con debito automatico): -> PAGO_PARCIAL.
--   - Introduce PENDIENTE_PAGO (factura emitida sin pago alguno, antes
--     caia en PAGO_PARCIAL ambiguamente).
--
-- Regla de negocio (Sprint 9 ext): el estudiante solo puede recibir
-- asignaciones de clase cuando esta MATRICULADO o CURSANDO. Esa transicion
-- ocurre cuando situacion_pago llega a PAGADO_TOTAL (sea por completar
-- pagos CONTADO o por emision de factura CREDITO).
-- =============================================================================

-- 0) Ampliar la columna a VARCHAR(30) porque "PENDIENTE_FACTURACION" tiene
--    21 caracteres y la columna original era VARCHAR(20).
ALTER TABLE estudiantes_schema.estudiantes
    ALTER COLUMN situacion_pago TYPE VARCHAR(30);

-- 1) Quitar CHECK actual para poder hacer UPDATE con valores intermedios
ALTER TABLE estudiantes_schema.estudiantes
    DROP CONSTRAINT ck_estudiantes_situacion_pago;

-- 2) Migrar valores legacy
--    PENDIENTE_MATRICULA -> PENDIENTE_FACTURACION (mismo concepto)
UPDATE estudiantes_schema.estudiantes
   SET situacion_pago = 'PENDIENTE_FACTURACION'
 WHERE situacion_pago = 'PENDIENTE_MATRICULA';

--    AL_DIA -> PAGADO_TOTAL (credito automatico se considera pagado)
UPDATE estudiantes_schema.estudiantes
   SET situacion_pago = 'PAGADO_TOTAL'
 WHERE situacion_pago = 'AL_DIA';

--    EN_MORA -> PAGO_PARCIAL (no existe mora con debito automatico;
--    si quedo en EN_MORA es porque hay saldo CONTADO sin pagar)
UPDATE estudiantes_schema.estudiantes
   SET situacion_pago = 'PAGO_PARCIAL'
 WHERE situacion_pago = 'EN_MORA';

--    SIN_DEUDA es ambiguo. Como ms-estudiantes no puede consultar facturas
--    desde aqui (otro schema/MS), aplicamos heuristica conservadora:
--    los que estan en SIN_DEUDA + PRE_MATRICULADO -> PENDIENTE_FACTURACION
--    (asumimos que nunca se les emitio factura). El sync masivo posterior
--    via Feign a ms-cobros corrige cualquier outlier.
UPDATE estudiantes_schema.estudiantes
   SET situacion_pago = 'PENDIENTE_FACTURACION'
 WHERE situacion_pago = 'SIN_DEUDA'
   AND estado = 'PRE_MATRICULADO';

--    SIN_DEUDA en otros estados (MATRICULADO/CURSANDO/COMPLETADO) implica
--    que pago todo: -> PAGADO_TOTAL.
UPDATE estudiantes_schema.estudiantes
   SET situacion_pago = 'PAGADO_TOTAL'
 WHERE situacion_pago = 'SIN_DEUDA';

-- 3) Corregir inconsistencias del modelo (estado MATRICULADO/CURSANDO sin
--    facturas no es valido en el nuevo modelo: si no pagaron, deberian
--    estar en PRE_MATRICULADO). Volvemos esos casos a PRE_MATRICULADO con
--    PENDIENTE_FACTURACION.
UPDATE estudiantes_schema.estudiantes
   SET estado = 'PRE_MATRICULADO',
       situacion_pago = 'PENDIENTE_FACTURACION',
       fecha_matricula = NULL
 WHERE estado IN ('MATRICULADO', 'CURSANDO')
   AND situacion_pago = 'PENDIENTE_FACTURACION';

-- 4) Reañadir CHECK con los 4 valores nuevos
ALTER TABLE estudiantes_schema.estudiantes
    ADD CONSTRAINT ck_estudiantes_situacion_pago
        CHECK (situacion_pago IN (
            'PENDIENTE_FACTURACION',
            'PENDIENTE_PAGO',
            'PAGO_PARCIAL',
            'PAGADO_TOTAL'
        ));

-- 5) Cambiar DEFAULT para nuevos estudiantes
ALTER TABLE estudiantes_schema.estudiantes
    ALTER COLUMN situacion_pago SET DEFAULT 'PENDIENTE_FACTURACION';

-- 6) Actualizar comentario explicativo de la columna
COMMENT ON COLUMN estudiantes_schema.estudiantes.situacion_pago IS
'Estado financiero. PENDIENTE_FACTURACION = sin factura. '
'PENDIENTE_PAGO = factura CONTADO emitida sin pago. '
'PAGO_PARCIAL = factura CONTADO con saldo. '
'PAGADO_TOTAL = todo saldado o CREDITO emitido (debito automatico asumido). '
'Solo PAGADO_TOTAL permite que el estudiante reciba asignaciones de clase.';
