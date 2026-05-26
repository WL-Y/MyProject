<template>
  <div class="flex items-center gap-2 px-3 py-2 transition-all duration-200" style="border: 1px solid var(--color-outline-variant); background: rgba(5, 5, 16, 0.6);">
    <span class="text-sm font-black" :style="{ fontFamily: 'var(--font-mono)', color: 'var(--color-primary)', textShadow: '0 0 8px rgba(0, 240, 255, 0.6)' }">></span>
    <input
      v-model="text"
      @keydown.enter="submit"
      :disabled="disabled"
      placeholder="type_command..."
      class="flex-1 bg-transparent text-sm outline-none placeholder:text-[var(--color-outline)]"
      :style="{ fontFamily: 'var(--font-mono)', color: 'var(--color-on-surface)' }"
    />
    <span v-if="!text && !disabled" class="terminal-cursor-block" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{ disabled?: boolean }>()
const emit = defineEmits<{ submit: [string] }>()
const text = ref('')

function submit() {
  if (text.value.trim()) {
    emit('submit', text.value)
    text.value = ''
  }
}
</script>
