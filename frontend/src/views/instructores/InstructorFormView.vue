<template>
  <div class="space-y-6">
    <div class="flex items-center gap-4">
      <Button icon="pi pi-arrow-left" text severity="secondary" @click="router.back()" />
      <h2 class="text-3xl font-bold">{{ isEditing ? 'Editar Instructor' : 'Nuevo Instructor' }}</h2>
    </div>

    <div class="p-6 bg-white rounded-lg shadow">
      <form @submit.prevent="guardar" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="block text-sm font-medium mb-2">Nombre Completo *</label>
            <InputText v-model="form.nombreCompleto" class="w-full" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Email *</label>
            <InputText v-model="form.email" type="email" class="w-full" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Cédula *</label>
            <InputText v-model="form.cédula" class="w-full" placeholder="0123456789" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Licencia de Conducir *</label>
            <InputText v-model="form.licenciaConducir" class="w-full" placeholder="ABC123456" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Especialidad *</label>
            <Dropdown
              v-model="form.especialidad"
              :options="[
                { label: 'Prácticas (Calle)', value: 'PRACTICAS' },
                { label: 'Seguridad Vial', value: 'SEGURIDAD' },
                { label: 'Conducción Defensiva', value: 'DEFENSIVA' },
                { label: 'Mecánica Básica', value: 'MECANICA' }
              ]"
              option-label="label"
              option-value="value"
              class="w-full"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Teléfono *</label>
            <InputText v-model="form.teléfono" class="w-full" placeholder="+593 9 XXXX XXXX" required />
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
import Dropdown from 'primevue/dropdown'
import instructoresService from '@/services/instructores'

const router = useRouter()
const route = useRoute()

const isLoading = ref(false)
const instructorId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : id
})

const isEditing = computed(() => !!instructorId.value)

const form = reactive({
  nombreCompleto: '',
  email: '',
  cédula: '',
  licenciaConducir: '',
  especialidad: '',
  teléfono: ''
})

const cargarInstructor = async () => {
  if (!isEditing.value) return
  try {
    isLoading.value = true
    const instructor = await instructoresService.obtenerInstructor(instructorId.value as number)
    Object.assign(form, instructor)
  } catch (error) {
    console.error('Error loading instructor:', error)
  } finally {
    isLoading.value = false
  }
}

const guardar = async () => {
  try {
    isLoading.value = true
    if (isEditing.value) {
      await instructoresService.actualizarInstructor(instructorId.value as number, {
        id: instructorId.value as number,
        ...form
      })
    } else {
      await instructoresService.crearInstructor(form)
    }
    router.push('/instructores')
  } catch (error) {
    console.error('Error saving:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  cargarInstructor()
})
</script>
