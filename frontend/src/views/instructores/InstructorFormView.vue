<template>
  <div class="space-y-6 max-w-4xl mx-auto">
    <PageHeader
      :title="isEditing ? 'Editar instructor' : 'Nuevo instructor'"
      :description="isEditing ? 'Actualiza la información del instructor.' : 'Registra un nuevo instructor en la escuela.'"
      icon="pi pi-id-card"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Instructores', to: '/instructores' },
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
        <p class="text-sm font-medium text-danger-600">No se pudo guardar</p>
        <p class="text-xs text-danger-600/80 mt-0.5">{{ errorMessage }}</p>
      </div>
    </div>

    <form @submit.prevent="guardar" class="space-y-6">
      <FormCard title="Datos personales" icon="pi pi-user">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Nombre" required>
            <InputText v-model="form.nombre" placeholder="Juan" class="w-full" required />
          </Field>
          <Field label="Apellido" required>
            <InputText v-model="form.apellido" placeholder="Pérez" class="w-full" required />
          </Field>
          <Field label="Cédula" required hint="10 dígitos, validación Ecuador">
            <InputText v-model="form.cedula" maxlength="10" placeholder="1234567890" class="w-full" :disabled="isEditing" required />
          </Field>
          <Field label="Fecha de nacimiento">
            <Calendar v-model="form.fechaNacimiento" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" :maxDate="new Date()" />
          </Field>
          <Field label="Email" required>
            <InputText v-model="form.email" type="email" placeholder="instructor@correo.com" class="w-full" required />
          </Field>
          <Field label="Teléfono" required hint="10 dígitos, formato 09xxxxxxxx">
            <InputText v-model="form.telefono" maxlength="10" placeholder="0987654321" class="w-full" required />
          </Field>
          <Field label="Dirección" class="md:col-span-2">
            <Textarea v-model="form.direccion" rows="2" placeholder="Calle, número, sector, ciudad" class="w-full" />
          </Field>
        </div>
      </FormCard>

      <FormCard title="Información laboral" icon="pi pi-briefcase">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Fecha de contratación">
            <Calendar v-model="form.fechaContratacion" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
          </Field>
        </div>
      </FormCard>

      <FormCard title="Contrato" icon="pi pi-file-edit">
        <div class="space-y-5">
          <Field label="Tipo de contrato" required>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
              <button
                v-for="opt in tiposContrato"
                :key="opt.value"
                type="button"
                @click="seleccionarTipoContrato(opt.value)"
                :class="['p-4 rounded-lg border-2 text-left transition-all',
                  form.tipoContrato === opt.value
                    ? 'border-brand-600 bg-brand-50'
                    : 'border-ink-200 bg-white hover:border-brand-300']"
              >
                <div class="flex items-center gap-2 mb-1">
                  <i :class="[opt.icon, form.tipoContrato === opt.value ? 'text-brand-700' : 'text-ink-500']" />
                  <span :class="['font-semibold text-sm', form.tipoContrato === opt.value ? 'text-brand-800' : 'text-ink-900']">
                    {{ opt.label }}
                  </span>
                </div>
                <p class="text-xs text-ink-600">{{ opt.desc }}</p>
              </button>
            </div>
          </Field>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <Field label="Horas contratadas por semana" required hint="Máximo 60 horas">
              <InputNumber
                v-model="form.horasContratoSemanales"
                :min="1"
                :max="60"
                suffix=" h"
                class="w-full"
              />
            </Field>

            <Field
              v-if="form.tipoContrato !== 'POR_HORAS'"
              label="Salario mensual (USD)"
              required
            >
              <InputNumber
                v-model="form.salarioMensual"
                mode="currency"
                currency="USD"
                locale="en-US"
                :minFractionDigits="2"
                class="w-full"
              />
            </Field>

            <Field
              v-if="form.tipoContrato === 'POR_HORAS'"
              label="Tarifa por hora (USD)"
              required
              hint="Pago por cada hora trabajada"
            >
              <InputNumber
                v-model="form.tarifaHora"
                mode="currency"
                currency="USD"
                locale="en-US"
                :minFractionDigits="2"
                class="w-full"
              />
            </Field>

            <Field
              v-if="form.tipoContrato !== 'POR_HORAS'"
              label="Tarifa por hora extra (USD)"
              hint="Opcional, para horas adicionales fuera del contrato"
            >
              <InputNumber
                v-model="form.tarifaHora"
                mode="currency"
                currency="USD"
                locale="en-US"
                :minFractionDigits="2"
                class="w-full"
              />
            </Field>
          </div>

          <div class="rounded-lg bg-info-50 border border-info-200 p-3 text-xs text-ink-700 flex items-start gap-2">
            <i class="pi pi-info-circle text-info-600 mt-0.5" />
            <p>
              <strong>{{ tipoContratoSeleccionado?.label }}:</strong>
              {{ tipoContratoSeleccionado?.detalleNomina }}
            </p>
          </div>
        </div>
      </FormCard>

      <FormCard title="Licencia de conducir" icon="pi pi-id-card">
        <div class="space-y-5">
          <div class="rounded-lg bg-info-50 border border-info-200 p-3 text-xs text-ink-700 flex items-start gap-2">
            <i class="pi pi-info-circle text-info-600 mt-0.5" />
            <p>
              En Ecuador el <strong>número de licencia coincide con la cédula</strong> del titular.
              Se asigna automáticamente desde el campo "Cédula" de arriba.
            </p>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <Field label="Número de licencia" hint="Igual a la cédula del instructor">
              <InputText :value="form.cedula || '—'" class="w-full" disabled />
            </Field>
            <Field label="Categoría" required>
              <div class="grid grid-cols-6 gap-2">
                <button
                  v-for="cat in ['A', 'B', 'C', 'D', 'E', 'F']"
                  :key="cat"
                  type="button"
                  @click="form.licenciaCategoria = cat"
                  :class="['h-11 rounded-lg border-2 font-bold transition-all',
                    form.licenciaCategoria === cat
                      ? 'border-brand-600 bg-brand-50 text-brand-700'
                      : 'border-ink-200 bg-white text-ink-600 hover:border-brand-300']"
                >{{ cat }}</button>
              </div>
            </Field>
            <Field label="Fecha de emisión" required>
              <Calendar v-model="form.licenciaEmision" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" required />
            </Field>
            <Field label="Fecha de caducidad" required>
              <Calendar v-model="form.licenciaCaducidad" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" :minDate="form.licenciaEmision || undefined" required />
            </Field>
          </div>
        </div>
      </FormCard>

      <FormCard title="Observaciones" icon="pi pi-comment">
        <Field label="Notas internas" hint="Opcional, máximo 1000 caracteres">
          <Textarea v-model="form.observaciones" rows="3" maxlength="1000" placeholder="Ej: Experiencia en categoría profesional, idiomas, certificaciones adicionales..." class="w-full" />
        </Field>
      </FormCard>

      <div class="flex items-center justify-end gap-3">
        <Button label="Cancelar" outlined type="button" @click="router.back()" />
        <Button :label="isEditing ? 'Guardar cambios' : 'Crear instructor'" icon="pi pi-check" type="submit" :loading="isLoading" />
      </div>
    </form>

    <Toast />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineComponent, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import PageHeader from '@/components/ui/PageHeader.vue'
