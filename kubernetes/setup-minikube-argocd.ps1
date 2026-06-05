# ============================================
# Argo CD + Minikube Setup Script (PowerShell)
# Proyecto: Escuela de Conducción
# Grupo: Software Processes - UDLA
# ============================================
# Ejecutar como: powershell -ExecutionPolicy Bypass -File setup-minikube-argocd.ps1

$ErrorActionPreference = "Stop"

# Functions
function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Blue
    Write-Host $Message -ForegroundColor Blue
    Write-Host "========================================" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

function Write-Warning-Custom {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
}

# Check prerequisites
function Check-Prerequisites {
    Write-Header "Verificando requisitos"

    # Check Minikube
    if (-not (Get-Command minikube -ErrorAction SilentlyContinue)) {
        Write-Error-Custom "Minikube no está instalado"
        Write-Info "Descargar desde: https://minikube.sigs.k8s.io/"
        exit 1
    }
    Write-Success "Minikube encontrado"

    # Check kubectl
    if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
        Write-Error-Custom "kubectl no está instalado"
        exit 1
    }
    Write-Success "kubectl encontrado"

    # Check Docker
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Error-Custom "Docker no está instalado"
        exit 1
    }
    Write-Success "Docker encontrado"

    Write-Host ""
}

# Start Minikube
function Start-Minikube-Local {
    Write-Header "Iniciando Minikube"

    $status = minikube status 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Warning-Custom "Minikube ya está corriendo"
    } else {
        Write-Info "Arrancando Minikube (esto puede tomar 1-2 minutos)..."
        minikube start --cpus=4 --memory=8192 --driver=hyperv
        Write-Success "Minikube iniciado"
    }

    Write-Host ""
}

# Install Argo CD
function Install-Argo-CD {
    Write-Header "Instalando Argo CD"

    Write-Info "Creando namespace argocd..."
    kubectl create namespace argocd 2>&1 | Out-Null
    Write-Warning-Custom "Namespace argocd puede ya existir (es OK)"

    Write-Info "Aplicando manifiestos de Argo CD..."
    $argocdUrl = "https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml"
    kubectl apply -n argocd -f $argocdUrl

    Write-Info "Esperando a que los pods de Argo CD estén listos (esto puede tomar 2-3 minutos)..."
    $tries = 0
    while ($tries -lt 30) {
        $ready = kubectl get pod -l "app.kubernetes.io/name=argocd-server" -n argocd -o jsonpath='{.items[0].status.conditions[?(@.type=="Ready")].status}' 2>&1
        if ($ready -eq "True") {
            Write-Success "Argo CD instalado"
            break
        }
        Write-Info "Esperando... ($tries/30)"
        Start-Sleep -Seconds 10
        $tries++
    }

    if ($tries -eq 30) {
        Write-Warning-Custom "Timeout esperando argocd-server, pero continuando..."
    }

    Write-Host ""
}

# Get Argo CD password
function Get-Argo-CD-Password {
    Write-Info "Obteniendo contraseña inicial de Argo CD..."

    try {
        $secret = kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" 2>&1
        if ($secret) {
            # Convert from base64
            $password = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($secret))
            Write-Success "Contraseña: $password"
            Write-Info "Usuario: admin"
        } else {
            Write-Warning-Custom "No se pudo obtener la contraseña"
        }
    } catch {
        Write-Warning-Custom "Error obteniendo contraseña: $_"
        Write-Info "Ejecutar manualmente:"
        Write-Info "kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath '{.data.password}' | base64 -d"
    }

    Write-Host ""
}

# Create namespace
function Create-App-Namespace {
    Write-Header "Creando namespace de la aplicación"

    kubectl apply -f kubernetes/argocd/00-namespace.yaml
    Write-Success "Namespace 'escuela-conduccion' creado"

    Write-Host ""
}

# Verify deployment
function Verify-Deployment {
    Write-Header "Verificando despliegue"

    Write-Info "Pods de Argo CD:"
    kubectl get pods -n argocd
    Write-Host ""

    Write-Info "Pods de la aplicación:"
    kubectl get pods -n escuela-conduccion -ErrorAction SilentlyContinue | Out-Host
    if ($LASTEXITCODE -ne 0) {
        Write-Info "Esperando sincronización..."
    }
    Write-Host ""

    Write-Info "Servicios:"
    kubectl get svc -n escuela-conduccion -ErrorAction SilentlyContinue | Out-Host
    if ($LASTEXITCODE -ne 0) {
        Write-Info "Esperando servicios..."
    }
    Write-Host ""
}

