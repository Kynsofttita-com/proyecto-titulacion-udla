<template>
  <!-- Exportación de reportes deshabilitada por ahora -->
</template>

<script setup lang="ts">
import { ref } from 'vue'
import reportesService from '@/services/reportes'

interface Props {
  tipoReporte: string
  tienesDatos?: boolean
}

const props = defineProps<Props>()
const cargando = ref(false)

async function exportar(formato: 'EXCEL' | 'CSV') {
  cargando.value = true
  try {
    const blob = await reportesService.exportarReporte({
      tipoReporte: props.tipoReporte,
      formato
    })
    const nombre = reportesService.generarNombreArchivo(props.tipoReporte, formato)
    reportesService.descargarArchivo(blob, nombre)
  } catch (error) {
    console.error('Error exportando reporte:', error)
    alert('Error exportando reporte')
  } finally {
    cargando.value = false
  }
}
</script>
