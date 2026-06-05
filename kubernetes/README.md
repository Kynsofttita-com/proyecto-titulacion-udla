# Kubernetes Manifests + Argo CD

**Infraestructura como Código (IaC) para el proyecto de titulación.**

---

## 📁 Estructura

```
kubernetes/
├── base/                           # Recursos base comunes
│   ├── postgres.yml               # Base de datos PostgreSQL
│   ├── rabbitmq.yml               # Message broker
│   ├── minio.yml                  # Object storage (S3-compatible)
│   ├── eureka.yml                 # Service discovery
│   ├── api-gateway.yml            # API Gateway (entry point)
│   ├── ms-auth.yml                # Microservicio: Autenticación
│   ├── ms-estudiantes.yml         # Microservicio: Estudiantes
│   ├── ms-instructores.yml        # Microservicio: Instructores
│   ├── ms-vehiculos.yml           # Microservicio: Vehículos
│   ├── ms-asignaciones.yml        # Microservicio: Asignaciones
│   ├── ms-cobros.yml              # Microservicio: Cobros
│   ├── ms-reportes.yml            # Microservicio: Reportes
│   ├── ms-notificaciones.yml      # Microservicio: Notificaciones
│   └── kustomization.yml          # Agrupa todos los recursos base
│
├── overlays/                       # Customizaciones por ambiente
│   └── dev/                        # Ambiente: Desarrollo
│       ├── kustomization.yml      # Reduce replicas, agrega labels
│       └── namespace-patch.yml    # Namespace específico de dev
│
└── argocd-application.yml         # Definición de Application en Argo CD
```

---

## 🚀 Uso Rápido

### 1. Validar manifiestos

```bash
# Verificar sintaxis YAML
kubeval base/*.yml overlays/dev/*.yml

# Previsualizar resources que serán creados
kustomize build overlays/dev
```

### 2. Desplegar con kubectl (sin Argo CD)

```bash
# Aplicar todos los manifiestos
kubectl apply -k overlays/dev

# Ver resources creados
kubectl get all -n escuela

# Ver logs
kubectl logs -f deployment/ms-auth -n escuela
```

### 3. Desplegar con Argo CD (GitOps)

```bash
# Crear Application en Argo CD
kubectl apply -f argocd-application.yml

# O en UI: Argo CD → Applications → New Application
# Completar:
#   Repository: tu-repo
#   Path: kubernetes/overlays/dev
#   Namespace: escuela
#   Sync Policy: Automatic

# Ver status
argocd app get proyecto-titulacion
```

---

## 📋 Recursos Desplegados

### Infraestructura

| Recurso | Puerto | Descripción |
|---------|--------|---|
| PostgreSQL | 5432 | Base de datos (9 schemas) |
| RabbitMQ | 5672/15672 | Message broker + Management UI |
| MinIO | 9000/9001 | Object storage (S3 compatible) |

### Servicios

| Servicio | Puerto | Replicas (dev) | Descripción |
|----------|--------|---|---|
| Eureka | 8761 | 1 | Service discovery |
| API Gateway | 8080 | 1-2 | Punto de entrada único |

### Microservicios

| MS | Puerto | Replicas (dev) | Schema | Descripción |
|---|--------|---|---|---|
| MS-Auth | 8081 | 1 | auth | Autenticación + JWT |
| MS-Estudiantes | 8082 | 1 | estudiantes | Gestión de estudiantes |
| MS-Instructores | 8083 | 1 | instructores | Gestión de instructores |
| MS-Vehículos | 8084 | 1 | vehiculos | Flota de vehículos |
| MS-Asignaciones | 8085 | 1 | asignaciones | Clases (instructor+estudiante+vehículo) |
| MS-Cobros | 8086 | 1 | cobros | Pagos e invoices |
| MS-Reportes | 8087 | 1 | reportes | Analytics y dashboards |
| MS-Notificaciones | 8088 | 1 | notificaciones | Email y notificaciones in-app |

---

## 🔄 GitOps Workflow

```
Developer                         GitHub                            Kubernetes
    │                               │                                   │
    ├─→ git push cambios ─────────→ │                                   │
    │                               │                                   │
    │                               ├─→ Webhook notifica Argo CD       │
    │                               │                                   │
    │                               │      Argo CD sincroniza ───────→ │
    │                               │                                   │
    │                               │      kubectl apply               │
    │                               │      nuevos pods arrancan         │
    │                               │                                   │
    │  ← cluster actualizado ─────← ←─ Reporte de sync               │
```

**Flujo:**
1. Developer hace `git push` en `/kubernetes`
2. GitHub webhook notifica a Argo CD
3. Argo CD detecta cambio en ~30 segundos
4. Argo CD compara Git vs Cluster (OutOfSync si hay diferencias)
5. Auto-sync recrea/actualiza recursos
6. Cluster refleja estado de Git

---

## 🔐 Variables de Configuración

### ConfigMaps (público)

```bash
# Credenciales publicas
kubectl get configmap -n escuela

# Ver contenido
kubectl get configmap postgres-config -n escuela -o yaml
```

### Secrets (encriptado)

