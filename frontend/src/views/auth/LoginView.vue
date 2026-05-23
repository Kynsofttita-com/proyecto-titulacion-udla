<template>
  <form @submit.prevent="handleLogin" class="space-y-4">
    <div>
      <label for="email" class="block text-sm font-medium mb-1">Email</label>
      <InputText
        id="email"
        v-model="email"
        type="email"
        class="w-full"
        placeholder="tu@email.com"
        required
      />
    </div>

    <div>
      <label for="password" class="block text-sm font-medium mb-1">Contraseña</label>
      <Password
        id="password"
        v-model="password"
        class="w-full"
        placeholder="••••••••"
        :feedback="false"
        required
      />
    </div>

    <Button type="submit" label="Iniciar Sesión" class="w-full" :loading="isLoading" />

    <div class="text-center space-y-2">
      <RouterLink to="/forgot-password" class="text-sm text-blue-600 hover:underline block">
        ¿Olvidaste tu contraseña?
      </RouterLink>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
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
const isLoading = ref(false)

const handleLogin = async () => {
  isLoading.value = true
  try {
    await authStore.login(email.value, password.value)
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch (error) {
    console.error('Login failed:', error)
  } finally {
    isLoading.value = false
  }
}
</script>
