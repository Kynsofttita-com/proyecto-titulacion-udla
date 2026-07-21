<template>
  <div class="space-y-6 max-w-4xl mx-auto">
    <PageHeader
      :title="isEditing ? 'Editar vehículo' : 'Nuevo vehículo'"
      :description="isEditing ? 'Actualiza los datos del vehículo.' : 'Registra un vehículo en la flota de la escuela.'"
      icon="pi pi-car"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Vehículos', to: '/vehiculos' },
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

    <form @submit.prevent="guardar" class="space-y-6" novalidate>
      <div class="rounded-lg bg-info-50 border border-info-200 px-4 py-2.5 flex items-center gap-2">
        <i class="pi pi-info-circle text-info-600" />
        <p class="text-sm text-ink-700">
          Los campos marcados con <span class="text-danger-600 font-semibold">*</span> son obligatorios.
        </p>
      </div>

      <FormCard title="Identificación" icon="pi pi-tag">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Placa" required :error="errors.placa" for="field-placa" hint="Formato: ABC-1234 o AB-1234A">
            <InputText
              id="field-placa"
              v-model="form.placa"
              @input="form.placa = form.placa.toUpperCase(); clearError('placa')"
              placeholder="ABC-1234"
              maxlength="8"
              class="w-full font-mono"
              :class="errors.placa ? '!border-danger-500 !bg-danger-50' : ''"
              :disabled="isEditing"
            />
          </Field>
          <Field label="VIN" :error="errors.vin" for="field-vin" hint="17 caracteres (opcional)">
            <InputText
              id="field-vin"
              v-model="form.vin"
              @input="form.vin = form.vin.toUpperCase(); clearError('vin')"
              maxlength="17"
              class="w-full font-mono"
              :class="errors.vin ? '!border-danger-500 !bg-danger-50' : ''"
              :disabled="isEditing"
            />
          </Field>
          <Field label="Marca" required :error="errors.marca" for="field-marca">
            <InputText
              id="field-marca"
              v-model="form.marca"
              placeholder="Toyota"
              class="w-full"
              :class="errors.marca ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="clearError('marca')"
            />
          </Field>
          <Field label="Modelo" required :error="errors.modelo" for="field-modelo">
            <InputText
              id="field-modelo"
              v-model="form.modelo"
              placeholder="Yaris"
              class="w-full"
              :class="errors.modelo ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="clearError('modelo')"
            />
          </Field>
          <Field label="Año" required :error="errors.anio" for="field-anio">
            <InputNumber
              inputId="field-anio"
              v-model="form.anio"
              :min="1990"
              :max="2050"
              :useGrouping="false"
              class="w-full"
              :class="errors.anio ? '[&_input]:!border-danger-500 [&_input]:!bg-danger-50' : ''"
              @update:modelValue="clearError('anio')"
            />
          </Field>
          <Field label="Color">
            <InputText v-model="form.color" placeholder="Blanco" class="w-full" />
          </Field>
          <Field label="Número de motor" hint="Aparece en la matrícula">
            <InputText v-model="form.numeroMotor" maxlength="50" class="w-full" />
          </Field>
          <Field label="Número de chasis" hint="Puede coincidir con VIN">
            <InputText v-model="form.numeroChasis" maxlength="50" class="w-full" />
          </Field>
        </div>
      </FormCard>

      <FormCard title="Operación" icon="pi pi-cog">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Kilometraje actual">
            <InputNumber v-model="form.kilometraje" :min="0" suffix=" km" class="w-full" />
          </Field>
          <Field label="Capacidad de pasajeros" hint="Incluye al conductor">
            <InputNumber v-model="form.capacidadPasajeros" :min="1" :max="100" class="w-full" />
          </Field>
          <Field label="Categoría de licencia" required :error="errors.categoriaLicenciaId" for="field-categoriaLicenciaId" hint="Qué licencia se necesita para conducirlo">
            <Dropdown
              inputId="field-categoriaLicenciaId"
              v-model="form.categoriaLicenciaId"
              :options="categorias"
              option-label="label"
              option-value="value"
              placeholder="Selecciona categoría"
              filter
              class="w-full"
              :class="errors.categoriaLicenciaId ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="clearError('categoriaLicenciaId')"
            />
          </Field>
          <Field label="Tipo de combustible" required :error="errors.tipoCombustibleId" for="field-tipoCombustibleId" hint="Determina el precio en el registro de carga">
            <Dropdown
              inputId="field-tipoCombustibleId"
              v-model="form.tipoCombustibleId"
              :options="tiposCombustible"
              option-label="label"
              option-value="value"
              placeholder="Selecciona combustible"
              class="w-full"
              :class="errors.tipoCombustibleId ? '!border-danger-500 !bg-danger-50' : ''"
              @update:modelValue="clearError('tipoCombustibleId')"
            />
          </Field>
          <Field label="Estado">
            <Dropdown
              v-model="form.estado"
              :options="opcionesEstado"
              option-label="label"
              option-value="value"
              class="w-full"
            />
          </Field>
        </div>
      </FormCard>

      <FormCard title="Documentos y vencimientos" icon="pi pi-shield">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Vencimiento SOAT" hint="Seguro Obligatorio">
            <Calendar v-model="form.soatVencimiento" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
          </Field>
          <Field label="Vencimiento Revisión Técnica (RTV)">
            <Calendar v-model="form.revisionVencimiento" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
          </Field>
          <Field label="Fecha de compra">
            <Calendar v-model="form.fechaCompra" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" :maxDate="new Date()" />
          </Field>
          <Field label="Valor de compra (USD)">
            <InputNumber v-model="form.valorCompra" mode="currency" currency="USD" locale="en-US" :minFractionDigits="2" class="w-full" />
          </Field>
        </div>
      </FormCard>

      <FormCard title="Observaciones" icon="pi pi-comment">
        <Field label="Notas internas" hint="Opcional, máximo 1000 caracteres">
          <Textarea v-model="form.observaciones" rows="3" maxlength="1000" placeholder="Detalles relevantes del vehículo, historial, características..." class="w-full" />
        </Field>
      </FormCard>

      <div class="flex items-center justify-end gap-3">
        <Button label="Cancelar" outlined type="button" @click="router.back()" />
        <Button :label="isEditing ? 'Guardar cambios' : 'Crear vehículo'" icon="pi pi-check" type="submit" :loading="isLoading" />
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
import Dropdown from 'primevue/dropdown'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import PageHeader from '@/components/ui/PageHeader.vue'
import FormCard from '@/components/ui/FormCard.vue'
import vehiculosService, {
  type CreateVehiculoRequest,
  type UpdateVehiculoRequest,
  type EstadoVehiculo
} from '@/services/vehiculos'