import FormCard from '@/components/ui/FormCard.vue'
import instructoresService, { type CreateInstructorRequest, type UpdateInstructorRequest, type TipoContrato } from '@/services/instructores'

const router = useRouter()
const route = useRoute()
const toast = useToast()

const Field = defineComponent({
  props: ['label', 'required', 'hint'],
  setup(props, { slots, attrs }) {
    return () =>
      h('div', { ...attrs }, [
        h('label', { class: 'block text-sm font-medium text-ink-700 mb-1.5' }, [
          props.label, props.required && h('span', { class: 'text-danger-500 ml-0.5' }, '*')
        ]),
        slots.default?.(),
        props.hint && h('p', { class: 'text-xs text-ink-500 mt-1' }, props.hint)
      ])
  }
})

const isLoading = ref(false)
const errorMessage = ref('')
const id = computed(() => {
  const v = route.params.id
  return typeof v === 'string' ? parseInt(v) : (v as number | undefined)
})
const isEditing = computed(() => !!id.value)

const form = reactive<{
  nombre: string
  apellido: string
  cedula: string
  email: string
  telefono: string
  direccion: string
  fechaNacimiento: Date | null
  fechaContratacion: Date | null
  salarioMensual: number | null
  tipoContrato: TipoContrato
  horasContratoSemanales: number
  tarifaHora: number | null
  licenciaNumero: string
  licenciaCategoria: string
  licenciaEmision: Date | null
  licenciaCaducidad: Date | null
  observaciones: string
}>({
  nombre: '',
  apellido: '',
  cedula: '',
  email: '',
  telefono: '',
  direccion: '',
  fechaNacimiento: null,
  fechaContratacion: null,
  salarioMensual: 850,
  tipoContrato: 'TIEMPO_COMPLETO',
  horasContratoSemanales: 40,
  tarifaHora: null,
  licenciaNumero: '',
  licenciaCategoria: 'B',
  licenciaEmision: null,
  licenciaCaducidad: null,
  observaciones: ''
})

