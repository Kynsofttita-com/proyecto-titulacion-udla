// =============================================================================
// MODULO REUTILIZABLE: REPORTE
// Se carga con:  def report = load 'pipeline/report.groovy'
// Se invoca con: report.ejecutar()
// =============================================================================

def ejecutar() {
    echo "================================================"
    echo "  ETAPA: REPORTE FINAL"
    echo "================================================"
    echo ">> Aplicacion : ${env.APP_NAME}"
    echo ">> Version    : ${env.APP_VERSION}"
    echo ">> Rama       : ${env.RAMA_ACTUAL}"
    echo ">> Build      : #${env.BUILD_NUMBER}"
    echo ">> Resultado  : OK"
    echo "================================================"
}

return this
