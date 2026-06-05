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
  /** Observaciones adicionales sobre el estudiante. */
  observaciones?: string
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

export interface DocumentoResponse {
  id: number
  tipo: string
  urlArchivo: string
  fechaSubida: string
  mimeType?: string
  tamanoBytes?: number
}

export interface ContactoEmergenciaResponse {
  id: number
  nombre: string
  telefono: string
  parentesco?: string
  esPrincipal: boolean
}

export interface EstudianteDetailResponse {
  id: number
  email: string
  nombre: string
  apellido: string
  cedula: string
  fechaNacimiento: string
  genero: string
  telefono: string
  direccion: string
  estado: string
  situacionPago: string
  fechaMatricula: string | null
  tipoSangre?: string
  tipoCursoId: number | null
  categoriaLicenciaId: number | null
  usuarioId: number | null
  observaciones?: string
  documentos: DocumentoResponse[]
  contactosEmergencia: ContactoEmergenciaResponse[]
  createdAt: string
  updatedAt: string
}

export interface ProgresoAcademico {
  horasCompletadas: number
  horasRequeridas: number
  porcentajeComplecion: number
  asignacionesCompletadas: number
  asignacionesPendientes: number
}

export interface ProgresoAcademicoHorasResponse {
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

  async obtenerEstudiante(id: number): Promise<EstudianteDetailResponse> {
    const response = await api.get<EstudianteDetailResponse>(`/estudiantes/${id}`)
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

  async cambiarEstadoEstudiante(id: number, estado: string, motivo?: string): Promise<EstudianteResponse> {
    const response = await api.patch<EstudianteResponse>(`/estudiantes/${id}/estado`, { estado, motivo })
    return response.data
  },

  async obtenerProgreso(id: number): Promise<ProgresoAcademico> {
    const response = await api.get<ProgresoAcademico>(`/estudiantes/${id}/progreso`)
    return response.data
  },

  async obtenerProgresoHoras(id: number): Promise<ProgresoAcademicoHorasResponse> {
    const response = await api.get<ProgresoAcademicoHorasResponse>(`/estudiantes/${id}/progreso/horas`)
    return response.data
  },

  // Documentos
  async listarDocumentos(estudianteId: number): Promise<DocumentoResponse[]> {
    const response = await api.get<DocumentoResponse[]>(`/estudiantes/${estudianteId}/documentos`)
    return response.data
  },

  async subirDocumento(estudianteId: number, tipo: string, archivo: File): Promise<DocumentoResponse> {
    const formData = new FormData()
    formData.append('tipo', tipo)
    formData.append('archivo', archivo)
    const response = await api.post<DocumentoResponse>(
      `/estudiantes/${estudianteId}/documentos/upload`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    )
    return response.data
  },

  async descargarDocumento(estudianteId: number, documentoId: number): Promise<{ blob: Blob; filename: string }> {
    const response = await api.get(
      `/estudiantes/${estudianteId}/documentos/${documentoId}/descargar`,
      { responseType: 'blob' }
    )
    const disposition = response.headers['content-disposition'] || ''
    let filename = `documento-${documentoId}`
    const match = disposition.match(/filename\*?=(?:UTF-8'')?["']?([^"';]+)["']?/i)
    if (match) {
      try { filename = decodeURIComponent(match[1]) } catch { filename = match[1] }
    }
    return { blob: response.data, filename }
  },

  async eliminarDocumento(estudianteId: number, documentoId: number): Promise<void> {
    await api.delete(`/estudiantes/${estudianteId}/documentos/${documentoId}`)
  },

  // Contactos de emergencia
  async listarContactosEmergencia(estudianteId: number): Promise<ContactoEmergenciaResponse[]> {
    const response = await api.get<ContactoEmergenciaResponse[]>(`/estudiantes/${estudianteId}/contactos-emergencia`)
    return response.data
  },

  async crearContactoEmergencia(estudianteId: number, data: { nombre: string; telefono: string; parentesco?: string; esPrincipal?: boolean }): Promise<ContactoEmergenciaResponse> {
    const response = await api.post<ContactoEmergenciaResponse>(`/estudiantes/${estudianteId}/contactos-emergencia`, data)
    return response.data
  },

  async actualizarContactoEmergencia(estudianteId: number, contactoId: number, data: { nombre: string; telefono: string; parentesco?: string; esPrincipal?: boolean }): Promise<ContactoEmergenciaResponse> {
    const response = await api.put<ContactoEmergenciaResponse>(`/estudiantes/${estudianteId}/contactos-emergencia/${contactoId}`, data)
    return response.data
  },

  async eliminarContactoEmergencia(estudianteId: number, contactoId: number): Promise<void> {
    await api.delete(`/estudiantes/${estudianteId}/contactos-emergencia/${contactoId}`)
  }
}

export default estudiantesService
