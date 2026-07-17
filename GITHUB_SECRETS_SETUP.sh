#!/bin/bash

# GitHub Secrets Configuration Script
# This script helps you configure the required secrets in GitHub

REPO="Kynsofttita-com/proyecto-titulacion-udla"
GITHUB_CLI="gh"

# Check if gh CLI is installed
if ! command -v $GITHUB_CLI &> /dev/null; then
    echo "❌ GitHub CLI not found. Install from: https://cli.github.com"
    exit 1
fi

echo "🔐 Configurando GitHub Secrets para: $REPO"
echo ""

# Function to set a secret
set_secret() {
    local name=$1
    local value=$2
    echo "Setting secret: $name"
    echo "$value" | $GITHUB_CLI secret set "$name" --repo "$REPO"
}

echo "Para configurar automáticamente, ejecuta:"
echo ""
echo "# 1. SonarQube"
echo "gh secret set sonar-host-url --repo $REPO --body 'http://sonarqube:9000'"
echo "gh secret set sonar-token --repo $REPO --body 'YOUR_SONAR_TOKEN_HERE'"
echo ""
echo "# 2. Docker Registry"
echo "gh secret set docker-registry --repo $REPO --body 'docker.io'"
echo "gh secret set docker-username --repo $REPO --body 'YOUR_DOCKER_USERNAME'"
echo "gh secret set docker-password --repo $REPO --body 'YOUR_DOCKER_PASSWORD'"
echo ""
echo "# 3. ArgoCD"
echo "gh secret set argocd-server --repo $REPO --body 'https://argocd.example.com'"
echo "gh secret set argocd-token --repo $REPO --body 'YOUR_ARGOCD_TOKEN'"
echo ""
echo "# 4. Slack"
echo "gh secret set slack-webhook --repo $REPO --body 'https://hooks.slack.com/services/YOUR/WEBHOOK/URL'"
echo ""
echo "O ve a: https://github.com/$REPO/settings/secrets/actions"
