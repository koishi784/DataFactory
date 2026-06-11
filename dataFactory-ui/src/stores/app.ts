import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const visitedViews = ref<string[]>([])

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function addVisitedView(path: string) {
    if (!visitedViews.value.includes(path)) {
      visitedViews.value.push(path)
    }
  }

  function removeVisitedView(path: string) {
    const index = visitedViews.value.indexOf(path)
    if (index > -1) {
      visitedViews.value.splice(index, 1)
    }
  }

  function closeOtherViews(path: string) {
    visitedViews.value = visitedViews.value.filter((v) => v === path)
  }

  function closeAllViews() {
    visitedViews.value = []
  }

  return {
    sidebarCollapsed,
    visitedViews,
    toggleSidebar,
    addVisitedView,
    removeVisitedView,
    closeOtherViews,
    closeAllViews,
  }
})
