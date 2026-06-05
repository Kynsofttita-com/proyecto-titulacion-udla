# ============================================
# AUTO-SETUP para DEMO de Argo CD en Clase
# Proyecto: Escuela de Conducción
# Grupo: Software Processes - UDLA
#
# USO:
#   powershell -ExecutionPolicy Bypass -File START-DEMO.ps1
# ============================================

$ErrorActionPreference = "Continue"

# Colors
function Write-Header { Write-Host "`n========== $args ==========`n" -ForegroundColor Blue }
function Write-Success { Write-Host "✅ $args" -ForegroundColor Green }
function Write-Error-Custom { Write-Host "❌ $args" -ForegroundColor Red }
function Write-Warning-Custom { Write-Host "⚠️  $args" -ForegroundColor Yellow }
function Write-Info { Write-Host "ℹ️  $args" -ForegroundColor Cyan }

Write-Header "🚀 INICIANDO SETUP PARA DEMO DE ARGO CD"

# ============================================
# STEP 1: Verificar requisitos
# ============================================
Write-Header "STEP 1: Verificando requisitos (30 seg)"

if (-not (Get-Command minikube -ErrorAction SilentlyContinue)) {
    Write-Error-Custom "Minikube no está instalado"
    exit 1
}
Write-Success "Minikube OK"

if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Error-Custom "kubectl no está instalado"
    exit 1
}
Write-Success "kubectl OK"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error-Custom "Docker no está instalado"
    exit 1
}
Write-Success "Docker OK"

# ============================================
# STEP 2: Iniciar Minikube
# ============================================
Write-Header "STEP 2: Iniciando Minikube (2-3 minutos)"

$status = minikube status 2>&1
if ($LASTEXITCODE -eq 0 -and $status -match "Running") {
    Write-Warning-Custom "Minikube ya está corriendo, continuando..."
} else {
    Write-Info "Arrancando Minikube..."
    minikube start --cpus=4 --memory=8192 --driver=docker
    Write-Success "Minikube iniciado"
}

# ============================================
# STEP 3: Instalar Argo CD
# ============================================
Write-Header "STEP 3: Instalando Argo CD (2-3 minutos)"

Write-Info "Creando namespace argocd..."
kubectl create namespace argocd 2>&1 | Out-Null

Write-Info "Aplicando manifiestos de Argo CD..."
$argocdUrl = "https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml"
kubectl apply -n argocd -f $argocdUrl

Write-Info "Esperando a que Argo CD esté listo..."
$ready = $false
$tries = 0
while (-not $ready -and $tries -lt 30) {
    $pod = kubectl get pod -l "app.kubernetes.io/name=argocd-server" -n argocd -o jsonpath='{.items[0].status.conditions[?(@.type=="Ready")].status}' 2>&1
    if ($pod -eq "True") {
        $ready = $true
        Write-Success "Argo CD está listo"
    } else {
        Write-Info "Esperando... ($tries/30)"
        Start-Sleep -Seconds 2
        $tries++
    }
}

if (-not $ready) {
    Write-Warning-Custom "Timeout esperando Argo CD, pero continuando..."
}

# ============================================
# STEP 4: Build de imágenes Docker
# ============================================
Write-Header "STEP 4: Build de imágenes Docker (5-10 minutos)"
Write-Info "IMPORTANTE: El build toma tiempo, sé paciente..."

# Configurar Docker para Minikube
Write-Info "Configurando Docker para Minikube..."
& minikube -p minikube docker-env | Out-Host
$env:DOCKER_HOST = "unix:///var/run/docker.sock"

# Cambiar directorio al proyecto
$projectDir = Split-Path -Parent $PSScriptRoot

Write-Info "Navegando a: $projectDir"
Push-Location $projectDir

# Build api-gateway
Write-Info "🔨 Construyendo api-gateway..."
try {
    Push-Location "backend/api-gateway"
    mvn clean package -DskipTests -q
    docker build -t api-gateway:latest .
    Pop-Location
    Write-Success "api-gateway construido"
} catch {
    Write-Warning-Custom "Error en api-gateway, continuando..."
    Pop-Location
}

# Build ms-auth
Write-Info "🔨 Construyendo ms-auth..."
try {
    Push-Location "backend/ms-auth"
    mvn clean package -DskipTests -q
    docker build -t ms-auth:latest .
    Pop-Location
    Write-Success "ms-auth construido"
} catch {
    Write-Warning-Custom "Error en ms-auth, continuando..."
    Pop-Location
}

# Build ms-estudiantes
Write-Info "🔨 Construyendo ms-estudiantes..."
try {
    Push-Location "backend/ms-estudiantes"
    mvn clean package -DskipTests -q
    docker build -t ms-estudiantes:latest .
    Pop-Location
    Write-Success "ms-estudiantes construido"
} catch {
    Write-Warning-Custom "Error en ms-estudiantes, continuando..."
    Pop-Location
}

