<template>
  <section class="card animate-fade-up">
    <header v-if="title || $slots.header" class="px-6 py-5 border-b border-ink-200">
      <slot name="header">
        <div class="flex items-center gap-3">
          <div v-if="icon" class="w-10 h-10 rounded-lg bg-brand-50 text-brand-700 flex items-center justify-center">
            <i :class="icon" />
          </div>
          <div>
            <h3 class="heading-3">{{ title }}</h3>
            <p v-if="description" class="muted text-xs mt-0.5">{{ description }}</p>
          </div>
        </div>
      </slot>
    </header>

    <div class="p-6">
      <slot />
    </div>

    <footer v-if="$slots.actions" class="px-6 py-4 border-t border-ink-200 bg-ink-50/50 flex items-center justify-end gap-3">
      <slot name="actions" />
    </footer>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
  icon?: string
}>()
</script>
