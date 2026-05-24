<template>
  <div class="space-y-6">
    <PageHeader
      title="Estudiantes"
      description="Gestión de estudiantes matriculados en la escuela."
      icon="pi pi-users"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Estudiantes' }]"
    >
      <template #actions>
        <Button label="Exportar" icon="pi pi-download" outlined />
        <Button label="Nuevo estudiante" icon="pi pi-plus" @click="navigateToForm()" />
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
      <StatCard label="Total" :value="stats.total" icon="pi pi-users" color="brand" />
      <StatCard label="Pre-matriculados" :value="stats.preMatriculados" icon="pi pi-clock" color="warning" hint="Pendientes de matricular" />
      <StatCard label="Matriculados" :value="stats.matriculados" icon="pi pi-check-circle" color="success" hint="Pagaron matrícula" />
      <StatCard label="Cursando" :value="stats.cursando" icon="pi pi-book" color="info" hint="Con clases asignadas" />
      <StatCard label="Completados" :value="stats.completados" icon="pi pi-graduation-cap" color="brand" />
    </div>

    <!-- Aviso de morosos / pendientes de matrícula -->
    <div v-if="stats.enMora > 0 || stats.pendienteMatricula > 0" class="card animate-fade-up p-4 border-l-4 !border-l-warning-600">
      <div class="flex items-start gap-3 flex-wrap">
        <div class="w-10 h-10 rounded-lg bg-warning-50 text-warning-700 flex items-center justify-center flex-shrink-0">
          <i class="pi pi-exclamation-triangle" />
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="text-sm font-semibold text-ink-900">Atención financiera</h3>
          <p class="text-xs text-ink-600 mt-0.5">
            <span v-if="stats.pendienteMatricula > 0">
              <strong>{{ stats.pendienteMatricula }}</strong> estudiante(s) sin matrícula emitida.
            </span>
            <span v-if="stats.enMora > 0" class="ml-2">
              <strong class="text-danger-700">{{ stats.enMora }}</strong> en mora.
            </span>
          </p>
        </div>
        <Button
          v-if="stats.pendienteMatricula > 0 || stats.enMora > 0"
          label="Ir a Cobros"
          icon="pi pi-wallet"
          size="small"
          outlined
          @click="router.push('/cobros')"
        />
      </div>
    </div>

    <DataTableCard>
      <template #toolbar>
        <span class="p-input-icon-left">
          <i class="pi pi-search text-ink-400" />
          <InputText
            v-model="searchTerm"
            placeholder="Buscar por nombre, cédula o email..."
            class="!pl-10 w-72"
            @keyup.enter="buscar"
          />
        </span>
        <Dropdown
          v-model="filtroEstado"
          :options="estadosFilter"
          option-label="label"
          option-value="value"
          placeholder="Todos los estados"
          showClear
          @change="cargar"
          class="w-52"
        />
        <Dropdown
          v-model="filtroSituacionPago"
          :options="situacionesFilter"
          option-label="label"
          option-value="value"
          placeholder="Situación pago"
          showClear
          @change="cargar"
          class="w-52"
        />
        <Button icon="pi pi-filter-slash" outlined @click="limpiar" v-tooltip="'Limpiar filtros'" />
      </template>

      <EmptyState
        v-if="!loading && estudiantes.length === 0"
        icon="pi pi-users"
        title="Aún no hay estudiantes"
        description="Comienza matriculando al primer estudiante de tu escuela."
      >
        <template #action>
          <Button label="Matricular estudiante" icon="pi pi-plus" @click="navigateToForm()" />
        </template>
      </EmptyState>

      <DataTable
        v-else
        :value="estudiantes"
        :loading="loading"
        striped-rows
        :rowHover="true"
        :pt="{ table: { style: 'min-width: 70rem' } }"
      >
        <Column header="Estudiante" :sortable="false">
          <template #body="{ data }">
            <div class="flex items-center gap-3">
              <Avatar :name="data.nombreCompleto || `${data.nombre} ${data.apellido}`" size="md" />
              <div>
                <p class="text-sm font-semibold text-ink-900">
                  {{ data.nombreCompleto || `${data.nombre ?? ''} ${data.apellido ?? ''}`.trim() }}
                </p>
                <p class="text-xs text-ink-500">{{ data.email }}</p>
              </div>
            </div>
          </template>
        </Column>
        <Column field="cedula" header="Cédula" />
        <Column field="telefono" header="Teléfono" />
        <Column header="Estado académico">
          <template #body="{ data }">
            <StatusBadge :status="data.estado" />
          </template>
        </Column>
        <Column header="Situación pago">
          <template #body="{ data }">
            <StatusBadge :status="data.situacionPago || 'SIN_DEUDA'" />
          </template>
        </Column>
        <Column header="Matrícula">
          <template #body="{ data }">
            <span class="text-sm text-ink-600">{{ data.fechaMatricula || '—' }}</span>
          </template>
        </Column>
        <Column header="" style="width: 180px">
          <template #body="{ data }">
            <div class="flex items-center justify-end gap-1">
              <Button icon="pi pi-eye" rounded text size="small" v-tooltip.top="'Ver detalle'" @click="navigateToDetail(data.id)" />
              <Button icon="pi pi-pencil" rounded text size="small" v-tooltip.top="'Editar datos'" @click="navigateToForm(data.id)" />
              <Button icon="pi pi-sync" rounded text size="small" v-tooltip.top="'Cambiar estado'" @click="abrirCambioEstado(data)" />
              <Button icon="pi pi-trash" rounded text size="small" severity="danger" v-tooltip.top="'Eliminar'" @click="confirmarEliminar(data)" />
            </div>
          </template>
        </Column>
      </DataTable>

      <!-- Dialog Cambiar Estado -->
      <Dialog v-model:visible="mostrarEstado" modal header="Cambiar estado del estudiante" :style="{ width: '580px' }">
        <div v-if="estudianteEstado" class="space-y-5">
          <div class="rounded-lg bg-ink-50 border border-ink-200 p-3 flex items-center gap-3">
            <Avatar :name="estudianteEstado.nombreCompleto || `${estudianteEstado.nombre} ${estudianteEstado.apellido}`" size="md" />
            <div class="flex-1">
              <p class="text-sm font-semibold text-ink-900">
                {{ estudianteEstado.nombreCompleto || `${estudianteEstado.nombre} ${estudianteEstado.apellido}` }}
              </p>
              <p class="text-xs text-ink-500">{{ estudianteEstado.cedula }} · {{ estudianteEstado.email }}</p>
            </div>
            <StatusBadge :status="estudianteEstado.estado" />
          </div>

          <div v-if="estadoError" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 text-sm text-danger-600">
            {{ estadoError }}
          </div>

          <div class="rounded-lg bg-info-50 border border-info-500/20 p-3 text-xs text-info-700 flex items-start gap-2">
            <i class="pi pi-info-circle mt-0.5" />
            <span>
              <strong>Recuerda:</strong> Algunas transiciones son automáticas:
              <span class="font-medium">PRE_MATRICULADO → MATRICULADO</span> al primer pago,
              <span class="font-medium">MATRICULADO → CURSANDO</span> al crear la primera asignación.
              Sólo necesitas forzar manualmente COMPLETADO o RETIRADO.
            </span>
          </div>

          <div>
            <p class="label mb-2">Selecciona el nuevo estado:</p>
            <div class="space-y-2">
              <button
                v-for="opcion in estadosDisponibles(estudianteEstado.estado)"
                :key="opcion.value"
                type="button"
                @click="nuevoEstado = opcion.value"
                :class="['w-full flex items-start gap-3 p-3 rounded-lg border-2 text-left transition',
                  nuevoEstado === opcion.value
                    ? 'border-brand-600 bg-brand-50/50'
                    : 'border-ink-200 hover:border-brand-300']"
              >
                <div :class="['w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0',
                  opcion.color === 'success' ? 'bg-success-50 text-success-600' :
                  opcion.color === 'info'    ? 'bg-info-50 text-info-600' :
                  opcion.color === 'warning' ? 'bg-warning-50 text-warning-600' :
                                              'bg-ink-100 text-ink-600']">
                  <i :class="opcion.icon" />
                </div>
                <div class="flex-1">
                  <p class="text-sm font-semibold text-ink-900">{{ opcion.label }}</p>
                  <p class="text-xs text-ink-500 mt-0.5">{{ opcion.descripcion }}</p>
                </div>
                <i v-if="nuevoEstado === opcion.value" class="pi pi-check-circle text-brand-700 text-lg" />
              </button>
            </div>
            <p v-if="estadosDisponibles(estudianteEstado.estado).length === 0" class="text-xs text-ink-500 mt-2">
              Este estudiante ya completó o se retiró. No hay transiciones disponibles.
            </p>
          </div>
        </div>
        <template #footer>
          <Button label="Cancelar" outlined @click="mostrarEstado = false" />
          <Button label="Aplicar cambio" icon="pi pi-check" :loading="cambiandoEstado" :disabled="!nuevoEstado" @click="confirmarCambioEstado" />
        </template>
      </Dialog>

      <Toast />

      <template #footer>
        <div class="flex items-center justify-between text-sm">
          <p class="muted">
            Mostrando <span class="text-ink-700 font-medium">{{ estudiantes.length }}</span> de
            <span class="text-ink-700 font-medium">{{ totalEstudiantes }}</span> estudiantes
          </p>
          <Paginator
            :rows="pageSize"
            :totalRecords="totalEstudiantes"
            :first="currentPage * pageSize"
            @page="onPageChange"
            template="PrevPageLink PageLinks NextPageLink"
          />
        </div>
      </template>
    </DataTableCard>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Paginator from 'primevue/paginator'
