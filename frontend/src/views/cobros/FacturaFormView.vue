<template>
  <div class="space-y-6">
    <div class="flex items-center gap-4">
      <Button icon="pi pi-arrow-left" text severity="secondary" @click="router.back()" />
      <h2 class="text-3xl font-bold">{{ isEditing ? 'Editar Factura' : 'Nueva Factura' }}</h2>
    </div>

    <div class="p-6 bg-white rounded-lg shadow">
      <form @submit.prevent="guardar" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="block text-sm font-medium mb-2">Estudiante *</label>
            <Dropdown
              v-model="form.estudianteId"
              :options="estudiantes"
              option-label="nombreCompleto"
              option-value="id"
              placeholder="Selecciona un estudiante"
              class="w-full"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Monto *</label>
            <InputNumber
              v-model="form.monto"
              mode="currency"
              currency="USD"
              class="w-full"
              :min-fraction-digits="2"
              :max-fraction-digits="2"
              required
            />
          </div>
          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-2">Descripción *</label>
            <Textarea v-model="form.descripción" rows="3" class="w-full" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Fecha Vencimiento *</label>
            <Calendar
              v-model="form.fechaVencimiento"
              date-format="dd/mm/yy"
              :show-icon="true"
              class="w-full"
              required
            />
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
import Dropdown from 'primevue/dropdown'
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import InputNumber from 'primevue/inputnumber'
import cobrosService from '@/services/cobros'
import estudiantesService from '@/services/estudiantes'

const router = useRouter()
const route = useRoute()

const isLoading = ref(false)
const estudiantes = ref([])
const facturaId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : id
})
const isEditing = computed(() => !!facturaId.value)

const form = reactive({
  estudianteId: null,
  descripción: '',
  monto: 0,
  fechaVencimiento: new Date()
})

const cargarEstudiantes = async () => {
  try {
    const response = await estudiantesService.obtenerEstudiantes(0, 1000)
    estudiantes.value = response.content
  } catch (error) {
    console.error('Error loading students:', error)
  }
}

const cargarFactura = async () => {
  if (!isEditing.value) return
  try {
    isLoading.value = true
    const factura = await cobrosService.obtenerFactura(facturaId.value as number)
    Object.assign(form, factura)
  } catch (error) {
    console.error('Error loading factura:', error)
  } finally {
    isLoading.value = false
  }
}

const guardar = async () => {
  try {
    isLoading.value = true
    const fecha = (form.fechaVencimiento as Date).toISOString().split('T')[0]
    if (isEditing.value) {
      await cobrosService.actualizarFactura(facturaId.value as number, {
        id: facturaId.value as number,
        ...form,
        fechaVencimiento: fecha
      })
    } else {
      await cobrosService.crearFactura({
        ...form,
        fechaVencimiento: fecha
      })
    }
    router.push('/cobros')
  } catch (error) {
    console.error('Error saving:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  cargarEstudiantes()
  cargarFactura()
})
</script>
