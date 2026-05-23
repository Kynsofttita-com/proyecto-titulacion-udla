<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-3xl font-bold">Instructores</h2>
      <Button label="Nuevo Instructor" icon="pi pi-plus" @click="navigateToForm" />
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
          @click="cargarInstructores"
        />
      </div>

      <DataTable
        :value="instructores"
        striped-rows
        table-style="min-width: 50rem"
        paginator
        :rows="10"
        :total-records="totalInstructores"
        :loading="isLoading"
        @page="onPageChange"
      >
        <Column field="nombreCompleto" header="Nombre Completo"></Column>
        <Column field="email" header="Email"></Column>
        <Column field="cédula" header="Cédula"></Column>
        <Column field="especialidad" header="Especialidad"></Column>
        <Column field="teléfono" header="Teléfono"></Column>
        <Column field="estado" header="Estado">
          <template #body="slotProps">
            <span :class="slotProps.data.estado === 'ACTIVO' ? 'text-green-600' : 'text-red-600'">
              {{ slotProps.data.estado }}
            </span>
          </template>
        </Column>
        <Column field="horasImpartidas" header="Horas Impartidas"></Column>
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
              @click="confirmarEliminar(slotProps.data.id)"
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
import instructoresService, { InstructorResponse } from '@/services/instructores'

const router = useRouter()

const instructores = ref<InstructorResponse[]>([])
const isLoading = ref(false)
const searchTerm = ref('')
const totalInstructores = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)

const cargarInstructores = async () => {
  try {
    isLoading.value = true
    const response = await instructoresService.obtenerInstructores(currentPage.value, pageSize.value)
    instructores.value = response.content
    totalInstructores.value = response.totalElements
  } catch (error) {
    console.error('Error loading instructores:', error)
  } finally {
    isLoading.value = false
  }
}

const buscar = async () => {
  if (searchTerm.value.trim()) {
    try {
      isLoading.value = true
      instructores.value = await instructoresService.buscarInstructores(searchTerm.value)
    } catch (error) {
      console.error('Error searching:', error)
    } finally {
      isLoading.value = false
    }
  } else {
    cargarInstructores()
  }
}

const onPageChange = (event: any) => {
  currentPage.value = event.page
  cargarInstructores()
}

const confirmarEliminar = async (id: number) => {
  if (confirm('¿Estás seguro de que deseas eliminar este instructor?')) {
    try {
      await instructoresService.eliminarInstructor(id)
      await cargarInstructores()
    } catch (error) {
      console.error('Error deleting:', error)
    }
  }
}

const navigateToForm = (id?: number) => {
  if (id) {
    router.push(`/instructores/${id}/editar`)
  } else {
    router.push('/instructores/nuevo')
  }
}

const navigateToDetail = (id: number) => {
  router.push(`/instructores/${id}`)
}

onMounted(() => {
  cargarInstructores()
})
</script>