# Build frontend
Write-Info "🔨 Construyendo frontend..."
try {
    Push-Location "frontend"
    npm install -q
    npm run build -q
    docker build -t frontend:latest .
    Pop-Location
    Write-Success "frontend construido"
} catch {
    Write-Warning-Custom "Error en frontend, continuando..."
    Pop-Location
}

# Eureka (usar imagen oficial)
Write-Info "🔨 Descargando eureka-server..."
try {
    docker pull springcloud/eureka:latest
    docker tag springcloud/eureka:latest eureka-server:latest
    Write-Success "eureka-server listo"
} catch {
    Write-Warning-Custom "Error con eureka, continuando..."
}

Pop-Location

# ============================================
# STEP 5: Crear namespace y aplicación
# ============================================
Write-Header "STEP 5: Creando namespace de aplicación"

Write-Info "Aplicando manifiestos..."
kubectl apply -f kubernetes/argocd/00-namespace.yaml
kubectl apply -f kubernetes/argocd/argo-app.yaml

Write-Success "Namespace y application creados"

# ============================================
# STEP 6: Esperar a que todo esté listo
# ============================================
Write-Header "STEP 6: Esperando a que todo esté listo (puede tomar 2-3 minutos)"

Write-Info "Esperando pods de escuela-conduccion..."
$tries = 0
while ($tries -lt 30) {
    $pods = kubectl get pods -n escuela-conduccion -o jsonpath='{.items[*].status.phase}' 2>&1
    if ($pods -match "Running" -and -not ($pods -match "Pending")) {
        Write-Success "Pods están corriendo!"
        break
    }
    Write-Info "Esperando... ($tries/30)"
    kubectl get pods -n escuela-conduccion 2>&1 | Select-Object -Last 3
    Start-Sleep -Seconds 2
    $tries++
}

# ============================================
# STEP 7: Mostrar información de acceso
# ============================================
Write-Header "🎉 SETUP COMPLETADO - INFORMACIÓN DE ACCESO"

Write-Success "Argo CD UI:"
Write-Host "  Ejecuta en una terminal: kubectl port-forward svc/argocd-server -n argocd 8443:443"
Write-Host "  URL: https://localhost:8443"
Write-Host "  Usuario: admin"

# Intentar obtener contraseña
try {
    $secret = kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" 2>&1
    $password = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($secret))
    Write-Host "  Contraseña: $password"
} catch {
    Write-Warning-Custom "No se pudo obtener contraseña (puede ya estar configurada)"
}

Write-Success "`nMinikube Dashboard:"
Write-Host "  Ejecuta: minikube dashboard"

Write-Success "`nFrontend:"
Write-Host "  Ejecuta: kubectl port-forward svc/frontend -n escuela-conduccion 3000:80"
Write-Host "  URL: http://localhost:3000"

Write-Success "`nAPI Gateway:"
Write-Host "  Ejecuta: kubectl port-forward svc/api-gateway -n escuela-conduccion 8080:8080"
Write-Host "  URL: http://localhost:8080/actuator/health"

# ============================================
# STEP 8: Mostrar estado
# ============================================
Write-Header "ESTADO ACTUAL"

Write-Info "Pods de Argo CD:"
kubectl get pods -n argocd

Write-Info "`nPods de la aplicación:"
kubectl get pods -n escuela-conduccion

Write-Info "`nServices:"
kubectl get svc -n escuela-conduccion

# ============================================
# INSTRUCCIONES FINALES
# ============================================
Write-Header "PRÓXIMOS PASOS PARA LA DEMO"

Write-Info "1. Abre 3 terminales PowerShell:"
Write-Host "`n   Terminal 1 (para dashboard):"
Write-Host "   minikube dashboard`n"

Write-Host "   Terminal 2 (para Argo CD):"
Write-Host "   kubectl port-forward svc/argocd-server -n argocd 8443:443`n"

Write-Host "   Terminal 3 (para comandos):"
Write-Host "   cd kubernetes"
Write-Host "   cat DEMO-CLASE.md`n"

Write-Info "2. Abre navegador con 2 pestañas:"
Write-Host "`n   Pestaña 1: http://localhost:8080 (Minikube Dashboard)`n"
Write-Host "   Pestaña 2: https://localhost:8443 (Argo CD)`n"

Write-Info "3. Lee el guion: kubernetes/DEMO-CLASE.md"
Write-Host "`n   Te dice paso a paso qué decir y qué hacer en vivo`n"

Write-Success "`n🎬 ¡LISTO PARA DEMO! - Duración: 20-25 minutos`n"

Write-Host "Para la demostración, sigue: kubernetes/DEMO-CLASE.md" -ForegroundColor Yellow
Write-Host "Acto 5 (GitOps demo) es el momento estrella: cambio en Git → sincronización automática" -ForegroundColor Yellow
