---
name: generate-flyway-migration
description: Generate a Flyway database migration script (versioned SQL) for PostgreSQL. Handles table creation, ALTERs, indexes, constraints, audit fields, and rollback documentation. Follows project naming conventions (V<timestamp>__description.sql) and Spanish/snake_case schema conventions.
---

# Generate Flyway Migration Skill

Creates a properly versioned and documented Flyway migration file.

## Inputs Needed

Ask the user for:
1. **Service**: which microservice
2. **Operation type**: CREATE TABLE, ALTER TABLE, ADD INDEX, INSERT DATA, etc.
3. **Description**: short snake_case description (e.g., `create_estudiantes_table`)
4. **Schema details**: table name, columns, types, constraints

## File Naming

Format: `V<YYYYMMDDHHMMSS>__<description>.sql`

Examples:
- `V20260106120000__create_estudiantes_table.sql`
- `V20260106120500__add_index_estudiantes_email.sql`
- `V20260106120800__alter_estudiantes_add_telefono.sql`

**Generate timestamp dynamically** using current UTC time formatted as `YYYYMMDDHHMMSS`.

Location: `microservices/<service>/src/main/resources/db/migration/`

## Templates

### CREATE TABLE Template

```sql
-- ============================================================================
-- Migration: V<timestamp>__create_<table>_table.sql
-- Author: <developer>
-- Date: <YYYY-MM-DD>
-- Description: Create <table_name> table for <feature>
-- Rollback: DROP TABLE <table_name> CASCADE;
-- ============================================================================

CREATE TABLE <table_name> (
    -- Primary key
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    
    -- Domain fields (Spanish naming, snake_case)
    cedula VARCHAR(10) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefono VARCHAR(10),
    fecha_nacimiento DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    
    -- Foreign keys
    instructor_id BIGINT,
    
    -- Audit fields (always include)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Soft delete (if needed)
    -- deleted_at TIMESTAMP WITH TIME ZONE,
    -- deleted_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT ck_<table>_cedula_format 
        CHECK (cedula ~ '^[0-9]{10}$'),
    
    CONSTRAINT ck_<table>_telefono_format 
        CHECK (telefono IS NULL OR telefono ~ '^0[0-9]{9}$'),
    
    CONSTRAINT ck_<table>_email_format 
        CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    
    CONSTRAINT ck_<table>_estado 
        CHECK (estado IN ('ACTIVO', 'INACTIVO', 'GRADUADO', 'RETIRADO')),
    
    CONSTRAINT fk_<table>_instructor 
        FOREIGN KEY (instructor_id) 
        REFERENCES instructores(id) 
        ON DELETE SET NULL
);

-- ============================================================================
-- Indexes
-- ============================================================================

-- Foreign key index (always)
CREATE INDEX idx_<table>_instructor_id 
    ON <table_name>(instructor_id);

-- Frequently filtered columns
CREATE INDEX idx_<table>_estado 
    ON <table_name>(estado);

-- Frequently sorted columns
CREATE INDEX idx_<table>_apellidos 
    ON <table_name>(apellidos);

-- Composite index for common query (newest first by status)
CREATE INDEX idx_<table>_estado_created 
    ON <table_name>(estado, created_at DESC);

-- ============================================================================
-- Comments (table & column documentation)
-- ============================================================================

COMMENT ON TABLE <table_name> IS '<Spanish description>';
COMMENT ON COLUMN <table_name>.cedula IS 'Cédula ecuatoriana de 10 dígitos con dígito verificador';
COMMENT ON COLUMN <table_name>.estado IS 'Estado: ACTIVO, INACTIVO, GRADUADO, RETIRADO';
COMMENT ON COLUMN <table_name>.version IS 'Optimistic locking version';
```

### ALTER TABLE - Add Column

```sql
-- ============================================================================
-- Migration: V<timestamp>__alter_<table>_add_<column>.sql
-- Description: Add <column> column to <table>
-- Rollback: ALTER TABLE <table> DROP COLUMN <column>;
-- ============================================================================

ALTER TABLE <table_name>
ADD COLUMN <column_name> <type> <nullable> <default>,
ADD CONSTRAINT ck_<table>_<column> CHECK (<rule>);

-- Backfill existing rows (if needed)
UPDATE <table_name> SET <column> = <default_value> WHERE <column> IS NULL;

-- Make NOT NULL after backfill (if needed)
ALTER TABLE <table_name> ALTER COLUMN <column> SET NOT NULL;

-- Index if frequently queried
CREATE INDEX idx_<table>_<column> ON <table_name>(<column>);

COMMENT ON COLUMN <table_name>.<column> IS '<description>';
```

