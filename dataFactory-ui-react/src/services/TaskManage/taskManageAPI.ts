import { request } from '@umijs/max';

// ==================== 任务分类相关 ====================

// 任务分类树节点
export interface TaskCategoryTreeNode {
    id: number;
    name: string;
    parentId: number;
    level: number;
    sortOrder: number;
    createTime: string;
    children: TaskCategoryTreeNode[];
}

// 创建分类数据
export interface CreateCategoryData {
    name: string;
    parentId: number;
    sortOrder?: number;
}

// 更新分类数据
export interface UpdateCategoryData {
    name: string;
    sortOrder?: number;
}

// ==================== 任务节点相关 ====================

// 节点配置基类
export interface NodeConfig {}

// START 节点配置
export interface StartNodeConfig extends NodeConfig {
    description?: string;
}

// API 节点配置
export interface ApiNodeConfig extends NodeConfig {
    apiId: number;
    paramMapping?: Record<string, string>;
    timeout?: number;
    retryCount?: number;
}

// 脚本节点配置
export interface ScriptNodeConfig extends NodeConfig {
    scriptId: number;
    scriptVersion?: string;
    params?: Record<string, string>;
    dataSourceId?: number;
}

// 字段映射配置
export interface FieldMapping {
    sourceField: string;
    targetField: string;
    transformRule?: string | null;
    defaultValue?: string | null;
}

// MAPPING 节点配置
export interface MappingNodeConfig extends NodeConfig {
    mappings: FieldMapping[];
}

// 输出字段映射
export interface OutputFieldMapping {
    sourceField: string;
    targetColumn: string;
}

// OUTPUT 节点配置
export interface OutputNodeConfig extends NodeConfig {
    outputType: 'DATABASE' | 'FILE' | 'API_PUSH';
    targetDataSourceId: number;
    targetTable: string;
    writeMode: 'INSERT' | 'UPSERT' | 'OVERWRITE' | 'APPEND';
    fieldMappings?: OutputFieldMapping[];
}

// END 节点配置
export interface EndNodeConfig extends NodeConfig {
    description?: string;
}

// 任务节点
export interface TaskNode {
    nodeId: string;
    nodeName: string;
    nodeType: 'START' | 'API' | 'SCRIPT' | 'MAPPING' | 'OUTPUT' | 'END';
    positionX: number;
    positionY: number;
    config: NodeConfig;
}

// 任务连线
export interface TaskEdge {
    edgeId: string;
    sourceNodeId: string;
    targetNodeId: string;
    condition?: string | null;
}

// ==================== 任务相关 ====================

// 任务列表项
export interface TaskItem {
    id: number;
    taskName: string;
    taskDescription: string;
    categoryId: number;
    status: number;
    executeStatus: number;
    scheduleType: string;
    lastExecuteTime: string | null;
    nextExecuteTime: string | null;
    createTime: string;
    updateTime: string;
}

// 触发配置
export interface TriggerConfig {
    scheduleType: 'API' | 'CRON';
    cronExpression?: string;
    effectiveDate?: string;
    expireDate?: string;
}

// 任务详情
export interface TaskDetail extends TaskItem {
    nodes: TaskNode[];
    edges: TaskEdge[];
    triggerConfig: TriggerConfig;
}

// 任务列表查询参数
export interface TaskListParams {
    pageNum?: number;
    pageSize?: number;
    keyword?: string;
    status?: string;
    categoryId?: number;
    executeStatus?: number;
}

// 创建任务数据
export interface CreateTaskData {
    taskName: string;
    categoryId: number;
    taskDescription?: string;
}

// 更新任务DAG配置数据
export interface UpdateTaskConfigData {
    nodes: TaskNode[];
    edges: TaskEdge[];
}

// 设置触发方式数据
export interface SetTriggerConfigData {
    scheduleType: 'API' | 'CRON';
    cronExpression?: string;
    effectiveDate?: string;
    expireDate?: string;
}

// ==================== 执行相关 ====================

// 节点执行结果
export interface NodeExecuteResult {
    nodeId: string;
    nodeName: string;
    nodeType: string;
    status: number;
    startTime: string;
    endTime: string;
    duration: number;
    inputData: string | null;
    outputData: string | null;
    errorMessage: string | null;
    logs: string;
}

