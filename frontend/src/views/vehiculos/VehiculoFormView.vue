<template>
  <div class="space-y-6">
    <div class="flex items-center gap-4">
      <Button icon="pi pi-arrow-left" text severity="secondary" @click="router.back()" />
      <h2 class="text-3xl font-bold">{{ isEditing ? 'Editar Vehículo' : 'Nuevo Vehículo' }}</h2>
    </div>

    <div class="p-6 bg-white rounded-lg shadow">
      <form @submit.prevent="guardar" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="block text-sm font-medium mb-2">Placa *</label>
            <InputText v-model="form.placa" class="w-full uppercase" placeholder="ABC-1234" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Marca *</label>
            <InputText v-model="form.marca" class="w-full" placeholder="Toyota" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Modelo *</label>
            <InputText v-model="form.modelo" class="w-full" placeholder="Corolla" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Año *</label>
            <InputNumber v-model="form.año" class="w-full" :min="1990" :max="2100" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Color *</label>
            <InputText v-model="form.color" class="w-full" placeholder="Blanco" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Nro. Motor *</label>
            <InputText v-model="form.nroMotor" class="w-full uppercase" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Nro. Chasis *</label>
            <InputText v-model="form.nroChasis" class="w-full uppercase" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Capacidad de Pasajeros *</label>
            <InputNumber v-model="form.capacidadPasajeros" class="w-full" :min="1" :max="20" required />
          </div>
        </div>

        <div class="flex gap-4 justify-end">
          <Button label="Cancelar" severity="secondary" @click="router.back()" />
          <Button label="Guardar" type="submit" :loading="isLoading" />
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import vehiculosService from '@/services/vehiculos'

const router = useRouter()
const route = useRoute()

const isLoading = ref(false)
const vehiculoId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : id
})

const isEditing = computed(() => !!vehiculoId.value)

const form = reactive({
  placa: '',
  marca: '',
  modelo: '',
  año: new Date().getFullYear(),
  color: '',
  nroMotor: '',
  nroChasis: '',
  capacidadPasajeros: 5
})

const cargarVehiculo = async () => {
  if (!isEditing.value) return
  try {
    isLoading.value = true
    const vehiculo = await vehiculosService.obtenerVehiculo(vehiculoId.value as number)
    Object.assign(form, vehiculo)
  } catch (error) {
    console.error('Error loading vehicle:', error)
  } finally {
    isLoading.value = false
  }
}

const guardar = async () => {
  try {
    isLoading.value = true
    if (isEditing.value) {
      await vehiculosService.actualizarVehiculo(vehiculoId.value as number, {
        id: vehiculoId.value as number,
        ...form
      })
    } else {
      await vehiculosService.crearVehiculo(form)
    }
    router.push('/vehiculos')
  } catch (error) {
    console.error('Error saving:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  cargarVehiculo()
})
</script>
