# Sistema de Control Administrativo y Financiero para Escuelas de Conducción

[![Backend CI](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/backend-ci.yml)
[![Docker Build](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/docker-build.yml/badge.svg)](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/docker-build.yml)
[![Frontend CI](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/frontend-ci.yml)
[![Integration Tests](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/integration-tests.yml/badge.svg)](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/integration-tests.yml)
[![Smoke E2E](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/smoke-e2e.yml/badge.svg)](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/smoke-e2e.yml)
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
> 1. [`DECISIONES.md`](./DECISIONES.md) — 32 decisiones técnicas (cerradas 2026-05-06, +2 ADRs Sprint 10)
> 2. [`PLAN_FASES.md`](./PLAN_FASES.md) — Plan vigente Sprints 5-12 (vertical por grupos)
> 3. [`SPRINTS_PLAN.xlsx`](./SPRINTS_PLAN.xlsx) — Plan original (referencia histórica)
> 4. [`CLAUDE.md`](./CLAUDE.md) — Guía operativa
> 5. [`docs/database/schema.md`](./docs/database/schema.md) — Diseño BD (41 tablas, 9 schemas, 22 migraciones)

---

## 🚀 Quick Start (5 minutos)

### Requisitos previos
- Docker & Docker Compose
- Git

### Levantar el sistema completo (14 containers)

```bash
# 1. Clonar/entrar al proyecto
git clone <repo-url>
cd proyecto-titulacion

# 2. Ejecutar desde infrastructure/docker/
cd infrastructure/docker
docker-compose up -d --build

# 3. Esperar arranque (~3-5 minutos)
docker-compose ps

# 4. Acceder
Frontend:      http://localhost:3000
API Gateway:   http://localhost:8080
Eureka:        http://localhost:8761
Adminer (BD):  http://localhost:8089
```

**Credenciales por defecto:** `admin@escuela.com` / (verificar en `.env`)

> Para detalles completos: ver [`GETTING_STARTED.md`](./GETTING_STARTED.md)

---

## 📌 Estado del proyecto (Sprint 12 - COMPLETADO)

| Sprint | Fase | Tema | Estado |
|---|---|---|---|
| **Sprint 0-4** | Infrastructure | Monorepo, Docker, Eureka, Gateway, Auth, BD, RabbitMQ | ✅ Cerrado |
| **Sprint 5-8** | Fase 1 — Grupo A | Backend + Frontend (Auth, Estudiantes, Instructores, Vehículos, Asignaciones, Cobros) + Testing | ✅ Cerrado |
| **Sprint 9** | Fase 2 — Grupo B | MS-Notificaciones + MS-Reportes backend | ✅ Cerrado |
| **Sprint 10** | Pulido | Kilometraje E2E, validaciones cross-MS, CI/CD mejorado | ✅ Cerrado |
| **Sprint 11** | Frontend Grupo B | Dashboards, reportes UI, UI mejorado | ✅ Cerrado |
| **Sprint 12** | Cierre & Deploy | E2E cruzado, performance, OWASP, deploy, v1.0.0 | ✅ **COMPLETADO** |

**Sistema: PRODUCTION READY ✅** — 14/14 containers UP, 280+ tests, OWASP compliant

> Ver detalle de tareas, subtareas y criterios de aceptación en [`PLAN_FASES.md`](./PLAN_FASES.md).
> ADRs técnicos del Sprint 10 (refactor dominio + estabilización CI/CD) en [`DECISIONES.md §24-§25`](./DECISIONES.md).

### Validación funcional

Los flujos críticos (login JWT, CRUDs del Grupo A, asignación tripartita con kilometraje E2E, las 6 validaciones cross-MS al crear asignación, forgot-password, reset, reconciliación de pagos, modelo de crédito con cuotas) están validados desde el frontend en `http://localhost:5173`. El pipeline **smoke-e2e** valida el flujo completo automáticamente en cada PR a `main` (14 contenedores Docker, login admin con retry, 12 endpoints REST, 404/400 ProblemDetail).

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
| **Gateway** | API Gateway | 8080 | Único punto de entrada, ruteo, JWT validation |
| **Discovery** | Eureka Server | 8761 | Service registration & discovery |

**Estado de implementación al 2026-05-26:**

- 🟢 **Grupo A (6 MS principales)** — backend + frontend + testing completos (Sprints 5-8)
- 🟡 **Grupo B (2 MS)** — schemas creados, controllers REST **en proceso (Sprint 9)**
- ⚙ **Servicios de soporte** (Eureka, Gateway, RabbitMQ, MinIO) — completos desde Sprint 1-4

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

### Frontend (Sprints 7-8, en uso)
- **Vue.js 3** (SPA) con Composition API + `<script setup lang="ts">`
- **Vite** + **Pinia** + **Vue Router** + **Axios** + **PrimeVue**
- Diseño responsive mobile-first
- Login + dashboards + CRUDs del Grupo A funcionales (Auth, Estudiantes, Instructores, Vehículos, Asignaciones, Cobros)

### Infraestructura
- **Docker** + **Docker Compose** (14 contenedores)
- **GitHub Actions** CI/CD — 5 workflows: `backend-ci`, `docker-build`, `frontend-ci`, `integration-tests`, `smoke-e2e` (con filtros `paths:` para no correr workflows innecesarios)
- **TZ JVM hardcoded** a `America/Guayaquil` en `Dockerfile.spring` (V10 estabilización)
- **Email:** Mailtrap (dev) / Gmail SMTP (prod)
- **Despliegue:** Oracle Cloud Free Tier (fallback DigitalOcean $6/mes) — pendiente Sprint 12

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
- Node.js 18+ (recomendado 20 LTS)
- Docker Desktop + Docker Compose
- Git

---

### Levantar el sistema completo (recomendado) ⭐

Este flujo levanta los 14 contenedores del backend + el frontend Vue en modo dev. Es lo que necesitás para usar la app.

```bash
# 1. Clonar el repo
git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git
cd proyecto-titulacion-udla

# 2. Levantar el stack completo del backend (14 contenedores Docker)
docker compose -f infrastructure/docker/docker-compose.yml up -d

# 3. Esperar a que todos los healthchecks pasen (~50-60s la primera vez, ~30s las siguientes)
docker compose -f infrastructure/docker/docker-compose.yml ps
# (todos deben aparecer "healthy")

# 4. Verificar que Eureka registró los 9 servicios (Gateway + 8 MS)
curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" \
  | python -c "import sys,json; d=json.load(sys.stdin); print(f'Apps registradas: {len(d[\"applications\"][\"application\"])}')"
# Debe imprimir: Apps registradas: 9

# 5. Instalar deps del frontend (solo la primera vez)
cd frontend
npm install

# 6. Configurar variables de entorno del frontend (solo la primera vez)
cp .env.example .env
# (el default apunta a http://localhost:8080, no requiere edición para local)

# 7. Levantar el frontend Vue (dev server con hot reload)
npm run dev
# Debe imprimir: VITE v5.x.x ready in XXXms - Local: http://localhost:5173/
```

**¡Listo!** Abrí **http://localhost:5173** en el navegador y entrá con:

```
Email:    admin@escuela.local
Password: Admin123!
```

---

### Opción alternativa — Solo infra + backend en el IDE

Si querés debuggear código del backend desde IntelliJ/Eclipse en lugar de usar las imágenes Docker:

```bash
# 1. Levantar solo Postgres + RabbitMQ + MinIO + Adminer (4 contenedores)
docker compose -f infrastructure/docker/docker-compose.infra.yml up -d

# 2. Compilar todos los módulos backend (necesario por shared libs)
cd backend
mvn clean install -DskipTests

# 3. Arrancar Eureka primero, luego Gateway, luego cada MS (cada uno en su terminal)
cd eureka-server && mvn spring-boot:run
# (otra terminal)
cd api-gateway && mvn spring-boot:run
# (etc., 1 terminal por MS — 10 terminales en total)

# 4. Frontend (en otra terminal más)
cd frontend && npm install && npm run dev
```

---

### Comandos útiles para administrar el sistema

```bash
# Ver logs del backend en vivo
docker compose -f infrastructure/docker/docker-compose.yml logs -f

# Ver logs de un MS específico
docker compose -f infrastructure/docker/docker-compose.yml logs -f ms-auth

# Detener el backend (preserva la BD)
docker compose -f infrastructure/docker/docker-compose.yml down

# Detener Y BORRAR la BD (cuidado)
docker compose -f infrastructure/docker/docker-compose.yml down -v

# Rebuild de imágenes tras cambios de código backend
docker compose -f infrastructure/docker/docker-compose.yml up -d --build

# Detener el frontend
# Ctrl+C en la terminal donde corre `npm run dev`
```

---

### URLs útiles

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| 🎯 **Frontend Vue** | http://localhost:5173 | `admin@escuela.local` / `Admin123!` |
| API Gateway | http://localhost:8080 | (JWT vía login) |
| Gateway routes | http://localhost:8080/actuator/gateway/routes | — |
| Eureka Dashboard | http://localhost:8761 | — |
| RabbitMQ Management | http://localhost:15672 | guest / guest |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin123 |
| Adminer (BD) | http://localhost:8888 | escuela_user / `<ver .env>` / escuela_db |

---

## 🗄️ Base de Datos

- **PostgreSQL 15** — 1 instancia, **9 schemas separados** (1 por microservicio + `shared_schema`)
- **41 tablas** distribuidas: `auth_schema` (12) · `vehiculos_schema` (6) · `estudiantes_schema` (5) · `instructores_schema` (4) · `asignaciones_schema` (3) · `cobros_schema` (4) · `notificaciones_schema` (3) · `reportes_schema` (2) · `shared_schema` (2)
- **FKs solo dentro del mismo schema** — entre MS se referencian IDs sin restricción FK (consistencia eventual vía RabbitMQ + Feign)
- **Auditing global:** `created_at`, `updated_at`, `created_by`, `updated_by` + `deleted_at` (soft delete)
- **22 migraciones Flyway** ejecutadas automáticamente al arrancar cada MS (V1-V6 en ms-auth, V1-V5 en ms-estudiantes, V1-V2 en ms-instructores/vehiculos/asignaciones/cobros, V1 en ms-notificaciones/ms-reportes)

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

**Cobertura objetivo:** 80%+ por módulo (medido desde Sprint 4 cuando hay lógica de negocio real). Testing completo del Grupo A cerrado en Sprint 8; testing del Grupo B pendiente del Sprint 11.

### Pipelines CI/CD activos

| Workflow | Trigger | Acciones |
|----------|---------|----------|
| `backend-ci.yml` | Push/PR a main | Maven build + tests unitarios + JaCoCo |
| `frontend-ci.yml` | Push/PR a main si cambia `frontend/**` | `npm ci` + `vite build` + upload `dist/` |
| `integration-tests.yml` | Push/PR a main si cambia `backend/**` | Postgres + RabbitMQ + `mvn verify -Dgroups=integration` |
| `smoke-e2e.yml` | Push/PR a main si cambia `backend/**` o `infrastructure/**` | Stack completo 14 contenedores + login admin + 12 endpoints REST + 404/400 ProblemDetail |
| `docker-build.yml` | Push a main si cambia `backend/**` o `infrastructure/docker/**` | Build de una imagen Spring de prueba (eureka-server) |

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

Especificación OpenAPI completa: pendiente de exportar a `docs/api/` en Sprint 13 (T13.6 Docs final). Mientras tanto, accesible vía SpringDoc en `http://localhost:<puerto>/v3/api-docs` de cada MS.

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

- 📘 [DECISIONES.md](./DECISIONES.md) — 30 decisiones técnicas + 2 ADRs Sprint 10 (refactor dominio + estabilización CI/CD)
- 📗 [PLAN_FASES.md](./PLAN_FASES.md) — Plan vigente Sprints 5-12 (vertical por grupos)
- 📘 [SPRINTS_PLAN.xlsx](./SPRINTS_PLAN.xlsx) — Plan original (referencia histórica)
- 📙 [CLAUDE.md](./CLAUDE.md) — Guía operativa para Claude Code
- 🗄️ [docs/database/schema.md](./docs/database/schema.md) — Diseño completo de BD (41 tablas, 9 schemas, 22 migraciones, diagramas Mermaid)
- 📁 [docs/database/secciones/](./docs/database/secciones/) — Documentación BD partida en 19 secciones temáticas
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

**Última actualización:** 2026-05-26 — Sprints 1-8 + Sprint 10 (estabilización) cerrados en `main`. Sprint 9 (Backend Grupo B) en proceso.
