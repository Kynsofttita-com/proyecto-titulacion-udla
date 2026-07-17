# Setup - ArgoCD y Jenkins

Guía de instalación y configuración de ArgoCD y Jenkins para el proyecto.

## Opciones de Instalación

### Opción 1: Desarrollo Local (Docker Compose)

```bash
cd .deployment
docker-compose up -d
```

**Acceso:**
- Jenkins: http://localhost:8080 (usuario: admin, contraseña: see logs)
- PostgreSQL: localhost:5432
- RabbitMQ: http://localhost:15672 (guest/guest)

### Opción 2: Kubernetes + ArgoCD (Recomendado para Producción)

#### 1. Instalación de ArgoCD

```bash
# Ejecutar script de instalación
cd .deployment/argocd
chmod +x install-argocd.sh
./install-argocd.sh

# Port-forward a ArgoCD
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Acceder a ArgoCD
# URL: https://localhost:8080
# Usuario: admin
# Contraseña: (obtener con el script)
```

#### 2. Crear Namespace y Recursos

```bash
cd .deployment/kubernetes

# Crear namespace
kubectl apply -f namespace.yaml

# Crear ConfigMaps y Secrets
kubectl apply -f configmaps.yaml

# Crear Services
kubectl apply -f services.yaml
```

#### 3. Crear la Aplicación en ArgoCD

```bash
cd .deployment/argocd

# Aplicar la configuración de la aplicación
kubectl apply -f argocd-application.yaml

# Verificar estado
kubectl get application -n proyecto-titulacion
argocd app get proyecto-titulacion
```

### Opción 3: Jenkins (Alternativa a GitHub Actions)

#### 1. Construcción de Imagen Docker

```bash
cd .deployment/jenkins
docker build -t jenkins-proyecto .
```

#### 2. Ejecución Local

```bash
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  jenkins-proyecto
```

#### 3. Configuración Inicial

1. Abrir http://localhost:8080
2. Obtener contraseña: `docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword`
3. Instalar plugins recomendados
4. Crear usuarios
5. Configurar credenciales en Jenkins:
   - GitHub token
   - Docker registry credentials
   - SonarQube token
   - ArgoCD token
   - Slack webhook

#### 4. Crear Pipeline en Jenkins

1. Nueva tarea → Pipeline
2. Nombre: `proyecto-titulacion`
3. Definición: Pipeline script from SCM
4. SCM: Git
5. URL: https://github.com/Kynsofttita-com/proyecto-titulacion-udla
6. Rama: `*/main`
7. Script path: `Jenkinsfile`

## Variables de Entorno y Credenciales

### Jenkins Credentials

| Credential ID | Tipo | Descripción |
|--------------|------|-------------|
| `github-token` | Secret text | GitHub personal access token |
| `docker-registry` | Username/Password | Docker registry credentials |
| `sonar-host-url` | Secret text | SonarQube server URL |
| `sonar-token` | Secret text | SonarQube auth token |
| `argocd-server` | Secret text | ArgoCD server URL |
| `argocd-token` | Secret text | ArgoCD auth token |
| `slack-webhook` | Secret text | Slack webhook URL |

### Crear Credenciales en Jenkins

```groovy
// En Jenkins → Manage Jenkins → Manage Credentials
// Usar la interfaz web para agregar credenciales
```

## GitHub Webhook Integración

### Para Jenkins

1. GitHub Repo → Settings → Webhooks
2. Agregar webhook:
   - Payload URL: `http://jenkins.example.com:8080/github-webhook/`
   - Content type: `application/json`
   - Eventos: Push, Pull Request
   - Active: ✓

### Para ArgoCD

```bash
# ArgoCD ya tiene integración nativa con GitHub
# Solo requiere SSH key o token
argocd repo add https://github.com/Kynsofttita-com/proyecto-titulacion-udla \
  --username git \
  --password <github-token>
```

## Monitoreo y Logs

### Jenkins

```bash
# Logs en tiempo real
docker logs -f jenkins

# Acceder a Jenkins
http://localhost:8080
```

### ArgoCD

```bash
# Logs de ArgoCD
kubectl logs -f deployment/argocd-server -n argocd

# Estado de aplicaciones
kubectl get applications -n proyecto-titulacion

# Detalles de una app
argocd app get proyecto-titulacion
```

### Kubernetes

```bash
# Ver pods
kubectl get pods -n proyecto-titulacion

# Logs de un pod
kubectl logs -f <pod-name> -n proyecto-titulacion

# Describir recurso
kubectl describe pod <pod-name> -n proyecto-titulacion
```

## Troubleshooting

### Jenkins no inicia

```bash
# Revisar logs
docker logs jenkins

# Verificar permisos
docker exec jenkins chown -R jenkins:jenkins /var/jenkins_home

# Reiniciar
docker restart jenkins
```

### ArgoCD no sincroniza

```bash
# Verificar estado
argocd app get proyecto-titulacion --refresh

# Forzar sincronización
argocd app sync proyecto-titulacion

# Ver errores
kubectl describe application proyecto-titulacion -n proyecto-titulacion
```

### Pods en pending

```bash
# Verificar recursos
kubectl describe node

# Ver eventos
kubectl get events -n proyecto-titulacion

# Revisar logs de pod
kubectl logs <pod-name> -n proyecto-titulacion
```

## Referencias

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [ArgoCD Documentation](https://argoproj.github.io/argo-cd/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
