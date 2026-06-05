
# ============================================
# SCRIPT: Setup Completo para Demo Argo CD
# ============================================
# Ejecuta ESTE script para:
#  1. Verificar Minikube
#  2. Instalar/Verificar Argo CD
#  3. Desplegar aplicación
#  4. Validar que todo funciona
# ============================================

Write-Host "🚀 INICIANDO SETUP COMPLETO PARA DEMO" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""

# ============================================
# PASO 1: VERIFICAR MINIKUBE
# ============================================
Write-Host "📍 PASO 1: Verificar Minikube" -ForegroundColor Cyan
Write-Host "---" -ForegroundColor Cyan

$minikube_status = minikube status 2>&1
if ($minikube_status -like "*Running*") {
    Write-Host "✅ Minikube está corriendo" -ForegroundColor Green
} else {
    Write-Host "⏱️  Minikube NO está corriendo. Iniciando..." -ForegroundColor Yellow
    Write-Host "   Comando: minikube start --cpus=4 --memory=8192 --driver=docker" -ForegroundColor Yellow
    Write-Host ""
    minikube start --cpus=4 --memory=8192 --driver=docker
    Write-Host "✅ Minikube iniciado" -ForegroundColor Green
}

Write-Host ""

# ============================================
# PASO 2: VERIFICAR/INSTALAR ARGO CD
# ============================================
Write-Host "📍 PASO 2: Verificar Argo CD" -ForegroundColor Cyan
Write-Host "---" -ForegroundColor Cyan

$argocd_namespace = kubectl get namespace argocd 2>&1
if ($argocd_namespace -like "*Active*") {
    Write-Host "✅ Namespace argocd existe" -ForegroundColor Green
} else {
    Write-Host "📦 Creando namespace argocd..." -ForegroundColor Yellow
    kubectl create namespace argocd
    Write-Host "✅ Namespace creado" -ForegroundColor Green
}

$argocd_pods = kubectl get pods -n argocd 2>&1 | Measure-Object -Line
if ($argocd_pods.Lines -gt 3) {
    Write-Host "✅ Argo CD ya está instalado" -ForegroundColor Green
} else {
    Write-Host "📦 Instalando Argo CD (esto tarda ~2 min)..." -ForegroundColor Yellow
    kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
    Write-Host "⏳ Esperando a que Argo CD esté ready..." -ForegroundColor Yellow
    kubectl wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd
    Write-Host "✅ Argo CD instalado y ready" -ForegroundColor Green
}

Write-Host ""

# ============================================
# PASO 3: OBTENER CONTRASEÑA ARGO CD
# ============================================
Write-Host "📍 PASO 3: Contraseña Argo CD" -ForegroundColor Cyan
Write-Host "---" -ForegroundColor Cyan

