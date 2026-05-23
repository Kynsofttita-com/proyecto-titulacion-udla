<template>
  <div class="space-y-6">
    <div class="flex items-center gap-4">
      <Button icon="pi pi-arrow-left" text severity="secondary" @click="router.back()" />
      <h2 class="text-3xl font-bold">{{ estudiante?.nombreCompleto || 'Cargando...' }}</h2>
      <Button icon="pi pi-pencil" text @click="router.push(`/estudiantes/${estudianteId}/editar`)" class="ml-auto" />
    </div>

    <div v-if="isLoading" class="text-center py-8">
      <ProgressSpinner></ProgressSpinner>
    </div>

    <div v-else-if="estudiante" class="space-y-6">
      <TabView>
        <TabPanel header="Información Personal" :header-style="{ padding: 0 }">
          <div class="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-gray-600">Email</label>
              <p class="text-lg font-medium">{{ estudiante.email }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Cédula</label>
              <p class="text-lg font-medium">{{ estudiante.cédula }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Nombre Completo</label>
              <p class="text-lg font-medium">{{ estudiante.nombreCompleto }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Género</label>
              <p class="text-lg font-medium">{{ estudiante.género }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Teléfono</label>
              <p class="text-lg font-medium">{{ estudiante.teléfono }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Estado</label>
              <span :class="estudiante.estado === 'ACTIVO' ? 'text-green-600' : 'text-red-600'" class="text-lg font-medium">
                {{ estudiante.estado }}
              </span>
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-gray-600">Dirección</label>
              <p class="text-lg font-medium">{{ estudiante.dirección }}</p>
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-gray-600">Fecha de Matrícula</label>
              <p class="text-lg font-medium">{{ estudiante.fechaMatriculación }}</p>
            </div>
          </div>
        </TabPanel>

        <TabPanel header="Progreso Académico" :header-style="{ padding: 0 }">
          <div v-if="progreso" class="p-6 space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div class="p-4 bg-blue-50 rounded-lg">
                <p class="text-sm font-gray-600 mb-1">Horas Completadas</p>
                <p class="text-3xl font-bold text-blue-600">{{ progreso.horasCompletadas }}</p>
              </div>
              <div class="p-4 bg-purple-50 rounded-lg">
                <p class="text-sm font-gray-600 mb-1">Horas Requeridas</p>
                <p class="text-3xl font-bold text-purple-600">{{ progreso.horasRequeridas }}</p>
              </div>
              <div class="p-4 bg-green-50 rounded-lg">
                <p class="text-sm font-gray-600 mb-1">% Completado</p>
                <p class="text-3xl font-bold text-green-600">{{ progreso.porcentajeComplección }}%</p>
              </div>
            </div>

            <ProgressBar :value="progreso.porcentajeComplección"></ProgressBar>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="p-4 border rounded-lg">
                <p class="text-sm font-gray-600 mb-2">Asignaciones Completadas</p>
                <p class="text-2xl font-bold">{{ progreso.asignacionesCompletadas }}</p>
              </div>
              <div class="p-4 border rounded-lg">
                <p class="text-sm font-gray-600 mb-2">Asignaciones Pendientes</p>
                <p class="text-2xl font-bold text-orange-600">{{ progreso.asignacionesPendientes }}</p>
              </div>
            </div>
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
import ProgressSpinner from 'primevue/progressspinner'
import ProgressBar from 'primevue/progressbar'
import estudiantesService, { EstudianteResponse, ProgresoAcademico } from '@/services/estudiantes'

const router = useRouter()
const route = useRoute()

const isLoading = ref(false)
const estudiante = ref<EstudianteResponse | null>(null)
const progreso = ref<ProgresoAcademico | null>(null)

const estudianteId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : id
})

const cargar = async () => {
  try {
    isLoading.value = true
    const [est, prog] = await Promise.all([
      estudiantesService.obtenerEstudiante(estudianteId.value as number),
      estudiantesService.obtenerProgreso(estudianteId.value as number)
    ])
    estudiante.value = est
    progreso.value = prog
  } catch (error) {
    console.error('Error loading student:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
