<template>
  <div class="space-y-6 max-w-5xl">
    <PageHeader
      title="Configuración del sistema"
      description="Personaliza los parámetros operativos y de marca de tu escuela."
      icon="pi pi-cog"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Configuración' }]"
    />

    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- Sidebar navegación interna -->
      <aside class="lg:col-span-1">
        <nav class="card p-2 space-y-1 sticky top-24">
          <button
            v-for="s in secciones"
            :key="s.key"
            @click="activa = s.key"
            :class="['w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-left transition',
              activa === s.key ? 'bg-brand-50 text-brand-700' : 'text-ink-600 hover:bg-ink-100']"
          >
            <i :class="['pi', s.icon, activa === s.key ? 'text-brand-700' : 'text-ink-400']" />
            {{ s.label }}
          </button>
        </nav>
      </aside>

      <!-- Contenido -->
      <div class="lg:col-span-3 space-y-6">
        <FormCard
          v-if="activa === 'escuela'"
          title="Información general"
          description="Datos institucionales que aparecerán en facturas y reportes."
          icon="pi pi-building"
        >
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <Field label="Nombre de la escuela" required>
              <InputText v-model="config.nombre" class="w-full" />
            </Field>
            <Field label="RUC" required>
              <InputText v-model="config.ruc" maxlength="13" class="w-full" />
            </Field>
            <Field label="Email institucional" required>
              <InputText v-model="config.email" type="email" class="w-full" />
            </Field>
            <Field label="Teléfono" required>
              <InputText v-model="config.telefono" class="w-full" />
            </Field>
            <Field label="Dirección" class="md:col-span-2">
              <Textarea v-model="config.direccion" rows="2" class="w-full" />
            </Field>
          </div>
          <template #actions>
            <Button label="Guardar cambios" icon="pi pi-check" @click="guardar" :loading="guardando" />
          </template>
        </FormCard>

        <FormCard
          v-if="activa === 'horarios'"
          title="Horarios de operación"
          description="Define los horarios en que se pueden programar clases."
          icon="pi pi-clock"
        >
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <Field label="Hora apertura">
              <Calendar v-model="config.horarioApertura" timeOnly :showIcon="true" class="w-full" />
            </Field>
            <Field label="Hora cierre">
              <Calendar v-model="config.horarioCierre" timeOnly :showIcon="true" class="w-full" />
            </Field>
            <Field label="Duración default por clase (minutos)">
              <InputNumber v-model="config.duracionClaseDefaultMin" :min="30" :max="240" class="w-full" />
            </Field>
            <Field label="Horas de recordatorio">
              <InputNumber v-model="config.horasRecordatorioClase" :min="1" :max="72" class="w-full" />
            </Field>
          </div>
          <template #actions>
            <Button label="Guardar cambios" icon="pi pi-check" @click="guardar" :loading="guardando" />
          </template>
        </FormCard>

        <FormCard
          v-if="activa === 'marca'"
          title="Identidad visual"
          description="Colores corporativos para reportes y facturas."
          icon="pi pi-palette"
        >
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <Field label="Color primario">
              <div class="flex items-center gap-3">
                <input type="color" v-model="config.colorPrimario" class="w-14 h-11 rounded-lg border border-ink-200 cursor-pointer" />
                <InputText v-model="config.colorPrimario" class="flex-1 !font-mono" />
              </div>
            </Field>
            <Field label="Color secundario">
              <div class="flex items-center gap-3">
                <input type="color" v-model="config.colorSecundario" class="w-14 h-11 rounded-lg border border-ink-200 cursor-pointer" />
                <InputText v-model="config.colorSecundario" class="flex-1 !font-mono" />
              </div>
            </Field>
          </div>
          <template #actions>
            <Button label="Guardar cambios" icon="pi pi-check" @click="guardar" :loading="guardando" />
          </template>
        </FormCard>

        <FormCard
          v-if="activa === 'alertas'"
          title="Alertas y notificaciones"
          description="Umbrales para alertas de mantenimiento y documentos."
          icon="pi pi-bell"
        >
          <Field label="Días antes para alerta SOAT por vencer">
            <InputNumber v-model="config.diasAlertaSoat" :min="1" :max="120" class="w-full md:w-1/2" />
          </Field>
          <template #actions>
            <Button label="Guardar cambios" icon="pi pi-check" @click="guardar" :loading="guardando" />
          </template>
        </FormCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, defineComponent, h } from 'vue'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import PageHeader from '@/components/ui/PageHeader.vue'
import FormCard from '@/components/ui/FormCard.vue'
import api from '@/services/api'

const Field = defineComponent({
  props: ['label', 'required'],
  setup(props, { slots, attrs }) {
    return () =>
      h('div', { ...attrs }, [
        h('label', { class: 'block text-sm font-medium text-ink-700 mb-1.5' }, [
          props.label, props.required && h('span', { class: 'text-danger-500 ml-0.5' }, '*')
        ]),
        slots.default?.()
      ])
  }
})

const activa = ref<'escuela' | 'horarios' | 'marca' | 'alertas'>('escuela')
const guardando = ref(false)

const secciones = [
  { key: 'escuela',  label: 'Información general', icon: 'pi-building' },
  { key: 'horarios', label: 'Horarios',            icon: 'pi-clock' },
  { key: 'marca',    label: 'Identidad visual',    icon: 'pi-palette' },
  { key: 'alertas',  label: 'Alertas',             icon: 'pi-bell' }
]

const config = reactive<any>({
  nombre: '', ruc: '', email: '', telefono: '', direccion: '',
  horarioApertura: null, horarioCierre: null,
  duracionClaseDefaultMin: 60, horasRecordatorioClase: 24,
  colorPrimario: '#1e40af', colorSecundario: '#3b82f6',
  diasAlertaSoat: 30
})

const cargar = async () => {
  try {
    const { data } = await api.get('/configuracion')
    Object.assign(config, data)
  } catch (e) { console.error(e) }
}

const guardar = async () => {
  guardando.value = true
  try {
    await api.put('/configuracion', config)
  } catch (e) { console.error(e) }
  finally { guardando.value = false }
}

onMounted(cargar)
</script>
