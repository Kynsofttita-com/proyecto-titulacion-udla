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
        filter
        required
      />
      <p v-if="categoriaCodigoEstudiante" class="text-xs text-gray-500 mt-1">
        Categoría requerida: <strong>{{ categoriaCodigoEstudiante }}</strong>
      </p>
      <p v-else-if="form.estudianteId" class="text-xs text-amber-600 mt-1">
        Este estudiante no tiene categoría de licencia asignada. Se mostrarán todos los instructores y vehículos.
      </p>
    </div>

    <div>
      <label class="block text-sm font-medium mb-2">Instructor *</label>
      <Dropdown
        v-model="form.instructorId"
        :options="instructoresFiltrados"
        option-label="nombreCompleto"
        option-value="id"
        :placeholder="placeholderInstructor"
        class="w-full"
        :disabled="!form.estudianteId"
        filter
        required
      />
      <p v-if="form.estudianteId && categoriaCodigoEstudiante && !instructoresFiltrados.length" class="text-xs text-amber-600 mt-1">
        No hay instructores con categoría {{ categoriaCodigoEstudiante }}.
      </p>
    </div>

    <div>
      <label class="block text-sm font-medium mb-2">Vehículo *</label>
      <Dropdown
        v-model="form.vehiculoId"
        :options="vehiculosFiltrados"
        option-label="placa"
        option-value="id"
        :placeholder="placeholderVehiculo"
        class="w-full"
        :disabled="!form.estudianteId"
        filter
        required
      />
      <p v-if="form.estudianteId && categoriaCodigoEstudiante && !vehiculosFiltrados.length" class="text-xs text-amber-600 mt-1">
        No hay vehículos con categoría {{ categoriaCodigoEstudiante }}.
      </p>
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
import { ref, reactive, computed, watch, onMounted } from 'vue'
import Button from 'primevue/button'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import InputMask from 'primevue/inputmask'
import asignacionesService from '@/services/asignaciones'
import estudiantesService, { type EstudianteResponse } from '@/services/estudiantes'
import instructoresService, { type InstructorListResponse } from '@/services/instructores'
import vehiculosService, { type VehiculoListResponse, type CategoriaLicenciaResponse } from '@/services/vehiculos'

const emit = defineEmits<{
  close: []
  save: []
}>()

const isLoading = ref(false)
const errorMessage = ref('')
const estudiantes = ref<EstudianteResponse[]>([])
const instructores = ref<InstructorListResponse[]>([])
const vehiculos = ref<VehiculoListResponse[]>([])
const categorias = ref<CategoriaLicenciaResponse[]>([])

const form = reactive({
  estudianteId: null as number | null,
  instructorId: null as number | null,
  vehiculoId: null as number | null,
  fechaProgramada: new Date(),
  horaInicio: '',
  horaFin: '',
  tipoClase: 'PRACTICA_CALLE' as 'PRACTICA_CALLE' | 'PRACTICA_PATIO' | 'TEORIA'
})

const estudianteSeleccionado = computed(() =>
  estudiantes.value.find(e => e.id === form.estudianteId) || null
)

const categoriaIdEstudiante = computed(() =>
  estudianteSeleccionado.value?.categoriaLicenciaId ?? null
)

const categoriaCodigoEstudiante = computed(() => {
  const id = categoriaIdEstudiante.value
  if (id == null) return null
  return categorias.value.find(c => c.id === id)?.codigo ?? null
})

const instructoresConNombre = computed(() =>
  instructores.value.map(i => ({ ...i, nombreCompleto: `${i.nombre} ${i.apellido}` }))
)

const instructoresFiltrados = computed(() => {
  const codigo = categoriaCodigoEstudiante.value
  if (!codigo) return instructoresConNombre.value
  return instructoresConNombre.value.filter(i => i.licenciaCategoria === codigo)
})

const vehiculosFiltrados = computed(() => {
  const id = categoriaIdEstudiante.value
  if (id == null) return vehiculos.value
  return vehiculos.value.filter(v => v.categoriaLicenciaId === id)
})

const placeholderInstructor = computed(() => {
  if (!form.estudianteId) return 'Selecciona primero un estudiante'
  if (categoriaCodigoEstudiante.value && !instructoresFiltrados.value.length) return 'Sin instructores para esta categoría'
  return 'Selecciona un instructor'
})

const placeholderVehiculo = computed(() => {
  if (!form.estudianteId) return 'Selecciona primero un estudiante'
  if (categoriaCodigoEstudiante.value && !vehiculosFiltrados.value.length) return 'Sin vehículos para esta categoría'
  return 'Selecciona un vehículo'
})

watch(() => form.estudianteId, () => {
  if (form.instructorId && !instructoresFiltrados.value.some(i => i.id === form.instructorId)) {
    form.instructorId = null
  }
  if (form.vehiculoId && !vehiculosFiltrados.value.some(v => v.id === form.vehiculoId)) {
    form.vehiculoId = null
  }
})

const cargarOpciones = async () => {
  try {
    const [est, inst, veh, cat] = await Promise.all([
      estudiantesService.obtenerEstudiantes(0, 100),
      instructoresService.obtenerInstructores(0, 100),
      vehiculosService.obtenerVehiculos(0, 100),
      vehiculosService.listarCategoriasLicencia(true)
    ])
    estudiantes.value = est.content
    instructores.value = inst.content
    vehiculos.value = veh.content
    categorias.value = cat
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
