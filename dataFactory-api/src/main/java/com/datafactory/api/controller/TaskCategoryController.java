package com.datafactory.api.controller;

import com.datafactory.common.model.dto.task.TaskCategoryRequest;
import com.datafactory.common.model.vo.task.TaskCategoryVo;
import com.datafactory.common.response.Result;
import com.datafactory.core.service.ITaskCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务分类控制器
 *
 * 提供任务分类的树形查询、新增、编辑、删除等 REST 接口
 */
@Tag(name = "任务分类管理", description = "任务分类的增删改查及树形结构查询")
@RestController
@RequestMapping("/api/v1/task-categories")
@RequiredArgsConstructor
public class TaskCategoryController {

    private final ITaskCategoryService taskCategoryService;

    /**
     * 查询任务分类树
     *
     * 获取全量任务分类树形结构，按层级关系组织
     *
     * @return 分类树根节点列表
     */
    @Operation(summary = "查询任务分类树", description = "获取全量任务分类树形结构")
    @GetMapping("/tree")
    public Result<List<TaskCategoryVo>> getCategoryTree() {
        List<TaskCategoryVo> tree = taskCategoryService.getCategoryTree();
        return Result.success(tree);
    }

    /**
     * 新增任务分类
     *
     * @param request 新增分类请求参数
     * @return 创建的分类对象
     */
    @Operation(summary = "新增任务分类", description = "新增一个任务分类节点")
    @PostMapping
    public Result<TaskCategoryVo> createCategory(@Valid @RequestBody TaskCategoryRequest request) {
        TaskCategoryVo category = taskCategoryService.createCategory(request);
        return Result.success(category);
    }

    /**
     * 编辑任务分类
     *
     * @param id      分类ID
     * @param request 编辑分类请求参数
     * @return 更新后的分类对象
     */
    @Operation(summary = "编辑任务分类", description = "编辑指定任务分类的信息")
    @PutMapping("/{id}")
    public Result<TaskCategoryVo> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody TaskCategoryRequest request) {
        TaskCategoryVo category = taskCategoryService.updateCategory(id, request);
        return Result.success(category);
    }

    /**
     * 删除任务分类
     *
     * 仅能删除无子分类且无关联任务的分类
     *
     * @param id 分类ID
     * @return 统一响应
     */
    @Operation(summary = "删除任务分类", description = "删除指定任务分类，仅能删除无子分类且无关联任务的分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        taskCategoryService.deleteCategory(id);
        return Result.success("删除成功", null);
    }
}
