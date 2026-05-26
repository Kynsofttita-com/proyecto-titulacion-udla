import api from './api'

// ============================================================================
// Tipos alineados con DTOs reales del backend (ms-instructores)
// ============================================================================

export type TipoContrato = 'TIEMPO_COMPLETO' | 'MEDIO_TIEMPO' | 'POR_HORAS'

export interface CreateInstructorRequest {
  cedula: string
  nombre: string
  apellido: string
  email: string
  telefono: string
  direccion?: string
  fechaNacimiento?: string // YYYY-MM-DD
  licenciaNumero: string
  licenciaCategoria: string
  licenciaEmision: string  // YYYY-MM-DD
  licenciaCaducidad: string // YYYY-MM-DD
  fechaContratacion?: string // YYYY-MM-DD
  salarioMensual?: number
  tipoContrato: TipoContrato
  horasContratoSemanales: number
  tarifaHora?: number
  observaciones?: string
}

export interface UpdateInstructorRequest {
  nombre?: string
  apellido?: string
  email?: string
  telefono?: string
  direccion?: string
  fechaNacimiento?: string
  licenciaNumero?: string
  licenciaCategoria?: string
  licenciaEmision?: string
  licenciaCaducidad?: string
  estado?: 'ACTIVO' | 'INACTIVO' | 'SUSPENDIDO'
  fechaContratacion?: string
  salarioMensual?: number
  tipoContrato?: TipoContrato
  horasContratoSemanales?: number
  tarifaHora?: number
  observaciones?: string
}

export interface InstructorListResponse {
  id: number
  cedula: string
  nombre: string
  apellido: string
  email: string
  telefono?: string
  licenciaNumero?: string
  licenciaCategoria: string
  licenciaCaducidad: string
  estado: 'ACTIVO' | 'INACTIVO' | 'SUSPENDIDO'
  tipoContrato?: TipoContrato
}

export interface InstructorResponse {
  id: number
  cedula: string
  nombre: string
  apellido: string
  email: string
  telefono: string
  direccion?: string
  fechaNacimiento?: string
  licenciaNumero: string
  licenciaCategoria: string
  licenciaEmision: string
  licenciaCaducidad: string
  estado: 'ACTIVO' | 'INACTIVO' | 'SUSPENDIDO'
  fechaContratacion?: string
  salarioMensual?: number
  tipoContrato: TipoContrato
  horasContratoSemanales: number
  tarifaHora?: number
  usuarioId?: number
  observaciones?: string
}

export interface ResumenHorasResponse {
  instructorId: number
  desde: string
  hasta: string
  tipoContrato: TipoContrato
  horasContratoSemanales: number
  horasContratadas: number
  horasCumplidas: number
  horasRestantes: number
  porcentajeCumplimiento: number
  sueldoEstimado: number
  observacion?: string
}

export interface FranjaHoraria {
  horaInicio: string // HH:mm
  horaFin: string    // HH:mm
}

export interface DisponibilidadDelDiaResponse {
  instructorId: number
  fecha: string         // YYYY-MM-DD
  diaSemana: number     // 1=lunes ... 7=domingo (ISO)
  disponible: boolean
  motivoNoDisponible?: string
  franjas: FranjaHoraria[]
}

// --- Disponibilidad semanal recurrente ---
export interface DisponibilidadRequest {
  diaSemana: number  // 1=L .. 7=D (ISO)
  horaInicio: string // HH:mm o HH:mm:ss
  horaFin: string
}

export interface DisponibilidadResponse {
  id: number
  diaSemana: number
  horaInicio: string
  horaFin: string
}

// --- Excepciones (horarios trabajo) ---
export type TipoExcepcion = 'EXTRA' | 'AUSENCIA'

export interface HorarioTrabajoRequest {
  fecha: string         // YYYY-MM-DD
  horaInicio?: string   // HH:mm (obligatorio si tipo=EXTRA)
  horaFin?: string
  tipo: TipoExcepcion
  motivo?: string
}

export interface HorarioTrabajoResponse {
  id: number
  fecha: string
  horaInicio?: string
  horaFin?: string
  tipo: TipoExcepcion
  motivo?: string
}

// ============================================================================
// Servicio
// ============================================================================

const instructoresService = {
  async obtenerInstructores(
    page: number = 0,
    size: number = 10,
    search?: string
  ): Promise<{ content: InstructorListResponse[]; totalElements: number }> {
    const response = await api.get<{ content: InstructorListResponse[]; totalElements: number }>(
      '/instructores',
      { params: { page, size, search } }
    )
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

  async obtenerDisponibilidadDelDia(id: number, fecha: string): Promise<DisponibilidadDelDiaResponse> {
    const response = await api.get<DisponibilidadDelDiaResponse>(
      `/instructores/${id}/disponibilidad`,
      { params: { fecha } }
    )
    return response.data
  },

  async obtenerResumenHoras(id: number, desde: string, hasta: string): Promise<ResumenHorasResponse> {
    const response = await api.get<ResumenHorasResponse>(
      `/instructores/${id}/resumen-horas`,
      { params: { desde, hasta } }
    )
    return response.data
  },

  // -------- Disponibilidad semanal recurrente --------
  async listarDisponibilidadSemanal(instructorId: number): Promise<DisponibilidadResponse[]> {
    const r = await api.get<DisponibilidadResponse[]>(`/instructores/${instructorId}/disponibilidad-semanal`)
    return r.data
  },

  async agregarDisponibilidadSemanal(instructorId: number, data: DisponibilidadRequest): Promise<DisponibilidadResponse> {
    const r = await api.post<DisponibilidadResponse>(`/instructores/${instructorId}/disponibilidad-semanal`, data)
    return r.data
  },

  async actualizarDisponibilidadSemanal(instructorId: number, disponibilidadId: number, data: DisponibilidadRequest): Promise<DisponibilidadResponse> {
    const r = await api.put<DisponibilidadResponse>(`/instructores/${instructorId}/disponibilidad-semanal/${disponibilidadId}`, data)
    return r.data
  },

  async eliminarDisponibilidadSemanal(instructorId: number, disponibilidadId: number): Promise<void> {
    await api.delete(`/instructores/${instructorId}/disponibilidad-semanal/${disponibilidadId}`)
  },

  // -------- Excepciones (horarios trabajo) --------
  async listarHorariosTrabajo(instructorId: number): Promise<HorarioTrabajoResponse[]> {
    const r = await api.get<HorarioTrabajoResponse[]>(`/instructores/${instructorId}/horarios-trabajo`)
    return r.data
  },

  async agregarHorarioTrabajo(instructorId: number, data: HorarioTrabajoRequest): Promise<HorarioTrabajoResponse> {
    const r = await api.post<HorarioTrabajoResponse>(`/instructores/${instructorId}/horarios-trabajo`, data)
    return r.data
  },

  async eliminarHorarioTrabajo(instructorId: number, horarioId: number): Promise<void> {
    await api.delete(`/instructores/${instructorId}/horarios-trabajo/${horarioId}`)
  }
}

export default instructoresService
