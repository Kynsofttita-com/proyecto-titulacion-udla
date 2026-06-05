# 🚀 IMPLEMENTACIÓN DE ARGO CD + KUBERNETES MINIKUBE

**Proyecto:** Sistema de Control Administrativo y Financiero para Escuelas de Conducción  
**Estudiantes:** Raúl Sebastián Cruz Baño, Hernán Mateo Jurado Moran  
**Asignatura:** Proyecto de Titulación - Kapstone  
**Institución:** Universidad de las Américas (UDLA)  
**Fecha:** 2026-06-05  

---

## 📋 TABLA DE CONTENIDOS

1. [Objetivo](#objetivo)
2. [Requisitos Previos](#requisitos-previos)
3. [Arquitectura Propuesta](#arquitectura-propuesta)
4. [Fase 1: Instalación de Herramientas](#fase-1-instalación-de-herramientas)
5. [Fase 2: Preparación del Repositorio](#fase-2-preparación-del-repositorio)
6. [Fase 3: Build de Imágenes Docker](#fase-3-build-de-imágenes-docker)
7. [Fase 4: Configuración de Argo CD](#fase-4-configuración-de-argo-cd)
8. [Fase 5: Despliegue Inicial](#fase-5-despliegue-inicial)
9. [Fase 6: Validación del Sistema](#fase-6-validación-del-sistema)
10. [Fase 7: GitOps en Acción](#fase-7-gitops-en-acción)
11. [Troubleshooting](#troubleshooting)

---

## OBJETIVO

Implementar un flujo **GitOps completo** usando **Argo CD** donde:

✅ **La fuente de verdad es Git:** Todos los cambios en infraestructura están en `/kubernetes`  
✅ **Auto-sincronización:** Cambios en Git → Automáticamente aplicados en Kubernetes  
✅ **Declarativo:** Definir estado deseado en YAML, no comandos imperativos  
✅ **Auditable:** Historial completo de quién cambió qué y cuándo  
✅ **Rollback instantáneo:** Revertir a versión anterior con un commit  

---

## REQUISITOS PREVIOS

### Hardware Mínimo
- **CPU:** 4 cores (Intel/AMD)
- **RAM:** 8 GB mínimo
- **Disk:** 30 GB libres
- **SO:** Windows 11 Pro / macOS / Linux

### Software Requerido
```
✅ Docker Desktop (instalado)
✅ Minikube v1.30+
✅ kubectl v1.28+
✅ Git v2.40+
✅ Acceso a GitHub (cuenta + SSH key configurada)
```

### Verificar Instalación
```bash
# En terminal (PowerShell/bash)
docker --version          # → Docker version 24.x.x
minikube version          # → minikube version: v1.30+
kubectl version --client  # → v1.28+
git --version             # → git version 2.40+
```

---

## ARQUITECTURA PROPUESTA

### Flujo GitOps (Argo CD)

```
┌──────────────────────────────────────────────────────────────┐
│                         GIT (GitHub)                          │
│  proyecto-titulacion/kubernetes/overlays/dev/kustomization   │
└──────────┬───────────────────────────────────────────────────┘
           │
           │ git push
           │
┌──────────▼───────────────────────────────────────────────────┐
│              Argo CD (Minikube: argocd namespace)             │
│  "Sincronizar cambios de Git al cluster Kubernetes"          │
└──────────┬───────────────────────────────────────────────────┘
           │
           │ kubectl apply
           │
┌──────────▼───────────────────────────────────────────────────┐
│         Kubernetes Cluster (Minikube: escuela namespace)      │
│  ├─ PostgreSQL                                               │
│  ├─ RabbitMQ                                                 │
│  ├─ MinIO                                                    │
│  ├─ Eureka Server                                            │
│  ├─ API Gateway                                              │
│  └─ 8 Microservicios                                         │
└──────────────────────────────────────────────────────────────┘
```

### Estructura de Directorios

```
proyecto-titulacion/
├── .git/                              (Repositorio Git)
├── kubernetes/                        ✨ NUEVA CARPETA
│   ├── base/                          (Recursos base comunes)
│   │   ├── postgres.yml
│   │   ├── rabbitmq.yml
│   │   ├── minio.yml
│   │   ├── eureka.yml
│   │   ├── api-gateway.yml
│   │   ├── ms-auth.yml
│   │   ├── ms-estudiantes.yml
│   │   ├── ms-instructores.yml
│   │   ├── ms-vehiculos.yml
│   │   ├── ms-asignaciones.yml
│   │   ├── ms-cobros.yml
│   │   ├── ms-reportes.yml
│   │   ├── ms-notificaciones.yml
│   │   └── kustomization.yml          (Agrupa todos los recursos)
│   │
│   ├── overlays/
│   │   └── dev/                       (Overrides para desarrollo)
│   │       ├── kustomization.yml      (Reduce replicas, agrega labels)
│   │       └── namespace-patch.yml
│   │
│   └── argocd-application.yml         (Define la App en Argo CD)
│
├── backend/                           (Código Spring Boot)
├── frontend/                          (Código Vue.js)
└── ...
```

---

## FASE 1: INSTALACIÓN DE HERRAMIENTAS

### Paso 1.1: Instalar Minikube

**Windows (PowerShell como Admin):**
```powershell
# Opción A: Via Chocolatey (recomendado)
choco install minikube

# Opción B: Manual
# Descargar: https://github.com/kubernetes/minikube/releases
# Ejecutar instalador
```

**Verificar:**
```powershell
minikube version
# → minikube version: v1.30.1
```

### Paso 1.2: Instalar kubectl

```powershell
# Via Chocolatey
choco install kubernetes-cli

# Verificar
kubectl version --client
# → v1.28.x
```

### Paso 1.3: Iniciar Minikube

```bash
# Crear cluster con suficientes recursos
minikube start \
  --cpus=4 \
  --memory=8192 \
  --driver=docker \
  --kubernetes-version=v1.28.0

# Esperar 1-2 min...

# Verificar estado
minikube status
# Salida esperada:
# minikube
# type: Control Plane
# host: Running
# kubelet: Running
# apiserver: Running
# kubeconfig: Configured
```

### Paso 1.4: Verificar Cluster

```bash
# Ver nodos
kubectl get nodes
# NAME       STATUS   ROLES           
# minikube   Ready    control-plane

# Ver namespaces
kubectl get namespaces
# default, kube-system, kube-public, kube-node-lease
```

---

## FASE 2: PREPARACIÓN DEL REPOSITORIO

### Paso 2.1: Clonar o Crear Estructura

Si ya tienes el proyecto, agrega la carpeta `kubernetes/`:

```bash
cd proyecto-titulacion

# Crear estructura si no existe
mkdir -p kubernetes/base
mkdir -p kubernetes/overlays/dev

# Copiar manifiestos (ya creados en sección anterior)
# Debería tener:
ls kubernetes/base/
# postgres.yml, rabbitmq.yml, minio.yml, eureka.yml, ...

ls kubernetes/overlays/dev/
# kustomization.yml, namespace-patch.yml
```

### Paso 2.2: Validar Sintaxis YAML

```bash
# Instalar kubeval (opcional, valida YAML)
choco install kubeval

# Validar todos los manifiestos
kubeval kubernetes/base/*.yml
kubeval kubernetes/overlays/dev/*.yml

# Salida esperada: ✓ PASS
```

### Paso 2.3: Commit a Git

```bash
# Agregar cambios
git add kubernetes/

# Crear commit
git commit -m "Sprint 12 (Infra Kubernetes + Argo CD - manifiestos base y overlays)"

# Push a main
git push origin main

# Verificar en GitHub
# https://github.com/tu-usuario/proyecto-titulacion/tree/main/kubernetes
```

---

## FASE 3: BUILD DE IMÁGENES DOCKER

### Paso 3.1: Usar Minikube Docker Daemon

Importante: Usar el Docker daemon de Minikube para que las imágenes estén disponibles en el cluster.

```bash
# En PowerShell / bash
eval $(minikube docker-env)

# Verificar que apunta a Minikube
docker ps
# Deberías ver containers de Minikube (kube-apiserver, etcd, etc.)
```

### Paso 3.2: Build Eureka Server

```bash
cd backend

# Build Eureka
docker build -t escuela/eureka-server:latest \
  -f infrastructure/docker/Dockerfile.spring \
  --build-arg MODULE=eureka-server \
  --build-arg SERVICE_PORT=8761 \
  .

# Salida esperada:
# → Successfully tagged escuela/eureka-server:latest
```

### Paso 3.3: Build API Gateway

```bash
docker build -t escuela/api-gateway:latest \
  -f infrastructure/docker/Dockerfile.spring \
  --build-arg MODULE=api-gateway \
  --build-arg SERVICE_PORT=8080 \
  .

# → Successfully tagged escuela/api-gateway:latest
```

### Paso 3.4: Build Todos los Microservicios

**Script para automatizar (crea archivo `build-all-images.sh`):**

```bash
#!/bin/bash
set -e

eval $(minikube docker-env)

MODULES=(
  "ms-auth:8081"
  "ms-estudiantes:8082"
  "ms-instructores:8083"
  "ms-vehiculos:8084"
  "ms-asignaciones:8085"
  "ms-cobros:8086"
  "ms-reportes:8087"
  "ms-notificaciones:8088"
)

for module in "${MODULES[@]}"; do
  IFS=':' read -r MODULE_NAME PORT <<< "$module"
  echo "Building $MODULE_NAME..."
  
  docker build -t escuela/$MODULE_NAME:latest \
    -f infrastructure/docker/Dockerfile.spring \
    --build-arg MODULE=$MODULE_NAME \
    --build-arg SERVICE_PORT=$PORT \
    .
done

echo "✅ All images built successfully!"
docker images | grep escuela
```

**Ejecutar:**
```bash
chmod +x build-all-images.sh
./build-all-images.sh

# Toma ~15-25 min (primera vez)
# Resultado esperado:
# → 10 imágenes: escuela/eureka-server, escuela/api-gateway, escuela/ms-*
```

### Paso 3.5: Verificar Imágenes

```bash
docker images | grep escuela

# Salida esperada:
# REPOSITORY                    TAG       IMAGE ID
# escuela/eureka-server         latest    abc123def456
# escuela/api-gateway           latest    def456abc123
# escuela/ms-auth               latest    ghi789jkl012
# ... (todos los MS)
```

---

## FASE 4: CONFIGURACIÓN DE ARGO CD

### Paso 4.1: Instalar Argo CD en Minikube

```bash
# Crear namespace argocd
kubectl create namespace argocd

# Instalar Argo CD (descarga manifiestos oficiales)
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Esperar a que los pods estén Ready (1-2 min)
kubectl wait --for=condition=available --timeout=300s \
  deployment/argocd-server -n argocd

# Verificar instalación
kubectl get pods -n argocd
# Salida esperada (todos Running):
# NAME                                READY   STATUS    
# argocd-application-controller-0     1/1     Running
# argocd-dex-server-xxx               1/1     Running
# argocd-repo-server-xxx              1/1     Running
# argocd-server-xxx                   1/1     Running
# argocd-redis-xxx                    1/1     Running
```

### Paso 4.2: Acceder a Argo CD UI

**Terminal 1: Port-forward (dejar abierta)**
```bash
kubectl port-forward svc/argocd-server -n argocd 8443:443
# → Forwarding from 127.0.0.1:8443 -> 8080
```

**Terminal 2: Obtener contraseña inicial**
```bash
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d

# Salida: abc123def456... (contraseña generada aleatoriamente)
# GUARDAR ESTA CONTRASEÑA
```

**Acceder a UI:**
```
URL: https://localhost:8443
Usuario: admin
Contraseña: (la anterior)

⚠️ Navegador mostrará warning de certificado autofirmado
→ Hacer click "Entiendo el riesgo" o "Proceed anyway"
```

### Paso 4.3: Cambiar Contraseña

Una vez logueado en Argo CD:

```
Settings (esquina inferior izq) 
  → Accounts 
  → Change Password
  → Nueva contraseña: tu-password-segura
```

### Paso 4.4: Configurar Repositorio Git

En Argo CD UI:

```
Settings 
  → Repositories
  → Connect Repo (botón)
  
Seleccionar:
  - Connection Method: HTTPS
  - Repository URL: https://github.com/tu-usuario/proyecto-titulacion
  - Username: tu-usuario-github
  - Personal Access Token: (crear en GitHub Settings → Developer Settings → Personal Access Tokens)
  
Click "Connect"
```

**Crear PAT en GitHub:**

```
GitHub → Settings 
  → Developer Settings 
  → Personal access tokens 
  → Tokens (classic)
  → Generate new token
  
Permisos necesarios:
  ☑ repo (Full control of private repositories)
  ☑ read:org
  
Copiar token y pegar en Argo CD
```

---

## FASE 5: DESPLIEGUE INICIAL

### Paso 5.1: Crear Application en Argo CD

**Opción A: Via UI (visual)**

```
Argo CD → Applications (pestaña superior)
  → New Application (botón)
  
Llenar:
  Application Name: proyecto-titulacion
  Project: default
  
  Source:
    Repository URL: https://github.com/tu-usuario/proyecto-titulacion
    Revision: main
    Path: kubernetes/overlays/dev
  
  Destination:
    Cluster: https://kubernetes.default.svc (local)
    Namespace: escuela
  
  Sync Policy:
    ☑ Automatic
    ☑ Prune Resources
    ☑ Self Heal
    
Create
```

**Opción B: Via CLI (automático)**

```bash
# Aplicar el archivo argocd-application.yml creado anteriormente
kubectl apply -f kubernetes/argocd-application.yml

# Verificar
kubectl get applications -n argocd
# NAME                     SYNC STATUS
# proyecto-titulacion      OutOfSync
```

### Paso 5.2: Sincronizar Aplicación

**Via UI:**
```
Argo CD UI 
  → Applications 
  → proyecto-titulacion
  → Sync (botón)
  → Synchronize
```

**Via CLI:**
```bash
kubectl get applications -n argocd
# → Ver nombre exacto

argocd app sync proyecto-titulacion

# Ver progreso
kubectl get pods -n escuela
# Esperar a que todos pasen a Running/Ready
```

### Paso 5.3: Monitorear Despliegue

```bash
# Ver todos los pods en namespace escuela
kubectl get pods -n escuela --watch

# Salida esperada (después de 3-5 min):
# NAME                            READY   STATUS    
# dev-postgres-xxx               1/1     Running
# dev-rabbitmq-xxx               1/1     Running
# dev-minio-xxx                  1/1     Running
# dev-eureka-server-xxx          1/1     Running
# dev-api-gateway-xxx            1/1     Running
# dev-ms-auth-xxx                1/1     Running
# dev-ms-estudiantes-xxx         1/1     Running
# ... (todos los MS)

# Ver detalles
kubectl describe pod dev-ms-auth-xxx -n escuela

# Ver logs
kubectl logs -f dev-ms-auth-xxx -n escuela
```

---

## FASE 6: VALIDACIÓN DEL SISTEMA

### Paso 6.1: Verificar Servicios

```bash
# Ver todos los servicios
kubectl get svc -n escuela

# Salida esperada:
# NAME              TYPE        CLUSTER-IP   
# postgres          ClusterIP   None
# rabbitmq          ClusterIP   None
# minio             ClusterIP   None
# eureka-server     ClusterIP   None
# api-gateway       LoadBalancer 10.x.x.x (Pending es normal en Minikube)
# ms-auth           ClusterIP   None
# ms-estudiantes    ClusterIP   None
```

### Paso 6.2: Port-Forward a Servicios

**En diferentes terminales (dejar abiertas):**

```bash
# Terminal 1: API Gateway
kubectl port-forward -n escuela svc/dev-api-gateway 8080:8080

# Terminal 2: Eureka
kubectl port-forward -n escuela svc/dev-eureka-server 8761:8761

# Terminal 3: RabbitMQ Management
kubectl port-forward -n escuela svc/dev-rabbitmq 15672:15672

# Terminal 4: MinIO Console
kubectl port-forward -n escuela svc/dev-minio 9001:9001

# Terminal 5: PostgreSQL (si necesitas)
kubectl port-forward -n escuela svc/dev-postgres 5432:5432
```

### Paso 6.3: Pruebas Funcionales

**Test 1: API Gateway está vivo**
```bash
curl http://localhost:8080/actuator/health
# Salida esperada: {"status":"UP"}
```

**Test 2: Eureka registra servicios**
```bash
curl http://localhost:8761/eureka/apps
# Salida esperada: XML con todas las apps registradas (EUREKA, GATEWAY, MS-AUTH, ...)
```

**Test 3: RabbitMQ Management**
```
Navegador: http://localhost:15672
Usuario: guest
Contraseña: guest

Verificar:
  - Connections: 1+
  - Channels: servicios conectados
  - Queues: colas creadas por listeners
```

**Test 4: MinIO Console**
```
Navegador: http://localhost:9001
Usuario: minioadmin
Contraseña: minioadmin123

Verificar:
  - Buckets creados
  - Object browser
```

**Test 5: MS-Auth Login (opcional, requiere frontend)**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@escuela.com",
    "password": "admin123"
  }'

# Salida esperada:
# {
#   "token": "eyJhbGc...",
#   "refreshToken": "...",
#   "expiresIn": 432000
# }
```

### Paso 6.4: Ver Estado en Argo CD

```
Argo CD UI → Applications → proyecto-titulacion

Verificar:
  ✅ SYNC STATUS: Synced (verde)
  ✅ HEALTH STATUS: Healthy (verde)
  ✅ 12+ Resources sincronizados
  
Tree view (lado izq):
  └─ escuela (namespace)
      ├─ Deployment: dev-postgres, dev-rabbitmq, dev-minio, dev-eureka-server, ...
      ├─ Service: dev-postgres, dev-rabbitmq, ...
      ├─ ConfigMap: postgres-config, minio-secret
      ├─ Secret: postgres-secret, ...
```

---

## FASE 7: GITOPS EN ACCIÓN

### Paso 7.1: Demostrar Sincronización Automática

#### Demo 1: Cambiar Replicas

**En Git:**
```bash
# Editar kubernetes/overlays/dev/kustomization.yml
nano kubernetes/overlays/dev/kustomization.yml

# Cambiar:
# replicas:
#   - name: api-gateway
#     count: 1        ← cambiar a 2

# Guardar y commit
git add kubernetes/overlays/dev/kustomization.yml
git commit -m "Sprint 12 (Argo CD - Aumentar replicas API Gateway)"
git push origin main
```

**En Kubernetes:**
```bash
# Observar cambio automático en cluster (después de 3 min)
kubectl get deployments -n escuela

# Salida: api-gateway ahora tiene READY 2/2
# ✅ Argo CD sincronizó el cambio automáticamente
```

**Captura:** Tomar screenshot de:
1. Git commit en GitHub
2. Argo CD mostrando sincronización
3. `kubectl get deployments` con 2 replicas

#### Demo 2: Cambiar Variable de Entorno

```bash
# Editar ms-auth.yml (EJEMPLO: cambiar timeout)
nano kubernetes/base/ms-auth.yml

# Buscar:
# env:
# - name: JAVA_OPTS
#   value: "-Duser.timezone=America/Guayaquil"
#   
# Cambiar a:
# env:
# - name: JAVA_OPTS
#   value: "-Duser.timezone=America/Guayaquil -Dserver.servlet.session.timeout=3600"

# Commit
git add kubernetes/base/ms-auth.yml
git commit -m "Sprint 12 (Argo CD - Aumentar session timeout)"
git push origin main
```

**Resultado:** Nuevos pods de ms-auth se crean con nueva configuración automáticamente.

#### Demo 3: Rollback Instantáneo

```bash
# En Argo CD UI:
Applications → proyecto-titulacion → History (pestaña)

Verás lista de syncs:
  - Sync 1: ... (master commit ABC123 - Aumentar replicas)
  - Sync 2: ... (master commit DEF456 - Cambiar timeout)
  
Click en Sync anterior:
  → Revise (botón)
  
Retorna a versión anterior automáticamente
✅ Todos los pods vuelven al estado anterior
```

### Paso 7.2: Verificar Historial de Cambios

**En GitHub:**
```
Ir a: Actions → Workflows
Ver commits que dispararon cambios en Kubernetes
Cada commit tiene su propia línea en el historial
```

**En Argo CD:**
```
Applications → proyecto-titulacion → Timeline

Ver:
  - Quién hizo cada cambio (git user)
  - Cuándo se sincronizó
  - Qué recursos fueron afectados
  - Duración de la sincronización
```

---

## TROUBLESHOOTING

### Problema 1: Pods en CrashLoopBackOff

```bash
# Ver logs del pod
kubectl logs -f dev-ms-auth-xxx -n escuela --tail=50

# Causas comunes:
# 1. Base de datos no está lista
#    → Esperar a que postgres esté Running y Ready
# 2. Imagen no encontrada (imagePullPolicy: Always en prod)
#    → Usar IfNotPresent y builds locales en Minikube
# 3. Spring Boot startup lento (>180s)
#    → Aumentar initialDelaySeconds en livenessProbe
```

### Problema 2: Argo CD no sincroniza cambios

```bash
# Verificar estado de Application
kubectl get applications -n argocd -o yaml

# Ver si hay errores de sync
kubectl describe application proyecto-titulacion -n argocd

# Soluciones:
# 1. Repositorio Git mal configurado
#    → Verificar credenciales en Argo CD Settings
# 2. Path incorrecto
#    → Verificar que kubernetes/overlays/dev existe
# 3. Manifiestos inválidos
#    → Correr: kustomize build kubernetes/overlays/dev
```

### Problema 3: Out of Memory en Minikube

```bash
# Ver recursos usados
minikube dashboard
# Ir a Metrics

# Soluciones:
# 1. Aumentar memoria Minikube
minikube stop
minikube start --memory=16384

# 2. Reducir replicas en dev
# Editar: kubernetes/overlays/dev/kustomization.yml
# replicas:
#   - name: api-gateway
#     count: 1    ← 1 replica en dev
```

### Problema 4: Puerto ya en uso (port-forward)

```bash
# Si 8080 ya está en uso
# Cambiar puerto local:
kubectl port-forward -n escuela svc/dev-api-gateway 8090:8080

# Ahora acceder a http://localhost:8090
```

### Problema 5: Imágenes no se encuentran

```bash
# Verificar que Minikube docker-env está activo
eval $(minikube docker-env)

# Verificar imágenes en Minikube
docker images | grep escuela

# Si faltan imágenes: rebuild
./build-all-images.sh
```

---

## 📊 CHECKLIST DE IMPLEMENTACIÓN

### Instalación Base
- [ ] Minikube instalado y running (`minikube status`)
- [ ] kubectl funcionando (`kubectl get nodes`)
- [ ] Docker apunta a Minikube (`eval $(minikube docker-env)`)

### Repositorio
- [ ] Carpeta `/kubernetes` existe en Git
- [ ] Manifiestos en `kubernetes/base/` validados
- [ ] Overlays en `kubernetes/overlays/dev/`
- [ ] Commit y push a main completados

### Imágenes Docker
- [ ] 10 imágenes compiladas (`docker images | grep escuela`)
- [ ] Nombres correctos: escuela/eureka-server, escuela/api-gateway, escuela/ms-*

### Argo CD
- [ ] Instalado en namespace `argocd` (`kubectl get pods -n argocd`)
- [ ] UI accesible en `https://localhost:8443`
- [ ] Repositorio GitHub conectado
- [ ] Application `proyecto-titulacion` creada

### Despliegue
- [ ] Namespace `escuela` existe
- [ ] 12+ recursos sincronizados (kubectl get all -n escuela)
- [ ] Todos los pods en status `Running` (kubectl get pods -n escuela)
- [ ] Eureka registra todos los servicios (http://localhost:8761)

### Validación
- [ ] API Gateway responde (`curl http://localhost:8080/actuator/health`)
- [ ] RabbitMQ UI accesible (`http://localhost:15672`)
- [ ] MinIO UI accesible (`http://localhost:9001`)
- [ ] Argo CD muestra SYNC STATUS = Synced

### GitOps
- [ ] Cambios en Git se sincronizan automáticamente
- [ ] Historial visible en Argo CD Timeline
- [ ] Rollback funciona correctamente

---

## 📸 CAPTURAS DE PANTALLA ESPERADAS

Para la presentación, tomar screenshots de:

### 1. Argo CD Dashboard
```
Applications → proyecto-titulacion
Mostrar:
  ✅ SYNC STATUS: Synced
  ✅ HEALTH STATUS: Healthy
  ✅ 12+ Resources
  ✅ Timeline con historial de cambios
```

### 2. Kubernetes Cluster
```bash
kubectl get all -n escuela
Mostrar:
  - Deployments: eureka, gateway, 8 MS
  - Services: todos los endpoints
  - Pods: todos Running
```

### 3. Eureka Server
```
http://localhost:8761
Mostrar:
  - General Info
  - DS Replicas: instancias registradas
  - Instances Currently Registered: EUREKA, GATEWAY, MS-AUTH, ...
```

### 4. Git Commits
```
GitHub → Commits
Mostrar:
  - Commits de manifiestos Kubernetes
  - Historial completo
  - Mensajes de commit siguiendo convención Sprint N
```

### 5. Demo GitOps
```
Secuencia:
  1. git push cambio en kubernetes/
  2. Argo CD detecta cambio (→ OutOfSync)
  3. Argo CD sincroniza automáticamente
  4. kubectl show pod con nueva configuración
```

---

## 📖 REFERENCIAS

### Documentación
- Argo CD: https://argo-cd.readthedocs.io/
- Kubernetes: https://kubernetes.io/docs/
- Minikube: https://minikube.sigs.k8s.io/
- Kustomize: https://kustomize.io/

### Comandos Útiles

```bash
# Argo CD CLI
argocd app list
argocd app get proyecto-titulacion
argocd app sync proyecto-titulacion
argocd app rollback proyecto-titulacion <revision>

# Kubernetes
kubectl get all -n escuela
kubectl describe pod <pod-name> -n escuela
kubectl logs -f <pod-name> -n escuela
kubectl exec -it <pod-name> -n escuela -- /bin/bash

# Minikube
minikube dashboard
minikube logs
minikube stop / minikube start

# Docker
docker images
docker ps
eval $(minikube docker-env)
```

---

## 📝 NOTAS PARA LA PRESENTACIÓN

### Puntos Clave a Explicar

1. **GitOps Philosophy**
   - Git es la fuente de verdad
   - Declarativo vs Imperativo
   - Beneficios: auditable, reversible, automático

2. **Argo CD en Acción**
   - Diferencia entre SYNC STATUS y HEALTH STATUS
   - Continuous Reconciliation (cada 3 min verifica)
   - Auto-sync vs Manual sync

3. **Ciclo Completo**
   - Developer hace commit
   - GitHub notifica a Argo CD
   - Argo CD sincroniza automáticamente
   - Cluster refleja estado de Git

4. **Kubernetes Concepts**
   - Declaración de recursos en YAML
   - Reconciliation loops
   - Health checks (liveness, readiness probes)

5. **Escalabilidad**
   - Multi-environment (dev, staging, prod)
   - Kustomize para reutilización
   - Secrets management

### Demo en Vivo

**Tiempo: 10-15 minutos**

```
1. Mostrar repo GitHub [1 min]
   → Carpeta kubernetes/ con estructura
   
2. Abrir Argo CD UI [2 min]
   → Dashboard mostrando aplicación
   
3. Cambiar algo en Git [3 min]
   → Editar kubernetes/overlays/dev/kustomization.yml
   → Commit y push
   
4. Observar sincronización [5 min]
   → Esperar a que Argo CD detecte cambio
   → OutOfSync → Syncing → Synced
   → Mostrar timeline de cambios
   
5. Verificar cambios en cluster [3 min]
   → kubectl get deployments
   → Nuevos pods con nueva configuración
   
6. Q&A [2 min]
```

---

## ✅ CRITERIOS DE ACEPTACIÓN

El proyecto está **completo y funcional** cuando:

✅ **Infraestructura**
- Minikube cluster creado y funcionando
- Argo CD instalado y configurado
- Repositorio GitHub conectado

✅ **Despliegue**
- Todos los manifiestos YAML creados
- Imágenes Docker compiladas
- 12+ recursos sincronizados en cluster

✅ **GitOps**
- Cambios en Git se sincronizan automáticamente
- Historial completo visible en Argo CD
- Rollback funciona correctamente

✅ **Documentación**
- Este documento completado
- Screenshots de cada fase
- Video demo (opcional)

✅ **Presentación**
- Exposición técnica en clase
- Q&A respondidas
- Nota de examen asignada

---

**Generado por:** Raúl Sebastián Cruz Baño, Hernán Mateo Jurado Moran  
**Fecha:** 2026-06-05  
**Estado:** ✅ PRONTO PARA PRODUCCIÓN

---

## 🎓 APRENDIZAJES LOGRADOS

Al completar este proyecto, demostramos:

1. **Kubernetes Profundo**
   - Crear manifiestos YAML desde cero
   - Deployments, Services, ConfigMaps, Secrets
   - Health checks, resource limits, probes

2. **GitOps Completo**
   - Argo CD como orquestador
   - Sincronización automática
   - Reconciliation loops

3. **DevOps Moderno**
   - Infraestructura como Código (IaC)
   - CI/CD pipeline
   - Continuous Deployment

4. **Buenas Prácticas**
   - Versionado de infraestructura en Git
   - Auditar quién hizo qué y cuándo
   - Rollback en segundos

5. **Arquitectura de Microservicios**
   - Desplegar 8 MS en Kubernetes
   - Service discovery con Eureka
   - Comunicación asíncrona con RabbitMQ

---

**Fin del documento**
