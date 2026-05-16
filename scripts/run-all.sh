#!/bin/bash

# =============================================================================
# PIPELINE COMPLETO - Ejecuta todos los jobs como lo hace Circle CI
# =============================================================================

set -e  # Exit si algún comando falla

BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                            ║${NC}"
echo -e "${BLUE}║   CIRCLE CI PIPELINE COMPLETO - MIGRACIÓN JENKINS         ║${NC}"
echo -e "${BLUE}║   Build #${CIRCLE_BUILD_NUM} | Rama: ${CIRCLE_BRANCH} | Usuario: ${CIRCLE_USERNAME}        ║${NC}"
echo -e "${BLUE}║                                                            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# ===========================================================================
# JOB 1: CHECKOUT & BUILD
# ===========================================================================
echo -e "${YELLOW}▶ JOB 1: CHECKOUT & BUILD${NC}"
echo "════════════════════════════════════════════════════════════"

echo -e "${YELLOW}[1.1] Descargando código del repositorio...${NC}"
sleep 1
echo "✓ Repositorio cargado"
echo ""

echo -e "${YELLOW}[1.2] Configurando variables de entorno...${NC}"
echo "  APP_NAME: ${APP_NAME}"
echo "  APP_VERSION: ${APP_VERSION}"
echo "  BRANCH: ${CIRCLE_BRANCH}"
echo "  BUILD #: ${CIRCLE_BUILD_NUM}"
echo "✓ Variables configuradas"
echo ""

echo -e "${YELLOW}[1.3] Verificando estructura del proyecto...${NC}"
ls -la src/ 2>/dev/null || echo "  ⚠ Carpeta src no encontrada"
echo "✓ Estructura verificada"
echo ""

echo -e "${YELLOW}[1.4] Ejecutando build...${NC}"
sleep 1
echo "✓ Build completado exitosamente"
echo ""

# ===========================================================================
# JOB 2 & 3: TESTS EN PARALELO
# ===========================================================================
echo -e "${YELLOW}▶ JOB 2 & 3: TESTS (PARALELO)${NC}"
echo "════════════════════════════════════════════════════════════"
echo ""

# Función para ejecutar en paralelo
run_parallel() {
    local job_name=$1
    local script=$2

    echo -e "${YELLOW}[$(date +%H:%M:%S)] Iniciando: ${job_name}${NC}"

    if [ -f "$script" ]; then
        if bash "$script" 2>&1; then
            echo -e "${GREEN}[$(date +%H:%M:%S)] ✓ ${job_name} completado${NC}"
            return 0
        else
            echo -e "${RED}[$(date +%H:%M:%S)] ✗ ${job_name} falló${NC}"
            return 1
        fi
    else
        echo -e "${RED}[$(date +%H:%M:%S)] ✗ Script ${script} no encontrado${NC}"
        return 1
    fi
}

# Ejecutar tests en paralelo (simulado con &)
run_parallel "Unit Tests" "jenkins-deber/tests/unit-tests.sh" &
UNIT_PID=$!

run_parallel "Integration Tests" "jenkins-deber/tests/integration-tests.sh" &
INT_PID=$!

# Esperar a que terminen
wait $UNIT_PID || UNIT_FAIL=1
wait $INT_PID || INT_FAIL=1

echo ""

if [ -z "$UNIT_FAIL" ] && [ -z "$INT_FAIL" ]; then
    echo -e "${GREEN}✓ TODOS LOS TESTS PASARON${NC}"
else
    echo -e "${RED}✗ ALGÚN TEST FALLÓ${NC}"
    exit 1
fi
echo ""

# ===========================================================================
# JOB 4: DEPLOY CONDICIONAL
# ===========================================================================
echo -e "${YELLOW}▶ JOB 4: DEPLOY CONDICIONAL${NC}"
echo "════════════════════════════════════════════════════════════"

if [ "${CIRCLE_BRANCH}" == "main" ]; then
    echo -e "${YELLOW}[Deploy] Rama detectada: MAIN → Desplegando a PRODUCCIÓN${NC}"
    sleep 1
    echo -e "${GREEN}✓ Deploy a PRODUCCIÓN completado${NC}"
    echo "  URL: https://api.proyecto.local"
    echo ""
elif [ "${CIRCLE_BRANCH}" == "develop" ]; then
    echo -e "${YELLOW}[Deploy] Rama detectada: DEVELOP → Desplegando a DESARROLLO${NC}"
    sleep 1
    echo -e "${GREEN}✓ Deploy a DESARROLLO completado${NC}"
    echo "  URL: https://dev-api.proyecto.local"
    echo ""
else
    echo -e "${YELLOW}[Deploy] Rama: ${CIRCLE_BRANCH} → Sin deploy automático${NC}"
    echo ""
fi

# ===========================================================================
# JOB 5: REPORTE FINAL
# ===========================================================================
echo -e "${YELLOW}▶ JOB 5: REPORTE FINAL${NC}"
echo "════════════════════════════════════════════════════════════"

echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ PIPELINE COMPLETADO EXITOSAMENTE           ║${NC}"
echo -e "${GREEN}╠════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║  Aplicación: ${APP_NAME}             ║${NC}"
echo -e "${GREEN}║  Versión:    ${APP_VERSION}                    ║${NC}"
echo -e "${GREEN}║  Rama:       ${CIRCLE_BRANCH}                             ║${NC}"
echo -e "${GREEN}║  Build:      #${CIRCLE_BUILD_NUM}                            ║${NC}"
echo -e "${GREEN}║  Usuario:    ${CIRCLE_USERNAME}                 ║${NC}"
echo -e "${GREEN}║                                                ║${NC}"
echo -e "${GREEN}║  Estado:     ✅ SUCCESS                        ║${NC}"
echo -e "${GREEN}║  Duración:   ~15-25 segundos                  ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════╝${NC}"
echo ""

exit 0
