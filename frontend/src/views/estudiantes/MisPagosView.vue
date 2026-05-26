<template>
  <div class="space-y-6">
    <PageHeader
      title="Mis pagos"
      description="Tu estado de cuenta: facturas emitidas y pagos registrados"
      icon="pi pi-wallet"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Mis pagos' }
      ]"
    >
      <template #actions>
        <Button label="Recargar" icon="pi pi-refresh" outlined :loading="loading" @click="cargar" />
      </template>
    </PageHeader>

    <div v-if="loading" class="card p-10 text-center text-ink-500">
      <i class="pi pi-spinner animate-spin text-2xl mb-2" />
      <p>Cargando tu estado de cuenta…</p>
    </div>

    <div v-else-if="!miId" class="card p-10 text-center">
      <i class="pi pi-info-circle text-warning-600 text-3xl mb-2" />
      <p class="text-sm text-ink-700">Tu cuenta aún no está vinculada a un registro de estudiante.</p>
    </div>

    <template v-else>
      <!-- KPIs -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label="Total facturado"
          :value="formatMoney(stats.totalFacturado)"
          icon="pi pi-file-edit"
          color="brand"
          :hint="`${facturas.length} ${facturas.length === 1 ? 'factura' : 'facturas'}`"
        />
        <StatCard
          label="Total pagado"
          :value="formatMoney(stats.totalPagado)"
          icon="pi pi-check-circle"
          color="success"
          :hint="`${pagos.length} ${pagos.length === 1 ? 'pago registrado' : 'pagos registrados'}`"
        />
        <StatCard
          label="Saldo pendiente"
          :value="formatMoney(stats.saldoPendiente)"
          icon="pi pi-clock"
          :color="stats.saldoPendiente > 0 ? 'warning' : 'success'"
          :hint="stats.saldoPendiente > 0 ? 'Pago pendiente' : 'Estás al día'"
        />
        <StatCard
          label="Facturas vencidas"
          :value="stats.facturasVencidas"
          icon="pi pi-exclamation-triangle"
          :color="stats.facturasVencidas > 0 ? 'danger' : 'success'"
          hint="Pagos atrasados"
        />
      </div>

      <!-- Banner vencidas -->
      <div v-if="stats.facturasVencidas > 0" class="card p-4 border-l-4 !border-l-danger-600">
        <div class="flex items-start gap-3">
          <i class="pi pi-exclamation-triangle text-danger-600 mt-0.5" />
          <div>
            <p class="text-sm font-semibold text-danger-700">Tienes {{ stats.facturasVencidas }} {{ stats.facturasVencidas === 1 ? 'factura vencida' : 'facturas vencidas' }}</p>
            <p class="text-xs text-ink-600 mt-0.5">Por favor regulariza tu situación cuanto antes con la administración de la escuela.</p>
          </div>
        </div>
      </div>

      <!-- Facturas -->
      <DataTableCard
        title="Mis facturas"
        :description="`${facturas.length} ${facturas.length === 1 ? 'factura emitida' : 'facturas emitidas'} a tu nombre`"
      >
        <EmptyState
          v-if="facturas.length === 0"
          icon="pi pi-receipt"
          title="No tienes facturas emitidas"
          description="Cuando la administración te emita una factura aparecerá aquí."
        />
        <DataTable v-else :value="facturas" striped-rows :pt="{ table: { style: 'min-width: 50rem' } }">
          <Column header="N° Factura">
            <template #body="{ data }">
              <span class="font-mono text-xs text-brand-700 font-semibold">{{ data.numeroFactura }}</span>
            </template>
          </Column>
          <Column header="Concepto">
            <template #body="{ data }">
              <p class="text-sm">{{ data.descripcion || conceptoNombre(data.conceptoFacturacionId) }}</p>
            </template>
          </Column>
          <Column header="Fecha emisión">
            <template #body="{ data }">
              <p class="text-sm">{{ formatearFecha(data.fechaEmision) }}</p>
            </template>
          </Column>
          <Column header="Vencimiento">
            <template #body="{ data }">
              <p class="text-sm" :class="esVencida(data) ? 'text-danger-700 font-semibold' : ''">
                {{ data.fechaVencimiento ? formatearFecha(data.fechaVencimiento) : '—' }}
              </p>
            </template>
          </Column>
          <Column header="Monto">
            <template #body="{ data }">
              <p class="text-sm font-semibold">{{ formatMoney(data.montoOriginal) }}</p>
              <p class="text-[11px] text-success-700">Pagado: {{ formatMoney(data.montoPagado) }}</p>
            </template>
          </Column>
          <Column header="Saldo">
            <template #body="{ data }">
              <span
                v-if="saldoFactura(data) > 0"
                class="inline-flex items-center px-2 py-0.5 rounded-md bg-warning-50 text-warning-700 text-xs font-bold border border-warning-500/20"
              >
                {{ formatMoney(saldoFactura(data)) }}
              </span>
              <span v-else class="inline-flex items-center gap-1 text-xs text-success-600 font-medium">
                <i class="pi pi-check-circle text-[10px]" /> Pagada
              </span>
            </template>
          </Column>
          <Column header="Tipo">
            <template #body="{ data }">
              <StatusBadge :status="data.tipoPago || 'CONTADO'" />
              <p v-if="data.tipoPago === 'CREDITO'" class="text-[11px] text-ink-500 mt-1">
                {{ data.cuotasPagadas || 0 }}/{{ data.numeroCuotas }} cuotas
              </p>
            </template>
          </Column>
          <Column header="Estado">
            <template #body="{ data }">
              <StatusBadge :status="data.estado" />
            </template>
          </Column>
        </DataTable>
      </DataTableCard>

      <!-- Pagos -->
      <DataTableCard
        title="Mis pagos"
        :description="`${pagos.length} ${pagos.length === 1 ? 'pago registrado' : 'pagos registrados'} en tu cuenta`"
      >
        <EmptyState
          v-if="pagos.length === 0"
          icon="pi pi-dollar"
          title="Aún no se han registrado pagos"
          description="Cuando realices un pago la administración lo registrará y aparecerá aquí."
        />
        <DataTable v-else :value="pagos" striped-rows :pt="{ table: { style: 'min-width: 45rem' } }">
          <Column header="N° Recibo">
            <template #body="{ data }">
              <span class="font-mono text-xs text-brand-700">{{ data.numeroRecibo || '#' + data.id }}</span>
            </template>
          </Column>
          <Column header="Factura">
            <template #body="{ data }">
              <span class="font-mono text-xs text-ink-700">{{ numeroFactura(data.facturaId) }}</span>
            </template>
          </Column>
          <Column header="Fecha">
            <template #body="{ data }">
              <p class="text-sm">{{ formatearFecha(data.fechaPago) }}</p>
            </template>
          </Column>
          <Column header="Monto">
            <template #body="{ data }">
              <p class="text-sm font-semibold text-success-700">{{ formatMoney(data.monto) }}</p>
            </template>
          </Column>
          <Column header="Método">
            <template #body="{ data }">
              <StatusBadge :status="data.metodoPago || 'EFECTIVO'" />
            </template>
          </Column>
          <Column header="Observaciones">
            <template #body="{ data }">
              <p class="text-sm text-ink-600">{{ data.observaciones || '—' }}</p>
            </template>
          </Column>
        </DataTable>
      </DataTableCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const authStore = useAuthStore()
