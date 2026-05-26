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
        left: '100%',
        color: item.color,
        textShadow: `0 0 6px ${item.color}`,
        fontSize: item.type === 5 ? '1.1rem' : '0.8rem',
        opacity: item.visible ? 1 : 0,
        transition: 'opacity 0.3s',
        animation: `danmaku-fly ${SCROLL_DURATION}s linear forwards`,
      }"
      @animationend="onAnimationEnd(item.key)"
    >
      {{ item.content }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useDanmakuStore } from '@/stores/danmaku'
import { usePlayerStore } from '@/stores/player'

const danmakuStore = useDanmakuStore()
const playerStore = usePlayerStore()

const SCROLL_DURATION = 15
const ROWS = 8

interface ActiveItem {
  key: string
  content: string
  color: string
  type: number
  row: number
  visible: boolean
}

const activeItems = ref<ActiveItem[]>([])
let rowIndex = 0

function nextRow(): number {
  const r = rowIndex % ROWS
  rowIndex++
  return r
}

function onAnimationEnd(key: string) {
  activeItems.value = activeItems.value.filter(i => i.key !== key)
}

watch(
  () => playerStore.progress,
  (currentTime) => {
    if (!danmakuStore.enabled || !danmakuStore.hasDanmaku) {
      activeItems.value = []
      return
    }

    const items = danmakuStore.currentDanmaku
    for (const d of items) {
      if (d.time >= currentTime && d.time <= currentTime + 2) {
        const key = `${d.time.toFixed(3)}-${d.content}`
        if (!activeItems.value.find(a => a.key === key)) {
          activeItems.value.push({
            key,
            content: d.content,
            color: d.color,
            type: d.type,
            row: nextRow(),
            visible: true,
          })
        }
      }
    }
  }
)

watch(
  () => playerStore.current?.bvid,
  () => {
    activeItems.value = []
    rowIndex = 0
  }
)
</script>

<style>
@keyframes danmaku-fly {
  0% {
    transform: translateX(0);
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateX(calc(-100vw - 100%));
    opacity: 0;
  }
}
</style>
