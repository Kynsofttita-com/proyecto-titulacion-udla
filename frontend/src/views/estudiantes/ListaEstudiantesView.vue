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

    <!-- Stats clickeables (aplican filtro por estado académico) -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
      <button
        type="button"
        @click="aplicarFiltroStat('total')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'total' ? 'ring-2 ring-brand-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="Total" :value="stats.total" icon="pi pi-users" color="brand" />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('preMatriculados')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'preMatriculados' ? 'ring-2 ring-warning-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="Pre-matriculados" :value="stats.preMatriculados" icon="pi pi-clock" color="warning" hint="Pendientes de matricular" />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('matriculados')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'matriculados' ? 'ring-2 ring-success-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="Matriculados" :value="stats.matriculados" icon="pi pi-check-circle" color="success" hint="Pagaron matrícula" />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('cursando')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'cursando' ? 'ring-2 ring-info-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="Cursando" :value="stats.cursando" icon="pi pi-book" color="info" hint="Con clases asignadas" />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('completados')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'completados' ? 'ring-2 ring-brand-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="Completados" :value="stats.completados" icon="pi pi-graduation-cap" color="brand" />
      </button>
    </div>

    <!-- Aviso financiero clickeable: sin facturar o con saldo CONTADO pendiente -->
    <div
      v-if="stats.enMora > 0 || stats.pendienteMatricula > 0"
      class="card animate-fade-up p-4 border-l-4 !border-l-warning-600"
    >
      <div class="flex items-start gap-3 flex-wrap">
        <div class="w-10 h-10 rounded-lg bg-warning-50 text-warning-700 flex items-center justify-center flex-shrink-0">
          <i class="pi pi-exclamation-triangle" />
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="text-sm font-semibold text-ink-900">Atención financiera</h3>
          <p class="text-xs text-ink-600 mt-0.5">
            Filtra por situación de pago para gestionarlos rápido.
          </p>
          <div class="flex gap-2 mt-2 flex-wrap">
            <button
              v-if="stats.pendienteMatricula > 0"
              type="button"
              @click="aplicarFiltroPago('PENDIENTE_FACTURACION')"
              :class="['inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium transition-all',
                filtroSituacionPago === 'PENDIENTE_FACTURACION'
                  ? 'bg-warning-600 text-white shadow-md'
                  : 'bg-warning-100 text-warning-800 hover:bg-warning-200']"
            >
              <i class="pi pi-file-edit text-xs" />
              {{ stats.pendienteMatricula }} sin factura emitida
            </button>
            <button
              v-if="stats.enMora > 0"
              type="button"
              @click="aplicarFiltroPago('PENDIENTE_PAGO')"
              :class="['inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium transition-all',
                filtroSituacionPago === 'PENDIENTE_PAGO'
                  ? 'bg-danger-600 text-white shadow-md'
                  : 'bg-danger-100 text-danger-800 hover:bg-danger-200']"
            >
              <i class="pi pi-wallet text-xs" />
              {{ stats.enMora }} con saldo por cobrar
            </button>
          </div>
        </div>
        <Button
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
        <div class="flex flex-wrap items-center gap-3 flex-1">
          <span class="p-input-icon-left">
            <i class="pi pi-search text-ink-400" />
            <InputText
              v-model="searchTerm"
              placeholder="Buscar por nombre, cédula o email..."
              class="!pl-10 w-72"
            />
          </span>
          <Dropdown
            v-model="filtroEstado"
            :options="estadosFilter"
            option-label="label"
            option-value="value"
            placeholder="Estado académico"
            showClear
            class="w-52"
          />
          <Dropdown
            v-model="filtroSituacionPago"
            :options="situacionesFilter"
            option-label="label"
            option-value="value"
            placeholder="Situación pago"
            showClear
            class="w-52"
          />
          <Button
            v-if="hayFiltrosActivos"
            label="Limpiar filtros"
            icon="pi pi-filter-slash"
            outlined
            severity="secondary"
            @click="limpiar"
          />
        </div>
      </template>

      <!-- Banner cuando hay filtro activo -->
      <div
        v-if="hayFiltrosActivos"
        class="mb-4 p-3 rounded-lg bg-info-50 border border-info-200 flex items-center justify-between gap-3 text-sm"
      >
        <div class="flex items-center gap-2 text-info-700 flex-wrap">
          <i class="pi pi-filter" />
          <span class="font-medium">
            Mostrando {{ estudiantesFiltrados.length }} de {{ estudiantes.length }} estudiante{{ estudiantes.length === 1 ? '' : 's' }}
          </span>
          <span v-if="filtroEstado" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">
            Estado: {{ humanLabel(filtroEstado) }}
          </span>
          <span v-if="filtroSituacionPago" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">
            Pago: {{ humanLabel(filtroSituacionPago) }}
          </span>
          <span v-if="searchTerm" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">
            Búsqueda: "{{ searchTerm }}"
          </span>
        </div>
      </div>

      <EmptyState
        v-if="!loading && estudiantesFiltrados.length === 0 && estudiantes.length === 0"
        icon="pi pi-users"
        title="Aún no hay estudiantes"
        description="Comienza matriculando al primer estudiante de tu escuela."
      >
        <template #action>
          <Button label="Matricular estudiante" icon="pi pi-plus" @click="navigateToForm()" />
        </template>
      </EmptyState>

      <EmptyState
        v-else-if="!loading && estudiantesFiltrados.length === 0"
        icon="pi pi-filter"
        title="Sin resultados"
        description="Ningún estudiante coincide con los filtros activos. Prueba ajustarlos."
      >
        <template #action>
          <Button label="Limpiar filtros" icon="pi pi-filter-slash" outlined @click="limpiar" />
        </template>
      </EmptyState>

      <DataTable
        v-else
        :value="estudiantesPagina"
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
            <StatusBadge :status="data.situacionPago || 'PENDIENTE_FACTURACION'" />
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
            Mostrando <span class="text-ink-700 font-medium">{{ estudiantesPagina.length }}</span> de
            <span class="text-ink-700 font-medium">{{ estudiantesFiltrados.length }}</span>
            <span v-if="hayFiltrosActivos"> filtrados (de {{ estudiantes.length }} total)</span>
            <span v-else> estudiantes</span>
          </p>
          <Paginator
            v-if="estudiantesFiltrados.length > pageSize"
            :rows="pageSize"
            :totalRecords="estudiantesFiltrados.length"
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
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
const filtroActivo = ref<string | null>(null)
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
  { label: 'Pendiente facturación', value: 'PENDIENTE_FACTURACION' },
  { label: 'Pendiente de pago',     value: 'PENDIENTE_PAGO' },
  { label: 'Pago parcial',          value: 'PAGO_PARCIAL' },
  { label: 'Pagado total',          value: 'PAGADO_TOTAL' }
]

