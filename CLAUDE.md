# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> ## ⚠️ IMPORTANTE — Documentos de referencia (orden de prioridad)
>
> 1. **`DECISIONES.md`** — Fuente de verdad de todas las decisiones técnicas (cerradas 2026-05-06). Ante cualquier conflicto con este CLAUDE.md, **DECISIONES.md gana**.
> 2. **`SPRINTS_PLAN.xlsx`** — Plan detallado de los 12 sprints con tareas, subtareas, pasos y criterios de aceptación.
> 3. **`CLAUDE.md`** (este archivo) — Contexto general y guía operativa.
>
> ### Cambios clave respecto a este documento original:
> - **Sprints son de 1 semana** (no 2). Total: 12 sprints.
> - **Desarrollo HORIZONTAL** — todas las capas avanzan en TODOS los MS a la par (no microservicio por microservicio).
> - **8 microservicios** (incluye MS-Notificaciones formalizado).
> - **Cache:** Caffeine in-memory (no Redis).
> - **Mensajería:** RabbitMQ confirmado (no Kafka).
> - **BD:** 1 PostgreSQL con 9 schemas separados (no DB-per-service).
> - **JWT:** clave de 512 bits (HS512), HttpOnly cookies.
> - **Storage de archivos:** MinIO.
> - **Email:** Mailtrap (dev) / Gmail SMTP (prod).
> - **Modelo de negocio:** Single-tenant configurable (cada escuela = 1 deploy).
> - **Despliegue:** Oracle Cloud Free Tier (fallback DigitalOcean $6/mes).
> - **Tracking:** Jira.
> - **Commits:** formato `Sprint N (Tarea)` o `Sprint N (Fix tarea)`.
> - **Convenciones de código:** español respetando estándares del lenguaje (PascalCase clases, camelCase métodos en Java; snake_case solo en BD y env vars).
>
> Para detalles completos, consultar `DECISIONES.md`.

## Project Overview

**Sistema de Control Administrativo y Financiero para Escuelas de Conducción**

A comprehensive microservices-based web application for managing driving schools in Ecuador. Handles student enrollment, instructor management, vehicle fleet control, class scheduling, payments, and reporting.

- **Methodology**: Scrum with 2-week sprints
- **Timeline**: September 24, 2025 - May 5, 2026 (41 weeks)
- **Architecture**: Microservices with API Gateway
- **Deployment**: Docker + Kubernetes/Docker Compose

## Technology Stack

### Backend
- **Runtime**: Java 21
- **Framework**: Spring Boot 3.x
- **Microservices**: Spring Cloud (Gateway, Eureka, Config)
- **Database**: PostgreSQL (relational)
- **Messaging**: RabbitMQ or Kafka (async events)
- **Security**: Spring Security + JWT (24h expiration)
- **ORM**: Spring Data JPA + Hibernate
- **API**: REST/JSON

### Frontend
- **Framework**: Vue.js 3 (SPA)
- **Responsive**: Mobile-first design
- **Browsers**: Chrome 120+, Firefox 120+, Edge 120+, Safari 16+

### Infrastructure
- **Containerization**: Docker
- **Orchestration**: Kubernetes or Docker Compose
- **CI/CD**: GitHub Actions (per microservice)

## Architecture

### Microservices (7 core services)

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Vue.js 3)                   │
└────────────────────┬────────────────────────────────────┘
                     │ HTTPS/REST
┌────────────────────▼────────────────────────────────────┐
│              API Gateway (Spring Cloud)                  │
│  - Single entry point, routing, load balancing          │
│  - Rate limiting, token validation                       │
└────────────────────┬────────────────────────────────────┘
                     │
    ┌────────────────┼────────────────┐
    │                │                │
┌───▼──┐  ┌────────┬─┴──┐  ┌─────────┴──┐
│MS-   │  │MS-     │    │  │MS-         │
│Auth  │  │Estuad. │... │  │Reportes   │
└──────┘  └────────┘    │  └────────────┘
    │
