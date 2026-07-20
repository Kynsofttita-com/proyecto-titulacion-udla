<template>
  <div class="space-y-6">
    <PageHeader
      :title="(esInstructor || esEstudiante) ? 'Mis clases' : 'Asignaciones de clases'"
      :description="esEstudiante ? 'Tus clases programadas y completadas.' : (esInstructor ? 'Tus clases programadas y en curso.' : 'Programación tripartita: estudiante + instructor + vehículo.')"
      icon="pi pi-calendar"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: (esInstructor || esEstudiante) ? 'Mis clases' : 'Asignaciones' }]"
    >
      <template #actions>
        <Button label="Vista lista" icon="pi pi-list" outlined @click="vista = 'lista'" :severity="vista === 'lista' ? 'primary' : 'secondary'" />
        <Button label="Vista calendario" icon="pi pi-calendar" outlined @click="vista = 'calendario'" :severity="vista === 'calendario' ? 'primary' : 'secondary'" />
        <Button v-if="!esInstructor && !esEstudiante" label="Nueva asignación" icon="pi pi-plus" @click="mostrarForm = true" />
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
          :description="(esInstructor || esEstudiante) ? 'Selecciona otro día en el calendario.' : 'Selecciona otro día o crea una nueva asignación.'"
        >
          <template #action>
            <Button v-if="!esInstructor && !esEstudiante" label="Crear asignación" icon="pi pi-plus" @click="mostrarForm = true" />
          </template>
        </EmptyState>
        <ul v-else class="divide-y divide-ink-200">
          <li v-for="a in asignacionesDelDia" :key="a.id" class="flex items-center gap-4 px-6 py-4 hover:bg-ink-50/50 transition">
            <div class="w-16 text-center flex-shrink-0">
              <p class="text-xl font-bold text-brand-700">{{ extraerHora(a) }}</p>
              <p class="text-[10px] text-ink-500 uppercase tracking-wider">{{ a.duracionMinutos || calcDuracion(a) }} min</p>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <p class="text-sm font-semibold text-ink-900">{{ nombreEstudiante(a.estudianteId) }}</p>
                <span class="text-xs text-ink-400">·</span>
                <span class="text-xs text-ink-500">{{ a.tipoClase || 'PRÁCTICA' }}</span>
                <span class="text-[10px] text-ink-400 ml-auto">Clase #{{ a.id }}</span>
              </div>
              <p class="text-xs text-ink-500 mt-0.5 flex items-center gap-2 flex-wrap">
                <span class="inline-flex items-center gap-1">
                  <i class="pi pi-id-card text-[10px]" />
                  <span class="text-ink-700">{{ nombreInstructor(a.instructorId) }}</span>
                </span>
                <span class="text-ink-300">·</span>
                <span class="inline-flex items-center gap-1">
                  <i class="pi pi-car text-[10px]" />
                  <span class="text-ink-700 font-mono">{{ placaVehiculo(a.vehiculoId) }}</span>
                </span>
              </p>
              <p v-if="a.kmInicial != null || a.kmFinal != null" class="text-xs text-info-600 mt-0.5">
                <i class="pi pi-car text-xs mr-1" />
                km {{ a.kmInicial ?? '?' }} → {{ a.kmFinal ?? '?' }}
                <span v-if="a.kmInicial != null && a.kmFinal != null" class="text-success-700 font-medium ml-1">
                  ({{ a.kmFinal - a.kmInicial }} km recorridos)
                </span>
              </p>
            </div>
            <div class="flex items-center gap-2 flex-shrink-0">
              <StatusBadge :status="a.estado" />
              <Button
                v-if="puedeIniciar(a)"
                label="Iniciar"
                icon="pi pi-play"
                size="small"
                severity="info"
                @click="abrirDialogIniciar(a)"
              />
              <Button
                v-if="puedeFinalizar(a)"
                label="Finalizar"
                icon="pi pi-check"
                size="small"
                severity="success"
                @click="abrirDialogFinalizar(a)"
              />
            </div>
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
            <p class="text-sm font-medium">{{ extraerFechaCorta(data) }}</p>
            <p class="text-xs text-ink-500">{{ extraerHora(data) }} · {{ data.duracionMinutos || calcDuracion(data) }}min</p>
          </template>
        </Column>
        <Column header="Estudiante">
          <template #body="{ data }">
            <span class="text-sm font-medium text-ink-900">{{ nombreEstudiante(data.estudianteId) }}</span>
          </template>
        </Column>
        <Column header="Instructor">
          <template #body="{ data }">
            <span class="text-sm text-ink-700">{{ nombreInstructor(data.instructorId) }}</span>
          </template>
        </Column>
        <Column header="Vehículo">
          <template #body="{ data }">
            <span class="text-sm font-mono text-ink-700">{{ placaVehiculo(data.vehiculoId) }}</span>
          </template>
        </Column>
        <Column field="tipoClase" header="Tipo" />
        <Column header="Recorrido">
          <template #body="{ data }">
            <span v-if="data.kmInicial != null && data.kmFinal != null" class="text-xs text-success-700 font-medium">
              {{ data.kmFinal - data.kmInicial }} km
            </span>
            <span v-else-if="data.kmInicial != null" class="text-xs text-info-600">
              Desde {{ data.kmInicial.toLocaleString('es-EC') }}
            </span>
            <span v-else class="text-ink-400 text-xs">—</span>
          </template>
        </Column>
        <Column header="Estado">
          <template #body="{ data }">
            <StatusBadge :status="data.estado" />
          </template>
        </Column>
        <Column header="" style="width: 200px">
          <template #body="{ data }">
            <div class="flex items-center gap-1 justify-end">
              <Button v-if="puedeIniciar(data)" label="Iniciar" icon="pi pi-play" size="small" severity="info" outlined @click="abrirDialogIniciar(data)" />
              <Button v-if="puedeFinalizar(data)" label="Finalizar" icon="pi pi-check" size="small" severity="success" outlined @click="abrirDialogFinalizar(data)" />
            </div>
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

    <!-- Dialog: Iniciar clase (km_inicial) -->
    <Dialog v-model:visible="dialogIniciarVisible" modal header="Iniciar clase" :style="{ width: '460px' }">
      <div v-if="asignacionActual" class="space-y-4">
        <div class="rounded-lg bg-info-50 border border-info-200 p-3 text-xs text-ink-700">
          <p><strong>Clase #{{ asignacionActual.id }}</strong> · Vehículo {{ placaVehiculo(asignacionActual.vehiculoId) }}</p>
          <p class="text-[11px] text-ink-500 mt-0.5">Estudiante: {{ nombreEstudiante(asignacionActual.estudianteId) }}</p>
          <p class="mt-1">Registra el kilometraje del odómetro <strong>antes</strong> de empezar la clase. Si lo dejas vacío, usaremos el kilometraje actual del vehículo ({{ kmActualVehiculo ?? '—' }} km).</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Kilometraje inicial</label>
          <InputNumber v-model="formIniciar.kmInicial" :min="0" suffix=" km" :placeholder="kmActualVehiculo ? `${kmActualVehiculo} km (actual del vehículo)` : 'km del odómetro'" class="w-full" />
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Observaciones <span class="text-xs text-ink-500">(opcional)</span></label>
          <Textarea v-model="formIniciar.observaciones" rows="2" placeholder="Estado del vehículo, condiciones, etc." class="w-full" />
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogIniciarVisible = false" :disabled="iniciando" />
        <Button label="Iniciar clase" icon="pi pi-play" severity="info" :loading="iniciando" @click="confirmarIniciar" />
      </template>
    </Dialog>

    <!-- Dialog: Finalizar clase (km_final) -->
    <Dialog v-model:visible="dialogFinalizarVisible" modal header="Finalizar clase" :style="{ width: '480px' }">
      <div v-if="asignacionActual" class="space-y-4">
        <div class="rounded-lg bg-success-50 border border-success-200 p-3 text-xs text-ink-700">
          <p><strong>Clase #{{ asignacionActual.id }}</strong> · Vehículo {{ placaVehiculo(asignacionActual.vehiculoId) }}</p>
          <p class="text-[11px] text-ink-500 mt-0.5">Estudiante: {{ nombreEstudiante(asignacionActual.estudianteId) }}</p>
          <p v-if="asignacionActual.kmInicial != null" class="mt-1">
            km inicial registrado: <strong>{{ asignacionActual.kmInicial.toLocaleString('es-EC') }}</strong>
          </p>
          <p v-else class="mt-1 text-warning-700">
            <i class="pi pi-exclamation-triangle text-xs mr-1" />
            Sin km inicial registrado — al finalizar se asumirá el km actual del vehículo como inicial.
          </p>
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Kilometraje final *</label>
          <InputNumber
            v-model="formFinalizar.kmFinal"
            :min="asignacionActual.kmInicial ?? 0"
            suffix=" km"
            placeholder="km del odómetro al terminar"
            class="w-full"
          />
          <p v-if="kmRecorridosPreview != null" class="text-xs text-success-700 mt-1 font-medium">
            <i class="pi pi-arrow-right text-xs mr-1" />
            Recorrido: {{ kmRecorridosPreview }} km
          </p>
        </div>
        <div>
          <label class="block text-sm font-medium text-ink-700 mb-1.5">Observaciones del recorrido <span class="text-xs text-ink-500">(opcional)</span></label>
          <Textarea v-model="formFinalizar.observacionesRecorrido" rows="3" placeholder="Desempeño del estudiante, incidentes, etc." class="w-full" maxlength="500" />
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="dialogFinalizarVisible = false" :disabled="finalizando" />
        <Button label="Finalizar clase" icon="pi pi-check" severity="success" :loading="finalizando" @click="confirmarFinalizar" />
      </template>
    </Dialog>

    <Toast />
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
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
import asignacionesService from '@/services/asignaciones'
import vehiculosService from '@/services/vehiculos'

