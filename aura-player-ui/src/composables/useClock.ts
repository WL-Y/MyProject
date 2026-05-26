import { ref, onMounted, onUnmounted } from 'vue'

export function useClock() {
  const hours = ref('')
  const minutes = ref('')
  const date = ref('')
  let timer: ReturnType<typeof setInterval> | null = null

  function update() {
    const now = new Date()
    hours.value = String(now.getHours()).padStart(2, '0')
    minutes.value = String(now.getMinutes()).padStart(2, '0')
    date.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
  }

  onMounted(() => {
    update()
    timer = setInterval(update, 1000)
  })

  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })

  return { hours, minutes, date }
}
