#!/bin/bash

# ============================================
# Argo CD + Minikube Setup Script
# Proyecto: Escuela de Conducción
# Grupo: Software Processes - UDLA
# ============================================

set -e  # Exit on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    print_header "Verificando requisitos"

    if ! command -v minikube &> /dev/null; then
        print_error "Minikube no está instalado"
        echo "Descargar desde: https://minikube.sigs.k8s.io/"
        exit 1
    fi
    print_success "Minikube encontrado"

    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl no está instalado"
        exit 1
    fi
    print_success "kubectl encontrado"

    if ! command -v docker &> /dev/null; then
        print_error "Docker no está instalado"
        exit 1
    fi
    print_success "Docker encontrado"

    echo ""
}

# Start Minikube
start_minikube() {
    print_header "Iniciando Minikube"

    if minikube status &> /dev/null; then
        print_warning "Minikube ya está corriendo"
    else
        print_info "Arrancando Minikube (esto puede tomar 1-2 minutos)..."
        minikube start --cpus=4 --memory=8192 --driver=docker
        print_success "Minikube iniciado"
    fi

    echo ""
}

# Install Argo CD
install_argocd() {
    print_header "Instalando Argo CD"

    print_info "Creando namespace argocd..."
    kubectl create namespace argocd 2>/dev/null || print_warning "Namespace argocd ya existe"

    print_info "Aplicando manifiestos de Argo CD..."
    kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

    print_info "Esperando a que los pods de Argo CD estén listos (esto puede tomar 2-3 minutos)..."
    kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=argocd-server -n argocd --timeout=300s 2>/dev/null || {
        print_warning "Timeout esperando argocd-server, pero continuando..."
    }

    print_success "Argo CD instalado"
    echo ""
}

# Get Argo CD initial password
get_argocd_password() {
    print_info "Obteniendo contraseña inicial de Argo CD..."
    PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" 2>/dev/null | base64 -d 2>/dev/null || echo "ERROR")

    if [ "$PASSWORD" = "ERROR" ]; then
        print_warning "No se pudo obtener la contraseña. Ejecutar manualmente:"
        echo "kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath \"{.data.password}\" | base64 -d && echo"
    else
        print_success "Contraseña: $PASSWORD"
        echo "Usuario: admin"
    fi
    echo ""
}

# Create namespace for application
create_app_namespace() {
    print_header "Creando namespace de la aplicación"

    kubectl apply -f kubernetes/argocd/00-namespace.yaml
    print_success "Namespace 'escuela-conduccion' creado"
    echo ""
}

# Build Docker images (using Minikube's Docker)
build_docker_images() {
    print_header "Construyendo imágenes Docker"

    print_info "Configurando Docker para usar Minikube..."
    eval $(minikube docker-env)

    print_info "Construyendo api-gateway..."
    cd backend/api-gateway
    mvn clean package -DskipTests > /dev/null 2>&1 && docker build -t api-gateway:latest . || {
        print_error "Error construyendo api-gateway"
        return 1
    }
    cd ../..
    print_success "api-gateway construido"

    print_info "Construyendo ms-auth..."
    cd backend/ms-auth
    mvn clean package -DskipTests > /dev/null 2>&1 && docker build -t ms-auth:latest . || {
        print_error "Error construyendo ms-auth"
        return 1
    }
    cd ../..
    print_success "ms-auth construido"

    print_info "Construyendo ms-estudiantes..."
    cd backend/ms-estudiantes
    mvn clean package -DskipTests > /dev/null 2>&1 && docker build -t ms-estudiantes:latest . || {
        print_error "Error construyendo ms-estudiantes"
        return 1
    }
    cd ../..
    print_success "ms-estudiantes construido"

    print_info "Construyendo frontend..."
    cd frontend
    npm install > /dev/null 2>&1 && npm run build > /dev/null 2>&1 && docker build -t frontend:latest . || {
        print_error "Error construyendo frontend"
        return 1
    }
    cd ..
    print_success "frontend construido"

    print_info "Descargando eureka-server..."
    docker pull springcloud/eureka:latest > /dev/null 2>&1
    docker tag springcloud/eureka:latest eureka-server:latest
    print_success "eureka-server listo"

    echo ""
}

