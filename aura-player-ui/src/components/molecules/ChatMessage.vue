<template>
  <article class="mb-2 flex w-full" :class="message.role === 'operator' ? 'justify-end' : 'justify-start'">
    <div
      class="max-w-[min(100%,38rem)] border-l-[3px] pl-4 pr-4 pt-2 pb-2"
      :style="{
        borderLeftColor: message.role === 'operator' ? 'var(--color-primary)' : 'var(--color-secondary)',
        boxShadow: message.role === 'operator'
          ? 'inset 3px 0 20px rgba(0, 240, 255, 0.08)'
          : 'inset 3px 0 20px rgba(255, 0, 255, 0.06)',
      }"
    >
      <div class="mb-1 flex items-baseline gap-2 opacity-80">
        <span class="terminal-label" :style="{
          color: message.role === 'operator' ? 'var(--color-primary)' : 'var(--color-secondary)',
          textShadow: message.role === 'operator' ? '0 0 8px rgba(0, 240, 255, 0.5)' : '0 0 8px rgba(255, 0, 255, 0.4)',
        }">
          {{ label }}
        </span>
        <span v-if="message.toolName" class="text-[10px] uppercase tracking-[0.12em]" style="font-family: var(--font-mono); color: var(--color-outline);">
          {{ message.toolName }}
        </span>
      </div>
      <div class="whitespace-pre-wrap text-sm leading-relaxed" style="font-family: var(--font-body);">
        <template v-for="(part, i) in parsedContent" :key="i">
          <!-- Track cards with + ADD button -->
          <div v-if="part.type === 'tracks'" class="my-2 space-y-1">
            <div v-for="(track, ti) in part.tracks" :key="ti"
                 class="flex items-center gap-2 p-2 text-xs transition-all duration-200"
                 style="border: 1px solid var(--color-outline-variant); background: rgba(13, 13, 31, 0.6);">
              <div class="flex-1 min-w-0">
                <div class="truncate font-semibold" style="font-family: var(--font-body);">{{ track.title }}</div>
                <div class="truncate opacity-60" style="font-family: var(--font-mono); font-size: 10px;">{{ track.author }} · {{ track.duration }}</div>
              </div>
              <button
                class="shrink-0 px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider transition-all duration-200"
                :style="addedSet.has(track.bvid)
                  ? { fontFamily: 'var(--font-headline)', border: '1px solid var(--color-outline-variant)', color: 'var(--color-outline)', background: 'transparent', cursor: 'default' }
                  : { fontFamily: 'var(--font-headline)', border: '1px solid var(--color-primary)', color: 'var(--color-primary)', background: 'rgba(0, 240, 255, 0.08)', textShadow: '0 0 8px rgba(0, 240, 255, 0.5)', boxShadow: '0 0 10px rgba(0, 240, 255, 0.2)' }"
                :disabled="addedSet.has(track.bvid)"
                @click="handleAdd(track)"
              >
                {{ addedSet.has(track.bvid) ? 'ADDED' : '+ ADD' }}
              </button>
            </div>
          </div>

          <!-- Added tracks (auto-added to playlist) -->
          <div v-else-if="part.type === 'added'" class="my-2 space-y-1">
            <div v-for="(track, ti) in part.tracks" :key="ti"
                 class="flex items-center gap-2 p-2 text-xs"
                 style="border: 1px solid rgba(0, 240, 255, 0.3); background: rgba(0, 240, 255, 0.06); box-shadow: 0 0 10px rgba(0, 240, 255, 0.08);">
              <span style="color: var(--color-primary); text-shadow: 0 0 6px rgba(0, 240, 255, 0.5);">&#10003;</span>
              <div class="flex-1 min-w-0">
                <div class="truncate font-semibold" style="font-family: var(--font-body);">{{ track.title }}</div>
                <div class="truncate opacity-60" style="font-family: var(--font-mono); font-size: 10px;">{{ track.author }}</div>
              </div>
            </div>
          </div>

          <!-- Regular code block -->
          <code v-else-if="part.type === 'code'" class="block my-1 p-2 text-xs overflow-x-auto" style="font-family: var(--font-mono); border: 1px solid var(--color-outline-variant); background: rgba(13, 13, 31, 0.5); color: var(--color-primary);">{{ part.text }}</code>

          <!-- Plain text -->
          <span v-else>{{ part.text }}</span>
        </template>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ChatMessage, BiliVideo } from '@/types'
