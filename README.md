# Sistema de Control Administrativo y Financiero para Escuelas de Conducción

**Proyecto de Titulación - Universidad de las Américas (UDLA)**

**Autores**: Raúl Sebastián Cruz Baño, Hernán Mateo Jurado Moran  
**Tutor**: Víctor Javier Gómez Regalado  
**Fecha**: 21 de enero de 2026  
**Ubicación**: Quito, Ecuador

---

## Descripción del Proyecto

Sistema integral web responsive para automatizar la administración operativa y financiera de escuelas de conducción en Ecuador. Implementado con arquitectura de microservicios, utilizando Java Spring Boot para el backend, Vue.js 3 para el frontend, y PostgreSQL para persistencia de datos.

### Problema Identificado

Las escuelas de conducción en Ecuador operan con:
- ❌ Procesos manuales y fragmentados
- ❌ Múltiples herramientas desintegradas (Excel, papeles)
- ❌ Falta de visión consolidada del estado operativo
- ❌ Deficiencias en control administrativo y financiero

**Impacto**: ~2,815-5,630 personas en el sector afectadas (563 escuelas × 5-10 administrativos)

### Solución Propuesta

✅ Plataforma web responsive unificada  
✅ Arquitectura de microservicios escalable  
✅ Integración completa de procesos  
✅ Dashboard con KPIs y reportes  
✅ Automatización de notificaciones  

---

## Características Principales

### 7 Módulos Funcionales

| Módulo | Descripción | Microservicio |
|--------|-------------|----------------|
| 👤 **Autenticación** | Login seguro, gestión de roles, auditoría | MS-Auth |
| 👨‍🎓 **Estudiantes** | Matrícula, seguimiento académico, documentación | MS-Estudiantes |
| 👨‍🏫 **Instructores** | Perfiles, certificaciones, disponibilidad, carga horaria | MS-Instructores |
| 🚗 **Vehículos** | Flota, mantenimiento, combustible, alertas | MS-Vehículos |
| 📅 **Asignaciones** | Programación de clases (estudiante + instructor + vehículo) | MS-Asignaciones |
| 💳 **Cobros** | Pagos, comprobantes, cuentas por cobrar, conciliación | MS-Cobros |
| 📊 **Reportes** | Operativos, financieros, KPIs, exportación PDF/Excel | MS-Reportes |
| 🔔 **Notificaciones** | Email transaccionales (async) | MS-Notificaciones |

---

## Stack Tecnológico

### Backend
```
Java 21 + Spring Boot 3.x
├── Spring Cloud (Gateway, Eureka, Config)
├── Spring Security + JWT (24h)
├── Spring Data JPA + Hibernate
├── PostgreSQL
├── RabbitMQ/Kafka (mensajería asíncrona)
└── Docker + Kubernetes/Compose
```

### Frontend
```
Vue.js 3 (SPA Responsive)
├── Vite (bundler)
├── Pinia (state management)
├── Axios (HTTP client)
├── Responsive Design (mobile-first)
└── ES2022+ + TypeScript
```

### Infrastructure
```
Docker Compose (local)
PostgreSQL 14+ (relacional)
RabbitMQ (message broker)
GitHub Actions (CI/CD)
```

---

## Instalación y Configuración

### Requisitos Previos
- ✅ Java 21 JDK
- ✅ Maven 3.8+
- ✅ Node.js 18+ + npm
- ✅ Docker & Docker Compose
- ✅ Git
- ✅ PostgreSQL 14+ (o usar Docker)

### Inicio Rápido

```bash
# 1. Clonar repositorio
git clone <repo-url>
cd proyecto-titulacion

# 2. Levantar infraestructura (PostgreSQL, RabbitMQ)
docker-compose up -d

# 3. Backend: Build microservicios
cd microservices
mvn clean install

# 4. Frontend: Instalar dependencias
cd ../frontend
npm install

# 5. Ejecutar
# Backend: mvn spring-boot:run (en cada microservicio)
# Frontend: npm run dev

# URLs:
# 🌐 Frontend: http://localhost:5173
# 🔌 API: http://localhost:8080
```

### Configuración de Base de Datos

```bash
# Las migraciones se ejecutan automáticamente al iniciar los servicios
# Para reset manual (desarrollo):
mvn flyway:clean flyway:migrate
```

---

## Estructura del Proyecto

