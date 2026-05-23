<template>
  <div class="auth-layout">
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <h1 class="text-4xl font-bold text-center">Gestión Escuelas</h1>
          <p class="text-center text-gray-600 mt-2">de Conducción</p>
        </div>

        <RouterView />

        <div class="auth-footer text-center text-sm text-gray-500 mt-6">
          <p>&copy; 2026 Universidad de las Américas. Todos los derechos reservados.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.auth-card {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.auth-header {
  margin-bottom: 30px;
}

.auth-footer {
  border-top: 1px solid #eee;
  padding-top: 20px;
}
</style>
