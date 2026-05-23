<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-3xl font-bold">Cobros y Facturación</h2>
      <Button label="Nueva Factura" icon="pi pi-plus" @click="navigateToFormFactura" />
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div class="p-6 bg-blue-50 rounded-lg border-l-4 border-blue-500">
        <p class="text-sm text-gray-600 mb-2">Total Facturado</p>
        <p class="text-3xl font-bold text-blue-600">${{ estadoCuenta?.totalFacturado.toFixed(2) || '0.00' }}</p>
      </div>

      <div class="p-6 bg-green-50 rounded-lg border-l-4 border-green-500">
        <p class="text-sm text-gray-600 mb-2">Total Pagado</p>
        <p class="text-3xl font-bold text-green-600">${{ estadoCuenta?.totalPagado.toFixed(2) || '0.00' }}</p>
      </div>

      <div class="p-6 bg-orange-50 rounded-lg border-l-4 border-orange-500">
        <p class="text-sm text-gray-600 mb-2">Saldo Pendiente</p>
        <p class="text-3xl font-bold text-orange-600">${{ estadoCuenta?.saldoPendiente.toFixed(2) || '0.00' }}</p>
      </div>

      <div class="p-6 bg-purple-50 rounded-lg border-l-4 border-purple-500">
        <p class="text-sm text-gray-600 mb-2">Estado General</p>
        <Tag
          :value="estadoCuenta?.estadoGeneral || 'N/A'"
          :severity="statusSeverity(estadoCuenta?.estadoGeneral)"
          class="mt-2"
        ></Tag>
      </div>
    </div>

    <TabView>
      <TabPanel header="Facturas" :header-style="{ padding: 0 }">
        <div class="p-6">
          <DataTable
            v-if="estadoCuenta?.facturas"
            :value="estadoCuenta.facturas"
            striped-rows
            table-style="min-width: 50rem"
            paginator
            :rows="10"
          >
            <Column field="id" header="Factura #"></Column>
            <Column field="descripción" header="Descripción"></Column>
            <Column field="monto" header="Monto">
              <template #body="slotProps">
                ${{ slotProps.data.monto.toFixed(2) }}
              </template>
            </Column>
            <Column field="fechaEmisión" header="Emisión"></Column>
            <Column field="fechaVencimiento" header="Vencimiento"></Column>
            <Column field="montoPagado" header="Pagado">
              <template #body="slotProps">
                ${{ slotProps.data.montoPagado.toFixed(2) }}
              </template>
            </Column>
            <Column field="estado" header="Estado">
              <template #body="slotProps">
                <Tag :value="slotProps.data.estado" :severity="facturaSeverity(slotProps.data.estado)" />
              </template>
            </Column>
            <Column header="Acciones" style="width: 150px">
              <template #body="slotProps">
                <Button
                  icon="pi pi-credit-card"
                  class="p-button-rounded p-button-text p-button-sm mr-2"
                  @click="navigateToFormPago(slotProps.data.id)"
                  title="Registrar pago"
                />
                <Button
                  icon="pi pi-eye"
                  class="p-button-rounded p-button-text p-button-sm"
                  @click="navigateToDetailFactura(slotProps.data.id)"
                />
              </template>
            </Column>
          </DataTable>
        </div>
      </TabPanel>

      <TabPanel header="Pagos" :header-style="{ padding: 0 }">
        <div class="p-6">
          <DataTable
            v-if="estadoCuenta?.pagos"
            :value="estadoCuenta.pagos"
            striped-rows
            table-style="min-width: 50rem"
            paginator
            :rows="10"
          >
            <Column field="id" header="Pago #"></Column>
            <Column field="monto" header="Monto">
              <template #body="slotProps">
                ${{ slotProps.data.monto.toFixed(2) }}
              </template>
            </Column>
            <Column field="fecha" header="Fecha"></Column>
            <Column field="metodoPago" header="Método"></Column>
            <Column field="referencia" header="Referencia"></Column>
            <Column field="estado" header="Estado">
              <template #body="slotProps">
                <Tag :value="slotProps.data.estado" :severity="pagoSeverity(slotProps.data.estado)" />
              </template>
            </Column>
          </DataTable>
        </div>
      </TabPanel>
    </TabView>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Tag from 'primevue/tag'
import cobrosService, { EstadoCuentaResponse } from '@/services/cobros'

const router = useRouter()

const estadoCuenta = ref<EstadoCuentaResponse | null>(null)
const isLoading = ref(false)
const estudianteId = ref(1) // Default, should come from auth or route

const statusSeverity = (estado?: string) => {
  switch (estado) {
    case 'SALDADO':
      return 'success'
    case 'PENDIENTE':
      return 'warning'
    case 'PAGO_EXCESIVO':
      return 'info'
    default:
      return 'secondary'
  }
}

const facturaSeverity = (estado: string) => {
  switch (estado) {
    case 'PAGADA':
      return 'success'
    case 'PENDIENTE':
      return 'danger'
    case 'PARCIALMENTE_PAGADA':
      return 'warning'
    default:
      return 'secondary'
  }
}

const pagoSeverity = (estado: string) => {
  return estado === 'VERIFICADO' ? 'success' : 'info'
}

const cargarEstadoCuenta = async () => {
  try {
    isLoading.value = true
    estadoCuenta.value = await cobrosService.obtenerEstadoCuenta(estudianteId.value)
  } catch (error) {
    console.error('Error loading account:', error)
  } finally {
    isLoading.value = false
  }
}

const navigateToFormFactura = () => {
  router.push('/cobros/factura/nuevo')
}

const navigateToFormPago = (facturaId: number) => {
  router.push(`/cobros/pago/nueva?facturaId=${facturaId}`)
}

const navigateToDetailFactura = (id: number) => {
  router.push(`/cobros/factura/${id}`)
}

onMounted(() => {
  cargarEstadoCuenta()
})
</script>
