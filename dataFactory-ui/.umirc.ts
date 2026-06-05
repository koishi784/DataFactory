import { defineConfig } from '@umijs/max';

export default defineConfig({
  antd: {},
  access: {},
  model: {},
  initialState: {},
  request: {},
  layout: {
    title: '数据工厂',
  },
  routes: [
    {
      path: '/login',
      name: '登录',
      component: './Login/login',
      layout: false,
    },
    {
      path: '/register',
      name: '注册',
      component: './Login/register',
      layout: false,
    },
    {
      path: '/',
      redirect: '/data',
    },
    {
      name: '数据工厂',
      path: '/data',
      routes: [
        {
          path: '/data',
          redirect: '/data/source/APIManage',
        },
        {
          name: '数据源管理',
          path: '/data/source',
          routes: [
            {
              name: '接口管理',
              path: '/data/source/APIManage',
              component: './DataFactory/APIManage',
            },
            {
              name: '数据库管理',
              path: '/data/source/dataManage',
              component: './DataFactory/DataManage',
            },
          ],
        },
        {
          name: '数据标准管理',
          path: '/data/dataStandard',
          routes: [
            {
              name: '数据标准目录',
              path: '/data/dataStandard/directory',
              component: './DataFactory/Directory',
            },
            {
              name: '码表管理',
              path: '/data/dataStandard/codeTable',
              component: './DataFactory/CodeTable',
            },
          ],
        },
        {
          name: '数据资产管理',
          path: '/data/dataAssetManage',
          component: './DataFactory/DataAssetManage',
        },
        {
          name: '脚本管理',
          path: '/data/scriptManage',
          component: './DataFactory/ScriptManage',
        },
        {
          name: '任务管理',
          path: '/data/taskManage',
          component: './DataFactory/TaskManage',
        },
      ],
    },
  ],
  npmClient: 'pnpm',
});
