# 📋 ÍNDICE MAESTRO — Punto de Entrada para Claude Code

**Este archivo es el punto de entrada.** Claude lo lee automáticamente al iniciar sesión.

---

## 🚀 Inicio Rápido

Para continuar trabajando, simplemente di:

```
Continuemos con Sprint 12 — MS-Notificaciones + MS-Reportes
```

Claude cargará todo el contexto automáticamente.

---

## 📂 Estructura del Proyecto

### **DOCUMENTACIÓN CRÍTICA** (leer en orden)

1. **`DECISIONES.md`** ⭐⭐⭐
   - **Qué es:** Fuente de verdad de todas las decisiones técnicas (32 decisiones)
   - **Por qué:** Define arquitectura, restricciones, convenciones
   - **Secciones clave:** §1-§10 (arquitectura), §15 (RabbitMQ), §23 (Notificaciones)
   - **Ubicación:** `/DECISIONES.md`

2. **`CLAUDE.md`** ⭐⭐⭐
   - **Qué es:** Guía operativa del proyecto
   - **Por qué:** Define stack, convenciones, testing, seguridad
   - **Cuándo leer:** Antes de codificar
   - **Ubicación:** `/CLAUDE.md`

3. **`PLAN_FASES.md`** ⭐⭐
   - **Qué es:** Plan vigente de Sprints 5-12 (desarrollo horizontal)
   - **Por qué:** Entiende qué falta, qué está en progress
   - **Sprint actual:** Sprint 12 (MS-Notificaciones + MS-Reportes)
   - **Ubicación:** `/PLAN_FASES.md`

4. **`README.md`**
   - **Qué es:** Descripción general del proyecto
   - **Cuándo leer:** Primero, para contexto
   - **Ubicación:** `/README.md`

5. **`docs/database/schema.md`**
   - **Qué es:** Diseño completo de BD (38 tablas, 9 schemas)
   - **Por qué:** Necesitás entender estructura de datos
   - **Ubicación:** `/docs/database/schema.md`

---

### **ONBOARDING DE SEBASTIÁN** (Sprint 12)

📦 **TODO ESTÁ AQUÍ:** Carpeta `ONBOARDING-SEBASTIAN-2026-07-07/`

```
ONBOARDING-SEBASTIAN-2026-07-07/
├── 1_INICIO_RAPIDO/
│   └── INICIO_RAPIDO_SEBASTIAN.md          (5 min - setup)
│
├── 2_GUIA_COMPLETA/
│   └── ONBOARDING_SEBASTIAN.md             (2-3 horas - guía detallada)
│
├── 3_REFERENCIAS_RAPIDAS/
│   ├── PLAN_SPRINT_12.txt                  (tareas de Sebastián)
│   ├── CONVENCION_COMMITS.txt              (git workflow)
│   ├── URLS_UTILES.txt                     (localhost URLs)
│   ├── DECISIONES_RESUMEN.txt              (decisiones clave)
│   └── DOCKER_COMPOSE_INFO.txt             (Docker explicado)
│
├── 4_CONFIGURACION/
│   └── .env.template                       (referencia variables)
│
└── 5_CHECKLIST/
    └── VALIDACION_POST_SETUP.md            (validar post-setup)
```

**ZIP comprimido:** `ONBOARDING-SEBASTIAN-2026-07-07.zip` (ya enviado a Sebastián)

**Guías para Hernán:**
- `GUIA_ENTREGA_PARA_HERNAN.md` — Instrucciones detalladas
- `ENTREGA_A_SEBASTIAN.md` — Checklist de entrega
- `ACCION_INMEDIATA_HERNAN.txt` — Resumen ejecutivo

---

## 🎯 Estado Actual del Proyecto

| Sprint | Fase | Tema | Estado |
|--------|------|------|--------|
| **Sprint 0-11** | Completo | Auth, Estudiantes, Instructores, Vehículos, Asignaciones, Cobros | ✅ TERMINADO |
| **Sprint 12** | EN CURSO | **MS-Notificaciones + MS-Reportes** | 🟡 INICIANDO |
| Sprint 13 | Planificado | Frontend Grupo B + E2E + Deploy v1.0.0 | 📋 PRÓXIMO |

