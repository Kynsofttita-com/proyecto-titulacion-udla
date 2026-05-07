-- =============================================================================
-- V1__Initial_Schema.sql
-- =============================================================================
-- MS-Auth: Migración inicial con tablas de auth_schema + shared_schema
-- Schemas: auth_schema (11 tablas) + shared_schema (2 tablas) = 13 tablas
-- =============================================================================
-- Ver docs/database/schema.md para el diseño completo y diagramas ER
-- =============================================================================

-- =============================================================================
-- SCHEMA: shared_schema (auditoria centralizada + idempotencia)
-- =============================================================================
-- Estas tablas las crea MS-Auth porque es el microservicio "owner" del schema
-- compartido. Los demás MS solo escriben/leen pero no crean estructura.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- shared_schema.audit_log
-- Registro de auditoría centralizado de todas las acciones del sistema.
-- Append-only: NUNCA se actualiza ni se borra.
-- -----------------------------------------------------------------------------
CREATE TABLE shared_schema.audit_log (
    id              BIGSERIAL    PRIMARY KEY,
    microservicio   VARCHAR(50)  NOT NULL,
    recurso         VARCHAR(100) NOT NULL,
    recurso_id      VARCHAR(50),
    accion          VARCHAR(50)  NOT NULL,
    usuario_id      BIGINT,
    usuario_email   VARCHAR(255),
    ip              VARCHAR(45),
    user_agent      TEXT,
    datos_anteriores JSONB,
    datos_nuevos    JSONB,
    correlation_id  VARCHAR(100),
    fecha           TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_audit_log_accion CHECK (accion IN ('CREATE', 'UPDATE', 'DELETE', 'READ', 'LOGIN', 'LOGOUT'))
);

CREATE INDEX idx_audit_log_microservicio_fecha ON shared_schema.audit_log (microservicio, fecha DESC);
CREATE INDEX idx_audit_log_usuario_fecha       ON shared_schema.audit_log (usuario_id, fecha DESC);
CREATE INDEX idx_audit_log_recurso             ON shared_schema.audit_log (recurso, recurso_id);
CREATE INDEX idx_audit_log_correlation         ON shared_schema.audit_log (correlation_id);

COMMENT ON TABLE shared_schema.audit_log IS 'Registro append-only de auditoria del sistema completo';

-- -----------------------------------------------------------------------------
-- shared_schema.processed_events
-- Tabla de idempotencia para consumidores RabbitMQ.
-- Antes de procesar un evento, validar que su event_id no esté aquí.
-- -----------------------------------------------------------------------------
CREATE TABLE shared_schema.processed_events (
    id                       BIGSERIAL    PRIMARY KEY,
    event_id                 UUID         NOT NULL,
    microservicio_consumidor VARCHAR(50)  NOT NULL,
    tipo_evento              VARCHAR(100) NOT NULL,
    processed_at             TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_processed_events_event_id UNIQUE (event_id)
);

CREATE INDEX idx_processed_events_microservicio_fecha ON shared_schema.processed_events (microservicio_consumidor, processed_at DESC);

COMMENT ON TABLE shared_schema.processed_events IS 'Tabla de idempotencia para consumidores de eventos RabbitMQ';


-- =============================================================================
-- SCHEMA: auth_schema (autenticacion + autorizacion + configuracion)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- auth_schema.categorias_licencia
-- Catalogo de categorias de licencia de conducir Ecuador (configurable por escuela).
-- (Se crea antes de tipos_curso porque tipos_curso tiene FK a esta tabla.)
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.categorias_licencia (
    id          BIGSERIAL    PRIMARY KEY,
    codigo      VARCHAR(20)  NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    activa      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    deleted_at  TIMESTAMP,

    CONSTRAINT uq_categorias_licencia_codigo UNIQUE (codigo)
);

COMMENT ON TABLE auth_schema.categorias_licencia IS 'Categorias de licencia que enseña la escuela (configurable)';