const authStore = useAuthStore()
const esInstructor = computed(() => authStore.currentRole === 'INSTRUCTOR')
const esEstudiante = computed(() => authStore.currentRole === 'ESTUDIANTE')
// Vista solo-lectura: aplica a ESTUDIANTE (sin crear/editar/cancelar/iniciar/finalizar)
const soloLectura = computed(() => esEstudiante.value)

const toast = useToast()

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

// Maps para mostrar nombres en lugar de IDs en las tarjetas/tabla.
// Se cargan una sola vez al inicio (poco volumen, no vale la pena cachear).
const mapEstudiantes = ref<Map<number, string>>(new Map())
const mapInstructores = ref<Map<number, string>>(new Map())
const mapVehiculos = ref<Map<number, string>>(new Map())

const nombreEstudiante = (id: number) => mapEstudiantes.value.get(id) || `Estudiante #${id}`
const nombreInstructor = (id: number) => mapInstructores.value.get(id) || `Instructor #${id}`
const placaVehiculo    = (id: number) => mapVehiculos.value.get(id) || `Vehículo #${id}`
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
    // Estudiantes que pueden recibir clases: MATRICULADO (aun sin clases) o
    // CURSANDO (ya en progreso). El backend rechaza los demas estados.
    // NOTA: antes filtrabamos por 'ACTIVO' (que no existe como estado de
    // estudiante) y omitiamos 'CURSANDO', asi que despues de agendar la
    // primera clase el estudiante desaparecia del selector.
    estudiantesActivos.value = (estRes.data.content || [])
      .filter((e: any) => e.estado === 'MATRICULADO' || e.estado === 'CURSANDO')
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

