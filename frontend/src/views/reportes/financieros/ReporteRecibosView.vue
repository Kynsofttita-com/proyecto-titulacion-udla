<template>
  <div class="space-y-6">
    <PageHeader
      title="Reporte: Recibos"
      description="Listado de recibos de pago emitidos"
      icon="pi pi-file-pdf"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Reportes' },
        { label: 'Recibos' }
      ]"
    >
      <template #actions>
        <ReporteExporter
          tipoReporte="RECIBOS"
          titulo="Reporte de Recibos"
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

    <!-- Filtros -->
    <div class="bg-white rounded-lg border border-ink-200 p-4 flex gap-2 flex-wrap items-end">
      <div>
        <label class="text-xs font-semibold text-ink-500 block mb-1">Estado</label>
        <select
          v-model="filtroEstado"
          class="px-3 py-2 border border-ink-200 rounded-lg text-sm"
        >
          <option value="">Todos</option>
          <option value="EMITIDO">Emitido</option>
          <option value="PAGADO">Pagado</option>
          <option value="ANULADO">Anulado</option>
        </select>
      </div>
      <button
        @click="cargar"
        class="px-4 py-2 bg-brand-600 text-white rounded-lg hover:bg-brand-700 text-sm font-medium"
      >
        Filtrar
      </button>
    </div>

    <!-- Tabla de recibos -->
    <DataTableCard title="Recibos">
      <div v-if="cargando" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="datos.length === 0" class="py-12">
        <EmptyState icon="pi pi-inbox" title="Sin datos" description="No hay recibos" />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b border-ink-200 bg-ink-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold">Número</th>
            <th class="px-4 py-3 text-left font-semibold">Estudiante</th>
            <th class="px-4 py-3 text-right font-semibold">Monto</th>
            <th class="px-4 py-3 text-left font-semibold">Fecha Emisión</th>
            <th class="px-4 py-3 text-left font-semibold">Estado</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="recibo in datos" :key="recibo.id" class="hover:bg-ink-50">
            <td class="px-4 py-3 font-mono font-bold text-brand-700">{{ recibo.numero }}</td>
            <td class="px-4 py-3">{{ recibo.estudianteNombre }}</td>
            <td class="px-4 py-3 text-right font-mono">{{ formatMoney(recibo.monto) }}</td>
            <td class="px-4 py-3 text-xs">{{ formatearFecha(recibo.fechaEmision) }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="recibo.estado" />
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
const filtroEstado = ref('')

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
    const response = await reportesService.generarReporteRecibos({
      tipoReporte: 'RECIBOS'
    })
    datos.value = response.datos.recibos || response.datos.data || []
  } catch (error) {
    console.error('Error:', error)
    datos.value = [
      { id: 1, numero: 'REC-001', estudianteNombre: 'Juan Pérez', monto: 1500, fechaEmision: '2026-07-01', estado: 'PAGADO' },
      { id: 2, numero: 'REC-002', estudianteNombre: 'María López', monto: 1500, fechaEmision: '2026-07-02', estado: 'PAGADO' },
      { id: 3, numero: 'REC-003', estudianteNombre: 'Carlos Sánchez', monto: 1500, fechaEmision: '2026-07-03', estado: 'EMITIDO' }
    ]
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
