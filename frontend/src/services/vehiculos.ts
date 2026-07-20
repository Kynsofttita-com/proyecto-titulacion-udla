import api from './api'

// ============================================================================
// Tipos alineados con DTOs reales del backend (ms-vehiculos)
// ============================================================================

export type EstadoVehiculo = 'ACTIVO' | 'MANTENIMIENTO' | 'FUERA_SERVICIO'

export interface CreateVehiculoRequest {
  placa: string                  // ABC-1234 o AB-1234A
  marca: string
  modelo: string
  anio: number
  vin?: string                   // 17 chars
  color?: string
  kilometraje?: number
  estado?: EstadoVehiculo
  soatVencimiento?: string       // YYYY-MM-DD
  revisionVencimiento?: string
  fechaCompra?: string
  valorCompra?: number
  categoriaLicenciaId?: number
  tipoCombustibleId?: number
  numeroMotor?: string
  numeroChasis?: string
  capacidadPasajeros?: number
  observaciones?: string
}

export interface UpdateVehiculoRequest {
  marca?: string
  modelo?: string
  anio?: number
  color?: string
  kilometraje?: number
  estado?: EstadoVehiculo
  soatVencimiento?: string
  revisionVencimiento?: string
  fechaCompra?: string
  valorCompra?: number
  categoriaLicenciaId?: number
  tipoCombustibleId?: number
  numeroMotor?: string
  numeroChasis?: string
  capacidadPasajeros?: number
  observaciones?: string
}

export interface VehiculoResponse {
  id: number
  placa: string
  marca: string
  modelo: string
  anio: number
  vin?: string
  color?: string
  kilometraje: number
  estado: EstadoVehiculo
  soatVencimiento?: string
  revisionVencimiento?: string
  fechaCompra?: string
  valorCompra?: number
  categoriaLicenciaId?: number
  tipoCombustibleId?: number
  numeroMotor?: string
  numeroChasis?: string
  capacidadPasajeros?: number
  observaciones?: string
  createdAt?: string
  updatedAt?: string
}

export interface VehiculoListResponse {
  id: number
  placa: string
  marca: string
  modelo: string
  anio: number
  color?: string
  kilometraje: number
  estado: EstadoVehiculo
  soatVencimiento?: string
  revisionVencimiento?: string
  categoriaLicenciaId?: number
  tipoCombustibleId?: number
}

// --- Tipos combustible ---
export type UnidadCombustible = 'GALON' | 'LITRO' | 'KWH'

export interface TipoCombustibleResponse {
  id: number
  codigo: string
  nombre: string
  unidad: UnidadCombustible
  precioActual: number
  activo: boolean
  observaciones?: string
  updatedAt?: string
  updatedBy?: string
}

export interface ActualizarPrecioRequest {
  precioActual: number
  activo?: boolean
  observaciones?: string
}

export interface CrearTipoCombustibleRequest {
  codigo: string             // MAYUSCULAS (EXTRA, GLP_AUTO)
  nombre: string
  unidad: UnidadCombustible
  precioActual: number
  observaciones?: string
}

// --- Categorias de licencia (cross-schema via ms-auth) ---
export interface CategoriaLicenciaResponse {
  id: number
  codigo: string
  descripcion: string
  activa: boolean
}

// --- Mantenimientos ---
export type TipoMantenimiento = 'PREVENTIVO' | 'CORRECTIVO'

export interface MantenimientoRequest {
  tipo: TipoMantenimiento
  fecha: string                // YYYY-MM-DD
  costo: number
  descripcion: string
  taller?: string
  kilometrajeServicio?: number
  proximaFecha?: string
}

export interface MantenimientoResponse {
  id: number
  vehiculoId: number
  tipo: TipoMantenimiento
  fecha: string
  costo: number
  descripcion: string
  taller?: string
  kilometrajeServicio?: number
  proximaFecha?: string
}

// --- Inspecciones ---
export type TipoInspeccion = 'TECNICA' | 'SOAT' | 'INTERNA'
export type ResultadoInspeccion = 'APROBADA' | 'REPROBADA' | 'CONDICIONADA'

