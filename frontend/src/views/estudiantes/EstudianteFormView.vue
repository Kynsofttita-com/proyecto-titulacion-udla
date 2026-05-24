<template>
  <div class="space-y-6 max-w-4xl mx-auto">
    <PageHeader
      :title="isEditing ? 'Editar estudiante' : 'Nuevo estudiante'"
      :description="isEditing ? 'Actualiza la información del estudiante.' : 'Completa los datos para matricular un estudiante en la escuela.'"
      icon="pi pi-user-plus"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Estudiantes', to: '/estudiantes' },
        { label: isEditing ? 'Editar' : 'Nuevo' }
      ]"
    >
      <template #actions>
        <Button label="Cancelar" outlined @click="router.back()" />
      </template>
    </PageHeader>

    <div v-if="errorMessage" class="rounded-lg bg-danger-50 border border-danger-500/20 p-4 flex items-start gap-3 animate-fade-up">
      <i class="pi pi-exclamation-circle text-danger-600 mt-0.5" />
      <div class="flex-1">
        <p class="text-sm font-medium text-danger-600">Validación fallida</p>
        <p class="text-xs text-danger-600/80 mt-0.5">{{ errorMessage }}</p>
      </div>
      <button @click="errorMessage = ''" class="text-danger-600 hover:bg-danger-100 w-6 h-6 rounded-md flex items-center justify-center">
        <i class="pi pi-times text-xs" />
      </button>
    </div>

    <form @submit.prevent="guardar" class="space-y-6">
      <FormCard
        title="Información personal"
        description="Datos básicos del estudiante. Los campos con * son requeridos."
        icon="pi pi-user"
      >
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Nombre" required>
            <InputText v-model="form.nombre" class="w-full" placeholder="Ej: Juan" required />
          </Field>
          <Field label="Apellido" required>
            <InputText v-model="form.apellido" class="w-full" placeholder="Ej: Pérez" required />
          </Field>
          <Field label="Cédula" required hint="10 dígitos, validación Ecuador">
            <InputText v-model="form.cedula" class="w-full" placeholder="1234567890" maxlength="10" required />
          </Field>
          <Field label="Fecha de nacimiento" required>
            <Calendar v-model="form.fechaNacimiento" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" required />
          </Field>
          <Field label="Género" required>
            <Dropdown
              v-model="form.genero"
              :options="[
                { label: 'Masculino', value: 'M' },
                { label: 'Femenino', value: 'F' },
                { label: 'Otro', value: 'O' }
              ]"
              option-label="label"
              option-value="value"
              placeholder="Selecciona"
              class="w-full"
              required
            />
          </Field>
          <Field label="Tipo de sangre">
            <Dropdown
              v-model="form.tipoSangre"
              :options="['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-']"
              placeholder="Selecciona"
              class="w-full"
              showClear
            />
          </Field>
        </div>
      </FormCard>

      <FormCard
        title="Contacto y dirección"
        description="Información para comunicación y entrega de documentos."
        icon="pi pi-phone"
      >
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Email" required>
            <span class="p-input-icon-left w-full">
              <i class="pi pi-envelope text-ink-400" />
              <InputText v-model="form.email" type="email" placeholder="estudiante@correo.com" class="w-full !pl-10" required />
            </span>
          </Field>
          <Field label="Teléfono" required hint="Formato Ecuador: 09XXXXXXXX">
            <span class="p-input-icon-left w-full">
              <i class="pi pi-phone text-ink-400" />
              <InputText v-model="form.telefono" placeholder="0987654321" maxlength="10" class="w-full !pl-10" required />
            </span>
          </Field>
          <Field label="Dirección" required class="md:col-span-2">
            <Textarea v-model="form.direccion" rows="3" class="w-full" placeholder="Calle, número, sector, ciudad" required />
          </Field>
        </div>
      </FormCard>

      <FormCard
        title="Plan académico"
        description="Curso que el estudiante va a tomar. El precio del curso determina cuánto debe pagar."
        icon="pi pi-graduation-cap"
      >
        <Field label="Tipo de curso" hint="Cada curso está vinculado a una categoría de licencia (A, B, C, etc.) y tiene un precio base que se usará para calcular los pagos.">
          <Dropdown
            v-model="form.tipoCursoId"
            :options="tiposCurso"
            optionValue="id"
            optionLabel="nombre"
            placeholder="Selecciona un curso"
            class="w-full"
            :loading="cargandoCursos"
            showClear
            @change="onTipoCursoChange"
          >
            <template #value="{ value, placeholder }">
              <span v-if="!value" class="text-ink-500">{{ placeholder }}</span>
              <span v-else class="text-sm">
                <span class="font-medium">{{ cursoSeleccionado?.nombre }}</span>
                <span class="text-ink-500"> · Cat. {{ cursoSeleccionado?.categoriaLicenciaCodigo }} · {{ cursoSeleccionado?.duracionTotalHoras }}h ·</span>
                <span class="font-semibold text-brand-700 ml-1">${{ Number(cursoSeleccionado?.precioBase || 0).toFixed(2) }}</span>
              </span>
            </template>
            <template #option="{ option }">
              <div class="flex items-center justify-between gap-3 w-full py-1">
                <div class="flex-1">
                  <p class="text-sm font-medium text-ink-900">{{ option.nombre }}</p>
                  <p class="text-[11px] text-ink-500">
                    Cat. <span class="font-semibold">{{ option.categoriaLicenciaCodigo }}</span>
                    · {{ option.duracionTotalHoras }} h
                  </p>
                </div>
                <span class="text-sm font-semibold text-brand-700">${{ Number(option.precioBase).toFixed(2) }}</span>
              </div>
            </template>
            <template #empty>
              <p class="px-3 py-2 text-sm text-ink-500">No hay tipos de curso configurados. Crea uno en Configuración → Catálogos.</p>
            </template>
          </Dropdown>
        </Field>

        <div v-if="cursoSeleccionado" class="mt-4 rounded-xl border border-brand-200 bg-brand-50/40 p-4 animate-fade-up">
          <div class="flex items-start gap-3">
            <div class="w-10 h-10 rounded-lg bg-brand-600 text-white flex items-center justify-center flex-shrink-0">
              <i class="pi pi-graduation-cap" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-bold text-ink-900">{{ cursoSeleccionado.nombre }}</p>
              <p class="text-xs text-ink-600 mt-0.5">
                Licencia categoría <strong>{{ cursoSeleccionado.categoriaLicenciaCodigo }}</strong>
                · {{ cursoSeleccionado.duracionTotalHoras }} horas de instrucción
              </p>
              <p class="text-xs text-ink-500 mt-2 flex items-start gap-1.5">
                <i class="pi pi-info-circle mt-0.5" />
                <span>Al matricular, el sistema te permitirá emitir facturas que sumen hasta <strong class="text-brand-700">${{ Number(cursoSeleccionado.precioBase).toFixed(2) }}</strong>. Puedes facturar de una vez o en partes.</span>
              </p>
            </div>
          </div>
        </div>
      </FormCard>

      <div class="flex items-center justify-end gap-3">
        <Button label="Cancelar" outlined @click="router.back()" type="button" />
        <Button :label="isEditing ? 'Guardar cambios' : 'Matricular estudiante'" icon="pi pi-check" type="submit" :loading="isLoading" />
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, defineComponent, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import PageHeader from '@/components/ui/PageHeader.vue'
import FormCard from '@/components/ui/FormCard.vue'
import estudiantesService, { type CreateEstudianteRequest } from '@/services/estudiantes'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()

