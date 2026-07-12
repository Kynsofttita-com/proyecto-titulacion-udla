import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import notificacionesService, { Notificacion, NotificacionesResponse } from '@/services/notificaciones'

export const useNotificacionesStore = defineStore('notificaciones', () => {
  const notificaciones = ref<Notificacion[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const pollingInterval = ref<NodeJS.Timeout | null>(null)
  const pollingEnabled = ref(false)
  const pollingIntervalMs = 30000 // 30 segundos

  const contadorNoLeidas = computed(() => {
    return notificaciones.value.filter(n => !n.leida).length
  })

  const notificacionesNoLeidas = computed(() => {
    return notificaciones.value.filter(n => !n.leida)
  })

  const notificacionesRecientes = computed(() => {
    return notificaciones.value
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 10)
  })

  async function obtenerNotificaciones(usuarioId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await notificacionesService.obtenerNotificaciones(usuarioId, {
        size: 50,
        page: 0
      })
      notificaciones.value = response.data
    } catch (err: any) {
      error.value = err.message || 'Error cargando notificaciones'
      console.error('Error en obtenerNotificaciones:', err)
    } finally {
      loading.value = false
    }
  }

  async function obtenerNotificacionesNoLeidas(usuarioId: number) {
    try {
      const noLeidas = await notificacionesService.obtenerNotificacionesNoLeidas(usuarioId)
      // Actualizar las no leídas localmente
      notificaciones.value = notificaciones.value.map(n => {
        const actualizada = noLeidas.find(nl => nl.id === n.id)
        return actualizada || n
      })
    } catch (err: any) {
      error.value = err.message || 'Error cargando notificaciones no leídas'
      console.error('Error en obtenerNotificacionesNoLeidas:', err)
    }
  }

  async function marcarComoLeida(notificacionId: number) {
    try {
      const notificacionActualizada = await notificacionesService.marcarComoLeida(notificacionId)
      const index = notificaciones.value.findIndex(n => n.id === notificacionId)
      if (index !== -1) {
        notificaciones.value[index] = notificacionActualizada
      }
    } catch (err: any) {
      error.value = err.message || 'Error marcando notificación como leída'
      console.error('Error en marcarComoLeida:', err)
    }
  }

  async function marcarTodasComoLeidas(usuarioId: number) {
    try {
      await notificacionesService.marcarTodasComoLeidas(usuarioId)
      notificaciones.value = notificaciones.value.map(n => ({
        ...n,
        leida: true,
        leidaEn: new Date().toISOString()
      }))
    } catch (err: any) {
      error.value = err.message || 'Error marcando todas como leídas'
      console.error('Error en marcarTodasComoLeidas:', err)
    }
  }

  async function eliminarNotificacion(notificacionId: number) {
    try {
      await notificacionesService.eliminarNotificacion(notificacionId)
      notificaciones.value = notificaciones.value.filter(n => n.id !== notificacionId)
    } catch (err: any) {
      error.value = err.message || 'Error eliminando notificación'
      console.error('Error en eliminarNotificacion:', err)
    }
  }

  async function eliminarTodasNotificaciones(usuarioId: number) {
    try {
      await notificacionesService.eliminarTodasNotificaciones(usuarioId)
      notificaciones.value = []
    } catch (err: any) {
      error.value = err.message || 'Error eliminando todas notificaciones'
      console.error('Error en eliminarTodasNotificaciones:', err)
    }
  }

  function iniciarPolling(usuarioId: number) {
    if (pollingEnabled.value) return

    pollingEnabled.value = true
    obtenerNotificaciones(usuarioId)

    pollingInterval.value = setInterval(() => {
      obtenerNotificacionesNoLeidas(usuarioId)
    }, pollingIntervalMs)
  }

  function detenerPolling() {
    if (pollingInterval.value) {
      clearInterval(pollingInterval.value)
      pollingInterval.value = null
    }
    pollingEnabled.value = false
  }

  function limpiar() {
    detenerPolling()
    notificaciones.value = []
    error.value = null
    loading.value = false
  }

  return {
    // State
    notificaciones,
    loading,
    error,
    pollingEnabled,

    // Computed
    contadorNoLeidas,
    notificacionesNoLeidas,
    notificacionesRecientes,

    // Actions
    obtenerNotificaciones,
    obtenerNotificacionesNoLeidas,
    marcarComoLeida,
    marcarTodasComoLeidas,
    eliminarNotificacion,
    eliminarTodasNotificaciones,
    iniciarPolling,
    detenerPolling,
    limpiar
  }
})
