#!/usr/bin/env bash
#
# Compila TODO el backend Spring Boot de forma SECUENCIAL (modulo por modulo)
# y copia los JARs listos a infrastructure/docker/jars-dist/ para que
# docker-compose los use.
#
# IMPORTANTE: Compilar en paralelo (mvn -T) o desde el root del multi-modulo
# causa bytecode corrupto por race condition MapStruct + Lombok. Este script
# compila modulo por modulo para evitar ese problema.
#
# Uso:
#   ./scripts/build-backend.sh
#
# Requisitos:
#   - Java 21 (JAVA_HOME configurado)
#   - Maven 3.8+
#

set -euo pipefail

# Ir al directorio raiz del proyecto (donde este script vive)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT_DIR"

echo "=================================================="
echo " Build Backend - Compilacion secuencial"
echo "=================================================="
echo ""
echo "Directorio: $ROOT_DIR"
echo ""

# Verificar Java y Maven
if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: Java no esta instalado o no esta en PATH"
    exit 1
fi
if ! command -v mvn >/dev/null 2>&1; then
    echo "ERROR: Maven no esta instalado o no esta en PATH"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1)
MVN_VERSION=$(mvn -version | head -1)
echo "Java: $JAVA_VERSION"
echo "Maven: $MVN_VERSION"
echo ""

# Paso 0: Limpiar cache local .m2 del proyecto y targets
echo "[0/4] Limpiando artefactos previos..."
rm -rf "$HOME/.m2/repository/com/escuela/" 2>/dev/null || true
find backend -type d -name "target" -exec rm -rf {} + 2>/dev/null || true
mkdir -p infrastructure/docker/jars-dist
rm -f infrastructure/docker/jars-dist/*.jar

# Paso 1: Parent POM
echo ""
echo "[1/4] Instalando parent POM..."
(cd backend && mvn install -N -DskipTests -q)
echo "  OK"

# Paso 2: Modulos compartidos (SECUENCIAL)
echo ""
echo "[2/4] Compilando modulos compartidos..."
for mod in common-events common-exceptions common-security common-jpa common-validation; do
    echo "  - shared/$mod"
    (cd "backend/shared/$mod" && mvn install -DskipTests -q)
done
echo "  OK"

# Paso 3: Microservicios (SECUENCIAL, uno por uno)
echo ""
echo "[3/4] Compilando microservicios..."
for ms in eureka-server api-gateway ms-auth ms-estudiantes ms-instructores \
          ms-vehiculos ms-asignaciones ms-cobros ms-reportes ms-notificaciones; do
    echo "  - $ms"
    (cd "backend/$ms" && mvn install -DskipTests -q)
done
echo "  OK"

# Paso 4: Copiar JARs a jars-dist/
echo ""
echo "[4/4] Copiando JARs a infrastructure/docker/jars-dist/..."
for ms in eureka-server api-gateway ms-auth ms-estudiantes ms-instructores \
          ms-vehiculos ms-asignaciones ms-cobros ms-reportes ms-notificaciones; do
    src="backend/$ms/target/$ms-0.0.1-SNAPSHOT.jar"
    dst="infrastructure/docker/jars-dist/$ms.jar"
    if [ ! -f "$src" ]; then
        echo "  ERROR: no se encontro $src"
        exit 1
    fi
    cp "$src" "$dst"
    echo "  - $ms.jar ($(du -h "$dst" | cut -f1))"
done

# Verificacion final: bytecode limpio (sin errores de compilacion)
echo ""
echo "Verificando integridad de bytecode..."
TOTAL_ERRORS=0
for jar in infrastructure/docker/jars-dist/*.jar; do
    ms=$(basename "$jar" .jar)
    # Buscar clases con "Unresolved compilation problems" (bug MapStruct+Lombok)
    errors=$(unzip -p "$jar" 2>/dev/null | strings 2>/dev/null | \
             grep -c "Unresolved compilation problems" || echo "0")
    if [ "$errors" -gt 0 ]; then
        echo "  FALLO: $ms tiene $errors errores"
        TOTAL_ERRORS=$((TOTAL_ERRORS + errors))
    fi
done
if [ "$TOTAL_ERRORS" -eq 0 ]; then
    echo "  OK - Todos los JARs con bytecode limpio"
fi

echo ""
echo "=================================================="
echo " Build completado exitosamente"
echo "=================================================="
echo ""
echo "Siguiente paso:"
echo "  cd infrastructure/docker"
echo "  docker-compose up -d --build"
echo ""
