<template>
  <div v-if="mostrar" class="relative">
    <button
      @click="mostrarDropdown = !mostrarDropdown"
      class="relative w-9 h-9 rounded-md hover:bg-ink-100 text-ink-600 flex items-center justify-center transition"
      :title="tituloBoton"
    >
      <i class="pi pi-car text-sm" />
      <span
        v-if="totalAlertas > 0"
        class="absolute -top-1 -right-1 min-w-[20px] h-5 px-1 text-white text-[10px] font-bold rounded-full flex items-center justify-center"
        :class="colorBadge"
      >
        {{ totalAlertas > 9 ? '9+' : totalAlertas }}
      </span>
    </button>

    <transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="transform opacity-0 scale-95"
      enter-to-class="transform opacity-100 scale-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="transform opacity-100 scale-100"
      leave-to-class="transform opacity-0 scale-95"
    >
      <div
        v-if="mostrarDropdown"
        class="absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-lg border border-ink-200 z-50"
        @click.stop
      >
        <div class="px-4 py-3 border-b border-ink-200 flex items-center justify-between">
          <div>
            <h3 class="font-semibold text-ink-900 flex items-center gap-2">
              <i class="pi pi-car text-brand-600" />
              Alertas SOAT
            </h3>
            <p class="text-[11px] text-ink-500 mt-0.5">Vencimientos próximos y vencidos</p>
          </div>
          <button
            @click="mostrarDropdown = false"
            class="text-ink-400 hover:text-ink-600"
          >
            <i class="pi pi-times text-sm" />
          </button>
        </div>

        <div class="max-h-96 overflow-y-auto">
          <div v-if="cargando" class="text-center py-8">
            <i class="pi pi-spin pi-spinner text-brand-600" />
            <p class="text-sm text-ink-500 mt-2">Cargando...</p>
          </div>

          <div v-else-if="alertas.length === 0" class="px-4 py-8 text-center">
            <i class="pi pi-check-circle text-2xl text-green-500 mb-2" />
            <p class="text-sm text-ink-500">Todos los vehículos al día</p>
          </div>

          <ul v-else class="divide-y divide-ink-100">
            <li
              v-for="alerta in alertasOrdenadas"
              :key="alerta.vehiculoId"
              class="px-4 py-3 hover:bg-ink-50 transition cursor-pointer"
              :class="claseFilaAlerta(alerta)"
              @click="irAVehiculo(alerta.vehiculoId)"
            >
              <div class="flex items-start gap-3">
                <div
                  class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0"
                  :class="claseIcono(alerta)"
                >
                  <i class="pi pi-car text-sm" />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2">
                    <p class="font-mono font-bold text-brand-700 text-sm">
                      {{ alerta.placa }}
                    </p>
                    <span
                      class="text-[10px] px-1.5 py-0.5 rounded font-semibold uppercase"
                      :class="badgeEstado(alerta)"
                    >
                      {{ etiquetaEstado(alerta) }}
                    </span>
                  </div>
                  <p class="text-xs text-ink-600 truncate mt-0.5">
                    {{ alerta.marca }} {{ alerta.modelo }}
                  </p>
                  <div class="flex items-center gap-3 mt-1 text-[11px] text-ink-500">
                    <span>
                      <i class="pi pi-calendar text-[10px] mr-1" />
                      {{ formatearFecha(alerta.soatVencimiento) }}
                    </span>
                    <span :class="colorTexto(alerta)">
                      {{ textoDias(alerta) }}
                    </span>
                  </div>
                </div>
              </div>
            </li>
          </ul>
        </div>

        <div v-if="alertas.length > 0" class="px-4 py-3 border-t border-ink-200 text-center">
          <router-link
            to="/reportes/operativos/vehiculos"
            class="text-sm text-brand-600 hover:text-brand-700 font-medium"
            @click="mostrarDropdown = false"
          >
            Ver reporte completo →
          </router-link>
        </div>
      </div>
    </transition>

    <div
      v-if="mostrarDropdown"
      @click="mostrarDropdown = false"
      class="fixed inset-0 z-40"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import vehiculosService, { type AlertaSoatResponse } from '@/services/vehiculos'

const router = useRouter()
const authStore = useAuthStore()

