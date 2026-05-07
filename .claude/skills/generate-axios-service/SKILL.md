---
name: generate-axios-service
description: Generate a typed Axios HTTP client service for a specific domain (e.g., estudiantes, vehiculos), with JWT token injection, automatic refresh on 401, error transformation, request/response logging, and TypeScript types matching the backend OpenAPI contract.
---

# Generate Axios Service Skill

Creates a typed API client service that wraps Axios calls.

## Inputs Needed

Ask the user for:
1. **Domain**: kebab-case (e.g., `estudiantes`, `vehiculos`)
2. **Backend URL**: usually `/v1/<domain>` via API Gateway
3. **Operations**: list with HTTP method + path + types

## Output Files

- `frontend/src/services/<domain>Service.ts` — domain service
- `frontend/src/services/api.ts` — shared Axios instance (only if not exists)
- `frontend/src/types/<domain>.ts` — TypeScript types

## Shared Axios Instance (api.ts)

```typescript
// frontend/src/services/api.ts
import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json'
  }
})

// Request interceptor: inject JWT
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    // Add correlation ID for distributed tracing
    config.headers['X-Correlation-Id'] = crypto.randomUUID()
    
    return config
  },
  (error: AxiosError) => Promise.reject(error)
)

// Response interceptor: handle errors, refresh token
let isRefreshing = false
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: Error) => void }> = []

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else if (token) resolve(token)
  })
  failedQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest: any = error.config
    const authStore = useAuthStore()

    // 401 → try refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Queue request until refresh completes
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve: (token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            resolve(api(originalRequest))
          }, reject })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        await authStore.refresh()
        processQueue(null, authStore.token)
        originalRequest.headers.Authorization = `Bearer ${authStore.token}`
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError as Error)
        await authStore.logout()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // Transform error to user-friendly message
    return Promise.reject(transformError(error))
  }
)

interface ApiError extends Error {
  status?: number
  errors?: Array<{ field: string; message: string }>
  problemDetail?: any
}

function transformError(error: AxiosError): ApiError {
  const apiError: ApiError = new Error('Network error') as ApiError
  
  if (error.response) {
    const data = error.response.data as any
    apiError.message = data.detail || data.message || `HTTP ${error.response.status}`
    apiError.status = error.response.status
    apiError.errors = data.errors
    apiError.problemDetail = data
  } else if (error.request) {
    apiError.message = 'Sin conexión al servidor'
  } else {
    apiError.message = error.message
  }
  
  return apiError
}

export default api
```

## Domain Service Template

```typescript
// frontend/src/services/<domain>Service.ts
import { api } from './api'
import type {
  <Entity>,
  Create<Entity>Request,
  Update<Entity>Request,
  Page
} from '@/types/<domain>'

const BASE_PATH = '/v1/<domain>'

export const <domain>Service = {
  /**
   * List <domain> with pagination
   */
  async list(page: number = 0, size: number = 20, sort?: string): Promise<Page<<Entity>>> {
    const params = new URLSearchParams({ 
      page: String(page), 
      size: String(size) 
    })
    if (sort) params.append('sort', sort)
    
    const { data } = await api.get<Page<<Entity>>>(`${BASE_PATH}?${params}`)
    return data
  },

  /**
   * Get a single <entity> by ID
   */
  async getById(id: number): Promise<<Entity>> {
    const { data } = await api.get<<Entity>>(`${BASE_PATH}/${id}`)
    return data
  },

  /**
   * Create a new <entity>
   */
  async create(request: Create<Entity>Request): Promise<<Entity>> {
    const { data } = await api.post<<Entity>>(BASE_PATH, request, {
      headers: { 'Idempotency-Key': crypto.randomUUID() }
    })
    return data
  },

  /**
   * Update an existing <entity>
   */
  async update(id: number, request: Update<Entity>Request): Promise<<Entity>> {
    const { data } = await api.put<<Entity>>(`${BASE_PATH}/${id}`, request)
    return data
  },

  /**
   * Delete an <entity>
   */
  async delete(id: number): Promise<void> {
    await api.delete(`${BASE_PATH}/${id}`)
  },

  // ===== Custom operations =====

  /**
   * Search by query string
   */
  async search(query: string, page: number = 0): Promise<Page<<Entity>>> {
    const params = new URLSearchParams({ q: query, page: String(page) })
    const { data } = await api.get<Page<<Entity>>>(`${BASE_PATH}/search?${params}`)
    return data
  },

  /**
   * Export as CSV/Excel
   */
  async export(format: 'csv' | 'excel'): Promise<Blob> {
    const { data } = await api.get<Blob>(`${BASE_PATH}/export?format=${format}`, {
      responseType: 'blob'
    })
    return data
  }
}
```

