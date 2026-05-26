<template>
  <div
    class="cyber-panel flex items-center justify-between px-4 py-2"
  >
    <span class="text-xl font-bold tracking-[0.15em]" :style="{ fontFamily: 'var(--font-mono)', color: 'var(--color-primary)', textShadow: '0 0 12px rgba(0, 240, 255, 0.7), 0 0 30px rgba(0, 240, 255, 0.3)' }">
      {{ hours }}<span class="colon-blink" :style="{ color: 'var(--color-secondary)', textShadow: '0 0 8px rgba(255, 0, 255, 0.6)' }">:</span>{{ minutes }}
    </span>
    <span class="text-xs font-bold tracking-[0.15em]" :style="{ fontFamily: 'var(--font-mono)', color: 'var(--color-outline)' }">
      {{ date }}
    </span>
  </div>
</template>

<script setup lang="ts">
import { useClock } from '@/composables/useClock'
const { hours, minutes, date } = useClock()
</script>
