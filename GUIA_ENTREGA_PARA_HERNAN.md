# 📋 Guía de Entrega para Hernán — Qué pasarle a Sebastián

**De:** Tu mismo (Hernán)  
**Para:** Cuando entregues el proyecto a Sebastián  
**Tipo:** Checklist de entrega + plantilla de comunicación

---

## 📦 Qué pasarle a Sebastián — Checklist completo

### Documentos que HEREDAN del repo (están en GitHub)

Estos **NO necesitás enviar** — Sebastián los descargará al clonar:

- ✅ `DECISIONES.md` — Todas las decisiones técnicas (32 decisiones)
- ✅ `PLAN_FASES.md` — Plan de Sprints 5-12
- ✅ `CLAUDE.md` — Guía operativa
- ✅ `README.md` — Descripción general
- ✅ `backend/README.md` — Setup backend
- ✅ `frontend/README.md` — Setup frontend
- ✅ `infrastructure/docker/README.md` — Docker detallado
- ✅ `.github/CONTRIBUTING.md` — Git workflow
- ✅ `docs/database/schema.md` — Diseño de BD

### Documentos que TÚ debes crear y pasar (están en la raíz ahora)

Estos **HEREDAN el formato de Hernán** y están listos para entregar:

- ✅ `ONBOARDING_SEBASTIAN.md` — Guía completa (⭐ ENTREGAR ESTA)
- ✅ `INICIO_RAPIDO_SEBASTIAN.md` — Resumen 5 minutos (⭐ ENTREGAR ESTA)
- ✅ `ENTREGA_A_SEBASTIAN.md` — Checklist para el propio Hernán

**Acción:** Sebastián debe leer primero `INICIO_RAPIDO_SEBASTIAN.md`, luego `ONBOARDING_SEBASTIAN.md`.

### Archivos CONFIDENCIALES que TÚ debes compartir por otra vía

Estos **NUNCA** van a GitHub — debes pasarlos directamente:

| Archivo | Contenido | Cómo pasar | Seguridad |
|---------|-----------|-----------|----------|
| `.env` | Credenciales BD, JWT, Mailtrap | En persona o 1Password | 🔒 CRÍTICO |
| `.env` backup | Copia de seguridad | Guardar en OneDrive/Drive | 🔒 Personal |
| `ssh key` (opcional) | Para GitHub SSH | Si Sebastián usa SSH | 🔒 Protegido |

**⚠️ NUNCA:**
- Envíes `.env` por email plano
- Lo subes a GitHub o Google Drive público
- Lo pinceles en mensajes de chat normales

---

## 📧 Email de entrega — Copia/Pega

Puedes usar esto como template:

---

### Asunto: Entrega del Proyecto Titulación — Onboarding Sprint 12

Hola Sebastián,

Te comparto los documentos y credenciales para que te integres al proyecto. Todo está listo para que empieces.

**📚 Documentos para leer (orden recomendado):**

1. **`INICIO_RAPIDO_SEBASTIAN.md`** (5 min) — Levanta todo en 4 pasos
2. **`ONBOARDING_SEBASTIAN.md`** (2-3 horas) — Guía completa con contexto
3. **Documentos en el repo** (ya clonados):
   - DECISIONES.md (§1-§10 primero)
   - PLAN_FASES.md (tu tarea en Sprint 12)
   - CLAUDE.md (guía operativa)
   - README.md (resumen ejecutivo)

**🔑 Credenciales que paso por separado:**
- `.env` → **Copiar en raíz del proyecto** (te lo paso en persona / 1Password / [TU MÉTODO])
- GitHub username: Ya agregado como collaborator

**🚀 Pasos para empezar HOY:**

```bash
git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git
cd proyecto-titulacion-udla
# Copia el .env que te pasé
docker compose -f infrastructure/docker/docker-compose.yml up -d
# Espera 60s, verifica que todos los containers digan "healthy"
cd frontend && npm install && npm run dev
# Abre http://localhost:5173 y loguea
```

Debés poder loguear con `admin@escuela.local` / `Admin123!`.

**⏱️ Timeline esperado:**

- **Hoy/Mañana:** Levanta todo + lee INICIO_RAPIDO_SEBASTIAN.md
- **Día 2:** Lee ONBOARDING_SEBASTIAN.md + DECISIONES.md (§1-§10)
- **Día 3:** Listo para que te asigne tareas de Sprint 12

**❓ Si algo no funciona:**

Abrí `ONBOARDING_SEBASTIAN.md` → sección "Troubleshooting rápido". Si no encuentrás tu error, reportámelo con el error exacto.

**🎯 Sprint 12 — Tu trabajo:**

- **MS-Notificaciones:** CRUD plantillas, listeners RabbitMQ, in-app notifications
- **MS-Reportes:** Operacionales, financieros, PDF/Excel export

Detalle completo en `PLAN_FASES.md`.

¿Preguntas en este momento? Preguntá sin problema.

¡Bienvenido al Sprint 12! 🚀

Hernán

---

