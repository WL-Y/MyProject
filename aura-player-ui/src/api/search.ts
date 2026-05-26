import type { Track } from '@/types'

export async function searchTracks(q: string, limit = 20): Promise<{ total: number; tracks: Track[] }> {
  const params = new URLSearchParams({ q, limit: String(limit) })
  const res = await fetch(`/api/search?${params}`)
  return res.json()
}
