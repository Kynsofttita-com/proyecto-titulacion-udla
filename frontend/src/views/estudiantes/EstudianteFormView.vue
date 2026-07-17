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

    <div v-if="errorMessage" class="rounded-lg bg-danger-50 border border-danger-500/20 p-4 flex items-start gap-3">
      <i class="pi pi-exclamation-circle text-danger-600 mt-0.5" />
      <div class="flex-1">
        <p class="text-sm font-medium text-danger-600">Error de validación</p>
        <p class="text-xs text-danger-600/80 mt-0.5">{{ errorMessage }}</p>
      </div>
      <button @click="errorMessage = ''" class="text-danger-600 hover:bg-danger-100 w-6 h-6 rounded-md flex items-center justify-center">
        <i class="pi pi-times text-xs" />
      </button>
    </div>

    <form @submit.prevent="guardar" class="space-y-6">
      <!-- Información Personal -->
      <div class="card p-6">
        <h3 class="heading-3 mb-4 flex items-center gap-2">
          <i class="pi pi-user text-brand-600" />
          Información personal
        </h3>
        <p class="text-sm text-ink-600 mb-4">Datos básicos del estudiante. Los campos con * son requeridos.</p>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Nombre *</label>
            <InputText v-model="form.nombre" placeholder="Ej: Juan" class="w-full border border-ink-300 bg-ink-50" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Apellido *</label>
            <InputText v-model="form.apellido" placeholder="Ej: Pérez" class="w-full border border-ink-300 bg-ink-50" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Cédula * <span class="text-xs text-ink-500">(10 dígitos)</span></label>
            <InputText v-model="form.cedula" placeholder="1234567890" maxlength="10" class="w-full border border-ink-300 bg-ink-50" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Fecha de nacimiento *</label>
            <Calendar v-model="form.fechaNacimiento" dateFormat="dd/mm/yy" :showIcon="true" class="w-full" :inputClass="'border border-ink-300 bg-ink-50'" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Género *</label>
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
              class="w-full border border-ink-300 bg-ink-50"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Tipo de sangre</label>
            <Dropdown
              v-model="form.tipoSangre"
              :options="['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-']"
              placeholder="Selecciona"
              class="w-full border border-ink-300 bg-ink-50"
              showClear
            />
          </div>
        </div>
      </div>

      <!-- Información de Contacto -->
      <div class="card p-6">
        <h3 class="heading-3 mb-4 flex items-center gap-2">
          <i class="pi pi-phone text-brand-600" />
          Información de contacto
        </h3>
        <p class="text-sm text-ink-600 mb-4">Información para comunicación y entrega de documentos.</p>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Email *</label>
            <InputText v-model="form.email" type="email" placeholder="estudiante@correo.com" class="w-full border border-ink-300 bg-ink-50" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-ink-700 mb-1.5">Teléfono * <span class="text-xs text-ink-500">(09XXXXXXXX)</span></label>
            <InputText v-model="form.telefono" placeholder="0987654321" maxlength="10" class="w-full border border-ink-300 bg-ink-50" required />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Dirección *</label>
          <Textarea v-model="form.direccion" rows="3" placeholder="Calle, número, sector, ciudad" class="w-full border border-ink-300 bg-ink-50" required />
        </div>
      </div>

      <!-- Plan Académico -->
      <div class="card p-6">
        <h3 class="heading-3 mb-4 flex items-center gap-2">
          <i class="pi pi-graduation-cap text-brand-600" />
          Plan académico
        </h3>
        <p class="text-sm text-ink-600 mb-4">Curso que el estudiante va a tomar. El precio del curso determina cuánto debe pagar.</p>

        <div class="mb-4">
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Tipo de curso *</label>
          <Dropdown
            v-model="form.tipoCursoId"
            :options="tiposCurso"
            optionValue="id"
            optionLabel="nombre"
            placeholder="Selecciona un curso"
            class="w-full border border-ink-300 bg-ink-50"
            :loading="cargandoCursos"
            @change="onTipoCursoChange"
            required
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
          <p class="text-xs text-ink-500 mt-1.5">Cada curso está vinculado a una categoría de licencia (A, B, C, etc.) y tiene un precio base que se usará para calcular los pagos.</p>
        </div>

        <div v-if="cursoSeleccionado" class="rounded-lg border border-brand-200 bg-brand-50 p-4 animate-fade-up">
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
              <p class="text-xs text-ink-600 mt-2 flex items-start gap-1.5">
                <i class="pi pi-tag mt-0.5" />
                <span>Precio base: <strong class="text-brand-700">${{ Number(cursoSeleccionado.precioBase).toFixed(2) }}</strong></span>
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Observaciones -->
      <div class="card p-6">
        <h3 class="heading-3 mb-4 flex items-center gap-2">
          <i class="pi pi-clipboard text-brand-600" />
          Observaciones
        </h3>
        <p class="text-sm text-ink-600 mb-4">Notas adicionales sobre el estudiante.</p>

        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Observaciones</label>
          <Textarea v-model="form.observaciones" rows="3" placeholder="Agregar observaciones..." class="w-full border border-ink-300 bg-ink-50" />
        </div>
      </div>

      <!-- Contactos de Emergencia -->
      <div class="card p-6">
        <h3 class="heading-3 mb-4 flex items-center gap-2">
          <i class="pi pi-users text-brand-600" />
          Contactos de emergencia
        </h3>
        <p class="text-sm text-ink-600 mb-4">Registra los contactos a quién avisar en caso de emergencia.</p>

        <div v-if="editandoContacto.id" class="space-y-4 mb-4 p-4 rounded-lg bg-info-50 border border-info-200">
          <h4 class="text-sm font-semibold text-ink-900">{{ editandoContacto.id === 'nuevo' ? 'Nuevo contacto' : 'Editar contacto' }}</h4>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-ink-700 mb-1.5">Nombre *</label>
              <InputText v-model="formContactoEmergencia.nombre" placeholder="Nombre completo" class="w-full border border-ink-300 bg-ink-50" />
            </div>
            <div>
              <label class="block text-sm font-medium text-ink-700 mb-1.5">Teléfono *</label>
              <InputText v-model="formContactoEmergencia.telefono" placeholder="0987654321" class="w-full border border-ink-300 bg-ink-50" />
            </div>
            <div>
              <label class="block text-sm font-medium text-ink-700 mb-1.5">Parentesco</label>
              <InputText v-model="formContactoEmergencia.parentesco" placeholder="Ej: Padre, Hermano..." class="w-full border border-ink-300 bg-ink-50" />
            </div>
            <div class="flex items-end">
              <label class="flex items-center gap-2 text-sm font-medium text-ink-700">
                <Checkbox v-model="formContactoEmergencia.esPrincipal" :binary="true" />
                Es principal
              </label>
            </div>
          </div>
          <div class="flex gap-2">
            <Button label="Guardar" icon="pi pi-check" size="small" @click="guardarContactoEmergencia()" :loading="guardandoContacto" />
            <Button label="Cancelar" icon="pi pi-times" severity="secondary" outlined size="small" @click="cancelarContactoEmergencia()" />
          </div>
        </div>

        <div v-if="contactosEmergencia && contactosEmergencia.length > 0" class="space-y-3 mb-4">
          <div
            v-for="contacto in contactosEmergencia"
            :key="contacto.id"
            class="p-3 rounded-lg bg-ink-50 border border-ink-200 flex items-start justify-between gap-2"
          >
            <div class="flex-1">
              <p class="text-sm font-medium text-ink-900">{{ contacto.nombre }}</p>
              <p class="text-xs text-ink-600">{{ contacto.parentesco || 'Parentesco no especificado' }}</p>
              <p class="text-xs text-ink-600 mt-1">{{ contacto.telefono }}</p>
              <span v-if="contacto.esPrincipal" class="inline-block mt-1.5 px-2 py-1 bg-brand-50 text-brand-700 text-xs font-medium rounded">
                Principal
              </span>
            </div>
            <div class="flex gap-1 flex-shrink-0">
              <Button
                icon="pi pi-pencil"
                severity="info"
                text
                size="small"
                @click="editarContacto(contacto)"
                v-tooltip="'Editar'"
              />
              <Button
                icon="pi pi-trash"
                severity="danger"
                text
                size="small"
                @click="eliminarContacto(contacto.id)"
                v-tooltip="'Eliminar'"
              />
            </div>
          </div>
        </div>
        <div v-else-if="!editandoContacto.id" class="text-sm text-ink-500 italic mb-4">Sin contactos de emergencia registrados</div>

        <Button
          v-if="!editandoContacto.id"
          label="Agregar contacto"
          icon="pi pi-plus"
          severity="success"
          text
          size="small"
          @click="agregarContacto()"
        />
      </div>

      <!-- Botones de acción -->
      <div class="flex items-center justify-end gap-3">
        <Button label="Cancelar" severity="secondary" outlined @click="router.back()" type="button" />
        <Button :label="isEditing ? 'Guardar cambios' : 'Matricular estudiante'" icon="pi pi-check" @click="guardar" :loading="isLoading" />
      </div>
    </form>

    <Toast />
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
import Toast from 'primevue/toast'
import Checkbox from 'primevue/checkbox'
import { useToast } from 'primevue/usetoast'
import PageHeader from '@/components/ui/PageHeader.vue'
import estudiantesService, { type CreateEstudianteRequest } from '@/services/estudiantes'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()
const toast = useToast()

