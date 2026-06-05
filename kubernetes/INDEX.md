# 📑 Índice de Documentación - Argo CD + Kubernetes

## 🎯 Si eres estudiante del grupo...

**Tienes 1 minuto? Empieza aquí:**
→ Lee: [`GUIA-RAPIDA.txt`](GUIA-RAPIDA.txt) (5 min)

**Tienes 10 minutos? Conoce el proyecto:**
→ Lee: [`RESUMEN-EJECUTIVO.md`](RESUMEN-EJECUTIVO.md) (10 min)

**Listo para trabajar? Sigue el orden:**

1. [`README.md`](README.md) - Visión general del proyecto
2. [`SETUP-ARGOCD.md`](SETUP-ARGOCD.md) - Instalación paso a paso
3. [`ARQUITECTURA-ARGOCD.md`](ARQUITECTURA-ARGOCD.md) - Entender cómo funciona
4. [`EVIDENCIA-PRESENTACION.md`](EVIDENCIA-PRESENTACION.md) - Documentar para presentación

---

## 📚 Documentación completa

### Para empezar rápido
- **[GUIA-RAPIDA.txt](GUIA-RAPIDA.txt)** ← Lee primero (5 min)
  - Checklist de qué hacer
  - Comandos principales
  - Problemas comunes
  
- **[RESUMEN-EJECUTIVO.md](RESUMEN-EJECUTIVO.md)** ← Contexto ejecutivo (10 min)
  - Objetivos de la asignación
  - Timeline sugerido
  - Conceptos clave

### Para aprender
- **[README.md](README.md)** ← Visión general del proyecto
  - Estructura de directorios
  - Quick start
  - Conceptos de GitOps

- **[ARQUITECTURA-ARGOCD.md](ARQUITECTURA-ARGOCD.md)** ← Explicación técnica
  - Diagramas de arquitectura
  - Flujo GitOps
  - Componentes de Argo CD
  - Ventajas de la solución

### Para implementar
- **[SETUP-ARGOCD.md](SETUP-ARGOCD.md)** ← Guía paso a paso (¡HACER MIENTRAS LEES!)
  - Requisitos del sistema
  - Instalación de Minikube
  - Instalación de Argo CD
  - Build de imágenes Docker
  - Despliegue de aplicación
  - Verificación y sincronización
  - Troubleshooting detallado

### Para documentar
- **[EVIDENCIA-PRESENTACION.md](EVIDENCIA-PRESENTACION.md)** ← Plantilla de evidencia
  - Screenshots esperadas
  - Explicaciones de cada paso
  - Checklist de evidencia
  - Estructura de presentación sugerida

---

## 🏗️ Manifiestos Kubernetes (GitOps)

Carpeta `argocd/` contiene la **fuente de verdad** de tu infraestructura:

```
argocd/
├── 00-namespace.yaml              # Crea namespace escuela-conduccion
├── argo-app.yaml                  # Define sincronización Argo CD
│
├── infrastructure/
│   ├── 01-postgresql.yaml         # Base de datos PostgreSQL
│   └── 02-eureka.yaml             # Service discovery Eureka
│
└── apps/
    ├── 01-api-gateway.yaml        # API Gateway Spring Cloud
    ├── 02-ms-auth.yaml            # Microservicio de Auth
    ├── 03-ms-estudiantes.yaml     # Microservicio de Estudiantes
    └── 04-frontend.yaml           # Frontend Vue.js 3
```

**⚠️ Importante:** Estos archivos van en Git y son sincronizados automáticamente por Argo CD.

---

## 🔧 Scripts de setup

- **[setup-minikube-argocd.ps1](setup-minikube-argocd.ps1)** ← Para Windows (PowerShell)
  ```powershell
  powershell -ExecutionPolicy Bypass -File setup-minikube-argocd.ps1
  ```

- **[setup-minikube-argocd.sh](setup-minikube-argocd.sh)** ← Para Linux/Mac (Bash)
  ```bash
  chmod +x setup-minikube-argocd.sh
  ./setup-minikube-argocd.sh
  ```

---

## 🎯 Flujos de trabajo

### Flujo 1: Primera vez (Setup inicial)
```
1. Leer RESUMEN-EJECUTIVO.md (5 min)
   ↓
2. Leer SETUP-ARGOCD.md (30 min leyendo + 1h ejecutando)
   ↓
3. Ejecutar script de setup
   ↓
4. Verificar: kubectl get pods -n escuela-conduccion
   ↓
5. Acceder a Argo CD: https://localhost:8443
```

### Flujo 2: Documentación (Para presentación)
```
1. Leer EVIDENCIA-PRESENTACION.md
   ↓
2. Practicar setup 2-3 veces
   ↓
3. Capturar pantallas siguiendo plantilla
   ↓
4. Completar documento de evidencia
   ↓
5. Preparar slides de presentación
```

### Flujo 3: GitOps demo (Sincronización automática)
```
1. Editar kubernetes/argocd/apps/01-api-gateway.yaml
   └─> Cambiar: replicas: 1 → replicas: 3
   ↓
2. git add + git commit + git push
   ↓
3. Esperar 30 segundos
   ↓
4. Argo CD UI muestra "OutOfSync"
   ↓
5. Clickear "Sync" o esperar auto-sync
   ↓
6. Argo CD UI muestra "Synced" ✅
   ↓
7. kubectl get pods muestra 3 replicas
```

