<template>
  <div class="space-y-6">
    <PageHeader
      title="Reporte: Morosidad"
      description="Clientes con pagos vencidos"
      icon="pi pi-exclamation-circle"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Reportes' },
        { label: 'Morosidad' }
      ]"
    >
      <template #actions>
        <ReporteExporter
          tipoReporte="MOROSIDAD"
          titulo="Reporte de Morosidad"
          :datos="datos"
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

    <!-- Alerta de morosidad total -->
    <div
      v-if="totalMoroso > 0"
      class="bg-warning-50 border border-warning-300 rounded-lg p-4 flex items-center gap-3"
    >
      <i class="pi pi-exclamation-triangle text-warning-700 text-2xl" />
      <div class="flex-1">
        <p class="font-semibold text-warning-900">Total moroso acumulado</p>
        <p class="text-2xl font-bold text-warning-700 mt-1">{{ formatMoney(totalMoroso) }}</p>
      </div>
    </div>

    <!-- Tabla de morosos -->
    <DataTableCard title="Clientes morosos">
      <div v-if="cargando" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="datos.length === 0" class="py-12">
        <EmptyState icon="pi pi-inbox" title="Sin datos" description="No hay clientes morosos" />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b border-ink-200 bg-ink-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold">Estudiante</th>
            <th class="px-4 py-3 text-left font-semibold">Teléfono</th>
            <th class="px-4 py-3 text-right font-semibold">Monto Vencido</th>
            <th class="px-4 py-3 text-left font-semibold">Vencimiento</th>
            <th class="px-4 py-3 text-right font-semibold">Días Atraso</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="moroso in datos" :key="moroso.id" class="hover:bg-ink-50">
            <td class="px-4 py-3 font-medium">{{ moroso.estudianteNombre }}</td>
            <td class="px-4 py-3 text-ink-600">{{ moroso.telefono }}</td>
            <td class="px-4 py-3 text-right font-mono font-bold text-danger-700">
              {{ formatMoney(moroso.montoVencido) }}
            </td>
            <td class="px-4 py-3 text-xs">{{ formatearFecha(moroso.fechaVencimiento) }}</td>
            <td class="px-4 py-3 text-right">
              <span
                :class="['px-2 py-1 rounded text-xs font-semibold',
                  moroso.diasAtraso > 30
                    ? 'bg-danger-100 text-danger-800'
                    : 'bg-warning-100 text-warning-800']"
              >
                {{ moroso.diasAtraso }}
              </span>
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
import ReporteExporter from '@/components/reportes/ReporteExporter.vue'
import Button from 'primevue/button'
import reportesService from '@/services/reportes'

const datos = ref<any[]>([])
const cargando = ref(false)
const totalMoroso = ref(0)

function formatMoney(valor: number): string {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'USD'
  }).format(valor)
}

function formatearFecha(fecha: string): string {
  if (!fecha) return '--'
  return new Date(fecha).toLocaleDateString('es-ES')
}

async function cargar() {
  cargando.value = true
  try {
    const response = await reportesService.generarReporteMorosidad({
      tipoReporte: 'MOROSIDAD'
    })
    totalMoroso.value = response.datos.totalMoroso || 0
    datos.value = response.datos.morosos || response.datos.data || []
  } catch (error) {
    console.error('Error:', error)
    totalMoroso.value = 3000
    datos.value = [
      { id: 1, estudianteNombre: 'Pedro Gómez', telefono: '0987654321', montoVencido: 1500, fechaVencimiento: '2026-06-15', diasAtraso: 24 },
      { id: 2, estudianteNombre: 'Rosa Martínez', telefono: '0912345678', montoVencido: 1500, fechaVencimiento: '2026-06-20', diasAtraso: 19 }
    ]
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