**Sprint 12 = MS-Notificaciones + MS-Reportes (Backend)**

---

## 🏗️ Microservicios (8 total)

| MS | Puerto | Estado | Responsable (Sprint 12) |
|----|--------|--------|---|
| MS-Auth | 8081 | ✅ Completo | — |
| MS-Estudiantes | 8082 | ✅ Completo | — |
| MS-Instructores | 8083 | ✅ Completo | — |
| MS-Vehículos | 8084 | ✅ Completo | — |
| MS-Asignaciones | 8085 | ✅ Completo | — |
| MS-Cobros | 8086 | ✅ Completo | — |
| **MS-Notificaciones** | 8088 | 🟡 EN PROGRESO | Sebastián |
| **MS-Reportes** | 8087 | 🟡 EN PROGRESO | Sebastián |

---

## 📋 Tareas de Sprint 12 (Sebastián)

### **Tarea 1: MS-Notificaciones**
- CRUD Plantillas de Email
- Listeners RabbitMQ (eventos de dominio)
- In-App Notifications (Backend)
- Tests: 80%+ cobertura

**Archivo de referencia:** `ONBOARDING-SEBASTIAN-2026-07-07/3_REFERENCIAS_RAPIDAS/PLAN_SPRINT_12.txt`

### **Tarea 2: MS-Reportes**
- Reportes Operacionales
- Reportes Financieros
- Export PDF/Excel
- Dashboard KPIs (prep backend)
- Tests: 75%+ cobertura

---

## 🔧 Stack Tecnológico

```
Java 21 + Spring Boot 3.4 + Spring Cloud 2024.0.0
PostgreSQL 15 (9 schemas)
RabbitMQ 3.12
Caffeine Cache
MapStruct 1.6
Apache POI + iText (Excel/PDF)
JUnit 5 + Mockito
Docker Compose (14 contenedores)
```

**Más detalles:** Ver `DECISIONES.md` §2-§10

---

## 🚀 Cómo Comenzar un Sprint

### **Para Claude (esta IA):**

Simplemente di:
```
Continuemos con Sprint 12 — MS-Notificaciones + MS-Reportes
```

Claude va a:
1. Leer este INDEX.md
2. Consultar DECISIONES.md + CLAUDE.md + PLAN_FASES.md
3. Entender el contexto completo
4. Continuar donde se quedó

**NO necesitas poner contexto manualmente.**

### **Para Sebastián (developer):**

1. Lee: `ONBOARDING-SEBASTIAN-2026-07-07/1_INICIO_RAPIDO/INICIO_RAPIDO_SEBASTIAN.md`
2. Levanta: Docker + frontend
3. Lee: `ONBOARDING-SEBASTIAN-2026-07-07/2_GUIA_COMPLETA/ONBOARDING_SEBASTIAN.md`
4. Consulta: Archivos en `3_REFERENCIAS_RAPIDAS/` cuando necesites
5. Valida: Checklist en `5_CHECKLIST/`

---

## 📂 Rutas Importantes

```
Backend Microservices:
  └─ /backend/ms-notificaciones/        ← Desarrollar aquí (Sprint 12)
  └─ /backend/ms-reportes/              ← Desarrollar aquí (Sprint 12)

Frontend:
  └─ /frontend/                         ← Sprint 13 (no tocar aún)

Database:
  └─ /backend/ms-*/src/main/resources/db/migration/V*.sql   ← Migraciones

Tests:
  └─ /backend/ms-*/src/test/java/       ← Tests (80%+ cobertura)

Docker:
  └─ /infrastructure/docker/docker-compose.yml  ← Levanta 14 contenedores
  └─ /infrastructure/docker/Dockerfile.spring   ← Build Spring Boot
```

---

## 🎯 Convenciones Clave

### **Git Commits**
```
Sprint 12 (Tarea X - Descripción corta)
```
Ejemplo:
```
Sprint 12 (Tarea 1 - CRUD Plantillas Email)
Sprint 12 (Tarea 1.2 - Service PlantillaEmail)
Sprint 12 (Tarea 2 - Reportes Operacionales)
```

