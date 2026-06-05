# 🚀 EJECUTAR SETUP AUTOMÁTICO

**Este script hace TODO el setup automáticamente en 5-10 minutos.**

---

## ⚡ OPCIÓN RÁPIDA (recomendado)

### Paso 1: Abre PowerShell

```powershell
# Presiona: Win + X → PowerShell (Admin)
# O:
# Búsqueda → PowerShell → Click derecho → Run as Administrator
```

### Paso 2: Ve a la carpeta del proyecto

```powershell
cd "C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto titulacion"
```

### Paso 3: Ejecuta el script

```powershell
.\SETUP-DEMO.ps1
```

**Eso es TODO.** El script hace:
- ✅ Verifica Minikube (lo inicia si está caído)
- ✅ Instala Argo CD (si no está)
- ✅ Crea la Application
- ✅ Sincroniza (despliega 8 microservicios)
- ✅ Espera a que todos los pods estén Running (3-5 min)
- ✅ Valida que todo funciona
- ✅ Te da las instrucciones finales

---

## 📊 QUÉ VAS A VER

Mientras se ejecuta, verás:

```
🚀 INICIANDO SETUP COMPLETO PARA DEMO
=========================================

📍 PASO 1: Verificar Minikube
---
✅ Minikube está corriendo

📍 PASO 2: Verificar Argo CD
---
✅ Argo CD ya está instalado

📍 PASO 3: Contraseña Argo CD
---
🔐 Contraseña Argo CD: abc123def456

📍 PASO 4: Crear Application Argo CD
---
✅ Application 'proyecto-titulacion' ya existe

📍 PASO 5: Sincronizar (desplegar todo)
---
⏳ Sincronizando... esto tarda 3-5 minutos

✅ Sincronización iniciada

📍 PASO 6: Esperando a que todos los pods estén Running
---
⏳ Esto puede tardar 3-5 minutos...
Intento 1/60 - Pods: 2/14 Running
Intento 2/60 - Pods: 4/14 Running
Intento 3/60 - Pods: 7/14 Running
...
✅ Todos los pods están Running

📍 PASO 7: Estado actual de pods
---
NAME                              READY   STATUS
dev-postgres-xxx                  1/1     Running
dev-rabbitmq-xxx                  1/1     Running
dev-minio-xxx                     1/1     Running
dev-eureka-server-xxx             1/1     Running
dev-api-gateway-xxx               1/1     Running
dev-ms-auth-xxx                   1/1     Running
dev-ms-estudiantes-xxx            1/1     Running
... (8 MS en total)

✅ SETUP COMPLETADO
=========================================

📍 SIGUIENTE PASO: PREPARAR PARA DEMO

Abre 4 terminales PowerShell (antes de la clase):

Terminal 1 (Dashboard):
  minikube dashboard

Terminal 2 (Monitor de pods):
  kubectl get pods -n escuela --watch

Terminal 3 (Port-forward Eureka):
  kubectl port-forward -n escuela svc/dev-eureka-server 8761:8761

Navegador (2 pestañas):
  Pestaña 1: https://localhost:8443 (Argo CD)
  Pestaña 2: http://localhost:8761 (Eureka)

Luego en Terminal 4:
  cd C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto titulacion
  Abre: kubernetes\DEMO-CLASE.md
  Sigue los ACTOS paso a paso

✨ ¡LISTO PARA DEMO!

🔍 Verificación final:
✅ 12+ pods running - LISTO PARA CLASE

=========================================
Script completado. ¡Éxito en la presentación! 🚀
=========================================
```

---

## ⏱️ TIEMPO ESPERADO

```
Paso 1 (Minikube):           < 1 min
Paso 2 (Argo CD):             1-2 min (si ya está instalado, 0 seg)
Paso 3 (Contraseña):          < 1 min
Paso 4 (Application):         < 1 min
Paso 5 (Sincronizar):         < 1 min
Paso 6 (Esperar pods):        3-5 min ⏳
Paso 7 (Ver pods):            < 1 min
Paso 8 (Validar):             < 1 min
────────────────────────────────────────
TOTAL:                        6-10 minutos
```

