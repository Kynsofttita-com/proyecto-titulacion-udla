# PLAN_FASES.md — Replanteo de Sprints 5-12 por Fases/Grupos

**Documento de Replanteo del Plan de Sprints**
**Fecha de cambio:** 2026-05-22
**Estado:** ACTIVO — sustituye al plan horizontal original (Sprints 5-12) descrito en `DECISIONES.md §20` y `SPRINTS_PLAN.xlsx`.

> **¿Por qué este documento?**
> Los Sprints 0-4 se ejecutaron bajo el enfoque **horizontal** (todas las capas avanzan a la par en TODOS los microservicios). A partir del Sprint 5 se cambia a un enfoque **vertical por grupos**: terminar completo un grupo de MS (backend → frontend → testing) antes de pasar al siguiente. El objetivo es tener entregables funcionales completos al final de cada fase, en vez de tener todos los MS a medias hasta el final.

---

## 1. División de microservicios en grupos

### 🅰️ Grupo A — PRINCIPALES (6 MS)

Microservicios con la mayor carga de lógica de negocio y/o que ya tienen base sólida desde Sprints 1-4.

| MS | Schema | Razón |
|---|---|---|
| **MS-Auth** | `auth_schema` | Ya tiene login/refresh/forgot/reset/lockout desde Sprint 4. Falta: módulo Configuración, gestión de usuarios (CRUD), roles/permisos UI. |
| **MS-Estudiantes** | `estudiantes_schema` | Entidad central del negocio. Matrícula, documentos, contactos de emergencia, progreso académico, asistencia. |
| **MS-Instructores** | `instructores_schema` | Necesario para Asignaciones (dependencia). Certificaciones, disponibilidad, horarios de trabajo. |
| **MS-Vehículos** | `vehiculos_schema` | Flota, mantenimientos, combustible, inspecciones, documentos. Alertas SOAT. |
| **MS-Asignaciones** | `asignaciones_schema` | Lógica más compleja: asignación tripartita (instructor + estudiante + vehículo), validación de disponibilidad cross-MS, reprogramación, historial. |
| **MS-Cobros** | `cobros_schema` | Facturas, pagos (incluye parciales), conceptos, reconciliación. Crítico para el flujo financiero. |

### 🅱️ Grupo B — SECUNDARIOS (2 MS)

Microservicios con lógica de negocio menor o que dependen del Grupo A para tener algo útil que mostrar/notificar/reportar.

| MS | Schema | Razón |
|---|---|---|
| **MS-Notificaciones** | `notificaciones_schema` | Consumer de eventos RabbitMQ. Ya tiene los 3 listeners base (Sprint 4). Faltan plantillas configurables, log de envíos, in-app notifications, polling endpoint. |
| **MS-Reportes** | `reportes_schema` | Agregación cross-MS, exportación PDF/Excel, KPIs, dashboard. Necesita que el Grupo A esté funcional. |

---

## 2. Flujo de las 3 fases

