# 🎬 GUION DE DEMO — Argo CD + Kubernetes en clase

**Proyecto:** Sistema de Control Administrativo para Escuelas de Conducción  
**Duración:** 15-20 minutos  
**Objetivo:** Demostrar GitOps en vivo — cambios en Git sincronizados automáticamente en Kubernetes  

---

## 📐 ANTES DE EMPEZAR: MONTAJE DE PANTALLAS

Vas a necesitar **4 terminales + navegador**:

```
┌──────────────────────┬──────────────────────┐
│  NAVEGADOR           │  TERMINAL 4 (demo)   │
│  (Argo CD + GitHub)  │  (comandos en vivo)  │
├──────────────────────┼──────────────────────┤
│ TERMINAL 1: Minikube │ TERMINAL 2: kubectl  │
│ (quieta)             │ watch (quieta)       │
├──────────────────────┼──────────────────────┤
│ TERMINAL 3: Eureka   │                      │
│ port-forward (quieta)│                      │
└──────────────────────┴──────────────────────┘
```

### Setup Inicial

**Terminal 1:**
```bash
minikube dashboard
```

**Terminal 2:**
```bash
kubectl get pods -n escuela --watch
```

**Terminal 3:**
```bash
kubectl port-forward -n escuela svc/dev-eureka-server 8761:8761
```

**Navegador:**
```
Pestaña 1: https://localhost:8443 (Argo CD)
Pestaña 2: http://localhost:8761 (Eureka)
```

---

## ✅ PASO 0 — VERIFICAR (Terminal 4)

```bash
minikube status
kubectl get all -n escuela | head -20
curl http://localhost:8080/actuator/health
```

**Qué decir:** "Tengo un cluster con 8 microservicios. Argo CD sincroniza Git con Kubernetes automáticamente. Vamos a verlo en acción."

---

## 🎬 ACTO 1 — ESTADO INICIAL (Argo CD UI)

En **https://localhost:8443** / Applications / proyecto-titulacion

Mostrar:
- ✅ SYNC STATUS: Synced (verde)
- ✅ HEALTH STATUS: Healthy (verde)
- ✅ 30+ Resources
- ✅ Timeline

**Qué decir:** "Argo CD sincroniza automáticamente. Todo está verde. Voy a cambiar algo en Git y veremos cómo lo aplica automáticamente."

---

## 🎬 ACTO 2 — EUREKA EN VIVO

En **http://localhost:8761**

Mostrar: Todas las instancias registradas (EUREKA, GATEWAY, 8 MS)

**Qué decir:** "Todos mis microservicios están registrados. Voy a aumentar replicas del API Gateway."

---

## 🎬 ACTO 3 — CAMBIO EN GIT ⭐ (ESTRELLA)

### PASO 1: Editar (Terminal 4)

```bash
code kubernetes/overlays/dev-demo/kustomization.yml
```

Cambiar:
```
replicas:
  - name: api-gateway
    count: 1     ← CAMBIAR a 3
```

Guardar.

### PASO 2: Commit & Push (Terminal 4)

```bash
git add kubernetes/overlays/dev-demo/kustomization.yml
git commit -m "Demo Clase - Aumentar replicas API Gateway a 3"
git push origin main
```

**Qué decir:** "Acabo de cambiar en Git. Lo empujé a GitHub. Ahora Argo CD lo detectará en ~30 segundos."

### PASO 3: Observar Argo CD (Navegador pestaña 1)

Status cambia:
```
Synced ✅
  ↓
OutOfSync ❌ (detectó cambio)
  ↓
Syncing 🔄 (aplicando)
  ↓
Synced ✅ (listo)

Timeline: evento nuevo
```

**Qué decir:** "¿Ven? Detectó el cambio en 30 seg, está sincronizando... ¡Ya está hecho en 4 segundos!"

### PASO 4: Validar (Terminal 4)

```bash
kubectl get deployments -n escuela | grep api-gateway

# Resultado: READY cambió a 2/2 ✅

kubectl get pods -n escuela | grep api-gateway

# Resultado: 2 pods running ✅
```

**En Terminal 2 (--watch):** Ves pods naciendo en tiempo real.

**Qué decir:** "Nuevo pod nació automáticamente. Git dice 2 replicas, Kubernetes ahora tiene 2. Sin comandos manuales."

---

## 🎬 ACTO 4 — ROLLBACK INSTANTÁNEO ⭐

### PASO 1: Revertir (Terminal 4)

```bash
code kubernetes/overlays/dev-demo/kustomization.yml

# Cambiar de 3 a 1

git add kubernetes/overlays/dev-demo/kustomization.yml
git commit -m "Demo Clase - Rollback: 1 replica"
git push origin main
```

