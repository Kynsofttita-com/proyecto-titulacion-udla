<template>
  <div class="space-y-6">
    <div class="flex items-center gap-4">
      <Button icon="pi pi-arrow-left" text severity="secondary" @click="router.back()" />
      <h2 class="text-3xl font-bold">Registrar Pago</h2>
    </div>

    <div class="p-6 bg-white rounded-lg shadow">
      <form @submit.prevent="guardar" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="block text-sm font-medium mb-2">Factura *</label>
            <Dropdown
              v-model="form.facturaId"
              :options="facturas"
              option-label="descripción"
              option-value="id"
              placeholder="Selecciona una factura"
              class="w-full"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Monto a Pagar *</label>
            <InputNumber
              v-model="form.monto"
              mode="currency"
              currency="USD"
              class="w-full"
              :min-fraction-digits="2"
              :max-fraction-digits="2"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Fecha del Pago *</label>
            <Calendar
              v-model="form.fecha"
              date-format="dd/mm/yy"
              :show-icon="true"
              class="w-full"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-2">Método de Pago *</label>
            <Dropdown
              v-model="form.metodoPago"
              :options="[
                { label: 'Efectivo', value: 'EFECTIVO' },
                { label: 'Transferencia', value: 'TRANSFERENCIA' },
                { label: 'Cheque', value: 'CHEQUE' },
                { label: 'Tarjeta', value: 'TARJETA' }
              ]"
              option-label="label"
              option-value="value"
              placeholder="Selecciona un método"
              class="w-full"
              required
            />
          </div>
          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-2">Referencia (comprobante, número cheque, etc.)</label>
            <InputText v-model="form.referencia" class="w-full" placeholder="Opcional" />
          </div>
        </div>

        <div class="flex gap-4 justify-end">
          <Button label="Cancelar" severity="secondary" @click="router.back()" />
          <Button label="Registrar Pago" type="submit" :loading="isLoading" />
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Button from 'primevue/button'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import cobrosService from '@/services/cobros'

const router = useRouter()
const route = useRoute()

const isLoading = ref(false)
const facturas = ref([])
const facturaIdPredeterminada = route.query.facturaId ? parseInt(route.query.facturaId as string) : null

const form = reactive({
  facturaId: facturaIdPredeterminada,
  monto: 0,
  fecha: new Date(),
  metodoPago: 'EFECTIVO',
  referencia: ''
})

const cargarFacturas = async () => {
  try {
    const response = await cobrosService.obtenerFacturas(0, 1000)
    facturas.value = response.content
  } catch (error) {
    console.error('Error loading facturas:', error)
  }
}

const guardar = async () => {
  try {
    isLoading.value = true
    const fecha = (form.fecha as Date).toISOString().split('T')[0]
    await cobrosService.crearPago({
      facturaId: form.facturaId as number,
      monto: form.monto as number,
      fecha,
      metodoPago: form.metodoPago as any,
      referencia: form.referencia || undefined
    })
    router.push('/cobros')
  } catch (error) {
    console.error('Error saving:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  cargarFacturas()
})
</script>
