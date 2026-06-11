package com.datafactory.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.codetable.CodeItemCreateRequest;
import com.datafactory.common.model.dto.codetable.CodeItemUpdateRequest;
import com.datafactory.common.model.dto.codetable.CodeTableCreateRequest;
import com.datafactory.common.model.dto.codetable.CodeTableUpdateRequest;
import com.datafactory.common.model.vo.codetable.CodeItemVo;
import com.datafactory.common.model.vo.codetable.CodeTableDetailVo;
import com.datafactory.common.model.vo.codetable.CodeTableListVo;
import com.datafactory.common.response.Result;
import com.datafactory.core.service.ICodeTableService;
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

import java.util.List;

/**
 * 码表管理控制器
 *
 * 提供码表的 CRUD、状态管理（发布/停用/删除）、批量操作，以及码值的增删改查等 REST 接口.
 */
@Tag(name = "码表管理", description = "码表的增删改查、发布/停用/删除、批量操作及码值管理")
@RestController
@RequestMapping("/api/v1/code-tables")
@RequiredArgsConstructor
public class CodeTableController {

    private final ICodeTableService codeTableService;

    /**
     * 查询码表列表
     *
     * 分页查询码表列表，支持多条件筛选。
     * 排序规则：状态优先（未发布→已发布→已停用），再按更新时间倒序。
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param keyword  关键词，匹配码表名称、码表编号
     * @param status   状态筛选，多值用逗号分隔：0=未发布 / 1=已发布 / 2=已停用
     * @return 分页结果
     */
    @Operation(summary = "查询码表列表", description = "分页查询码表列表，支持多条件筛选")
    @GetMapping
    public Result<Page<CodeTableListVo>> getCodeTableList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<CodeTableListVo> page = codeTableService.getCodeTableList(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }

    /**
     * 查询码表详情
     *
     * @param id 码表ID
     * @return 码表详情（含码值列表）
     */
    @Operation(summary = "查询码表详情", description = "根据ID获取码表基本信息（不含码值列表）")
    @GetMapping("/{id}")
    public Result<CodeTableDetailVo> getCodeTableDetail(@PathVariable Long id) {
        CodeTableDetailVo detail = codeTableService.getCodeTableDetail(id);
        return Result.success(detail);
    }

    /**
     * 查询码值列表
     *
     * @param id 码表ID
     * @return 码值列表
     */
    @Operation(summary = "查询码值列表", description = "查询指定码表下的所有码值")
    @GetMapping("/{id}/items")
    public Result<List<CodeItemVo>> getCodeItems(@PathVariable("id") Long id) {
        List<CodeItemVo> items = codeTableService.getCodeItems(id);
        return Result.success(items);
    }

    /**
     * 新增码表
     *
     * 码表编号由系统自动生成（格式 MZB + 5 位数字），创建后状态为 DRAFT。支持同时传入初始码值列表。
     *
     * @param request 新增码表请求参数
     * @return 创建的码表详情
     */
    @Operation(summary = "新增码表", description = "新增一条码表。码表编号由系统自动生成（格式 MZB + 5 位数字），创建后状态为 DRAFT。支持同时传入初始码值列表")
    @PostMapping
    public Result<CodeTableDetailVo> createCodeTable(@Valid @RequestBody CodeTableCreateRequest request) {
        CodeTableDetailVo detail = codeTableService.createCodeTable(request);
        return Result.success(detail);
    }

    /**
     * 编辑码表
     *
     * 仅未发布(0)和已停用(2)状态可编辑。码表编号不可修改。
     *
     * @param id      码表ID
     * @param request 编辑码表请求参数
     * @return 统一响应
     */
    @Operation(summary = "编辑码表", description = "仅未发布和已停用状态可编辑。码表编号不可修改")
    @PutMapping("/{id}")
    public Result<Void> updateCodeTable(@PathVariable Long id, @Valid @RequestBody CodeTableUpdateRequest request) {
        codeTableService.updateCodeTable(id, request);
        return Result.success("编辑成功", null);
    }

