<template>
  <div class="space-y-6">
    <PageHeader
      title="Reporte: Ingresos"
      description="Facturado vs cobrado — separado por lo que ya entró y lo que aún se debe"
      icon="pi pi-arrow-up-right"
      :breadcrumbs="[
        { label: 'Inicio', to: '/dashboard' },
        { label: 'Reportes' },
        { label: 'Ingresos' }
      ]"
    >
      <template #actions>
        <ReporteExporter
          tipoReporte="INGRESOS_PERIODO"
          titulo="Reporte de Ingresos por Periodo"
          :datos="datosFiltrados"
          :tienesDatos="datosFiltrados.length > 0"
        />
        <Button
          label="Generar reporte"
          icon="pi pi-refresh"
          @click="cargar"
          :loading="cargando"
          severity="primary"
        />
      </template>
    </PageHeader>

    <!-- KPIs financieros: 4 cards diferenciadas por rol -->
    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
      <!-- Facturado (todo lo emitido, activo) -->
      <div class="bg-white rounded-lg border border-info-200 border-l-4 border-l-info-500 p-4">
        <div class="flex items-center gap-2 mb-1">
          <i class="pi pi-file text-info-600 text-sm" />
          <p class="text-xs text-ink-500 uppercase font-semibold">Facturado</p>
        </div>
        <p class="text-2xl font-bold text-info-700">{{ formatMoney(totalFacturado) }}</p>
        <p class="text-[11px] text-ink-500 mt-1">
          {{ facturasActivas }} factura(s) activa(s)
          <span v-if="facturasAnuladas > 0" class="text-ink-400">
            · {{ facturasAnuladas }} anulada(s)
          </span>
        </p>
      </div>

      <!-- Cobrado (dinero que realmente entró) -->
      <div class="bg-white rounded-lg border border-success-200 border-l-4 border-l-success-500 p-4">
        <div class="flex items-center gap-2 mb-1">
          <i class="pi pi-check-circle text-success-600 text-sm" />
          <p class="text-xs text-ink-500 uppercase font-semibold">Cobrado</p>
        </div>
        <p class="text-2xl font-bold text-success-700">{{ formatMoney(totalCobrado) }}</p>
        <p class="text-[11px] text-ink-500 mt-1">Dinero recibido en caja</p>
      </div>

      <!-- Por cobrar (saldo pendiente) -->
      <div class="bg-white rounded-lg border border-warning-200 border-l-4 border-l-warning-500 p-4">
        <div class="flex items-center gap-2 mb-1">
          <i class="pi pi-clock text-warning-600 text-sm" />
          <p class="text-xs text-ink-500 uppercase font-semibold">Por cobrar</p>
        </div>
        <p class="text-2xl font-bold text-warning-700">{{ formatMoney(totalPorCobrar) }}</p>
        <p class="text-[11px] text-ink-500 mt-1">Saldo pendiente de las facturas</p>
      </div>

      <!-- % de cobranza (indicador de salud) -->
      <div class="bg-white rounded-lg border border-brand-200 border-l-4 border-l-brand-500 p-4">
        <div class="flex items-center gap-2 mb-1">
          <i class="pi pi-percentage text-brand-600 text-sm" />
          <p class="text-xs text-ink-500 uppercase font-semibold">% Cobranza</p>
        </div>
        <p class="text-2xl font-bold" :class="colorCobranza">
          {{ porcentajeCobrado.toFixed(1) }}%
        </p>
        <div class="mt-2 h-1.5 bg-ink-100 rounded-full overflow-hidden">
          <div
            class="h-full rounded-full transition-all"
            :class="colorBarraCobranza"
            :style="{ width: `${Math.min(100, porcentajeCobrado)}%` }"
          />
        </div>
      </div>
    </div>

    <ReporteFiltros
      v-if="datos.length > 0"
      :campos="camposFiltrables"
      :datos="datos"
      @update:datosFiltrados="datosFiltrados = $event"
    />

    <!-- Tabla desglosada -->
    <DataTableCard title="Detalle de facturas">
      <template #description>
        <span class="text-xs text-ink-500">
          <span class="inline-block w-2 h-2 rounded-full bg-info-500 mr-1" /> Facturado ·
          <span class="inline-block w-2 h-2 rounded-full bg-success-500 mr-1 ml-2" /> Cobrado ·
          <span class="inline-block w-2 h-2 rounded-full bg-warning-500 mr-1 ml-2" /> Saldo pendiente
        </span>
      </template>

      <div v-if="cargando" class="flex items-center justify-center py-12">
        <i class="pi pi-spin pi-spinner text-brand-600 text-2xl" />
      </div>

      <div v-else-if="datos.length === 0" class="py-12">
        <EmptyState icon="pi pi-inbox" title="Sin datos" description="Genera el reporte" />
      </div>

      <div v-else-if="datosFiltrados.length === 0" class="py-12">
        <EmptyState icon="pi pi-filter-slash" title="Sin coincidencias" description="Ningun ingreso coincide con el filtro" />
      </div>

      <table v-else class="w-full text-sm">
        <thead class="border-b-2 border-ink-200 bg-ink-50">
          <tr>
            <th class="px-3 py-3 text-left font-semibold">Factura</th>
            <th class="px-3 py-3 text-left font-semibold">Estudiante</th>
            <th class="px-3 py-3 text-center font-semibold text-xs">Tipo</th>
            <th class="px-3 py-3 text-right font-semibold text-info-700">Facturado</th>
            <th class="px-3 py-3 text-right font-semibold text-success-700">Cobrado</th>
            <th class="px-3 py-3 text-right font-semibold text-warning-700">Saldo</th>
            <th class="px-3 py-3 text-left font-semibold text-xs">Vencimiento</th>
            <th class="px-3 py-3 text-left font-semibold">Estado</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-ink-200">
          <tr v-for="ing in datosFiltrados" :key="ing.id" class="hover:bg-ink-50">
            <td class="px-3 py-3">
              <p class="font-mono font-bold text-brand-700 text-xs">{{ ing.numeroFactura || `#${ing.id}` }}</p>
              <p class="text-[10px] text-ink-500 mt-0.5">{{ formatearFecha(ing.fechaEmision) }}</p>
            </td>
            <td class="px-3 py-3">
              <p class="text-sm font-medium">{{ ing.estudianteNombre }}</p>
            </td>
            <td class="px-3 py-3 text-center">
              <span
                class="inline-flex px-2 py-0.5 rounded text-[10px] font-bold uppercase"
                :class="ing.tipoPago === 'CREDITO' ? 'bg-info-100 text-info-700' : 'bg-brand-100 text-brand-700'"
              >
                {{ ing.tipoPago }}
              </span>
              <p v-if="ing.tipoPago === 'CREDITO' && ing.numeroCuotas" class="text-[10px] text-ink-500 mt-0.5">
                {{ ing.cuotasPagadas || 0 }}/{{ ing.numeroCuotas }} cuotas
              </p>
            </td>
            <td class="px-3 py-3 text-right font-mono text-info-700 font-semibold">
              {{ formatMoney(ing.montoFacturado) }}
            </td>
            <td class="px-3 py-3 text-right font-mono text-success-700 font-bold">
              {{ formatMoney(ing.montoCobrado) }}
            </td>
            <td class="px-3 py-3 text-right font-mono" :class="Number(ing.saldo) > 0 ? 'text-warning-700 font-bold' : 'text-ink-400'">
              {{ formatMoney(ing.saldo) }}
            </td>
            <td class="px-3 py-3 text-xs">
              {{ formatearFecha(ing.fechaVencimiento) }}
            </td>
            <td class="px-3 py-3">
              <StatusBadge :status="ing.estado" />
            </td>
          </tr>
        </tbody>
        <tfoot class="border-t-2 border-ink-300 bg-ink-50 font-bold">
          <tr>
            <td colspan="3" class="px-3 py-3 text-right text-xs uppercase text-ink-600">Totales</td>
            <td class="px-3 py-3 text-right font-mono text-info-700">{{ formatMoney(sumaFacturadoFiltrado) }}</td>
            <td class="px-3 py-3 text-right font-mono text-success-700">{{ formatMoney(sumaCobradoFiltrado) }}</td>
            <td class="px-3 py-3 text-right font-mono text-warning-700">{{ formatMoney(sumaSaldoFiltrado) }}</td>
            <td colspan="2"></td>
          </tr>
        </tfoot>
      </table>
    </DataTableCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTableCard from '@/components/ui/DataTableCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import ReporteExporter from '@/components/reportes/ReporteExporter.vue'