-- -----------------------------------------------------------------------------
-- auth_schema.usuarios
-- Usuarios del sistema (admin, staff, instructores, estudiantes).
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.usuarios (
    id              BIGSERIAL    PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(60)  NOT NULL,                  -- BCrypt hash (60 chars)
    nombre          VARCHAR(100) NOT NULL,
    apellido        VARCHAR(100) NOT NULL,
    telefono        VARCHAR(10),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    locked          BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_attempts SMALLINT     NOT NULL DEFAULT 0,
    last_login      TIMESTAMP,
    lock_until      TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    deleted_at      TIMESTAMP,

    CONSTRAINT uq_usuarios_email     UNIQUE (email),
    CONSTRAINT ck_usuarios_email     CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT ck_usuarios_telefono  CHECK (telefono IS NULL OR (LENGTH(telefono) = 10 AND telefono ~ '^09[0-9]{8}$')),
    CONSTRAINT ck_usuarios_failed    CHECK (failed_attempts >= 0 AND failed_attempts <= 99)
);

CREATE INDEX idx_usuarios_email          ON auth_schema.usuarios (email);
CREATE INDEX idx_usuarios_activo_locked  ON auth_schema.usuarios (activo, locked) WHERE deleted_at IS NULL;

COMMENT ON TABLE auth_schema.usuarios IS 'Usuarios del sistema con autenticacion JWT';

-- -----------------------------------------------------------------------------
-- auth_schema.roles
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.roles (
    id          BIGSERIAL    PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    deleted_at  TIMESTAMP,

    CONSTRAINT uq_roles_nombre UNIQUE (nombre)
);

COMMENT ON TABLE auth_schema.roles IS 'Roles del sistema (ADMIN, STAFF, INSTRUCTOR, ESTUDIANTE)';

-- -----------------------------------------------------------------------------
-- auth_schema.permisos
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.permisos (
    id          BIGSERIAL    PRIMARY KEY,
    codigo      VARCHAR(100) NOT NULL,
    recurso     VARCHAR(50)  NOT NULL,
    accion      VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    deleted_at  TIMESTAMP,

    CONSTRAINT uq_permisos_codigo UNIQUE (codigo)
);

CREATE INDEX idx_permisos_recurso_accion ON auth_schema.permisos (recurso, accion);

COMMENT ON TABLE auth_schema.permisos IS 'Permisos granulares (ej: ESTUDIANTES_READ, COBROS_WRITE)';

-- -----------------------------------------------------------------------------
-- auth_schema.usuario_rol (junction many-to-many)
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.usuario_rol (
    usuario_id BIGINT      NOT NULL,
    rol_id     BIGINT      NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by VARCHAR(50),

    CONSTRAINT pk_usuario_rol      PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario_rol_usr  FOREIGN KEY (usuario_id) REFERENCES auth_schema.usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_rol_rol  FOREIGN KEY (rol_id)     REFERENCES auth_schema.roles (id)    ON DELETE CASCADE
);

CREATE INDEX idx_usuario_rol_rol ON auth_schema.usuario_rol (rol_id);

-- -----------------------------------------------------------------------------
-- auth_schema.rol_permiso (junction many-to-many)
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.rol_permiso (
    rol_id     BIGINT      NOT NULL,
    permiso_id BIGINT      NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by VARCHAR(50),

    CONSTRAINT pk_rol_permiso      PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rol_permiso_rol  FOREIGN KEY (rol_id)     REFERENCES auth_schema.roles (id)    ON DELETE CASCADE,
    CONSTRAINT fk_rol_permiso_per  FOREIGN KEY (permiso_id) REFERENCES auth_schema.permisos (id) ON DELETE CASCADE
);

CREATE INDEX idx_rol_permiso_permiso ON auth_schema.rol_permiso (permiso_id);