```
┌─────────────────────────────────────────────────────────────┐
│  FASE 1 — GRUPO A (Sprints 5-8)                            │
│  ┌───────────┐   ┌───────────┐   ┌──────────┐   ┌────────┐ │
│  │ Sprint 5  │ → │ Sprint 6  │ → │ Sprint 7 │ → │ Sprint 8│ │
│  │ Backend   │   │ Backend   │   │ Frontend │   │ Testing │ │
│  │ A pt.1    │   │ A pt.2    │   │ Grupo A  │   │ Grupo A │ │
│  └───────────┘   └───────────┘   └──────────┘   └────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  FASE 2 — GRUPO B (Sprints 9-11)                           │
│  ┌───────────┐   ┌───────────┐   ┌──────────┐              │
│  │ Sprint 9  │ → │ Sprint 10 │ → │ Sprint 11│              │
│  │ Backend B │   │ Frontend B│   │ Testing B│              │
│  └───────────┘   └───────────┘   └──────────┘              │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  FASE 3 — CIERRE (Sprint 12)                                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Sprint 12 — E2E global + Performance + OWASP + Demo  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. FASE 1 — Grupo A

### 🟦 Sprint 5 — Backend Grupo A, parte 1 (CRUDs base)

**Objetivo:** dejar funcionales los CRUDs REST de los 4 MS más simples del Grupo A. Cierra el módulo de configuración de MS-Auth.

**MS incluidos:** MS-Auth (cierre), MS-Estudiantes, MS-Instructores, MS-Vehículos.

**Tareas:**

| Tarea | MS | Descripción | Criterios de aceptación |
|---|---|---|---|
| **T5.1** | MS-Auth | Cierre módulo Configuración + CRUD usuarios/roles | `GET/PUT /configuracion`, CRUD `tipos_curso`, `conceptos_facturacion`, `categorias_licencia`, `plantillas_email`. CRUD `/usuarios` (admin), asignación de roles. Tests unit ≥80%. |
| **T5.2** | MS-Estudiantes | CRUD completo estudiantes | `POST/GET/PUT/DELETE /estudiantes` + sub-recursos `/documentos`, `/contactos-emergencia`, `/progreso`, `/asistencia`. Validación cédula Ecuador. `@PreAuthorize` por rol. Soft delete. Tests ≥80%. |
| **T5.3** | MS-Instructores | CRUD completo instructores | `POST/GET/PUT/DELETE /instructores` + `/certificaciones`, `/disponibilidad`, `/horarios-trabajo`. Validación cédula + licencia profesional. Endpoint `GET /instructores/{id}/disponibilidad?fecha`. Tests ≥80%. |
| **T5.4** | MS-Vehículos | CRUD completo vehículos | `POST/GET/PUT/DELETE /vehiculos` + `/mantenimientos`, `/combustible`, `/inspecciones`, `/documentos`. Validación placa Ecuador. Alerta SOAT (campo `fecha_vencimiento_soat`). Tests ≥80%. |
| **T5.5** | Cross | Evento `usuario.creado` propaga a MS dependientes | Cuando se crea estudiante/instructor, MS-Auth crea usuario automáticamente (publica `UsuarioCreadoEvent` consumido por MS-Estudiantes/Instructores para enlazar `usuario_id`). |

**Branches:** `feature/sprint-5-1-auth-configuracion`, `feature/sprint-5-2-estudiantes-crud`, `feature/sprint-5-3-instructores-crud`, `feature/sprint-5-4-vehiculos-crud`, `feature/sprint-5-5-evento-usuario-creado`.

**PRs:** 1 por tarea (5 PRs).

**Validación cierre Sprint 5:** suite curl `test_sprint5_grupo_a_pt1.sh` con ≥50 tests cubriendo CRUDs, validaciones Ecuador, autorización por rol, soft delete.

---

### 🟦 Sprint 6 — Backend Grupo A, parte 2 (CRUDs complejos)

**Objetivo:** terminar los 2 MS más complejos del Grupo A (lógica cross-MS).

**MS incluidos:** MS-Asignaciones, MS-Cobros.

**Tareas:**

| Tarea | MS | Descripción | Criterios de aceptación |
|---|---|---|---|
| **T6.1** | MS-Asignaciones | CRUD asignación tripartita | `POST /asignaciones` valida disponibilidad de instructor (Feign → MS-Instructores), estudiante activo (Feign → MS-Estudiantes), vehículo disponible (Feign → MS-Vehículos). Detección de conflictos (mismo instructor/vehículo en mismo horario). Tests ≥80%. |
| **T6.2** | MS-Asignaciones | Reprogramación + historial | `PUT /asignaciones/{id}/reprogramar`, tabla `historial_estados` registra cambios. Eventos `asignacion.creada`, `asignacion.reprogramada`, `asignacion.cancelada`. |
| **T6.3** | MS-Cobros | CRUD facturas + pagos | `POST/GET /facturas` (con líneas de detalle), `POST /pagos` (soporta parciales, suma hasta total). Cálculo de saldo pendiente. Validación de RUC. Estado: PENDIENTE/PAGADA/PARCIAL/ANULADA. Tests ≥80%. |
| **T6.4** | MS-Cobros | Reconciliación + reporte de saldo | `GET /cobros/estudiante/{id}` devuelve estado de cuenta (facturas + pagos + saldo). Eventos `pago.registrado`, `factura.emitida`. |
| **T6.5** | Cross | Circuit Breakers Resilience4j en Feign clients | Los clients en MS-Asignaciones a Estudiantes/Instructores/Vehículos deben tener circuit breaker + retry + fallback. Test de fallo (Feign con MS caído devuelve 503). |

**Branches:** `feature/sprint-6-1-asignaciones-crud`, `feature/sprint-6-2-asignaciones-reprogramar`, `feature/sprint-6-3-cobros-facturas-pagos`, `feature/sprint-6-4-cobros-reconciliacion`, `feature/sprint-6-5-resilience4j`.

**PRs:** 5.

**Validación cierre Sprint 6:** suite curl `test_sprint6_grupo_a_pt2.sh` con ≥40 tests cubriendo asignación tripartita, conflictos, pagos parciales, eventos publicados.

---

### 🟦 Sprint 7 — Frontend Grupo A (vistas/forms/stores)

**Objetivo:** implementar TODA la UI del Grupo A. Al cerrar el sprint, un usuario puede usar la app completa para gestionar usuarios, estudiantes, instructores, vehículos, asignaciones y cobros.

**Tareas:**

| Tarea | Foco | Descripción |
|---|---|---|
| **T7.1** | Infra Frontend | Setup base Vue 3 + Vite + TS + PrimeVue + Pinia + Router + Axios + interceptors JWT (cookie auto), MainLayout/AuthLayout, sidebar, header, breadcrumbs. |
| **T7.2** | Auth + Configuración | LoginView, ForgotPasswordView, ResetPasswordView, ConfiguracionView (admin), UsuariosCRUDView, RolesView. Store `useAuthStore`. |
| **T7.3** | Estudiantes | ListaEstudiantesView (datatable + filtros), EstudianteFormView (crear/editar), EstudianteDetailView (tabs: datos, documentos, contactos, progreso, asistencia). Store `useEstudiantesStore`. |
| **T7.4** | Instructores | ListaInstructoresView, InstructorFormView, InstructorDetailView (tabs: datos, certificaciones, disponibilidad, horarios). Calendario de disponibilidad con FullCalendar. Store `useInstructoresStore`. |
| **T7.5** | Vehículos | ListaVehiculosView, VehiculoFormView, VehiculoDetailView (tabs: datos, mantenimientos, combustible, inspecciones, docs). Alertas SOAT en dashboard. Store `useVehiculosStore`. |
| **T7.6** | Asignaciones | CalendarioAsignacionesView (FullCalendar drag & drop), AsignacionFormView (wizard tripartita con validación en vivo), AsignacionDetailView. Store `useAsignacionesStore`. |
| **T7.7** | Cobros | EstadoCuentaView (por estudiante), FacturaFormView, PagoFormView (soporta parciales), HistoricoCobrosView. Store `useCobrosStore`. |

**Branches:** `feature/sprint-7-1-frontend-base`, ..., `feature/sprint-7-7-cobros-ui`.

**PRs:** 7.

**Validación cierre Sprint 7:** smoke manual de los 7 flujos en Chrome+Firefox. Lint + type-check + vitest unit tests ≥80%.

---

### 🟦 Sprint 8 — Testing Grupo A (unit + integration + E2E)

**Objetivo:** dejar el Grupo A con cobertura ≥80% en backend y frontend, e2e con Cypress de los flujos críticos.

**Tareas:**

| Tarea | Tipo | Descripción |
|---|---|---|
| **T8.1** | Unit Backend | Cobertura backend ≥80% en los 6 MS del Grupo A. Subir umbral si está bajo. JaCoCo verifies. |
| **T8.2** | Integration Backend | Testcontainers (Postgres + RabbitMQ) por MS. Tests `*IT.java` cubriendo controllers + service + repository real. Mínimo 5 IT por MS. |
| **T8.3** | Unit Frontend | Vitest ≥80% en components, stores, composables del Grupo A. |
| **T8.4** | E2E Cypress | 5 flujos completos: 1) Login→logout, 2) Matricular estudiante completo, 3) Crear asignación tripartita, 4) Registrar pago parcial, 5) Editar configuración escuela. |
| **T8.5** | Bugfixes | Slot para corregir bugs encontrados durante testing. |

**Branches:** `feature/sprint-8-1-backend-coverage`, ..., `feature/sprint-8-5-bugfixes`.

**PRs:** 5.

**Validación cierre Sprint 8 = cierre Fase 1:** todos los tests pasan en CI. Demo del Grupo A end-to-end. Documentar evidencia en `docs/fase-1-cierre.md`.

---

## 4. FASE 2 — Grupo B

### 🟩 Sprint 9 — Backend Grupo B

**Objetivo:** dejar funcionales los 2 MS del Grupo B.

**Tareas:**

| Tarea | MS | Descripción |
|---|---|---|
| **T9.1** | MS-Notificaciones | Plantillas configurables CRUD (`plantillas_email`), variables sustituibles ({{nombre}}, {{fecha}}, etc.). Tabla `log_envios` registra cada envío. Endpoint `POST /notificaciones/test` para probar plantillas. |
| **T9.2** | MS-Notificaciones | Notificaciones in-app: tabla `notificaciones`, `GET /notificaciones?leidas=false`, `PATCH /notificaciones/{id}/leer`. Consumer adicional para eventos del Grupo A (asignación.creada, pago.registrado, etc.). |
| **T9.3** | MS-Reportes | Endpoints de reportes operativos: estudiantes activos, instructores con horas, vehículos con SOAT por vencer, asistencia por curso. Queries cross-MS vía Feign. |
| **T9.4** | MS-Reportes | Endpoints de reportes financieros: ingresos por período, saldos pendientes, morosidad, recibos emitidos. KPIs para dashboard. |
| **T9.5** | MS-Reportes | Exportación PDF (Thymeleaf + Flying Saucer / OpenPDF) + Excel (Apache POI). Endpoint `POST /reportes/exportar?formato=pdf|excel`. |
| **T9.6** | MS-Reportes | Cache de reportes (Caffeine) con TTL configurable. Tabla `ejecuciones_reporte` para auditoría. |

**Branches/PRs:** 6.

**Validación cierre Sprint 9:** suite curl `test_sprint9_grupo_b.sh` con ≥30 tests. Generar 5 reportes diferentes en PDF y Excel.

---

### 🟩 Sprint 10 — Frontend Grupo B

**Objetivo:** UI completa del Grupo B + integración con eventos del Grupo A.

**Tareas:**

| Tarea | Foco | Descripción |
|---|---|---|
| **T10.1** | Notificaciones in-app | Componente `<NotificacionesDropdown />` en header con badge (count no leídas). Polling cada 30s. Marcar como leída. Store `useNotificacionesStore`. |
| **T10.2** | Configuración Notificaciones | PlantillasEmailView (CRUD + preview), LogEnviosView (histórico, filtros). |
| **T10.3** | Dashboard | DashboardView con KPIs (Chart.js): estudiantes activos, ingresos del mes, clases programadas hoy, vehículos con SOAT por vencer. Cards interactivas. |
| **T10.4** | Reportes operativos UI | ReporteEstudiantesView, ReporteInstructoresView, ReporteVehiculosView, ReporteAsistenciaView. Filtros (fechaInicio/fechaFin), tabla, botones export PDF/Excel. |
| **T10.5** | Reportes financieros UI | ReporteIngresosView, ReporteMorosidadView, ReporteRecibosView. Charts (barras, líneas). Export PDF/Excel. |

**Branches/PRs:** 5.

**Validación cierre Sprint 10:** smoke manual de todos los reportes. Notificaciones in-app llegan en <30s tras evento. Exports PDF/Excel funcionan.

---

### 🟩 Sprint 11 — Testing Grupo B

**Objetivo:** Grupo B con cobertura ≥80% y E2E.

**Tareas:**

| Tarea | Tipo | Descripción |
|---|---|---|
| **T11.1** | Unit Backend | Cobertura backend ≥80% en MS-Notificaciones y MS-Reportes. |
| **T11.2** | Integration Backend | Testcontainers para MS-Notificaciones (RabbitMQ + Postgres + GreenMail SMTP), MS-Reportes (Postgres + Feign mocks). |
| **T11.3** | Unit Frontend | Vitest ≥80% en Dashboard, Reportes, Notificaciones. |
| **T11.4** | E2E Cypress | 3 flujos: 1) Evento publicado→email enviado→notif in-app→leída, 2) Generar reporte operativo + export PDF, 3) Editar plantilla de email + envío de prueba. |
| **T11.5** | Bugfixes | Slot para correcciones. |

**Branches/PRs:** 5.

**Validación cierre Sprint 11 = cierre Fase 2:** todos los tests pasan. Demo del Grupo B. Documentar en `docs/fase-2-cierre.md`.

---

## 5. FASE 3 — Cierre

### 🟥 Sprint 12 — Cierre global

**Objetivo:** integración total, performance, seguridad, demo y documentación final.

**Tareas:**

| Tarea | Tipo | Descripción |
|---|---|---|
| **T12.1** | E2E cruzado | Cypress: flujo completo de un estudiante real. Matrícula → emisión factura → asignación 5 clases → asistencia → pago parcial → pago restante → recibo enviado por email → aparece en reporte financiero. |
| **T12.2** | Performance | JMeter: 50 usuarios concurrentes en endpoints críticos. Target: p95 <500ms. Identificar bottlenecks, agregar índices BD si faltan, ajustar pool de conexiones HikariCP. |
| **T12.3** | OWASP Top 10 review | Checklist completo: injection (parametrized queries OK), broken auth (JWT validado), sensitive data exposure (HTTPS, BCrypt, HttpOnly), XXE (N/A), broken access control (`@PreAuthorize` revisado), security misconfig, XSS (Vue auto-escape), insecure deserialization, components con vulns (mvn dependency-check), insufficient logging. |
| **T12.4** | Rate limiting Gateway | Implementar 100 req/min/IP en API Gateway (definido en DECISIONES §6.3, pospuesto). Bucket4j o filtro custom. |
| **T12.5** | Limpieza | Eliminar TODOs, código muerto, console.log, dependencias no usadas. Cleanup de refresh tokens expirados (scheduler). |
| **T12.6** | Documentación final | README maestro actualizado, runbook de operación (`docs/runbook.md`), guía de usuario (`docs/manual-usuario.md`), C4 diagrams actualizados, OpenAPI specs publicados en `/docs/api/`. |
| **T12.7** | Deploy | Subir a Oracle Cloud Free Tier (o fallback DigitalOcean $6). Nginx + Let's Encrypt. Dominio configurado. Backup diario automatizado (cron + pg_dump). |
| **T12.8** | Demo + Presentación | Video demo del sistema completo (15 min). Slides de cierre. Entrega final para titulación. |

**Branches/PRs:** 8.

**Validación cierre Sprint 12 = cierre del proyecto:** sistema desplegado, accesible públicamente, demo lista, documentación completa. Crear tag `v1.0.0`.

---

## 6. Dependencias críticas

| Dependencia | Sprint origen | Sprint destino |
|---|---|---|
| `UsuarioCreadoEvent` publicado por MS-Auth | Sprint 4 (ya hecho) | Sprint 5 consume en MS-Estudiantes/Instructores |
| CRUD Instructores funcional | Sprint 5 (T5.3) | Sprint 6 (T6.1) MS-Asignaciones lo consulta vía Feign |
| Eventos del Grupo A (`asignacion.creada`, `pago.registrado`, etc.) | Sprint 6 | Sprint 9 (T9.2) MS-Notificaciones los consume |
| CRUDs Grupo A funcionales | Sprint 6 | Sprint 7 (UI consume APIs) |
| Frontend base setup | Sprint 7 (T7.1) | Sprint 10 lo extiende |
| Reportes funcionando | Sprint 9 | Sprint 10 (UI consume) |
| Todos los flujos | Sprints 5-11 | Sprint 12 (E2E cruzado) |

---

## 7. Reglas operativas (vigentes desde Sprint 5)

1. **Branching:** `feature/sprint-N-X-descripcion-corta` (ej: `feature/sprint-5-2-estudiantes-crud`).
2. **Commits:** formato `Sprint N (Tarea X descripcion)`. 1 commit = 1 tarea finalizada y probada.
3. **PRs:** **1 PR por commit/tarea**, squash and merge a `main`. CI verde obligatorio.
4. **Cobertura:** JaCoCo ≥80% obligatorio en cada PR a partir del Sprint 5.
5. **Validación al cerrar sprint:** suite curl + smoke manual + reportar en `docs/sprint-N-cierre.md`.
6. **Modelo Claude:** Opus 4.7 high (regla permanente).
7. **Idioma:** Español respetando convenciones del lenguaje. Documentos formales en español correcto.
8. **NO programar sin OK explícito del usuario.** Documentos de planificación (.md, .xlsx) sí se pueden generar sin OK.

---

## 8. Tabla resumen de los 8 sprints restantes

| Sprint | Fase | Foco | MS impactados | PRs estimados | Validación |
|---|---|---|---|---|---|
| **5** | Fase 1 | Backend A pt.1 (CRUDs base) | Auth, Estudiantes, Instructores, Vehículos | 5 | curl + JaCoCo ≥80% |
| **6** | Fase 1 | Backend A pt.2 (CRUDs complejos) | Asignaciones, Cobros | 5 | curl + JaCoCo + Feign + circuit breakers |
| **7** | Fase 1 | Frontend Grupo A | 6 MS del Grupo A (UI) | 7 | smoke manual + vitest ≥80% |
| **8** | Fase 1 | Testing Grupo A | 6 MS del Grupo A | 5 | unit+IT+E2E Cypress 5 flujos |
| **9** | Fase 2 | Backend Grupo B | Notificaciones, Reportes | 6 | curl + reports PDF/Excel |
| **10** | Fase 2 | Frontend Grupo B | Dashboard, Reportes UI, Notif | 5 | smoke + polling notif <30s |
| **11** | Fase 2 | Testing Grupo B | 2 MS del Grupo B | 5 | unit+IT+E2E Cypress 3 flujos |
| **12** | Fase 3 | Cierre global | TODO el sistema | 8 | E2E cruzado, deploy, demo |

**Total PRs estimados Sprints 5-12: ~46 PRs** (vs ~12 PRs si fuera 1 por sprint).

---

## 9. Cambios respecto al plan original (`DECISIONES.md §20`)

| Aspecto | Plan original (horizontal) | Plan nuevo (vertical por grupos) |
|---|---|---|
| Sprint 5 | CRUD Estudiantes solo | CRUDs Auth+Estudiantes+Instructores+Vehículos |
| Sprint 6 | CRUD Instructores+Vehículos | CRUDs Asignaciones+Cobros |
| Sprint 7 | CRUD Asignaciones+Cobros | Frontend completo Grupo A |
| Sprint 8 | MS-Reportes (backend) | Testing Grupo A |
| Sprint 9 | Frontend base | Backend Grupo B (Notif+Reportes) |
| Sprint 10 | Frontend CRUDs | Frontend Grupo B |
| Sprint 11 | Frontend Asignaciones+Reportes+Dashboard | Testing Grupo B |
| Sprint 12 | Cierre (tests, deploy, docs) | Cierre (igual: E2E cruzado, deploy, demo, docs) |

---

## 10. Referencias

- `DECISIONES.md` — Decisiones técnicas (sigue siendo fuente de verdad para stack/convenciones)
- `SPRINTS_PLAN.xlsx` — Plan tabular original (queda como histórico; **este documento lo sustituye para Sprints 5-12**)
- `CLAUDE.md` — Contexto general
- `docs/sprint-4-validacion-e2e.md` — Evidencia de cierre Sprint 4
- `docs/database/schema.md` — Modelo de BD detallado (consolidado en Sprint 10, reemplaza al anterior `MODELO_BD_COMPLETO.md`)

---

*Documento generado: 2026-05-22*
*Última actualización: 2026-05-22*
