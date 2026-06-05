# 🎬 DEMO ARGO CD - PASO A PASO PARA EJECUTAR

**Duración:** 20 minutos  
**Terminales necesarias:** 5  
**Resultado:** Demostración de GitOps en vivo

---

## 📺 DISPOSICIÓN DE PANTALLA

Acomoda 5 ventanas así:

```
LAPTOP / PANTALLA:

TOP (70% de la pantalla):
┌─────────────────────────────────────────────────────────┐
│                                                         │
│         NAVEGADOR (2 pestañas abiertas)                │
│  ┌──────────────────┬──────────────────────────────┐   │
│  │ Pestaña 1:       │ Pestaña 2:                   │   │
│  │ Minikube         │ Argo CD                      │   │
│  │ http://...       │ https://localhost:8443       │   │
│  └──────────────────┴──────────────────────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘

BOTTOM (30% de la pantalla):
┌──────────────────────┬──────────────────────────────────┐
│  TERMINAL 4          │  TERMINAL 5                      │
│  (watch)             │  (git commands)                  │
│                      │                                  │
└──────────────────────┴──────────────────────────────────┘

BACKGROUND (no visible, pero corriendo):
- TERMINAL 1: Minikube Dashboard (DÉJALO CORRIENDO)
- TERMINAL 2: Argo CD port-forward (DÉJALO CORRIENDO)
- TERMINAL 3: Disponible para comandos
```

---

## ⚡ SETUP INICIAL (5 minutos)

### TERMINAL 1 - MINIKUBE DASHBOARD

Ejecuta ESTO y déjalo corriendo (se abre solo en navegador):

```bash
minikube dashboard
```

✅ Se abrirá automáticamente en `http://127.0.0.1:xxxxx`

---

### TERMINAL 2 - ARGO CD PORT-FORWARD

Ejecuta ESTO y déjalo corriendo (NO lo cierres):

```bash
kubectl port-forward svc/argocd-server -n argocd 8443:443
```

✅ Debe mostrar: `Forwarding from 127.0.0.1:8443 -> 443`

---

### NAVEGADOR - Abre 2 pestañas

**Pestaña 1 - Minikube Dashboard:**
```
http://127.0.0.1:XXXXX  (se abrió automáticamente desde Terminal 1)
```

**Pestaña 2 - Argo CD UI:**
```
https://localhost:8443
Usuario: admin
Contraseña: rJKEstJ0j0l3WtA7
```

---

## 🎬 ACTO 1 - Verificación inicial (2 minutos)

### TERMINAL 3 - Ejecuta:

```bash
minikube status
```

✅ Debes ver:
```
host: Running
kubelet: Running
apiserver: Running
```

Luego:

```bash
kubectl get pods -n escuela-conduccion
```

✅ Debes ver 6 pods (algunos Running, algunos con problemas - es normal)

---

## 🎬 ACTO 2 - Mostrar arquitectura en Minikube (3 minutos)

### NAVEGADOR - Pestaña 1 (Minikube Dashboard)

1. **Selecciona namespace:** Esquina superior, selecciona `escuela-conduccion`

2. **Haz clic en Workloads → Deployments**
   - Debes ver 6 deployments (api-gateway, ms-auth, ms-estudiantes, frontend, postgres, eureka)

3. **Haz clic en Workloads → Pods**
   - Debes ver 6 pods

4. **Haz clic en Network → Services**
   - Debes ver 6 servicios

**QUÉ DICES:**
> "Aquí ven mi aplicación en Kubernetes. Tengo 6 servicios sincronizados automáticamente por Argo CD desde mi repositorio Git."

---

## 🎬 ACTO 3 - Mostrar Argo CD UI (3 minutos)

### NAVEGADOR - Pestaña 2 (Argo CD)

1. **En la izquierda, haz clic en `escuela-conduccion`**

2. **Observa el árbol de recursos** (ves todo lo que hay desplegado)

3. **Observa el estado** - debe estar **Synced ✅** (verde)

4. **Haz clic en la pestaña "Source"**
   - Repository: `https://github.com/tu-repo`
   - Path: `kubernetes/argocd`

**QUÉ DICES:**
> "Argo CD monitorea continuamente mi repositorio Git. El estado 'Synced' significa que Kubernetes tiene exactamente lo que Git dice que debe tener."

---

## 🎬 ACTO 4 - Ver aplicación corriendo (2 minutos)

### NAVEGADOR - Nueva pestaña

Abre: `http://localhost:3000`

Debes ver una página HTML con info de tu proyecto.

**QUÉ DICES:**
> "El frontend está sirviendo desde un contenedor dentro de Kubernetes. Todo sincronizado automáticamente por Argo CD."

---

## ⭐ ACTO 5 - DEMO GITOPS EN VIVO (8 minutos - EL MOMENTO IMPORTANTE)

### PASO 1: Preparar visualización

**TERMINAL 4 - Ejecuta ESTO (no cierres después):**

```bash
kubectl get deployments -n escuela-conduccion -w
```

✅ Debes ver una lista que se actualiza en tiempo real

---

### PASO 2: Cambiar algo en Git

**TERMINAL 3 - Ejecuta estos comandos:**

