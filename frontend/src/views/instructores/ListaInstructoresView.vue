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

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard label="Total" :value="stats.total" icon="pi pi-id-card" color="brand" />
      <StatCard label="Activos" :value="stats.activos" icon="pi pi-check-circle" color="success" />
      <StatCard label="Categoría E" :value="stats.categoriaE" icon="pi pi-bookmark" color="info" />
      <StatCard label="Licencia próx. a vencer" :value="stats.vencen" icon="pi pi-exclamation-triangle" color="warning" />
    </div>

    <DataTableCard>
      <template #toolbar>
        <span class="p-input-icon-left">
          <i class="pi pi-search text-ink-400" />
          <InputText v-model="searchTerm" placeholder="Buscar instructor..." class="!pl-10 w-72" @keyup.enter="cargar" />
        </span>
        <Button icon="pi pi-filter-slash" outlined @click="limpiar" v-tooltip="'Limpiar'" />
      </template>

      <EmptyState
        v-if="!loading && instructores.length === 0"
        icon="pi pi-id-card"
        title="Sin instructores registrados"
        description="Agrega el primer instructor para empezar a programar clases."
      >
        <template #action>
          <Button label="Nuevo instructor" icon="pi pi-plus" @click="navigateToForm()" />
        </template>
      </EmptyState>

      <DataTable v-else :value="instructores" :loading="loading" striped-rows :pt="{ table: { style: 'min-width: 60rem' } }">
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
        <Column field="telefono" header="Teléfono" />
        <Column header="Estado">
          <template #body="{ data }">
            <StatusBadge :status="data.estado" />
          </template>
        </Column>
        <Column header="" style="width: 140px">
          <template #body="{ data }">
            <div class="flex items-center justify-end gap-1">
              <Button icon="pi pi-eye" rounded text size="small" @click="$router.push(`/instructores/${data.id}`)" />
              <Button icon="pi pi-pencil" rounded text size="small" @click="navigateToForm(data.id)" />
              <Button icon="pi pi-trash" rounded text size="small" severity="danger" @click="eliminar(data)" />
            </div>
          </template>
        </Column>
      </DataTable>
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
import Tooltip from 'primevue/tooltip'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import api from '@/services/api'

const vTooltip = Tooltip
const router = useRouter()

const instructores = ref<any[]>([])
const loading = ref(false)
const searchTerm = ref('')
const stats = reactive({ total: 0, activos: 0, categoriaE: 0, vencen: 0 })

const cargar = async () => {
  try {
    loading.value = true
    const { data } = await api.get('/instructores', { params: { size: 100, search: searchTerm.value || undefined } })
    instructores.value = data.content || []
    stats.total = data.totalElements ?? instructores.value.length
    stats.activos = instructores.value.filter(i => i.estado === 'ACTIVO').length
    stats.categoriaE = instructores.value.filter(i => i.licenciaCategoria === 'E').length
    const in60d = new Date(); in60d.setDate(in60d.getDate() + 60)
    stats.vencen = instructores.value.filter(i => i.licenciaCaducidad && new Date(i.licenciaCaducidad) < in60d).length
  } finally { loading.value = false }
}

const limpiar = () => { searchTerm.value = ''; cargar() }
const navigateToForm = (id?: number) => router.push(id ? `/instructores/${id}/editar` : '/instructores/nuevo')
const eliminar = async (i: any) => {
  if (!confirm(`¿Eliminar a ${i.nombre} ${i.apellido}?`)) return
  await api.delete(`/instructores/${i.id}`); cargar()
}

onMounted(cargar)
</script>
