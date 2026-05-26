# 6. Migraciones Flyway aplicadas

[← Volver al índice](../schema.md)

> Estado real de migraciones por microservicio al 2026-05-26. Cada MS versiona sus migraciones de forma independiente en `backend/<ms>/src/main/resources/db/migration/`. MS-Auth crea también `shared_schema` en su V1.

---

## Resumen global

| MS | Migraciones | Total |
|----|-------------|-------|
| `ms-auth` | V1, V1_5 (seed), V2, V3, V4, V5, V6 | 7 |
| `ms-estudiantes` | V1, V2, V3, V4, V5 | 5 |
| `ms-instructores` | V1, V2 | 2 |
| `ms-vehiculos` | V1, V2 | 2 |
| `ms-asignaciones` | V1, V2 | 2 |
| `ms-cobros` | V1, V2 | 2 |
| `ms-notificaciones` | V1 | 1 |
| `ms-reportes` | V1 | 1 |

**Total: 22 migraciones aplicadas.**

---

## Detalle por microservicio

### ms-auth

| Migración | Descripción | Sprint |
|-----------|-------------|--------|
| `V1__Initial_Schema.sql` | Crea `shared_schema` + `auth_schema` (usuarios, roles, permisos, junctions, password_reset_token, configuracion_escuela, tipos_curso, conceptos_facturacion, categorias_licencia, plantillas_email, audit_log, processed_events) | Sprint 2 |
| `V1_5__Seed_Data.sql` | Datos seed: roles ADMIN/STAFF/INSTRUCTOR/ESTUDIANTE, permisos, admin@escuela.local, categorías licencia, conceptos facturación, tipos curso, plantillas email, configuración escuela default | Sprint 2 |
| `V2__refresh_tokens.sql` | Tabla `refresh_tokens` para refresh token rotation con JTI (UUID) | Sprint 4 |
| `V3__password_change_required.sql` | Columna `password_change_required` en `usuarios` para forzar cambio en primer login | Sprint 5 |
| `V4__seguridad_configurable.sql` | Parámetros de seguridad configurables en `configuracion_escuela`: `max_intentos_fallidos`, `duracion_bloqueo_minutos`, `expiracion_token_reset_minutos` | Sprint 5 |
| `V5__usuario_datos_personales.sql` | Campos personales en `usuarios`: `cedula`, `fecha_nacimiento`, `genero`, `direccion`, `ciudad`, `provincia` | Sprint 7 |
| `V6__fix_admin_password_hash.sql` | Corrección del hash bcrypt del admin seed para que coincida con `Admin123!` | Sprint 10 |

### ms-estudiantes

| Migración | Descripción | Sprint |
|-----------|-------------|--------|
| `V1__Initial_Schema.sql` | Crea `estudiantes_schema` con `estudiantes`, `documentos`, `contactos_emergencia`, `progreso_academico`, `asistencia` | Sprint 2 |
| `V2__estados_extendidos.sql` | Estados académicos extendidos (`PRE_MATRICULADO/MATRICULADO/CURSANDO/COMPLETADO/RETIRADO`) + columna `situacion_pago` inicial | Sprint 10 |
| `V3__pendiente_matricula.sql` | Añade `PENDIENTE_MATRICULA` a `situacion_pago` y cambia DEFAULT | Sprint 10 |
| `V4__situacion_pago_simplificada.sql` | Simplifica `situacion_pago` a 4 valores (`PENDIENTE_FACTURACION/PENDIENTE_PAGO/PAGO_PARCIAL/PAGADO_TOTAL`), amplía la columna a `VARCHAR(30)` | Sprint 10 |
| `V5__Add_Horas_Completadas.sql` | Contador `minutos_completados` para auto-transición a `COMPLETADO` | Sprint 10 |

### ms-instructores

| Migración | Descripción | Sprint |
|-----------|-------------|--------|
| `V1__Initial_Schema.sql` | Crea `instructores_schema` con `instructores`, `certificaciones`, `disponibilidad`, `horarios_trabajo` | Sprint 2 |
| `V2__Add_Contrato_Fields.sql` | Campos de contrato: `tipo_contrato`, `horas_contrato_semanales`, `tarifa_hora` | Sprint 10 |

### ms-vehiculos

| Migración | Descripción | Sprint |
|-----------|-------------|--------|
| `V1__Initial_Schema.sql` | Crea `vehiculos_schema` con `vehiculos`, `mantenimientos`, `registros_combustible`, `inspecciones`, `documentos_vehiculo` | Sprint 2 |
| `V2__Add_Combustible_Y_Campos_Vehiculo.sql` | Nueva tabla `tipos_combustible` (con seed Ecuador) + campos `numero_motor`, `numero_chasis`, `capacidad_pasajeros`, `tipo_combustible_id` en `vehiculos` | Sprint 10 |

### ms-asignaciones

| Migración | Descripción | Sprint |
|-----------|-------------|--------|
| `V1__Initial_Schema.sql` | Crea `asignaciones_schema` con `asignaciones`, `cambios_asignacion`, `historial_estados` | Sprint 2 |
| `V2__Add_Kilometraje_Asignacion.sql` | Campos `km_inicial`, `km_final`, `hora_inicio_real`, `hora_fin_real`, `observaciones_recorrido` en `asignaciones` | Sprint 10 |

### ms-cobros

| Migración | Descripción | Sprint |
|-----------|-------------|--------|
| `V1__Initial_Schema.sql` | Crea `cobros_schema` con `facturas`, `pagos`, `reconciliacion` | Sprint 2 |
| `V2__credito_y_cuotas.sql` | Ampliación de `facturas` (tipo_pago, numero_cuotas, frecuencia_cuota, etc.) + nueva tabla `factura_cuotas` + campos `numero_cuota` y `factura_cuota_id` en `pagos` | Sprint 10 |

### ms-notificaciones

| Migración | Descripción | Sprint |
|-----------|-------------|--------|
| `V1__Initial_Schema.sql` | Crea `notificaciones_schema` con `notificaciones`, `log_envios_email`, `preferencias_notificacion` | Sprint 2 |

### ms-reportes

| Migración | Descripción | Sprint |
|-----------|-------------|--------|
| `V1__Initial_Schema.sql` | Crea `reportes_schema` con `cache_reportes`, `ejecuciones_reporte` | Sprint 2 |

---

## Estrategia Flyway

- Cada microservicio se versiona independientemente.
- MS-Auth crea también `shared_schema` (no es un MS independiente porque solo contiene 2 tablas de uso compartido).
- No hay dependencias entre migraciones de distintos MS.
- Cada servicio puede desplegarse sin esperar a otros.
- Cada PR a `main` agrega como máximo 1 migración nueva por MS (regla de oro para evitar conflictos de versionado en paralelo).
