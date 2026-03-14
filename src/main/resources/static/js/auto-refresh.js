(() => {
  const seconds = Number(document.body?.dataset?.autoRefreshSeconds || 0)
  if (!Number.isFinite(seconds) || seconds <= 0) {
    return
  }

  const intervalMs = seconds * 1000
  window.setInterval(() => {
    if (document.querySelector("dialog[open]")) {
      return
    }
    window.location.reload()
  }, intervalMs)
})()
