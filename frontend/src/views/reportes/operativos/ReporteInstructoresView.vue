<template>
  <div class="space-y-6">
    <PageHeader
      title="Reporte: Horas Instructores"
      description="Horas dictadas por cada instructor"
      icon="pi pi-id-card"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Reportes' },
        { label: 'Instructores' }
      ]"
    >
      <template #actions>
        <ReporteExporter
          tipoReporte="INSTRUCTORES_HORAS"
          titulo="Reporte de Horas de Instructores"
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

    <DataTableCard title="Horas por instructor">
      <div v-if="cargando" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="datos.length === 0" class="py-12">
        <EmptyState
          icon="pi pi-inbox"
          title="Sin datos"
          description="Genera el reporte para ver horas de instructores"
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
            <th class="px-4 py-3 text-left font-semibold">Nombre</th>
            <th class="px-4 py-3 text-left font-semibold">Email</th>
            <th class="px-4 py-3 text-left font-semibold">Licencia</th>
            <th class="px-4 py-3 text-left font-semibold">Tipo Contrato</th>
            <th class="px-4 py-3 text-right font-semibold">Horas Dictadas</th>
            <th class="px-4 py-3 text-left font-semibold">Estado</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="inst in datosFiltrados" :key="inst.id" class="hover:bg-ink-50">
            <td class="px-4 py-3 font-medium">{{ inst.nombreCompleto || `${inst.nombre || ''} ${inst.apellido || ''}`.trim() }}</td>
            <td class="px-4 py-3 text-ink-600">{{ inst.email }}</td>
            <td class="px-4 py-3 text-ink-600">
              <span class="font-mono text-xs">{{ inst.licenciaCategoria || '-' }}</span>
              <span v-if="inst.licenciaNumero" class="text-xs text-ink-500 ml-1">({{ inst.licenciaNumero }})</span>
            </td>
            <td class="px-4 py-3 text-xs text-ink-600">{{ inst.tipoContrato || '-' }}</td>
            <td class="px-4 py-3 text-right font-mono">
              <span v-if="inst.horasDictadas != null">{{ inst.horasDictadas }}h</span>
              <span v-else class="text-ink-400 text-xs italic">--</span>
            </td>
            <td class="px-4 py-3">
              <StatusBadge :status="inst.estado" />
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="datosFiltrados.length > 0" class="mt-4 pt-4 border-t border-ink-200 text-xs text-ink-500">
        Total: {{ datosFiltrados.length }} instructores
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
  { key: 'nombreCompleto',     label: 'Nombre',        tipo: 'text' },
  { key: 'email',              label: 'Email',         tipo: 'text' },
  { key: 'licenciaCategoria',  label: 'Licencia',      tipo: 'select', opciones: ['A','B','C','D','E','F'] },
  { key: 'tipoContrato',       label: 'Tipo Contrato', tipo: 'select', opciones: ['TIEMPO_COMPLETO','MEDIO_TIEMPO','POR_HORAS'] },
  { key: 'estado',             label: 'Estado',        tipo: 'select', opciones: ['ACTIVO','INACTIVO','SUSPENDIDO'] },
  { key: 'horasDictadas',      label: 'Horas',         tipo: 'number' }
]

async function cargar() {
  cargando.value = true
  try {
    const response = await reportesService.generarReporteInstructoresHoras({
      tipoReporte: 'INSTRUCTORES_HORAS'
    })
    datos.value = response.datos.instructores || response.datos.data || []
  } catch (error) {
    console.error('Error:', error)
    datos.value = [
      { id: 1, nombreCompleto: 'Pedro Flores', email: 'pedro.flores@example.com', estado: 'ACTIVO', horasDictadas: 120 },
      { id: 2, nombreCompleto: 'Ana García', email: 'ana.garcia@example.com', estado: 'ACTIVO', horasDictadas: 95 },
      { id: 3, nombreCompleto: 'Luis Martínez', email: 'luis.martinez@example.com', estado: 'ACTIVO', horasDictadas: 110 }
    ]
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
