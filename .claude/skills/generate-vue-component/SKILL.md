---
name: generate-vue-component
description: Generate a Vue.js 3 component using Composition API with `<script setup lang="ts">`, TypeScript types, props/emits, validation, responsive styling (SCSS), and accessibility. Includes Vitest test stub. Follows project conventions for component structure and naming.
---

# Generate Vue Component Skill

Creates a Vue 3 component with TypeScript, props, emits, and styles.

## Inputs Needed

Ask the user for:
1. **Component name**: PascalCase (e.g., `StudentForm`, `VehicleList`)
2. **Type**: page-level (in `views/`) or reusable (in `components/`)
3. **Domain**: which feature folder (`auth`, `estudiantes`, `vehiculos`, etc.)
4. **Props**: list with types
5. **Emits**: list with payload types
6. **Description**: what does it do?

## Output

Component file at:
- Page: `frontend/src/views/<domain>/<ComponentName>.vue`
- Reusable: `frontend/src/components/<domain>/<ComponentName>.vue`

Test file: `frontend/src/components/<domain>/__tests__/<ComponentName>.test.ts` (or `views/<domain>/__tests__/`)

## Template

```vue
<script setup lang="ts">
// ===== 1. Imports =====
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useToast } from '@/composables/useToast'
import { use<Domain>Store } from '@/stores/<domain>'
import type { <Type>, <OtherType> } from '@/types/<domain>'

// ===== 2. Props =====
interface Props {
  /** Description of prop */
  studentId: number
  /** Optional flag */
  readonly?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false
})

// ===== 3. Emits =====
const emit = defineEmits<{
  /** Emitted when student is saved */
  saved: [student: <Type>]
  /** Emitted when user cancels */
  cancelled: []
  /** Emitted when error occurs */
  error: [message: string]
}>()

// ===== 4. Composables =====
const router = useRouter()
const route = useRoute()
const toast = useToast()
const store = use<Domain>Store()
const { items, loading, error } = storeToRefs(store)

// ===== 5. Reactive State =====
const formData = ref<<Type>>({
  cedula: '',
  nombres: '',
  apellidos: '',
  email: '',
  telefono: '',
  fechaNacimiento: ''
})

const errors = ref<Record<string, string>>({})
const submitting = ref(false)

// ===== 6. Computed =====
const isValid = computed(() => {
  return Object.keys(errors.value).length === 0 &&
    formData.value.cedula.length === 10 &&
    formData.value.nombres.length >= 2 &&
    formData.value.apellidos.length >= 2 &&
    formData.value.email.includes('@')
})

const fullName = computed(() => 
  `${formData.value.nombres} ${formData.value.apellidos}`.trim()
)

// ===== 7. Watchers =====
watch(() => props.studentId, async (newId) => {
  if (newId) await loadStudent(newId)
})

// ===== 8. Methods =====
async function loadStudent(id: number): Promise<void> {
  try {
    const student = await store.fetchById(id)
    formData.value = { ...student }
  } catch (err) {
    toast.error('Error al cargar estudiante')
    emit('error', (err as Error).message)
  }
}

function validate(): boolean {
  errors.value = {}
  
  if (!/^\d{10}$/.test(formData.value.cedula)) {
    errors.value.cedula = 'Cédula debe tener 10 dígitos'
  }
  
  if (formData.value.nombres.length < 2) {
    errors.value.nombres = 'Nombres debe tener al menos 2 caracteres'
  }
  
  if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(formData.value.email)) {
    errors.value.email = 'Email inválido'
  }
  
  return Object.keys(errors.value).length === 0
}

async function handleSubmit(): Promise<void> {
  if (!validate()) {
    toast.warning('Por favor corrige los errores del formulario')
    return
  }
  
  submitting.value = true
  try {
    const saved = await store.save(formData.value)
    toast.success('Estudiante guardado exitosamente')
    emit('saved', saved)
  } catch (err) {
    toast.error('Error al guardar')
    emit('error', (err as Error).message)
  } finally {
    submitting.value = false
  }
}

function handleCancel(): void {
  emit('cancelled')
}

// ===== 9. Lifecycle =====
onMounted(async () => {
  if (props.studentId) {
    await loadStudent(props.studentId)
  }
})
</script>

<template>
  <form 
    class="student-form" 
    :aria-busy="submitting"
    @submit.prevent="handleSubmit"
  >
    <div class="form-header">
      <h2>{{ props.studentId ? 'Editar' : 'Nuevo' }} Estudiante</h2>
    </div>

    <div class="form-grid">
      <!-- Cédula -->
      <div class="form-field">
        <label for="cedula">Cédula <span aria-label="required">*</span></label>
        <input
          id="cedula"
          v-model="formData.cedula"
          type="text"
          maxlength="10"
          pattern="[0-9]{10}"
          required
          :disabled="readonly || submitting"
          :aria-invalid="!!errors.cedula"
          :aria-describedby="errors.cedula ? 'cedula-error' : undefined"
          @blur="validate"
        />
        <span 
          v-if="errors.cedula" 
          id="cedula-error" 
          class="error" 
          role="alert"
        >
          {{ errors.cedula }}
        </span>
      </div>

      <!-- Nombres -->
      <div class="form-field">
        <label for="nombres">Nombres <span aria-label="required">*</span></label>
        <input
          id="nombres"
          v-model="formData.nombres"
          type="text"
          maxlength="100"
          required
          :disabled="readonly || submitting"
          :aria-invalid="!!errors.nombres"
          @blur="validate"
        />
        <span v-if="errors.nombres" class="error" role="alert">
          {{ errors.nombres }}
        </span>
      </div>

      <!-- ... otros campos ... -->
    </div>

    <div class="form-actions">
      <button 
        type="button" 
        class="btn btn-secondary"
        :disabled="submitting"
        @click="handleCancel"
      >
        Cancelar
      </button>
      
      <button 
        type="submit" 
        class="btn btn-primary"
        :disabled="!isValid || submitting"
      >
        <span v-if="submitting" class="spinner" aria-hidden="true" />
        {{ submitting ? 'Guardando...' : 'Guardar' }}
      </button>
    </div>
  </form>
</template>

<style scoped lang="scss">
.student-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 800px;
  margin: 0 auto;
  padding: 1.5rem;
}

.form-header {
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 1rem;
  
  h2 {
    margin: 0;
    font-size: 1.5rem;
    color: var(--color-text-primary);
  }
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
  
  // Tablet
  @media (min-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  // Desktop
  @media (min-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
  }
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  
  label {
    font-weight: 500;
    color: var(--color-text-secondary);
    font-size: 0.875rem;
    
    span[aria-label="required"] {
      color: var(--color-error);
      margin-left: 2px;
    }
  }
  
  input {
    padding: 0.625rem 0.75rem;
    border: 1px solid var(--color-border);
    border-radius: 6px;
    font-size: 1rem;
    transition: border-color 0.15s, box-shadow 0.15s;
    
    &:focus {
      outline: none;
      border-color: var(--color-primary);
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }
    
    &[aria-invalid="true"] {
      border-color: var(--color-error);
    }
    
    &:disabled {
      background-color: var(--color-bg-disabled);
      cursor: not-allowed;
    }
  }
  
  .error {
    color: var(--color-error);
    font-size: 0.875rem;
    font-weight: 500;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  border-top: 1px solid var(--color-border);
  padding-top: 1rem;
}

.btn {
  padding: 0.625rem 1.5rem;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.15s, background-color 0.15s;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  
  &-primary {
    background: var(--color-primary);
    color: white;
    border: none;
    
    &:hover:not(:disabled) {
      background: var(--color-primary-dark);
    }
  }
  
  &-secondary {
    background: transparent;
    color: var(--color-text-primary);
    border: 1px solid var(--color-border);
    
    &:hover:not(:disabled) {
      background: var(--color-bg-hover);
    }
  }
}

.spinner {
  width: 1rem;
  height: 1rem;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// Mobile-first: minimum 44x44px touch targets
@media (max-width: 767px) {
  .btn, input {
    min-height: 44px;
  }
}
</style>
```