```bash
# Credenciales secretas (encriptadas)
kubectl get secret -n escuela

# Ver secret (encode base64)
kubectl get secret postgres-secret -n escuela -o jsonpath="{.data.POSTGRES_PASSWORD}" | base64 -d
```

---

## 📊 Monitoreo

### Ver estado de resources

```bash
# Todos los resources
kubectl get all -n escuela

# Detalle de deployment
kubectl describe deployment ms-auth -n escuela

# Logs de un pod
kubectl logs -f deployment/ms-auth -n escuela --tail=50

# Ejecutar comando dentro de pod
kubectl exec -it deployment/ms-auth -n escuela -- /bin/bash
```

### Argo CD UI

```
https://localhost:8443
Applications → proyecto-titulacion

Ver:
  - SYNC STATUS: Synced/OutOfSync/Syncing
  - HEALTH STATUS: Healthy/Degraded/Unknown
  - Resource tree: todos los resources
  - Timeline: historial de syncs
  - Logs: detalle de cada sync
```

---

## ⚙️ Customización por Ambiente

### Agregar ambiente (ej: producción)

```bash
# 1. Crear carpeta
mkdir -p overlays/prod

# 2. Crear overlays/prod/kustomization.yml
cat > overlays/prod/kustomization.yml << 'EOF'
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
  - ../../base

namespace: escuela-prod

namePrefix: prod-

replicas:
  - name: api-gateway
    count: 3          # Más replicas en prod
  - name: eureka-server
    count: 3
  # ... resto de MS con 3 replicas

commonLabels:
  environment: production
EOF

# 3. Actualizar Argo CD Application
# source.path: kubernetes/overlays/prod
```

---

## 🧪 Validación

```bash
# Verificar sintaxis
kustomize build overlays/dev > /tmp/manifests.yml
kubeval /tmp/manifests.yml

# Dry-run (no aplicar, solo verificar)
kubectl apply -k overlays/dev --dry-run=client

# Ver qué cambios harían
kubectl apply -k overlays/dev --dry-run=client -o yaml
```

---

## 🔄 Updates y Cambios

### Cambiar imagen de un MS

```yaml
# kubernetes/base/ms-auth.yml
spec:
  template:
    spec:
      containers:
      - name: ms-auth
        image: escuela/ms-auth:v2.0.0    ← Cambiar versión
        imagePullPolicy: Always
```

```bash
git add kubernetes/base/ms-auth.yml
git commit -m "Sprint 12 (Actualizar MS-Auth a v2.0.0)"
git push

# Argo CD sincroniza automáticamente
# Nuevos pods con v2.0.0
```

### Cambiar replica count

```yaml
# kubernetes/overlays/dev/kustomization.yml
replicas:
  - name: api-gateway
    count: 3          ← Cambiar
```

```bash
git add kubernetes/overlays/dev/kustomization.yml
git commit -m "Sprint 12 (Aumentar replicas API Gateway a 3)"
git push

# Argo CD sincroniza: 3 pods de API Gateway
```

### Agregar variable de entorno

```yaml
# kubernetes/base/ms-auth.yml
env:
  - name: LOG_LEVEL
    value: "DEBUG"    ← NUEVA
```

```bash
git add kubernetes/base/ms-auth.yml
git commit -m "Sprint 12 (Agregar LOG_LEVEL)"
git push

# Argo CD recrea pods con nueva env var
```

---

## 🐛 Troubleshooting

### Pod en CrashLoopBackOff

```bash
# Ver logs
kubectl logs -f pod/dev-ms-auth-xxx -n escuela

# Común: Startup timeout
# Solución: aumentar initialDelaySeconds en livenessProbe
kubectl describe pod dev-ms-auth-xxx -n escuela
```

### Database connection failed

```bash
# Verificar que PostgreSQL está Running
kubectl get pod dev-postgres-xxx -n escuela

# Verificar readiness
kubectl describe pod dev-postgres-xxx -n escuela | grep Readiness

# Aumentar timeout si es muy rápido
# kubernetes/base/ms-auth.yml:
# initialDelaySeconds: 180  ← Esperar 3 min antes de health check
```

### Argo CD no sincroniza

```bash
# Verificar Application status
kubectl describe application proyecto-titulacion -n argocd

# Ver logs de Argo CD
kubectl logs -f deployment/argocd-application-controller -n argocd

# Validar manifiestos
kustomize build overlays/dev | kubeval
```

---

## 📚 Referencias

- **Kubernetes Docs:** https://kubernetes.io/docs/
- **Kustomize:** https://kustomize.io/
- **Argo CD:** https://argo-cd.readthedocs.io/
- **Dockerfile Spring:** `infrastructure/docker/Dockerfile.spring`

---

## 📝 Notas

- **Database:** PostgreSQL con 9 schemas (1 por MS + shared)
- **Storage:** EmptyDir (dev) / PersistentVolume (prod)
- **Networking:** Headless Services para MS (DNS directo)
- **Health checks:** Liveness + Readiness en todos los containers
- **Resources:** Requests/Limits definidos (CPU + Memory)

---

**Generado para:** Proyecto de Titulación UDLA  
**Fecha:** 2026-06-05  
**Estado:** ✅ Listo para producción

