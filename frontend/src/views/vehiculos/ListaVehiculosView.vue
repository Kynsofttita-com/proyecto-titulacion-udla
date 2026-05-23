<template>
  <div class="space-y-6">
    <PageHeader
      title="Flota de vehículos"
      description="Gestión de vehículos disponibles para prácticas y exámenes."
      icon="pi pi-car"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Vehículos' }]"
    >
      <template #actions>
        <Button label="Alertas SOAT" icon="pi pi-bell" outlined @click="$router.push('/vehiculos?alertas=true')" />
        <Button label="Nuevo vehículo" icon="pi pi-plus" @click="navigateToForm()" />
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard label="Total flota" :value="stats.total" icon="pi pi-car" color="brand" />
      <StatCard label="Disponibles" :value="stats.activos" icon="pi pi-check-circle" color="success" />
      <StatCard label="En mantenimiento" :value="stats.mantenimiento" icon="pi pi-wrench" color="warning" />
      <StatCard label="SOAT por vencer" :value="stats.soatVencen" icon="pi pi-exclamation-triangle" color="danger" />
    </div>

    <DataTableCard>
      <template #toolbar>
        <span class="p-input-icon-left">
          <i class="pi pi-search text-ink-400" />
          <InputText v-model="searchTerm" placeholder="Buscar por placa o marca..." class="!pl-10 w-72" @keyup.enter="cargar" />
        </span>
        <Button icon="pi pi-filter-slash" outlined @click="limpiar" v-tooltip="'Limpiar'" />
      </template>

      <EmptyState
        v-if="!loading && vehiculos.length === 0"
        icon="pi pi-car"
        title="Sin vehículos en la flota"
        description="Registra el primer vehículo para asignar prácticas."
      >
        <template #action>
          <Button label="Registrar vehículo" icon="pi pi-plus" @click="navigateToForm()" />
        </template>
      </EmptyState>

      <DataTable v-else :value="vehiculos" :loading="loading" striped-rows :pt="{ table: { style: 'min-width: 60rem' } }">
        <Column header="Vehículo">
          <template #body="{ data }">
            <div class="flex items-center gap-3">
              <div class="w-11 h-11 rounded-lg bg-gradient-to-br from-brand-100 to-brand-200 text-brand-800 flex items-center justify-center">
                <i class="pi pi-car" />
              </div>
              <div>
                <p class="text-sm font-semibold text-ink-900">{{ data.marca }} {{ data.modelo }}</p>
                <p class="text-xs text-ink-500">Año {{ data.año || data.anio }} · {{ data.color || 'Sin color' }}</p>
              </div>
            </div>
          </template>
        </Column>
        <Column header="Placa">
          <template #body="{ data }">
            <span class="inline-flex items-center px-2.5 py-1 rounded-md border-2 border-ink-300 bg-white font-mono font-bold text-sm text-ink-900">
              {{ data.placa }}
            </span>
          </template>
        </Column>
        <Column header="VIN">
          <template #body="{ data }">
            <span class="font-mono text-xs text-ink-600">{{ data.vin || '—' }}</span>
          </template>
        </Column>
        <Column header="Kilometraje">
          <template #body="{ data }">
            <span class="text-sm">{{ (data.kilometraje ?? 0).toLocaleString() }} km</span>
          </template>
        </Column>
        <Column header="Estado">
          <template #body="{ data }">
            <StatusBadge :status="data.estado || 'ACTIVO'" />
          </template>
        </Column>
        <Column header="" style="width: 140px">
          <template #body="{ data }">
            <div class="flex items-center justify-end gap-1">
              <Button icon="pi pi-eye" rounded text size="small" @click="$router.push(`/vehiculos/${data.id}`)" />
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
import api from '@/services/api'

const vTooltip = Tooltip
const router = useRouter()

const vehiculos = ref<any[]>([])
const loading = ref(false)
const searchTerm = ref('')
const stats = reactive({ total: 0, activos: 0, mantenimiento: 0, soatVencen: 0 })

const cargar = async () => {
  try {
    loading.value = true
    const { data } = await api.get('/vehiculos', { params: { size: 100 } })
    let lista = data.content || []
    if (searchTerm.value) {
      const t = searchTerm.value.toLowerCase()
      lista = lista.filter((v: any) =>
        (v.placa || '').toLowerCase().includes(t) ||
        (v.marca || '').toLowerCase().includes(t) ||
        (v.modelo || '').toLowerCase().includes(t)
      )
    }
    vehiculos.value = lista
    stats.total = data.totalElements ?? lista.length
    stats.activos = lista.filter((v: any) => (v.estado || 'ACTIVO') === 'ACTIVO').length
    stats.mantenimiento = lista.filter((v: any) => v.estado === 'MANTENIMIENTO').length
    stats.soatVencen = 0
  } finally { loading.value = false }
}

const limpiar = () => { searchTerm.value = ''; cargar() }
const navigateToForm = (id?: number) => router.push(id ? `/vehiculos/${id}/editar` : '/vehiculos/nuevo')
const eliminar = async (v: any) => {
  if (!confirm(`¿Eliminar vehículo ${v.placa}?`)) return
  await api.delete(`/vehiculos/${v.id}`); cargar()
}

onMounted(cargar)
</script>
