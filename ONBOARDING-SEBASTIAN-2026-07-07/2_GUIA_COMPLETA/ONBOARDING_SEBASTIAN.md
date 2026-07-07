# 🚀 Guía Completa de Onboarding — Sebastián

**Última actualización:** 2026-07-07  
**Responsable:** Hernán Mateo Jurado Moran  
**Estado actual:** Sprint 11 completado en `main` (commit: `ac452b7`). Sprint 12 por comenzar.

---

## 📋 Indice

1. [Requisitos previos](#requisitos-previos)
2. [Clonar el repositorio](#clonar-el-repositorio)
3. [Variables de entorno](#variables-de-entorno)
4. [Levantar el sistema](#levantar-el-sistema)
5. [Documentos críticos a leer](#documentos-críticos-a-leer)
6. [Credenciales y servicios compartidos](#credenciales-y-servicios-compartidos)
7. [Estado actual del proyecto](#estado-actual-del-proyecto)
8. [Convenciones y workflow](#convenciones-y-workflow)
9. [Cómo trabajar con Claude Code](#cómo-trabajar-con-claude-code)
10. [Troubleshooting rápido](#troubleshooting-rápido)

---

## Requisitos previos

Antes de comenzar, asegurate de tener instalado:

### Windows (recomendado: Windows Terminal + WSL2)

- **Java 21 JDK** ([openjdk.org](https://openjdk.org/projects/jdk/21/))
  ```bash
  java -version  # Debe decir "21.x.x"
  ```
  
- **Maven 3.8+** ([maven.apache.org](https://maven.apache.org/))
  ```bash
  mvn -v  # Debe decir "3.8.x" o superior
  ```
  
- **Node.js 18+** (recomendado 20 LTS) ([nodejs.org](https://nodejs.org/))
  ```bash
  node --version  # Debe decir "v18.x.x" o "v20.x.x"
  npm --version   # Debe decir "9.x.x" o superior
  ```
  
- **Docker Desktop** ([docker.com](https://www.docker.com/products/docker-desktop/))
  - Incluye Docker Engine + Docker Compose
  - Verificar: `docker --version` y `docker compose version`
  
- **Git** ([git-scm.com](https://git-scm.com/))
  - Verificar: `git --version`

- **Claude Code CLI** (opcional pero recomendado)
  - Instalar: `npm install -g claude` ([claudecode.com](https://claude.ai/code))

**✅ Checklist rápido:**
```bash
java -version && mvn -v && node --version && docker --version && git --version
# Todos deben imprimir versiones válidas
```

---

## Clonar el repositorio

### Paso 1: Crear una carpeta de trabajo

```bash
# En Windows, dentro de una terminal WSL2 o Git Bash
cd ~
mkdir -p TARIUS-DESARROLLO  # (o donde guardes tus proyectos)
cd TARIUS-DESARROLLO
```

### Paso 2: Clonar el repo

```bash
git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git
cd proyecto-titulacion-udla
```

### Paso 3: Verificar que clonaste correctamente

```bash
ls -la
# Debe ver: CLAUDE.md, DECISIONES.md, SPRINTS_PLAN.xlsx, backend/, frontend/, infrastructure/, docs/

git log --oneline | head -5
# Debe ver commits recientes (ej. "ac452b7 Sprint 11...")
```

---

## Variables de entorno

### Paso 1: Copiar archivo `.env.example` a `.env`

```bash
# Desde la raíz del proyecto
cp .env.example .env
```

### Paso 2: Llenar las variables (Hernán te pasará el `.env` funcional)

Abre `.env` en tu editor y completa los valores que Hernán te dé. **IMPORTANTE:**

- ⚠️ **NUNCA commitees `.env`** — está en `.gitignore` por seguridad
- Las variables clave son:
  - `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`
  - `POSTGRES_INITDB_ARGS` (si aplica)
  - `JWT_SECRET` (clave simétrica HS512)
  - `MAILTRAP_USER`, `MAILTRAP_PASSWORD` (para emails dev)
  - `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`

**Ejemplo de `.env` funcional** (Hernán lo compartirá):
```bash
# PostgreSQL
POSTGRES_USER=escuela_user
POSTGRES_PASSWORD=<contraseña-segura>
POSTGRES_DB=escuela_db
POSTGRES_INITDB_ARGS=-c shared_preload_libraries=pg_stat_statements

# JWT (copia el valor que Hernán te dé)
JWT_SECRET=<clave-512-bits-base64>

# Mailtrap (dev)
MAILTRAP_USER=<usuario>
MAILTRAP_PASSWORD=<contraseña>

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin123

# RabbitMQ
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# Timezone (IMPORTANTE — no cambiar)
TZ=America/Guayaquil
```

**Verifica que el archivo quedó bien:**
```bash
grep -E "POSTGRES_USER|JWT_SECRET|MAILTRAP" .env
# Debe imprimir tus variables rellenadas (sin `<...>` vacíos)
```

---

## Levantar el sistema

### Opción A: **Recomendado — Stack completo en Docker** 🐳

```bash
# 1. Levantar TODOS los contenedores (Backend + Frontend infraestructura)
docker compose -f infrastructure/docker/docker-compose.yml up -d

# 2. Esperar ~50-60s la primera vez (healthchecks)
docker compose -f infrastructure/docker/docker-compose.yml ps
# Esperar a que todos digan "healthy"

# 3. Verificar que Eureka registró 9 servicios
curl -s http://localhost:8761/eureka/apps \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'Servicios: {len(d[\"applications\"][\"application\"])}')"
# Debe imprimir: Servicios: 9

# 4. Instalar deps del frontend
cd frontend
npm install

# 5. Copiar .env del frontend (si no existe)
cp .env.example .env
# (default apunta a http://localhost:8080, OK para local)

# 6. Levantar frontend Vue (dev server)
npm run dev
# Debe imprimir: "VITE vX.X.X ready in XXXms — Local: http://localhost:5173/"
```

**¡Listo!** Abrí **http://localhost:5173** y entrá con:
```
Email:    admin@escuela.local
Password: Admin123!
```

### Opción B: **Solo infraestructura + backend en IDE**

Si querés debuggear desde IntelliJ/Eclipse (más lento pero útil para desarrollo):

```bash
# 1. Levantar solo Postgres + RabbitMQ + MinIO + Adminer
docker compose -f infrastructure/docker/docker-compose.infra.yml up -d

# 2. Compilar todos los módulos backend
cd backend
mvn clean install -DskipTests

# 3. Arrancar Eureka + Gateway + cada MS (en terminales separadas)
# Terminal 1:
cd eureka-server
mvn spring-boot:run

# Terminal 2:
cd api-gateway
mvn spring-boot:run

# Terminal 3+:
cd ms-auth && mvn spring-boot:run
# (y repetir para ms-estudiantes, ms-instructores, etc.)

# Terminal final: frontend
cd frontend
npm install && npm run dev
```

### Verificar que todo está vivo

| Servicio | URL | Cómo verificar |
|----------|-----|---|
| 🎯 **Frontend** | http://localhost:5173 | Abrí el navegador, debe cargar |
| API Gateway | http://localhost:8080 | `curl http://localhost:8080/actuator/health` |
| Eureka | http://localhost:8761 | Abrí el navegador, debe mostrar 9 apps |
| RabbitMQ | http://localhost:15672 | Usuario: `guest`, Contraseña: `guest` |
| MinIO | http://localhost:9001 | Usuario: `minioadmin`, Contraseña: `minioadmin123` |
| Adminer (BD) | http://localhost:8888 | Acceso a PostgreSQL visual |

---

## Documentos críticos a leer

**Orden de lectura (clave para entender el proyecto):**

### 📘 1. **DECISIONES.md** (FUENTE DE VERDAD)
- **Qué es:** Todas las decisiones técnicas del proyecto (32 decisiones)
- **Por qué:** Define la arquitectura, las restricciones y las convenciones
- **Acción:** Lee §1-§10 (decisiones de infra + arquitectura) PRIMERO
- **Ubicación:** Raíz del proyecto (`/DECISIONES.md`)

### 📗 2. **PLAN_FASES.md**
- **Qué es:** Plan vigente de Sprints 5-12 (todos en desarrollo horizontal)
- **Por qué:** Entiende qué falta, qué está en progress, qué se vuelve prioritario
- **Acción:** Lee la sección Sprint 12 completa
- **Ubicación:** Raíz del proyecto (`/PLAN_FASES.md`)

### 📙 3. **CLAUDE.md** (esta carpeta)
- **Qué es:** Guía operativa para usar Claude Code en el proyecto
- **Por qué:** Define convenciones, stack, directorios, testing, seguridad
- **Acción:** Léelo completo — es tu biblia mientras codifiques
- **Ubicación:** Raíz del proyecto (`/CLAUDE.md`)

### 📊 4. **SPRINTS_PLAN.xlsx**
- **Qué es:** Plan original de 12 sprints (referencia histórica)
- **Por qué:** Contexto histórico; la versión vigente es PLAN_FASES.md
- **Acción:** Consulta si necesitás fechas o criterios de aceptación detallados
- **Ubicación:** Raíz del proyecto (`/SPRINTS_PLAN.xlsx`)

### 🗄️ 5. **docs/database/schema.md**
- **Qué es:** Diseño completo de la BD (38 tablas, 9 schemas, ER diagrams)
- **Por qué:** Necesitás entender la estructura de datos
- **Acción:** Lee las primeras 3 secciones (schemas, tablas principales)
- **Ubicación:** `docs/database/schema.md`

### 🤝 6. **.github/CONTRIBUTING.md**
- **Qué es:** GitHub Flow, convenciones de commits, PR workflow
- **Por qué:** Define cómo trabajamos juntos
- **Acción:** Memoriza el formato de commits (`Sprint N (Tarea X)`)
- **Ubicación:** `.github/CONTRIBUTING.md`

### 🐳 7. **infrastructure/docker/README.md**
- **Qué es:** Detalle de Docker Compose (qué container, puertos, env vars)
- **Por qué:** Entender cómo se montan los servicios
- **Acción:** Úsalo como referencia si hay problemas de conexión
- **Ubicación:** `infrastructure/docker/README.md`

### 🔧 8. **backend/README.md**
- **Qué es:** Cómo levantar el backend desde IDE, comandos Maven útiles
- **Por qué:** Desarrollo local más eficiente
- **Acción:** Guárdalo en bookmarks
- **Ubicación:** `backend/README.md`

### 📄 9. **Este archivo (ONBOARDING_SEBASTIAN.md)**
- **Qué es:** Tu entrada al proyecto
- **Acción:** Ya lo estás leyendo 😄

---

## Credenciales y servicios compartidos

### 🔑 Credenciales que Hernán debe compartirte

Estas NO están en git. Hernán las pasará de otra forma (email, 1Password, etc.):

| Servicio | Usuario | Contraseña | Ubicación en `.env` |
|----------|---------|------------|-----|
| PostgreSQL | `escuela_user` | `<POSTGRES_PASSWORD>` | `POSTGRES_PASSWORD` |
| JWT Secret | — | `<JWT_SECRET>` (512 bits base64) | `JWT_SECRET` |
| Mailtrap (dev email) | `<MAILTRAP_USER>` | `<MAILTRAP_PASSWORD>` | `MAILTRAP_USER`, `MAILTRAP_PASSWORD` |
| MinIO (S3 local) | `minioadmin` | `minioadmin123` | `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD` |
| RabbitMQ | `guest` | `guest` | Valores por defecto |

### 🌐 Servicios externos (producción — NO tocar)

- **Oracle Cloud Free Tier** — Deploy producción (Hernán solo)
- **GitHub Actions** — CI/CD automático (configurado)
- **Jira** — Tracking de sprints (Hernán + tutor)
- **Gmail SMTP** — Email producción (credencial separada, Hernán solo)

### ✅ Servicios locales (siempre activos en Docker)

- **PostgreSQL 15** — BD local (`localhost:5432`)
- **RabbitMQ 3.12** — Message broker (`localhost:5672`, mgmt: `localhost:15672`)
- **MinIO 8.5** — Object storage (`localhost:9000`, console: `localhost:9001`)
- **Eureka** — Service discovery (`localhost:8761`)
- **API Gateway** — Entry point (`localhost:8080`)
- **8 Microservicios** — Puertos 8081-8088

---

## Estado actual del proyecto

### ✅ Completado (Sprints 1-11)

| Componente | Sprint | Estado |
|-----------|--------|--------|
| Infraestructura (Eureka, Gateway, RabbitMQ) | 1-4 | ✅ Completo |
| Backend Grupo A (6 MS) | 5-6 | ✅ Completo |
| Frontend Grupo A | 7-8 | ✅ Completo |
| Testing Grupo A | 8 | ✅ Completo (80%+ cobertura) |
| Estabilización (kilometraje, contratos, CI/CD) | 10 | ✅ Completo |
| Vistas por rol + password forzado + progreso académico | 11 | ✅ Completo |

### 🟡 En progreso o próximo

| Tarea | Sprint | Estado |
|-------|--------|--------|
| **Backend Grupo B: MS-Notificaciones + MS-Reportes** | 12 | 🟡 Comienza aquí |
| Frontend Grupo B (KPIs, reportes UI) | 12-13 | 📋 Planificado |
| E2E cruzado + JMeter (50 usuarios, p95<500ms) | 12-13 | 📋 Planificado |
| OWASP audit + rate limiting | 12-13 | 📋 Planificado |
| Docs final + tag v1.0.0 | 13 | 📋 Planificado |

### 🎯 Tu entrada: **Sprint 12 — Backend Grupo B**

**MS-Notificaciones:**
- Plantillas de email CRUD
- In-app notifications (WebSocket-ready)
- Listeners RabbitMQ para eventos de dominio

**MS-Reportes:**
- Reportes operacionales (estudiantes, instructores, vehículos)
- Reportes financieros (ingresos, cobranza, deuda)
- Exportación PDF + Excel
- Dashboard KPIs

**Consulta el detalle completo en `PLAN_FASES.md` — sección "Sprint 12".**

---

## Convenciones y workflow

### Git Workflow

**Branch naming:**
```
feature/sprint-12-X-descripcion-corta
       └─ Ej: feature/sprint-12-1-notificaciones-crud
```

**Commit format:**
```
Sprint 12 (Tarea X descripcion)
      └─ Ej: "Sprint 12 (Tarea 1 - Notificaciones CRUD)"

O si es fix post-merge:
Sprint 12 (Fix tarea X)
```

**Workflow:**
1. Crear branch local desde `main`
2. Hacer commits atómicos (1 commit = 1 tarea)
3. Pushear a remote (mismo nombre de branch)
4. Abrir PR en GitHub
5. Esperar a que **todos** los workflows CI pasen ✅
6. Pedir review a Hernán
7. Merge con "Squash and merge" → automáticamente entra a `main`

### Código Java

**Paquetes:**
```
com.escuela.<servicio>.<layer>
Ej: com.escuela.notificaciones.controller
    com.escuela.notificaciones.service
    com.escuela.notificaciones.repository
    com.escuela.notificaciones.entity
    com.escuela.notificaciones.dto
```

**Naming:**
- Clases: `PascalCase` (ej. `NotificacionController`, `NotificacionService`)
- Métodos: `camelCase` (ej. `enviarNotificacion()`)
- Constantes: `UPPER_SNAKE_CASE` (ej. `MAX_REINTENTOS`)
- Propiedades privadas: `private` sin underscore (ej. `private String titulo`)

**Formato:**
- Google Java Style (4 espacios indentación)
- Línea máxima: 120 caracteres
- **SIN comentarios** a menos que sea lógica no obvia

### Código Vue.js

**Estructura:**
```
src/
  ├── components/          # Componentes reutilizables
  ├── views/               # Vistas/páginas
  ├── stores/              # Pinia stores
  ├── services/            # Axios clients
  ├── router/              # Vue Router config
  └── utils/               # Helpers
```

**Naming:**
- Archivos: `PascalCase.vue` (ej. `NotificacionForm.vue`)
- Importes: Componentes en `<PascalCase />`, utils en `camelCase`
- Props/emits: `camelCase` (ej. `:open="isOpen"`)
- Variables: `camelCase`

**Setup:**
```vue
<script setup lang="ts">
import { ref, computed } from 'vue'

const message = ref('')
</script>
```

### Base de datos

**Tablas:** `snake_case`, plural (ej. `notificaciones`, `plantillas_email`)  
**Columnas:** `snake_case` (ej. `id_notificacion`, `fecha_creacion`)  
**Foreign keys:** `{tabla}_id` (ej. `usuario_id`, `escuela_id`)  
**Auditoría:** Automático `created_at`, `updated_at`, `created_by`, `updated_by`

---

## Cómo trabajar con Claude Code

Claude Code es la IA que te ayudará a codificar. Acá están los puntos clave:

### 1. **Instalación y setup**

```bash
# Instalar la CLI (si no lo hiciste)
npm install -g claude

# Verificar
claude --version

# Loguearte (abre navegador)
claude login
```

### 2. **Cómo invocarlo en el proyecto**

Desde la raíz del proyecto:
```bash
cd proyecto-titulacion-udla
claude code
# Se abre una sesión interactiva con tu proyecto cargado
```

### 3. **Comandos útiles en la sesión**

| Comando | Qué hace |
|---------|----------|
| `/help` | Muestra comandos disponibles |
| `/status` | Estado del repositorio (git) |
| `/preview` | Preview de cambios pendientes |
| `/commit` | Crea un commit (aparecerá prompt de confirmación) |
| `/push` | Pushea a remote (requiere confirmación) |
| `/test` | Corre tests (si están configurados) |

### 4. **Interactuar con Claude**

**Puedes pedir:**
- "Implementa el CRUD de Notificaciones en ms-notificaciones"
- "Revisa el código por vulnerabilidades OWASP"
- "Agrega tests unitarios al NotificacionService"
- "Crea la migración Flyway para la tabla plantillas_email"
- "Refactoriza el frontend para mejorar performance"

**Claude va a:**
1. Leer el código existente
2. Entender la arquitectura
3. Hacer cambios directamente en los archivos
4. Crear commits atómicos
5. Reportar si hay issues o si necesita confirmación

### 5. **Hernán + Claude**

Hernán usa `claude code` para:
- Revisar PR y hacer cambios directamente
- Implementar tareas complejas
- Debuggear issues en prod
- Hacer refactors grandes

**Tú vas a:**
- Usar `claude` para acelerar tu trabajo
- Hacer PRs normales desde tu rama
- Pedir reviews a Hernán

### 6. **Archivo CLAUDE.md del proyecto**

Este archivo (`CLAUDE.md` en la raíz) contiene:
- Decisiones sobre stack
- Guía de arquitectura
- Testing strategy
- Convenciones de código
- Troubleshooting

**Claude lo leerá automáticamente — no tienes que hacer nada.**

---

## Troubleshooting rápido

### ❌ "Docker containers no levantan"

```bash
# 1. Verificar que Docker está corriendo
docker ps

# 2. Ver logs detallados
docker compose -f infrastructure/docker/docker-compose.yml logs

# 3. Forzar rebuild
docker compose -f infrastructure/docker/docker-compose.yml up -d --build

# 4. Si sigue fallando, borra todo y empieza limpio
docker compose -f infrastructure/docker/docker-compose.yml down -v
docker system prune -a --volumes
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

### ❌ "PostgreSQL no conecta"

```bash
# 1. Ver si el contenedor está vivo
docker ps | grep postgres

# 2. Verificar credenciales en .env
grep POSTGRES .env

# 3. Conectarse manualmente
docker exec -it escuela-postgres psql -U escuela_user -d escuela_db -c "\d"
# Debe listar las tablas existentes
```

### ❌ "Frontend no carga en http://localhost:5173"

```bash
# 1. Ver si npm run dev está corriendo
ps aux | grep "npm run dev"

# 2. Ver logs de Vite
npm run dev
# Buscar errores en la salida

# 3. Verificar que .env del frontend apunta a API Gateway
cat frontend/.env
# Debe tener: VITE_API_BASE_URL=http://localhost:8080

# 4. Limpiar node_modules y reinstalar
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### ❌ "Tests fallan localmente"

```bash
# 1. Ejecutar con output verbose
mvn test -e

# 2. Ver si Testcontainers necesita Docker
# (debe estar corriendo Docker Desktop)
docker ps

# 3. Saltarse tests si es muy urgente (NO EN PROD)
mvn package -DskipTests

# 4. Ejecutar tests de una clase específica
mvn test -Dtest=NotificacionServiceTest
```

### ❌ "Git: "main branch is protected""

```bash
# No puedes pushear directamente a main
# Debes hacer un PR desde tu feature branch

# 1. Asegúrate de estar en tu feature branch
git branch

# 2. Pushear la rama
git push origin feature/sprint-12-1-notificaciones-crud

# 3. En GitHub, abrir PR desde esa rama a main

# 4. Esperar a que CI pase + review de Hernán
```

### ❌ "JWT Secret no funciona"

```bash
# 1. Verificar que la clave está en .env
grep JWT_SECRET .env
# No debe estar vacío o con <...>

# 2. Verificar encoding (debe ser base64)
echo -n "tu-jwt-secret" | base64

# 3. Rebuild los contenedores
docker compose -f infrastructure/docker/docker-compose.yml up -d --build ms-auth

# 4. Probar login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@escuela.local","password":"Admin123!"}'
# Debe devolver JWT en respuesta
```

### ❌ "RabbitMQ connection refused"

```bash
# 1. Ver si RabbitMQ está vivo
docker ps | grep rabbitmq

# 2. Acceder a la consola
# http://localhost:15672 (guest/guest)

# 3. Ver si los exchanges existen
# Management → Exchanges → debe haber eventos_domain, etc.

# 4. Reiniciar RabbitMQ
docker compose -f infrastructure/docker/docker-compose.yml restart rabbitmq
```

### ✅ Checklist de validación post-setup

Verifica que tu entorno está listo:

```bash
# 1. Clonar y git status
cd proyecto-titulacion-udla
git status
# Debe decir: "On branch main" y "nothing to commit"

# 2. Docker 14/14 healthy
docker compose -f infrastructure/docker/docker-compose.yml ps
# Todos los servicios deben decir "healthy"

# 3. Frontend carga
curl -s http://localhost:5173 | head -1
# Debe tener HTML

# 4. API Gateway responde
curl -s http://localhost:8080/actuator/health

# 5. Eureka registró 9 servicios
curl -s http://localhost:8761/eureka/apps | grep -c application
# Debe imprimir: 9

# 6. Puedes loguear
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@escuela.local","password":"Admin123!"}'
# Debe devolver token (JSON con acceso)

# 7. Java 21 + Maven 3.8+
java -version && mvn -v

# 8. .env está completo
grep -c "^[A-Z_]*=" .env
# Debe ser >20 variables
```

---

## 🎯 Próximos pasos

**Una vez que hayas completado la guía:**

1. ✅ Clona el repo
2. ✅ Configura `.env` (Hernán lo pasa)
3. ✅ Levanta Docker Compose (`docker compose ... up -d`)
4. ✅ Corre `npm install && npm run dev` en frontend
5. ✅ Verifica login en http://localhost:5173
6. ✅ Lee DECISIONES.md + PLAN_FASES.md
7. ✅ Revisa tu tarea asignada en Sprint 12
8. ✅ Instala `claude` CLI y pruébalo
9. ✅ **Avísale a Hernán que estás listo** 🚀

---

## 📞 Contacto

- **Hernán** (Lead + reviewea PRs): Duda o bloqueo → mensaje directo
- **Tutor (Víctor Gómez)**: Decisiones arquitectónicas → email
- **Repositorio**: https://github.com/Kynsofttita-com/proyecto-titulacion-udla

---

**¡Bienvenido al proyecto! 🎉**

Si algo no te queda claro, preguntatle a Hernán. Este es un proyecto grande pero bien documentado — estamos para ayudarte.

**Última actualización:** 2026-07-07 — Estado Sprint 11 ✅ Completo, Sprint 12 está ahora.
