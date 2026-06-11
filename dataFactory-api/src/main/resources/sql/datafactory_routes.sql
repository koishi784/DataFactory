-- =====================================================
-- 数据工厂大数据平台 - 系统路由数据脚本
-- 基于 API 接口文档 V1.0 提取
-- 来源: docs/API接口文档.md
-- 数据库: MySQL 8.0+
-- =====================================================

-- 必须先存在 sys_route 表（在 datafactory.sql 中定义）
-- 本脚本仅包含路由数据的 INSERT，用于补充初始化数据
-- 使用 REPLACE INTO 确保幂等执行

-- =====================================================
-- 1. 认证与授权模块 (module: auth)
-- =====================================================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(1,  '用户注册',           '/api/v1/auth/register',      'POST',   'auth', NULL,             0, '新用户注册',                            '2026-06-04 11:48:02'),
(2,  '用户登录',           '/api/v1/auth/login',         'POST',   'auth', NULL,             0, '用户登录',                              '2026-06-04 11:48:02'),
(3,  '用户登出',           '/api/v1/auth/logout',        'POST',   'auth', NULL,             1, '退出登录，使当前令牌失效',              '2026-06-04 11:48:02'),
(4,  '获取当前用户信息',   '/api/v1/auth/user-info',     'GET',    'auth', NULL,             1, '获取当前登录用户的详细信息',            '2026-06-04 11:48:02'),
(5,  '刷新令牌',           '/api/v1/auth/refresh-token', 'POST',   'auth', NULL,             0, '刷新访问令牌',                          '2026-06-04 11:48:02'),
(6,  '修改密码',           '/api/v1/auth/password',      'PUT',    'auth', NULL,             1, '修改登录密码',                          '2026-06-04 11:48:02'),
(7,  '修改个人信息',       '/api/v1/auth/profile',       'PUT',    'auth', NULL,             1, '修改昵称/邮箱/手机号等基本信息',        '2026-06-04 11:48:02');

-- =====================================================
-- 2. 接口管理模块 - 分类管理 (module: api)
-- =====================================================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(10, '查询接口分类树',     '/api/v1/api-categories/tree', 'GET',   'api', 'api:read',  1, '获取全量接口分类树',                    '2026-06-04 11:48:02'),
(11, '新增接口分类',       '/api/v1/api-categories',      'POST',  'api', 'api:write', 1, '新增接口分类',                          '2026-06-04 11:48:02'),
(12, '编辑接口分类',       '/api/v1/api-categories/{id}', 'PUT',   'api', 'api:write', 1, '编辑接口分类（名称/排序/父级）',        '2026-06-04 11:48:02'),
(13, '删除接口分类',       '/api/v1/api-categories/{id}', 'DELETE','api', 'api:write', 1, '删除指定接口分类',                      '2026-06-04 11:48:02');

-- =====================================================
-- 3. 接口管理模块 - 接口注册管理 (module: api)
-- =====================================================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(20, '查询接口列表',       '/api/v1/apis',                'GET',    'api', 'api:read',  1, '分页查询接口列表',                      '2026-06-04 11:48:02'),
(21, '查询接口详情',       '/api/v1/apis/{id}',           'GET',    'api', 'api:read',  1, '查询接口详情（含参数配置）',            '2026-06-04 11:48:02'),
(22, '新增接口',           '/api/v1/apis',                'POST',   'api', 'api:write', 1, '新增注册接口（含参数配置）',            '2026-06-04 11:48:02'),
(23, '编辑接口',           '/api/v1/apis/{id}',           'PUT',    'api', 'api:write', 1, '编辑接口信息',                          '2026-06-04 11:48:02'),
(24, '发布接口',           '/api/v1/apis/{id}/publish',   'PUT',    'api', 'api:write', 1, '发布接口（未发布→已发布）',            '2026-06-04 11:48:02'),
(25, '停用接口',           '/api/v1/apis/{id}/disable',   'PUT',    'api', 'api:write', 1, '停用接口（已发布→已停用）',            '2026-06-04 11:48:02'),
(26, '删除接口',           '/api/v1/apis/{id}',           'DELETE', 'api', 'api:write', 1, '删除接口（仅草稿状态可删）',            '2026-06-04 11:48:02'),
(27, '批量发布接口',       '/api/v1/apis/batch/publish',  'PUT',    'api', 'api:write', 1, '批量发布接口',                          '2026-06-04 11:48:02'),
(28, '批量停用接口',       '/api/v1/apis/batch/disable',  'PUT',    'api', 'api:write', 1, '批量停用接口',                          '2026-06-04 11:48:02'),
(29, '批量修改接口分类',   '/api/v1/apis/batch/category', 'PUT',    'api', 'api:write', 1, '批量修改接口所属分类',                  '2026-06-04 11:48:02'),
(30, '接口测试调用',       '/api/v1/apis/{id}/test',      'POST',   'api', 'api:read',  1, '在线测试调用注册的外部接口',            '2026-06-04 11:48:02');