## 🎁 Lo que debe recibir Sebastián — Resumido

**En el repo (con el git clone):**
```
proyecto-titulacion-udla/
├── ONBOARDING_SEBASTIAN.md ← LEER ESTO PRIMERO
├── INICIO_RAPIDO_SEBASTIAN.md ← DESPUÉS ESTO (5 min)
├── DECISIONES.md
├── PLAN_FASES.md
├── CLAUDE.md
├── README.md
├── backend/
├── frontend/
├── infrastructure/
└── docs/
```

**Por otra vía (TÚ pasás):**
```
.env (archivo confidencial)
  ├── POSTGRES_PASSWORD
  ├── JWT_SECRET
  ├── MAILTRAP_USER / PASSWORD
  └── MINIO_ROOT_PASSWORD
```

**En GitHub (ya está):**
```
Acceso collaborator con permisos de write
```

---

## ✅ Validación de entrega completa

Completa este checklist **antes de marcar a Sebastián como "onboarded":**

- [ ] Sebastián clonó el repo exitosamente
- [ ] Tiene un `.env` funcional (sin placeholders `<...>`)
- [ ] Ejecutó `docker compose ... up -d` y todos los containers están "healthy"
- [ ] Frontend levantó en `http://localhost:5173`
- [ ] Pudo loguear con admin@escuela.local / Admin123!
- [ ] Leyó `INICIO_RAPIDO_SEBASTIAN.md` (confirmó que lo hizo)
- [ ] Leyó `ONBOARDING_SEBASTIAN.md` (preguntó dudas si las tuvo)
- [ ] Leyó `DECISIONES.md` §1-§10
- [ ] Leyó `PLAN_FASES.md` — sección Sprint 12
- [ ] Sabe cuál es su tarea asignada
- [ ] Instaló `claude` CLI (opcional pero recomendado)
- [ ] Hizo su primer commit de prueba (`git commit -m "test"`)
- [ ] Puede hacer push/PR sin errores

Si todos los ☑️ están completados, **Sebastián está listo para que le asignes tareas.**

---

## 🔄 Si algo se rompe durante la entrega

**Escenario 1:** Sebastián no puede loguear
- Verificar que el `.env` tiene credenciales correctas
- Verificar que PostgreSQL está "healthy" en Docker
- Revisar logs: `docker compose ... logs ms-auth`

**Escenario 2:** Docker containers no levantan
- `docker system prune -a --volumes` (bomba nuclear)
- Rebuild: `docker compose ... up -d --build`
- Si sigue fallando: revisar logs `docker compose ... logs`

**Escenario 3:** Frontend no carga
- `npm install` de vuelta
- Verificar que `.env` del frontend apunta a `http://localhost:8080`
- `npm run dev` de nuevo

**Escenario 4:** Git clone falla
- Verificar que tiene credenciales correctas (GitHub)
- Si es SSH, verificar key pública en GitHub settings
- Usar HTTPS si SSH no funciona

---

## 📞 Puntos de contacto

**Después de onboarding, Sebastián reporta dudas a:**

- **Código / Arquitectura:** Hernán (vos)
- **Decisiones técnicas:** Revisar DECISIONES.md primero, luego tutor Víctor
- **BD / Queries:** DECISIONES.md §20-§21 o Hernán
- **Devops / Docker:** CLAUDE.md "Deployment" o Hernán
- **Git workflow:** .github/CONTRIBUTING.md o Hernán

---

## 🎯 Resultado esperado

**Después de 3 días de onboarding, Sebastián debe:**

✅ Clonar y levantar el sistema en su máquina  
✅ Entender la arquitectura microservicios  
✅ Saber qué es su Sprint 12 + tareas concretas  
✅ Poder hacer commits, PRs, y trabajar con Claude Code  
✅ Tener un `.env` funcional y seguro  
✅ Conocer las convenciones del proyecto  

Entonces puede empezar a codificar sin supervisión directa.

---

## 📝 Notas finales

- Este onboarding duró **unas 4-5 horas de prep** para Hernán (tú) — ahora Sebastián la hace en 2-3 horas. ✅ Ganancia.
- Los documentos `ONBOARDING_SEBASTIAN.md` + `INICIO_RAPIDO_SEBASTIAN.md` son **reutilizables** — si en el futuro viene otro dev, puedes usarlos de nuevo.
- La guía es **agnóstica del IDE** — funciona con IntelliJ, VS Code, cualquiera.
- Si Sebastián se atasca después, tienes `ENTREGA_A_SEBASTIAN.md` como referencia de qué falla típicamente.

---

**¡Lista de entrega lista!** 🎉

Cuando estés listo, ejecuta:
```bash
# Desde la raíz del proyecto
git add ONBOARDING_SEBASTIAN.md INICIO_RAPIDO_SEBASTIAN.md ENTREGA_A_SEBASTIAN.md
git commit -m "Docs: Onboarding guides para Sebastián (Sprint 12)"
git push
```

Luego compartí este email (el template arriba) y el link al repo. ¡Listo!
