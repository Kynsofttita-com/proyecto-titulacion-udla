<template>
  <div class="space-y-6">
    <PageHeader
      title="Asignaciones de clases"
      description="Programación tripartita: estudiante + instructor + vehículo."
      icon="pi pi-calendar"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Asignaciones' }]"
    >
      <template #actions>
        <Button label="Vista lista" icon="pi pi-list" outlined @click="vista = 'lista'" :severity="vista === 'lista' ? 'primary' : 'secondary'" />
        <Button label="Vista calendario" icon="pi pi-calendar" outlined @click="vista = 'calendario'" :severity="vista === 'calendario' ? 'primary' : 'secondary'" />
        <Button label="Nueva asignación" icon="pi pi-plus" @click="mostrarForm = true" />
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard label="Total asignaciones" :value="stats.total" icon="pi pi-calendar" color="brand" />
      <StatCard label="Confirmadas" :value="stats.confirmadas" icon="pi pi-check-circle" color="success" />
      <StatCard label="Programadas" :value="stats.programadas" icon="pi pi-clock" color="warning" />
      <StatCard label="Esta semana" :value="stats.semana" icon="pi pi-bookmark" color="info" />
    </div>

    <!-- Vista Calendario -->
    <div v-if="vista === 'calendario'" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <section class="card animate-fade-up p-6 lg:col-span-1">
        <h3 class="heading-3 mb-4">Selecciona un día</h3>
        <Calendar
          v-model="fechaSeleccionada"
          :inline="true"
          dateFormat="yy-mm-dd"
          class="!w-full"
          @date-select="cargar"
        />
        <div class="mt-5 pt-5 border-t border-ink-200">
          <p class="text-xs uppercase tracking-wider text-ink-500 font-semibold mb-3">Leyenda</p>
          <div class="space-y-2">
            <LegendItem color="bg-success-500" label="Confirmada" />
            <LegendItem color="bg-warning-500" label="Programada" />
            <LegendItem color="bg-info-500" label="En curso" />
            <LegendItem color="bg-danger-500" label="Cancelada" />
          </div>
        </div>
      </section>

      <DataTableCard
        :title="`Asignaciones · ${formatearFecha(fechaSeleccionada)}`"
        :description="`${asignacionesDelDia.length} clase(s) programada(s) para este día`"
        class="lg:col-span-2"
      >
        <EmptyState
          v-if="asignacionesDelDia.length === 0"
          icon="pi pi-calendar-times"
          title="Sin clases en este día"
          description="Selecciona otro día o crea una nueva asignación."
        >
          <template #action>
            <Button label="Crear asignación" icon="pi pi-plus" @click="mostrarForm = true" />
          </template>
        </EmptyState>
        <ul v-else class="divide-y divide-ink-200">
          <li v-for="a in asignacionesDelDia" :key="a.id" class="flex items-center gap-4 px-6 py-4 hover:bg-ink-50/50 transition">
            <div class="w-16 text-center flex-shrink-0">
              <p class="text-xl font-bold text-brand-700">{{ extraerHora(a.fechaHora) }}</p>
              <p class="text-[10px] text-ink-500 uppercase tracking-wider">{{ a.duracionMinutos || 60 }} min</p>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <p class="text-sm font-semibold text-ink-900">Clase #{{ a.id }}</p>
                <span class="text-xs text-ink-400">·</span>
                <span class="text-xs text-ink-500">{{ a.tipoClase || 'PRÁCTICA' }}</span>
              </div>
              <p class="text-xs text-ink-500 mt-0.5">
                Estudiante <span class="text-ink-700 font-medium">#{{ a.estudianteId }}</span> ·
                Instructor <span class="text-ink-700 font-medium">#{{ a.instructorId }}</span> ·
                Vehículo <span class="text-ink-700 font-medium">#{{ a.vehiculoId }}</span>
              </p>
            </div>
            <StatusBadge :status="a.estado" />
          </li>
        </ul>
      </DataTableCard>
    </div>

    <!-- Vista Lista -->
    <DataTableCard v-else title="Todas las asignaciones">
      <DataTable :value="asignaciones" :loading="loading" striped-rows :pt="{ table: { style: 'min-width: 60rem' } }">
        <Column field="id" header="#" style="width: 70px" />
        <Column header="Fecha y hora">
          <template #body="{ data }">
            <p class="text-sm font-medium">{{ extraerFechaCorta(data.fechaHora) }}</p>
            <p class="text-xs text-ink-500">{{ extraerHora(data.fechaHora) }} · {{ data.duracionMinutos || 60 }}min</p>
          </template>
        </Column>
        <Column header="Estudiante">
          <template #body="{ data }">
            <span class="text-sm">#{{ data.estudianteId }}</span>
          </template>
        </Column>
        <Column header="Instructor">
          <template #body="{ data }">
            <span class="text-sm">#{{ data.instructorId }}</span>
          </template>
        </Column>
        <Column header="Vehículo">
          <template #body="{ data }">
            <span class="text-sm">#{{ data.vehiculoId }}</span>
          </template>
        </Column>
        <Column field="tipoClase" header="Tipo" />
        <Column header="Estado">
          <template #body="{ data }">
            <StatusBadge :status="data.estado" />
          </template>
        </Column>
      </DataTable>
    </DataTableCard>

    <!-- Dialog Nueva Asignación -->
    <Dialog v-model:visible="mostrarForm" modal header="Nueva asignación" :style="{ width: '720px' }" :pt="{ root: { class: 'mx-4' } }">
      <div class="space-y-5">
        <div v-if="formError" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 flex items-start gap-2 text-sm text-danger-600">
          <i class="pi pi-exclamation-circle mt-0.5" />
          <span>{{ formError }}</span>
        </div>

        <!-- ESTUDIANTE -->
        <div>
          <label class="label mb-1.5 block">
            <span class="flex items-center gap-2">
              <i class="pi pi-user text-brand-600" />
              Estudiante *
            </span>
          </label>
          <AutoComplete
            v-model="selEstudiante"
            :suggestions="estudiantesFiltered"
            @complete="filterEstudiantes"
            optionLabel="nombreCompleto"
            placeholder="Buscar por nombre, cédula o email..."
            class="w-full"
            :pt="{ input: { class: 'w-full' } }"
          >
            <template #option="{ option }">
              <div class="flex items-center gap-3 py-1">
                <div class="w-9 h-9 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center text-xs font-semibold">
                  {{ initials(option.nombreCompleto) }}
                </div>
                <div class="flex-1">
                  <p class="text-sm font-medium text-ink-900">{{ option.nombreCompleto }}</p>
                  <p class="text-xs text-ink-500">{{ option.cedula }} · {{ option.email }}</p>
                </div>
                <StatusBadge :status="option.estado" />
              </div>
            </template>
            <template #empty>
              <p class="px-3 py-2 text-sm text-ink-500">Sin estudiantes activos coincidentes</p>
            </template>
          </AutoComplete>

          <!-- Card detalle estudiante -->
          <div v-if="selEstudiante" class="mt-3 rounded-lg border border-brand-200 bg-brand-50/40 p-3 animate-fade-up">
            <div class="grid grid-cols-2 gap-x-4 gap-y-1.5 text-xs">
              <DetailRow label="Cédula" :value="selEstudiante.cedula" />
              <DetailRow label="Estado" :value="selEstudiante.estado" />
              <DetailRow label="Teléfono" :value="selEstudiante.telefono || '—'" />
              <DetailRow label="Email" :value="selEstudiante.email" />
            </div>
          </div>
        </div>

        <!-- INSTRUCTOR -->
        <div>
          <label class="label mb-1.5 block">
            <span class="flex items-center gap-2">
              <i class="pi pi-id-card text-brand-600" />
              Instructor *
            </span>
          </label>
          <AutoComplete
            v-model="selInstructor"
            :suggestions="instructoresFiltered"
            @complete="filterInstructores"
            optionLabel="nombreCompleto"
            placeholder="Buscar por nombre o cédula..."
            class="w-full"
            :pt="{ input: { class: 'w-full' } }"
          >
            <template #option="{ option }">
              <div class="flex items-center gap-3 py-1">
                <div class="w-9 h-9 rounded-full bg-success-50 text-success-600 flex items-center justify-center text-xs font-semibold">
                  {{ initials(option.nombreCompleto) }}
                </div>
                <div class="flex-1">
                  <p class="text-sm font-medium text-ink-900">{{ option.nombreCompleto }}</p>
                  <p class="text-xs text-ink-500">{{ option.cedula }} · Licencia {{ option.licenciaCategoria || '?' }}</p>
                </div>
                <StatusBadge :status="option.estado" />
              </div>
            </template>
            <template #empty>
              <p class="px-3 py-2 text-sm text-ink-500">Sin instructores activos coincidentes</p>
            </template>
          </AutoComplete>

          <div v-if="selInstructor" class="mt-3 rounded-lg border border-brand-200 bg-brand-50/40 p-3 animate-fade-up">
            <div class="grid grid-cols-2 gap-x-4 gap-y-1.5 text-xs">
              <DetailRow label="Cédula" :value="selInstructor.cedula" />
              <DetailRow label="Licencia" :value="`${selInstructor.licenciaCategoria || '?'} · ${selInstructor.licenciaNumero || '—'}`" />
              <DetailRow label="Caducidad licencia" :value="selInstructor.licenciaCaducidad || '—'" />
              <DetailRow label="Teléfono" :value="selInstructor.telefono || '—'" />
            </div>
          </div>
        </div>

        <!-- VEHÍCULO -->
        <div>
          <label class="label mb-1.5 block">
            <span class="flex items-center gap-2">
              <i class="pi pi-car text-brand-600" />
              Vehículo *
            </span>
          </label>
          <AutoComplete
            v-model="selVehiculo"
            :suggestions="vehiculosFiltered"
            @complete="filterVehiculos"
            optionLabel="placa"
            placeholder="Buscar por placa, marca o modelo..."
            class="w-full"
            :pt="{ input: { class: 'w-full' } }"
          >
            <template #option="{ option }">
              <div class="flex items-center gap-3 py-1">
                <div class="w-9 h-9 rounded-lg bg-info-50 text-info-600 flex items-center justify-center">
                  <i class="pi pi-car text-sm" />
                </div>
                <div class="flex-1">
                  <p class="text-sm font-medium text-ink-900">
                    <span class="font-mono">{{ option.placa }}</span>
                    <span class="text-ink-500"> · {{ option.marca }} {{ option.modelo }}</span>
                  </p>
                  <p class="text-xs text-ink-500">Año {{ option.año || option.anio || '?' }} · {{ (option.kilometraje ?? 0).toLocaleString() }} km</p>
                </div>
                <StatusBadge :status="option.estado || 'ACTIVO'" />
              </div>
            </template>
            <template #empty>
              <p class="px-3 py-2 text-sm text-ink-500">Sin vehículos disponibles</p>
            </template>
          </AutoComplete>

          <div v-if="selVehiculo" class="mt-3 rounded-lg border border-brand-200 bg-brand-50/40 p-3 animate-fade-up">
            <div class="grid grid-cols-2 gap-x-4 gap-y-1.5 text-xs">
              <DetailRow label="Placa" :value="selVehiculo.placa" :mono="true" />
              <DetailRow label="Modelo" :value="`${selVehiculo.marca} ${selVehiculo.modelo}`" />
              <DetailRow label="Año / Color" :value="`${selVehiculo.año || selVehiculo.anio || '?'} · ${selVehiculo.color || 'sin color'}`" />
              <DetailRow label="Kilometraje" :value="`${(selVehiculo.kilometraje ?? 0).toLocaleString()} km`" />
            </div>
          </div>
        </div>

        <hr class="border-ink-200" />

        <!-- FECHA Y HORAS -->
        <div class="space-y-4">
          <div>
            <label class="label mb-1.5 block">
              <span class="flex items-center gap-2">
                <i class="pi pi-calendar text-brand-600" />
                Fecha *
              </span>
            </label>
            <Calendar v-model="formAsig.fecha" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" :minDate="new Date()" />
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="label mb-1.5 block">Hora inicio *</label>
              <Calendar v-model="formAsig.horaInicio" timeOnly :showIcon="true" class="w-full" />
            </div>
            <div>
              <label class="label mb-1.5 block">Hora fin *</label>
              <Calendar v-model="formAsig.horaFin" timeOnly :showIcon="true" class="w-full" />
            </div>
          </div>
          <div>
            <label class="label mb-1.5 block">Observaciones</label>
            <Textarea v-model="formAsig.observaciones" rows="2" class="w-full" placeholder="Notas opcionales sobre la clase..." />
          </div>
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="cancelarForm" />
        <Button label="Crear asignación" icon="pi pi-check" :loading="creando" @click="crear" />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, defineComponent, h } from 'vue'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import AutoComplete from 'primevue/autocomplete'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import api from '@/services/api'

