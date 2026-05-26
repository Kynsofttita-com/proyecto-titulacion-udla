# 1. Convenciones generales

[← Volver al índice](../schema.md)

> Convenciones de naming y tipos de datos comunes a todos los schemas del sistema.

---

## Naming

- **Tablas:** `snake_case`, plural (`estudiantes`, `instructores`, `facturas`)
- **Columnas:** `snake_case` (`fecha_matricula`, `id_estudiante`)
- **Primary keys:** `id` tipo `BIGSERIAL` (auto-increment)
- **Foreign keys:** `{tabla_singular}_id` (ej. `estudiante_id`, `instructor_id`)
- **Constraints:** `{tipo}_{tabla}_{campo}` (ej. `uq_estudiantes_cedula`, `fk_asignaciones_instructor`, `ck_estudiantes_estado`)
- **Índices:** `idx_{tabla}_{campo(s)}` (ej. `idx_estudiantes_estado`)

---

## Tipos estándar

| Concepto | Tipo PostgreSQL | Notas |
|----------|-----------------|-------|
| Identificador (PK) | `BIGSERIAL` | Auto-increment 64-bit |
| Texto corto | `VARCHAR(N)` | Tamaño explícito |
| Texto largo | `TEXT` | Sin límite |
| Booleano | `BOOLEAN` | TRUE/FALSE |
| Entero pequeño | `SMALLINT` o `INTEGER` | |
| Monto monetario | `NUMERIC(10,2)` | USD con 2 decimales |
| Fecha y hora | `TIMESTAMP` | Sin timezone (la JVM corre fija en `America/Guayaquil` por `Dockerfile.spring`) |
| Fecha sola | `DATE` | |
| UUID | `UUID` | Para JTI de refresh tokens, event IDs, etc. |
| JSON | `JSONB` | Binary JSON (más performante que JSON) |
| Enum (lista cerrada) | `VARCHAR(N) CHECK (col IN (...))` | Más flexible que tipo ENUM nativo |

---

## Foreign keys cross-schema: NO

> **Regla estricta:** las foreign keys SOLO existen entre tablas del MISMO schema. Entre microservicios, se almacenan IDs de referencia pero **sin** restricción FK a nivel BD.
>
> **Razón:** mantener desacoplamiento. Cada microservicio puede evolucionar su schema independientemente. La consistencia eventual entre MS se gestiona vía eventos RabbitMQ y/o llamadas Feign de validación cuando se necesita confirmación síncrona.

Ver [16. Relaciones cross-microservicio](16-relaciones-cross-microservicio.md) para el mapa completo de referencias entre microservicios.