### ALTER TABLE - Drop Column

> ⚠️ **Always wait at least 2 deploys** between deprecating column in code and dropping it. Otherwise, rolling back is impossible.

```sql
-- ============================================================================
-- Migration: V<timestamp>__alter_<table>_drop_<column>.sql
-- Description: Drop deprecated column <column>
-- Prerequisite: Column unused for 2+ deploys (verified)
-- Rollback: Cannot rollback automatically. Restore from backup if needed.
-- ============================================================================

ALTER TABLE <table_name> DROP COLUMN IF EXISTS <column>;
```

### Add Index (concurrently in production)

```sql
-- ============================================================================
-- Migration: V<timestamp>__add_index_<table>_<columns>.sql
-- Description: Add index for <reason>
-- Note: Use CREATE INDEX CONCURRENTLY to avoid table locks (large tables)
-- ============================================================================

-- For dev/staging:
CREATE INDEX IF NOT EXISTS idx_<table>_<columns>
    ON <table_name>(<column1>, <column2>);

-- For production with large tables, use:
-- CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_<table>_<columns>
--     ON <table_name>(<column1>, <column2>);
-- (Note: CREATE INDEX CONCURRENTLY cannot be in a transaction; Flyway needs `<<EOF` workaround)
```

### Insert Reference Data (Repeatable Migration `R__`)

```sql
-- ============================================================================
-- Migration: R__seed_<table>_reference_data.sql
-- Description: Reference data for <table> (re-runs on checksum change)
-- ============================================================================

INSERT INTO <table_name> (id, codigo, descripcion)
VALUES 
    (1, 'A1', 'Motocicleta hasta 200cc'),
    (2, 'A', 'Motocicleta más de 200cc'),
    (3, 'B', 'Vehículo particular'),
    (4, 'C1', 'Camión liviano hasta 4500kg'),
    (5, 'C', 'Camión pesado más de 4500kg'),
    (6, 'D1', 'Bus liviano'),
    (7, 'D', 'Bus pesado'),
    (8, 'E1', 'Articulado liviano'),
    (9, 'E', 'Articulado pesado'),
    (10, 'F', 'Vehículo adaptado'),
    (11, 'G', 'Maquinaria pesada')
ON CONFLICT (id) DO UPDATE 
SET descripcion = EXCLUDED.descripcion;
```

## Workflow

1. **Determine** migration type (CREATE, ALTER, INDEX, INSERT)
2. **Generate** timestamp: `date -u +%Y%m%d%H%M%S`
3. **Construct** filename: `V<timestamp>__<description>.sql`
4. **Generate** the SQL with proper structure
5. **Add** rollback note in comment (mental rollback for ALTER/DROP)
6. **Save** to `microservices/<service>/src/main/resources/db/migration/`
7. **Test** locally:
   ```bash
   cd microservices/<service>
   mvn flyway:migrate
   ```
8. **Verify** schema with: `mvn flyway:info`
9. **Run** application: `mvn spring-boot:run` (Hibernate validates schema)

## Quality Checklist

- [ ] Filename follows format `V<timestamp>__<description>.sql`
- [ ] Header comment with author, date, description, rollback
- [ ] Audit columns included (created_at, updated_at, created_by, updated_by, version)
- [ ] Constraints enforce business rules (CHECK)
- [ ] Foreign keys defined with proper ON DELETE behavior
- [ ] Indexes on FK and queried columns
- [ ] Column comments document non-obvious fields
- [ ] No hardcoded values (use variables/parameters where possible)
- [ ] Tested locally before commit
- [ ] No DROP statements without 2-deploy notice

## Anti-Patterns to Avoid

❌ Don't modify existing migrations after they're committed (Flyway checksum check fails)
❌ Don't put multiple unrelated changes in one migration
❌ Don't use `IF EXISTS` on CREATE TABLE in initial migrations (silent failures)
❌ Don't drop columns that might still be in use elsewhere
❌ Don't forget to backfill data before adding NOT NULL
❌ Don't use timestamp for VARCHAR — use proper TIMESTAMP type
❌ Don't store money as FLOAT — use NUMERIC(12, 2)

## Notes

- All timestamps are TIMESTAMP WITH TIME ZONE (UTC at storage)
- Use NUMERIC(12, 2) for money (USD, 2 decimals)
- Use BIGINT for IDs (future-proof against overflow)
- Use VARCHAR(n) with explicit length for bounded strings
- Use TEXT only for unbounded text (descriptions, comments)
- Always reference `database-designer` agent for schema decisions
