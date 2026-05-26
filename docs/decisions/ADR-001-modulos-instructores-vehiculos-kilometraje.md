# ADR-001: Pulido módulos Instructores + Vehículos + Kilometraje en Asignaciones

**Estado:** Aceptada
**Fecha:** 2026-05-25
**Decisores:** Hernán Mateo Jurado (desarrollo), confirmado durante sesión iterativa

## Contexto

Tras cerrar el Sprint 9 (perfil de usuarios + refactor de estados), los módulos de Instructores, Vehículos y Asignaciones quedaron funcionales pero con:

1. **Frontend desalineado del backend** en varios DTOs (campos inexistentes, nombres con tildes inconsistentes, endpoints fantasma).
2. **Validaciones de negocio débiles**: se podían crear asignaciones a instructores fuera de su horario, con vehículos sin SOAT vigente, etc.
3. **Sin trazabilidad de uso real**: no se medían kilómetros recorridos por clase ni horas acumuladas por estudiante.
4. **Sin gestión de contratos de instructor**: no había cómo diferenciar TIEMPO_COMPLETO de POR_HORAS, ni configurar tarifas.
5. **Sin precio configurable de combustibles**: el costo de cada carga se ingresaba manual, sin cálculo automático.

## Decisión

Implementar tres bloques de funcionalidad en una sola sesión vertical:

### Bloque A — Instructores
- Migración V2 `ms-instructores`: agregar campos `tipo_contrato`, `horas_contrato_semanales`, `tarifa_hora`.
- Tipos de contrato: `TIEMPO_COMPLETO`, `MEDIO_TIEMPO`, `POR_HORAS` con validación cruzada (POR_HORAS exige tarifaHora; TC/MT exigen salarioMensual).
- Endpoint `GET /instructores/{id}/resumen-horas?desde=&hasta=` con cálculo de horas contratadas, sueldo estimado.
- Validación de dominio Ecuador: `licencia_numero == cedula`.
- Frontend pulido: ListaInstructoresView con StatCards clickeables, filtros, banner; InstructorFormView con sección "Contrato"; InstructorDetailView con TabView (Información / Horario) + grilla semanal de disponibilidad + lista de excepciones EXTRA/AUSENCIA.

### Bloque B — Vehículos
- Migración V2 `ms-vehiculos`: agregar campos `numero_motor`, `numero_chasis`, `capacidad_pasajeros`, `tipo_combustible_id` (FK) + tabla nueva `tipos_combustible` con seed Ecuador (Extra, Ecopaís, Súper, Diésel, Eléctrico).
- Endpoint CRUD `/tipos-combustible` (admin configura precios).
- Auto-cálculo de costo al registrar carga de combustible: si no se envía `costoTotal`, se calcula `litros × precio_actual_del_tipo`.
- Fix Inspeccion: alineado a valores oficiales RTV Ecuador (`APROBADA / REPROBADA / CONDICIONADA`).
- DTOs limpios (renombre `año` → `anio`).
- Frontend pulido: ListaVehiculosView con 5 StatCards clickeables + filtros; VehiculoFormView con 4 secciones; VehiculoDetailView con TabView 5 tabs + card "Eficiencia y consumo" derivada de las cargas registradas.
- Vista admin nueva `/configuracion/combustible` para gestión de precios.

### Bloque C — Asignaciones
- Migración V2 `ms-asignaciones`: agregar `km_inicial`, `km_final`, `hora_inicio_real`, `hora_fin_real`, `observaciones_recorrido`.
- 3 endpoints nuevos: `PATCH /asignaciones/{id}/iniciar`, `PATCH /asignaciones/{id}/finalizar`, `GET /asignaciones/{id}/recorrido`.
- **Sync cross-MS** al finalizar:
  - `PUT /vehiculos/{id}/kilometraje` (monotónico, solo aumenta).
  - `PUT /estudiantes/{id}/horas-completadas/incrementar` (suma minutos reales).
- **6 validaciones de negocio nuevas** en `validarEntidadesExisten()`:
  1. Estudiante con `categoriaLicenciaId` configurada.
  2. Vehículo en estado ACTIVO (no MANTENIMIENTO/FUERA_SERVICIO).
  3. Vehículo con SOAT vigente.
  4. Vehículo con RTV vigente.
  5. Categoría del vehículo == categoría del estudiante.
  6. Instructor con disponibilidad real (horario semanal + sin AUSENCIA + horario dentro de franja).
- Frontend: botones "Iniciar"/"Finalizar" contextuales en `/asignaciones` con modales y toast de resultado del sync.

### Bloque D — Estudiantes (soporte del sync)
- Migración V5 `ms-estudiantes`: columna `minutos_completados INTEGER DEFAULT 0`.
- Endpoint `PUT /estudiantes/{id}/horas-completadas/incrementar` consumido por ms-asignaciones.
- Auto-transición `MATRICULADO → CURSANDO` al sumar primeros minutos.

### Bloque E — Frontend cleanup
- `services/asignaciones.ts` reescrito alineado al backend (eliminados endpoints fantasma `verificar-disponibilidad` y `/fecha`; corregido `reprogramar` a PUT; tipos `EstadoAsignacion` con 6 valores reales; `TipoClase` unificado a `TEORICA/PRACTICA/EXAMEN`).

## Decisiones de diseño relevantes

