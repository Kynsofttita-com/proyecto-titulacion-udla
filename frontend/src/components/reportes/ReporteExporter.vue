<template>
  <div class="flex gap-2">
    <button
      disabled
      title="Exportación en desarrollo"
      class="px-3 py-2 bg-success-600 text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed text-sm font-medium flex items-center gap-2"
    >
      <i class="pi pi-file-excel" />
      Excel
    </button>
    <button
      disabled
      title="Exportación en desarrollo"
      class="px-3 py-2 bg-info-600 text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed text-sm font-medium flex items-center gap-2"
    >
      <i class="pi pi-file" />
      CSV
    </button>
  </div>
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