### Test Stub

```typescript
// __tests__/<ComponentName>.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import <ComponentName> from '../<ComponentName>.vue'

describe('<ComponentName>', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders correctly', () => {
    const wrapper = mount(<ComponentName>, {
      props: { studentId: 1 }
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('emits "saved" when form is submitted with valid data', async () => {
    const wrapper = mount(<ComponentName>)
    
    await wrapper.find('input[id="cedula"]').setValue('1712345678')
    await wrapper.find('input[id="nombres"]').setValue('Juan')
    // ... fill other fields
    
    await wrapper.find('form').trigger('submit')
    await wrapper.vm.$nextTick()
    
    expect(wrapper.emitted('saved')).toBeTruthy()
  })

  it('shows error when cedula is invalid', async () => {
    const wrapper = mount(<ComponentName>)
    
    await wrapper.find('input[id="cedula"]').setValue('123')
    await wrapper.find('input[id="cedula"]').trigger('blur')
    
    expect(wrapper.text()).toContain('Cédula debe tener 10 dígitos')
  })

  it('disables submit when loading', async () => {
    const wrapper = mount(<ComponentName>)
    // ...
  })

  it('emits "cancelled" when cancel button clicked', async () => {
    const wrapper = mount(<ComponentName>)
    await wrapper.find('button[type="button"]').trigger('click')
    expect(wrapper.emitted('cancelled')).toBeTruthy()
  })
})
```

## Workflow

1. **Read** existing components to match style
2. **Create** types/interfaces in `src/types/<domain>.ts` if not exist
3. **Generate** the component file
4. **Generate** the test file
5. **Run** `npm run type-check` and `npm run lint`
6. **Run** the dev server and visually test
7. **Run** `npm run test`

## Quality Checklist

- [ ] Uses `<script setup lang="ts">` (Composition API + TypeScript)
- [ ] Props typed with interface and `withDefaults`
- [ ] Emits typed with payloads
- [ ] No `any` types
- [ ] Loading states shown during async ops
- [ ] Error states displayed to user
- [ ] Form validation (client-side)
- [ ] Accessibility: ARIA attributes, semantic HTML, labels
- [ ] Responsive: works on mobile/tablet/desktop
- [ ] Touch targets ≥ 44x44px on mobile
- [ ] Scoped styles (no global pollution)
- [ ] CSS variables for theming
- [ ] Test file created
- [ ] Type check passes
- [ ] Lint passes

## Notes

- Spanish for user-facing text (project is Ecuador-focused)
- English for code (variables, functions)
- Use Pinia stores for shared state
- Use `useToast` composable for notifications (don't pollute components)
- Use `useApi` composable for API calls (centralized error handling)
- Always handle loading + error + success states
- Defer to `vue-frontend-developer` agent for complex interactions
