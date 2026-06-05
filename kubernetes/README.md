# 🚀 Argo CD + Kubernetes - Proyecto de Software Processes

**Estado:** ✅ Listo para deployment  
**Última actualización:** Junio 2026  
**Grupo:** Software Processes - UDLA

---

## 📚 Documentación

Este directorio contiene la implementación de **Argo CD** para desplegar la aplicación de escuela de conducción en **Minikube** usando **GitOps**.

### 📖 Guías (leer en este orden):

1. **[SETUP-ARGOCD.md](SETUP-ARGOCD.md)** ← **EMPIEZA AQUÍ**
   - Instalación paso a paso de Minikube + Argo CD
   - Build de imágenes Docker
   - Despliegue de la aplicación
   - Troubleshooting

2. **[ARQUITECTURA-ARGOCD.md](ARQUITECTURA-ARGOCD.md)**
   - Explicación de la arquitectura
   - Diagramas conceptuales
   - Flujos de GitOps
   - Componentes de Argo CD

3. **[EVIDENCIA-PRESENTACION.md](EVIDENCIA-PRESENTACION.md)**
   - Plantilla para documentar capturas
   - Checklist de evidencia
   - Sugerencias para la presentación

---

## 🏗️ Estructura de directorios

```
kubernetes/
├── README.md                        # Este archivo
├── SETUP-ARGOCD.md                  # Guía de instalación
├── ARQUITECTURA-ARGOCD.md           # Explicación técnica
├── EVIDENCIA-PRESENTACION.md        # Plantilla de evidencia
│
├── argocd/                          # ← FUENTE DE VERDAD (GitOps)
│   ├── 00-namespace.yaml            # Namespace base
│   ├── argo-app.yaml                # Application de Argo CD
│   │
│   ├── infrastructure/              # Servicios compartidos
│   │   ├── 01-postgresql.yaml       # Base de datos
│   │   └── 02-eureka.yaml           # Service discovery
│   │
│   ├── apps/                        # Aplicaciones
│   │   ├── 01-api-gateway.yaml      # API Gateway
│   │   ├── 02-ms-auth.yaml          # Microservicio Auth
│   │   ├── 03-ms-estudiantes.yaml   # Microservicio Estudiantes
│   │   └── 04-frontend.yaml         # Frontend Vue.js
│   │
│   └── secrets/                     # Secretos (opcional)
│
└── scripts/                         # Scripts auxiliares (crear)
    └── setup.sh                     # Automatización
```

---

## ⚡ Quick Start (3 minutos)

### Si quieres empezar YA sin leer la guía completa:

```bash
# 1. Iniciar Minikube
minikube start --cpus=4 --memory=8192

# 2. Instalar Argo CD
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# 3. Acceder a Argo CD
kubectl port-forward svc/argocd-server -n argocd 8443:443

# 4. En navegador: https://localhost:8443
# Usuario: admin
# Contraseña: (ejecutar comando en SETUP-ARGOCD.md)

# 5. Build de imágenes (en otra terminal)
eval $(minikube docker-env)  # Linux/Mac
docker build -t api-gateway:latest ./backend/api-gateway
docker build -t ms-auth:latest ./backend/ms-auth
docker build -t ms-estudiantes:latest ./backend/ms-estudiantes
docker build -t frontend:latest ./frontend
docker pull springcloud/eureka:latest && docker tag springcloud/eureka:latest eureka-server:latest

# 6. Crear la Application (en Argo CD UI o CLI)
kubectl apply -f kubernetes/argocd/argo-app.yaml
```

**⚠️ Importante:** Editar `kubernetes/argocd/argo-app.yaml` con tu repositorio antes del paso 6.

---

## 🔍 ¿Cómo verificar que todo funciona?

```bash
# 1. Verificar Argo CD
kubectl get pods -n argocd

# 2. Verificar aplicación en Kubernetes
kubectl get pods -n escuela-conduccion
kubectl get svc -n escuela-conduccion

# 3. Acceder a los servicios
kubectl port-forward svc/frontend -n escuela-conduccion 3000:80
# Abrir http://localhost:3000 en navegador

# 4. Ver estado en Argo CD UI
# https://localhost:8443 → App "escuela-conduccion" debe estar "Synced" ✅
```

---

## 🎯 Objetivos de la asignación

✅ Desplegar aplicación usando Argo CD  
✅ Evidenciar sincronización GitOps (cambios en Git → cluster)  
✅ Documentar con capturas de pantalla  
✅ Presentación técnica en clase  

---

## 📡 GitOps: El concepto clave

**Git = Source of Truth (Fuente de Verdad)**

```
Cambio en Git → Argo CD lo detecta → Aplica en Kubernetes → Sincronizado ✅
```

### Ejemplo práctico:

**Cambio 1: Aumentar réplicas**
```bash
# Editar: kubernetes/argocd/apps/01-api-gateway.yaml
# Cambiar: replicas: 1 → replicas: 3
# Commit y push
# Resultado: Argo CD crea 2 pods adicionales automáticamente
```

**Cambio 2: Cambiar imagen**
```bash
# Editar: kubernetes/argocd/apps/01-api-gateway.yaml
# Cambiar: image: api-gateway:v1 → image: api-gateway:v2
# Commit y push
# Resultado: Argo CD reinicia pods con la nueva imagen
```

