import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { PageResult } from '@/types'

interface CrudOptions<T> {
  apiGetList: (params: any) => Promise<PageResult<T>>
  apiCreate?: (data: any) => Promise<any>
  apiUpdate?: (id: number, data: any) => Promise<any>
  apiDelete?: (id: number) => Promise<any>
  defaultForm?: Record<string, any>
}

export function useCrud<T extends { id?: number }>(options: CrudOptions<T>) {
  const { apiGetList, apiCreate, apiUpdate, apiDelete, defaultForm = {} } = options

  const list = ref<T[]>([])
  const loading = ref(false)
  const total = ref(0)
  const searchForm = reactive<Record<string, any>>({ ...defaultForm })

  const pagination = reactive({
    pageNum: 1,
    pageSize: 20,
  })

  // 弹窗相关
  const dialogVisible = ref(false)
  const isEdit = ref(false)
  const editingId = ref<number | null>(null)
  const formData = reactive<Record<string, any>>({ ...defaultForm })

  async function fetchData() {
    loading.value = true
    try {
      const params = { ...searchForm, ...pagination }
      const result = await apiGetList(params) as any
      if (Array.isArray(result)) {
        list.value = result as T[]
      } else if (result.records) {
        list.value = result.records as T[]
        total.value = result.total ?? 0
      }
    } catch {
      list.value = []
    } finally {
      loading.value = false
    }
  }

  function handleSearch() {
    pagination.pageNum = 1
    fetchData()
  }

  function handleReset() {
    Object.keys(searchForm).forEach((key) => {
      searchForm[key] = defaultForm[key] ?? ''
    })
    pagination.pageNum = 1
    fetchData()
  }

  function handlePageChange(page: number) {
    pagination.pageNum = page
    fetchData()
  }

  function handleSizeChange(size: number) {
    pagination.pageSize = size
    pagination.pageNum = 1
    fetchData()
  }

  function handleAdd() {
    isEdit.value = false
    editingId.value = null
    Object.assign(formData, JSON.parse(JSON.stringify(defaultForm)))
    dialogVisible.value = true
  }

  function handleEdit(row: T) {
    isEdit.value = true
    editingId.value = row.id!
    Object.assign(formData, JSON.parse(JSON.stringify(row)))
    dialogVisible.value = true
  }

  async function handleSubmit() {
    try {
      if (isEdit.value && editingId.value) {
        await apiUpdate?.(editingId.value, formData)
        ElMessage.success('更新成功')
      } else {
        await apiCreate?.(formData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch {
      // 错误已在拦截器中处理
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

  return {
    list,
    loading,
    total,
    pagination,
    searchForm,
    dialogVisible,
    isEdit,
    editingId,
    formData,
    fetchData,
    handleSearch,
    handleReset,
    handlePageChange,
    handleSizeChange,
    handleAdd,
    handleEdit,
    handleSubmit,
    handleDelete,
  }
}
