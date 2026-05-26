-- =============================================================================
-- V2__Add_Combustible_Y_Campos_Vehiculo.sql  -  MS-Vehiculos
-- Sprint 11: agrega tabla tipos_combustible (precios configurables por admin)
--            + campos detallados al vehiculo (motor, chasis, capacidad, combustible)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- vehiculos_schema.tipos_combustible
-- -----------------------------------------------------------------------------
CREATE TABLE vehiculos_schema.tipos_combustible (
    id              BIGSERIAL    PRIMARY KEY,
    codigo          VARCHAR(20)  NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    unidad          VARCHAR(20)  NOT NULL DEFAULT 'GALON',  -- GALON / LITRO / KWH
    precio_actual   NUMERIC(8,4) NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    observaciones   TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(50)  DEFAULT 'system',
    updated_by      VARCHAR(50),

    CONSTRAINT uq_tipos_combustible_codigo UNIQUE (codigo),
    CONSTRAINT ck_tipos_combustible_unidad CHECK (unidad IN ('GALON', 'LITRO', 'KWH')),
    CONSTRAINT ck_tipos_combustible_precio CHECK (precio_actual > 0)
);

CREATE INDEX idx_tipos_combustible_activo ON vehiculos_schema.tipos_combustible (activo);

COMMENT ON TABLE  vehiculos_schema.tipos_combustible IS
    'Catalogo de combustibles disponibles con precio actual configurable por el admin';
COMMENT ON COLUMN vehiculos_schema.tipos_combustible.codigo IS
    'EXTRA | SUPER | ECOPAIS | DIESEL | DIESEL_PREMIUM | ELECTRICO';
COMMENT ON COLUMN vehiculos_schema.tipos_combustible.unidad IS
    'GALON (combustibles liquidos en Ecuador), LITRO (alternativa), KWH (electricos)';
COMMENT ON COLUMN vehiculos_schema.tipos_combustible.precio_actual IS
    'Precio vigente por unidad. Admin lo actualiza cuando sube/baja el precio publico';

-- Seed con tipos vigentes en Ecuador (precios referenciales 2026 — ajustar segun necesidad)
INSERT INTO vehiculos_schema.tipos_combustible (codigo, nombre, unidad, precio_actual, observaciones) VALUES
    ('EXTRA',          'Gasolina Extra (87 octanos)',       'GALON', 2.4600, 'Combustible mas usado en autos basicos'),
    ('ECOPAIS',        'Gasolina Ecopais (87 oct + etanol)','GALON', 2.4600, 'Alternativa a Extra, mismo precio'),
    ('SUPER',          'Gasolina Super (92 octanos)',       'GALON', 3.8500, 'Para motores de alta compresion'),
    ('DIESEL',         'Diesel Premium',                    'GALON', 1.8000, 'Para camiones, buses y maquinaria'),
    ('ELECTRICO',      'Energia electrica',                 'KWH',   0.1100, 'Vehiculos electricos (tarifa residencial)');

-- -----------------------------------------------------------------------------
-- ALTER vehiculos_schema.vehiculos: agregar campos detallados
-- -----------------------------------------------------------------------------
ALTER TABLE vehiculos_schema.vehiculos
    ADD COLUMN numero_motor        VARCHAR(50),
    ADD COLUMN numero_chasis       VARCHAR(50),
    ADD COLUMN capacidad_pasajeros SMALLINT,
    ADD COLUMN tipo_combustible_id BIGINT;

ALTER TABLE vehiculos_schema.vehiculos
    ADD CONSTRAINT fk_vehiculos_tipo_combustible
        FOREIGN KEY (tipo_combustible_id)
        REFERENCES vehiculos_schema.tipos_combustible (id);

ALTER TABLE vehiculos_schema.vehiculos
    ADD CONSTRAINT ck_vehiculos_capacidad
        CHECK (capacidad_pasajeros IS NULL OR capacidad_pasajeros BETWEEN 1 AND 100);

-- Numero motor/chasis suelen tener formato libre alfanumerico; sin constraint estricto
CREATE INDEX idx_vehiculos_tipo_combustible
    ON vehiculos_schema.vehiculos (tipo_combustible_id)
    WHERE deleted_at IS NULL;

COMMENT ON COLUMN vehiculos_schema.vehiculos.numero_motor IS
    'Numero de motor (aparece en matricula). Identificador unico del motor segun fabricante.';
COMMENT ON COLUMN vehiculos_schema.vehiculos.numero_chasis IS
    'Numero de chasis (puede coincidir con VIN, suele aparecer en matricula).';
COMMENT ON COLUMN vehiculos_schema.vehiculos.capacidad_pasajeros IS
    'Capacidad maxima de pasajeros segun matricula (incluye conductor).';
COMMENT ON COLUMN vehiculos_schema.vehiculos.tipo_combustible_id IS
    'FK a tipos_combustible. Determina que combustible usa el vehiculo.';
