<template>
  <div class="space-y-6">
    <PageHeader
      title="Reporte: SOAT Vehículos"
      description="Estado de SOAT y próximos vencimientos"
      icon="pi pi-car"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Reportes' },
        { label: 'Vehículos' }
      ]"
    >
      <template #actions>
        <ReporteExporter
          tipoReporte="VEHICULOS_SOAT"
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

    <DataTableCard title="Estado SOAT">
      <div v-if="cargando" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="datos.length === 0" class="py-12">
        <EmptyState icon="pi pi-inbox" title="Sin datos" description="Genera el reporte" />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b border-ink-200 bg-ink-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold">Placa</th>
            <th class="px-4 py-3 text-left font-semibold">Vehículo</th>
            <th class="px-4 py-3 text-left font-semibold">SOAT Vence</th>
            <th class="px-4 py-3 text-left font-semibold">Estado</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="veh in datos" :key="veh.id" class="hover:bg-ink-50">
            <td class="px-4 py-3 font-mono font-bold text-brand-700">{{ veh.placa }}</td>
            <td class="px-4 py-3">{{ veh.marca }} {{ veh.modelo }}</td>
            <td class="px-4 py-3">{{ formatearFecha(veh.fechaVencimientoSoat) }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="veh.soatVigente ? 'VIGENTE' : 'VENCIDO'" />
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
    const response = await reportesService.generarReporteVehiculosSoat({
      tipoReporte: 'VEHICULOS_SOAT'
    })
    datos.value = response.datos.vehiculos || response.datos.data || []
  } catch (error) {
    console.error('Error:', error)
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
