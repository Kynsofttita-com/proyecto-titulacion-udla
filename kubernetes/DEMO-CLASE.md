# 🎬 GUION DE DEMO — Argo CD + Kubernetes en Clase

**Duración:** 20-25 minutos  
**Objetivo:** Demostrar GitOps funcionando en vivo con sincronización automática  
**Público:** Profesor + compañeros de clase  
**Proyecto:** Escuela de Conducción (Microservicios)

---

## 📋 Montaje de pantallas (IMPORTANTE)

Acomoda 3 ventanas visible:

```
┌──────────────────────┬──────────────────────┐
│  NAVEGADOR           │  TERMINAL 3          │
│  (Argo CD UI +       │  (comandos en vivo)  │
│   Frontend)          │                      │
├──────────────────────┴──────────────────────┤
│  TERMINAL 1: Minikube Dashboard (fondo)     │
│  TERMINAL 2: port-forward (no la toques)    │
└───────────────────────────────────────────┘
```

**Antes de empezar: Abre todo esto**

Terminal 1 (NO TOQUES DESPUÉS):
```bash
minikube dashboard
```

Terminal 2 (NO TOQUES DESPUÉS):
```bash
kubectl port-forward svc/argocd-server -n argocd 8443:443
```

Terminal 3 (AQUÍ escribes):
```bash
# Tu terminal para ejecutar comandos en vivo
```

---

## ⏱️ ACTO 1 — Verificación inicial (2 minutos)

### En Terminal 3, ejecuta:

```bash
# Verificar que Minikube está corriendo
minikube status

# Verificar que Argo CD está instalado
kubectl get pods -n argocd | grep argocd-server

# Verificar que la aplicación está sincronizada
kubectl get application escuela-conduccion -n argocd
```

### Qué debes ver:
```
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
```

### Qué DICES al curso:
> "Tengo un cluster de Kubernetes corriendo localmente con Argo CD instalado. 
> Aquí está mi aplicación de escuela de conducción sincronizada con Git."

---

## ⏱️ ACTO 2 — Arquitectura visual (3 minutos)

### En el navegador con Minikube Dashboard:

1. **Haz clic en `escuela-conduccion` namespace** (esquina superior)
2. **Muestra estos componentes:**

#### Workloads → Deployments
```
Mostrar:
  ✅ api-gateway (1 replica)
  ✅ ms-auth (1 replica)
  ✅ ms-estudiantes (1 replica)
  ✅ frontend (1 replica)
  ✅ postgres (1 replica)
  ✅ eureka (1 replica)
```

**QUÉ DICES:**
> "Aquí se ven todos los deployments de mi aplicación.
> Cada uno es un servicio diferente sincronizado por Argo CD desde Git.
> La columna 'Ready' muestra que todos están corriendo."

#### Workloads → Pods
```
Mostrar:
  ✅ api-gateway-xxxxx (1/1 Running)
  ✅ ms-auth-xxxxx (1/1 Running)
  ✅ ms-estudiantes-xxxxx (1/1 Running)
  ✅ frontend-xxxxx (1/1 Running)
  ✅ postgres-xxxxx (1/1 Running)
  ✅ eureka-xxxxx (1/1 Running)
```

**QUÉ DICES:**
> "Aquí están los 6 pods corriendo.
> Cada pod es una instancia de cada servicio.
> La magia: todo esto se sincroniza automáticamente desde Git gracias a Argo CD."

#### Network → Services
```
Mostrar:
  ✅ api-gateway (NodePort)
  ✅ frontend (NodePort)
  ✅ ms-auth (ClusterIP)
  ✅ ms-estudiantes (ClusterIP)
  ✅ postgres (ClusterIP)
  ✅ eureka (ClusterIP)
```

**QUÉ DICES:**
> "Los Services exponen los pods a la red.
> NodePort = accesible desde afuera (frontend, gateway)
> ClusterIP = accesible solo dentro del cluster"

---

## ⏱️ ACTO 3 — Argo CD UI (3 minutos)

