<template>
  <div class="flex items-center gap-3">
    <button @click="$emit('prev')" class="transition-all duration-200" style="color: var(--color-outline);"
      @mouseenter="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.color = 'var(--color-primary)'; t.style.textShadow = '0 0 10px rgba(0, 240, 255, 0.6)'; t.style.transform = 'scale(1.1)'; }"
      @mouseleave="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.color = 'var(--color-outline)'; t.style.textShadow = ''; t.style.transform = ''; }">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M6 6h2v12H6zm3.5 6 8.5 6V6z"/></svg>
    </button>
    <button @click="$emit('toggle')"
      class="flex h-11 w-11 items-center justify-center transition-all duration-200"
      style="border: 1px solid var(--color-primary); color: var(--color-primary); box-shadow: 0 0 16px rgba(0, 240, 255, 0.4);"
      @mouseenter="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.background = 'rgba(0, 240, 255, 0.15)'; t.style.boxShadow = '0 0 30px rgba(0, 240, 255, 0.7), 0 0 60px rgba(0, 240, 255, 0.3)'; }"
      @mouseleave="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.background = ''; t.style.boxShadow = '0 0 16px rgba(0, 240, 255, 0.4)'; }">
      <svg v-if="!playing" width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
      <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/></svg>
    </button>
    <button @click="$emit('next')" class="transition-all duration-200" style="color: var(--color-outline);"
      @mouseenter="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.color = 'var(--color-primary)'; t.style.textShadow = '0 0 10px rgba(0, 240, 255, 0.6)'; t.style.transform = 'scale(1.1)'; }"
      @mouseleave="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.color = 'var(--color-outline)'; t.style.textShadow = ''; t.style.transform = ''; }">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"/></svg>
    </button>
    <button @click="$emit('stop')" class="transition-all duration-200" style="color: var(--color-outline);"
      @mouseenter="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.color = 'var(--color-error)'; t.style.textShadow = '0 0 10px rgba(255, 51, 102, 0.6)'; }"
      @mouseleave="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.color = 'var(--color-outline)'; t.style.textShadow = ''; }">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="6" width="12" height="12"/></svg>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{ playing: boolean }>()
defineEmits(['prev', 'toggle', 'next', 'stop'])
</script>
