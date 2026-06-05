# ⚡ GUÍA RÁPIDA: ARGO CD + KUBERNETES (TL;DR)

**Para los que necesitan ir rápido.** Paso a paso sin explicación.

---

## 1️⃣ INSTALACIÓN (15 min)

```bash
# Terminal 1: Instalar y arrancar Minikube
choco install minikube kubernetes-cli

minikube start \
  --cpus=4 \
  --memory=8192 \
  --driver=docker

# Esperar a que esté Ready
kubectl get nodes
# → minikube Ready
```

---

## 2️⃣ BUILD DE IMÁGENES (25 min)

```bash
# Terminal: En carpeta backend/
cd backend

# Activar Minikube Docker daemon
eval $(minikube docker-env)

# Compilar todas las imágenes
chmod +x ../build-all-images.sh
./build-all-images.sh

# Esperar a que terminen (~20 min)
# Resultado: 10 imágenes en Minikube ✅
```

---

## 3️⃣ INSTALAR ARGO CD (5 min)

```bash
# Terminal: Crear namespace y instalar
kubectl create namespace argocd

kubectl apply -n argocd -f \
  https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Esperar a que pods estén Running (~2 min)
kubectl get pods -n argocd
```

---

## 4️⃣ ACCEDER A ARGO CD (2 min)

```bash
# Terminal 1: Port-forward (dejar abierta)
kubectl port-forward svc/argocd-server -n argocd 8443:443

# Terminal 2: Obtener contraseña
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d

# Navegador
https://localhost:8443
Usuario: admin
Contraseña: (la anterior)
```

---

## 5️⃣ CONECTAR REPOSITORIO GIT (2 min)

En Argo CD UI:

```
Settings → Repositories → Connect Repo

Connection Method: HTTPS
Repository URL: https://github.com/tu-usuario/proyecto-titulacion
Username: tu-usuario
Password: (GitHub personal access token)

Connect
```

---

## 6️⃣ CREAR APPLICATION (1 min)

```bash
# En terminal (desde raíz del proyecto)
kubectl apply -f kubernetes/argocd-application.yml

# Esperar a que aparezca en Argo CD (~30 seg)
```

O en Argo CD UI:

```
New Application
  Name: proyecto-titulacion
  Repository: tu-repo
  Path: kubernetes/overlays/dev
  Destination: Local cluster / escuela
  Sync Policy: Automatic
Create
```

---

## 7️⃣ SINCRONIZAR (2 min)

```bash
# Terminal o UI
# Terminal:
argocd app sync proyecto-titulacion

# O en UI: Applications → proyecto-titulacion → Sync
```

**Esperar a que todos los pods estén Running:**

```bash
kubectl get pods -n escuela --watch
```

---

## 8️⃣ VERIFICAR (3 min)

```bash
# 1. Ver todos los resources
kubectl get all -n escuela

# 2. Port-forwards a servicios
# Terminal A:
kubectl port-forward -n escuela svc/dev-api-gateway 8080:8080

# Terminal B:
kubectl port-forward -n escuela svc/dev-eureka-server 8761:8761

# Terminal C:
kubectl port-forward -n escuela svc/dev-rabbitmq 15672:15672

# 3. Probar endpoints
curl http://localhost:8080/actuator/health
# Esperar: {"status":"UP"} ✅

# 4. Ver Eureka
# Navegador: http://localhost:8761
# Verificar: 10 apps registered ✅
```

---

## 🎬 DEMO GITOPS (5 min)

### Cambiar replicas

```bash
# Editar
nano kubernetes/overlays/dev/kustomization.yml

# Cambiar:
# replicas:
#   - name: api-gateway
#     count: 2    ← CAMBIO

# Commit
git add kubernetes/overlays/dev/kustomization.yml
git commit -m "Sprint 12 (Demo - Aumentar replicas)"
git push origin main

# Observar en Argo CD UI
# Status: OutOfSync → Syncing → Synced ✅

# Verificar en cluster
kubectl get deployment -n escuela | grep api-gateway
# → 2/2 Ready ✅
```

---

## 📋 CHECKLIST FINAL

- [ ] Minikube running (`minikube status`)
- [ ] 10 imágenes compiladas (`docker images | grep escuela`)
- [ ] Argo CD instalado (`kubectl get pods -n argocd`)
- [ ] Repositorio conectado (Argo CD Settings)
- [ ] Application creada (Argo CD Applications)
- [ ] 12+ pods running (`kubectl get pods -n escuela`)
- [ ] API Gateway responde (`curl http://localhost:8080/actuator/health`)
- [ ] Eureka registra servicios (`http://localhost:8761`)
- [ ] Argo CD muestra Synced ✅
- [ ] Demo GitOps funciona (cambio en Git → sincronización automática)

---

## 🆘 PROBLEMAS COMUNES

### Problema 1: Pods en CrashLoopBackOff

```bash
kubectl logs -f <pod-name> -n escuela
# Ver qué error hay
# Causa común: Postgres/RabbitMQ no vivo
# Esperar a que estén Ready primero
```

### Problema 2: Imágenes no encontradas

```bash
# Verificar que docker apunta a Minikube
eval $(minikube docker-env)

# Recompile
./build-all-images.sh
```

### Problema 3: Argo CD no sincroniza

```bash
# Verificar repositorio Git
kubectl describe application proyecto-titulacion -n argocd

# Verificar path existe
ls kubernetes/overlays/dev/

# Validar YAML
kustomize build kubernetes/overlays/dev/
```

### Problema 4: Port-forward falla

```bash
# Puerto ya en uso
# Cambiar puerto local:
kubectl port-forward -n escuela svc/dev-api-gateway 8090:8080
# Ahora acceder a http://localhost:8090
```

---

## 📚 ENTREGABLES

1. ✅ Carpeta `/kubernetes` con manifiestos
2. ✅ `kubernetes/argocd-application.yml`
3. ✅ `IMPLEMENTACION-ARGOCD.md` (documentación completa)
4. ✅ `PRESENTACION-TECNICA-ARGOCD.md` (para presentación en clase)
5. ✅ Screenshots de:
   - Git commits
   - Argo CD Dashboard
   - Cluster running
   - Demo en vivo

---

## 🎯 VERIFICAR ANTES DE PRESENTAR

```bash
# Debe pasar TODO esto sin errores:

# 1. Minikube vivo
minikube status

# 2. Cluster sano
kubectl get nodes
kubectl get all -n escuela | wc -l  # Debe ser >30

# 3. Argo CD sano
kubectl get pods -n argocd | grep Running | wc -l  # Debe ser >=5

# 4. Application sincronizada
argocd app get proyecto-titulacion | grep -i "sync.*synced"

# 5. Servicios responden
curl http://localhost:8080/actuator/health
curl http://localhost:8761/eureka/apps | grep UP | wc -l  # Debe ser >=10
```

---

## ⏱️ TIEMPO TOTAL

- Instalación: 15 min
- Build imágenes: 25 min
- Argo CD setup: 10 min
- Deploy: 5 min
- Verificación: 5 min
- Demo: 5 min

**TOTAL: ~65 minutos (1 hora)**

---

**Generado para:** Presentación en clase  
**Versión:** 1.0  
**Fecha:** 2026-06-05