const Field = defineComponent({
  props: ['label', 'required', 'hint'],
  setup(props, { slots, attrs }) {
    return () =>
      h('div', { ...attrs }, [
        h('label', { class: 'block text-sm font-medium text-ink-700 mb-1.5' }, [
          props.label,
          props.required && h('span', { class: 'text-danger-500 ml-0.5' }, '*')
        ]),
        slots.default?.(),
        props.hint && h('p', { class: 'text-xs text-ink-500 mt-1' }, props.hint)
      ])
  }
})

const isLoading = ref(false)
const errorMessage = ref('')
const estudianteId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : (id as number | undefined)
})
const isEditing = computed(() => !!estudianteId.value)

const form = reactive<CreateEstudianteRequest & { fechaNacimiento: string | Date }>({
  nombre: '', apellido: '', email: '', cedula: '',
  fechaNacimiento: '', genero: '', telefono: '', direccion: '',
  tipoSangre: undefined,
  tipoCursoId: null,
  categoriaLicenciaId: null
})

const tiposCurso = ref<any[]>([])
const cargandoCursos = ref(false)
const cursoSeleccionado = computed(() =>
  tiposCurso.value.find(c => c.id === form.tipoCursoId) || null
)

const cargarTiposCurso = async () => {
  cargandoCursos.value = true
  try {
    const { data } = await api.get('/tipos-curso', { params: { size: 100 } })
    tiposCurso.value = (data.content || []).filter((c: any) => c.activo !== false)
  } catch (e) {
    console.warn('No se pudieron cargar tipos de curso', e)
  } finally { cargandoCursos.value = false }
}

const onTipoCursoChange = () => {
  // Al cambiar el tipo de curso, auto-setea categoriaLicenciaId desde la
  // relacion del catalogo (no pedimos al usuario que la elija aparte).
  const sel = cursoSeleccionado.value
  form.categoriaLicenciaId = sel?.categoriaLicenciaId ?? null
}

const cargarEstudiante = async () => {
  if (!isEditing.value) return
  try {
    isLoading.value = true
    const estudiante = await estudiantesService.obtenerEstudiante(estudianteId.value as number)
    Object.assign(form, estudiante)
  } catch (e: any) {
    errorMessage.value = e.response?.data?.detail || 'No se pudo cargar el estudiante'
  } finally { isLoading.value = false }
}

const formatFecha = (v: string | Date): string => {
  if (!v) return ''
  if (typeof v === 'string') return v.substring(0, 10)
  const d = v as Date
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const guardar = async () => {
  errorMessage.value = ''
  try {
    isLoading.value = true
    const payload: CreateEstudianteRequest = {
      nombre: form.nombre.trim(),
      apellido: form.apellido.trim(),
      email: form.email.trim(),
      cedula: form.cedula.trim(),
      fechaNacimiento: formatFecha(form.fechaNacimiento),
      genero: form.genero,
      telefono: form.telefono.trim(),
      direccion: form.direccion.trim(),
      tipoSangre: form.tipoSangre || undefined,
      tipoCursoId: form.tipoCursoId || null,
      categoriaLicenciaId: form.categoriaLicenciaId || null
    }
    if (isEditing.value) await estudiantesService.actualizarEstudiante(estudianteId.value as number, payload)
    else await estudiantesService.crearEstudiante(payload)
    router.push('/estudiantes')
  } catch (e: any) {
    const data = e.response?.data
    if (data?.errors) {
      errorMessage.value = `${data.title || 'Validación'} → ${Object.entries(data.errors).map(([k, v]) => `${k}: ${v}`).join(' · ')}`
    } else {
      errorMessage.value = data?.detail || data?.message || 'Error al guardar el estudiante'
    }
  } finally { isLoading.value = false }
}

onMounted(async () => {
  await cargarTiposCurso()
  await cargarEstudiante()
})
</script>
