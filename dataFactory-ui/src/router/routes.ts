import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

/** 侧边栏菜单项定义 */
export interface MenuItem {
  path: string
  title: string
  icon?: string
  children?: MenuItem[]
  permission?: string
}

/** 所有业务路由 */
export const businessRoutes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/DashboardView.vue'),
    meta: { title: '工作台', icon: 'Odometer' },
  },
  // ===== 数据源管理 =====
  {
    path: '/api',
    redirect: '/api/list',
    meta: { title: '接口管理', icon: 'Link', permission: 'api:read' },
    children: [
      {
        path: 'category',
        name: 'ApiCategory',
        component: () => import('@/views/api/ApiCategoryView.vue'),
        meta: { title: '接口分类管理', permission: 'api:write' },
      },
      {
        path: 'list',
        name: 'ApiList',
        component: () => import('@/views/api/ApiListView.vue'),
        meta: { title: '接口列表', permission: 'api:read' },
      },
      {
        path: 'create',
        name: 'ApiCreate',
        component: () => import('@/views/api/ApiFormView.vue'),
        meta: { title: '新增接口', permission: 'api:write', hidden: true },
      },
      {
        path: ':id/edit',
        name: 'ApiEdit',
        component: () => import('@/views/api/ApiFormView.vue'),
        meta: { title: '编辑接口', permission: 'api:write', hidden: true },
      },
      {
        path: ':id/test',
        name: 'ApiTest',
        component: () => import('@/views/api/ApiTestView.vue'),
        meta: { title: '接口测试', permission: 'api:read', hidden: true },
      },
    ],
  },
  {
    path: '/database',
    redirect: '/database/list',
    meta: { title: '数据库管理', icon: 'Coin', permission: 'database:read' },
    children: [
      {
        path: 'list',
        name: 'DatabaseList',
        component: () => import('@/views/database/DatabaseListView.vue'),
        meta: { title: '数据库连接', permission: 'database:read' },
      },
      {
        path: 'create',
        name: 'DatabaseCreate',
        component: () => import('@/views/database/DatabaseFormView.vue'),
        meta: { title: '新增连接', permission: 'database:write', hidden: true },
      },
      {
        path: ':id/edit',
        name: 'DatabaseEdit',
        component: () => import('@/views/database/DatabaseFormView.vue'),
        meta: { title: '编辑连接', permission: 'database:write', hidden: true },
      },
      {
        path: ':id/detail',
        name: 'DatabaseDetail',
        component: () => import('@/views/database/DatabaseDetailView.vue'),
        meta: { title: '连接详情', permission: 'database:read', hidden: true },
      },
    ],
  },
  // ===== 数据标准管理 =====
  {
    path: '/standard',
    name: 'DataStandard',
    component: () => import('@/views/dataStandard/DataStandardListView.vue'),
    meta: { title: '数据标准目录', icon: 'Document', permission: 'standard:read' },
  },
  {
    path: '/standard/create',
    name: 'DataStandardCreate',
    component: () => import('@/views/dataStandard/DataStandardFormView.vue'),
    meta: { title: '新增数据标准', permission: 'standard:write', hidden: true },
  },
  {
    path: '/standard/:id/edit',
    name: 'DataStandardEdit',
    component: () => import('@/views/dataStandard/DataStandardFormView.vue'),
    meta: { title: '编辑数据标准', permission: 'standard:write', hidden: true },
  },
  {
    path: '/code-table',
    redirect: '/code-table/list',
    meta: { title: '码表管理', icon: 'Collection', permission: 'standard:read' },
    children: [
      {
        path: 'list',
        name: 'CodeTableList',
        component: () => import('@/views/codeTable/CodeTableListView.vue'),
        meta: { title: '码表列表', permission: 'standard:read' },
      },
      {
        path: 'create',
        name: 'CodeTableCreate',
        component: () => import('@/views/codeTable/CodeTableFormView.vue'),
        meta: { title: '新增码表', permission: 'standard:write', hidden: true },
      },
      {
        path: ':id/edit',
        name: 'CodeTableEdit',
        component: () => import('@/views/codeTable/CodeTableFormView.vue'),
        meta: { title: '编辑码表', permission: 'standard:write', hidden: true },
      },
    ],
  },
  // ===== 数据资产管理 =====
  {
    path: '/asset',
    redirect: '/asset/list',
    meta: { title: '数据资产管理', icon: 'FolderOpened', permission: 'asset:read' },
    children: [
      {
        path: 'list',
        name: 'AssetList',
        component: () => import('@/views/asset/AssetListView.vue'),
        meta: { title: '资产目录', permission: 'asset:read' },
      },
      {
        path: 'create',
        name: 'AssetCreate',
        component: () => import('@/views/asset/AssetFormView.vue'),
        meta: { title: '新增资产', permission: 'asset:write', hidden: true },
      },
      {
        path: ':id/edit',
        name: 'AssetEdit',
        component: () => import('@/views/asset/AssetFormView.vue'),
        meta: { title: '编辑资产', permission: 'asset:write', hidden: true },
      },
    ],
  },
  // ===== 脚本管理 =====
  {
    path: '/script',
    redirect: '/script/list',
    meta: { title: '脚本管理', icon: 'Monitor', permission: 'script:read' },
    children: [
      {
        path: 'list',
        name: 'ScriptList',
        component: () => import('@/views/script/ScriptListView.vue'),
        meta: { title: '脚本列表', permission: 'script:read' },
      },
      {
        path: 'create',
        name: 'ScriptCreate',
        component: () => import('@/views/script/ScriptFormView.vue'),
        meta: { title: '新增脚本', permission: 'script:write', hidden: true },
      },
      {
        path: ':id/edit',
        name: 'ScriptEdit',
        component: () => import('@/views/script/ScriptFormView.vue'),
        meta: { title: '编辑脚本', permission: 'script:write', hidden: true },
      },
    ],
  },
  // ===== 任务管理 =====
  {
    path: '/task',
    redirect: '/task/list',
    meta: { title: '任务管理', icon: 'SetUp', permission: 'task:read' },
    children: [
      {
        path: 'list',
        name: 'TaskList',
        component: () => import('@/views/task/TaskListView.vue'),
        meta: { title: '任务列表', permission: 'task:read' },
      },
      {
        path: 'create',
        name: 'TaskCreate',
        component: () => import('@/views/task/TaskFormView.vue'),
        meta: { title: '新增任务', permission: 'task:write', hidden: true },
      },
      {
        path: ':id/edit',
        name: 'TaskEdit',
        component: () => import('@/views/task/TaskFormView.vue'),
        meta: { title: '编辑任务', permission: 'task:write', hidden: true },
      },
      {
        path: ':id/dag',
        name: 'TaskDag',
        component: () => import('@/views/task/TaskDagView.vue'),
        meta: { title: 'DAG 编排', permission: 'task:write', hidden: true },
      },
      {
        path: ':id/executions',
        name: 'TaskExecutions',
        component: () => import('@/views/task/TaskExecutionView.vue'),
        meta: { title: '执行历史', permission: 'task:read', hidden: true },
      },
    ],
  },
]

/** 完整路由表(不包含动态路由) */
export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', hidden: true },
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      ...businessRoutes,
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '404', hidden: true },
  },
]
