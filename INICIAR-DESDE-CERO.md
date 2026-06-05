# 🚀 INICIAR DESDE CERO - Paso a paso detallado

**Duración total:** 30 minutos  
**Objetivo:** Setup completo + Demo funcionando  
**Plataforma:** Windows PowerShell

---

## PASO 1️⃣ - CIERRA TODO (1 minuto)

```
❌ Cierra todas las terminales abiertas
❌ Cierra Chrome
❌ Cierra VS Code
❌ Cierra cualquier ventana relacionada
```

Si Minikube está corriendo, detenerlo:

```powershell
minikube stop
```

Espera a que diga "Stopping" y luego "Stopped".

---

## PASO 2️⃣ - ABRE 5 POWERSHELLS (2 minutos)

### En Windows:

```
Presiona: Windows + X
Selecciona: Windows PowerShell (Administrator)
```

Repite 5 veces, hasta tener 5 ventanas de PowerShell abiertas.

**Renombra cada una mentalmente:**
- PowerShell 1 → "Minikube Dashboard"
- PowerShell 2 → "Port-Forward Argo CD"
- PowerShell 3 → "Verificación"
- PowerShell 4 → "Watch Pods" (minimizar)
- PowerShell 5 → "Git Commands" (minimizar)

---

## PASO 3️⃣ - ABRE GOOGLE CHROME

Abre Google Chrome (vacío, no necesita nada aún).

---

## PASO 4️⃣ - TERMINAL 1: INICIAR MINIKUBE (5 minutos)

### En PowerShell 1 (Minikube Dashboard):

```powershell
minikube start
```

✅ Debes ver output como:

```
* minikube v1.38.1
* Starting "minikube" primary control-plane node
* Pulling base image v0.0.50 ...
* Verifying Kubernetes components...
  - Using image docker.io/kubernetesui/dashboard:v2.7.0
  ...
* Done! kubectl is now configured to use "minikube" cluster
```

**Espera a que diga "Done!"**

Luego en la MISMA PowerShell 1, ejecuta:

```powershell
minikube dashboard
```

✅ Se abrirá automáticamente en Chrome con URL como:

```
http://127.0.0.1:12345
```

**DÉJALO CORRIENDO, no cierres esta terminal.**

---

## PASO 5️⃣ - VERIFICAR MINIKUBE (2 minutos)

### En PowerShell 3 (Verificación):

```powershell
minikube status
```

✅ Debes ver:

```
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```

Si ves "Running" en todo, continúa.

Si ves "Stopped", regresa a PASO 4 y ejecuta `minikube start` nuevamente.

---

## PASO 6️⃣ - TERMINAL 2: PORT-FORWARD ARGO CD (3 minutos)

### En PowerShell 2 (Port-Forward):

```powershell
kubectl port-forward svc/argocd-server -n argocd 8443:443
```

✅ Debes ver:

```
Forwarding from 127.0.0.1:8443 -> 443
Forwarding from [::1]:8443 -> 443
```

**DÉJALO CORRIENDO, no cierres esta terminal.**

---

## PASO 7️⃣ - VERIFICAR ARGO CD (2 minutos)

### En PowerShell 3 (Verificación):

```powershell
kubectl get pods -n argocd | head -10
```

✅ Debes ver pods de Argo CD:

```
NAME                                              READY   STATUS
argocd-application-controller-0                  1/1     Running
argocd-server-76755b46f8-lbm9m                   1/1     Running
argocd-dex-server-569b757-fsb48                  1/1     Running
argocd-redis-b9496d8bf-t79pc                     1/1     Running
...
```

Si ves "Running" en todos, continúa.

---

## PASO 8️⃣ - VERIFICAR APLICACIÓN EN KUBERNETES (2 minutos)

### En PowerShell 3 (Verificación):

```powershell
kubectl get pods -n escuela-conduccion
```

✅ Debes ver 6 pods:

```
NAME                             READY   STATUS
postgres-5c756679b9-cdfrw        1/1     Running
eureka-77dd96fb74-85jgz          1/1     Running
frontend-984995c96-fkrfz         1/1     Running
ms-estudiantes-bd96cd9f8-q6c6t   1/1     Running
api-gateway-776575cdfc-cs8cs     0/1     CrashLoopBackOff  (NORMAL)
ms-auth-64554fb694-tpst4         0/1     CrashLoopBackOff  (NORMAL)
```

⚠️ Los 2 últimos pueden estar en CrashLoopBackOff - **ESO ES NORMAL**.

Continúa.

---

## PASO 9️⃣ - CHROME: MINIKUBE DASHBOARD (1 minuto)

### En Chrome (Pestaña 1):

La URL ya debería estar en Chrome (se abrió automático en PASO 4).

Si no está, busca en la barra de direcciones:

```
http://127.0.0.1 (verifica el puerto exacto)
```

✅ Debes ver:

```
Kubernetes Dashboard
Cluster: minikube
Namespace selector: (dropdown)
```

**Selecciona namespace `escuela-conduccion`** en el dropdown de la esquina superior.

Luego haz clic en:

```
Workloads → Pods
```

✅ Debes ver los 6 pods.

---

## PASO 🔟 - CHROME: ARGO CD LOGIN (2 minutos)

