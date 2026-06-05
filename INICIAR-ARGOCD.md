# 🚀 INICIAR ARGO CD + KUBERNETES — GUÍA RÁPIDA

**Proyecto:** Sistema de Control Administrativo para Escuelas de Conducción  
**Estudiantes:** Raúl Sebastián Cruz Baño, Hernán Mateo Jurado Moran  
**Institución:** UDLA  
**Última actualización:** 2026-06-05

---

## ⏱️ TIEMPO TOTAL: ~65 MINUTOS

```
Instalación:          15 min
Build imágenes:       25 min
Argo CD setup:        10 min
Deploy + validación:  10 min
Demo GitOps:           5 min
```

---

## 📋 ANTES DE EMPEZAR

### Requisitos
- ✅ Windows 11 Pro / macOS / Linux
- ✅ 4+ CPU cores
- ✅ 8+ GB RAM
- ✅ 30 GB disco libre
- ✅ Docker Desktop instalado
- ✅ Git instalado

### Verificar instalación

```bash
docker --version
git --version
# Ambos deben funcionar
```

---

## 🎯 PASO A PASO: 8 FASES

### FASE 1: INSTALAR MINIKUBE (5 min)

```bash
# Windows (PowerShell como Admin):
choco install minikube kubernetes-cli

# Verificar
minikube version
kubectl version --client

# Arrancar cluster
minikube start \
  --cpus=4 \
  --memory=8192 \
  --driver=docker

# Esperar a que esté "Ready"
kubectl get nodes
```

**Salida esperada:**
```
NAME       STATUS   ROLES
minikube   Ready    control-plane
```

---

### FASE 2: BUILD DE IMÁGENES (25 min)

```bash
# 1. Ir a carpeta backend
cd backend

# 2. Activar Minikube Docker
eval $(minikube docker-env)

# 3. Compilar todas las imágenes
chmod +x ../build-all-images.sh
./build-all-images.sh

# Esperar 20-25 minutos...
# Salida final: 10 imágenes compiladas ✅
```

**Verificar resultado:**
```bash
docker images | grep escuela
# Debe mostrar 10 imágenes
```

---

### FASE 3: INSTALAR ARGO CD (8 min)

```bash
# 1. Crear namespace
kubectl create namespace argocd

# 2. Instalar Argo CD
kubectl apply -n argocd -f \
  https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# 3. Esperar a que pods estén Running (2 min)
kubectl get pods -n argocd --watch
# Presionar Ctrl+C cuando todos estén Running

# 4. Verificar
kubectl get pods -n argocd | grep Running | wc -l
# Debe ser ≥5
```

---

### FASE 4: ACCEDER A ARGO CD (3 min)

**Terminal 1: Port-forward (dejar abierta)**
```bash
kubectl port-forward svc/argocd-server -n argocd 8443:443
```

**Terminal 2: Obtener contraseña**
```bash
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d
# Salida: abc123def456... (contraseña)
# GUARDAR ESTA CONTRASEÑA
```

**Navegador:**
```
URL: https://localhost:8443
Usuario: admin
Contraseña: (la anterior)

⚠️ Navegador: Aceptar certificado autofirmado
```

---

### FASE 5: CONECTAR GIT (3 min)

**En Argo CD UI:**

```
Settings (esquina inferior izq)
  → Repositories
  → Connect Repo
  
Llenar:
  Connection Method: HTTPS
  Repository URL: https://github.com/tu-usuario/proyecto-titulacion
  Username: tu-usuario-github
  Password: (GitHub PAT - ver abajo)
  
Connect
```

**Generar GitHub PAT:**

```
GitHub → Settings 
  → Developer Settings 
  → Personal access tokens 
  → Tokens (classic)
  → Generate new token
  
Permisos: ☑ repo, ☑ read:org
Copiar token y pegar en Argo CD
```

---

### FASE 6: CREAR APPLICATION (2 min)

**En Terminal:**
```bash
# Desde raíz del proyecto
kubectl apply -f kubernetes/argocd-application.yml

# Esperar a que aparezca en Argo CD (~30 seg)
```

