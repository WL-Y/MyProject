import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Track } from '@/types'

export const usePlayerStore = defineStore('player', () => {
  const playlist = ref<Track[]>([])
  const index = ref(-1)
  const playing = ref(false)
  const progress = ref(0)
  const duration = ref(0)
  const volume = ref(0.7)

  const current = computed(() =>
    index.value >= 0 && index.value < playlist.value.length
      ? playlist.value[index.value] ?? null
      : null
  )

  const audio = ref<HTMLAudioElement | null>(null)

  function initAudio(el: HTMLAudioElement) {
    audio.value = el
    el.volume = volume.value
    el.addEventListener('timeupdate', () => { progress.value = el.currentTime })
    el.addEventListener('loadedmetadata', () => { duration.value = el.duration })
    el.addEventListener('ended', () => next())
    el.addEventListener('play', () => { playing.value = true })
    el.addEventListener('pause', () => { playing.value = false })
  }

  function playTrack(track: Track, pl?: Track[]) {
    if (pl?.length) {
      playlist.value = [...pl]
      const i = Math.max(pl.findIndex(t => t.id === track.id), 0)
      index.value = i
    } else {
      const i = playlist.value.findIndex(t => t.id === track.id)
      if (i >= 0) {
        index.value = i
      } else {
        playlist.value = [track]
        index.value = 0
      }
    }
    if (audio.value) {
      audio.value.src = track.url
      audio.value.play()
    }
  }

  function addTracks(tracks: Track[]) {
    const ids = new Set(playlist.value.map(t => t.id))
    const fresh = tracks.filter(t => !ids.has(t.id))
    if (!fresh.length) return
    playlist.value = [...playlist.value, ...fresh]

    if (index.value < 0 && playlist.value.length > 0) {
      index.value = 0
      playTrack(playlist.value[0])
    }
  }

  function removeTrack(trackId: string) {
    const rmIdx = playlist.value.findIndex(t => t.id === trackId)
    if (rmIdx < 0) return
    const next = [...playlist.value]
    next.splice(rmIdx, 1)
    playlist.value = next

    if (rmIdx === index.value) {
      if (next.length === 0) {
        index.value = -1
        audio.value?.pause()
      } else {
        const newIdx = Math.min(rmIdx, next.length - 1)
        index.value = newIdx
        playTrack(next[newIdx])
      }
    } else if (rmIdx < index.value) {
      index.value--
    }
  }

  function next() {
    if (!playlist.value.length) return
    const ni = Math.min(playlist.value.length - 1, Math.max(index.value + 1, 0))
    if (ni === index.value && index.value >= 0) return
    index.value = ni
    playTrack(playlist.value[ni])
  }

  function prev() {
    if (!playlist.value.length || index.value <= 0) return
    const ni = Math.max(0, index.value - 1)
    index.value = ni
    playTrack(playlist.value[ni])
  }

  function togglePlay() {
    if (!audio.value) return
    if (index.value < 0 && playlist.value.length > 0) {
      index.value = 0
      playTrack(playlist.value[0])
      return
    }
    if (playing.value) audio.value.pause()
    else audio.value.play()
  }

  function seek(time: number) {
    if (audio.value) audio.value.currentTime = time
  }

  function setVolume(v: number) {
    volume.value = v
    if (audio.value) audio.value.volume = v
  }

  function stop() {
    if (audio.value) {
      audio.value.pause()
      audio.value.currentTime = 0
    }
  }

  async function loadTracks() {
    try {
      const res = await fetch('/api/tracks/scan')
      const data = await res.json()
      if (data.tracks?.length) {
        playlist.value = data.tracks
        if (index.value < 0 && playlist.value.length > 0) {
          index.value = 0
        }
      }
    } catch {
      // scan failed, ignore
    }
  }

  async function syncFromScan() {
    try {
      const res = await fetch('/api/tracks/scan')
      const data = await res.json()
      if (!data.tracks?.length) return

      const scanMap = new Map<string, Track>()
      for (const t of data.tracks as Track[]) {
        if (t.bvid) scanMap.set(t.bvid, t)
      }

      // Replace placeholder tracks (id=bvid) with real scan data, add missing
      const seen = new Set<string>()
      const merged: Track[] = []
      for (const t of playlist.value) {
        if (t.bvid && scanMap.has(t.bvid)) {
          const real = scanMap.get(t.bvid)!
          if (!seen.has(real.id)) {
            merged.push({ ...real })
            seen.add(real.id)
          }
        } else {
          if (!seen.has(t.id)) {
            merged.push(t)
            seen.add(t.id)
          }
        }
      }
      // Add scan tracks not yet in playlist
      for (const t of data.tracks as Track[]) {
        if (!seen.has(t.id)) {
          merged.push(t)
          seen.add(t.id)
        }
      }
      playlist.value = merged
    } catch {
      // ignore
    }
  }

  return {
    playlist, index, playing, progress, duration, volume, current,
    initAudio, playTrack, addTracks, removeTrack, next, prev, togglePlay, seek, setVolume, stop,
    loadTracks, syncFromScan
  }
})
