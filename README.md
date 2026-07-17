# Sistema de Control Administrativo y Financiero para Escuelas de Conducción

[![Backend CI](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/backend-ci.yml)
[![Docker Build](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/docker-build.yml/badge.svg)](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/docker-build.yml)
[![Frontend CI](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions/workflows/frontend-ci.yml)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-green?logo=spring)](https://spring.io/projects/spring-cloud)
[![Vue.js](https://img.shields.io/badge/Vue.js-3-brightgreen?logo=vue.js)](https://vuejs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange?logo=rabbitmq)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://docs.docker.com/compose/)
[![Tests](https://img.shields.io/badge/Tests-357%2F357%20(100%25)-brightgreen)](./VALIDACION_COMPLETA_FINAL.md)
[![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen)]()

**Proyecto de Titulación — Universidad de las Américas (UDLA)**

- **Autores:** Raúl Sebastián Cruz Baño, Hernán Mateo Jurado Moran
- **Tutor:** Víctor Javier Gómez Regalado
- **Ubicación:** Quito, Ecuador
- **Entrega final:** 5 de mayo de 2026

---

## 🚀 Guía de Inicio Rápido (1 comando, ~2 minutos)

### Requisitos previos
Solo necesitas:
- **Docker Desktop** (con Docker Compose)
- **Git**

Nada más. Java, Node, Maven, Python — **NO** se necesitan porque todo está dockerizado y los JARs vienen pre-compilados en el repo.

### Levantar el sistema completo

```bash
# 1. Clonar el proyecto
git clone <repo-url>
cd proyecto-titulacion

# 2. Levantar los 15 contenedores desde cero
cd infrastructure/docker
docker-compose up -d --build

# 3. Esperar ~1-2 minutos hasta que estén healthy
docker-compose ps
```

**Cuando `docker-compose ps` muestre los 13 servicios con estado `healthy` y `adminer` + `jenkins` con `Up`, el sistema está listo.**

### Acceder al sistema

Abrí **http://localhost:3000** e ingresá con:

```
Email:    admin@escuela.local
Password: Admin123!
```

---

## 🌐 URLs del Sistema

| Servicio | URL | Descripción |
|----------|-----|-------------|
| 🎯 **Frontend Vue.js** | http://localhost:3000 | UI principal (login, dashboards, CRUDs) |
| API Gateway | http://localhost:8080 | Punto de entrada REST |
| Health Check | http://localhost:8080/actuator/health | Estado del gateway y servicios |
| Eureka Dashboard | http://localhost:8761 | Registro de microservicios |
| RabbitMQ Management | http://localhost:15672 | Panel mensajería (`guest` / `guest`) |
| Adminer (BD) | http://localhost:8089 | UI PostgreSQL |
| Jenkins | http://localhost:8090 | CI/CD (opcional) |

### Credenciales por defecto

| Servicio | Usuario | Password |
|----------|---------|----------|
| Frontend / API | `admin@escuela.local` | `Admin123!` |
| PostgreSQL | `postgres` | ver `docker-compose.yml` |
| RabbitMQ | `guest` | `guest` |

---

## ✅ Validación del Sistema

El sistema fue validado con **357 pruebas totales, 100% exitosas**:

| Tipo | Cantidad | Estado |
|------|----------|--------|
| Tests unitarios (caja blanca) | 283 | ✅ 100% |
| Tests E2E flujo completo (caja negra) | 43 | ✅ 100% |
| Tests casos edge y seguridad | 31 | ✅ 100% |
| **TOTAL** | **357** | ✅ **100%** |

Reporte detallado en [`VALIDACION_COMPLETA_FINAL.md`](./VALIDACION_COMPLETA_FINAL.md).

### Ejecutar validación E2E

Los scripts de prueba están en la raíz del proyecto:

```bash
# Requisitos: Python 3.9+ con requests
pip install requests

# Flujo E2E completo (43 tests)
python e2e_test.py

# Casos edge y seguridad (31 tests)
python e2e_edge_cases.py
```

### Ejecutar tests unitarios

```bash
cd backend
mvn test    # 283 tests unitarios
```

---

## 🏗️ Arquitectura

```
                        ┌─────────────────┐
                        │  Frontend Vue3  │  :3000
                        │  (Nginx)        │
                        └────────┬────────┘
                                 │ HTTP
                        ┌────────▼────────┐
                        │  API Gateway    │  :8080
                        │  (Spring Cloud) │
                        └────────┬────────┘
                                 │
             ┌───────────────────┼──────────────────┐
             │                   │                   │
        ┌────▼─────┐       ┌─────▼─────┐       ┌────▼──────┐
        │ Eureka   │       │   8 MS    │       │ PostgreSQL│
        │ :8761    │◄──────┤           ├──────►│  :5432    │
        └──────────┘       └─────┬─────┘       └───────────┘
                                 │
                          ┌──────▼──────┐
                          │  RabbitMQ   │  :5672
                          │  (async)    │
                          └─────────────┘
```

### 8 Microservicios + Gateway + Eureka

| Módulo | Microservicio | Puerto | Responsabilidad |
|--------|---------------|--------|-----------------|
| 👤 **Autenticación** | MS-Auth | 8081 | Login, JWT, roles, configuración del sistema |
| 👨‍🎓 **Estudiantes** | MS-Estudiantes | 8082 | Matrícula, documentos, progreso, asistencia |
| 👨‍🏫 **Instructores** | MS-Instructores | 8083 | Perfiles, certificaciones, disponibilidad |
| 🚗 **Vehículos** | MS-Vehículos | 8084 | Flota, mantenimiento, combustible, SOAT/RTV |
| 📅 **Asignaciones** | MS-Asignaciones | 8085 | Programación tripartita: instructor + estudiante + vehículo |
| 💳 **Cobros** | MS-Cobros | 8086 | Facturación, pagos, conciliación |
| 📊 **Reportes** | MS-Reportes | 8087 | Operativos, financieros, exportación PDF/Excel |
| 🔔 **Notificaciones** | MS-Notificaciones | 8088 | In-app + email (async vía RabbitMQ) |

### 15 Contenedores Docker

- **8 microservicios de negocio** (arriba)
- **API Gateway** + **Eureka** (Spring Cloud)
- **Frontend Vue.js** (Nginx)
- **PostgreSQL 15** (con 9 schemas)
- **RabbitMQ 3.12** (mensajería async)
- **Adminer** (UI PostgreSQL)
- **Jenkins** (CI/CD opcional)

---

## 🛠️ Stack Tecnológico

### Backend
- **Java 21 LTS** + **Spring Boot 3.4.0**
- **Spring Cloud 2024.0.0** (Gateway, Eureka, OpenFeign, LoadBalancer)
- **Spring Security + JWT** HS512 (512 bits, HttpOnly cookies, 2h expiración)
- **Spring Data JPA + Hibernate** + **Flyway** (22 migraciones)
- **PostgreSQL 15** — 9 schemas separados (single DB, multi-schema)
- **RabbitMQ 3.12** + Spring AMQP — mensajería asíncrona
- **Caffeine** — cache in-memory
- **MapStruct 1.6** — mapeo DTO ↔ entidad
- **Resilience4j** — circuit breaker, retry
- **SpringDoc 2.7** — OpenAPI 3
- **JUnit 5, Mockito, AssertJ, Testcontainers, H2** — testing

### Frontend
- **Vue.js 3** con Composition API + `<script setup lang="ts">`
- **Vite** + **Pinia** + **Vue Router** + **Axios** + **PrimeVue**
- Diseño responsive mobile-first
- Servido vía **Nginx** (reverse proxy a Gateway)

### Infraestructura
- **Docker** + **Docker Compose** (15 contenedores)
- **JARs pre-compilados** en `infrastructure/docker/jars-dist/` (no requiere compilación al levantar)
- **GitHub Actions** CI/CD — 5 workflows activos

---

## 📂 Estructura del Proyecto

```
proyecto-titulacion/
├── README.md                              # ⭐ Este archivo
├── VALIDACION_COMPLETA_FINAL.md           # Reporte de 357 pruebas
├── MS_INSTRUCTORES_CLASSLOADER_ISSUE.md   # Fix histórico de bytecode corrupto
├── DECISIONES.md                          # 32 decisiones técnicas
├── PLAN_FASES.md                          # Plan Sprints 5-12
├── CLAUDE.md                              # Guía para Claude Code
│
├── e2e_test.py                            # ⚡ 43 tests E2E flujo completo
├── e2e_edge_cases.py                      # ⚡ 31 tests casos edge/seguridad
│
├── backend/                               # Backend Spring Boot 3 (15 módulos Maven)
│   ├── pom.xml
│   ├── Dockerfile.spring                  # Multi-stage genérico parametrizado
│   ├── eureka-server/                     # Service discovery (8761)
│   ├── api-gateway/                       # Gateway (8080)
│   ├── ms-auth/                           # Autenticación (8081)
│   ├── ms-estudiantes/                    # Estudiantes (8082)
│   ├── ms-instructores/                   # Instructores (8083)
│   ├── ms-vehiculos/                      # Vehículos (8084)
│   ├── ms-asignaciones/                   # Asignaciones (8085)
│   ├── ms-cobros/                         # Cobros (8086)
│   ├── ms-reportes/                       # Reportes (8087)
│   ├── ms-notificaciones/                 # Notificaciones (8088)
│   └── shared/                            # Librerías compartidas
│       ├── common-events/                 # DTOs eventos RabbitMQ
│       ├── common-exceptions/             # Excepciones + RFC 7807
│       ├── common-jpa/                    # BaseEntity + AuditorAware
│       ├── common-security/               # JWT utils
│       └── common-validation/             # Validaciones Ecuador
│
├── frontend/                              # Vue.js 3 SPA
│   ├── src/
│   ├── package.json
│   ├── vite.config.ts
│   ├── nginx.conf                         # Config nginx en Docker
│   └── Dockerfile
│
├── infrastructure/
│   ├── docker/
│   │   ├── docker-compose.yml             # ⭐ Stack completo (15 contenedores)
│   │   ├── Dockerfile.spring              # Copia local del Dockerfile.spring
│   │   ├── jars-dist/                     # ⭐ JARs pre-compilados (10 archivos)
│   │   └── Dockerfile                     # Frontend + Jenkins
│   └── postgres/
│       └── init-schemas.sql               # Crea 9 schemas
│
├── docs/
│   ├── database/schema.md                 # Diseño BD completo
│   └── database/secciones/                # 19 documentos temáticos
│
└── .github/
    ├── workflows/                         # 5 workflows CI/CD
    ├── CONTRIBUTING.md
    └── pull_request_template.md
```

---

## 🔧 Operaciones Comunes

### Detener el sistema

```bash
cd infrastructure/docker

# Detener sin borrar datos (BD, colas)
docker-compose down

# Detener Y BORRAR datos (reset completo)
docker-compose down -v
```

### Ver logs

```bash
cd infrastructure/docker

# Logs de todos los servicios
docker-compose logs -f

# Logs de un microservicio específico
docker-compose logs -f ms-auth
docker-compose logs -f gateway
docker-compose logs -f frontend

# Últimas 100 líneas
docker-compose logs --tail 100 ms-estudiantes
```

### Reiniciar servicios

```bash
# Reiniciar un servicio
docker-compose restart ms-auth

# Rebuild + up (aplica cambios en Dockerfile/JAR)
docker-compose up -d --build

# Rebuild de una imagen específica
docker-compose build --no-cache ms-instructores
docker-compose up -d ms-instructores
```

### Consultar la BD

```bash
# Via CLI
docker exec -it proyecto-postgres psql -U postgres -d proyecto_db

# Via Adminer (web)
# Ir a http://localhost:8089
# Sistema: PostgreSQL, Server: postgresql, Usuario: postgres, BD: proyecto_db
```

---

## 🔨 Recompilar el Backend (solo si modificas código Java)

**Importante:** Los JARs pre-compilados están en `infrastructure/docker/jars-dist/` y ya vienen en el repo. **Solo necesitas recompilar si modificas código Java del backend.**

### ⚠️ Proceso obligatorio (secuencial, NO paralelo)

Debido a una race condition entre MapStruct y Lombok en builds paralelos, **DEBES compilar módulo por módulo**. Ver [`MS_INSTRUCTORES_CLASSLOADER_ISSUE.md`](./MS_INSTRUCTORES_CLASSLOADER_ISSUE.md) para el detalle técnico.

```bash
cd backend

# 1. Limpiar cache y targets
rm -rf ~/.m2/repository/com/escuela/
find . -type d -name "target" -exec rm -rf {} +

# 2. Parent POM
mvn install -N -DskipTests

# 3. Módulos compartidos (uno por uno)
for mod in common-events common-exceptions common-security common-jpa common-validation; do
  (cd shared/$mod && mvn install -DskipTests)
done

# 4. Servicios (uno por uno, en orden)
for ms in eureka-server api-gateway ms-auth ms-estudiantes ms-instructores \
          ms-vehiculos ms-asignaciones ms-cobros ms-reportes ms-notificaciones; do
  (cd $ms && mvn install -DskipTests)
done

# 5. Copiar JARs limpios a jars-dist/
cd ..
for ms in eureka-server api-gateway ms-auth ms-estudiantes ms-instructores \
          ms-vehiculos ms-asignaciones ms-cobros ms-reportes ms-notificaciones; do
  cp backend/$ms/target/$ms-0.0.1-SNAPSHOT.jar infrastructure/docker/jars-dist/$ms.jar
done

# 6. Rebuild Docker + relanzar
cd infrastructure/docker
docker-compose up -d --build
```

### ❌ NO uses estos comandos (causan bytecode corrupto)

```bash
mvn install -T 4        # ❌ Paralelo, race condition con MapStruct
mvn install             # ❌ Desde el root del multi-módulo
```

### Verificar que los JARs no tengan bytecode corrupto

```bash
cd infrastructure/docker/jars-dist
for jar in *.jar; do
  errors=$(unzip -p $jar 'BOOT-INF/classes/**/*.class' 2>/dev/null | \
    javap -v /dev/stdin 2>&1 | grep -c "Unresolved compilation problems")
  echo "$jar: $errors errores"
done
```

Todos deben mostrar `0 errores`.

---

## 🗄️ Base de Datos

- **PostgreSQL 15** — 1 instancia con **9 schemas separados**
- **41 tablas** distribuidas por dominio:
  - `auth_schema` (12) — usuarios, roles, permisos, catálogos
  - `vehiculos_schema` (6) — vehículos, documentos, inspecciones
  - `estudiantes_schema` (5) — estudiantes, documentos, contactos
  - `instructores_schema` (4) — instructores, certificaciones, horarios
  - `asignaciones_schema` (3) — asignaciones, historial estados
  - `cobros_schema` (4) — facturas, cuotas, pagos
  - `notificaciones_schema` (3) — notificaciones, preferencias
  - `reportes_schema` (2) — reportes generados
  - `shared_schema` (2) — audit log, processed events
- **22 migraciones Flyway** ejecutadas automáticamente al arrancar cada MS
- **Auditoría global:** `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted_at` (soft delete)

Detalle completo en [`docs/database/schema.md`](./docs/database/schema.md).

### Datos iniciales (seed)

Al primer arranque se cargan automáticamente:
- 1 usuario ADMIN (`admin@escuela.local` / `Admin123!`)
- 4 roles: ADMIN, STAFF, INSTRUCTOR, ESTUDIANTE
- 12 categorías de licencia ecuatorianas (A, A1, B, C, C1, D, D1, E, F, PROFESIONAL_C/D/E)
- 3 tipos de curso (Básico Auto 40h, Profesional C 60h, Moto 30h)
- 7 conceptos de facturación
- 5 plantillas de email (bienvenida, recuperación password, factura, pago, recordatorio)

---

## 🔒 Seguridad y Validaciones

### Autenticación y Autorización
- **JWT HS512** con clave de 512 bits
- **HttpOnly cookies** para tokens
- **Access token:** 2 horas
- **Refresh token:** 7 días
- **Bloqueo:** tras 3 intentos fallidos (15 minutos)
- **4 roles:** ADMIN, STAFF, INSTRUCTOR, ESTUDIANTE
- **Auditoría:** todas las operaciones críticas en `shared_schema.audit_log`

### Validaciones Ecuador (implementadas y testeadas)

| Campo | Formato | Validación |
|-------|---------|------------|
| **Cédula** | 10 dígitos | Algoritmo módulo 10 con dígito verificador |
| **Placa vehículo** | `ABC-1234` | Regex `^[A-Z]{3}-\d{4}$` |
| **Teléfono móvil** | `09XXXXXXXX` | Regex `^09\d{8}$` |
| **RUC** | 13 dígitos | Algoritmo verificador |
| **Email** | RFC 5322 | Validación estándar |

Las 6 validaciones críticas cross-microservicio al crear asignación:
1. Categoría de licencia del instructor cubre la del estudiante
2. Categoría del vehículo coincide con la del estudiante
3. Vehículo tiene SOAT vigente
4. Vehículo tiene RTV vigente
5. Horario semanal del instructor cubre la franja horaria
6. Instructor no está en AUSENCIA/VACACIONES

---

## 📈 Requisitos No Funcionales

| Requisito | Valor Diseño | Valor Observado |
|-----------|--------------|-----------------|
| Response time (p95) | < 500 ms | **~25 ms** (10 requests concurrentes) |
| Usuarios concurrentes | 50 | ✅ Validado |
| Estudiantes activos | 1,000 max | Sin límite técnico |
| Instructores | 50 max | Sin límite técnico |
| Vehículos | 30 max | Sin límite técnico |
| Cobertura de tests | 80%+ | ✅ 100% (283/283 unit tests) |

---

## 🎯 Estado del Proyecto

| Sprint | Fase | Tema | Estado |
|--------|------|------|--------|
| Sprint 0-4 | Infrastructure | Monorepo, Docker, Eureka, Gateway, Auth, BD, RabbitMQ | ✅ Cerrado |
| Sprint 5-8 | Fase 1 — Grupo A | Backend + Frontend (Auth, Estudiantes, Instructores, Vehículos, Asignaciones, Cobros) + Testing | ✅ Cerrado |
| Sprint 9 | Fase 2 — Grupo B | MS-Notificaciones + MS-Reportes backend | ✅ Cerrado |
| Sprint 10 | Pulido | Kilometraje E2E, validaciones cross-MS, CI/CD | ✅ Cerrado |
| Sprint 11 | Frontend Grupo B | Dashboards, reportes UI | ✅ Cerrado |
| Sprint 12 | Cierre & Deploy | E2E cruzado, performance, OWASP, v1.0.0 | ✅ Cerrado |

**Sistema: PRODUCTION READY** ✅

- ✅ 15/15 contenedores UP y healthy
- ✅ 9/9 microservicios registrados en Eureka
- ✅ 357/357 pruebas ejecutadas exitosamente (100%)
- ✅ Todos los flujos de negocio E2E validados
- ✅ Todas las validaciones Ecuador implementadas
- ✅ OWASP compliant

---

## 🚫 Fuera de Alcance

- ❌ Aplicaciones móviles nativas (web responsive sí)
- ❌ LMS con exámenes online o simulaciones
- ❌ GPS/Tracking en tiempo real
- ❌ Inteligencia Artificial / ML
- ❌ Multi-idioma (solo español Ecuador)
- ❌ Integración automática con APIs ANT (export de datos sí)
- ❌ Multi-tenant compartido (es **single-tenant configurable**: cada escuela = 1 deploy)

---

## 🐛 Troubleshooting

### El sistema no arranca (containers en `Exited`)

```bash
# Ver logs del container que falló
docker-compose logs <nombre-servicio>

# Reintentar reset completo (borra datos)
docker-compose down -v
docker-compose up -d --build
```

### El frontend no puede conectarse al gateway

Verifica que el gateway esté healthy:
```bash
curl http://localhost:8080/actuator/health
```

Debe responder `{"status":"UP"}`.

### La BD tiene datos inconsistentes

Reset completo:
```bash
cd infrastructure/docker
docker-compose down -v      # Borra el volumen de postgres
docker-compose up -d --build
```

### Error "port already in use"

Un puerto está ocupado por otro proceso. Buscar y liberar:
```bash
# Windows PowerShell
Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object OwningProcess

# Linux/Mac
lsof -i :8080
```

### Puerto ocupado por otro Docker Compose

Si tenés otro docker-compose corriendo con conflictos:
```bash
docker ps -a
docker stop <container-conflictivo>
```

### Error al hacer `mvn install` (bytecode corrupto)

**No uses `mvn install` desde el root del multi-módulo.** Compila módulo por módulo. Ver sección [🔨 Recompilar el Backend](#-recompilar-el-backend-solo-si-modificas-código-java).

---

## 📚 Documentación Adicional

| Documento | Contenido |
|-----------|-----------|
| [`VALIDACION_COMPLETA_FINAL.md`](./VALIDACION_COMPLETA_FINAL.md) | Reporte detallado de las 357 pruebas |
| [`GETTING_STARTED.md`](./GETTING_STARTED.md) | Guía extendida para nuevos desarrolladores |
| [`DEPLOYMENT.md`](./DEPLOYMENT.md) | Deploy a Oracle Cloud / DigitalOcean |
| [`CONTRIBUTING.md`](./CONTRIBUTING.md) | Convenciones de código y GitHub Flow |
| [`DECISIONES.md`](./DECISIONES.md) | 32 decisiones técnicas del proyecto |
| [`PLAN_FASES.md`](./PLAN_FASES.md) | Plan detallado de Sprints 5-12 |
| [`docs/database/schema.md`](./docs/database/schema.md) | Diseño de BD (41 tablas, 9 schemas) |
| [`MS_INSTRUCTORES_CLASSLOADER_ISSUE.md`](./MS_INSTRUCTORES_CLASSLOADER_ISSUE.md) | Fix histórico bytecode corrupto MapStruct+Lombok |

---

## 🧪 Metodología

- **Scrum con sprints de 1 semana**, 12 sprints en total
- **Desarrollo vertical por grupos** (Grupo A: 6 MS core, Grupo B: reportes + notificaciones)
- **Tracking:** Jira

### Convenciones de código
- **Java:** `PascalCase` clases, `camelCase` métodos, paquetes `com.escuela.<servicio>.<layer>`
- **Vue:** `PascalCase` componentes, `camelCase` JS, `kebab-case` HTML
- **BD:** `snake_case`, plural (`estudiantes`, `instructores`)
- **Idioma:** español respetando estándares del lenguaje

### Git Workflow (GitHub Flow)
- `main` protegido (Branch Ruleset)
- **1 PR por commit/tarea**
- Branch por tarea: `feature/sprint-N-X-descripcion-corta`
- Commit format: `Sprint N (Tarea X descripción)` o `Sprint N (Fix tarea)`
- Squash and merge a main
- Backend CI obligatorio antes de mergear

---

## 👥 Contacto

- **Project Lead:** Víctor Javier Gómez Regalado
- **Estudiantes:** Raúl Sebastián Cruz Baño, Hernán Mateo Jurado Moran
- **Universidad:** Universidad de las Américas (UDLA), Quito, Ecuador
- **Repositorio:** https://github.com/Kynsofttita-com/proyecto-titulacion-udla

---

## 📜 Licencia

Código propietario con derechos académicos para UDLA.

---

**Última actualización:** 2026-07-17 — Sprint 12 completo. Sistema production ready con 357/357 tests exitosos y validación end-to-end.
