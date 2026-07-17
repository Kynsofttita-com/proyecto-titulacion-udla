# DECISIONES.md

**Documento de Decisiones Técnicas y de Arquitectura**
**Proyecto:** Sistema de Control Administrativo y Financiero para Escuelas de Conducción
**Universidad:** Universidad de las Américas (UDLA), Quito, Ecuador
**Fecha de cierre de decisiones:** 2026-05-06
**Última actualización:** 2026-07-17
**Estado:** ✅ **COMPLETADO** — Todos los sprints (1-12) cerrados en `main`. Sistema 100% production-ready con 154/154 tests OK, 97% coverage, CI/CD avanzado (GitHub Actions + Jenkins + ArgoCD), DevSecOps activo (OWASP + Trivy + Gitleaks + CodeQL) y documentación deployment completa.

> Este documento consolida todas las decisiones técnicas, arquitectónicas y de proceso tomadas antes de iniciar el desarrollo. Es la fuente de verdad del proyecto y debe ser respetado durante toda la ejecución de los 13 sprints. Cambios estructurales posteriores al cierre se incorporan como ADRs en este mismo documento (ver §23 en adelante).

---

## 1. Información General

| Atributo | Valor |
|----------|-------|
| Nombre del proyecto | Sistema de Control Administrativo y Financiero para Escuelas de Conducción |
| Equipo | Hernán Mateo Jurado Moran (desarrollo) / Raúl Sebastián Cruz Baño (documentación) |
| Tutor | Víctor Javier Gómez Regalado |
| Universidad | UDLA - Quito, Ecuador |
| Metodología | Scrum |
| Duración total | 12 sprints de 1 semana cada uno (12 semanas) |
| Tracking de tareas | Jira |
| Idioma de la aplicación | Español (Ecuador) |
| Modelo de negocio | SaaS B2B - Single-tenant configurable |

---

## 2. Stack Técnico

### Backend
- **Lenguaje:** Java 21 (LTS)
- **Framework:** Spring Boot 3.x
- **Microservicios:** Spring Cloud (Gateway, Eureka, OpenFeign)
- **ORM:** Spring Data JPA + Hibernate
- **Migraciones:** Flyway
- **Seguridad:** Spring Security + JWT (jjwt library)
- **API:** REST/JSON con OpenAPI 3.0 (SpringDoc)
- **Mensajería:** Spring AMQP + RabbitMQ
- **Cache:** Caffeine (in-memory, sin infraestructura adicional)
- **Email:** Spring Mail + Thymeleaf (plantillas HTML)
- **File storage:** MinIO (S3 compatible, self-hosted)
- **Testing:** JUnit 5, Mockito, AssertJ, Testcontainers, MockMvc
- **Cobertura:** JaCoCo (threshold 80%)
- **Build:** Maven 3.8+

### Frontend
- **Framework:** Vue.js 3 (Composition API)
- **Lenguaje:** TypeScript (strict mode)
- **Build tool:** Vite
- **State management:** Pinia + pinia-plugin-persistedstate
- **Routing:** Vue Router 4
- **HTTP client:** Axios
- **UI library:** PrimeVue + PrimeIcons
- **Validación de forms:** VeeValidate + Yup
- **Charts:** Chart.js o ApexCharts (decisión en Sprint 11)
- **Calendario:** FullCalendar o vue-cal
- **Fechas:** date-fns o dayjs
- **Testing:** Vitest + Vue Test Utils + @testing-library/vue + Cypress (E2E)
- **Linting:** ESLint + Prettier

### Infraestructura
- **Base de datos:** PostgreSQL 15 (1 instancia, schemas separados por MS)
- **Mensajería:** RabbitMQ 3.12 (con plugin management)
- **Service Discovery:** Eureka (Netflix OSS / Spring Cloud)
- **API Gateway:** Spring Cloud Gateway
- **Object Storage:** MinIO
- **Email (dev):** Mailtrap
- **Email (prod):** Gmail SMTP (o SendGrid si requiere volumen)
- **Containerización:** Docker + Docker Compose
- **Orquestación:** Docker Compose (manifests Kubernetes opcionales para titulación)
- **CI/CD:** GitHub Actions
- **Code quality:** SonarQube (opcional, agregable)
- **Deploy producción:** Oracle Cloud Free Tier (fallback: DigitalOcean Droplet $6/mes)

---

## 3. Arquitectura

### 3.1 Microservicios (8 servicios + soportes)

| Microservicio | Puerto | Responsabilidad |
|---------------|--------|-----------------|
| API Gateway | 8080 | Punto de entrada único, routing, JWT validation, rate limiting, CORS |
| MS-Auth | 8081 | Autenticación, autorización, JWT, gestión de usuarios/roles, **módulo Configuración** |
| MS-Estudiantes | 8082 | Gestión de estudiantes, documentos, progreso, asistencia |
| MS-Instructores | 8083 | Gestión de instructores, certificaciones, disponibilidad |
| MS-Vehículos | 8084 | Gestión de flota, mantenimiento, combustible, inspecciones |
| MS-Asignaciones | 8085 | Programación de clases (instructor + estudiante + vehículo) |
| MS-Cobros | 8086 | Facturación, pagos, reconciliación, soporte de pagos parciales |
| MS-Reportes | 8087 | Agregación de datos, reportes, exportación PDF/Excel |
| MS-Notificaciones | 8088 | Notificaciones in-app + emails transaccionales |

### 3.2 Servicios de soporte

| Servicio | Puerto | Función |
|----------|--------|---------|
| Eureka Server | 8761 | Service discovery |
| PostgreSQL | 5432 | Base de datos relacional |
| RabbitMQ | 5672 | Broker de mensajes |
| RabbitMQ Mgmt UI | 15672 | Dashboard de administración |
| MinIO | 9000 | Object storage (API) |
| MinIO Console | 9001 | Dashboard web |
| Frontend Vue | 5173 | Aplicación web SPA |

### 3.3 Comunicación entre servicios

- **Síncrona:** OpenFeign (con Resilience4j Circuit Breaker)
- **Asíncrona:** RabbitMQ con Topic Exchanges
- **Frontend → Backend:** Solo a través del API Gateway (puerto 8080)
- **Service-to-service:** A través de Eureka (load-balanced)

### 3.4 Modelo de despliegue

**Decisión: Single-tenant configurable**
- 1 deploy completo del sistema = 1 escuela cliente
- Cada cliente recibe su propia instancia con BD aislada
- Personalización a través del módulo "Configuración" en MS-Auth (gestionado por el ADMIN)
- Permite vender a múltiples escuelas con instancias independientes

---

## 4. Bases de Datos

### 4.1 Estrategia: 1 instancia PostgreSQL, 9 schemas

**Database name:** `escuela_db`

**Schemas:**

| Schema | Microservicio | Tablas principales |
|--------|---------------|--------------------|
| `auth_schema` | MS-Auth | usuarios, roles, permisos, usuario_rol, rol_permiso, password_reset_token, configuracion_escuela |
| `estudiantes_schema` | MS-Estudiantes | estudiantes, documentos, asistencia, progreso_academico, contactos_emergencia |
| `instructores_schema` | MS-Instructores | instructores, certificaciones, disponibilidad, horarios_trabajo |
| `vehiculos_schema` | MS-Vehículos | vehiculos, mantenimientos, registros_combustible, inspecciones, documentos_vehiculo |
| `asignaciones_schema` | MS-Asignaciones | asignaciones, cambios_asignacion, historial_estados |
| `cobros_schema` | MS-Cobros | facturas, pagos, conceptos_facturacion, reconciliacion |
| `reportes_schema` | MS-Reportes | cache_reportes, ejecuciones_reporte |
| `notificaciones_schema` | MS-Notificaciones | notificaciones, plantillas_email, log_envios |
| `shared_schema` | Compartido | audit_log (auditoría centralizada) |

