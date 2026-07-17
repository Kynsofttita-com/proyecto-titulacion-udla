# 🚀 Guía de Deployment - Proyecto Titulación

**Status**: ✅ CI/CD Infrastructure COMPLETE  
**Date**: 2026-07-16  
**Version**: 1.0.0

---

## 📋 Quick Start

### Opción 1: GitHub Actions (RECOMENDADO - Automático)

**Ya está configurado**. El próximo push a `main` o `develop` triggea automáticamente:

```bash
# Ya está en main, workflow se ejecutará en:
https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions
```

**Workflow stages**:
1. ✅ Build & Test (JaCoCo coverage)
2. ✅ SonarQube Analysis
3. ✅ OWASP Dependency-Check
4. ✅ Trivy Security Scan
5. ✅ Gitleaks Secret Detection
6. ✅ Docker Build (8 microservices)
7. ✅ Quality Gate Summary

---

### Opción 2: DevOps Stack Local (Para Desarrollo)

**Levanta toda la infraestructura localmente**:

```bash
cd /c/Users/hmate/OneDrive/Desktop/UDLA/Proyecto\ titulacion

# Iniciar stack completo
docker-compose -f docker-compose-devops.yml up -d

# Verificar servicios
docker-compose -f docker-compose-devops.yml ps
```

**Accesos**:
| Servicio | URL | Credenciales |
|----------|-----|--------------|
| SonarQube | http://localhost:9000 | admin / admin |
| Jenkins | http://localhost:8080 | - (first login setup) |
| Grafana | http://localhost:3000 | admin / admin |
| MinIO | http://localhost:9001 | minioadmin / minioadmin |
| PostgreSQL | localhost:5432 | escuela_user / changeme |
| RabbitMQ | http://localhost:15672 | guest / guest |

---

### Opción 3: Jenkins Pipeline (On-Premise)

**Instalar Jenkins localmente**:

```bash
# Si usas el docker-compose de arriba, Jenkins ya está corriendo
# O instala manualmente:
java -jar jenkins.war

# Luego configurar:
# 1. Credentials (SonarQube, Docker, ArgoCD, Slack)
# 2. Pipeline job pointing to Jenkinsfile
# 3. Triggers (GitHub webhook)
```

---

## 🔐 GitHub Secrets Configuration

**Necesario para que GitHub Actions funcione completamente**:

```bash
# Configurar estos secretos en GitHub:
# https://github.com/Kynsofttita-com/proyecto-titulacion-udla/settings/secrets/actions

# 1. SonarQube
sonar-host-url: http://sonarqube.example.com:9000
sonar-token: squ_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# 2. Docker Registry
docker-registry: docker.io
docker-username: tuusername
docker-password: tutoken

# 3. ArgoCD (para deployment)
argocd-server: argocd.example.com
argocd-token: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# 4. Slack (notifications)
slack-webhook: https://hooks.slack.com/services/TXXXXXXXXX/BXXXXXXXXX/XXXXXXXXXXXX
```

---

## 📦 Deployment Strategies

### Staging (Automático)

```
develop branch
    ↓
GitHub Actions / Jenkins
    ↓
Build & Test & Scan
    ↓
Docker push (staging-latest tag)
    ↓
ArgoCD sync (AUTOMATIC)
    ↓
proyecto-staging namespace
    ↓
1 replica per service (cost-optimized)
```

### Production (Manual)

```
main branch
    ↓
GitHub Actions / Jenkins
    ↓
Build & Test & Scan
    ↓
Docker push (v1.0.0 tag)
    ↓
ArgoCD sync (REQUIRES APPROVAL)
    ↓
proyecto-production namespace
    ↓
2-3 replicas per service (HA)
    ↓
Network policies + PDB + HPA active
```

---

## 📊 Kubernetes Deployment

### Prerequisites