const router = useRouter()
const route = useRoute()
const toast = useToast()

const Field = defineComponent({
  props: {
    label: { type: String, required: true },
    required: { type: Boolean, default: false },
    hint: { type: String, default: '' },
    error: { type: String, default: '' },
    for: { type: String, default: '' }
  },
  setup(props, { slots, attrs }) {
    return () =>
      h('div', { ...attrs }, [
        h('label', {
          class: 'block text-sm font-medium text-ink-700 mb-1.5',
          for: props.for || undefined
        }, [
          props.label,
          props.required ? h('span', { class: 'text-danger-600 ml-0.5 font-semibold' }, ' *') : null
        ]),
        slots.default?.(),
        props.error
          ? h('p', { class: 'text-xs text-danger-600 mt-1 flex items-center gap-1' }, [
              h('i', { class: 'pi pi-exclamation-circle text-[10px]' }),
              props.error
            ])
          : (props.hint ? h('p', { class: 'text-xs text-ink-500 mt-1' }, props.hint) : null)
      ])
  }
})

const isLoading = ref(false)
const errorMessage = ref('')
const errors = reactive<Record<string, string>>({})
const setError = (k: string, v: string) => { errors[k] = v }
const clearError = (k: string) => { if (errors[k]) delete errors[k] }
const clearAllErrors = () => { Object.keys(errors).forEach(k => delete errors[k]) }
const ORDEN_CAMPOS = ['placa','vin','marca','modelo','anio','categoriaLicenciaId','tipoCombustibleId']
const focusFirstError = () => {
  const primero = ORDEN_CAMPOS.find(k => errors[k])
  if (!primero) return
  const el = document.getElementById(`field-${primero}`)
  if (!el) return
  el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  setTimeout(() => (el as HTMLElement).focus?.(), 300)
}
const id = computed(() => {
  const v = route.params.id
  return typeof v === 'string' ? parseInt(v) : (v as number | undefined)
})
const isEditing = computed(() => !!id.value)