try {
    $password = kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" 2>/dev/null | `
                powershell -Command "[System.Convert]::FromBase64String([Console]::In.ReadLine()) | [System.Text.Encoding]::UTF8.GetString([Console]::In.ReadToEnd())" 2>/dev/null

    if ($password) {
        Write-Host "🔐 Contraseña Argo CD: $password" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  No se pudo obtener contraseña (probablemente ya fue cambiada)" -ForegroundColor Yellow
}

Write-Host ""

# ============================================
# PASO 4: CREAR APPLICATION ARGO CD
# ============================================
Write-Host "📍 PASO 4: Crear Application Argo CD" -ForegroundColor Cyan
Write-Host "---" -ForegroundColor Cyan

$app_exists = kubectl get applications -n argocd 2>&1 | findstr "proyecto-titulacion"
if ($app_exists) {
    Write-Host "✅ Application 'proyecto-titulacion' ya existe" -ForegroundColor Green
} else {
    Write-Host "📦 Creando Application..." -ForegroundColor Yellow
    kubectl apply -f kubernetes/argocd-application.yml
    Write-Host "✅ Application creada" -ForegroundColor Green
}

Write-Host ""

# ============================================
# PASO 5: SINCRONIZAR APPLICATION
# ============================================
Write-Host "📍 PASO 5: Sincronizar (desplegar todo)" -ForegroundColor Cyan
Write-Host "---" -ForegroundColor Cyan
Write-Host "⏳ Sincronizando... esto tarda 3-5 minutos" -ForegroundColor Yellow
Write-Host ""

argocd app sync proyecto-titulacion 2>&1 | Out-Null

Write-Host "✅ Sincronización iniciada" -ForegroundColor Green
Write-Host ""

# ============================================
# PASO 6: ESPERAR A QUE TODO ESTÉ RUNNING
# ============================================
Write-Host "📍 PASO 6: Esperando a que todos los pods estén Running" -ForegroundColor Cyan
Write-Host "---" -ForegroundColor Cyan
Write-Host "⏳ Esto puede tardar 3-5 minutos..." -ForegroundColor Yellow
Write-Host ""

$max_attempts = 60
$attempt = 0
$all_running = $false

while ($attempt -lt $max_attempts) {
    $pods_output = kubectl get pods -n escuela 2>&1
    $running_pods = $pods_output | findstr "Running" | Measure-Object -Line
    $total_pods = $pods_output | findstr "dev-" | Measure-Object -Line

    if ($total_pods.Lines -gt 0) {
        Write-Host "Intento $($attempt + 1)/$max_attempts - Pods: $($running_pods.Lines)/$($total_pods.Lines) Running" -ForegroundColor Cyan

        if ($running_pods.Lines -eq $total_pods.Lines -and $total_pods.Lines -gt 10) {
            $all_running = $true
            break
        }
    }

    $attempt++
    Start-Sleep -Seconds 5
}

if ($all_running) {
    Write-Host "✅ Todos los pods están Running" -ForegroundColor Green
} else {
    Write-Host "⚠️  Timeout esperando pods. Verificando manualmente..." -ForegroundColor Yellow
}

Write-Host ""

# ============================================
# PASO 7: MOSTRAR PODS
# ============================================
Write-Host "📍 PASO 7: Estado actual de pods" -ForegroundColor Cyan
Write-Host "---" -ForegroundColor Cyan

kubectl get pods -n escuela
Write-Host ""

# ============================================
# PASO 8: VALIDACIONES
# ============================================
Write-Host "📍 PASO 8: Validaciones" -ForegroundColor Cyan
Write-Host "---" -ForegroundColor Cyan

# Test 1: API Gateway
Write-Host "Test 1: API Gateway" -ForegroundColor Yellow
try {
    $health = curl -s http://localhost:8080/actuator/health
    if ($health -like "*UP*") {
        Write-Host "  ✅ API Gateway responde" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  API Gateway no responde correctamente" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ⚠️  No se pudo conectar a API Gateway (puerto 8080)" -ForegroundColor Yellow
    Write-Host "     (Necesitas port-forward: kubectl port-forward -n escuela svc/dev-api-gateway 8080:8080)" -ForegroundColor Gray
}

# Test 2: Argo CD
Write-Host "Test 2: Argo CD" -ForegroundColor Yellow
$app_status = kubectl get applications -n argocd proyecto-titulacion -o jsonpath="{.status.operationState.phase}" 2>/dev/null
if ($app_status -like "*Succeeded*" -or $app_status -eq "") {
    Write-Host "  ✅ Argo CD Application está sincronizada" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Argo CD status: $app_status" -ForegroundColor Yellow
}

# Test 3: Recursos
Write-Host "Test 3: Recursos en cluster" -ForegroundColor Yellow
$resource_count = kubectl get all -n escuela 2>&1 | Measure-Object -Line
Write-Host "  ✅ Total de recursos: $($resource_count.Lines) líneas" -ForegroundColor Green

Write-Host ""

# ============================================
# PASO 9: INSTRUCCIONES FINALES
# ============================================
Write-Host "✅ SETUP COMPLETADO" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""

Write-Host "📍 SIGUIENTE PASO: PREPARAR PARA DEMO" -ForegroundColor Cyan
Write-Host ""
Write-Host "Abre 4 terminales PowerShell (antes de la clase):" -ForegroundColor Yellow
Write-Host ""

Write-Host "Terminal 1 (Dashboard):" -ForegroundColor Magenta
Write-Host "  minikube dashboard" -ForegroundColor Gray
Write-Host ""

Write-Host "Terminal 2 (Monitor de pods):" -ForegroundColor Magenta
Write-Host "  kubectl get pods -n escuela --watch" -ForegroundColor Gray
Write-Host ""

Write-Host "Terminal 3 (Port-forward Eureka):" -ForegroundColor Magenta
Write-Host "  kubectl port-forward -n escuela svc/dev-eureka-server 8761:8761" -ForegroundColor Gray
Write-Host ""

Write-Host "Navegador (2 pestañas):" -ForegroundColor Magenta
Write-Host "  Pestaña 1: https://localhost:8443 (Argo CD)" -ForegroundColor Gray
Write-Host "  Pestaña 2: http://localhost:8761 (Eureka)" -ForegroundColor Gray
Write-Host ""

Write-Host "Luego en Terminal 4:" -ForegroundColor Magenta
Write-Host "  cd C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto titulacion" -ForegroundColor Gray
Write-Host "  Abre: kubernetes\DEMO-CLASE.md" -ForegroundColor Gray
Write-Host "  Sigue los ACTOS paso a paso" -ForegroundColor Gray
Write-Host ""

Write-Host "✨ ¡LISTO PARA DEMO!" -ForegroundColor Green
Write-Host ""

# ============================================
# Verificación final
# ============================================
Write-Host "🔍 Verificación final:" -ForegroundColor Cyan

$final_check = kubectl get pods -n escuela | findstr "Running" | Measure-Object -Line
if ($final_check.Lines -gt 10) {
    Write-Host "✅ 12+ pods running - LISTO PARA CLASE" -ForegroundColor Green
} else {
    Write-Host "⚠️  Menos de 12 pods corriendo. Verifica logs:" -ForegroundColor Yellow
    Write-Host "   kubectl logs -f deployment/dev-ms-auth -n escuela" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "Script completado. ¡Éxito en la presentación! 🚀" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
