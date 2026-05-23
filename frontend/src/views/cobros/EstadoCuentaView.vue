<template>
  <div class="space-y-6">
    <PageHeader
      title="Cobros y facturación"
      description="Gestión de facturas emitidas y pagos recibidos."
      icon="pi pi-wallet"
      :breadcrumbs="[{ label: 'Inicio', to: '/dashboard' }, { label: 'Cobros' }]"
    >
      <template #actions>
        <Button label="Registrar pago" icon="pi pi-dollar" outlined @click="mostrarFormPago = true" />
        <Button label="Nueva factura" icon="pi pi-plus" @click="mostrarFormFactura = true" />
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard
        label="Total facturado"
        :value="formatMoney(stats.totalFacturado)"
        icon="pi pi-file-edit"
        color="brand"
      />
      <StatCard
        label="Total pagado"
        :value="formatMoney(stats.totalPagado)"
        icon="pi pi-check-circle"
        color="success"
      />
      <StatCard
        label="Saldo pendiente"
        :value="formatMoney(stats.saldoPendiente)"
        icon="pi pi-clock"
        color="warning"
      />
      <StatCard
        label="Facturas activas"
        :value="stats.facturas"
        icon="pi pi-receipt"
        color="info"
        :hint="`${stats.pagos} pagos registrados`"
      />
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <!-- Facturas -->
      <DataTableCard title="Facturas" :description="`${facturas.length} facturas en el sistema`">
        <template #toolbar>
          <Button label="Nueva" icon="pi pi-plus" size="small" @click="mostrarFormFactura = true" />
        </template>
        <EmptyState
          v-if="facturas.length === 0"
          icon="pi pi-receipt"
          title="Sin facturas emitidas"
          description="Emite la primera factura para empezar a registrar cobros."
        >
          <template #action>
            <Button label="Nueva factura" icon="pi pi-plus" @click="mostrarFormFactura = true" />
          </template>
        </EmptyState>
        <DataTable v-else :value="facturas" striped-rows :pt="{ table: { style: 'min-width: 30rem' } }">
          <Column header="#">
            <template #body="{ data }">
              <span class="font-mono text-xs text-brand-700 font-semibold">{{ data.numeroFactura }}</span>
            </template>
          </Column>
          <Column field="estudianteId" header="Est." style="width: 80px" />
          <Column header="Monto">
            <template #body="{ data }">
              <p class="text-sm font-semibold">{{ formatMoney(data.montoOriginal) }}</p>
              <p class="text-[11px] text-ink-500">Pagado: {{ formatMoney(data.montoPagado) }}</p>
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
      <DataTableCard title="Pagos recibidos" :description="`${pagos.length} pagos registrados`">
        <EmptyState
          v-if="pagos.length === 0"
          icon="pi pi-dollar"
          title="Sin pagos registrados"
          description="Los pagos aparecerán aquí al ser registrados."
        />
        <DataTable v-else :value="pagos" striped-rows :pt="{ table: { style: 'min-width: 30rem' } }">
          <Column field="id" header="#" style="width: 60px" />
          <Column field="facturaId" header="Factura" style="width: 90px" />
          <Column header="Monto">
            <template #body="{ data }">
              <span class="font-semibold text-success-600">{{ formatMoney(data.monto) }}</span>
            </template>
          </Column>
          <Column field="metodoPago" header="Método">
            <template #body="{ data }">
              <span class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-ink-100 text-xs font-medium text-ink-700">
                <i :class="metodoIcon(data.metodoPago)" class="text-[10px]" />
                {{ data.metodoPago }}
              </span>
            </template>
          </Column>
          <Column header="Fecha">
            <template #body="{ data }">
              <span class="text-xs text-ink-600">{{ (data.fechaPago || '').substring(0, 10) }}</span>
            </template>
          </Column>
        </DataTable>
      </DataTableCard>
    </div>

    <!-- Dialog Factura -->
    <Dialog v-model:visible="mostrarFormFactura" modal header="Nueva factura" :style="{ width: '500px' }">
      <div class="space-y-4">
        <div v-if="errF" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 text-sm text-danger-600">{{ errF }}</div>
        <div>
          <label class="label mb-1.5 block">Estudiante ID *</label>
          <InputNumber v-model="formF.estudianteId" :useGrouping="false" class="w-full" />
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="label mb-1.5 block">Concepto ID *</label>
            <InputNumber v-model="formF.conceptoFacturacionId" :useGrouping="false" class="w-full" />
          </div>
          <div>
            <label class="label mb-1.5 block">Monto (USD) *</label>
            <InputNumber v-model="formF.montoOriginal" mode="currency" currency="USD" locale="en-US" class="w-full" />
          </div>
        </div>
        <div>
          <label class="label mb-1.5 block">Fecha vencimiento *</label>
          <Calendar v-model="formF.fechaVencimiento" dateFormat="yy-mm-dd" :showIcon="true" class="w-full" />
        </div>
        <div>
          <label class="label mb-1.5 block">Descripción</label>
          <Textarea v-model="formF.descripcion" rows="2" class="w-full" />
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="mostrarFormFactura = false" />
        <Button label="Crear factura" icon="pi pi-check" :loading="creandoF" @click="crearFactura" />
      </template>
    </Dialog>

    <!-- Dialog Pago -->
    <Dialog v-model:visible="mostrarFormPago" modal header="Registrar pago" :style="{ width: '500px' }">
      <div class="space-y-4">
        <div v-if="errP" class="rounded-lg bg-danger-50 border border-danger-500/20 p-3 text-sm text-danger-600">{{ errP }}</div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="label mb-1.5 block">Factura ID *</label>
            <InputNumber v-model="formP.facturaId" :useGrouping="false" class="w-full" />
          </div>
          <div>
            <label class="label mb-1.5 block">Monto (USD) *</label>
            <InputNumber v-model="formP.monto" mode="currency" currency="USD" locale="en-US" class="w-full" />
          </div>
        </div>
        <div>
          <label class="label mb-1.5 block">Método de pago *</label>
          <Dropdown
            v-model="formP.metodoPago"
            :options="[{label:'Efectivo',value:'EFECTIVO'},{label:'Transferencia',value:'TRANSFERENCIA'},{label:'Tarjeta',value:'TARJETA'}]"
            optionLabel="label"
            optionValue="value"
            class="w-full"
          />
        </div>
        <div>
          <label class="label mb-1.5 block">Referencia / Observaciones</label>
          <InputText v-model="formP.observaciones" class="w-full" />
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" outlined @click="mostrarFormPago = false" />
        <Button label="Registrar pago" icon="pi pi-check" :loading="creandoP" @click="crearPago" />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import api from '@/services/api'