export interface InspeccionRequest {
  tipo: TipoInspeccion
  fecha: string
  resultado: ResultadoInspeccion
  archivoUrl?: string
  observaciones?: string
  proximaInspeccion?: string
}

export interface InspeccionResponse {
  id: number
  vehiculoId: number
  tipo: TipoInspeccion
  fecha: string
  resultado: ResultadoInspeccion
  archivoUrl?: string
  observaciones?: string
  proximaInspeccion?: string
}

// --- Combustible ---
export interface CombustibleRequest {
  fecha: string                       // ISO date-time YYYY-MM-DDTHH:mm:ss
  litros: number
  costoTotal?: number                 // opcional: si no se envía, se autocalcula
  tipoCombustibleIdOverride?: number  // opcional: si se llenó con otro tipo
  kilometrajeActual: number
  estacion?: string
}

export interface CombustibleResponse {
  id: number
  vehiculoId: number
  fecha: string
  litros: number
  costoTotal: number
  costoPorLitro: number
  kilometrajeActual: number
  estacion?: string
}

// --- Documentos ---
export interface DocumentoVehiculoRequest {
  tipo: string
  numero?: string
  urlArchivo: string
  fechaEmision?: string
  fechaVencimiento?: string
}

export interface DocumentoVehiculoResponse {
  id: number
  vehiculoId: number
  tipo: string
  numero?: string
  urlArchivo: string
  fechaEmision?: string
  fechaVencimiento?: string
}

// --- Alertas SOAT ---
export interface AlertaSoatResponse {
  vehiculoId: number
  placa: string
  marca: string
  modelo: string
  soatVencimiento: string
  diasParaVencer: number
  vencido: boolean
}

// ============================================================================
// Servicio
// ============================================================================

