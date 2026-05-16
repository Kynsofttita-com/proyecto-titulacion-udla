#!/bin/bash

# =============================================================================
# VALIDAR CONFIGURACIÓN - Verifica que todo esté bien antes de ejecutar
# =============================================================================

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}╔══════════════════════════════════════════════╗${NC}"
echo -e "${YELLOW}║     VALIDACIÓN DE CONFIGURACIÓN CIRCLE CI    ║${NC}"
echo -e "${YELLOW}╚══════════════════════════════════════════════╝${NC}"
echo ""

ERRORS=0

# Validar archivos requeridos
echo -e "${YELLOW}[1] Validando archivos requeridos...${NC}"

files_required=(
    ".circleci/config.yml"
    "jenkins-deber/tests/unit-tests.sh"
    "jenkins-deber/tests/integration-tests.sh"
    "jenkins-deber/Jenkinsfile"
    "src/app.js"
)

for file in "${files_required[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}  ✓ $file${NC}"
    else
        echo -e "${RED}  ✗ $file (NO ENCONTRADO)${NC}"
        ERRORS=$((ERRORS+1))
    fi
done
echo ""

# Validar sintaxis YAML (si yamllint está disponible)
echo -e "${YELLOW}[2] Validando sintaxis YAML...${NC}"

if command -v yamllint &> /dev/null; then
    if yamllint .circleci/config.yml > /dev/null 2>&1; then
        echo -e "${GREEN}  ✓ .circleci/config.yml (sintaxis válida)${NC}"
    else
        echo -e "${RED}  ✗ .circleci/config.yml (errores de sintaxis)${NC}"
        yamllint .circleci/config.yml
        ERRORS=$((ERRORS+1))
    fi
else
    echo -e "${YELLOW}  ⚠ yamllint no está instalado (saltando validación)${NC}"
fi
echo ""

# Validar contenido de src/app.js
echo -e "${YELLOW}[3] Validando contenido de src/app.js...${NC}"

if grep -q "function sumar" src/app.js 2>/dev/null; then
    echo -e "${GREEN}  ✓ Función 'sumar' encontrada${NC}"
else
    echo -e "${RED}  ✗ Función 'sumar' NO encontrada${NC}"
    ERRORS=$((ERRORS+1))
fi

if grep -q "module.exports" src/app.js 2>/dev/null; then
    echo -e "${GREEN}  ✓ Exportaciones encontradas${NC}"
else
    echo -e "${RED}  ✗ Exportaciones NO encontradas${NC}"
    ERRORS=$((ERRORS+1))
fi
echo ""

# Validar permisos de ejecución
echo -e "${YELLOW}[4] Validando permisos de ejecución...${NC}"

for script in jenkins-deber/tests/*.sh; do
    if [ -x "$script" ]; then
        echo -e "${GREEN}  ✓ $script (ejecutable)${NC}"
    else
        echo -e "${YELLOW}  ⚠ $script (no ejecutable, corrigiendo...)${NC}"
        chmod +x "$script"
        echo -e "${GREEN}  ✓ Permisos actualizados${NC}"
    fi
done
echo ""

# Resumen final
echo -e "${YELLOW}╔══════════════════════════════════════════════╗${NC}"
if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}║          ✓ VALIDACIÓN EXITOSA               ║${NC}"
    echo -e "${GREEN}║  Todo está listo para ejecutar              ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════╝${NC}"
    exit 0
else
    echo -e "${RED}║          ✗ VALIDACIÓN FALLIDA               ║${NC}"
    echo -e "${RED}║  Se encontraron $ERRORS error(es)            ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════╝${NC}"
    exit 1
fi