-- =====================================================
-- 4. 数据库连接管理模块 (module: database)
-- =====================================================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(40, '查询数据库连接列表',         '/api/v1/databases',                    'GET',    'database', 'database:read',  1, '分页查询数据库连接列表',                '2026-06-04 11:48:02'),
(41, '查询数据库连接详情',         '/api/v1/databases/{id}',               'GET',    'database', 'database:read',  1, '查询数据库连接详细信息',                '2026-06-04 11:48:02'),
(42, '新增数据库连接',             '/api/v1/databases',                    'POST',   'database', 'database:write', 1, '新增数据库连接',                        '2026-06-04 11:48:02'),
(43, '编辑数据库连接',             '/api/v1/databases/{id}',               'PUT',    'database', 'database:write', 1, '编辑数据库连接',                        '2026-06-04 11:48:02'),
(44, '测试数据库连接',             '/api/v1/databases/{id}/test',          'POST',   'database', 'database:read',  1, '测试数据库连接是否可用',                '2026-06-04 11:48:02'),
(45, '发布数据库连接',             '/api/v1/databases/{id}/publish',       'PUT',    'database', 'database:write', 1, '发布数据库连接',                        '2026-06-04 11:48:02'),
(46, '停用数据库连接',             '/api/v1/databases/{id}/disable',       'PUT',    'database', 'database:write', 1, '停用数据库连接',                        '2026-06-04 11:48:02'),
(47, '删除数据库连接',             '/api/v1/databases/{id}',               'DELETE', 'database', 'database:write', 1, '删除数据库连接（仅草稿状态可删）',      '2026-06-04 11:48:02'),
(48, '批量发布数据库连接',         '/api/v1/databases/batch/publish',      'PUT',    'database', 'database:write', 1, '批量发布数据库连接',                    '2026-06-04 11:48:02'),
(49, '批量停用数据库连接',         '/api/v1/databases/batch/disable',      'PUT',    'database', 'database:write', 1, '批量停用数据库连接',                    '2026-06-04 11:48:02');

-- =====================================================
-- 5. 数据标准管理模块 - 数据标准目录 (module: standard)
-- =====================================================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(50, '查询数据标准列表',         '/api/v1/data-standards',                  'GET',    'standard', 'standard:read',  1, '分页查询数据标准列表',                  '2026-06-04 11:48:02'),
(51, '查询数据标准详情',         '/api/v1/data-standards/{id}',             'GET',    'standard', 'standard:read',  1, '查询数据标准详情',                      '2026-06-04 11:48:02'),
(52, '新增数据标准',             '/api/v1/data-standards',                  'POST',   'standard', 'standard:write', 1, '新增数据标准',                          '2026-06-04 11:48:02'),
(53, '编辑数据标准',             '/api/v1/data-standards/{id}',             'PUT',    'standard', 'standard:write', 1, '编辑数据标准',                          '2026-06-04 11:48:02'),
(54, '删除数据标准',             '/api/v1/data-standards/{id}',             'DELETE', 'standard', 'standard:write', 1, '删除数据标准（仅草稿状态可删）',        '2026-06-04 11:48:02'),
(55, '发布数据标准',             '/api/v1/data-standards/{id}/publish',     'PUT',    'standard', 'standard:write', 1, '发布数据标准',                          '2026-06-04 11:48:02'),
(56, '停用数据标准',             '/api/v1/data-standards/{id}/disable',     'PUT',    'standard', 'standard:write', 1, '停用数据标准',                          '2026-06-04 11:48:02'),
(57, '批量发布数据标准',         '/api/v1/data-standards/batch/publish',    'PUT',    'standard', 'standard:write', 1, '批量发布数据标准',                      '2026-06-04 11:48:02'),
(58, '批量停用数据标准',         '/api/v1/data-standards/batch/disable',    'PUT',    'standard', 'standard:write', 1, '批量停用数据标准',                      '2026-06-04 11:48:02');