### **Branch Naming**
```
feature/sprint-12-X-descripcion-corta
```
Ejemplo:
```
feature/sprint-12-1-plantillas-crud
feature/sprint-12-2-reportes-operacionales
```

### **Paquetes Java**
```
com.escuela.notificaciones.controller
com.escuela.notificaciones.service
com.escuela.notificaciones.repository
com.escuela.notificaciones.entity
com.escuela.notificaciones.dto
```

Más detalles en: `CLAUDE.md` (Code Conventions)

---

## 🔗 URLs Localhost (Dev)

```
Frontend:           http://localhost:5173
API Gateway:        http://localhost:8080
Eureka:             http://localhost:8761
RabbitMQ Console:   http://localhost:15672 (guest/guest)
PostgreSQL (Adminer): http://localhost:8888
MinIO Console:      http://localhost:9001 (minioadmin/minioadmin123)

Swagger MS-Notificaciones: http://localhost:8088/swagger-ui.html
Swagger MS-Reportes:       http://localhost:8087/swagger-ui.html
```

**Archivo completo:** `ONBOARDING-SEBASTIAN-2026-07-07/3_REFERENCIAS_RAPIDAS/URLS_UTILES.txt`

---

## 📚 Referencias Rápidas

| Necesitás | Dónde está |
|-----------|-----------|
| Decisiones técnicas | `DECISIONES.md` |
| Stack y convenciones | `CLAUDE.md` |
| Plan de sprints | `PLAN_FASES.md` |
| Diseño BD | `docs/database/schema.md` |
| Git workflow | `ONBOARDING-SEBASTIAN-2026-07-07/3_REFERENCIAS_RAPIDAS/CONVENCION_COMMITS.txt` |
| Tareas de Sprint 12 | `ONBOARDING-SEBASTIAN-2026-07-07/3_REFERENCIAS_RAPIDAS/PLAN_SPRINT_12.txt` |
| URLs localhost | `ONBOARDING-SEBASTIAN-2026-07-07/3_REFERENCIAS_RAPIDAS/URLS_UTILES.txt` |
| Docker explicado | `ONBOARDING-SEBASTIAN-2026-07-07/3_REFERENCIAS_RAPIDAS/DOCKER_COMPOSE_INFO.txt` |

---

## ✅ Estado de Entrega

| Aspecto | Estado |
|--------|--------|
| **ZIP Onboarding** | ✅ Creado y enviado a Sebastián |
| **.env** | ✅ Pasado por vía segura |
| **Documentación** | ✅ Completa y organizada |
| **Código del proyecto** | ✅ Sin cambios, limpio |
| **Git** | ✅ Listo para commitear |
| **Sprint 12** | 🟡 Listo para comenzar |

---

## 🎯 Próximos Pasos

### **Hoy (Inmediato):**
```bash
git add .
git commit -m "Docs: Onboarding kit para Sebastián + estado Sprint 12"
git push origin main
```

### **Próximo Sprint:**
Sebastián comienza MS-Notificaciones + MS-Reportes

---

## 📞 Contacto / Dudas

- **Código / Arquitectura:** Revisar `DECISIONES.md` + `CLAUDE.md`
- **Migraciones BD:** Ver ejemplos en `/backend/ms-*/src/main/resources/db/migration/`
- **Testing:** Revisar `CLAUDE.md` (Testing Strategy)
- **Git Issues:** Ver `.github/CONTRIBUTING.md`

---

## 🎉 ¡LISTO PARA SPRINT 12!

**El proyecto está 100% documentado, organizado y listo.**

Para continuar, simplemente di:

```
Continuemos con Sprint 12
```

Claude Code cargará TODO el contexto automáticamente. ✅

---

**Última actualización:** 2026-07-07  
**Sprint actual:** 12 (MS-Notificaciones + MS-Reportes)  
**Status:** ✅ Documentado y Listo  
**Próximo:** Sprint 13 (Frontend Grupo B + Deploy v1.0.0)
