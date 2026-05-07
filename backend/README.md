# Backend - Sistema de Control Administrativo

Backend del Sistema de Control Administrativo y Financiero para Escuelas de Conducción. Arquitectura de microservicios con Spring Boot 3 + Spring Cloud.

## 📦 Estructura

```
backend/
├── pom.xml                      # POM padre (versiones centralizadas)
│
├── shared/                      # Librerías compartidas entre microservicios
│   ├── common-events/           # DTOs de eventos RabbitMQ
│   ├── common-exceptions/       # Excepciones base + handlers RFC 7807
│   └── common-security/         # Utilidades JWT (Sprint 4)
│
├── eureka-server/               # Service discovery (puerto 8761)
├── api-gateway/                 # API Gateway con Spring Cloud Gateway (puerto 8080)
│
├── ms-auth/                     # Autenticación + Configuración (puerto 8081)
├── ms-estudiantes/              # Gestión de estudiantes (puerto 8082)
├── ms-instructores/             # Gestión de instructores (puerto 8083)
├── ms-vehiculos/                # Gestión de flota (puerto 8084)
├── ms-asignaciones/             # Programación de clases (puerto 8085)
├── ms-cobros/                   # Facturación y pagos (puerto 8086)
├── ms-reportes/                 # Reportes y exportación (puerto 8087)
└── ms-notificaciones/           # In-app + email (puerto 8088)
```

## 🚀 Arranque local (desarrollo)

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

> ⚠️ **Importante:** Este paso es necesario la primera vez para que los microservicios encuentren los módulos `shared/` (common-events, common-exceptions, common-security) en el repositorio Maven local.

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

Verificar: http://localhost:8761 (debería mostrar el dashboard de Eureka)

#### b) API Gateway

```bash
cd backend/api-gateway
mvn spring-boot:run
```

Verificar:
- http://localhost:8080/actuator/health → `UP`
- http://localhost:8080/actuator/gateway/routes → 8 rutas hacia los MS

#### c) Microservicios de dominio

Cada uno en su propia terminal:

```bash
cd backend/ms-auth        && mvn spring-boot:run
cd backend/ms-estudiantes && mvn spring-boot:run
# ... etc.
```

Cada MS:
- Se registra automáticamente en Eureka al iniciar
- Conecta a Postgres con su schema específico (`auth_schema`, `estudiantes_schema`, etc.)
- Conecta a RabbitMQ
- Expone `/actuator/health` en su puerto

## 🧪 Compilación y pruebas

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

## 🛠️ Comandos útiles

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

## 🔌 Puertos asignados

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

## 🔍 Endpoints útiles (Actuator)

Todos los servicios exponen estos endpoints:

- `/actuator/health` — Healthcheck (UP/DOWN)
- `/actuator/info` — Información del build
- `/actuator/metrics` — Métricas

El **API Gateway** además expone:
- `/actuator/gateway/routes` — Rutas configuradas

El **Eureka Server** además expone:
- `/eureka/apps` — Apps registradas (con `Accept: application/json`)

## 📚 Stack técnico

- **Java**: 21 (LTS)
- **Spring Boot**: 3.4.0
- **Spring Cloud**: 2024.0.0 (Eureka, Gateway, OpenFeign, LoadBalancer)
- **Persistencia**: PostgreSQL 15 + JPA/Hibernate + Flyway
- **Mensajería**: RabbitMQ + Spring AMQP
- **Cache**: Caffeine (in-memory)
- **Resilience**: Resilience4j (circuit breaker, retry)
- **JWT**: jjwt 0.12.x (Sprint 4)
- **Object storage**: MinIO 8.5.x
- **Mapeo**: MapStruct 1.6.x
- **OpenAPI**: SpringDoc 2.7.x
- **Testing**: JUnit 5, Mockito, AssertJ, Testcontainers, H2

## 📖 Documentación adicional

- [DECISIONES.md](../DECISIONES.md) — 30 decisiones técnicas del proyecto
- [SPRINTS_PLAN.xlsx](../SPRINTS_PLAN.xlsx) — Plan detallado de los 12 sprints
- [CLAUDE.md](../CLAUDE.md) — Guía operativa del proyecto

## ✅ Estado de validación (Sprint 1)

```
[x] Compilación: BUILD SUCCESS en 14/14 módulos
[x] Eureka Server arranca y expone dashboard
[x] API Gateway arranca, se registra en Eureka, tiene 8 rutas configuradas
[x] MS-Auth arranca, conecta a Postgres, se registra en Eureka
[x] Gateway descubre MS-Auth via Service Discovery (lb://ms-auth)
[x] Ruteo end-to-end Gateway → MS-Auth funcional
[ ] Tests unitarios con cobertura 80%+ (Sprint 4-12)
[ ] Tests de integración con Testcontainers (Sprint 4-12)
[ ] CI/CD con GitHub Actions (Sprint 1 - Tarea 3)
```
