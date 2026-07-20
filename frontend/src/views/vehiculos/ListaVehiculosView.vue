<template>
  <div class="space-y-6">
    <PageHeader
      title="Vehículos"
      description="Gestión de la flota vehicular de la escuela."
      icon="pi pi-car"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Vehículos' }]"
    >
      <template #actions>
        <Button label="Combustible" icon="pi pi-bolt" outlined @click="router.push('/configuracion/combustible')" />
        <Button label="Exportar" icon="pi pi-download" outlined />
        <Button label="Nuevo vehículo" icon="pi pi-plus" @click="navigateToForm()" />
      </template>
    </PageHeader>

    <!-- StatCards clickeables (filtran el listado) -->
    <div class="grid grid-cols-2 lg:grid-cols-5 gap-4">
      <button
        type="button"
        @click="aplicarFiltroStat('total')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'total' ? 'ring-2 ring-brand-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="Total" :value="stats.total" icon="pi pi-car" color="brand" />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('activos')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'activos' ? 'ring-2 ring-success-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="Activos" :value="stats.activos" icon="pi pi-check-circle" color="success" hint="Disponibles" />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('mantenimiento')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'mantenimiento' ? 'ring-2 ring-warning-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="En mantenimiento" :value="stats.mantenimiento" icon="pi pi-wrench" color="warning" />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('soatPorVencer')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'soatPorVencer' ? 'ring-2 ring-info-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="SOAT por vencer" :value="stats.soatPorVencer" icon="pi pi-shield" color="info" hint="< 30 días" />
      </button>
      <button
        type="button"
        @click="aplicarFiltroStat('soatVencido')"
        :class="['text-left rounded-xl transition-all',
          filtroActivo === 'soatVencido' ? 'ring-2 ring-danger-500 ring-offset-2' : 'hover:scale-[1.02]']"
      >
        <StatCard label="SOAT vencido" :value="stats.soatVencido" icon="pi pi-exclamation-triangle" color="danger" hint="¡Urgente!" />
      </button>
    </div>

    <DataTableCard>
      <template #toolbar>
        <div class="flex flex-wrap items-center gap-3 flex-1">
          <span class="p-input-icon-left">
            <i class="pi pi-search text-ink-400" />
            <InputText v-model="searchTerm" placeholder="Buscar placa, marca, modelo..." class="!pl-10 w-72" />
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
            class="w-52"
          />

          <Dropdown
            v-model="filtroCombustible"
            :options="opcionesCombustible"
            option-label="label"
            option-value="value"
            placeholder="Combustible"
            showClear
            class="w-44"
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

      <div
        v-if="hayFiltrosActivos"
        class="mb-4 p-3 rounded-lg bg-info-50 border border-info-200 flex items-center justify-between gap-3 text-sm"
      >
        <div class="flex items-center gap-2 text-info-700 flex-wrap">
          <i class="pi pi-filter" />
          <span class="font-medium">
            Mostrando {{ vehiculosFiltrados.length }} de {{ vehiculos.length }} vehículo{{ vehiculos.length === 1 ? '' : 's' }}
          </span>
          <span v-if="filtroEstado" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">Estado: {{ filtroEstado }}</span>
          <span v-if="filtroCategoria" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">Cat: {{ etiquetaCategoria(filtroCategoria) }}</span>
          <span v-if="filtroCombustible" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">Comb: {{ etiquetaCombustible(filtroCombustible) }}</span>
          <span v-if="searchTerm" class="px-2 py-0.5 rounded bg-white border border-info-300 text-xs">"{{ searchTerm }}"</span>
        </div>
      </div>

      <EmptyState
        v-if="!loading && vehiculosFiltrados.length === 0 && vehiculos.length === 0"
        icon="pi pi-car"
        title="Sin vehículos registrados"
        description="Agrega el primer vehículo de la flota para empezar a programar clases."
      >
        <template #action>
          <Button label="Nuevo vehículo" icon="pi pi-plus" @click="navigateToForm()" />
        </template>
      </EmptyState>

      <EmptyState
        v-else-if="!loading && vehiculosFiltrados.length === 0"
        icon="pi pi-filter"
        title="Sin resultados"
        description="Ningún vehículo coincide con los filtros aplicados."
      >
        <template #action>
          <Button label="Limpiar filtros" icon="pi pi-filter-slash" outlined @click="limpiar" />
        </template>
      </EmptyState>

      <DataTable v-else :value="vehiculosFiltrados" :loading="loading" striped-rows :pt="{ table: { style: 'min-width: 72rem' } }">
        <Column header="Vehículo">
          <template #body="{ data }">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-lg bg-brand-50 text-brand-700 flex items-center justify-center flex-shrink-0">
                <i class="pi pi-car" />
              </div>
              <div>
                <p class="text-sm font-semibold text-ink-900">{{ data.marca }} {{ data.modelo }}</p>
                <p class="text-xs text-ink-500">{{ data.anio }}{{ data.color ? ` · ${data.color}` : '' }}</p>
              </div>
            </div>
          </template>
        </Column>
        <Column header="Placa">
          <template #body="{ data }">
            <span class="inline-flex items-center px-2.5 py-1 rounded-md bg-ink-100 border border-ink-300 font-mono font-bold text-sm text-ink-900">
              {{ data.placa }}
            </span>
          </template>
        </Column>
        <Column header="Categoría">
          <template #body="{ data }">
            <span v-if="data.categoriaLicenciaId" class="inline-flex items-center justify-center w-8 h-7 rounded-md bg-brand-100 text-brand-700 text-xs font-bold">
              {{ codigoCategoria(data.categoriaLicenciaId) }}
            </span>
            <span v-else class="text-ink-400 text-xs">—</span>
          </template>
        </Column>
        <Column header="Combustible">
          <template #body="{ data }">
            <span v-if="data.tipoCombustibleId" class="text-xs text-ink-700">
              <i :class="iconoCombustible(data.tipoCombustibleId)" class="mr-1 text-ink-500" />
              {{ codigoCombustible(data.tipoCombustibleId) }}
            </span>
            <span v-else class="text-ink-400 text-xs">—</span>
          </template>
        </Column>
        <Column field="kilometraje" header="Kilometraje">
          <template #body="{ data }">
            <span class="text-sm text-ink-700">{{ (data.kilometraje || 0).toLocaleString('es-EC') }} km</span>
          </template>
        </Column>
        <Column header="SOAT">
          <template #body="{ data }">
            <div v-if="data.soatVencimiento" class="flex flex-col">
              <span class="text-xs text-ink-900">{{ fmtFechaLocal(data.soatVencimiento) }}</span>
              <span :class="['text-xs font-medium', tonoBadgeFecha(data.soatVencimiento)]">
                {{ etiquetaTiempoFecha(data.soatVencimiento) }}
              </span>
            </div>
            <span v-else class="text-ink-400 text-xs">Sin SOAT</span>
          </template>
        </Column>
        <Column header="Estado">
          <template #body="{ data }">
            <StatusBadge :status="data.estado" />
          </template>
        </Column>
        <Column header="" style="width: 140px">
          <template #body="{ data }">
            <div class="flex items-center justify-end gap-1">
              <Button icon="pi pi-eye" rounded text size="small" @click="$router.push(`/vehiculos/${data.id}`)" v-tooltip="'Ver detalle'" />
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
import { fmtFechaLocal, parseLocalDate } from '@/utils/fechas'
import vehiculosService, {
  type VehiculoListResponse,
  type CategoriaLicenciaResponse,
  type TipoCombustibleResponse
} from '@/services/vehiculos'

