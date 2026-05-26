import api from './api'

// ============================================================================
// Tipos alineados con los DTOs reales del backend (ms-asignaciones).
// Eliminados:
//   - obtenerAsignacionesPorFecha (endpoint /fecha no existe)
//   - verificarDisponibilidad (endpoint no existe; las validaciones ocurren al crear)
//   - reprogramarAsignacion POST (era PUT; usar el método correcto)
// Tipos unificados al backend:
//   - tipoClase: TEORICA / PRACTICA / EXAMEN  (no PRACTICA_CALLE/PATIO)
//   - estado: 6 valores reales del enum
// ============================================================================

export type EstadoAsignacion =
  | 'PROGRAMADA'
  | 'CONFIRMADA'
  | 'EN_CURSO'
  | 'COMPLETADA'
  | 'CANCELADA'
  | 'NO_ASISTIO'

export type TipoClase = 'TEORICA' | 'PRACTICA' | 'EXAMEN'

export interface CreateAsignacionRequest {
  estudianteId: number
  instructorId: number
  vehiculoId: number
  fecha: string         // YYYY-MM-DD (campo real del backend)
  horaInicio: string    // HH:mm:ss
  horaFin: string       // HH:mm:ss
  observaciones?: string
  // tipoClase NO se envía en el create actual; backend hace default a PRACTICA.
}

export interface UpdateAsignacionRequest {
  estudianteId?: number
  instructorId?: number
  vehiculoId?: number
  fecha?: string
  horaInicio?: string
  horaFin?: string
  estado?: EstadoAsignacion
  observaciones?: string
}

export interface ReprogramarRequest {
  fecha: string
  horaInicio: string
  horaFin: string
  motivoCancelacion?: string
}

/**
 * Forma real que devuelve el backend.
 * NOTA: el backend devuelve `fechaHora` (LocalDateTime); el mapper descompone
 * a `fecha`/`horaInicio`/`horaFin`. Algunos endpoints devuelven uno u otro
 * — usamos los dos para máxima compatibilidad.
 */
export interface AsignacionResponse {
  id: number
  estudianteId: number
  instructorId: number
  vehiculoId: number
  fechaHora?: string             // ISO LocalDateTime, principal
  fecha?: string | null          // descompuesto (puede venir null)
  horaInicio?: string | null
  horaFin?: string | null
  duracionMinutos?: number
  estado: EstadoAsignacion
  tipoClase?: TipoClase
  observaciones?: string | null
  // Kilometraje (poblado al iniciar/finalizar):
  kmInicial?: number | null
  kmFinal?: number | null
  horaInicioReal?: string | null
  horaFinReal?: string | null
  observacionesRecorrido?: string | null
}

// --- Kilometraje (iniciar/finalizar clase) ---
export interface IniciarAsignacionRequest {
  kmInicial?: number | null   // si null, backend toma el km actual del vehículo
  observaciones?: string
}

export interface FinalizarAsignacionRequest {
  kmFinal: number             // obligatorio
  observacionesRecorrido?: string
}

export interface RecorridoResponse {
  asignacionId: number
  vehiculoId: number
  estado: EstadoAsignacion
  kmInicial: number | null
  kmFinal: number | null
  kmRecorridos: number | null
  horaInicioReal: string | null
  horaFinReal: string | null
  duracionRealMinutos: number | null
  observacionesRecorrido: string | null
  syncVehiculoExitoso: boolean | null
  mensajeSyncVehiculo: string | null
  syncEstudianteExitoso: boolean | null
  mensajeSyncEstudiante: string | null
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
  estado: EstadoAsignacion
  dateCreated?: string
}

const asignacionesService = {
  // -------- CRUD --------
  async obtenerAsignaciones(page: number = 0, size: number = 50): Promise<{ content: AsignacionResponse[]; totalElements: number }> {
    const r = await api.get<{ content: AsignacionResponse[]; totalElements: number }>('/asignaciones', {
      params: { page, size }
    })
    return r.data
  },

  async obtenerAsignacion(id: number): Promise<AsignacionResponse> {
    const r = await api.get<AsignacionResponse>(`/asignaciones/${id}`)
    return r.data
  },

  async crearAsignacion(data: CreateAsignacionRequest): Promise<AsignacionResponse> {
    const r = await api.post<AsignacionResponse>('/asignaciones', data)
    return r.data
  },

  async actualizarAsignacion(id: number, data: UpdateAsignacionRequest): Promise<AsignacionResponse> {
    const r = await api.put<AsignacionResponse>(`/asignaciones/${id}`, data)
    return r.data
  },

  async eliminarAsignacion(id: number): Promise<void> {
    await api.delete(`/asignaciones/${id}`)
  },

  // -------- Reprogramar (PUT, no POST) --------
  async reprogramarAsignacion(id: number, data: ReprogramarRequest): Promise<AsignacionResponse> {
    const r = await api.put<AsignacionResponse>(`/asignaciones/${id}/reprogramar`, data)
    return r.data
  },

  // -------- Historial por estudiante --------
  async obtenerHistorial(estudianteId: number): Promise<HistorialAsignacionItem[]> {
    const r = await api.get<{ content: HistorialAsignacionItem[] }>(
      `/asignaciones/estudiante/${estudianteId}`,
      { params: { size: 100, sort: 'fechaHora,desc' } }
    )
    return r.data?.content ?? []
  },

  // -------- Kilometraje --------
  async iniciarAsignacion(id: number, data: IniciarAsignacionRequest): Promise<RecorridoResponse> {
    const r = await api.patch<RecorridoResponse>(`/asignaciones/${id}/iniciar`, data)
    return r.data
  },

  async finalizarAsignacion(id: number, data: FinalizarAsignacionRequest): Promise<RecorridoResponse> {
    const r = await api.patch<RecorridoResponse>(`/asignaciones/${id}/finalizar`, data)
    return r.data
  },

  async obtenerRecorrido(id: number): Promise<RecorridoResponse> {
    const r = await api.get<RecorridoResponse>(`/asignaciones/${id}/recorrido`)
    return r.data
  }
}

export default asignacionesService