const estudianteId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : (id as number | undefined)
})
const isEditing = computed(() => !!estudianteId.value)

const form = reactive<CreateEstudianteRequest & { fechaNacimiento: string | Date | null; observaciones?: string }>({
  nombre: '',
  apellido: '',
  email: '',
  cedula: '',
  fechaNacimiento: null,
  genero: '',
  telefono: '',
  direccion: '',
  tipoSangre: undefined,
  tipoCursoId: null,
  categoriaLicenciaId: null,
  observaciones: ''
})

const tiposCurso = ref<any[]>([])
const cargandoCursos = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

const cursoSeleccionado = computed(() =>
  tiposCurso.value.find(c => c.id === form.tipoCursoId) || null
)

const contactosEmergencia = ref<any[]>([])
const editandoContacto = reactive<any>({ id: null })
const formContactoEmergencia = reactive<any>({ nombre: '', telefono: '', parentesco: '', esPrincipal: false })
const guardandoContacto = ref(false)

const cargarTiposCurso = async () => {
  cargandoCursos.value = true
  try {
    const { data } = await api.get('/tipos-curso', { params: { size: 100 } })
    tiposCurso.value = (data.content || []).filter((c: any) => c.activo !== false)
  } catch (e) {
    console.warn('No se pudieron cargar tipos de curso', e)
  } finally {
    cargandoCursos.value = false
  }
}

