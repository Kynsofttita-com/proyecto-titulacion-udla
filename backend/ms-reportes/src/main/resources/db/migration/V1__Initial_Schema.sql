-- =============================================================================
-- V1__Initial_Schema.sql - MS-Reportes
-- 2 tablas en reportes_schema (cache + auditoria de ejecuciones)
-- Ver docs/database/schema.md sección 11
-- =============================================================================

-- -----------------------------------------------------------------------------
-- reportes_schema.cache_reportes
-- -----------------------------------------------------------------------------
CREATE TABLE reportes_schema.cache_reportes (
    id                BIGSERIAL    PRIMARY KEY,
    tipo              VARCHAR(50)  NOT NULL,
    parametros        JSONB        NOT NULL,
    datos             JSONB        NOT NULL,
    hash_parametros   VARCHAR(64)  NOT NULL,                  -- SHA-256 de parametros para lookup eficiente
    generado_en       TIMESTAMP    NOT NULL DEFAULT NOW(),
    expiracion        TIMESTAMP    NOT NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50),

    CONSTRAINT uq_cache_reportes_hash UNIQUE (hash_parametros)
);

CREATE INDEX idx_cache_reportes_hash       ON reportes_schema.cache_reportes (hash_parametros);
CREATE INDEX idx_cache_reportes_expiracion ON reportes_schema.cache_reportes (expiracion);

COMMENT ON TABLE reportes_schema.cache_reportes IS 'Cache de reportes generados (lookup por hash de parametros)';

-- -----------------------------------------------------------------------------
-- reportes_schema.ejecuciones_reporte
-- -----------------------------------------------------------------------------
CREATE TABLE reportes_schema.ejecuciones_reporte (
    id                BIGSERIAL    PRIMARY KEY,
    tipo_reporte      VARCHAR(50)  NOT NULL,
    parametros        JSONB        NOT NULL,
    usuario_id        BIGINT       NOT NULL,
    fecha_ejecucion   TIMESTAMP    NOT NULL DEFAULT NOW(),
    duracion_ms       INTEGER,
    estado            VARCHAR(20)  NOT NULL,
    archivo_url       VARCHAR(500),
    formato           VARCHAR(10),
    error_mensaje     TEXT,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by        VARCHAR(50),

    CONSTRAINT ck_ejecuciones_estado  CHECK (estado IN ('EXITO', 'ERROR')),
    CONSTRAINT ck_ejecuciones_formato CHECK (formato IS NULL OR formato IN ('PDF', 'EXCEL')),
    CONSTRAINT ck_ejecuciones_duracion CHECK (duracion_ms IS NULL OR duracion_ms >= 0)
);

CREATE INDEX idx_ejecuciones_tipo_fecha ON reportes_schema.ejecuciones_reporte (tipo_reporte, fecha_ejecucion DESC);
CREATE INDEX idx_ejecuciones_usuario    ON reportes_schema.ejecuciones_reporte (usuario_id);
