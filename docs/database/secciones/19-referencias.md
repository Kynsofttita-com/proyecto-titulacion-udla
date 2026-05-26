# 19. Referencias

[← Volver al índice](../schema.md)

> Enlaces a documentación complementaria, decisiones técnicas y código fuente relacionado con el modelo de datos.

---

## Documentos del proyecto

| Documento | Propósito |
|-----------|-----------|
| [DECISIONES.md](../../../DECISIONES.md) | Decisiones técnicas formales del proyecto |
| [CLAUDE.md](../../../CLAUDE.md) | Contexto general del proyecto y guía operativa |
| [PLAN_FASES.md](../../../PLAN_FASES.md) | Plan vigente de sprints (5–12) |
| [er-diagram.dbml](../er-diagram.dbml) | Modelo de BD en formato DBML para dbdiagram.io |

---

## Secciones relevantes de `DECISIONES.md`

| Sección | Tema |
|---------|------|
| §2 | Stack técnico (incluye PostgreSQL 15) |
| §4 | Bases de Datos (estrategia general: 1 instancia, 9 schemas) |
| §11 | Validaciones específicas Ecuador (cédula, RUC, placa, teléfono) |
| §15 | API Design + formato de errores RFC 7807 |
| §24 | ADR Sprint 10: refactor de dominio Grupo A (estados extendidos, factura_cuotas, kilometraje, 6 validaciones) |
| §25 | ADR Sprint 10: estabilización CI/CD y plataforma (TZ JVM, V6 bcrypt fix, ProblemDetail global, IdempotencyStore deuda) |

---

## Código fuente

| Ruta | Contenido |
|------|-----------|
| `backend/<ms>/src/main/resources/db/migration/V*.sql` | Migraciones Flyway por microservicio |
| `backend/<ms>/src/main/java/com/escuela/<ms>/entity/` | Entidades JPA correspondientes a las tablas |
| `backend/<ms>/src/main/java/com/escuela/<ms>/repository/` | Repositorios Spring Data JPA |
| `backend/shared/common-events/` | DTOs de eventos RabbitMQ |
| `backend/shared/common-security/` | `BaseEntity`, `AuditorAware`, audit fields automáticos |
| `backend/shared/common-validation/` | Custom validators (`@CedulaEcuador`, `@PlacaEcuador`, etc.) |
| `infrastructure/postgres/init-schemas.sql` | Script que crea los 9 schemas al primer arranque del contenedor |

---

## Referencias externas

- **[PostgreSQL 15 docs](https://www.postgresql.org/docs/15/)** — Referencia oficial del motor de BD
- **[Flyway docs](https://documentation.red-gate.com/flyway/)** — Herramienta de versionado de schema
- **[Mermaid.js](https://mermaid.js.org/syntax/entityRelationshipDiagram.html)** — Sintaxis de diagramas ER
- **[dbdiagram.io](https://dbdiagram.io)** — Visor del archivo `er-diagram.dbml`
- **[RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807)** — Problem Details for HTTP APIs (formato de errores)

---

## Convenciones de versionado del documento

Este documento se versiona junto con el código en el mismo repositorio:

- Cuando se agrega una migración nueva (`V*`), actualizar el archivo de su schema correspondiente en `secciones/` y la tabla de [6. Migraciones Flyway aplicadas](06-migraciones-flyway-aplicadas.md).
- Cuando se cambia un constraint o se agrega/quita una columna, actualizar la sección de tablas del schema afectado.
- Cuando se modifica un evento RabbitMQ o se agrega una validación cross-MS, actualizar [16. Relaciones cross-microservicio](16-relaciones-cross-microservicio.md) y [17. Validaciones obligatorias al crear asignación](17-validaciones-obligatorias-crear-asignacion.md).
- Cuando se agrega un schema nuevo, crear un archivo `secciones/XX-<nombre>-schema.md` y actualizar el índice principal.
