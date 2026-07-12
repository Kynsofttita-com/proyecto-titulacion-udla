<template>
  <div class="space-y-6">
    <PageHeader
      title="Plantillas de Email"
      description="Crea y gestiona plantillas de correo electrónico configurables"
      icon="pi pi-envelope"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Plantillas' }
      ]"
    >
      <template #actions>
        <Button
          label="Nueva plantilla"
          icon="pi pi-plus"
          @click="abrirFormulario()"
          severity="primary"
        />
      </template>
    </PageHeader>

    <DataTableCard title="Plantillas disponibles">
      <div v-if="loading" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="plantillas.length === 0" class="py-12">
        <EmptyState
          icon="pi pi-inbox"
          title="Sin plantillas"
          description="Crea la primera plantilla para comenzar"
        />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b border-ink-200 bg-ink-50">
          <tr>
            <th class="px-4 py-3 text-left font-semibold">Código</th>
            <th class="px-4 py-3 text-left font-semibold">Nombre</th>
            <th class="px-4 py-3 text-left font-semibold">Estado</th>
            <th class="px-4 py-3 text-center font-semibold">Acciones</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="p in plantillas" :key="p.id" class="hover:bg-ink-50">
            <td class="px-4 py-3 font-mono text-xs">{{ p.codigo }}</td>
            <td class="px-4 py-3">{{ p.nombre }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="p.activa ? 'ACTIVA' : 'INACTIVA'" />
            </td>
            <td class="px-4 py-3 text-center">
              <div class="flex gap-2 justify-center">
                <button
                  @click="abrirPreview(p)"
                  class="text-info-600 hover:text-info-700"
                  title="Vista previa"
                >
                  <i class="pi pi-eye text-sm" />
                </button>
                <button
                  @click="abrirFormulario(p)"
                  class="text-warning-600 hover:text-warning-700"
                  title="Editar"
                >
                  <i class="pi pi-pencil text-sm" />
                </button>
                <button
                  @click="eliminar(p.id)"
                  class="text-danger-600 hover:text-danger-700"
                  title="Eliminar"
                >
                  <i class="pi pi-trash text-sm" />
                </button>
              </div>
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
import Button from 'primevue/button'
import plantillasService, { Plantilla, CreatePlantillaRequest } from '@/services/plantillas'

const plantillas = ref<Plantilla[]>([])
const loading = ref(false)

async function cargar() {
  loading.value = true
  try {
    plantillas.value = await plantillasService.obtenerPlantillas()
  } catch (error) {
    console.error('Error cargando plantillas:', error)
  } finally {
    loading.value = false
  }
}

function abrirFormulario(plantilla?: Plantilla) {
  // TODO: Implementar modal de edición
  console.log('Abrir formulario', plantilla)
}

async function abrirPreview(plantilla: Plantilla) {
  try {
    const preview = await plantillasService.previewPlantilla(plantilla.id, {})
    console.log('Preview:', preview)
  } catch (error) {
    console.error('Error preview:', error)
  }
}

async function eliminar(id: number) {
  if (!confirm('¿Eliminar esta plantilla?')) return
  try {
    await plantillasService.eliminarPlantilla(id)
    await cargar()
  } catch (error) {
    console.error('Error eliminando:', error)
  }
}

onMounted(() => {
  cargar()
})
</script>
