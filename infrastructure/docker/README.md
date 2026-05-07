# Docker - Infraestructura

Esta carpeta contiene los archivos de Docker Compose para levantar el entorno del proyecto.

## Archivos

| Archivo | Propósito |
|---------|-----------|
| `docker-compose.infra.yml` | **Solo infraestructura** (Postgres, RabbitMQ, MinIO, Adminer). Para desarrollo local con los MS corriendo en el IDE. |
| `docker-compose.yml` | **Stack completo** (infra + Eureka + API Gateway + 8 microservicios). Disponible y validado desde Sprint 1. |
| `Dockerfile.spring` | Dockerfile multi-stage compartido para todos los servicios Java (Eureka, Gateway, los 8 MS). |

## Servicios — `docker-compose.infra.yml` (4 contenedores)

| Servicio | Puerto host | Acceso |
|----------|-------------|--------|
| **PostgreSQL 15** | 5432 | `psql -h localhost -U escuela_user -d escuela_db` |
| **RabbitMQ 3.12** | 5672 | AMQP |
| **RabbitMQ Mgmt UI** | 15672 | http://localhost:15672 (guest/guest) |
| **MinIO API** | 9000 | S3 compatible |
| **MinIO Console** | 9001 | http://localhost:9001 (minioadmin/minioadmin123) |
| **Adminer** | 8888 | http://localhost:8888 |

## Servicios — `docker-compose.yml` (14 contenedores: infra + Eureka + Gateway + 8 MS)

Incluye todos los anteriores **más**:

| Servicio | Puerto host | Health |
|----------|-------------|--------|
| **Eureka Server** | 8761 | http://localhost:8761 |
| **API Gateway** | 8080 | http://localhost:8080/actuator/health |
| **MS-Auth** | 8081 | http://localhost:8081/actuator/health |
| **MS-Estudiantes** | 8082 | http://localhost:8082/actuator/health |
| **MS-Instructores** | 8083 | http://localhost:8083/actuator/health |
| **MS-Vehículos** | 8084 | http://localhost:8084/actuator/health |
| **MS-Asignaciones** | 8085 | http://localhost:8085/actuator/health |
| **MS-Cobros** | 8086 | http://localhost:8086/actuator/health |
| **MS-Reportes** | 8087 | http://localhost:8087/actuator/health |
| **MS-Notificaciones** | 8088 | http://localhost:8088/actuator/health |

## Comandos comunes

```bash
# Solo infra (MS corren desde el IDE)
docker compose -f infrastructure/docker/docker-compose.infra.yml up -d

# Stack completo containerizado
docker compose -f infrastructure/docker/docker-compose.yml up -d

# Stack completo + rebuild de imágenes (tras cambios de código)
docker compose -f infrastructure/docker/docker-compose.yml up -d --build

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

## Configuración

Las credenciales y puertos se configuran vía variables de entorno en el archivo `.env` (raíz del proyecto). Ver `.env.example`.

## Schemas de PostgreSQL

Al levantar Postgres por primera vez se ejecuta automáticamente `../postgres/init-schemas.sql` que crea los **9 schemas** (uno por microservicio + `shared_schema`):

`auth_schema`, `estudiantes_schema`, `instructores_schema`, `vehiculos_schema`, `asignaciones_schema`, `cobros_schema`, `reportes_schema`, `notificaciones_schema`, `shared_schema`.

Las tablas dentro de cada schema se crean vía **migraciones Flyway V1** que corre cada microservicio al arrancar (Sprint 2).

## Notas

- Adminer solo para desarrollo (NO usar en producción).
- MinIO usa credenciales por defecto en dev — cambiar en producción.
- El stack completo tarda ~50–60 s en pasar todos los healthchecks la primera vez.
