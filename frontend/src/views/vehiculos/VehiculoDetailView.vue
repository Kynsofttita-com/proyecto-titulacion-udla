<template>
  <div class="space-y-6">
    <div class="flex items-center gap-4">
      <Button icon="pi pi-arrow-left" text severity="secondary" @click="router.back()" />
      <h2 class="text-3xl font-bold">{{ vehiculo?.placa || 'Cargando...' }}</h2>
      <Button icon="pi pi-pencil" text @click="router.push(`/vehiculos/${vehiculoId}/editar`)" class="ml-auto" />
    </div>

    <div v-if="isLoading" class="text-center py-8">
      <ProgressSpinner></ProgressSpinner>
    </div>

    <div v-else-if="vehiculo" class="space-y-6">
      <TabView>
        <TabPanel header="Información" :header-style="{ padding: 0 }">
          <div class="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-gray-600">Placa</label>
              <p class="text-lg font-medium uppercase">{{ vehiculo.placa }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Marca</label>
              <p class="text-lg font-medium">{{ vehiculo.marca }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Modelo</label>
              <p class="text-lg font-medium">{{ vehiculo.modelo }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Año</label>
              <p class="text-lg font-medium">{{ vehiculo.año }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Color</label>
              <p class="text-lg font-medium">{{ vehiculo.color }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Capacidad</label>
              <p class="text-lg font-medium">{{ vehiculo.capacidadPasajeros }} pasajeros</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Nro. Motor</label>
              <p class="text-lg font-medium uppercase">{{ vehiculo.nroMotor }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Nro. Chasis</label>
              <p class="text-lg font-medium uppercase">{{ vehiculo.nroChasis }}</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Kilometraje</label>
              <p class="text-lg font-bold text-blue-600">{{ vehiculo.kilometraje }} km</p>
            </div>
            <div>
              <label class="block text-sm font-gray-600">Estado</label>
              <Tag :value="vehiculo.estado" :severity="estadoSeverity"></Tag>
            </div>
          </div>
        </TabPanel>

        <TabPanel header="Revisión Técnica" :header-style="{ padding: 0 }">
          <div v-if="revision" class="p-6 space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div class="p-4 bg-blue-50 rounded-lg">
                <p class="text-sm font-gray-600 mb-1">Última Revisión</p>
                <p class="text-xl font-bold">{{ revision.fechaRevision }}</p>
              </div>
              <div class="p-4 bg-purple-50 rounded-lg">
                <p class="text-sm font-gray-600 mb-1">Vencimiento</p>
                <p class="text-xl font-bold">{{ revision.fechaVencimiento }}</p>
              </div>
              <div class="p-4 rounded-lg" :class="revision.estado === 'VIGENTE' ? 'bg-green-50' : 'bg-red-50'">
                <p class="text-sm font-gray-600 mb-1">Estado</p>
                <p :class="revision.estado === 'VIGENTE' ? 'text-green-600' : 'text-red-600'" class="text-xl font-bold">
                  {{ revision.estado }}
                </p>
              </div>
            </div>
            <Button label="Actualizar Revisión" icon="pi pi-refresh" />
          </div>
        </TabPanel>

        <TabPanel header="Mantenimiento" :header-style="{ padding: 0 }">
          <div class="p-6 space-y-6">
            <Button label="Agregar Mantenimiento" icon="pi pi-plus" />
            
            <DataTable v-if="mantenimiento.length > 0" :value="mantenimiento" striped-rows table-style="min-width: 50rem">
              <Column field="tipo" header="Tipo">
                <template #body="slotProps">
                  <Tag
                    :value="slotProps.data.tipo"
                    :severity="slotProps.data.tipo === 'PREVENTIVO' ? 'info' : 'warning'"
                  ></Tag>
                </template>
              </Column>
              <Column field="descripción" header="Descripción"></Column>
              <Column field="fecha" header="Fecha"></Column>
              <Column field="costo" header="Costo">
                <template #body="slotProps">
                  ${{ slotProps.data.costo.toFixed(2) }}
                </template>
              </Column>
              <Column field="estado" header="Estado">
                <template #body="slotProps">
                  <Tag
                    :value="slotProps.data.estado"
                    :severity="slotProps.data.estado === 'COMPLETADO' ? 'success' : 'warning'"
                  ></Tag>
                </template>
              </Column>
            </DataTable>
            <div v-else class="text-center py-8 text-gray-500">
              <p>No hay registros de mantenimiento</p>
            </div>
          </div>
        </TabPanel>
      </TabView>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Button from 'primevue/button'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import ProgressSpinner from 'primevue/progressspinner'
import vehiculosService, { VehiculoResponse, MantenimientoRegistro, RevisionTecnica } from '@/services/vehiculos'

const router = useRouter()
const route = useRoute()

const isLoading = ref(false)
const vehiculo = ref<VehiculoResponse | null>(null)
const mantenimiento = ref<MantenimientoRegistro[]>([])
const revision = ref<RevisionTecnica | null>(null)

const vehiculoId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : id
})

const estadoSeverity = computed(() => {
  if (!vehiculo.value) return 'secondary'
  return vehiculo.value.estado === 'ACTIVO' ? 'success' : 'warning'
})

const cargar = async () => {
  try {
    isLoading.value = true
    const [veh, mant, rev] = await Promise.all([
      vehiculosService.obtenerVehiculo(vehiculoId.value as number),
      vehiculosService.obtenerMantenimiento(vehiculoId.value as number),
      vehiculosService.obtenerRevision(vehiculoId.value as number)
    ])
    vehiculo.value = veh
    mantenimiento.value = mant
    revision.value = rev
  } catch (error) {
    console.error('Error loading vehicle:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  cargar()
})
</script>