import Tooltip from 'primevue/tooltip'
import Dialog from 'primevue/dialog'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import estudiantesService from '@/services/estudiantes'
import api from '@/services/api'
import { humanLabel } from '@/utils/labels'

const vTooltip = Tooltip
const router = useRouter()

const estudiantes = ref<any[]>([])
const loading = ref(false)
const searchTerm = ref('')
const filtroEstado = ref<string | null>(null)
const filtroSituacionPago = ref<string | null>(null)
const totalEstudiantes = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)

const stats = reactive({
  total: 0,
  preMatriculados: 0,
  matriculados: 0,
  cursando: 0,
  completados: 0,
  enMora: 0,
  pendienteMatricula: 0
})

const estadosFilter = [
  { label: 'Pre-matriculado', value: 'PRE_MATRICULADO' },
  { label: 'Matriculado',     value: 'MATRICULADO' },
  { label: 'Cursando',        value: 'CURSANDO' },
  { label: 'Completado',      value: 'COMPLETADO' },
  { label: 'Retirado',        value: 'RETIRADO' }
]

const situacionesFilter = [
  { label: 'Pendiente matrícula', value: 'PENDIENTE_MATRICULA' },
  { label: 'Sin deuda',            value: 'SIN_DEUDA' },
  { label: 'Pago parcial',         value: 'PAGO_PARCIAL' },
  { label: 'Al día',               value: 'AL_DIA' },
  { label: 'En mora',              value: 'EN_MORA' },
  { label: 'Pagado total',         value: 'PAGADO_TOTAL' }
]

