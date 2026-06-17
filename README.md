# DataFactory 数据工厂大数据平台

企业级数据工厂/大数据平台，提供接口管理、数据资产管理、码表管理、数据标准管理、数据库连接管理、脚本执行、DAG 任务编排以及 Coze AI 集成等核心功能。

## 技术栈

| 层级     | 技术                                      |
| -------- | ----------------------------------------- |
| 后端框架 | Spring Boot 3.2.5                         |
| 持久层   | MyBatis-Plus 3.5.5                        |
| 数据库   | MySQL 8.0                                 |
| 缓存     | Redis                                    |
| 认证     | JWT (jjwt 0.12.5)                        |
| API 文档 | SpringDoc OpenAPI 2.5.0                   |
| 脚本引擎 | Groovy 4.0.21, Python                     |
| Excel    | EasyExcel 3.3.4                           |
| 前端框架 | Vue 3 + TypeScript + Vite                 |
| UI 组件  | Element Plus                              |
| 状态管理 | Pinia                                     |
| 流程编排 | @vue-flow/core (DAG 任务编辑)             |

## 模块结构

```
DataFactory
├── dataFactory-common      # 公共模块（工具类、DTO/VO、异常、枚举）
├── dataFactory-core        # 核心业务模块（实体、Mapper、Service、脚本执行器）
├── dataFactory-api         # 启动模块（Controller、配置、SQL 建表脚本）
└── dataFactory-ui          # 前端模块（Vue 3 + Element Plus）
```

## 核心功能

- **接口管理**：注册和管理数据接口，支持分类、请求头、请求参数配置
- **数据资产管理**：数据资产注册、目录管理、字段定义
- **码表管理**：码表和码值的维护
- **数据标准**：数据标准定义和导入
- **数据库连接管理**：多数据库连接配置和管理
- **脚本管理**：支持 Groovy 和 Python 脚本的分类管理和执行
- **任务编排**：基于 DAG 的可视化任务编排、调度和执行历史追踪
- **认证与权限**：JWT 认证、用户管理、角色权限控制
- **AI 助手**：集成 Coze AI，提供智能辅助功能

## 数据库

共 31 张表，按业务模块分为 11 组：

| 模块       | 核心表                                                  |
| ---------- | ------------------------------------------------------- |
| 接口管理   | api_category, api_header, api_info, api_param           |
| 数据资产   | asset, asset_directory, asset_directory_rel, asset_field |
| 码表       | code_table, code_item                                   |
| 数据标准   | data_standard                                          |
| 数据库连接 | database_connection                                     |
| 脚本管理   | script_category, script, script_param                   |
| 权限管理   | sys_permission, sys_role, sys_role_permission, sys_role_resource, sys_resource, sys_route |
| 认证       | sys_user, sys_user_role, sys_refresh_token              |
| 任务编排   | task_category, task, task_edge, task_execution, task_node, task_node_execution |
| 文件       | sys_file                                                |

建表脚本位于 `dataFactory-api/src/main/resources/sql/datafactory.sql`。

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+

### 后端启动

1. 创建 MySQL 数据库并执行建表脚本：

```sql
CREATE DATABASE datafactory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后导入 `dataFactory-api/src/main/resources/sql/datafactory.sql`。

2. 修改 `dataFactory-api/src/main/resources/application.yaml` 中的数据库和 Redis 连接信息。

3. 启动后端：

```bash
mvn clean install -DskipTests
cd dataFactory-api
mvn spring-boot:run
```

服务启动后访问 `http://localhost:8080`，Swagger 文档位于 `http://localhost:8080/doc.html`。

### 前端启动

```bash
cd dataFactory-ui
npm install
npm run dev
```

开发服务器默认运行在 `http://localhost:5173`。

## 默认配置

| 配置项   | 默认值                            |
| -------- | --------------------------------- |
| 服务端口 | 8080                              |
| JWT 有效期 | 访问令牌 2h，刷新令牌 7d           |
| 文件上传 | 最大 100MB                        |
| 脚本存储 | ./upload/script                   |

## 项目结构

```
DataFactory
├── pom.xml                              # Maven 根 POM
├── dataFactory-common/
│   ├── pom.xml
│   └── src/main/java/com/datafactory/
│       ├── config/                      # 配置属性类
│       ├── enums/                       # 枚举
│       ├── exception/                   # 异常类
│       ├── model/dto/                   # 请求 DTO
│       ├── model/vo/                    # 响应 VO
│       ├── response/                    # 统一响应封装
│       └── utils/                       # 工具类
├── dataFactory-core/
│   ├── pom.xml
│   └── src/main/java/com/datafactory/
│       ├── config/                      # MyBatis-Plus/Redis/Swagger 配置
│       ├── domain/entity/               # 实体类
│       ├── domain/mapper/               # Mapper 接口
│       ├── executor/                    # 脚本执行器
│       ├── model/                       # 业务模型
│       └── service/                     # Service 接口和实现
├── dataFactory-api/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/datafactory/
│       │   ├── config/                  # WebMVC/CORS 配置
│       │   ├── controller/              # REST API
│       │   └── DataFactoryApplication.java
│       └── resources/
│           ├── application.yaml         # 应用配置
│           └── sql/datafactory.sql      # 建表脚本
└── dataFactory-ui/
    ├── package.json
    └── src/
        ├── api/                         # API 调用
        ├── components/                  # 通用组件
        ├── layouts/                     # 布局
        ├── router/                      # 路由
        ├── stores/                      # Pinia 状态
        ├── views/                       # 页面
        └── types/                       # 类型定义
```
