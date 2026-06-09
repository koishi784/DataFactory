package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.database.DatabaseCreateRequest;
import com.datafactory.common.model.dto.database.DatabaseUpdateRequest;
import com.datafactory.common.model.vo.database.DatabaseDetailVo;
import com.datafactory.common.model.vo.database.DatabaseListVo;
import com.datafactory.common.model.vo.database.DatabaseTestResultVo;
import com.datafactory.common.utils.AesUtils;
import com.datafactory.common.utils.StatusUtils;
import com.datafactory.core.domain.entity.DatabaseConnection;
import com.datafactory.core.domain.mapper.DatabaseConnectionMapper;
import com.datafactory.core.service.IDatabaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库连接管理服务实现类
 *
 * 实现数据库连接的 CRUD、状态管理、批量操作、连接测试等完整业务逻辑。
 * 仅 MySQL 类型支持连接测试，其他数据库类型标注"该功能待开发"。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IDatabaseServiceImpl extends ServiceImpl<DatabaseConnectionMapper, DatabaseConnection> implements IDatabaseService {

    private final AesUtils aesUtils;

    /**
     * 分页查询数据库连接列表
     *
     * 支持多条件筛选（关键词、数据库类型、状态），排序规则：
     * 优先级一：按状态 DRAFT(0) → PUBLISHED(1) → DISABLED(2)
     * 优先级二：按更新时间倒序排列
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param keyword  关键词，模糊匹配连接名称、数据库名
     * @param dbType   数据库类型筛选
     * @param status   状态筛选，多值用逗号分隔
     * @return 分页结果
     */
    @Override
    public Page<DatabaseListVo> getDatabaseList(Integer pageNum, Integer pageSize, String keyword,
                                                 String dbType, String status) {
        // 1. 构建分页参数
        Page<DatabaseConnection> page = new Page<>(
                pageNum != null ? pageNum : 1,
                pageSize != null ? pageSize : 20
        );

        // 2. 构建查询条件
        LambdaQueryWrapper<DatabaseConnection> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词模糊匹配（连接名称、数据库名）
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.and(w -> w
                    .like(DatabaseConnection::getConnectionName, keyword)
                    .or()
                    .like(DatabaseConnection::getDatabaseName, keyword)
            );
        }

        // 数据库类型筛选
        if (dbType != null && !dbType.isBlank()) {
            queryWrapper.eq(DatabaseConnection::getDbType, dbType.toUpperCase());
        }

        // 状态筛选（多值逗号分隔）
        if (status != null && !status.isBlank()) {
            List<Integer> statusList = StatusUtils.parseStatusList(status);
            if (!statusList.isEmpty()) {
                queryWrapper.in(DatabaseConnection::getStatus, statusList);
            }
        }

        // 3. 排序：状态优先（DRAFT=0 → PUBLISHED=1 → DISABLED=2），再按更新时间倒序
        queryWrapper.last("ORDER BY FIELD(status, 0, 1, 2), update_time DESC");

        // 4. 执行分页查询
        Page<DatabaseConnection> dbPage = page(page, queryWrapper);

        // 5. 转换为 VO
        List<DatabaseListVo> voList = dbPage.getRecords().stream()
                .map(this::convertToListVo)
                .collect(Collectors.toList());

        // 6. 构建分页结果
        Page<DatabaseListVo> voPage = new Page<>(dbPage.getCurrent(), dbPage.getSize(), dbPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询数据库连接详情
     *
     * @param id 连接ID
     * @return 连接详情（不含密码，含 jdbcParams）
     */
    @Override
    public DatabaseDetailVo getDatabaseDetail(Long id) {
        DatabaseConnection connection = lambdaQuery()
                .eq(DatabaseConnection::getId, id)
                .one();
        if (connection == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据库连接不存在");
        }
        return convertToDetailVo(connection);
    }

    /**
     * 新增数据库连接
     *
     * 密码使用 AES 加密后存储，初始状态为 DRAFT(0)。
     *
     * @param request 新增连接请求参数
     * @return 创建的连接实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DatabaseConnection createDatabase(DatabaseCreateRequest request) {
        // 1. 创建实体
        DatabaseConnection connection = new DatabaseConnection();
        connection.setConnectionName(request.getConnectionName());
        connection.setDbType(request.getDbType().toUpperCase());
        connection.setHost(request.getHost());
        connection.setPort(request.getPort());
        connection.setDatabaseName(request.getDatabaseName());
        connection.setUsername(request.getUsername());
        // AES 加密密码
        connection.setPassword(aesUtils.encrypt(request.getPassword()));
        connection.setJdbcParams(request.getJdbcParams());
        connection.setDescription(request.getDescription());
        connection.setStatus(0); // DRAFT

        save(connection);

        log.info("新增数据库连接成功：connectionName={}, id={}", request.getConnectionName(), connection.getId());
        return connection;
    }

    /**
     * 编辑数据库连接
     *
     * 仅 DRAFT(0) 状态可编辑。密码字段不传则不修改。
     *
     * @param id      连接ID
     * @param request 编辑连接请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDatabase(Long id, DatabaseUpdateRequest request) {
        // 1. 校验连接是否存在
        DatabaseConnection connection = lambdaQuery()
                .eq(DatabaseConnection::getId, id)
                .one();
        if (connection == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据库连接不存在");
        }

        // 2. 校验状态（仅草稿状态可编辑）
        if (connection.getStatus() != 0) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布或已停用的数据库连接不可编辑");
        }

        // 3. 更新字段
        connection.setConnectionName(request.getConnectionName());
        connection.setDbType(request.getDbType().toUpperCase());
        connection.setHost(request.getHost());
        connection.setPort(request.getPort());
        connection.setDatabaseName(request.getDatabaseName());
        connection.setUsername(request.getUsername());
        connection.setJdbcParams(request.getJdbcParams());
        connection.setDescription(request.getDescription());

        // 密码可选：传了则 AES 重新加密，不传则保留原值
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            connection.setPassword(aesUtils.encrypt(request.getPassword()));
        }

        lambdaUpdate()
                .eq(DatabaseConnection::getId, id)
                .update(connection);

        log.info("编辑数据库连接成功：id={}, connectionName={}", id, request.getConnectionName());
    }

    /**
     * 测试数据库连接
     *
     * MySQL 类型：实际建立 JDBC 连接并执行 SELECT 1 验证。
     * 其他数据库类型：返回"该功能待开发"。
     *
     * @param id 连接ID
     * @return 测试结果
     */
    @Override
    public DatabaseTestResultVo testDatabase(Long id) {
        // 1. 查询连接信息
        DatabaseConnection connection = lambdaQuery()
                .eq(DatabaseConnection::getId, id)
                .one();
        if (connection == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据库连接不存在");
        }

        DatabaseTestResultVo result = new DatabaseTestResultVo();

        // 2. 仅 MySQL 类型支持连接测试
        if (!"MYSQL".equalsIgnoreCase(connection.getDbType())) {
            result.setSuccess(false);
            result.setResponseTime(0L);
            result.setErrorMessage("该功能待开发，仅支持 MySQL 数据库连接测试");
            log.warn("测试数据库连接：不支持的数据库类型 {}, id={}", connection.getDbType(), id);
            return result;
        }

        // 3. 建立 MySQL JDBC 连接并执行 SELECT 1
        long startTime = System.currentTimeMillis();
        try {
            // 构建 JDBC URL
            String jdbcUrl = buildMysqlJdbcUrl(connection);
            // 解密密码
            String plainPassword = aesUtils.decrypt(connection.getPassword());

            // 建立连接
            try (Connection conn = DriverManager.getConnection(jdbcUrl, connection.getUsername(), plainPassword);
                 PreparedStatement stmt = conn.prepareStatement("SELECT 1")) {
                stmt.execute();
            }

            long endTime = System.currentTimeMillis();
            result.setSuccess(true);
            result.setResponseTime(endTime - startTime);

            // 更新测试时间和结果
            connection.setLastTestTime(LocalDateTime.now());
            connection.setLastTestResult(1);
            lambdaUpdate()
                    .eq(DatabaseConnection::getId, id)
                    .update(connection);

            log.info("测试数据库连接成功：id={}, responseTime={}ms", id, endTime - startTime);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            result.setSuccess(false);
            result.setResponseTime(endTime - startTime);
            result.setErrorMessage("数据库连接测试失败：" + e.getMessage());

            // 更新测试结果（失败）
            connection.setLastTestTime(LocalDateTime.now());
            connection.setLastTestResult(0);
            lambdaUpdate()
                    .eq(DatabaseConnection::getId, id)
                    .update(connection);

            log.warn("测试数据库连接失败：id={}, error={}", id, e.getMessage());
        }

        return result;
    }

    /**
     * 发布数据库连接
     *
     * 将 DRAFT(0) 状态的连接变更为 PUBLISHED(1)。
     *
     * @param id 连接ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishDatabase(Long id) {
        DatabaseConnection connection = lambdaQuery()
                .eq(DatabaseConnection::getId, id)
                .one();
        if (connection == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据库连接不存在");
        }
        if (connection.getStatus() != 0) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅未发布状态的数据库连接可发布");
        }

        lambdaUpdate()
                .eq(DatabaseConnection::getId, id)
                .set(DatabaseConnection::getStatus, 1)
                .update();

        log.info("发布数据库连接成功：id={}, connectionName={}", id, connection.getConnectionName());
    }

    /**
     * 停用数据库连接
     *
     * 将 PUBLISHED(1) 状态的连接变更为 DISABLED(2)。
     *
     * @param id 连接ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableDatabase(Long id) {
        DatabaseConnection connection = lambdaQuery()
                .eq(DatabaseConnection::getId, id)
                .one();
        if (connection == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据库连接不存在");
        }
        if (connection.getStatus() != 1) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "仅已发布状态的数据库连接可停用");
        }

        lambdaUpdate()
                .eq(DatabaseConnection::getId, id)
                .set(DatabaseConnection::getStatus, 2)
                .update();

        log.info("停用数据库连接成功：id={}, connectionName={}", id, connection.getConnectionName());
    }

    /**
     * 删除数据库连接
     *
     * 仅可删除 DRAFT(0) 状态的连接（逻辑删除）。
     *
     * @param id 连接ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDatabase(Long id) {
        DatabaseConnection connection = lambdaQuery()
                .eq(DatabaseConnection::getId, id)
                .one();
        if (connection == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "数据库连接不存在");
        }
        if (connection.getStatus() != 0) {
            throw new BusinessException(StatusCode.RESOURCE_STATUS_NOT_ALLOWED, "已发布或已停用的数据库连接不可删除");
        }

        removeById(id);
        log.info("删除数据库连接成功：id={}, connectionName={}", id, connection.getConnectionName());
    }

    /**
     * 批量发布数据库连接
     *
     * 校验规则：所选连接不能包含已发布(1)或已停用(2)状态的连接。
     *
     * @param request 批量操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPublish(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选连接
        List<DatabaseConnection> dbList = lambdaQuery()
                .in(DatabaseConnection::getId, ids)
                .list();

        // 2. 校验数据完整性
        if (dbList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分数据库连接不存在");
        }

        // 3. 校验是否包含已发布或已停用连接
        boolean hasInvalid = dbList.stream().anyMatch(db -> db.getStatus() != 0);
        if (hasInvalid) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED, "所选数据库连接中包含已发布或已停用状态的连接，操作不合法");
        }

        // 4. 批量更新为已发布
        lambdaUpdate()
                .in(DatabaseConnection::getId, ids)
                .set(DatabaseConnection::getStatus, 1)
                .update();

        log.info("批量发布数据库连接成功：ids={}", ids);
    }

    /**
     * 批量停用数据库连接
     *
     * 校验规则：所选连接不能包含未发布(0)或已停用(2)状态的连接。
     *
     * @param request 批量操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDisable(BatchIdsRequest request) {
        List<Long> ids = request.getIds();

        // 1. 查询所选连接
        List<DatabaseConnection> dbList = lambdaQuery()
                .in(DatabaseConnection::getId, ids)
                .list();

        // 2. 校验数据完整性
        if (dbList.size() != ids.size()) {
            throw new BusinessException(StatusCode.NOT_FOUND, "部分数据库连接不存在");
        }

        // 3. 校验是否均为已发布状态
        boolean hasInvalid = dbList.stream().anyMatch(db -> db.getStatus() != 1);
        if (hasInvalid) {
            throw new BusinessException(StatusCode.BATCH_OPERATION_FAILED, "所选数据库连接中包含未发布或已停用状态的连接，操作不合法");
        }

        // 4. 批量更新为已停用
        lambdaUpdate()
                .in(DatabaseConnection::getId, ids)
                .set(DatabaseConnection::getStatus, 2)
                .update();

        log.info("批量停用数据库连接成功：ids={}", ids);
    }

    // ==================== 私有工具方法 ====================

    /**
     * 构建 MySQL JDBC URL
     *
     * @param connection 数据库连接实体
     * @return JDBC URL 字符串
     */
    private String buildMysqlJdbcUrl(DatabaseConnection connection) {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:mysql://")
                .append(connection.getHost())
                .append(":")
                .append(connection.getPort())
                .append("/")
                .append(connection.getDatabaseName());

        // 附加 JDBC 参数
        if (connection.getJdbcParams() != null && !connection.getJdbcParams().isBlank()) {
            url.append("?").append(connection.getJdbcParams());
        } else {
            // 默认参数
            url.append("?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
        }

        return url.toString();
    }

    /**
     * DatabaseConnection → DatabaseListVo 转换
     *
     * 密码字段不返回。
     */
    private DatabaseListVo convertToListVo(DatabaseConnection entity) {
        DatabaseListVo vo = new DatabaseListVo();
        vo.setId(entity.getId());
        vo.setConnectionName(entity.getConnectionName());
        vo.setDbType(entity.getDbType());
        vo.setHost(entity.getHost());
        vo.setPort(entity.getPort());
        vo.setDatabaseName(entity.getDatabaseName());
        vo.setUsername(entity.getUsername());
        vo.setStatus(entity.getStatus());
        vo.setDescription(entity.getDescription());
        vo.setLastTestTime(entity.getLastTestTime());
        vo.setLastTestResult(entity.getLastTestResult());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    /**
     * DatabaseConnection → DatabaseDetailVo 转换
     *
     * 密码字段不返回，额外包含 jdbcParams。
     */
    private DatabaseDetailVo convertToDetailVo(DatabaseConnection entity) {
        DatabaseDetailVo vo = new DatabaseDetailVo();
        vo.setId(entity.getId());
        vo.setConnectionName(entity.getConnectionName());
        vo.setDbType(entity.getDbType());
        vo.setHost(entity.getHost());
        vo.setPort(entity.getPort());
        vo.setDatabaseName(entity.getDatabaseName());
        vo.setUsername(entity.getUsername());
        vo.setStatus(entity.getStatus());
        vo.setDescription(entity.getDescription());
        vo.setJdbcParams(entity.getJdbcParams());
        vo.setLastTestTime(entity.getLastTestTime());
        vo.setLastTestResult(entity.getLastTestResult());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
