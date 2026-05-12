// =============================================================================
// MODULO REUTILIZABLE: BUILD
// Se carga con:  def build = load 'pipeline/build.groovy'
// Se invoca con: build.ejecutar()
// =============================================================================

def ejecutar() {
    echo "================================================"
    echo "  ETAPA: BUILD"
    echo "================================================"
    echo ">> Compilando proyecto: ${env.APP_NAME}"
    echo ">> Version: ${env.APP_VERSION}"

    if (isUnix()) {
        sh '''
            echo "[BUILD] Verificando estructura del proyecto..."
            ls -la src/
            echo "[BUILD] Compilacion finalizada (simulada)."
        '''
    } else {
        bat '''
            echo [BUILD] Verificando estructura del proyecto...
            dir src
            echo [BUILD] Compilacion finalizada (simulada).
        '''
    }

    echo ">> Build completado correctamente."
}

return this
