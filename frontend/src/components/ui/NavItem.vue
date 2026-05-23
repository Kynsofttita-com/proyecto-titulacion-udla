<template>
  <router-link
    :to="to"
    v-slot="{ isActive, href, navigate }"
    custom
  >
    <a
      :href="href"
      @click="navigate"
      :class="[
        'group flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all',
        isActive
          ? 'bg-brand-50 text-brand-700 shadow-sm'
          : 'text-ink-600 hover:bg-ink-100 hover:text-ink-900'
      ]"
    >
      <i
        :class="[
          'pi text-base',
          icon,
          isActive ? 'text-brand-700' : 'text-ink-500 group-hover:text-ink-700'
        ]"
      />
      <span class="flex-1">{{ label }}</span>
      <span
        v-if="badge"
        class="px-1.5 py-0.5 text-[10px] font-semibold rounded-md bg-brand-600 text-white"
      >{{ badge }}</span>
      <span
        v-if="isActive"
        class="w-1 h-5 rounded-full bg-brand-600"
      />
    </a>
  </router-link>
</template>

<script setup lang="ts">
defineProps<{
  to: string
  icon: string
  label: string
  badge?: string | number
}>()
</script>
