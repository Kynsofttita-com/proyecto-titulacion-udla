# Arquitectura del Sistema — Escuela de Conducir

> Estado: **producción** (Oracle Cloud Free, `http://160.34.220.63/`)
> Última actualización: 2026-07-20

Este documento describe la arquitectura desplegada: qué corre, en qué máquina, cómo se hablan los componentes, y qué reglas se cumplen. Los diagramas están en Mermaid y se renderizan directamente en GitHub / VS Code.

---

## 1. Vista general (deployment + comunicaciones)

```mermaid
graph TB
    subgraph Cliente["Cliente"]
        USR[Usuario Web<br/>Chrome / Firefox]
    end

    subgraph Oracle["Oracle Cloud Free Tier · VM.A1.Flex 4 OCPU / 24GB · 160.34.220.63"]

        subgraph Frontend["Capa Presentación"]
            FE[Frontend Vue 3 SPA<br/>nginx :80]
        end

        subgraph Gateway["API Gateway"]
            GW[Spring Cloud Gateway :8080<br/>JWT HS512 + CORS + Routing]
        end

        subgraph Discovery["Service Discovery"]
            EU[Eureka Server :8761]
        end

        subgraph MS["8 Microservicios · Spring Boot 3.4 · Java 21"]
            AU[ms-auth :8081]
            ES[ms-estudiantes :8082]
            IN[ms-instructores :8083]
            VE[ms-vehiculos :8084]
            AS[ms-asignaciones :8085]
            CO[ms-cobros :8086]
            RE[ms-reportes :8087]
            NO[ms-notificaciones :8088]
        end

        subgraph Data["Persistencia + Mensajería"]
            PG[(PostgreSQL 15 :5432<br/>1 instancia · 9 schemas)]
            RB[RabbitMQ 3.12<br/>:5672 / :15672]
            MI[MinIO :9000/:9001<br/>Storage archivos]
        end

        subgraph Obs["Observabilidad"]
            PR[Prometheus :9090<br/>13 targets · retention 15d]
            GR[Grafana :3030<br/>3 dashboards]
            CA[cAdvisor + Node-Exporter]
        end
    end

    USR -->|HTTPS| FE
    FE -->|/api/**| GW
    GW -->|routing por prefijo| AU
    GW --> ES
    GW --> IN
    GW --> VE
    GW --> AS
    GW --> CO
    GW --> RE
    GW --> NO

    AU -.registra.-> EU
    ES -.-> EU
    IN -.-> EU
    VE -.-> EU
    AS -.-> EU
    CO -.-> EU
    RE -.-> EU
    NO -.-> EU

    AU --> PG
    ES --> PG
    IN --> PG
    VE --> PG
    AS --> PG
    CO --> PG
    RE --> PG
    NO --> PG

    ES --> MI
    VE --> MI

    VE -.eventos SOAT.-> RB
    IN -.eventos licencia.-> RB
    CO -.eventos pago.-> RB
    RB -.consume eventos.-> NO

    AU -.metrics.-> PR
    ES -.-> PR
    IN -.-> PR
    VE -.-> PR
    AS -.-> PR
    CO -.-> PR
    RE -.-> PR
    NO -.-> PR
    CA -.-> PR
    PR --> GR

    style FE fill:#42b883,color:#fff
    style GW fill:#6db33f,color:#fff
    style EU fill:#6db33f,color:#fff
    style PG fill:#336791,color:#fff
    style RB fill:#ff6600,color:#fff
    style MI fill:#c72c48,color:#fff
    style PR fill:#e6522c,color:#fff
    style GR fill:#f46800,color:#fff
```

---

## 2. Sync cross-MS síncrono (Feign)

Cada flecha es una llamada HTTP interna con `X-User-Email/Roles/Id` propagado por interceptor (`FeignConfig`). Cuando no hay request HTTP padre (schedulers/eventos) se usa identidad `system@escuela.local` con roles `SYSTEM,ADMIN`.

```mermaid
graph LR
    AS[ms-asignaciones]
    VE[ms-vehiculos]
    ES[ms-estudiantes]
    IN[ms-instructores]
    RE[ms-reportes]
    CO[ms-cobros]

    AS -->|km_final al finalizar clase| VE
    AS -->|+minutos completados| ES
    AS -->|valida disponibilidad + licencia| IN
    AS -->|valida SOAT/RTV/categoría| VE

    IN -->|horas cumplidas del mes| AS

    RE -->|listar / detalle| ES
    RE -->|listar / detalle| IN
    RE -->|listar / detalle| VE
    RE -->|listar cobros + morosidad| CO
    RE -->|paginado + horas x instructor| AS

    CO -->|nombre + email del estudiante| ES
```

---

## 3. Eventos asíncronos (RabbitMQ)

Todo listener persiste el `eventId` en `IdempotencyStore` antes de procesarlo — así se evita ejecutar el mismo evento dos veces si el broker re-entrega.

```mermaid
graph LR
    VE[ms-vehiculos] -->|SoatVencimientoProximoEvent| EX{Exchange<br/>vehiculos.events}
    IN[ms-instructores] -->|LicenciaVencimientoProximoEvent| EX2{Exchange<br/>instructores.events}
    CO[ms-cobros] -->|PagoAtrasadoEvent| EX3{Exchange<br/>cobros.events}
    AS[ms-asignaciones] -->|AsignacionCreada / Reprogramada / Cancelada| EX4{Exchange<br/>asignaciones.events}

    EX --> Q1[notificaciones.operativos]
    EX2 --> Q1
    EX3 --> Q2[notificaciones.financieros]
    EX4 --> Q3[notificaciones.asignaciones]

    Q1 --> NO[ms-notificaciones<br/>+ IdempotencyStore]
    Q2 --> NO
    Q3 --> NO

    NO -->|SMTP| MT[Mailtrap dev / Gmail prod]
    NO --> DB[(notificaciones_schema<br/>persistencia + audit)]
```

