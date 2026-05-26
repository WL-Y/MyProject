<template>
  <span
    class="inline-block h-2.5 w-2.5 rounded-full"
    :class="color === 'primary' ? '' : ''"
    :style="color === 'primary'
      ? { background: 'var(--color-primary)', boxShadow: '0 0 12px rgba(0, 240, 255, 0.8), 0 0 30px rgba(0, 240, 255, 0.3)', animation: 'pulse 1.8s ease-in-out infinite' }
      : color === 'secondary'
        ? { background: 'var(--color-secondary)', boxShadow: '0 0 12px rgba(255, 0, 255, 0.8), 0 0 30px rgba(255, 0, 255, 0.3)', animation: 'pulse 2.0s ease-in-out infinite' }
        : { background: 'var(--color-outline)', opacity: 0.5 }
    "
  />
</template>

<script setup lang="ts">
defineProps<{
  color?: 'primary' | 'secondary' | 'default'
}>()
</script>
