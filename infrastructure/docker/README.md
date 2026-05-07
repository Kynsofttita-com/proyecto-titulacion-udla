# Docker - Infraestructura

Esta carpeta contiene los archivos de Docker Compose para levantar el entorno de desarrollo del proyecto.

## Archivos

| Archivo | Propósito |
|---------|-----------|
| `docker-compose.infra.yml` | **Solo infraestructura** (Postgres, RabbitMQ, MinIO, Adminer). Para desarrollo local con MS corriendo en el IDE. |
| `docker-compose.yml` | **Stack completo** (infraestructura + todos los microservicios + frontend). _Se creará a partir del Sprint 1 cuando los MS estén implementados._ |

## Servicios incluidos en `docker-compose.infra.yml`

| Servicio | Puerto host | Puerto interno | Acceso |
|----------|-------------|----------------|--------|
| **PostgreSQL** | 5432 | 5432 | `psql -h localhost -U escuela_user -d escuela_db` |
| **RabbitMQ** | 5672 | 5672 | AMQP |
| **RabbitMQ Mgmt UI** | 15672 | 15672 | http://localhost:15672 (guest/guest) |
| **MinIO API** | 9000 | 9000 | S3 compatible API |
| **MinIO Console** | 9001 | 9001 | http://localhost:9001 (minioadmin/minioadmin123) |
| **Adminer** | 8888 | 8080 | http://localhost:8888 |

## Comandos comunes

```bash
# Levantar infraestructura (desde la raíz del proyecto)
docker compose -f infrastructure/docker/docker-compose.infra.yml up -d

# Ver logs en tiempo real
docker compose -f infrastructure/docker/docker-compose.infra.yml logs -f

# Ver logs de un servicio específico
docker compose -f infrastructure/docker/docker-compose.infra.yml logs -f postgres

# Detener servicios (preserva datos)
docker compose -f infrastructure/docker/docker-compose.infra.yml down

# Detener servicios y BORRAR todos los datos (CUIDADO)
docker compose -f infrastructure/docker/docker-compose.infra.yml down -v

# Verificar estado
docker compose -f infrastructure/docker/docker-compose.infra.yml ps
```

## Configuración

Las credenciales y puertos se configuran a través de variables de entorno en el archivo `.env` (raíz del proyecto). Ver `.env.example` como referencia.

## Schemas de PostgreSQL

Al levantar Postgres por primera vez, se ejecuta automáticamente el script `../postgres/init-schemas.sql` que crea los **9 schemas** (uno por microservicio + `shared_schema`).

## Notas

- El `docker-compose.yml` original de la raíz del proyecto fue movido aquí en el Sprint 0.
- Adminer es solo para desarrollo (NO usar en producción).
- MinIO usa credenciales por defecto en dev — cambiar en producción.
