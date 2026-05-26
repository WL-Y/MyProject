export interface Track {
  id: string
  title: string
  author: string
  date: string
  filename: string
  subDir: string
  size: number
  url: string
  bvid?: string
}

export interface ChatMessage {
  id: string
  role: 'agent' | 'operator' | 'system' | 'tool'
  content: string
  timestamp: number
  toolName?: string
}

export interface PlayerState {
  current: Track | null
  playlist: Track[]
  index: number
  playing: boolean
  progress: number
  duration: number
  volume: number
}

export interface BiliVideo {
  bvid: string
  title: string
  author: string
  duration: string
  play: number
  pic: string
  id?: string
  url?: string
}

export interface DanmakuItem {
  time: number
  content: string
  type: number
  color: string
}

export type AppMode = 'local' | 'cloud'
