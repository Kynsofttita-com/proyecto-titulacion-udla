<template>
  <div class="space-y-6">
    <PageHeader
      title="Panel General"
      description="Resumen ejecutivo del estado actual de la escuela."
      icon="pi pi-th-large"
      :breadcrumbs="[{ label: 'Inicio' }]"
    >
      <template #actions>
        <Button label="Nueva clase" icon="pi pi-plus" @click="$router.push('/asignaciones')" />
      </template>
    </PageHeader>

    <!-- KPIs -->
    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-5">
      <StatCard
        label="Estudiantes activos"
        :value="kpis.estudiantes"
        icon="pi pi-users"
        color="brand"
        :hint="`${kpis.estudiantesTotal} total registrados`"
      />
      <StatCard
        label="Instructores"
        :value="kpis.instructores"
        icon="pi pi-id-card"
        color="info"
        hint="Disponibles para asignación"
      />
      <StatCard
        label="Vehículos en flota"
        :value="kpis.vehiculos"
        icon="pi pi-car"
        color="success"
        hint="Habilitados para prácticas"
      />
      <StatCard
        label="Facturación pendiente"
        :value="formatMoney(kpis.saldoPendiente)"
        icon="pi pi-wallet"
        color="warning"
        :hint="`${kpis.facturas} facturas activas`"
      />
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-3 gap-5">
      <DataTableCard
        title="Clases programadas para hoy"
        description="Asignaciones activas con horario en las próximas horas"
        class="xl:col-span-2"
      >
        <template #toolbar>
          <Button label="Ver calendario" icon="pi pi-calendar" text @click="$router.push('/asignaciones')" />
        </template>
        <div v-if="clasesHoy.length === 0">
          <EmptyState
            icon="pi pi-calendar-times"
            title="Sin clases para hoy"
            description="Cuando se programen asignaciones aparecerán acá."
          >
            <template #action>
              <Button label="Crear asignación" icon="pi pi-plus" @click="$router.push('/asignaciones')" />
            </template>
          </EmptyState>
        </div>
        <ul v-else class="divide-y divide-ink-200">
          <li v-for="c in clasesHoy" :key="c.id" class="flex items-center gap-4 px-6 py-3">
            <div class="w-14 text-center">
              <p class="text-lg font-bold text-brand-700">{{ c.hora }}</p>
              <p class="text-[10px] text-ink-500 uppercase">{{ c.duracion }}min</p>
            </div>
            <div class="flex-1">
              <p class="text-sm font-semibold text-ink-900">Clase #{{ c.id }}</p>
              <p class="text-xs text-ink-500">Estudiante {{ c.estudianteId }} · Instructor {{ c.instructorId }} · Veh. {{ c.vehiculoId }}</p>
            </div>
            <StatusBadge :status="c.estado" />
          </li>
        </ul>
      </DataTableCard>

      <section class="card animate-fade-up p-6">
        <h3 class="heading-3 mb-4">Accesos rápidos</h3>
        <div class="space-y-2">
          <QuickAction icon="pi-user-plus"     label="Matricular estudiante" to="/estudiantes/nuevo" />
          <QuickAction icon="pi-id-card"       label="Nuevo instructor"      to="/instructores" />
          <QuickAction icon="pi-car"           label="Registrar vehículo"    to="/vehiculos" />
          <QuickAction icon="pi-calendar-plus" label="Programar clase"       to="/asignaciones" />
          <QuickAction icon="pi-file-edit"     label="Emitir factura"        to="/cobros" />
        </div>
      </section>
    </div>

    <DataTableCard title="Estudiantes recientes" description="Últimos estudiantes registrados en el sistema">
      <template #toolbar>
        <Button label="Ver todos" icon="pi pi-arrow-right" iconPos="right" text @click="$router.push('/estudiantes')" />
      </template>
      <DataTable :value="ultimosEstudiantes" striped-rows :loading="loading" :pt="{ table: { style: 'min-width: 50rem' } }">
        <Column header="Estudiante">
          <template #body="{ data }">
            <div class="flex items-center gap-3">
              <Avatar :name="data.nombreCompleto" size="sm" />
              <div>
                <p class="text-sm font-medium text-ink-900">{{ data.nombreCompleto }}</p>
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
      </DataTable>
    </DataTableCard>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, defineComponent, h } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import api from '@/services/api'

const QuickAction = defineComponent({
  props: ['icon', 'label', 'to'],
  setup(props) {
    const r = useRouter()
    return () =>
      h('button',
        {
          class: 'w-full flex items-center gap-3 p-3 rounded-lg border border-ink-200 hover:border-brand-400 hover:bg-brand-50/50 transition group text-left',
          onClick: () => r.push(props.to)
        },
        [
          h('div', { class: 'w-9 h-9 rounded-lg bg-brand-50 text-brand-700 flex items-center justify-center group-hover:bg-brand-100 transition' },
            [h('i', { class: `pi ${props.icon}` })]
          ),
          h('span', { class: 'text-sm font-medium text-ink-700 flex-1' }, props.label),
          h('i', { class: 'pi pi-angle-right text-ink-400 group-hover:text-brand-600 group-hover:translate-x-0.5 transition' })
        ]
      )
  }
})

const loading = ref(false)
const kpis = reactive({
  estudiantes: 0, estudiantesTotal: 0, instructores: 0, vehiculos: 0,
  saldoPendiente: 0, facturas: 0
})
const clasesHoy = ref<any[]>([])
const ultimosEstudiantes = ref<any[]>([])

const formatMoney = (n: number) => `$${(n ?? 0).toLocaleString('es-EC', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

const cargar = async () => {
  loading.value = true
  try {
    const [est, ins, veh, asig, fact] = await Promise.allSettled([
      api.get('/estudiantes', { params: { size: 100 } }),
      api.get('/instructores', { params: { size: 100 } }),
      api.get('/vehiculos', { params: { size: 100 } }),
      api.get('/asignaciones', { params: { size: 100 } }),
      api.get('/facturas', { params: { size: 100 } })
    ])

    if (est.status === 'fulfilled') {
      const data = est.value.data
      kpis.estudiantesTotal = data.totalElements ?? data.content?.length ?? 0
      kpis.estudiantes = (data.content || []).filter((e: any) => e.estado === 'ACTIVO' || e.estado === 'MATRICULADO').length
      ultimosEstudiantes.value = (data.content || []).slice(0, 5)
    }
    if (ins.status === 'fulfilled') kpis.instructores = ins.value.data.totalElements ?? 0
    if (veh.status === 'fulfilled') kpis.vehiculos    = veh.value.data.totalElements ?? 0
    if (fact.status === 'fulfilled') {
      const facturas = fact.value.data.content || []
      kpis.facturas = facturas.length
      kpis.saldoPendiente = facturas.reduce((s: number, f: any) => s + parseFloat(f.saldo ?? f.montoOriginal ?? 0), 0)
    }
    if (asig.status === 'fulfilled') {
      const hoy = new Date().toISOString().substring(0, 10)
      clasesHoy.value = (asig.value.data.content || [])
        .filter((a: any) => (a.fechaHora || a.fecha || '').startsWith(hoy))
        .map((a: any) => ({
          id: a.id,
          hora: (a.fechaHora || '').substring(11, 16) || '--:--',
          duracion: a.duracionMinutos || 60,
          estudianteId: a.estudianteId,
          instructorId: a.instructorId,
          vehiculoId: a.vehiculoId,
          estado: a.estado
        }))
        .slice(0, 6)
    }
  } finally {
    loading.value = false
  }
}

onMounted(cargar)
</script>
