import api from './api'

export interface EstadoCuentaResponse {
  estudianteId: number
  nombreEstudiante: string
  totalFacturado: number
  totalPagado: number
  saldoPendiente: number
  estadoGeneral: 'SALDADO' | 'PENDIENTE' | 'PAGO_EXCESIVO'
  facturas: FacturaResponse[]
  pagos: PagoResponse[]
}

export interface CreateFacturaRequest {
  estudianteId: number
  descripción: string
  monto: number
  fechaVencimiento: string
}

export interface UpdateFacturaRequest extends CreateFacturaRequest {
  id: number
}

export interface FacturaResponse {
  id: number
  estudianteId: number
  descripción: string
  monto: number
  fechaEmisión: string
  fechaVencimiento: string
  estado: 'PAGADA' | 'PENDIENTE' | 'PARCIALMENTE_PAGADA'
  montoPagado: number
  montoRestante: number
}

export interface CreatePagoRequest {
  facturaId: number
  monto: number
  fecha: string
  metodoPago: 'EFECTIVO' | 'TRANSFERENCIA' | 'CHEQUE' | 'TARJETA'
  referencia?: string
}

export interface PagoResponse {
  id: number
  facturaId: number
  monto: number
  fecha: string
  metodoPago: string
  referencia: string | null
  estado: 'REGISTRADO' | 'VERIFICADO'
}

const cobrosService = {
  async obtenerEstadoCuenta(estudianteId: number): Promise<EstadoCuentaResponse> {
    const response = await api.get<EstadoCuentaResponse>(`/facturas/estudiante/${estudianteId}`)
    return response.data
  },

  async obtenerFacturas(page: number = 0, size: number = 10): Promise<{ content: FacturaResponse[]; totalElements: number }> {
    const response = await api.get<{ content: FacturaResponse[]; totalElements: number }>('/facturas', {
      params: { page, size }
    })
    return response.data
  },

  async obtenerFactura(id: number): Promise<FacturaResponse> {
    const response = await api.get<FacturaResponse>(`/facturas/${id}`)
    return response.data
  },

  async crearFactura(data: CreateFacturaRequest): Promise<FacturaResponse> {
    const response = await api.post<FacturaResponse>('/facturas', data)
    return response.data
  },

  async actualizarFactura(id: number, data: UpdateFacturaRequest): Promise<FacturaResponse> {
    const response = await api.put<FacturaResponse>(`/facturas/${id}`, data)
    return response.data
  },

  async eliminarFactura(id: number): Promise<void> {
    await api.delete(`/facturas/${id}`)
  },

  async obtenerPagos(page: number = 0, size: number = 10): Promise<{ content: PagoResponse[]; totalElements: number }> {
    const response = await api.get<{ content: PagoResponse[]; totalElements: number }>('/pagos', {
      params: { page, size }
    })
    return response.data
  },

  async obtenerPago(id: number): Promise<PagoResponse> {
    const response = await api.get<PagoResponse>(`/pagos/${id}`)
    return response.data
  },

  async crearPago(data: CreatePagoRequest): Promise<PagoResponse> {
    const response = await api.post<PagoResponse>('/pagos', data)
    return response.data
  },

  async obtenerPagosPorFactura(facturaId: number): Promise<PagoResponse[]> {
    const response = await api.get<PagoResponse[]>(`/facturas/${facturaId}/pagos`)
    return response.data
  },

  async obtenerPagosPorEstudiante(estudianteId: number): Promise<HistorialPagoItem[]> {
    const response = await api.get<{ content: HistorialPagoItem[] }>(
      `/pagos/estudiante/${estudianteId}`,
      { params: { size: 100, sort: 'fechaPago,desc' } }
    )
    return response.data?.content ?? []
  },

  async generarReporteFiscal(fechaInicio: string, fechaFin: string): Promise<Blob> {
    const response = await api.get(`/cobros/reportes/fiscal`, {
      params: { fechaInicio, fechaFin },
      responseType: 'blob'
    })
    return response.data
  }
}

/**
 * Forma cruda del backend (PagoListResponse) — sin estado calculado.
 */
export interface HistorialPagoItem {
  id: number
  facturaId: number
  monto: number
  fechaPago: string
  metodoPago: string
  referenciaTransaccion: string | null
}

export default cobrosService