// Componente helper para mostrar filas de detalle
const DetailRow = defineComponent({
  props: ['label', 'value', 'mono'],
  setup(props) {
    return () => h('div', { class: 'flex items-center justify-between gap-2' }, [
      h('span', { class: 'text-ink-500' }, props.label + ':'),
      h('span', { class: `font-medium text-ink-800 ${props.mono ? 'font-mono' : ''}` }, props.value)
    ])
  }
})

const initials = (name: string) => {
  return (name || '?')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map(s => s[0]?.toUpperCase())
    .join('')
}

const LegendItem = defineComponent({
  props: ['color', 'label'],
  setup(props) {
    return () => h('div', { class: 'flex items-center gap-2' }, [
      h('span', { class: `w-2.5 h-2.5 rounded-full ${props.color}` }),
      h('span', { class: 'text-sm text-ink-700' }, props.label)
    ])
  }
})

const vista = ref<'calendario' | 'lista'>('calendario')
const fechaSeleccionada = ref<Date>(new Date())
const asignaciones = ref<any[]>([])
const loading = ref(false)
const stats = reactive({ total: 0, confirmadas: 0, programadas: 0, semana: 0 })

const mostrarForm = ref(false)
const creando = ref(false)
const formError = ref('')
const formAsig = reactive<any>({
  fecha: new Date(), horaInicio: null, horaFin: null, observaciones: ''
})

