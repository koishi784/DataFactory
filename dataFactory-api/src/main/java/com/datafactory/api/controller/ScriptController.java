package com.datafactory.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.script.ScriptBatchCategoryRequest;
import com.datafactory.common.model.dto.script.ScriptCreateRequest;
import com.datafactory.common.model.dto.script.ScriptDebugRequest;
import com.datafactory.common.model.dto.script.ScriptUpdateRequest;
import com.datafactory.common.model.vo.script.ScriptDebugVo;
import com.datafactory.common.model.vo.script.ScriptDetailVo;
import com.datafactory.common.model.vo.script.ScriptListVo;
import com.datafactory.common.response.Result;
import com.datafactory.core.service.IScriptService;
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
 * 脚本控制器
 *
 * 提供脚本的增删改查、在线调试、状态管理（发布/停用/删除）以及批量操作的 REST 接口。
 * 支持 GROOVY（JVM 沙箱执行）和 PYTHON（外部进程执行）两种脚本类型。
 */
@Tag(name = "脚本管理", description = "脚本的 CRUD、发布/停用/删除、在线调试及批量操作")
@RestController
@RequestMapping("/api/v1/scripts")
@RequiredArgsConstructor
public class ScriptController {

    private final IScriptService scriptService;

    // ==================== 脚本 CRUD ====================

    @Operation(summary = "查询脚本列表", description = "分页查询脚本列表，支持按关键词、状态、分类筛选。排序规则：状态优先，再按更新时间倒序")
    @GetMapping
    public Result<Page<ScriptListVo>> getScriptList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId) {
        Page<ScriptListVo> page = scriptService.getScriptList(pageNum, pageSize, keyword, status, categoryId);
        return Result.success(page);
    }

    @Operation(summary = "查询脚本详情", description = "根据ID获取脚本完整信息，包含输入/输出参数列表")
    @GetMapping("/{id}")
    public Result<ScriptDetailVo> getScriptDetail(@PathVariable Long id) {
        ScriptDetailVo detail = scriptService.getScriptDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "新增脚本", description = "新增一条脚本。支持两种模式：(1) 文件上传 - 通过 fileId 关联已上传的脚本文件；(2) 在线编辑 - 直接提交 scriptContent 源代码。脚本名称全局唯一，创建后状态为 DRAFT。scriptType 支持 GROOVY / PYTHON")
    @PostMapping
    public Result<ScriptDetailVo> createScript(@Valid @RequestBody ScriptCreateRequest request) {
        ScriptDetailVo detail = scriptService.createScript(request);
        return Result.success(detail);
    }

    @Operation(summary = "编辑脚本", description = "仅未发布和已停用状态可编辑。支持两种模式切换：传 fileId 则按文件上传模式更新，传 scriptContent 则按在线编辑模式更新，同时传则互斥。fileId 和 scriptContent 都不传则不更新脚本内容")
    @PutMapping("/{id}")
    public Result<Void> updateScript(@PathVariable Long id, @Valid @RequestBody ScriptUpdateRequest request) {
        scriptService.updateScript(id, request);
        return Result.success("编辑成功", null);
    }

    @Operation(summary = "删除脚本", description = "仅可删除 DRAFT 状态的脚本")
    @DeleteMapping("/{id}")
    public Result<Void> deleteScript(@PathVariable Long id) {
        scriptService.deleteScript(id);
        return Result.success("删除成功", null);
    }

    // ==================== 状态流转 ====================

    @Operation(summary = "发布脚本", description = "将未发布或已停用状态的脚本发布为已发布")
    @PutMapping("/{id}/publish")
    public Result<Void> publishScript(@PathVariable Long id) {
        scriptService.publishScript(id);
        return Result.success("发布成功", null);
    }

    @Operation(summary = "停用脚本", description = "将已发布状态的脚本变更为已停用")
    @PutMapping("/{id}/disable")
    public Result<Void> disableScript(@PathVariable Long id) {
        scriptService.disableScript(id);
        return Result.success("停用成功", null);
    }

    // ==================== 在线调试 ====================

    @Operation(summary = "在线调试脚本", description = "读取脚本源代码（在线编辑模式优先，文件上传模式其次）并通过对应的执行器（Groovy / Python）执行，返回执行结果。Groovy 脚本自动注入 JdbcTemplate 和业务参数。不限制脚本状态")
    @PostMapping("/{id}/debug")
    public Result<ScriptDebugVo> debugScript(@PathVariable Long id, @RequestBody ScriptDebugRequest request) {
        ScriptDebugVo result = scriptService.debugScript(id, request);
        return Result.success(result);
    }

    // ==================== 批量操作 ====================

    @Operation(summary = "批量发布脚本", description = "批量发布选中的脚本。所选脚本不能包含已发布状态")
    @PutMapping("/batch/publish")
    public Result<Void> batchPublish(@Valid @RequestBody BatchIdsRequest request) {
        scriptService.batchPublish(request);
        return Result.success("批量发布成功", null);
    }

    @Operation(summary = "批量停用脚本", description = "批量停用选中的脚本。所选脚本不能包含未发布或已停用状态")
    @PutMapping("/batch/disable")
    public Result<Void> batchDisable(@Valid @RequestBody BatchIdsRequest request) {
        scriptService.batchDisable(request);
        return Result.success("批量停用成功", null);
    }

    @Operation(summary = "批量修改脚本分类", description = "批量修改脚本分类。所选脚本须全部为未发布或已停用状态，不能包含已发布状态")
    @PutMapping("/batch/category")
    public Result<Void> batchCategory(@Valid @RequestBody ScriptBatchCategoryRequest request) {
        scriptService.batchCategory(request);
        return Result.success("批量分类成功", null);
    }
}