import { useAgentStore } from '@/stores/agent'
import { usePlayerStore } from '@/stores/player'

const props = defineProps<{ message: ChatMessage }>()

const agentStore = useAgentStore()
const playerStore = usePlayerStore()

const label = computed(() => {
  switch (props.message.role) {
    case 'agent': return 'AGENT_01'
    case 'operator': return 'OPERATOR'
    case 'system': return 'SYSTEM'
    case 'tool': return 'TOOL'
    default: return 'UNKNOWN'
  }
})

const addedSet = computed(() => {
  return new Set([...agentStore.convertingSet, ...agentStore.convertedSet])
})


interface ParsedPart {
  type: 'text' | 'code' | 'tracks' | 'added'
  text?: string
  tracks?: BiliVideo[]
}

function tryParseTracks(json: string): BiliVideo[] | null {
  try {
    const arr = JSON.parse(json)
    if (!Array.isArray(arr)) return null
    return arr.filter((t: any) => t && (t.bvid || t.id)).map((t: any) => ({
      bvid: t.bvid || '',
      title: t.title || '',
      author: t.author || '',
      duration: t.duration || '',
      play: t.play || 0,
      pic: t.pic || '',
      id: t.id || t.bvid || '',
      url: t.url || null,
    }))
  } catch {
    return null
  }
}

const parsedContent = computed(() => {
  const parts: ParsedPart[] = []
  const regex = /```(\w*)\n([\s\S]*?)```/g
  let last = 0
  let match
  while ((match = regex.exec(props.message.content)) !== null) {
    if (match.index > last) {
      parts.push({ type: 'text', text: props.message.content.slice(last, match.index) })
    }
    const lang = match[1]?.toLowerCase() ?? ''
    const body = match[2]?.trim() ?? ''

    if (lang === 'tracks' || lang === 'added') {
      const tracks = tryParseTracks(body)
      if (tracks && tracks.length > 0) {
        parts.push({ type: lang, tracks })
      } else {
        parts.push({ type: 'code', text: body })
      }
    } else {
      parts.push({ type: 'code', text: body })
    }
    last = match.index + match[0].length
  }
  if (last < props.message.content.length) {
    parts.push({ type: 'text', text: props.message.content.slice(last) })
  }
  return parts
})

// Auto-add tracks from ```added blocks
const addedTracks = computed(() => {
  const tracks: BiliVideo[] = []
  for (const part of parsedContent.value) {
    if (part.type === 'added' && part.tracks) {
      tracks.push(...part.tracks)
    }
  }
  return tracks
})

// Watch for added tracks and auto-add to playlist
import { watch, nextTick } from 'vue'
watch(addedTracks, (tracks) => {
  if (tracks.length > 0) {
    const playerTracks = tracks.map(t => {
      // Use the pre-built url from API if available, otherwise construct from id
      let url: string
      let subDir = ''
      let filename = ''
      if (t.url) {
        url = t.url
        const id = t.id || ''
        const slashIdx = id.indexOf('/')
        subDir = slashIdx >= 0 ? id.substring(0, slashIdx) : ''
        filename = slashIdx >= 0 ? id.substring(slashIdx + 1) : id
      } else {
        const id = t.id || t.bvid
        const slashIdx = id.indexOf('/')
        if (slashIdx >= 0) {
          subDir = id.substring(0, slashIdx)
          filename = id.substring(slashIdx + 1)
          url = `/api/tracks/stream?path=${subDir}/${encodeURIComponent(filename)}`
        } else {
          // id is just a bvid - leave url empty, syncFromScan will fix it
          url = ''
        }
      }
      return {
        id: t.bvid,
        title: t.title,
        author: t.author,
        date: '',
        filename,
        subDir,
        size: 0,
        url,
        bvid: t.bvid,
      }
    })
    playerStore.addTracks(playerTracks)
    // Mark as converted
    for (const t of tracks) {
      agentStore.convertedSet.add(t.bvid)
    }
    // Sync from scan to fix any placeholder tracks with missing URLs
    nextTick(() => playerStore.syncFromScan())
  }
}, { immediate: true })

function handleAdd(track: BiliVideo) {
  agentStore.queueConvert([track.bvid])
}
</script>