# Print next steps
function Print-Next-Steps {
    Write-Header "Próximos pasos"

    Write-Host ""
    Write-Info "1. Acceder a Argo CD:"
    Write-Host "   kubectl port-forward svc/argocd-server -n argocd 8443:443"
    Write-Host "   Abrir: https://localhost:8443"
    Write-Host ""

    Write-Info "2. Acceder a la aplicación:"
    Write-Host "   kubectl port-forward svc/frontend -n escuela-conduccion 3000:80"
    Write-Host "   Abrir: http://localhost:3000"
    Write-Host ""

    Write-Info "3. Ver logs de un servicio:"
    Write-Host "   kubectl logs <pod-name> -n escuela-conduccion -f"
    Write-Host ""

    Write-Info "4. Demostrar GitOps:"
    Write-Host "   - Editar kubernetes/argocd/apps/01-api-gateway.yaml (cambiar replicas)"
    Write-Host "   - Hacer git commit y push"
    Write-Host "   - Ver cambio en Argo CD UI (OutOfSync → Synced)"
    Write-Host ""

    Write-Info "5. Documentación:"
    Write-Host "   - Ver: kubernetes/README.md"
    Write-Host "   - Setup detallado: kubernetes/SETUP-ARGOCD.md"
    Write-Host "   - Arquitectura: kubernetes/ARQUITECTURA-ARGOCD.md"
    Write-Host "   - Evidencia: kubernetes/EVIDENCIA-PRESENTACION.md"
    Write-Host ""
}

# Main execution
function Main {
    Clear-Host
    Write-Header "🚀 Argo CD + Kubernetes Setup (PowerShell)"
    Write-Host ""

    Check-Prerequisites
    Start-Minikube-Local
    Install-Argo-CD
    Get-Argo-CD-Password
    Create-App-Namespace

    # Ask about Docker images
    Write-Host ""
    $response = Read-Host "¿Construir imágenes Docker? (esto toma ~10-15 min) (s/n)"
    if ($response -eq 's' -or $response -eq 'S') {
        Write-Warning-Custom "Docker build es lento en PowerShell. Se recomienda:"
        Write-Info "eval \$(minikube docker-env)"
        Write-Info "docker build -t api-gateway:latest ./backend/api-gateway"
        Write-Info "docker build -t ms-auth:latest ./backend/ms-auth"
        Write-Info "docker build -t ms-estudiantes:latest ./backend/ms-estudiantes"
        Write-Info "docker build -t frontend:latest ./frontend"
        Write-Info "docker pull springcloud/eureka:latest"
    } else {
        Write-Info "Construcción de imágenes skipeada."
        Write-Info "Hazlo manualmente en Git Bash:"
        Write-Host "eval \$(minikube docker-env)"
        Write-Host "docker build -t api-gateway:latest ./backend/api-gateway"
        Write-Host "docker build -t ms-auth:latest ./backend/ms-auth"
        Write-Host "docker build -t ms-estudiantes:latest ./backend/ms-estudiantes"
        Write-Host "docker build -t frontend:latest ./frontend"
        Write-Host "docker pull springcloud/eureka:latest && docker tag springcloud/eureka:latest eureka-server:latest"
    }
    Write-Host ""

    # Create Application
    Write-Host ""
    Write-Warning-Custom "⚠️  IMPORTANTE: Edita kubernetes/argocd/argo-app.yaml"
    Write-Host "    Reemplaza: https://github.com/tu-usuario/tu-repo.git"
    Write-Host "    con tu URL real de repositorio"
    Write-Host ""

    $response = Read-Host "¿Has actualizado argo-app.yaml? (s/n)"
    if ($response -eq 's' -or $response -eq 'S') {
        kubectl apply -f kubernetes/argocd/argo-app.yaml
        Write-Success "Application creada en Argo CD"
    } else {
        Write-Warning-Custom "Skipping. Hazlo manualmente cuando tengas la URL."
    }
    Write-Host ""

    Verify-Deployment
    Print-Next-Steps

    Write-Success "Setup completado! 🎉"
}

# Run main
Main
