<template>
  <div class="space-y-6">
    <PageHeader
      title="Instructores"
      description="Gestión del personal docente de la escuela."
      icon="pi pi-id-card"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Instructores' }]"
    >
      <template #actions>
        <Button label="Exportar" icon="pi pi-download" outlined />
        <Button label="Nuevo instructor" icon="pi pi-plus" @click="navigateToForm()" />
      </template>
    </PageHeader>

    <!-- Stats clickeables (aplican filtro) -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <button
        type="button"
        @click="aplicarFiltroStat('total')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'total' ? 'ring-2 ring-brand-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="Total" :value="stats.total" icon="pi pi-id-card" color="brand" />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('activos')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'activos' ? 'ring-2 ring-success-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="Activos" :value="stats.activos" icon="pi pi-check-circle" color="success" />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('porVencer')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'porVencer' ? 'ring-2 ring-warning-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard
          label="Licencia próx. a vencer"
          :value="stats.vencen"
          icon="pi pi-exclamation-triangle"
          color="warning"
          hint="Vence en < 60 días"
        />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('vencidas')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'vencidas' ? 'ring-2 ring-danger-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard
          label="Licencias vencidas"
          :value="stats.vencidas"
          icon="pi pi-times-circle"
          color="danger"
          hint="Requieren renovación"
        />
      </button>
    </div>

    <DataTableCard>
      <template #toolbar>
        <div class="flex flex-wrap items-center gap-3 flex-1">
          <span class="p-input-icon-left">
            <i class="pi pi-search text-ink-400" />
            <InputText v-model="searchTerm" placeholder="Buscar nombre, cédula, email..." class="!pl-10 w-72" @keyup.enter="cargar" />
          </span>

          <Dropdown
            v-model="filtroEstado"
            :options="opcionesEstado"
            option-label="label"
            option-value="value"
            placeholder="Estado"
            showClear
            class="w-44"
          />

          <Dropdown
            v-model="filtroCategoria"
            :options="opcionesCategoria"
            option-label="label"
            option-value="value"
            placeholder="Categoría licencia"
            showClear
            class="w-44"
          />

          <Dropdown
            v-model="filtroTipoContrato"
            :options="opcionesTipoContrato"
            option-label="label"
            option-value="value"
            placeholder="Tipo de contrato"
            showClear
            class="w-52"
          />

          <Dropdown
            v-model="filtroLicencia"
            :options="opcionesLicencia"
            option-label="label"
            option-value="value"
            placeholder="Estado licencia"
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
            Mostrando {{ instructoresFiltrados.length }} de {{ instructores.length }} instructor{{ instructores.length === 1 ? '' : 'es' }}
          </span>
          <span v-if="filtroEstado" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">Estado: {{ filtroEstado }}</span>
          <span v-if="filtroCategoria" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">Categoría: {{ filtroCategoria }}</span>
          <span v-if="filtroTipoContrato" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">Contrato: {{ etiquetaContrato(filtroTipoContrato) }}</span>
          <span v-if="filtroLicencia" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">Licencia: {{ etiquetaLicencia(filtroLicencia) }}</span>
          <span v-if="searchTerm" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">Búsqueda: "{{ searchTerm }}"</span>
        </div>
      </div>

      <EmptyState
        v-if="!loading && instructoresFiltrados.length === 0 && instructores.length === 0"
        icon="pi pi-id-card"
        title="Sin instructores registrados"
        description="Agrega el primer instructor para empezar a programar clases."
      >
        <template #action>
          <Button label="Nuevo instructor" icon="pi pi-plus" @click="navigateToForm()" />
        </template>
      </EmptyState>

      <EmptyState
        v-else-if="!loading && instructoresFiltrados.length === 0"
        icon="pi pi-filter"
        title="Sin resultados"
        description="Ningún instructor coincide con los filtros activos. Prueba ajustarlos."
      >
        <template #action>
          <Button label="Limpiar filtros" icon="pi pi-filter-slash" outlined @click="limpiar" />
        </template>
      </EmptyState>

      <DataTable v-else :value="instructoresFiltrados" :loading="loading" striped-rows :pt="{ table: { style: 'min-width: 72rem' } }">
        <Column header="Instructor">
          <template #body="{ data }">
            <div class="flex items-center gap-3">
              <Avatar :name="`${data.nombre} ${data.apellido}`" size="md" />
              <div>
                <p class="text-sm font-semibold text-ink-900">{{ data.nombre }} {{ data.apellido }}</p>
                <p class="text-xs text-ink-500">{{ data.email }}</p>
              </div>
            </div>
          </template>
        </Column>
        <Column field="cedula" header="Cédula" />
        <Column header="Licencia">
          <template #body="{ data }">
            <div class="flex items-center gap-2">
              <span class="inline-flex items-center justify-center w-7 h-7 rounded-md bg-brand-100 text-brand-700 text-xs font-bold">
                {{ data.licenciaCategoria || '?' }}
              </span>
              <span class="text-sm text-ink-700">{{ data.licenciaNumero || '—' }}</span>
            </div>
          </template>
        </Column>
        <Column header="Caducidad licencia">
          <template #body="{ data }">
            <div v-if="data.licenciaCaducidad" class="flex flex-col">
              <span class="text-sm text-ink-900">{{ new Date(data.licenciaCaducidad).toLocaleDateString('es-EC') }}</span>
              <span :class="['text-xs font-medium', tonoBadgeLicencia(data.licenciaCaducidad)]">
                {{ etiquetaTiempoLicencia(data.licenciaCaducidad) }}
              </span>
            </div>
            <span v-else class="text-ink-400 text-sm">—</span>
          </template>
        </Column>
        <Column header="Contrato">
          <template #body="{ data }">
            <span v-if="data.tipoContrato" class="text-xs font-medium text-ink-700">
              <i :class="iconoContrato(data.tipoContrato)" class="mr-1 text-ink-500" />
              {{ etiquetaContrato(data.tipoContrato) }}
            </span>
            <span v-else class="text-ink-400 text-xs">—</span>
          </template>
        </Column>
        <Column field="telefono" header="Teléfono" />
        <Column header="Estado">
          <template #body="{ data }">
            <StatusBadge :status="data.estado" />
          </template>
        </Column>
        <Column header="" style="width: 140px">
          <template #body="{ data }">
            <div class="flex items-center justify-end gap-1">
              <Button icon="pi pi-eye" rounded text size="small" @click="$router.push(`/instructores/${data.id}`)" v-tooltip="'Ver detalle'" />
              <Button icon="pi pi-pencil" rounded text size="small" @click="navigateToForm(data.id)" v-tooltip="'Editar'" />
              <Button icon="pi pi-trash" rounded text size="small" severity="danger" @click="eliminar(data)" v-tooltip="'Eliminar'" />
            </div>
          </template>
        </Column>
      </DataTable>
    </DataTableCard>

    <Toast />
    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Tooltip from 'primevue/tooltip'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import instructoresService, { type InstructorListResponse, type TipoContrato } from '@/services/instructores'

