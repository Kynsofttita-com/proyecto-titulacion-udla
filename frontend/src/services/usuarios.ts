import api from './api'

export interface CreateUsuarioRequest {
  email: string
  nombreCompleto: string
  roles: string[]
}

export interface UpdateUsuarioRequest extends CreateUsuarioRequest {
  id: number
}

export interface UsuarioResponse {
  id: number
  email: string
  nombreCompleto: string
  roles: string[]
  estado: 'ACTIVO' | 'INACTIVO'
}

const usuariosService = {
  async obtenerUsuarios(): Promise<UsuarioResponse[]> {
    const response = await api.get<UsuarioResponse[]>('/usuarios')
    return response.data
  },

  async obtenerUsuario(id: number): Promise<UsuarioResponse> {
    const response = await api.get<UsuarioResponse>(`/usuarios/${id}`)
    return response.data
  },

  async crearUsuario(data: CreateUsuarioRequest): Promise<UsuarioResponse> {
    const response = await api.post<UsuarioResponse>('/usuarios', data)
    return response.data
  },

  async actualizarUsuario(id: number, data: UpdateUsuarioRequest): Promise<UsuarioResponse> {
    const response = await api.put<UsuarioResponse>(`/usuarios/${id}`, data)
    return response.data
  },

  async eliminarUsuario(id: number): Promise<void> {
    await api.delete(`/usuarios/${id}`)
  },

  /**
   * Cambio de contraseña por el propio usuario (self-service).
   *
   * NOTA (deuda Sprint 13): el endpoint backend `/auth/cambiar-password`
   * aún no está implementado. Devolverá 404 hasta que se implemente.
   * Documentado en DECISIONES.md / project_state. El form de cambio en
   * /perfil quedó listo para conectarse automáticamente cuando se cree
   * el endpoint.
   */
  async cambiarPassword(data: { currentPassword: string; newPassword: string }): Promise<void> {
    await api.post('/auth/cambiar-password', data)
  }
}

export default usuariosService
