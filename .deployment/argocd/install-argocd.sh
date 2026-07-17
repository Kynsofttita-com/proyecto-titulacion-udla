#!/bin/bash
# Instalación de ArgoCD en Kubernetes

set -e

echo "======================================"
echo "Instalando ArgoCD"
echo "======================================"

# Crear namespace para ArgoCD
kubectl create namespace argocd || true

# Instalar ArgoCD usando manifiestos oficiales
echo "Descargando e instalando ArgoCD..."
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Esperar a que ArgoCD esté listo
echo "Esperando a que ArgoCD esté listo..."
kubectl wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd

# Obtener password inicial de admin
echo ""
echo "======================================"
echo "ArgoCD instalado correctamente"
echo "======================================"
echo ""
echo "Para acceder a ArgoCD:"
echo "1. Port-forward: kubectl port-forward svc/argocd-server -n argocd 8080:443"
echo "2. URL: https://localhost:8080"
echo "3. Username: admin"
echo ""

# Obtener password
ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" 2>/dev/null | base64 -d || echo "Check with: kubectl get secret -n argocd")
echo "4. Password: $ARGOCD_PASSWORD"
echo ""

# Crear namespace para el proyecto
echo "Creando namespace para el proyecto..."
kubectl create namespace proyecto-titulacion || true

# Aplicar la configuración de la aplicación
echo "Aplicando configuración de ArgoCD Application..."
kubectl apply -f argocd-application.yaml

echo "Listo! ArgoCD está instalado y configurado."
