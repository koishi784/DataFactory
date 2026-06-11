import { useAuthStore } from '@/stores/auth'

export function usePermission() {
  const authStore = useAuthStore()

  function hasPermission(perm: string): boolean {
    return authStore.hasPermission(perm)
  }

  function hasAnyPermission(perms: string[]): boolean {
    return perms.some((perm) => authStore.hasPermission(perm))
  }

  function hasAllPermissions(perms: string[]): boolean {
    return perms.every((perm) => authStore.hasPermission(perm))
  }

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
  }
}