const vTooltip = Tooltip
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()

const vehiculos = ref<VehiculoListResponse[]>([])
const categorias = ref<CategoriaLicenciaResponse[]>([])
const tiposCombustible = ref<TipoCombustibleResponse[]>([])

const loading = ref(false)
const searchTerm = ref('')
const stats = reactive({ total: 0, activos: 0, mantenimiento: 0, soatPorVencer: 0, soatVencido: 0 })

const filtroEstado = ref<string | null>(null)
const filtroCategoria = ref<number | null>(null)
const filtroCombustible = ref<number | null>(null)
const filtroSoat = ref<string | null>(null)
const filtroActivo = ref<string | null>(null)

const opcionesEstado = [
  { label: 'Activo', value: 'ACTIVO' },
  { label: 'En mantenimiento', value: 'MANTENIMIENTO' },
  { label: 'Fuera de servicio', value: 'FUERA_SERVICIO' }
]

const opcionesCategoria = computed(() =>
  categorias.value.map(c => ({ label: `${c.codigo} - ${c.descripcion}`, value: c.id }))
)
const opcionesCombustible = computed(() =>
  tiposCombustible.value.filter(t => t.activo).map(t => ({ label: t.nombre, value: t.id }))
)

const diasParaVencer = (fecha?: string): number | null => {
  // Parseamos "YYYY-MM-DD" en local para que no se corra un dia por TZ (UTC-5).
  const d = parseLocalDate(fecha)
  if (!d) return null
  const hoy = new Date(); hoy.setHours(0, 0, 0, 0)
  d.setHours(0, 0, 0, 0)
  return Math.floor((d.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24))
}

// Criterio: vencido = hoy o antes (coherente con AlertaSoatService y con morosidad).
const tonoBadgeFecha = (fecha?: string): string => {
  const d = diasParaVencer(fecha)
  if (d === null) return 'text-ink-400'
  if (d <= 0) return 'text-danger-600'
  if (d < 30) return 'text-warning-600'
  return 'text-success-600'
}

