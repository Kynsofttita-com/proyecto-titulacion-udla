# Docker — Infraestructura del proyecto

Esta carpeta contiene los archivos de Docker Compose y el Dockerfile compartido para levantar el entorno del proyecto en local y en CI.

**Última actualización:** 2026-07-17 (Reorganización de estructura e integración de infraestructura).

---

## Archivos

| Archivo | Propósito |
|---------|-----------|
| [`docker-compose.yml`](./docker-compose.yml) | **Stack completo** (14 contenedores: BD + Messaging + Eureka + Gateway + 8 MS + Frontend + Jenkins). Archivo único y principal. |
| [`../../backend/Dockerfile.spring`](../../backend/Dockerfile.spring) | Dockerfile **multi-stage parametrizado** compartido por todos los servicios Java (Eureka, Gateway, 8 MS). Build cacheable. |
| [`../postgres/init-schemas.sql`](../postgres/init-schemas.sql) | Script ejecutado al primer arranque de Postgres. Crea los 9 schemas. |
| [`../jenkins/Dockerfile`](../jenkins/Dockerfile) | Dockerfile para Jenkins CI/CD. |

> El `.env` de esta carpeta **no se commitea** (está en `.gitignore`). Ver `.env.example` en la raíz del proyecto para el template.

---

## Servicios — `docker-compose.yml` (14 contenedores completo)

Incluye infraestructura **más** servicios core, microservicios y herramientas:

### Infraestructura
| Servicio | Puerto | Health |
|----------|--------|--------|
| **PostgreSQL 15** | 5432 | healthcheck via `pg_isready` |
| **RabbitMQ 3.12** | 5672 / 15672 | healthcheck via `rabbitmq-diagnostics` |
| **Adminer** | 8089 | http://localhost:8089 |

### Servicios Core
| Servicio | Puerto | Health |
|----------|--------|--------|
| **Eureka Server** | 8761 | http://localhost:8761/actuator/health |
| **API Gateway** | 8080 | http://localhost:8080/actuator/health |
| **Frontend** | 3000 | http://localhost:3000 |

### Microservicios (8)
| Servicio | Puerto | Health |
|----------|--------|--------|
| **MS-Auth** | 8081 | http://localhost:8081/actuator/health |
| **MS-Estudiantes** | 8082 | http://localhost:8082/actuator/health |
| **MS-Instructores** | 8083 | http://localhost:8083/actuator/health |
| **MS-Vehículos** | 8084 | http://localhost:8084/actuator/health |
| **MS-Asignaciones** | 8085 | http://localhost:8085/actuator/health |
| **MS-Cobros** | 8086 | http://localhost:8086/actuator/health |
| **MS-Reportes** | 8087 | http://localhost:8087/actuator/health |
| **MS-Notificaciones** | 8088 | http://localhost:8088/actuator/health |

### Herramientas
| Servicio | Puerto | Descripción |
|----------|--------|------------|
| **Jenkins** | 8090 | http://localhost:8090 (CI/CD pipeline) |

**Credenciales admin:** `admin@escuela.local` / `Admin123!` (fijado correctamente desde Sprint 9 con migración `V6__fix_admin_password_hash.sql` en ms-auth).

---

## `Dockerfile.spring` — Patrón multi-stage parametrizado

Un solo Dockerfile sirve para los 10 servicios Java (Eureka + Gateway + 8 MS). Parámetros:

- `MODULE` — nombre del módulo Maven (ej. `ms-auth`, `api-gateway`)
- `SERVICE_PORT` — puerto donde escucha el servicio

### Configuraciones clave (Sprint 9 - Estabilización)

| Configuración | Valor | Por qué |
|---|---|---|
| **TZ JVM** | `JAVA_OPTS=-Duser.timezone=America/Guayaquil` | Sin esto, `LocalDateTime.now()` dentro del contenedor devuelve UTC y se persiste con desfase de +5 h. Ver `DECISIONES.md §25.1`. |
| **Container support** | `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` | JVM respeta límites de memoria del contenedor |
| **Healthcheck** | `--interval=30s --timeout=10s --start-period=180s --retries=10` | `start-period=180s` y `retries=10` necesarios porque en runners gratuitos de GitHub Actions Spring tarda ~200 s en arrancar (vs ~30 s en laptop). Ver `DECISIONES.md §25.2`. |
| **Imagen runtime** | `eclipse-temurin:21-jre-alpine` | JRE 21 ligera (sin JDK) con usuario no-root |

### Estructura multi-stage

- **Stage 1 (build):** maven 3.9 + Java 21 → compila el módulo + shared libs + descarga dependencias cacheadas
- **Stage 2 (runtime):** JRE 21 alpine + usuario no-root + healthcheck

---

## Schemas de PostgreSQL

Al levantar Postgres por primera vez se ejecuta automáticamente [`../postgres/init-schemas.sql`](../postgres/init-schemas.sql) que crea los **9 schemas**:

`auth_schema`, `estudiantes_schema`, `instructores_schema`, `vehiculos_schema`, `asignaciones_schema`, `cobros_schema`, `reportes_schema`, `notificaciones_schema`, `shared_schema`.

### Migraciones Flyway (22 al 2026-05-26)

Cada microservicio versiona sus migraciones independientemente y las aplica al arrancar:

