<template>
  <div class="flex items-center gap-2">
    <svg width="16" height="16" viewBox="0 0 24 24" fill="var(--color-primary)" style="filter: drop-shadow(0 0 4px rgba(0, 240, 255, 0.5));">
      <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"/>
    </svg>
    <input
      type="range"
      min="0"
      max="1"
      step="0.01"
      :value="volume"
      @input="$emit('change', Number(($event.target as HTMLInputElement).value))"
      class="h-1 w-20 cursor-pointer appearance-none"
      style="background: linear-gradient(90deg, var(--color-primary), var(--color-secondary)); border-radius: 2px; accent-color: var(--color-primary);"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{ volume: number }>()
defineEmits<{ change: [number] }>()
</script>
