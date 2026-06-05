# 📸 Plantilla: Evidencia de Implementación Argo CD

> Esta plantilla sirve para documentar capturas de pantalla y evidencia técnica para tu presentación.
> Completa cada sección con:
> - Screenshot (captura)
> - Explicación breve
> - Timestamp o comando que lo generó

---

## 1️⃣ Minikube iniciado y funcionando

### Captura esperada:
```bash
$ minikube status
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```

### Imagen a capturar:
- Terminal mostrando `minikube status`
- `kubectl cluster-info` confirmando cluster local
- Opcional: Dashboard de Minikube (`minikube dashboard`)

**Archivo para anexar:** `evidencia/01-minikube-status.png`

---

## 2️⃣ Argo CD instalado en el cluster

### Comando de verificación:
```bash
$ kubectl get pods -n argocd

NAME                                      READY   STATUS    RESTARTS
argocd-application-controller-0           1/1     Running   0
argocd-dex-server-6d7b9d8c-abc12         1/1     Running   0
argocd-notifications-controller-0         1/1     Running   0
argocd-redis-7d4b5d8c-def45              1/1     Running   0
argocd-repo-server-6d7b9d8c-ghi78        1/1     Running   0
argocd-server-7d4b5d8c-jkl90             1/1     Running   0
```

### Imagen a capturar:
- Terminal: `kubectl get pods -n argocd`
- Screenshot de la UI de Argo CD accesible en `https://localhost:8443`
- Login con usuario `admin` y contraseña obtenida

**Archivo para anexar:** `evidencia/02-argocd-installed.png`

---

## 3️⃣ Imágenes Docker construidas

### Comando de verificación:
```bash
# Configurar Docker environment para Minikube
eval $(minikube docker-env)  # Linux/Mac
# o
& minikube -p minikube docker-env | Invoke-Expression  # Windows

# Listar imágenes
$ docker images | grep -E "(api-gateway|ms-auth|ms-estudiantes|frontend|eureka)"

REPOSITORY           TAG        IMAGE ID       CREATED        SIZE
api-gateway          latest     abc12def34     5 minutes ago   450MB
ms-auth              latest     def45ghi67     10 minutes ago  480MB
ms-estudiantes       latest     ghi78jkl90     8 minutes ago   500MB
frontend             latest     jkl01mno23     3 minutes ago   120MB
eureka-server        latest     mno56pqr78     12 hours ago    480MB
```

### Imagen a capturar:
- Terminal mostrando `docker images` con todas las imágenes
- Tamaño de cada una (aproximado)

**Archivo para anexar:** `evidencia/03-docker-images.png`

---

## 4️⃣ Application creada en Argo CD

### UI de Argo CD mostrando:

```
Application: escuela-conduccion
Status: Synced ✅
Health: Healthy 💚
Last Sync: 2 minutes ago
```

### Componentes visibles:
- Namespace `escuela-conduccion` como destino
- Repository URL del proyecto
- Branch: `main`
- Path: `kubernetes/argocd`

### Imagen a capturar:
- Pantalla principal de Argo CD con la aplicación visible
- Click en "escuela-conduccion" → detalle de la aplicación
- Árbol de recursos sincronizados (tree view)

**Archivo para anexar:** `evidencia/04-argocd-app-created.png`

---

## 5️⃣ Pods de la aplicación corriendo

### Comando de verificación:
```bash
$ kubectl get pods -n escuela-conduccion

NAME                             READY   STATUS    RESTARTS   AGE
api-gateway-abc123def45-xyz78   1/1     Running   0          3m
ms-auth-def456ghi78-abc12       1/1     Running   0          3m
ms-estudiantes-ghi789jkl01-def45 1/1    Running   0          3m
postgres-jkl012mno34-ghi78      1/1     Running   0          5m
eureka-mno345pqr56-jkl01        1/1     Running   0          4m
frontend-pqr678stu90-mno34      1/1     Running   0          2m
```

### Imagen a capturar:
- Terminal: `kubectl get pods -n escuela-conduccion`
- Descripción detallada de un pod: `kubectl describe pod <pod-name>`
- Logs de un servicio: `kubectl logs <pod-name>`

