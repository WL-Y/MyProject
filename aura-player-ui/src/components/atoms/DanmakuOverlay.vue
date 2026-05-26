<template>
  <div
    v-if="danmakuStore.enabled && danmakuStore.hasDanmaku"
    class="pointer-events-none absolute inset-0 overflow-hidden"
    style="z-index: 10;"
  >
    <div
      v-for="item in activeItems"
      :key="item.key"
      class="absolute whitespace-nowrap text-sm font-bold"
      :style="{
        top: `${item.row * 14 + 2}%`,
        color: item.color,
        textShadow: `0 0 6px ${item.color}, 0 0 12px ${item.color}44`,
        animation: `danmaku-drift ${SCROLL_DURATION}s linear forwards`,
        left: '100%',
        fontSize: item.type === 5 ? '1.1rem' : '0.8rem',
        opacity: 0.85,
      }"
    >
      {{ item.content }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { useDanmakuStore } from '@/stores/danmaku'
import { usePlayerStore } from '@/stores/player'
import type { DanmakuItem } from '@/types'

const danmakuStore = useDanmakuStore()
const playerStore = usePlayerStore()

const SCROLL_DURATION = 10
const ROWS = 6
const LOOKAHEAD = 2 // seconds ahead to pre-spawn

interface ActiveItem {
  key: string
  content: string
  color: string
  type: number
  row: number
  spawnTime: number
}

const activeItems = ref<ActiveItem[]>([])
const spawnedTimes = ref<Set<number>>(new Set())
let rowIndex = 0

function nextRow(): number {
  const r = rowIndex % ROWS
  rowIndex++
  return r
}

watch(
  () => playerStore.progress,
  (currentTime) => {
    if (!danmakuStore.enabled || !danmakuStore.hasDanmaku) {
      activeItems.value = []
      return
    }

    // Spawn new items that are within the lookahead window
    const items = danmakuStore.currentDanmaku
    for (const d of items) {
      if (d.time >= currentTime && d.time <= currentTime + LOOKAHEAD && !spawnedTimes.value.has(d.time)) {
        spawnedTimes.value.add(d.time)
        activeItems.value.push({
          key: `${d.time}-${d.content.slice(0, 8)}-${Math.random().toString(36).slice(2, 6)}`,
          content: d.content,
          color: d.color,
          type: d.type,
          row: nextRow(),
          spawnTime: d.time,
        })
      }
    }

    // Remove items that have scrolled off-screen or are too far behind
    const now = Date.now()
    const deadline = currentTime - 3 // remove items older than 3 seconds behind
    activeItems.value = activeItems.value.filter(item => {
      return item.spawnTime > deadline
    })
  }
)

// Reset when track changes
watch(
  () => playerStore.current?.bvid,
  () => {
    activeItems.value = []
    spawnedTimes.value = new Set()
    rowIndex = 0
  }
)

// Clean up old spawned times periodically to prevent memory leak
watch(
  () => playerStore.playing,
  (playing) => {
    if (!playing) {
      // When paused, keep current items but stop spawning
    }
  }
)
</script>

<style scoped>
@keyframes danmaku-drift {
  0% {
    transform: translateX(0);
    opacity: 0;
  }
  5% {
    opacity: 0.9;
  }
  90% {
    opacity: 0.9;
  }
  100% {
    transform: translateX(calc(-100vw - 100%));
    opacity: 0;
  }
}
</style>
