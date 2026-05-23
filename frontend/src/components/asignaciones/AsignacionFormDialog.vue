<template>
  <form @submit.prevent="guardar" class="space-y-4">
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
      <label class="block text-sm font-medium mb-2">Instructor *</label>
      <Dropdown
        v-model="form.instructorId"
        :options="instructores"
        option-label="nombreCompleto"
        option-value="id"
        placeholder="Selecciona un instructor"
        class="w-full"
        required
      />
    </div>

    <div>
      <label class="block text-sm font-medium mb-2">Vehículo *</label>
      <Dropdown
        v-model="form.vehiculoId"
        :options="vehiculos"
        option-label="placa"
        option-value="id"
        placeholder="Selecciona un vehículo"
        class="w-full"
        required
      />
    </div>

    <div>
      <label class="block text-sm font-medium mb-2">Fecha Programada *</label>
      <Calendar
        v-model="form.fechaProgramada"
        date-format="dd/mm/yy"
        :show-icon="true"
        class="w-full"
        required
      />
    </div>

    <div class="grid grid-cols-2 gap-4">
      <div>
        <label class="block text-sm font-medium mb-2">Hora Inicio *</label>
        <InputMask v-model="form.horaInicio" mask="99:99" placeholder="HH:MM" required />
      </div>
      <div>
        <label class="block text-sm font-medium mb-2">Hora Fin *</label>
        <InputMask v-model="form.horaFin" mask="99:99" placeholder="HH:MM" required />
      </div>
    </div>

    <div>
      <label class="block text-sm font-medium mb-2">Tipo de Clase *</label>
      <Dropdown
        v-model="form.tipoClase"
        :options="[
          { label: 'Práctica en Calle', value: 'PRACTICA_CALLE' },
          { label: 'Práctica en Patio', value: 'PRACTICA_PATIO' },
          { label: 'Teoría', value: 'TEORIA' }
        ]"
        option-label="label"
        option-value="value"
        placeholder="Selecciona el tipo de clase"
        class="w-full"
        required
      />
    </div>

    <div v-if="errorMessage" class="p-3 bg-red-50 border border-red-200 rounded-lg">
      <p class="text-sm text-red-700">{{ errorMessage }}</p>
    </div>

    <div class="flex gap-2 justify-end pt-4">
      <Button label="Cancelar" severity="secondary" @click="$emit('close')" />
      <Button label="Guardar" type="submit" :loading="isLoading" />
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import Button from 'primevue/button'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import InputMask from 'primevue/inputmask'
import asignacionesService from '@/services/asignaciones'
import estudiantesService from '@/services/estudiantes'
import instructoresService from '@/services/instructores'
import vehiculosService from '@/services/vehiculos'

const emit = defineEmits<{
  close: []
  save: []
}>()

const isLoading = ref(false)
const errorMessage = ref('')
const estudiantes = ref([])
const instructores = ref([])
const vehiculos = ref([])

const form = reactive({
  estudianteId: null,
  instructorId: null,
  vehiculoId: null,
  fechaProgramada: new Date(),
  horaInicio: '',
  horaFin: '',
  tipoClase: 'PRACTICA_CALLE'
})

const cargarOpciones = async () => {
  try {
    const [est, inst, veh] = await Promise.all([
      estudiantesService.obtenerEstudiantes(0, 100),
      instructoresService.obtenerInstructores(0, 100),
      vehiculosService.obtenerVehiculos(0, 100)
    ])
    estudiantes.value = est.content
    instructores.value = inst.content
    vehiculos.value = veh.content
  } catch (error) {
    console.error('Error loading options:', error)
  }
}

const guardar = async () => {
  if (!form.estudianteId || !form.instructorId || !form.vehiculoId || !form.horaInicio || !form.horaFin) {
    errorMessage.value = 'Por favor completa todos los campos'
    return
  }

  try {
    isLoading.value = true
    errorMessage.value = ''

    const fecha = (form.fechaProgramada as Date).toISOString().split('T')[0]
    
    const disponibilidad = await asignacionesService.verificarDisponibilidad({
      estudianteId: form.estudianteId as number,
      instructorId: form.instructorId as number,
      vehiculoId: form.vehiculoId as number,
      fechaProgramada: fecha,
      horaInicio: form.horaInicio,
      horaFin: form.horaFin,
      tipoClase: form.tipoClase as any
    })

    if (!disponibilidad.disponible) {
      errorMessage.value = `No disponible: ${disponibilidad.conflictos.join(', ')}`
      return
    }

    await asignacionesService.crearAsignacion({
      estudianteId: form.estudianteId as number,
      instructorId: form.instructorId as number,
      vehiculoId: form.vehiculoId as number,
      fechaProgramada: fecha,
      horaInicio: form.horaInicio,
      horaFin: form.horaFin,
      tipoClase: form.tipoClase as any
    })

    emit('save')
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message || 'Error al crear asignación'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  cargarOpciones()
})
</script>