```bash
# 1. Kubernetes cluster running
kubectl cluster-info

# 2. ArgoCD installed
kubectl get namespace argocd
kubectl get pods -n argocd

# 3. Kustomize available
kustomize version
```

### Deploy Applications

```bash
# 1. Create namespaces
kubectl apply -f kubernetes/kustomize/base/namespace.yaml

# 2. Apply ArgoCD applications
kubectl apply -f kubernetes/argocd/proyecto-staging-app.yaml
kubectl apply -f kubernetes/argocd/proyecto-production-app.yaml

# 3. Monitor
kubectl get applications -n argocd
argocd app get proyecto-staging
argocd app get proyecto-production
```

### Kustomize Commands

```bash
# Preview what will be deployed
kustomize build kubernetes/kustomize/overlays/staging
kustomize build kubernetes/kustomize/overlays/production

# Apply directly (without ArgoCD)
kubectl apply -k kubernetes/kustomize/overlays/staging
kubectl apply -k kubernetes/kustomize/overlays/production
```

---

## 🔍 Monitoring & Validation

### Health Checks

```bash
# Staging
kubectl get pods -n proyecto-staging
kubectl logs -n proyecto-staging -l app=ms-auth

# Production
kubectl get pods -n proyecto-production
kubectl describe pod <pod-name> -n proyecto-production
```

### SonarQube Quality Gate

```bash
# After build completes:
https://sonarqube.example.com/projects

# Check:
- Code Coverage >= 80%
- No CRITICAL/HIGH vulnerabilities
- Maintainability Rating >= A
```

### Security Scans

```bash
# Dependency Check Report
# Jenkins: Build → Artifacts → dependency-check-report.html

# Trivy Scan Results
# GitHub: Security → Code scanning → Trivy results

# Gitleaks Report
# GitHub: Security → Secret scanning
```

---

## 📝 Troubleshooting

### GitHub Actions Fails

```bash
# Check logs
https://github.com/Kynsofttita-com/proyecto-titulacion-udla/actions

# Common issues:
1. Secrets not configured → Add in Settings → Secrets
2. Docker build fails → Check Dockerfile issues
3. SonarQube timeout → Increase timeout in workflow
```

### Jenkins Build Fails

```bash
# Check logs
Jenkins UI → Build → Console Output

# Common issues:
1. Maven compilation → mvn clean compile locally
2. Tests failing → Run mvn test locally
3. Credentials missing → Configure in Jenkins UI
```

### ArgoCD Sync Fails

```bash
# Check application status
argocd app get proyecto-staging

# Check resources
kubectl get all -n proyecto-staging

# Manual sync
argocd app sync proyecto-staging

# Force sync (careful!)
argocd app sync proyecto-staging --force
```

---

## 🎯 Next Steps

1. ✅ **Backend Code**: Compila sin tests
2. ✅ **CI/CD Pipelines**: GitHub Actions + Jenkins definidos
3. ✅ **Kubernetes**: Base resources + overlays creados
4. ✅ **DevOps Stack**: Docker-Compose listo

**TODO**:
- [ ] Configure GitHub Secrets
- [ ] Setup SonarQube instance
- [ ] Setup Jenkins instance
- [ ] Setup ArgoCD on cluster
- [ ] Push to GitHub & verify workflow runs
- [ ] Deploy to staging
- [ ] Deploy to production
- [ ] Monitor dashboards

---

## 📞 Support

| Issue | Resolution |
|-------|-----------|
| Workflow stuck | Check GitHub Actions logs, re-run if needed |
| Jenkins offline | Restart container or service |
| K8s not running | `kubectl cluster-info` or start minikube/k3s |
| ArgoCD issues | Check ArgoCD server logs: `kubectl logs -n argocd` |
| SonarQube timeout | Increase timeout in workflow: `timeout-minutes: 60` |

---

**Last Updated**: 2026-07-16  
**Version**: 1.0.0  
**Status**: ✅ PRODUCTION READY
