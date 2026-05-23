<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-3xl font-bold">Calendario de Asignaciones</h2>
      <Button label="Nueva Asignación" icon="pi pi-plus" @click="showDialog = true" />
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <div class="lg:col-span-3">
        <div class="p-4 bg-white rounded-lg shadow">
          <Calendar
            v-model="selectedDate"
            :inline="true"
            @date-select="cargarAsignacionesDelDia"
            date-format="dd/mm/yy"
            :show-other-months="false"
          />
        </div>
      </div>

      <div class="space-y-4">
        <div class="p-4 bg-white rounded-lg shadow">
          <h3 class="text-lg font-bold mb-4">
            {{ selectedDate ? formatDate(selectedDate) : 'Selecciona una fecha' }}
          </h3>

          <div v-if="asignacionesDelDia.length === 0" class="text-gray-500 text-center py-4">
            <p>No hay asignaciones para este día</p>
          </div>

          <div v-else class="space-y-2 max-h-96 overflow-y-auto">
            <div
              v-for="asignacion in asignacionesDelDia"
              :key="asignacion.id"
              class="p-3 border rounded-lg hover:bg-gray-50 cursor-pointer"
              @click="navigateToDetail(asignacion.id)"
            >
              <p class="font-medium text-sm">{{ asignacion.horaInicio }} - {{ asignacion.horaFin }}</p>
              <p class="text-xs text-gray-600">{{ asignacion.nombreEstudiante }}</p>
              <p class="text-xs text-gray-600">{{ asignacion.nombreInstructor }}</p>
              <Tag :value="asignacion.estado" :severity="statusSeverity(asignacion.estado)" class="mt-1" />
            </div>
          </div>
        </div>

        <Button label="Ver todas las asignaciones" icon="pi pi-list" class="w-full" @click="router.push('/asignaciones-lista')" />
      </div>
    </div>

    <Dialog v-model:visible="showDialog" header="Nueva Asignación" :modal="true" style="width: 50vw">
      <AsignacionFormDialog @close="showDialog = false" @save="onAsignacionSaved" />
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Dialog from 'primevue/dialog'
import Tag from 'primevue/tag'
import asignacionesService, { AsignacionResponse } from '@/services/asignaciones'
import AsignacionFormDialog from '@/components/asignaciones/AsignacionFormDialog.vue'

const router = useRouter()

const selectedDate = ref<Date | null>(new Date())
const showDialog = ref(false)
const asignacionesDelDia = ref<AsignacionResponse[]>([])
const isLoading = ref(false)

const formatDate = (date: Date) => {
  return date.toLocaleDateString('es-EC', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
}

const statusSeverity = (estado: string) => {
  switch (estado) {
    case 'PROGRAMADA':
      return 'info'
    case 'COMPLETADA':
      return 'success'
    case 'CANCELADA':
      return 'danger'
    default:
      return 'secondary'
  }
}

const cargarAsignacionesDelDia = async (date?: any) => {
  const dateToLoad = date || selectedDate.value
  if (!dateToLoad) return

  try {
    isLoading.value = true
    const fecha = dateToLoad.toISOString().split('T')[0]
    asignacionesDelDia.value = await asignacionesService.obtenerAsignacionesPorFecha(fecha)
  } catch (error) {
    console.error('Error loading asignaciones:', error)
  } finally {
    isLoading.value = false
  }
}

const navigateToDetail = (id: number) => {
  router.push(`/asignaciones/${id}`)
}

const onAsignacionSaved = () => {
  showDialog.value = false
  cargarAsignacionesDelDia()
}
</script>
