# 4. Arquitectura de schemas

[← Volver al índice](../schema.md)

> Distribución de los 9 schemas dentro de la única instancia PostgreSQL `escuela_db`.

---

## Diagrama de schemas

```
+------------------------------------------------------------------+
|                       1 PostgreSQL DB                            |
|                         escuela_db                               |
+------------------------------------------------------------------+
|                                                                  |
|  +------------------------------------------------------------+  |
|  |  shared_schema (Auditoría centralizada + idempotencia)     |  |
|  |  - audit_log                                               |  |
|  |  - processed_events                                        |  |
|  +------------------------------------------------------------+  |
|                                                                  |
|  +------------------+  +------------------+  +-----------------+ |
|  | auth_schema      |  |estudiantes_schema|  |instructores_    | |
|  | (MS-Auth)        |  |(MS-Estudiantes)  |  |schema           | |
|  |                  |  |                  |  |(MS-Instructores)| |
|  | * usuarios       |  | * estudiantes    |  |                 | |
|  | * roles          |  | * documentos     |  | * instructores  | |
|  | * permisos       |  | * contactos_     |  | * certificaci.  | |
|  | * usuario_rol    |  |   emergencia     |  | * disponibilid. | |
|  | * rol_permiso    |  | * progreso_      |  | * horarios_     | |
|  | * password_reset_|  |   academico      |  |   trabajo       | |
|  |   token          |  | * asistencia     |  |                 | |
|  | * refresh_tokens |  |                  |  |                 | |
|  | * configuracion_ |  |                  |  |                 | |
|  |   escuela        |  |                  |  |                 | |
|  | * tipos_curso    |  |                  |  |                 | |
|  | * conceptos_     |  |                  |  |                 | |
|  |   facturacion    |  |                  |  |                 | |
|  | * categorias_    |  |                  |  |                 | |
|  |   licencia       |  |                  |  |                 | |
|  | * plantillas_    |  |                  |  |                 | |
|  |   email          |  |                  |  |                 | |
|  +------------------+  +------------------+  +-----------------+ |
|                                                                  |
|  +------------------+  +------------------+  +-----------------+ |
|  | vehiculos_schema |  |asignaciones_     |  | cobros_schema   | |
|  | (MS-Vehiculos)   |  |schema            |  | (MS-Cobros)     | |
|  |                  |  |(MS-Asignaciones) |  |                 | |
|  | * vehiculos      |  |                  |  | * facturas      | |
|  | * mantenimientos |  | * asignaciones   |  | * factura_      | |
|  | * registros_     |  | * cambios_       |  |   cuotas        | |
|  |   combustible    |  |   asignacion     |  | * pagos         | |
|  | * inspecciones   |  | * historial_     |  | * reconciliacion| |
|  | * documentos_    |  |   estados        |  |                 | |
|  |   vehiculo       |  |                  |  |                 | |
|  | * tipos_         |  |                  |  |                 | |
|  |   combustible    |  |                  |  |                 | |
|  +------------------+  +------------------+  +-----------------+ |
|                                                                  |
|  +-------------------------+  +-------------------------------+  |
|  | notificaciones_schema   |  | reportes_schema               |  |
|  | (MS-Notificaciones)     |  | (MS-Reportes)                 |  |
|  |                         |  |                               |  |
|  | * notificaciones        |  | * cache_reportes              |  |
|  | * log_envios_email      |  | * ejecuciones_reporte         |  |
|  | * preferencias_         |  |                               |  |
|  |   notificacion          |  |                               |  |
|  +-------------------------+  +-------------------------------+  |
+------------------------------------------------------------------+
```

---

## Tabla resumen de schemas

| Schema | Microservicio | Puerto | Tablas | Propósito |
|--------|---------------|--------|--------|-----------|
| `shared_schema` | Compartido (lo crea MS-Auth) | — | 2 | Auditoría centralizada + idempotencia eventos |
| `auth_schema` | MS-Auth | 8081 | 12 | Autenticación, autorización, configuración del sistema |
| `estudiantes_schema` | MS-Estudiantes | 8082 | 5 | Gestión de estudiantes |
| `instructores_schema` | MS-Instructores | 8083 | 4 | Gestión de instructores |
| `vehiculos_schema` | MS-Vehículos | 8084 | 6 | Gestión de flota |
| `asignaciones_schema` | MS-Asignaciones | 8085 | 3 | Programación tripartita de clases |
| `cobros_schema` | MS-Cobros | 8086 | 4 | Facturación, pagos, crédito a cuotas |
| `notificaciones_schema` | MS-Notificaciones | 8088 | 3 | Notificaciones in-app + emails |
| `reportes_schema` | MS-Reportes | 8087 | 2 | Cache y ejecuciones de reportes |

**Total: 9 schemas, 41 tablas, 1 base de datos.**

---

## Servicios de soporte

| Servicio | Puerto |
|----------|--------|
| Eureka Server | 8761 |
| RabbitMQ (broker) | 5672 |
| RabbitMQ Management UI | 15672 |
| MinIO (API) | 9000 |
| MinIO Console | 9001 |
| API Gateway | 8080 |
| Frontend Vue | 5173 |