┌───┴────────────────────────────────────┐
│  Shared Services                       │
│  - PostgreSQL Database                 │
│  - RabbitMQ/Kafka Message Broker      │
│  - Service Discovery (Eureka)          │
│  - Configuration Server                │
└────────────────────────────────────────┘
```

### 7 Microservices

1. **MS-Auth** (Authentication & Authorization)
   - User login/logout, JWT token generation
   - Role-based access control (Admin, Staff, Instructor, Student)
   - Account lockout after 3 failed attempts
   - Audit logging

2. **MS-Estudiantes** (Student Management)
   - Student enrollment, personal data, documentation
   - Academic progress tracking
   - Class attendance records

3. **MS-Instructores** (Instructor Management)
   - Instructor profiles, certifications, licenses
   - Schedule management, availability
   - Teaching hour tracking

4. **MS-Vehículos** (Vehicle Fleet Control)
   - Vehicle registration and documentation
   - Maintenance scheduling (preventive/corrective)
   - Fuel consumption tracking, mileage logging
   - SOAT and technical inspection alerts

5. **MS-Asignaciones** (Class Scheduling)
   - Tripartite assignment: instructor + student + vehicle
   - Automatic availability validation
   - Conflict detection and notifications
   - Class confirmation and changes

6. **MS-Cobros** (Financial Management)
   - Student payment registration
   - Invoice/receipt generation
   - Account reconciliation
   - Partial payment support

7. **MS-Reportes** (Reports & Analytics)
   - Operational reports (students, instructors, vehicles)
   - Financial reports (income, receivables, arrears)
   - KPI dashboard
   - PDF/Excel export

### Supporting Services

- **API Gateway**: Unified entry point, routing, load balancing
- **MS-Notificaciones**: Email notifications (async via RabbitMQ/Kafka)
- **Service Discovery (Eureka)**: Dynamic service registration
- **Config Server**: Centralized configuration management

## Directory Structure

```
proyecto-titulacion/
├── CLAUDE.md                          # This file
├── README.md                          # Project overview
├── docker-compose.yml                 # Local development setup
├── kubernetes/                        # K8s manifests (future)
│
├── frontend/                          # Vue.js 3 SPA
│   ├── src/
│   │   ├── components/               # Reusable UI components
│   │   ├── views/                    # Page-level components
│   │   ├── stores/                   # Pinia state management
│   │   ├── services/                 # API client services
│   │   └── router/                   # Vue Router configuration
│   ├── package.json
│   └── vite.config.js
│
├── api-gateway/                       # Spring Cloud Gateway
│   ├── src/main/java/gateway/
│   │   ├── config/                   # Gateway routes, filters
│   │   └── security/                 # JWT validation
│   └── application.yml
│
├── microservices/
│   ├── ms-auth/                      # Authentication service
│   │   └── src/main/java/auth/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       ├── dto/
│   │       └── security/
│   │
│   ├── ms-estudiantes/               # Students service
│   │   └── src/main/java/estudiantes/
│   │       └── (similar structure)
│   │
│   ├── ms-instructores/              # Instructors service
│   ├── ms-vehiculos/                 # Vehicle management
│   ├── ms-asignaciones/              # Class assignments
│   ├── ms-cobros/                    # Payments service
│   ├── ms-reportes/                  # Reports service
│   │
│   └── shared/                       # Shared libraries
│       ├── common-dtos/              # Shared DTOs
│       ├── common-exceptions/        # Custom exceptions
│       └── common-utils/             # Utilities
│
├── infrastructure/
│   ├── database/                     # SQL migrations
│   │   └── migrations/
│   ├── docker/                       # Docker images
│   └── scripts/                      # DevOps scripts
│
└── docs/                             # Documentation
    ├── architecture/                 # C4 diagrams, ER models
    ├── api/                          # OpenAPI/Swagger specs
    └── guides/                       # Development guides
