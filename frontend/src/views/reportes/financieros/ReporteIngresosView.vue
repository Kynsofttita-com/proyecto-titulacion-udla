<template>
  <div class="space-y-6">
    <PageHeader
      title="Reporte: Ingresos"
      description="Ingresos por período y tendencias"
      icon="pi pi-arrow-up-right"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Reportes' },
        { label: 'Ingresos' }
      ]"
    >
      <template #actions>
        <ReporteExporter
          tipoReporte="INGRESOS_PERIODO"
          :tienesDatos="tienesDatos"
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

    <!-- KPIs financieros -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-lg border border-ink-200 p-4">
        <p class="text-xs text-ink-500 uppercase font-semibold mb-1">Total Ingresos</p>
        <p class="text-2xl font-bold text-success-700">{{ formatMoney(totalIngresos) }}</p>
      </div>
      <div class="bg-white rounded-lg border border-ink-200 p-4">
        <p class="text-xs text-ink-500 uppercase font-semibold mb-1">Total Transacciones</p>
        <p class="text-2xl font-bold text-info-700">{{ totalTransacciones }}</p>
      </div>
      <div class="bg-white rounded-lg border border-ink-200 p-4">
        <p class="text-xs text-ink-500 uppercase font-semibold mb-1">Promedio/Transacción</p>
        <p class="text-2xl font-bold text-brand-700">{{ formatMoney(promedio) }}</p>
      </div>
    </div>

    <!-- Tabla de ingresos -->
    <DataTableCard title="Detalle de ingresos">
      <div v-if="cargando" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="datos.length === 0" class="py-12">
        <EmptyState icon="pi pi-inbox" title="Sin datos" description="Genera el reporte" />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b border-ink-200 bg-ink-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold">Estudiante</th>
            <th class="px-4 py-3 text-left font-semibold">Concepto</th>
            <th class="px-4 py-3 text-right font-semibold">Monto</th>
            <th class="px-4 py-3 text-left font-semibold">Fecha</th>
            <th class="px-4 py-3 text-left font-semibold">Estado</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="ing in datos" :key="ing.id" class="hover:bg-ink-50">
            <td class="px-4 py-3">{{ ing.estudianteNombre }}</td>
            <td class="px-4 py-3 text-sm text-ink-600">{{ ing.concepto }}</td>
            <td class="px-4 py-3 text-right font-mono font-semibold text-success-700">
              {{ formatMoney(ing.monto) }}
            </td>
            <td class="px-4 py-3 text-xs">{{ formatearFecha(ing.fecha) }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="ing.estado" />
            </td>
          </tr>
        </tbody>
      </table>
    </DataTableCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import ReporteExporter from '@/components/reportes/ReporteExporter.vue'
import Button from 'primevue/button'
import reportesService from '@/services/reportes'

const datos = ref<any[]>([])
const cargando = ref(false)
const totalIngresos = ref(0)
const totalTransacciones = ref(0)

const promedio = computed(() => {
  return totalTransacciones.value > 0
    ? totalIngresos.value / totalTransacciones.value
    : 0
})

const tienesDatos = computed(() => datos.value.length > 0)

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
    const response = await reportesService.generarReporteIngresos({
      tipoReporte: 'INGRESOS_PERIODO'
    })
    totalIngresos.value = response.datos.totalIngresos || 0
    totalTransacciones.value = response.datos.totalTransacciones || 0
    datos.value = response.datos.ingresos || response.datos.data || []
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