// El backend devuelve `fecha` (LocalDate "YYYY-MM-DD"), `horaInicio` y `horaFin`
// (LocalTime "HH:MM:SS") como campos separados. Estas funciones aceptan tanto
// el item de asignacion como un string legacy (compatibilidad).
const extraerHora = (a: any) => {
  if (!a) return '--:--'
  if (typeof a === 'string') return a.length >= 16 ? a.substring(11, 16) : a.substring(0, 5)
  return (a.horaInicio || '').substring(0, 5) || '--:--'
}
const extraerFechaCorta = (a: any) => {
  if (!a) return '—'
  if (typeof a === 'string') return a.substring(0, 10)
  return a.fecha || '—'
}
const formatearFecha = (d: Date) => d.toLocaleDateString('es-EC', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })

// Calcula duracion en minutos a partir de horaInicio y horaFin
const calcDuracion = (a: any) => {
  if (!a?.horaInicio || !a?.horaFin) return 60
  const hi = a.horaInicio.substring(0, 5).split(':').map(Number)
  const hf = a.horaFin.substring(0, 5).split(':').map(Number)
  return (hf[0] * 60 + hf[1]) - (hi[0] * 60 + hi[1])
}

const asignacionesDelDia = computed(() => {
  const f = fmtFecha(fechaSeleccionada.value)
  return asignaciones.value.filter(a => a.fecha === f)
})

