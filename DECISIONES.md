# DECISIONES.md

**Documento de Decisiones Técnicas y de Arquitectura**
**Proyecto:** Sistema de Control Administrativo y Financiero para Escuelas de Conducción
**Universidad:** Universidad de las Américas (UDLA), Quito, Ecuador
**Fecha de cierre de decisiones:** 2026-05-06
**Estado:** Definiciones cerradas - Listo para iniciar Sprint 1

> Este documento consolida todas las decisiones técnicas, arquitectónicas y de proceso tomadas antes de iniciar el desarrollo. Es la fuente de verdad del proyecto y debe ser respetado durante toda la ejecución de los 12 sprints. Para cambios futuros, registrar como ADR en `/docs/decisions/`.

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

### 9.1 Branching strategy: GitHub Flow simplificado

> **Actualizado en Sprint 2.0** (cambio desde "Trunk-based con develop" a "GitHub Flow"). Razón: simplicidad, menor fricción y flujo estándar en la industria moderna. Cada feature pasa por PR con CI/CD obligatorio antes de merge a `main`.

**Branches:**
- `main` → branch principal, siempre estable, **protegida** (no se permite push directo)
- `feature/sprint-N-<descripcion-corta>` → para nuevas features de cada sprint
- `fix/<descripcion-corta>` → para correcciones de bugs
- `docs/sprint-N-<descripcion-corta>` → para cambios solo de documentación
- `chore/<descripcion-corta>` → para tareas de mantenimiento (deps, configs, CI)

**Flujo:**
1. Crear branch desde `main` actualizado
2. Hacer commits con formato `Sprint N (Tarea)` (ver 9.2)
3. Push del branch al remote (`git push -u origin <branch>`)
4. Abrir Pull Request en GitHub apuntando a `main`
5. Esperar a que CI pase (Backend CI + Docker Build si aplica)
6. **Squash and merge** desde GitHub UI (mantiene historia limpia en `main`)
7. Eliminar el branch tras merge (GitHub lo ofrece automáticamente)

**Convenciones de naming de branches:**
- Usar guiones: `feature/sprint-2-1-disenar-schema-bd` (no underscore ni camelCase)
- Mantenerlo corto: 3-5 palabras descriptivas
- Sin acentos ni caracteres especiales

**Excepción permitida:** commits directos a `main` solo en la fase de **setup inicial** del repositorio (Sprints 0-2.0). A partir del Sprint 2.1, todo via PR.

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

**Workflows:**

| Workflow | Trigger | Acciones |
|----------|---------|---------|
| `backend-ci.yml` | Push a feature/develop/main | Maven build, unit tests, JaCoCo report |
| `frontend-ci.yml` | Push con cambios en `frontend/` | npm install, lint, unit tests, build |
| `integration-tests.yml` | PR a develop | Levanta Testcontainers, corre integration tests |
| `e2e-tests.yml` | Post-merge a develop | docker-compose up, Cypress E2E |
| `deploy-staging.yml` | Merge a main | Build images, push to registry, deploy a Oracle Cloud |

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

## 20. Plan de 12 Sprints (referencia)

| Sprint | Capa horizontal | Foco |
|--------|----------------|------|
| 1 | Estructura base | Esqueleto de los 7 MS + API Gateway + Eureka + Docker |
| 2 | Bases de datos | Schemas + migraciones Flyway en TODOS los MS |
| 3 | Mensajería | RabbitMQ + eventos asincrónos en TODOS los MS |
| 4 | Seguridad | JWT en MS-Auth + filtro JWT en Gateway y todos + Rate limiting |
| 5 | Lógica de negocio | CRUD Estudiantes (patrón replicable) |
| 6 | Lógica de negocio | CRUD Instructores + Vehículos |
| 7 | Lógica de negocio | CRUD Asignaciones (cross-MS) + Cobros |
| 8 | Reportería | MS-Reportes + agregación + exportación PDF/Excel |
| 9 | Frontend base | Vue 3 + Pinia + Router + Axios + componentes base |
| 10 | Frontend CRUDs | UI Estudiantes/Instructores/Vehículos/Cobros |
| 11 | Frontend cierre | UI Asignaciones + Reportes + Dashboard + E2E tests |
| 12 | Cierre | Tests 80%+ + Deploy + Documentación final |

> Detalle completo en `SPRINTS_PLAN.xlsx` (pestaña "Sprints Detallado").

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
| 30 | Sprints | 12 sprints de 1 semana, desarrollo horizontal |

---

## 22. Próximos pasos

1. **Pendiente del equipo:** decidir si arrancamos el Sprint 1
2. **Sprint 0 (no formalizado):** crear repositorio Git en GitHub, estructura de carpetas, archivos iniciales, docker-compose de infraestructura, validar que herramientas estén instaladas
3. **Sprint 1 (oficial inicio):** implementar la estructura base de los 7 MS + API Gateway + Eureka según el plan en `SPRINTS_PLAN.xlsx`

> **Este documento está cerrado.** Cambios futuros se registran como ADRs en `/docs/decisions/`.

---

*Documento generado: 2026-05-06*
*Última actualización: 2026-05-06*
