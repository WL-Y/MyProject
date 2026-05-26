<template>
  <section
    class="cyber-panel flex h-full min-h-[min(420px,70vh)] w-full flex-1 flex-col overflow-hidden"
  >
    <!-- Glitch header bar -->
    <div class="h-[1px] shrink-0" style="background: linear-gradient(90deg, transparent, var(--color-primary), var(--color-secondary), transparent);" />

    <header
      class="flex shrink-0 flex-wrap items-center gap-x-3 gap-y-2 px-3 py-2.5"
      style="border-bottom: 1px solid var(--color-outline-variant); background: rgba(0, 240, 255, 0.02);"
    >
      <GlowDot color="primary" />
      <span class="terminal-label" :style="{ color: 'var(--color-primary)', textShadow: '0 0 8px rgba(0, 240, 255, 0.5)' }">
        NEURAL_AGENT
      </span>
      <div class="ml-auto flex flex-wrap items-center gap-2">
        <Badge :label="agentStore.loading ? 'PROCESSING' : 'STANDBY'" :variant="agentStore.loading ? 'primary' : 'default'" />
        <Badge :label="agentStore.sessionId ? 'SESSION_OK' : 'NO_SESSION'" :variant="agentStore.sessionId ? 'secondary' : 'default'" />
      </div>
    </header>

    <div ref="listRef" class="min-h-0 flex-1 overflow-y-auto px-2 py-3">
      <template v-if="agentStore.messages.length === 0 && !agentStore.loading">
        <p class="px-2 text-center text-sm opacity-55" :style="{ fontFamily: 'var(--font-body)' }">
          Awaiting operator input...
        </p>
      </template>
      <template v-else>
        <ChatMessageComp v-for="m in agentStore.messages" :key="m.id" :message="m" />
        <article v-if="showThinking" class="mb-2 flex w-full justify-start">
          <div class="border-l-[3px] pl-4 pr-4 pt-3 pb-3" style="border-left-color: var(--color-primary); box-shadow: inset 3px 0 12px rgba(0, 240, 255, 0.15);">
            <span class="terminal-label" :style="{ color: 'var(--color-primary)', textShadow: '0 0 6px rgba(0, 240, 255, 0.4)' }">AGENT_01</span>
            <span class="ml-2 text-[10px] uppercase tracking-[0.15em]" style="font-family: var(--font-mono); color: var(--color-outline);">thinking</span>
            <div class="mt-2 flex items-center gap-1.5">
              <span v-for="i in 3" :key="i" class="inline-block h-2 w-2 rounded-full" :style="{ background: 'var(--color-primary)', boxShadow: '0 0 8px var(--color-primary)', animation: `thinking-dot 1.4s ease-in-out ${(i-1) * 0.2}s infinite` }" />
            </div>
          </div>
        </article>
      </template>
    </div>

    <div class="shrink-0 px-3 py-3" style="border-top: 1px solid var(--color-outline-variant); background: rgba(0, 0, 0, 0.2);">
      <div class="flex items-center gap-2">
        <div class="min-w-0 flex-1">
          <CommandInput :disabled="agentStore.loading" @submit="agentStore.sendMessage($event)" />
        </div>
        <button
          v-if="agentStore.loading"
          @click="agentStore.cancel()"
          class="flex h-9 w-9 shrink-0 items-center justify-center transition-all duration-200"
          style="border: 1px solid var(--color-outline-variant); background: rgba(0,0,0,0.3);"
          @mouseenter="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.borderColor = 'var(--color-error)'; t.style.color = 'var(--color-error)'; t.style.boxShadow = '0 0 12px rgba(255, 51, 102, 0.5)'; }"
          @mouseleave="(e: MouseEvent) => { const t = e.target as HTMLElement; t.style.borderColor = 'var(--color-outline-variant)'; t.style.color = 'var(--color-outline)'; t.style.boxShadow = ''; }"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="6" width="12" height="12" rx="1"/></svg>
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useAgentStore } from '@/stores/agent'
import Badge from '@/components/atoms/Badge.vue'
import GlowDot from '@/components/atoms/GlowDot.vue'
import ChatMessageComp from '@/components/molecules/ChatMessage.vue'
import CommandInput from '@/components/molecules/CommandInput.vue'

const agentStore = useAgentStore()
const listRef = ref<HTMLDivElement | null>(null)

const showThinking = computed(() =>
  agentStore.loading && (agentStore.messages.length === 0 || agentStore.messages[agentStore.messages.length - 1].role !== 'agent')
)

watch(() => agentStore.messages.length, () => {
  nextTick(() => {
    if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
  })
})
</script>
