# 2. Audit fields y soft delete

[← Volver al índice](../schema.md)

> Política de auditoría temporal y borrado lógico aplicada a todas las tablas del sistema.

---

## Audit fields obligatorios (BaseEntity)

Todas las tablas que extienden `BaseEntity` incluyen:

```sql
created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
updated_at  TIMESTAMP,
created_by  VARCHAR(50),
updated_by  VARCHAR(50)
```

- `created_at` y `created_by` se establecen una sola vez al crear el registro.
- `updated_at` y `updated_by` se actualizan en cada modificación (gestionado vía JPA `@CreatedDate` / `@LastModifiedDate` / `@CreatedBy` / `@LastModifiedBy` con `AuditorAware` enlazado a `SecurityContextHolder`).
- `created_by` / `updated_by` guardan el email o user-id del usuario autenticado.

### Excepciones documentadas

| Tabla | Excepción | Motivo |
|-------|-----------|--------|
| `auth_schema.refresh_tokens` | Solo `created_at` y `revocado_at` | No usa BaseEntity. Solo aplica INSERT + UPDATE de un único campo (`revocado`) |
| `cobros_schema.pagos` | Sin `updated_at` ni `deleted_at` | Append-only por auditoría contable |
| `shared_schema.audit_log` | Sin audit fields ni soft delete | Append-only puro |
| `shared_schema.processed_events` | Solo `processed_at` | Registro inmutable de idempotencia |

---

## Soft delete

> **Política:** soft delete (`deleted_at TIMESTAMP NULL`) en todas las entidades EXCEPTO `pagos`, `audit_log` y `processed_events`.

```sql
deleted_at  TIMESTAMP NULL  -- NULL = registro activo, valor = registro borrado
```

Las queries siempre filtran por `deleted_at IS NULL` (gestionado a nivel repositorio o vía `@Where` annotation en JPA).

---

## Versionado optimista

Para tablas con alta concurrencia (ej. `facturas`, `asignaciones`):

```sql
version  BIGINT NOT NULL DEFAULT 0
```

Mapeado con `@Version` en JPA para optimistic locking.
