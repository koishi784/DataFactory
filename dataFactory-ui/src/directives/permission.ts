import type { Directive } from 'vue'
import { useAuthStore } from '@/stores/auth'

const permissionDirective: Directive = {
  mounted(el: HTMLElement, binding) {
    const authStore = useAuthStore()
    const { value } = binding

    if (!value) return

    const permissions = Array.isArray(value) ? value : [value]
    const hasPermission = permissions.some((perm: string) => authStore.hasPermission(perm))

    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
  },
}

export default permissionDirective
