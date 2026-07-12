import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import reportesService, { DashboardKPIResponse } from '@/services/reportes'

export interface KPI {
  nombre: string
  valor: number | string
  unidad?: string
  icono?: string
  color?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  tendencia?: 'up' | 'down' | 'stable'
  porcentaje?: number
}

export const useDashboardStore = defineStore('dashboard', () => {
  const kpis = ref<Record<string, any>>({})
  const loading = ref(false)
  const error = ref<string | null>(null)
  const ultimaActualizacion = ref<Date | null>(null)
  const refreshInterval = ref<NodeJS.Timeout | null>(null)

  const tieneKPIs = computed(() => Object.keys(kpis.value).length > 0)

  const kpisFormateados = computed(() => {
    return [
      {
        nombre: 'Estudiantes Activos',
        valor: kpis.value.totalEstudiantes || 0,
        icono: 'pi-users',
        color: 'primary'
      },
      {
        nombre: 'Ingresos Este Mes',
        valor: `$${(kpis.value.ingresosEsteMes || 0).toLocaleString('es-ES')}`,
        icono: 'pi-wallet',
        color: 'success'
      },
      {
        nombre: 'Clases Hoy',
        valor: kpis.value.clasesHoy || 0,
        icono: 'pi-calendar',
        color: 'info'
      },
      {
        nombre: 'SOAT Próximo a Vencer',
        valor: kpis.value.soatPorVencer || 0,
        icono: 'pi-exclamation-triangle',
        color: 'warning'
      }
    ]
  })

  async function obtenerKPIs() {
    loading.value = true
    error.value = null
    try {
      const response = await reportesService.obtenerKPIs()
      kpis.value = response.kpis
      ultimaActualizacion.value = new Date()
    } catch (err: any) {
      error.value = err.message || 'Error cargando KPIs'
      console.error('Error en obtenerKPIs:', err)
    } finally {
      loading.value = false
    }
  }

  function iniciarAutoRefresh(intervaloMs: number = 60000) {
    if (refreshInterval.value) clearInterval(refreshInterval.value)

    refreshInterval.value = setInterval(() => {
      obtenerKPIs()
    }, intervaloMs)
  }

  function detenerAutoRefresh() {
    if (refreshInterval.value) {
      clearInterval(refreshInterval.value)
      refreshInterval.value = null
    }
  }

  function limpiar() {
    detenerAutoRefresh()
    kpis.value = {}
    error.value = null
    loading.value = false
    ultimaActualizacion.value = null
  }

  return {
    // State
    kpis,
    loading,
    error,
    ultimaActualizacion,

    // Computed
    tieneKPIs,
    kpisFormateados,

    // Actions
    obtenerKPIs,
    iniciarAutoRefresh,
    detenerAutoRefresh,
    limpiar
  }
})