    /**
     * 新增码值
     *
     * @param tableId 码表ID
     * @param request 新增码值请求参数
     * @return 创建的码值
     */
    @Operation(summary = "新增码值", description = "为指定码表新增一条码值")
    @PostMapping("/{tableId}/items")
    public Result<CodeItemVo> createCodeItem(@PathVariable Long tableId, @Valid @RequestBody CodeItemCreateRequest request) {
        CodeItemVo item = codeTableService.createCodeItem(tableId, request);
        return Result.success(item);
    }

    /**
     * 更新码值
     *
     * @param tableId 码表ID
     * @param itemId  码值ID
     * @param request 更新码值请求参数
     * @return 统一响应
     */
    @Operation(summary = "更新码值", description = "更新指定码表下的指定码值")
    @PutMapping("/{tableId}/items/{itemId}")
    public Result<Void> updateCodeItem(@PathVariable Long tableId, @PathVariable Long itemId,
                                       @Valid @RequestBody CodeItemUpdateRequest request) {
        codeTableService.updateCodeItem(tableId, itemId, request);
        return Result.success("更新成功", null);
    }

    /**
     * 删除码值
     *
     * @param tableId 码表ID
     * @param itemId  码值ID
     * @return 统一响应
     */
    @Operation(summary = "删除码值", description = "删除指定码表下的指定码值")
    @DeleteMapping("/{tableId}/items/{itemId}")
    public Result<Void> deleteCodeItem(@PathVariable Long tableId, @PathVariable Long itemId) {
        codeTableService.deleteCodeItem(tableId, itemId);
        return Result.success("删除成功", null);
    }

    /**
     * 发布码表
     *
     * @param id 码表ID
     * @return 统一响应
     */
    @Operation(summary = "发布码表", description = "将未发布或已停用状态的码表发布为已发布")
    @PutMapping("/{id}/publish")
    public Result<Void> publishCodeTable(@PathVariable Long id) {
        codeTableService.publishCodeTable(id);
        return Result.success("发布成功", null);
    }

    /**
     * 停用码表
     *
     * @param id 码表ID
     * @return 统一响应
     */
    @Operation(summary = "停用码表", description = "将已发布状态的码表变更为已停用")
    @PutMapping("/{id}/disable")
    public Result<Void> disableCodeTable(@PathVariable Long id) {
        codeTableService.disableCodeTable(id);
        return Result.success("停用成功", null);
    }

    /**
     * 删除码表
     *
     * 仅可删除 DRAFT 状态的码表。
     *
     * @param id 码表ID
     * @return 统一响应
     */
    @Operation(summary = "删除码表", description = "仅可删除 DRAFT 状态的码表")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCodeTable(@PathVariable Long id) {
        codeTableService.deleteCodeTable(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量发布码表
     *
     * 校验规则：所选码表不能包含已发布状态。
     *
     * @param request 批量发布请求参数
     * @return 统一响应
     */
    @Operation(summary = "批量发布码表", description = "批量发布选中的码表。校验规则：所选码表不能包含已发布状态")
    @PutMapping("/batch/publish")
    public Result<Void> batchPublish(@Valid @RequestBody BatchIdsRequest request) {
        codeTableService.batchPublish(request);
        return Result.success("批量发布成功", null);
    }

    /**
     * 批量停用码表
     *
     * 校验规则：所选码表不能包含未发布或已停用状态。
     *
     * @param request 批量停用请求参数
     * @return 统一响应
     */
    @Operation(summary = "批量停用码表", description = "批量停用选中的码表。校验规则：所选码表不能包含未发布或已停用状态")
    @PutMapping("/batch/disable")
    public Result<Void> batchDisable(@Valid @RequestBody BatchIdsRequest request) {
        codeTableService.batchDisable(request);
        return Result.success("批量停用成功", null);
    }
}