### En el navegador, nueva pestaña:

**URL:** https://localhost:8443

**Login:** 
- Usuario: `admin`
- Contraseña: [Te la mostré al inicio]

### Qué debes mostrar:

1. **Dashboard de Argo CD**
   - Botón "escuela-conduccion" en la izquierda
   - Estado: **Synced ✅** (verde)

2. **Clickea en "escuela-conduccion"**
   - Árbol de recursos (tree view)
   - Mostrar estructura:
     ```
     escuela-conduccion (Application)
       ├── Namespace
       ├── PostgreSQL (Deployment, Service, PVC)
       ├── Eureka (Deployment, Service)
       ├── API Gateway (Deployment, Service)
       ├── MS-Auth (Deployment, Service)
       ├── MS-Estudiantes (Deployment, Service)
       └── Frontend (Deployment, Service)
     ```

3. **Pestaña "Source"**
   - Mostrar: Repository = `https://github.com/tu-repo`
   - Path = `kubernetes/argocd`
   - Branch = `main`

**QUÉ DICES:**
> "Argo CD está mirando continuamente mi repositorio Git.
> La carpeta 'kubernetes/argocd' contiene todos los manifiestos.
> El estado 'Synced' significa que Kubernetes coincide exactamente con lo que dice Git.
> Si alguien cambia algo en Git, Argo CD lo sincroniza automáticamente."

---

## ⏱️ ACTO 4 — Aplicación funcionando (3 minutos)

### En el navegador, nueva pestaña:

**Frontend:** http://localhost:3000

**QUÉ DICES:**
> "Aquí está el frontend de la aplicación corriendo dentro del cluster.
> Viene del contenedor Docker del pod de frontend, sincronizado por Argo CD.
> Los usuarios accederían a través de este navegador."

### Opcional: Probar endpoints del API Gateway

**URL:** http://localhost:8080/actuator/health

**Debería responder:**
```json
{
  "status": "UP"
}
```

**QUÉ DICES:**
> "El API Gateway está saludable y respondiendo requests.
> Es el punto de entrada único a todos los microservicios."

---

## ⏱️ ACTO 5 — GitOps en acción ⭐ (EL MOMENTO ESTRELLA - 8 minutos)

### Escena: Demostración de cambio automático

**Antes de esto:**
- Abre **2 terminales más** (Terminal 4 y 5)
- Divide pantalla: Terminal 4 (watch) + Navegador (Argo CD)

---

### PASO 1: Monitorear cambios (Terminal 4)

```bash
# Modo observador: verás en tiempo real los pods
kubectl get deployments -n escuela-conduccion -w
```

Debe mostrar:
```
NAME            READY   UP-TO-DATE   AVAILABLE
api-gateway     1/1     1            1
ms-auth         1/1     1            1
ms-estudiantes  1/1     1            1
...
```

**QUÉ DICES:**
> "Voy a activar el modo vigilancia (-w = watch).
> Voy a cambiar algo en Git y verán cómo Kubernetes se actualiza automáticamente."

---

### PASO 2: Cambio en Git (Terminal 5)

```bash
# Ir a la carpeta del proyecto
cd /c/Users/hmate/OneDrive/Desktop/UDLA/Proyecto\ titulacion

# Editar el archivo de api-gateway
code kubernetes/argocd/apps/01-api-gateway.yaml
```

**CAMBIO QUE VAS A HACER:**

Busca esta línea:
```yaml
  replicas: 1
```

Cámbiala a:
```yaml
  replicas: 3
```

Guarda (Ctrl+S) y cierra el editor.

**QUÉ DICES:**
> "Acabo de cambiar el número de réplicas del API Gateway de 1 a 3.
> Ahora voy a hacer commit y push para que Argo CD lo vea."

---

### PASO 3: Commit y Push (Terminal 5)

```bash
# Agregar el cambio
git add kubernetes/argocd/apps/01-api-gateway.yaml

# Commit con mensaje profesional
git commit -m "Sprint 12 (Demo GitOps - Aumentar réplicas API Gateway a 3)"

# Push al repositorio
git push origin main
```

