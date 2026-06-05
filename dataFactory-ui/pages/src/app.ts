import { message } from 'antd';
import logo from './assets/logo.jpg';
import { getUserInfo } from './services/UserAPIServices/UserAPI';

// 全局初始化数据配置，用于 Layout 用户信息和权限初始化
export async function getInitialState(): Promise<{
  name: string;
  userInfo: any;
  permissionRoutes: any;
  avatar: any;
}> {
  const token = localStorage.getItem('token');

  if (token) {
    try {
      const response = await getUserInfo();

      if (response.data) {
        localStorage.setItem('userInfo', JSON.stringify(response.data));

        return {
          name: response.data?.username || response.data?.nickname || '用户',
          userInfo: response.data,
          avatar: logo,
          permissionRoutes: response.data?.permissions,
        };
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('userInfo');
    }
  }

  return {
    name: '未登录',
    avatar: logo,
    userInfo: '',
    permissionRoutes: '',
  };
}

export const layout = ({ initialState }: any) => {
  return {
    layout: 'mix',
    splitMenus: true,
    logo: logo,
    menu: {
      locale: false,
    },
    title: '数据工厂',

    logout: async () => {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('userInfo');
      window.location.href = '/login';
    },

    avatarProps: {
      render: () => {
        return initialState?.name || '用户';
      },

      token: {
        sider: {
          colorMenuBackground: '#fff',
          colorTextMenuTitle: '#333',
          colorTextMenu: '#333',
          colorTextMenuSelected: '#1890FF',
          colorTextMenuItemHover: '#1890FF',
          colorBgMenuItemHover: 'rgba(24, 144, 255, 0.1)',
          colorBgMenuItemSelected: 'rgba(24, 144, 255, 0.1)',
        },
        header: {
          colorBgHeader: '#001529',
          colorHeaderTitle: '#FFFFFF',
          heightLayoutHeader: 60,
          colorTextRightActionsItem: '#FFFFFF',
        },
        pageContainer: {
          paddingBlockPageContainerContent: 0,
          paddingInlinePageContainerContent: 0,
        },
      },
    },
  };
};

export const request = {
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
  requestInterceptors: [
    (url: string, options: any) => {
      const token = localStorage.getItem('token');
      if (token) {
        const headers = {
          ...options.headers,
          Authorization: `Bearer ${token}`,
        };
        return {
          url,
          options: { ...options, headers },
        };
      }

      return {
        url,
        options: { ...options, interceptors: true },
      };
    },
  ],
  responseInterceptors: [
    (response: any) => {
      const { data } = response;
      if (data.code === 100415) {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userInfo');
        message.error('登录已过期，请重新登录');
        window.location.href = '/login';
        throw new Error('登录已过期');
      }
      if (data.code !== 100200) {
        message.error(data.message);
        throw data.message;
      }
      return response;
    },
  ],
  errorConfig: {
    errorHandler: (error: any) => {
      message.error(error.message || '请求失败');
    },
  },
};