### En Chrome (Pestaña 2 nueva):

En la barra de direcciones, ve a:

```
https://localhost:8443
```

⚠️ Puede aparecer advertencia de "no es seguro" - **IGNORA y haz clic en "Avanzado" → "Continuar de todas formas"**

✅ Debes ver login de Argo CD:

```
Argo CD
Username: _______
Password: _______
```

Ingresa:

```
Username: admin
Password: rJKEstJ0j0l3WtA7
```

Haz clic en "Sign In".

✅ Debes ver dashboard de Argo CD con "escuela-conduccion" en la izquierda.

---

## PASO 1️⃣1️⃣ - VERIFICAR GIT ESTÁ LIMPIO (1 minuto)

### En PowerShell 3 (Verificación):

```powershell
cd C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto\ titulacion
```

Luego:

```powershell
git status
```

✅ Debes ver:

```
On branch main
nothing to commit, working tree clean
```

Si ves cambios, haz:

```powershell
git stash
```

---

## PASO 1️⃣2️⃣ - ORGANIZAR PANTALLA (3 minutos)

### En tu escritorio:

1. **Chrome maximizado en la mitad superior** (70% de la pantalla)
   - Pestaña 1: Minikube Dashboard (http://127.0.0.1:XXXX)
   - Pestaña 2: Argo CD (https://localhost:8443)

2. **PowerShell 4 y 5 en la mitad inferior** (30% de pantalla)
   - PowerShell 4 a la izquierda (Watch Pods)
   - PowerShell 5 a la derecha (Git Commands)

3. **PowerShell 1 y 2 minimizadas** (pero corriendo en background)

---

## ✅ VERIFICACIÓN FINAL - ANTES DE DEMO (1 minuto)

### En PowerShell 3 (Verificación):

Ejecuta TODOS estos comandos y verifica:

```powershell
# 1. Minikube debe estar Running
minikube status
```

✅ Debe decir "Running"

```powershell
# 2. Argo CD debe tener 7 pods
kubectl get pods -n argocd | wc -l
```

✅ Debe mostrar 7 o más

```powershell
# 3. Aplicación debe tener 6 pods
kubectl get pods -n escuela-conduccion
```

✅ Debe mostrar 6 pods

```powershell
# 4. Argo CD UI debe estar accesible
curl https://localhost:8443 -k
```

✅ Debe responder (no error)

---

## 🎬 AHORA ESTÁ LISTO PARA DEMO

Si todos los pasos pasaron ✅, estás listo para:

### Abre kubernetes/DEMO-CLASE.md

Y sigue el ACTO POR ACTO.

---

## ⏱️ TIMELINE DESDE CERO

```
PASO 1:  Cierra todo                          1 min
PASO 2:  Abre 5 PowerShells + Chrome          2 min
PASO 3:  (Chrome abierto)                     -
PASO 4:  Minikube start + dashboard           5 min
PASO 5:  Verifica minikube status             2 min
PASO 6:  Port-forward Argo CD                 3 min
PASO 7:  Verifica Argo CD pods                2 min
PASO 8:  Verifica app en Kubernetes           2 min
PASO 9:  Chrome - Minikube Dashboard          1 min
PASO 10: Chrome - Argo CD login               2 min
PASO 11: Git status verificación              1 min
PASO 12: Organiza pantalla                    3 min
PASO 13: Verificación final                   1 min
         ─────────────────────────
         TOTAL:                              25 minutos

LUEGO:
       Sigue kubernetes/DEMO-CLASE.md         20 minutos
       ─────────────────────────
TOTAL TODO:                                  45 minutos
```

---

## 🆘 SI ALGO FALLA

### Minikube no inicia

```powershell
minikube delete
minikube start
```

### Argo CD no responde

Verifica que PowerShell 2 dice "Forwarding..." - si no, ejecuta:

```powershell
kubectl port-forward svc/argocd-server -n argocd 8443:443
```

### Pods no están corriendo

Espera 2 minutos más, Kubernetes toma tiempo en iniciar.

```powershell
kubectl get pods -n escuela-conduccion -w
```

### Chrome no carga

Limpia caché:
- Ctrl + Shift + Delete
- Borra todo
- Recarga página

---

## 🎯 CHECKLIST FINAL

Antes de empezar la DEMO, verifica:

```
✅ PowerShell 1: Minikube Dashboard corriendo (terminal visible)
✅ PowerShell 2: Port-forward corriendo (terminal visible)
✅ PowerShell 3: Disponible para comandos
✅ PowerShell 4: Minimizada, lista para -w
✅ PowerShell 5: Minimizada, lista para git

✅ Chrome abierto
   ✅ Pestaña 1: Minikube Dashboard (http://...)
   ✅ Pestaña 2: Argo CD (https://localhost:8443)

✅ Todos los pods corriendo o esperados
✅ Git sin cambios (git status limpio)
✅ Tienes kubernetes/DEMO-CLASE.md abierto
✅ Sabes dónde está kubernetes/argocd/apps/01-api-gateway.yaml

✅ LISTO PARA DEMO
```

---

## 🚀 PRÓXIMO PASO

Cuando todo esté listo, abre:

```
kubernetes/DEMO-CLASE.md
```

Y sigue ACTO POR ACTO.

¡Adelante! 🎬
