export interface ChatRequest {
  message: string
  mode: string
  history?: Array<{ role: string; content: string }>
}

export function streamChat(
  req: ChatRequest,
  onEvent: (event: string, data: unknown) => void,
  signal?: AbortSignal
): () => void {
  const controller = new AbortController()
  const combinedSignal = signal
    ? AbortSignal.any([signal, controller.signal])
    : controller.signal

  fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
    signal: combinedSignal,
  }).then(async (res) => {
    if (!res.ok || !res.body) {
      onEvent('error', { error: `HTTP ${res.status}` })
      return
    }

    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() ?? ''

      for (const line of lines) {
        if (line.startsWith('data:')) {
          // Strip all leading 'data:' prefixes (backend may send double prefix)
          let data = line.slice(5).trim()
          while (data.startsWith('data:')) {
            data = data.slice(5).trim()
          }
          if (data === '[DONE]') {
            onEvent('done', { status: 'completed' })
            return
          }
          if (!data) continue
          try {
            onEvent('output', JSON.parse(data))
          } catch {
            onEvent('output', data)
          }
        }
      }
    }
    onEvent('done', { status: 'completed' })
  }).catch((err) => {
    if (err.name !== 'AbortError') {
      onEvent('error', { error: String(err) })
    }
  })

  return () => controller.abort()
}