const cargar = async () => {
  try {
    loading.value = true
    // Una sola llamada con tamaño grande; post-filter + paginación local
    const response = await estudiantesService.obtenerEstudiantes(0, 500)
    estudiantes.value = response.content
    const list = response.content
    stats.total              = response.totalElements
    stats.preMatriculados    = list.filter((e: any) => e.estado === 'PRE_MATRICULADO').length
    stats.matriculados       = list.filter((e: any) => e.estado === 'MATRICULADO' || e.estado === 'ACTIVO').length
    stats.cursando           = list.filter((e: any) => e.estado === 'CURSANDO').length
    stats.completados        = list.filter((e: any) => e.estado === 'COMPLETADO').length
    stats.enMora             = list.filter((e: any) =>
        ['PENDIENTE_PAGO', 'PAGO_PARCIAL', 'EN_MORA'].includes(e.situacionPago)).length
    stats.pendienteMatricula = list.filter((e: any) =>
        ['PENDIENTE_FACTURACION', 'PENDIENTE_MATRICULA'].includes(e.situacionPago)).length
  } finally {
    loading.value = false
  }
}

// ----- Post-filter + búsqueda local -----
const estudiantesFiltrados = computed(() => {
  let resultado = estudiantes.value

  if (filtroEstado.value) {
    if (filtroEstado.value === 'MATRICULADO') {
      // Compatibilidad: ACTIVO legacy cuenta como matriculado
      resultado = resultado.filter((e: any) => e.estado === 'MATRICULADO' || e.estado === 'ACTIVO')
    } else {
      resultado = resultado.filter((e: any) => e.estado === filtroEstado.value)
    }
  }
  if (filtroSituacionPago.value) {
    resultado = resultado.filter((e: any) => e.situacionPago === filtroSituacionPago.value)
  }
  if (searchTerm.value.trim()) {
    const t = searchTerm.value.trim().toLowerCase()
    resultado = resultado.filter((e: any) => {
      const nombreCompleto = (e.nombreCompleto || `${e.nombre || ''} ${e.apellido || ''}`).toLowerCase()
      return (
        nombreCompleto.includes(t) ||
        (e.cedula || '').toLowerCase().includes(t) ||
        (e.email || '').toLowerCase().includes(t)
      )
    })
  }
  return resultado
})

const estudiantesPagina = computed(() => {
  const start = currentPage.value * pageSize.value
  return estudiantesFiltrados.value.slice(start, start + pageSize.value)
})

const hayFiltrosActivos = computed(() =>
  !!(filtroEstado.value || filtroSituacionPago.value || searchTerm.value)
)

// Reset de página cuando cambian los filtros (si la página actual queda fuera de rango)
watch([filtroEstado, filtroSituacionPago, searchTerm], () => {
  currentPage.value = 0
})

// ----- StatCards clickeables -----
const aplicarFiltroStat = (stat: string) => {
  if (filtroActivo.value === stat) {
    filtroActivo.value = null
    filtroEstado.value = null
    return
  }
  filtroActivo.value = stat
  filtroEstado.value = null
  switch (stat) {
    case 'total':           /* sin filtro */                              break
    case 'preMatriculados': filtroEstado.value = 'PRE_MATRICULADO';       break
    case 'matriculados':    filtroEstado.value = 'MATRICULADO';           break
    case 'cursando':        filtroEstado.value = 'CURSANDO';              break
    case 'completados':     filtroEstado.value = 'COMPLETADO';            break
  }
}

const aplicarFiltroPago = (situacion: string) => {
  filtroSituacionPago.value = filtroSituacionPago.value === situacion ? null : situacion
}

const limpiar = () => {
  searchTerm.value = ''
  filtroEstado.value = null
  filtroSituacionPago.value = null
  filtroActivo.value = null
  currentPage.value = 0
}

const onPageChange = (e: any) => {
  currentPage.value = e.page
}

const confirmarEliminar = async (est: any) => {
  if (!confirm(`¿Eliminar a ${est.nombreCompleto || `${est.nombre} ${est.apellido}`}?`)) return
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
