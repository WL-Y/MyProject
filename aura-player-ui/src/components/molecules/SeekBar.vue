<template>
  <div class="flex items-center gap-3">
    <span class="w-10 text-right text-[10px] font-bold" style="font-family: var(--font-mono); color: var(--color-primary); text-shadow: 0 0 6px rgba(0, 240, 255, 0.4);">
      {{ formatTime(progress) }}
    </span>
    <div class="flex-1 cursor-pointer" @click="handleClick">
      <ProgressBar :percent="duration > 0 ? (progress / duration) * 100 : 0" :playing="playing" />
    </div>
    <span class="w-10 text-[10px] font-bold" style="font-family: var(--font-mono); color: var(--color-outline);">
      {{ formatTime(duration) }}
    </span>
  </div>
</template>

<script setup lang="ts">
import ProgressBar from '@/components/atoms/ProgressBar.vue'

const props = defineProps<{
  progress: number
  duration: number
  playing: boolean
}>()
const emit = defineEmits<{ seek: [number] }>()

function formatTime(s: number): string {
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}:${sec.toString().padStart(2, '0')}`
}

function handleClick(e: MouseEvent) {
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
  const percent = (e.clientX - rect.left) / rect.width
  emit('seek', percent * props.duration)
}
</script>