const vTooltip = Tooltip
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const instructores = ref<InstructorListResponse[]>([])
const loading = ref(false)
const searchTerm = ref('')
const stats = reactive({ total: 0, activos: 0, vencen: 0, vencidas: 0 })

// ----- Filtros -----
const filtroEstado = ref<string | null>(null)
const filtroCategoria = ref<string | null>(null)
const filtroTipoContrato = ref<string | null>(null)
const filtroLicencia = ref<string | null>(null) // 'VIGENTE' | 'POR_VENCER' | 'VENCIDA'
const filtroActivo = ref<string | null>(null)   // marca el StatCard seleccionado

const opcionesEstado = [
  { label: 'Activo', value: 'ACTIVO' },
  { label: 'Inactivo', value: 'INACTIVO' },
  { label: 'Suspendido', value: 'SUSPENDIDO' }
]
const opcionesCategoria = ['A', 'B', 'C', 'D', 'E', 'F'].map(c => ({ label: `Categoría ${c}`, value: c }))
const opcionesTipoContrato = [
  { label: 'Tiempo completo', value: 'TIEMPO_COMPLETO' },
  { label: 'Medio tiempo', value: 'MEDIO_TIEMPO' },
  { label: 'Por horas', value: 'POR_HORAS' }
]
const opcionesLicencia = [
  { label: 'Vigente (> 60 d.)', value: 'VIGENTE' },
  { label: 'Por vencer (< 60 d.)', value: 'POR_VENCER' },
  { label: 'Vencida', value: 'VENCIDA' }
]

// ----- Helpers -----
const diasParaVencer = (caducidad?: string): number | null => {
  if (!caducidad) return null
  const hoy = new Date(); hoy.setHours(0, 0, 0, 0)
  const v = new Date(caducidad)
  return Math.floor((v.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24))
}

const tonoBadgeLicencia = (caducidad?: string): string => {
  const d = diasParaVencer(caducidad)
  if (d === null) return 'text-ink-400'
  if (d < 0) return 'text-danger-600'
  if (d < 60) return 'text-warning-600'
  return 'text-success-600'
}

const etiquetaTiempoLicencia = (caducidad?: string): string => {
  const d = diasParaVencer(caducidad)
  if (d === null) return ''
  if (d < 0) return `Vencida hace ${Math.abs(d)} días`
  if (d === 0) return 'Vence hoy'
  if (d < 60) return `Vence en ${d} días`
  return `Vigente (${d} días)`
}

