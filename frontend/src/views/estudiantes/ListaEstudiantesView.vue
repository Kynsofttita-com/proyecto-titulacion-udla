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

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard label="Total" :value="stats.total" icon="pi pi-users" color="brand" />
      <StatCard label="Activos" :value="stats.activos" icon="pi pi-check-circle" color="success" />
      <StatCard label="Pre-matriculados" :value="stats.preMatriculados" icon="pi pi-clock" color="warning" />
      <StatCard label="Completados" :value="stats.completados" icon="pi pi-graduation-cap" color="info" />
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
        :pt="{ table: { style: 'min-width: 60rem' } }"
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
        <Column header="Estado">
          <template #body="{ data }">
            <StatusBadge :status="data.estado" />
          </template>
        </Column>
        <Column header="Matrícula">
          <template #body="{ data }">
            <span class="text-sm text-ink-600">{{ data.fechaMatricula || '—' }}</span>
          </template>
        </Column>
        <Column header="" style="width: 140px">
          <template #body="{ data }">
            <div class="flex items-center justify-end gap-1">
              <Button icon="pi pi-eye" rounded text size="small" v-tooltip.top="'Ver detalle'" @click="navigateToDetail(data.id)" />
              <Button icon="pi pi-pencil" rounded text size="small" v-tooltip.top="'Editar'" @click="navigateToForm(data.id)" />
              <Button icon="pi pi-trash" rounded text size="small" severity="danger" v-tooltip.top="'Eliminar'" @click="confirmarEliminar(data)" />
            </div>
          </template>
        </Column>
      </DataTable>

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
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import estudiantesService from '@/services/estudiantes'

const vTooltip = Tooltip
const router = useRouter()

const estudiantes = ref<any[]>([])
const loading = ref(false)
const searchTerm = ref('')
const filtroEstado = ref<string | null>(null)
const totalEstudiantes = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)

const stats = reactive({ total: 0, activos: 0, preMatriculados: 0, completados: 0 })

const estadosFilter = [
  { label: 'Activo', value: 'ACTIVO' },
  { label: 'Pre-matriculado', value: 'PRE_MATRICULADO' },
  { label: 'Completado', value: 'COMPLETADO' },
  { label: 'Retirado', value: 'RETIRADO' }
]

const cargar = async () => {
  try {
    loading.value = true
    const response = await estudiantesService.obtenerEstudiantes(currentPage.value, pageSize.value)
    let data = response.content
    if (filtroEstado.value) data = data.filter((e: any) => e.estado === filtroEstado.value)
    estudiantes.value = data
    totalEstudiantes.value = response.totalElements

    // Estadísticas globales (sin filtro)
    const all = await estudiantesService.obtenerEstudiantes(0, 100)
    stats.total = all.totalElements
    stats.activos = all.content.filter((e: any) => e.estado === 'ACTIVO' || e.estado === 'MATRICULADO').length
    stats.preMatriculados = all.content.filter((e: any) => e.estado === 'PRE_MATRICULADO').length
    stats.completados = all.content.filter((e: any) => e.estado === 'COMPLETADO').length
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

onMounted(cargar)
</script>