const alertas = ref<AlertaSoatResponse[]>([])
const cargando = ref(false)
const mostrarDropdown = ref(false)
let intervalId: number | null = null

const rolesConAcceso = ['ADMIN', 'STAFF']
const mostrar = computed(() => rolesConAcceso.includes(authStore.currentRole || ''))

const totalAlertas = computed(() => alertas.value.length)

const alertasOrdenadas = computed(() =>
  [...alertas.value].sort((a, b) => a.diasParaVencer - b.diasParaVencer)
)

const tituloBoton = computed(() => {
  if (totalAlertas.value === 0) return 'Alertas SOAT - Todo al día'
  const vencidos = alertas.value.filter(a => a.vencido).length
  const proximos = totalAlertas.value - vencidos
  return `${vencidos} vencido(s), ${proximos} próximo(s) a vencer`
})

const colorBadge = computed(() => {
  const tieneVencidos = alertas.value.some(a => a.vencido)
  const tieneUrgentes = alertas.value.some(a => !a.vencido && a.diasParaVencer <= 7)
  if (tieneVencidos) return 'bg-red-500'
  if (tieneUrgentes) return 'bg-orange-500'
  return 'bg-yellow-500'
})

function claseFilaAlerta(a: AlertaSoatResponse): string {
  if (a.vencido) return 'border-l-2 border-red-500 bg-red-50/40'
  if (a.diasParaVencer <= 7) return 'border-l-2 border-orange-500 bg-orange-50/40'
  return 'border-l-2 border-yellow-500 bg-yellow-50/40'
}

function claseIcono(a: AlertaSoatResponse): string {
  if (a.vencido) return 'bg-red-100 text-red-600'
  if (a.diasParaVencer <= 7) return 'bg-orange-100 text-orange-600'
  return 'bg-yellow-100 text-yellow-600'
}

function badgeEstado(a: AlertaSoatResponse): string {
  if (a.vencido) return 'bg-red-100 text-red-700'
  if (a.diasParaVencer <= 7) return 'bg-orange-100 text-orange-700'
  return 'bg-yellow-100 text-yellow-700'
}

function etiquetaEstado(a: AlertaSoatResponse): string {
  if (a.vencido) return 'Vencido'
  if (a.diasParaVencer <= 7) return 'Urgente'
  return 'Próximo'
}

function colorTexto(a: AlertaSoatResponse): string {
  if (a.vencido) return 'text-red-600 font-semibold'
  if (a.diasParaVencer <= 7) return 'text-orange-600 font-semibold'
  return 'text-yellow-700 font-medium'
}

function textoDias(a: AlertaSoatResponse): string {
  if (a.vencido) {
    const dias = Math.abs(a.diasParaVencer)
    return `Vencido hace ${dias} día${dias === 1 ? '' : 's'}`
  }
  if (a.diasParaVencer === 0) return 'Vence hoy'
  if (a.diasParaVencer === 1) return 'Vence mañana'
  return `Vence en ${a.diasParaVencer} días`
}

function formatearFecha(fecha: string): string {
  if (!fecha) return '--'
  // El backend manda LocalDate "YYYY-MM-DD" (sin hora). new Date(str) lo
  // interpreta como UTC medianoche, y al mostrar en TZ Ecuador (UTC-5)
  // aparece el dia anterior. Parseamos manualmente en local para evitarlo.
  const [y, m, d] = fecha.substring(0, 10).split('-').map(Number)
  if (!y || !m || !d) return fecha
  return new Date(y, m - 1, d).toLocaleDateString('es-ES')
}

function irAVehiculo(id: number) {
  mostrarDropdown.value = false
  router.push(`/vehiculos/${id}`)
}

async function cargarAlertas() {
  if (!mostrar.value) return
  cargando.value = true
  try {
    alertas.value = await vehiculosService.alertasSoat(30)
  } catch (err) {
    console.error('Error cargando alertas SOAT:', err)
    alertas.value = []
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargarAlertas()
  intervalId = window.setInterval(cargarAlertas, 60000)
})

onUnmounted(() => {
  if (intervalId !== null) {
    clearInterval(intervalId)
    intervalId = null
  }
})
</script>
