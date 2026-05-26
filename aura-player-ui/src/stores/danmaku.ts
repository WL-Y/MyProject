import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { DanmakuItem } from '@/types'
import { usePlayerStore } from './player'
import { getDanmaku } from '@/api/bili'

export const useDanmakuStore = defineStore('danmaku', () => {
  const enabled = ref(false)
  const danmakuMap = ref<Record<string, DanmakuItem[]>>({})

  const playerStore = usePlayerStore()

  const currentDanmaku = computed(() => {
    const bvid = playerStore.current?.bvid
    return bvid ? danmakuMap.value[bvid] ?? [] : []
  })

  const hasDanmaku = computed(() => currentDanmaku.value.length > 0)

  function toggleDanmaku() {
    enabled.value = !enabled.value
  }

  async function fetchDanmaku(bvid: string) {
    if (danmakuMap.value[bvid]) return
    try {
      const res = await getDanmaku(bvid)
      if (res.danmaku?.length) {
        danmakuMap.value[bvid] = res.danmaku
      }
    } catch { /* ignore */ }
  }

  watch(() => playerStore.current?.bvid, (bvid) => {
    if (bvid) fetchDanmaku(bvid)
  })

  return { enabled, currentDanmaku, hasDanmaku, toggleDanmaku, fetchDanmaku }
})
