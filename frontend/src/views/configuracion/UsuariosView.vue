<template>
  <div class="space-y-6">
    <PageHeader
      title="Usuarios del sistema"
      description="Gestión de cuentas, roles y desbloqueo de usuarios."
      icon="pi pi-users"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Configuración', to: '/configuracion' },
        { label: 'Usuarios' }
      ]"
    >
      <template #actions>
        <Button label="Recargar" icon="pi pi-refresh" outlined @click="cargar" :loading="loading" />
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard label="Total usuarios" :value="stats.total" icon="pi pi-users" color="brand" />
      <StatCard label="Activos" :value="stats.activos" icon="pi pi-check-circle" color="success" />
      <StatCard label="Bloqueados" :value="stats.bloqueados" icon="pi pi-lock" color="warning" />
      <StatCard label="Inactivos" :value="stats.inactivos" icon="pi pi-times-circle" color="danger" />
    </div>

    <DataTableCard>
      <template #toolbar>
        <span class="p-input-icon-left">
          <i class="pi pi-search text-ink-400" />
          <InputText v-model="busqueda" placeholder="Buscar por nombre o email..." class="!pl-10 w-72" />
        </span>
        <Dropdown
          v-model="filtro"
          :options="[
            { label: 'Todos', value: 'todos' },
            { label: 'Activos', value: 'activos' },
            { label: 'Bloqueados', value: 'bloqueados' },
            { label: 'Inactivos', value: 'inactivos' }
          ]"
          optionLabel="label"
          optionValue="value"
          class="w-48"
        />
      </template>

      <DataTable :value="usuariosFiltrados" :loading="loading" striped-rows :pt="{ table: { style: 'min-width: 60rem' } }">
        <Column header="Usuario">
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
        <Column header="Roles">
          <template #body="{ data }">
            <div class="flex gap-1 flex-wrap">
              <span
                v-for="r in (data.roles || [])"
                :key="r"
                class="inline-flex items-center px-2 py-0.5 rounded-md bg-brand-50 text-brand-700 text-xs font-medium border border-brand-200"
              >{{ r }}</span>
            </div>
          </template>
        </Column>
        <Column header="Último acceso">
          <template #body="{ data }">
            <span class="text-xs text-ink-600">
              {{ data.lastLogin ? new Date(data.lastLogin).toLocaleString('es-EC') : 'Nunca' }}
            </span>
          </template>
        </Column>
        <Column header="Estado">
          <template #body="{ data }">
            <div class="space-y-1">
              <StatusBadge v-if="data.locked" status="LOCKED" label="Bloqueado" />
              <StatusBadge v-else-if="data.activo" status="ACTIVO" />
              <StatusBadge v-else status="INACTIVO" />
            </div>
          </template>
        </Column>
        <Column header="" style="width: 200px">
          <template #body="{ data }">
            <div class="flex items-center justify-end gap-2">
              <Button
                v-if="data.locked"
                label="Desbloquear"
                icon="pi pi-unlock"
                size="small"
                @click="desbloquear(data)"
                :loading="desbloqueandoId === data.id"
              />
              <Button
                v-else
                icon="pi pi-ellipsis-v"
                rounded
                text
                size="small"
                disabled
                v-tooltip.top="'Sin acciones disponibles'"
              />
            </div>
          </template>
        </Column>
      </DataTable>
    </DataTableCard>

    <Toast />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Tooltip from 'primevue/tooltip'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import api from '@/services/api'

const vTooltip = Tooltip
const toast = useToast()

const usuarios = ref<any[]>([])
const loading = ref(false)
const busqueda = ref('')
const filtro = ref<'todos' | 'activos' | 'bloqueados' | 'inactivos'>('todos')
const desbloqueandoId = ref<number | null>(null)

const stats = reactive({ total: 0, activos: 0, bloqueados: 0, inactivos: 0 })

const usuariosFiltrados = computed(() => {
  let result = usuarios.value
  if (filtro.value === 'activos')      result = result.filter(u => u.activo && !u.locked)
  else if (filtro.value === 'bloqueados') result = result.filter(u => u.locked)
  else if (filtro.value === 'inactivos')  result = result.filter(u => !u.activo)
  if (busqueda.value.trim()) {
    const q = busqueda.value.toLowerCase()
    result = result.filter(u =>
      (u.email || '').toLowerCase().includes(q) ||
      (u.nombre || '').toLowerCase().includes(q) ||
      (u.apellido || '').toLowerCase().includes(q)
    )
  }
  return result
})

const cargar = async () => {
  try {
    loading.value = true
    const { data } = await api.get('/usuarios', { params: { size: 200 } })
    usuarios.value = data.content || []
    stats.total = usuarios.value.length
    stats.activos = usuarios.value.filter(u => u.activo && !u.locked).length
    stats.bloqueados = usuarios.value.filter(u => u.locked).length
    stats.inactivos = usuarios.value.filter(u => !u.activo).length
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los usuarios', life: 4000 })
  } finally { loading.value = false }
}

const desbloquear = async (u: any) => {
  try {
    desbloqueandoId.value = u.id
    await api.post(`/usuarios/${u.id}/desbloquear`)
    toast.add({
      severity: 'success',
      summary: 'Cuenta desbloqueada',
      detail: `${u.email} puede iniciar sesión nuevamente`,
      life: 4000
    })
    cargar()
  } catch (e: any) {
    toast.add({
      severity: 'error',
      summary: 'No se pudo desbloquear',
      detail: e.response?.data?.detail || 'Error en el servidor',
      life: 4000
    })
  } finally { desbloqueandoId.value = null }
}

onMounted(cargar)
</script>
