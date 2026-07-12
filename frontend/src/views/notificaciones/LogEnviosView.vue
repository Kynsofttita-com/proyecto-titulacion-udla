<template>
  <div class="space-y-6">
    <PageHeader
      title="Histórico de Envíos"
      description="Consulta el estado de todos los correos electrónicos enviados"
      icon="pi pi-history"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Histórico' }
      ]"
    />

    <!-- Filtros -->
    <FormCard title="Filtrar">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Estado</label>
          <select
            v-model="filtros.estado"
            class="w-full px-3 py-2 border border-ink-200 rounded-lg text-sm"
          >
            <option value="">Todos</option>
            <option value="ENVIADO">Enviado</option>
            <option value="PENDIENTE">Pendiente</option>
            <option value="ERROR">Error</option>
            <option value="RECHAZADO">Rechazado</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Desde</label>
          <input
            v-model="filtros.fechaDesde"
            type="date"
            class="w-full px-3 py-2 border border-ink-200 rounded-lg text-sm"
          />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Hasta</label>
          <input
            v-model="filtros.fechaHasta"
            type="date"
            class="w-full px-3 py-2 border border-ink-200 rounded-lg text-sm"
          />
        </div>
        <div class="flex items-end gap-2">
          <button
            @click="cargar"
            class="flex-1 px-4 py-2 bg-brand-600 text-white rounded-lg hover:bg-brand-700 text-sm font-medium"
          >
            Filtrar
          </button>
          <button
            @click="limpiarFiltros"
            class="px-4 py-2 border border-ink-200 rounded-lg hover:bg-ink-50 text-sm"
          >
            Limpiar
          </button>
        </div>
      </div>
    </FormCard>

    <!-- Tabla de envíos -->
    <DataTableCard title="Envíos registrados">
      <div v-if="loading" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="envios.length === 0" class="py-12">
        <EmptyState
          icon="pi pi-inbox"
          title="Sin registros"
          description="No hay envíos que coincidan con los filtros"
        />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b border-ink-200 bg-ink-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold">Plantilla</th>
            <th class="px-4 py-3 text-left font-semibold">Destinatario</th>
            <th class="px-4 py-3 text-left font-semibold">Estado</th>
            <th class="px-4 py-3 text-left font-semibold">Intentos</th>
            <th class="px-4 py-3 text-left font-semibold">Fecha</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="envio in envios" :key="envio.id" class="hover:bg-ink-50">
            <td class="px-4 py-3 font-mono text-xs">
              {{ envio.plantillaCodigo }}
            </td>
            <td class="px-4 py-3 text-ink-600">{{ envio.destinatario }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="envio.estado" />
            </td>
            <td class="px-4 py-3">{{ envio.intentos }}</td>
            <td class="px-4 py-3 text-xs text-ink-500">
              {{ formatearFecha(envio.createdAt) }}
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
import FormCard from '@/components/ui/FormCard.vue'
import plantillasService, { LogEnvio, LogEnviosFilter } from '@/services/plantillas'

const envios = ref<LogEnvio[]>([])
const loading = ref(false)
const filtros = ref<LogEnviosFilter>({
  estado: '',
  fechaDesde: '',
  fechaHasta: '',
  page: 0,
  size: 50
})

function formatearFecha(fecha: string): string {
  return new Date(fecha).toLocaleString('es-ES')
}

async function cargar() {
  loading.value = true
  try {
    const resultado = await plantillasService.obtenerLogEnvios({
      estado: filtros.value.estado || undefined,
      fechaDesde: filtros.value.fechaDesde || undefined,
      fechaHasta: filtros.value.fechaHasta || undefined,
      page: 0,
      size: 50
    })
    envios.value = resultado.data
  } catch (error) {
    console.error('Error cargando log envíos:', error)
  } finally {
    loading.value = false
  }
}

function limpiarFiltros() {
  filtros.value = {
    estado: '',
    fechaDesde: '',
    fechaHasta: '',
    page: 0,
    size: 50
  }
  cargar()
}

onMounted(() => {
  cargar()
})
</script>
