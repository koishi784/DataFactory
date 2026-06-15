package com.datafactory.api.controller;

import com.datafactory.common.model.dto.script.ScriptCategoryCreateRequest;
import com.datafactory.common.model.dto.script.ScriptCategoryUpdateRequest;
import com.datafactory.common.model.vo.script.ScriptCategoryVo;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 脚本分类控制器
 *
 * 提供脚本分类树的查询与 CRUD 的 REST 接口。
 */
@Tag(name = "脚本管理", description = "脚本分类的增删改查")
@RestController
@RequestMapping("/api/v1/script-categories")
@RequiredArgsConstructor
public class ScriptCategoryController {

    private final IScriptService scriptService;

    @Operation(summary = "查询脚本分类树", description = "获取脚本分类的多层级树形结构")
    @GetMapping("/tree")
    public Result<List<ScriptCategoryVo>> getCategoryTree() {
        List<ScriptCategoryVo> tree = scriptService.getCategoryTree();
        return Result.success(tree);
    }

    @Operation(summary = "新增脚本分类", description = "新增脚本分类节点。同一父级下名称不可重复")
    @PostMapping
    public Result<ScriptCategoryVo> createCategory(@Valid @RequestBody ScriptCategoryCreateRequest request) {
        ScriptCategoryVo category = scriptService.createCategory(request);
        return Result.success(category);
    }

    @Operation(summary = "编辑脚本分类", description = "编辑脚本分类的名称和排序")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody ScriptCategoryUpdateRequest request) {
        scriptService.updateCategory(id, request);
        return Result.success("编辑成功", null);
    }

    @Operation(summary = "删除脚本分类", description = "仅能删除无子分类且无关联脚本的分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        scriptService.deleteCategory(id);
        return Result.success("删除成功", null);
    }
}