const cargar = async () => {
  try {
    loading.value = true

    // Cargar en paralelo: asignaciones + catalogos para lookup de nombres
    const [asigRes, estRes, insRes, vehRes] = await Promise.allSettled([
      api.get('/asignaciones', { params: { size: 500 } }),
      api.get('/estudiantes',  { params: { size: 500 } }),
      api.get('/instructores', { params: { size: 200 } }),
      api.get('/vehiculos',    { params: { size: 200 } })
    ])

    // Construir maps id -> nombre/placa
    if (estRes.status === 'fulfilled') {
      const items = estRes.value.data.content || []
      mapEstudiantes.value = new Map(items.map((e: any) => [
        e.id,
        e.nombreCompleto || `${e.nombre ?? ''} ${e.apellido ?? ''}`.trim() || `Estudiante #${e.id}`
      ]))
    }
    if (insRes.status === 'fulfilled') {
      const items = insRes.value.data.content || []
      mapInstructores.value = new Map(items.map((i: any) => [
        i.id,
        i.nombreCompleto || `${i.nombre ?? ''} ${i.apellido ?? ''}`.trim() || `Instructor #${i.id}`
      ]))
    }
    if (vehRes.status === 'fulfilled') {
      const items = vehRes.value.data.content || []
      mapVehiculos.value = new Map(items.map((v: any) => [
        v.id,
        v.placa ? `${v.placa}${v.marca ? ` · ${v.marca} ${v.modelo ?? ''}`.trim() : ''}` : `Vehículo #${v.id}`
      ]))
    }

    let lista = asigRes.status === 'fulfilled' ? (asigRes.value.data.content || []) : []

    // INSTRUCTOR: solo ve sus propias asignaciones. Filtrado client-side
    // por simplicidad (volumen esperado pequeño en MVP). Cuando crezca
    // se puede agregar param ?instructorId= al endpoint.
    if (esInstructor.value) {
      if (!authStore.currentInstructorId) {
        await authStore.loadInstructorId()
      }
      const miId = authStore.currentInstructorId
      lista = miId ? lista.filter((a: any) => a.instructorId === miId) : []
    }
    // ESTUDIANTE: solo ve sus propias clases.
    if (esEstudiante.value) {
      if (!authStore.currentEstudianteId) {
        await authStore.loadEstudianteId()
      }
      const miEstId = authStore.currentEstudianteId
      lista = miEstId ? lista.filter((a: any) => a.estudianteId === miEstId) : []
    }

    asignaciones.value = lista
    stats.total = lista.length
    stats.confirmadas = lista.filter((a: any) => a.estado === 'CONFIRMADA').length
    stats.programadas = lista.filter((a: any) => a.estado === 'PROGRAMADA').length
    const ahora = new Date()
    const finSem = new Date(ahora); finSem.setDate(ahora.getDate() + 7)
    stats.semana = lista.filter((a: any) => {
      const f = new Date((a.fecha || '') + 'T' + (a.horaInicio || '00:00'))
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

// =========================================================================
//  KILOMETRAJE: iniciar / finalizar clase
// =========================================================================

const dialogIniciarVisible = ref(false)
const dialogFinalizarVisible = ref(false)
const iniciando = ref(false)
const finalizando = ref(false)
const asignacionActual = ref<any>(null)
const kmActualVehiculo = ref<number | null>(null)

const formIniciar = reactive<{ kmInicial: number | null; observaciones: string }>({
  kmInicial: null,
  observaciones: ''
})
const formFinalizar = reactive<{ kmFinal: number | null; observacionesRecorrido: string }>({
  kmFinal: null,
  observacionesRecorrido: ''
})

const kmRecorridosPreview = computed(() => {
  if (!asignacionActual.value) return null
  const kmI = asignacionActual.value.kmInicial ?? kmActualVehiculo.value
  if (kmI == null || formFinalizar.kmFinal == null) return null
  const diff = formFinalizar.kmFinal - kmI
  return diff >= 0 ? diff : null
})

const puedeIniciar = (a: any): boolean => {
  // ESTUDIANTE no puede iniciar — solo lectura
  if (soloLectura.value) return false
  // Se puede iniciar si está PROGRAMADA o CONFIRMADA y aún no fue iniciada (sin km_inicial)
  const estado = a?.estado
  return (estado === 'PROGRAMADA' || estado === 'CONFIRMADA') && a?.kmInicial == null
}

const puedeFinalizar = (a: any): boolean => {
  if (soloLectura.value) return false
  // Se puede finalizar si está EN_CURSO, o si está CONFIRMADA pero el instructor olvidó iniciar
  const estado = a?.estado
  return estado === 'EN_CURSO' || estado === 'CONFIRMADA' || estado === 'PROGRAMADA'
}

const abrirDialogIniciar = async (a: any) => {
  asignacionActual.value = a
  formIniciar.kmInicial = null
  formIniciar.observaciones = ''
  kmActualVehiculo.value = null
  try {
    const v = await vehiculosService.obtenerVehiculo(a.vehiculoId)
    kmActualVehiculo.value = v.kilometraje ?? 0
  } catch { /* ignore */ }
  dialogIniciarVisible.value = true
}

const abrirDialogFinalizar = async (a: any) => {
  asignacionActual.value = a
  formFinalizar.kmFinal = null
  formFinalizar.observacionesRecorrido = ''
  if (a.kmInicial == null) {
    try {
      const v = await vehiculosService.obtenerVehiculo(a.vehiculoId)
      kmActualVehiculo.value = v.kilometraje ?? 0
    } catch { /* ignore */ }
  }
  dialogFinalizarVisible.value = true
}

const confirmarIniciar = async () => {
  if (!asignacionActual.value) return
  iniciando.value = true
  try {
    const r = await asignacionesService.iniciarAsignacion(asignacionActual.value.id, {
      kmInicial: formIniciar.kmInicial,
      observaciones: formIniciar.observaciones?.trim() || undefined
    })
    toast.add({
      severity: 'success',
      summary: 'Clase iniciada',
      detail: `Clase #${r.asignacionId} en curso (km inicial: ${r.kmInicial})`,
      life: 3000
    })
    dialogIniciarVisible.value = false
    await cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo iniciar la clase',
      life: 4000
    })
  } finally {
    iniciando.value = false
  }
}

const confirmarFinalizar = async () => {
  if (!asignacionActual.value) return
  if (formFinalizar.kmFinal == null || formFinalizar.kmFinal < 0) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Kilometraje final es requerido', life: 3000 })
    return
  }
  finalizando.value = true
  try {
    const r = await asignacionesService.finalizarAsignacion(asignacionActual.value.id, {
      kmFinal: Number(formFinalizar.kmFinal),
      observacionesRecorrido: formFinalizar.observacionesRecorrido?.trim() || undefined
    })
    const detalle = r.syncVehiculoExitoso
      ? `${r.kmRecorridos} km recorridos. Odómetro del vehículo actualizado.`
      : `${r.kmRecorridos} km recorridos. ${r.mensajeSyncVehiculo || ''}`
    toast.add({
      severity: r.syncVehiculoExitoso ? 'success' : 'warn',
      summary: 'Clase finalizada',
      detail: detalle,
      life: 5000
    })
    dialogFinalizarVisible.value = false
    await cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudo finalizar la clase',
      life: 4000
    })
  } finally {
    finalizando.value = false
  }
}

onMounted(cargar)
</script>