-- =====================================================
-- 6. 数据标准管理模块 - 码表管理 (module: codetable)
-- =====================================================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(60, '查询码表列表',                     '/api/v1/code-tables',                            'GET',    'codetable', 'standard:read',  1, '分页查询码表列表',                      '2026-06-04 11:48:02'),
(61, '查询码表详情',                     '/api/v1/code-tables/{id}',                       'GET',    'codetable', 'standard:read',  1, '查询码表详细信息',                      '2026-06-04 11:48:02'),
(62, '查询码值列表',                     '/api/v1/code-tables/{id}/items',                 'GET',    'codetable', 'standard:read',  1, '查询指定码表下的码值列表',              '2026-06-04 11:48:02'),
(63, '新增码表',                         '/api/v1/code-tables',                            'POST',   'codetable', 'standard:write', 1, '新增码表',                              '2026-06-04 11:48:02'),
(64, '编辑码表',                         '/api/v1/code-tables/{id}',                       'PUT',    'codetable', 'standard:write', 1, '编辑码表信息',                          '2026-06-04 11:48:02'),
(65, '新增码值',                         '/api/v1/code-tables/{tableId}/items',             'POST',   'codetable', 'standard:write', 1, '新增码值',                              '2026-06-04 11:48:02'),
(66, '更新码值',                         '/api/v1/code-tables/{tableId}/items/{itemId}',    'PUT',    'codetable', 'standard:write', 1, '更新码值信息',                          '2026-06-04 11:48:02'),
(67, '删除码值',                         '/api/v1/code-tables/{tableId}/items/{itemId}',    'DELETE', 'codetable', 'standard:write', 1, '删除码值',                              '2026-06-04 11:48:02'),
(68, '发布码表',                         '/api/v1/code-tables/{id}/publish',               'PUT',    'codetable', 'standard:write', 1, '发布码表',                              '2026-06-04 11:48:02'),
(69, '停用码表',                         '/api/v1/code-tables/{id}/disable',               'PUT',    'codetable', 'standard:write', 1, '停用码表',                              '2026-06-04 11:48:02'),
(70, '删除码表',                         '/api/v1/code-tables/{id}',                       'DELETE', 'codetable', 'standard:write', 1, '删除码表（仅草稿状态可删）',            '2026-06-04 11:48:02'),
(71, '批量发布码表',                     '/api/v1/code-tables/batch/publish',              'PUT',    'codetable', 'standard:write', 1, '批量发布码表',                          '2026-06-04 11:48:02'),
(72, '批量停用码表',                     '/api/v1/code-tables/batch/disable',              'PUT',    'codetable', 'standard:write', 1, '批量停用码表',                          '2026-06-04 11:48:02');

