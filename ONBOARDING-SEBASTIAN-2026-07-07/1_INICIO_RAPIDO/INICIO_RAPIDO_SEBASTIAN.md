# ⚡ Inicio Rápido — 5 minutos

**Para:** Sebastián  
**De:** Hernán  
**Cuándo:** HOY — ejecuta esto para levantar el proyecto

---

## 🚀 En 4 pasos

### 1️⃣ Clonar

```bash
git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git
cd proyecto-titulacion-udla
```

### 2️⃣ Copiar .env

Hernán te pasó un archivo `.env` → cópialo en la raíz del proyecto (mismo directorio que este archivo).

```bash
ls -la .env
# Debe existir (no debe decir "No such file")
```

### 3️⃣ Levantar Docker

```bash
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

Espera **60 segundos** y verifica:

```bash
docker compose -f infrastructure/docker/docker-compose.yml ps
# Todos deben decir "healthy" (no puede haber "unhealthy" ni "exited")
```

### 4️⃣ Levantar frontend

```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```

Debe decir algo como:  
```
VITE v5.x.x ready in XXXms
➜  Local: http://localhost:5173/
```

---

## ✅ Validar que funciona

Abrí tu navegador en: **http://localhost:5173**

Logueate con:
```
Email:    admin@escuela.local
Password: Admin123!
```

Si entra al dashboard, ✅ **ESTÁ TODO OK**.

---

## 📚 Ahora lee esto (en orden)

Después de validar que funciona, lee en este orden (1-2 horas):

1. `ONBOARDING_SEBASTIAN.md` — Tu guía completa (📖 45 min)
2. `DECISIONES.md` — Las decisiones técnicas (§1-§10 por ahora, ~30 min)
3. `PLAN_FASES.md` — Qué vas a desarrollar en Sprint 12 (~20 min)

---

## 🆘 Si algo falla

Abrí `ONBOARDING_SEBASTIAN.md` → ve a "Troubleshooting rápido" → busca tu error.

Si no lo encontrás, reportá el **error exacto** a Hernán.

---

## 🎯 Próximo paso

Una vez que hayas leído todo (hoy o mañana):

Hernán te asigna las tareas específicas de Sprint 12.

---

**¿Preguntas en este momento?** Preguntá.  
**¿Listo para leer?** Abrí ONBOARDING_SEBASTIAN.md.

🚀 ¡Bienvenido!
