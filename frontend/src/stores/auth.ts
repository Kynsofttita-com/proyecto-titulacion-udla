import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

interface User {
  id: number
  email: string
  nombreCompleto: string
  roles: string[]
}

export const useAuthStore = defineStore(
  'auth',
  () => {
    const user = ref<User | null>(null)
    const token = ref<string | null>(null)
    const isLoading = ref(false)
    const error = ref<string | null>(null)

    const isAuthenticated = computed(() => !!token.value && !!user.value)

    const hasRole = (roles: string[]) => {
      return user.value?.roles.some(role => roles.includes(role)) ?? false
    }

    const login = async (email: string, password: string) => {
      isLoading.value = true
      error.value = null
      try {
        const response = await api.post('/auth/login', { email, password })
        token.value = response.data.accessToken || response.data.token
        user.value = response.data.user
        api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
      } catch (err: any) {
        const data = err.response?.data
        const status = err.response?.status
        // Mensajes claros según el código de error
        if (status === 423) {
          // Cuenta bloqueada — extraer fecha y formatearla legible
          const detail = data?.detail || data?.message || ''
          const match = detail.match(/(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})/)
          if (match) {
            const lockDate = new Date(match[1])
            const fecha = lockDate.toLocaleString('es-EC', {
              day: '2-digit', month: 'long', hour: '2-digit', minute: '2-digit'
            })
            error.value = `Cuenta bloqueada temporalmente por seguridad. Podrás intentar nuevamente a las ${fecha}.`
          } else {
            error.value = 'Cuenta bloqueada temporalmente. Espera unos minutos antes de reintentar.'
          }
        } else if (status === 401) {
          error.value = 'Correo o contraseña incorrectos.'
        } else if (status === 0 || !status) {
          error.value = 'No se pudo conectar con el servidor. Verifica tu conexión.'
        } else {
          error.value = data?.detail || data?.message || 'Error al iniciar sesión.'
        }
        throw err
      } finally {
        isLoading.value = false
      }
    }

    const logout = async () => {
      try {
        await api.post('/auth/logout')
      } catch (_) {
        // si el backend no responde igual cerramos sesión local
      }
      // 1. Limpiar estado en memoria
      token.value = null
      user.value = null
      error.value = null
      delete api.defaults.headers.common['Authorization']
      // 2. Limpiar localStorage de Pinia persist
      try {
        localStorage.removeItem('auth')
      } catch (_) {}
    }

    const refreshToken = async () => {
      try {
        const response = await api.post('/auth/refresh')
        token.value = response.data.accessToken || response.data.token
        api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
      } catch (err) {
        logout()
      }
    }

    return {
      user,
      token,
      isLoading,
      error,
      isAuthenticated,
      hasRole,
      login,
      logout,
      refreshToken
    }
  },
  {
    persist: {
      paths: ['token', 'user']
    }
  }
)