**Debería responder:**
```
[main 1a2b3c4d] Sprint 12 (Demo GitOps - Aumentar réplicas API Gateway a 3)
 1 file changed, 1 insertion(+), 1 deletion(-)
```

**QUÉ DICES:**
> "Hice commit y push del cambio a Git.
> Ahora Argo CD va a detectarlo (en 10-30 segundos)."

---

### PASO 4: Esperar detección de cambios (Navegador - Argo CD)

En Argo CD UI, mira el estado de "escuela-conduccion":

**Antes:** ✅ Synced  
**Ahora:** ⚠️ OutOfSync (puede tomar 10-30 segundos)

**QUÉ DICES:**
> "¡Argo CD detectó que Git tiene cambios que no están en Kubernetes!
> El estado cambió de 'Synced' a 'OutOfSync'.
> Ahora va a sincronizarse automáticamente."

---

### PASO 5: Sincronización automática (Argo CD UI)

**En Argo CD UI:**
1. Click en botón "Sync" (si no está en auto-sync)
2. Muestra el progreso de sincronización

**En Terminal 4 (watch):**
Ver aparecer nuevos pods:

```
api-gateway     1/1     1            1        (línea anterior)
api-gateway     2/3     2            2        (actualización en vivo)
api-gateway     3/3     3            3        (COMPLETO!)
```

**QUÉ DICES mientras ves el cambio:**
> "¡Miren! En tiempo real, Kubernetes está creando 2 pods nuevos del API Gateway.
> Se puede ver en el watch (-w) como van subiendo de 1/3 a 2/3 a 3/3.
> Todo esto pasó automáticamente porque cambié un archivo en Git.
> ESO es GitOps."

---

### PASO 6: Confirmación final (Argo CD UI)

En Argo CD, después de 1-2 minutos:

**Estado:** ✅ Synced (verde)

En Terminal 4, cuando termine:
```
api-gateway     3/3     3            3        ✅
```

**QUÉ DICES:**
> "Perfecto, ahora está 'Synced' de nuevo.
> Git y Kubernetes son idénticos nuevamente.
> Kubernetes tiene exactamente lo que Git dice que debe tener.
> ESO es GitOps: Git es la fuente de verdad."

---

## ⏱️ ACTO 6 — Rollback automático (OPCIONAL - 3 minutos)

### Si quieres impresionar más, muestra auto-reparación:

**En Terminal 5:**

```bash
# Listar pods de api-gateway
kubectl get pods -n escuela-conduccion | grep api-gateway

# Eliminar uno a propósito
kubectl delete pod api-gateway-xxxxx -n escuela-conduccion
# (reemplaza xxxxx con el nombre real)
```

**En Terminal 4 (watch):**
Ver como el pod aparece en Terminating y otro nuevo en ContainerCreating.

**QUÉ DICES:**
> "Eliminé un pod a la fuerza.
> Kubernetes detectó que ahora hay 2/3 réplicas en lugar de 3.
> Automáticamente creó uno nuevo para mantener 3.
> Eso es 'self-healing' - auto-recuperación."

---

## 🎯 ACTO 7 — Resumen y conclusiones (2 minutos)

### Qué demostraste:

1. ✅ **Kubernetes corriendo** con 6 servicios
2. ✅ **Argo CD sincronizando** desde Git
3. ✅ **GitOps en acción** - cambio Git → automático en Kubernetes
4. ✅ **Auto-sincronización** - detecta cambios en 10-30 segundos
5. ✅ **Aplicación funcionando** - frontend + servicios accesibles

### Qué DICES al final:

> "Lo importante que demostramos hoy:
>
> **Primero:** Kubernetes es un orquestador de contenedores.
> Mantiene tus aplicaciones disponibles automáticamente.
> 
> **Segundo:** Argo CD implementa GitOps.
> Tu infraestructura se describe en archivos YAML en Git.
> Los cambios se sincronizan automáticamente en el cluster.
> 
> **Tercero:** Git es la fuente de verdad.
> Cualquiera puede ver el historial completo de cambios.
> Si algo se rompe, revertes el commit y todo vuelve a estar bien.
> 
> Esto es lo que usan empresas como Google, Netflix y Amazon.
> Es la forma moderna de hacer DevOps."

