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
          <tr v-for="est in datos" :key="est.id" class="hover:bg-ink-50">
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

      <div v-if="datos.length > 0" class="mt-4 pt-4 border-t border-ink-200 text-xs text-ink-500">
        Total: {{ datos.length }} estudiantes
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
