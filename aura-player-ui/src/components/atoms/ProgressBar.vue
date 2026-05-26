<template>
  <div class="relative h-[3px] w-full overflow-hidden rounded" style="background: rgba(13, 13, 31, 0.8); border: 1px solid var(--color-outline-variant);">
    <div
      class="absolute top-0 left-0 h-full transition-all duration-150"
      :style="{
        width: `${percent}%`,
        background: 'linear-gradient(90deg, var(--color-primary), var(--color-secondary))',
        boxShadow: '0 0 12px rgba(0, 240, 255, 0.7), 0 0 24px rgba(255, 0, 255, 0.3)',
      }"
    />
    <div
      v-if="playing"
      class="absolute top-1/2 -translate-y-1/2 h-3 w-3 rounded-full"
      :style="{
        left: `calc(${percent}% - 6px)`,
        background: 'var(--color-primary)',
        boxShadow: '0 0 16px rgba(0, 240, 255, 0.9), 0 0 32px rgba(0, 240, 255, 0.5), 0 0 48px rgba(255, 0, 255, 0.3)',
      }"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  percent: number
  playing?: boolean
}>()
</script>
