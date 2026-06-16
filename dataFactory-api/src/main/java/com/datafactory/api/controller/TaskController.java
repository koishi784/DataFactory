package com.datafactory.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.task.*;
import com.datafactory.common.model.vo.task.*;
import com.datafactory.common.response.Result;
import com.datafactory.core.domain.entity.Task;
import com.datafactory.core.service.ITaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理控制器
 *
 * 提供任务的 CRUD、DAG 编排、触发配置、执行管理、状态变更等 REST 接口
 */
@Tag(name = "任务管理", description = "任务的增删改查、DAG 编排、触发配置、执行管理等接口")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskService taskService;

    // ==================== 查询 ====================

    /**
     * 分页查询任务列表
     *
     * 支持多条件筛选：关键词、发布状态、分类、执行状态
     */
    @Operation(summary = "分页查询任务列表", description = "分页查询任务列表，支持按关键词、状态、分类、执行状态筛选")
    @GetMapping
    public Result<IPage<TaskListVo>> getTaskPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer executeStatus) {
        IPage<TaskListVo> page = taskService.getTaskPage(pageNum, pageSize, keyword, status, categoryId, executeStatus);
        return Result.success(page);
    }

    /**
     * 查询任务详情
     *
     * 包含任务基本信息、DAG 节点和连线、触发配置等完整信息
     */
    @Operation(summary = "查询任务详情", description = "查询单个任务的完整信息，包括 DAG 节点、连线和触发配置")
    @GetMapping("/{id}")
    public Result<TaskDetailVo> getTaskDetail(@PathVariable Long id) {
        TaskDetailVo detail = taskService.getTaskDetail(id);
        return Result.success(detail);
    }

    /**
     * 分页查询任务执行历史
     */
    @Operation(summary = "查询任务执行历史", description = "分页查询指定任务的执行历史记录")
    @GetMapping("/{id}/executions")
    public Result<IPage<TaskExecutionVo>> getExecutionHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status) {
        IPage<TaskExecutionVo> page = taskService.getExecutionHistory(
                id, pageNum, pageSize, startDate, endDate, status);
        return Result.success(page);
    }

    // ==================== 新增/编辑 ====================

    /**
     * 新增任务（基本信息）
     *
     * 创建任务的基本信息（三步向导第一步），创建后状态为 DRAFT
     */
    @Operation(summary = "新增任务", description = "新增任务基本信息（三步向导第一步），创建后状态为未发布")
    @PostMapping
    public Result<Task> createTask(@Valid @RequestBody TaskCreateRequest request) {
        Task task = taskService.createTask(request);
        return Result.success(task);
    }

    /**
     * 更新任务 DAG 配置
     *
     * 配置任务 DAG 流程（三步向导第二步），仅未发布和已停用状态可配置
     */
    @Operation(summary = "更新任务 DAG 配置", description = "更新任务的 DAG 节点和连线配置（三步向导第二步）")
    @PutMapping("/{id}/config")
    public Result<Void> updateTaskConfig(
            @PathVariable Long id,
            @Valid @RequestBody TaskConfigRequest request) {
        taskService.updateTaskConfig(id, request);
        return Result.success("DAG 配置保存成功", null);
    }

    /**
     * 任务触发设置
     *
     * 配置任务触发方式（三步向导第三步），支持 API 触发和定时任务两种方式
     */
    @Operation(summary = "任务触发设置", description = "配置任务触发方式（三步向导第三步），支持 API 触发和定时任务")
    @PutMapping("/{id}/trigger-config")
    public Result<Void> updateTriggerConfig(
            @PathVariable Long id,
            @Valid @RequestBody TaskTriggerConfigRequest request) {
        taskService.updateTriggerConfig(id, request);
        return Result.success("触发设置保存成功", null);
    }

    // ==================== 状态变更 ====================

    /**
     * 发布任务
     */
    @Operation(summary = "发布任务", description = "将未发布或已停用的任务发布为已发布状态")
    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        taskService.publish(id);
        return Result.success("发布成功", null);
    }

    /**
     * 停用任务
     */
    @Operation(summary = "停用任务", description = "将已发布的任务停用")
    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        taskService.disable(id);
        return Result.success("停用成功", null);
    }

    /**
     * 删除任务
     */
    @Operation(summary = "删除任务", description = "删除未发布状态的任务")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量发布任务
     */
    @Operation(summary = "批量发布任务", description = "批量发布任务，所选任务须全部为未发布或已停用状态")
    @PutMapping("/batch/publish")
    public Result<Void> batchPublish(@Valid @RequestBody BatchIdsRequest request) {
        taskService.batchPublish(request.getIds());
        return Result.success("批量发布成功", null);
    }

    /**
     * 批量停用任务
     */
    @Operation(summary = "批量停用任务", description = "批量停用任务，所选任务须全部为已发布状态")
    @PutMapping("/batch/disable")
    public Result<Void> batchDisable(@Valid @RequestBody BatchIdsRequest request) {
        taskService.batchDisable(request.getIds());
        return Result.success("批量停用成功", null);
    }

    // ==================== 执行管理 ====================

    /**
     * 测试运行任务
     *
     * 在线测试运行指定任务的 DAG 流程，按拓扑排序逐节点执行
     */
    @Operation(summary = "测试运行任务", description = "在线测试运行任务的 DAG 流程，返回各节点执行日志和结果")
    @PostMapping("/{id}/test-run")
    public Result<TaskTestRunVo> testRun(
            @PathVariable Long id,
            @RequestBody(required = false) TaskTestRunRequest request) {
        // 前端可能不传请求体，此时使用默认值构造一个空请求
        if (request == null) {
            request = new TaskTestRunRequest();
        }
        TaskTestRunVo result = taskService.testRun(id, request);
        return Result.success(result);
    }

    /**
     * 手动执行任务
     *
     * 立即执行指定任务（仅已发布状态的任务可执行）
     */
    @Operation(summary = "手动执行任务", description = "立即执行指定任务，仅已发布状态的任务可执行")
    @PostMapping("/{id}/execute")
    public Result<Long> executeTask(
            @PathVariable Long id,
            @RequestBody(required = false) TaskExecuteRequest request) {
        // 前端可能不传请求体，此时使用默认值构造一个空请求
        if (request == null) {
            request = new TaskExecuteRequest();
        }
        Long executionId = taskService.executeTask(id, request);
        return Result.success("任务已启动", executionId);
    }

    /**
     * 停止正在执行的任务
     */
    @Operation(summary = "停止正在执行的任务", description = "取消正在执行中的任务")
    @PostMapping("/{id}/executions/{executionId}/cancel")
    public Result<Void> cancelExecution(
            @PathVariable Long id,
            @PathVariable Long executionId) {
        taskService.cancelExecution(id, executionId);
        return Result.success("任务已取消", null);
    }
}