```
proyecto-titulacion/
├── CLAUDE.md                    # Guía para Claude Code ⭐
├── README.md                    # Este archivo
├── docker-compose.yml           # Compose para desarrollo local
│
├── frontend/                    # Vue.js 3 SPA
│   ├── src/
│   │   ├── components/         # Componentes reutilizables
│   │   ├── views/              # Páginas
│   │   ├── stores/             # Estado global (Pinia)
│   │   ├── services/           # Clientes API
│   │   └── router/             # Rutas
│   └── package.json
│
├── microservices/              # Backend (Java/Spring)
│   ├── api-gateway/            # Gateway (8080)
│   ├── ms-auth/                # Autenticación (8081)
│   ├── ms-estudiantes/         # Estudiantes (8082)
│   ├── ms-instructores/        # Instructores (8083)
│   ├── ms-vehiculos/           # Vehículos (8084)
│   ├── ms-asignaciones/        # Asignaciones (8085)
│   ├── ms-cobros/              # Cobros (8086)
│   ├── ms-reportes/            # Reportes (8087)
│   └── shared/                 # Librerías compartidas
│
├── infrastructure/
│   ├── database/               # Migraciones SQL
│   ├── docker/                 # Dockerfiles
│   └── scripts/                # Scripts DevOps
│
└── docs/                       # Documentación
    ├── architecture/           # Diagramas C4
    ├── api/                    # OpenAPI/Swagger
    └── guides/                 # Manuales de desarrollo
```

---

## Comandos Útiles

### Backend

```bash
# Build servicio específico
cd microservices/ms-auth
mvn clean package -DskipTests

# Ejecutar localmente
mvn spring-boot:run

# Tests unitarios
mvn test

# Tests de integración
mvn verify -Dgroups=integration

# Build Docker
docker build -t ms-auth:latest .
```

### Frontend

```bash
# Servidor de desarrollo (HMR)
npm run dev

# Build producción
npm run build

# Tests unitarios
npm run test

# Tests E2E
npm run test:e2e

# Linting
npm run lint
npm run format
```

### Docker Compose

```bash
# Iniciar todo
docker-compose up -d

# Ver logs
docker-compose logs -f ms-auth

# Detener
docker-compose down

# Rebuild
docker-compose build --no-cache
```

---

## API Documentation

### Base URLs
- **Local**: `http://localhost:8080`
- **Desarrollo**: `https://dev-api.proyecto.local`

### Autenticación
```
POST /auth/login
{
  "username": "user@escuela.com",
  "password": "password123"
}

Respuesta:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400,
  "role": "ROLE_ADMIN"
}
```

### Endpoints Principales

```
POST   /estudiantes              → Matricular estudiante
GET    /estudiantes/{id}         → Obtener datos estudiante
GET    /estudiantes/{id}/progreso → Progreso académico

POST   /asignaciones             → Programar clase
GET    /asignaciones/{id}        → Detalles asignación

POST   /cobros                   → Registrar pago
GET    /cobros/estudiante/{id}   → Estado de cuenta

GET    /reportes/estudiantes     → Reporte de estudiantes
GET    /reportes/financiero      → Reporte financiero
POST   /reportes/exportar        → Exportar PDF/Excel
```

**[Ver especificación completa: /docs/api/openapi.yaml]**

---

## Metodología y Planificación

### Scrum con Sprints de 2 Semanas

**Timeline**: 24 sep 2025 - 5 may 2026 (41 semanas)

| Fase | Duración | Objetivo |
|------|----------|----------|
| 📋 Planificación | 6 sem | Factibilidad y alcance |
| 🔍 Análisis | 6 sem | Requisitos detallados |
| 🎨 Diseño | 3 sem | Arquitectura y prototipos |
| 💻 Desarrollo | 12 sem | Implementación (7 microservicios) |
| ✅ Pruebas | 4 sem | QA e integración |

### Ceremonias
- 📌 **Sprint Planning**: Lunes 2h
- 🔄 **Daily Standup**: Diario 15min
- 👀 **Sprint Review**: Viernes 1h
- 🎯 **Retrospective**: Viernes 1h

### Herramientas
- **Jira**: Backlog, sprints, burn-down
- **GitHub**: Versionado, PRs
- **Confluence**: Documentación

---

## Requisitos No Funcionales

### Rendimiento
- ⚡ Respuesta <500ms (p95)
- 📦 Capacidad: 50 usuarios concurrentes
- 🔄 Throughput: 100 req/s