---

## ❓ Búsqueda rápida

### Tengo una pregunta sobre...

**Qué es Argo CD?**
→ [`ARQUITECTURA-ARGOCD.md`](ARQUITECTURA-ARGOCD.md) - Sección "¿Qué es Argo CD?"

**Qué es GitOps?**
→ [`RESUMEN-EJECUTIVO.md`](RESUMEN-EJECUTIVO.md) - Sección "Concepto clave: GitOps"

**Cómo instalar Minikube?**
→ [`SETUP-ARGOCD.md`](SETUP-ARGOCD.md) - Sección "Instalación de Minikube"

**Cómo instalar Argo CD?**
→ [`SETUP-ARGOCD.md`](SETUP-ARGOCD.md) - Sección "Instalación de Argo CD"

**Cómo hacer build de imágenes Docker?**
→ [`SETUP-ARGOCD.md`](SETUP-ARGOCD.md) - Sección "Build de imágenes Docker"

**Cómo desplegar la aplicación?**
→ [`SETUP-ARGOCD.md`](SETUP-ARGOCD.md) - Sección "Despliegue de la aplicación"

**Problema: pods en ImagePullBackOff**
→ [`SETUP-ARGOCD.md`](SETUP-ARGOCD.md) - Sección "Troubleshooting"

**Problema: No puedo acceder a Argo CD**
→ [`SETUP-ARGOCD.md`](SETUP-ARGOCD.md) - Sección "Troubleshooting"

**Cómo documentar capturas para presentación?**
→ [`EVIDENCIA-PRESENTACION.md`](EVIDENCIA-PRESENTACION.md) - Toda la página

**Cómo explicar GitOps en la presentación?**
→ [`ARQUITECTURA-ARGOCD.md`](ARQUITECTURA-ARGOCD.md) - Sección "Concepto clave para la presentación"

**Qué comandos necesito?**
→ [`SETUP-ARGOCD.md`](SETUP-ARGOCD.md) - Sección "Common Commands"
→ [`GUIA-RAPIDA.txt`](GUIA-RAPIDA.txt) - Sección "Comandos útiles"

---

## 📅 Timeline sugerido

### Semana 1: Setup (2 horas)
- [ ] Leer RESUMEN-EJECUTIVO.md
- [ ] Leer SETUP-ARGOCD.md
- [ ] Ejecutar script de setup
- [ ] Verificar que todo funciona

### Semana 2: Documentación (2 horas)
- [ ] Practicar setup 2-3 veces
- [ ] Capturar pantallas
- [ ] Completar EVIDENCIA-PRESENTACION.md

### Semana 3: Presentación (3 horas)
- [ ] Preparar slides
- [ ] Ensayar demo en vivo
- [ ] Verificar sin errores
- [ ] Preparar plan B

---

## 📞 Soporte

**Si tienes un error:**
1. Busca en [`SETUP-ARGOCD.md` → Troubleshooting](SETUP-ARGOCD.md#-troubleshooting)
2. Si no está, pregunta al grupo o profesor

**Si tienes una pregunta conceptual:**
1. Lee [`ARQUITECTURA-ARGOCD.md`](ARQUITECTURA-ARGOCD.md)
2. Lee [`RESUMEN-EJECUTIVO.md`](RESUMEN-EJECUTIVO.md)

**Si necesitas recordar un comando:**
1. Consulta [`GUIA-RAPIDA.txt`](GUIA-RAPIDA.txt) - "Comandos útiles"

---

## 🚀 Próximo paso

**Si estás leyendo esto por primera vez:**

1. Abre [`GUIA-RAPIDA.txt`](GUIA-RAPIDA.txt) (5 min read)
2. Luego abre [`RESUMEN-EJECUTIVO.md`](RESUMEN-EJECUTIVO.md) (10 min read)
3. Después ejecuta el setup (`SETUP-ARGOCD.md`)

**¡Éxito con tu proyecto! 🚀**

---

## 📋 Contenido completo de este directorio

```
kubernetes/
├── INDEX.md                         ← Este archivo
├── GUIA-RAPIDA.txt                  ← Empieza aquí (5 min)
├── RESUMEN-EJECUTIVO.md             ← Resumen ejecutivo (10 min)
├── README.md                         ← Visión general
├── SETUP-ARGOCD.md                  ← Guía paso a paso
├── ARQUITECTURA-ARGOCD.md           ← Explicación técnica
├── EVIDENCIA-PRESENTACION.md        ← Plantilla de evidencia
├── setup-minikube-argocd.ps1        ← Script Windows
├── setup-minikube-argocd.sh         ← Script Linux/Mac
│
└── argocd/                          ← MANIFIESTOS KUBERNETES
    ├── 00-namespace.yaml
    ├── argo-app.yaml
    ├── infrastructure/
    │   ├── 01-postgresql.yaml
    │   └── 02-eureka.yaml
    ├── apps/
    │   ├── 01-api-gateway.yaml
    │   ├── 02-ms-auth.yaml
    │   ├── 03-ms-estudiantes.yaml
    │   └── 04-frontend.yaml
    └── secrets/ (para secretos encriptados)
```

---

**Grupo:** Software Processes - UDLA  
**Fecha:** Junio 2026  
**Estado:** ✅ Completo y listo para usar

¡Buena suerte! 🚀
