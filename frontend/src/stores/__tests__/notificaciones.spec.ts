import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificacionesStore } from '../notificaciones'
import * as notificacionesService from '@/services/notificaciones'

vi.mock('@/services/notificaciones')

describe('useNotificacionesStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('inicializa con estado vacío', () => {
    const store = useNotificacionesStore()

    expect(store.notificaciones).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.pollingEnabled).toBe(false)
  })

  it('calcula correctamente contadorNoLeidas', () => {
    const store = useNotificacionesStore()

    store.notificaciones = [
      {
        id: 1,
        usuarioId: 1,
        titulo: 'Notif 1',
        contenido: 'Contenido',
        tipo: 'IN_APP',
        prioridad: 'NORMAL',
        leida: false,
        createdAt: '2026-07-09T10:00:00Z'
      },
      {
        id: 2,
        usuarioId: 1,
        titulo: 'Notif 2',
        contenido: 'Contenido',
        tipo: 'IN_APP',
        prioridad: 'NORMAL',
        leida: true,
        createdAt: '2026-07-09T10:01:00Z'
      }
    ]

    expect(store.contadorNoLeidas).toBe(1)
  })

  it('obtiene notificacionesNoLeidas', () => {
    const store = useNotificacionesStore()

    store.notificaciones = [
      {
        id: 1,
        usuarioId: 1,
        titulo: 'No leída',
        contenido: 'Contenido',
        tipo: 'IN_APP',
        prioridad: 'NORMAL',
        leida: false,
        createdAt: '2026-07-09T10:00:00Z'
      },
      {
        id: 2,
        usuarioId: 1,
        titulo: 'Leída',
        contenido: 'Contenido',
        tipo: 'IN_APP',
        prioridad: 'NORMAL',
        leida: true,
        createdAt: '2026-07-09T10:01:00Z'
      }
    ]

    expect(store.notificacionesNoLeidas).toHaveLength(1)
    expect(store.notificacionesNoLeidas[0].leida).toBe(false)
  })

  it('obtiene notificacionesRecientes (max 10 ordenadas por fecha desc)', () => {
    const store = useNotificacionesStore()

    store.notificaciones = Array.from({ length: 15 }, (_, i) => ({
      id: i,
      usuarioId: 1,
      titulo: `Notif ${i}`,
      contenido: 'Contenido',
      tipo: 'IN_APP' as const,
      prioridad: 'NORMAL' as const,
      leida: false,
      createdAt: new Date(Date.now() - i * 1000).toISOString()
    }))

    expect(store.notificacionesRecientes).toHaveLength(10)
    expect(store.notificacionesRecientes[0].id).toBe(0)
  })

  it('marcarComoLeida actualiza el estado', async () => {
    const store = useNotificacionesStore()

    const notif = {
      id: 1,
      usuarioId: 1,
      titulo: 'Test',
      contenido: 'Contenido',
      tipo: 'IN_APP' as const,
      prioridad: 'NORMAL' as const,
      leida: false,
      createdAt: '2026-07-09T10:00:00Z'
    }

    store.notificaciones = [notif]

    vi.spyOn(notificacionesService.default, 'marcarComoLeida').mockResolvedValue({
      ...notif,
      leida: true,
      leidaEn: '2026-07-09T10:01:00Z'
    })

    await store.marcarComoLeida(1)

    expect(store.notificaciones[0].leida).toBe(true)
  })

  it('eliminarNotificacion remueve del estado', async () => {
    const store = useNotificacionesStore()

    store.notificaciones = [
      {
        id: 1,
        usuarioId: 1,
        titulo: 'Test',
        contenido: 'Contenido',
        tipo: 'IN_APP',
        prioridad: 'NORMAL',
        leida: false,
        createdAt: '2026-07-09T10:00:00Z'
      }
    ]

    vi.spyOn(notificacionesService.default, 'eliminarNotificacion').mockResolvedValue(undefined)

    await store.eliminarNotificacion(1)

    expect(store.notificaciones).toHaveLength(0)
  })

  it('limpiar resetea el estado completo', () => {
    const store = useNotificacionesStore()

    store.notificaciones = [
      {
        id: 1,
        usuarioId: 1,
        titulo: 'Test',
        contenido: 'Contenido',
        tipo: 'IN_APP',
        prioridad: 'NORMAL',
        leida: false,
        createdAt: '2026-07-09T10:00:00Z'
      }
    ]
    store.loading = true
    store.error = 'Test error'
    store.pollingEnabled = true

    store.limpiar()

    expect(store.notificaciones).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.pollingEnabled).toBe(false)
  })

  it('iniciarPolling y detenerPolling', () => {
    vi.useFakeTimers()
    const store = useNotificacionesStore()

    vi.spyOn(store, 'obtenerNotificaciones').mockResolvedValue()

    expect(store.pollingEnabled).toBe(false)

    store.iniciarPolling(1)

    expect(store.pollingEnabled).toBe(true)

    store.detenerPolling()

    expect(store.pollingEnabled).toBe(false)

    vi.useRealTimers()
  })
})