---

## 🔄 Flujo típico de trabajo

```
1. Developer modifica manifiestos Kubernetes
   └── Edita archivos en kubernetes/argocd/

2. Developer hace commit y push
   └── git commit -m "Sprint 11 (Cambio)"
   └── git push origin main

3. Argo CD detecta cambio (automático cada 3 min)
   └── Compara desired (Git) vs actual (Kubernetes)
   └── Muestra estado "OutOfSync"

4. Argo CD sincroniza automáticamente (si auto-sync enabled)
   └── Aplica manifiestos con kubectl
   └── Kubernetes ajusta los pods
   └── Estado cambia a "Synced" ✅

5. Verificar en Kubernetes
   └── kubectl get pods, svc, etc.
   └── Todo coincide con lo que dice Git
```

---

## 🆘 Troubleshooting rápido

| Problema | Solución |
|----------|----------|
| Pods en "ImagePullBackOff" | Reconstruir imágenes Docker en Minikube |
| "Cannot connect to Argo CD" | `kubectl port-forward svc/argocd-server -n argocd 8443:443` |
| "OutOfSync nunca cambia a Synced" | Habilitar auto-sync en Argo CD o clickear "Sync" |
| PostgreSQL falla | `kubectl logs postgres-... -n escuela-conduccion` |
| Application no existe en Argo CD | `kubectl apply -f kubernetes/argocd/argo-app.yaml` |

Más detalles en: [SETUP-ARGOCD.md#troubleshooting](SETUP-ARGOCD.md#-troubleshooting)

---

## 📸 Para la presentación

Necesitarán capturar y documentar:

- [ ] Minikube corriendo (`minikube status`)
- [ ] Argo CD UI accesible y login exitoso
- [ ] Pods de la aplicación corriendo
- [ ] Frontend funcionando en navegador
- [ ] API Gateway respondiendo requests
- [ ] Cambio en Git (mostrar diff)
- [ ] Estado OutOfSync en Argo CD
- [ ] Sincronización automática
- [ ] Estado Synced confirmado
- [ ] (Opcional) Self-healing: pod eliminado y recreado automáticamente

Ver [EVIDENCIA-PRESENTACION.md](EVIDENCIA-PRESENTACION.md) para plantilla completa.

---

## 🔑 Conceptos clave para explicar en la presentación

### 1. **Declarativo vs Imperativo**
- ❌ Imperativo: "ejecuta este comando" (`kubectl run ...`)
- ✅ Declarativo: "aquí está el estado deseado" (archivos YAML)

### 2. **Source of Truth**
- Git contiene los manifiestos (source of truth)
- Kubernetes intenta coincidir con lo que dice Git
- Si hay diferencia → Argo CD lo sincroniza

### 3. **Continuous Deployment**
- No requiere pipeline CI/CD tradicional
- Cambios en Git → automáticamente en cluster
- En segundos, no en horas

### 4. **Self-Healing**
- Alguien borra un pod → Argo CD lo recrea
- Cambio manual en Kubernetes → Argo CD lo revierte
- La infraestructura siempre coincide con el código

---

## 📋 Checklist antes de presentar

- [ ] Todos los manifiestos en `kubernetes/argocd/` están listos
- [ ] Argo CD instalado en Minikube
- [ ] Imágenes Docker construidas y accesibles en Minikube
- [ ] Application creada en Argo CD y sincronizada
- [ ] Frontend accesible en navegador
- [ ] Documento de evidencia completado con capturas
- [ ] Presentación preparada (diapositivas)
- [ ] Prueba en vivo del cambio Git → sincronización
- [ ] Contingencia: capturas de respaldo en caso de fallo

---

## 🚀 Próximos pasos

1. **Leer [SETUP-ARGOCD.md](SETUP-ARGOCD.md)** - Guía paso a paso
2. **Leer [ARQUITECTURA-ARGOCD.md](ARQUITECTURA-ARGOCD.md)** - Entender conceptos
3. **Practicar setup** - Hacer todo 2-3 veces antes de presentar
4. **Documentar evidencia** - Capturar pantallas en [EVIDENCIA-PRESENTACION.md](EVIDENCIA-PRESENTACION.md)
5. **Preparar presentación** - Slides + demostración en vivo

---

## 📞 Soporte

**Si algo falla:**
1. Consulta la sección "Troubleshooting" en [SETUP-ARGOCD.md](SETUP-ARGOCD.md)
2. Ver logs: `kubectl logs <pod-name> -n argocd` o `kubectl logs <pod-name> -n escuela-conduccion`
3. Revisar estado: `kubectl describe pod <pod-name> -n <namespace>`

---

## 📚 Referencias

- [Argo CD Docs](https://argo-cd.readthedocs.io/)
- [Kubernetes Docs](https://kubernetes.io/docs/)
- [Minikube](https://minikube.sigs.k8s.io/)
- [GitOps Principles](https://gitops.tech/)

---

**Grupo:** Software Processes - UDLA  
**Asignatura:** Procesos de Software  
**Profesor:** [Nombre del docente]  
**Fecha:** Junio 2026