const tiposContrato: { value: TipoContrato; label: string; icon: string; desc: string; horasDefault: number; detalleNomina: string }[] = [
  {
    value: 'TIEMPO_COMPLETO',
    label: 'Tiempo completo',
    icon: 'pi pi-briefcase',
    desc: '~40 h/semana, salario mensual fijo',
    horasDefault: 40,
    detalleNomina: 'El instructor recibe su salario mensual fijo. Las horas extra (opcional tarifa) se pagan aparte.'
  },
  {
    value: 'MEDIO_TIEMPO',
    label: 'Medio tiempo',
    icon: 'pi pi-clock',
    desc: '~20 h/semana, salario mensual fijo',
    horasDefault: 20,
    detalleNomina: 'El instructor recibe su salario mensual fijo (reducido). Las horas extra (opcional tarifa) se pagan aparte.'
  },
  {
    value: 'POR_HORAS',
    label: 'Por horas',
    icon: 'pi pi-calculator',
    desc: 'Pago variable según horas trabajadas',
    horasDefault: 10,
    detalleNomina: 'El instructor cobra exclusivamente por hora trabajada (tarifa × horas cumplidas). No tiene salario fijo.'
  }
]

const tipoContratoSeleccionado = computed(() => tiposContrato.find(t => t.value === form.tipoContrato))

const seleccionarTipoContrato = (tipo: TipoContrato) => {
  form.tipoContrato = tipo
  const opt = tiposContrato.find(t => t.value === tipo)
  if (opt) form.horasContratoSemanales = opt.horasDefault
  if (tipo === 'POR_HORAS') {
    form.salarioMensual = null
  } else if (form.salarioMensual === null) {
    form.salarioMensual = 850
  }
}

const fmtFecha = (v: any): string | undefined => {
  if (!v) return undefined
  if (typeof v === 'string') return v.substring(0, 10)
  if (v instanceof Date) {
    return `${v.getFullYear()}-${String(v.getMonth() + 1).padStart(2, '0')}-${String(v.getDate()).padStart(2, '0')}`
  }
  return undefined
}

const parseFecha = (v?: string): Date | null => {
  if (!v) return null
  const parts = v.substring(0, 10).split('-')
  if (parts.length !== 3) return null
  return new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
}

const cargar = async () => {
  if (!isEditing.value || !id.value) return
  try {
    const data = await instructoresService.obtenerInstructor(id.value)
    form.nombre = data.nombre || ''
    form.apellido = data.apellido || ''
    form.cedula = data.cedula || ''
    form.email = data.email || ''
    form.telefono = data.telefono || ''
    form.direccion = data.direccion || ''
    form.fechaNacimiento = parseFecha(data.fechaNacimiento)
    form.fechaContratacion = parseFecha(data.fechaContratacion)
    form.salarioMensual = data.salarioMensual ? Number(data.salarioMensual) : null
    form.tipoContrato = data.tipoContrato || 'TIEMPO_COMPLETO'
    form.horasContratoSemanales = data.horasContratoSemanales || 40
    form.tarifaHora = data.tarifaHora ? Number(data.tarifaHora) : null
    form.licenciaNumero = data.licenciaNumero || ''
    form.licenciaCategoria = data.licenciaCategoria || 'B'
    form.licenciaEmision = parseFecha(data.licenciaEmision)
    form.licenciaCaducidad = parseFecha(data.licenciaCaducidad)
    form.observaciones = data.observaciones || ''
  } catch (e: any) {
    errorMessage.value = e.response?.data?.detail || 'No se pudo cargar el instructor'
  }
}

