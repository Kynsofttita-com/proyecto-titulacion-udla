# 👋 LEEME PRIMERO

**Para:** Sebastián  
**De:** Hernán  
**Cuándo:** Hoy mismo — en los próximos 30 minutos

---

## 🚀 Lo que hay en este ZIP

Este es tu **kit completo de onboarding**. Incluye:

- ✅ Guía rápida (5 minutos)
- ✅ Guía completa (2-3 horas)
- ✅ Referencias rápidas (qué consultar cuando dudes)
- ✅ Template de `.env` (qué credenciales necesitás)
- ✅ Checklist de validación

**Total de tiempo esperado:** 3-4 horas (puedes distribuirlo en 2-3 días)

---

## ⚡ Si solo tienes 30 minutos AHORA

### Paso 1: Descomprime el ZIP

```bash
unzip ONBOARDING-SEBASTIAN-2026-07-07.zip
cd ONBOARDING-SEBASTIAN-2026-07-07
```

### Paso 2: Lee la guía rápida

Abre: `1_INICIO_RAPIDO/INICIO_RAPIDO_SEBASTIAN.md`

(5 minutos — es super corta)

### Paso 3: Ejecuta los 4 pasos

Siguen exactamente lo que dice la guía rápida:

```bash
# 1. Clonar
git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git
cd proyecto-titulacion-udla

# 2. Copiar .env
# Hernán te pasó un archivo .env por otra vía
# Cópialo aquí: cp /ruta/donde/lo/guardaste/.env .env

# 3. Levantar Docker
docker compose -f infrastructure/docker/docker-compose.yml up -d

# 4. Levantar frontend
cd frontend && npm install && npm run dev
```

Abre navegador en: **http://localhost:5173**

Loguea con:
```
Email:    admin@escuela.local
Password: Admin123!
```

Si ves el dashboard, ✅ **¡FUNCIONA!**

### Paso 4: Valida que todo está OK

Abre: `5_CHECKLIST/VALIDACION_POST-SETUP.md`

Marca los checkboxes. Todos deben estar ✅.

---

## 📚 Después (cuando tengas más tiempo)

**Lee en este orden:**

1. **`2_GUIA_COMPLETA/ONBOARDING_SEBASTIAN.md`** (2-3 horas)
   - Todo lo que necesitás saber del proyecto
   - Arquitectura, convenciones, troubleshooting
   - ⭐ LA MÁS IMPORTANTE

2. **`3_REFERENCIAS_RAPIDAS/`** (5-15 min cada una, según necesites)
   - `PLAN_SPRINT_12.txt` — Tu tarea en Sprint 12
   - `CONVENCION_COMMITS.txt` — Cómo hacer commits
   - `URLS_UTILES.txt` — URLs localhost
   - `DECISIONES_RESUMEN.txt` — Decisiones técnicas (resumen)

3. **Documentos en el repositorio** (después de clonar)
   - `DECISIONES.md` (en el repo)
   - `PLAN_FASES.md` (en el repo)
   - `CLAUDE.md` (en el repo)

---

## 🔑 El archivo más importante: `.env`

### ⚠️ CRÍTICO

Hernán **NO lo incluyó en el ZIP** (contiene credenciales secretas).

**Cómo funciona:**
1. Hernán te pasó un archivo `.env` por **otra vía** (en persona, email cifrado, 1Password, etc.)
2. Tú copias ese `.env` en la **raíz del proyecto clonado**
3. `docker compose` lee automáticamente el `.env`
4. Listo.

**Si no recibiste el `.env`:**
→ Preguntá a Hernán dónde está.

**Qué hay en el `.env`:**
- Contraseña PostgreSQL
- Clave JWT
- Credenciales Mailtrap (email dev)
- Credenciales MinIO

(Ver `4_CONFIGURACION/.env.template` para referencia)

---

## 🎯 Resumen de hoy

```
⏱️ 30 minutos ahora:
   1. Descomprimir ZIP
   2. Leer INICIO_RAPIDO (5 min)
   3. Ejecutar 4 pasos (20 min)
   4. Validar que funciona (5 min)

📚 Cuando tengas tiempo (próximas 24-48 horas):
   1. Leer ONBOARDING_SEBASTIAN.md completo (2-3 horas)
   2. Consultar referencias rápidas según necesites
   3. Reportar dudas a Hernán

🚀 Cuando todo esté OK:
   1. Hernán te asigna tareas de Sprint 12
   2. Empezás a producir código
```

---

## 🆘 Si algo falla

### Docker no levanta

Abre: `2_GUIA_COMPLETA/ONBOARDING_SEBASTIAN.md` → Sección "Troubleshooting rápido" → Tema "Docker containers no levantan"

### Frontend no carga

Abre: `2_GUIA_COMPLETA/ONBOARDING_SEBASTIAN.md` → Sección "Troubleshooting rápido" → Tema "Frontend no carga"

### No tengo el `.env`

Preguntá a Hernán dónde está (no debería estar en el ZIP).

### No encuentro la solución a mi problema

Reportá el **error exacto** a Hernán. Incluí:
- El error que ves (completo, en consola)
- Qué paso estabas haciendo cuando pasó
- Qué ya intentaste

---

## 📋 Estructura del ZIP

```
ONBOARDING-SEBASTIAN-2026-07-07/
│
├── LEEME_PRIMERO.md                    ← Estás aquí
├── ONBOARDING_ZIP_INDEX.md             ← Dónde va cada archivo
│
├── 1_INICIO_RAPIDO/
│   └── INICIO_RAPIDO_SEBASTIAN.md      ← 5 minutos, levanta todo
│
├── 2_GUIA_COMPLETA/
│   └── ONBOARDING_SEBASTIAN.md         ← La guía detallada
│
├── 3_REFERENCIAS_RAPIDAS/
│   ├── PLAN_SPRINT_12.txt
│   ├── CONVENCION_COMMITS.txt
│   ├── URLS_UTILES.txt
│   ├── DECISIONES_RESUMEN.txt
│   └── DOCKER_COMPOSE_INFO.txt
│
├── 4_CONFIGURACION/
│   └── .env.template                   ← Referencia (NO uses esto, pedí .env real)
│
└── 5_CHECKLIST/
    └── VALIDACION_POST-SETUP.md        ← Verifica que todo funciona
```

---

## ✅ Checklist de HOY

- [ ] Descomprimí el ZIP
- [ ] Leí este archivo (LEEME_PRIMERO.md)
- [ ] Leí `1_INICIO_RAPIDO/INICIO_RAPIDO_SEBASTIAN.md`
- [ ] Ejecuté los 4 pasos (clonar, .env, docker, frontend)
- [ ] Abrí http://localhost:5173 y pude loguear
- [ ] Marqué los checks en `5_CHECKLIST/VALIDACION_POST-SETUP.md`

Si todos ✅ → **¡Excelente! Pasás a la guía completa mañana.**

---

## 🤝 Contacto

**Si tienes dudas:**
→ Preguntá a Hernán (no esperes, preguntá)

**Si algo no funciona:**
→ Reportá el error exacto (con screenshot si es posible)

**Si necesitás ayuda con códigos:**
→ Usa Claude Code (está instalado, `claude code`)

---

## 🚀 ¡A comenzar!

**Ahora:**
1. Descomprime el ZIP
2. Lee `1_INICIO_RAPIDO/INICIO_RAPIDO_SEBASTIAN.md`
3. Ejecuta los 4 pasos

**¡Bienvenido! 🎉**

---

**Creado:** 2026-07-07  
**Para:** Sebastián  
**De:** Hernán (vía Claude Code)