| Decisión | Justificación |
|---|---|
| Rechazar (no advertir) las 6 validaciones de asignación | Evitar crear datos inválidos en producción; un admin podría no notar warnings |
| Sync de horas y km **best-effort con circuit breaker** | Si ms-vehiculos o ms-estudiantes no responden, la clase igual se finaliza; el `RecorridoResponse` indica el estado del sync |
| Sync de km **monotónico** | Nunca permitir retroceder el odómetro maestro del vehículo |
| Unificar `tipoClase` al estándar backend (`TEORICA/PRACTICA/EXAMEN`) | Backend es la fuente de verdad y el SQL ya tiene el constraint |
| `tipos_combustible` como tabla, no enum | Permite agregar tipos regionales (GLP_AUTO, biodiesel) sin migración |
| Precio combustible en la tabla, no por carga | Refleja el precio público vigente; admin lo actualiza cuando cambia |
| Renombrar `año` → `anio` en DTOs | Problemas con `ñ` en JSON/URL params, propagación a frontend |
| Categoría licencia obligatoria en estudiante y vehículo para crear asignación | Caso real: estudiante con licencia B no debe aprender en camión cat C |

## Consecuencias

### Positivas
- Operación de la escuela mucho más cercana al uso real: cada clase actualiza km del vehículo y horas del estudiante.
- Predicciones útiles habilitadas: próxima carga de combustible estimada, eficiencia km/galón.
- Imposible (vía API) crear asignaciones obviamente incorrectas (instructor sin horario, vehículo sin SOAT, etc.).
- Frontend totalmente alineado al backend en estos 3 módulos.

### Negativas / Trade-offs
- Backend cambió bastante — riesgo de regresión en flujos legacy. Mitigación: tests Maven `mvn test` siguen pasando (verificar antes de PR).
- Los datos legacy (estudiantes/vehículos sin `categoriaLicenciaId`) ya no permiten crear asignaciones hasta que se les asigne categoría. Mitigación: warning visible en UI.
- Para que `iniciar/finalizar` funcione, instructor debe tener horario configurado y vehículo tipo de combustible — el cliente debe migrar los registros existentes o configurarlos al usar.

### Pendientes que esta decisión no resuelve
- Notificaciones automáticas (ms-notificaciones no integrado aún por decisión del usuario).
- Auto-transición `CURSANDO → COMPLETADO` cuando estudiante alcanza horas requeridas del curso (hoy solo MATRICULADO→CURSANDO).
- Vista "Mis clases" para rol INSTRUCTOR.
- Reportes agregados (horas dictadas por instructor / mes, eficiencia flota).
- Cobros enlazados a asignaciones COMPLETADAS (descontar de paquete de horas pagado).

## Bugs encontrados y resueltos durante la implementación

### Bug 1 — TZ JVM ≠ Postgres
- Síntoma: `POST /horarios-trabajo` tipo EXTRA con horas daba HTTP 500 (check constraint).
- Causa: `Dockerfile.spring` tenía hardcoded `JAVA_OPTS=... -Duser.timezone=America/Guayaquil`, mientras Postgres en UTC. Hibernate aplicaba offset +5h al persistir `LocalTime+LocalDate`.
- Fix: sobrescribir `JAVA_OPTS` en `docker-compose.yml` (anchor `&ms-environment`) con `-Duser.timezone=UTC` + agregar `TZ=UTC`.

### Bug 2 — Feign no soporta PATCH
- Síntoma: sync de km vehículo desde ms-asignaciones daba 500 con "Invalid HTTP method: PATCH".
- Causa: Feign default usa `HttpURLConnection` que no soporta PATCH.
- Fix: cambiar el endpoint de destino de PATCH a PUT (es idempotente igual).

### Bug 3 — Mapper MapStruct con `ignore=true` arbitrarios
- Síntoma: `VehiculoResponse` nunca devolvía `fechaMantenimiento` ni `fechaInspeccion` aunque estaban en el DTO.
- Causa: el mapper tenía `@Mapping(target = "...", ignore = true)` para evitar warnings de campos no mapeados, pero el efecto era que los campos quedaban null.
- Fix: reescribir mapper limpio sin ignores arbitrarios.

### Bug 4 — Discrepancias frontend/backend (Vehículos)
- Síntoma: el detail de vehículo mostraba todos los campos vacíos.
- Causa: `services/vehiculos.ts` tenía interfaces con campos `nroMotor`, `nroChasis`, `capacidadPasajeros`, `fechaUltimaRevision` que el backend nunca devolvía.
- Fix: reescritura completa del service alineado a los DTOs reales.

## Alternativas consideradas (y descartadas)

| Alternativa | Por qué no |
|---|---|
| Sync de km vía evento RabbitMQ en vez de Feign | Más complejo para 1:1 simple; Feign con circuit breaker es suficiente y da respuesta inmediata al usuario |
| Hacer las validaciones de asignación solo "warning" con bypass | Riesgo de errores operativos en producción; un admin distraído podría crear datos inválidos |
| Mantener `año` con ñ en el DTO | Problemas constantes con encoding URL/JSON. La consistencia `anio` es estándar en el resto del proyecto |
| Crear `tipos_combustible` como enum Java | No permitiría agregar tipos regionales sin redeploy |
| Precio combustible por carga (sin tabla central) | Imposible saber "precio actual" para auto-cálculo |

## Referencias

- `memory/decisiones_kilometraje_horas.md` — detalle técnico del modelo.
- `memory/feedback_tz_dockerfile.md` — bug TZ.
- `memory/feedback_feign_no_patch.md` — bug Feign.
- Migraciones Flyway aplicadas:
  - `ms-instructores/V2__Add_Contrato_Fields.sql`
  - `ms-vehiculos/V2__Add_Combustible_Y_Campos_Vehiculo.sql`
  - `ms-asignaciones/V2__Add_Kilometraje_Asignacion.sql`
  - `ms-estudiantes/V5__Add_Horas_Completadas.sql`
