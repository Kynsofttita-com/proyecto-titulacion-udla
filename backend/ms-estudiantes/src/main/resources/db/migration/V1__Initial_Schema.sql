-- =============================================================================
-- V1__Initial_Schema.sql - MS-Estudiantes
-- 5 tablas en estudiantes_schema: estudiantes, documentos, contactos_emergencia,
-- progreso_academico, asistencia
-- Ver docs/database/schema.md sección 6 para diseño completo y diagrama ER
-- =============================================================================

-- -----------------------------------------------------------------------------
-- estudiantes_schema.estudiantes
-- -----------------------------------------------------------------------------
CREATE TABLE estudiantes_schema.estudiantes (
    id                    BIGSERIAL    PRIMARY KEY,
    cedula                VARCHAR(10)  NOT NULL,
    nombre                VARCHAR(100) NOT NULL,
    apellido              VARCHAR(100) NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    telefono              VARCHAR(10)  NOT NULL,
    direccion             TEXT,
    fecha_nacimiento      DATE         NOT NULL,
    genero                CHAR(1),
    estado                VARCHAR(20)  NOT NULL DEFAULT 'PRE_MATRICULADO',
    fecha_matricula       DATE,
    tipo_curso_id         BIGINT,                       -- ref a auth_schema.tipos_curso (sin FK cross-schema)
    categoria_licencia_id BIGINT,                       -- ref a auth_schema.categorias_licencia
    usuario_id            BIGINT,                       -- ref a auth_schema.usuarios (si tiene login)
    observaciones         TEXT,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50),
    deleted_at            TIMESTAMP,

    CONSTRAINT uq_estudiantes_cedula  UNIQUE (cedula),
    CONSTRAINT uq_estudiantes_email   UNIQUE (email),
    CONSTRAINT ck_estudiantes_cedula  CHECK (LENGTH(cedula) = 10 AND cedula ~ '^[0-9]{10}$'),
    CONSTRAINT ck_estudiantes_email   CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT ck_estudiantes_tel     CHECK (LENGTH(telefono) = 10 AND telefono ~ '^09[0-9]{8}$'),
    CONSTRAINT ck_estudiantes_genero  CHECK (genero IS NULL OR genero IN ('M', 'F', 'O')),
    CONSTRAINT ck_estudiantes_estado  CHECK (estado IN ('PRE_MATRICULADO', 'ACTIVO', 'COMPLETADO', 'RETIRADO'))
);

CREATE INDEX idx_estudiantes_cedula          ON estudiantes_schema.estudiantes (cedula);
CREATE INDEX idx_estudiantes_email           ON estudiantes_schema.estudiantes (email);
CREATE INDEX idx_estudiantes_estado          ON estudiantes_schema.estudiantes (estado) WHERE deleted_at IS NULL;
CREATE INDEX idx_estudiantes_apellido_nombre ON estudiantes_schema.estudiantes (apellido, nombre);

COMMENT ON TABLE estudiantes_schema.estudiantes IS 'Estudiantes matriculados en la escuela';

-- -----------------------------------------------------------------------------
-- estudiantes_schema.documentos
-- -----------------------------------------------------------------------------
CREATE TABLE estudiantes_schema.documentos (
    id            BIGSERIAL    PRIMARY KEY,
    estudiante_id BIGINT       NOT NULL,
    tipo          VARCHAR(30)  NOT NULL,
    url_archivo   VARCHAR(500) NOT NULL,
    fecha_subida  TIMESTAMP    NOT NULL DEFAULT NOW(),
    mime_type     VARCHAR(100),
    tamano_bytes  BIGINT,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP,
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    deleted_at    TIMESTAMP,

    CONSTRAINT fk_documentos_estudiante FOREIGN KEY (estudiante_id) REFERENCES estudiantes_schema.estudiantes (id) ON DELETE CASCADE,
    CONSTRAINT ck_documentos_tipo       CHECK (tipo IN ('CEDULA_FRENTE', 'CEDULA_REVERSO', 'FOTO', 'EXAMEN_MEDICO', 'OTRO'))
);

CREATE INDEX idx_documentos_estudiante ON estudiantes_schema.documentos (estudiante_id);

-- -----------------------------------------------------------------------------
-- estudiantes_schema.contactos_emergencia
-- -----------------------------------------------------------------------------
CREATE TABLE estudiantes_schema.contactos_emergencia (
    id            BIGSERIAL    PRIMARY KEY,
    estudiante_id BIGINT       NOT NULL,
    nombre        VARCHAR(200) NOT NULL,
    telefono      VARCHAR(15)  NOT NULL,
    parentesco    VARCHAR(50),
    es_principal  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP,
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    deleted_at    TIMESTAMP,

    CONSTRAINT fk_contactos_estudiante FOREIGN KEY (estudiante_id) REFERENCES estudiantes_schema.estudiantes (id) ON DELETE CASCADE
);

CREATE INDEX idx_contactos_estudiante ON estudiantes_schema.contactos_emergencia (estudiante_id);

-- -----------------------------------------------------------------------------
-- estudiantes_schema.progreso_academico (1 fila por estudiante)
-- -----------------------------------------------------------------------------
CREATE TABLE estudiantes_schema.progreso_academico (
    id                    BIGSERIAL PRIMARY KEY,
    estudiante_id         BIGINT    NOT NULL,
    clases_planeadas      SMALLINT  NOT NULL DEFAULT 0,
    clases_completadas    SMALLINT  NOT NULL DEFAULT 0,
    clases_pendientes     SMALLINT  NOT NULL DEFAULT 0,
    clases_canceladas     SMALLINT  NOT NULL DEFAULT 0,
    calificacion_promedio NUMERIC(4,2),
    aprobado              BOOLEAN,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50),

    CONSTRAINT uq_progreso_estudiante      UNIQUE (estudiante_id),
    CONSTRAINT fk_progreso_estudiante      FOREIGN KEY (estudiante_id) REFERENCES estudiantes_schema.estudiantes (id) ON DELETE CASCADE,
    CONSTRAINT ck_progreso_calificacion    CHECK (calificacion_promedio IS NULL OR (calificacion_promedio BETWEEN 0 AND 100)),
    CONSTRAINT ck_progreso_clases          CHECK (clases_planeadas >= 0 AND clases_completadas >= 0 AND clases_pendientes >= 0 AND clases_canceladas >= 0)
);

-- -----------------------------------------------------------------------------
-- estudiantes_schema.asistencia
-- -----------------------------------------------------------------------------
CREATE TABLE estudiantes_schema.asistencia (
    id             BIGSERIAL PRIMARY KEY,
    estudiante_id  BIGINT    NOT NULL,
    asignacion_id  BIGINT    NOT NULL,           -- ref a asignaciones_schema.asignaciones
    fecha_clase    DATE      NOT NULL,
    asistio        BOOLEAN   NOT NULL,
    justificacion  TEXT,
    observaciones  TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP,
    created_by     VARCHAR(50),
    updated_by     VARCHAR(50),

    CONSTRAINT uq_asistencia_estudiante_asignacion UNIQUE (estudiante_id, asignacion_id),
    CONSTRAINT fk_asistencia_estudiante            FOREIGN KEY (estudiante_id) REFERENCES estudiantes_schema.estudiantes (id) ON DELETE CASCADE
);

CREATE INDEX idx_asistencia_estudiante_fecha ON estudiantes_schema.asistencia (estudiante_id, fecha_clase);
CREATE INDEX idx_asistencia_asignacion       ON estudiantes_schema.asistencia (asignacion_id);
