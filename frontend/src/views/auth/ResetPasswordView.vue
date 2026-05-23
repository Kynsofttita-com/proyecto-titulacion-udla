<template>
  <div class="space-y-7">
    <div>
      <h1 class="text-3xl font-bold text-ink-900 tracking-tight">Nueva contraseña</h1>
      <p class="text-ink-500 mt-2">
        Define una contraseña segura. Debe tener al menos 8 caracteres.
      </p>
    </div>

    <div v-if="!token" class="rounded-lg bg-warning-50 border border-warning-500/20 p-4 flex items-start gap-3">
      <i class="pi pi-exclamation-triangle text-warning-600 mt-0.5" />
      <div>
        <p class="text-sm font-medium text-warning-700">Enlace inválido</p>
        <p class="text-xs text-warning-700/80 mt-0.5">
          El enlace de recuperación no es válido o ha expirado. Solicita uno nuevo.
        </p>
      </div>
    </div>

    <div v-if="success" class="rounded-lg bg-success-50 border border-success-500/20 p-4 flex items-start gap-3 animate-fade-up">
      <i class="pi pi-check-circle text-success-600 mt-0.5" />
      <div>
        <p class="text-sm font-medium text-success-700">Contraseña actualizada</p>
        <p class="text-xs text-success-700/80 mt-0.5">
          Tu contraseña fue cambiada exitosamente. Ya puedes iniciar sesión.
        </p>
      </div>
    </div>

    <div v-if="error" class="rounded-lg bg-danger-50 border border-danger-500/20 p-4 flex items-start gap-3">
      <i class="pi pi-exclamation-circle text-danger-600 mt-0.5" />
      <div>
        <p class="text-sm font-medium text-danger-600">No se pudo cambiar</p>
        <p class="text-xs text-danger-600/80 mt-0.5">{{ error }}</p>
      </div>
    </div>

    <form v-if="token && !success" @submit.prevent="handleSubmit" class="space-y-5">
      <div>
        <label class="label mb-1.5 block">Nueva contraseña</label>
        <Password
          v-model="password"
          class="w-full"
          input-class="w-full"
          placeholder="••••••••"
          toggle-mask
          :feedback="true"
          required
        />
        <p v-if="password && password.length < 8" class="text-xs text-danger-600 mt-1">
          Mínimo 8 caracteres
        </p>
      </div>

      <div>
        <label class="label mb-1.5 block">Confirmar contraseña</label>
        <Password
          v-model="confirmPassword"
          class="w-full"
          input-class="w-full"
          placeholder="••••••••"
          :feedback="false"
          toggle-mask
          required
        />
        <p v-if="confirmPassword && password !== confirmPassword" class="text-xs text-danger-600 mt-1">
          Las contraseñas no coinciden
        </p>
      </div>

      <Button
        type="submit"
        :loading="isLoading"
        :disabled="isLoading || password.length < 8 || password !== confirmPassword"
        class="w-full !py-3 !text-base !font-semibold"
      >
        <span class="flex items-center gap-2">
          Cambiar contraseña
          <i class="pi pi-check text-sm" />
        </span>
      </Button>
    </form>

    <div class="pt-4 border-t border-ink-200 text-center">
      <RouterLink to="/login" class="inline-flex items-center gap-1.5 text-sm font-medium text-brand-700 hover:text-brand-800">
        <i class="pi pi-arrow-left text-xs" />
        Volver al inicio de sesión
      </RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Password from 'primevue/password'
import Button from 'primevue/button'
import api from '@/services/api'

const route = useRoute()
const router = useRouter()
const password = ref('')
const confirmPassword = ref('')
const isLoading = ref(false)
const error = ref('')
const success = ref(false)
const token = computed(() => (route.query.token as string) || '')

const handleSubmit = async () => {
  if (password.value.length < 8) {
    error.value = 'La contraseña debe tener al menos 8 caracteres'
    return
  }
  if (password.value !== confirmPassword.value) {
    error.value = 'Las contraseñas no coinciden'
    return
  }
  isLoading.value = true
  error.value = ''
  try {
    await api.post('/auth/reset-password', {
      token: token.value,
      newPassword: password.value
    })
    success.value = true
    // Redirige al login después de 3 segundos
    setTimeout(() => router.push('/login'), 3000)
  } catch (err: any) {
    error.value = err.response?.data?.detail || err.response?.data?.message || 'No se pudo cambiar la contraseña. El enlace puede haber expirado.'
  } finally {
    isLoading.value = false
  }
}
</script>
