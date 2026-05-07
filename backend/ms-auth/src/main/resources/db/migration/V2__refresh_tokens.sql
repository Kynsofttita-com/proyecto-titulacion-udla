-- =============================================================================
-- V2 - auth_schema.refresh_tokens
-- =============================================================================
-- Tabla para persistir refresh tokens emitidos por MS-Auth.
-- Necesaria para implementar refresh token rotation: cada uso del refresh
-- genera uno nuevo y revoca el anterior (deteccion de tokens robados).
--
-- No usa BaseEntity (audit fields) porque solo hace INSERT y UPDATE de un
-- unico campo (revocado), no necesita createdBy/updatedBy.
-- =============================================================================

CREATE TABLE auth_schema.refresh_tokens (
    id            BIGSERIAL    PRIMARY KEY,
    usuario_id    BIGINT       NOT NULL,
    jti           UUID         NOT NULL,
    expira_en     TIMESTAMP    NOT NULL,
    revocado      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    revocado_at   TIMESTAMP,

    CONSTRAINT uq_refresh_tokens_jti UNIQUE (jti),
    CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (usuario_id)
        REFERENCES auth_schema.usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_usuario_revocado
    ON auth_schema.refresh_tokens (usuario_id, revocado);

CREATE INDEX idx_refresh_tokens_expira
    ON auth_schema.refresh_tokens (expira_en)
    WHERE revocado = FALSE;

COMMENT ON TABLE auth_schema.refresh_tokens IS
    'Refresh tokens activos emitidos por MS-Auth. Soporta rotation y revocacion.';
COMMENT ON COLUMN auth_schema.refresh_tokens.jti IS
    'JWT ID (UUID) del refresh token. Usado para validar y revocar.';
COMMENT ON COLUMN auth_schema.refresh_tokens.revocado IS
    'TRUE si el token fue revocado (logout o rotation). No se borra para auditoria.';