const validar = (): string | null => {
  if (!form.nombre.trim()) return 'El nombre es requerido'
  if (!form.apellido.trim()) return 'El apellido es requerido'
  if (!form.cedula.trim()) return 'La cédula es requerida'
  if (form.cedula.length !== 10) return 'La cédula debe tener 10 dígitos'
  if (!form.email.trim()) return 'El email es requerido'
  if (!form.telefono.trim()) return 'El teléfono es requerido'
  if (form.telefono.length < 7 || form.telefono.length > 10) return 'Teléfono debe tener entre 7 y 10 dígitos'
  if (!form.licenciaCategoria) return 'La categoría de licencia es requerida'
  if (!form.licenciaEmision) return 'La fecha de emisión de licencia es requerida'
  if (!form.licenciaCaducidad) return 'La fecha de caducidad de licencia es requerida'
  if (form.licenciaEmision && form.licenciaCaducidad && form.licenciaCaducidad <= form.licenciaEmision) {
    return 'La fecha de caducidad debe ser posterior a la emisión'
  }
  if (!form.tipoContrato) return 'El tipo de contrato es requerido'
  if (!form.horasContratoSemanales || form.horasContratoSemanales < 1 || form.horasContratoSemanales > 60) {
    return 'Las horas semanales deben estar entre 1 y 60'
  }
  if (form.tipoContrato === 'POR_HORAS') {
    if (!form.tarifaHora || form.tarifaHora <= 0) {
      return 'Para contratos POR_HORAS la tarifa por hora es obligatoria y > 0'
    }
  } else {
    if (!form.salarioMensual || form.salarioMensual <= 0) {
      return 'Para contratos TIEMPO_COMPLETO o MEDIO_TIEMPO el salario mensual es obligatorio'
    }
  }
  if (form.tarifaHora !== null && form.tarifaHora !== undefined && form.tarifaHora <= 0) {
    return 'La tarifa por hora debe ser mayor a 0'
  }
  return null
}

const guardar = async () => {
  errorMessage.value = ''
  const err = validar()
  if (err) {
    errorMessage.value = err
    return
  }
  try {
    isLoading.value = true
    if (isEditing.value && id.value) {
      const payload: UpdateInstructorRequest = {
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        email: form.email.trim(),
        telefono: form.telefono.trim(),
        direccion: form.direccion?.trim() || undefined,
        fechaNacimiento: fmtFecha(form.fechaNacimiento),
        // En Ecuador la licencia = cedula; no se envia si no cambia (el backend lo valida igual)
        licenciaCategoria: form.licenciaCategoria,
        licenciaEmision: fmtFecha(form.licenciaEmision),
        licenciaCaducidad: fmtFecha(form.licenciaCaducidad),
        fechaContratacion: fmtFecha(form.fechaContratacion),
        salarioMensual: form.tipoContrato !== 'POR_HORAS' && form.salarioMensual ? Number(form.salarioMensual) : undefined,
        tipoContrato: form.tipoContrato,
        horasContratoSemanales: form.horasContratoSemanales,
        tarifaHora: form.tarifaHora ? Number(form.tarifaHora) : undefined,
        observaciones: form.observaciones?.trim() || undefined
      }
      await instructoresService.actualizarInstructor(id.value, payload)
      toast.add({ severity: 'success', summary: 'Actualizado', detail: 'Instructor actualizado', life: 2500 })
    } else {
      const payload: CreateInstructorRequest = {
        cedula: form.cedula.trim(),
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        email: form.email.trim(),
        telefono: form.telefono.trim(),
        direccion: form.direccion?.trim() || undefined,
        fechaNacimiento: fmtFecha(form.fechaNacimiento),
        // En Ecuador el numero de licencia = cedula del instructor
        licenciaNumero: form.cedula.trim(),
        licenciaCategoria: form.licenciaCategoria,
        licenciaEmision: fmtFecha(form.licenciaEmision)!,
        licenciaCaducidad: fmtFecha(form.licenciaCaducidad)!,
        fechaContratacion: fmtFecha(form.fechaContratacion),
        salarioMensual: form.tipoContrato !== 'POR_HORAS' && form.salarioMensual ? Number(form.salarioMensual) : undefined,
        tipoContrato: form.tipoContrato,
        horasContratoSemanales: form.horasContratoSemanales,
        tarifaHora: form.tarifaHora ? Number(form.tarifaHora) : undefined,
        observaciones: form.observaciones?.trim() || undefined
      }
      await instructoresService.crearInstructor(payload)
      toast.add({ severity: 'success', summary: 'Creado', detail: 'Instructor registrado correctamente', life: 2500 })
    }
    setTimeout(() => router.push('/instructores'), 1000)
  } catch (e: any) {
    const d = e.response?.data
    errorMessage.value = d?.errors
      ? Object.entries(d.errors).map(([k, v]) => `${k}: ${v}`).join(' · ')
      : (d?.detail || d?.message || 'Error al guardar')
  } finally {
    isLoading.value = false
  }
}

onMounted(cargar)
</script>