### 4.2 Convenciones de BD

- **Tablas:** snake_case, plural (`estudiantes`, `instructores`)
- **Columnas:** snake_case (`fecha_matricula`, `id_estudiante`)
- **Primary keys:** `id` BIGINT auto-increment
- **Foreign keys:** `{tabla}_id` (ej: `estudiante_id`, `instructor_id`)
- **NO foreign keys cross-schema** (cada MS solo tiene FKs internas; entre MS solo se almacenan IDs de referencia)
- **Audit fields obligatorios en todas las tablas:**
  - `created_at TIMESTAMP NOT NULL DEFAULT NOW()`
  - `updated_at TIMESTAMP`
  - `created_by VARCHAR(50)`
  - `updated_by VARCHAR(50)`
- **Soft delete:** `deleted_at TIMESTAMP NULL` en todas las entidades EXCEPTO pagos y audit_log (estos nunca se borran)
- **Migraciones:** Flyway con scripts versionados (`V<timestamp>__<descripcion>.sql`)

### 4.3 Datos seed iniciales

- 1 usuario administrador: `admin@escuela.local` / `Admin123!`
- 2 usuarios staff demo
- 3 instructores demo
- 5 vehículos demo
- 10 estudiantes demo
- Conceptos de facturación: "Curso Básico", "Examen", "Repetición"
- Roles: ADMIN, STAFF, INSTRUCTOR, ESTUDIANTE
- Permisos granulares por recurso/acción

---

## 5. Roles y Permisos

| Rol | Capacidades |
|-----|------------|
| **ADMIN** | Todo: gestión completa, configuración del sistema, reportes financieros, gestión de usuarios/roles |
| **STAFF** (Personal Administrativo) | CRUD estudiantes/instructores/vehículos/asignaciones/cobros, ver reportes (sin configuración del sistema) |
| **INSTRUCTOR** | Ver sus asignaciones, ver estudiantes asignados, marcar asistencia |
| **ESTUDIANTE** | Ver su perfil, sus clases, su saldo, su histórico |

### 5.1 Estrategia de autorización

- **Spring Security** con `@EnableMethodSecurity`
- **Anotaciones por endpoint:** `@PreAuthorize("hasRole('ADMIN')")`
- **Permisos granulares:** `@PreAuthorize("hasAuthority('ESTUDIANTES_READ')")`
- **JWT incluye:** userId, email, roles[], permissions[]

---

## 6. Seguridad

### 6.1 Autenticación

- **Método:** JWT (JSON Web Tokens)
- **Algoritmo de firma:** HS512 (HMAC con SHA-512)
- **Longitud de clave:** **512 bits** (mínimo)
- **Access token:** expira en 24 horas (86400000 ms)
- **Refresh token:** expira en 7 días (604800000 ms)
- **Storage en frontend:** **HttpOnly cookies** (mitigación XSS)
- **Encoding de passwords:** BCrypt con salt
- **Account lockout:** 3 intentos fallidos → bloqueo 15 minutos
- **Password reset:** token único con expiración de 1 hora, enviado por email

### 6.2 Validación JWT

- API Gateway valida el JWT antes de rutear (excepto `/auth/login`, `/auth/refresh`, `/auth/forgot-password`)
- Cada microservicio recibe headers propagados: `X-User-Id`, `X-User-Email`, `X-User-Roles`
- Sesiones stateless (`SessionCreationPolicy.STATELESS`)

### 6.3 Otras políticas de seguridad

- **HTTPS/TLS 1.2+** obligatorio en producción
- **CORS:** solo orígenes whitelist (frontend localhost:5173, dominio prod)
- **CSRF:** disabled (API REST stateless)
- **XSS:** auto-escape de Vue + sanitización en backend
- **SQL Injection:** parameterized queries via JPA/Hibernate
- **Rate limiting:** 100 requests/min por IP en API Gateway (desde Sprint 4)

---

## 7. Convenciones de Código

### 7.1 Java (Backend)

**Idioma de los nombres:** Español, respetando convenciones obligatorias del lenguaje Java.

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Paquetes | minúsculas | `com.escuela.estudiantes.service` |
| Clases | PascalCase | `Estudiante`, `EstudianteService`, `EstudianteController` |
| Interfaces | PascalCase | `EstudianteRepository`, `EventPublisher` |
| Métodos | camelCase | `buscarPorCedula()`, `crearEstudiante()`, `listarTodos()` |
| Variables | camelCase | `fechaMatricula`, `cedulaEstudiante`, `montoTotal` |
| Constantes | UPPER_SNAKE_CASE | `MAX_INTENTOS_LOGIN`, `TIEMPO_BLOQUEO_MINUTOS` |
| Tablas BD | snake_case plural | `estudiantes`, `instructores` |
| Columnas BD | snake_case | `fecha_matricula`, `id_estudiante` |

**Estructura de paquetes por microservicio:**
```
com.escuela.<microservicio>/
├── controller/      # REST controllers
├── service/         # Business logic
├── repository/      # JPA repositories
├── entity/          # JPA entities
├── dto/             # Request/Response DTOs
│   ├── request/
│   └── response/
├── mapper/          # MapStruct mappers
├── config/          # @Configuration classes
├── security/        # Security-specific (filters, JWT)
├── exception/       # Custom exceptions
├── event/           # Event DTOs and publishers
└── client/          # OpenFeign clients (cross-MS)
```

**Formateo:** Google Java Style (4 espacios de indentación)

### 7.2 TypeScript / Vue (Frontend)

