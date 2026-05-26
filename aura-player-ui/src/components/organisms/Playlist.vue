<template>
  <div class="cyber-panel min-h-0 flex-1 overflow-hidden">
    <div class="flex items-center justify-between px-3 py-2" style="border-bottom: 1px solid var(--color-outline-variant); background: rgba(0, 240, 255, 0.02);">
      <span class="terminal-label" :style="{ color: 'var(--color-primary)', textShadow: '0 0 6px rgba(0, 240, 255, 0.4)' }">PLAYLIST</span>
      <span class="data-text" :style="{ color: 'var(--color-outline)' }">
        {{ playerStore.playlist.length }}_TRACKS
      </span>
    </div>
    <div class="overflow-y-auto" style="max-height: 300px;">
      <div
        v-for="(track, i) in playerStore.playlist"
        :key="track.id"
        @click="playerStore.playTrack(track)"
        class="flex cursor-pointer items-center gap-3 px-3 py-2 transition-all duration-200"
        :class="i === playerStore.index ? '' : ''"
        :style="i === playerStore.index
          ? { background: 'rgba(0, 240, 255, 0.08)', borderLeft: '2px solid var(--color-primary)', boxShadow: 'inset 2px 0 12px rgba(0, 240, 255, 0.1)' }
          : {}"
        @mouseenter="(e: MouseEvent) => { if (i !== playerStore.index) { const t = e.target as HTMLElement; t.style.background = 'rgba(255, 0, 255, 0.04)'; } }"
        @mouseleave="(e: MouseEvent) => { if (i !== playerStore.index) { const t = e.target as HTMLElement; t.style.background = ''; } }"
      >
        <span class="w-6 text-right text-[10px] font-bold" style="font-family: var(--font-mono);"
          :style="i === playerStore.index && playerStore.playing
            ? { color: 'var(--color-primary)', textShadow: '0 0 8px rgba(0, 240, 255, 0.7)' }
            : { color: 'var(--color-outline)' }"
        >
          {{ i === playerStore.index && playerStore.playing ? '\u25B6' : String(i + 1).padStart(2, '0') }}
        </span>
        <div class="min-w-0 flex-1">
          <p class="truncate text-xs font-semibold" style="font-family: var(--font-body);">{{ track.title }}</p>
          <p class="truncate text-[10px]" style="font-family: var(--font-mono); color: var(--color-outline);">{{ track.author }}</p>
        </div>
        <button
          @click.stop="playerStore.removeTrack(track.id)"
          class="transition-all duration-200"
          style="color: var(--color-outline);"
          @mouseenter="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.color = 'var(--color-error)'; t.style.textShadow = '0 0 8px rgba(255, 51, 102, 0.6)'; }"
          @mouseleave="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.color = 'var(--color-outline)'; t.style.textShadow = ''; }"
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
        </button>
      </div>
      <p v-if="playerStore.playlist.length === 0" class="px-3 py-4 text-center text-xs" style="font-family: var(--font-mono); color: var(--color-outline);">
        NO_TRACKS_LOADED
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { usePlayerStore } from '@/stores/player'
const playerStore = usePlayerStore()
</script>