import ReporteFiltros, { type CampoFiltro } from '@/components/reportes/ReporteFiltros.vue'
import Button from 'primevue/button'
import reportesService from '@/services/reportes'
import { fmtFechaLocal } from '@/utils/fechas'

const datos = ref<any[]>([])
const datosFiltrados = ref<any[]>([])
const cargando = ref(false)
const totalFacturado = ref(0)
const totalCobrado = ref(0)
const totalPorCobrar = ref(0)
const porcentajeCobrado = ref(0)
const facturasActivas = ref(0)
const facturasAnuladas = ref(0)

const camposFiltrables: CampoFiltro[] = [
  { key: 'estudianteNombre', label: 'Estudiante',       tipo: 'text' },
  { key: 'numeroFactura',    label: 'N° Factura',       tipo: 'text' },
  { key: 'tipoPago',         label: 'Tipo',             tipo: 'select', opciones: ['CONTADO', 'CREDITO'] },
  { key: 'estado',           label: 'Estado',           tipo: 'select', opciones: ['PENDIENTE', 'PARCIAL', 'PAGADA', 'ANULADA'] },
  { key: 'fechaEmision',     label: 'Fecha Emisión',    tipo: 'date' },
  { key: 'fechaVencimiento', label: 'Vencimiento',      tipo: 'date' },
  { key: 'montoFacturado',   label: 'Monto facturado',  tipo: 'number' },
  { key: 'montoCobrado',     label: 'Monto cobrado',    tipo: 'number' },
  { key: 'saldo',            label: 'Saldo pendiente',  tipo: 'number' }
]

