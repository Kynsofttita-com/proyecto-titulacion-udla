// =============================================================================
// MODULO REUTILIZABLE: TEST
// Recibe parametro 'tipo' (unit | integration)
// Se carga con:  def test = load 'pipeline/test.groovy'
// Se invoca con: test.ejecutar('unit')  o  test.ejecutar('integration')
// =============================================================================

def ejecutar(String tipo) {
    echo "================================================"
    echo "  ETAPA: TEST [${tipo.toUpperCase()}]"
    echo "================================================"

    def scriptPath = "tests/${tipo}-tests.sh"

    if (isUnix()) {
        sh """
            chmod +x ${scriptPath}
            ./${scriptPath}
        """
    } else {
        bat "bash ${scriptPath}"
    }

    echo ">> Test '${tipo}' finalizado."
}

return this
