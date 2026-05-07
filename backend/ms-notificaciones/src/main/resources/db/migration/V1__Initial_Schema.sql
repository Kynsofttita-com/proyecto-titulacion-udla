-- =============================================================================
-- V1__Initial_Schema.sql - MS-Notificaciones
-- 3 tablas en notificaciones_schema (in-app + email + preferencias)
-- Ver docs/database/schema.md sección 12
-- =============================================================================

-- -----------------------------------------------------------------------------
-- notificaciones_schema.notificaciones (in-app)
-- -----------------------------------------------------------------------------
CREATE TABLE notificaciones_schema.notificaciones (
    id              BIGSERIAL    PRIMARY KEY,
    usuario_id      BIGINT       NOT NULL,                       -- ref auth_schema.usuarios
    tipo            VARCHAR(50)  NOT NULL,
    titulo          VARCHAR(255) NOT NULL,
    mensaje         TEXT         NOT NULL,
    leida           BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT NOW(),
    fecha_lectura   TIMESTAMP,
    datos_extra     JSONB,
    prioridad       VARCHAR(10)  NOT NULL DEFAULT 'NORMAL',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    deleted_at      TIMESTAMP,

    CONSTRAINT ck_notificaciones_prioridad CHECK (prioridad IN ('BAJA', 'NORMAL', 'ALTA'))
);

CREATE INDEX idx_notificaciones_usuario_no_leidas ON notificaciones_schema.notificaciones (usuario_id, leida)
    WHERE leida = FALSE AND deleted_at IS NULL;
CREATE INDEX idx_notificaciones_fecha             ON notificaciones_schema.notificaciones (fecha_creacion DESC);

-- -----------------------------------------------------------------------------
-- notificaciones_schema.log_envios_email
-- -----------------------------------------------------------------------------
CREATE TABLE notificaciones_schema.log_envios_email (
    id                BIGSERIAL    PRIMARY KEY,
    destinatario      VARCHAR(255) NOT NULL,
    asunto            VARCHAR(255) NOT NULL,
    plantilla_codigo  VARCHAR(100),
    estado            VARCHAR(20)  NOT NULL,
    enviado_en        TIMESTAMP,
    intentos          SMALLINT     NOT NULL DEFAULT 0,
    error_mensaje     TEXT,
    usuario_id        BIGINT,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50),

    CONSTRAINT ck_log_envios_email_estado   CHECK (estado IN ('PENDIENTE', 'ENVIADO', 'FALLIDO')),
    CONSTRAINT ck_log_envios_email_intentos CHECK (intentos >= 0)
);

CREATE INDEX idx_log_envios_email_estado_fecha ON notificaciones_schema.log_envios_email (estado, created_at DESC);
CREATE INDEX idx_log_envios_email_destinatario ON notificaciones_schema.log_envios_email (destinatario);

-- -----------------------------------------------------------------------------
-- notificaciones_schema.preferencias_notificacion
-- -----------------------------------------------------------------------------
CREATE TABLE notificaciones_schema.preferencias_notificacion (
    id                       BIGSERIAL PRIMARY KEY,
    usuario_id               BIGINT    NOT NULL,             -- ref auth_schema.usuarios
    recibir_email            BOOLEAN   NOT NULL DEFAULT TRUE,
    recibir_in_app           BOOLEAN   NOT NULL DEFAULT TRUE,
    recibir_recordatorios    BOOLEAN   NOT NULL DEFAULT TRUE,
    recibir_alertas_admin    BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP,
    created_by               VARCHAR(50),
    updated_by               VARCHAR(50),

    CONSTRAINT uq_preferencias_usuario UNIQUE (usuario_id)
);