// ---------- Listas activas (cargadas al abrir el dialog) ----------
const estudiantesActivos = ref<any[]>([])
const instructoresActivos = ref<any[]>([])
const vehiculosActivos = ref<any[]>([])

// Selección actual del AutoComplete
const selEstudiante = ref<any>(null)
const selInstructor = ref<any>(null)
const selVehiculo = ref<any>(null)

// Sugerencias filtradas
const estudiantesFiltered = ref<any[]>([])
const instructoresFiltered = ref<any[]>([])
const vehiculosFiltered = ref<any[]>([])

const filterEstudiantes = (e: any) => {
  const q = (e.query || '').toLowerCase().trim()
  if (!q) { estudiantesFiltered.value = estudiantesActivos.value.slice(0, 20); return }
  estudiantesFiltered.value = estudiantesActivos.value.filter(es => {
    const full = (es.nombreCompleto || '').toLowerCase()
    return full.includes(q)
      || (es.cedula || '').toLowerCase().includes(q)
      || (es.email || '').toLowerCase().includes(q)
  }).slice(0, 20)
}

const filterInstructores = (e: any) => {
  const q = (e.query || '').toLowerCase().trim()
  if (!q) { instructoresFiltered.value = instructoresActivos.value.slice(0, 20); return }
  instructoresFiltered.value = instructoresActivos.value.filter(i => {
    return (i.nombreCompleto || '').toLowerCase().includes(q)
      || (i.cedula || '').toLowerCase().includes(q)
  }).slice(0, 20)
}

