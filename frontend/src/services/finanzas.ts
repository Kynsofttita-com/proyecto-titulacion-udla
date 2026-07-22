import api from './api'

// ============================================================================
// Tipos alineados con DTOs de ms-cobros (modulo contabilidad)
// ============================================================================

export type TipoCuenta = 'EFECTIVO' | 'BANCO' | 'TARJETA'
export type TipoMovimiento = 'INGRESO' | 'GASTO'

// ---------- Cuentas ----------
export interface CuentaRequest {
  nombre: string
  tipo: TipoCuenta
  numeroCuenta?: string        // requerido si BANCO/TARJETA
  saldoInicial: number
  activo?: boolean
  observaciones?: string
}

export interface CuentaResponse {
  id: number
  nombre: string
  tipo: TipoCuenta
  numeroCuenta?: string
  saldoInicial: number
  saldoActual: number
  activo: boolean
  observaciones?: string
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

// ---------- Categorías de movimiento ----------
export interface CategoriaMovimientoRequest {
  codigo: string
  nombre: string
  tipo: TipoMovimiento
  activo?: boolean
}

export interface CategoriaMovimientoResponse {
  id: number
  codigo: string
  nombre: string
  tipo: TipoMovimiento
  esSistema: boolean
  activo: boolean
  createdAt?: string
  updatedAt?: string
}

// ---------- Movimientos contables ----------
export interface MovimientoContableRequest {
  fecha: string                // YYYY-MM-DD
  tipo: TipoMovimiento
  monto: number
  cuentaId: number
  categoriaId: number
  descripcion?: string
  referencia?: string
  // Vínculo opcional a vehículo (para gastos manuales de combustible/mantenimiento)
  vehiculoId?: number | null
  placaVehiculo?: string | null
  kilometraje?: number | null
  // Vínculo opcional a persona a la que se paga (categorías de sueldo)
  pagadoAId?: number | null
  nombrePagadoA?: string | null
}

export interface MovimientoContableResponse {
  id: number
  fecha: string
  tipo: TipoMovimiento
  monto: number
  cuentaId: number
  cuentaNombre: string
  categoriaId: number
  categoriaCodigo: string
  categoriaNombre: string
  descripcion?: string
  referencia?: string
  pagoId?: number                   // no-NULL = auto-generado desde un pago
  registroCombustibleId?: number    // no-NULL = auto-generado desde combustible en Vehículos
  mantenimientoId?: number          // no-NULL = auto-generado desde mantenimiento en Vehículos
  vehiculoId?: number
  placaVehiculo?: string
  kilometraje?: number
  pagadoAId?: number                // no-NULL = sueldo pagado a instructor/staff
  nombrePagadoA?: string
  anulado: boolean
  motivoAnulacion?: string
  createdAt?: string
  createdBy?: string
}

export interface AnularMovimientoRequest {
  motivo: string
}

export interface MovimientoFiltros {
  cuentaId?: number
  categoriaId?: number
  tipo?: TipoMovimiento
  fechaInicio?: string
  fechaFin?: string
  page?: number
  size?: number
}

interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

// ============================================================================
// Service
// ============================================================================

const finanzasService = {
  // -------- Cuentas --------
  async listarCuentas(soloActivas = false): Promise<CuentaResponse[]> {
    const r = await api.get<CuentaResponse[]>('/cuentas', { params: { soloActivas } })
    return r.data
  },

  async obtenerCuenta(id: number): Promise<CuentaResponse> {
    const r = await api.get<CuentaResponse>(`/cuentas/${id}`)
    return r.data
  },

  async crearCuenta(data: CuentaRequest): Promise<CuentaResponse> {
    const r = await api.post<CuentaResponse>('/cuentas', data)
    return r.data
  },

  async actualizarCuenta(id: number, data: CuentaRequest): Promise<CuentaResponse> {
    const r = await api.put<CuentaResponse>(`/cuentas/${id}`, data)
    return r.data
  },

  async desactivarCuenta(id: number): Promise<void> {
    await api.delete(`/cuentas/${id}`)
  },

  // -------- Categorías --------
  async listarCategorias(tipo?: TipoMovimiento, soloActivas = true): Promise<CategoriaMovimientoResponse[]> {
    const r = await api.get<CategoriaMovimientoResponse[]>('/categorias-movimiento', {
      params: { tipo, soloActivas }
    })
    return r.data
  },

  async crearCategoria(data: CategoriaMovimientoRequest): Promise<CategoriaMovimientoResponse> {
    const r = await api.post<CategoriaMovimientoResponse>('/categorias-movimiento', data)
    return r.data
  },

  async actualizarCategoria(id: number, data: CategoriaMovimientoRequest): Promise<CategoriaMovimientoResponse> {
    const r = await api.put<CategoriaMovimientoResponse>(`/categorias-movimiento/${id}`, data)
    return r.data
  },

  async desactivarCategoria(id: number): Promise<void> {
    await api.delete(`/categorias-movimiento/${id}`)
  },

  // -------- Movimientos --------
  async buscarMovimientos(filtros: MovimientoFiltros = {}): Promise<SpringPage<MovimientoContableResponse>> {
    const r = await api.get<SpringPage<MovimientoContableResponse>>('/movimientos', { params: filtros })
    return r.data
  },

  async obtenerMovimiento(id: number): Promise<MovimientoContableResponse> {
    const r = await api.get<MovimientoContableResponse>(`/movimientos/${id}`)
    return r.data
  },

  async crearMovimiento(data: MovimientoContableRequest): Promise<MovimientoContableResponse> {
    const r = await api.post<MovimientoContableResponse>('/movimientos', data)
    return r.data
  },

  async actualizarMovimiento(id: number, data: MovimientoContableRequest): Promise<MovimientoContableResponse> {
    const r = await api.put<MovimientoContableResponse>(`/movimientos/${id}`, data)
    return r.data
  },

  async anularMovimiento(id: number, motivo: string): Promise<void> {
    await api.post(`/movimientos/${id}/anular`, { motivo })
  }
}

export default finanzasService
