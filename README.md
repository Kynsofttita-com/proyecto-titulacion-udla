# Sistema de Control Administrativo y Financiero para Escuelas de Conducción

[![Backend CI](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/backend-ci.yml)
[![Docker Build](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/docker-build.yml/badge.svg)](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/docker-build.yml)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-green?logo=spring)](https://spring.io/projects/spring-cloud)
[![Vue.js](https://img.shields.io/badge/Vue.js-3-brightgreen?logo=vue.js)](https://vuejs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange?logo=rabbitmq)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://docs.docker.com/compose/)

**Proyecto de Titulación — Universidad de las Américas (UDLA)**

- **Autores:** Raúl Sebastián Cruz Baño, Hernán Mateo Jurado Moran
- **Tutor:** Víctor Javier Gómez Regalado
- **Ubicación:** Quito, Ecuador
- **Entrega final:** 5 de mayo de 2026

> 📚 **Documentos de referencia (orden de prioridad):**
> 1. [`DECISIONES.md`](./DECISIONES.md) — 30 decisiones técnicas finales (cerradas 2026-05-06)
> 2. [`SPRINTS_PLAN.xlsx`](./SPRINTS_PLAN.xlsx) — Plan de los 12 sprints
> 3. [`CLAUDE.md`](./CLAUDE.md) — Guía operativa
> 4. [`docs/database/schema.md`](./docs/database/schema.md) — Diseño BD (38 tablas, 9 schemas)

---

## 📌 Estado actual del proyecto

| Sprint | Tema | Estado |
|---|---|---|
| **Sprint 0** | Setup monorepo + infra docker | ✅ Cerrado |
| **Sprint 1** | Estructura Maven + Eureka + Gateway + Containerización | ✅ Cerrado |
| **Sprint 2.0** | CI/CD + GitHub Flow | ✅ Cerrado |
| **Sprint 2** | Diseño BD + Migraciones Flyway + Entidades JPA + Repositorios | ✅ Cerrado (38 tablas, 33 entidades, 34 repositorios) |
| **Sprint 3** | Mensajería RabbitMQ + eventos asíncronos | 🚧 En progreso |
| Sprints 4–12 | Auth/JWT, CRUDs, Frontend Vue, Reportes, QA, Deploy | 📋 Planificado |

---

## 🎯 Descripción del Proyecto

Sistema integral web responsive para automatizar la administración operativa y financiera de escuelas de conducción en Ecuador. Arquitectura de **microservicios** (8 MS + Gateway + Eureka), **Java Spring Boot 3** en backend, **Vue.js 3** en frontend, **PostgreSQL 15** con 9 schemas y mensajería asíncrona con **RabbitMQ**.

### Problema identificado

Las escuelas de conducción en Ecuador operan con:
- Procesos manuales y fragmentados
- Múltiples herramientas desintegradas (Excel, papeles)
- Falta de visión consolidada del estado operativo

**Impacto:** ~2,815–5,630 personas en el sector (563 escuelas × 5–10 administrativos).

### Solución propuesta

Plataforma web responsive unificada · Arquitectura de microservicios · Integración completa de procesos · Dashboard con KPIs · Automatización de notificaciones por email.

---

## 🏗️ Arquitectura: 8 Microservicios + Gateway + Eureka

| Módulo | Microservicio | Puerto | Responsabilidad |
|--------|---------------|--------|-----------------|
| 👤 **Autenticación** | MS-Auth | 8081 | Login, JWT, roles, configuración del sistema |
| 👨‍🎓 **Estudiantes** | MS-Estudiantes | 8082 | Matrícula, documentos, progreso, asistencia |
| 👨‍🏫 **Instructores** | MS-Instructores | 8083 | Perfiles, certificaciones, disponibilidad |
| 🚗 **Vehículos** | MS-Vehículos | 8084 | Flota, mantenimiento, combustible, inspecciones |
| 📅 **Asignaciones** | MS-Asignaciones | 8085 | Programación tripartita: instructor + estudiante + vehículo |
| 💳 **Cobros** | MS-Cobros | 8086 | Facturación, pagos, conciliación |
| 📊 **Reportes** | MS-Reportes | 8087 | Operativos, financieros, exportación PDF/Excel |
| 🔔 **Notificaciones** | MS-Notificaciones | 8088 | In-app + email (async) |
| **Gateway** | API Gateway | 8080 | Único punto de entrada, ruteo, rate limit |
| **Discovery** | Eureka Server | 8761 | Service registration & discovery |

---

## 🛠️ Stack Tecnológico

### Backend
- **Java 21** (LTS) + **Spring Boot 3.4.0**
- **Spring Cloud 2024.0.0** (Gateway, Eureka, OpenFeign, LoadBalancer)
- **Spring Security + JWT** (HS512, 512 bits, HttpOnly cookies, 24 h expiración) — Sprint 4
- **Spring Data JPA + Hibernate** + **Flyway** migrations
- **PostgreSQL 15** — 1 instancia, 9 schemas separados (no DB-per-service)
- **RabbitMQ 3.12** + Spring AMQP — mensajería asíncrona
- **Caffeine** — cache in-memory (no Redis)
- **MinIO 8.5.x** — object storage S3-compatible
- **MapStruct 1.6.x** — mapeo DTO ↔ entidad
- **Resilience4j** — circuit breaker, retry
- **SpringDoc 2.7.x** — OpenAPI 3
- **JUnit 5, Mockito, AssertJ, Testcontainers, H2** — testing

### Frontend (Sprint 6+)
- **Vue.js 3** (SPA) con Composition API + `<script setup lang="ts">`
- **Vite** + **Pinia** + **Vue Router** + **Axios**
- Diseño responsive mobile-first

### Infraestructura
- **Docker** + **Docker Compose** (14 contenedores)
- **GitHub Actions** CI/CD (Backend CI obligatorio, Docker Build path-filtered)
- **Email:** Mailtrap (dev) / Gmail SMTP (prod)
- **Despliegue:** Oracle Cloud Free Tier (fallback DigitalOcean $6/mes)

---

## 📂 Estructura del Proyecto

```
proyecto-titulacion/
├── CLAUDE.md                          # Guía operativa para Claude Code
├── DECISIONES.md                      # Decisiones técnicas (fuente de verdad)
├── SPRINTS_PLAN.xlsx                  # Plan de 12 sprints
├── README.md                          # Este archivo
│
├── backend/                           # Backend Spring Boot 3 (15 módulos Maven)
│   ├── pom.xml                        # POM padre
│   ├── eureka-server/                 # Service discovery (8761)
│   ├── api-gateway/                   # Gateway (8080)
│   ├── ms-auth/                       # Autenticación (8081)
│   ├── ms-estudiantes/                # Estudiantes (8082)
│   ├── ms-instructores/               # Instructores (8083)
│   ├── ms-vehiculos/                  # Vehículos (8084)
│   ├── ms-asignaciones/               # Asignaciones (8085)
│   ├── ms-cobros/                     # Cobros (8086)
│   ├── ms-reportes/                   # Reportes (8087)
│   ├── ms-notificaciones/             # Notificaciones (8088)
│   └── shared/                        # Librerías compartidas
│       ├── common-events/             # DTOs de eventos RabbitMQ
│       ├── common-exceptions/         # Excepciones + handlers RFC 7807
│       ├── common-jpa/                # BaseEntity + AuditorAware + AutoConfiguration
│       └── common-security/           # Utilidades JWT (Sprint 4)
│
├── frontend/                          # Vue.js 3 SPA (Sprint 6+)
│
├── infrastructure/
│   ├── docker/
│   │   ├── docker-compose.infra.yml   # Solo infra (4 contenedores)
│   │   ├── docker-compose.yml         # Stack completo (14 contenedores)
│   │   ├── Dockerfile.spring          # Multi-stage para servicios Java
│   │   └── README.md
│   └── postgres/
│       └── init-schemas.sql           # Crea los 9 schemas
│
├── docs/
│   ├── database/
│   │   └── schema.md                  # Diseño BD: 38 tablas, 9 schemas, diagramas Mermaid
│   ├── architecture/                  # C4 diagrams (futuro)
│   └── api/                           # OpenAPI specs (futuro)
│
└── .github/
    ├── workflows/                     # backend-ci.yml + docker-build.yml
    ├── CONTRIBUTING.md                # GitHub Flow + convenciones
    └── pull_request_template.md
```

---

## 🚀 Inicio Rápido

### Requisitos previos
- Java 21 JDK
- Maven 3.8+
- Node.js 18+ (frontend, Sprint 6+)
- Docker Desktop + Docker Compose
- Git

### Opción A — Stack completo containerizado (recomendado)

```bash
# 1. Clonar
git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git
cd proyecto-titulacion-udla

# 2. Levantar los 14 contenedores
docker compose -f infrastructure/docker/docker-compose.yml up -d

# 3. Verificar (esperar ~50-60 s a que pasen los healthchecks)
docker compose -f infrastructure/docker/docker-compose.yml ps
```

### Opción B — Solo infra (los MS corren desde tu IDE)

```bash
# Levantar solo Postgres + RabbitMQ + MinIO + Adminer
docker compose -f infrastructure/docker/docker-compose.infra.yml up -d

# Compilar backend
cd backend
mvn clean install -DskipTests

# Arrancar Eureka primero, luego Gateway, luego cada MS
cd eureka-server && mvn spring-boot:run
# (en otra terminal)
cd api-gateway && mvn spring-boot:run
# (etc., uno por terminal)
```

### URLs útiles

| Servicio | URL |
|----------|-----|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| Gateway routes | http://localhost:8080/actuator/gateway/routes |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| MinIO Console | http://localhost:9001 (minioadmin/minioadmin123) |
| Adminer (BD) | http://localhost:8888 |
| Frontend (Sprint 6+) | http://localhost:5173 |

---

## 🗄️ Base de Datos

- **PostgreSQL 15** — 1 instancia, **9 schemas separados** (1 por microservicio + `shared_schema`)
- **38 tablas** distribuidas: `auth_schema` (11) · `vehiculos_schema` (5) · `estudiantes_schema` (5) · `instructores_schema` (4) · `asignaciones_schema` (3) · `cobros_schema` (3) · `notificaciones_schema` (3) · `reportes_schema` (2) · `shared_schema` (2)
- **FKs solo dentro del mismo schema** — entre MS se referencian IDs sin restricción FK (consistencia eventual vía RabbitMQ)
- **Auditing global:** `created_at`, `updated_at`, `created_by`, `updated_by` + `deleted_at` (soft delete)
- **Migraciones Flyway V1** ejecutadas automáticamente al arrancar cada MS

Ver detalle completo en [`docs/database/schema.md`](./docs/database/schema.md).

---

## 🧪 Testing

```bash
# Tests smoke + unitarios
cd backend && mvn test

# Tests de integración (requiere Docker para Testcontainers)
mvn verify -Dgroups=integration

# Cobertura JaCoCo
mvn test jacoco:report
# Reportes en: backend/<ms>/target/site/jacoco/index.html
```

**Cobertura objetivo:** 80%+ por módulo (medido desde Sprint 4 cuando hay lógica de negocio real).

---

## 🌐 API Documentation

### Base URL
- **Local:** `http://localhost:8080`
- **Documentación Swagger UI** (cuando el MS lo expone): `http://localhost:<puerto>/swagger-ui.html`

### Autenticación (Sprint 4)
JWT con HS512 (clave 512 bits) en HttpOnly cookies, expiración 24 h, bloqueo tras 3 intentos fallidos.

```
POST /auth/login          → Login
POST /auth/refresh        → Refresh token
POST /auth/logout         → Logout
POST /auth/forgot-password
```

Ver especificación completa en `docs/api/` (a generar en Sprint 4).

---

## 🔄 Metodología y Workflow

### Scrum con sprints de **1 semana**, 12 sprints en total

**Tracking:** Jira

### Convenciones de código
- **Java:** PascalCase clases, camelCase métodos, paquetes `com.escuela.<servicio>.<layer>`
- **Vue:** PascalCase componentes, camelCase JS, kebab-case HTML
- **BD:** snake_case, plural (`estudiantes`, `instructores`)
- **Idioma:** español respetando estándares del lenguaje (snake_case solo en BD y env vars)

### Git Workflow — GitHub Flow
- **`main`** protegido (Branch Ruleset)
- **1 PR por cada commit/tarea** (desde Sprint 3) — antes era 1 PR por sprint completo, cambiado por problemas con tests acumulados al final
- Branch por tarea: `feature/sprint-N-X-descripcion-corta`
- Commit format: `Sprint N (Tarea X descripcion)` o `Sprint N (Fix tarea)`
- Squash and merge a main
- Backend CI obligatorio antes de mergear

Ver [`.github/CONTRIBUTING.md`](./.github/CONTRIBUTING.md) para el detalle.

---

## 🔒 Seguridad

- 🔐 **Spring Security + JWT** HS512 (512 bits), HttpOnly cookies, 24h
- 🛡️ **bcrypt** para passwords + bloqueo tras 3 intentos fallidos (15 min)
- 📋 **Auditoría completa** en `shared_schema.audit_log` (usuario, timestamp, IP, acción)
- ✅ **Validaciones Ecuador:** cédula (10 dígitos + dígito verificador), placa (`ABC-1234` o `AB-1234A`), RUC (13 dígitos), teléfono móvil (`09XXXXXXXX`)
- 🔒 **HTTPS/TLS 1.2+** mandatorio en producción

---

## 📈 Requisitos no funcionales

| Requisito | Valor |
|-----------|-------|
| Response time (p95) | < 500 ms |
| Usuarios concurrentes | 50 |
| Estudiantes activos | 1,000 max |
| Instructores | 50 max |
| Vehículos | 30 max |
| Backups | Diarios, retención 30 días |
| SLA disponibilidad | 99.9% |

---

## 🚫 Fuera de alcance

- ❌ Aplicaciones móviles nativas (web responsive sí)
- ❌ LMS con exámenes online o simulaciones
- ❌ GPS/Tracking en tiempo real
- ❌ Inteligencia Artificial / ML
- ❌ Multi-idioma (solo español Ecuador)
- ❌ Integración automática con APIs ANT (export de datos sí)
- ❌ Multi-tenant compartido (es **single-tenant configurable**: cada escuela = 1 deploy)

---

## 📚 Documentación adicional

- 📘 [DECISIONES.md](./DECISIONES.md) — 30 decisiones técnicas
- 📗 [SPRINTS_PLAN.xlsx](./SPRINTS_PLAN.xlsx) — Plan detallado por sprint
- 📙 [CLAUDE.md](./CLAUDE.md) — Guía operativa para Claude Code
- 🗄️ [docs/database/schema.md](./docs/database/schema.md) — Diseño completo de BD
- 🔧 [backend/README.md](./backend/README.md) — Cómo levantar el backend
- 🐳 [infrastructure/docker/README.md](./infrastructure/docker/README.md) — Detalle de Docker Compose
- 🤝 [.github/CONTRIBUTING.md](./.github/CONTRIBUTING.md) — GitHub Flow + convenciones

---

## 👥 Contacto

- **Project Lead:** Víctor Javier Gómez Regalado
- **Estudiantes:** Raúl Sebastián Cruz Baño, Hernán Mateo Jurado Moran
- **Universidad:** Universidad de las Américas (UDLA), Quito, Ecuador
- **Repositorio:** https://github.com/Kynsofttita-com/proyecto-titulacion-udla

---

## 📜 Licencia

Código propietario de Kynsoft SAS con derechos académicos para UDLA.

---

**Última actualización:** 2026-05-07 (cierre Sprint 2)