const filterVehiculos = (e: any) => {
  const q = (e.query || '').toLowerCase().trim()
  if (!q) { vehiculosFiltered.value = vehiculosActivos.value.slice(0, 20); return }
  vehiculosFiltered.value = vehiculosActivos.value.filter(v => {
    return (v.placa || '').toLowerCase().includes(q)
      || (v.marca || '').toLowerCase().includes(q)
      || (v.modelo || '').toLowerCase().includes(q)
  }).slice(0, 20)
}

// Carga listas activas al abrir el dialog
const cargarListasParaForm = async () => {
  try {
    const [estRes, insRes, vehRes] = await Promise.all([
      api.get('/estudiantes', { params: { size: 200 } }),
      api.get('/instructores', { params: { size: 200 } }),
      api.get('/vehiculos', { params: { size: 200 } })
    ])
    // Solo activos
    estudiantesActivos.value = (estRes.data.content || [])
      .filter((e: any) => e.estado === 'ACTIVO' || e.estado === 'MATRICULADO')
      .map((e: any) => ({
        ...e,
        nombreCompleto: e.nombreCompleto || `${e.nombre ?? ''} ${e.apellido ?? ''}`.trim()
      }))
    instructoresActivos.value = (insRes.data.content || [])
      .filter((i: any) => i.estado === 'ACTIVO')
      .map((i: any) => ({
        ...i,
        nombreCompleto: `${i.nombre ?? ''} ${i.apellido ?? ''}`.trim()
      }))
    vehiculosActivos.value = (vehRes.data.content || [])
      .filter((v: any) => !v.estado || v.estado === 'ACTIVO')
  } catch (e) {
    formError.value = 'No se pudieron cargar las listas de selección'
  }
}

