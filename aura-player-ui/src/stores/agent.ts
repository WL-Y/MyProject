import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import type { ChatMessage } from '@/types'
import { streamChat } from '@/api/chat'
import { useModeStore } from './mode'

function newId(): string {
  return crypto.randomUUID?.() ?? `m-${Date.now()}-${Math.random().toString(36).slice(2)}`
}

export const useAgentStore = defineStore('agent', () => {
  const messages = ref<ChatMessage[]>([])
  const loading = ref(false)
  const sessionId = ref<string | null>(null)
  const convertQueue = ref<string[]>([])
  const convertingSet = reactive(new Set<string>())
  const convertedSet = reactive(new Set<string>())

  let cancelFn: (() => void) | null = null
  const historyRef = ref<Array<{ role: string; content: string }>>([])

  function appendFromPayload(data: unknown) {
    if (!data || typeof data !== 'object') return
    const d = data as Record<string, unknown>
    const ts = Date.now()

    if (typeof d.session_id === 'string' && d.session_id) {
      sessionId.value ??= d.session_id
    }

    const t = d.type

    if (t === 'assistant') {
      const msg = d.message as Record<string, unknown> | undefined
      const content = msg?.content
      if (!Array.isArray(content)) return
      for (const block of content as Array<Record<string, unknown>>) {
        if (block.type === 'text' && typeof block.text === 'string' && block.text.trim()) {
          messages.value.push({ id: newId(), role: 'agent', content: block.text, timestamp: ts })
        } else if (block.type === 'tool_use' && typeof block.name === 'string') {
          let summary = `Tool: ${block.name}`
          if (block.input !== undefined) {
            try { summary += `\n${JSON.stringify(block.input).slice(0, 480)}` } catch { summary += '\n[input]' }
          }
          messages.value.push({ id: newId(), role: 'tool', content: summary, timestamp: ts, toolName: block.name })
        }
      }
      return
    }

    if (t === 'tool_call') {
      const name = (typeof d.name === 'string' && d.name) || (typeof d.tool === 'string' && d.tool) || 'tool'
      let body = typeof d.arguments === 'string' ? d.arguments : d.input !== undefined ? JSON.stringify(d.input) : ''
      if (!body.trim()) body = '{}'
      messages.value.push({ id: newId(), role: 'tool', content: `${name}\n${body.slice(0, 512)}`, timestamp: ts, toolName: name })
      return
    }

    if (t === 'result' && d.subtype === 'success' && typeof d.result === 'string') {
      const text = d.result.trim()
      if (text.length) {
        const lastAgent = [...messages.value].reverse().find(m => m.role === 'agent')
        if (!(lastAgent && lastAgent.content === text)) {
          messages.value.push({ id: newId(), role: 'agent', content: text, timestamp: ts })
        }
      }
    }
  }

  function sendMessage(text: string) {
    const trimmed = text.trim()
    if (!trimmed) return
    const ts = Date.now()

    messages.value.push({ id: newId(), role: 'operator', content: trimmed, timestamp: ts })
    historyRef.value = messages.value
      .filter(m => m.role === 'agent' || m.role === 'operator')
      .slice(-30)
      .map(m => ({ role: m.role, content: m.content }))

    loading.value = true

    const modeStore = useModeStore()
    cancelFn = streamChat(
      { message: trimmed, mode: modeStore.mode, history: historyRef.value },
      (event, data) => {
        if (event === 'output') {
          appendFromPayload(data)
        } else if (event === 'error') {
          const err = typeof data === 'string' ? data : JSON.stringify(data ?? 'error')
          messages.value.push({ id: newId(), role: 'system', content: err, timestamp: Date.now() })
        } else if (event === 'done') {
          loading.value = false
          flush()
        }
      }
    )
  }

  function queueConvert(bvids: string[]) {
    const existing = new Set([...convertQueue.value, ...Array.from(convertingSet), ...Array.from(convertedSet)])
    const fresh = bvids.filter(bv => !existing.has(bv))
    if (!fresh.length) return
    convertQueue.value = [...convertQueue.value, ...fresh]
    if (!loading.value) flush()
  }

  function flush() {
    const queue = convertQueue.value
    if (!queue.length) return
    convertQueue.value = []
    for (const bv of queue) convertingSet.add(bv)

    const urls = queue.map(bv => `https://www.bilibili.com/video/${bv}`).join('\n')
    sendMessage(`请将以下B站视频转为音频并加入播放列表:\n${urls}`)
  }

  function cancel() {
    cancelFn?.()
    cancelFn = null
    loading.value = false
    convertQueue.value = []
    convertingSet.clear()
  }

  return {
    messages, loading, sessionId, convertQueue, convertingSet, convertedSet,
    sendMessage, queueConvert, cancel
  }
})
