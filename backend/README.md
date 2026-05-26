# Backend — Sistema de Control Administrativo

Backend del Sistema de Control Administrativo y Financiero para Escuelas de Conducción. Arquitectura de microservicios con Spring Boot 3 + Spring Cloud.

**Última actualización:** 2026-05-26 — Sprint 9 (Estabilización) cerrado. Sprint 10 (Backend Grupo B) en proceso.

---

## Estructura

```
backend/
├── pom.xml                      # POM padre (versiones centralizadas)
│
├── shared/                      # Librerías compartidas entre microservicios
│   ├── common-events/           # DTOs de eventos RabbitMQ (BaseEvent + dominio Sprint 3)
│   ├── common-exceptions/       # Excepciones base + handlers RFC 7807
│   ├── common-jpa/              # BaseEntity (audit + soft delete) + AuditorAware + JpaAuditingConfig
│   ├── common-security/         # Utilidades JWT (Sprint 4) + helpers de cookies HttpOnly
│   └── common-validation/       # Custom validators Ecuador (@CedulaEcuador, @PlacaEcuador, @RucEcuador)
│
├── eureka-server/               # Service discovery (puerto 8761)
├── api-gateway/                 # API Gateway con Spring Cloud Gateway (puerto 8080)
│
├── ms-auth/                     # Autenticación + Configuración + Usuarios (puerto 8081)
├── ms-estudiantes/              # Gestión de estudiantes + progreso académico (puerto 8082)
├── ms-instructores/             # Instructores + certificaciones + contratos (puerto 8083)
├── ms-vehiculos/                # Flota + mantenimientos + combustible + tipos combustible (puerto 8084)
├── ms-asignaciones/             # Programación tripartita + kilometraje E2E (puerto 8085)
├── ms-cobros/                   # Facturación + pagos parciales + crédito a cuotas (puerto 8086)
├── ms-reportes/                 # Reportes + exportación PDF/Excel (puerto 8087) [en proceso]
└── ms-notificaciones/           # In-app + email (puerto 8088) [en proceso para plantillas + in-app]
```

---

## Arranque local (desarrollo)

### 1. Levantar infraestructura (Docker)

```bash
# Desde la raíz del proyecto
docker compose -f infrastructure/docker/docker-compose.infra.yml up -d
```

