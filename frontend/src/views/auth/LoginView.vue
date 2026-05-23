<template>
  <form @submit.prevent="handleLogin" class="space-y-6">
    <div class="space-y-2">
      <label for="email" class="block text-sm font-semibold text-gray-700">Email</label>
      <InputText
        id="email"
        v-model="email"
        type="email"
        class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        placeholder="tu@email.com"
        required
      />
      <small v-if="errors.email" class="text-red-500">{{ errors.email }}</small>
    </div>

    <div class="space-y-2">
      <label for="password" class="block text-sm font-semibold text-gray-700">Contraseña</label>
      <Password
        id="password"
        v-model="password"
        class="w-full"
        input-class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        placeholder="••••••••"
        :feedback="false"
        toggle-mask
        required
      />
      <small v-if="errors.password" class="text-red-500">{{ errors.password }}</small>
    </div>

    <div v-if="authStore.error" class="p-4 bg-red-50 border border-red-200 rounded-lg">
      <p class="text-sm text-red-700">{{ authStore.error }}</p>
    </div>

    <Button
      type="submit"
      label="Iniciar Sesión"
      class="w-full py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition"
      :loading="authStore.isLoading"
      :disabled="authStore.isLoading"
    />

    <div class="text-center space-y-3">
      <RouterLink to="/forgot-password" class="text-sm text-blue-600 hover:text-blue-700 block">
        ¿Olvidaste tu contraseña?
      </RouterLink>
      <p class="text-sm text-gray-600">
        Demo: admin@escuela.com / password123
      </p>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const errors = reactive({
  email: '',
  password: ''
})

const validateForm = () => {
  errors.email = ''
  errors.password = ''

  if (!email.value) errors.email = 'Email es requerido'
  if (!password.value) errors.password = 'Contraseña es requerida'

  return !errors.email && !errors.password
}

const handleLogin = async () => {
  if (!validateForm()) return

  try {
    await authStore.login(email.value, password.value)
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch (error) {
    if (error instanceof Error) {
      authStore.error = error.message
    }
  }
}
</script>
