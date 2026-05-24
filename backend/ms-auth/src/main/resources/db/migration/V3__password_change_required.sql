-- =============================================================================
-- V3: Cambio de contrasenia obligatorio al primer login
-- =============================================================================
-- Agrega flag para forzar al usuario a cambiar la contrasenia inicial que el
-- admin le asigno al crear su cuenta. Mejora la seguridad porque el admin
-- conoce la contrasenia temporal, por lo que el usuario debe cambiarla.
-- =============================================================================

ALTER TABLE auth_schema.usuarios
    ADD COLUMN password_change_required BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN auth_schema.usuarios.password_change_required IS
    'Si true, fuerza al usuario a cambiar la contrasenia al iniciar sesion. Se activa al crear el usuario desde admin y se desactiva al cambiarla.';

-- Usuarios existentes NO requieren cambio (ya estan en uso)
UPDATE auth_schema.usuarios SET password_change_required = FALSE;
