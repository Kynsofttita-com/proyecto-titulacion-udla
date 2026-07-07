# 📦 Índice de Entrega a Sebastián — Dónde va cada archivo

**De:** Hernán  
**Para:** Sebastián  
**Estado:** Kit completo de onboarding Sprint 12

---

## 🗂️ Estructura del ZIP

```
ONBOARDING-SEBASTIAN-2026-07-07.zip
│
├── 📍 LEEME_PRIMERO.md                    ← ⭐ ABRE ESTO PRIMERO
│
├── 1_INICIO_RAPIDO/
│   └── INICIO_RAPIDO_SEBASTIAN.md         ← 5 minutos para levantar todo
│
├── 2_GUIA_COMPLETA/
│   └── ONBOARDING_SEBASTIAN.md            ← 2-3 horas, guía detallada
│
├── 3_REFERENCIAS_RAPIDAS/
│   ├── DECISIONES_RESUMEN.txt            ← Decisiones técnicas (§1-§10)
│   ├── PLAN_SPRINT_12.txt                ← Tu tarea en Sprint 12
│   ├── CONVENCION_COMMITS.txt            ← Formato de commits
│   └── URLS_UTILES.txt                   ← Localhost URLs
│
├── 4_CONFIGURACION/
│   ├── .env.template                     ← ← ← IMPORTANTE: Reemplazar con .env real
│   └── docker-compose-info.txt           ← Portos y servicios
│
└── 5_CHECKLIST/
    └── VALIDACION_POST-SETUP.md          ← Verifica que todo funciona
```

---

## ✅ Instrucciones paso-a-paso

### **PASO 1: Descomprime el ZIP**

```bash
cd C:\tu-ruta-de-descargas
unzip ONBOARDING-SEBASTIAN-2026-07-07.zip
cd ONBOARDING-SEBASTIAN-2026-07-07
ls -la
```

### **PASO 2: Lee LEEME_PRIMERO.md**

⭐ Abre este archivo PRIMERO. Te dice exactamente qué hacer.

### **PASO 3: Levanta el sistema (5 minutos)**

Abre: `1_INICIO_RAPIDO/INICIO_RAPIDO_SEBASTIAN.md`

Ejecuta los 4 pasos ahí. Debe quedar todo levantado en Docker + frontend.

### **PASO 4: Copia el `.env` real**

Hernán te pasó un archivo `.env` por otra vía (en persona, 1Password, etc.).

**IMPORTANTE:**
```bash
# Mover el .env real que recibiste a la raíz del proyecto clonado
cp <ruta-donde-lo-guardaste>/.env ~/TARIUS-DESARROLLO/proyecto-titulacion-udla/.env

# NO uses el .env.template — ese es solo referencia
```

### **PASO 5: Valida que funciona**

Abre: `5_CHECKLIST/VALIDACION_POST-SETUP.md`

Ejecuta todos los checks. Si todos ✅, pasás al siguiente paso.

### **PASO 6: Lee la guía completa**

Abre: `2_GUIA_COMPLETA/ONBOARDING_SEBASTIAN.md`

Léela de punta a punta (~2-3 horas). Es tu "biblia" del proyecto.

### **PASO 7: Consulta referencias rápidas**

Cuando necesites recordar algo, abre:
- `3_REFERENCIAS_RAPIDAS/CONVENCION_COMMITS.txt` — Formato de commits
- `3_REFERENCIAS_RAPIDAS/URLS_UTILES.txt` — URLs localhost
- `3_REFERENCIAS_RAPIDAS/PLAN_SPRINT_12.txt` — Tu tarea
- `3_REFERENCIAS_RAPIDAS/DECISIONES_RESUMEN.txt` — Decisiones arquitectónicas

---

## 📋 Qué va donde

### En tu máquina (después de descomprimir)

```
C:\Users\tu-usuario\TARIUS-DESARROLLO\proyecto-titulacion-udla\
│
├── .env                              ← ← ← Pega aquí el .env real (NO el .template)
├── ONBOARDING_SEBASTIAN.md           ← (Ya está en el repo)
├── INICIO_RAPIDO_SEBASTIAN.md        ← (Ya está en el repo)
│
├── backend/
├── frontend/
├── infrastructure/
│   └── docker/
│       └── docker-compose.yml
│
└── docs/
    ├── DECISIONES.md                 ← Leerlo (en repo)
    ├── PLAN_FASES.md                 ← Leerlo (en repo)
    └── CLAUDE.md                     ← Leerlo (en repo)
```

