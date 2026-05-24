import api from './api'

export interface CreateAsignacionRequest {
  estudianteId: number
  instructorId: number
  vehiculoId: number
  fechaProgramada: string
  horaInicio: string
  horaFin: string
  tipoClase: 'PRACTICA_CALLE' | 'PRACTICA_PATIO' | 'TEORIA'
}

export interface UpdateAsignacionRequest extends CreateAsignacionRequest {
  id: number
}

export interface AsignacionResponse {
  id: number
  estudianteId: number
  nombreEstudiante: string
  instructorId: number
  nombreInstructor: string
  vehiculoId: number
  placaVehiculo: string
  fechaProgramada: string
  horaInicio: string
  horaFin: string
  tipoClase: string
  estado: 'PROGRAMADA' | 'COMPLETADA' | 'CANCELADA'
  observaciones: string | null
}

export interface AvailabilityCheckResponse {
  disponible: boolean
  conflictos: string[]
}

export interface ReprogramacionRequest {
  asignacionId: number
  nuevaFecha: string
  nuevaHoraInicio: string
  nuevaHoraFin: string
  motivo: string
}

const asignacionesService = {
  async obtenerAsignaciones(page: number = 0, size: number = 10): Promise<{ content: AsignacionResponse[]; totalElements: number }> {
    const response = await api.get<{ content: AsignacionResponse[]; totalElements: number }>('/asignaciones', {
      params: { page, size }
    })
    return response.data
  },

  async obtenerAsignacionesPorFecha(fecha: string): Promise<AsignacionResponse[]> {
    const response = await api.get<AsignacionResponse[]>('/asignaciones/fecha', {
      params: { fecha }
    })
    return response.data
  },

  async obtenerAsignacion(id: number): Promise<AsignacionResponse> {
    const response = await api.get<AsignacionResponse>(`/asignaciones/${id}`)
    return response.data
  },

  async crearAsignacion(data: CreateAsignacionRequest): Promise<AsignacionResponse> {
    const response = await api.post<AsignacionResponse>('/asignaciones', data)
    return response.data
  },

  async actualizarAsignacion(id: number, data: UpdateAsignacionRequest): Promise<AsignacionResponse> {
    const response = await api.put<AsignacionResponse>(`/asignaciones/${id}`, data)
    return response.data
  },

  async eliminarAsignacion(id: number): Promise<void> {
    await api.delete(`/asignaciones/${id}`)
  },

  async verificarDisponibilidad(data: CreateAsignacionRequest): Promise<AvailabilityCheckResponse> {
    const response = await api.post<AvailabilityCheckResponse>('/asignaciones/verificar-disponibilidad', data)
    return response.data
  },

  async reprogramarAsignacion(data: ReprogramacionRequest): Promise<AsignacionResponse> {
    const response = await api.post<AsignacionResponse>('/asignaciones/reprogramar', data)
    return response.data
  },

  async obtenerHistorial(estudianteId: number): Promise<HistorialAsignacionItem[]> {
    const response = await api.get<{ content: HistorialAsignacionItem[] }>(
      `/asignaciones/estudiante/${estudianteId}`,
      { params: { size: 100, sort: 'fechaHora,desc' } }
    )
    return response.data?.content ?? []
  }
}

/**
 * Forma cruda del backend (AsignacionListResponse) — sin nombres resueltos.
 */
export interface HistorialAsignacionItem {
  id: number
  estudianteId: number
  instructorId: number
  vehiculoId: number
  fecha: string
  horaInicio: string
  horaFin: string
  estado: string
  dateCreated: string
}

export default asignacionesService
