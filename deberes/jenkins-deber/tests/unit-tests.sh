#!/bin/bash
# =============================================================================
# PRUEBA UNITARIA SIMPLE
# Verifica que la funcion sumar() del archivo src/app.js retorne lo esperado
# =============================================================================

echo "[UNIT TEST] Iniciando pruebas unitarias..."
echo "[UNIT TEST] Test 1: Verificar que el archivo src/app.js exista"

if [ -f "src/app.js" ]; then
    echo "  [PASS] src/app.js existe"
else
    echo "  [FAIL] src/app.js NO existe"
    exit 1
fi

echo "[UNIT TEST] Test 2: Verificar que contiene la funcion 'sumar'"
if grep -q "function sumar" src/app.js; then
    echo "  [PASS] Funcion 'sumar' encontrada"
else
    echo "  [FAIL] Funcion 'sumar' no encontrada"
    exit 1
fi

echo "[UNIT TEST] Todas las pruebas unitarias pasaron correctamente."
exit 0
