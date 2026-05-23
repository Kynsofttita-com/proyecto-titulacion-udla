import axios, { AxiosInstance } from 'axios'
import { useAuthStore } from '@/stores/auth'

const api: AxiosInstance = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const authStore = useAuthStore()
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      try {
        await authStore.refreshToken()
        return api(originalRequest)
      } catch (err) {
        authStore.logout()
        window.location.href = '/login'
        return Promise.reject(err)
      }
    }

    return Promise.reject(error)
  }
)

export default api