-- -----------------------------------------------------------------------------
-- auth_schema.password_reset_token
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.password_reset_token (
    id         BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT    NOT NULL,
    token      UUID      NOT NULL DEFAULT gen_random_uuid(),
    expira_en  TIMESTAMP NOT NULL,
    usado      BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_password_reset_token        UNIQUE (token),
    CONSTRAINT fk_password_reset_token_usr    FOREIGN KEY (usuario_id) REFERENCES auth_schema.usuarios (id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token_token   ON auth_schema.password_reset_token (token);
CREATE INDEX idx_password_reset_token_usuario ON auth_schema.password_reset_token (usuario_id);

-- -----------------------------------------------------------------------------
-- auth_schema.tipos_curso
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.tipos_curso (
    id                    BIGSERIAL    PRIMARY KEY,
    nombre                VARCHAR(100) NOT NULL,
    descripcion           TEXT,
    duracion_total_horas  SMALLINT     NOT NULL,
    precio_base           NUMERIC(10,2) NOT NULL,
    categoria_licencia_id BIGINT,
    activo                BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50),
    deleted_at            TIMESTAMP,

    CONSTRAINT uq_tipos_curso_nombre   UNIQUE (nombre),
    CONSTRAINT fk_tipos_curso_cat_lic  FOREIGN KEY (categoria_licencia_id) REFERENCES auth_schema.categorias_licencia (id),
    CONSTRAINT ck_tipos_curso_horas    CHECK (duracion_total_horas > 0),
    CONSTRAINT ck_tipos_curso_precio   CHECK (precio_base > 0)
);

COMMENT ON TABLE auth_schema.tipos_curso IS 'Tipos de cursos ofrecidos (Basico, Profesional, Moto, etc.)';

-- -----------------------------------------------------------------------------
-- auth_schema.conceptos_facturacion
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.conceptos_facturacion (
    id          BIGSERIAL    PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    monto_base  NUMERIC(10,2) NOT NULL,
    descripcion TEXT,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    deleted_at  TIMESTAMP,

    CONSTRAINT uq_conceptos_facturacion_nombre UNIQUE (nombre),
    CONSTRAINT ck_conceptos_facturacion_monto  CHECK (monto_base > 0)
);

COMMENT ON TABLE auth_schema.conceptos_facturacion IS 'Conceptos de facturacion (Curso, Examen, etc.)';

-- -----------------------------------------------------------------------------
-- auth_schema.plantillas_email
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.plantillas_email (
    id          BIGSERIAL    PRIMARY KEY,
    codigo      VARCHAR(100) NOT NULL,
    asunto      VARCHAR(255) NOT NULL,
    cuerpo_html TEXT         NOT NULL,
    variables   JSONB,
    activa      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    deleted_at  TIMESTAMP,

    CONSTRAINT uq_plantillas_email_codigo UNIQUE (codigo)
);

COMMENT ON TABLE auth_schema.plantillas_email IS 'Plantillas de emails transaccionales (recuperar password, etc.)';

-- -----------------------------------------------------------------------------
-- auth_schema.configuracion_escuela (single-tenant configurable)
-- Tabla con UNA SOLA fila. Parametros configurables de la escuela.
-- -----------------------------------------------------------------------------
CREATE TABLE auth_schema.configuracion_escuela (
    id                          BIGSERIAL    PRIMARY KEY,
    nombre                      VARCHAR(255) NOT NULL,
    ruc                         VARCHAR(13)  NOT NULL,
    direccion                   TEXT,
    telefono                    VARCHAR(20),
    email                       VARCHAR(255),
    logo_url                    VARCHAR(500),
    color_primario              VARCHAR(7),
    color_secundario            VARCHAR(7),
    duracion_clase_default_min  SMALLINT     NOT NULL DEFAULT 60,
    horario_apertura            TIME,
    horario_cierre              TIME,
    horas_recordatorio_clase    SMALLINT     NOT NULL DEFAULT 24,
    dias_alerta_soat            SMALLINT     NOT NULL DEFAULT 30,
    created_at                  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP,
    created_by                  VARCHAR(50),
    updated_by                  VARCHAR(50),

    CONSTRAINT uq_configuracion_escuela_ruc  UNIQUE (ruc),
    CONSTRAINT ck_configuracion_escuela_ruc  CHECK (LENGTH(ruc) = 13 AND ruc ~ '^[0-9]{13}$'),
    CONSTRAINT ck_configuracion_escuela_dur  CHECK (duracion_clase_default_min > 0),
    CONSTRAINT ck_configuracion_escuela_color_pri CHECK (color_primario IS NULL OR color_primario ~ '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT ck_configuracion_escuela_color_sec CHECK (color_secundario IS NULL OR color_secundario ~ '^#[0-9A-Fa-f]{6}$')
);

COMMENT ON TABLE auth_schema.configuracion_escuela IS 'Configuracion de la escuela (single-tenant, una sola fila)';

-- Función + trigger para garantizar que solo exista UNA fila en configuracion_escuela
CREATE OR REPLACE FUNCTION auth_schema.fn_check_configuracion_escuela_single_row()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM auth_schema.configuracion_escuela) >= 1 THEN
        RAISE EXCEPTION 'Solo se permite una fila en configuracion_escuela (single-tenant)';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_configuracion_escuela_single_row
    BEFORE INSERT ON auth_schema.configuracion_escuela
    FOR EACH ROW EXECUTE FUNCTION auth_schema.fn_check_configuracion_escuela_single_row();
