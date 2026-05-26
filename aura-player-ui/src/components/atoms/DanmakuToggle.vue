<template>
  <button
    @click="danmakuStore.toggleDanmaku()"
    class="terminal-label transition-all duration-200"
    :style="{
      fontFamily: 'var(--font-headline)',
      color: danmakuStore.enabled ? 'var(--color-primary)' : 'var(--color-outline)',
      textShadow: danmakuStore.enabled ? '0 0 10px rgba(0, 240, 255, 0.6)' : '',
    }"
  >
    {{ danmakuStore.enabled ? 'DANMAKU_ON' : 'DANMAKU_OFF' }}
  </button>
</template>

<script setup lang="ts">
import { useDanmakuStore } from '@/stores/danmaku'
const danmakuStore = useDanmakuStore()
</script>