**O en Argo CD UI:**
```
Applications (pestaña superior)
  → New Application
  
Llenar:
  Name: proyecto-titulacion
  Repository: tu-repo
  Path: kubernetes/overlays/dev
  Destination: https://kubernetes.default.svc / escuela
  Sync Policy: Automatic ☑ / Prune ☑ / Self Heal ☑
  
Create
```

---

### FASE 7: SINCRONIZAR (2 min)

**En Argo CD UI:**
```
Applications → proyecto-titulacion
  → Sync (botón)
  → Synchronize

Estado: OutOfSync → Syncing → Synced ✅
```

**O en Terminal:**
```bash
argocd app sync proyecto-titulacion
```

**Monitorear pods:**
```bash
kubectl get pods -n escuela --watch
# Todos deben pasar a Running/Ready
# Esperar 3-5 minutos
```

---

### FASE 8: VALIDAR (3 min)

```bash
# Port-forwards a servicios (nuevas terminales)

# Terminal A: API Gateway
kubectl port-forward -n escuela svc/dev-api-gateway 8080:8080

# Terminal B: Eureka
kubectl port-forward -n escuela svc/dev-eureka-server 8761:8761

# Terminal C: RabbitMQ
kubectl port-forward -n escuela svc/dev-rabbitmq 15672:15672

# Test 1: API Gateway
curl http://localhost:8080/actuator/health
# Resultado: {"status":"UP"} ✅

# Test 2: Eureka en navegador
# http://localhost:8761
# Verificar: 10 servicios registrados ✅

# Test 3: Todos los recursos
kubectl get all -n escuela
# Verificar: 30+ recursos, todos Running ✅
```

---

## 🎬 DEMO GITOPS (5 min) — OPCIONAL

### Demostrar sincronización automática

**Paso 1: Cambiar replica count**
```bash
# Editar
nano kubernetes/overlays/dev/kustomization.yml

# Buscar:
# replicas:
#   - name: api-gateway
#     count: 1

# Cambiar a:
# replicas:
#   - name: api-gateway
#     count: 2

# Guardar: Ctrl+X → Y → Enter
```

**Paso 2: Commit y push**
```bash
git add kubernetes/overlays/dev/kustomization.yml
git commit -m "Sprint 12 (Demo - Aumentar replicas API Gateway)"
git push origin main
```

**Paso 3: Observar cambios automáticos**

En Argo CD UI:
```
proyecto-titulacion Application

Status cambia:
  OutOfSync (naranja)
  ↓
  Syncing (azul)
  ↓
  Synced (verde) ✅
  
Timeline: Aparece nuevo evento de sincronización
```

En Terminal:
```bash
kubectl get deployment -n escuela | grep api-gateway
# Resultado: READY cambia de 1/1 a 2/2 ✅

# Ver nuevos pods
kubectl get pods -n escuela | grep api-gateway
# Resultado: 2 pods corriendo ✅
```

**Conclusión:** ✅ Cambio en Git se aplicó automáticamente en Kubernetes

---

## 📞 DOCUMENTACIÓN DISPONIBLE

| Archivo | Propósito | Cuándo leer |
|---------|----------|-----------|
| **IMPLEMENTACION-ARGOCD.md** | Guía completa de 7 fases | Referencia técnica |
| **PRESENTACION-TECNICA-ARGOCD.md** | Script de presentación en clase | Antes de presentar |
| **GUIA-RAPIDA.md** | TL;DR paso a paso | Si necesitas ir rápido |
| **kubernetes/README.md** | Referencia de manifiestos | Consulta técnica |
| **ENTREGABLES.md** | Qué se entregó | Completitud del proyecto |
| **Este archivo** | Ejecución rápida | AHORA |

---

## ✅ CHECKLIST: VALIDAR QUE TODO FUNCIONA

- [ ] Minikube running (`minikube status` → all green)
- [ ] Kubectl conectado (`kubectl get nodes`)
- [ ] 10 imágenes Docker compiladas (`docker images | grep escuela`)
- [ ] Argo CD instalado (`kubectl get pods -n argocd` → 5+ running)
- [ ] GitHub repositorio conectado (Argo CD Settings)
- [ ] Application `proyecto-titulacion` creada (Argo CD Applications)
- [ ] Namespace `escuela` creado (`kubectl get namespace escuela`)
- [ ] 12+ pods running (`kubectl get pods -n escuela`)
- [ ] API Gateway responde (`curl http://localhost:8080/actuator/health`)
- [ ] Eureka registra servicios (`curl http://localhost:8761/eureka/apps | grep UP`)
- [ ] Argo CD UI muestra Synced ✅

