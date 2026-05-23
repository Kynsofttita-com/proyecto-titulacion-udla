<template>
  <div class="min-h-screen flex">
    <!-- Panel izquierdo: branding -->
    <aside class="hidden lg:flex lg:w-1/2 relative overflow-hidden bg-gradient-to-br from-brand-900 via-brand-800 to-brand-700">
      <!-- Decorative blobs -->
      <div class="absolute -top-32 -left-32 w-96 h-96 rounded-full bg-brand-500/30 blur-3xl" />
      <div class="absolute -bottom-32 -right-32 w-[500px] h-[500px] rounded-full bg-brand-400/20 blur-3xl" />
      <div class="absolute top-1/3 right-10 w-64 h-64 rounded-full bg-info-500/20 blur-3xl" />

      <div class="relative z-10 flex flex-col justify-between p-12 text-white w-full">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 rounded-xl bg-white/10 backdrop-blur-sm flex items-center justify-center border border-white/20">
            <i class="pi pi-car text-xl" />
          </div>
          <div>
            <p class="font-bold text-lg leading-tight">Escuela de Conducción</p>
            <p class="text-xs text-white/70">Panel Administrativo</p>
          </div>
        </div>

        <div class="space-y-5">
          <h2 class="text-5xl font-bold leading-tight tracking-tight">
            Gestiona tu escuela<br />
            con <span class="text-brand-200">eficiencia</span>.
          </h2>
          <p class="text-lg text-white/70 max-w-md">
            Sistema integral para administrar estudiantes, instructores,
            vehículos, asignaciones y cobros desde un solo lugar.
          </p>
          <div class="grid grid-cols-3 gap-4 pt-6 max-w-md">
            <Feature icon="pi-users"     label="Estudiantes" />
            <Feature icon="pi-id-card"   label="Instructores" />
            <Feature icon="pi-car"       label="Flota" />
            <Feature icon="pi-calendar"  label="Clases" />
            <Feature icon="pi-wallet"    label="Cobros" />
            <Feature icon="pi-chart-bar" label="Reportes" />
          </div>
        </div>

        <p class="text-xs text-white/50">
          © 2026 Universidad de las Américas — Tesis de Titulación
        </p>
      </div>
    </aside>

    <!-- Panel derecho: formulario -->
    <main class="flex-1 flex items-center justify-center p-6 lg:p-12 bg-white">
      <div class="w-full max-w-md">
        <div class="lg:hidden flex items-center gap-3 mb-8">
          <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-brand-600 to-brand-800 flex items-center justify-center">
            <i class="pi pi-car text-white" />
          </div>
          <div>
            <p class="font-bold text-ink-900">Escuela de Conducción</p>
            <p class="text-xs text-ink-500">Panel Administrativo</p>
          </div>
        </div>
        <RouterView />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { defineComponent, h } from 'vue'

const Feature = defineComponent({
  props: ['icon', 'label'],
  setup(props) {
    return () =>
      h('div', { class: 'flex flex-col items-center gap-1.5 p-3 rounded-lg bg-white/5 border border-white/10' }, [
        h('i', { class: `pi ${props.icon} text-lg text-brand-200` }),
        h('span', { class: 'text-xs text-white/80' }, props.label)
      ])
  }
})
</script>
