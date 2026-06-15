-- =====================================================
-- 数据工厂大数据平台 - 数据库初始化脚本
-- 基于 API接口文档 V1.0 设计
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================

CREATE DATABASE IF NOT EXISTS `datafactory`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `datafactory`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 接口管理模块
-- =====================================================

-- 1.1 接口分类表
DROP TABLE IF EXISTS `api_category`;
CREATE TABLE `api_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称（同一父级下不可重名）',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级分类ID，顶级为0',
  `level` int NOT NULL DEFAULT 1 COMMENT '层级',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parent_name`(`parent_id` ASC, `name` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '接口分类表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `api_category` (`id`, `name`, `parent_id`, `level`, `sort_order`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
(1, '电商数据',  0, 1, 1, '2026-06-08 11:08:30', '2026-06-08 11:08:30', 'admin',  'system', 0),
(2, '支付数据',  0, 1, 2, '2026-06-08 11:08:30', '2026-06-08 11:08:30', 'admin',  NULL,     0),
(3, '订单接口',  1, 2, 1, '2026-06-08 11:08:30', '2026-06-08 11:08:30', 'admin',  NULL,     0),
(4, '商品接口',  1, 2, 2, '2026-06-08 11:08:30', '2026-06-08 11:08:30', 'admin',  NULL,     0),
(5, '支付账单',  2, 2, 1, '2026-06-08 11:08:30', '2026-06-08 11:08:30', 'admin',  NULL,     0),
(6, '物流数据',  0, 1, 3, '2026-06-08 11:11:38', '2026-06-08 11:11:38', 'system', 'system', 0),
(7, '物流追踪',  6, 2, 1, '2026-06-08 11:11:53', '2026-06-08 11:11:53', 'system', 'system', 0),
(8, '物流追踪2', 6, 2, 1, '2026-06-08 11:26:44', '2026-06-08 11:32:20', 'system', 'system', 1);

-- 1.2 接口请求头配置表
DROP TABLE IF EXISTS `api_header`;
CREATE TABLE `api_header`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '请求头ID',
  `api_id` bigint NOT NULL COMMENT '接口ID',
  `header_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求头名称',
  `header_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求头值',
  `required` tinyint NOT NULL DEFAULT 0 COMMENT '是否必填: 0-否, 1-是',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '说明',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_id`(`api_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '接口请求头配置表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `api_header` (`id`, `api_id`, `header_key`, `header_value`, `required`, `description`, `sort_order`) VALUES
(1, 1, 'Content-Type',    'application/json',      1, '请求内容类型', 1),
(2, 1, 'Authorization',   'Bearer {token}',        1, '认证令牌',     2),
(3, 2, 'Content-Type',    'application/json',      1, '请求内容类型', 1),
(8, 5, 'Content-Type',    'application/json',      1, '请求内容类型', 0),
(9, 5, 'App-Key',         'LOGISTICS_2026',        1, '应用密钥',     0);

-- 1.3 注册接口表
DROP TABLE IF EXISTS `api_info`;
CREATE TABLE `api_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '接口ID',
  `api_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口名称（全局唯一，不允许空格）',
  `api_description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接口说明',
  `category_id` bigint NOT NULL COMMENT '所属分类ID',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口来源',
  `protocol` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'HTTP' COMMENT '协议: HTTP/HTTPS',
  `method` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求方法: GET/POST',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口URL路径（全局唯一）',
  `timeout` int NOT NULL DEFAULT 30000 COMMENT '超时时间（毫秒，范围1~1800000，默认30000=30秒）',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1.0.0' COMMENT '当前版本号',
  `response_example` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应示例',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_api_name`(`api_name` ASC) USING BTREE,
  UNIQUE INDEX `uk_url`(`url` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '注册接口表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `api_info` (`id`, `api_name`, `api_description`, `category_id`, `source`, `protocol`, `method`, `url`, `timeout`, `retry_count`, `status`, `version`, `response_example`, `remark`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
(1, '获取订单详情',   '根据订单ID获取订单详细信息', 3, '订单系统', 'HTTPS', 'GET', 'https://order-system.example.com/api/v1/orders/{orderId}', 5000, 1, 2, '1.0.0', '{\"orderId\":\"ORD20260602001\",\"amount\":299.00,\"status\":\"PAID\"}',  '用于数据工厂获取电商订单数据', '2026-06-08 11:09:03', '2026-06-08 11:34:37', 'admin',  'admin',  0),
(2, '创建订单',       '创建新的电商订单',          3, '订单系统', 'HTTPS', 'POST', 'https://order-system.example.com/api/v1/orders',             10000, 0, 1, '1.0.0', '{\"orderId\":\"ORD20260602002\",\"status\":\"CREATED\"}',         NULL,                             '2026-06-08 11:09:03', '2026-06-08 11:09:03', 'admin',  'admin',  0),
(3, '查询商品列表',   '分页查询商品列表',          4, '商品系统', 'HTTPS', 'GET',  'https://product-system.example.com/api/v1/products',          3000,  2, 2, '1.0.0', '{\"total\":100,\"list\":[{\"productId\":\"P001\",\"name\":\"商品A\"}]}', NULL, '2026-06-08 11:09:03', '2026-06-08 11:09:03', 'admin',  'admin',  0),
(4, '查询支付账单',   '查询支付流水账单',          5, '支付系统', 'HTTPS', 'GET',  'https://payment-system.example.com/api/v1/bills',             3000,  0, 2, '1.0.0', NULL,                           NULL,                             '2026-06-08 11:09:03', '2026-06-08 11:49:41', 'admin',  'admin',  0),
(5, '获取物流轨迹',   '根据物流单号查询物流轨迹信息', 7, '物流系统', 'HTTPS', 'GET',  'https://logistics-system.example.com/api/v1/tracking/{trackingNo}', 5000, 1, 2, '1.0.0', '{\"trackingNo\":\"SF1234567890\",\"status\":\"IN_TRANSIT\",\"detail\":[{\"time\":\"2026-06-08 10:00:00\",\"location\":\"上海市分拣中心\"}]}', '用于数据工厂获取物流轨迹数据', '2026-06-08 11:12:48', '2026-06-08 11:53:36', 'system', 'system', 0),
(8, 'httpbin测试',    '测试接口测试功能调用',      1, '测试',   'HTTPS', 'GET',  'https://httpbin.org/get',                                     10000, 0, 0, '1.0.0', NULL,                           NULL,                             '2026-06-08 11:49:54', '2026-06-08 11:53:39', 'system', 'system', 0);

-- 1.4 接口请求参数配置表
DROP TABLE IF EXISTS `api_param`;
CREATE TABLE `api_param`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '参数ID',
  `api_id` bigint NOT NULL COMMENT '接口ID',
  `param_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数名称',
  `param_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数类型: QUERY/PATH/HEADER/BODY',
  `data_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据类型: STRING/INTEGER/LONG/DOUBLE/BOOLEAN/DATE/DATETIME/OBJECT/ARRAY',
  `required` tinyint NOT NULL DEFAULT 0 COMMENT '是否必填: 0-否, 1-是',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '参数说明',
  `default_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认值',
  `example_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '示例值',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `validation_rule` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '校验规则(正则表达式)',
  `min_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最小值',
  `max_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最大值',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_id`(`api_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '接口请求参数配置表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `api_param` (`id`, `api_id`, `param_name`, `param_type`, `data_type`, `required`, `description`, `default_value`, `example_value`, `sort_order`, `validation_rule`, `min_value`, `max_value`) VALUES
(1, 1, 'orderId',       'PATH',  'STRING',  1, '订单ID',                    NULL, 'ORD20260602001', 1, NULL, NULL, NULL),
(2, 1, 'includeItems',  'QUERY', 'BOOLEAN', 0, '是否包含订单明细',          'false', 'true', 2, NULL, NULL, NULL),
(3, 2, 'userId',        'BODY',  'STRING',  1, '用户ID',                    NULL, 'USER2026001',    1, NULL, NULL, NULL),
(4, 2, 'amount',        'BODY',  'DOUBLE',  1, '订单金额',                  NULL, '299.00',         2, NULL, NULL, NULL),
(5, 3, 'pageNum',       'QUERY', 'INTEGER', 0, '页码',                      '1', '1',          1, NULL, NULL, NULL),
(6, 3, 'pageSize',      'QUERY', 'INTEGER', 0, '每页条数',                  '20', '20',         2, NULL, NULL, NULL),
(11, 5, 'trackingNo',    'PATH',  'STRING',  1, '物流单号',                  NULL, 'SF1234567890',  1, NULL, NULL, NULL),
(12, 5, 'includeDetail', 'QUERY', 'BOOLEAN', 0, '是否返回详细轨迹',          'false', 'true', 2, NULL, NULL, NULL);

-- =====================================================
-- 2. 数据资产管理模块
-- =====================================================

-- 2.1 数据资产表
DROP TABLE IF EXISTS `asset`;
CREATE TABLE `asset`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资产ID',
  `asset_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '中文名称（全局唯一，仅支持中英文）',
  `english_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '英文名称（全局唯一，仅支持英文大小写、数字及下划线，英文开头）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据资产表描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_asset_name`(`asset_name` ASC) USING BTREE,
  UNIQUE INDEX `uk_english_name`(`english_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据资产表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- 2.2 资产目录表
DROP TABLE IF EXISTS `asset_directory`;
CREATE TABLE `asset_directory`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '目录ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目录名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级目录ID，顶级为0',
  `level` int NOT NULL DEFAULT 1 COMMENT '层级',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资产目录表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- 2.3 资产目录关联表
DROP TABLE IF EXISTS `asset_directory_rel`;
CREATE TABLE `asset_directory_rel`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `asset_id` bigint NOT NULL COMMENT '资产ID',
  `directory_id` bigint NOT NULL COMMENT '目录ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_asset_directory`(`asset_id` ASC, `directory_id` ASC) USING BTREE,
  INDEX `idx_asset_id`(`asset_id` ASC) USING BTREE,
  INDEX `idx_directory_id`(`directory_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资产目录关联表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- 2.4 资产字段定义表
DROP TABLE IF EXISTS `asset_field`;
CREATE TABLE `asset_field`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段ID',
  `asset_id` bigint NOT NULL COMMENT '所属资产ID',
  `english_field_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段英文名称（仅支持英文大小写、数字及下划线，英文开头）',
  `chinese_field_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段中文名称（仅支持中文及英文大小写）',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '字段说明',
  `standard_id` bigint NULL DEFAULT NULL COMMENT '关联数据标准ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_asset_id`(`asset_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资产字段定义表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- =====================================================
-- 3. 码表管理模块
-- =====================================================

-- 3.1 码表
DROP TABLE IF EXISTS `code_table`;
CREATE TABLE `code_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '码表ID',
  `table_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '码表名称(全局唯一)',
  `table_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '码表编号(系统自动生成, 格式MZB+5位数字, 全局唯一)',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '说明',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_table_code`(`table_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_table_name`(`table_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '码表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- 3.2 码值表
DROP TABLE IF EXISTS `code_item`;
CREATE TABLE `code_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '码值ID',
  `table_id` bigint NOT NULL COMMENT '所属码表ID',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '值',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `parent_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '父级编码(层级码表)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '码值状态: 1=启用, 0=停用',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_table_code`(`table_id` ASC, `code` ASC) USING BTREE,
  INDEX `idx_table_id`(`table_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '码值表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- =====================================================
-- 4. 数据标准管理模块
-- =====================================================

-- 4.1 数据标准表
DROP TABLE IF EXISTS `data_standard`;
CREATE TABLE `data_standard`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标准ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '中文名称',
  `english_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '英文名称',
  `standard_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标准编号(系统自动生成, 格式BZ+5位数字, 全局唯一)',
  `data_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据类型: String/Int/Float/Enum',
  `length` int NULL DEFAULT NULL COMMENT '数据长度(仅String类型可填, 正整数)',
  `precision` int NULL DEFAULT NULL COMMENT '精度-小数位(仅Float类型可填, 非负整数)',
  `default_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认值',
  `range_min` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取值范围最小值(Int/Float类型可填)',
  `range_max` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取值范围最大值(Int/Float类型可填)',
  `enum_range` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '枚举范围-引用码表编码(仅Enum类型可填, 必须为已发布码表编码)',
  `source_organization` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源机构',
  `nullable` tinyint NOT NULL DEFAULT 1 COMMENT '是否可为空: 0=可为空, 1=不可为空',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标准说明',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_standard_code`(`standard_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据标准表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- =====================================================
-- 5. 数据库管理模块
-- =====================================================

-- 5.1 数据库连接表
DROP TABLE IF EXISTS `database_connection`;
CREATE TABLE `database_connection`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '连接ID',
  `connection_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '连接名称（全局唯一，仅支持中英文、数字、下划线，不支持特殊符号及空格）',
  `db_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据库类型: MYSQL/POSTGRESQL/ORACLE/SQLSERVER/HIVE/CLICKHOUSE',
  `host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主机地址',
  `port` int NOT NULL COMMENT '端口号',
  `database_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据库名称',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '连接用户名',
  `password` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '连接密码(加密存储)',
  `jdbc_params` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'JDBC额外连接参数',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述说明',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  `last_test_time` datetime NULL DEFAULT NULL COMMENT '最近测试连接时间',
  `last_test_result` tinyint NULL DEFAULT NULL COMMENT '最近测试结果: 1=成功, 0=失败',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_connection_name`(`connection_name` ASC) USING BTREE,
  UNIQUE INDEX `uk_host_port_db`(`host` ASC, `port` ASC, `database_name` ASC) USING BTREE,
  INDEX `idx_db_type`(`db_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据库连接表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `database_connection` (`id`, `connection_name`, `db_type`, `host`, `port`, `database_name`, `username`, `password`, `jdbc_params`, `description`, `status`, `last_test_time`, `last_test_result`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
(1, '本地测试库',  'MYSQL', 'localhost', 3306, 'datafactory',  'root', 'V7HBXktCsfVXMEuPjcyS6EE4OAdpaq2NlLBnhZLzpG/UKQ==', NULL, '本地测试连接', 2, '2026-06-09 10:46:11', 1, '2026-06-09 10:43:27', '2026-06-09 10:46:51', 'system', 'system', 0),
(3, '本地测试库3', 'MYSQL', 'localhost', 3306, 'datafactory3', 'root', 'LppPhkBj32ZtfjozZsokg8R690oS4SsML3Oag176VrSA/Q==', NULL, '本地测试连接', 2, NULL,               NULL, '2026-06-09 10:47:58', '2026-06-09 10:49:29', 'system', 'system', 0);

-- =====================================================
-- 6. 脚本管理模块
-- =====================================================

-- 6.1 脚本分类表
DROP TABLE IF EXISTS `script_category`;
CREATE TABLE `script_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级分类ID，顶级为0',
  `level` int NOT NULL DEFAULT 1 COMMENT '层级',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '脚本分类表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `script_category` (`id`, `name`, `parent_id`, `level`, `sort_order`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
(1, '数据清洗脚本', 0, 1, 1, '2026-06-01 10:00:00', '2026-06-01 10:00:00', 'admin', 'admin', 0),
(2, '数据编排脚本', 0, 1, 2, '2026-06-01 10:00:00', '2026-06-01 10:00:00', 'admin', 'admin', 0),
(3, '企业数据清洗', 1, 2, 1, '2026-06-01 10:00:00', '2026-06-01 10:00:00', 'admin', 'admin', 0);

-- 6.2 脚本表
-- 支持两种脚本来源：
--   文件上传模式：通过 file_id + file_name 关联上传的文件（适用于 PYTHON）
--   在线编辑模式：直接通过 script_content 存储脚本源代码（适用于 GROOVY）
DROP TABLE IF EXISTS `script`;
CREATE TABLE `script`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '脚本ID',
  `script_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '脚本名称（全局唯一，仅支持中文和英文大小写）',
  `script_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GROOVY' COMMENT '脚本类型: GROOVY / PYTHON',
  `category_id` bigint NOT NULL COMMENT '所属分类ID',
  `file_id` bigint NULL DEFAULT NULL COMMENT '上传文件ID（文件上传模式时使用）',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '脚本文件名（如 xxx.py，文件上传模式时使用）',
  `script_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '脚本源代码（在线编辑模式时使用）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '说明',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0=未发布, 1=已发布, 2=已停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_script_name`(`script_name` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '脚本表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `script` (`id`, `script_name`, `script_type`, `category_id`, `file_id`, `file_name`, `script_content`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
(1, '工商信息清洗脚本', 'PYTHON', 1, NULL, NULL, 'import pandas as pd\n\n# 清洗企业工商信息\ndef clean_commercial_data(df):\n    # 去除空值\n    df = df.dropna(subset=[\"company_name\"])\n    # 去除重复\n    df = df.drop_duplicates(subset=[\"credit_code\"])\n    return df', '清洗企业工商注册数据，去重去空', 1, '2026-06-01 10:00:00', '2026-06-01 10:30:00', 'admin', 'admin', 0),
(2, '通用编排脚本',   'PYTHON', 2, NULL, NULL, 'import pandas as pd\n\n# 通用数据编排\ndef orchestrate_data(df_list):\n    result = pd.concat(df_list, axis=1)\n    return result', '通用数据编排归并脚本',        0, '2026-06-01 11:00:00', '2026-06-01 11:00:00', 'admin', 'admin', 0),
(3, '清洗测试脚本',   'PYTHON', 3, NULL, NULL, 'def test():\n    print(\"清洗测试通过\")\n    return True', '清洗流程测试脚本',              0, '2026-06-01 12:00:00', '2026-06-01 12:00:00', 'admin', 'admin', 0),
(4, '编排测试脚本',   'PYTHON', 2, NULL, NULL, 'def test():\n    print(\"编排测试通过\")\n    return True', '编排流程测试脚本',              2, '2026-06-01 13:00:00', '2026-06-01 13:30:00', 'admin', 'admin', 0);

-- 6.3 脚本参数定义表
DROP TABLE IF EXISTS `script_param`;
CREATE TABLE `script_param`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '参数ID',
  `script_id` bigint NOT NULL COMMENT '脚本ID',
  `param_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数名称',
  `param_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据类型',
  `param_direction` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数方向: INPUT/OUTPUT',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '参数描述',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_script_id`(`script_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '脚本参数定义表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `script_param` (`id`, `script_id`, `param_name`, `param_type`, `param_direction`, `description`) VALUES
(1, 1, '企业唯一id', 'Int',    'INPUT',  '企业唯一标识'),
(2, 1, 'id',          'Int',    'OUTPUT', '企业唯一id');

-- =====================================================
-- 7. 认证与授权模块
-- =====================================================

-- 7.1 权限表
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限标识',
  `permission_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `module` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属模块',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_permission_code`(`permission_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `module`, `description`, `create_time`, `create_by`, `update_by`) VALUES
(1,  'api:read',      '接口查询',     '接口管理',  NULL, '2026-06-04 11:48:02', NULL, NULL),
(2,  'api:write',     '接口管理',     '接口管理',  NULL, '2026-06-04 11:48:02', NULL, NULL),
(3,  'database:read',  '数据库连接查询', '数据库管理', NULL, '2026-06-04 11:48:02', NULL, NULL),
(4,  'database:write', '数据库连接管理', '数据库管理', NULL, '2026-06-04 11:48:02', NULL, NULL),
(5,  'standard:read',  '数据标准查询',  '数据标准',  NULL, '2026-06-04 11:48:02', NULL, NULL),
(6,  'standard:write', '数据标准管理',  '数据标准',  NULL, '2026-06-04 11:48:02', NULL, NULL),
(7,  'asset:read',    '数据资产查询',  '数据资产',  NULL, '2026-06-04 11:48:02', NULL, NULL),
(8,  'asset:write',   '数据资产管理',  '数据资产',  NULL, '2026-06-04 11:48:02', NULL, NULL),
(9,  'script:read',   '脚本查询',     '脚本管理',  NULL, '2026-06-04 11:48:02', NULL, NULL),
(10, 'script:write',  '脚本管理',     '脚本管理',  NULL, '2026-06-04 11:48:02', NULL, NULL),
(11, 'task:read',     '任务查询',     '任务管理',  NULL, '2026-06-04 11:48:02', NULL, NULL),
(12, 'task:write',    '任务管理',     '任务管理',  NULL, '2026-06-04 11:48:02', NULL, NULL),
(13, 'task:execute',  '任务执行',     '任务管理',  NULL, '2026-06-04 11:48:02', NULL, NULL);

-- 7.2 刷新令牌表
DROP TABLE IF EXISTS `sys_refresh_token`;
CREATE TABLE `sys_refresh_token`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '令牌ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `token` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '刷新令牌',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_token`(`token` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '刷新令牌表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `sys_refresh_token` (`id`, `user_id`, `token`, `expires_at`, `create_time`) VALUES
(12, 2, 'eaa692ed40a04007a9bbd19e018bc673', '2026-06-12 12:59:55', '2026-06-05 12:59:55'),
(13, 2, '62b09dff98284a9195b534d072b475d7', '2026-06-12 13:01:40', '2026-06-05 13:01:40'),
(14, 1, 'ec04e711517d43af97ac9c36e2bd5191', '2026-06-16 10:37:52', '2026-06-09 10:37:52');

-- 7.3 系统资源表
DROP TABLE IF EXISTS `sys_resource`;
CREATE TABLE `sys_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源唯一编码',
  `resource_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源名称（菜单显示名称）',
  `resource_type` tinyint NOT NULL COMMENT '资源类型：1=菜单，2=按钮/权限点，3=接口/API',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父资源ID，0=顶级菜单',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '前端路由路径（菜单类型有值，按钮/API为NULL）',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标名称',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '同级排序（数值越小越靠前）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_resource_code`(`resource_code` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_path`(`path` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统资源表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `sys_resource` (`id`, `resource_code`, `resource_name`, `resource_type`, `parent_id`, `path`, `icon`, `sort_order`, `status`, `create_time`, `update_time`) VALUES
(1, 'DATA_SOURCE',   '数据源管理', 1, 0, '/data-source',    'datasource', 0,  1, '2026-06-04 11:48:02', NULL),
(2, 'DATA_STANDARD', '数据标准管理', 1, 0, '/data-standard', 'standard',   1,  1, '2026-06-04 11:48:02', NULL),
(3, 'ASSET_MANAGE',  '数据资产管理', 1, 0, '/asset',         'asset',     2,  1, '2026-06-04 11:48:02', NULL),
(4, 'SCRIPT_MANAGE', '脚本管理',    1, 0, '/script',        'script',    3,  1, '2026-06-04 11:48:02', NULL),
(5, 'TASK_MANAGE',   '任务管理',    1, 0, '/task',          'task',      4,  1, '2026-06-04 11:48:02', NULL),
(6, 'SYSTEM_MANAGE', '系统管理',    1, 0, '/system',        'setting',   99, 1, '2026-06-04 11:48:02', NULL);

-- 7.4 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `create_time`, `update_time`, `deleted`, `create_by`, `update_by`) VALUES
(1, '系统管理员', 'ADMIN',          '系统管理员，拥有全部权限', '2026-06-04 11:48:02', '2026-06-04 11:48:02', 0, NULL, NULL),
(2, '数据工程师', 'DATA_ENGINEER',   '数据工程师角色',           '2026-06-04 11:48:02', '2026-06-04 11:48:02', 0, NULL, NULL),
(3, '数据查看者', 'DATA_VIEWER',    '数据查看者，仅有查询权限', '2026-06-04 11:48:02', '2026-06-04 11:48:02', 0, NULL, NULL);

-- 7.5 角色权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_by`, `update_by`) VALUES
(1,  1, 1,  NULL, NULL), (2,  1, 2,  NULL, NULL), (3,  1, 7,  NULL, NULL), (4,  1, 8,  NULL, NULL),
(5,  1, 3,  NULL, NULL), (6,  1, 4,  NULL, NULL), (7,  1, 9,  NULL, NULL), (8,  1, 10, NULL, NULL),
(9,  1, 5,  NULL, NULL), (10, 1, 6,  NULL, NULL), (11, 1, 13, NULL, NULL), (12, 1, 11, NULL, NULL),
(13, 1, 12, NULL, NULL),
(16, 2, 1,  NULL, NULL), (17, 2, 2,  NULL, NULL), (18, 2, 7,  NULL, NULL), (19, 2, 8,  NULL, NULL),
(20, 2, 3,  NULL, NULL), (21, 2, 4,  NULL, NULL), (22, 2, 9,  NULL, NULL), (23, 2, 10, NULL, NULL),
(24, 2, 5,  NULL, NULL), (25, 2, 6,  NULL, NULL), (26, 2, 13, NULL, NULL), (27, 2, 11, NULL, NULL),
(28, 2, 12, NULL, NULL),
(31, 3, 1,  NULL, NULL), (32, 3, 7,  NULL, NULL), (33, 3, 3,  NULL, NULL), (34, 3, 9,  NULL, NULL),
(35, 3, 5,  NULL, NULL), (36, 3, 11, NULL, NULL);

-- 7.6 角色资源关联表
DROP TABLE IF EXISTS `sys_role_resource`;
CREATE TABLE `sys_role_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_resource`(`role_id` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色资源关联表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `sys_role_resource` (`id`, `role_id`, `resource_id`) VALUES
-- admin 角色拥有所有资源 (1-6)
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5), (6, 1, 6),
(7, 1, 7), (8, 1, 8), (9, 1, 9), (10, 1, 10), (11, 1, 11), (12, 1, 12), (13, 1, 13),
-- DATA_ENGINEER 拥有除系统管理外的所有资源 (1-5)
(14, 1, 14),
(16, 2, 1), (17, 2, 2), (18, 2, 3), (19, 2, 4), (20, 2, 5),
(21, 2, 7), (22, 2, 8), (23, 2, 9), (24, 2, 10), (25, 2, 11), (26, 2, 12),
(27, 2, 13), (28, 2, 14),
-- DATA_VIEWER 拥有所有模块的查看权限 (1-14)
(31, 3, 1), (32, 3, 2), (33, 3, 3), (34, 3, 4), (35, 3, 5), (36, 3, 6),
(37, 3, 7), (38, 3, 8), (39, 3, 9), (40, 3, 10), (41, 3, 11), (42, 3, 12),
(43, 3, 13), (44, 3, 14);

-- =====================================================
-- 8. 路由管理模块
-- =====================================================

-- 8.1 系统路由表
-- 路由数据通过 REPLACE INTO 插入，确保幂等执行
-- 模块划分：
--   auth      - 认证与授权模块
--   api       - 接口管理模块（分类管理 + 接口注册管理）
--   database  - 数据库连接管理模块
--   standard  - 数据标准管理模块（数据标准目录）
--   codetable - 码表管理模块
--   asset     - 数据资产管理模块
--   script    - 脚本管理模块
--   task      - 任务管理模块
--   common    - 通用接口模块（文件上传/下载、数据导入/导出）
DROP TABLE IF EXISTS `sys_route`;
CREATE TABLE `sys_route`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '路由ID',
  `route_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路由名称',
  `route_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路由路径',
  `method` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求方法: GET/POST/PUT/DELETE',
  `module` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属模块',
  `permission` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所需权限标识',
  `is_auth` tinyint NOT NULL DEFAULT 1 COMMENT '是否需要认证: 0-公开, 1-需认证',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_route_path_method`(`route_path` ASC, `method` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 144 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统路由表' ROW_FORMAT = Dynamic;

-- ==================== 8.2 认证与授权模块 (module: auth) ====================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(1,  '用户注册',           '/api/v1/auth/register',      'POST',   'auth', NULL,             0, '新用户注册',                            '2026-06-04 11:48:02'),
(2,  '用户登录',           '/api/v1/auth/login',         'POST',   'auth', NULL,             0, '用户登录',                              '2026-06-04 11:48:02'),
(3,  '用户登出',           '/api/v1/auth/logout',        'POST',   'auth', NULL,             1, '退出登录，使当前令牌失效',              '2026-06-04 11:48:02'),
(4,  '获取当前用户信息',   '/api/v1/auth/user-info',     'GET',    'auth', NULL,             1, '获取当前登录用户的详细信息',            '2026-06-04 11:48:02'),
(5,  '刷新令牌',           '/api/v1/auth/refresh-token', 'POST',   'auth', NULL,             0, '刷新访问令牌',                          '2026-06-04 11:48:02'),
(6,  '修改密码',           '/api/v1/auth/password',      'PUT',    'auth', NULL,             1, '修改登录密码',                          '2026-06-04 11:48:02'),
(7,  '修改个人信息',       '/api/v1/auth/profile',       'PUT',    'auth', NULL,             1, '修改昵称/邮箱/手机号等基本信息',        '2026-06-04 11:48:02');

-- ==================== 8.3 接口管理模块 - 分类管理 (module: api) ====================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(10, '查询接口分类树',     '/api/v1/api-categories/tree', 'GET',   'api', 'api:read',  1, '获取全量接口分类树',                    '2026-06-04 11:48:02'),
(11, '新增接口分类',       '/api/v1/api-categories',      'POST',  'api', 'api:write', 1, '新增接口分类',                          '2026-06-04 11:48:02'),
(12, '编辑接口分类',       '/api/v1/api-categories/{id}', 'PUT',   'api', 'api:write', 1, '编辑接口分类（名称/排序/父级）',        '2026-06-04 11:48:02'),
(13, '删除接口分类',       '/api/v1/api-categories/{id}', 'DELETE','api', 'api:write', 1, '删除指定接口分类',                      '2026-06-04 11:48:02');

-- ==================== 8.4 接口管理模块 - 接口注册管理 (module: api) ====================
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

-- ==================== 8.5 数据库连接管理模块 (module: database) ====================
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

-- ==================== 8.6 数据标准管理模块 - 数据标准目录 (module: standard) ====================
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

-- ==================== 8.7 数据标准管理模块 - 码表管理 (module: codetable) ====================
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

-- ==================== 8.8 数据资产管理模块 (module: asset) ====================
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

-- ==================== 8.9 脚本管理模块 (module: script) ====================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(100, '查询脚本分类树',       '/api/v1/script-categories/tree',       'GET',    'script', 'script:read',  1, '获取脚本分类树形结构',                  '2026-06-04 11:48:02'),
(101, '新增脚本分类',         '/api/v1/script-categories',            'POST',   'script', 'script:write', 1, '新增脚本分类',                          '2026-06-04 11:48:02'),
(102, '编辑脚本分类',         '/api/v1/script-categories/{id}',       'PUT',    'script', 'script:write', 1, '编辑脚本分类',                          '2026-06-04 11:48:02'),
(103, '删除脚本分类',         '/api/v1/script-categories/{id}',       'DELETE', 'script', 'script:write', 1, '删除脚本分类',                          '2026-06-04 11:48:02'),
(104, '查询脚本列表',         '/api/v1/scripts',                      'GET',    'script', 'script:read',  1, '分页查询脚本列表',                      '2026-06-04 11:48:02'),
(105, '查询脚本详情',         '/api/v1/scripts/{id}',                 'GET',    'script', 'script:read',  1, '查询脚本详情（含参数）',                '2026-06-04 11:48:02'),
(106, '新增脚本',             '/api/v1/scripts',                      'POST',   'script', 'script:write', 1, '新增脚本（支持文件上传/在线编辑）',     '2026-06-04 11:48:02'),
(107, '编辑脚本',             '/api/v1/scripts/{id}',                 'PUT',    'script', 'script:write', 1, '编辑脚本信息',                          '2026-06-04 11:48:02'),
(108, '在线调试脚本',         '/api/v1/scripts/{id}/debug',           'POST',   'script', 'script:read',  1, '在线调试执行脚本',                      '2026-06-04 11:48:02'),
(109, '发布脚本',             '/api/v1/scripts/{id}/publish',         'PUT',    'script', 'script:write', 1, '发布脚本',                              '2026-06-04 11:48:02'),
(110, '停用脚本',             '/api/v1/scripts/{id}/disable',         'PUT',    'script', 'script:write', 1, '停用脚本',                              '2026-06-04 11:48:02'),
(111, '删除脚本',             '/api/v1/scripts/{id}',                 'DELETE', 'script', 'script:write', 1, '删除脚本（仅草稿状态可删）',            '2026-06-04 11:48:02'),
(112, '批量发布脚本',         '/api/v1/scripts/batch/publish',        'PUT',    'script', 'script:write', 1, '批量发布脚本',                          '2026-06-04 11:48:02'),
(113, '批量停用脚本',         '/api/v1/scripts/batch/disable',        'PUT',    'script', 'script:write', 1, '批量停用脚本',                          '2026-06-04 11:48:02');

-- ==================== 8.10 任务管理模块 (module: task) ====================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(120, '查询任务分类树',               '/api/v1/task-categories/tree',                 'GET',    'task', 'task:read',   1, '获取任务分类树形结构',                  '2026-06-04 11:48:02'),
(121, '新增任务分类',                 '/api/v1/task-categories',                      'POST',   'task', 'task:write',  1, '新增任务分类',                          '2026-06-04 11:48:02'),
(122, '编辑任务分类',                 '/api/v1/task-categories/{id}',                 'PUT',    'task', 'task:write',  1, '编辑任务分类（名称/排序/父级）',        '2026-06-04 11:48:02'),
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

-- ==================== 8.11 通用接口 (module: common) ====================
REPLACE INTO `sys_route` (`id`, `route_name`, `route_path`, `method`, `module`, `permission`, `is_auth`, `description`, `create_time`) VALUES
(140, '文件上传',       '/api/v1/common/upload',                'POST',   'common', NULL, 1, '上传文件（multipart/form-data）',        '2026-06-04 11:48:02'),
(141, '文件下载',       '/api/v1/common/download/{fileId}',     'GET',    'common', NULL, 1, '根据文件ID下载文件',                    '2026-06-04 11:48:02'),
(142, '数据导出',       '/api/v1/common/export',                'POST',   'common', NULL, 1, '数据导出为Excel/CSV',                   '2026-06-04 11:48:02'),
(143, '数据导入',       '/api/v1/common/import',                'POST',   'common', NULL, 1, '从Excel/CSV导入数据',                   '2026-06-04 11:48:02');

-- =====================================================
-- 9. 系统管理模块
-- =====================================================

-- 9.1 系统用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=停用',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `uk_mobile`(`mobile` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `mobile`, `status`, `remark`, `last_login_time`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`) VALUES
(1, 'admin',    '$2a$10$6tfAgxiYh6Zs59oNkMIpb.8C3xG0fTe7tPFc3Kc9rRGxRy3nSlfn.', '管理员', 'adminn@example.com', '13900139000', 1, '系统默认管理员', '2026-06-09 10:37:52', '2026-06-04 11:48:02', '2026-06-05 11:34:06', NULL, 'system', 0),
(2, 'zhangsan', '$2a$10$Gm.LZfmqgX4DPBHQZmGhC.G4kKxdY.ST45hJo2Xj9n5689V0Tja2W', '张三',   'zhangsan@example.com', '13800138000', 1, '数据开发工程师',  '2026-06-05 13:01:40', '2026-06-05 12:59:55', '2026-06-05 12:59:55', 'system', 'system', 0);

-- 9.2 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- 数据
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_by`, `update_by`) VALUES
(1, 1, 1, NULL, NULL);

-- =====================================================
-- 10. 任务管理模块
-- =====================================================

-- 10.1 任务分类表
DROP TABLE IF EXISTS `task_category`;
CREATE TABLE `task_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级分类ID，顶级为0',
  `level` int NOT NULL DEFAULT 1 COMMENT '层级',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务分类表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- 10.2 任务表
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称（全局唯一，仅支持中文和英文大小写）',
  `task_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务说明',
  `category_id` bigint NOT NULL COMMENT '所属分类ID',
  `schedule_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '调度类型: API/CRON',
  `cron_expression` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Cron表达式(scheduleType=CRON时有效)',
  `effective_date` datetime NULL DEFAULT NULL COMMENT '生效日期',
  `expire_date` datetime NULL DEFAULT NULL COMMENT '失效日期',
  `pause_on_failure` tinyint NOT NULL DEFAULT 1 COMMENT '失败后暂停调度: 0-否, 1-是',
  `task_timeout` int NOT NULL DEFAULT 60 COMMENT '任务超时时间(分钟)',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '失败重试次数',
  `retry_interval` int NOT NULL DEFAULT 5 COMMENT '重试间隔(分钟)',
  `alert_email` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '告警邮箱',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '发布状态: 0=未发布, 1=已发布, 2=已停用',
  `execute_status` tinyint NULL DEFAULT NULL COMMENT '最近执行状态: 0=等待, 1=执行中, 2=成功, 3=失败, 4=已取消',
  `last_execute_time` datetime NULL DEFAULT NULL COMMENT '最近执行时间',
  `next_execute_time` datetime NULL DEFAULT NULL COMMENT '下次执行时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_name`(`task_name` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_execute_status`(`execute_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- 10.3 任务边表
DROP TABLE IF EXISTS `task_edge`;
CREATE TABLE `task_edge`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '连线记录ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `edge_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '连线标识(如edge_1)',
  `source_node_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源节点ID',
  `target_node_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标节点ID',
  `condition` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '条件表达式',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_edge`(`task_id` ASC, `edge_id` ASC) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务边表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- 10.4 任务执行历史表
DROP TABLE IF EXISTS `task_execution`;
CREATE TABLE `task_execution`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '执行记录ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `status` tinyint NOT NULL COMMENT '执行状态: 0=等待, 1=执行中, 2=成功, 3=失败, 4=已取消',
  `trigger_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发方式: MANUAL/CRON/EVENT',
  `trigger_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发人',
  `task_params` json NULL COMMENT '任务参数(JSON)',
  `debug_mode` tinyint NOT NULL DEFAULT 0 COMMENT '是否调试模式: 0-否, 1-是',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `total_duration` bigint NULL DEFAULT NULL COMMENT '总耗时(毫秒)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务执行历史表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- 10.5 任务节点表
DROP TABLE IF EXISTS `task_node`;
CREATE TABLE `task_node`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '节点记录ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `node_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点标识(DAG图中唯一, 如node_1)',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点名称',
  `node_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点类型: START/API/SCRIPT/MAPPING/OUTPUT/END',
  `position_x` double NULL DEFAULT NULL COMMENT '画布X坐标',
  `position_y` double NULL DEFAULT NULL COMMENT '画布Y坐标',
  `node_config` json NULL COMMENT '节点配置(JSON格式, 根据nodeType不同结构不同)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_node`(`task_id` ASC, `node_id` ASC) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务节点表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- 10.6 任务节点执行结果表
DROP TABLE IF EXISTS `task_node_execution`;
CREATE TABLE `task_node_execution`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '节点执行ID',
  `execution_id` bigint NOT NULL COMMENT '执行记录ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `node_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点标识',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点名称',
  `node_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点类型',
  `status` tinyint NOT NULL COMMENT '执行状态: 2=成功, 3=失败, 5=跳过',
  `start_time` datetime NULL DEFAULT NULL COMMENT '节点开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '节点结束时间',
  `duration` bigint NULL DEFAULT NULL COMMENT '节点耗时(毫秒)',
  `input_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '节点输入数据摘要',
  `output_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '节点输出数据摘要',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息(失败时返回)',
  `logs` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '节点执行日志',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_execution_id`(`execution_id` ASC) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务节点执行结果表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- =====================================================
-- 11. 文件管理模块
-- =====================================================

-- 11.1 系统文件记录表
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `stored_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统文件记录表' ROW_FORMAT = Dynamic;

-- 数据（无初始数据）

-- =====================================================
-- 附录: 数据库迁移 SQL
-- =====================================================
-- 以下 SQL 仅适用于从旧版表结构升级到新版（已有 database 需要做增量变更时执行）
-- 如果是全新安装本脚本，以下变更已在 DDL 中包含，无需再次执行。
--
-- -- 1. 脚本表增加 script_content 字段，支持在线编辑模式
-- ALTER TABLE `script`
--   MODIFY COLUMN `file_id` bigint NULL COMMENT '上传文件ID（文件上传模式时使用）',
--   MODIFY COLUMN `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '脚本文件名（如 xxx.py，文件上传模式时使用）',
--   ADD COLUMN `script_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '脚本源代码（在线编辑模式时使用）' AFTER `file_name`,
--   MODIFY COLUMN `script_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GROOVY' COMMENT '脚本类型: GROOVY / PYTHON';

SET FOREIGN_KEY_CHECKS = 1;