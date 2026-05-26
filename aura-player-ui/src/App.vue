<template>
  <div class="dot-matrix-bg scanline-overlay flex min-h-[100dvh] items-center justify-center p-3 text-[var(--color-on-surface)] md:p-6 lg:p-8" style="position: relative;">
    <!-- Data rain particles -->
    <div ref="rainRef" class="data-rain-container" aria-hidden="true" />

    <!-- Main panel -->
    <div
      class="crt-glow flex h-[min(94dvh,56rem)] w-full max-w-7xl flex-col overflow-hidden"
      style="position: relative; z-index: 1; border: 1px solid var(--color-outline-variant); background-color: rgba(5, 5, 16, 0.94); backdrop-filter: blur(4px); box-shadow: 0 0 60px rgba(0, 240, 255, 0.08), 0 0 120px rgba(255, 0, 255, 0.04), inset 0 0 120px rgba(0, 240, 255, 0.04);"
    >
      <!-- Top neon accent line -->
      <div class="h-[2px] shrink-0" style="background: linear-gradient(90deg, transparent, var(--color-primary), var(--color-secondary), var(--color-tertiary), transparent); box-shadow: 0 0 12px var(--color-primary), 0 0 24px var(--color-secondary);" />

      <header
        class="flex shrink-0 flex-wrap items-center justify-between gap-4 px-4 py-3 md:px-6"
        style="border-bottom: 1px solid var(--color-outline-variant); background: linear-gradient(180deg, rgba(0, 240, 255, 0.04) 0%, transparent 100%);"
      >
        <Logo />
      </header>

      <main class="flex min-h-0 flex-1 flex-col gap-4 overflow-hidden p-4 md:grid md:grid-cols-2 md:gap-6 md:p-6">
        <div class="relative flex min-h-0 min-w-0 flex-1 flex-col gap-4 overflow-hidden" style="z-index: 2;">
          <DanmakuOverlay />
          <ClockPanel />
          <Player />
          <Playlist />
        </div>
        <div class="flex min-h-0 min-w-0 flex-1" style="z-index: 2;">
          <AgentChat />
        </div>
      </main>

      <StatusBar />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import Logo from '@/components/atoms/Logo.vue'
import DanmakuOverlay from '@/components/atoms/DanmakuOverlay.vue'
import Player from '@/components/organisms/Player.vue'
import Playlist from '@/components/organisms/Playlist.vue'
import AgentChat from '@/components/organisms/AgentChat.vue'
import ClockPanel from '@/components/organisms/ClockPanel.vue'
import StatusBar from '@/components/organisms/StatusBar.vue'

// === DATA RAIN EFFECT ===
const rainRef = ref<HTMLDivElement | null>(null)
let rainTimer: ReturnType<typeof setInterval> | null = null
const DROPS = 40

function spawnRainDrop() {
  if (!rainRef.value) return
  const el = document.createElement('span')
  el.className = 'data-drop'
  el.textContent = Math.random() > 0.5
    ? Math.random().toString(2).slice(2, 8)
    : String.fromCharCode(0x30A0 + Math.random() * 96)
  el.style.left = Math.random() * 100 + '%'
  el.style.top = '-2%'
  el.style.animationDuration = (4 + Math.random() * 6) + 's'
  el.style.animationDelay = Math.random() * 2 + 's'
  rainRef.value.appendChild(el)

  el.addEventListener('animationend', () => el.remove(), { once: true })
}

onMounted(() => {
  // Initial burst
  for (let i = 0; i < DROPS; i++) {
    setTimeout(() => spawnRainDrop(), Math.random() * 2000)
  }
  // Continuous drip
  rainTimer = setInterval(() => {
    const current = rainRef.value?.children.length ?? 0
    if (current < DROPS) spawnRainDrop()
  }, 400)
})

onUnmounted(() => {
  if (rainTimer) clearInterval(rainTimer)
})
</script>