**Archivo para anexar:** `evidencia/05-pods-running.png`

---

## 6️⃣ Servicios accesibles

### Comando de verificación:
```bash
$ kubectl get svc -n escuela-conduccion

NAME            TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)
frontend        NodePort   10.96.123.45    <none>        80:30000/TCP
api-gateway     NodePort   10.96.234.56    <none>        8080:30080/TCP
ms-auth         ClusterIP  10.96.345.67    <none>        8081/TCP
ms-estudiantes  ClusterIP  10.96.456.78    <none>        8082/TCP
postgres        ClusterIP  10.96.567.89    <none>        5432/TCP
eureka          ClusterIP  10.96.678.90    <none>        8761/TCP
```

### Port-Forward para acceso local:
```bash
# Terminal 1: Frontend
kubectl port-forward svc/frontend -n escuela-conduccion 3000:80

# Terminal 2: API Gateway
kubectl port-forward svc/api-gateway -n escuela-conduccion 8080:8080

# Terminal 3: Eureka
kubectl port-forward svc/eureka -n escuela-conduccion 8761:8761
```

### Imagen a capturar:
- Terminal: `kubectl get svc -n escuela-conduccion`
- Browser: `http://localhost:3000` → Frontend cargando ✅
- Browser: `http://localhost:8080/actuator/health` → Status JSON
- Browser: `http://localhost:8761/` → Eureka UI

**Archivo para anexar:**
- `evidencia/06-services.png` (tabla de servicios)
- `evidencia/07-frontend-accessible.png` (navegador con frontend)
- `evidencia/08-gateway-health.png` (JSON de health)

---

## 7️⃣ Demostración: GitOps en acción

### Paso 1: Cambio en Git

**Editar archivo:**
```bash
# Cambiar replicas en api-gateway
cat kubernetes/argocd/apps/01-api-gateway.yaml | grep -A2 "spec:"
```

**Antes:**
```yaml
spec:
  replicas: 1
```

**Cambio:**
```yaml
spec:
  replicas: 3
```

**Commit:**
```bash
git add kubernetes/argocd/apps/01-api-gateway.yaml
git commit -m "Sprint 11 (Aumentar réplicas API Gateway a 3)"
git push origin main
```

### Imagen a capturar:
- `git diff` mostrando el cambio (1 → 3 replicas)
- `git log` mostrando el nuevo commit

**Archivo para anexar:** `evidencia/09-git-change.png`

---

### Paso 2: Argo CD detecta cambio (OutOfSync)

**Comando:**
```bash
# Esperar ~30 segundos a que Argo CD detecte el cambio
kubectl get application escuela-conduccion -n argocd -o yaml | grep syncStatus -A5
```

**Salida esperada:**
```yaml
syncStatus:
  comparedTo:
    source:
      path: kubernetes/argocd
      repoURL: https://github.com/tu-repo
      targetRevision: HEAD
  status: OutOfSync  ⚠️
```

### Imagen a capturar:
- Argo CD UI mostrando badge "OutOfSync"
- Color amarillo/naranja indicando desincronización
- Detalles en el árbol de recursos

**Archivo para anexar:** `evidencia/10-outofsynced.png`

---

### Paso 3: Sincronización automática

**Si auto-sync está habilitado, esperar ~1 minuto:**
```bash
watch kubectl get pods -n escuela-conduccion | grep api-gateway
```

**Salida esperada (antes):**
```
api-gateway-abc123def45-xyz78   1/1     Running   0          10m
```

**Salida esperada (después):**
```
api-gateway-abc123def45-xyz78   1/1     Running   0          10m
api-gateway-abc123def45-new12   1/1     Running   0          30s  ← NUEVO
api-gateway-abc123def45-new34   1/1     Running   0          20s  ← NUEVO
```

### Comando manual si no es automático:
```bash
# Clickear "Sync" en Argo CD UI
# o
argocd app sync escuela-conduccion
```

### Imagen a capturar:
- Terminal mostrando 3 pods de api-gateway
- Argo CD UI ahora mostrando "Synced" ✅
- Verde = sincronizado

**Archivo para anexar:**
- `evidencia/11-syncing.png` (proceso)
- `evidencia/12-synced.png` (resultado final)