const etiquetaTiempoFecha = (fecha?: string): string => {
  const d = diasParaVencer(fecha)
  if (d === null) return ''
  if (d < 0) return `Vencido ${Math.abs(d)}d`
  if (d === 0) return 'Vencido hoy'
  return `${d}d`
}

const codigoCategoria = (id: number) => categorias.value.find(c => c.id === id)?.codigo || '?'
const etiquetaCategoria = (id: number) => categorias.value.find(c => c.id === id)?.codigo || `#${id}`
const codigoCombustible = (id: number) => tiposCombustible.value.find(t => t.id === id)?.codigo || '?'
const etiquetaCombustible = (id: number) => tiposCombustible.value.find(t => t.id === id)?.codigo || `#${id}`
const iconoCombustible = (id: number): string => {
  const c = tiposCombustible.value.find(t => t.id === id)?.codigo
  if (c === 'ELECTRICO') return 'pi pi-bolt'
  if (c === 'DIESEL') return 'pi pi-truck'
  return 'pi pi-cog'
}

const vehiculosFiltrados = computed(() => {
  let r = vehiculos.value
  if (filtroEstado.value) r = r.filter(v => v.estado === filtroEstado.value)
  if (filtroCategoria.value) r = r.filter(v => v.categoriaLicenciaId === filtroCategoria.value)
  if (filtroCombustible.value) r = r.filter(v => v.tipoCombustibleId === filtroCombustible.value)
  if (filtroSoat.value) {
    r = r.filter(v => {
      const d = diasParaVencer(v.soatVencimiento)
      if (d === null) return false
      if (filtroSoat.value === 'POR_VENCER') return d > 0 && d < 30
      if (filtroSoat.value === 'VENCIDO') return d <= 0
      return true
    })
  }
  if (searchTerm.value.trim()) {
    const t = searchTerm.value.trim().toLowerCase()
    r = r.filter(v =>
      v.placa.toLowerCase().includes(t) ||
      v.marca.toLowerCase().includes(t) ||
      v.modelo.toLowerCase().includes(t)
    )
  }
  return r
})

const hayFiltrosActivos = computed(() =>
  !!(filtroEstado.value || filtroCategoria.value || filtroCombustible.value || filtroSoat.value || searchTerm.value)
)

const aplicarFiltroStat = (stat: string) => {
  if (filtroActivo.value === stat) {
    filtroActivo.value = null
    filtroEstado.value = null
    filtroSoat.value = null
    return
  }
  filtroActivo.value = stat
  filtroEstado.value = null
  filtroSoat.value = null
  switch (stat) {
    case 'total':          /* sin filtro */                       break
    case 'activos':        filtroEstado.value = 'ACTIVO';         break
    case 'mantenimiento':  filtroEstado.value = 'MANTENIMIENTO';  break
    case 'soatPorVencer':  filtroSoat.value = 'POR_VENCER';       break
    case 'soatVencido':    filtroSoat.value = 'VENCIDO';          break
  }
}

const cargar = async () => {
  loading.value = true
  try {
    const [data, cats, tipos] = await Promise.all([
      vehiculosService.obtenerVehiculos(0, 100),
      vehiculosService.listarCategoriasLicencia(false).catch(() => []),
      vehiculosService.listarTiposCombustible(false).catch(() => [])
    ])
    vehiculos.value = data.content || []
    categorias.value = cats
    tiposCombustible.value = tipos
    stats.total = data.totalElements ?? vehiculos.value.length
    stats.activos = vehiculos.value.filter(v => v.estado === 'ACTIVO').length
    stats.mantenimiento = vehiculos.value.filter(v => v.estado === 'MANTENIMIENTO').length
    stats.soatPorVencer = vehiculos.value.filter(v => {
      const d = diasParaVencer(v.soatVencimiento)
      return d !== null && d > 0 && d < 30
    }).length
    stats.soatVencido = vehiculos.value.filter(v => {
      const d = diasParaVencer(v.soatVencimiento)
      return d !== null && d <= 0
    }).length
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: e.response?.data?.detail || 'No se pudieron cargar los vehículos',
      life: 4000
    })
  } finally { loading.value = false }
}

const limpiar = () => {
  searchTerm.value = ''
  filtroEstado.value = null
  filtroCategoria.value = null
  filtroCombustible.value = null
  filtroSoat.value = null
  filtroActivo.value = null
}

const navigateToForm = (id?: number) => router.push(id ? `/vehiculos/${id}/editar` : '/vehiculos/nuevo')

const eliminar = (v: VehiculoListResponse) => {
  confirm.require({
    message: `¿Eliminar el vehículo "${v.marca} ${v.modelo} (${v.placa})"?`,
    header: 'Confirmar eliminación',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Eliminar',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await vehiculosService.eliminarVehiculo(v.id)
        toast.add({ severity: 'success', summary: 'Eliminado', detail: 'Vehículo eliminado', life: 3000 })
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
