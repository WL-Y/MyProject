import type { Track } from '@/types'

export async function scanTracks(subDir?: string): Promise<{ tracks: Track[] }> {
  const params = subDir ? `?subDir=${encodeURIComponent(subDir)}` : ''
  const res = await fetch(`/api/tracks/scan${params}`)
  return res.json()
}
