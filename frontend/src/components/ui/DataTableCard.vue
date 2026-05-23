<template>
  <section class="card animate-fade-up overflow-hidden">
    <header
      v-if="$slots.toolbar || title"
      class="px-6 py-4 border-b border-ink-200 flex flex-col md:flex-row md:items-center md:justify-between gap-3"
    >
      <div>
        <h3 v-if="title" class="heading-3">{{ title }}</h3>
        <p v-if="description" class="muted text-xs mt-1">{{ description }}</p>
      </div>
      <div class="flex items-center gap-2 flex-wrap">
        <slot name="toolbar" />
      </div>
    </header>

    <div>
      <slot />
    </div>

    <footer v-if="$slots.footer" class="px-6 py-3 border-t border-ink-200 bg-ink-50/50">
      <slot name="footer" />
    </footer>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
}>()
</script>