// 测试运行任务参数
export interface TestRunTaskData {
    taskParams?: Record<string, any>;
    debugMode?: boolean;
}

// 测试运行任务结果
export interface TestRunTaskResult {
    executionId: number;
    status: number;
    startTime: string;
    endTime: string;
    totalDuration: number;
    nodeResults: NodeExecuteResult[];
}

// 执行历史记录
export interface ExecutionRecord {
    executionId: number;
    status: number;
    startTime: string;
    endTime: string;
    duration: number;
    triggerType: string;
    triggerBy: string;
}

// 执行历史查询参数
export interface ExecutionHistoryParams {
    pageNum?: number;
    pageSize?: number;
    startDate?: string;
    endDate?: string;
    status?: string;
}

// 手动执行任务参数
export interface ExecuteTaskData {
    taskParams?: Record<string, any>;
}

// 批量操作结果
export interface BatchOperationResult {
    successCount: number;
    failCount: number;
}

// ==================== API 接口 ====================

// §11.1 查询任务分类树
export async function getTaskCategoryTree() {
    return request('/task-categories/tree', {
        method: 'GET',
    });
}

// §11.2 查询任务列表
export async function getTaskList(params?: TaskListParams) {
    return request('/tasks', {
        method: 'GET',
        params,
    });
}

// §11.3 查询任务详情
export async function getTaskDetail(id: number) {
    return request(`/tasks/${id}`, {
        method: 'GET',
    });
}

// §11.4 新增任务
export async function createTask(data: CreateTaskData) {
    return request('/tasks', {
        method: 'POST',
        data,
    });
}

// §11.5 更新任务DAG配置
export async function updateTaskConfig(id: number, data: UpdateTaskConfigData) {
    return request(`/tasks/${id}/config`, {
        method: 'PUT',
        data,
    });
}

// §11.9 设置任务触发方式
export async function setTaskTriggerConfig(id: number, data: SetTriggerConfigData) {
    return request(`/tasks/${id}/trigger-config`, {
        method: 'PUT',
        data,
    });
}

// §11.10 测试运行任务
export async function testRunTask(id: number, data?: TestRunTaskData) {
    return request(`/tasks/${id}/test-run`, {
        method: 'POST',
        data: data || {},
    });
}

// §11.11 查询任务执行历史
export async function getTaskExecutionHistory(id: number, params?: ExecutionHistoryParams) {
    return request(`/tasks/${id}/executions`, {
        method: 'GET',
        params,
    });
}

// §11.12 手动执行任务
export async function executeTask(id: number, data?: ExecuteTaskData) {
    return request(`/tasks/${id}/execute`, {
        method: 'POST',
        data: data || {},
    });
}

// §11.13 停止正在执行的任务
export async function cancelTaskExecution(id: number, executionId: number) {
    return request(`/tasks/${id}/executions/${executionId}/cancel`, {
        method: 'POST',
    });
}

// §11.14 发布任务
export async function publishTask(id: number) {
    return request(`/tasks/${id}/publish`, {
        method: 'PUT',
    });
}

// §11.14 停用任务
export async function disableTask(id: number) {
    return request(`/tasks/${id}/disable`, {
        method: 'PUT',
    });
}

// §11.14 删除任务
export async function deleteTask(id: number) {
    return request(`/tasks/${id}`, {
        method: 'DELETE',
    });
}

// §11.15 批量发布任务
export async function batchPublishTasks(data: { ids: number[] }) {
    return request('/tasks/batch/publish', {
        method: 'PUT',
        data,
    });
}

// §11.16 批量停用任务
export async function batchDisableTasks(data: { ids: number[] }) {
    return request('/tasks/batch/disable', {
        method: 'PUT',
        data,
    });
}

// §11.18 新增任务分类
export async function createTaskCategory(data: CreateCategoryData) {
    return request('/task-categories', {
        method: 'POST',
        data,
    });
}

// §11.18 编辑任务分类
export async function updateTaskCategory(id: number, data: UpdateCategoryData) {
    return request(`/task-categories/${id}`, {
        method: 'PUT',
        data,
    });
}

// §11.18 删除任务分类
export async function deleteTaskCategory(id: number) {
    return request(`/task-categories/${id}`, {
        method: 'DELETE',
    });
}