```bash
cd /c/Users/hmate/OneDrive/Desktop/UDLA/Proyecto\ titulacion
```

Luego abre el archivo en tu editor favorito:

```bash
code kubernetes/argocd/apps/01-api-gateway.yaml
```

O si no tienes `code`:

```bash
notepad kubernetes/argocd/apps/01-api-gateway.yaml
```

**CAMBIO QUE DEBES HACER:**

Busca esta línea (está en la línea ~6):

```yaml
  replicas: 1
```

Cámbiala a:

```yaml
  replicas: 3
```

Guarda el archivo (Ctrl+S) y ciérralo.

**QUÉ DICES:**
> "Acabo de cambiar el número de réplicas de 1 a 3. Ahora voy a hacer commit para que Argo CD lo vea."

---

### PASO 3: Commit y push a Git

**TERMINAL 3 - Ejecuta:**

```bash
git add kubernetes/argocd/apps/01-api-gateway.yaml
```

Luego:

```bash
git commit -m "Sprint 12 (Demo - Aumentar réplicas API Gateway a 3)"
```

Luego:

```bash
git push origin main
```

✅ Debes ver respuesta de git (puede tomar 5-10 segundos)

**QUÉ DICES:**
> "Hice commit y push. Ahora Argo CD va a detectar este cambio en los próximos 30 segundos."

---

### PASO 4: Esperar a que Argo CD detecte el cambio

**NAVEGADOR - Pestaña 2 (Argo CD)**

Mira el estado de "escuela-conduccion" en la UI.

Debe cambiar de **Synced ✅** a **OutOfSync ⚠️** en los próximos 10-30 segundos.

**QUÉ DICES MIENTRAS ESPERAS:**
> "Argo CD está detectando que Git tiene cambios que no están en Kubernetes. Debería cambiar el estado a 'OutOfSync' en unos segundos..."

---

### PASO 5: Ver sincronización en tiempo real

En cuanto veas **OutOfSync** en Argo CD:

**NAVEGADOR - Pestaña 2 (Argo CD)**

Haz clic en el botón **"Sync"** (si no está en auto-sync)

O simplemente espera, Argo CD debería sincronizar automáticamente.

**MIENTRAS TANTO, en TERMINAL 4 (el -w que dejaste corriendo):**

Verás cambios como:

```
NAME            READY   UP-TO-DATE   AVAILABLE
api-gateway     1/1     1            1          (ANTES)

api-gateway     2/3     2            2          (DURANTE - ¡LO VES!)

api-gateway     3/3     3            3          (DESPUÉS - ¡LISTO!)
```

**QUÉ DICES mientras observas:**
> "¡Miren! En tiempo real, Kubernetes está creando 2 pods adicionales. La columna READY sube de 1/1 a 2/3 a 3/3. Todo esto pasó automáticamente porque cambié un archivo en Git."

---

### PASO 6: Confirmación final

Después de ~2 minutos:

**NAVEGADOR - Pestaña 2 (Argo CD)**

El estado debe volver a **Synced ✅** (verde)

**TERMINAL 4**

Debes ver 3/3 en la fila de api-gateway

**QUÉ DICES:**
> "Perfecto. Ahora está 'Synced' de nuevo. Kubernetes tiene exactamente 3 réplicas como Git dice que debe tener. ESO es GitOps - Git es la fuente de verdad."

---

## 🏁 Resumen final (1 minuto)

**QUÉ DICES:**

> "Lo que acabamos de ver es GitOps en acción:
> 
> 1. Cambié un archivo en Git (replicas: 1 → 3)
> 2. Hice commit y push
> 3. Argo CD detectó automáticamente el cambio
> 4. Kubernetes aplicó el cambio sin comandos manuales
> 5. Los pods se crearon automáticamente
> 
> Esto es lo que usan empresas como Google y Netflix. Git es la fuente de verdad, y tu infraestructura siempre coincide con tu código."

---

## 🆘 SI ALGO FALLA

| Problema | Solución |
|----------|----------|
| Argo CD no carga | Verifica Terminal 2, debe decir "Forwarding..." |
| No veo cambios en -w | Espera 30 segundos, a veces tarda |
| OutOfSync no aparece | Espera 1 minuto, Argo CD hace polling cada 3 min |
| Git push falla | Verifica git status, quizás hay conflictos |

---

## ⏱️ TIMING

```
Acto 1: 2 min
Acto 2: 3 min
Acto 3: 3 min
Acto 4: 2 min
Acto 5: 8 min
Resumen: 1 min
────────
TOTAL: 19 minutos ✅
```

---

## 📋 CHECKLIST ANTES DE EMPEZAR

- [ ] Terminal 1 corriendo: `minikube dashboard`
- [ ] Terminal 2 corriendo: `kubectl port-forward svc/argocd-server...`
- [ ] Navegador con 2 pestañas abiertas
- [ ] Minikube Dashboard accesible
- [ ] Argo CD accesible (login exitoso)
- [ ] Terminal 3 lista para comandos
- [ ] Terminal 4 lista para `-w`
- [ ] Terminal 5 lista para git
- [ ] Archivos guardados en editor

---

## 🎯 LISTO

Sigue este documento paso a paso y tu demo de Argo CD + GitOps será exitosa.

¡Buena suerte! 🚀
