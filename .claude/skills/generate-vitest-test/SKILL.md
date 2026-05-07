---
name: generate-vitest-test
description: Generate Vitest unit tests for Vue 3 components, Pinia stores, composables, and TypeScript services. Uses Vue Test Utils + @testing-library/vue, with mocked Axios and Pinia. Targets 80%+ coverage with proper user-interaction testing.
---

# Generate Vitest Test Skill

Creates comprehensive unit tests for frontend code.

## Inputs Needed

Ask the user for:
1. **Type**: component / store / composable / service
2. **Target file**: path to file under test
3. **Focus**: happy path / edge cases / accessibility / all

## Test Templates

### 1. Component Test (with Vue Test Utils)

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, type VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import StudentForm from '../StudentForm.vue'
import { useEstudiantesStore } from '@/stores/estudiantes'

describe('StudentForm.vue', () => {
  let wrapper: VueWrapper

  beforeEach(() => {
    setActivePinia(createPinia())
    
    wrapper = mount(StudentForm, {
      global: {
        stubs: ['router-link']
      },
      props: {
        readonly: false
      }
    })
  })

  describe('rendering', () => {
    it('renders correctly with default props', () => {
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.find('form').exists()).toBe(true)
      expect(wrapper.text()).toContain('Nuevo Estudiante')
    })

    it('renders "Editar" mode when studentId is provided', async () => {
      await wrapper.setProps({ studentId: 1 })
      expect(wrapper.text()).toContain('Editar Estudiante')
    })

    it('disables inputs when readonly is true', async () => {
      await wrapper.setProps({ readonly: true })
      const inputs = wrapper.findAll('input')
      inputs.forEach(input => {
        expect(input.attributes('disabled')).toBeDefined()
      })
    })
  })

  describe('validation', () => {
    it('shows error when cedula is invalid format', async () => {
      const cedulaInput = wrapper.find('input[id="cedula"]')
      await cedulaInput.setValue('123')
      await cedulaInput.trigger('blur')

      expect(wrapper.text()).toContain('Cédula debe tener 10 dígitos')
    })

    it('shows error when email is invalid', async () => {
      const emailInput = wrapper.find('input[id="email"]')
      await emailInput.setValue('not-an-email')
      await emailInput.trigger('blur')

      expect(wrapper.text()).toContain('Email inválido')
    })

    it('disables submit button when form is invalid', async () => {
      const submitBtn = wrapper.find('button[type="submit"]')
      expect(submitBtn.attributes('disabled')).toBeDefined()
    })

    it('enables submit when all required fields are valid', async () => {
      await wrapper.find('input[id="cedula"]').setValue('1712345678')
      await wrapper.find('input[id="nombres"]').setValue('Juan')
      await wrapper.find('input[id="apellidos"]').setValue('Pérez')
      await wrapper.find('input[id="email"]').setValue('juan@example.com')
      await wrapper.find('input[id="fechaNacimiento"]').setValue('2000-01-01')

      const submitBtn = wrapper.find('button[type="submit"]')
      expect(submitBtn.attributes('disabled')).toBeUndefined()
    })
  })

  describe('user interactions', () => {
    it('emits "saved" event when form is submitted with valid data', async () => {
      const store = useEstudiantesStore()
      const mockSaved = { id: 1, cedula: '1712345678', nombres: 'Juan' }
      vi.spyOn(store, 'create').mockResolvedValue(mockSaved as any)

      // Fill form
      await wrapper.find('input[id="cedula"]').setValue('1712345678')
      await wrapper.find('input[id="nombres"]').setValue('Juan')
      await wrapper.find('input[id="apellidos"]').setValue('Pérez')
      await wrapper.find('input[id="email"]').setValue('juan@example.com')
      await wrapper.find('input[id="fechaNacimiento"]').setValue('2000-01-01')

      await wrapper.find('form').trigger('submit')
      await wrapper.vm.$nextTick()

      expect(wrapper.emitted('saved')).toBeTruthy()
      expect(wrapper.emitted('saved')?.[0][0]).toEqual(mockSaved)
    })

    it('emits "cancelled" when cancel button is clicked', async () => {
      await wrapper.find('button[type="button"]').trigger('click')
      expect(wrapper.emitted('cancelled')).toBeTruthy()
    })

    it('shows loading state during submission', async () => {
      const store = useEstudiantesStore()
      vi.spyOn(store, 'create').mockImplementation(() => new Promise(() => {}))  // never resolves

      // Fill and submit
      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit')
      await wrapper.vm.$nextTick()

      const submitBtn = wrapper.find('button[type="submit"]')
      expect(submitBtn.text()).toContain('Guardando...')
      expect(submitBtn.attributes('disabled')).toBeDefined()
    })

    it('emits "error" when submission fails', async () => {
      const store = useEstudiantesStore()
      vi.spyOn(store, 'create').mockRejectedValue(new Error('Network error'))

      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.emitted('error')).toBeTruthy()
    })
  })

  describe('accessibility', () => {
    it('has proper labels for all inputs', () => {
      const inputs = wrapper.findAll('input')
      inputs.forEach(input => {
        const id = input.attributes('id')
        const label = wrapper.find(`label[for="${id}"]`)
        expect(label.exists()).toBe(true)
      })
    })

    it('marks required fields with aria-label', () => {
      const requiredMarkers = wrapper.findAll('span[aria-label="required"]')
      expect(requiredMarkers.length).toBeGreaterThan(0)
    })

    it('sets aria-invalid on fields with errors', async () => {
      await wrapper.find('input[id="cedula"]').setValue('123')
      await wrapper.find('input[id="cedula"]').trigger('blur')

      const cedulaInput = wrapper.find('input[id="cedula"]')
      expect(cedulaInput.attributes('aria-invalid')).toBe('true')
    })
  })
})