```

## Development Setup

### Prerequisites
- Java 21 (JDK)
- Maven 3.8+
- Node.js 18+ (for frontend)
- Docker & Docker Compose
- Git
- PostgreSQL 14+ (or use Docker)

### Initial Setup

```bash
# Clone repository
git clone <repo-url>
cd proyecto-titulacion

# Start infrastructure (PostgreSQL, RabbitMQ)
docker-compose up -d

# Backend: Build all microservices
cd microservices
mvn clean install

# Frontend: Install dependencies
cd ../frontend
npm install
npm run dev
```

## Common Commands

### Backend (Microservices)

```bash
# Build specific microservice
cd microservices/ms-auth
mvn clean package -DskipTests

# Run microservice locally
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Run unit tests
mvn test

# Run integration tests
mvn verify -Dgroups=integration

# Run with Docker
docker build -t ms-auth:latest .
docker run -p 8081:8081 ms-auth:latest

# Check service logs
mvn spring-boot:run | tail -f

# Build all services at once
cd ..
mvn clean package -DskipTests
```

### Frontend

```bash
# Development server with HMR
npm run dev

# Build for production
npm run build

# Run unit tests
npm run test

# Run end-to-end tests
npm run test:e2e

# Linting and formatting
npm run lint
npm run format

# Type checking (TypeScript)
npm run type-check
```

### Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs for specific service
docker-compose logs -f ms-auth

# Stop all services
docker-compose down

# Rebuild images
docker-compose build --no-cache

# Prune unused images/networks
docker system prune -a
```

### Database

```bash
# Execute migrations
mvn flyway:migrate

# Create migration file
mvn flyway:undo

# Reset database (dev only)
mvn flyway:clean flyway:migrate
```

## API Documentation

### API Gateway Base URL
- **Local**: `http://localhost:8080`
- **Development**: `https://dev-api.proyecto.local`
- **Production**: `https://api.proyecto.local`

### Authentication
All endpoints (except login) require JWT token in header:
```
Authorization: Bearer <token>
```

### Key Endpoints

**Authentication**
```
POST /auth/login                    # User login
POST /auth/refresh                  # Refresh token
POST /auth/logout                   # Logout
POST /auth/forgot-password          # Request password reset
```

**Students**
```
POST /estudiantes                   # Enroll student
GET /estudiantes/{id}              # Get student details
PUT /estudiantes/{id}              # Update student
GET /estudiantes/{id}/progreso     # Get academic progress
```

**Instructors**
```
GET /instructores                  # List instructors
GET /instructores/{id}/disponibilidad  # Check availability
```

**Classes**
```
POST /asignaciones                 # Schedule class
GET /asignaciones/{id}             # Get assignment
PUT /asignaciones/{id}/reprogramar # Reschedule
```

**Payments**
```
POST /cobros                       # Register payment
GET /cobros/estudiante/{id}        # Get student account
```

**Reports**
```
GET /reportes/estudiantes?fechaInicio&fechaFin
GET /reportes/financiero?fechaInicio&fechaFin
POST /reportes/exportar            # Export as PDF/Excel
```

Specs OpenAPI accesibles vía SpringDoc en `http://localhost:<puerto>/v3/api-docs` de cada MS. Export a archivos YAML/JSON está planificado para Sprint 13 (T13.6 Docs final) en una nueva carpeta `docs/api/` que se creará entonces.

## Code Conventions

### Java Backend
- **Package naming**: `com.kynsoft.<servicio>.<layer>` (e.g., `com.kynsoft.auth.controller`)
- **Class naming**: PascalCase (e.g., `StudentService`, `StudentRepository`)
- **Method naming**: camelCase (e.g., `enrollStudent()`, `getStudentById()`)
- **Constants**: UPPER_SNAKE_CASE
- **Formatting**: Google Java Style (4-space indentation)

### Vue.js Frontend
- **Component naming**: PascalCase and kebab-case (e.g., `StudentForm.vue`, `<student-form />`)
- **File structure**: Features grouped by domain (e.g., `/src/features/students/`)
- **Naming**: camelCase for JS, kebab-case for HTML attributes
- **Formatting**: Prettier (2-space indentation, single quotes)

