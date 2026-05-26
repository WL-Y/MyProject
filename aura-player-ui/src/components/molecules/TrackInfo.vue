<template>
  <div class="flex items-center gap-4">
    <div class="flex h-12 w-12 items-center justify-center" style="border: 1px solid var(--color-outline-variant); background: rgba(0, 0, 0, 0.3);">
      <GlowDot :color="playing ? 'primary' : 'default'" />
    </div>
    <div class="min-w-0 flex-1">
      <p class="truncate text-sm font-bold" :style="{ fontFamily: 'var(--font-headline)', color: playing ? 'var(--color-primary)' : 'var(--color-on-surface)', textShadow: playing ? '0 0 8px rgba(0, 240, 255, 0.5)' : '' }">
        {{ track?.title ?? 'NO_TRACK' }}
      </p>
      <p class="truncate text-xs font-medium" style="font-family: var(--font-mono); color: var(--color-outline);">
        {{ track?.author ?? 'UNKNOWN_ARTIST' }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import GlowDot from '@/components/atoms/GlowDot.vue'
import type { Track } from '@/types'

defineProps<{
  track: Track | null
  playing: boolean
}>()
</script>