# Create Argo CD Application
create_argocd_application() {
    print_header "Creando aplicación en Argo CD"

    print_warning "⚠️  IMPORTANTE: Edita kubernetes/argocd/argo-app.yaml y reemplaza:"
    echo "    https://github.com/tu-usuario/tu-repo.git"
    echo "    con tu URL de repositorio real"
    echo ""
    read -p "¿Has actualizado argo-app.yaml con tu repositorio? (s/n): " -n 1 -r
    echo

    if [[ $REPLY =~ ^[Ss]$ ]]; then
        kubectl apply -f kubernetes/argocd/argo-app.yaml
        print_success "Application creada en Argo CD"
    else
        print_warning "Skipping application creation. Hazlo manualmente cuando listes la URL."
    fi
    echo ""
}

# Verify deployment
verify_deployment() {
    print_header "Verificando despliegue"

    print_info "Pods de Argo CD:"
    kubectl get pods -n argocd
    echo ""

    print_info "Pods de la aplicación:"
    kubectl get pods -n escuela-conduccion || echo "Esperando sincronización..."
    echo ""

    print_info "Servicios:"
    kubectl get svc -n escuela-conduccion || echo "Esperando servicios..."
    echo ""
}

# Print next steps
print_next_steps() {
    print_header "Próximos pasos"

    echo ""
    print_info "1. Acceder a Argo CD:"
    echo "   kubectl port-forward svc/argocd-server -n argocd 8443:443"
    echo "   Abrir: https://localhost:8443"
    echo ""

    print_info "2. Acceder a la aplicación:"
    echo "   kubectl port-forward svc/frontend -n escuela-conduccion 3000:80"
    echo "   Abrir: http://localhost:3000"
    echo ""

    print_info "3. Ver logs de un servicio:"
    echo "   kubectl logs <pod-name> -n escuela-conduccion -f"
    echo ""

    print_info "4. Demostrar GitOps:"
    echo "   - Editar kubernetes/argocd/apps/01-api-gateway.yaml (cambiar replicas)"
    echo "   - Hacer git commit y push"
    echo "   - Ver cambio en Argo CD UI (OutOfSync → Synced)"
    echo ""

    print_info "5. Documentación:"
    echo "   - Ver: kubernetes/README.md"
    echo "   - Setup detallado: kubernetes/SETUP-ARGOCD.md"
    echo "   - Arquitectura: kubernetes/ARQUITECTURA-ARGOCD.md"
    echo "   - Evidencia: kubernetes/EVIDENCIA-PRESENTACION.md"
    echo ""
}

# Main execution
main() {
    clear
    print_header "🚀 Argo CD + Kubernetes Setup"
    echo ""

    check_prerequisites
    start_minikube
    install_argocd
    get_argocd_password
    create_app_namespace

    # Ask user about building images (can be slow)
    read -p "¿Construir imágenes Docker? (esto toma ~10-15 min) (s/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        build_docker_images
    else
        print_warning "Skipping Docker image build. Hazlo manualmente ejecutando:"
        echo "eval \$(minikube docker-env)"
        echo "docker build -t api-gateway:latest ./backend/api-gateway"
        echo "docker build -t ms-auth:latest ./backend/ms-auth"
        echo "docker build -t ms-estudiantes:latest ./backend/ms-estudiantes"
        echo "docker build -t frontend:latest ./frontend"
        echo "docker pull springcloud/eureka:latest && docker tag springcloud/eureka:latest eureka-server:latest"
        echo ""
    fi

    create_argocd_application
    verify_deployment
    print_next_steps

    print_success "Setup completado! 🎉"
}

# Run main
main