watch(mostrarForm, (open) => {
  if (open) cargarListasParaForm()
  else cancelarForm()
})

const cancelarForm = () => {
  mostrarForm.value = false
  selEstudiante.value = null
  selInstructor.value = null
  selVehiculo.value = null
  formAsig.fecha = new Date()
  formAsig.horaInicio = null
  formAsig.horaFin = null
  formAsig.observaciones = ''
  formError.value = ''
}

const fmtFecha = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
const fmtHora = (d: Date) => d ? `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:00` : '00:00:00'

const extraerHora = (fh: string) => fh ? fh.substring(11, 16) : '--:--'
const extraerFechaCorta = (fh: string) => fh ? fh.substring(0, 10) : '—'
const formatearFecha = (d: Date) => d.toLocaleDateString('es-EC', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })

const asignacionesDelDia = computed(() => {
  const f = fmtFecha(fechaSeleccionada.value)
  return asignaciones.value.filter(a => (a.fechaHora || '').startsWith(f))
})

const cargar = async () => {
  try {
    loading.value = true
    const { data } = await api.get('/asignaciones', { params: { size: 200 } })
    asignaciones.value = data.content || []
    stats.total = data.totalElements ?? asignaciones.value.length
    stats.confirmadas = asignaciones.value.filter(a => a.estado === 'CONFIRMADA').length
    stats.programadas = asignaciones.value.filter(a => a.estado === 'PROGRAMADA').length
    const ahora = new Date()
    const finSem = new Date(ahora); finSem.setDate(ahora.getDate() + 7)
    stats.semana = asignaciones.value.filter(a => {
      const f = new Date(a.fechaHora || a.fecha)
      return f >= ahora && f <= finSem
    }).length
  } finally { loading.value = false }
}

const crear = async () => {
  formError.value = ''
  // Validaciones del lado cliente
  if (!selEstudiante.value) { formError.value = 'Selecciona un estudiante'; return }
  if (!selInstructor.value) { formError.value = 'Selecciona un instructor'; return }
  if (!selVehiculo.value)   { formError.value = 'Selecciona un vehículo'; return }
  if (!formAsig.fecha)      { formError.value = 'Selecciona la fecha'; return }
  if (!formAsig.horaInicio || !formAsig.horaFin) { formError.value = 'Indica hora inicio y hora fin'; return }
  try {
    creando.value = true
    const payload = {
      estudianteId: selEstudiante.value.id,
      instructorId: selInstructor.value.id,
      vehiculoId:   selVehiculo.value.id,
      fecha: fmtFecha(formAsig.fecha as Date),
      horaInicio: fmtHora(formAsig.horaInicio),
      horaFin:    fmtHora(formAsig.horaFin),
      observaciones: formAsig.observaciones
    }
    await api.post('/asignaciones', payload)
    cancelarForm()
    cargar()
  } catch (e: any) {
    formError.value = e.response?.data?.detail || 'Error al crear la asignación'
  } finally { creando.value = false }
}

onMounted(cargar)
</script>
