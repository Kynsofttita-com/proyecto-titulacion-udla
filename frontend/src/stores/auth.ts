import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

interface User {
  id: number
  email: string
  nombre?: string
  apellido?: string
  nombreCompleto?: string
  roles: string[]
  mustChangePassword?: boolean
}

export const useAuthStore = defineStore(
  'auth',
  () => {
    const user = ref<User | null>(null)
    const token = ref<string | null>(null)
    const activeRole = ref<string | null>(null)
    // ID del instructor asociado al user logueado (solo poblado si rol = INSTRUCTOR
    // y existe un instructor con usuario_id = user.id). Usado para filtrar las
    // vistas de "mis clases" y "mis estudiantes".
    const currentInstructorId = ref<number | null>(null)
    // Equivalente para ESTUDIANTE: id del registro de estudiante asociado.
    // Usado para filtrar "mis clases", "mi progreso" y "mis pagos".
    const currentEstudianteId = ref<number | null>(null)
    const isLoading = ref(false)
    const error = ref<string | null>(null)

    // ============== COMPUTED ==============

    const isAuthenticated = computed(() => !!token.value && !!user.value)

    const userId = computed<number | null>(() => user.value?.id ?? null)

    const roles = computed<string[]>(() => user.value?.roles ?? [])

    const hasMultipleRoles = computed(() => roles.value.length > 1)

    const currentRole = computed(() => {
      // Si hay un rol activo seleccionado y el user lo tiene, ese
      if (activeRole.value && roles.value.includes(activeRole.value)) {
        return activeRole.value
      }
      // Sino, el primero (prioridad implícita: ADMIN > STAFF > INSTRUCTOR > ESTUDIANTE por orden del backend)
      return roles.value[0] || ''
    })

    const mustChangePassword = computed(() => user.value?.mustChangePassword ?? false)

    // ============== FUNCIONES ==============

    const hasRole = (rolesQuery: string[]) => {
      return user.value?.roles.some(role => rolesQuery.includes(role)) ?? false
    }

    const setActiveRole = (rol: string) => {
      if (user.value?.roles.includes(rol)) {
        activeRole.value = rol
      }
    }

    const refreshProfile = async () => {
      try {
        const response = await api.get('/auth/me')
        user.value = response.data
      } catch (err) {
        console.error('Error refrescando perfil', err)
      }
    }

    // Carga el instructorId asociado al usuario logueado si tiene rol INSTRUCTOR.
    // Idempotente: si ya esta cargado o el user no es INSTRUCTOR, no hace nada.
    const loadInstructorId = async () => {
      if (currentInstructorId.value) return
      if (!user.value?.roles?.includes('INSTRUCTOR')) return
      try {
        const response = await api.get('/instructores/me')
        currentInstructorId.value = response.data?.id ?? null
      } catch (err) {
        // 404 = usuario no esta vinculado a ningun instructor. Es valido para
        // un INSTRUCTOR recien creado al que aun no le asignaron registro.
        console.warn('No se pudo cargar instructor asociado al usuario', err)
        currentInstructorId.value = null
      }
    }

    // Carga el estudianteId asociado al usuario logueado si tiene rol ESTUDIANTE.
    // Idempotente: si ya esta cargado o el user no es ESTUDIANTE, no hace nada.
    const loadEstudianteId = async () => {
      if (currentEstudianteId.value) return
      if (!user.value?.roles?.includes('ESTUDIANTE')) return
      try {
        const response = await api.get('/estudiantes/me')
        currentEstudianteId.value = response.data?.id ?? null
      } catch (err) {
        console.warn('No se pudo cargar estudiante asociado al usuario', err)
        currentEstudianteId.value = null
      }
    }

    const login = async (email: string, password: string) => {
      isLoading.value = true
      error.value = null
      try {
        const response = await api.post('/auth/login', { email, password })
        token.value = response.data.accessToken || response.data.token
        user.value = response.data.user
        // Reset del rol activo al login para que tome el primero por defecto
        activeRole.value = null
        currentInstructorId.value = null
        currentEstudianteId.value = null
        api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
        // Cargar IDs derivados en segundo plano (no bloqueante)
        if (user.value?.roles?.includes('INSTRUCTOR')) {
          loadInstructorId().catch(() => {})
        }
        if (user.value?.roles?.includes('ESTUDIANTE')) {
          loadEstudianteId().catch(() => {})
        }
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
      activeRole.value = null
      currentInstructorId.value = null
      currentEstudianteId.value = null
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
      // state
      user,
      token,
      activeRole,
      currentInstructorId,
      currentEstudianteId,
      isLoading,
      error,
      // computed
      isAuthenticated,
      userId,
      roles,
      hasMultipleRoles,
      currentRole,
      mustChangePassword,
      // functions
      hasRole,
      setActiveRole,
      refreshProfile,
      loadInstructorId,
      loadEstudianteId,
      login,
      logout,
      refreshToken
    }
  },
  {
    persist: {
      paths: ['token', 'user', 'activeRole', 'currentInstructorId', 'currentEstudianteId']
    }
  }
)