**Si TODO está ✅ → LISTO PARA PRESENTAR**

---

## 🆘 PROBLEMAS COMUNES

### Pods en CrashLoopBackOff

```bash
# Ver qué error hay
kubectl logs -f dev-ms-auth-xxx -n escuela

# Común: Database not ready
# Solución: Esperar a que postgres esté Running
kubectl logs -f dev-postgres-xxx -n escuela
```

### Argo CD no detecta cambios

```bash
# Verificar conexión Git
kubectl describe application proyecto-titulacion -n argocd

# Verificar repositorio
kubectl get repositories -n argocd
```

### Puertos ocupados

```bash
# Si puerto 8443 está ocupado
kubectl port-forward svc/argocd-server -n argocd 9443:443
# Acceder a https://localhost:9443 en su lugar
```

---

## 🎓 PARA LA PRESENTACIÓN EN CLASE

**Qué mostrar:**

1. **Terminal con cluster vivo**
   ```bash
   kubectl get all -n escuela
   # Mostrar 30+ recursos
   ```

2. **GitHub commits**
   - Mostrar carpeta `/kubernetes` en main
   - Mostrar commit "Sprint 12 (Argo CD + Kubernetes)"

3. **Argo CD Dashboard**
   - Mostrar Application sincronizada
   - Mostrar Timeline de cambios

4. **Demo en vivo (5 min)**
   - Cambiar replicas en Git
   - Commit y push
   - Observar sincronización automática
   - Mostrar nuevos pods en Kubernetes

5. **Validación funcional**
   - `curl http://localhost:8080/actuator/health` → 200
   - Navegador: `http://localhost:8761` → Eureka con 10 servicios

---

## 🚀 PRÓXIMAS ACCIONES

### Después de presenta presentación:

1. **Agregar environment producción**
   ```bash
   mkdir -p kubernetes/overlays/prod
   # Copiar dev, aumentar replicas a 3-5
   ```

2. **Agregar secret management**
   ```bash
   # Sealed Secrets o Vault
   ```

3. **Monitoreo y observabilidad**
   ```bash
   # Prometheus + Grafana
   # ELK Stack para logs
   ```

4. **Backup automation**
   ```bash
   # Velero para snapshots
   # PostgreSQL backup diario
   ```

---

## 📊 ESTADO FINAL

```
✅ Minikube cluster          Running
✅ Argo CD                   Healthy
✅ 14 contenedores           All running
✅ 8 microservicios          Deployed
✅ PostgreSQL                Healthy
✅ RabbitMQ                  Healthy
✅ Eureka                    10/10 services registered
✅ API Gateway               Responsive
✅ GitOps pipeline           Working (auto-sync)
✅ Documentación             Completa

RESULTADO FINAL: 100% FUNCIONAL ✨
```

---

## 📞 CONTACTO

**Si necesitas ayuda:**

1. **Error técnico:** Ver troubleshooting arriba o en IMPLEMENTACION-ARGOCD.md
2. **Pregunta arquitectura:** Ver PRESENTACION-TECNICA-ARGOCD.md
3. **Referencia manifiestos:** Ver kubernetes/README.md
4. **Detalles completos:** Ver IMPLEMENTACION-ARGOCD.md

---

## 🎯 LISTO PARA PRESENTAR

Este documento contiene TODO lo necesario para:

✅ Instalar Minikube + Argo CD en 65 minutos  
✅ Desplegar 8 microservicios en Kubernetes  
✅ Demostrar GitOps en acción  
✅ Presentar en clase (15-20 min)  
✅ Responder preguntas técnicas  

**¡ADELANTE! 🚀**

---

**Generado para:** Presentación en clase  
**Versión:** 1.0  
**Fecha:** 2026-06-05  

Commit en Git: `d825598` — Sprint 12 (Argo CD + Kubernetes)
