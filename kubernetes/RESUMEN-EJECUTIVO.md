# 📋 Resumen Ejecutivo - Argo CD + Kubernetes

**Para:** Grupo Software Processes - UDLA  
**Asignatura:** Procesos de Software  
**Entrega:** [Tu fecha]  
**Presentación:** [Tu fecha/hora]

---

## 🎯 ¿Qué tienes que hacer?

### 1️⃣ **Setup inicial (Primer encuentro)**
```bash
# En Windows (PowerShell):
powershell -ExecutionPolicy Bypass -File kubernetes/setup-minikube-argocd.ps1

# O manual (todos los sistemas):
# 1. Iniciar Minikube: minikube start --cpus=4 --memory=8192
# 2. Instalar Argo CD: kubectl apply -n argocd -f (URL)
# 3. Build de imágenes Docker en Minikube
# 4. Crear Application en Argo CD
```

### 2️⃣ **Documentar evidencia (Para presentación)**
Capturar pantallas de:
- ✅ Minikube corriendo
- ✅ Argo CD UI accesible
- ✅ Pods de aplicación en Kubernetes
- ✅ Frontend funcionando
- ✅ Cambio en Git → Sincronización automática
- ✅ Estado Synced en Argo CD

(Ver: `kubernetes/EVIDENCIA-PRESENTACION.md`)

### 3️⃣ **Preparar presentación (Antes de exponer)**
- 10-15 minutos de slides
- 5-10 minutos de demo en vivo
- Explicar qué es Argo CD y GitOps
- Mostrar sincronización automática

---

## 📚 Documentos en orden de lectura

| # | Documento | Para qué | Tiempo |
|---|-----------|----------|--------|
| 1 | **README.md** | Entender la estructura | 5 min |
| 2 | **SETUP-ARGOCD.md** | Hacer el setup paso a paso | 30 min |
| 3 | **ARQUITECTURA-ARGOCD.md** | Entender cómo funciona | 15 min |
| 4 | **EVIDENCIA-PRESENTACION.md** | Documentar capturas | 30 min |

---

## ⚡ Timeline sugerido

### **Semana 1: Setup**
- [ ] Leer README.md y SETUP-ARGOCD.md
- [ ] Ejecutar setup (script o manual)
- [ ] Verificar que todo funciona
- [ ] Tiempo: ~2 horas

### **Semana 2: Documentación**
- [ ] Practicar el setup 2-3 veces
- [ ] Capturar pantallas siguiendo EVIDENCIA-PRESENTACION.md
- [ ] Completar documento de evidencia
- [ ] Tiempo: ~2 horas

### **Semana 3: Presentación**
- [ ] Preparar slides
- [ ] Practicar demo en vivo
- [ ] Verificar que todo funciona sin errores
- [ ] Preparar plan B (capturas de respaldo)
- [ ] Tiempo: ~3 horas

---

## 🏆 Qué van a aprender

### Conceptos técnicos:
- **Kubernetes** - Orquestación de contenedores
- **GitOps** - Infraestructura como código
- **Argo CD** - Continuous Deployment declarativo
- **Minikube** - Cluster local para desarrollo
- **Docker** - Containerización de aplicaciones

### Habilidades prácticas:
- Setup de Kubernetes local
- Instalación y configuración de Argo CD
- Despliegue de aplicaciones multi-contenedor
- Demostración de sincronización automática
- Documentación técnica profesional

---

## 🔑 Concepto clave: GitOps

```
Git (repositorio)
    ↓ (Argo CD watchea cambios)
Cambios detectados
    ↓ (auto-sync)
kubectl apply nuevos manifiestos
    ↓
Kubernetes se actualiza
    ↓
Resultado: Infraestructura = Código en Git
```

**Lo importante:**
- Git es la "fuente de verdad"
- Cambios en Git → automáticamente en Kubernetes
- Si alguien modifica Kubernetes manualmente → Argo CD lo revierte
- No hay comandos kubectl manuales, todo es declarativo

---

## 📦 Estructura de carpetas

```
kubernetes/
├── README.md                        ← EMPIEZA AQUÍ
├── SETUP-ARGOCD.md                  ← Guía paso a paso
├── ARQUITECTURA-ARGOCD.md           ← Explicación técnica
├── EVIDENCIA-PRESENTACION.md        ← Para documentar capturas
├── RESUMEN-EJECUTIVO.md             ← Este archivo
├── setup-minikube-argocd.sh         ← Script Linux/Mac
├── setup-minikube-argocd.ps1        ← Script PowerShell (Windows)
│
└── argocd/                          ← FUENTE DE VERDAD (GitOps)
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
    └── secrets/
```

---

## 🎯 Objetivos de la asignación (Checklist)

- [ ] **Desplegar aplicación funcional** usando Argo CD
- [ ] **Evidenciar sincronización** - cambios en Git → Kubernetes
- [ ] **Documentar con capturas** - mínimo 10 screenshots
- [ ] **Manifiestos YAML** - en `kubernetes/argocd/`
- [ ] **Presentación técnica** - 15-20 minutos en clase
- [ ] **Demostración en vivo** - GitOps funcionando

