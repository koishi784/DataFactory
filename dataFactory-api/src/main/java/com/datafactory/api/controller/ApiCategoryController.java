package com.datafactory.api.controller;

import com.datafactory.common.model.dto.api.ApiCategoryCreateRequest;
import com.datafactory.common.model.dto.api.ApiCategoryUpdateRequest;
import com.datafactory.common.model.vo.api.ApiCategoryTreeVo;
import com.datafactory.common.response.Result;
import com.datafactory.core.domain.entity.ApiCategory;
import com.datafactory.core.service.IApiCategoryService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 接口分类控制器
 *
 * 提供接口分类的树形查询、新增、编辑、删除等 REST 接口
 */
@Tag(name = "接口分类管理", description = "接口分类的增删改查及树形结构查询")
@RestController
@RequestMapping("/api/v1/api-categories")
@RequiredArgsConstructor
public class ApiCategoryController {

    private final IApiCategoryService apiCategoryService;

    /**
     * 查询接口分类树
     *
     * 获取全量接口分类树形结构，按层级关系组织
     *
     * @return 分类树根节点列表
     */
    @Operation(summary = "查询接口分类树", description = "获取全量接口分类树形结构")
    @GetMapping("/tree")
    public Result<List<ApiCategoryTreeVo>> getCategoryTree() {
        List<ApiCategoryTreeVo> tree = apiCategoryService.getCategoryTree();
        return Result.success(tree);
    }

    /**
     * 新增接口分类
     *
     * @param request 新增分类请求参数
     * @return 创建的完整分类对象
     */
    @Operation(summary = "新增接口分类", description = "新增一个接口分类节点")
    @PostMapping
    public Result<ApiCategory> createCategory(@Valid @RequestBody ApiCategoryCreateRequest request) {
        ApiCategory category = apiCategoryService.createCategory(request);
        return Result.success(category);
    }

    /**
     * 编辑接口分类
     *
     * @param id      分类ID
     * @param request 编辑分类请求参数
     * @return 统一响应
     */
    @Operation(summary = "编辑接口分类", description = "编辑指定接口分类的信息")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ApiCategoryUpdateRequest request) {
        apiCategoryService.updateCategory(id, request);
        return Result.success("编辑成功", null);
    }

    /**
     * 删除接口分类
     *
     * 仅能删除无子分类且无关联接口的分类
     *
     * @param id 分类ID
     * @return 统一响应
     */
    @Operation(summary = "删除接口分类", description = "删除指定接口分类，仅能删除无子分类且无关联接口的分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        apiCategoryService.deleteCategory(id);
        return Result.success("删除成功", null);
    }
}
