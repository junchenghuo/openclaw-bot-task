(() => {
  const openButtons = document.querySelectorAll(".topic-link[data-modal-id]")
  for (const btn of openButtons) {
    btn.addEventListener("click", () => {
      const modalId = btn.getAttribute("data-modal-id")
      const dialog = modalId ? document.getElementById(modalId) : null
      if (dialog && typeof dialog.showModal === "function") {
        dialog.showModal()
      }
    })
  }

  const closeButtons = document.querySelectorAll(".modal-close[data-close-id]")
  for (const btn of closeButtons) {
    btn.addEventListener("click", () => {
      const modalId = btn.getAttribute("data-close-id")
      const dialog = modalId ? document.getElementById(modalId) : null
      if (dialog && typeof dialog.close === "function") {
        dialog.close()
      }
    })
  }
})()