const cargar = async () => {
  try {
    loading.value = true
    const response = await estudiantesService.obtenerEstudiantes(currentPage.value, pageSize.value)
    let data = response.content
    if (filtroEstado.value) data = data.filter((e: any) => e.estado === filtroEstado.value)
    if (filtroSituacionPago.value) data = data.filter((e: any) => e.situacionPago === filtroSituacionPago.value)
    estudiantes.value = data
    totalEstudiantes.value = response.totalElements

    // Estadísticas globales (sin filtro)
    const all = await estudiantesService.obtenerEstudiantes(0, 500)
    const list = all.content
    stats.total              = all.totalElements
    stats.preMatriculados    = list.filter((e: any) => e.estado === 'PRE_MATRICULADO').length
    // Compatibilidad transitoria: ACTIVO legado se cuenta como MATRICULADO
    stats.matriculados       = list.filter((e: any) => e.estado === 'MATRICULADO' || e.estado === 'ACTIVO').length
    stats.cursando           = list.filter((e: any) => e.estado === 'CURSANDO').length
    stats.completados        = list.filter((e: any) => e.estado === 'COMPLETADO').length
    stats.enMora             = list.filter((e: any) => e.situacionPago === 'EN_MORA').length
    stats.pendienteMatricula = list.filter((e: any) => e.situacionPago === 'PENDIENTE_MATRICULA').length
  } finally {
    loading.value = false
  }
}

const buscar = async () => {
  if (!searchTerm.value.trim()) return cargar()
  try {
    loading.value = true
    estudiantes.value = await estudiantesService.buscarEstudiantes(searchTerm.value) as any[]
    totalEstudiantes.value = estudiantes.value.length
  } finally {
    loading.value = false
  }
}

const limpiar = () => {
  searchTerm.value = ''
  filtroEstado.value = null
  filtroSituacionPago.value = null
  currentPage.value = 0
  cargar()
}

const onPageChange = (e: any) => {
  currentPage.value = e.page
  cargar()
}

const confirmarEliminar = async (est: any) => {
  if (!confirm(`¿Eliminar a ${est.nombreCompleto}?`)) return
  try {
    await estudiantesService.eliminarEstudiante(est.id)
    cargar()
  } catch (e) { console.error(e) }
}

