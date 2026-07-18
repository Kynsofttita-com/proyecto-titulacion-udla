import api from './api'

export interface Notificacion {
  id: number
  usuarioId: number
  titulo: string
  mensaje: string
  tipo: string
  prioridad: 'BAJA' | 'NORMAL' | 'ALTA'
  leida: boolean
  fechaCreacion?: string
  fechaLectura?: string
  createdAt: string
  updatedAt?: string
}

export interface FiltroNotificacionesRequest {
  usuarioId: number
  leida?: boolean
  tipo?: string
  prioridad?: string
  desde?: string
  hasta?: string
  page?: number
  size?: number
}

export interface NotificacionesResponse {
  data: Notificacion[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
}

// Formato Page<> devuelto por Spring Data (backend)
interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

class NotificacionesService {
  async obtenerNotificaciones(
    usuarioId: number,
    filtros?: Partial<FiltroNotificacionesRequest>
  ): Promise<NotificacionesResponse> {
    try {
      const params = new URLSearchParams({
        usuarioId: String(usuarioId),
        ...Object.entries(filtros || {})
          .filter(([, v]) => v !== undefined && v !== null)
          .reduce((acc, [k, v]) => ({ ...acc, [k]: String(v) }), {})
      })

      const response = await api.get<SpringPage<Notificacion>>(
        `/notificaciones?${params}`
      )
      return {
        data: response.data.content ?? [],
        totalElements: response.data.totalElements ?? 0,
        totalPages: response.data.totalPages ?? 0,
        currentPage: response.data.number ?? 0,
        pageSize: response.data.size ?? 0
      }
    } catch (error) {
      console.error('Error obteniendo notificaciones:', error)
      throw error
    }
  }

  async obtenerNotificacionesNoLeidas(usuarioId: number): Promise<Notificacion[]> {
    try {
      const response = await this.obtenerNotificaciones(usuarioId, {
        leida: false
      })
      return response.data
    } catch (error) {
      console.error('Error obteniendo notificaciones no leídas:', error)
      throw error
    }
  }

  async obtenerNotificacionesPorTipo(
    usuarioId: number,
    tipo: string
  ): Promise<Notificacion[]> {
    try {
      const response = await this.obtenerNotificaciones(usuarioId, { tipo })
      return response.data
    } catch (error) {
      console.error(`Error obteniendo notificaciones tipo ${tipo}:`, error)
      throw error
    }
  }

  async obtenerNotificacionesPorPrioridad(
    usuarioId: number,
    prioridad: string
  ): Promise<Notificacion[]> {
    try {
      const response = await this.obtenerNotificaciones(usuarioId, { prioridad })
      return response.data
    } catch (error) {
      console.error(`Error obteniendo notificaciones prioridad ${prioridad}:`, error)
      throw error
    }
  }

  async marcarComoLeida(notificacionId: number): Promise<Notificacion> {
    try {
      const response = await api.put<Notificacion>(
        `/notificaciones/${notificacionId}/marcar-leida`
      )
      return response.data
    } catch (error) {
      console.error('Error marcando notificación como leída:', error)
      throw error
    }
  }

  async marcarTodasComoLeidas(usuarioId: number): Promise<void> {
    try {
      await api.put(`/notificaciones/usuario/${usuarioId}/marcar-todas-leidas`)
    } catch (error) {
      console.error('Error marcando todas como leídas:', error)
      throw error
    }
  }

  async eliminarNotificacion(notificacionId: number): Promise<void> {
    try {
      await api.delete(`/notificaciones/${notificacionId}`)
    } catch (error) {
      console.error('Error eliminando notificación:', error)
      throw error
    }
  }

  async eliminarTodasNotificaciones(usuarioId: number): Promise<void> {
    try {
      await api.delete(`/notificaciones/usuario/${usuarioId}`)
    } catch (error) {
      console.error('Error eliminando todas notificaciones:', error)
      throw error
    }
  }
}

export default new NotificacionesService()