---

## 🆘 PLAN B — Si algo falla en vivo

| Problema | Solución rápida |
|----------|-----------------|
| Argo CD no abre | Revisa Terminal 2: debe decir `Forwarding from 127.0.0.1:8443 -> 443` |
| Frontend no abre | `kubectl port-forward svc/frontend -n escuela-conduccion 3000:80` |
| Los pods no suben | Esperar 30 segundos más, Kubernetes toma tiempo |
| Git no sincroniza | Esperar 60 segundos, Argo CD hace polling cada 3 minutos |
| Todo se rompió | Muestra las **capturas de pantalla de respaldo** |

---

## 📸 Capturas que debes guardar de respaldo

Antes de la clase, haz un ensayo completo y guarda estas pantallas:

1. **Minikube Dashboard** - Todos los pods corriendo
2. **Argo CD Login** - UI accesible
3. **Argo CD Tree** - Árbol de recursos
4. **Frontend funcionando** - Navegador con la app
5. **git diff** - Cambio visible en terminal
6. **OutOfSync** - Estado en Argo CD
7. **Sincronizando** - Pods apareciendo en watch
8. **Synced** - Estado final en Argo CD
9. **Verificación final** - 3 replicas en lugar de 1

Si algo falla en vivo, mostras estas capturas y explicas qué pasó.

---

## ⏰ Timeline estimado

```
Verificación inicial       2 min
Arquitectura visual        3 min
Argo CD UI                 3 min
Aplicación funcionando     3 min
GitOps demo (ESTRELLA)     8 min
Self-healing (optional)    3 min
Conclusiones               2 min
────────────────────────────────
TOTAL                   20-25 min
```

---

## 🎓 Qué explicar de fondo (para preguntas)

### ¿Qué es Kubernetes?
> "Sistema que orquesta y gestiona contenedores Docker.
> Mantiene tus aplicaciones corriendo, las escala automáticamente,
> y se recupera si algo falla."

### ¿Qué es Argo CD?
> "Herramienta que implementa GitOps.
> Sincroniza continuamente lo que dicen tus archivos YAML en Git
> con lo que está corriendo en Kubernetes."

### ¿Qué es GitOps?
> "Paradigma donde Git es la fuente de verdad de tu infraestructura.
> Cambios = commits. Rollbacks = revertir commits.
> Auditoría completa en git log."

### ¿Por qué es importante?
> "Reproducibilidad: otro developer corre `git clone` + `kubectl apply`
> y tiene exactamente lo mismo.
> Auditoría: ves quién cambió qué y cuándo.
> Rollback automático: si algo se rompe, revertes el commit."

---

## ✅ Checklist antes de la clase

- [ ] Minikube corriendo
- [ ] Argo CD instalado y accesible
- [ ] Imágenes Docker construidas
- [ ] Application sincronizada
- [ ] Frontend accesible
- [ ] 3 terminales + navegador listos
- [ ] Cambio de Git preparado (replicas 1 → 3)
- [ ] Screenshots de respaldo guardadas
- [ ] Presentación ensayada 1-2 veces
- [ ] Timing practicado (debe caber en 25 min)

---

## 🎬 Comando para empezar TODO desde cero (si necesitas reset)

```powershell
# SOLO si Minikube está parado
# Esto toma ~10 minutos

minikube start --cpus=4 --memory=8192
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl apply -f kubernetes/argocd/

# Esperar ~2 minutos a que Argo CD se inicie
# Luego verificar:
kubectl get pods -n argocd
kubectl get pods -n escuela-conduccion

# Ya puedes empezar la demo
```

---

**¡Éxito en la presentación! 🚀**

Grupo: Software Processes - UDLA  
Junio 2026