---

### Paso 4: Verificación final

```bash
$ kubectl get pods -n escuela-conduccion | grep api-gateway
api-gateway-abc123def45-xyz78   1/1     Running   0          10m
api-gateway-abc123def45-new12   1/1     Running   0          1m
api-gateway-abc123def45-new34   1/1     Running   0          1m

$ kubectl get deployment -n escuela-conduccion api-gateway -o yaml | grep "replicas:"
  replicas: 3
```

### Imagen a capturar:
- Terminal confirmando 3 réplicas corriendo
- Argo CD UI mostrando "Synced ✅"
- Explicación: "Git = Kubernetes"

**Archivo para anexar:** `evidencia/13-final-state.png`

---

## 8️⃣ Self-Healing Demo (OPCIONAL)

### Demostración: Eliminar un pod manualmente

```bash
# Eliminar el pod manualmente
kubectl delete pod api-gateway-abc123def45-new12 -n escuela-conduccion

# Observar (en 5-10 segundos Argo CD lo recrea)
watch kubectl get pods -n escuela-conduccion | grep api-gateway
```

### Salida esperada:
```
api-gateway-abc123def45-xyz78   1/1     Running   0          11m
api-gateway-abc123def45-new12   1/1     Terminating  0       (borrado)
api-gateway-abc123def45-new34   1/1     Running   0          2m
api-gateway-abc123def45-new56   1/1     ContainerCreating 0  (NUEVO - recreado)
```

### Imagen a capturar:
- Terminal mostrando el pod siendo eliminado
- Terminal mostrando el pod recreado automáticamente
- Explicación: "Auto-reparación automática"

**Archivo para anexar:** `evidencia/14-self-healing.png`

---

## 📋 Checklist de evidencia

- [ ] Minikube corriendo (status + cluster-info)
- [ ] Argo CD instalado (pods + UI login)
- [ ] Imágenes Docker construidas (docker images)
- [ ] Application creada en Argo CD (UI)
- [ ] Pods de la aplicación corriendo (kubectl)
- [ ] Servicios accesibles (frontend + gateway)
- [ ] GitOps demo - cambio en Git
- [ ] GitOps demo - OutOfSync detectado
- [ ] GitOps demo - Sincronización automática
- [ ] GitOps demo - Synced confirmado
- [ ] (Opcional) Self-healing demo
- [ ] Log de errores/resolución (troubleshooting)

---

## 🎬 Estructura de presentación sugerida

```
Diapositiva 1: Portada
  - Título: "Argo CD + Kubernetes + GitOps"
  - Grupo
  - Fecha

Diapositiva 2: ¿Qué es Argo CD?
  - Definición simple
  - GitOps concept
  - Ventajas

Diapositiva 3: Arquitectura
  - Diagrama: Git → Argo CD → Kubernetes
  - Componentes principales

Diapositiva 4-5: Demo en vivo
  - Mostrar Minikube corriendo
  - Mostrar Argo CD UI
  - Mostrar pods en Kubernetes

Diapositiva 6-7: GitOps en acción
  - Cambio en Git (aumentar replicas)
  - Estado OutOfSync en Argo CD
  - Estado Synced después
  - Comparación: antes/después

Diapositiva 8: Conclusiones
  - Ventajas de GitOps
  - Casos de uso
  - Preguntas

Apéndice: Screenshots numeradas
  - 01-minikube-status.png
  - 02-argocd-installed.png
  - ... (todas las capturas)
```

---

## 💡 Consejos para la presentación

1. **Practica antes:** Haz todo el setup 2-3 veces antes de presentar
2. **Timing:** Déjate 15-20 min para cambio + sincronización
3. **Contingencia:** Ten capturas de respaldo por si algo falla en vivo
4. **Explicación clara:** Argo CD = Git es fuente de verdad
5. **Evita jerga:** Explica conceptos en palabras simples
6. **Terminal visible:** Aumenta el tamaño de fuente (40pt mínimo)

---

**Grupo:** Software Processes - UDLA  
**Fecha entrega:** [Tu fecha]  
**Presentación:** [Tu fecha/hora]
