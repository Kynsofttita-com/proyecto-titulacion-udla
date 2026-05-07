-- =============================================================================
-- V1__Initial_Schema.sql - MS-Vehiculos
-- 5 tablas en vehiculos_schema
-- Ver docs/database/schema.md sección 8
-- =============================================================================

-- -----------------------------------------------------------------------------
-- vehiculos_schema.vehiculos
-- -----------------------------------------------------------------------------
CREATE TABLE vehiculos_schema.vehiculos (
    id                    BIGSERIAL    PRIMARY KEY,
    placa                 VARCHAR(8)   NOT NULL,
    marca                 VARCHAR(50)  NOT NULL,
    modelo                VARCHAR(50)  NOT NULL,
    anio                  SMALLINT     NOT NULL,
    vin                   VARCHAR(17),
    color                 VARCHAR(30),
    kilometraje           INTEGER      NOT NULL DEFAULT 0,
    estado                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    soat_vencimiento      DATE,
    revision_vencimiento  DATE,
    fecha_compra          DATE,
    valor_compra          NUMERIC(10,2),
    categoria_licencia_id BIGINT,                   -- ref auth_schema.categorias_licencia
    observaciones         TEXT,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50),
    deleted_at            TIMESTAMP,

    CONSTRAINT uq_vehiculos_placa  UNIQUE (placa),
    CONSTRAINT uq_vehiculos_vin    UNIQUE (vin),
    CONSTRAINT ck_vehiculos_placa  CHECK (placa ~ '^[A-Z]{3}-[0-9]{4}$' OR placa ~ '^[A-Z]{2}-[0-9]{4}[A-Z]$'),
    CONSTRAINT ck_vehiculos_anio   CHECK (anio BETWEEN 1990 AND 2050),
    CONSTRAINT ck_vehiculos_km     CHECK (kilometraje >= 0),
    CONSTRAINT ck_vehiculos_estado CHECK (estado IN ('ACTIVO', 'MANTENIMIENTO', 'FUERA_SERVICIO')),
    CONSTRAINT ck_vehiculos_valor  CHECK (valor_compra IS NULL OR valor_compra >= 0)
);

CREATE INDEX idx_vehiculos_placa        ON vehiculos_schema.vehiculos (placa);
CREATE INDEX idx_vehiculos_estado       ON vehiculos_schema.vehiculos (estado) WHERE deleted_at IS NULL;
CREATE INDEX idx_vehiculos_soat_proximo ON vehiculos_schema.vehiculos (soat_vencimiento) WHERE deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- vehiculos_schema.mantenimientos
-- -----------------------------------------------------------------------------
CREATE TABLE vehiculos_schema.mantenimientos (
    id                    BIGSERIAL    PRIMARY KEY,
    vehiculo_id           BIGINT       NOT NULL,
    tipo                  VARCHAR(20)  NOT NULL,
    fecha                 DATE         NOT NULL,
    costo                 NUMERIC(10,2) NOT NULL,
    descripcion           TEXT         NOT NULL,
    taller                VARCHAR(255),
    kilometraje_servicio  INTEGER,
    proxima_fecha         DATE,
    proximo_kilometraje   INTEGER,
    archivo_factura_url   VARCHAR(500),
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50),
    deleted_at            TIMESTAMP,

    CONSTRAINT fk_mantenimientos_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos_schema.vehiculos (id) ON DELETE CASCADE,
    CONSTRAINT ck_mantenimientos_tipo     CHECK (tipo IN ('PREVENTIVO', 'CORRECTIVO')),
    CONSTRAINT ck_mantenimientos_costo    CHECK (costo >= 0)
);

CREATE INDEX idx_mantenimientos_vehiculo_fecha ON vehiculos_schema.mantenimientos (vehiculo_id, fecha DESC);

-- -----------------------------------------------------------------------------
-- vehiculos_schema.registros_combustible (con generated column)
-- -----------------------------------------------------------------------------
CREATE TABLE vehiculos_schema.registros_combustible (
    id                  BIGSERIAL    PRIMARY KEY,
    vehiculo_id         BIGINT       NOT NULL,
    fecha               TIMESTAMP    NOT NULL,
    litros              NUMERIC(8,2) NOT NULL,
    costo_total         NUMERIC(10,2) NOT NULL,
    costo_por_litro     NUMERIC(8,4) GENERATED ALWAYS AS (costo_total / NULLIF(litros, 0)) STORED,
    kilometraje_actual  INTEGER      NOT NULL,
    estacion            VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),

    CONSTRAINT fk_combustible_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos_schema.vehiculos (id) ON DELETE CASCADE,
    CONSTRAINT ck_combustible_litros   CHECK (litros > 0),
    CONSTRAINT ck_combustible_costo    CHECK (costo_total > 0),
    CONSTRAINT ck_combustible_km       CHECK (kilometraje_actual >= 0)
);

CREATE INDEX idx_combustible_vehiculo_fecha ON vehiculos_schema.registros_combustible (vehiculo_id, fecha DESC);

-- -----------------------------------------------------------------------------
-- vehiculos_schema.inspecciones
-- -----------------------------------------------------------------------------
CREATE TABLE vehiculos_schema.inspecciones (
    id                  BIGSERIAL    PRIMARY KEY,
    vehiculo_id         BIGINT       NOT NULL,
    tipo                VARCHAR(20)  NOT NULL,
    fecha               DATE         NOT NULL,
    resultado           VARCHAR(20)  NOT NULL,
    archivo_url         VARCHAR(500),
    observaciones       TEXT,
    proxima_inspeccion  DATE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50),
    deleted_at          TIMESTAMP,

    CONSTRAINT fk_inspecciones_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos_schema.vehiculos (id) ON DELETE CASCADE,
    CONSTRAINT ck_inspecciones_tipo     CHECK (tipo IN ('TECNICA', 'SOAT', 'INTERNA')),
    CONSTRAINT ck_inspecciones_result   CHECK (resultado IN ('APROBADA', 'REPROBADA', 'CONDICIONADA'))
);

CREATE INDEX idx_inspecciones_vehiculo_fecha ON vehiculos_schema.inspecciones (vehiculo_id, fecha DESC);

-- -----------------------------------------------------------------------------
-- vehiculos_schema.documentos_vehiculo
-- -----------------------------------------------------------------------------
CREATE TABLE vehiculos_schema.documentos_vehiculo (
    id                BIGSERIAL    PRIMARY KEY,
    vehiculo_id       BIGINT       NOT NULL,
    tipo              VARCHAR(50)  NOT NULL,
    numero            VARCHAR(100),
    url_archivo       VARCHAR(500) NOT NULL,
    fecha_emision     DATE,
    fecha_vencimiento DATE,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50),
    deleted_at        TIMESTAMP,

    CONSTRAINT fk_documentos_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos_schema.vehiculos (id) ON DELETE CASCADE
);

CREATE INDEX idx_documentos_vehiculo ON vehiculos_schema.documentos_vehiculo (vehiculo_id);