## TypeScript Types Template

```typescript
// frontend/src/types/<domain>.ts

export interface <Entity> {
  id: number
  cedula: string
  nombres: string
  apellidos: string
  email: string
  telefono?: string
  fechaNacimiento: string  // ISO date string
  estado: Estado<Entity>
  createdAt: string  // ISO datetime
  updatedAt: string
}

export type Estado<Entity> = 'ACTIVO' | 'INACTIVO' | 'GRADUADO' | 'RETIRADO'

export interface Create<Entity>Request {
  cedula: string
  nombres: string
  apellidos: string
  email: string
  telefono?: string
  fechaNacimiento: string
}

export type Update<Entity>Request = Partial<Omit<Create<Entity>Request, 'cedula'>>

// Generic pagination
export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
```

## Special Case: Auth Service

```typescript
// frontend/src/services/authService.ts
import { api } from './api'
import type { LoginRequest, LoginResponse, User } from '@/types/auth'

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const { data } = await api.post<LoginResponse>('/v1/auth/login', credentials)
    return data
  },

  async logout(refreshToken: string): Promise<void> {
    await api.post('/v1/auth/logout', { refreshToken })
  },

  async refresh(refreshToken: string): Promise<LoginResponse> {
    const { data } = await api.post<LoginResponse>('/v1/auth/refresh', { refreshToken })
    return data
  },

  async getCurrentUser(): Promise<User> {
    const { data } = await api.get<User>('/v1/auth/me')
    return data
  },

  async forgotPassword(email: string): Promise<void> {
    await api.post('/v1/auth/forgot-password', { email })
  },

  async resetPassword(token: string, newPassword: string): Promise<void> {
    await api.post('/v1/auth/reset-password', { token, newPassword })
  },

  async changePassword(currentPassword: string, newPassword: string): Promise<void> {
    await api.post('/v1/auth/change-password', { currentPassword, newPassword })
  }
}
```

## Workflow

1. **Read** OpenAPI spec for the backend service (`docs/api/<service>-openapi.yaml`)
2. **Generate** TypeScript types matching the API contract
3. **Generate** the service file with all operations
4. **Verify** the shared `api.ts` exists (create if not)
5. **Run** `npm run type-check`
6. **Test** with browser DevTools or unit test (mock api)

## Quality Checklist

- [ ] All operations have JSDoc comments
- [ ] Request/Response typed (no `any`)
- [ ] Error handling delegated to interceptors
- [ ] No try/catch in service methods (let it propagate)
- [ ] Pagination params standardized
- [ ] Idempotency-Key on POST for financial ops
- [ ] Correlation-Id sent on all requests (for tracing)
- [ ] Type check passes

## Auto-Generation Alternative

For large APIs, consider generating from OpenAPI:
```bash
npm install --save-dev openapi-typescript-codegen

npx openapi-typescript-codegen --input docs/api/ms-estudiantes-openapi.yaml \
  --output frontend/src/api/generated --client axios
```

Then wrap generated client in domain service for additional logic (caching, etc.).

## Notes

- Keep services thin (just HTTP + types)
- No business logic in services (that goes in stores or composables)
- Don't import services directly in components — go through stores
- Always use the shared `api` instance (interceptors handle auth, errors)
- For file uploads, use `FormData` and override `Content-Type`
- For downloads, use `responseType: 'blob'`