const facturas = ref<any[]>([])
const pagos = ref<any[]>([])
const stats = reactive({ totalFacturado: 0, totalPagado: 0, saldoPendiente: 0, facturas: 0, pagos: 0 })

const mostrarFormFactura = ref(false)
const mostrarFormPago = ref(false)
const creandoF = ref(false)
const creandoP = ref(false)
const errF = ref('')
const errP = ref('')

const formF = reactive<any>({ estudianteId: null, conceptoFacturacionId: 1, montoOriginal: 0, fechaVencimiento: null, descripcion: '' })
const formP = reactive<any>({ facturaId: null, monto: 0, metodoPago: 'EFECTIVO', observaciones: '' })

const formatMoney = (n: number) => `$${(parseFloat(n as any) || 0).toLocaleString('es-EC', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

const metodoIcon = (m: string) => ({
  EFECTIVO: 'pi pi-dollar',
  TRANSFERENCIA: 'pi pi-arrows-h',
  TARJETA: 'pi pi-credit-card'
}[m] || 'pi pi-wallet')

const fmtFecha = (d: any) => {
  if (!d) return null
  if (typeof d === 'string') return d.substring(0, 10)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const cargar = async () => {
  const [f, p] = await Promise.allSettled([
    api.get('/facturas', { params: { size: 100 } }),
    api.get('/pagos', { params: { size: 100 } })
  ])
  if (f.status === 'fulfilled') {
    facturas.value = f.value.data.content || []
    stats.facturas = facturas.value.length
    stats.totalFacturado = facturas.value.reduce((s, x) => s + parseFloat(x.montoOriginal || 0), 0)
    stats.totalPagado = facturas.value.reduce((s, x) => s + parseFloat(x.montoPagado || 0), 0)
    stats.saldoPendiente = stats.totalFacturado - stats.totalPagado
  }
  if (p.status === 'fulfilled') {
    pagos.value = p.value.data.content || []
    stats.pagos = pagos.value.length
  }
}

const crearFactura = async () => {
  errF.value = ''
  try {
    creandoF.value = true
    await api.post('/facturas', { ...formF, fechaVencimiento: fmtFecha(formF.fechaVencimiento) })
    mostrarFormFactura.value = false
    Object.assign(formF, { estudianteId: null, conceptoFacturacionId: 1, montoOriginal: 0, descripcion: '' })
    cargar()
  } catch (e: any) {
    errF.value = e.response?.data?.detail || 'Error al crear factura'
  } finally { creandoF.value = false }
}

const crearPago = async () => {
  errP.value = ''
  try {
    creandoP.value = true
    await api.post('/pagos', { ...formP })
    mostrarFormPago.value = false
    Object.assign(formP, { facturaId: null, monto: 0, observaciones: '' })
    cargar()
  } catch (e: any) {
    errP.value = e.response?.data?.detail || 'Error al registrar pago'
  } finally { creandoP.value = false }
}

onMounted(cargar)
</script>
