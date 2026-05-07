-- =============================================================================
-- V1__Initial_Schema.sql - MS-Instructores
-- 4 tablas en instructores_schema
-- Ver docs/database/schema.md sección 7
-- =============================================================================

-- -----------------------------------------------------------------------------
-- instructores_schema.instructores
-- -----------------------------------------------------------------------------
CREATE TABLE instructores_schema.instructores (
    id                  BIGSERIAL    PRIMARY KEY,
    cedula              VARCHAR(10)  NOT NULL,
    nombre              VARCHAR(100) NOT NULL,
    apellido            VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    telefono            VARCHAR(10)  NOT NULL,
    direccion           TEXT,
    fecha_nacimiento    DATE,
    licencia_numero     VARCHAR(50)  NOT NULL,
    licencia_categoria  VARCHAR(20)  NOT NULL,
    licencia_emision    DATE         NOT NULL,
    licencia_caducidad  DATE         NOT NULL,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    fecha_contratacion  DATE,
    salario_mensual     NUMERIC(10,2),
    usuario_id          BIGINT,                       -- ref auth_schema.usuarios
    observaciones       TEXT,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),
    deleted_at          TIMESTAMP,

    CONSTRAINT uq_instructores_cedula      UNIQUE (cedula),
    CONSTRAINT uq_instructores_email       UNIQUE (email),
    CONSTRAINT uq_instructores_licencia    UNIQUE (licencia_numero),
    CONSTRAINT ck_instructores_cedula      CHECK (LENGTH(cedula) = 10 AND cedula ~ '^[0-9]{10}$'),
    CONSTRAINT ck_instructores_email       CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT ck_instructores_telefono    CHECK (LENGTH(telefono) = 10 AND telefono ~ '^09[0-9]{8}$'),
    CONSTRAINT ck_instructores_estado      CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO')),
    CONSTRAINT ck_instructores_salario     CHECK (salario_mensual IS NULL OR salario_mensual >= 0),
    CONSTRAINT ck_instructores_lic_fechas  CHECK (licencia_caducidad > licencia_emision)
);

CREATE INDEX idx_instructores_cedula     ON instructores_schema.instructores (cedula);
CREATE INDEX idx_instructores_licencia   ON instructores_schema.instructores (licencia_numero);
CREATE INDEX idx_instructores_estado     ON instructores_schema.instructores (estado) WHERE deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- instructores_schema.certificaciones
-- -----------------------------------------------------------------------------
CREATE TABLE instructores_schema.certificaciones (
    id              BIGSERIAL    PRIMARY KEY,
    instructor_id   BIGINT       NOT NULL,
    tipo            VARCHAR(100) NOT NULL,
    fecha_obtencion DATE         NOT NULL,
    vigencia_hasta  DATE,
    entidad_emisora VARCHAR(255),
    archivo_url     VARCHAR(500),
    observaciones   TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    deleted_at      TIMESTAMP,

    CONSTRAINT fk_certificaciones_instructor FOREIGN KEY (instructor_id) REFERENCES instructores_schema.instructores (id) ON DELETE CASCADE
);

CREATE INDEX idx_certificaciones_instructor ON instructores_schema.certificaciones (instructor_id);

-- -----------------------------------------------------------------------------
-- instructores_schema.disponibilidad (recurrente semanal)
-- -----------------------------------------------------------------------------
CREATE TABLE instructores_schema.disponibilidad (
    id            BIGSERIAL PRIMARY KEY,
    instructor_id BIGINT    NOT NULL,
    dia_semana    SMALLINT  NOT NULL,                -- 1=Lunes ... 7=Domingo
    hora_inicio   TIME      NOT NULL,
    hora_fin      TIME      NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP,
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    deleted_at    TIMESTAMP,

    CONSTRAINT fk_disponibilidad_instructor   FOREIGN KEY (instructor_id) REFERENCES instructores_schema.instructores (id) ON DELETE CASCADE,
    CONSTRAINT ck_disponibilidad_dia          CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT ck_disponibilidad_horas        CHECK (hora_fin > hora_inicio)
);

CREATE UNIQUE INDEX uq_disponibilidad_instructor_dia_hora
    ON instructores_schema.disponibilidad (instructor_id, dia_semana, hora_inicio)
    WHERE deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- instructores_schema.horarios_trabajo (excepciones puntuales)
-- -----------------------------------------------------------------------------
CREATE TABLE instructores_schema.horarios_trabajo (
    id            BIGSERIAL   PRIMARY KEY,
    instructor_id BIGINT      NOT NULL,
    fecha         DATE        NOT NULL,
    hora_inicio   TIME,
    hora_fin      TIME,
    tipo          VARCHAR(20) NOT NULL,
    motivo        TEXT,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP,
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    deleted_at    TIMESTAMP,

    CONSTRAINT fk_horarios_trabajo_instructor FOREIGN KEY (instructor_id) REFERENCES instructores_schema.instructores (id) ON DELETE CASCADE,
    CONSTRAINT ck_horarios_trabajo_tipo       CHECK (tipo IN ('EXTRA', 'AUSENCIA')),
    CONSTRAINT ck_horarios_trabajo_horas      CHECK (hora_fin IS NULL OR hora_inicio IS NULL OR hora_fin > hora_inicio)
);

CREATE INDEX idx_horarios_trabajo_instructor_fecha ON instructores_schema.horarios_trabajo (instructor_id, fecha);
