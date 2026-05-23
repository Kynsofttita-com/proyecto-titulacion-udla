import api from './api'

export interface CreateVehiculoRequest {
  placa: string
  marca: string
  modelo: string
  año: number
  color: string
  nroMotor: string
  nroChasis: string
  capacidadPasajeros: number
}

export interface UpdateVehiculoRequest extends CreateVehiculoRequest {
  id: number
}

export interface VehiculoResponse {
  id: number
  placa: string
  marca: string
  modelo: string
  año: number
  color: string
  nroMotor: string
  nroChasis: string
  capacidadPasajeros: number
  estado: 'ACTIVO' | 'MANTENIMIENTO' | 'ELIMINADO'
  kilometraje: number
  fechaUltimaRevision: string
  proxima Revision: string
}

export interface MantenimientoRegistro {
  id: number
  tipo: 'PREVENTIVO' | 'CORRECTIVO'
  descripción: string
  fecha: string
  costo: number
  estado: 'COMPLETADO' | 'PENDIENTE'
}

export interface RevisionTecnica {
  id: number
  fechaRevision: string
  fechaVencimiento: string
  estado: 'VIGENTE' | 'VENCIDO'
}

const vehiculosService = {
  async obtenerVehiculos(page: number = 0, size: number = 10): Promise<{ content: VehiculoResponse[]; totalElements: number }> {
    const response = await api.get<{ content: VehiculoResponse[]; totalElements: number }>('/vehiculos', {
      params: { page, size }
    })
    return response.data
  },

  async buscarVehiculos(término: string): Promise<VehiculoResponse[]> {
    const response = await api.get<VehiculoResponse[]>('/vehiculos/buscar', {
      params: { término }
    })
    return response.data
  },

  async obtenerVehiculo(id: number): Promise<VehiculoResponse> {
    const response = await api.get<VehiculoResponse>(`/vehiculos/${id}`)
    return response.data
  },

  async crearVehiculo(data: CreateVehiculoRequest): Promise<VehiculoResponse> {
    const response = await api.post<VehiculoResponse>('/vehiculos', data)
    return response.data
  },

  async actualizarVehiculo(id: number, data: UpdateVehiculoRequest): Promise<VehiculoResponse> {
    const response = await api.put<VehiculoResponse>(`/vehiculos/${id}`, data)
    return response.data
  },

  async eliminarVehiculo(id: number): Promise<void> {
    await api.delete(`/vehiculos/${id}`)
  },

  async obtenerMantenimiento(id: number): Promise<MantenimientoRegistro[]> {
    const response = await api.get<MantenimientoRegistro[]>(`/vehiculos/${id}/mantenimiento`)
    return response.data
  },

  async agregarMantenimiento(id: number, data: Omit<MantenimientoRegistro, 'id'>): Promise<MantenimientoRegistro> {
    const response = await api.post<MantenimientoRegistro>(`/vehiculos/${id}/mantenimiento`, data)
    return response.data
  },

  async obtenerRevision(id: number): Promise<RevisionTecnica> {
    const response = await api.get<RevisionTecnica>(`/vehiculos/${id}/revision`)
    return response.data
  },

  async actualizarRevision(id: number, data: Omit<RevisionTecnica, 'id'>): Promise<RevisionTecnica> {
    const response = await api.put<RevisionTecnica>(`/vehiculos/${id}/revision`, data)
    return response.data
  }
}

export default vehiculosService
