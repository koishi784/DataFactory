package com.datafactory.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.database.DatabaseCreateRequest;
import com.datafactory.common.model.dto.database.DatabaseUpdateRequest;
import com.datafactory.common.model.vo.database.DatabaseDetailVo;
import com.datafactory.common.model.vo.database.DatabaseListVo;
import com.datafactory.common.model.vo.database.DatabaseTestResultVo;
import com.datafactory.core.domain.entity.DatabaseConnection;

/**
 * 数据库连接管理服务接口
 *
 * 提供数据库连接的 CRUD、状态管理（发布/停用/删除）、批量操作、连接测试等业务方法
 */
public interface IDatabaseService {

    /**
     * 分页查询数据库连接列表
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param keyword  关键词，模糊匹配连接名称、数据库名
     * @param dbType   数据库类型筛选
     * @param status   状态筛选，多值用逗号分隔：0=未发布 / 1=已发布 / 2=已停用
     * @return 分页结果
     */
    Page<DatabaseListVo> getDatabaseList(Integer pageNum, Integer pageSize, String keyword,
                                          String dbType, String status);

    /**
     * 查询数据库连接详情
     *
     * @param id 连接ID
     * @return 连接详情（不含密码）
     */
    DatabaseDetailVo getDatabaseDetail(Long id);

    /**
     * 新增数据库连接
     *
     * 密码使用 AES 加密后存储，初始状态为 DRAFT(0)。
     *
     * @param request 新增连接请求参数
     * @return 创建的连接实体
     */
    DatabaseConnection createDatabase(DatabaseCreateRequest request);

    /**
     * 编辑数据库连接
     *
     * 仅 DRAFT(0) 状态可编辑。密码字段不传则不修改。
     *
     * @param id      连接ID
     * @param request 编辑连接请求参数
     */
    void updateDatabase(Long id, DatabaseUpdateRequest request);

    /**
     * 测试数据库连接
     *
     * MySQL 类型实际建立 JDBC 连接并执行 SELECT 1 验证。
     * 其他数据库类型返回"该功能待开发"。
     *
     * @param id 连接ID
     * @return 测试结果（success、responseTime、errorMessage）
     */
    DatabaseTestResultVo testDatabase(Long id);

    /**
     * 发布数据库连接
     *
     * 将 DRAFT(0) 状态的连接变更为 PUBLISHED(1)。
     *
     * @param id 连接ID
     */
    void publishDatabase(Long id);

    /**
     * 停用数据库连接
     *
     * 将 PUBLISHED(1) 状态的连接变更为 DISABLED(2)。
     *
     * @param id 连接ID
     */
    void disableDatabase(Long id);

    /**
     * 删除数据库连接
     *
     * 仅可删除 DRAFT(0) 状态的连接。
     *
     * @param id 连接ID
     */
    void deleteDatabase(Long id);

    /**
     * 批量发布数据库连接
     *
     * @param request 批量操作请求
     */
    void batchPublish(BatchIdsRequest request);

    /**
     * 批量停用数据库连接
     *
     * @param request 批量操作请求
     */
    void batchDisable(BatchIdsRequest request);
}
