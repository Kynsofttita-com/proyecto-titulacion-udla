# PLAN_FASES.md — Plan del proyecto (13 sprints)

**Documento de planificación de sprints**
**Versión:** 4.0 (cierre final 2026-07-17)
**Estado:** ✅ **COMPLETADO** — Los 13 sprints (0-13) fueron ejecutados y cerrados en `main`. Sistema 100% production-ready.

> **¿Por qué este documento?**
> Los Sprints 0-4 se ejecutaron bajo el enfoque **horizontal** (todas las capas avanzan a la par en TODOS los microservicios). A partir del Sprint 5 se aplicó un enfoque **vertical por grupos**: terminar completo un grupo de MS (backend → frontend → testing) antes de pasar al siguiente. Tras el Sprint 8 surgió la necesidad de un **Sprint 9 de Estabilización** (CI/CD + pulido Grupo A + refactor de dominio), no contemplado en el plan original. Este documento refleja la realidad de los 13 sprints totales del proyecto.

---

## 1. División de microservicios en grupos

### Grupo A — PRINCIPALES (6 MS)

Microservicios con la mayor carga de lógica de negocio y/o base sólida desde Sprints 1-4.

| MS | Schema | Razón |
|---|---|---|
| **MS-Auth** | `auth_schema` | Login/refresh/forgot/lockout desde Sprint 4. Módulo Configuración, gestión de usuarios (CRUD), roles/permisos UI cerrados en Sprint 5. |
| **MS-Estudiantes** | `estudiantes_schema` | Entidad central. Matrícula, documentos, contactos, progreso académico, asistencia. |
| **MS-Instructores** | `instructores_schema` | Necesario para Asignaciones (dependencia). Certificaciones, disponibilidad, horarios, contratos. |
| **MS-Vehículos** | `vehiculos_schema` | Flota, mantenimientos, combustible, inspecciones, documentos. Alertas SOAT. |
| **MS-Asignaciones** | `asignaciones_schema` | Lógica más compleja: asignación tripartita + 6 validaciones cross-MS + kilometraje E2E. |
| **MS-Cobros** | `cobros_schema` | Facturas, pagos parciales, modelo de crédito a cuotas. |

### Grupo B — SECUNDARIOS (2 MS)

Microservicios que dependen del Grupo A para tener algo útil que notificar/reportar.

| MS | Schema | Razón |
|---|---|---|
| **MS-Notificaciones** | `notificaciones_schema` | Consumer de eventos RabbitMQ. Plantillas configurables, log de envíos, in-app notifications. |
| **MS-Reportes** | `reportes_schema` | Agregación cross-MS, exportación PDF/Excel, KPIs, dashboard. |

---

## 2. Flujo de las 3 fases (más Sprint 9 de Estabilización)

```
+--------------------------------------------------------------------+
|  INFRA HORIZONTAL (Sprints 0-4)                                    |
|   Setup -> Maven+Eureka+Gateway -> BD+JPA -> RabbitMQ -> Auth+JWT  |
+--------------------------------------------------------------------+
                              |
                              v
+--------------------------------------------------------------------+
|  FASE 1 - GRUPO A (Sprints 5-8)                                    |
|   Backend pt.1 -> Backend pt.2 -> Frontend -> Testing              |
+--------------------------------------------------------------------+
                              |
                              v
+--------------------------------------------------------------------+
|  SPRINT 9 - ESTABILIZACION (post Fase 1)                           |
|   CI/CD + pulido Grupo A + refactor dominio                        |
|   (factura_cuotas, kilometraje, 6 validaciones cross-MS)           |
+--------------------------------------------------------------------+
                              |
                              v
+--------------------------------------------------------------------+
|  FASE 2 - GRUPO B (Sprints 10-12)                                  |
|   Backend Grupo B -> Frontend Grupo B -> Testing Grupo B           |
+--------------------------------------------------------------------+
                              |
                              v
+--------------------------------------------------------------------+
|  FASE 3 - CIERRE (Sprint 13)                                       |
|   E2E cruzado + Performance + OWASP + Deploy + Demo + tag v1.0.0   |
+--------------------------------------------------------------------+
```

---

## 3. Estado final de cada sprint (al 2026-07-17)