const navigateToForm = (id?: number) => router.push(id ? `/estudiantes/${id}/editar` : '/estudiantes/nuevo')
const navigateToDetail = (id: number) => router.push(`/estudiantes/${id}`)

// ============ CAMBIO DE ESTADO ============
const toast = useToast()
const mostrarEstado = ref(false)
const estudianteEstado = ref<any>(null)
const nuevoEstado = ref<string>('')
const cambiandoEstado = ref(false)
const estadoError = ref('')

interface EstadoOpcion {
  value: string
  label: string
  descripcion: string
  icon: string
  color: 'success' | 'info' | 'warning' | 'ink'
}

const TRANSICIONES: Record<string, EstadoOpcion[]> = {
  PRE_MATRICULADO: [
    { value: 'MATRICULADO', label: 'Matricular',  descripcion: 'Forzar matriculación sin esperar el pago (normalmente esto ocurre automático al registrar el primer pago).', icon: 'pi pi-check-circle', color: 'success' },
    { value: 'RETIRADO',    label: 'Retirar',     descripcion: 'El estudiante no concretó la matrícula y se retira del sistema.',                                            icon: 'pi pi-times-circle', color: 'ink' }
  ],
  MATRICULADO: [
    { value: 'CURSANDO',   label: 'Marcar como cursando', descripcion: 'El estudiante ya inició clases (normalmente automático al crear la primera asignación).', icon: 'pi pi-book',          color: 'info' },
    { value: 'COMPLETADO', label: 'Marcar completado',    descripcion: 'Terminó todas las horas y aprobó el curso.',                                              icon: 'pi pi-graduation-cap',color: 'info' },
    { value: 'RETIRADO',   label: 'Retirar',              descripcion: 'El estudiante abandona el curso.',                                                        icon: 'pi pi-times-circle',  color: 'ink' }
  ],
  CURSANDO: [
    { value: 'COMPLETADO', label: 'Marcar completado', descripcion: 'Terminó todas las horas y aprobó el curso.', icon: 'pi pi-graduation-cap', color: 'info' },
    { value: 'RETIRADO',   label: 'Retirar',           descripcion: 'El estudiante abandonó el curso.',           icon: 'pi pi-times-circle',  color: 'ink' }
  ],
  // Legado: estudiantes en estado ACTIVO (antes de la migración V2 de estudiantes)
  ACTIVO: [
    { value: 'MATRICULADO', label: 'Pasar a MATRICULADO (renombre)', descripcion: 'Migra al nuevo modelo de estados.', icon: 'pi pi-refresh',  color: 'success' },
    { value: 'CURSANDO',    label: 'Marcar como cursando',           descripcion: 'Ya está tomando clases.',           icon: 'pi pi-book',     color: 'info' },
    { value: 'COMPLETADO',  label: 'Marcar completado',              descripcion: 'Terminó el curso.',                 icon: 'pi pi-graduation-cap', color: 'info' },
    { value: 'RETIRADO',    label: 'Retirar',                        descripcion: 'El estudiante abandonó.',           icon: 'pi pi-times-circle', color: 'ink' }
  ],
  COMPLETADO: [],
  RETIRADO: [
    { value: 'MATRICULADO', label: 'Reactivar', descripcion: 'Re-matricular al estudiante (vuelve a recibir clases).', icon: 'pi pi-refresh', color: 'success' }
  ]
}

const estadosDisponibles = (actual: string): EstadoOpcion[] => TRANSICIONES[actual] || []

const abrirCambioEstado = (est: any) => {
  estudianteEstado.value = est
  nuevoEstado.value = ''
  estadoError.value = ''
  mostrarEstado.value = true
}

const confirmarCambioEstado = async () => {
  if (!estudianteEstado.value || !nuevoEstado.value) return
  estadoError.value = ''
  try {
    cambiandoEstado.value = true
    await api.put(`/estudiantes/${estudianteEstado.value.id}`, { estado: nuevoEstado.value })
    toast.add({
      severity: 'success',
      summary: 'Estado actualizado',
      detail: `Ahora está en estado ${humanLabel(nuevoEstado.value)}`,
      life: 4000
    })
    mostrarEstado.value = false
    cargar()
  } catch (e: any) {
    const data = e.response?.data
    estadoError.value = data?.errors
      ? Object.values(data.errors).join(' · ')
      : (data?.detail || 'No se pudo cambiar el estado')
  } finally { cambiandoEstado.value = false }
}

onMounted(cargar)
</script>
