import api from './api'

export interface CreateInstructorRequest {
  email: string
  nombreCompleto: string
  cédula: string
  especialidad: string
  licenciaConducir: string
  teléfono: string
}

export interface UpdateInstructorRequest extends CreateInstructorRequest {
  id: number
}

export interface InstructorResponse {
  id: number
  email: string
  nombreCompleto: string
  cédula: string
  especialidad: string
  licenciaConducir: string
  teléfono: string
  estado: 'ACTIVO' | 'INACTIVO'
  horasImpartidas: number
}

export interface DisponibilidadSlot {
  día: string
  horaInicio: string
  horaFin: string
  disponible: boolean
}

const instructoresService = {
  async obtenerInstructores(page: number = 0, size: number = 10): Promise<{ content: InstructorResponse[]; totalElements: number }> {
    const response = await api.get<{ content: InstructorResponse[]; totalElements: number }>('/instructores', {
      params: { page, size }
    })
    return response.data
  },

  async buscarInstructores(término: string): Promise<InstructorResponse[]> {
    const response = await api.get<InstructorResponse[]>('/instructores/buscar', {
      params: { término }
    })
    return response.data
  },

  async obtenerInstructor(id: number): Promise<InstructorResponse> {
    const response = await api.get<InstructorResponse>(`/instructores/${id}`)
    return response.data
  },

  async crearInstructor(data: CreateInstructorRequest): Promise<InstructorResponse> {
    const response = await api.post<InstructorResponse>('/instructores', data)
    return response.data
  },

  async actualizarInstructor(id: number, data: UpdateInstructorRequest): Promise<InstructorResponse> {
    const response = await api.put<InstructorResponse>(`/instructores/${id}`, data)
    return response.data
  },

  async eliminarInstructor(id: number): Promise<void> {
    await api.delete(`/instructores/${id}`)
  },

  async obtenerDisponibilidad(id: number): Promise<DisponibilidadSlot[]> {
    const response = await api.get<DisponibilidadSlot[]>(`/instructores/${id}/disponibilidad`)
    return response.data
  },

  async actualizarDisponibilidad(id: number, slots: DisponibilidadSlot[]): Promise<void> {
    await api.put(`/instructores/${id}/disponibilidad`, slots)
  }
}

export default instructoresService
