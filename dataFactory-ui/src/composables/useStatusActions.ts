import { ElMessage, ElMessageBox } from 'element-plus'

interface StatusActionsOptions {
  apiPublish?: (id: number) => Promise<any>
  apiDisable?: (id: number) => Promise<any>
  apiDelete?: (id: number) => Promise<any>
  batchPublish?: (ids: number[]) => Promise<any>
  batchDisable?: (ids: number[]) => Promise<any>
  fetchData: () => void
  getSelectedIds: () => number[]
  getSelectedStatuses: () => number[]  // 用于校验批量操作合法性
}

export function useStatusActions(options: StatusActionsOptions) {
  const { apiPublish, apiDisable, apiDelete, batchPublish, batchDisable, fetchData, getSelectedIds, getSelectedStatuses } = options

  async function handlePublish(id: number, name?: string) {
    try {
      await ElMessageBox.confirm(
        `确定发布"${name || id}"吗？发布后如需修改需先停用。`,
        '发布确认',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
      )
      await apiPublish?.(id)
      ElMessage.success('发布成功')
      fetchData()
    } catch {
      // 取消操作
    }
  }

  async function handleDisable(id: number, name?: string) {
    try {
      await ElMessageBox.confirm(
        `确定停用"${name || id}"吗？停用后已有的调用关系将保留。`,
        '停用确认',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
      )
      await apiDisable?.(id)
      ElMessage.success('停用成功')
      fetchData()
    } catch {
      // 取消操作
    }
  }

  async function handleDelete(id: number, name?: string) {
    try {
      await ElMessageBox.confirm(
        `确定删除"${name || id}"吗？此操作不可恢复。`,
        '删除确认',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
      )
      await apiDelete?.(id)
      ElMessage.success('删除成功')
      fetchData()
    } catch {
      // 取消操作
    }
  }

  async function handleBatchPublish() {
    const ids = getSelectedIds()
    if (ids.length === 0) {
      ElMessage.warning('请先选择需要发布的数据')
      return
    }
    try {
      await ElMessageBox.confirm(
        `确定批量发布选中的 ${ids.length} 项数据吗？`,
        '批量发布确认',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
      )
      await batchPublish?.(ids)
      ElMessage.success('批量发布成功')
      fetchData()
    } catch {
      // 取消操作
    }
  }

  async function handleBatchDisable() {
    const ids = getSelectedIds()
    if (ids.length === 0) {
      ElMessage.warning('请先选择需要停用的数据')
      return
    }
    try {
      await ElMessageBox.confirm(
        `确定批量停用选中的 ${ids.length} 项数据吗？`,
        '批量停用确认',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
      )
      await batchDisable?.(ids)
      ElMessage.success('批量停用成功')
      fetchData()
    } catch {
      // 取消操作
    }
  }

  return {
    handlePublish,
    handleDisable,
    handleDelete,
    handleBatchPublish,
    handleBatchDisable,
  }
}
