-- =============================================================================
-- V1__Initial_Schema.sql - MS-Asignaciones
-- 3 tablas en asignaciones_schema (con optimistic locking)
-- Ver docs/database/schema.md sección 9
-- =============================================================================

-- -----------------------------------------------------------------------------
-- asignaciones_schema.asignaciones (clase tripartita)
-- -----------------------------------------------------------------------------
CREATE TABLE asignaciones_schema.asignaciones (
    id                  BIGSERIAL    PRIMARY KEY,
    instructor_id       BIGINT       NOT NULL,                      -- ref instructores_schema.instructores
    estudiante_id       BIGINT       NOT NULL,                      -- ref estudiantes_schema.estudiantes
    vehiculo_id         BIGINT       NOT NULL,                      -- ref vehiculos_schema.vehiculos
    fecha_hora          TIMESTAMP    NOT NULL,
    duracion_minutos    SMALLINT     NOT NULL DEFAULT 60,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'PROGRAMADA',
    tipo_clase          VARCHAR(20)  NOT NULL,
    ubicacion           VARCHAR(255),
    observaciones       TEXT,
    motivo_cancelacion  TEXT,
    version             BIGINT       NOT NULL DEFAULT 0,             -- optimistic locking
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),
    deleted_at          TIMESTAMP,

    CONSTRAINT ck_asignaciones_duracion   CHECK (duracion_minutos > 0),
    CONSTRAINT ck_asignaciones_estado     CHECK (estado IN ('PROGRAMADA', 'CONFIRMADA', 'EN_CURSO', 'COMPLETADA', 'CANCELADA', 'NO_ASISTIO')),
    CONSTRAINT ck_asignaciones_tipo_clase CHECK (tipo_clase IN ('TEORICA', 'PRACTICA', 'EXAMEN'))
);

CREATE INDEX idx_asignaciones_instructor_fecha ON asignaciones_schema.asignaciones (instructor_id, fecha_hora);
CREATE INDEX idx_asignaciones_estudiante_fecha ON asignaciones_schema.asignaciones (estudiante_id, fecha_hora);
CREATE INDEX idx_asignaciones_vehiculo_fecha   ON asignaciones_schema.asignaciones (vehiculo_id, fecha_hora);
CREATE INDEX idx_asignaciones_estado_fecha     ON asignaciones_schema.asignaciones (estado, fecha_hora);

-- -----------------------------------------------------------------------------
-- asignaciones_schema.cambios_asignacion (registro de reprogramaciones)
-- -----------------------------------------------------------------------------
CREATE TABLE asignaciones_schema.cambios_asignacion (
    id                   BIGSERIAL PRIMARY KEY,
    asignacion_id        BIGINT    NOT NULL,
    fecha_cambio         TIMESTAMP NOT NULL DEFAULT NOW(),
    motivo               TEXT      NOT NULL,
    fecha_anterior       TIMESTAMP,
    fecha_nueva          TIMESTAMP,
    instructor_anterior  BIGINT,
    instructor_nuevo     BIGINT,
    vehiculo_anterior    BIGINT,
    vehiculo_nuevo       BIGINT,
    usuario_id           BIGINT    NOT NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by           VARCHAR(50),

    CONSTRAINT fk_cambios_asignacion_asig FOREIGN KEY (asignacion_id) REFERENCES asignaciones_schema.asignaciones (id) ON DELETE CASCADE
);

CREATE INDEX idx_cambios_asignacion_asig ON asignaciones_schema.cambios_asignacion (asignacion_id);

-- -----------------------------------------------------------------------------
-- asignaciones_schema.historial_estados
-- -----------------------------------------------------------------------------
CREATE TABLE asignaciones_schema.historial_estados (
    id              BIGSERIAL   PRIMARY KEY,
    asignacion_id   BIGINT      NOT NULL,
    estado_anterior VARCHAR(20),
    estado_nuevo    VARCHAR(20) NOT NULL,
    fecha_cambio    TIMESTAMP   NOT NULL DEFAULT NOW(),
    usuario_id      BIGINT,
    observaciones   TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(50),

    CONSTRAINT fk_historial_estados_asig FOREIGN KEY (asignacion_id) REFERENCES asignaciones_schema.asignaciones (id) ON DELETE CASCADE
);

CREATE INDEX idx_historial_estados_asig_fecha ON asignaciones_schema.historial_estados (asignacion_id, fecha_cambio DESC);
