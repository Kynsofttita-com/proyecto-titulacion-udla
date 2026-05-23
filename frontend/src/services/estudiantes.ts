import api from './api'

export interface CreateEstudianteRequest {
  email: string
  nombreCompleto: string
  cédula: string
  fechaNacimiento: string
  género: string
  teléfono: string
  dirección: string
}

export interface UpdateEstudianteRequest extends CreateEstudianteRequest {
  id: number
}

export interface EstudianteResponse {
  id: number
  email: string
  nombreCompleto: string
  cédula: string
  fechaNacimiento: string
  género: string
  teléfono: string
  dirección: string
  estado: 'ACTIVO' | 'INACTIVO'
  fechaMatriculación: string
}

export interface ProgresoAcademico {
  horasCompletadas: number
  horasRequeridas: number
  porcentajeComplección: number
  asignacionesCompletadas: number
  asignacionesPendientes: number
}

const estudiantesService = {
  async obtenerEstudiantes(page: number = 0, size: number = 10): Promise<{ content: EstudianteResponse[]; totalElements: number }> {
    const response = await api.get<{ content: EstudianteResponse[]; totalElements: number }>('/estudiantes', {
      params: { page, size }
    })
    return response.data
  },

  async buscarEstudiantes(término: string): Promise<EstudianteResponse[]> {
    const response = await api.get<EstudianteResponse[]>('/estudiantes/buscar', {
      params: { término }
    })
    return response.data
  },

  async obtenerEstudiante(id: number): Promise<EstudianteResponse> {
    const response = await api.get<EstudianteResponse>(`/estudiantes/${id}`)
    return response.data
  },

  async crearEstudiante(data: CreateEstudianteRequest): Promise<EstudianteResponse> {
    const response = await api.post<EstudianteResponse>('/estudiantes', data)
    return response.data
  },

  async actualizarEstudiante(id: number, data: UpdateEstudianteRequest): Promise<EstudianteResponse> {
    const response = await api.put<EstudianteResponse>(`/estudiantes/${id}`, data)
    return response.data
  },

  async eliminarEstudiante(id: number): Promise<void> {
    await api.delete(`/estudiantes/${id}`)
  },

  async obtenerProgreso(id: number): Promise<ProgresoAcademico> {
    const response = await api.get<ProgresoAcademico>(`/estudiantes/${id}/progreso`)
    return response.data
  }
}

export default estudiantesService