const categorias = ref<{ label: string; value: number }[]>([])
const tiposCombustible = ref<{ label: string; value: number }[]>([])

const opcionesEstado = [
  { label: 'Activo', value: 'ACTIVO' },
  { label: 'En mantenimiento', value: 'MANTENIMIENTO' },
  { label: 'Fuera de servicio', value: 'FUERA_SERVICIO' }
]

const form = reactive<{
  placa: string; vin: string; marca: string; modelo: string
  anio: number; color: string; kilometraje: number
  numeroMotor: string; numeroChasis: string; capacidadPasajeros: number | null
  categoriaLicenciaId: number | null; tipoCombustibleId: number | null
  estado: EstadoVehiculo
  soatVencimiento: Date | null; revisionVencimiento: Date | null
  fechaCompra: Date | null; valorCompra: number | null
  observaciones: string
}>({
  placa: '', vin: '', marca: '', modelo: '',
  anio: new Date().getFullYear(),
  color: '', kilometraje: 0,
  numeroMotor: '', numeroChasis: '', capacidadPasajeros: 5,
  categoriaLicenciaId: null, tipoCombustibleId: null,
  estado: 'ACTIVO',
  soatVencimiento: null, revisionVencimiento: null,
  fechaCompra: null, valorCompra: null,
  observaciones: ''
})

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
  const p = v.substring(0, 10).split('-')
  if (p.length !== 3) return null
  return new Date(parseInt(p[0]), parseInt(p[1]) - 1, parseInt(p[2]))
}

const cargarCatalogos = async () => {
  try {
    const [cats, tipos] = await Promise.all([
      vehiculosService.listarCategoriasLicencia(true),
      vehiculosService.listarTiposCombustible(true)
    ])
    categorias.value = cats.map(c => ({ label: `${c.codigo} - ${c.descripcion}`, value: c.id }))
    tiposCombustible.value = tipos.map(t => ({
      label: `${t.nombre}  ($${Number(t.precioActual).toFixed(2)}/${t.unidad.toLowerCase()})`,
      value: t.id
    }))
  } catch (e) {
    console.warn('No se pudieron cargar catalogos', e)
  }
}

const cargar = async () => {
  if (!isEditing.value || !id.value) return
  try {
    const data = await vehiculosService.obtenerVehiculo(id.value)
    form.placa = data.placa || ''
    form.vin = data.vin || ''
    form.marca = data.marca || ''
    form.modelo = data.modelo || ''
    form.anio = data.anio || new Date().getFullYear()
    form.color = data.color || ''
    form.kilometraje = data.kilometraje || 0
    form.numeroMotor = data.numeroMotor || ''
    form.numeroChasis = data.numeroChasis || ''
    form.capacidadPasajeros = data.capacidadPasajeros ?? 5
    form.categoriaLicenciaId = data.categoriaLicenciaId ?? null
    form.tipoCombustibleId = data.tipoCombustibleId ?? null
    form.estado = data.estado || 'ACTIVO'
    form.soatVencimiento = parseFecha(data.soatVencimiento)
    form.revisionVencimiento = parseFecha(data.revisionVencimiento)
    form.fechaCompra = parseFecha(data.fechaCompra)
    form.valorCompra = data.valorCompra ?? null
    form.observaciones = data.observaciones || ''
  } catch (e: any) {
    errorMessage.value = e.response?.data?.detail || 'No se pudo cargar el vehículo'
  }
}