-- =====================================================
-- 7. 数据资产管理模块 (module: asset)
-- =====================================================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(80, '查询资产目录树',             '/api/v1/assets/directories/tree',        'GET',    'asset', 'asset:read',  1, '获取资产目录树形结构',                  '2026-06-04 11:48:02'),
(81, '新增资产目录',               '/api/v1/assets/directories',              'POST',   'asset', 'asset:write', 1, '新增资产目录',                          '2026-06-04 11:48:02'),
(82, '编辑资产目录',               '/api/v1/assets/directories/{id}',         'PUT',    'asset', 'asset:write', 1, '编辑资产目录（名称/排序/父级）',        '2026-06-04 11:48:02'),
(83, '删除资产目录',               '/api/v1/assets/directories/{id}',         'DELETE', 'asset', 'asset:write', 1, '删除资产目录',                          '2026-06-04 11:48:02'),
(84, '查询数据资产列表',           '/api/v1/assets',                          'GET',    'asset', 'asset:read',  1, '分页查询数据资产列表',                  '2026-06-04 11:48:02'),
(85, '查询数据资产详情',           '/api/v1/assets/{id}',                     'GET',    'asset', 'asset:read',  1, '查询数据资产详情（含字段列表）',        '2026-06-04 11:48:02'),
(86, '新增数据资产表',             '/api/v1/assets',                          'POST',   'asset', 'asset:write', 1, '新增数据资产表（含字段定义）',          '2026-06-04 11:48:02'),
(87, '编辑数据资产表',             '/api/v1/assets/{id}',                     'PUT',    'asset', 'asset:write', 1, '编辑数据资产表',                        '2026-06-04 11:48:02'),
(88, '删除数据资产',               '/api/v1/assets/{id}',                     'DELETE', 'asset', 'asset:write', 1, '删除数据资产（仅草稿状态可删）',        '2026-06-04 11:48:02'),
(89, '发布数据资产',               '/api/v1/assets/{id}/publish',             'PUT',    'asset', 'asset:write', 1, '发布数据资产',                          '2026-06-04 11:48:02'),
(90, '停用数据资产',               '/api/v1/assets/{id}/disable',             'PUT',    'asset', 'asset:write', 1, '停用数据资产',                          '2026-06-04 11:48:02'),
(91, '批量发布数据资产',           '/api/v1/assets/batch/publish',            'PUT',    'asset', 'asset:write', 1, '批量发布数据资产',                      '2026-06-04 11:48:02'),
(92, '批量停用数据资产',           '/api/v1/assets/batch/disable',            'PUT',    'asset', 'asset:write', 1, '批量停用数据资产',                      '2026-06-04 11:48:02');
-- 注：查询资产字段列表含在详情查询中，无单独接口

-- =====================================================
-- 8. 脚本管理模块 (module: script)
-- =====================================================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(100, '查询脚本分类树',       '/api/v1/script-categories/tree',       'GET',    'script', 'script:read',  1, '获取脚本分类树形结构',                  '2026-06-04 11:48:02'),
(101, '新增脚本分类',         '/api/v1/script-categories',            'POST',   'script', 'script:write', 1, '新增脚本分类',                          '2026-06-04 11:48:02'),
(102, '编辑脚本分类',         '/api/v1/script-categories/{id}',       'PUT',    'script', 'script:write', 1, '编辑脚本分类',                          '2026-06-04 11:48:02'),
(103, '删除脚本分类',         '/api/v1/script-categories/{id}',       'DELETE', 'script', 'script:write', 1, '删除脚本分类',                          '2026-06-04 11:48:02'),
(104, '查询脚本列表',         '/api/v1/scripts',                      'GET',    'script', 'script:read',  1, '分页查询脚本列表',                      '2026-06-04 11:48:02'),
(105, '查询脚本详情',         '/api/v1/scripts/{id}',                 'GET',    'script', 'script:read',  1, '查询脚本详情（含参数）',                '2026-06-04 11:48:02'),
(106, '新增脚本',             '/api/v1/scripts',                      'POST',   'script', 'script:write', 1, '新增脚本',                              '2026-06-04 11:48:02'),
(107, '编辑脚本',             '/api/v1/scripts/{id}',                 'PUT',    'script', 'script:write', 1, '编辑脚本信息',                          '2026-06-04 11:48:02'),
(108, '在线调试脚本',         '/api/v1/scripts/{id}/debug',           'POST',   'script', 'script:read',  1, '在线调试执行脚本',                      '2026-06-04 11:48:02'),
(109, '发布脚本',             '/api/v1/scripts/{id}/publish',         'PUT',    'script', 'script:write', 1, '发布脚本',                              '2026-06-04 11:48:02'),
(110, '停用脚本',             '/api/v1/scripts/{id}/disable',         'PUT',    'script', 'script:write', 1, '停用脚本',                              '2026-06-04 11:48:02'),
(111, '删除脚本',             '/api/v1/scripts/{id}',                 'DELETE', 'script', 'script:write', 1, '删除脚本（仅草稿状态可删）',            '2026-06-04 11:48:02'),
(112, '批量发布脚本',         '/api/v1/scripts/batch/publish',        'PUT',    'script', 'script:write', 1, '批量发布脚本',                          '2026-06-04 11:48:02'),
(113, '批量停用脚本',         '/api/v1/scripts/batch/disable',        'PUT',    'script', 'script:write', 1, '批量停用脚本',                          '2026-06-04 11:48:02');