async function fillValidForm(wrapper: VueWrapper) {
  await wrapper.find('input[id="cedula"]').setValue('1712345678')
  await wrapper.find('input[id="nombres"]').setValue('Juan')
  await wrapper.find('input[id="apellidos"]').setValue('Pérez')
  await wrapper.find('input[id="email"]').setValue('juan@example.com')
  await wrapper.find('input[id="fechaNacimiento"]').setValue('2000-01-01')
}

async function flushPromises() {
  await new Promise(resolve => setTimeout(resolve, 0))
}
```

### 2. Store Test

See `generate-pinia-store` skill for Pinia store test template.

### 3. Composable Test

```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent } from 'vue'
import { useApi } from '../useApi'

describe('useApi composable', () => {
  it('returns loading=true while request is in flight', async () => {
    const TestComponent = defineComponent({
      template: '<div>{{ loading }}</div>',
      setup() {
        const { loading, execute } = useApi(() => 
          new Promise(resolve => setTimeout(resolve, 100))
        )
        execute()
        return { loading }
      }
    })

    const wrapper = mount(TestComponent)
    expect(wrapper.text()).toBe('true')
    
    await new Promise(resolve => setTimeout(resolve, 150))
    await wrapper.vm.$nextTick()
    
    expect(wrapper.text()).toBe('false')
  })

  it('exposes data on success', async () => {
    const mockFn = vi.fn().mockResolvedValue({ id: 1, name: 'Test' })
    
    let result: any
    const TestComponent = defineComponent({
      template: '<div></div>',
      setup() {
        result = useApi(mockFn)
        return {}
      }
    })
    mount(TestComponent)
    
    await result.execute()
    
    expect(result.data.value).toEqual({ id: 1, name: 'Test' })
    expect(result.error.value).toBeNull()
  })

  it('exposes error on failure', async () => {
    const mockFn = vi.fn().mockRejectedValue(new Error('Failed'))
    
    let result: any
    const TestComponent = defineComponent({
      template: '<div></div>',
      setup() {
        result = useApi(mockFn)
        return {}
      }
    })
    mount(TestComponent)
    
    await expect(result.execute()).rejects.toThrow('Failed')
    
    expect(result.error.value).toBeInstanceOf(Error)
    expect(result.data.value).toBeNull()
  })
})
```

### 4. Service Test (with mocked Axios)

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { estudiantesService } from '../estudiantesService'
import { api } from '../api'

vi.mock('../api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

describe('estudiantesService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('list', () => {
    it('calls correct endpoint with params', async () => {
      const mockData = { content: [], totalElements: 0, page: 0, size: 20, totalPages: 0, first: true, last: true }
      vi.mocked(api.get).mockResolvedValue({ data: mockData } as any)

      const result = await estudiantesService.list(0, 20)

      expect(api.get).toHaveBeenCalledWith('/v1/estudiantes?page=0&size=20')
      expect(result).toEqual(mockData)
    })
  })

  describe('create', () => {
    it('sends Idempotency-Key header', async () => {
      const request = { cedula: '1712345678', nombres: 'Juan', apellidos: 'Pérez', email: 'j@e.com', fechaNacimiento: '2000-01-01' }
      vi.mocked(api.post).mockResolvedValue({ data: { id: 1, ...request } } as any)

      await estudiantesService.create(request)

      expect(api.post).toHaveBeenCalledWith(
        '/v1/estudiantes',
        request,
        expect.objectContaining({
          headers: expect.objectContaining({ 'Idempotency-Key': expect.any(String) })
        })
      )
    })
  })
})
```

## Vitest Config (vitest.config.ts)

```typescript
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  test: {
    globals: true,
    environment: 'jsdom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'dist/',
        '**/*.config.{ts,js}',
        '**/types/**',
        '**/router/**'
      ],
      thresholds: {
        lines: 80,
        functions: 80,
        branches: 70,
        statements: 80
      }
    },
    setupFiles: ['./vitest.setup.ts']
  }
})
```

## Workflow

1. **Read** the file under test and understand its behavior
2. **Identify** all inputs, outputs, and side effects
3. **Generate** test file with describe blocks per feature
4. **Mock** external dependencies (api, stores, router)
5. **Cover**: rendering, props, emits, user interactions, edge cases
6. **Run** `npm run test`
7. **Run** `npm run coverage` and verify thresholds met

## Quality Checklist

- [ ] Tests use describe/it for logical grouping
- [ ] Test names descriptive (`it('does X when Y')`)
- [ ] Each test independent (no shared state)
- [ ] External deps mocked
- [ ] Cleanup in beforeEach (setActivePinia, vi.clearAllMocks)
- [ ] User interactions tested (typing, clicking)
- [ ] Loading + error + success states tested
- [ ] Accessibility tested (labels, ARIA)
- [ ] Coverage thresholds met (80% lines)
- [ ] No flaky tests (no Math.random, dates mocked)
- [ ] Fast (< 50ms per test)

## Notes

- Use `@testing-library/vue` for user-centric tests
- Use Vue Test Utils for fine-grained component testing
- Mock at the boundary (mock api/stores, not internal helpers)
- Test behavior, not implementation
- Don't test framework code (Vue's reactivity, etc.)
- For E2E flows, use Cypress (not Vitest)