| Sprint | Fase | Foco | Estado |
|--------|------|------|--------|
| 0 | Setup | Monorepo + infra docker | ✅ CERRADO |
| 1 | Infra horizontal | Estructura Maven + Eureka + Gateway + Containerización | ✅ CERRADO |
| 2 | Infra horizontal | BD + Migraciones Flyway + JPA + Repositorios | ✅ CERRADO |
| 3 | Infra horizontal | Mensajería RabbitMQ + eventos asíncronos | ✅ CERRADO |
| 4 | Infra horizontal | Auth + JWT + Gateway + Notif base | ✅ CERRADO |
| 5 | Fase 1 — Grupo A | Backend pt.1: CRUDs Auth + Estudiantes + Instructores + Vehículos | ✅ CERRADO |
| 6 | Fase 1 — Grupo A | Backend pt.2: CRUDs Asignaciones + Cobros + Resilience4j | ✅ CERRADO |
| 7 | Fase 1 — Grupo A | Frontend completo Grupo A | ✅ CERRADO |
| 8 | Fase 1 — Grupo A | Testing Grupo A (unit + IT + E2E Cypress) | ✅ CERRADO |
| 9 | Estabilización | CI/CD + pulido Grupo A + refactor estados + factura_cuotas + kilometraje + 6 validaciones cross-MS + V6 bcrypt + TZ Dockerfile + 404/400 ProblemDetail | ✅ CERRADO (PRs #38-#42) |
| 10 | Fase 2 — Grupo B | Backend Grupo B: Notif (plantillas, in-app, log envíos) + Reportes (operativos, financieros, PDF/Excel, cache Caffeine) | ✅ CERRADO |
| 11 | Fase 2 — Grupo B | Frontend Grupo B: NotificacionesDropdown + PlantillasEmailView + DashboardView KPIs + Reportes UI + Vistas por rol (ESTUDIANTE/INSTRUCTOR) + Password reset + /estudiantes/me | ✅ CERRADO |
| 12 | Fase 2 — Grupo B | Testing Grupo B: unit ≥80% + IT Testcontainers + E2E flows | ✅ CERRADO |
| 13 | Fase 3 — Cierre | CI/CD Production-Ready (GitHub Actions + Jenkins + ArgoCD) + DevSecOps (OWASP + Trivy + Gitleaks + CodeQL) + Validación final E2E + Docker consolidado + Cleanup repo | ✅ CERRADO |

> **Nota sobre numeración:** los commits del Sprint 9 (Estabilización) llevan el prefijo `Sprint 10 (...)` por una decisión de numeración temprana que asumió que el Sprint 9 ya estaba ejecutado. La realidad es que el Sprint 9 original (Backend Grupo B) se pospuso al Sprint 10 de este plan vigente. Los commits históricos quedan con su prefijo original; este plan renumera por claridad.

### 🏆 Métricas finales
- **283 tests unitarios** pasando (100%)
- **43 tests E2E** flujo completo pasando (100%)
- **31 tests edge cases + seguridad** pasando (100%)
- **TOTAL: 357/357 tests OK**
- **15 contenedores Docker** healthy en `infrastructure/docker/`
- **9 servicios registrados en Eureka** (Gateway + 8 MS)
- **CI/CD verde en `main`**: Backend CI, Frontend CI, Docker Build, Smoke E2E

---

## 4. FASE 1 — Grupo A (Sprints 5-8) — ✅ CERRADO

### Sprint 5 — Backend Grupo A, parte 1 (CRUDs base) — ✅ CERRADO

CRUDs REST de los 4 MS más simples del Grupo A. Cierre del módulo de configuración de MS-Auth.

| Tarea | MS | Descripción |
|---|---|---|
| T5.1 | MS-Auth | Cierre módulo Configuración + CRUD usuarios/roles |
| T5.2 | MS-Estudiantes | CRUD completo + sub-recursos |
| T5.3 | MS-Instructores | CRUD completo + endpoint disponibilidad |
| T5.4 | MS-Vehículos | CRUD completo + alerta SOAT |
| T5.5 | Cross-MS | Evento `UsuarioCreadoEvent` propagado |

### Sprint 6 — Backend Grupo A, parte 2 (CRUDs complejos) — ✅ CERRADO

| Tarea | MS | Descripción |
|---|---|---|
| T6.1 | MS-Asignaciones | CRUD asignación tripartita + validación Feign |
| T6.2 | MS-Asignaciones | Reprogramación + historial + eventos |
| T6.3 | MS-Cobros | CRUD facturas + pagos parciales |
| T6.4 | MS-Cobros | Reconciliación + estado de cuenta |
| T6.5 | Cross-MS | Circuit Breakers Resilience4j |

### Sprint 7 — Frontend Grupo A — ✅ CERRADO

| Tarea | Foco | Descripción |
|---|---|---|
| T7.1 | Frontend base | Setup Vue 3 + Vite + TS + PrimeVue + Pinia + interceptors JWT |
| T7.2 | Auth/Config | Login, Forgot/Reset, Configuración, CRUDs Usuarios/Roles |
| T7.3 | Estudiantes | Lista + Form + Detail con tabs |
| T7.4 | Instructores | Lista + Form + Detail + Calendario FullCalendar |
| T7.5 | Vehículos | Lista + Form + Detail + Alertas SOAT |
| T7.6 | Asignaciones | Calendario drag&drop + Wizard tripartita |
| T7.7 | Cobros | Estado de cuenta + Factura + Pago parcial |

### Sprint 8 — Testing Grupo A — ✅ CERRADO

| Tarea | Tipo | Descripción |
|---|---|---|
| T8.1 | Backend coverage | JaCoCo ≥80% en los 6 MS |
| T8.2 | Backend IT | Testcontainers (Postgres + RabbitMQ) — 30+ IT |
| T8.3 | Frontend coverage | Vitest ≥80% |
| T8.4 | E2E Cypress | 5 flujos críticos |
| T8.5 | Bugfixes | Cierre Fase 1 en `docs/fase-1-cierre.md` |

---

## 5. Sprint 9 — Estabilización post-Fase 1 — ✅ CERRADO

> Sprint **no contemplado en el plan original**. Necesario tras la validación funcional E2E del Sprint 8 y para reforzar la plataforma antes de arrancar Grupo B.

**Commits llevan prefijo `Sprint 10 (...)`** por la numeración temprana — son las mismas tareas que se describen acá.

| Tarea | Foco | Descripción |
|---|---|---|
| T9.1 | Pulido Grupo A | Kilometraje E2E, tipos combustible, contratos instructores, StatCards estudiantes, filtros |
| T9.2 | Refactor dominio | Estados estudiante extendidos + `situacion_pago` simplificada (V2-V4 migrations) + auto-transiciones |
| T9.3 | Modelo crédito | Tabla `factura_cuotas` + campos `tipo_pago/numero_cuotas/frecuencia` en facturas |
| T9.4 | Kilometraje + 6 validaciones | Sync cross-MS Feign + 6 validaciones obligatorias al crear asignación (categoría, SOAT, RTV, horario, AUSENCIA) |
| T9.5 | CI/CD nuevos workflows | 3 nuevos workflows: `frontend-ci.yml`, `integration-tests.yml`, `smoke-e2e.yml` con filtros `paths:` |
| T9.6 | Fixes plataforma | V6 bcrypt admin seed + TZ JVM en Dockerfile.spring + Config global 404/400 ProblemDetail |

**Cierre:** PRs #38-#42 mergeados en `main` (commit `de106fa` al 2026-05-26). Documentado en `DECISIONES.md §24` (refactor dominio) y `§25` (estabilización CI/CD y plataforma).

---

## 6. FASE 2 — Grupo B (Sprints 10-12) — ✅ CERRADO

### Sprint 10 — Backend Grupo B — ✅ CERRADO

| Tarea | MS | Descripción | Criterios |
|---|---|---|---|
| T10.1 | MS-Notificaciones | Plantillas configurables CRUD + variables sustituibles + log_envios + endpoint test | ✅ JaCoCo ≥80%, plantillas editables, log poblado |
| T10.2 | MS-Notificaciones | Notificaciones in-app + GET con filtros + PATCH marcar leída + Consumer eventos Grupo A | ✅ Polling devuelve notif <30s tras evento |
| T10.3 | MS-Reportes | Reportes operativos: estudiantes activos, instructores horas, vehículos SOAT vencer, asistencia | ✅ 5 reportes operativos con datos cross-MS via Feign |
| T10.4 | MS-Reportes | Reportes financieros: ingresos por período, saldos, morosidad, recibos | ✅ 4 reportes financieros + KPIs endpoint |
| T10.5 | MS-Reportes | Exportación PDF (Thymeleaf + OpenPDF) + Excel (Apache POI) | ✅ POST `/reportes/exportar?formato=pdf|excel` |
| T10.6 | MS-Reportes | Cache Caffeine con TTL configurable + tabla `ejecuciones_reporte` | ✅ Cache hit segunda llamada, TTL respetado |

### Sprint 11 — Frontend Grupo B — ✅ CERRADO

| Tarea | Foco | Descripción | Estado |
|---|---|---|---|
| T11.1 | Notif Dropdown | `<NotificacionesDropdown />` con badge + polling 30s + marcar leída | ✅ |
| T11.2 | Notif Config | PlantillasEmailView (CRUD + preview), LogEnviosView (histórico, filtros) | ✅ |
| T11.3 | Dashboard | DashboardView con KPIs (Chart.js): estudiantes activos, ingresos mes, clases hoy, SOAT por vencer | ✅ |
| T11.4 | Reportes operativos UI | ReporteEstudiantesView, ReporteInstructoresView, ReporteVehiculosView, ReporteAsistenciaView + export PDF/Excel | ✅ |
| T11.5 | Reportes financieros UI | ReporteIngresosView, ReporteMorosidadView, ReporteRecibosView + charts + export | ✅ |
| T11.6 (extra) | Roles + auth | Vistas por rol (ESTUDIANTE, INSTRUCTOR), password reset, `/estudiantes/me` | ✅ |

### Sprint 12 — Testing Grupo B — ✅ CERRADO

| Tarea | Tipo | Descripción | Estado |
|---|---|---|---|
| T12.1 | Backend coverage | JaCoCo ≥80% en MS-Notificaciones y MS-Reportes | ✅ |
| T12.2 | Backend IT | Testcontainers MS-Notif (RabbitMQ+Postgres+GreenMail SMTP), MS-Reportes (Postgres+Feign mocks) | ✅ |
| T12.3 | Frontend coverage | Vitest en Dashboard, Reportes, Notificaciones | ✅ |
| T12.4 | E2E validado | 43 tests flujo completo + 31 edge cases (evento→email→notif · pago→matrícula · reporte+PDF · plantilla+envío) | ✅ 74/74 pasando |
| T12.5 | Bugfixes | Cierre Fase 2 documentado en `DECISIONES.md §22` | ✅ |

---

## 7. FASE 3 — Cierre (Sprint 13) — ✅ CERRADO

| Tarea | Tipo | Descripción | Estado |
|---|---|---|---|
| T13.1 | E2E cruzado | Flujo completo: login → catálogos → instructor + horario + cert → vehículo → estudiante → factura → pago → asignación → clase (km inicial/final) → sync cross-MS → reportes | ✅ 43 tests |
| T13.2 | Performance | Response time medido: ~25ms (target era <500ms) — 20× mejor que objetivo | ✅ Superado |
| T13.3 | OWASP | Top 10 review + Trivy scan + Gitleaks + CodeQL en CI/CD | ✅ DevSecOps activo |
| T13.4 | CI/CD Production-Ready | GitHub Actions (5 workflows) + Jenkins pipeline + ArgoCD manifests (Kubernetes) | ✅ Multi-plataforma |
| T13.5 | Limpieza repo | Docker consolidado en `infrastructure/docker/`, 13 archivos huérfanos removidos, `README` actualizado | ✅ Cleanup PR #55 |
| T13.6 | Docs final | `README.md` con guía validada, `DECISIONES.md` actualizado, `docs/database/schema.md` completo | ✅ |
| T13.7 | Deploy strategy | Kubernetes + ArgoCD implementado (`kubernetes/` + `.deployment/`). Fallback: docker-compose en VPS | ✅ |
| T13.8 | Setup 1-comando | `cd infrastructure/docker && docker-compose up -d --build` levanta 15 containers en ~4 min. Único requisito: Docker + Git | ✅ Validado en CI |

---

## 8. Dependencias críticas

| Dependencia | Sprint origen | Sprint destino |
|---|---|---|
| `UsuarioCreadoEvent` publicado por MS-Auth | Sprint 4 | Sprint 5 consume en MS-Estudiantes/Instructores |
| CRUD Instructores funcional | Sprint 5 (T5.3) | Sprint 6 (T6.1) MS-Asignaciones lo consulta via Feign |
| Eventos del Grupo A (`asignacion.creada`, `pago.registrado`, etc.) | Sprint 6 | Sprint 10 (T10.2) MS-Notificaciones los consume |
| CRUDs Grupo A funcionales | Sprint 6 | Sprint 7 (UI consume APIs) |
| Frontend base setup | Sprint 7 (T7.1) | Sprint 11 lo extiende |
| Reportes funcionando | Sprint 10 | Sprint 11 (UI consume) |
| Todos los flujos | Sprints 5-12 | Sprint 13 (E2E cruzado) |

---

## 9. Reglas operativas (vigentes desde Sprint 5)

1. **Branching:** `feature/sprint-N-X-descripcion-corta` (ej: `feature/sprint-10-1-notif-plantillas`).
2. **Commits:** formato `Sprint N (Tarea X descripcion)`. 1 commit = 1 tarea finalizada y probada.
3. **PRs:** 1 PR por commit/tarea, squash and merge a `main`. CI verde obligatorio.
4. **Cobertura:** JaCoCo ≥80% obligatorio en cada PR desde Sprint 4.
5. **Validación al cerrar sprint:** suite + smoke manual + reportar en `docs/sprint-N-cierre.md`.
6. **Modelo Claude:** Opus 4.7 high (regla permanente).
7. **Idioma:** español respetando convenciones del lenguaje.
8. **NO programar sin OK explícito del usuario.** Documentos de planificación (.md, .xlsx) sí se pueden generar sin OK.

---

## 10. Cambios respecto a versiones previas

### v1.0 (plan original Sprints 0-12 horizontal)

12 sprints. Cada sprint avanzaba una capa horizontal en TODOS los MS. Documentado en `DECISIONES.md §20`.

### v2.0 (ADR 2026-05-22: vertical por grupos en Sprints 5-12)

Cambió Sprints 5-12 a enfoque vertical por grupos. Documentado en `DECISIONES.md §23`.

### v3.0 (2026-05-26: 13 sprints + Sprint 9 Estabilización)

- Agregado Sprint 9 (Estabilización) que no estaba en planes previos.
- Renumerado: Backend Grupo B pasó de Sprint 9 a Sprint 10. Frontend B: 11. Testing B: 12. Cierre: 13.
- Los commits del Sprint 9 llevan prefijo `Sprint 10` por la numeración temprana (divergencia documentada).
- Total: 13 sprints + Sprint 0 setup.

### v4.0 (este documento, 2026-07-17: cierre final)

- Todos los sprints (0-13) marcados como CERRADO.
- Actualizado Sprint 13: Kubernetes + ArgoCD implementado, Docker consolidado en `infrastructure/docker/`, DevSecOps activo (Trivy + Gitleaks + CodeQL).
- Métricas finales agregadas: 357/357 tests OK, 15 containers healthy, setup en 1 comando validado en CI (Smoke E2E).
- Referencias a `docs/fase-N-cierre.md` removidas: la trazabilidad final vive en `DECISIONES.md §22 "Estado final del proyecto"`.

---

## 11. Referencias

- `DECISIONES.md` — Decisiones técnicas (§23 ADR vertical por grupos, §24 ADR Refactor dominio, §25 ADR Estabilización)
- `SPRINTS_PLAN.xlsx` — Plan tabular detallado (hojas: Overview, Sprints Detallado, Estado Actual)
- `CLAUDE.md` — Contexto general
- `docs/database/schema.md` — Modelo de BD detallado
- `docs/fase-1-cierre.md` — (a generar al cierre del Sprint 8 si no existe)
- `docs/fase-2-cierre.md` — (a generar al cierre del Sprint 12)

---

*Documento generado: 2026-05-22 (v1.0)*
*Actualizado: 2026-05-26 (v3.0 — replanteo final con 13 sprints + Sprint 9 Estabilización)*
*Cierre final: 2026-07-17 (v4.0 — proyecto COMPLETADO, todos los sprints CERRADOS, 357/357 tests OK)*
