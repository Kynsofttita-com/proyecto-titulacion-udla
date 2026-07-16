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

      <div v-else-if="error" class="py-8 px-4 bg-red-50 rounded-lg border border-red-200">
        <div class="flex items-center gap-3">
          <i class="pi pi-exclamation-circle text-red-600 text-2xl" />
          <div>
            <p class="font-semibold text-red-900">Error al generar el reporte</p>
            <p class="text-red-700 text-sm">{{ error }}</p>
            <p class="text-red-600 text-xs mt-2">Asegúrate de que hay vehículos registrados en el sistema</p>
          </div>
        </div>
      </div>

      <div v-else-if="datos.length === 0" class="py-12">
        <EmptyState icon="pi pi-car" title="Sin datos disponibles" description="No hay vehículos registrados en el sistema" />
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
const error = ref<string>('')

function formatearFecha(fecha: string): string {
  if (!fecha) return '--'
  return new Date(fecha).toLocaleDateString('es-ES')
}

async function cargar() {
  cargando.value = true
  error.value = ''
  try {
    const response = await reportesService.generarReporteVehiculosSoat({
      tipoReporte: 'VEHICULOS_SOAT'
    })

    // Extraer datos del reporte
    const vehiculos = response.datos?.vehiculos || []
    console.log('Vehículos recibidos:', vehiculos)

    // Mapear a formato esperado si es necesario
    datos.value = vehiculos.map((v: any) => ({
      id: v.id,
      placa: v.placa,
      marca: v.marca,
      modelo: v.modelo,
      fechaVencimientoSoat: v.fecha_soat || v.fechaSoat || v.fecha_vencimiento_soat,
      soatVigente: v.soat_vigente !== false && v.soatVigente !== false,
      estado: v.estado,
      diasParaVencer: v.dias_para_vencer || v.diasParaVencer
    }))
  } catch (err: any) {
    console.error('Error generando reporte:', err)
    error.value = err.response?.data?.message || err.message || 'Error al generar el reporte. Intenta de nuevo.'
    datos.value = []
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
