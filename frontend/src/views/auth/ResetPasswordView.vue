<template>
  <div class="space-y-6">
    <div>
      <p class="text-sm text-gray-600">
        Ingresa tu nueva contraseña. Debe tener al menos 8 caracteres.
      </p>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div class="space-y-2">
        <label for="password" class="block text-sm font-semibold text-gray-700">Nueva Contraseña</label>
        <Password
          id="password"
          v-model="password"
          class="w-full"
          input-class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="••••••••"
          toggle-mask
          required
        />
        <small v-if="password && password.length < 8" class="text-red-500">
          Mínimo 8 caracteres
        </small>
      </div>

      <div class="space-y-2">
        <label for="confirmPassword" class="block text-sm font-semibold text-gray-700">Confirmar Contraseña</label>
        <Password
          id="confirmPassword"
          v-model="confirmPassword"
          class="w-full"
          input-class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="••••••••"
          :feedback="false"
          toggle-mask
          required
        />
        <small v-if="confirmPassword && password !== confirmPassword" class="text-red-500">
          Las contraseñas no coinciden
        </small>
      </div>

      <div v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-lg">
        <p class="text-sm text-red-700">{{ error }}</p>
      </div>

      <Button
        type="submit"
        label="Cambiar Contraseña"
        class="w-full py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition"
        :loading="isLoading"
        :disabled="isLoading || password.length < 8 || password !== confirmPassword"
      />
    </form>

    <RouterLink to="/login" class="text-sm text-blue-600 hover:text-blue-700 block text-center">
      ← Volver al login
    </RouterLink>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import Password from 'primevue/password'
import Button from 'primevue/button'
import api from '@/services/api'

const route = useRoute()
const password = ref('')
const confirmPassword = ref('')
const isLoading = ref(false)
const error = ref('')
const token = computed(() => route.query.token as string)

const handleSubmit = async () => {
  if (password.value.length < 8) {
    error.value = 'Contraseña debe tener al menos 8 caracteres'
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
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Error al cambiar contraseña'
  } finally {
    isLoading.value = false
  }
}
</script>
