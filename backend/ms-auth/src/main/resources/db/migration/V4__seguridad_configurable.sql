-- =============================================================================
-- V4: Parametros de seguridad configurables desde la UI
-- =============================================================================
-- Mueve los valores hardcoded de AuthProperties (maxFailedAttempts, lockoutDurationMinutes)
-- a la tabla configuracion_escuela para que el admin pueda ajustarlos en runtime
-- sin reiniciar el sistema.
-- =============================================================================

ALTER TABLE auth_schema.configuracion_escuela
    ADD COLUMN max_intentos_fallidos SMALLINT NOT NULL DEFAULT 3,
    ADD COLUMN duracion_bloqueo_minutos SMALLINT NOT NULL DEFAULT 15,
    ADD COLUMN expiracion_token_reset_minutos SMALLINT NOT NULL DEFAULT 60;

ALTER TABLE auth_schema.configuracion_escuela
    ADD CONSTRAINT ck_config_max_intentos CHECK (max_intentos_fallidos BETWEEN 1 AND 10),
    ADD CONSTRAINT ck_config_duracion_bloqueo CHECK (duracion_bloqueo_minutos BETWEEN 1 AND 1440),
    ADD CONSTRAINT ck_config_token_reset CHECK (expiracion_token_reset_minutos BETWEEN 5 AND 1440);

COMMENT ON COLUMN auth_schema.configuracion_escuela.max_intentos_fallidos IS
    'Numero de intentos fallidos de login antes de bloquear la cuenta. 1-10. Default 3.';
COMMENT ON COLUMN auth_schema.configuracion_escuela.duracion_bloqueo_minutos IS
    'Duracion del bloqueo automatico tras superar max_intentos_fallidos. 1-1440 min. Default 15.';
COMMENT ON COLUMN auth_schema.configuracion_escuela.expiracion_token_reset_minutos IS
    'Tiempo de validez del link de recuperacion de contrasenia. 5-1440 min. Default 60.';
