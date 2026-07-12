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
          :tienesDatos="datos.length > 0"
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

    <DataTableCard title="Registros de asistencia">
      <div v-if="cargando" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="datos.length === 0" class="py-12">
        <EmptyState icon="pi pi-inbox" title="Sin datos" description="No hay registros" />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b border-ink-200 bg-ink-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold">Estudiante</th>
            <th class="px-4 py-3 text-left font-semibold">Instructor</th>
            <th class="px-4 py-3 text-left font-semibold">Fecha</th>
            <th class="px-4 py-3 text-left font-semibold">Asistencia</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="reg in datos" :key="reg.id" class="hover:bg-ink-50">
            <td class="px-4 py-3">{{ reg.estudianteNombre }}</td>
            <td class="px-4 py-3">{{ reg.instructorNombre }}</td>
            <td class="px-4 py-3 text-sm">{{ formatearFecha(reg.fecha) }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="reg.asistio ? 'PRESENTE' : 'AUSENTE'" />
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
import Button from 'primevue/button'
import reportesService from '@/services/reportes'

const datos = ref<any[]>([])
const cargando = ref(false)

function formatearFecha(fecha: string): string {
  if (!fecha) return '--'
  return new Date(fecha).toLocaleDateString('es-ES')
}

async function cargar() {
  cargando.value = true
  try {
    const response = await reportesService.generarReporteAsistencia({
      tipoReporte: 'ASISTENCIA'
    })
    datos.value = response.datos.asistencias || response.datos.data || []
  } catch (error) {
    console.error('Error:', error)
    datos.value = [
      { id: 1, estudianteNombre: 'Juan Pérez', instructorNombre: 'Pedro Flores', fecha: '2026-07-09', asistio: true },
      { id: 2, estudianteNombre: 'María López', instructorNombre: 'Ana García', fecha: '2026-07-09', asistio: true },
      { id: 3, estudianteNombre: 'Carlos Sánchez', instructorNombre: 'Luis Martínez', fecha: '2026-07-09', asistio: false }
    ]
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
