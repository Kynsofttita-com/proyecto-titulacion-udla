# 📦 Checklist de Entrega a Sebastián

**Responsable:** Hernán Mateo Jurado Moran  
**Fecha recomendada de entrega:** Antes de que Sebastián inicie Sprint 12  
**Duración esperada de onboarding:** 2-3 horas

---

## 📋 Lista de verificación completa

### ✅ PASO 1: Acceso al repositorio

**Qué hacer:**
- [ ] Confirmar que Sebastián tiene acceso al repo GitHub (`Kynsofttita-com/proyecto-titulacion-udla`)
- [ ] Agregar a Sebastián como **Collaborator** (Settings → Collaborators)
- [ ] Darle permisos de **Write** (puede hacer push, no puede borrar branches)

**Confirmación:**
```bash
# Sebastián debe poder ejecutar:
git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git
# Sin errores de autenticación
```

---

### ✅ PASO 2: Archivo `.env` funcional

**Qué hacer:**
- [ ] Generar o compartir el `.env` **CON valores reales** (NO .env.example)
- [ ] Asegurar que tenga:
  - `POSTGRES_PASSWORD` (contraseña real para BD)
  - `JWT_SECRET` (clave HS512 de 512 bits en base64)
  - `MAILTRAP_USER` y `MAILTRAP_PASSWORD` (si usas Mailtrap en dev)
  - `MINIO_ROOT_PASSWORD` y `MINIO_ROOT_USER`
  - `POSTGRES_INITDB_ARGS` (si tienes)
  - `TZ=America/Guayaquil` (crítico para JVM)

**Cómo compartirlo de forma segura:**
- ❌ NO por email plano
- ✅ **Opción A:** Copiar el archivo de tu máquina a la de Sebastián en persona
- ✅ **Opción B:** Usar 1Password / LastPass compartido (si tienes licencia)
- ✅ **Opción C:** Compartir por Telegram/Signal en sesión privada (borrar después)
- ✅ **Opción D:** Crear un `.env.local` compartido en una carpeta en OneDrive temporal

**Verificación:**
```bash
# Sebastián debe poder pegar el .env en la raíz del proyecto
cd proyecto-titulacion-udla
ls -la .env
# Debe existir y tener modo 644 (lectura solo)
```

---

### ✅ PASO 3: Documentos de referencia

**Orden de lectura recomendado para Sebastián:**

| # | Documento | Ubicación | Tiempo | Acción |
|---|-----------|-----------|--------|--------|
| 1️⃣ | **Este onboarding** | `ONBOARDING_SEBASTIAN.md` | 20 min | Leer de punta a punta |
| 2️⃣ | **DECISIONES.md §1-§10** | `/DECISIONES.md` (líneas 1-150) | 30 min | Decisiones arquitectónicas |
| 3️⃣ | **CLAUDE.md** | `/CLAUDE.md` | 45 min | Stack, directorios, testing |
| 4️⃣ | **PLAN_FASES.md — Sprint 12** | `/PLAN_FASES.md` (sección Sprint 12) | 20 min | Tu tarea asignada |
| 5️⃣ | **docs/database/schema.md §1-§3** | `docs/database/schema.md` | 30 min | Estructura BD (los 9 schemas) |
| 6️⃣ | **README.md** | `/README.md` | 15 min | Resumen ejecutivo |
| 7️⃣ | **.github/CONTRIBUTING.md** | `.github/CONTRIBUTING.md` | 10 min | Git workflow + commits |
| 8️⃣ | **infrastructure/docker/README.md** | `infrastructure/docker/README.md` | 10 min | Docker Compose detallado |
| 9️⃣ | **DECISIONES.md §11-§32** | `/DECISIONES.md` (líneas 150+) | 45 min | Decisiones técnicas del detalle |

**Total:** ~3-4 horas de lectura  
**Recomendación:** Leer §1-§8 el primer día, §9 cuando tengas dudas

