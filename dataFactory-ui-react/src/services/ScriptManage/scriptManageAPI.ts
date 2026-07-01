import { request } from '@umijs/max';

// 脚本分类树节点
export interface ScriptCategoryTreeNode {
    id: number;
    name: string;
    parentId: number;
    level: number;
    sortOrder: number;
    createTime: string;
    children: ScriptCategoryTreeNode[];
}

// 脚本列表项
export interface ScriptItem {
    id: number;
    scriptName: string;
    scriptType: string;
    categoryId: number;
    fileName: string;
    description: string;
    status: number;
    createTime: string;
    updateTime: string;
}

// 参数对象
export interface ScriptParam {
    id: number;
    paramName: string;
    paramType: string;
    description: string;
}

// 脚本详情
export interface ScriptDetail extends ScriptItem {
    inputParams: ScriptParam[];
    outputParams: ScriptParam[];
}

// 脚本列表查询参数
export interface ScriptListParams {
    pageNum?: number;
    pageSize?: number;
    keyword?: string;
    status?: string;
    categoryId?: number;
}

// 创建/编辑脚本的参数对象
export interface CreateScriptParamData {
    paramName: string;
    paramType: string;
    description?: string;
}

// 创建脚本数据
export interface CreateScriptData {
    scriptName: string;
    categoryId: number;
    fileId: number;
    description?: string;
    inputParams?: CreateScriptParamData[];
    outputParams?: CreateScriptParamData[];
}

// 编辑脚本数据
export interface UpdateScriptData {
    scriptName: string;
    categoryId: number;
    fileId?: number;
    description?: string;
    inputParams?: CreateScriptParamData[];
    outputParams?: CreateScriptParamData[];
}

// 调试脚本参数
export interface DebugScriptData {
    params?: Record<string, any>;
}

// 调试脚本结果
export interface DebugScriptResult {
    success: boolean;
    executeTime: number;
    result: string;
    errorMessage: string | null;
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

export async function getScriptCategoryTree() {
    return request('/script-categories/tree', {
        method: 'GET',
    });
}

export async function getScriptList(params?: ScriptListParams) {
    return request('/scripts', {
        method: 'GET',
        params,
    });
}

export async function getScriptDetail(id: number) {
    return request(`/scripts/${id}`, {
        method: 'GET',
    });
}

export async function createScript(data: CreateScriptData) {
    return request('/scripts', {
        method: 'POST',
        data,
    });
}

export async function updateScript(id: number, data: UpdateScriptData) {
    return request(`/scripts/${id}`, {
        method: 'PUT',
        data,
    });
}

export async function debugScript(id: number, data: DebugScriptData) {
    return request(`/scripts/${id}/debug`, {
        method: 'POST',
        data,
    });
}

export async function publishScript(id: number) {
    return request(`/scripts/${id}/publish`, {
        method: 'PUT',
    });
}

export async function disableScript(id: number) {
    return request(`/scripts/${id}/disable`, {
        method: 'PUT',
    });
}

export async function deleteScript(id: number) {
    return request(`/scripts/${id}`, {
        method: 'DELETE',
    });
}

export async function batchPublishScripts(data: { ids: number[] }) {
    return request('/scripts/batch/publish', {
        method: 'PUT',
        data,
    });
}

export async function batchDisableScripts(data: { ids: number[] }) {
    return request('/scripts/batch/disable', {
        method: 'PUT',
        data,
    });
}

export async function createScriptCategory(data: CreateCategoryData) {
    return request('/script-categories', {
        method: 'POST',
        data,
    });
}

export async function updateScriptCategory(id: number, data: UpdateCategoryData) {
    return request(`/script-categories/${id}`, {
        method: 'PUT',
        data,
    });
}

export async function deleteScriptCategory(id: number) {
    return request(`/script-categories/${id}`, {
        method: 'DELETE',
    });
}