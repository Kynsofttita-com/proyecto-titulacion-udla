<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-3xl font-bold">Vehículos</h2>
      <Button label="Nuevo Vehículo" icon="pi pi-plus" @click="navigateToForm" />
    </div>

    <div class="p-4 bg-white rounded-lg shadow">
      <div class="flex gap-4 mb-4">
        <InputGroup class="flex-1">
          <InputText
            v-model="searchTerm"
            placeholder="Buscar por placa o marca..."
            @keyup.enter="buscar"
          />
          <Button icon="pi pi-search" @click="buscar" severity="primary" />
        </InputGroup>
        <Button
          icon="pi pi-refresh"
          text
          severity="secondary"
          @click="cargarVehiculos"
        />
      </div>

      <DataTable
        :value="vehiculos"
        striped-rows
        table-style="min-width: 50rem"
        paginator
        :rows="10"
        :total-records="totalVehiculos"
        :loading="isLoading"
        @page="onPageChange"
      >
        <Column field="placa" header="Placa"></Column>
        <Column field="marca" header="Marca"></Column>
        <Column field="modelo" header="Modelo"></Column>
        <Column field="año" header="Año"></Column>
        <Column field="color" header="Color"></Column>
        <Column field="estado" header="Estado">
          <template #body="slotProps">
            <Tag
              :value="slotProps.data.estado"
              :severity="slotProps.data.estado === 'ACTIVO' ? 'success' : 'warning'"
            ></Tag>
          </template>
        </Column>
        <Column field="kilometraje" header="Kilometraje (km)"></Column>
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
import Tag from 'primevue/tag'
import vehiculosService, { VehiculoResponse } from '@/services/vehiculos'

const router = useRouter()

const vehiculos = ref<VehiculoResponse[]>([])
const isLoading = ref(false)
const searchTerm = ref('')
const totalVehiculos = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)

const cargarVehiculos = async () => {
  try {
    isLoading.value = true
    const response = await vehiculosService.obtenerVehiculos(currentPage.value, pageSize.value)
    vehiculos.value = response.content
    totalVehiculos.value = response.totalElements
  } catch (error) {
    console.error('Error loading vehiculos:', error)
  } finally {
    isLoading.value = false
  }
}

const buscar = async () => {
  if (searchTerm.value.trim()) {
    try {
      isLoading.value = true
      vehiculos.value = await vehiculosService.buscarVehiculos(searchTerm.value)
    } catch (error) {
      console.error('Error searching:', error)
    } finally {
      isLoading.value = false
    }
  } else {
    cargarVehiculos()
  }
}

const onPageChange = (event: any) => {
  currentPage.value = event.page
  cargarVehiculos()
}

const confirmarEliminar = async (id: number) => {
  if (confirm('¿Estás seguro de que deseas eliminar este vehículo?')) {
    try {
      await vehiculosService.eliminarVehiculo(id)
      await cargarVehiculos()
    } catch (error) {
      console.error('Error deleting:', error)
    }
  }
}

const navigateToForm = (id?: number) => {
  if (id) {
    router.push(`/vehiculos/${id}/editar`)
  } else {
    router.push('/vehiculos/nuevo')
  }
}

const navigateToDetail = (id: number) => {
  router.push(`/vehiculos/${id}`)
}

onMounted(() => {
  cargarVehiculos()
})
</script>
