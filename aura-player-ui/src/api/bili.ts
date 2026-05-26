import type { BiliVideo, DanmakuItem } from '@/types'

export async function searchBili(keyword: string, page = 1): Promise<{ total: number; videos: BiliVideo[] }> {
  const params = new URLSearchParams({ keyword, page: String(page) })
  const res = await fetch(`/api/bili/search?${params}`)
  return res.json()
}

export async function getDanmaku(bvid: string): Promise<{ danmaku: DanmakuItem[] }> {
  const params = new URLSearchParams({ bvid })
  const res = await fetch(`/api/bili/danmaku?${params}`)
  return res.json()
}
