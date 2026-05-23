import api from './api'

export interface SchoolConfigRequest {
  nombre: string
  ruc: string
  email: string
  telefono: string
  direccion: string
}

export interface SchoolConfigResponse extends SchoolConfigRequest {
  id: number
}

const configuracionService = {
  async obtenerConfiguracion(): Promise<SchoolConfigResponse> {
    const response = await api.get<SchoolConfigResponse>('/configuracion/escuela')
    return response.data
  },

  async actualizarConfiguracion(data: SchoolConfigRequest): Promise<SchoolConfigResponse> {
    const response = await api.put<SchoolConfigResponse>('/configuracion/escuela', data)
    return response.data
  }
}

export default configuracionService