### En el ZIP de entrega

```
Este ZIP contiene:
- Guías de Sebastián (INICIO_RAPIDO, ONBOARDING)
- Referencias rápidas (.txt con snippets)
- Template de .env (NUNCA va el .env real en el ZIP)
- Checklist de validación
```

---

## 🔑 El archivo más importante: `.env`

### ⚠️ CRÍTICO: NO está en el ZIP

El archivo `.env` real **NUNCA va en el ZIP** (contiene credenciales).

**Cómo funciona:**
1. Hernán prepara `.env` con valores reales
2. Hernán pasa `.env` **POR OTRA VÍA** (en persona, 1Password, etc.)
3. Sebastián coloca `.env` en la raíz del proyecto
4. `docker compose` lee automáticamente el `.env`

**El ZIP incluye `.env.template`:**
- Es solo REFERENCIA (qué variables existen)
- Tiene valores placeholders `<CAMBIAR>`
- Sirve para saber qué credenciales pedir

---

## 📱 Resumen visual — Qué Sebastián necesita

```
Día 1: Recibe 2 cosas
  ├── ZIP con guías (este archivo)
  └── .env real (de Hernán, por otra vía)

Día 1: Ejecuta
  ├── Descomprime ZIP
  ├── Lee LEEME_PRIMERO.md
  ├── Lee 1_INICIO_RAPIDO/INICIO_RAPIDO_SEBASTIAN.md
  ├── Copia .env real a la carpeta del proyecto
  ├── Ejecuta docker compose ... up -d
  └── Verifica que todo levantó (docker ps)

Día 2-3: Lee
  ├── 2_GUIA_COMPLETA/ONBOARDING_SEBASTIAN.md (2-3 horas)
  ├── Consulta 3_REFERENCIAS_RAPIDAS/ según necesite
  └── Reporta dudas a Hernán

Día 3: Validación
  ├── Hernán verifica que Sebastián levantó todo correctamente
  ├── Asigna tareas de Sprint 12
  └── ✅ Sebastián está listo
```

---

## 🎯 Ruta rápida si ya tienes prisa

**Si solo tienes 30 minutos:**

1. Descomprime el ZIP
2. Lee `LEEME_PRIMERO.md` (5 min)
3. Lee `1_INICIO_RAPIDO/INICIO_RAPIDO_SEBASTIAN.md` (5 min)
4. Ejecuta los 4 pasos (20 min)
5. Valida que funciona (5 min)

Después, cuando tengas más tiempo, lee la guía completa.

---

## ❓ Preguntas frecuentes

**P: ¿Dónde está el `.env` real?**  
R: No está en el ZIP. Hernán lo pasa por otra vía (en persona, 1Password, etc.). El ZIP solo tiene `.env.template` como referencia.

**P: ¿Qué hago si me falta una credencial?**  
R: Mira `.env.template` para ver qué falta. Preguntá a Hernán por esa variable específica.

**P: ¿Puedo compartir el ZIP con alguien más?**  
R: Sí, el ZIP es seguro (no tiene credenciales). Pero NO compartas el `.env` real que recibas de Hernán.

**P: ¿Las URLs de localhost en `3_REFERENCIAS_RAPIDAS/URLS_UTILES.txt` están actualizadas?**  
R: Sí. Pero si cambias puertos en docker-compose, actualiza esas URLs.

**P: ¿Qué pasa si borré el ZIP después de descomprimir?**  
R: Los documentos están también en el repositorio. Si los necesitás, clona el repo.

---

## ✅ Checklist de Sebastián

Una vez descomprimido el ZIP:

- [ ] Descomprimí el ZIP exitosamente
- [ ] Leí `LEEME_PRIMERO.md`
- [ ] Ejecuté los 4 pasos de `INICIO_RAPIDO_SEBASTIAN.md`
- [ ] Docker levantó (14/14 healthy)
- [ ] Frontend carga en http://localhost:5173
- [ ] Pude loguear con admin@escuela.local / Admin123!
- [ ] Coloqué `.env` real en la carpeta correcta
- [ ] Leí `ONBOARDING_SEBASTIAN.md` completamente
- [ ] Marqué todos los checks de `5_CHECKLIST/VALIDACION_POST-SETUP.md`

Si todos ✅, **estás listo para trabajar en Sprint 12**. 🚀

---

**¡Bienvenido al proyecto!**

Próximo paso: Leé `LEEME_PRIMERO.md` que está en el ZIP.

