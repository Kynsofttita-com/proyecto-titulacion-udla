<template>
  <div class="flex flex-col gap-4 mb-6 animate-fade-up">
    <nav v-if="breadcrumbs?.length" class="flex items-center gap-2 text-sm text-ink-500">
      <i class="pi pi-home text-xs" />
      <template v-for="(item, idx) in breadcrumbs" :key="idx">
        <i v-if="idx > 0" class="pi pi-angle-right text-[10px] text-ink-400" />
        <router-link
          v-if="item.to && idx !== breadcrumbs.length - 1"
          :to="item.to"
          class="hover:text-brand-700 transition-colors"
        >{{ item.label }}</router-link>
        <span v-else class="text-ink-700 font-medium">{{ item.label }}</span>
      </template>
    </nav>

    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
      <div class="flex items-start gap-4">
        <div v-if="icon" class="flex-shrink-0 w-12 h-12 rounded-xl bg-brand-50 text-brand-700 flex items-center justify-center">
          <i :class="icon" class="text-xl" />
        </div>
        <div>
          <h1 class="heading-1">{{ title }}</h1>
          <p v-if="description" class="muted mt-1">{{ description }}</p>
        </div>
      </div>
      <div v-if="$slots.actions" class="flex items-center gap-2 flex-wrap">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Crumb { label: string; to?: string }
defineProps<{
  title: string
  description?: string
  icon?: string
  breadcrumbs?: Crumb[]
}>()
</script>
