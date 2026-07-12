-- =============================================================================
-- V2__Add_Plantillas.sql - MS-Notificaciones
-- Tabla para almacenar plantillas de email reutilizables con variables
-- =============================================================================

-- Añadir tabla plantillas (para templates de email con variables {{variable}})
CREATE TABLE notificaciones_schema.plantillas (
    id              BIGSERIAL    PRIMARY KEY,
    codigo          VARCHAR(100) NOT NULL UNIQUE,                      -- e.g., "password-reset", "usuario-bloqueado"
    nombre          VARCHAR(255) NOT NULL,                             -- Nombre descriptivo
    descripcion     TEXT,                                              -- Descripción para admins
    asunto          VARCHAR(255) NOT NULL,                             -- Asunto del email
    contenido       TEXT         NOT NULL,                             -- HTML con variables {{variable}}
    activa          BOOLEAN      NOT NULL DEFAULT TRUE,                -- Habilitar/deshabilitar
    variables       JSONB,                                             -- Metadata de variables esperadas [{name: "nombre", tipo: "string"}]
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    deleted_at      TIMESTAMP,

    CONSTRAINT ck_plantillas_codigo_no_vacio CHECK (LENGTH(TRIM(codigo)) > 0)
);

CREATE INDEX idx_plantillas_codigo      ON notificaciones_schema.plantillas (codigo) WHERE deleted_at IS NULL;
CREATE INDEX idx_plantillas_activa      ON notificaciones_schema.plantillas (activa) WHERE deleted_at IS NULL;
CREATE INDEX idx_plantillas_deleted_at  ON notificaciones_schema.plantillas (deleted_at);

-- Comentario descriptivo
COMMENT ON TABLE notificaciones_schema.plantillas IS
'Plantillas reutilizables de email. Contiene HTML con variables {{var}} que se reemplazan al enviar.
Ejemplo: contenido = "<p>Hola {{nombre}}, tu enlace es {{resetLink}}</p>"';

COMMENT ON COLUMN notificaciones_schema.plantillas.variables IS
'Array JSON con metadata de variables esperadas. Ej: [{"name":"nombre","tipo":"string","descripcion":"Nombre del usuario"}]';