---

## 🚨 Común problemas y soluciones

| Problema | Solución |
|----------|----------|
| Pods en "ImagePullBackOff" | Reconstruir imágenes Docker: `eval $(minikube docker-env)` + `docker build ...` |
| No puedo acceder a Argo CD | Hacer port-forward: `kubectl port-forward svc/argocd-server -n argocd 8443:443` |
| Application no sincroniza | Editar `argo-app.yaml` con tu URL de repo y hacer `kubectl apply` |
| PostgreSQL falla | Ver logs: `kubectl logs postgres-... -n escuela-conduccion -f` |
| Minikube sin memoria | Aumentar: `minikube stop` → `minikube start --memory=12288` |

(Más detalles en SETUP-ARGOCD.md → Troubleshooting)

---

## 📞 Preguntas frecuentes

**P: ¿Qué es Argo CD?**  
R: Es una herramienta que sincroniza aplicaciones Kubernetes con lo que dicen tus manifiestos YAML en Git. Si cambias algo en Git, automáticamente lo aplica en el cluster.

**P: ¿Qué es GitOps?**  
R: Es un paradigma donde Git es la fuente de verdad de todo tu infraestructura. Cambios = commits, rollbacks = revertir commits, auditoría = git history.

**P: ¿Necesito Kubernetes en producción?**  
R: Para esta asignación, no. Minikube es suficiente (es Kubernetes local en tu máquina).

**P: ¿Qué pasa si algo se rompe en la demo?**  
R: Prepara capturas de respaldo y un plan B. Explica conceptualmente qué iba a pasar.

**P: ¿Cuánto tiempo se toma todo?**  
R: Setup: 1-2h | Documentación: 1-2h | Presentación: 1h (total ~4-5h repartidas en 3 semanas)

---

## 📋 Checklist antes de presentar

### Preparación técnica:
- [ ] Minikube iniciado y corriendo
- [ ] Argo CD instalado y accesible
- [ ] Imágenes Docker construidas
- [ ] Application creada en Argo CD
- [ ] Pods de la aplicación corriendo
- [ ] Frontend accesible en navegador
- [ ] Prueba de sincronización (Git → Kubernetes)

### Documentación:
- [ ] README.md completado
- [ ] EVIDENCIA-PRESENTACION.md con screenshots
- [ ] Manifiestos YAML en `kubernetes/argocd/`
- [ ] Documento de evidencia lista para entregar

### Presentación:
- [ ] Slides preparadas (5-10 slides)
- [ ] Demo ensayada 2-3 veces
- [ ] Explicación clara de GitOps
- [ ] Capturas de respaldo en caso de fallo
- [ ] Tiempo practícado (15-20 min total)

---

## 🎓 Qué explicar en la presentación

### Slide 1: Introducción
- Nombre del proyecto
- Qué es Argo CD
- Por qué usamos GitOps

### Slide 2: Arquitectura
- Diagrama: Git → Argo CD → Kubernetes
- Componentes principales

### Slide 3-4: Demo en vivo
- Mostrar Minikube
- Mostrar Argo CD UI
- Mostrar aplicación en navegador

### Slide 5-6: GitOps en acción
- Cambio en Git (mostrar diff)
- OutOfSync en Argo CD
- Sincronización automática
- Synced confirmado

### Slide 7: Conclusiones
- Ventajas de GitOps
- Casos de uso
- Lecciones aprendidas

---

## 🚀 Comando rápido para empezar

```bash
# 1. Terminal 1 - Setup
powershell -ExecutionPolicy Bypass -File kubernetes/setup-minikube-argocd.ps1

# 2. Terminal 2 - Ver Argo CD (esperar 1 min después del setup)
kubectl port-forward svc/argocd-server -n argocd 8443:443
# Abrir: https://localhost:8443 (usuario: admin, pass: ver terminal 1)

# 3. Terminal 3 - Ver Frontend (cuando los pods estén running)
kubectl port-forward svc/frontend -n escuela-conduccion 3000:80
# Abrir: http://localhost:3000

# 4. Terminal 4 - Watch logs
kubectl logs -f deployment/api-gateway -n escuela-conduccion
```

---

## 📞 Soporte y referencias

- **Script de setup:** `kubernetes/setup-minikube-argocd.ps1` (Windows) o `.sh` (Linux/Mac)
- **Documentación completa:** `kubernetes/SETUP-ARGOCD.md`
- **Argo CD oficial:** https://argo-cd.readthedocs.io/
- **Kubernetes docs:** https://kubernetes.io/docs/

---

## ✅ Resumen

Tienes:
1. ✅ Manifiestos Kubernetes listos en `kubernetes/argocd/`
2. ✅ Script de setup para automatizar
3. ✅ Documentación paso a paso
4. ✅ Plantilla de evidencia para capturas
5. ✅ Todo listo para una presentación profesional

**Próximo paso:** Leer `kubernetes/README.md` y ejecutar el setup.

---

**¡Éxito con tu presentación! 🚀**

Grupo: Software Processes - UDLA  
Junio 2026
