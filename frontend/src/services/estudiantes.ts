import api from './api'

export interface CreateEstudianteRequest {
  email: string
  nombre: string
  apellido: string
  cedula: string
  fechaNacimiento: string
  genero: string
  telefono: string
  direccion: string
  tipoSangre?: string
  /** ID del tipo de curso contratado (de auth_schema.tipos_curso). */
  tipoCursoId?: number | null
  /** ID de la categoría de licencia. Se deduce del tipoCurso si se omite. */
  categoriaLicenciaId?: number | null
}

export interface UpdateEstudianteRequest extends Partial<CreateEstudianteRequest> {}

export interface EstudianteResponse {
  id: number
  email: string
  nombre?: string
  apellido?: string
  nombreCompleto?: string
  cedula: string
  fechaNacimiento?: string
  genero?: string
  telefono?: string
  direccion?: string
  estado: string
  situacionPago?: string
  fechaMatricula?: string | null
  tipoSangre?: string
  tipoCursoId?: number | null
  categoriaLicenciaId?: number | null
}

export interface ProgresoAcademico {
  horasCompletadas: number
  horasRequeridas: number
  porcentajeComplecion: number
  asignacionesCompletadas: number
  asignacionesPendientes: number
}

const estudiantesService = {
  async obtenerEstudiantes(page = 0, size = 10): Promise<{ content: EstudianteResponse[]; totalElements: number }> {
    const response = await api.get('/estudiantes', { params: { page, size } })
    return response.data
  },

  async buscarEstudiantes(termino: string): Promise<EstudianteResponse[]> {
    const response = await api.get('/estudiantes', { params: { search: termino } })
    return response.data?.content ?? response.data
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
