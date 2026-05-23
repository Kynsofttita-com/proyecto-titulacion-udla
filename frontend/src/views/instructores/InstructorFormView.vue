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
            <InputText v-model="form.nombre" class="w-full" required />
          </Field>
          <Field label="Apellido" required>
            <InputText v-model="form.apellido" class="w-full" required />
          </Field>
          <Field label="Cédula" required hint="10 dígitos, validación Ecuador">
            <InputText v-model="form.cedula" maxlength="10" class="w-full" required />
          </Field>
          <Field label="Fecha de nacimiento" required>
            <Calendar v-model="form.fechaNacimiento" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" required />
          </Field>
          <Field label="Email" required>
            <InputText v-model="form.email" type="email" class="w-full" required />
          </Field>
          <Field label="Teléfono" required>
            <InputText v-model="form.telefono" maxlength="10" class="w-full" required />
          </Field>
          <Field label="Dirección" required class="md:col-span-2">
            <Textarea v-model="form.direccion" rows="2" class="w-full" required />
          </Field>
        </div>
      </FormCard>

      <FormCard title="Información laboral" icon="pi pi-briefcase">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Fecha de contratación" required>
            <Calendar v-model="form.fechaContratacion" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" required />
          </Field>
          <Field label="Salario mensual (USD)" required>
            <InputNumber v-model="form.salarioMensual" mode="currency" currency="USD" locale="en-US" class="w-full" />
          </Field>
        </div>
      </FormCard>

      <FormCard title="Licencia de conducir" icon="pi pi-id-card">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <Field label="Número de licencia" required>
            <InputText v-model="form.licenciaNumero" class="w-full" required />
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
            <Calendar v-model="form.licenciaCaducidad" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" required />
          </Field>
        </div>
      </FormCard>

      <div class="flex items-center justify-end gap-3">
        <Button label="Cancelar" outlined type="button" @click="router.back()" />
        <Button :label="isEditing ? 'Guardar cambios' : 'Crear instructor'" icon="pi pi-check" type="submit" :loading="isLoading" />
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
  nombre: '', apellido: '', cedula: '', email: '', telefono: '', direccion: '',
  fechaNacimiento: '', fechaContratacion: '', salarioMensual: 850,
  licenciaNumero: '', licenciaCategoria: 'B', licenciaEmision: '', licenciaCaducidad: ''
})

const fmt = (v: any): string => {
  if (!v) return ''
  if (typeof v === 'string') return v.substring(0, 10)
  const d = v as Date
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const cargar = async () => {
  if (!isEditing.value) return
  try {
    const { data } = await api.get(`/instructores/${id.value}`)
    Object.assign(form, data)
  } catch (e: any) {
    errorMessage.value = e.response?.data?.detail || 'No se pudo cargar el instructor'
  }
}

const guardar = async () => {
  errorMessage.value = ''
  try {
    isLoading.value = true
    const payload = {
      ...form,
      fechaNacimiento: fmt(form.fechaNacimiento),
      fechaContratacion: fmt(form.fechaContratacion),
      licenciaEmision: fmt(form.licenciaEmision),
      licenciaCaducidad: fmt(form.licenciaCaducidad)
    }
    if (isEditing.value) await api.put(`/instructores/${id.value}`, payload)
    else await api.post('/instructores', payload)
    router.push('/instructores')
  } catch (e: any) {
    const d = e.response?.data
    errorMessage.value = d?.errors
      ? Object.entries(d.errors).map(([k, v]) => `${k}: ${v}`).join(' · ')
      : (d?.detail || d?.message || 'Error al guardar')
  } finally { isLoading.value = false }
}

onMounted(cargar)
</script>