const validarPlaca = (placa: string) => /^[A-Z]{3}-\d{4}$|^[A-Z]{2}-\d{4}[A-Z]$/.test(placa)

const validar = (): boolean => {
  errorMessage.value = ''
  clearAllErrors()

  if (!form.placa.trim()) setError('placa', 'La placa es requerida')
  else if (!isEditing.value && !validarPlaca(form.placa.trim())) {
    setError('placa', 'Placa inválida. Formato: ABC-1234 o AB-1234A')
  }
  if (!form.marca.trim()) setError('marca', 'La marca es requerida')
  if (!form.modelo.trim()) setError('modelo', 'El modelo es requerido')
  if (!form.anio || form.anio < 1990 || form.anio > 2050) setError('anio', 'El año debe estar entre 1990 y 2050')
  if (form.vin && form.vin.length !== 17) setError('vin', 'El VIN debe tener 17 caracteres (déjalo vacío si no lo conoces)')
  if (!form.categoriaLicenciaId) setError('categoriaLicenciaId', 'Selecciona la categoría de licencia')
  if (!form.tipoCombustibleId) setError('tipoCombustibleId', 'Selecciona el tipo de combustible')

  if (Object.keys(errors).length > 0) {
    focusFirstError()
    return false
  }
  return true
}

const guardar = async () => {
  if (!validar()) return

  try {
    isLoading.value = true
    if (isEditing.value && id.value) {
      const payload: UpdateVehiculoRequest = {
        marca: form.marca.trim(),
        modelo: form.modelo.trim(),
        anio: form.anio,
        color: form.color?.trim() || undefined,
        kilometraje: form.kilometraje,
        estado: form.estado,
        soatVencimiento: fmtFecha(form.soatVencimiento),
        revisionVencimiento: fmtFecha(form.revisionVencimiento),
        fechaCompra: fmtFecha(form.fechaCompra),
        valorCompra: form.valorCompra ?? undefined,
        categoriaLicenciaId: form.categoriaLicenciaId ?? undefined,
        tipoCombustibleId: form.tipoCombustibleId ?? undefined,
        numeroMotor: form.numeroMotor?.trim() || undefined,
        numeroChasis: form.numeroChasis?.trim() || undefined,
        capacidadPasajeros: form.capacidadPasajeros ?? undefined,
        observaciones: form.observaciones?.trim() || undefined
      }
      await vehiculosService.actualizarVehiculo(id.value, payload)
      toast.add({ severity: 'success', summary: 'Actualizado', detail: 'Vehículo actualizado', life: 2500 })
    } else {
      const payload: CreateVehiculoRequest = {
        placa: form.placa.trim(),
        vin: form.vin?.trim() || undefined,
        marca: form.marca.trim(),
        modelo: form.modelo.trim(),
        anio: form.anio,
        color: form.color?.trim() || undefined,
        kilometraje: form.kilometraje,
        estado: form.estado,
        soatVencimiento: fmtFecha(form.soatVencimiento),
        revisionVencimiento: fmtFecha(form.revisionVencimiento),
        fechaCompra: fmtFecha(form.fechaCompra),
        valorCompra: form.valorCompra ?? undefined,
        categoriaLicenciaId: form.categoriaLicenciaId ?? undefined,
        tipoCombustibleId: form.tipoCombustibleId ?? undefined,
        numeroMotor: form.numeroMotor?.trim() || undefined,
        numeroChasis: form.numeroChasis?.trim() || undefined,
        capacidadPasajeros: form.capacidadPasajeros ?? undefined,
        observaciones: form.observaciones?.trim() || undefined
      }
      await vehiculosService.crearVehiculo(payload)
      toast.add({ severity: 'success', summary: 'Creado', detail: 'Vehículo registrado', life: 2500 })
    }
    setTimeout(() => router.push('/vehiculos'), 800)
  } catch (e: any) {
    const d = e.response?.data
    errorMessage.value = d?.errors
      ? Object.entries(d.errors).map(([k, v]) => `${k}: ${v}`).join(' · ')
      : (d?.detail || d?.message || 'Error al guardar')
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  await cargarCatalogos()
  await cargar()
})
</script>