### Database
- **Table naming**: snake_case, plural (e.g., `estudiantes`, `instructores`)
- **Column naming**: snake_case (e.g., `id_estudiante`, `fecha_matricula`)
- **Primary keys**: `id` (auto-increment bigint)
- **Foreign keys**: `{table}_id` (e.g., `estudiante_id`)

## Testing Strategy

### Unit Tests
- **Location**: `src/test/java` (Java), `src/components/__tests__/` (Vue)
- **Framework**: JUnit 5, Mockito (Java), Vitest (Vue)
- **Minimum coverage**: 80% per module
- **Run**: `mvn test` or `npm run test`

### Integration Tests
- **Tag**: `@Tag("integration")`
- **Database**: Use H2 in-memory or testcontainers
- **Run**: `mvn verify -Dgroups=integration`

### End-to-End Tests
- **Framework**: Cypress or Playwright
- **Scenarios**: Critical user flows (enrollment, payment, scheduling)
- **Run**: `npm run test:e2e`

### Load Testing
- **Tool**: JMeter or Gatling
- **Target**: 50 concurrent users
- **Success criteria**: <500ms response time (p95)

## Security

### Authentication & Authorization
- **Method**: JWT (JSON Web Tokens)
- **Expiration**: 24 hours
- **Storage**: HTTP-only cookies (frontend)
- **Roles**: Admin, Personal Administrativo, Instructor, Estudiante
- **Endpoint protection**: Spring Security @PreAuthorize

### Data Protection
- **In transit**: HTTPS/TLS 1.2+ (mandatory)
- **At rest**: Database encryption at infrastructure level
- **Passwords**: bcrypt with salt
- **Account lockout**: 3 failed attempts → 15 minute lockout

### Input Validation
- **Format**: Ecuador-specific (10-digit cédula, ABC-1234 plates, etc.)
- **XSS prevention**: Vue.js auto-escaping + sanitization
- **SQL injection**: Parameterized queries via Hibernate
- **CSRF**: Spring Security CSRF tokens

### Audit & Compliance
- **Audit logging**: All operations logged with user, timestamp, IP, action
- **Non-repudiation**: Critical operations require additional confirmation
- **Backups**: Daily automated backups with 30-day retention
- **GDPR-lite**: Support for data export/deletion requests

## Performance Optimization

### Backend
- **Caching**: Redis for frequently accessed data (instructors, vehicles availability)
- **Connection pooling**: HikariCP (default in Spring Boot)
- **Database indexing**: Indexes on foreign keys and query filters
- **Pagination**: Limit 50 records default per API response
- **Lazy loading**: Avoid N+1 queries with proper JOIN strategies

### Frontend
- **Code splitting**: Route-based lazy loading with Vue Router
- **Image optimization**: WebP with PNG fallback, lazy loading
- **State management**: Pinia stores only for shared state
- **API caching**: Browser cache headers, SWR pattern

### Monitoring
- **Application metrics**: Spring Actuator, Prometheus
- **Log aggregation**: ELK stack or Cloudwatch
- **Alerting**: Email/Slack for critical issues
- **Performance tracking**: Response times, error rates, throughput

## Deployment

### Local Development
```bash
docker-compose up -d
# Frontend: http://localhost:5173
# API Gateway: http://localhost:8080
# Admin: http://localhost:8088 (optional)
```

### CI/CD Pipeline
- **VCS**: GitHub
- **CI**: GitHub Actions
- **Stages**: 
  1. Build & test (per microservice)
  2. SonarQube quality gate
  3. Docker image build & push
  4. Helm deployment to staging/prod
- **Approvals**: Manual gate before production

### Infrastructure (Kubernetes)
```bash
# Deploy to cluster
kubectl apply -f kubernetes/

# Scale microservice
kubectl scale deployment ms-auth --replicas=3

# View status
kubectl get pods -l app=proyecto-titulacion
```

