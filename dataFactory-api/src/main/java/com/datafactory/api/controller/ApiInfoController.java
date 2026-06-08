package com.datafactory.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.ApiCreateRequest;
import com.datafactory.common.model.dto.api.ApiTestRequest;
import com.datafactory.common.model.dto.api.ApiUpdateRequest;
import com.datafactory.common.model.dto.api.BatchCategoryRequest;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.vo.api.ApiDetailVo;
import com.datafactory.common.model.vo.api.ApiInfoVo;
import com.datafactory.common.model.vo.api.ApiTestResultVo;
import com.datafactory.common.response.Result;
import com.datafactory.core.domain.entity.ApiInfo;
import com.datafactory.core.service.IApiInfoService;
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
 * 接口注册管理控制器
 *
 * 提供注册接口的 CRUD、状态管理、批量操作、测试调用等 REST 接口
 */
@Tag(name = "接口注册管理", description = "注册接口的增删改查、发布/停用/删除、批量操作及测试调用")
@RestController
@RequestMapping("/api/v1/apis")
@RequiredArgsConstructor
public class ApiInfoController {

    private final IApiInfoService apiInfoService;

    /**
     * 查询接口列表
     *
     * 分页查询接口列表，支持多条件筛选。排序规则：状态优先（未发布→已发布→已停用），再按更新时间排序。
     *
     * @param pageNum    页码，默认 1
     * @param pageSize   每页条数，默认 20，最大 100
     * @param keyword    关键词，模糊匹配接口名称、接口说明
     * @param status     状态筛选，多值用逗号分隔：0=未发布 / 1=已发布 / 2=已停用
     * @param categoryId 分类ID筛选
     * @param source     接口来源筛选
     * @param sortOrder  更新时间排序：asc（升序）/ desc（降序），默认 desc
     * @return 分页结果
     */
    @Operation(summary = "查询接口列表", description = "分页查询接口列表，支持多条件筛选。排序规则参照全局规范 1.2")
    @GetMapping
    public Result<Page<ApiInfoVo>> getApiList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        Page<ApiInfoVo> page = apiInfoService.getApiList(
                pageNum, pageSize, keyword, status, categoryId, source, sortOrder);
        return Result.success(page);
    }

    /**
     * 查询接口详情
     *
     * @param id 接口ID
     * @return 接口完整信息
     */
    @Operation(summary = "查询接口详情", description = "根据ID获取单个注册接口的完整信息")
    @GetMapping("/{id}")
    public Result<ApiDetailVo> getApiDetail(@PathVariable Long id) {
        ApiDetailVo detail = apiInfoService.getApiDetail(id);
        return Result.success(detail);
    }

    /**
     * 新增注册接口
     *
     * @param request 新增接口请求参数
     * @return 创建的接口信息（状态为 DRAFT）
     */
    @Operation(summary = "新增注册接口", description = "创建一个新的注册接口，创建后状态为 DRAFT")
    @PostMapping
    public Result<ApiInfo> createApi(@Valid @RequestBody ApiCreateRequest request) {
        ApiInfo apiInfo = apiInfoService.createApi(request);
        return Result.success(apiInfo);
    }

    /**
     * 编辑接口（草稿状态）
     *
     * @param id      接口ID
     * @param request 编辑接口请求参数
     * @return 统一响应
     */
    @Operation(summary = "编辑接口", description = "修改处于 DRAFT 状态的接口信息，已发布或已停用接口不可编辑")
    @PutMapping("/{id}")
    public Result<Void> updateApi(@PathVariable Long id, @Valid @RequestBody ApiUpdateRequest request) {
        apiInfoService.updateApi(id, request);
        return Result.success("编辑成功", null);
    }

    /**
     * 发布接口
     *
     * @param id 接口ID
     * @return 统一响应
     */
    @Operation(summary = "发布接口", description = "将 DRAFT 状态的接口发布上线，状态变更为 PUBLISHED")
    @PutMapping("/{id}/publish")
    public Result<Void> publishApi(@PathVariable Long id) {
        apiInfoService.publishApi(id);
        return Result.success("发布成功", null);
    }

    /**
     * 停用接口
     *
     * @param id 接口ID
     * @return 统一响应
     */
    @Operation(summary = "停用接口", description = "将 PUBLISHED 状态的接口停用，状态变更为 DISABLED")
    @PutMapping("/{id}/disable")
    public Result<Void> disableApi(@PathVariable Long id) {
        apiInfoService.disableApi(id);
        return Result.success("停用成功", null);
    }

    /**
     * 删除接口
     *
     * @param id 接口ID
     * @return 统一响应
     */
    @Operation(summary = "删除接口", description = "仅可删除 DRAFT 状态的接口，已发布或已停用的接口不可删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteApi(@PathVariable Long id) {
        apiInfoService.deleteApi(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量发布接口
     *
     * @param request 批量发布请求参数
     * @return 统一响应
     */
    @Operation(summary = "批量发布接口", description = "批量发布选中的接口。校验规则：所选接口不能包含已发布状态")
    @PutMapping("/batch/publish")
    public Result<Void> batchPublish(@Valid @RequestBody BatchIdsRequest request) {
        apiInfoService.batchPublish(request);
        return Result.success("批量发布成功", null);
    }

    /**
     * 批量停用接口
     *
     * @param request 批量停用请求参数
     * @return 统一响应
     */
    @Operation(summary = "批量停用接口", description = "批量停用选中的接口。校验规则：所选接口不能包含未发布或已停用状态")
    @PutMapping("/batch/disable")
    public Result<Void> batchDisable(@Valid @RequestBody BatchIdsRequest request) {
        apiInfoService.batchDisable(request);
        return Result.success("批量停用成功", null);
    }

    /**
     * 批量修改接口分类
     *
     * @param request 批量分类请求参数
     * @return 统一响应
     */
    @Operation(summary = "批量修改接口分类", description = "批量修改选中的接口所属分类。校验规则：所选接口不能包含已发布状态")
    @PutMapping("/batch/category")
    public Result<Void> batchCategory(@Valid @RequestBody BatchCategoryRequest request) {
        apiInfoService.batchCategory(request);
        return Result.success("批量分类成功", null);
    }

    /**
     * 接口测试调用
     *
     * @param id      接口ID
     * @param request 测试调用请求参数
     * @return 测试调用结果
     */
    @Operation(summary = "接口测试调用", description = "对待注册接口进行在线测试调用，返回调用结果。不限制接口状态")
    @PostMapping("/{id}/test")
    public Result<ApiTestResultVo> testApi(@PathVariable Long id, @RequestBody ApiTestRequest request) {
        ApiTestResultVo result = apiInfoService.testApi(id, request);
        return Result.success(result);
    }
}