const etiquetaContrato = (tipo?: string): string => {
  switch (tipo) {
    case 'TIEMPO_COMPLETO': return 'Tiempo completo'
    case 'MEDIO_TIEMPO':    return 'Medio tiempo'
    case 'POR_HORAS':       return 'Por horas'
    default:                return tipo || '—'
  }
}

const iconoContrato = (tipo?: string): string => {
  switch (tipo) {
    case 'TIEMPO_COMPLETO': return 'pi pi-briefcase'
    case 'MEDIO_TIEMPO':    return 'pi pi-clock'
    case 'POR_HORAS':       return 'pi pi-calculator'
    default:                return 'pi pi-file'
  }
}

const etiquetaLicencia = (v: string) =>
  opcionesLicencia.find(o => o.value === v)?.label || v

// ----- Lista filtrada (post-filter) -----
const instructoresFiltrados = computed(() => {
  let resultado = instructores.value

  if (filtroEstado.value) {
    resultado = resultado.filter(i => i.estado === filtroEstado.value)
  }
  if (filtroCategoria.value) {
    resultado = resultado.filter(i => i.licenciaCategoria === filtroCategoria.value)
  }
  if (filtroTipoContrato.value) {
    resultado = resultado.filter(i => i.tipoContrato === filtroTipoContrato.value)
  }
  if (filtroLicencia.value) {
    resultado = resultado.filter(i => {
      const d = diasParaVencer(i.licenciaCaducidad)
      if (d === null) return false
      if (filtroLicencia.value === 'VIGENTE') return d >= 60
      if (filtroLicencia.value === 'POR_VENCER') return d >= 0 && d < 60
      if (filtroLicencia.value === 'VENCIDA') return d < 0
      return true
    })
  }
  return resultado
})

const hayFiltrosActivos = computed(() =>
  !!(filtroEstado.value || filtroCategoria.value || filtroTipoContrato.value || filtroLicencia.value || searchTerm.value)
)

// ----- StatCards clickeables -----
const aplicarFiltroStat = (stat: string) => {
  // Si ya estaba activo este filtro, lo quitamos
  if (filtroActivo.value === stat) {
    filtroActivo.value = null
    filtroEstado.value = null
    filtroCategoria.value = null
    filtroLicencia.value = null
    return
  }
  filtroActivo.value = stat
  // Resetear los otros filtros antes de aplicar el del stat
  filtroEstado.value = null
  filtroCategoria.value = null
  filtroLicencia.value = null

  switch (stat) {
    case 'total':       /* sin filtro */                         break
    case 'activos':     filtroEstado.value = 'ACTIVO';           break
    case 'porVencer':   filtroLicencia.value = 'POR_VENCER';     break
    case 'vencidas':    filtroLicencia.value = 'VENCIDA';        break
  }
}

// ----- Carga -----
const cargar = async () => {
  try {
    loading.value = true
    const data = await instructoresService.obtenerInstructores(0, 100, searchTerm.value || undefined)
    instructores.value = data.content || []
    stats.total = data.totalElements ?? instructores.value.length
    stats.activos = instructores.value.filter(i => i.estado === 'ACTIVO').length
    stats.vencen = instructores.value.filter(i => {
      const d = diasParaVencer(i.licenciaCaducidad)
      return d !== null && d >= 0 && d < 60
    }).length
    stats.vencidas = instructores.value.filter(i => {
      const d = diasParaVencer(i.licenciaCaducidad)
      return d !== null && d < 0
    }).length
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudieron cargar los instructores',
      life: 4000
    })
  } finally { loading.value = false }
}

const limpiar = () => {
  searchTerm.value = ''
  filtroEstado.value = null
  filtroCategoria.value = null
  filtroTipoContrato.value = null
  filtroLicencia.value = null
  filtroActivo.value = null
  cargar()
}

const navigateToForm = (id?: number) => router.push(id ? `/instructores/${id}/editar` : '/instructores/nuevo')

const eliminar = (i: InstructorListResponse) => {
  confirm.require({
    message: `¿Eliminar al instructor "${i.nombre} ${i.apellido}"? Esta acción no se puede deshacer.`,
    header: 'Confirmar eliminación',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Eliminar',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await instructoresService.eliminarInstructor(i.id)
        toast.add({ severity: 'success', summary: 'Eliminado', detail: 'Instructor eliminado', life: 3000 })
        cargar()
      } catch (e: any) {
        toast.add({
          severity: 'error',
          summary: 'Error',
          detail: e.response?.data?.detail || 'No se pudo eliminar',
          life: 4000
        })
      }
    }
  })
}

onMounted(cargar)
</script>