const vehiculosService = {
  // -------- Vehiculos (CRUD) --------
  async obtenerVehiculos(
    page: number = 0,
    size: number = 50,
    search?: string,
    estado?: string
  ): Promise<{ content: VehiculoListResponse[]; totalElements: number }> {
    const r = await api.get<{ content: VehiculoListResponse[]; totalElements: number }>('/vehiculos', {
      params: { page, size, search, estado }
    })
    return r.data
  },

  async obtenerVehiculo(id: number): Promise<VehiculoResponse> {
    const r = await api.get<VehiculoResponse>(`/vehiculos/${id}`)
    return r.data
  },

  async crearVehiculo(data: CreateVehiculoRequest): Promise<VehiculoResponse> {
    const r = await api.post<VehiculoResponse>('/vehiculos', data)
    return r.data
  },

  async actualizarVehiculo(id: number, data: UpdateVehiculoRequest): Promise<VehiculoResponse> {
    const r = await api.put<VehiculoResponse>(`/vehiculos/${id}`, data)
    return r.data
  },

  async eliminarVehiculo(id: number): Promise<void> {
    await api.delete(`/vehiculos/${id}`)
  },

  // -------- Tipos de combustible --------
  async listarTiposCombustible(soloActivos: boolean = false): Promise<TipoCombustibleResponse[]> {
    const r = await api.get<TipoCombustibleResponse[]>('/tipos-combustible', {
      params: { soloActivos }
    })
    return r.data
  },

  async actualizarPrecioCombustible(id: number, data: ActualizarPrecioRequest): Promise<TipoCombustibleResponse> {
    const r = await api.put<TipoCombustibleResponse>(`/tipos-combustible/${id}/precio`, data)
    return r.data
  },

  async crearTipoCombustible(data: CrearTipoCombustibleRequest): Promise<TipoCombustibleResponse> {
    const r = await api.post<TipoCombustibleResponse>('/tipos-combustible', data)
    return r.data
  },

  async desactivarTipoCombustible(id: number): Promise<void> {
    await api.delete(`/tipos-combustible/${id}`)
  },

  // -------- Categorias licencia (vive en ms-auth) --------
  async listarCategoriasLicencia(soloActivas: boolean = true): Promise<CategoriaLicenciaResponse[]> {
    const r = await api.get<CategoriaLicenciaResponse[] | { content: CategoriaLicenciaResponse[] }>(
      '/categorias-licencia',
      { params: { soloActivas } }
    )
    // ms-auth devuelve Page<>, ms-vehiculos devolvía List<>; aceptamos ambas
    const data = r.data as any
    return Array.isArray(data) ? data : (data.content || [])
  },

  // -------- Mantenimientos --------
  async listarMantenimientos(vehiculoId: number): Promise<MantenimientoResponse[]> {
    const r = await api.get<MantenimientoResponse[]>(`/vehiculos/${vehiculoId}/mantenimientos`)
    return r.data
  },

  async registrarMantenimiento(vehiculoId: number, data: MantenimientoRequest): Promise<MantenimientoResponse> {
    const r = await api.post<MantenimientoResponse>(`/vehiculos/${vehiculoId}/mantenimientos`, data)
    return r.data
  },

  async actualizarMantenimiento(vehiculoId: number, mantId: number, data: MantenimientoRequest): Promise<MantenimientoResponse> {
    const r = await api.put<MantenimientoResponse>(`/vehiculos/${vehiculoId}/mantenimientos/${mantId}`, data)
    return r.data
  },

  async eliminarMantenimiento(vehiculoId: number, mantId: number): Promise<void> {
    await api.delete(`/vehiculos/${vehiculoId}/mantenimientos/${mantId}`)
  },

  // -------- Inspecciones --------
  async listarInspecciones(vehiculoId: number): Promise<InspeccionResponse[]> {
    const r = await api.get<InspeccionResponse[]>(`/vehiculos/${vehiculoId}/inspecciones`)
    return r.data
  },

  async registrarInspeccion(vehiculoId: number, data: InspeccionRequest): Promise<InspeccionResponse> {
    const r = await api.post<InspeccionResponse>(`/vehiculos/${vehiculoId}/inspecciones`, data)
    return r.data
  },

  async actualizarInspeccion(vehiculoId: number, inspeccionId: number, data: InspeccionRequest): Promise<InspeccionResponse> {
    const r = await api.put<InspeccionResponse>(`/vehiculos/${vehiculoId}/inspecciones/${inspeccionId}`, data)
    return r.data
  },

  async eliminarInspeccion(vehiculoId: number, inspeccionId: number): Promise<void> {
    await api.delete(`/vehiculos/${vehiculoId}/inspecciones/${inspeccionId}`)
  },

  // -------- Combustible --------
  async listarCombustible(vehiculoId: number, page: number = 0, size: number = 50): Promise<{ content: CombustibleResponse[]; totalElements: number }> {
    const r = await api.get<{ content: CombustibleResponse[]; totalElements: number }>(
      `/vehiculos/${vehiculoId}/combustible`,
      { params: { page, size } }
    )
    return r.data
  },

  async registrarCombustible(vehiculoId: number, data: CombustibleRequest): Promise<CombustibleResponse> {
    const r = await api.post<CombustibleResponse>(`/vehiculos/${vehiculoId}/combustible`, data)
    return r.data
  },

  async eliminarCombustible(vehiculoId: number, registroId: number): Promise<void> {
    await api.delete(`/vehiculos/${vehiculoId}/combustible/${registroId}`)
  },

  // -------- Documentos --------
  async listarDocumentos(vehiculoId: number): Promise<DocumentoVehiculoResponse[]> {
    const r = await api.get<DocumentoVehiculoResponse[]>(`/vehiculos/${vehiculoId}/documentos`)
    return r.data
  },

  async crearDocumento(vehiculoId: number, data: DocumentoVehiculoRequest): Promise<DocumentoVehiculoResponse> {
    const r = await api.post<DocumentoVehiculoResponse>(`/vehiculos/${vehiculoId}/documentos`, data)
    return r.data
  },

  async eliminarDocumento(vehiculoId: number, docId: number): Promise<void> {
    await api.delete(`/vehiculos/${vehiculoId}/documentos/${docId}`)
  },

  // -------- Alertas SOAT --------
  async alertasSoat(dias: number = 30): Promise<AlertaSoatResponse[]> {
    const r = await api.get<AlertaSoatResponse[]>('/vehiculos/alertas-soat', {
      params: { dias }
    })
    return r.data
  }
}

export default vehiculosService
