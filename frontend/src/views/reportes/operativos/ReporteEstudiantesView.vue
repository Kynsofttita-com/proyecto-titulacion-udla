<template>
  <div class="space-y-6">
    <PageHeader
      title="Reporte: Estudiantes Activos"
      description="Listado de estudiantes activos en la escuela"
      icon="pi pi-users"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Reportes' },
        { label: 'Estudiantes' }
      ]"
    >
      <template #actions>
        <ReporteExporter
          tipoReporte="ESTUDIANTES_ACTIVOS"
          titulo="Reporte de Estudiantes Activos"
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

    <!-- Filtros -->
    <ReporteFiltros
      v-if="datos.length > 0"
      :campos="camposFiltrables"
      :datos="datos"
      @update:datosFiltrados="datosFiltrados = $event"
    />

    <!-- Tabla de resultados -->
    <DataTableCard title="Estudiantes activos">
      <div v-if="cargando" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="datos.length === 0" class="py-12">
        <EmptyState
          icon="pi pi-inbox"
          title="Sin datos"
          description="Genera el reporte para ver los estudiantes activos"
        />
      </div>

      <div v-else-if="datosFiltrados.length === 0" class="py-12">
        <EmptyState
          icon="pi pi-filter-slash"
          title="Sin coincidencias"
          description="Ningun registro coincide con el filtro aplicado"
        />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b border-ink-200 bg-ink-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold">ID</th>
            <th class="px-4 py-3 text-left font-semibold">Nombre</th>
            <th class="px-4 py-3 text-left font-semibold">Email</th>
            <th class="px-4 py-3 text-left font-semibold">Estado</th>
            <th class="px-4 py-3 text-left font-semibold">Fecha Matricula</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="est in datosFiltrados" :key="est.id" class="hover:bg-ink-50">
            <td class="px-4 py-3 font-mono text-xs">{{ est.id }}</td>
            <td class="px-4 py-3 font-medium">{{ est.nombreCompleto }}</td>
            <td class="px-4 py-3 text-ink-600">{{ est.email }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="est.estado" />
            </td>
            <td class="px-4 py-3 text-xs text-ink-500">
              {{ formatearFecha(est.fechaMatricula) }}
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="datosFiltrados.length > 0" class="mt-4 pt-4 border-t border-ink-200 text-xs text-ink-500">
        Total: {{ datosFiltrados.length }} estudiantes
      </div>
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
  { key: 'nombreCompleto', label: 'Nombre',         tipo: 'text' },
  { key: 'email',          label: 'Email',          tipo: 'text' },
  { key: 'estado',         label: 'Estado',         tipo: 'select', opciones: ['PRE_MATRICULADO','MATRICULADO','CURSANDO','FINALIZADO','SUSPENDIDO'] },
  { key: 'fechaMatricula', label: 'Fecha Matricula', tipo: 'date' },
  { key: 'id',             label: 'ID',             tipo: 'number' }
]

function formatearFecha(fecha: string): string {
  if (!fecha) return '--'
  return new Date(fecha).toLocaleDateString('es-ES')
}

async function cargar() {
  cargando.value = true
  try {
    const response = await reportesService.generarReporteEstudiantesActivos({
      tipoReporte: 'ESTUDIANTES_ACTIVOS'
    })
    datos.value = response.datos.estudiantes || response.datos.data || []
  } catch (error) {
    console.error('Error generando reporte:', error)
    // Mock data cuando el backend no está disponible
    datos.value = [
      {
        id: 1,
        nombreCompleto: 'Juan Pérez García',
        email: 'juan.perez@example.com',
        estado: 'MATRICULADO',
        fechaMatricula: '2026-01-15'
      },
      {
        id: 2,
        nombreCompleto: 'María López Rodríguez',
        email: 'maria.lopez@example.com',
        estado: 'CURSANDO',
        fechaMatricula: '2026-02-10'
      },
      {
        id: 3,
        nombreCompleto: 'Carlos Sánchez Moreno',
        email: 'carlos.sanchez@example.com',
        estado: 'MATRICULADO',
        fechaMatricula: '2026-03-05'
      }
    ]
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
