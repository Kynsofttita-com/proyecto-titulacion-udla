# Estado del Sistema - Docker Compose Local

**Fecha:** 2026-07-12 23:30 UTC-5  
**Status:** Parcialmente Operacional  
**Contenedores:** 14 totales (9 corriendo, 5 con errores)

## Servicios Funcionando ✅

### Infraestructura (4/4)
- **PostgreSQL** (puerto 5432): Healthy - Aceptando conexiones
- **RabbitMQ** (puerto 5672, Management 15672): Healthy - API operativa
- **Eureka** (puerto 8761): Healthy - Service Discovery operativo
- **Adminer** (puerto 8089): Up - Admin BD disponible

### API Gateway & Frontend (2/2)
- **API Gateway** (puerto 8080): Healthy
  - Responde a `/actuator/health`
  - 4 microservicios registrados en Eureka
  - Status: `{"status":"UP"}`

- **Frontend Vue.js** (puerto 3000): Iniciando
  - Nginx configurado y listo
  - Health check en progreso

### Microservicios Operativos (3/8)
1. **MS-Auth** (puerto 8081): Healthy ✅
2. **MS-Notificaciones** (puerto 8088): Healthy ✅
3. **MS-Reportes** (puerto 8087): Healthy ✅

### Servicios NO Operativos (5/8)
Los siguientes servicios tienen errores en la compilación (ClassNotFoundException: EstudianteMapper):
- MS-Estudiantes (8082): Exited (1) - MapStruct compilation issue
- MS-Cobros (8086): Exited (1) - MapStruct compilation issue
- MS-Vehículos (8084): Exited (1) - MapStruct compilation issue
- MS-Asignaciones (8085): Exited (1) - MapStruct compilation issue
- MS-Instructores (8083): Exited (1) - MapStruct compilation issue

## Conectividad Validada ✅

```bash
# API Gateway - OK
curl http://localhost:8080/actuator/health
→ {"status":"UP", "components": {...}}

# Eureka - OK
curl http://localhost:8761/actuator/health
→ {"status":"UP"}

# RabbitMQ Management API - OK
curl -u guest:guest http://localhost:15672/api/aliveness-test/%2F
→ {"status":"ok"}

# PostgreSQL - OK
pg_isready -U postgres
→ /var/run/postgresql:5432 - accepting connections
```

## Puertos Disponibles Localmente

| Servicio | Puerto | Estado | URL |
|----------|--------|--------|-----|
| Frontend | 3000 | Iniciando | http://localhost:3000 |
| API Gateway | 8080 | ✅ Healthy | http://localhost:8080 |
| Eureka | 8761 | ✅ Healthy | http://localhost:8761 |
| MS-Auth | 8081 | ✅ Healthy | http://localhost:8081 |
| MS-Reportes | 8087 | ✅ Healthy | http://localhost:8087 |
| MS-Notificaciones | 8088 | ✅ Healthy | http://localhost:8088 |
| RabbitMQ (AMQP) | 5672 | ✅ Healthy | amqp://guest:guest@localhost:5672 |
| RabbitMQ (Mgmt) | 15672 | ✅ Healthy | http://localhost:15672 |
| PostgreSQL | 5432 | ✅ Healthy | postgresql://postgres:postgres123@localhost:5432/proyecto_db |
| Adminer | 8089 | ✅ Up | http://localhost:8089 |

## Problemas Identificados

### Crítico: Microservicios no iniciando
5 microservicios fallan con `NoClassDefFoundError: EstudianteMapper`. Esto indica un problema en la compilación de las clases generadas por MapStruct. Las posibles causas:
1. Las clases no se compilaron durante el build Maven
2. Faltan dependencias de MapStruct
3. Configuración incompleta del annotation processor

### Recomendación
Ejecutar build manual desde IDE o revisar los pom.xml de los servicios afectados:
```bash
cd backend
mvn clean package -DskipTests -pl ms-estudiantes,ms-cobros,ms-vehiculos,ms-asignaciones,ms-instructores
```

## Para Grabar el Video Demo

### Opción 1: Usar servicios disponibles actualmente
- API Gateway está completamente operativo
- Eureka muestra 4 servicios registrados
- RabbitMQ está listo para mensajería
- PostgreSQL está listo para datos
- Adminer disponible para ver BD

### Opción 2: Esperar resolución de los 5 servicios
Para tener el sistema 100% funcional, resolver el problema de MapStruct en esos servicios primero.

## Comando para reinicar todos los servicios

```bash
docker-compose down
docker-compose up -d
```

## Logs de Contenedores

Para ver logs de un servicio específico:
```bash
docker-compose logs -f [nombre-contenedor]
```

---
**Generado automáticamente** | Sistema listo para testing parcial
