#!/bin/bash

# SCRIPT: Build all Docker images for Kubernetes deployment
# Usage: ./build-all-images.sh
# Requiere: Docker + Minikube + Maven

set -e

echo "============================================"
echo "🐳 Building all Docker images for Kubernetes"
echo "============================================"
echo ""

# 1. Verificar que Minikube docker-env está activo
echo "✓ Verificando Minikube docker environment..."
if ! eval $(minikube docker-env) > /dev/null 2>&1; then
  echo "❌ Error: Minikube not running. Start with: minikube start"
  exit 1
fi

# 2. Verificar que estamos en carpeta backend
if [ ! -f "pom.xml" ]; then
  echo "❌ Error: pom.xml not found. Run from backend/ folder"
  echo "   cd backend && ./build-all-images.sh"
  exit 1
fi

# 3. Módulos a compilar
MODULES=(
  "eureka-server:8761"
  "api-gateway:8080"
  "ms-auth:8081"
  "ms-estudiantes:8082"
  "ms-instructores:8083"
  "ms-vehiculos:8084"
  "ms-asignaciones:8085"
  "ms-cobros:8086"
  "ms-reportes:8087"
  "ms-notificaciones:8088"
)

echo "📋 Building ${#MODULES[@]} modules:"
echo ""

# 4. Build cada módulo
BUILD_COUNT=0
FAIL_COUNT=0

for module in "${MODULES[@]}"; do
  IFS=':' read -r MODULE_NAME PORT <<< "$module"

  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "📦 Building: $MODULE_NAME (port: $PORT)"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

  if docker build \
    -t escuela/$MODULE_NAME:latest \
    -f infrastructure/docker/Dockerfile.spring \
    --build-arg MODULE=$MODULE_NAME \
    --build-arg SERVICE_PORT=$PORT \
    . 2>&1; then

    echo "✅ SUCCESS: escuela/$MODULE_NAME:latest"
    ((BUILD_COUNT++))
  else
    echo "❌ FAILED: $MODULE_NAME"
    ((FAIL_COUNT++))
  fi

  echo ""
done

# 5. Resultado final
echo "============================================"
echo "📊 BUILD SUMMARY"
echo "============================================"
echo "✅ Successful:  $BUILD_COUNT"
echo "❌ Failed:     $FAIL_COUNT"
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
  echo "✨ All images built successfully!"
  echo ""
  echo "📋 Built images:"
  docker images | grep escuela | awk '{print "   - " $1 ":" $2 " (" $3 ")"}'
  echo ""
  echo "🚀 Next step: Deploy with Kubernetes"
  echo "   kubectl apply -f kubernetes/"
  exit 0
else
  echo "⚠️  Some builds failed. Check logs above."
  exit 1
fi