const loading = ref(false)
const facturas = ref<any[]>([])
const pagos = ref<any[]>([])
const conceptos = ref<any[]>([])
const stats = reactive({
  totalFacturado: 0,
  totalPagado: 0,
  saldoPendiente: 0,
  facturasVencidas: 0
})

const miId = computed(() => authStore.currentEstudianteId)

const formatMoney = (n: number) =>
  new Intl.NumberFormat('es-EC', { style: 'currency', currency: 'USD' })
    .format(Number(n) || 0)

const formatearFecha = (fecha: string) => {
  if (!fecha) return '—'
  const f = fecha.length >= 10 ? fecha.substring(0, 10) : fecha
  const [y, m, d] = f.split('-')
  if (!y || !m || !d) return fecha
  return `${d}/${m}/${y}`
}

const saldoFactura = (f: any) => Math.max(0, Number(f.montoOriginal || 0) - Number(f.montoPagado || 0))

const esVencida = (f: any) => {
  if (!f.fechaVencimiento || f.estado === 'PAGADA') return false
  const hoy = new Date().toISOString().substring(0, 10)
  return f.fechaVencimiento < hoy && saldoFactura(f) > 0
}

const conceptoNombre = (id: number) => {
  const c = conceptos.value.find((x: any) => x.id === id)
  return c?.nombre || 'Concepto sin especificar'
}

const numeroFactura = (id: number) => {
  const f = facturas.value.find((x: any) => x.id === id)
  return f?.numeroFactura || `#${id}`
}

const cargar = async () => {
  loading.value = true
  try {
    if (!authStore.currentEstudianteId) {
      await authStore.loadEstudianteId()
    }
    const miEstId = authStore.currentEstudianteId
    if (!miEstId) return

    const [facRes, pagRes, conRes] = await Promise.allSettled([
      api.get('/facturas',                 { params: { size: 200 } }),
      api.get('/pagos',                    { params: { size: 200 } }),
      api.get('/conceptos-facturacion')
    ])

    facturas.value = facRes.status === 'fulfilled'
      ? (facRes.value.data.content || []).filter((f: any) => f.estudianteId === miEstId)
      : []
    pagos.value = pagRes.status === 'fulfilled'
      ? (pagRes.value.data.content || []).filter((p: any) => p.estudianteId === miEstId)
      : []
    conceptos.value = conRes.status === 'fulfilled'
      ? (Array.isArray(conRes.value.data) ? conRes.value.data : (conRes.value.data.content || []))
      : []

    // Ordenar más reciente primero
    facturas.value.sort((a: any, b: any) => (b.fechaEmision || '').localeCompare(a.fechaEmision || ''))
    pagos.value.sort((a: any, b: any) => (b.fechaPago || '').localeCompare(a.fechaPago || ''))

    stats.totalFacturado = facturas.value.reduce((s, f) => s + Number(f.montoOriginal || 0), 0)
    stats.totalPagado = pagos.value.reduce((s, p) => s + Number(p.monto || 0), 0)
    stats.saldoPendiente = Math.max(0, stats.totalFacturado - stats.totalPagado)
    stats.facturasVencidas = facturas.value.filter(f => esVencida(f)).length
  } finally {
    loading.value = false
  }
}

onMounted(cargar)
</script>
