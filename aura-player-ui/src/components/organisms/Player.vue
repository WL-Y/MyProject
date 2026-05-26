<template>
  <div class="cyber-panel flex flex-col gap-4 p-4">
    <TrackInfo :track="playerStore.current" :playing="playerStore.playing" />
    <div class="flex flex-wrap items-center justify-between gap-4">
      <ControlBar
        :playing="playerStore.playing"
        @prev="playerStore.prev()"
        @toggle="playerStore.togglePlay()"
        @next="playerStore.next()"
        @stop="playerStore.stop()"
      />
      <VolumeControl :volume="playerStore.volume" @change="playerStore.setVolume($event)" />
    </div>
    <SeekBar
      :progress="playerStore.progress"
      :duration="playerStore.duration"
      :playing="playerStore.playing"
      @seek="playerStore.seek($event)"
    />
    <audio ref="audioRef" class="hidden" preload="metadata" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { usePlayerStore } from '@/stores/player'
import ControlBar from '@/components/molecules/ControlBar.vue'
import SeekBar from '@/components/molecules/SeekBar.vue'
import TrackInfo from '@/components/molecules/TrackInfo.vue'
import VolumeControl from '@/components/molecules/VolumeControl.vue'

const playerStore = usePlayerStore()
const audioRef = ref<HTMLAudioElement | null>(null)

onMounted(async () => {
  if (audioRef.value) playerStore.initAudio(audioRef.value)
  await playerStore.loadTracks()
})
</script>
