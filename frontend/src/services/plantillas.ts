import api from './api'

export interface Plantilla {
  id: number
  codigo: string
  asunto: string
  cuerpoHtml: string
  activa: boolean
  variables?: string[]
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface CreatePlantillaRequest {
  codigo: string
  asunto: string
  cuerpoHtml: string
  activa?: boolean
  variables?: string[]
}

export interface UpdatePlantillaRequest {
  codigo?: string
  asunto?: string
  cuerpoHtml?: string
  activa?: boolean
  variables?: string[]
}

export interface LogEnvio {
  id: number
  plantillaId: number
  plantillaCodigo: string
  plantillaNombre: string
  destinatario: string
  estado: 'ENVIADO' | 'PENDIENTE' | 'ERROR' | 'RECHAZADO'
  mensaje?: string
  intentos: number
  proximoIntento?: string
  createdAt: string
  updatedAt?: string
}

export interface LogEnviosFilter {
  estado?: string
  plantillaId?: number
  destinatario?: string
  fechaDesde?: string
  fechaHasta?: string
  page?: number
  size?: number
}

class PlantillasService {
  async obtenerPlantillas(activas?: boolean): Promise<Plantilla[]> {
    try {
      // Backend paginado: devuelve Spring Page { content, totalElements, ... }.
      // Traemos hasta 200 para que la UI muestre todas sin paginacion.
      const params = new URLSearchParams()
      params.append('size', '200')
      if (activas !== undefined) params.append('activas', String(activas))
      const response = await api.get<any>(`/plantillas-email?${params}`)
      // Compatibilidad: si algun dia el backend devuelve array plano, seguir funcionando.
      const data = response.data
      if (Array.isArray(data)) return data
      return data?.content ?? []
    } catch (error) {
      console.error('Error obteniendo plantillas:', error)
      throw error
    }
  }

  async obtenerPlantillaPorId(id: number): Promise<Plantilla> {
    try {
      const response = await api.get<Plantilla>(`/plantillas-email/${id}`)
      return response.data
    } catch (error) {
      console.error(`Error obteniendo plantilla ${id}:`, error)
      throw error
    }
  }

  async obtenerPlantillaPorCodigo(codigo: string): Promise<Plantilla> {
    try {
      const response = await api.get<Plantilla>(`/plantillas/codigo/${codigo}`)
      return response.data
    } catch (error) {
      console.error(`Error obteniendo plantilla ${codigo}:`, error)
      throw error
    }
  }

  async crearPlantilla(datos: CreatePlantillaRequest): Promise<Plantilla> {
    try {
      const response = await api.post<Plantilla>('/plantillas-email', datos)
      return response.data
    } catch (error) {
      console.error('Error creando plantilla:', error)
      throw error
    }
  }

  async actualizarPlantilla(id: number, datos: UpdatePlantillaRequest): Promise<Plantilla> {
    try {
      const response = await api.put<Plantilla>(`/plantillas-email/${id}`, datos)
      return response.data
    } catch (error) {
      console.error(`Error actualizando plantilla ${id}:`, error)
      throw error
    }
  }

  async eliminarPlantilla(id: number): Promise<void> {
    try {
      await api.delete(`/plantillas-email/${id}`)
    } catch (error) {
      console.error(`Error eliminando plantilla ${id}:`, error)
      throw error
    }
  }

  async previewPlantilla(
    id: number,
    variables?: Record<string, any>
  ): Promise<{ html: string; asunto: string }> {
    try {
      const response = await api.post<{ html: string; asunto: string }>(
        `/plantillas/${id}/preview`,
        { variables }
      )
      return response.data
    } catch (error) {
      console.error('Error generando preview:', error)
      throw error
    }
  }

  async obtenerLogEnvios(filtros?: LogEnviosFilter): Promise<{
    data: LogEnvio[]
    totalElements: number
    totalPages: number
    currentPage: number
  }> {
    try {
      const params = new URLSearchParams()

      if (filtros) {
        if (filtros.estado) params.append('estado', filtros.estado)
        if (filtros.plantillaId) params.append('plantillaId', String(filtros.plantillaId))
        if (filtros.destinatario) params.append('destinatario', filtros.destinatario)
        if (filtros.fechaDesde) params.append('fechaDesde', filtros.fechaDesde)
        if (filtros.fechaHasta) params.append('fechaHasta', filtros.fechaHasta)
        if (filtros.page !== undefined) params.append('page', String(filtros.page))
        if (filtros.size !== undefined) params.append('size', String(filtros.size))
      }

      const response = await api.get(
        `/plantillas/log-envios?${params}`
      )
      return response.data
    } catch (error) {
      console.error('Error obteniendo log envíos:', error)
      throw error
    }
  }

  async reenviarEmailPrueba(
    plantillaId: number,
    email: string,
    variables?: Record<string, any>
  ): Promise<void> {
    try {
      await api.post(`/plantillas/${plantillaId}/enviar-prueba`, {
        email,
        variables
      })
    } catch (error) {
      console.error('Error reenviando email de prueba:', error)
      throw error
    }
  }
}

export default new PlantillasService()
