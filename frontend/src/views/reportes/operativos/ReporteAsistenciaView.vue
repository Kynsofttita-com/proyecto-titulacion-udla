<template>
  <div class="space-y-6">
    <PageHeader
      title="Reporte: Asistencia"
      description="Registro de asistencia a clases"
      icon="pi pi-calendar-check"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Reportes' },
        { label: 'Asistencia' }
      ]"
    >
      <template #actions>
        <ReporteExporter
          tipoReporte="ASISTENCIA"
          titulo="Reporte de Asistencia"
          :datos="datosFiltrados"
          :tienesDatos="datosFiltrados.length > 0"
        />
        <Button
          label="Generar reporte"
          icon="pi pi-refresh"
          @click="cargar"
          :loading="cargando"
          severity="primary"
        />
      </template>
    </PageHeader>

    <ReporteFiltros
      v-if="datos.length > 0"
      :campos="camposFiltrables"
      :datos="datos"
      @update:datosFiltrados="datosFiltrados = $event"
    />

    <DataTableCard title="Registros de asistencia">
      <div v-if="cargando" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="datos.length === 0" class="py-12">
        <EmptyState icon="pi pi-inbox" title="Sin datos" description="No hay registros" />
      </div>

      <div v-else-if="datosFiltrados.length === 0" class="py-12">
        <EmptyState icon="pi pi-filter-slash" title="Sin coincidencias" description="Ningun registro coincide con el filtro" />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b border-ink-200 bg-ink-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold">Estudiante</th>
            <th class="px-4 py-3 text-left font-semibold">Estado</th>
            <th class="px-4 py-3 text-right font-semibold">Clases Programadas</th>
            <th class="px-4 py-3 text-right font-semibold">Clases Asistidas</th>
            <th class="px-4 py-3 text-right font-semibold">% Asistencia</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="reg in datosFiltrados" :key="reg.estudianteId" class="hover:bg-ink-50">
            <td class="px-4 py-3">{{ reg.estudianteNombre }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="reg.estado" />
            </td>
            <td class="px-4 py-3 text-right font-mono">{{ reg.clasesProgramadas }}</td>
            <td class="px-4 py-3 text-right font-mono">{{ reg.clasesAsistidas }}</td>
            <td class="px-4 py-3 text-right font-mono font-bold" :class="reg.porcentaje >= 80 ? 'text-success-700' : reg.porcentaje >= 60 ? 'text-warning-700' : 'text-danger-700'">
              {{ reg.porcentaje.toFixed(1) }}%
            </td>
          </tr>
        </tbody>
      </table>
    </DataTableCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import ReporteExporter from '@/components/reportes/ReporteExporter.vue'
import ReporteFiltros, { type CampoFiltro } from '@/components/reportes/ReporteFiltros.vue'
import Button from 'primevue/button'
import reportesService from '@/services/reportes'

const datos = ref<any[]>([])
const datosFiltrados = ref<any[]>([])
const cargando = ref(false)

const camposFiltrables: CampoFiltro[] = [
  { key: 'estudianteNombre',  label: 'Estudiante',           tipo: 'text' },
  { key: 'estado',            label: 'Estado',               tipo: 'select', opciones: ['PRE_MATRICULADO','MATRICULADO','CURSANDO','FINALIZADO','SUSPENDIDO'] },
  { key: 'clasesProgramadas', label: 'Clases Programadas',   tipo: 'number' },
  { key: 'clasesAsistidas',   label: 'Clases Asistidas',     tipo: 'number' },
  { key: 'porcentaje',        label: '% Asistencia',         tipo: 'number' }
]

async function cargar() {
  cargando.value = true
  try {
    const response = await reportesService.generarReporteAsistencia({
      tipoReporte: 'ASISTENCIA'
    })
    // Backend devuelve resumen agregado por estudiante:
    // { estudianteId, estado, porcentaje_asistencia, clases_programadas, clases_asistidas, nombre, apellido }
    const asistencias = response.datos.asistencias || response.datos.data || []
    datos.value = asistencias.map((a: any) => {
      const nombre = a.nombre || ''
      const apellido = a.apellido || ''
      const nombreCompleto = (nombre + ' ' + apellido).trim() || `Estudiante #${a.estudianteId}`
      return {
        estudianteId: a.estudianteId,
        estudianteNombre: nombreCompleto,
        estado: a.estado || '-',
        clasesProgramadas: a.clases_programadas ?? a.clasesProgramadas ?? 0,
        clasesAsistidas: a.clases_asistidas ?? a.clasesAsistidas ?? 0,
        porcentaje: a.porcentaje_asistencia ?? a.porcentajeAsistencia ?? 0
      }
    })
  } catch (error) {
    console.error('Error:', error)
    datos.value = []
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
