<template>
  <footer
    class="flex shrink-0 flex-wrap items-center justify-between gap-2 px-4 py-2"
    style="border-top: 1px solid var(--color-outline-variant); background: linear-gradient(180deg, transparent 0%, rgba(0, 240, 255, 0.03) 100%);"
  >
    <div class="flex items-center gap-3">
      <GlowDot color="primary" />
      <span class="terminal-label" :style="{ color: 'var(--color-primary)', textShadow: '0 0 8px rgba(0, 240, 255, 0.5)' }">
        AURAMUSIC v0.1.0
      </span>
    </div>
    <div class="flex items-center gap-3">
      <DanmakuToggle />
    </div>
  </footer>
</template>

<script setup lang="ts">
import GlowDot from '@/components/atoms/GlowDot.vue'
import DanmakuToggle from '@/components/atoms/DanmakuToggle.vue'
</script>
