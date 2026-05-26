<template>
  <span
    class="inline-flex items-center px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-[0.15em]"
    :style="variant === 'primary'
      ? {
          fontFamily: 'var(--font-headline)',
          background: 'rgba(0, 240, 255, 0.15)',
          border: '1px solid var(--color-primary)',
          color: 'var(--color-primary)',
          boxShadow: '0 0 10px rgba(0, 240, 255, 0.3), inset 0 0 6px rgba(0, 240, 255, 0.1)',
          textShadow: '0 0 6px rgba(0, 240, 255, 0.6)',
        }
      : variant === 'secondary'
        ? {
            fontFamily: 'var(--font-headline)',
            background: 'rgba(255, 0, 255, 0.1)',
            border: '1px solid var(--color-secondary)',
            color: 'var(--color-secondary)',
            boxShadow: '0 0 10px rgba(255, 0, 255, 0.3), inset 0 0 6px rgba(255, 0, 255, 0.1)',
            textShadow: '0 0 6px rgba(255, 0, 255, 0.6)',
          }
        : {
            fontFamily: 'var(--font-headline)',
            background: 'rgba(107, 107, 159, 0.1)',
            border: '1px solid var(--color-outline-variant)',
            color: 'var(--color-outline)',
          }
    "
  >
    {{ label }}
  </span>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  variant?: 'default' | 'primary' | 'secondary'
}>()
</script>
