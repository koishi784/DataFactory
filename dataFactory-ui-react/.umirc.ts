import { defineConfig } from '@umijs/max';
import { Component } from 'react';

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
      path: '/',
      redirect: '/login',
    },
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
              name: '人工注册',
              path: '/data/source/APIManualRegistration',
              component: './DataFactory/APIManualRegistration',
              hideInMenu: true,
            },
            {
              name: '数据库管理',
              path: '/data/source/dataManage',
              component: './DataFactory/DataBaseManage',
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
          name: '新增资产表',
          path: '/data/addDataAsset',
          component: './DataFactory/AddDataAsset',
          hideInMenu: true,
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
        {
          name: '新增任务',
          path: '/data/addTask',
          component: './DataFactory/AddTask',
          hideInMenu: true,
        }
      ],
    },
    {
      path: '/userManage',
      name: '用户管理',
      routes: [
        {
          path: '/userManage',
          redirect: '/userManage/profile'
        },
        {
          name: '个人信息',
          path: '/userManage/profile',
          component: './Login/userManage'
        }
      ]
    }
  ],
  npmClient: 'pnpm',
});