const onTipoCursoChange = () => {
  const sel = cursoSeleccionado.value
  form.categoriaLicenciaId = sel?.categoriaLicenciaId ?? null
}

const cargarEstudiante = async () => {
  if (!isEditing.value) return
  try {
    isLoading.value = true
    const estudiante = await estudiantesService.obtenerEstudiante(estudianteId.value as number)
    Object.assign(form, {
      nombre: estudiante.nombre,
      apellido: estudiante.apellido,
      email: estudiante.email,
      cedula: estudiante.cedula,
      fechaNacimiento: estudiante.fechaNacimiento ? new Date(estudiante.fechaNacimiento) : null,
      genero: estudiante.genero,
      telefono: estudiante.telefono,
      direccion: estudiante.direccion,
      tipoSangre: estudiante.tipoSangre,
      tipoCursoId: estudiante.tipoCursoId,
      categoriaLicenciaId: estudiante.categoriaLicenciaId,
      observaciones: estudiante.observaciones || ''
    })
  } catch (e: any) {
    errorMessage.value = e.response?.data?.detail || 'No se pudo cargar el estudiante'
  } finally {
    isLoading.value = false
  }
}

const validar = (): boolean => {
  errorMessage.value = ''

  if (!form.nombre?.trim()) {
    errorMessage.value = 'El nombre es requerido'
    return false
  }
  if (!form.apellido?.trim()) {
    errorMessage.value = 'El apellido es requerido'
    return false
  }
  if (!form.cedula?.trim() || form.cedula.length !== 10) {
    errorMessage.value = 'La cédula debe tener 10 dígitos'
    return false
  }
  if (!form.email?.trim()) {
    errorMessage.value = 'El email es requerido'
    return false
  }
  if (!form.telefono?.trim() || form.telefono.length !== 10) {
    errorMessage.value = 'El teléfono debe tener 10 dígitos'
    return false
  }
  if (!form.direccion?.trim()) {
    errorMessage.value = 'La dirección es requerida'
    return false
  }
  if (!form.fechaNacimiento) {
    errorMessage.value = 'La fecha de nacimiento es requerida'
    return false
  }
  if (!form.genero) {
    errorMessage.value = 'El género es requerido'
    return false
  }
  if (!form.tipoCursoId) {
    errorMessage.value = 'El tipo de curso es requerido'
    return false
  }

  return true
}