-- =====================================================
-- 9. 任务管理模块 (module: task)
-- =====================================================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(120, '查询任务分类树',               '/api/v1/task-categories/tree',                 'GET',    'task', 'task:read',   1, '获取任务分类树形结构',                  '2026-06-04 11:48:02'),
(121, '新增任务分类',                 '/api/v1/task-categories',                      'POST',   'task', 'task:write',  1, '新增任务分类',                          '2026-06-04 11:48:02'),
(122, '编辑任务分类',                 '/api/v1/task-categories/{id}',                 'PUT',    'task', 'task:write',  1, '编辑任务分类',                          '2026-06-04 11:48:02'),
(123, '删除任务分类',                 '/api/v1/task-categories/{id}',                 'DELETE', 'task', 'task:write',  1, '删除任务分类（只能删无子类无任务项）',  '2026-06-04 11:48:02'),
(124, '查询任务列表',                 '/api/v1/tasks',                                'GET',    'task', 'task:read',   1, '分页查询任务列表',                      '2026-06-04 11:48:02'),
(125, '查询任务详情',                 '/api/v1/tasks/{id}',                           'GET',    'task', 'task:read',   1, '查询任务基本信息',                      '2026-06-04 11:48:02'),
(126, '新增任务',                     '/api/v1/tasks',                                'POST',   'task', 'task:write',  1, '新增任务基本信息',                      '2026-06-04 11:48:02'),
(127, '更新任务DAG配置',             '/api/v1/tasks/{id}/config',                     'PUT',    'task', 'task:write',  1, '更新任务DAG节点和连线配置',             '2026-06-04 11:48:02'),
(128, '设置任务触发方式',             '/api/v1/tasks/{id}/trigger-config',            'PUT',    'task', 'task:write',  1, '设置任务触发方式（手动/CRON/API）',     '2026-06-04 11:48:02'),
(129, '测试运行任务',                 '/api/v1/tasks/{id}/test-run',                  'POST',   'task', 'task:execute', 1, '测试运行DAG流程',                       '2026-06-04 11:48:02'),
(130, '手动执行任务',                 '/api/v1/tasks/{id}/execute',                   'POST',   'task', 'task:execute', 1, '手动执行已发布的任务',                  '2026-06-04 11:48:02'),
(131, '停止任务执行',                 '/api/v1/tasks/{id}/executions/{executionId}/cancel', 'POST', 'task', 'task:execute', 1, '停止正在执行的任务',                    '2026-06-04 11:48:02'),
(132, '查询任务执行历史',             '/api/v1/tasks/{id}/executions',                'GET',    'task', 'task:read',   1, '查询任务执行历史记录',                  '2026-06-04 11:48:02'),
(133, '发布任务',                     '/api/v1/tasks/{id}/publish',                   'PUT',    'task', 'task:write',  1, '发布任务',                              '2026-06-04 11:48:02'),
(134, '停用任务',                     '/api/v1/tasks/{id}/disable',                   'PUT',    'task', 'task:write',  1, '停用任务',                              '2026-06-04 11:48:02'),
(135, '删除任务',                     '/api/v1/tasks/{id}',                           'DELETE', 'task', 'task:write',  1, '删除任务（仅草稿状态可删）',            '2026-06-04 11:48:02'),
(136, '批量发布任务',                 '/api/v1/tasks/batch/publish',                  'PUT',    'task', 'task:write',  1, '批量发布任务',                          '2026-06-04 11:48:02'),
(137, '批量停用任务',                 '/api/v1/tasks/batch/disable',                  'PUT',    'task', 'task:write',  1, '批量停用任务',                          '2026-06-04 11:48:02');

-- =====================================================
-- 10. 通用接口 (module: common)
-- =====================================================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(140, '文件上传',       '/api/v1/common/upload',                'POST',   'common', NULL, 1, '上传文件（multipart/form-data）',        '2026-06-04 11:48:02'),
(141, '文件下载',       '/api/v1/common/download/{fileId}',     'GET',    'common', NULL, 1, '根据文件ID下载文件',                    '2026-06-04 11:48:02'),
(142, '数据导出',       '/api/v1/common/export',                'POST',   'common', NULL, 1, '数据导出为Excel/CSV',                   '2026-06-04 11:48:02'),
(143, '数据导入',       '/api/v1/common/import',                'POST',   'common', NULL, 1, '从Excel/CSV导入数据',                   '2026-06-04 11:48:02');
