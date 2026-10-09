export function formatFriendlyTime(value, fallback = '-') {
  if (!value) return fallback
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return fallback
  const now = new Date()
  const day = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const difference = Math.round((today - day) / 86400000)
  const time = date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
  if (difference === 0) return `今天 ${time}`
  if (difference === 1) return `昨天 ${time}`
  if (difference === 2) return `前天 ${time}`
  if (difference > 2 && difference < 7) return `${difference}天前 ${time}`
  return `${date.getMonth() + 1}月${date.getDate()}日 ${time}`
}

export function formatFullTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString('zh-CN', { hour12: false })
}
