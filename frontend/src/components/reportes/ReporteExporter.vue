<template>
  <div class="inline-flex items-center gap-2">
    <Button
      icon="pi pi-file-pdf"
      label="PDF"
      severity="danger"
      outlined
      size="small"
      :disabled="!tienesDatos || cargando === 'PDF'"
      :loading="cargando === 'PDF'"
      @click="exportar('PDF')"
    />
    <Button
      icon="pi pi-file-excel"
      label="Excel"
      severity="success"
      outlined
      size="small"
      :disabled="!tienesDatos || cargando === 'EXCEL'"
      :loading="cargando === 'EXCEL'"
      @click="exportar('EXCEL')"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Button from 'primevue/button'
import { useToast } from 'primevue/usetoast'
import reportesService from '@/services/reportes'

interface Props {
  /** Codigo del tipo de reporte (ej: ESTUDIANTES_ACTIVOS, VEHICULOS_SOAT). */
  tipoReporte: string
  /**
   * Titulo legible para el archivo generado
   * (aparece en el PDF y en el nombre del archivo).
   */
  titulo?: string
  /** Datos actuales de la tabla del reporte, tal como se muestran. */
  datos?: Record<string, any>[]
  /** Boolean helper para saber si hay filas para exportar. */
  tienesDatos?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  titulo: '',
  datos: () => [],
  tienesDatos: false
})

const toast = useToast()
const cargando = ref<'PDF' | 'EXCEL' | null>(null)

async function exportar(formato: 'PDF' | 'EXCEL') {
  if (!props.tienesDatos || !props.datos || props.datos.length === 0) {
    toast.add({
      severity: 'warn',
      summary: 'Sin datos',
      detail: 'Genera primero el reporte para poder exportarlo',
      life: 3000
    })
    return
  }

  cargando.value = formato
  const titulo = props.titulo || tituloDefault(props.tipoReporte)

  try {
    const blob = formato === 'PDF'
      ? await reportesService.exportarPDF(titulo, props.datos)
      : await reportesService.exportarExcel(titulo, props.datos)

    const nombre = reportesService.generarNombreArchivo(props.tipoReporte, formato)
    reportesService.descargarArchivo(blob, nombre)

    toast.add({
      severity: 'success',
      summary: 'Descarga iniciada',
      detail: `${nombre} se guardo en tu carpeta de descargas`,
      life: 3000
    })
  } catch (error: any) {
    console.error(`Error exportando reporte a ${formato}:`, error)
    toast.add({
      severity: 'error',
      summary: 'Error al exportar',
      detail: error?.response?.data?.detail || `No se pudo generar el archivo ${formato}`,
      life: 4000
    })
  } finally {
    cargando.value = null
  }
}

function tituloDefault(tipo: string): string {
  const map: Record<string, string> = {
    ESTUDIANTES_ACTIVOS: 'Reporte de Estudiantes Activos',
    INSTRUCTORES_HORAS: 'Reporte de Horas de Instructores',
    VEHICULOS_SOAT: 'Reporte de SOAT de Vehiculos',
    ASISTENCIA: 'Reporte de Asistencia',
    INGRESOS_PERIODO: 'Reporte de Ingresos por Periodo',
    SALDOS_ESTUDIANTE: 'Reporte de Saldos de Estudiantes',
    MOROSIDAD: 'Reporte de Morosidad',
    RECIBOS: 'Reporte de Recibos'
  }
  return map[tipo] || `Reporte ${tipo}`
}
</script>