// Colores del % cobranza según salud financiera.
const colorCobranza = computed(() => {
  const p = porcentajeCobrado.value
  if (p >= 80) return 'text-success-700'
  if (p >= 50) return 'text-warning-700'
  return 'text-danger-700'
})

const colorBarraCobranza = computed(() => {
  const p = porcentajeCobrado.value
  if (p >= 80) return 'bg-success-500'
  if (p >= 50) return 'bg-warning-500'
  return 'bg-danger-500'
})

// Totales calculados sobre el filtro aplicado (para que sumen lo visible).
const sumaFacturadoFiltrado = computed(() =>
  datosFiltrados.value
    .filter(d => d.estado !== 'ANULADA')
    .reduce((s, d) => s + Number(d.montoFacturado || 0), 0)
)
const sumaCobradoFiltrado = computed(() =>
  datosFiltrados.value
    .filter(d => d.estado !== 'ANULADA')
    .reduce((s, d) => s + Number(d.montoCobrado || 0), 0)
)
const sumaSaldoFiltrado = computed(() =>
  datosFiltrados.value
    .filter(d => d.estado !== 'ANULADA')
    .reduce((s, d) => s + Number(d.saldo || 0), 0)
)

function formatMoney(valor: any): string {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'USD'
  }).format(Number(valor) || 0)
}

function formatearFecha(fecha: string): string {
  return fmtFechaLocal(fecha, {}, '--', 'es-ES')
}

async function cargar() {
  cargando.value = true
  try {
    const response = await reportesService.generarReporteIngresos({
      tipoReporte: 'INGRESOS_PERIODO'
    })
    const d = response.datos || {}
    // Nuevos campos del backend (fallback a los legacy si el backend viejo aún corre).
    totalFacturado.value  = Number(d.totalFacturado ?? d.totalIngresos ?? 0)
    totalCobrado.value    = Number(d.totalCobrado ?? 0)
    totalPorCobrar.value  = Number(d.totalPorCobrar ?? 0)
    porcentajeCobrado.value = Number(d.porcentajeCobrado ?? 0)
    facturasActivas.value = Number(d.facturasActivas ?? d.totalTransacciones ?? 0)
    facturasAnuladas.value = Number(d.facturasAnuladas ?? 0)
    datos.value = d.ingresos || d.data || []
  } catch (error) {
    console.error('Error:', error)
    totalFacturado.value = 0
    totalCobrado.value = 0
    totalPorCobrar.value = 0
    porcentajeCobrado.value = 0
    facturasActivas.value = 0
    facturasAnuladas.value = 0
    datos.value = []
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