### PASO 2: Observar rollback

**En Argo CD:** OutOfSync → Synced ✅
**En Terminal 2:** Pod Terminating → desaparece
**En Terminal 4:** `kubectl get deployments` muestra 1/1

**Qué decir:** "Rollback en Git, Argo CD sincronizó, pod fue destruido. Todo automático."

---

## 🎬 ACTO 5 (OPCIONAL) — CAMBIO EN VARIABLE

```bash
nano kubernetes/base/ms-auth.yml

# Cambiar: LOG_LEVEL=DEBUG

git add kubernetes/base/ms-auth.yml
git commit -m "Demo - LOG_LEVEL DEBUG"
git push origin main
```

Resultado: Nuevos pods de ms-auth se crean.

**Qué decir:** "Cada cambio — replicas, variables, versiones — Argo CD sincroniza automáticamente."

---

## 🎬 ACTO 6 — GITHUB HISTORIAL

Abre: **https://github.com/tu-usuario/proyecto-titulacion**

Muestra commits:
- "Demo Clase - Aumentar replicas..."
- "Demo Clase - Rollback..."
- "Demo - LOG_LEVEL..."

Con diffs y timestamps.

**Qué decir:** "Cada cambio de infraestructura está versionado. Auditoría completa."

---

## 🎬 CIERRE — ARCHIVOS YAML

Abre: **kubernetes/** en explorador

Muestra:
- base/ (14 manifiestos)
- overlays/dev/ (customizaciones)
- argocd-application.yml (cómo Argo CD sincroniza)

Abre **kubernetes/base/api-gateway.yml** y muestra:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 1              ← Declaramos aquí
  template:
    spec:
      containers:
      - name: gateway
        image: escuela/api-gateway:latest
        livenessProbe:
          httpGet:
            path: /actuator/health
```

**Qué decir:** "Toda infraestructura en YAML, versionada, reproducible. Si el cluster falla: `kubectl apply -k kubernetes/overlays/dev` y renace en minutos."

---

## 📋 RESUMEN: QUÉ DEMUESTRA CADA ACTO

| Acto | Demuestra |
|------|-----------|
| 1 | Argo CD funciona (SYNC STATUS verde) |
| 2 | Microservicios vivos (Eureka muestra 10) |
| 3 | GitOps (cambio Git → nuevos pods) |
| 4 | Rollback automático (revert → pods destruidos) |
| 5 | Cambios en variables se aplican |
| 6 | Auditoría en GitHub (historial de commits) |
| Cierre | Infrastructure as Code (YAML reproducible) |

---

## 🆘 PLAN B SI FALLA

| Problema | Solución |
|----------|----------|
| Argo CD no abre | `kubectl port-forward svc/argocd-server -n argocd 8443:443` |
| Eureka no muestra | Espera 30 seg, F5 |
| Git push falla | `git remote -v`, reintenta |
| Kubernetes caído | `minikube status` / `minikube start` |
| Pods no se crean | `kubectl logs -f deployment/dev-api-gateway -n escuela` |
| TODO ROTO | Tengo capturas de respaldo |

---

## 📸 CAPTURAS A GUARDAR

Antes de clase, toma screenshots de:

1. Argo CD Dashboard - Synced ✅
2. Eureka - 10 servicios
3. Git diff - cambio en YAML
4. Argo CD Timeline - cambio detectado
5. kubectl deployments - READY 2/2
6. GitHub - commits
7. Carpeta kubernetes/ - YAML

---

## ⏱️ TIMING

- Paso 0: 2 min
- Acto 1-2: 4 min
- Acto 3: 5 min
- Acto 4: 3 min
- Acto 5: 2 min (opcional)
- Acto 6: 2 min
- Cierre: 1 min

**TOTAL: 15-20 min** ✅

---

## 🚀 CHECKLIST

- [ ] Minikube running
- [ ] 4 terminales + navegador listos
- [ ] Argo CD (https://localhost:8443) ✅
- [ ] Eureka (http://localhost:8761) ✅
- [ ] API Gateway responde ✅
- [ ] Todos pods Running
- [ ] GitHub accesible
- [ ] Ensayo completo
- [ ] Capturas guardadas

---

## 💡 TIPS

1. Habla lentamente — K8s tarda 5-10 seg
2. Terminal 2 (--watch) es visual — ponla en grande
3. "En prod sincroniza cada 3 min, aquí 30 seg con webhooks"
4. Cierra el loop: Git → detecta → aplica → valida
5. Ten Plan B — capturas de respaldo

---

**¡Éxito en la presentación! 🚀**