## Project Management

### Scrum Ceremonies
- **Sprint Planning**: Every Monday, 2 hours (start of 2-week sprint)
- **Daily Standup**: 9:15 AM, 15 minutes
- **Sprint Review**: Every other Friday, 1 hour
- **Sprint Retrospective**: Every other Friday, 1 hour

### Tracking Tools
- **Jira**: Sprint board, backlog, burn-down charts
- **GitHub Projects**: Code-level tracking (optional)
- **Confluence**: Documentation and meeting notes

### Definition of Done
- [ ] Code peer-reviewed and merged to develop
- [ ] Unit tests written (80%+ coverage)
- [ ] Integration tests passing
- [ ] API documentation updated (Swagger)
- [ ] Frontend tested in Chrome, Firefox, Safari
- [ ] No security vulnerabilities (OWASP Top 10)
- [ ] Performance metrics met (<500ms p95)
- [ ] Database migrations applied and tested
- [ ] Code formatted and linted

## Important Constraints

### Technical Constraints
- Must use microservices architecture with Spring Cloud
- PostgreSQL is the only relational database
- Every microservice must be independently deployable (Docker)
- Circuit breaker pattern for inter-service communication
- Async messaging for event-driven features

### Scope Constraints
- **NO** native mobile apps (web responsive only)
- **NO** LMS with online exams or simulations
- **NO** GPS tracking or real-time vehicle location
- **NO** Machine Learning or predictive analytics
- **NO** Multi-language support (Spanish/Ecuador only)
- **NO** Integration with government ANT APIs (data export only)

### Capacity Constraints
- Supports up to 50 concurrent users
- Max 1,000 active students per school
- Max 50 instructors, 30 vehicles
- Daily backups with 30-day retention

## Useful References

- **Database**: Modelo completo en `docs/database/schema.md` (monolítico) + `docs/database/secciones/` (19 archivos por tema) + `docs/database/er-diagram.dbml` (para dbdiagram.io)
- **Decisions**: ADRs consolidados en `DECISIONES.md` (raíz). §23 ADR vertical por grupos, §24 ADR refactor dominio Sprint 9, §25 ADR estabilización CI/CD
- **Plan de sprints**: `PLAN_FASES.md` (13 sprints) + `SPRINTS_PLAN.xlsx` (tabular)
- **Migraciones BD**: `backend/<ms>/src/main/resources/db/migration/V*.sql`
- **Architecture (futuro)**: C4 diagrams + runbook + manual usuario pendientes para Sprint 13 (T13.6)
- **API Spec**: SpringDoc en `http://localhost:<puerto>/v3/api-docs` de cada MS. Export a `docs/api/` planificado para Sprint 13

## Troubleshooting

**Q: Microservice won't start (port already in use)**
```bash
# Find and kill process
lsof -i :8081  # Check which process
kill -9 <PID>

# Or use different port
export SERVER_PORT=8085
mvn spring-boot:run
```

**Q: RabbitMQ connection failed**
```bash
# Ensure RabbitMQ is running in Docker
docker-compose ps
docker-compose logs rabbitmq

# Restart if needed
docker-compose restart rabbitmq
```

**Q: Tests failing due to database**
```bash
# Use in-memory H2 for tests
# Spring Boot detects H2 on classpath and uses it for @SpringBootTest

# Or reset Postgres
mvn flyway:clean flyway:migrate -Dspring.profiles.active=test
```

**Q: CORS errors in frontend**
```bash
# Check API Gateway CORS configuration in api-gateway/application.yml
# Ensure frontend origin is whitelisted
```

## Contact & Support

- **Project Lead**: Víctor Javier Gómez Regalado
- **Students**: Raúl Sebastián Cruz Baño, Hernán Mateo Jurado Moran
- **University**: Universidad de las Américas (UDLA), Quito, Ecuador
- **Submission Deadline**: May 5, 2026
