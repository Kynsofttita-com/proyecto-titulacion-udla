-- =============================================================================
-- V8: Remueve las 5 plantillas seed introducidas por V7
-- =============================================================================
-- V7 sembro 5 plantillas de demostracion (INVITACION_USUARIO, REPORTE_MOROSIDAD_
-- SEMANAL, RECORDATORIO_CUOTA_PROXIMA, ALERTA_SOAT_VENCIMIENTO, NUEVA_CLASE_
-- INSTRUCTOR). El feedback del usuario indica que no las quiere en la base:
-- las editaria/crearia manualmente desde la UI cuando las necesite.
--
-- Idempotente: DELETE con codigo IN (...). Ejecuta 0 filas si no existen.
-- =============================================================================

DELETE FROM auth_schema.plantillas_email
WHERE codigo IN (
    'INVITACION_USUARIO',
    'REPORTE_MOROSIDAD_SEMANAL',
    'RECORDATORIO_CUOTA_PROXIMA',
    'ALERTA_SOAT_VENCIMIENTO',
    'NUEVA_CLASE_INSTRUCTOR'
);