const guardar = async () => {
  if (!validar()) return

  isLoading.value = true
  try {
    const payload: any = {
      nombre: form.nombre.trim(),
      apellido: form.apellido.trim(),
      cedula: form.cedula.trim(),
      email: form.email.trim(),
      telefono: form.telefono.trim(),
      direccion: form.direccion.trim(),
      fechaNacimiento: form.fechaNacimiento
        ? form.fechaNacimiento instanceof Date
          ? form.fechaNacimiento.toISOString().split('T')[0]
          : form.fechaNacimiento
        : undefined,
      genero: form.genero,
      tipoSangre: form.tipoSangre || undefined,
      tipoCursoId: form.tipoCursoId || undefined,
      categoriaLicenciaId: form.categoriaLicenciaId || undefined,
      observaciones: form.observaciones?.trim() || undefined
    }

    if (isEditing.value) {
      await estudiantesService.actualizarEstudiante(estudianteId.value as number, payload)
      toast.add({
        severity: 'success',
        summary: 'Actualizado',
        detail: 'Los datos del estudiante se han actualizado',
        life: 3000
      })
    } else {
      // Al crear, incluir contactos de emergencia acumulados localmente
      // (el backend acepta contactosEmergencia en CreateEstudianteRequest)
      if (contactosEmergencia.value.length > 0) {
        payload.contactosEmergencia = contactosEmergencia.value.map(c => ({
          nombre: c.nombre,
          telefono: c.telefono,
          parentesco: c.parentesco || undefined,
          esPrincipal: c.esPrincipal || false
        }))
      }
      await estudiantesService.crearEstudiante(payload)
      toast.add({
        severity: 'success',
        summary: 'Matriculado',
        detail: 'El estudiante se ha matriculado correctamente',
        life: 3000
      })
    }

    setTimeout(() => router.push('/estudiantes'), 1500)
  } catch (e: any) {
    errorMessage.value = e.response?.data?.detail || 'No se pudo guardar el estudiante'
  } finally {
    isLoading.value = false
  }
}

const cargarContactosEmergencia = async () => {
  if (!isEditing.value) return
  try {
    contactosEmergencia.value = await estudiantesService.listarContactosEmergencia(estudianteId.value as number)
  } catch (e) {
    console.warn('No se pudieron cargar contactos', e)
  }
}