---

## ✅ CUANDO TERMINE, VERÁS

```
✅ Todos los pods están Running
✅ API Gateway responde
✅ Argo CD Application está sincronizada
✅ 30+ recursos en el cluster
✅ LISTO PARA CLASE
```

---

## 🎯 DESPUÉS DEL SCRIPT: PREPARAR PARA DEMO

Una vez que el script termine, abre **4 terminales** (ANTES de entrar a clase):

### **Terminal 1: Dashboard**
```powershell
minikube dashboard
# Se abre navegador solo con Kubernetes Dashboard
```

### **Terminal 2: Watch de pods**
```powershell
kubectl get pods -n escuela --watch
# Mostrará cambios en tiempo real cuando hagas cambios en Git
```

### **Terminal 3: Port-forward Eureka**
```powershell
kubectl port-forward -n escuela svc/dev-eureka-server 8761:8761
# Dejarlo corriendo, verás: Forwarding from 127.0.0.1:8761 -> 8761
```

### **Navegador: 2 pestañas**
```
Pestaña 1: https://localhost:8443
  → Login: admin / (contraseña del script)
  → Ir a: Applications → proyecto-titulacion

Pestaña 2: http://localhost:8761
  → Ver Eureka con 10 servicios registrados
```

### **Terminal 4: Comandos del demo**
```powershell
# Aquí ejecutarás los comandos de DEMO-CLASE.md
cd "C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto titulacion"
code kubernetes\DEMO-CLASE.md
```

---

## 🆘 SI ALGO FALLA EN EL SCRIPT

### Error: "minikube: command not found"
```powershell
# Instalar Minikube
choco install minikube
# O descargar desde: https://github.com/kubernetes/minikube/releases
```

### Error: "kubectl: command not found"
```powershell
# Instalar kubectl
choco install kubernetes-cli
```

### Error: "Pod stuck in ContainerCreating"
```powershell
# Ver logs del pod
kubectl logs -f deployment/dev-ms-auth -n escuela

# Si tarda mucho, aumentar timeout del script editando:
# Línea 107: $max_attempts = 120  (en lugar de 60)
```

### Error: "Application already exists"
```powershell
# Borrar y recrear
kubectl delete application proyecto-titulacion -n argocd
kubectl apply -f kubernetes/argocd-application.yml
```

---

## 📝 RESUMEN: FLUJO COMPLETO

```
1. Ejecutas:  .\SETUP-DEMO.ps1
                    ↓
2. Script hace todo automáticamente
   - Minikube ✅
   - Argo CD ✅
   - Despliega 8 MS ✅
   - Valida ✅
                    ↓
3. Script te dice: "LISTO PARA CLASE"
                    ↓
4. Tú abres 4 terminales + navegador
                    ↓
5. Ejecutas: kubernetes\DEMO-CLASE.md
                    ↓
6. Demo en vivo funciona perfecto 🎉
```

---

## ⚡ COMANDO TODO EN UNO

Si quieres ejecutar en una sola línea:

```powershell
cd "C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto titulacion"; .\SETUP-DEMO.ps1
```

---

## ✨ RESULTADO ESPERADO

**Después de ejecutar el script, tienes:**

✅ Minikube corriendo  
✅ Argo CD instalado y funcionando  
✅ 8 microservicios desplegados  
✅ 12+ pods en estado Running  
✅ API Gateway respondiendo  
✅ Eureka mostrando 10 servicios  
✅ GitHub conectado a Argo CD  
✅ TODO LISTO PARA HACER LA DEMO EN CLASE  

---

**¡Ejecuta el script y luego abre `kubernetes\DEMO-CLASE.md`!** 🚀