| MS | Migraciones | Sprint inicial → última |
|----|-------------|--------------------------|
| `ms-auth` | V1, V1_5 (seed), V2, V3, V4, V5, V6 | Sprint 2 → Sprint 9 (fix bcrypt admin) |
| `ms-estudiantes` | V1, V2, V3, V4, V5 | Sprint 2 → Sprint 9 (estados + situacion_pago + minutos_completados) |
| `ms-instructores` | V1, V2 | Sprint 2 → Sprint 9 (contratos) |
| `ms-vehiculos` | V1, V2 | Sprint 2 → Sprint 9 (tipos_combustible + campos vehículo) |
| `ms-asignaciones` | V1, V2 | Sprint 2 → Sprint 9 (kilometraje E2E) |
| `ms-cobros` | V1, V2 | Sprint 2 → Sprint 9 (factura_cuotas) |
| `ms-notificaciones` | V1 | Sprint 2 |
| `ms-reportes` | V1 | Sprint 2 |

Ver detalle completo en [`docs/database/schema.md §6`](../../docs/database/schema.md#6-migraciones-flyway-aplicadas).

---

## Comandos comunes

### Desde la raíz del proyecto

```bash
# Stack COMPLETO (14 contenedores: BD + MS + Frontend + Jenkins)
docker compose -f infrastructure/docker/docker-compose.yml up -d

# Stack completo + rebuild de imágenes (tras cambios de código)
docker compose -f infrastructure/docker/docker-compose.yml up -d --build

# Esperar a que todos los healthchecks pasen (recomendado para CI)
docker compose -f infrastructure/docker/docker-compose.yml up -d --wait --wait-timeout 600

# Ver logs en tiempo real
docker compose -f infrastructure/docker/docker-compose.yml logs -f

# Logs de un servicio específico
docker compose -f infrastructure/docker/docker-compose.yml logs -f ms-auth

# Estado de los contenedores
docker compose -f infrastructure/docker/docker-compose.yml ps

# Detener (preserva volúmenes con la data)
docker compose -f infrastructure/docker/docker-compose.yml down

# Detener y BORRAR la data (CUIDADO)
docker compose -f infrastructure/docker/docker-compose.yml down -v
```

### Desde esta carpeta (infrastructure/docker/)

```bash
# Si estás en infrastructure/docker/, puedes ejecutar sin -f:
docker compose up -d
docker compose up -d --build
docker compose ps
docker compose logs -f ms-auth
docker compose down
```

### Tiempos de arranque esperados

| Entorno | Tiempo a "todos healthy" | Notas |
|---------|--------------------------|-------|
| Laptop dev (16+ GB RAM) | ~50–60 s | Primera vez ~3 min si no hay imágenes cacheadas |
| Runner GHA gratis | ~180–250 s | Por eso `start-period=180s` y `wait-timeout 600` |
| Producción Oracle Cloud (Sprint 13) | ~60–90 s estimado | Pendiente de validar |

---

## Workflows CI/CD que usan estos archivos

| Workflow | Archivo usado |
|----------|---------------|
| `docker-build.yml` | `Dockerfile.spring` (build de imagen test de `eureka-server`) |
| `integration-tests.yml` | Services Postgres + RabbitMQ como GitHub Services (no usa estos compose) |
| `smoke-e2e.yml` | `docker-compose.yml` completo (14 contenedores) + login admin con retry + 12 endpoints REST + 404/400 ProblemDetail |

Triggers `paths:` filtran cuándo correr — ver `.github/workflows/` y `DECISIONES.md §25.5`.

---

## Configuración por variables de entorno

Las credenciales y puertos se configuran vía `.env` en la raíz del proyecto. Ver `.env.example`. Las variables clave:

- `POSTGRES_*` — credenciales de Postgres
- `RABBITMQ_*` — credenciales de RabbitMQ
- `JWT_SECRET` — clave HS512 de 512 bits para JWT (generar con `openssl rand -base64 64`)
- `MAIL_*` — Mailtrap (dev) o Gmail SMTP (prod)
- `MINIO_*` — credenciales MinIO
- `EUREKA_URL` — URL del servidor Eureka

---

## Notas

- **Adminer** solo para desarrollo; NO usar en producción.
- **MinIO** usa credenciales por defecto en dev; cambiar para producción.
- El **stack completo** tarda ~50–60 s en pasar todos los healthchecks la primera vez en laptop; bastante más en runners GHA gratuitos.
- Para limpiar imágenes huérfanas: `docker system prune -a` (cuidado, borra imágenes no usadas).
- Para reset total (data incluida): `docker compose -f infrastructure/docker/docker-compose.yml down -v` + borrar `data/` si existe.

---

## Referencias

- [`docs/database/schema.md`](../../docs/database/schema.md) — Modelo BD completo (41 tablas, 22 migraciones)
- [`DECISIONES.md §25`](../../DECISIONES.md) — ADR Sprint 9: Estabilización CI/CD y plataforma (TZ JVM, healthchecks, V6 bcrypt, 404/400)
- [`PLAN_FASES.md §5`](../../PLAN_FASES.md) — Detalle del Sprint 9 (Estabilización)
- [`.github/workflows/`](../../.github/workflows/) — 5 workflows CI/CD activos