---

### ✅ PASO 4: Verificar que puede levantar el sistema

**Instrucciones para Sebastián:**

```bash
# 1. Clonar
git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git
cd proyecto-titulacion-udla

# 2. Copiar .env que Hernán pasó
cp <ruta-del-.env-que-recibiste> .env

# 3. Levantad Docker
docker compose -f infrastructure/docker/docker-compose.yml up -d

# 4. Esperar ~60s y verificar
docker compose -f infrastructure/docker/docker-compose.yml ps
# TODOS deben decir "healthy"

# 5. Instalar frontend deps
cd frontend
npm install

# 6. Copiar .env del frontend
cp .env.example .env

# 7. Levantar frontend
npm run dev
```

**Checklist de validación:**
- [ ] `docker compose ... ps` → todos healthy
- [ ] `http://localhost:5173` carga
- [ ] Puede loguear con `admin@escuela.local` / `Admin123!`
- [ ] Eureka muestra 9 servicios en `http://localhost:8761`
- [ ] RabbitMQ console accesible en `http://localhost:15672`

**Si falla algo:**
- [ ] Sebastián reporta el error exacto
- [ ] Hernán lo ayuda a debuggear (ver sección "Troubleshooting" en ONBOARDING_SEBASTIAN.md)

---

### ✅ PASO 5: Instalar Claude Code (opcional pero recomendado)

**Instrucciones para Sebastián:**

```bash
# 1. Instalar CLI
npm install -g claude

# 2. Verificar instalación
claude --version

# 3. Loguearse (abre navegador)
claude login

# 4. Entrar en el proyecto
cd proyecto-titulacion-udla
claude code

# 5. Prueba simple
# En la sesión de Claude, escribi: /status
# Debe mostrar: "On branch main" y estado del repo
```

**Opcional:** Si Sebastián prefiere usar VS Code + Extension, instalar:
- Extensión "Claude" en VS Code Marketplace
- Loguearse con la misma cuenta

---

### ✅ PASO 6: Verificar acceso a servicios compartidos

**Servicios y credenciales que Hernán debe confirmar:**

| Servicio | Para qué | Credencial | Estado |
|----------|----------|-----------|--------|
| **GitHub repo** | Código fuente | OAuth/SSH | ✅ Acceso ya dado |
| **Jira** | Tracking de tareas | Usuario/contraseña | ❓ Verificar con tutor |
| **OneDrive TARIUS** | Documentos empresa | OAuth | ❓ Preguntar si necesita |
| **Mailtrap** (dev) | Testing emails | Usuario/pw en .env | ✅ Incluido en .env |
| **Oracle Cloud** | Deploy prod | Fernandito + Hernán solo | ❌ NO — Solo Hernán |

**Acción:** Confirmar con Sebastián qué servicios va a usar realmente en Sprint 12.

---

## 📧 Mensaje de entrega para Sebastián

Puedes enviarle algo así:

---

### 🚀 ¡Bienvenido al proyecto!

Hola Sebastián,

Te pasé los siguientes documentos e instrucciones para que te integres al proyecto. Es bastante, pero no es complicado — el proyecto está bien estructurado.

**📦 Qué incluye tu entrega:**

1. **ONBOARDING_SEBASTIAN.md** — Tu guía de inicio (léelo primero)
2. **Archivo .env funcional** — Lo necesitás para levantar Docker
3. **Acceso GitHub** — Ya te agregué al repo
4. **Credenciales compartidas** — Mailtrap, JWT secret, BD (en el .env)

**📋 Próximos pasos (en orden):**

1. Lee `ONBOARDING_SEBASTIAN.md` completo (~45 min)
2. Clona el repo: `git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git`
3. Copia el `.env` que te pasé en la raíz
4. Levanta Docker: `docker compose -f infrastructure/docker/docker-compose.yml up -d`
5. Espera ~60s y verifica que todos los contenedores digan "healthy"
6. Levanta el frontend: `cd frontend && npm install && npm run dev`
7. Abre http://localhost:5173 y loguea con `admin@escuela.local` / `Admin123!`