Esto levanta:
- PostgreSQL en `localhost:5432` (escuela_db con 9 schemas)
- RabbitMQ en `localhost:5672` (UI: http://localhost:15672)
- MinIO en `localhost:9000` (UI: http://localhost:9001)
- Adminer en http://localhost:8888

### 2. Compilar e instalar todos los módulos

> **Importante:** este paso es necesario la primera vez para que los microservicios encuentren los módulos `shared/` en el repositorio Maven local.

```bash
# Desde backend/
mvn clean install -DskipTests
```

### 3. Arrancar los servicios (en orden)

**Orden recomendado:** Eureka → Gateway → microservicios.

#### a) Eureka Server (primero, siempre)

```bash
cd backend/eureka-server
mvn spring-boot:run
```

Verificar: http://localhost:8761

#### b) API Gateway

```bash
cd backend/api-gateway
mvn spring-boot:run
```

Verificar:
- http://localhost:8080/actuator/health → `UP`
- http://localhost:8080/actuator/gateway/routes → 10+ rutas hacia los MS

#### c) Microservicios de dominio

Cada uno en su propia terminal:

```bash
cd backend/ms-auth        && mvn spring-boot:run
cd backend/ms-estudiantes && mvn spring-boot:run
# ... etc.
```

Cada MS:
- Se registra automáticamente en Eureka al iniciar
- Conecta a Postgres con su schema específico
- Aplica sus migraciones Flyway al arrancar
- Conecta a RabbitMQ
- Expone `/actuator/health` en su puerto

---

## Compilación y pruebas

```bash
# Compilar todos los módulos
mvn clean compile

# Ejecutar tests unitarios
mvn test

# Ejecutar tests de integración (requiere Docker para Testcontainers)
mvn verify -Dgroups=integration

# Cobertura JaCoCo
mvn test jacoco:report
# Reportes generados en: <ms>/target/site/jacoco/index.html

# Build completo con tests
mvn clean install
```

**Cobertura objetivo:** 80% por módulo (obligatorio desde Sprint 4 para CI verde).

---

## Comandos útiles

```bash
# Compilar un solo MS y sus dependencias necesarias
mvn clean install -pl ms-auth -am -DskipTests

# Ejecutar un solo MS (requiere haber instalado las shared libs antes)
cd ms-auth && mvn spring-boot:run

# Ver dependencias de un módulo
mvn dependency:tree -pl ms-auth

# Limpiar repositorio local de los artefactos del proyecto
mvn dependency:purge-local-repository -DmanualInclude=com.escuela
```

---

## Puertos asignados

| Servicio | Puerto | URL |
|----------|--------|-----|
| API Gateway | 8080 | http://localhost:8080 |
| MS-Auth | 8081 | http://localhost:8081 |
| MS-Estudiantes | 8082 | http://localhost:8082 |
| MS-Instructores | 8083 | http://localhost:8083 |
| MS-Vehículos | 8084 | http://localhost:8084 |
| MS-Asignaciones | 8085 | http://localhost:8085 |
| MS-Cobros | 8086 | http://localhost:8086 |
| MS-Reportes | 8087 | http://localhost:8087 |
| MS-Notificaciones | 8088 | http://localhost:8088 |
| Eureka Server | 8761 | http://localhost:8761 |

---

## Endpoints útiles (Actuator)

Todos los servicios exponen:

- `/actuator/health` — Healthcheck (UP/DOWN)
- `/actuator/info` — Información del build
- `/actuator/metrics` — Métricas
- `/v3/api-docs` — SpringDoc OpenAPI 3 spec (JSON)

El **API Gateway** además expone:
- `/actuator/gateway/routes` — Rutas configuradas

El **Eureka Server** además expone:
- `/eureka/apps` — Apps registradas (con `Accept: application/json`)

---

## Stack técnico

- **Java**: 21 (LTS)
- **Spring Boot**: 3.4.0
- **Spring Cloud**: 2024.0.0 (Eureka, Gateway, OpenFeign, LoadBalancer)
- **Persistencia**: PostgreSQL 15 + JPA/Hibernate + Flyway (22 migraciones al 2026-05-26)
- **Mensajería**: RabbitMQ + Spring AMQP (consumers con idempotencia via `shared_schema.processed_events`)
- **Cache**: Caffeine in-memory (declarado, pendiente implementación masiva — ver `DECISIONES.md §25`)
- **Resilience**: Resilience4j (circuit breaker, retry en Feign clients de MS-Asignaciones)
- **JWT**: jjwt 0.12.x con HS512 (clave 512 bits) y refresh token rotation (Sprint 4 + Sprint 9 V2)
- **Object storage**: MinIO 8.5.x
- **Mapeo**: MapStruct 1.6.x con lazy mapper init en services
- **OpenAPI**: SpringDoc 2.7.x
- **Testing**: JUnit 5, Mockito, AssertJ, Testcontainers, GreenMail (SMTP), H2

### Configuraciones críticas (Sprint 9 — Estabilización)

- **TZ JVM:** `JAVA_OPTS=-Duser.timezone=America/Guayaquil` en `Dockerfile.spring` (sin esto, `LocalDateTime.now()` devuelve UTC dentro del contenedor)
- **404/400 ProblemDetail:** `spring.mvc.throw-exception-if-no-handler-found: true` + `spring.web.resources.add-mappings: false` en cada `application.yml`
- **Healthcheck Docker:** `start-period=180s + retries=10` (necesario para runners GHA Free Tier)
- **Refresh token rotation:** tabla `refresh_tokens` con JTI UUID, revocación al rotar (V2 ms-auth)
- **6 validaciones cross-MS al crear asignación:** categoría licencia, SOAT, RTV, horario instructor, AUSENCIA (Sprint 9 T9.4)

---

## Documentación adicional

- [DECISIONES.md](../DECISIONES.md) — 30 decisiones técnicas + 2 ADRs Sprint 9 (refactor dominio §24 + estabilización CI/CD §25)
- [PLAN_FASES.md](../PLAN_FASES.md) — Plan vigente de 13 sprints (vertical por grupos)
- [SPRINTS_PLAN.xlsx](../SPRINTS_PLAN.xlsx) — Plan tabular detallado por sprint y tarea
- [CLAUDE.md](../CLAUDE.md) — Guía operativa del proyecto
- [docs/database/schema.md](../docs/database/schema.md) — Modelo BD completo (41 tablas, 22 migraciones, diagramas Mermaid)
- [.github/CONTRIBUTING.md](../.github/CONTRIBUTING.md) — GitHub Flow + convenciones de commit
- [infrastructure/docker/README.md](../infrastructure/docker/README.md) — Detalle de Docker Compose

---

## Estado por sprint

### Sprints CERRADOS

| Sprint | Tema | Validación |
|--------|------|------------|
| 0 | Setup monorepo + infra docker | 4 contenedores healthy, 9 schemas creados |
| 1 | Estructura Maven + Eureka + Gateway + Containerización | BUILD SUCCESS 15/15 módulos, 14 contenedores stack completo |
| 2 | BD + Migraciones Flyway + JPA + Repositorios | 38 tablas iniciales, 33 entidades JPA, 34 repositorios, Hibernate validate verde |
| 3 | Mensajería RabbitMQ + eventos asíncronos | 8 exchanges + 16 queues + idempotencia, flujo E2E entre MS validado |
| 4 | Auth + JWT + Gateway validation + Notif base | Login/refresh/forgot/reset funcional, 129 tests pasados, 11 bugs corregidos |
| 5 | Backend Grupo A pt.1 (Auth+Estudiantes+Instructores+Vehículos) | CRUDs funcionales, validaciones Ecuador, soft delete, JaCoCo ≥80% |
| 6 | Backend Grupo A pt.2 (Asignaciones+Cobros+Resilience4j) | Asignación tripartita, pagos parciales, circuit breakers, JaCoCo ≥80% |
| 7 | Frontend Grupo A completo | 7 vistas con CRUDs + calendario + wizard tripartita, vitest ≥80% |
| 8 | Testing Grupo A (unit+IT+E2E Cypress 5 flujos) | 30+ IT con Testcontainers, 5 flujos E2E pasan en CI |
| **9** | **Estabilización: CI/CD + pulido Grupo A + refactor dominio** | **5 PRs mergeados (#38-#42): kilometraje E2E, factura_cuotas, 6 validaciones, V6 bcrypt, TZ JVM, 404/400 ProblemDetail, 3 workflows nuevos** |

### Sprints PENDIENTES

| Sprint | Tema | Estado |
|--------|------|--------|
| **10** | **Backend Grupo B**: MS-Notificaciones (plantillas + in-app + log envíos) + MS-Reportes (operativos + financieros + PDF/Excel + cache) | EN PROCESO |
| 11 | Frontend Grupo B: NotificacionesDropdown + PlantillasEmailView + DashboardView KPIs + Reportes UI | PLANIFICADO |
| 12 | Testing Grupo B: unit ≥80% + IT Testcontainers + E2E Cypress 3 flujos | PLANIFICADO |
| 13 | Cierre: E2E cruzado + JMeter + OWASP + Rate limiting Gateway + Limpieza + Docs final + Deploy Oracle Cloud + Demo + tag v1.0.0 | PLANIFICADO |

---

## Credenciales de prueba

```
admin@escuela.local / Admin123!
```

(hash bcrypt cost 10 fijado en `V6__fix_admin_password_hash.sql` desde Sprint 9)
