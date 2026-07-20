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
          titulo="Reporte de SOAT de Vehiculos"
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

      <div v-else-if="datosFiltrados.length === 0" class="py-12">
        <EmptyState
          icon="pi pi-filter-slash"
          title="Sin coincidencias"
          description="Ningun vehiculo coincide con el filtro aplicado"
        />
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
          <tr v-for="veh in datosFiltrados" :key="veh.id" class="hover:bg-ink-50">
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
import ReporteFiltros, { type CampoFiltro } from '@/components/reportes/ReporteFiltros.vue'
import Button from 'primevue/button'
import reportesService from '@/services/reportes'
import { fmtFechaLocal } from '@/utils/fechas'

const datos = ref<any[]>([])
const datosFiltrados = ref<any[]>([])
const cargando = ref(false)
const error = ref<string>('')

const camposFiltrables: CampoFiltro[] = [
  { key: 'placa',                  label: 'Placa',   tipo: 'text' },
  { key: 'marca',                  label: 'Marca',   tipo: 'text' },
  { key: 'modelo',                 label: 'Modelo',  tipo: 'text' },
  { key: 'fechaVencimientoSoat',   label: 'SOAT Vence', tipo: 'date' },
  { key: 'estado',                 label: 'Estado',  tipo: 'select', opciones: ['ACTIVO','MANTENIMIENTO','FUERA_SERVICIO'] }
]

function formatearFecha(fecha: string): string {
  return fmtFechaLocal(fecha, {}, '--', 'es-ES')
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

    // Backend devuelve: soatVencimiento (YYYY-MM-DD). Calculamos vigencia y dias en frontend.
    const hoy = new Date()
    hoy.setHours(0, 0, 0, 0)

    datos.value = vehiculos.map((v: any) => {
      const fechaSoat = v.soatVencimiento
      let diasParaVencer: number | null = null
      let soatVigente = false

      if (fechaSoat) {
        const vence = new Date(fechaSoat + 'T00:00:00')
        const msPorDia = 1000 * 60 * 60 * 24
        diasParaVencer = Math.floor((vence.getTime() - hoy.getTime()) / msPorDia)
        soatVigente = diasParaVencer >= 0
      }

      return {
        id: v.id,
        placa: v.placa,
        marca: v.marca,
        modelo: v.modelo,
        fechaVencimientoSoat: fechaSoat,
        soatVigente,
        estado: v.estado,
        diasParaVencer
      }
    })
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