**📚 Orden de lectura recomendado:**

Después de que levantes todo, lee en este orden:
- DECISIONES.md (§1-§10, ~30 min)
- PLAN_FASES.md (Sprint 12, ~20 min)
- CLAUDE.md (completo, ~45 min)
- docs/database/schema.md (§1-§3, ~30 min)

Total: ~3 horas de lectura. Hacé esto en los primeros 2 días.

**🆘 Si algo falla:**

Consulta la sección "Troubleshooting" en ONBOARDING_SEBASTIAN.md. Si sigue sin funcionar, reportá el error exacto en un mensaje y lo resolvemos juntos.

**🎯 Tu primer sprint (Sprint 12):**

Vas a trabajar en:
- Backend de MS-Notificaciones (CRUD plantillas, listeners RabbitMQ)
- Backend de MS-Reportes (operacionales, financieros, PDF/Excel)

El detalle exacto está en `PLAN_FASES.md`. Cuando hayas leído todo, nos vemos para asignar tareas.

**¿Preguntas?** Preguntá sin problema.

¡Bienvenido! 🚀

Hernán

---

## 🎁 Archivos para compartir (checklist final)

Confirma que Sebastián recibió TODO esto:

```
proyecto-titulacion-udla/
├── ✅ ONBOARDING_SEBASTIAN.md     ← Guía completa (compartir)
├── ✅ .env                         ← Credenciales funcionales (compartir)
├── ✅ DECISIONES.md               ← Decisiones técnicas (ya en repo)
├── ✅ PLAN_FASES.md               ← Plan de sprints (ya en repo)
├── ✅ CLAUDE.md                   ← Guía operativa (ya en repo)
├── ✅ README.md                   ← Resumen ejecutivo (ya en repo)
├── ✅ backend/README.md           ← Backend setup (ya en repo)
├── ✅ frontend/README.md          ← Frontend setup (ya en repo)
├── ✅ infrastructure/docker/README.md  ← Docker detallado (ya en repo)
├── ✅ .github/CONTRIBUTING.md     ← Git workflow (ya en repo)
└── ✅ docs/database/schema.md     ← Diseño BD (ya en repo)
```

**Nota:** Los archivos marcados "ya en repo" están en GitHub. Los que dicen "compartir" debes pasarlos directamente.

---

## ✨ Validación de "lista verde"

**Sebastián está listo para empezar cuando:**

- [ ] Ha leído ONBOARDING_SEBASTIAN.md
- [ ] Puede clonar el repo sin errores
- [ ] Tiene el `.env` correcto (sin placeholders `<...>`)
- [ ] `docker compose ... ps` muestra 14/14 healthy
- [ ] Frontend carga en http://localhost:5173
- [ ] Puede loguear
- [ ] Ha leído DECISIONES.md §1-§10
- [ ] Sabe qué va a desarrollar en Sprint 12
- [ ] Puede hacer un commit de prueba y un push
- [ ] Ha instalado claude CLI (opcional pero recomendado)

Si todo esto es ✅, **¡está listo para que le asignes tareas!**

---

## 📞 Support post-onboarding

Si Sebastián se atasca:

1. **Error de Docker** → Consultar "Troubleshooting" en ONBOARDING_SEBASTIAN.md
2. **Duda arquitectónica** → Revisar DECISIONES.md y PLAN_FASES.md
3. **Cómo codificar** → Revisar CLAUDE.md y ejemplos en el repo
4. **PR bloqueado en CI** → Revisar .github/CONTRIBUTING.md y logs de GitHub Actions
5. **Algo no queda claro** → Preguntale a Hernán

---

**¡Listo! Tu compañero está equipado para comenzar.** 🎉
