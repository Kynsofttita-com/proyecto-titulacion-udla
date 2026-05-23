<template>
  <div class="space-y-6">
    <div class="flex items-center gap-4">
      <Button icon="pi pi-arrow-left" text severity="secondary" @click="router.back()" />
      <h2 class="text-3xl font-bold">{{ instructor?.nombreCompleto || 'Cargando...' }}</h2>
      <Button icon="pi pi-pencil" text @click="router.push(`/instructores/${instructorId}/editar`)" class="ml-auto" />
    </div>

    <div v-if="isLoading" class="text-center py-8">
      <ProgressSpinner></ProgressSpinner>
    </div>

    <div v-else-if="instructor" class="space-y-6">
      <TabView>
        <TabPanel header="Información" :header-style="{ padding: 0 }">
          <div class="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-gray-600">Email</label>
              <p class="text-lg font-medium">{{ instructor.email }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Cédula</label>
              <p class="text-lg font-medium">{{ instructor.cédula }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Licencia de Conducir</label>
              <p class="text-lg font-medium">{{ instructor.licenciaConducir }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Especialidad</label>
              <p class="text-lg font-medium">{{ instructor.especialidad }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Teléfono</label>
              <p class="text-lg font-medium">{{ instructor.teléfono }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Estado</label>
              <span :class="instructor.estado === 'ACTIVO' ? 'text-green-600' : 'text-red-600'" class="text-lg font-medium">
                {{ instructor.estado }}
              </span>
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-gray-600">Horas Impartidas</label>
              <p class="text-2xl font-bold text-blue-600">{{ instructor.horasImpartidas }}h</p>
            </div>
          </div>
        </TabPanel>

        <TabPanel header="Disponibilidad" :header-style="{ padding: 0 }">
          <div class="p-6">
            <div class="mb-6">
              <h3 class="text-lg font-bold mb-4">Horario de Disponibilidad</h3>
              <DataTable :value="disponibilidad" striped-rows table-style="min-width: 50rem">
                <Column field="día" header="Día"></Column>
                <Column field="horaInicio" header="Hora Inicio"></Column>
                <Column field="horaFin" header="Hora Fin"></Column>
                <Column field="disponible" header="Disponible">
                  <template #body="slotProps">
                    <span :class="slotProps.data.disponible ? 'text-green-600' : 'text-red-600'">
                      {{ slotProps.data.disponible ? 'Disponible' : 'No Disponible' }}
                    </span>
                  </template>
                </Column>
              </DataTable>
            </div>
            <Button label="Editar Disponibilidad" icon="pi pi-pencil" />
          </div>
        </TabPanel>
      </TabView>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Button from 'primevue/button'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import ProgressSpinner from 'primevue/progressspinner'
import instructoresService, { InstructorResponse, DisponibilidadSlot } from '@/services/instructores'

const router = useRouter()
const route = useRoute()

const isLoading = ref(false)
const instructor = ref<InstructorResponse | null>(null)
const disponibilidad = ref<DisponibilidadSlot[]>([])

const instructorId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : id
})

const cargar = async () => {
  try {
    isLoading.value = true
    const [inst, disp] = await Promise.all([
      instructoresService.obtenerInstructor(instructorId.value as number),
      instructoresService.obtenerDisponibilidad(instructorId.value as number)
    ])
    instructor.value = inst
    disponibilidad.value = disp
  } catch (error) {
    console.error('Error loading instructor:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
