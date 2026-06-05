# 🚀 Argo CD + Minikube - Setup Guía Completa

## 📋 Índice
1. [Requisitos](#requisitos)
2. [Instalación de Minikube](#instalación-de-minikube)
3. [Instalación de Argo CD](#instalación-de-argocd)
4. [Build de imágenes Docker](#build-de-imágenes-docker)
5. [Despliegue de la aplicación](#despliegue-de-la-aplicación)
6. [Verificación y sincronización](#verificación-y-sincronización)
7. [Cambios y sincronización en tiempo real](#cambios-y-sincronización-en-tiempo-real)
8. [Troubleshooting](#troubleshooting)

---

## 📦 Requisitos

- **Minikube** instalado (Linux/Mac/Windows)
- **kubectl** v1.24+
- **Docker Desktop** o Docker Engine
- **Git** configurado
- Espacio en disco: mín. 10 GB

### Verificar requisitos:
```bash
minikube version
kubectl version --client
docker version
```

---

## 🔧 Instalación de Minikube

### 1. Iniciar Minikube (si no está corriendo)
```bash
# Windows (PowerShell con permisos admin)
minikube start --cpus=4 --memory=8192 --driver=hyperv

# Linux/Mac
minikube start --cpus=4 --memory=8192 --driver=docker
```

### 2. Verificar estado
```bash
minikube status
kubectl cluster-info
```

### 3. Habilitar ingress (opcional, para exponer aplicaciones)
```bash
minikube addons enable ingress
```

---

## 🎯 Instalación de Argo CD

### Paso 1: Crear namespace para Argo CD
```bash
kubectl create namespace argocd
```

### Paso 2: Descargar e instalar Argo CD (última versión estable)
```bash
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### Paso 3: Esperar a que todos los pods estén listos
```bash
kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=argocd-server -n argocd --timeout=300s
```

### Paso 4: Acceder a la interfaz de Argo CD

**Opción A: Port-Forward (recomendado para desarrollo)**
```bash
kubectl port-forward svc/argocd-server -n argocd 8443:443
# Abrir en navegador: https://localhost:8443
```

**Opción B: Exposer con Service NodePort**
```bash
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "NodePort"}}'
# Obtener el puerto:
kubectl get svc argocd-server -n argocd
# Acceder: https://minikube-ip:puerto
```

### Paso 5: Obtener contraseña inicial
```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d && echo
# Usuario: admin
```

**Credenciales Argo CD:**
- Usuario: `admin`
- Contraseña: `(obtenida del comando anterior)`

---

## 🐳 Build de imágenes Docker

Argo CD necesita que las imágenes Docker estén disponibles. Para Minikube, usa el repositorio interno.

### Paso 1: Configurar shell para acceder al Docker de Minikube
```bash
# Windows (PowerShell)
& minikube -p minikube docker-env | Invoke-Expression

# Linux/Mac
eval $(minikube docker-env)
```

### Paso 2: Build de los servicios

**API Gateway:**
```bash
cd backend/api-gateway
mvn clean package -DskipTests
docker build -t api-gateway:latest .
```

**MS-Auth:**
```bash
cd backend/ms-auth
mvn clean package -DskipTests
docker build -t ms-auth:latest .
```

**MS-Estudiantes:**
```bash
cd backend/ms-estudiantes
mvn clean package -DskipTests
docker build -t ms-estudiantes:latest .
```

**Frontend:**
```bash
cd frontend
npm install
npm run build
docker build -t frontend:latest .
```

**Eureka Server** (si no existe, usar imagen pública):
```bash
# Usar una imagen existente o crear un Dockerfile mínimo
docker pull springcloud/eureka:latest
docker tag springcloud/eureka:latest eureka-server:latest
```

### Paso 3: Verificar imágenes construidas
```bash
docker images | grep -E "(api-gateway|ms-auth|ms-estudiantes|frontend|eureka)"
```

---

## 📡 Despliegue de la aplicación

### Paso 1: Crear namespace de la aplicación
```bash
kubectl apply -f kubernetes/argocd/00-namespace.yaml
```

### Paso 2: Crear la Application de Argo CD

**Opción A: Usar kubectl apply**
```bash
# Primero, actualizar el repositorio en argo-app.yaml
# CAMBIAR: https://github.com/tu-usuario/tu-repo.git

kubectl apply -f kubernetes/argocd/argo-app.yaml
```

**Opción B: Usar UI de Argo CD**
1. Ir a Argo CD UI (https://localhost:8443)
2. Click en "+ NEW APP"
3. Completar:
   - **Application Name**: `escuela-conduccion`
   - **Project**: `default`
   - **Repository URL**: (tu repositorio GitHub)
   - **Path**: `kubernetes/argocd`
   - **Destination Cluster**: `https://kubernetes.default.svc`
   - **Destination Namespace**: `escuela-conduccion`
4. Habilitar "Auto-sync" (Prune + Self Heal)
5. Click en "CREATE"

### Paso 3: Sincronizar aplicación
```bash
# Desde CLI (si lo instalaste)
argocd app sync escuela-conduccion

# O manualmente en la UI: botón "Sync"
```

### Paso 4: Esperar a que los pods se inicien
```bash
kubectl get pods -n escuela-conduccion -w
```

---

## ✅ Verificación y sincronización

### Verificar status de Argo CD
```bash
# Ver estado de la aplicación
kubectl get application -n argocd
kubectl describe application escuela-conduccion -n argocd

# Ver eventos de sincronización
kubectl logs -n argocd svc/argocd-application-controller -f
```

### Verificar pods de la aplicación
```bash
kubectl get pods -n escuela-conduccion -o wide
kubectl describe pod <pod-name> -n escuela-conduccion
```

### Verificar servicios
```bash
kubectl get svc -n escuela-conduccion
```

### Acceder a los servicios (Port-Forward)
```bash
# Frontend
kubectl port-forward -n escuela-conduccion svc/frontend 3000:80

# API Gateway
kubectl port-forward -n escuela-conduccion svc/api-gateway 8080:8080

# MS-Auth
kubectl port-forward -n escuela-conduccion svc/ms-auth 8081:8081

# Eureka
kubectl port-forward -n escuela-conduccion svc/eureka 8761:8761
```

---

## 🔄 Cambios y sincronización en tiempo real

### Demostrar GitOps (cambiar réplicas)

#### Paso 1: Cambiar número de réplicas en Git
```bash
# Editar kubernetes/argocd/apps/01-api-gateway.yaml
# Cambiar: replicas: 1 → replicas: 2
```

#### Paso 2: Hacer commit y push
```bash
git add kubernetes/argocd/
git commit -m "Sprint 11 (Aumentar réplicas API Gateway)"
git push origin main
```

#### Paso 3: Observar sincronización automática en Argo CD
```bash
# En la UI: ver el badge de "OutOfSync" → "Synced"
# O con CLI:
kubectl get deployment -n escuela-conduccion -w
```

#### Paso 4: Verificar el nuevo pod
```bash
kubectl get pods -n escuela-conduccion | grep api-gateway
```

---

## 🆘 Troubleshooting

### ❌ Problema: "ImagePullBackOff" en pods

**Causa:** Las imágenes no están disponibles en Minikube.

**Solución:**
```bash
# Configurar Docker del shell para Minikube
eval $(minikube docker-env)  # Linux/Mac
# o
& minikube -p minikube docker-env | Invoke-Expression  # Windows

# Reconstruir imágenes
docker build -t api-gateway:latest ./backend/api-gateway
docker build -t ms-auth:latest ./backend/ms-auth
```

### ❌ Problema: "CrashLoopBackOff" en pods de Java

**Causa:** Falta de memoria o variables de entorno incorrectas.

**Solución:**
```bash
# Ver logs
kubectl logs <pod-name> -n escuela-conduccion -f

# Aumentar memoria en Minikube
minikube stop
minikube start --memory=12288
```

### ❌ Problema: PostgreSQL no se conecta

**Solución:**
```bash
# Verificar si el pod de Postgres está corriendo
kubectl get pods -n escuela-conduccion | grep postgres

# Ver logs de PostgreSQL
kubectl logs postgres-... -n escuela-conduccion -f

# Ejecutar aplicaciones de BD desde dentro del cluster
kubectl exec -it postgres-... -n escuela-conduccion -- psql -U escuela_user -d escuela_db
```

### ❌ Problema: No puedo acceder a la UI de Argo CD

**Solución:**
```bash
# Hacer port-forward nuevamente
kubectl port-forward svc/argocd-server -n argocd 8443:443

# Usar --address=0.0.0.0 para acceso remoto
kubectl port-forward --address=0.0.0.0 svc/argocd-server -n argocd 8443:443
```

---

## 📸 Evidencia para la presentación

Durante tu presentación, captura pantallas de:

1. ✅ **Minikube corriendo**
   ```bash
   minikube status
   kubectl cluster-info
   ```

2. ✅ **Argo CD UI con la aplicación sincronizada**
   - Imagen de la aplicación "escuela-conduccion" en estado "Synced"
   - Árbol de recursos desplegados

3. ✅ **Pods corriendo**
   ```bash
   kubectl get pods -n escuela-conduccion
   ```

4. ✅ **Servicios accesibles**
   - Frontend funcionando (http://localhost:3000)
   - API Gateway respondiendo (http://localhost:8080/actuator/health)

5. ✅ **Cambio en Git → Sincronización automática**
   - Mostrar cambio en manifiestos
   - Mostrar "OutOfSync" en Argo CD
   - Ejecutar Sync automático
   - Mostrar "Synced" nuevamente

---

## 🎬 Script rápido de setup (para automatizar)

Crea un archivo `setup-argocd.sh`:

```bash
#!/bin/bash

set -e

echo "🔧 Starting Minikube..."
minikube start --cpus=4 --memory=8192

echo "📦 Installing Argo CD..."
kubectl create namespace argocd || true
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=argocd-server -n argocd --timeout=300s

echo "🏗️ Creating escuela-conduccion namespace..."
kubectl apply -f kubernetes/argocd/00-namespace.yaml

echo "🐳 Building Docker images (usando Docker de Minikube)..."
eval $(minikube docker-env)
docker build -t api-gateway:latest ./backend/api-gateway
docker build -t ms-auth:latest ./backend/ms-auth
docker build -t ms-estudiantes:latest ./backend/ms-estudiantes
docker build -t frontend:latest ./frontend
docker pull springcloud/eureka:latest && docker tag springcloud/eureka:latest eureka-server:latest

echo "📡 Creating Argo CD Application..."
kubectl apply -f kubernetes/argocd/argo-app.yaml

echo "⏳ Waiting for deployment..."
kubectl wait --for=condition=Ready pod -n escuela-conduccion --all --timeout=600s || true

echo "✅ Setup completo!"
echo "Acceder a Argo CD: kubectl port-forward svc/argocd-server -n argocd 8443:443"
echo "Usuario: admin"
echo "Contraseña: $(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)"
```

Ejecutar:
```bash
chmod +x setup-argocd.sh
./setup-argocd.sh
```

---

## 📚 Recursos adicionales

- **Argo CD Docs**: https://argo-cd.readthedocs.io/
- **Kubernetes Docs**: https://kubernetes.io/docs/
- **Minikube**: https://minikube.sigs.k8s.io/

---

**Grupo:** Software Processes - UDLA  
**Fecha:** Junio 2026  
**Profesor:** [Nombre del docente]