const agregarContacto = () => {
  editandoContacto.id = 'nuevo'
  Object.assign(formContactoEmergencia, {
    nombre: '',
    telefono: '',
    parentesco: '',
    esPrincipal: false
  })
}

const editarContacto = (contacto: any) => {
  editandoContacto.id = contacto.id
  Object.assign(formContactoEmergencia, {
    nombre: contacto.nombre,
    telefono: contacto.telefono,
    parentesco: contacto.parentesco || '',
    esPrincipal: contacto.esPrincipal || false
  })
}

const cancelarContactoEmergencia = () => {
  editandoContacto.id = null
  Object.assign(formContactoEmergencia, {
    nombre: '',
    telefono: '',
    parentesco: '',
    esPrincipal: false
  })
}

const guardarContactoEmergencia = async () => {
  if (!formContactoEmergencia.nombre?.trim() || !formContactoEmergencia.telefono?.trim()) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'Nombre y teléfono son requeridos',
      life: 3000
    })
    return
  }

  const datosContacto = {
    nombre: formContactoEmergencia.nombre.trim(),
    telefono: formContactoEmergencia.telefono.trim(),
    parentesco: formContactoEmergencia.parentesco?.trim() || undefined,
    esPrincipal: formContactoEmergencia.esPrincipal
  }

  // Modo CREAR: buffer local (se envia al matricular estudiante)
  if (!isEditing.value) {
    if (editandoContacto.id === 'nuevo') {
      // ID temporal negativo para distinguir de los del backend
      const tempId = -Date.now()
      contactosEmergencia.value.push({ id: tempId, ...datosContacto })
      toast.add({
        severity: 'success',
        summary: 'Agregado',
        detail: 'Contacto de emergencia agregado (se guardará al matricular)',
        life: 3000
      })
    } else {
      const idx = contactosEmergencia.value.findIndex(c => c.id === editandoContacto.id)
      if (idx !== -1) {
        contactosEmergencia.value[idx] = { id: editandoContacto.id, ...datosContacto }
      }
      toast.add({
        severity: 'success',
        summary: 'Actualizado',
        detail: 'Contacto de emergencia actualizado',
        life: 3000
      })
    }
    cancelarContactoEmergencia()
    return
  }

  // Modo EDITAR: llamada API real
  guardandoContacto.value = true
  try {
    if (editandoContacto.id === 'nuevo') {
      await estudiantesService.crearContactoEmergencia(estudianteId.value as number, datosContacto)
      toast.add({
        severity: 'success',
        summary: 'Agregado',
        detail: 'Contacto de emergencia agregado',
        life: 3000
      })
    } else {
      await estudiantesService.actualizarContactoEmergencia(estudianteId.value as number, editandoContacto.id, datosContacto)
      toast.add({
        severity: 'success',
        summary: 'Actualizado',
        detail: 'Contacto de emergencia actualizado',
        life: 3000
      })
    }
    cancelarContactoEmergencia()
    cargarContactosEmergencia()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo guardar',
      life: 3000
    })
  } finally {
    guardandoContacto.value = false
  }
}

const eliminarContacto = async (contactoId: number) => {
  // Modo CREAR: quitar del array local
  if (!isEditing.value) {
    contactosEmergencia.value = contactosEmergencia.value.filter(c => c.id !== contactoId)
    toast.add({
      severity: 'success',
      summary: 'Eliminado',
      detail: 'Contacto de emergencia eliminado',
      life: 3000
    })
    return
  }

  // Modo EDITAR: llamada API real
  try {
    await estudiantesService.eliminarContactoEmergencia(estudianteId.value as number, contactoId)
    toast.add({
      severity: 'success',
      summary: 'Eliminado',
      detail: 'Contacto de emergencia eliminado',
      life: 3000
    })
    cargarContactosEmergencia()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo eliminar',
      life: 3000
    })
  }
}

onMounted(() => {
  cargarTiposCurso()
  cargarEstudiante()
  cargarContactosEmergencia()
})
</script>
