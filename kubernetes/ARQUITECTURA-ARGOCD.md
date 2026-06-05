# 🏗️ Arquitectura: Argo CD en Kubernetes

## 📐 Diagrama de la solución

```
┌─────────────────────────────────────────────────────────────┐
│                     GitHub Repository                        │
│  ├── backend/                                               │
│  │   ├── ms-auth/                                           │
│  │   ├── ms-estudiantes/                                    │
│  │   └── api-gateway/                                       │
│  ├── frontend/                                              │
│  └── kubernetes/argocd/  ← SOURCE OF TRUTH (GitOps)          │
│      ├── infrastructure/                                    │
│      │   ├── 01-postgresql.yaml                            │
│      │   └── 02-eureka.yaml                                │
│      ├── apps/                                             │
│      │   ├── 01-api-gateway.yaml                           │
│      │   ├── 02-ms-auth.yaml                               │
│      │   ├── 03-ms-estudiantes.yaml                        │
│      │   └── 04-frontend.yaml                              │
│      ├── 00-namespace.yaml                                 │
│      └── argo-app.yaml                                     │
└─────────────────────────────────────────────────────────────┘
                           ↓ (poll/webhook)
┌─────────────────────────────────────────────────────────────┐
│         Argo CD (argocd namespace)                           │
│  ├── argocd-server (UI + API)                              │
│  ├── argocd-repo-server (git repo sync)                    │
│  ├── argocd-application-controller (reconciliation)        │
│  └── argocd-dex-server (OIDC)                              │
└─────────────────────────────────────────────────────────────┘
                           ↓ (apply manifests)
┌─────────────────────────────────────────────────────────────┐
│        Minikube Kubernetes Cluster                           │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │  escuela-conduccion namespace                      │   │
│  │                                                    │   │
│  │  Frontend Pod → Service (NodePort:30000)          │   │
│  │  API Gateway Pod → Service (NodePort:30080)       │   │
│  │  MS-Auth Pod → Service (ClusterIP:8081)           │   │
│  │  MS-Estudiantes Pod → Service (ClusterIP:8082)    │   │
│  │  PostgreSQL Pod → Service (ClusterIP:5432)        │   │
│  │  Eureka Pod → Service (ClusterIP:8761)            │   │
│  │                                                    │   │
│  └────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 🔄 Flujo GitOps

```
1. Developer edita manifiestos en Git
   ├── Cambio: kubernetes/argocd/apps/01-api-gateway.yaml
   │   └── replicas: 1 → replicas: 2
   └── git commit + git push

2. Argo CD detecta cambio (polling o webhook)
   ├── Argo repo-server descarga cambios
   ├── Application controller compara desired state vs actual state
   └── Estado: OutOfSync (falta sincronizar)

3. Argo CD aplica cambios automáticamente (auto-sync enabled)
   ├── kubectl apply -f manifiestos actualizados
   ├── Kubernetes crea nuevo pod de api-gateway
   └── Estado: Synced (infraestructura = código)

4. Verificación
   ├── kubectl get pods -n escuela-conduccion
   └── Ver 2 pods de api-gateway corriendo
```

## 📊 Componentes de Argo CD

| Componente | Responsabilidad |
|-----------|-----------------|
| **argocd-server** | UI + REST API, autenticación |
| **argocd-repo-server** | Clona y maneja repositorio Git |
| **argocd-application-controller** | Sincronización y reconciliación continua |
| **argocd-dex-server** | OIDC/SSO (opcional) |
| **argocd-notifications** | Alertas (opcional) |

## 🎯 Características principales implementadas

### 1. **Automated Sync**
```yaml
syncPolicy:
  automated:
    prune: true    # Elimina recursos no presentes en Git
    selfHeal: true # Repara cambios manuales en el cluster
```

**¿Qué significa?**
- Si alguien hace `kubectl delete pod api-gateway`, Argo CD lo recrea automáticamente
- Si alguien cambia algo manualmente en Kubernetes, Argo CD revierte a lo que dice Git
- Git siempre gana (source of truth)

### 2. **Application Health Status**
```
Synced         ✅ → Cluster == Git
OutOfSync      ⚠️  → Git tiene cambios sin aplicar
Unknown        ❓  → Argo CD no puede verificar estado
```

### 3. **Dry-run antes de aplicar**
```bash
# Argo CD primero compila manifiestos
argocd app diff escuela-conduccion