### Disponibilidad
- 🟢 SLA: 99.9%
- 💾 Backups diarios (retención 30 días)
- 🔁 Disaster recovery plan

### Seguridad
- 🔒 HTTPS obligatorio (TLS 1.2+)
- 🔑 JWT con expiración 24h
- 🔐 Contraseñas bcrypt
- 🚫 Bloqueo tras 3 intentos fallidos (15 min)
- 📋 Auditoría completa (usuario, timestamp, IP, acción)

### Escalabilidad
- 📈 Arquitectura de microservicios
- 🐳 Containerización Docker
- ☸️ Orquestación Kubernetes-ready

---

## Limitaciones del Proyecto (Fuera de Alcance)

❌ Aplicaciones móviles nativas  
❌ LMS con exámenes online  
❌ GPS/Seguimiento en tiempo real  
❌ Inteligencia Artificial  
❌ Multi-idioma  
❌ Integración automática ANT  
❌ Chat o comunicación real-time  

---

## Validación Ecuatoriana

El sistema valida datos en formato ecuatoriano:
- 🆔 Cédula: 10 dígitos (con dígito verificador)
- 🚗 Placas: ABC-1234 o AAA-1234
- 📱 Teléfonos: 10 dígitos
- 📅 Fechas: DD/MM/AAAA
- 💵 Montos: USD con 2 decimales

---

## Testing

### Cobertura Mínima: 80%

```bash
# Unit tests
mvn test

# Integration tests
mvn verify -Dgroups=integration

# Frontend tests
npm run test

# E2E tests
npm run test:e2e
```

---

## Deployment

### Local
```bash
docker-compose up -d
# 🌐 http://localhost:5173 (frontend)
# 🔌 http://localhost:8080 (API)
```

### Production (Kubernetes)
```bash
kubectl apply -f kubernetes/
kubectl scale deployment ms-auth --replicas=3
```

---

## Contribuciones y Código

### Style Guide
- **Java**: Google Java Style (CLAUDE.md)
- **JavaScript/Vue**: Prettier + ESLint
- **SQL**: snake_case, plural names
- **Commits**: Conventional Commits

### Pull Request Workflow
1. ✨ Crear rama: `feature/MS-001-nombre`
2. 💻 Implementar + tests
3. 📝 Actualizar documentación
4. 🔍 Code review (1 aprobación mínimo)
5. ✅ Merge a `develop`

---

## Monitoreo y Observabilidad

### Metrics
- Prometheus + Grafana
- Application metrics via Spring Actuator
- Dashboard de KPIs

### Logging
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Structured logging con SLF4J
- Trazabilidad de transacciones

### Alerting
- Email/Slack para incidentes críticos
- Umbrales: CPU >80%, memoria >85%, errores >1%

---

## Costos del Proyecto

**Presupuesto total estimado**: $22,370.00 USD

| Fase | Costo | % |
|------|-------|---|
| 💻 Desarrollo | $8,400 | 37.55% |
| 📋 Planificación | $4,090 | 18.28% |
| 🔍 Análisis | $3,600 | 16.09% |
| 👨‍💼 Administración | $2,880 | 12.88% |
| 🎨 Diseño | $1,800 | 8.05% |
| ✅ Pruebas | $1,600 | 7.15% |

**Nota**: Considera únicamente recursos humanos. Infraestructura (cloud, dominios, certificados) no incluida.

---

## Documentación Adicional

- 📘 [CLAUDE.md](./CLAUDE.md) - Guía completa para Claude Code
- 🏗️ [Arquitectura C4](./docs/architecture/) - Diagramas de contexto y componentes
- 🔌 [API OpenAPI](./docs/api/openapi.yaml) - Especificación completa de endpoints
- 📊 [ER Diagrams](./docs/database/) - Modelo relacional
- 📖 [Frontend Guide](./docs/frontend-guide.md) - Patrones y mejores prácticas Vue.js
- 🔒 [Security Policy](./docs/SECURITY.md) - Políticas de seguridad

---

## Soporte y Contacto

- **Project Lead**: Víctor Javier Gómez Regalado
- **Estudiantes**: Raúl Sebastián Cruz Baño, Hernán Mateo Jurado Moran
- **Universidad**: Universidad de las Américas (UDLA)
- **Ubicación**: Quito, Ecuador
- **Fecha de Entrega**: 5 de mayo de 2026

---

## Licencia

Código propietario de Kynsoft SAS con derechos académicos para UDLA.

---

**Última actualización**: 6 de mayo de 2026
