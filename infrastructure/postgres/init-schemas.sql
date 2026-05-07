-- =============================================================================
-- Inicialización de schemas para escuela_db
-- Sistema de Control Administrativo - Escuelas de Conducción
-- =============================================================================
-- Este script se ejecuta automáticamente al levantar el contenedor de Postgres
-- por primera vez. Crea los 9 schemas (uno por microservicio + shared).
-- Las migraciones de tablas son responsabilidad de Flyway en cada microservicio.
-- =============================================================================

-- Crear schemas (uno por microservicio + shared)
CREATE SCHEMA IF NOT EXISTS auth_schema;
CREATE SCHEMA IF NOT EXISTS estudiantes_schema;
CREATE SCHEMA IF NOT EXISTS instructores_schema;
CREATE SCHEMA IF NOT EXISTS vehiculos_schema;
CREATE SCHEMA IF NOT EXISTS asignaciones_schema;
CREATE SCHEMA IF NOT EXISTS cobros_schema;
CREATE SCHEMA IF NOT EXISTS reportes_schema;
CREATE SCHEMA IF NOT EXISTS notificaciones_schema;
CREATE SCHEMA IF NOT EXISTS shared_schema;

-- Otorgar permisos al usuario de la aplicación (escuela_user) sobre todos los schemas
-- Nota: el usuario se crea automáticamente desde POSTGRES_USER en docker-compose
GRANT ALL PRIVILEGES ON SCHEMA auth_schema TO escuela_user;
GRANT ALL PRIVILEGES ON SCHEMA estudiantes_schema TO escuela_user;
GRANT ALL PRIVILEGES ON SCHEMA instructores_schema TO escuela_user;
GRANT ALL PRIVILEGES ON SCHEMA vehiculos_schema TO escuela_user;
GRANT ALL PRIVILEGES ON SCHEMA asignaciones_schema TO escuela_user;
GRANT ALL PRIVILEGES ON SCHEMA cobros_schema TO escuela_user;
GRANT ALL PRIVILEGES ON SCHEMA reportes_schema TO escuela_user;
GRANT ALL PRIVILEGES ON SCHEMA notificaciones_schema TO escuela_user;
GRANT ALL PRIVILEGES ON SCHEMA shared_schema TO escuela_user;

-- Establecer search_path por defecto (incluye public para extensiones de Postgres)
ALTER DATABASE escuela_db SET search_path TO public, shared_schema;

-- Habilitar extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";    -- Para UUIDs (tokens, event IDs, etc.)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";     -- Para hashing y cifrado

-- Mensaje informativo
DO $$
BEGIN
    RAISE NOTICE '====================================================';
    RAISE NOTICE 'escuela_db inicializada con 9 schemas:';
    RAISE NOTICE '  - auth_schema (MS-Auth + Configuración)';
    RAISE NOTICE '  - estudiantes_schema (MS-Estudiantes)';
    RAISE NOTICE '  - instructores_schema (MS-Instructores)';
    RAISE NOTICE '  - vehiculos_schema (MS-Vehículos)';
    RAISE NOTICE '  - asignaciones_schema (MS-Asignaciones)';
    RAISE NOTICE '  - cobros_schema (MS-Cobros)';
    RAISE NOTICE '  - reportes_schema (MS-Reportes)';
    RAISE NOTICE '  - notificaciones_schema (MS-Notificaciones)';
    RAISE NOTICE '  - shared_schema (audit_log centralizado)';
    RAISE NOTICE '====================================================';
END $$;
