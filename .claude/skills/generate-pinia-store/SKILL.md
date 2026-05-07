---
name: generate-pinia-store
description: Generate a Pinia store using Composition API style with TypeScript, including state, getters (computed), actions (async with loading/error states), persistence to localStorage if needed, and Vitest tests. Follows the project's standard pattern (one store per domain).
---

# Generate Pinia Store Skill

Creates a typed Pinia store for managing domain state.

## Inputs Needed

Ask the user for:
1. **Store name**: kebab-case domain (e.g., `auth`, `estudiantes`, `vehiculos`)
2. **Entity type**: TypeScript interface name
3. **Operations**: which CRUD ops + custom actions
4. **Persistence**: persist to localStorage? (rare; usually only auth)

## Output

- Store: `frontend/src/stores/<domain>.ts`
- Test: `frontend/src/stores/__tests__/<domain>.test.ts`

## Template (Composition Store)

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { <domain>Service } from '@/services/<domain>Service'
import type {
  <Entity>,
  Create<Entity>Request,
  Update<Entity>Request,
  Page
} from '@/types/<domain>'

export const use<Domain>Store = defineStore('<domain>', () => {
  // ===== State =====
  const items = ref<<Entity>[]>([])
  const currentItem = ref<<Entity> | null>(null)
  const totalElements = ref(0)
  const totalPages = ref(0)
  const currentPage = ref(0)
  const pageSize = ref(20)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // ===== Getters (computed) =====
  const isEmpty = computed(() => items.value.length === 0)
  const hasError = computed(() => error.value !== null)
  const isLoaded = computed(() => !loading.value && !hasError.value)

  function findById(id: number): <Entity> | undefined {
    return items.value.find(item => item.id === id)
  }

  // ===== Actions =====

  async function fetchAll(page: number = 0, size: number = 20): Promise<void> {
    loading.value = true
    error.value = null
    
    try {
      const response = await <domain>Service.list(page, size)
      items.value = response.content
      totalElements.value = response.totalElements
      totalPages.value = response.totalPages
      currentPage.value = response.page
      pageSize.value = response.size
    } catch (err) {
      error.value = (err as Error).message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchById(id: number): Promise<<Entity>> {
    loading.value = true
    error.value = null
    
    try {
      const item = await <domain>Service.getById(id)
      currentItem.value = item
      
      // Update in list if exists
      const index = items.value.findIndex(i => i.id === id)
      if (index >= 0) {
        items.value[index] = item
      }
      
      return item
    } catch (err) {
      error.value = (err as Error).message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function create(request: Create<Entity>Request): Promise<<Entity>> {
    loading.value = true
    error.value = null
    
    try {
      const created = await <domain>Service.create(request)
      items.value.unshift(created)  // add to beginning
      totalElements.value += 1
      return created
    } catch (err) {
      error.value = (err as Error).message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function update(id: number, request: Update<Entity>Request): Promise<<Entity>> {
    loading.value = true
    error.value = null
    
    try {
      const updated = await <domain>Service.update(id, request)
      
      const index = items.value.findIndex(i => i.id === id)
      if (index >= 0) {
        items.value[index] = updated
      }
      if (currentItem.value?.id === id) {
        currentItem.value = updated
      }
      
      return updated
    } catch (err) {
      error.value = (err as Error).message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function remove(id: number): Promise<void> {
    loading.value = true
    error.value = null
    
    try {
      await <domain>Service.delete(id)
      items.value = items.value.filter(i => i.id !== id)
      totalElements.value -= 1
      if (currentItem.value?.id === id) {
        currentItem.value = null
      }
    } catch (err) {
      error.value = (err as Error).message
      throw err
    } finally {
      loading.value = false
    }
  }

  function clearError(): void {
    error.value = null
  }

  function reset(): void {
    items.value = []
    currentItem.value = null
    totalElements.value = 0
    totalPages.value = 0
    currentPage.value = 0
    error.value = null
    loading.value = false
  }

  return {
    // state
    items,
    currentItem,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    loading,
    error,
    
    // getters
    isEmpty,
    hasError,
    isLoaded,
    findById,
    
    // actions
    fetchAll,
    fetchById,
    create,
    update,
    remove,
    clearError,
    reset
  }
})
```

## Special Case: Auth Store (with persistence)

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/authService'
import type { User, LoginRequest } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('access_token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refresh_token'))
  const user = ref<User | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  
  const hasRole = (role: string) => 
    computed(() => user.value?.roles.includes(role) ?? false)
  
  const isAdmin = computed(() => user.value?.roles.includes('ADMIN') ?? false)

  async function login(credentials: LoginRequest): Promise<void> {
    loading.value = true
    error.value = null
    
    try {
      const response = await authService.login(credentials)
      
      token.value = response.accessToken
      refreshToken.value = response.refreshToken
      user.value = response.user
      
      // Persist
      localStorage.setItem('access_token', response.accessToken)
      localStorage.setItem('refresh_token', response.refreshToken)
    } catch (err) {
      error.value = 'Credenciales inválidas'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function logout(): Promise<void> {
    try {
      if (refreshToken.value) {
        await authService.logout(refreshToken.value)
      }
    } catch (err) {
      // ignore - logout locally anyway
    } finally {
      token.value = null
      refreshToken.value = null
      user.value = null
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
    }
  }

  async function refresh(): Promise<void> {
    if (!refreshToken.value) {
      throw new Error('No refresh token')
    }
    
    const response = await authService.refresh(refreshToken.value)
    token.value = response.accessToken
    localStorage.setItem('access_token', response.accessToken)
  }

  async function fetchCurrentUser(): Promise<void> {
    if (!token.value) return
    
    try {
      user.value = await authService.getCurrentUser()
    } catch (err) {
      logout()
    }
  }

  return {
    token,
    user,
    loading,
    error,
    isAuthenticated,
    hasRole,
    isAdmin,
    login,
    logout,
    refresh,
    fetchCurrentUser
  }
})
```

## Test Template

```typescript
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { use<Domain>Store } from '../<domain>'
import { <domain>Service } from '@/services/<domain>Service'

vi.mock('@/services/<domain>Service')

describe('<Domain> Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('fetchAll', () => {
    it('should populate items on success', async () => {
      const store = use<Domain>Store()
      const mockData = {
        content: [{ id: 1, ...mockEntity }],
        totalElements: 1,
        totalPages: 1,
        page: 0,
        size: 20
      }
      vi.mocked(<domain>Service.list).mockResolvedValue(mockData)

      await store.fetchAll()

      expect(store.items).toHaveLength(1)
      expect(store.totalElements).toBe(1)
      expect(store.loading).toBe(false)
      expect(store.error).toBeNull()
    })

    it('should set error on failure', async () => {
      const store = use<Domain>Store()
      vi.mocked(<domain>Service.list).mockRejectedValue(new Error('Network error'))

      await expect(store.fetchAll()).rejects.toThrow('Network error')
      
      expect(store.error).toBe('Network error')
      expect(store.loading).toBe(false)
    })
  })

  describe('create', () => {
    it('should add new item to start of list', async () => {
      const store = use<Domain>Store()
      store.items = [{ id: 2, ...mockEntity }]
      
      const newItem = { id: 1, ...mockEntity }
      vi.mocked(<domain>Service.create).mockResolvedValue(newItem)

      await store.create({ /* request */ })

      expect(store.items[0]).toEqual(newItem)
      expect(store.items).toHaveLength(2)
      expect(store.totalElements).toBe(1)  // incremented
    })
  })

  describe('reset', () => {
    it('should clear all state', () => {
      const store = use<Domain>Store()
      store.items = [{ id: 1, ...mockEntity }]
      store.error = 'Error'
      
      store.reset()
      
      expect(store.items).toHaveLength(0)
      expect(store.error).toBeNull()
    })
  })
})
```

## Workflow

1. **Read** existing stores (e.g., `auth.ts`) for conventions
2. **Generate** TypeScript types in `src/types/<domain>.ts`
3. **Generate** API service in `src/services/<domain>Service.ts` (use `generate-axios-service`)
4. **Generate** the Pinia store
5. **Generate** test file with mocked service
6. **Run** `npm run type-check && npm run test`

## Quality Checklist

- [ ] Composition API style (not Options API)
- [ ] All state typed with TypeScript
- [ ] Loading + error states tracked
- [ ] Actions are async functions
- [ ] Errors thrown after setting error state (component can decide)
- [ ] State properly reset (`reset()` action)
- [ ] No direct mutations from components (only via actions)
- [ ] Tests cover happy path + error path
- [ ] No `any` types

## Notes

- Use composition stores (`defineStore('id', () => { ... })`)
- One store per bounded context (auth, estudiantes, vehiculos, etc.)
- Don't store derived data in state — use computed
- Persist only auth tokens (and similar) in localStorage; never user data
- Use `storeToRefs(store)` in components to maintain reactivity
- Reset store on logout (call `reset()` from auth store)