---

## 4. Stack técnico

| Capa | Tecnología |
|---|---|
| **Frontend** | Vue 3 + Vite + PrimeVue + TailwindCSS + Pinia + TypeScript |
| **Gateway** | Spring Cloud Gateway 4.x, filtro JWT |
| **Discovery** | Netflix Eureka Server |
| **Backend** | Java 21, Spring Boot 3.4, Spring Data JPA, MapStruct, Feign |
| **Auth** | JWT HS512 (clave 512 bits), HttpOnly cookies, bcrypt |
| **BD** | PostgreSQL 15, 1 instancia, 9 schemas, Flyway |
| **Mensajería** | RabbitMQ 3.12, direct exchanges por dominio, idempotency store |
| **Storage** | MinIO (S3-compatible) para docs de estudiantes y vehículos |
| **Cache** | Caffeine in-memory (por servicio, no distribuido) |
| **Monitoreo** | Prometheus + Micrometer + Grafana + cAdvisor + Node Exporter |
| **Contenedores** | Docker + Docker Compose (K8s planificado para v2) |
| **CI** | GitHub Actions (workflows: backend-ci, frontend-ci, docker-build, integration-tests, smoke-e2e) |
| **Deploy prod** | Oracle Cloud Free, ARM Ampere A1.Flex, Ubuntu 22.04 |

---

## 5. Los 9 schemas de PostgreSQL

Modelo consolidado: **1 instancia PostgreSQL con 9 schemas separados** (uno por microservicio + `shared_schema`). Ver `DECISIONES.md §4.1` para el ADR completo.

| Schema | Dueño | Tablas principales |
|---|---|---|
| `auth_schema` | ms-auth | usuarios, roles, permisos, sesiones, configuracion_escuela, plantillas_email |
| `estudiantes_schema` | ms-estudiantes | estudiantes, documentos, progreso_academico, tipos_curso |
| `instructores_schema` | ms-instructores | instructores, horarios_trabajo, excepciones (ausencias/extra) |
| `vehiculos_schema` | ms-vehiculos | vehiculos, mantenimientos, inspecciones, registros_combustible, categorias_licencia, tipos_combustible |
| `asignaciones_schema` | ms-asignaciones | asignaciones, historial_estados |
| `cobros_schema` | ms-cobros | facturas, factura_cuotas, pagos, plantillas_recibo |
| `reportes_schema` | ms-reportes | ejecuciones_reporte, cache_reporte |
| `notificaciones_schema` | ms-notificaciones | notificaciones, idempotency_store |
| `shared_schema` | compartido | catálogos comunes |

---

## 6. Reglas arquitectónicas clave

- **No hay FK cross-schema** — cada MS solo lee/escribe su propio schema. Las referencias son por ID lógico (`instructor_id`, `vehiculo_id`, etc.).
- **Auth centralizado en el Gateway** — el Gateway valida el JWT y propaga `X-User-Email/Roles/Id` a los MS. Los MS no validan JWT por sí mismos; solo confían en esos headers y los verifican con `AuthHeaderGuard`.
- **Feign entre MS lleva headers de identidad** propagados vía `RequestInterceptor` (para llamadas encadenadas). Cuando no hay request padre (schedulers) se usa identidad `SYSTEM`.
- **Idempotencia async** — todo listener de RabbitMQ pasa por `IdempotencyStore` (evita procesar el mismo `eventId` dos veces).
- **Sync de km al finalizar clase** — `ms-asignaciones.finalizar()` propaga `km_final` a `ms-vehiculos.kilometraje` vía Feign.
- **Propagación SOAT/RTV** — al registrar/actualizar una inspección `SOAT` o `TECNICA` `APROBADA/CONDICIONADA` en `ms-vehiculos.InspeccionService`, la `proximaInspeccion` se propaga al campo del vehículo si es posterior a la actual.
- **Circuit-breaker + retry** para llamadas Feign críticas (via Resilience4j en algunos MS).
- **Single-tenant configurable** — cada escuela = 1 deploy independiente. La `configuracion_escuela` va en `auth_schema`.

---

## 7. Puertos y URLs públicas (producción)

| Servicio | URL | Auth |
|---|---|---|
| Frontend | `http://160.34.220.63/` | admin@escuela.local / Admin123! |
| API Gateway | `http://160.34.220.63/api/**` | JWT en cookie |
| Actuator health | `http://160.34.220.63/api/actuator/health` | JWT |
| Grafana | `http://160.34.220.63:3030/` | admin / admin123 |
| Prometheus | `http://160.34.220.63:9090/` | — |
| Eureka Dashboard | `http://160.34.220.63:8761/` | — |
| MinIO Console | `http://160.34.220.63:9001/` | minioadmin / minioadmin123 |
| RabbitMQ Management | `http://160.34.220.63:15672/` | guest / guest |

> ⚠️ Credenciales por defecto en RabbitMQ/MinIO/Grafana — cambiar antes de exponer al público real.

---

## 8. Referencias cruzadas

- **Decisiones técnicas** completas: `DECISIONES.md` (ADRs consolidados).
- **Plan de sprints**: `PLAN_FASES.md`.
- **Modelo de datos detallado**: `docs/database/schema.md` (monolítico) + `docs/database/secciones/*.md` (por dominio).
- **ER diagrama importable a dbdiagram.io**: `docs/database/er-diagram.dbml`.
- **Runbook operativo** (en la VM): `~/RUNBOOK.md`.
- **Guía de defensa**: `docs/GUIA_DEFENSA.md` (Q&A, screenshots, arquitectura resumida).
