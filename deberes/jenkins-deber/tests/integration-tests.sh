#!/bin/bash
# =============================================================================
# PRUEBA DE INTEGRACION SIMPLE
# Simula la integracion de modulos del proyecto
# =============================================================================

echo "[INTEGRATION TEST] Iniciando pruebas de integracion..."

echo "[INTEGRATION TEST] Test 1: Verificar estructura de carpetas"
if [ -d "src" ] && [ -d "jenkins-deber/tests" ] && [ -d "jenkins-deber/pipeline" ]; then
    echo "  [PASS] Estructura del proyecto correcta (src, jenkins-deber/tests, jenkins-deber/pipeline)"
else
    echo "  [FAIL] Falta alguna carpeta del proyecto"
    exit 1
fi

echo "[INTEGRATION TEST] Test 2: Verificar Jenkinsfile en jenkins-deber"
if [ -f "jenkins-deber/Jenkinsfile" ]; then
    echo "  [PASS] Jenkinsfile encontrado"
else
    echo "  [FAIL] Jenkinsfile no encontrado"
    exit 1
fi

# Simulamos un pequeno delay para visualizar la paralelizacion en Jenkins
echo "[INTEGRATION TEST] Simulando integracion entre modulos..."
sleep 3

echo "[INTEGRATION TEST] Todas las pruebas de integracion pasaron correctamente."
exit 0
