# 📊 Estado Actual del Setup - Argo CD Demo

**Fecha:** Junio 5, 2026  
**Estado:** ✅ 80% Completado  
**Próximo:** Construir imágenes Docker

---

## ✅ LO QUE YA ESTÁ FUNCIONANDO

### Minikube
```
Status: RUNNING ✅
Cluster: kubernetes
Contexto: minikube
```

### Argo CD (7/7 pods corriendo)
```
argocd-application-controller-0          ✅
argocd-applicationset-controller-...     ✅
argocd-dex-server-...                    ✅
argocd-notifications-controller-...      ✅
argocd-redis-...                         ✅
argocd-repo-server-...                   ✅
argocd-server-...                        ✅

UI: https://localhost:8443
Usuario: admin
Contraseña: rJKEstJ0j0l3WtA7
```

### Infraestructura (escuela-conduccion namespace)
```
PostgreSQL: ✅ Running
Eureka: ⏳ ContainerCreating (espera imagen Docker)
```

### Services creados
```
api-gateway       NodePort    8080:30080
frontend          NodePort    80:30000
ms-auth           ClusterIP   8081
ms-estudiantes    ClusterIP   8082
eureka            ClusterIP   8761
postgres          ClusterIP   5432
```

---

## ⚠️ LO QUE FALTA

### Imágenes Docker (CRÍTICO)
Necesitas construir estas imágenes en Minikube:

1. **api-gateway:latest**
2. **ms-auth:latest**
3. **ms-estudiantes:latest**
4. **frontend:latest**
5. **eureka-server:latest**

---

## 🚀 PASOS PARA COMPLETAR

### PASO 1: Acceder a Argo CD UI

**Terminal 1:**
```bash
kubectl port-forward svc/argocd-server -n argocd 8443:443
```

**Navegador:**
```
https://localhost:8443
Usuario: admin
Contraseña: rJKEstJ0j0l3WtA7
```

---

### PASO 2: Construir imágenes Docker (15-20 minutos)

**Terminal 2 - Bash/Git Bash (importante: NO PowerShell):**

```bash
# Configurar Docker para Minikube
eval $(minikube docker-env)

# Ir a carpeta del proyecto
cd /c/Users/hmate/OneDrive/Desktop/UDLA/Proyecto\ titulacion

# Build 1: API Gateway
cd backend/api-gateway
mvn clean package -DskipTests
docker build -t api-gateway:latest .
cd ../..

# Build 2: MS-Auth
cd backend/ms-auth
mvn clean package -DskipTests
docker build -t ms-auth:latest .
cd ../..

# Build 3: MS-Estudiantes
cd backend/ms-estudiantes
mvn clean package -DskipTests
docker build -t ms-estudiantes:latest .
cd ../..

# Build 4: Frontend
cd frontend
npm install
npm run build
docker build -t frontend:latest .
cd ..

# Build 5: Eureka
docker pull springcloud/eureka:latest
docker tag springcloud/eureka:latest eureka-server:latest
```

**Tiempo estimado:** 15-20 minutos (Maven es lento)

---

### PASO 3: Verificar que los pods arrancan

**Terminal 3:**
```bash
kubectl get pods -n escuela-conduccion -w
```

Debes ver cambiar de:
- `ErrImagePull` → `ContainerCreating` → `Running` ✅

---

### PASO 4: Crear Application en Argo CD

**Editar:** `kubernetes/argocd/argo-app.yaml`

Cambiar esta línea:
```yaml
repoURL: https://github.com/tu-usuario/tu-repo.git
```

Por tu repositorio real.

**Aplicar:**
```bash
kubectl apply -f kubernetes/argocd/argo-app.yaml
```

**Verificar:**
```bash
kubectl get application -n argocd
# Debes ver: escuela-conduccion    Synced
```

---

## 🎬 ENTONCES PODRÁS:

✅ Ver la aplicación en Argo CD UI (https://localhost:8443)  
✅ Verificar 6 pods corriendo en Kubernetes  
✅ Hacer demo de GitOps (cambio Git → sincronización automática)  
✅ Mostrar Frontend funcionando  

---

## 📋 ESTADO POR COMPONENTE

| Componente | Estado | Acción |
|-----------|--------|--------|
| Minikube | ✅ Corriendo | Nada |
| Argo CD | ✅ Instalado | Nada |
| PostgreSQL | ✅ Running | Nada |
| API Gateway | ⏳ Espera imagen | Build Docker |
| MS-Auth | ⏳ Espera imagen | Build Docker |
| MS-Estudiantes | ⏳ Espera imagen | Build Docker |
| Frontend | ⏳ Espera imagen | Build Docker |
| Eureka | ⏳ Espera imagen | Pull & tag |
| Application (Argo CD) | ⏳ No creada | Editar + apply |

---

## 🆘 TROUBLESHOOTING

### "No puedo acceder a Argo CD en https://localhost:8443"

Verifica:
```bash
# Terminal debe tener port-forward activo:
kubectl port-forward svc/argocd-server -n argocd 8443:443
# Debe mostrar: Forwarding from 127.0.0.1:8443 -> 443
```

### "Docker build toma mucho tiempo"

Normal, Maven toma 5-10 minutos por servicio. Ten paciencia.

Puedes acelerar:
```bash
mvn clean package -DskipTests -q  # -q = quiet
```

### "ImagePullBackOff no cambia a Running"

Verifica que la imagen está construida:
```bash
docker images | grep -E "(api-gateway|ms-auth|frontend|eureka)"
```

Si no aparece, hiciste build en Docker diferente. Revisa que:
```bash
eval $(minikube docker-env)
```
Esté ejecutado en la terminal.

### "Los manifiestos ya existen"

No problema, kubectl apply es idempotente:
```bash
kubectl apply -f kubernetes/argocd/
```

---

## 📞 COMANDOS RÁPIDOS

```bash
# Ver pods
kubectl get pods -n escuela-conduccion

# Ver logs de un pod
kubectl logs <pod-name> -n escuela-conduccion -f

# Revisar estado de Application en Argo CD
kubectl get application -n argocd

# Port-forward para Argo CD
kubectl port-forward svc/argocd-server -n argocd 8443:443

# Port-forward para Frontend
kubectl port-forward svc/frontend -n escuela-conduccion 3000:80

# Ver imagenes disponibles en Minikube
docker images

# Limpiar si algo se rompe
kubectl delete namespace escuela-conduccion
kubectl delete namespace argocd
minikube delete
```

---

## ✨ DESPUÉS DE COMPLETAR

Tendrás:

✅ **Kubernetes local** con 6 microservicios  
✅ **Argo CD** sincronizando desde Git  
✅ **GitOps listo** para demostración  
✅ **Presentación preparada** (guion + checklist)  

---

## 📅 TIMELINE

- **Ahora:** Construir imágenes Docker (15-20 min)
- **+20 min:** Esperar pods, crear Application
- **+5 min:** Verificar todo funciona
- **Total:** 30-40 minutos

---

**Estado:** Setup 80% completo  
**Próximo paso:** PASO 2 - Construir imágenes Docker

¡Puedes hacerlo! 🚀
