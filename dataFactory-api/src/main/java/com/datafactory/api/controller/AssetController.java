package com.datafactory.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.asset.AssetCreateRequest;
import com.datafactory.common.model.dto.asset.AssetDirectoryCreateRequest;
import com.datafactory.common.model.dto.asset.AssetDirectoryUpdateRequest;
import com.datafactory.common.model.dto.asset.AssetUpdateRequest;
import com.datafactory.common.model.vo.asset.AssetDetailVo;
import com.datafactory.common.model.vo.asset.AssetDirectoryVo;
import com.datafactory.common.model.vo.asset.AssetListVo;
import com.datafactory.common.response.Result;
import com.datafactory.core.service.IAssetService;
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
 * 数据资产管理控制器
 *
 * 提供资产目录树查询与 CRUD、资产的增删改查、状态管理（发布/停用/删除）以及批量操作的 REST 接口。
 */
@Tag(name = "数据资产管理", description = "数据资产目录的增删改查，以及资产的 CRUD、发布/停用/删除及批量操作")
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final IAssetService assetService;

    // ==================== 资产目录 ====================

    @Operation(summary = "查询资产目录树", description = "获取资产目录的多层级树形结构（仅包含目录节点，不含资产）")
    @GetMapping("/directories/tree")
    public Result<List<AssetDirectoryVo>> getDirectoryTree() {
        List<AssetDirectoryVo> tree = assetService.getDirectoryTree();
        return Result.success(tree);
    }

    @Operation(summary = "新增资产目录", description = "新增资产目录节点。有资产的目录下不可新建子目录")
    @PostMapping("/directories")
    public Result<AssetDirectoryVo> createDirectory(@Valid @RequestBody AssetDirectoryCreateRequest request) {
        AssetDirectoryVo directory = assetService.createDirectory(request);
        return Result.success(directory);
    }

    @Operation(summary = "编辑资产目录", description = "编辑资产目录的名称和排序")
    @PutMapping("/directories/{id}")
    public Result<Void> updateDirectory(@PathVariable Long id, @Valid @RequestBody AssetDirectoryUpdateRequest request) {
        assetService.updateDirectory(id, request);
        return Result.success("编辑成功", null);
    }

    @Operation(summary = "删除资产目录", description = "仅能删除无子目录且无关联资产的目录")
    @DeleteMapping("/directories/{id}")
    public Result<Void> deleteDirectory(@PathVariable Long id) {
        assetService.deleteDirectory(id);
        return Result.success("删除成功", null);
    }

    // ==================== 资产 CRUD ====================

    @Operation(summary = "查询资产列表", description = "分页查询资产列表，支持按关键词、状态、目录筛选。排序规则：状态优先，再按更新时间倒序")
    @GetMapping
    public Result<Page<AssetListVo>> getAssetList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long directoryId) {
        Page<AssetListVo> page = assetService.getAssetList(pageNum, pageSize, keyword, status, directoryId);
        return Result.success(page);
    }

    @Operation(summary = "查询资产详情", description = "根据ID获取单个资产完整信息，包含字段定义列表和关联目录")
    @GetMapping("/{id}")
    public Result<AssetDetailVo> getAssetDetail(@PathVariable Long id) {
        AssetDetailVo detail = assetService.getAssetDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "新增数据资产表", description = "新增一条数据资产。assetName 和 englishName 全局唯一，创建后状态为 DRAFT")
    @PostMapping
    public Result<AssetDetailVo> createAsset(@Valid @RequestBody AssetCreateRequest request) {
        AssetDetailVo detail = assetService.createAsset(request);
        return Result.success(detail);
    }

    @Operation(summary = "编辑数据资产表", description = "仅未发布和已停用状态可编辑")
    @PutMapping("/{id}")
    public Result<Void> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetUpdateRequest request) {
        assetService.updateAsset(id, request);
        return Result.success("编辑成功", null);
    }

    @Operation(summary = "删除数据资产表", description = "仅可删除 DRAFT 状态的资产")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return Result.success("删除成功", null);
    }

    // ==================== 状态流转 ====================

    @Operation(summary = "发布数据资产", description = "将未发布或已停用状态的资产发布为已发布")
    @PutMapping("/{id}/publish")
    public Result<Void> publishAsset(@PathVariable Long id) {
        assetService.publishAsset(id);
        return Result.success("发布成功", null);
    }

    @Operation(summary = "停用数据资产", description = "将已发布状态的资产变更为已停用")
    @PutMapping("/{id}/disable")
    public Result<Void> disableAsset(@PathVariable Long id) {
        assetService.disableAsset(id);
        return Result.success("停用成功", null);
    }

    // ==================== 批量操作 ====================

    @Operation(summary = "批量发布数据资产", description = "批量发布选中的数据资产。所选资产不能包含已发布状态")
    @PutMapping("/batch/publish")
    public Result<Void> batchPublish(@Valid @RequestBody BatchIdsRequest request) {
        assetService.batchPublish(request);
        return Result.success("批量发布成功", null);
    }

    @Operation(summary = "批量停用数据资产", description = "批量停用选中的数据资产。所选资产不能包含未发布或已停用状态")
    @PutMapping("/batch/disable")
    public Result<Void> batchDisable(@Valid @RequestBody BatchIdsRequest request) {
        assetService.batchDisable(request);
        return Result.success("批量停用成功", null);
    }
}