**Idioma de los nombres:** Español, respetando convenciones de TypeScript/Vue.

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Componentes Vue (archivo) | PascalCase | `EstudianteForm.vue`, `EstudiantesList.vue` |
| Componentes Vue (en template) | kebab-case | `<estudiante-form />`, `<estudiantes-list />` |
| Composables | camelCase con `use` prefix | `useAuth()`, `useEstudiantes()` |
| Stores Pinia | camelCase | `useAuthStore()`, `useEstudiantesStore()` |
| Variables | camelCase | `estudianteSeleccionado`, `fechaActual` |
| Funciones | camelCase | `buscarEstudiante()`, `formatearFecha()` |
| Tipos/Interfaces | PascalCase | `Estudiante`, `CreateEstudianteRequest` |
| Constantes | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE`, `API_BASE_URL` |
| Atributos HTML | kebab-case | `<input v-model="value" data-test-id="cedula" />` |

**Formateo:** Prettier (2 espacios, comilla simple, sin punto y coma)

### 7.3 Variables de entorno

- **Convención:** UPPER_SNAKE_CASE
- **Ejemplo:** `POSTGRES_HOST`, `JWT_SECRET`, `MAIL_HOST`

---

## 8. Estructura del Repositorio (Monorepo)

```
proyecto-titulacion/
├── .github/workflows/              # CI/CD pipelines
├── .gitignore
├── README.md
├── CLAUDE.md
├── DECISIONES.md                   # Este documento
├── SPRINTS_PLAN.xlsx               # Plan de sprints detallado
├── .env.example                    # Template de variables de entorno
│
├── backend/
│   ├── pom.xml                     # POM padre (gestión de dependencias)
│   ├── api-gateway/
│   ├── eureka-server/
│   ├── ms-auth/
│   ├── ms-estudiantes/
│   ├── ms-instructores/
│   ├── ms-vehiculos/
│   ├── ms-asignaciones/
│   ├── ms-cobros/
│   ├── ms-reportes/
│   ├── ms-notificaciones/
│   └── shared/
│       ├── common-events/          # DTOs de eventos RabbitMQ
│       ├── common-exceptions/      # Excepciones compartidas
│       └── common-security/        # JwtTokenProvider, filters
│
├── frontend/                       # Vue 3 + TypeScript + PrimeVue
│   ├── src/
│   │   ├── components/
│   │   │   ├── common/             # Componentes reutilizables
│   │   │   └── <dominio>/          # Componentes por dominio
│   │   ├── views/                  # Páginas (rutas)
│   │   ├── layouts/                # AuthLayout, MainLayout
│   │   ├── stores/                 # Pinia stores
│   │   ├── services/               # Axios services
│   │   ├── router/                 # Vue Router config
│   │   ├── composables/            # useAuth, useToast, etc.
│   │   ├── types/                  # Interfaces TypeScript
│   │   ├── utils/                  # Helpers
│   │   └── assets/scss/            # Estilos globales
│   ├── tests/                      # Vitest + Cypress
│   ├── package.json
│   └── vite.config.ts
│
├── infrastructure/
│   ├── docker/                     # Toda la infraestructura Docker
│   │   ├── docker-compose.yml      # Stack completo
│   │   ├── docker-compose.infra.yml # Solo infra (BD, Rabbit, Eureka, MinIO)
│   │   └── Dockerfile.<ms>         # (cada MS también tiene su Dockerfile interno)
│   ├── postgres/
│   │   └── init-schemas.sql        # Crea los 9 schemas al levantar
│   ├── rabbitmq/
│   │   └── rabbitmq.conf
│   ├── minio/
│   │   └── init-buckets.sh
│   └── nginx/                      # (opcional, para producción)
│
└── docs/
    ├── architecture/               # Diagramas C4, ER
    ├── api/                        # OpenAPI specs por MS
    ├── decisions/                  # ADRs futuros
    └── guides/                     # Setup, dev, deploy
```

---

## 9. Convenciones de Git

### 9.1 Branching strategy: GitHub Flow + 1 PR por commit/tarea

> **Actualizado en Sprint 3 (2026-05-07):** evolucionado nuevamente a **"1 PR por cada commit/tarea"**. Razón: en Sprint 2 hubo problemas al cerrar el PR completo del sprint (los tests/CI fallaban al final y había muchos commits acumulados que arreglar). Cambio para detectar fallos de CI temprano por cada tarea, no esperar al final del sprint. Historial:
> - Sprints 0–2.0: 1 PR por tarea
> - Sprint 2.1–2.6: 1 PR por sprint completo (descartada)
> - Sprint 3+: **1 PR por commit/tarea** (vigente)

**Branches:**
- `main` → branch principal, siempre estable, **protegida** (no se permite push directo)
- `feature/sprint-N-<descripcion-amplia>` → un branch por sprint completo (no por tarea individual)
- `fix/<descripcion-corta>` → para correcciones de bugs urgentes
- `docs/sprint-N-<descripcion-corta>` → para cambios solo de documentación
- `chore/<descripcion-corta>` → para tareas de mantenimiento (deps, configs, CI)

**Flujo (1 PR por commit/tarea — vigente desde Sprint 3):**
1. Crear branch desde `main` actualizado al inicio de la tarea: `feature/sprint-N-X-descripcion-corta` (ej: `feature/sprint-5-2-estudiantes-crud`)
2. Implementar UNA tarea (T_N.X) completa y probada localmente
3. Commit único con formato `Sprint N (Tarea X descripcion)` (ver 9.2)
4. Push al remote
5. Abrir Pull Request pequeño a `main` (sin esperar a más tareas)
6. Esperar a que CI pase (Backend CI + Docker Build si aplica)
7. **Squash and merge** desde GitHub UI
8. Eliminar el branch tras merge
9. Pasar a la siguiente tarea (nuevo branch desde main actualizado)

**Convenciones de naming de branches:**
- Usar guiones: `feature/sprint-2-base-de-datos` (descripcion del sprint, no de la tarea)
- Mantenerlo corto: 3-5 palabras descriptivas del objetivo del sprint
- Sin acentos ni caracteres especiales

**Excepciones aceptadas:**
- Commits directos a `main` solo en setup inicial (Sprints 0-2.0). A partir del 2.1, todo via PR.
- Sub-PRs dentro de un sprint si la pieza es independiente y arriesgada (ej: PR #1 del Sprint 2.0 movió `CONTRIBUTING.md`).
- Sprints con sub-PRs (como Sprint 2.0 con CI/CD y luego mover docs) están permitidos cuando hay razones técnicas.

**Ventajas de 1 PR por commit/tarea (vigente desde Sprint 3):**
- Detección temprana de fallos en CI (cada tarea valida sola, no se acumulan errores)
- PRs pequeños y revisables (foco en una sola tarea)
- Historia de `main` muy limpia: `Sprint N (Tarea X descripcion)` por commit squashed
- Bisect más preciso si aparece un bug semanas después
- Cada PR representa una unidad de trabajo claramente trazable
- Para el jurado: cada tarea del PLAN_FASES.md se mapea a 1 PR mergeado

### 9.1.1 Branch Protection Rules en `main`

Configuradas en GitHub Settings → Branches:
- ✅ Require a pull request before merging (0 approvals requeridas, single dev)
- ✅ Require status checks to pass (Backend CI obligatorio; Docker Build cuando aplica)
- ✅ Require branches to be up to date before merging
- ✅ Require conversation resolution before merging
- ❌ Force pushes prohibidos
- ❌ Branch deletion prohibida
- ✅ Aplicar reglas a administradores también (no bypass)

### 9.2 Convenciones de commits

**Formato:** `Sprint N (Tarea) Descripción adicional opcional`

**Reglas:**
- 1 commit = 1 tarea finalizada y probada
- Si es una corrección: `Sprint N (Fix tarea)`

**Ejemplos:**
```
Sprint 1 (Crear estructura Maven multi-modulo)
Sprint 1 (Configurar Spring Boot 3 en MS-Auth)
Sprint 2 (Crear migraciones Flyway en MS-Estudiantes)
Sprint 2 (Fix migracion V1 MS-Cobros - constraint duplicada)
Sprint 4 (Implementar JWT en MS-Auth)
Sprint 5 (Implementar CRUD Estudiantes)
Sprint 5 (Fix validacion cedula Ecuador)
```

### 9.3 Pull Requests

- PR debe pasar checklist de Definition of Done (ver sección 17)
- Mínimo 1 review antes de merge
- Tests automáticos deben pasar (CI verde)
- JaCoCo coverage ≥ 80%

---

## 10. Variables de Entorno

### 10.1 Estrategia

- Archivo `.env` en la raíz (NO se sube a Git, está en `.gitignore`)
- Archivo `.env.example` (SÍ se sube, sirve de template)
- En cada microservicio: `application.yml` lee con `${VARIABLE:default}` syntax
- Cada ambiente tiene su propio `.env` con valores apropiados

### 10.2 Comportamiento por ambiente

| Ambiente | `POSTGRES_HOST` ejemplo | Nota |
|----------|------------------------|------|
| Dev local (sin Docker) | `localhost` | Postgres corre en la máquina del dev |
| Docker Compose local | `postgres` | DNS interno de Docker resuelve al servicio |
| Producción VPS/Cloud | `db.miescuela.com` o IP | Servidor real |

### 10.3 Variables clave (`.env.example`)

```bash
# === Database ===
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=escuela_db
POSTGRES_USER=escuela_user
POSTGRES_PASSWORD=changeme

