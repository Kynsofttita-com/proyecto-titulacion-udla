-- =============================================================================
-- V5: Datos personales extendidos del usuario
-- =============================================================================
-- Agrega campos adicionales para recopilar informacion personal mas completa
-- de los usuarios: cedula, fecha nacimiento, genero, direccion, ciudad, provincia.
-- =============================================================================

ALTER TABLE auth_schema.usuarios
    ADD COLUMN cedula VARCHAR(10),
    ADD COLUMN fecha_nacimiento DATE,
    ADD COLUMN genero VARCHAR(1),
    ADD COLUMN direccion VARCHAR(255),
    ADD COLUMN ciudad VARCHAR(100),
    ADD COLUMN provincia VARCHAR(100);

COMMENT ON COLUMN auth_schema.usuarios.cedula IS
    'Cedula de identidad ecuatoriana (10 digitos). Opcional.';
COMMENT ON COLUMN auth_schema.usuarios.fecha_nacimiento IS
    'Fecha de nacimiento del usuario. Opcional.';
COMMENT ON COLUMN auth_schema.usuarios.genero IS
    'Genero del usuario (M=Masculino, F=Femenino, O=Otro). Opcional.';
COMMENT ON COLUMN auth_schema.usuarios.direccion IS
    'Direccion de residencia del usuario. Opcional.';
COMMENT ON COLUMN auth_schema.usuarios.ciudad IS
    'Ciudad de residencia del usuario. Opcional.';
COMMENT ON COLUMN auth_schema.usuarios.provincia IS
    'Provincia de residencia del usuario. Opcional.';