# Muestra qué va a cambiar antes de hacerlo
```

## 🔐 Seguridad en GitOps

### Credenciales
- Secrets de Kubernetes encriptadas
- Contraseña de DB en Secret (no en manifiestos públicos)
- JWT para autenticación entre servicios

### Acceso a Git
- Usar SSH keys o tokens con permisos limitados
- (En el repo del proyecto: usar Personal Access Token con permisos solo a `kubernetes/argocd/`)

### RBAC en Kubernetes
```yaml
# Solo Argo CD puede hacer apply en escuela-conduccion
kind: RoleBinding
metadata:
  namespace: escuela-conduccion
roleRef:
  kind: ClusterRole
  name: admin
subjects:
- kind: ServiceAccount
  name: argocd-application-controller
  namespace: argocd
```

## 📈 Ventajas de esta arquitectura

| Ventaja | Explicación |
|---------|------------|
| **Declarativo** | Todo está en YAML en Git, no comandos manuales |
| **Auditable** | Git history = historial completo de cambios |
| **Reproducible** | Mismo código → mismo resultado siempre |
| **Recuperable** | Revertir cambios = revertir commit |
| **Escalable** | Múltiples clusters apuntando al mismo repo |
| **Auto-reparación** | El cluster se auto-corrige si hay desviaciones |

## 🚀 Flujo de deployment para nuevos cambios

```
Cambio de código → Build image → Push a Docker registry
                                        ↓
                                Actualizar manifiestos
                                  (image: tag new)
                                        ↓
                                  Commit + Push Git
                                        ↓
                          Argo CD detecta cambio
                                        ↓
                           Muestra como OutOfSync
                                        ↓
                      Auto-sync (o click manual)
                                        ↓
                            kubectl apply nuevos
                                        ↓
                              Pods se reinician
                               con nueva imagen
                                        ↓
                           Muestra como Synced ✅
```

## 💡 Ejemplo: Cambio en tiempo real

### Escenario: Aumentar réplicas de API Gateway

**Paso 1: Cambio en Git**
```yaml
# kubernetes/argocd/apps/01-api-gateway.yaml
spec:
  replicas: 1  # ANTES
  
# DESPUÉS
spec:
  replicas: 3
```

**Paso 2: Argo CD lo detecta**
```
⚠️ OutOfSync
Desired: 3 replicas
Actual: 1 replica
```

**Paso 3: Auto-sync lo aplica**
```bash
$ kubectl get pods -n escuela-conduccion
NAME                            READY   STATUS    RESTARTS
api-gateway-5f9d8c7b9-abc12    1/1     Running   0
api-gateway-5f9d8c7b9-def45    1/1     Running   0  ← NUEVO
api-gateway-5f9d8c7b9-ghi78    1/1     Running   0  ← NUEVO
```

**Paso 4: Muestra Synced**
```
✅ Synced
Desired: 3 replicas
Actual: 3 replicas
```

## 📋 Estructura de directorios (GitOps)

```
kubernetes/argocd/
├── 00-namespace.yaml              # Namespace base
├── argo-app.yaml                  # Application manifest (punto de entrada)
├── infrastructure/                # Recursos compartidos
│   ├── 01-postgresql.yaml         # Base de datos
│   └── 02-eureka.yaml             # Service discovery
├── apps/                          # Aplicaciones desplegables
│   ├── 01-api-gateway.yaml
│   ├── 02-ms-auth.yaml
│   ├── 03-ms-estudiantes.yaml
│   └── 04-frontend.yaml
└── secrets/                       # Secretos (encriptados en git)
    └── (vacío, se crean manualmente)
```

## 🎓 Conceptos clave para la presentación

1. **Declarativo vs Imperativo**
   - ❌ Imperativo: `kubectl create deployment` (comandos)
   - ✅ Declarativo: YAML files (estado deseado)

2. **Source of Truth (Fuente de Verdad)**
   - Git = único lugar donde vive la verdad
   - Kubernetes debe coincidir con Git siempre
   - Si no coinciden, Argo CD lo arregla

3. **Continuous Deployment**
   - Argo CD watchea cambios continuamente
   - Auto-sync = cambios en Git → cluster en segundos
   - No requiere pipeline manual

4. **Self-Healing**
   - Alguien borra un pod → Argo CD lo recrea
   - Alguien modifica un deployment → Argo CD lo revierte
   - Infraestructura = código

---

**Próximos pasos para el grupo:**
1. Estudiar manifiestos YAML en `kubernetes/argocd/`
2. Practicar cambios y ver sincronización
3. Documentar en detalle cada cambio
4. Preparar demostraciones para la presentación