# === RabbitMQ ===
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# === JWT (512 bits mínimo) ===
JWT_SECRET=changeme-this-must-be-512-bits-or-64-characters-long-for-security
JWT_ACCESS_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# === Email (Mailtrap dev) ===
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=your-mailtrap-user
MAIL_PASSWORD=your-mailtrap-pass
MAIL_FROM=noreply@escuela.local

# === MinIO ===
MINIO_HOST=localhost
MINIO_PORT=9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# === Eureka ===
EUREKA_URL=http://localhost:8761/eureka
```

---

## 11. Validaciones Específicas de Ecuador

| Dato | Formato | Validación |
|------|---------|-----------|
| **Cédula** | 10 dígitos | Algoritmo dígito verificador (módulo 10) |
| **RUC** | 13 dígitos | Termina en `001`, los primeros 10 son cédula válida |
| **Placa vehículo** | `ABC-1234` o `AB-1234A` | Regex: `^[A-Z]{3}-\d{4}$\|^[A-Z]{2}-\d{4}[A-Z]$` |
| **Teléfono móvil** | `09XXXXXXXX` | 10 dígitos, inicia con 09 |
| **Teléfono fijo Quito** | `02XXXXXXX` | 9 dígitos, inicia con 02 |
| **Moneda** | USD | NUMERIC(10,2), formato `$1,234.56` |
| **Licencia conducir** | A, B, C, D, E, F + Profesional | Lista cerrada (enum) |

### 11.1 Implementación

- **Backend:** Custom validators (`@CedulaEcuador`, `@PlacaEcuador`)
- **Frontend:** Funciones en `utils/validators.ts` + integración con VeeValidate

---

## 12. Estrategia de Testing

### 12.1 Pirámide de tests

| Tipo | Proporción | Herramienta | Ejecución |
|------|-----------|-------------|-----------|
| **Unit tests** | 70% | JUnit 5 + Mockito (Java), Vitest (Vue) | Cada commit, locales rápidos |
| **Integration tests** | 20% | Testcontainers (Postgres + RabbitMQ) | CI pipeline |
| **E2E tests** | 10% | Cypress | CI pipeline (post-merge a develop) |

### 12.2 Cobertura mínima

| Capa | Mínimo |
|------|--------|
| Service layer (Java) | 90% |
| Controller (Java) | 80% |
| Repository | Opcional (Spring Data ya lo testea) |
| Componentes Vue | 80% |
| **Global por microservicio** | 80% |

### 12.3 Tags

- `@Tag("integration")` para tests de integración (corren con `mvn verify -Dgroups=integration`)
- Tests unitarios corren con `mvn test`

---

## 13. CI/CD

### 13.1 GitHub Actions desde Sprint 1

**Workflows (estado al 2026-05-26):**

| Workflow | Trigger | Acciones | Estado |
|----------|---------|---------|--------|
| `backend-ci.yml` | Push a feature/main | Maven build, unit tests, JaCoCo report | ✅ Activo |
| `docker-build.yml` | Push a main / PRs | Build de imágenes Docker de cada MS | ✅ Activo |
| `frontend-ci.yml` | Push con cambios en `frontend/**` | `npm ci` + `vite build` + upload `dist/` artifact | ✅ Activo (desde Sprint 10) |
| `integration-tests.yml` | PR / push a main | Services Postgres 15 + RabbitMQ 3.12, `mvn verify`, init-schemas.sql, tests con `@Tag("integration")` | ✅ Activo (desde Sprint 10) |
| `smoke-e2e.yml` | PR / push a main | `docker compose up` 14 contenedores, Eureka 9 apps, 10 `/actuator/health`, login admin con retry, 12 endpoints, 404 + 400 ProblemDetail. En failure: dump logs como artifact | ✅ Activo (desde Sprint 10, sustituye al `e2e-tests.yml` Cypress original) |
| `deploy-staging.yml` (planeado) | Merge a main | Build images, push to registry, deploy a Oracle Cloud | ⚠ Pendiente Sprint 12 |

### 13.2 JaCoCo (cobertura)

- Plugin en `pom.xml` de cada MS
- Genera reporte HTML en `target/site/jacoco/index.html`
- **Threshold 80% activado desde Sprint 4** (antes hay poco código)
- `mvn jacoco:check` falla el build si cobertura < 80%
- Subida de reportes a Codecov (opcional)

### 13.3 SonarQube (opcional)

- Integración en Sprint 4
- Quality gate como required check en PRs

---

## 14. Logging y Observabilidad

### 14.1 Logging

- **Formato:** JSON estructurado con Logback + logstash-logback-encoder
- **Correlation ID:** todos los logs llevan `correlationId` propagado desde el Gateway
- **Niveles:** ERROR, WARN, INFO (default), DEBUG (solo dev)
- **Archivos:** logs van a stdout (Docker captura)

### 14.2 Spring Actuator

Endpoints habilitados desde Sprint 1:
- `/actuator/health` (healthcheck)
- `/actuator/info` (versión, build)
- `/actuator/metrics` (métricas básicas)

### 14.3 Prometheus + Grafana

- Opcional para Sprint 12 si queda tiempo
- No es bloqueante para titulación

---

## 15. API Design

### 15.1 Convenciones REST

- Recursos en plural: `/estudiantes`, `/instructores`
- Métodos HTTP correctos: GET, POST, PUT, PATCH, DELETE
- Códigos de respuesta estándar: 200, 201, 204, 400, 401, 403, 404, 409, 500

### 15.2 Paginación

- **Default:** 20 items por página
- **Máximo:** 100 items por página
- **Query params:** `?page=0&size=20&sort=fechaCreacion,desc`
- **Respuesta:** Spring Data `Page<T>` con metadata (totalElements, totalPages, etc.)

### 15.3 Formato de fechas

- **Backend (API):** ISO 8601 con UTC → `2026-05-15T14:30:00Z`
- **Frontend (UI):** DD/MM/YYYY HH:mm → `15/05/2026 14:30`
- **BD:** TIMESTAMP en UTC
- **Conversión en frontend** con `date-fns` o `dayjs`

### 15.4 Formato de errores: RFC 7807 Problem Details

```json
{
  "type": "https://api.escuela.com/errors/cedula-duplicada",
  "title": "Cédula duplicada",
  "status": 409,
  "detail": "Ya existe un estudiante con cédula 1712345678",
  "instance": "/estudiantes",
  "timestamp": "2026-05-15T14:30:00Z",
  "errors": []
}
```

Para errores de validación, `errors` contiene array de `{field, message}`.

### 15.5 Documentación OpenAPI

- SpringDoc en cada MS genera automáticamente `/v3/api-docs`
- Swagger UI disponible en `/swagger-ui.html`
- Anotaciones obligatorias: `@Tag`, `@Operation`, `@ApiResponses`, `@Schema`

---

## 16. Notificaciones (MS-Notificaciones)

### 16.1 Canales

**In-app (campanita 🔔)**
- Tabla `notificaciones` en `notificaciones_schema`
- Frontend hace polling cada 30 segundos a `GET /notificaciones?leidas=false`
- Marcar como leída: `PATCH /notificaciones/{id}/leer`

**Email (Mailtrap dev / Gmail SMTP prod)**
- Plantillas HTML con Thymeleaf
- Tabla `plantillas_email` configurable por el ADMIN
- Tabla `log_envios` registra todos los emails enviados (auditoría)

### 16.2 Eventos que disparan notificaciones

| Evento | In-app | Email |
|--------|--------|-------|
| Recuperar contraseña | ❌ | ✅ (obligatorio) |
| Confirmación de matrícula | ✅ | ✅ |
| Recibo de pago | ✅ | ✅ |
| Recordatorio de clase (24h antes) | ✅ | ✅ |
| Asignación de clase nueva | ✅ | ✅ |
| Reprogramación de clase | ✅ | ✅ |
| Cancelación de clase | ✅ | ✅ |
| SOAT por vencer (30 días antes) | ✅ (admin) | ❌ |
| Estudiante con deuda | ✅ (admin) | ❌ |

### 16.3 Implementación

- MS-Notificaciones consume eventos de RabbitMQ de los demás MS
- Topic exchange `events.notificaciones` con routing keys por tipo
- Idempotencia con tabla `processed_events` (no procesar el mismo evento dos veces)

---

## 17. Multi-tenancy y Configuración

### 17.1 Modelo: Single-tenant configurable

- Cada deploy = 1 escuela cliente
- Datos completamente aislados (BD propia)
- Personalización a través del módulo "Configuración" en MS-Auth

### 17.2 Tabla `configuracion_escuela` (en `auth_schema`)

Parámetros configurables por el ADMIN desde el panel:

**Datos de la escuela:**
- nombre, RUC, dirección, teléfono, email
- logo (URL en MinIO)
- color primario y secundario (branding)

**Parámetros de operación:**
- duración estándar de clases (60/90/120 min, configurable)
- horario de operación (apertura, cierre)
- días laborables

**Tipos de cursos (tabla `tipos_curso`):**
- nombre, descripción, duración total, precio base
- Lista configurable, no hardcoded

**Conceptos de facturación (tabla `conceptos_facturacion`):**
- nombre, monto base, descripción
- Lista configurable: "Curso Básico", "Examen", "Repetición", etc.

**Categorías de licencias enseñadas (tabla `categorias_licencia`):**
- A, B, C, D, E, F, Profesional
- Configurables (escuela podría no enseñar todas)

**Configuración de notificaciones:**
- email de origen (`MAIL_FROM`)
- horas antes de clase para recordatorio (default 24)
- días antes para alerta SOAT (default 30)

**Plantillas de email:**
- Editables por el ADMIN
- Variables: `{{nombre_estudiante}}`, `{{fecha_clase}}`, etc.

### 17.3 Endpoints de configuración

```
GET /configuracion              # Obtener config actual
PUT /configuracion              # Actualizar config (solo ADMIN)
GET /tipos-curso                # Listar
POST /tipos-curso               # Crear (solo ADMIN)
PUT /tipos-curso/{id}           # Actualizar
DELETE /tipos-curso/{id}        # Eliminar
# Similar para conceptos-facturacion, categorias-licencia, plantillas-email
```

---

## 18. Despliegue

### 18.1 Ambiente local (desarrollo)

- `docker-compose -f infrastructure/docker/docker-compose.infra.yml up -d` → solo BD/Rabbit/Eureka/MinIO
- Cada microservicio se corre desde el IDE o `mvn spring-boot:run`
- Frontend con `npm run dev` (puerto 5173)

### 18.2 Ambiente production-like local

- `docker-compose -f infrastructure/docker/docker-compose.yml up -d` → todo containerizado

### 18.3 Producción

**Decisión: Oracle Cloud Free Tier** (fallback: DigitalOcean Droplet $6/mes)

**Recursos:**
- Oracle Cloud: 4 ARM cores + 24GB RAM (gratis siempre)
- 1 VPS Ubuntu 22.04 LTS
- Docker + Docker Compose instalados
- Nginx como reverse proxy con SSL (Let's Encrypt)
- Dominio: a definir (Namecheap ~$10/año o subdominio gratis)

**Pipeline de deploy (GitHub Actions):**
1. Build de imágenes Docker
2. Push a Docker Hub o GitHub Container Registry
3. SSH al VPS y `docker-compose pull && docker-compose up -d`

---

## 19. Definition of Done (DoD)

Cada tarea, antes de hacerse commit y considerarse "done", debe cumplir:

- [ ] Código implementado siguiendo las convenciones de este documento
- [ ] Tests unitarios escritos y pasando (cobertura ≥ 80%)
- [ ] Tests de integración (si aplica) pasando
- [ ] OpenAPI spec actualizado (para endpoints nuevos)
- [ ] Code review por al menos 1 persona
- [ ] Sin warnings del compilador
- [ ] Sin `TODO` o `FIXME` sin issue asociado
- [ ] CI verde (build + tests pasando)
- [ ] JaCoCo report verde (a partir de Sprint 4)
- [ ] Probado manualmente en local
- [ ] Documentación actualizada si hay cambios significativos
- [ ] Sin vulnerabilidades nuevas (OWASP Top 10)
- [ ] Performance dentro de los SLAs (<500ms p95)
- [ ] Migraciones de BD probadas y documentadas

---

## 20. Plan de 12 Sprints (referencia histórica)

> **⚠️ ACTUALIZACIÓN 2026-05-22:** Los Sprints 5-12 fueron **replanteados** del enfoque horizontal al enfoque **vertical por grupos** (ver sección 23 y `PLAN_FASES.md`). La tabla siguiente es la versión histórica original. Para los Sprints 5-12 vigentes, consultar `PLAN_FASES.md`.

### 20.1 Plan original (Sprints 0-4 ejecutados, Sprints 5-12 sustituidos por PLAN_FASES.md)

| Sprint | Capa horizontal | Foco | Estado |
|--------|----------------|------|--------|
| 0-4 | Estructura, BD, mensajería, seguridad | ✅ Ejecutados horizontal (Sprints 1-4 cerrados) | ✅ Cerrado |
| ~~5~~ | ~~Lógica de negocio~~ | ~~CRUD Estudiantes (patrón replicable)~~ | ❌ Sustituido por PLAN_FASES.md |
| ~~6~~ | ~~Lógica de negocio~~ | ~~CRUD Instructores + Vehículos~~ | ❌ Sustituido |
| ~~7~~ | ~~Lógica de negocio~~ | ~~CRUD Asignaciones (cross-MS) + Cobros~~ | ❌ Sustituido |
| ~~8~~ | ~~Reportería~~ | ~~MS-Reportes + agregación + exportación PDF/Excel~~ | ❌ Sustituido |
| ~~9~~ | ~~Frontend base~~ | ~~Vue 3 + Pinia + Router + Axios + componentes base~~ | ❌ Sustituido |
| ~~10~~ | ~~Frontend CRUDs~~ | ~~UI Estudiantes/Instructores/Vehículos/Cobros~~ | ❌ Sustituido |
| ~~11~~ | ~~Frontend cierre~~ | ~~UI Asignaciones + Reportes + Dashboard + E2E tests~~ | ❌ Sustituido |
| ~~12~~ | ~~Cierre~~ | ~~Tests 80%+ + Deploy + Documentación final~~ | ✅ Mantiene foco de cierre |

### 20.2 Plan vigente (Sprints 5-12) — estado al 2026-05-26

| Sprint | Fase | Foco planeado | Estado real |
|--------|------|---------------|-------------|
| 5 | Fase 1 — Grupo A | Backend A pt.1: CRUDs Auth+Estudiantes+Instructores+Vehículos | ✅ Cerrado |
| 6 | Fase 1 — Grupo A | Backend A pt.2: CRUDs Asignaciones+Cobros | ✅ Cerrado |
| 7 | Fase 1 — Grupo A | Frontend completo del Grupo A | ✅ Cerrado |
| 8 | Fase 1 — Grupo A | Testing Grupo A (unit+IT+E2E) | ✅ Cerrado |
| 9 | Fase 2 — Grupo B | Backend Grupo B: Notificaciones+Reportes (plantillas, in-app, log envíos, reportes operativos/financieros, exportación PDF/Excel, cache Caffeine) | ⚠ **PENDIENTE** |
| 10 | Fase 2 — Grupo B | Plan original: Frontend Grupo B (dashboard, reportes, notif) | ⚠ **Ejecutado con scope distinto:** pulido Grupo A (kilometraje, tipos combustible, contratos instructores, StatCards estudiantes) + refactor estados/situacion_pago + 3 nuevos workflows CI/CD + fixes E2E. El Frontend Grupo B queda pendiente |
| 11 | Fase 2 — Grupo B | Testing Grupo B (unit+IT+E2E Cypress 3 flujos) | ⚠ Pendiente |
| 12 | Fase 3 — Cierre | E2E cruzado + Performance + OWASP + Rate limiting Gateway + Deploy Oracle Cloud + Demo + Docs final | ⚠ Pendiente |

> **Detalle completo, tareas, subtareas y criterios de aceptación en `PLAN_FASES.md`.**
>
> ⚠ Al 2026-05-26 hay **drift entre `PLAN_FASES.md` y la ejecución real** del Sprint 10. Pendiente: reestructurar los sprints restantes para reincorporar Frontend Grupo B (que no se hizo en Sprint 10) y mantener Testing Grupo B + Cierre.

---

## 21. Resumen ejecutivo de decisiones

| # | Decisión | Valor |
|---|----------|-------|
| 1 | Stack backend | Java 21 + Spring Boot 3 + Spring Cloud |
| 2 | Stack frontend | Vue 3 + TypeScript + PrimeVue + Pinia |
| 3 | Base de datos | PostgreSQL 15, 1 instancia, 9 schemas |
| 4 | Mensajería | RabbitMQ |
| 5 | Cache | Caffeine (in-memory) |
| 6 | Service Discovery | Eureka |
| 7 | API Gateway | Spring Cloud Gateway |
| 8 | File storage | MinIO |
| 9 | Email | Mailtrap (dev) / Gmail SMTP (prod) |
| 10 | Auth | JWT 512 bits + HttpOnly cookies + BCrypt |
| 11 | Tracking | Jira |
| 12 | Repositorio | Monorepo Git |
| 13 | Branching | Trunk-based simplificado |
| 14 | Commits | `Sprint N (Tarea)` formato |
| 15 | CI/CD | GitHub Actions desde Sprint 1 |
| 16 | Cobertura | JaCoCo 80% threshold (activo desde Sprint 4) |
| 17 | Testing | Pirámide 70/20/10 + Testcontainers + Cypress |
| 18 | Logging | JSON estructurado + correlationId + Actuator |
| 19 | Idioma código | Español respetando convenciones del lenguaje |
| 20 | API errors | RFC 7807 Problem Details |
| 21 | Paginación | 20 default / 100 max |
| 22 | Rate limiting | 100 req/min/IP en Gateway (Sprint 4) |
| 23 | Soft delete | Todas excepto pagos y audit_log |
| 24 | Fechas | ISO 8601 backend / DD/MM/YYYY UI |
| 25 | Multi-tenant | Single-tenant configurable |
| 26 | Roles | ADMIN, STAFF, INSTRUCTOR, ESTUDIANTE |
| 27 | Notificaciones | In-app + email |
| 28 | Despliegue | Oracle Cloud Free Tier (fallback DigitalOcean $6) |
| 29 | Validaciones | Cédula, RUC, placas, teléfono, USD (Ecuador) |
| 30 | Sprints | 12 sprints de 1 semana. **Sprints 1-4 horizontal (ejecutados). Sprints 5-12 vertical por grupos** (ver §23 y `PLAN_FASES.md`) |

---

## 22. Próximos pasos (al 2026-05-26)

1. **Reestructurar el plan restante** — el Sprint 10 ejecutado no coincide con el "Frontend Grupo B" del plan. Redistribuir los entregables pendientes (Backend Grupo B + Frontend Grupo B + Testing Grupo B + Cierre) en los sprints restantes y actualizar `PLAN_FASES.md` y `SPRINTS_PLAN.xlsx` con la nueva secuencia.
2. **Sprint 9 — Backend Grupo B (pendiente):** MS-Notificaciones (plantillas CRUD, in-app, log envíos, consumer de eventos del Grupo A) + MS-Reportes (endpoints operativos y financieros, exportación PDF/Excel, cache Caffeine). ~6 PRs según `PLAN_FASES.md §4`.
3. **Frontend Grupo B (pendiente):** NotificacionesDropdown con badge + polling, PlantillasEmailView, DashboardView con KPIs (Chart.js), Reportes UI operativos/financieros. ~5 PRs.
4. **Testing Grupo B (pendiente):** unit ≥80% MS-Notif + MS-Reportes, integration con Testcontainers + GreenMail SMTP + Feign mocks, E2E Cypress (evento→email→notif in-app, reporte+PDF, plantilla email + envío de prueba). ~5 PRs.
5. **Sprint 12 — Cierre global (pendiente):** E2E cruzado (matrícula→factura→clases→pago→recibo→reporte), JMeter 50 usuarios p95<500ms, OWASP Top 10, rate limiting Gateway con Bucket4j, limpieza (TODOs, console.log), docs final (README + runbook + manual usuario + C4 actualizado), deploy Oracle Cloud Free Tier + Nginx + Let's Encrypt + backups, video demo 15 min, tag `v1.0.0`. ~8 PRs.

**Hitos cerrados al 2026-05-26:**
- 5 PRs (#38-#42) del Sprint 10 mergeados en `main` (commit `de106fa`)
- 3 nuevos workflows CI/CD activos: `frontend-ci.yml`, `integration-tests.yml`, `smoke-e2e.yml`
- Refactor de estados estudiante + situacion_pago ampliado a VARCHAR(30) (ver §24)
- Kilometraje E2E en asignaciones + sync cross-MS + 6 validaciones nuevas en crear asignación (ver §24)
- Estabilización CI/CD y plataforma: TZ JVM Dockerfile, 404/400 ProblemDetail, ajustes GHA Free Tier, V6 fix bcrypt hash (ver §25)

---

## 23. ADR-2026-05-22 — Cambio de enfoque horizontal a vertical por grupos (Sprints 5-12)

**Fecha:** 2026-05-22
**Estado:** ✅ Aceptada
**Decisor:** Hernán Mateo Jurado Moran (titular del desarrollo)

### Contexto

Los Sprints 0-4 se ejecutaron bajo desarrollo **horizontal** (todas las capas — estructura, BD, mensajería, seguridad — avanzaban a la par en TODOS los microservicios). Este enfoque fue acertado para la infraestructura base porque permitió tener:
- Estructura Maven consistente en los 8 MS (Sprint 1)
- 9 schemas + 38 tablas creados de una sola pasada (Sprint 2)
- RabbitMQ topology con 8 exchanges + 16 queues en todos los MS (Sprint 3)
- JWT framework + Gateway + Notificaciones funcionando E2E (Sprint 4)

Sin embargo, al llegar a los Sprints 5-12 (lógica de negocio + frontend + testing), el enfoque horizontal ya no es óptimo porque:
- Habría que avanzar el CRUD de los 8 MS en paralelo, sin entregables completos hasta el final.
- El frontend tendría que esperar al Sprint 9 (en el plan original) para empezar, comprimiendo demasiado el cierre del proyecto.
- El testing E2E quedaría al final, dejando bugs acumulados.

### Decisión

A partir del **Sprint 5**, cambiar al enfoque **vertical por grupos**:

1. **Dividir los 8 MS en 2 grupos:**
   - **Grupo A (6 MS principales):** Auth, Estudiantes, Instructores, Vehículos, Asignaciones, Cobros
   - **Grupo B (2 MS secundarios):** Notificaciones, Reportes

2. **Por cada grupo, completar todas las capas antes de pasar al siguiente:**
   - Backend (CRUDs, lógica, eventos)
   - Frontend (vistas, forms, stores)
   - Testing (unit, integration, E2E)

3. **Sprint 12 queda como cierre global** (E2E cruzado, performance, OWASP, deploy, demo, documentación final).

### Justificación

- **Entregables funcionales completos al cierre de cada fase:** al final del Sprint 8, el Grupo A está 100% usable. Al final del Sprint 11, todo el sistema funciona.
- **Mejor para demos parciales:** el equipo (y eventualmente el jurado) puede ver el Grupo A funcionando antes de empezar el B.
- **Reduce el riesgo de quedarse sin tiempo al final:** los 6 MS más importantes están terminados antes del Sprint 9.
- **El frontend arranca antes:** en Sprint 7 ya hay UI funcional, no en Sprint 9 como el plan original.
- **Testing distribuido en el tiempo:** unit+IT+E2E del Grupo A en Sprint 8, del Grupo B en Sprint 11 — bugs detectados más temprano, no todos al final.

### Consecuencias

**Positivas:**
- Cada fase produce un sistema parcial pero funcional al 100%.
- Riesgo de "todo a medias" eliminado.
- Demos intermedias posibles tras Sprint 8 y Sprint 11.
- Trazabilidad clara: cada sprint del PLAN_FASES.md → conjunto de PRs mergeados.

**Negativas (mitigadas):**
- MS-Asignaciones (Grupo A) necesita Instructores funcional. **Mitigación:** MS-Instructores se incluye en el Sprint 5 (parte del Grupo A), así Asignaciones (Sprint 6) puede consumir Feign al CRUD ya funcional.
- Notificaciones y Reportes (Grupo B) reciben eventos del Grupo A. **Mitigación:** los eventos ya están publicados desde Sprint 6; el Grupo B los empieza a consumir desde Sprint 9 sin bloqueo.
- Frontend del Grupo B (Sprint 10) reusa la base del Sprint 7. **Mitigación:** ya se planificó así en `PLAN_FASES.md`.

### Documento de referencia

Detalle completo de tareas, subtareas, criterios de aceptación y dependencias: **`PLAN_FASES.md`** (raíz del repo).

### Actualizaciones derivadas

- Sección 20 de este documento actualizada para marcar el plan horizontal de Sprints 5-12 como histórico.
- Sección 21 #30 actualizada para reflejar enfoque híbrido.
- `SPRINTS_PLAN.xlsx` queda como referencia histórica del plan original; PLAN_FASES.md es la fuente de verdad para Sprints 5-12.

---

## 24. ADR-2026-05-Sprint10 — Refactor de dominio y endurecimiento operativo del Grupo A

**Fecha:** Sprint 10 (cerrado en `main` el 2026-05-26)
**Estado:** ✅ Aceptada e implementada (PRs #36, #37, #39, #40, #42 mergeados)
**Decisor:** Hernán Mateo Jurado Moran

### Contexto

Durante la validación E2E previa al cierre de la Fase 1 del proyecto se detectaron tres categorías de hallazgos en el Grupo A que requerían ajustes de dominio:

1. **Estados del estudiante y situación de pago** estaban subespecificados: solo existía `PENDIENTE_MATRICULA` y faltaban estados intermedios (`MATRICULADO`, `CURSANDO`) que reflejaran el ciclo de vida real desde la matrícula hasta la finalización del curso. La columna `situacion_pago` tampoco soportaba el flujo de pagos parciales con la granularidad necesaria.
2. **MS-Cobros** no modelaba la opción de crédito: las facturas se cobraban siempre como pago único, sin posibilidad de plan de cuotas.
3. **MS-Asignaciones** no registraba el kilometraje real recorrido durante cada clase, lo que rompía la trazabilidad de uso de vehículos y de horas dictadas/recibidas. Adicionalmente, varias validaciones de negocio críticas no se ejecutaban al crear una asignación.

### Decisión

**24.1 Refactor de estados del estudiante + `situacion_pago` ampliado**

- Estados extendidos: `PRE_INSCRITO` → `MATRICULADO` → `CURSANDO` → `FINALIZADO` (más `RETIRADO` como estado terminal alterno).
- Auto-transiciones por evento:
  - `MATRICULADO` cuando se emite la primera factura del estudiante.
  - `CURSANDO` cuando se completa la primera asignación.
  - `FINALIZADO` cuando `horasCompletadas ≥ horasRequeridas` del tipo de curso.
- Columna `situacion_pago` ampliada a `VARCHAR(30)` (V4 + V4 fix) y enum: `PENDIENTE_FACTURACION` / `PENDIENTE_PAGO` / `PAGO_PARCIAL` / `PAGADO_TOTAL`.
- Default de `situacion_pago` cambiado a `PENDIENTE_FACTURACION` (PR #39) — el default anterior (`PENDIENTE_MATRICULA`) violaba el check constraint y rompía todo `POST /estudiantes` con 500.
- Frontend alineado: `StatusBadge`, `EstadoCuentaView`, `ListaEstudiantes` muestran las nuevas etiquetas.

**24.2 Modelo de crédito en MS-Cobros + tabla `factura_cuotas`**

- La factura puede emitirse con o sin plan de cuotas; si lleva crédito, se generan N registros en `factura_cuotas` con fecha de vencimiento y monto por cuota.
- El cálculo de saldo de la factura suma los pagos aplicados a cada cuota, no al total directo.
- Estados de cuota: `PENDIENTE` / `VENCIDA` / `PAGADA`. Estado de factura derivado de sus cuotas.

**24.3 Kilometraje E2E en MS-Asignaciones + sync cross-MS**

- La asignación se inicia con `kilometrajeInicial` y se finaliza con `kilometrajeFinal` y `minutosCompletados`.
- Al finalizar la clase se publica el evento `AsignacionCompletadaEvent` consumido por:
  - **MS-Vehículos**: actualiza el odómetro del vehículo via Feign **PUT** (no PATCH — limitación HttpURLConnection de Feign por default, documentada como deuda).
  - **MS-Estudiantes**: incrementa `minutosCompletados` del progreso académico y recalcula `horasCompletadas/horasRequeridas/porcentaje` on-the-fly contra el `tipoCurso` del estudiante (vía Feign `TipoCursoClient`).
- El listener de progreso académico **NO usa idempotencia** porque la `IdempotencyStore` actual tiene `UNIQUE(event_id)` y no permite dos consumers del mismo MS bindando el mismo routing key. Trade-off documentado; fix futuro: ampliar a `UNIQUE(event_id, consumer_scope)`.

**24.4 Seis validaciones nuevas obligatorias al crear asignación**

Al ejecutar `POST /asignaciones` se valida ahora (vía Feign cross-MS):

1. La categoría de licencia del **instructor** habilita la categoría que el estudiante está cursando.
2. La categoría del **vehículo** coincide con la categoría que el estudiante está cursando.
3. El vehículo tiene **SOAT vigente** a la fecha de la asignación.
4. El vehículo tiene **RTV (revisión técnica vehicular) vigente** a la fecha.
5. El **horario semanal del instructor** cubre el rango horario solicitado.
6. El instructor no está en **AUSENCIA** (vacaciones/licencia) en esa fecha.

Cualquier falla devuelve `409 Conflict` con `ProblemDetail` específico.

### Consecuencias

- El ciclo de vida del estudiante queda trazable y consultable por filtros del frontend.
- Los reportes financieros (pendientes del Sprint 9) podrán mostrar cartera por vencer con la granularidad de cuota.
- El uso de vehículos queda auditable: cualquier discrepancia de odómetro se puede reconciliar contra la suma de kilometrajes de las asignaciones.
- Las 6 validaciones nuevas previenen errores operativos comunes (instructor sin categoría, vehículo sin SOAT, etc.) que antes solo se detectaban manualmente.

### Migraciones BD aplicadas

- MS-Estudiantes V3, V4 (situacion_pago + estados extendidos)
- MS-Cobros V_factura_cuotas
- MS-Asignaciones V_kilometraje + V_validaciones

---

## 25. ADR-2026-05-Sprint10 — Estabilización CI/CD y plataforma

**Fecha:** Sprint 10 (cerrado en `main` el 2026-05-26, PR #38)
**Estado:** ✅ Aceptada e implementada
**Decisor:** Hernán Mateo Jurado Moran

### Contexto

Al ejecutar el primer pipeline E2E completo en GitHub Actions sobre el stack containerizado real (14 contenedores) salieron a la luz varios problemas operativos que en local no se manifestaban:

- Los `LocalTime/LocalDate` persistidos se desfasaban +5 horas al ir a Postgres porque la JVM dentro del contenedor corría en UTC mientras que el resto del sistema asumía `America/Guayaquil`.
- Los runners gratuitos de GitHub Actions son significativamente más lentos que una laptop de desarrollo: Spring Boot tardaba ~200 segundos en arrancar dentro del runner, contra ~30 s en local. Los healthchecks y timeouts originales reventaban antes de que los servicios estuvieran listos.
- El `GlobalExceptionHandler` con un `@ExceptionHandler(Exception.class)` capturaba `NoHandlerFoundException` y devolvía `500` cuando el handler correcto era `404`. Adicionalmente, `MethodArgumentTypeMismatchException` (path variable con tipo inválido, ej. `/estudiantes/abc`) también caía al `500`.
- El admin seed de `V1_5__Seed_Data.sql` tenía un hash bcrypt que no matcheaba la contraseña documentada `Admin123!`. En las BD locales funcionaba porque alguien lo había cambiado manualmente; en cualquier despliegue limpio el login admin fallaba con `401`.

### Decisión

**25.1 Zona horaria fija en JVM (Dockerfile.spring)**

Se hardcodea en `infrastructure/docker/Dockerfile.spring`:
```dockerfile
ENV JAVA_OPTS="-Duser.timezone=America/Guayaquil"
```
Sobrescribible por `docker-compose` si en algún ambiente futuro hace falta otra TZ. Sin esto, `LocalDateTime.now()` dentro del contenedor devuelve UTC y se persiste con desfase de +5 h.

**25.2 Ajustes de timing para runner GHA Free Tier**

- Healthcheck del `Dockerfile.spring`: `start-period=180s` + `retries=10` (vs los defaults 30s/3 que reventaban antes de que Spring termine de arrancar).
- En el workflow `smoke-e2e.yml`: `docker compose up --wait --wait-timeout 600` (10 min de margen).
- Retry loop 6×10 s al primer login admin contra el Gateway (el `LoadBalancer` interno tarda hasta 30 s en refrescar la cache de Eureka tras el arranque de `ms-auth`).

**25.3 Configuración global de 404/400 con ProblemDetail RFC 7807**

En cada `application.yml` de los MS afectados:
```yaml
spring:
  mvc:
    throw-exception-if-no-handler-found: true
  web:
    resources:
      add-mappings: false
```
Esto permite que `NoHandlerFoundException` llegue al `@ExceptionHandler` y devuelva `404` con `ProblemDetail`. Adicionalmente se agregó handler para `MethodArgumentTypeMismatchException` → `400` (path variable de tipo inválido devolvía 500 antes).

**25.4 Migración V6 — corrección de hash bcrypt del admin seed**

`V6__fix_admin_password_hash.sql` regenera el hash bcrypt correcto para `Admin123!` en el usuario `admin@escuela.local` del seed. A partir de cualquier despliegue limpio el login admin funciona sin intervención manual.

**25.5 Tres nuevos workflows CI/CD**

- `frontend-ci.yml` — `npm ci` + `vite build` + upload artifact `dist/`. Dispara con cambios a `frontend/**`.
- `integration-tests.yml` — services Postgres 15 + RabbitMQ 3.12, `mvn verify -Dgroups=integration` con `init-schemas.sql`, step separado para tests con `@Tag("integration")` (queda preparado; hoy no hay tests con ese tag).
- `smoke-e2e.yml` — `docker compose up` 14 contenedores, valida Eureka registró 9 apps, `/actuator/health` de los 10 servicios, login admin con retry, 12 endpoints REST, comportamiento 404 y 400 ProblemDetail. En failure: dump de logs de contenedores como artifact.

### Limitaciones conocidas (deuda registrada)

- **Feign no soporta PATCH por default** (HttpURLConnection). El sync de odómetro de vehículo usa `PUT` como workaround. Fix futuro: configurar OkHttp.
- **IdempotencyStore con `UNIQUE(event_id)`**: impide que dos consumers del mismo MS procesen el mismo evento. El listener de progreso académico fue exento de idempotency. Fix futuro: ampliar schema a `UNIQUE(event_id, consumer_scope)`.
- **vue-tsc roto en local** por bug upstream de Node 22 + TS 5.6 (no nuestro). El build con `vite build` funciona; el `type-check` por separado no.
- **4 vulnerabilidades dependabot en `main`** (1 high, 3 moderate). A revisar en limpieza del Sprint 12.
- **Caffeine cache nunca implementado**; queda como deuda para Sprint 9 (reportes) o Sprint 12.

### Consecuencias

- El pipeline E2E corre verde sobre runners gratuitos de GitHub Actions, permitiendo gate de calidad en cada PR a `main` sin coste.
- Cualquier despliegue limpio del sistema (Oracle Cloud futuro, DigitalOcean fallback, o re-bootstrap local) tiene admin operativo desde el primer arranque.
- Las respuestas de error siguen RFC 7807 de forma consistente, alineadas con §15.4 de este documento.

---

> **Este documento está cerrado para decisiones estructurales originales.** Cambios futuros se registran como ADRs nuevos (§24, §25, ...) en este mismo archivo.

---

*Documento generado: 2026-05-06*
*Última actualización: 2026-05-26 — agregadas secciones §24 (refactor dominio Grupo A) y §25 (estabilización CI/CD y plataforma); actualizados encabezado, §13.1 workflows, §20 estado real de sprints, §22 próximos pasos*
