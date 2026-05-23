<template>
  <div class="space-y-6">
    <div class="flex items-center gap-4">
      <Button icon="pi pi-arrow-left" text severity="secondary" @click="router.back()" />
      <h2 class="text-3xl font-bold">{{ isEditing ? 'Editar Estudiante' : 'Nuevo Estudiante' }}</h2>
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
            <label class="block text-sm font-medium mb-2">Fecha de Nacimiento *</label>
            <Calendar v-model="form.fechaNacimiento" date-format="dd/mm/yy" class="w-full" required />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Género *</label>
            <Dropdown
              v-model="form.género"
              :options="[
                { label: 'Masculino', value: 'M' },
                { label: 'Femenino', value: 'F' },
                { label: 'Otro', value: 'O' }
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
          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-2">Dirección *</label>
            <Textarea v-model="form.dirección" rows="3" class="w-full" required />
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
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import estudiantesService from '@/services/estudiantes'

const router = useRouter()
const route = useRoute()

const isLoading = ref(false)
const estudianteId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : id
})

const isEditing = computed(() => !!estudianteId.value)

const form = reactive({
  nombreCompleto: '',
  email: '',
  cédula: '',
  fechaNacimiento: '',
  género: '',
  teléfono: '',
  dirección: ''
})

const cargarEstudiante = async () => {
  if (!isEditing.value) return
  try {
    isLoading.value = true
    const estudiante = await estudiantesService.obtenerEstudiante(estudianteId.value as number)
    Object.assign(form, estudiante)
  } catch (error) {
    console.error('Error loading student:', error)
  } finally {
    isLoading.value = false
  }
}

const guardar = async () => {
  try {
    isLoading.value = true
    if (isEditing.value) {
      await estudiantesService.actualizarEstudiante(estudianteId.value as number, {
        id: estudianteId.value as number,
        ...form
      })
    } else {
      await estudiantesService.crearEstudiante(form)
    }
    router.push('/estudiantes')
  } catch (error) {
    console.error('Error saving:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  cargarEstudiante()
})
</script>
