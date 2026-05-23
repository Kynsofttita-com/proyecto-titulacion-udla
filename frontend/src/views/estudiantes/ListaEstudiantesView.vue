<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-3xl font-bold">Estudiantes</h2>
      <Button label="Nuevo Estudiante" icon="pi pi-plus" @click="navigateToForm" />
    </div>

    <div class="p-4 bg-white rounded-lg shadow">
      <div class="flex gap-4 mb-4">
        <InputGroup class="flex-1">
          <InputText
            v-model="searchTerm"
            placeholder="Buscar por nombre o cédula..."
            @keyup.enter="buscar"
          />
          <Button icon="pi pi-search" @click="buscar" severity="primary" />
        </InputGroup>
        <Button
          icon="pi pi-refresh"
          text
          severity="secondary"
          @click="cargarEstudiantes"
        />
      </div>

      <DataTable
        :value="estudiantes"
        striped-rows
        table-style="min-width: 50rem"
        paginator
        :rows="10"
        :total-records="totalEstudiantes"
        :loading="isLoading"
        @page="onPageChange"
      >
        <Column field="nombreCompleto" header="Nombre Completo"></Column>
        <Column field="email" header="Email"></Column>
        <Column field="cédula" header="Cédula"></Column>
        <Column field="teléfono" header="Teléfono"></Column>
        <Column field="estado" header="Estado">
          <template #body="slotProps">
            <span :class="slotProps.data.estado === 'ACTIVO' ? 'text-green-600' : 'text-red-600'">
              {{ slotProps.data.estado }}
            </span>
          </template>
        </Column>
        <Column field="fechaMatriculación" header="Matrícula"></Column>
        <Column header="Acciones" style="width: 220px">
          <template #body="slotProps">
            <Button
              icon="pi pi-eye"
              class="p-button-rounded p-button-text p-button-sm mr-2"
              @click="navigateToDetail(slotProps.data.id)"
            />
            <Button
              icon="pi pi-pencil"
              class="p-button-rounded p-button-text p-button-sm mr-2"
              @click="navigateToForm(slotProps.data.id)"
            />
            <Button
              icon="pi pi-trash"
              class="p-button-rounded p-button-text p-button-sm p-button-danger"
              @click="confirmarEliminar(slotProps.data)"
            />
          </template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import InputGroup from 'primevue/inputgroup'
import estudiantesService, { EstudianteResponse } from '@/services/estudiantes'

const router = useRouter()

const estudiantes = ref<EstudianteResponse[]>([])
const isLoading = ref(false)
const searchTerm = ref('')
const totalEstudiantes = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)

const cargarEstudiantes = async () => {
  try {
    isLoading.value = true
    const response = await estudiantesService.obtenerEstudiantes(currentPage.value, pageSize.value)
    estudiantes.value = response.content
    totalEstudiantes.value = response.totalElements
  } catch (error) {
    console.error('Error loading estudiantes:', error)
  } finally {
    isLoading.value = false
  }
}

const buscar = async () => {
  if (searchTerm.value.trim()) {
    try {
      isLoading.value = true
      estudiantes.value = await estudiantesService.buscarEstudiantes(searchTerm.value)
    } catch (error) {
      console.error('Error searching:', error)
    } finally {
      isLoading.value = false
    }
  } else {
    cargarEstudiantes()
  }
}

const onPageChange = (event: any) => {
  currentPage.value = event.page
  cargarEstudiantes()
}

const confirmarEliminar = (estudiante: EstudianteResponse) => {
  router.push(`/estudiantes/${estudiante.id}/eliminar`)
}

const navigateToForm = (id?: number) => {
  if (id) {
    router.push(`/estudiantes/${id}/editar`)
  } else {
    router.push('/estudiantes/nuevo')
  }
}

const navigateToDetail = (id: number) => {
  router.push(`/estudiantes/${id}`)
}

onMounted(() => {
  cargarEstudiantes()
})
</script>
