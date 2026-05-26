# Schema de Base de Datos

**Sistema de Control Administrativo y Financiero para Escuelas de Conducción**

> Documento índice del modelo de datos. La descripción detallada de cada sección está en archivos separados dentro de `secciones/` para mejorar la legibilidad y el rendimiento del renderizado Mermaid.
>
> **Versión:** 3.0 (Sprint 10 — 19 secciones independientes)
> **Database:** `escuela_db` (PostgreSQL 15)
> **Estrategia:** 1 instancia, 9 schemas separados (1 por microservicio + `shared_schema`)
> **Última actualización:** 2026-05-26

---

## Índice de secciones

| # | Documento | Contenido |
|---|-----------|-----------|
| 1 | [Convenciones generales](secciones/01-convenciones-generales.md) | Naming, tipos PostgreSQL, FKs cross-schema |
| 2 | [Audit fields y soft delete](secciones/02-audit-fields-y-soft-delete.md) | Política de auditoría temporal y borrado lógico |
| 3 | [Validaciones específicas Ecuador](secciones/03-validaciones-especificas-ecuador.md) | Cédula, RUC, placa, teléfono, monto USD |
| 4 | [Arquitectura de schemas](secciones/04-arquitectura-de-schemas.md) | Distribución de los 9 schemas en la BD |
| 5 | [Diagrama ER global](secciones/05-diagrama-er-global.md) | Vista de conjunto del modelo de datos |
| 6 | [Migraciones Flyway aplicadas](secciones/06-migraciones-flyway-aplicadas.md) | 22 migraciones al 2026-05-26 |
| 7 | [Schema: `auth_schema`](secciones/07-auth-schema.md) | Autenticación, autorización, configuración (12 tablas) |
| 8 | [Schema: `estudiantes_schema`](secciones/08-estudiantes-schema.md) | Gestión de estudiantes, progreso, asistencia (5 tablas) |
| 9 | [Schema: `instructores_schema`](secciones/09-instructores-schema.md) | Instructores, certificaciones, disponibilidad, contratos (4 tablas) |
| 10 | [Schema: `vehiculos_schema`](secciones/10-vehiculos-schema.md) | Flota, mantenimientos, combustible, inspecciones (6 tablas) |
| 11 | [Schema: `asignaciones_schema`](secciones/11-asignaciones-schema.md) | Clases tripartitas + kilometraje E2E (3 tablas) |
| 12 | [Schema: `cobros_schema`](secciones/12-cobros-schema.md) | Facturación, pagos, crédito a cuotas (4 tablas) |
| 13 | [Schema: `notificaciones_schema`](secciones/13-notificaciones-schema.md) | Notificaciones in-app + emails + preferencias (3 tablas) |
| 14 | [Schema: `reportes_schema`](secciones/14-reportes-schema.md) | Cache y ejecuciones de reportes (2 tablas) |
| 15 | [Schema: `shared_schema`](secciones/15-shared-schema.md) | Auditoría centralizada + idempotencia (2 tablas) |
| 16 | [Relaciones cross-microservicio](secciones/16-relaciones-cross-microservicio.md) | Mapa de referencias + eventos RabbitMQ |
| 17 | [Validaciones obligatorias al crear asignación](secciones/17-validaciones-obligatorias-crear-asignacion.md) | Las 6 validaciones cross-MS (Sprint 10) |
| 18 | [Datos seed iniciales](secciones/18-datos-seed-iniciales.md) | Roles, permisos, admin, catálogos Ecuador |
| 19 | [Referencias](secciones/19-referencias.md) | Documentos relacionados, código fuente, recursos externos |

**Total: 9 schemas, 41 tablas, 22 migraciones, 1 base de datos.**

---

## Resumen rápido de schemas

| Schema | Microservicio | Puerto | Tablas | Sección |
|--------|---------------|--------|--------|---------|
| `shared_schema` | Compartido | — | 2 | [§15](secciones/15-shared-schema.md) |
| `auth_schema` | MS-Auth | 8081 | 12 | [§7](secciones/07-auth-schema.md) |
| `estudiantes_schema` | MS-Estudiantes | 8082 | 5 | [§8](secciones/08-estudiantes-schema.md) |
| `instructores_schema` | MS-Instructores | 8083 | 4 | [§9](secciones/09-instructores-schema.md) |
| `vehiculos_schema` | MS-Vehículos | 8084 | 6 | [§10](secciones/10-vehiculos-schema.md) |
| `asignaciones_schema` | MS-Asignaciones | 8085 | 3 | [§11](secciones/11-asignaciones-schema.md) |
| `cobros_schema` | MS-Cobros | 8086 | 4 | [§12](secciones/12-cobros-schema.md) |
| `notificaciones_schema` | MS-Notificaciones | 8088 | 3 | [§13](secciones/13-notificaciones-schema.md) |
| `reportes_schema` | MS-Reportes | 8087 | 2 | [§14](secciones/14-reportes-schema.md) |

---

## Versión alternativa en DBML

Si el renderer Mermaid no funciona en tu entorno, el modelo equivalente está disponible en formato DBML:

- Archivo: [`er-diagram.dbml`](./er-diagram.dbml)
- Uso: pegar contenido en [dbdiagram.io](https://dbdiagram.io), exportar a PNG/SVG/PDF.

---

*Documento generado: 2026-05-22 (v1.0 — diseño inicial)*
*Reescrito: 2026-05-26 (v2.0 — consolidación con MODELO_BD_COMPLETO.md + estado real Sprint 10)*
*Partido en secciones: 2026-05-26 (v3.0 — 19 archivos independientes para óptimo renderizado Mermaid)*
