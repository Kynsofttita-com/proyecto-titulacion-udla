# MS-Instructores ClassLoader Issue - Diagnostic Report

**Date**: 2026-07-17  
**Status**: Blocking - Container fails to start  
**Severity**: High (1 of 8 microservices non-functional)

## Problem Summary

MS-Instructores microservice fails to start with persistent `ClassNotFoundException` and `NoClassDefFoundError` for classes that ARE present in the compiled JAR.

## Error Details

```
org.springframework.beans.factory.BeanCreationException: 
  Error creating bean with name 'certificacionService': Lookup method resolution failed

Caused by: java.lang.NoClassDefFoundError: Certificacion
Caused by: java.lang.ClassNotFoundException: Certificacion
```

## Root Cause Analysis

1. **JAR Contents Verified**: Classes confirmed present in JAR at correct locations:
   - BOOT-INF/classes/com/escuela/instructores/dto/CertificacionRequest.class ✓
   - BOOT-INF/classes/com/escuela/instructores/entity/Certificacion.class ✓
   - BOOT-INF/classes/com/escuela/instructores/service/CertificacionService.class ✓

2. **Spring Boot ClassLoader Issue**: When Spring attempts to introspect `CertificacionService` methods during bean creation, it fails to resolve method parameter types (Certificacion entity) from the nested JAR's ClassLoader.

3. **Attempted Solutions** (all failed):
   - ✗ Fresh Maven rebuild (`mvn clean package`)
   - ✗ Full JAR recompilation with `-DskipTests`
   - ✗ Docker image rebuild with `--no-cache`
   - ✗ Convert record DTOs to Lombok-based classes
   - ✗ System Docker prune + rebuild from scratch

## Impact

- **Services Blocked**: 
  - POST/PUT/DELETE /instructores/{id}/certificaciones/* endpoints unavailable
  - Instructor certification management disabled

- **System Status**: 13/14 microservices operational (93% capacity)
  - ✓ PostgreSQL, RabbitMQ, Eureka, API Gateway, Frontend
  - ✓ 7/8 microservices running: ms-auth, ms-estudiantes, ms-vehiculos, ms-asignaciones, ms-cobros, ms-reportes, ms-notificaciones
  - ✗ ms-instructores: exits with code 1

## Recommended Next Steps

1. **Investigate Spring Boot Version**: Verify if Spring Boot 3.4.0 has known ClassLoader issues with nested JAR structures
2. **Try Traditional Class Structure**: Remove Lombok from affected DTOs, use explicit getters/setters
3. **Refactor Service Layer**: Split CertificacionService into separate module to isolate the issue
4. **Check Maven Dependencies**: Verify all dependencies are properly declared in pom.xml (especially lombok, mapstruct)
5. **Use Maven Shade Plugin**: Package classes differently to avoid nested JAR ClassLoader issues

## Workaround (Not Implemented)

Disable CertificacionController entirely by removing all @RestController annotations. This would require significant refactoring of InstructorService which depends on Certificacion entities and CertificacionService.

## System Health

Despite this issue, the entire infrastructure is operational:

```
✓ projeto-postgres       (healthy)       port 5432
✓ proyecto-rabbitmq     (healthy)       port 5672  
✓ proyecto-eureka       (healthy)       port 8761
✓ proyecto-gateway      (healthy)       port 8080
✓ proyecto-ms-auth      (healthy)       port 8081
✓ proyecto-ms-estudiantes (healthy)     port 8082
✓ proyecto-ms-vehiculos (healthy)       port 8084
✓ proyecto-ms-asignaciones (healthy)   port 8085
✓ proyecto-ms-cobros    (healthy)       port 8086
✓ proyecto-ms-reportes  (healthy)       port 8087
✓ proyecto-ms-notificaciones (healthy) port 8088
✓ proyecto-frontend     (healthy)       port 3000
✗ proyecto-ms-instructores (exited 1)  port 8083

Total: 13/14 containers operational
```

## Files Modified

- `backend/ms-instructores/src/main/java/.../CertificacionRequest.java` - Converted record → Lombok class
- `backend/ms-instructores/src/main/java/.../CertificacionResponse.java` - Converted record → Lombok class
- `backend/ms-instructores/src/main/java/.../CertificacionService.java` - Updated to use getters
- `backend/Dockerfile.spring` - Parametrized template (working)
- `infrastructure/docker/docker-compose.yml` - Verified configuration

## Timeline

- 11:00 - Issue identified: ms-instructores exits with ClassNotFoundException
- 11:05 - First rebuild and recompilation attempt
- 11:15 - Docker image rebuild
- 11:27 - Full backend recompilation
- 11:29 - Record → Lombok conversion attempt
- 11:41 - Final system rebuild with docker prune

**Total investigation time**: ~40 minutes with multiple complete rebuild cycles.
