-- =====================================================
-- 数据工厂大数据平台 - 数据库初始化脚本
-- 基于 API接口文档 V1.0 设计
-- 数据库: MySQL 8.0+
-- =====================================================

CREATE DATABASE IF NOT EXISTS datafactory
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE datafactory;

-- =====================================================
-- 1. 认证与授权模块
-- =====================================================

-- 1.1 系统用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username       VARCHAR(20)  NOT NULL COMMENT '用户名',
  password       VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  nickname       VARCHAR(20)  NULL     COMMENT '昵称',
  email          VARCHAR(64)  NULL     COMMENT '邮箱',
  mobile         VARCHAR(11)  NULL     COMMENT '手机号',
  status         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=停用',
  remark         VARCHAR(200) NULL     COMMENT '备注',
  last_login_time DATETIME    NULL     COMMENT '最后登录时间',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by      VARCHAR(32)  NULL     COMMENT '创建人',
  update_by      VARCHAR(32)  NULL     COMMENT '更新人',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_username (username),
  UNIQUE INDEX uk_email (email),
  UNIQUE INDEX uk_mobile (mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 1.2 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name   VARCHAR(32)  NOT NULL COMMENT '角色名称',
  role_code   VARCHAR(32)  NOT NULL COMMENT '角色编码',
  description VARCHAR(200) NULL     COMMENT '描述',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 1.3 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 1.4 权限表
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  permission_code VARCHAR(64)  NOT NULL COMMENT '权限标识',
  permission_name VARCHAR(32)  NOT NULL COMMENT '权限名称',
  module          VARCHAR(32)  NOT NULL COMMENT '所属模块',
  description     VARCHAR(200) NULL     COMMENT '描述',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 1.5 角色权限关联表
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
  id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  role_id       BIGINT NOT NULL COMMENT '角色ID',
  permission_id BIGINT NOT NULL COMMENT '权限ID',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 1.6 刷新令牌表
DROP TABLE IF EXISTS sys_refresh_token;
CREATE TABLE sys_refresh_token (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '令牌ID',
  user_id     BIGINT       NOT NULL COMMENT '用户ID',
  token       VARCHAR(512) NOT NULL COMMENT '刷新令牌',
  expires_at  DATETIME     NOT NULL COMMENT '过期时间',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  INDEX idx_user_id (user_id),
  INDEX idx_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刷新令牌表';

-- 1.7 系统资源表（前端菜单/权限点）
DROP TABLE IF EXISTS sys_resource;
CREATE TABLE sys_resource (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  resource_code VARCHAR(64)  NOT NULL COMMENT '资源唯一编码',
  resource_name VARCHAR(100) NOT NULL COMMENT '资源名称（菜单显示名称）',
  resource_type TINYINT      NOT NULL COMMENT '资源类型：1=菜单，2=按钮/权限点，3=接口/API',
  parent_id     BIGINT       NOT NULL DEFAULT 0 COMMENT '父资源ID，0=顶级菜单',
  path          VARCHAR(255) NULL     COMMENT '前端路由路径（菜单类型有值，按钮/API为NULL）',
  icon          VARCHAR(100) NULL     COMMENT '图标名称',
  sort_order    INT          NOT NULL DEFAULT 0 COMMENT '同级排序（数值越小越靠前）',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME     NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_resource_code (resource_code),
  INDEX idx_parent_id (parent_id),
  INDEX idx_path (path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统资源表';

-- 1.8 角色资源关联表
DROP TABLE IF EXISTS sys_role_resource;
CREATE TABLE sys_role_resource (
  id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  role_id     BIGINT NOT NULL COMMENT '角色ID',
  resource_id BIGINT NOT NULL COMMENT '资源ID',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_role_resource (role_id, resource_id),
  INDEX idx_role_id (role_id),
  INDEX idx_resource_id (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色资源关联表';

-- =====================================================
-- 2. 路由管理模块
-- =====================================================

-- 2.1 系统路由表
DROP TABLE IF EXISTS sys_route;
CREATE TABLE sys_route (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '路由ID',
  route_name    VARCHAR(64)  NOT NULL COMMENT '路由名称',
  route_path    VARCHAR(255) NOT NULL COMMENT '路由路径',
  method        VARCHAR(8)   NOT NULL COMMENT '请求方法: GET/POST/PUT/DELETE',
  module        VARCHAR(32)  NOT NULL COMMENT '所属模块',
  permission    VARCHAR(64)  NULL     COMMENT '所需权限标识',
  is_auth       TINYINT      NOT NULL DEFAULT 1 COMMENT '是否需要认证: 0-公开, 1-需认证',
  description   VARCHAR(100) NULL     COMMENT '路由说明',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_route_path_method (route_path, method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统路由表';

-- =====================================================
-- 3. 接口管理模块
-- =====================================================

-- 3.1 接口分类表
DROP TABLE IF EXISTS api_category;
CREATE TABLE api_category (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  name        VARCHAR(50)  NOT NULL COMMENT '分类名称',
  parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父级分类ID，顶级为0',
  level       INT          NOT NULL DEFAULT 1 COMMENT '层级',
  sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by   VARCHAR(32)  NULL     COMMENT '创建人',
  update_by   VARCHAR(32)  NULL     COMMENT '更新人',
  deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口分类表';

-- 3.2 注册接口表
DROP TABLE IF EXISTS api_info;
CREATE TABLE api_info (
  id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '接口ID',
  api_name         VARCHAR(30)  NOT NULL COMMENT '接口名称',
  api_description  VARCHAR(200) NULL     COMMENT '接口说明',
  category_id      BIGINT       NOT NULL COMMENT '所属分类ID',
  source           VARCHAR(50)  NOT NULL COMMENT '接口来源',
  protocol         VARCHAR(8)   NOT NULL DEFAULT 'HTTP' COMMENT '协议: HTTP/HTTPS',
  method           VARCHAR(8)   NOT NULL COMMENT '请求方法: GET/POST/PUT/DELETE',
  url              VARCHAR(500) NOT NULL COMMENT '接口URL路径',
  timeout          INT          NOT NULL DEFAULT 30000 COMMENT '超时时间(毫秒)',
  retry_count      INT          NOT NULL DEFAULT 0 COMMENT '重试次数',
  status           TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  version          VARCHAR(20)  NOT NULL DEFAULT '1.0.0' COMMENT '当前版本号',
  response_example TEXT         NULL     COMMENT '响应示例',
  remark           VARCHAR(500) NULL     COMMENT '备注',
  create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by        VARCHAR(32)  NULL     COMMENT '创建人',
  update_by        VARCHAR(32)  NULL     COMMENT '更新人',
  deleted          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  INDEX idx_category_id (category_id),
  INDEX idx_status (status),
  INDEX idx_api_name (api_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='注册接口表';

-- 3.3 接口请求头配置表
DROP TABLE IF EXISTS api_header;
CREATE TABLE api_header (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '请求头ID',
  api_id       BIGINT       NOT NULL COMMENT '接口ID',
  header_key   VARCHAR(100) NOT NULL COMMENT '请求头名称',
  header_value VARCHAR(500) NULL     COMMENT '请求头值',
  required     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否必填: 0-否, 1-是',
  description  VARCHAR(200) NULL     COMMENT '说明',
  sort_order   INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (id),
  INDEX idx_api_id (api_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口请求头配置表';

-- 3.4 接口请求参数配置表
DROP TABLE IF EXISTS api_param;
CREATE TABLE api_param (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '参数ID',
  api_id          BIGINT       NOT NULL COMMENT '接口ID',
  param_name      VARCHAR(100) NOT NULL COMMENT '参数名称',
  param_type      VARCHAR(16)  NOT NULL COMMENT '参数类型: QUERY/PATH/HEADER/BODY',
  data_type       VARCHAR(16)  NOT NULL COMMENT '数据类型: STRING/INTEGER/LONG/DOUBLE/BOOLEAN/DATE/DATETIME/OBJECT/ARRAY',
  required        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否必填: 0-否, 1-是',
  description     VARCHAR(200) NULL     COMMENT '参数说明',
  default_value   VARCHAR(500) NULL     COMMENT '默认值',
  example_value   VARCHAR(500) NULL     COMMENT '示例值',
  sort_order      INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  validation_rule VARCHAR(500) NULL     COMMENT '校验规则(正则表达式)',
  min_value       VARCHAR(50)  NULL     COMMENT '最小值',
  max_value       VARCHAR(50)  NULL     COMMENT '最大值',
  PRIMARY KEY (id),
  INDEX idx_api_id (api_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口请求参数配置表';

-- =====================================================
-- 4. 数据库管理模块
-- =====================================================

-- 4.1 数据库连接表
DROP TABLE IF EXISTS database_connection;
CREATE TABLE database_connection (
  id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '连接ID',
  connection_name  VARCHAR(50)  NOT NULL COMMENT '连接名称',
  db_type          VARCHAR(16)  NOT NULL COMMENT '数据库类型: MYSQL/POSTGRESQL/ORACLE/SQLSERVER/HIVE/CLICKHOUSE',
  host             VARCHAR(255) NOT NULL COMMENT '主机地址',
  port             INT          NOT NULL COMMENT '端口号',
  database_name    VARCHAR(100) NOT NULL COMMENT '数据库名称',
  username         VARCHAR(100) NOT NULL COMMENT '连接用户名',
  password         VARCHAR(512) NOT NULL COMMENT '连接密码(加密存储)',
  jdbc_params      VARCHAR(500) NULL     COMMENT 'JDBC额外连接参数',
  description      VARCHAR(200) NULL     COMMENT '描述说明',
  status           TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  last_test_time   DATETIME     NULL     COMMENT '最近测试连接时间',
  last_test_result TINYINT      NULL     COMMENT '最近测试结果: 1=成功, 0=失败',
  create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by        VARCHAR(32)  NULL     COMMENT '创建人',
  update_by        VARCHAR(32)  NULL     COMMENT '更新人',
  deleted          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  INDEX idx_db_type (db_type),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库连接表';

-- =====================================================
-- 5. 数据标准管理模块
-- =====================================================

-- 5.1 数据标准表（支持目录树和标准项）
DROP TABLE IF EXISTS data_standard;
CREATE TABLE data_standard (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '标准ID',
  name          VARCHAR(50)  NOT NULL COMMENT '标准名称/目录名称',
  parent_id     BIGINT       NOT NULL DEFAULT 0 COMMENT '父节点ID，根目录传0',
  level         INT          NOT NULL DEFAULT 1 COMMENT '层级',
  type          VARCHAR(16)  NOT NULL COMMENT '节点类型: FOLDER/STANDARD',
  standard_code VARCHAR(50)  NULL     COMMENT '标准编码(全局唯一, type=STANDARD时必填)',
  data_type     VARCHAR(16)  NULL     COMMENT '数据类型(type=STANDARD时必填)',
  length        INT          NULL     COMMENT '数据长度',
  `precision`   INT          NULL     COMMENT '精度(小数位)',
  default_value VARCHAR(200) NULL     COMMENT '默认值',
  value_range   VARCHAR(200) NULL     COMMENT '取值范围说明',
  description   VARCHAR(500) NULL     COMMENT '说明',
  status        TINYINT      NULL     COMMENT '状态(type=STANDARD时有效): 0=未发布, 1=已发布, 2=已停用',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by     VARCHAR(32)  NULL     COMMENT '创建人',
  update_by     VARCHAR(32)  NULL     COMMENT '更新人',
  deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_standard_code (standard_code),
  INDEX idx_parent_id (parent_id),
  INDEX idx_type (type),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据标准表';

-- =====================================================
-- 6. 码表管理模块
-- =====================================================

-- 6.1 码表
DROP TABLE IF EXISTS code_table;
CREATE TABLE code_table (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '码表ID',
  table_name  VARCHAR(50)  NOT NULL COMMENT '码表名称',
  table_code  VARCHAR(50)  NOT NULL COMMENT '码表编码(全局唯一)',
  description VARCHAR(200) NULL     COMMENT '说明',
  status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by   VARCHAR(32)  NULL     COMMENT '创建人',
  update_by   VARCHAR(32)  NULL     COMMENT '更新人',
  deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_table_code (table_code),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='码表';

-- 6.2 码值表
DROP TABLE IF EXISTS code_item;
CREATE TABLE code_item (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '码值ID',
  table_id    BIGINT       NOT NULL COMMENT '所属码表ID',
  code        VARCHAR(100) NOT NULL COMMENT '编码',
  name        VARCHAR(100) NOT NULL COMMENT '名称',
  value       VARCHAR(500) NOT NULL COMMENT '值',
  sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  parent_code VARCHAR(100) NULL     COMMENT '父级编码(层级码表)',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '码值状态: 1=启用, 0=停用',
  description VARCHAR(200) NULL     COMMENT '说明',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_table_code (table_id, code),
  INDEX idx_table_id (table_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='码值表';

-- =====================================================
-- 7. 数据资产管理模块
-- =====================================================

-- 7.1 数据资产表
DROP TABLE IF EXISTS asset;
CREATE TABLE asset (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '资产ID',
  asset_name   VARCHAR(50)  NOT NULL COMMENT '资产表名称',
  asset_code   VARCHAR(50)  NOT NULL COMMENT '资产表编码(全局唯一)',
  parent_id    BIGINT       NOT NULL DEFAULT 0 COMMENT '上级目录ID',
  type         VARCHAR(16)  NOT NULL DEFAULT 'ASSET' COMMENT '节点类型: FOLDER/ASSET',
  source_type  VARCHAR(16)  NULL     COMMENT '来源类型: DATABASE/API/MANUAL',
  source_id    BIGINT       NULL     COMMENT '来源ID(关联数据库连接或注册接口)',
  source_table VARCHAR(100) NULL     COMMENT '来源表名',
  description  VARCHAR(500) NULL     COMMENT '说明',
  status       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by    VARCHAR(32)  NULL     COMMENT '创建人',
  update_by    VARCHAR(32)  NULL     COMMENT '更新人',
  deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_asset_code (asset_code),
  INDEX idx_parent_id (parent_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据资产表';

-- 7.2 资产字段定义表
DROP TABLE IF EXISTS asset_field;
CREATE TABLE asset_field (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字段ID',
  asset_id        BIGINT       NOT NULL COMMENT '所属资产ID',
  field_name      VARCHAR(100) NOT NULL COMMENT '字段名称',
  field_type      VARCHAR(32)  NOT NULL COMMENT '字段类型',
  field_length    INT          NULL     COMMENT '字段长度',
  field_precision INT          NULL     COMMENT '精度',
  is_primary_key  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否主键: 0-否, 1-是',
  is_nullable     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可为空: 0-否, 1-是',
  default_value   VARCHAR(200) NULL     COMMENT '默认值',
  description     VARCHAR(200) NULL     COMMENT '字段说明',
  standard_id     BIGINT       NULL     COMMENT '关联数据标准ID',
  sort_order      INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (id),
  INDEX idx_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产字段定义表';

-- 7.3 资产标签表
DROP TABLE IF EXISTS asset_tag;
CREATE TABLE asset_tag (
  id       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  asset_id BIGINT      NOT NULL COMMENT '资产ID',
  tag      VARCHAR(50) NOT NULL COMMENT '标签',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_asset_tag (asset_id, tag),
  INDEX idx_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产标签表';

-- =====================================================
-- 8. 脚本管理模块
-- =====================================================

-- 8.1 脚本表
DROP TABLE IF EXISTS script;
CREATE TABLE script (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '脚本ID',
  script_name    VARCHAR(50)  NOT NULL COMMENT '脚本名称',
  script_type    VARCHAR(16)  NOT NULL COMMENT '脚本类型: SQL/PYTHON/SHELL/SPARK/FLINK',
  script_content LONGTEXT     NOT NULL COMMENT '脚本内容',
  description    VARCHAR(500) NULL     COMMENT '说明',
  version        VARCHAR(20)  NOT NULL DEFAULT '1.0.0' COMMENT '当前版本号',
  status         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by      VARCHAR(32)  NULL     COMMENT '创建人',
  update_by      VARCHAR(32)  NULL     COMMENT '更新人',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  INDEX idx_script_type (script_type),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='脚本表';

-- 8.2 脚本版本历史表
DROP TABLE IF EXISTS script_version;
CREATE TABLE script_version (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  script_id      BIGINT       NOT NULL COMMENT '脚本ID',
  version        VARCHAR(20)  NOT NULL COMMENT '版本号',
  script_content LONGTEXT     NOT NULL COMMENT '脚本内容',
  change_log     VARCHAR(500) NULL     COMMENT '变更说明',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  create_by      VARCHAR(32)  NULL     COMMENT '创建人',
  PRIMARY KEY (id),
  INDEX idx_script_id (script_id),
  INDEX idx_script_version (script_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='脚本版本历史表';

-- 8.3 脚本参数定义表
DROP TABLE IF EXISTS script_param;
CREATE TABLE script_param (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '参数ID',
  script_id     BIGINT       NOT NULL COMMENT '脚本ID',
  param_name    VARCHAR(100) NOT NULL COMMENT '参数名称',
  param_type    VARCHAR(16)  NOT NULL COMMENT '参数类型: STRING/INTEGER/LONG/DOUBLE/BOOLEAN/DATE',
  required      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否必填: 0-否, 1-是',
  default_value VARCHAR(200) NULL     COMMENT '默认值',
  description   VARCHAR(200) NULL     COMMENT '参数说明',
  PRIMARY KEY (id),
  INDEX idx_script_id (script_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='脚本参数定义表';

-- =====================================================
-- 9. 任务管理模块
-- =====================================================

-- 9.1 任务表
DROP TABLE IF EXISTS task;
CREATE TABLE task (
  id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  task_name         VARCHAR(50)  NOT NULL COMMENT '任务名称',
  task_description  VARCHAR(500) NULL     COMMENT '任务说明',
  schedule_type     VARCHAR(16)  NOT NULL DEFAULT 'MANUAL' COMMENT '调度类型: MANUAL/CRON/EVENT',
  cron_expression   VARCHAR(100) NULL     COMMENT 'Cron表达式(scheduleType=CRON时有效)',
  event_type        VARCHAR(32)  NULL     COMMENT '事件类型(scheduleType=EVENT时有效)',
  effective_date    DATETIME     NULL     COMMENT '生效日期',
  expire_date       DATETIME     NULL     COMMENT '失效日期',
  pause_on_failure  TINYINT      NOT NULL DEFAULT 1 COMMENT '失败后暂停调度: 0-否, 1-是',
  task_timeout      INT          NOT NULL DEFAULT 60 COMMENT '任务超时时间(分钟)',
  retry_count       INT          NOT NULL DEFAULT 0 COMMENT '失败重试次数',
  retry_interval    INT          NOT NULL DEFAULT 5 COMMENT '重试间隔(分钟)',
  alert_email       VARCHAR(200) NULL     COMMENT '告警邮箱',
  status            TINYINT      NOT NULL DEFAULT 0 COMMENT '发布状态: 0=未发布, 1=已发布, 2=已停用',
  execute_status    TINYINT      NULL     COMMENT '最近执行状态: 0=等待, 1=执行中, 2=成功, 3=失败, 4=已取消',
  last_execute_time DATETIME     NULL     COMMENT '最近执行时间',
  next_execute_time DATETIME     NULL     COMMENT '下次执行时间',
  create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by         VARCHAR(32)  NULL     COMMENT '创建人',
  update_by         VARCHAR(32)  NULL     COMMENT '更新人',
  deleted           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  INDEX idx_status (status),
  INDEX idx_execute_status (execute_status),
  INDEX idx_schedule_type (schedule_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 9.2 任务节点表（DAG节点）
DROP TABLE IF EXISTS task_node;
CREATE TABLE task_node (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '节点记录ID',
  task_id     BIGINT       NOT NULL COMMENT '任务ID',
  node_id     VARCHAR(50)  NOT NULL COMMENT '节点标识(DAG图中唯一, 如node_1)',
  node_name   VARCHAR(100) NOT NULL COMMENT '节点名称',
  node_type   VARCHAR(16)  NOT NULL COMMENT '节点类型: START/API/SCRIPT/MAPPING/OUTPUT/END',
  position_x  DOUBLE       NULL     COMMENT '画布X坐标',
  position_y  DOUBLE       NULL     COMMENT '画布Y坐标',
  node_config JSON         NULL     COMMENT '节点配置(JSON格式, 根据nodeType不同结构不同)',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_task_node (task_id, node_id),
  INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务节点表';

-- 9.3 任务边表（DAG连线）
DROP TABLE IF EXISTS task_edge;
CREATE TABLE task_edge (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '连线记录ID',
  task_id        BIGINT       NOT NULL COMMENT '任务ID',
  edge_id        VARCHAR(50)  NOT NULL COMMENT '连线标识(如edge_1)',
  source_node_id VARCHAR(50)  NOT NULL COMMENT '源节点ID',
  target_node_id VARCHAR(50)  NOT NULL COMMENT '目标节点ID',
  `condition`    VARCHAR(500) NULL     COMMENT '条件表达式',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_task_edge (task_id, edge_id),
  INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务边表';

-- 9.4 任务执行历史表
DROP TABLE IF EXISTS task_execution;
CREATE TABLE task_execution (
  id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '执行记录ID',
  task_id        BIGINT      NOT NULL COMMENT '任务ID',
  status         TINYINT     NOT NULL COMMENT '执行状态: 0=等待, 1=执行中, 2=成功, 3=失败, 4=已取消',
  trigger_type   VARCHAR(16) NOT NULL COMMENT '触发方式: MANUAL/CRON/EVENT',
  trigger_by     VARCHAR(32) NULL     COMMENT '触发人',
  task_params    JSON        NULL     COMMENT '任务参数(JSON)',
  debug_mode     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否调试模式: 0-否, 1-是',
  start_time     DATETIME    NULL     COMMENT '开始时间',
  end_time       DATETIME    NULL     COMMENT '结束时间',
  total_duration BIGINT      NULL     COMMENT '总耗时(毫秒)',
  create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  INDEX idx_task_id (task_id),
  INDEX idx_status (status),
  INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务执行历史表';

-- 9.5 任务节点执行结果表
DROP TABLE IF EXISTS task_node_execution;
CREATE TABLE task_node_execution (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '节点执行ID',
  execution_id  BIGINT       NOT NULL COMMENT '执行记录ID',
  task_id       BIGINT       NOT NULL COMMENT '任务ID',
  node_id       VARCHAR(50)  NOT NULL COMMENT '节点标识',
  node_name     VARCHAR(100) NOT NULL COMMENT '节点名称',
  node_type     VARCHAR(16)  NOT NULL COMMENT '节点类型',
  status        TINYINT      NOT NULL COMMENT '执行状态: 2=成功, 3=失败, 5=跳过',
  start_time    DATETIME     NULL     COMMENT '节点开始时间',
  end_time      DATETIME     NULL     COMMENT '节点结束时间',
  duration      BIGINT       NULL     COMMENT '节点耗时(毫秒)',
  input_data    TEXT         NULL     COMMENT '节点输入数据摘要',
  output_data   TEXT         NULL     COMMENT '节点输出数据摘要',
  error_message TEXT         NULL     COMMENT '错误信息(失败时返回)',
  logs          LONGTEXT     NULL     COMMENT '节点执行日志',
  PRIMARY KEY (id),
  INDEX idx_execution_id (execution_id),
  INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务节点执行结果表';

-- =====================================================
-- 10. 初始化数据
-- =====================================================

-- 10.1 初始化路由数据
INSERT INTO sys_route (route_name, route_path, method, module, permission, is_auth, description) VALUES
-- 认证模块
('用户注册', '/api/v1/auth/register', 'POST', 'auth', NULL, 0, '新用户注册'),
('用户登录', '/api/v1/auth/login', 'POST', 'auth', NULL, 0, '用户登录'),
('用户登出', '/api/v1/auth/logout', 'POST', 'auth', NULL, 1, '退出登录'),
('获取当前用户信息', '/api/v1/auth/user-info', 'GET', 'auth', NULL, 1, '获取当前用户信息'),
('刷新令牌', '/api/v1/auth/refresh-token', 'POST', 'auth', NULL, 0, '刷新访问令牌'),
('修改密码', '/api/v1/auth/password', 'PUT', 'auth', NULL, 1, '修改密码'),

-- 接口分类管理
('查询接口分类树', '/api/v1/api-categories/tree', 'GET', 'api', 'api:read', 1, '获取全量接口分类树'),
('新增接口分类', '/api/v1/api-categories', 'POST', 'api', 'api:write', 1, '新增接口分类'),
('编辑接口分类', '/api/v1/api-categories/{id}', 'PUT', 'api', 'api:write', 1, '编辑接口分类'),
('删除接口分类', '/api/v1/api-categories/{id}', 'DELETE', 'api', 'api:write', 1, '删除接口分类'),

-- 接口注册管理
('查询接口列表', '/api/v1/apis', 'GET', 'api', 'api:read', 1, '分页查询接口列表'),
('查询接口详情', '/api/v1/apis/{id}', 'GET', 'api', 'api:read', 1, '查询接口详情'),
('新增接口', '/api/v1/apis', 'POST', 'api', 'api:write', 1, '新增注册接口'),
('编辑接口', '/api/v1/apis/{id}', 'PUT', 'api', 'api:write', 1, '编辑接口'),
('发布接口', '/api/v1/apis/{id}/publish', 'PUT', 'api', 'api:write', 1, '发布接口'),
('停用接口', '/api/v1/apis/{id}/disable', 'PUT', 'api', 'api:write', 1, '停用接口'),
('删除接口', '/api/v1/apis/{id}', 'DELETE', 'api', 'api:write', 1, '删除接口'),
('批量发布接口', '/api/v1/apis/batch/publish', 'PUT', 'api', 'api:write', 1, '批量发布接口'),
('批量停用接口', '/api/v1/apis/batch/disable', 'PUT', 'api', 'api:write', 1, '批量停用接口'),
('批量修改接口分类', '/api/v1/apis/batch/category', 'PUT', 'api', 'api:write', 1, '批量分类'),
('接口测试调用', '/api/v1/apis/{id}/test', 'POST', 'api', 'api:read', 1, '在线测试调用接口'),

-- 数据库连接管理
('查询数据库连接列表', '/api/v1/databases', 'GET', 'database', 'database:read', 1, '分页查询数据库连接列表'),
('查询数据库连接详情', '/api/v1/databases/{id}', 'GET', 'database', 'database:read', 1, '查询数据库连接详情'),
('新增数据库连接', '/api/v1/databases', 'POST', 'database', 'database:write', 1, '新增数据库连接'),
('编辑数据库连接', '/api/v1/databases/{id}', 'PUT', 'database', 'database:write', 1, '编辑数据库连接'),
('测试数据库连接', '/api/v1/databases/{id}/test', 'POST', 'database', 'database:read', 1, '测试数据库连接'),
('发布数据库连接', '/api/v1/databases/{id}/publish', 'PUT', 'database', 'database:write', 1, '发布数据库连接'),
('停用数据库连接', '/api/v1/databases/{id}/disable', 'PUT', 'database', 'database:write', 1, '停用数据库连接'),
('删除数据库连接', '/api/v1/databases/{id}', 'DELETE', 'database', 'database:write', 1, '删除数据库连接'),
('批量发布数据库连接', '/api/v1/databases/batch/publish', 'PUT', 'database', 'database:write', 1, '批量发布数据库连接'),
('批量停用数据库连接', '/api/v1/databases/batch/disable', 'PUT', 'database', 'database:write', 1, '批量停用数据库连接'),

-- 数据标准管理
('查询数据标准目录树', '/api/v1/data-standards/tree', 'GET', 'standard', 'standard:read', 1, '获取数据标准目录树'),
('查询数据标准详情', '/api/v1/data-standards/{id}', 'GET', 'standard', 'standard:read', 1, '查询数据标准详情'),
('新增数据标准', '/api/v1/data-standards', 'POST', 'standard', 'standard:write', 1, '新增数据标准'),
('编辑数据标准', '/api/v1/data-standards/{id}', 'PUT', 'standard', 'standard:write', 1, '编辑数据标准'),
('删除数据标准', '/api/v1/data-standards/{id}', 'DELETE', 'standard', 'standard:write', 1, '删除数据标准'),
('发布数据标准', '/api/v1/data-standards/{id}/publish', 'PUT', 'standard', 'standard:write', 1, '发布数据标准'),
('停用数据标准', '/api/v1/data-standards/{id}/disable', 'PUT', 'standard', 'standard:write', 1, '停用数据标准'),
('批量发布数据标准', '/api/v1/data-standards/batch/publish', 'PUT', 'standard', 'standard:write', 1, '批量发布数据标准'),
('批量停用数据标准', '/api/v1/data-standards/batch/disable', 'PUT', 'standard', 'standard:write', 1, '批量停用数据标准'),

-- 码表管理
('查询码表列表', '/api/v1/code-tables', 'GET', 'codetable', 'standard:read', 1, '分页查询码表列表'),
('查询码表详情', '/api/v1/code-tables/{id}', 'GET', 'codetable', 'standard:read', 1, '查询码表详情'),
('查询码值列表', '/api/v1/code-tables/{id}/items', 'GET', 'codetable', 'standard:read', 1, '查询码表下的码值列表'),
('新增码表', '/api/v1/code-tables', 'POST', 'codetable', 'standard:write', 1, '新增码表'),
('编辑码表', '/api/v1/code-tables/{id}', 'PUT', 'codetable', 'standard:write', 1, '编辑码表'),
('新增码值', '/api/v1/code-tables/{tableId}/items', 'POST', 'codetable', 'standard:write', 1, '新增码值'),
('更新码值', '/api/v1/code-tables/{tableId}/items/{itemId}', 'PUT', 'codetable', 'standard:write', 1, '更新码值'),
('删除码值', '/api/v1/code-tables/{tableId}/items/{itemId}', 'DELETE', 'codetable', 'standard:write', 1, '删除码值'),
('发布码表', '/api/v1/code-tables/{id}/publish', 'PUT', 'codetable', 'standard:write', 1, '发布码表'),
('停用码表', '/api/v1/code-tables/{id}/disable', 'PUT', 'codetable', 'standard:write', 1, '停用码表'),
('删除码表', '/api/v1/code-tables/{id}', 'DELETE', 'codetable', 'standard:write', 1, '删除码表'),
('批量发布码表', '/api/v1/code-tables/batch/publish', 'PUT', 'codetable', 'standard:write', 1, '批量发布码表'),
('批量停用码表', '/api/v1/code-tables/batch/disable', 'PUT', 'codetable', 'standard:write', 1, '批量停用码表'),

-- 数据资产管理
('查询数据资产目录树', '/api/v1/assets/tree', 'GET', 'asset', 'asset:read', 1, '获取数据资产目录树'),
('查询数据资产详情', '/api/v1/assets/{id}', 'GET', 'asset', 'asset:read', 1, '查询数据资产详情'),
('新增数据资产表', '/api/v1/assets', 'POST', 'asset', 'asset:write', 1, '新增数据资产表'),
('编辑数据资产表', '/api/v1/assets/{id}', 'PUT', 'asset', 'asset:write', 1, '编辑数据资产表'),
('删除数据资产', '/api/v1/assets/{id}', 'DELETE', 'asset', 'asset:write', 1, '删除数据资产表'),
('发布数据资产', '/api/v1/assets/{id}/publish', 'PUT', 'asset', 'asset:write', 1, '发布数据资产'),
('停用数据资产', '/api/v1/assets/{id}/disable', 'PUT', 'asset', 'asset:write', 1, '停用数据资产'),
('批量发布数据资产', '/api/v1/assets/batch/publish', 'PUT', 'asset', 'asset:write', 1, '批量发布数据资产'),
('批量停用数据资产', '/api/v1/assets/batch/disable', 'PUT', 'asset', 'asset:write', 1, '批量停用数据资产'),

-- 脚本管理
('查询脚本列表', '/api/v1/scripts', 'GET', 'script', 'script:read', 1, '分页查询脚本列表'),
('查询脚本详情', '/api/v1/scripts/{id}', 'GET', 'script', 'script:read', 1, '查询脚本详情'),
('新增脚本', '/api/v1/scripts', 'POST', 'script', 'script:write', 1, '新增脚本'),
('编辑脚本', '/api/v1/scripts/{id}', 'PUT', 'script', 'script:write', 1, '编辑脚本'),
('查询脚本版本历史', '/api/v1/scripts/{id}/versions', 'GET', 'script', 'script:read', 1, '查询脚本版本历史'),
('在线调试脚本', '/api/v1/scripts/{id}/debug', 'POST', 'script', 'script:read', 1, '在线调试脚本'),
('发布脚本', '/api/v1/scripts/{id}/publish', 'PUT', 'script', 'script:write', 1, '发布脚本'),
('停用脚本', '/api/v1/scripts/{id}/disable', 'PUT', 'script', 'script:write', 1, '停用脚本'),
('删除脚本', '/api/v1/scripts/{id}', 'DELETE', 'script', 'script:write', 1, '删除脚本'),
('批量发布脚本', '/api/v1/scripts/batch/publish', 'PUT', 'script', 'script:write', 1, '批量发布脚本'),
('批量停用脚本', '/api/v1/scripts/batch/disable', 'PUT', 'script', 'script:write', 1, '批量停用脚本'),

-- 任务管理
('查询任务列表', '/api/v1/tasks', 'GET', 'task', 'task:read', 1, '分页查询任务列表'),
('查询任务详情', '/api/v1/tasks/{id}', 'GET', 'task', 'task:read', 1, '查询任务详情'),
('新增任务', '/api/v1/tasks', 'POST', 'task', 'task:write', 1, '新增任务'),
('编辑任务', '/api/v1/tasks/{id}', 'PUT', 'task', 'task:write', 1, '编辑任务'),
('设置任务触发方式', '/api/v1/tasks/{id}/trigger-config', 'PUT', 'task', 'task:write', 1, '设置任务触发方式'),
('测试运行任务', '/api/v1/tasks/{id}/test-run', 'POST', 'task', 'task:execute', 1, '测试运行DAG流程'),
('手动执行任务', '/api/v1/tasks/{id}/execute', 'POST', 'task', 'task:execute', 1, '手动执行任务'),
('停止执行', '/api/v1/tasks/{id}/executions/{executionId}/cancel', 'POST', 'task', 'task:execute', 1, '停止正在执行的任务'),
('查询任务执行历史', '/api/v1/tasks/{id}/executions', 'GET', 'task', 'task:read', 1, '查询任务执行历史'),
('发布任务', '/api/v1/tasks/{id}/publish', 'PUT', 'task', 'task:write', 1, '发布任务'),
('停用任务', '/api/v1/tasks/{id}/disable', 'PUT', 'task', 'task:write', 1, '停用任务'),
('删除任务', '/api/v1/tasks/{id}', 'DELETE', 'task', 'task:write', 1, '删除任务'),
('批量发布任务', '/api/v1/tasks/batch/publish', 'PUT', 'task', 'task:write', 1, '批量发布任务'),
('批量停用任务', '/api/v1/tasks/batch/disable', 'PUT', 'task', 'task:write', 1, '批量停用任务'),

-- 通用接口
('文件上传', '/api/v1/common/upload', 'POST', 'common', NULL, 1, '文件上传'),
('文件下载', '/api/v1/common/download/{fileId}', 'GET', 'common', NULL, 1, '文件下载'),
('数据导出', '/api/v1/common/export', 'POST', 'common', NULL, 1, '数据导出'),
('数据导入', '/api/v1/common/import', 'POST', 'common', NULL, 1, '数据导入');

-- 10.2 初始化权限数据
INSERT INTO sys_permission (permission_code, permission_name, module) VALUES
('api:read', '接口查询', '接口管理'),
('api:write', '接口管理', '接口管理'),
('database:read', '数据库连接查询', '数据库管理'),
('database:write', '数据库连接管理', '数据库管理'),
('standard:read', '数据标准查询', '数据标准'),
('standard:write', '数据标准管理', '数据标准'),
('asset:read', '数据资产查询', '数据资产'),
('asset:write', '数据资产管理', '数据资产'),
('script:read', '脚本查询', '脚本管理'),
('script:write', '脚本管理', '脚本管理'),
('task:read', '任务查询', '任务管理'),
('task:write', '任务管理', '任务管理'),
('task:execute', '任务执行', '任务管理');

-- 10.3 初始化角色
INSERT INTO sys_role (role_name, role_code, description) VALUES
('系统管理员', 'ADMIN', '系统管理员，拥有全部权限'),
('数据工程师', 'DATA_ENGINEER', '数据工程师角色'),
('数据查看者', 'DATA_VIEWER', '数据查看者，仅有查询权限');

-- 10.4 管理员角色关联全部权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 10.5 数据工程师角色关联权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE permission_code IN (
  'api:read', 'api:write',
  'database:read', 'database:write',
  'standard:read', 'standard:write',
  'asset:read', 'asset:write',
  'script:read', 'script:write',
  'task:read', 'task:write', 'task:execute'
);

-- 10.6 数据查看者角色关联权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission WHERE permission_code LIKE '%:read';

-- 10.7 初始化管理员用户 (密码: Admin@123, BCrypt加密)
INSERT INTO sys_user (username, password, nickname, email, status, remark) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 'admin@datafactory.com', 1, '系统默认管理员');

-- 管理员关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- =====================================================
-- 10.8 初始化前端资源菜单数据
-- path 格式规范：
--   顶级菜单：       /{module}
--   子菜单：         /{parent-path}/{child}
--   按钮/API权限点：  NULL
-- =====================================================

INSERT INTO sys_resource (resource_code, resource_name, resource_type, parent_id, path, icon, sort_order) VALUES

-- ========== 顶级菜单 ==========
('DATA_SOURCE',   '数据源管理',   1, 0, '/data-source',             'datasource', 0),
('DATA_STANDARD', '数据标准管理', 1, 0, '/data-standard',           'standard',   1),
('ASSET_MANAGE',  '数据资产管理', 1, 0, '/asset',                   'asset',      2),
('SCRIPT_MANAGE', '脚本管理',     1, 0, '/script',                  'script',     3),
('TASK_MANAGE',   '任务管理',     1, 0, '/task',                    'task',       4),
('SYSTEM_MANAGE', '系统管理',     1, 0, '/system',                  'setting',   99),

-- ========== 数据源管理 > 接口管理 ==========
('API_MANAGE',      '接口管理',   1, 100, '/data-source/api',       'api',        0),
('DATABASE_MANAGE', '数据库管理', 1, 100, '/data-source/database',  'database',   1),

-- ========== 数据标准管理 ==========
('STANDARD_DIR', '数据标准目录', 1, 200, '/data-standard/directory', 'directory', 0),
('CODE_TABLE',   '码表管理',     1, 200, '/data-standard/code-table','table',     1),

-- ========== 数据资产管理 ==========
('ASSET_DIR', '数据资产目录', 1, 300, '/asset/directory', 'directory', 0),

-- ========== 系统管理 ==========
('USER_MANAGE',  '用户管理', 1, 600, '/system/user',  'user',   0),
('ROLE_MANAGE',  '角色管理', 1, 600, '/system/role',  'avatar', 1),
('ROUTE_MANAGE', '路由管理', 1, 600, '/system/route', 'menu',   2),

-- ========== 按钮级权限点（resource_type=2, path=NULL） ==========
('API_BTN_ADD',     '新增接口',     2, 101, NULL, 'plus',   0),
('API_BTN_EDIT',    '编辑接口',     2, 101, NULL, 'edit',   1),
('API_BTN_DELETE',  '删除接口',     2, 101, NULL, 'delete', 2),
('API_BTN_PUBLISH', '发布接口',     2, 101, NULL, 'check',  3),
('API_BTN_DISABLE', '停用接口',     2, 101, NULL, 'close',  4),
('API_BTN_TEST',    '测试调用',     2, 101, NULL, 'play',   5),
('API_BTN_EXPORT',  '导出',         2, 101, NULL, 'download', 6),

('DB_BTN_ADD',     '新增连接',    2, 102, NULL, 'plus',   0),
('DB_BTN_EDIT',    '编辑连接',    2, 102, NULL, 'edit',   1),
('DB_BTN_DELETE',  '删除连接',    2, 102, NULL, 'delete', 2),
('DB_BTN_PUBLISH', '发布连接',    2, 102, NULL, 'check',  3),
('DB_BTN_DISABLE', '停用连接',    2, 102, NULL, 'close',  4),
('DB_BTN_TEST',    '测试连接',    2, 102, NULL, 'play',   5),

('STD_BTN_ADD',     '新增标准',    2, 201, NULL, 'plus',   0),
('STD_BTN_EDIT',    '编辑标准',    2, 201, NULL, 'edit',   1),
('STD_BTN_DELETE',  '删除标准',    2, 201, NULL, 'delete', 2),
('STD_BTN_PUBLISH', '发布标准',    2, 201, NULL, 'check',  3),
('STD_BTN_DISABLE', '停用标准',    2, 201, NULL, 'close',  4),

('CT_BTN_ADD',      '新增码表',    2, 202, NULL, 'plus',    0),
('CT_BTN_EDIT',     '编辑码表',    2, 202, NULL, 'edit',    1),
('CT_BTN_DELETE',   '删除码表',    2, 202, NULL, 'delete',  2),
('CT_BTN_PUBLISH',  '发布码表',    2, 202, NULL, 'check',   3),
('CT_BTN_DISABLE',  '停用码表',    2, 202, NULL, 'close',   4),
('CT_BTN_ITEM',     '管理码值',    2, 202, NULL, 'list',    5),

('ASSET_BTN_ADD',     '新增资产',    2, 301, NULL, 'plus',   0),
('ASSET_BTN_EDIT',    '编辑资产',    2, 301, NULL, 'edit',   1),
('ASSET_BTN_DELETE',  '删除资产',    2, 301, NULL, 'delete', 2),
('ASSET_BTN_PUBLISH', '发布资产',    2, 301, NULL, 'check',  3),
('ASSET_BTN_DISABLE', '停用资产',    2, 301, NULL, 'close',  4),

('SCRIPT_BTN_ADD',     '新增脚本',     2, 400, NULL, 'plus',   0),
('SCRIPT_BTN_EDIT',    '编辑脚本',     2, 400, NULL, 'edit',   1),
('SCRIPT_BTN_DELETE',  '删除脚本',     2, 400, NULL, 'delete', 2),
('SCRIPT_BTN_PUBLISH', '发布脚本',     2, 400, NULL, 'check',  3),
('SCRIPT_BTN_DISABLE', '停用脚本',     2, 400, NULL, 'close',  4),
('SCRIPT_BTN_DEBUG',   '在线调试',     2, 400, NULL, 'play',   5),
('SCRIPT_BTN_VERSION', '版本历史',     2, 400, NULL, 'history',6),

('TASK_BTN_ADD',       '新增任务',     2, 500, NULL, 'plus',    0),
('TASK_BTN_EDIT',      '编辑任务',     2, 500, NULL, 'edit',    1),
('TASK_BTN_DELETE',    '删除任务',     2, 500, NULL, 'delete',  2),
('TASK_BTN_PUBLISH',   '发布任务',     2, 500, NULL, 'check',   3),
('TASK_BTN_DISABLE',   '停用任务',     2, 500, NULL, 'close',   4),
('TASK_BTN_EXECUTE',   '执行任务',     2, 500, NULL, 'play',    5),
('TASK_BTN_STOP',      '停止任务',     2, 500, NULL, 'stop',    6),
('TASK_BTN_HISTORY',   '执行历史',     2, 500, NULL, 'history', 7),

('USER_BTN_ADD',    '新增用户',  2, 601, NULL, 'plus',   0),
('USER_BTN_EDIT',   '编辑用户',  2, 601, NULL, 'edit',   1),
('USER_BTN_DELETE', '删除用户',  2, 601, NULL, 'delete', 2),

('ROLE_BTN_ADD',    '新增角色',  2, 602, NULL, 'plus',   0),
('ROLE_BTN_EDIT',   '编辑角色',  2, 602, NULL, 'edit',   1),
('ROLE_BTN_DELETE', '删除角色',  2, 602, NULL, 'delete', 2),

('ROUTE_BTN_ADD',    '新增路由',  2, 603, NULL, 'plus',   0),
('ROUTE_BTN_EDIT',   '编辑路由',  2, 603, NULL, 'edit',   1),
('ROUTE_BTN_DELETE', '删除路由',  2, 603, NULL, 'delete', 2);

-- =====================================================
-- 10.9 初始化角色-资源关联
-- =====================================================

-- 管理员(role_id=1)关联所有菜单资源(type=1)
INSERT INTO sys_role_resource (role_id, resource_id)
SELECT 1, id FROM sys_resource WHERE resource_type = 1;

-- 数据工程师(role_id=2)关联全部业务菜单（排除系统管理）
INSERT INTO sys_role_resource (role_id, resource_id)
SELECT 2, id FROM sys_resource WHERE resource_type = 1 AND resource_code != 'SYSTEM_MANAGE';

-- 数据查看者(role_id=3)仅关联所有菜单（只读）
INSERT INTO sys_role_resource (role_id, resource_id)
SELECT 3, id FROM sys_resource WHERE resource_type = 1;
