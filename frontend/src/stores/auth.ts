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
        token.value = response.data.token
        user.value = response.data.user
        api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
      } catch (err: any) {
        error.value = err.response?.data?.message || 'Error al iniciar sesión'
        throw err
      } finally {
        isLoading.value = false
      }
    }

    const logout = async () => {
      try {
        await api.post('/auth/logout')
      } finally {
        token.value = null
        user.value = null
        delete api.defaults.headers.common['Authorization']
      }
    }

    const refreshToken = async () => {
      try {
        const response = await api.post('/auth/refresh')
        token.value = response.data.token
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
