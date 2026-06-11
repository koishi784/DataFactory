package com.datafactory.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.database.DatabaseCreateRequest;
import com.datafactory.common.model.dto.database.DatabaseUpdateRequest;
import com.datafactory.common.model.vo.database.DatabaseDetailVo;
import com.datafactory.common.model.vo.database.DatabaseListVo;
import com.datafactory.common.model.vo.database.DatabaseTestResultVo;
import com.datafactory.common.response.Result;
import com.datafactory.core.domain.entity.DatabaseConnection;
import com.datafactory.core.service.IDatabaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据库连接管理控制器
 *
 * 提供数据库连接的 CRUD、状态管理（发布/停用/删除）、批量操作及连接测试等 REST 接口。
 * 仅支持 MySQL 数据库连接测试，其他数据库类型标注"该功能待开发"。
 */
@Tag(name = "数据库连接管理", description = "数据库连接的增删改查、发布/停用/删除、批量操作及连接测试")
@RestController
@RequestMapping("/api/v1/databases")
@RequiredArgsConstructor
public class DatabaseController {

    private final IDatabaseService databaseService;

    /**
     * 查询数据库连接列表
     *
     * 分页查询数据库连接列表，支持多条件筛选。
     * 排序规则：状态优先（未发布→已发布→已停用），再按更新时间倒序。
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param keyword  关键词，模糊匹配连接名称、数据库名
     * @param dbType   数据库类型筛选
     * @param status   状态筛选，多值用逗号分隔：0=未发布 / 1=已发布 / 2=已停用
     * @return 分页结果
     */
    @Operation(summary = "查询数据库连接列表", description = "分页查询已注册的数据库连接列表")
    @GetMapping
    public Result<Page<DatabaseListVo>> getDatabaseList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dbType,
            @RequestParam(required = false) String status) {
        Page<DatabaseListVo> page = databaseService.getDatabaseList(
                pageNum, pageSize, keyword, dbType, status);
        return Result.success(page);
    }

    /**
     * 查询数据库连接详情
     *
     * @param id 连接ID
     * @return 连接详情（不含密码，含 jdbcParams）
     */
    @Operation(summary = "查询数据库连接详情", description = "根据ID获取单个数据库连接的完整信息")
    @GetMapping("/{id}")
    public Result<DatabaseDetailVo> getDatabaseDetail(@PathVariable Long id) {
        DatabaseDetailVo detail = databaseService.getDatabaseDetail(id);
        return Result.success(detail);
    }

    /**
     * 新增数据库连接
     *
     * @param request 新增连接请求参数（密码使用 AES 加密后存储）
     * @return 创建的连接信息（状态为 DRAFT）
     */
    @Operation(summary = "新增数据库连接", description = "创建一个新的数据库连接，创建后状态为 DRAFT。密码使用 AES 加密存储")
    @PostMapping
    public Result<DatabaseConnection> createDatabase(@Valid @RequestBody DatabaseCreateRequest request) {
        DatabaseConnection connection = databaseService.createDatabase(request);
        return Result.success(connection);
    }

    /**
     * 编辑数据库连接
     *
     * 仅 DRAFT 状态的连接可编辑。密码字段不传则不修改。
     *
     * @param id      连接ID
     * @param request 编辑连接请求参数
     * @return 统一响应
     */
    @Operation(summary = "编辑数据库连接", description = "修改处于 DRAFT 状态的数据库连接。密码字段可选，不传则不修改")
    @PutMapping("/{id}")
    public Result<Void> updateDatabase(@PathVariable Long id, @Valid @RequestBody DatabaseUpdateRequest request) {
        databaseService.updateDatabase(id, request);
        return Result.success("编辑成功", null);
    }

    /**
     * 测试数据库连接
     *
     * MySQL 类型实际建立 JDBC 连接并执行 SELECT 1 验证。
     * 其他数据库类型返回"该功能待开发"。
     *
     * @param id 连接ID
     * @return 测试结果（success、responseTime、errorMessage）
     */
    @Operation(summary = "测试数据库连接", description = "测试指定数据库连接的连通性。仅支持 MySQL 类型")
    @PostMapping("/{id}/test")
    public Result<DatabaseTestResultVo> testDatabase(@PathVariable Long id) {
        DatabaseTestResultVo result = databaseService.testDatabase(id);
        return Result.success(result);
    }

    /**
     * 发布数据库连接
     *
     * @param id 连接ID
     * @return 统一响应
     */
    @Operation(summary = "发布数据库连接", description = "将 DRAFT 状态的数据库连接发布上线，状态变更为 PUBLISHED")
    @PutMapping("/{id}/publish")
    public Result<Void> publishDatabase(@PathVariable Long id) {
        databaseService.publishDatabase(id);
        return Result.success("发布成功", null);
    }

    /**
     * 停用数据库连接
     *
     * @param id 连接ID
     * @return 统一响应
     */
    @Operation(summary = "停用数据库连接", description = "将 PUBLISHED 状态的数据库连接停用，状态变更为 DISABLED")
    @PutMapping("/{id}/disable")
    public Result<Void> disableDatabase(@PathVariable Long id) {
        databaseService.disableDatabase(id);
        return Result.success("停用成功", null);
    }

    /**
     * 删除数据库连接
     *
     * 仅可删除 DRAFT 状态的连接。
     *
     * @param id 连接ID
     * @return 统一响应
     */
    @Operation(summary = "删除数据库连接", description = "仅可删除 DRAFT 状态的数据库连接，已发布或已停用的不可删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDatabase(@PathVariable Long id) {
        databaseService.deleteDatabase(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量发布数据库连接
     *
     * 校验规则：所选连接不能包含已发布或已停用状态。
     *
     * @param request 批量发布请求参数
     * @return 统一响应
     */
    @Operation(summary = "批量发布数据库连接", description = "批量发布选中的数据库连接。校验规则：所选连接不能包含已发布状态")
    @PutMapping("/batch/publish")
    public Result<Void> batchPublish(@Valid @RequestBody BatchIdsRequest request) {
        databaseService.batchPublish(request);
        return Result.success("批量发布成功", null);
    }

    /**
     * 批量停用数据库连接
     *
     * 校验规则：所选连接不能包含未发布或已停用状态。
     *
     * @param request 批量停用请求参数
     * @return 统一响应
     */
    @Operation(summary = "批量停用数据库连接", description = "批量停用选中的数据库连接。校验规则：所选连接不能包含未发布或已停用状态")
    @PutMapping("/batch/disable")
    public Result<Void> batchDisable(@Valid @RequestBody BatchIdsRequest request) {
        databaseService.batchDisable(request);
        return Result.success("批量停用成功", null);
    }
}
