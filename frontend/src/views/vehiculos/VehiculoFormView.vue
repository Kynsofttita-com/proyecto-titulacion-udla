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

    <form @submit.prevent="guardar" class="space-y-6">
      <FormCard title="Identificación" icon="pi pi-id-card">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Placa" required hint="Formato Ecuador: ABC-1234">
            <InputText v-model="form.placa" placeholder="PBX-1234" class="w-full !font-mono !uppercase" required />
          </Field>
          <Field label="VIN / Chasis" required>
            <InputText v-model="form.vin" placeholder="17 caracteres" maxlength="17" class="w-full !font-mono" required />
          </Field>
          <Field label="Marca" required>
            <InputText v-model="form.marca" placeholder="Toyota, Chevrolet..." class="w-full" required />
          </Field>
          <Field label="Modelo" required>
            <InputText v-model="form.modelo" placeholder="Corolla, Sail..." class="w-full" required />
          </Field>
          <Field label="Año" required>
            <InputNumber v-model="form.año" :min="1990" :max="2030" :useGrouping="false" class="w-full" />
          </Field>
          <Field label="Color">
            <InputText v-model="form.color" placeholder="Blanco, Plata..." class="w-full" />
          </Field>
        </div>
      </FormCard>

      <FormCard title="Operación" icon="pi pi-wrench">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Kilometraje actual">
            <InputNumber v-model="form.kilometraje" suffix=" km" :min="0" class="w-full" />
          </Field>
          <Field label="Última fecha de mantenimiento">
            <Calendar v-model="form.fechaMantenimiento" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
          </Field>
          <Field label="Última inspección técnica">
            <Calendar v-model="form.fechaInspeccion" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
          </Field>
        </div>
      </FormCard>

      <FormCard title="Observaciones" icon="pi pi-file">
        <Field label="Notas adicionales">
          <Textarea v-model="form.observaciones" rows="3" class="w-full" placeholder="Detalles relevantes del vehículo..." />
        </Field>
      </FormCard>

      <div class="flex items-center justify-end gap-3">
        <Button label="Cancelar" outlined type="button" @click="router.back()" />
        <Button :label="isEditing ? 'Guardar cambios' : 'Crear vehículo'" icon="pi pi-check" type="submit" :loading="isLoading" />
      </div>
    </form>
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
import PageHeader from '@/components/ui/PageHeader.vue'
import FormCard from '@/components/ui/FormCard.vue'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()

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

const form = reactive<any>({
  placa: '', vin: '', marca: '', modelo: '', año: new Date().getFullYear(),
  color: '', kilometraje: 0, fechaMantenimiento: null, fechaInspeccion: null, observaciones: ''
})

const fmt = (v: any) => {
  if (!v) return null
  if (typeof v === 'string') return v.substring(0, 10)
  const d = v as Date
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const cargar = async () => {
  if (!isEditing.value) return
  try {
    const { data } = await api.get(`/vehiculos/${id.value}`)
    Object.assign(form, data)
    if (data.anio && !data.año) form.año = data.anio
  } catch (e: any) {
    errorMessage.value = e.response?.data?.detail || 'No se pudo cargar'
  }
}

const guardar = async () => {
  errorMessage.value = ''
  try {
    isLoading.value = true
    const payload = {
      ...form,
      placa: (form.placa || '').toUpperCase(),
      fechaMantenimiento: fmt(form.fechaMantenimiento),
      fechaInspeccion: fmt(form.fechaInspeccion)
    }
    if (isEditing.value) await api.put(`/vehiculos/${id.value}`, payload)
    else await api.post('/vehiculos', payload)
    router.push('/vehiculos')
  } catch (e: any) {
    const d = e.response?.data
    errorMessage.value = d?.errors
      ? Object.entries(d.errors).map(([k, v]) => `${k}: ${v}`).join(' · ')
      : (d?.detail || d?.message || 'Error al guardar')
  } finally { isLoading.value = false }
}

onMounted(cargar)
</script>
