package com.datafactory.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datafactory.common.model.dto.api.BatchIdsRequest;
import com.datafactory.common.model.dto.datastandard.DataStandardCreateRequest;
import com.datafactory.common.model.dto.datastandard.DataStandardUpdateRequest;
import com.datafactory.common.model.vo.datastandard.DataStandardDetailVo;
import com.datafactory.common.model.vo.datastandard.DataStandardListVo;
import com.datafactory.common.model.vo.datastandard.DataStandardImportResultVo;
import com.datafactory.common.response.Result;
import com.datafactory.core.service.IDataStandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 数据标准管理控制器
 *
 * 提供数据标准的 CRUD、状态管理（发布/停用/删除）、批量操作等 REST 接口。
 */
@Tag(name = "数据标准管理", description = "数据标准的增删改查、发布/停用/删除及批量操作")
@RestController
@RequestMapping("/api/v1/data-standards")
@RequiredArgsConstructor
public class DataStandardController {

    private final IDataStandardService dataStandardService;

    /**
     * 查询数据标准列表
     *
     * 分页查询数据标准列表，支持多条件筛选。
     * 排序规则：状态优先（未发布→已发布→已停用），再按更新时间倒序。
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @param keyword  关键词，模糊匹配中文名称、英文名称、标准编号
     * @param status   状态筛选，多值用逗号分隔：0=未发布 / 1=已发布 / 2=已停用
     * @param dataType 数据类型筛选：String / Int / Float / Enum
     * @return 分页结果
     */
    @Operation(summary = "查询数据标准列表", description = "分页查询数据标准列表，支持多条件筛选。排序规则：状态优先（未发布→已发布→已停用），再按更新时间倒序")
    @GetMapping
    public Result<Page<DataStandardListVo>> getStandardList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dataType) {
        Page<DataStandardListVo> page = dataStandardService.getStandardList(
                pageNum, pageSize, keyword, status, dataType);
        return Result.success(page);
    }

    /**
     * 查询数据标准详情
     *
     * @param id 标准ID
     * @return 数据标准详情
     */
    @Operation(summary = "查询数据标准详情", description = "根据ID获取单个数据标准的完整信息")
    @GetMapping("/{id}")
    public Result<DataStandardDetailVo> getStandardDetail(@PathVariable Long id) {
        DataStandardDetailVo detail = dataStandardService.getStandardDetail(id);
        return Result.success(detail);
    }

    /**
     * 新增数据标准
     *
     * 标准编号由系统自动生成（格式 BZ + 5 位数字），创建后状态为 DRAFT。
     *
     * @param request 新增数据标准请求参数
     * @return 创建的数据标准详情
     */
    @Operation(summary = "新增数据标准", description = "新增一条数据标准。标准编号由系统自动生成（格式 BZ + 5 位数字），创建后状态为 DRAFT")
    @PostMapping
    public Result<DataStandardDetailVo> createStandard(@Valid @RequestBody DataStandardCreateRequest request) {
        DataStandardDetailVo detail = dataStandardService.createStandard(request);
        return Result.success(detail);
    }

    /**
     * 编辑数据标准
     *
     * 仅未发布(0)和已停用(2)状态可编辑。标准编号不可修改。
     *
     * @param id      标准ID
     * @param request 编辑数据标准请求参数
     * @return 统一响应
     */
    @Operation(summary = "编辑数据标准", description = "仅未发布和已停用状态可编辑。标准编号不可修改")
    @PutMapping("/{id}")
    public Result<Void> updateStandard(@PathVariable Long id, @Valid @RequestBody DataStandardUpdateRequest request) {
        dataStandardService.updateStandard(id, request);
        return Result.success("编辑成功", null);
    }

    /**
     * 发布数据标准
     *
     * @param id 标准ID
     * @return 统一响应
     */
    @Operation(summary = "发布数据标准", description = "将未发布或已停用状态的数据标准发布为已发布")
    @PutMapping("/{id}/publish")
    public Result<Void> publishStandard(@PathVariable Long id) {
        dataStandardService.publishStandard(id);
        return Result.success("发布成功", null);
    }

    /**
     * 停用数据标准
     *
     * @param id 标准ID
     * @return 统一响应
     */
    @Operation(summary = "停用数据标准", description = "将已发布状态的数据标准变更为已停用")
    @PutMapping("/{id}/disable")
    public Result<Void> disableStandard(@PathVariable Long id) {
        dataStandardService.disableStandard(id);
        return Result.success("停用成功", null);
    }

    /**
     * 删除数据标准
     *
     * 仅可删除 DRAFT 状态的标准。
     *
     * @param id 标准ID
     * @return 统一响应
     */
    @Operation(summary = "删除数据标准", description = "仅可删除 DRAFT 状态的数据标准，已发布或已停用的不可删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteStandard(@PathVariable Long id) {
        dataStandardService.deleteStandard(id);
        return Result.success("删除成功", null);
    }

    /**
     * 批量发布数据标准
     *
     * 校验规则：所选标准不能包含已发布状态。
     *
     * @param request 批量发布请求参数
     * @return 统一响应
     */
    @Operation(summary = "批量发布数据标准", description = "批量发布选中的数据标准。校验规则：所选标准不能包含已发布状态")
    @PutMapping("/batch/publish")
    public Result<Void> batchPublish(@Valid @RequestBody BatchIdsRequest request) {
        dataStandardService.batchPublish(request);
        return Result.success("批量发布成功", null);
    }

    /**
     * 批量停用数据标准
     *
     * 校验规则：所选标准不能包含未发布或已停用状态。
     *
     * @param request 批量停用请求参数
     * @return 统一响应
     */
    @Operation(summary = "批量停用数据标准", description = "批量停用选中的数据标准。校验规则：所选标准不能包含未发布或已停用状态")
    @PutMapping("/batch/disable")
    public Result<Void> batchDisable(@Valid @RequestBody BatchIdsRequest request) {
        dataStandardService.batchDisable(request);
        return Result.success("批量停用成功", null);
    }

    /**
     * 下载导入模板
     *
     * 生成包含表头行的 Excel 模板文件，供用户填写数据标准信息后批量导入。
     *
     * @param response HTTP 响应
     * @throws IOException 文件写入异常
     */
    @Operation(summary = "下载导入模板", description = "下载数据标准批量导入的 Excel 模板文件，包含表头列")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        dataStandardService.downloadTemplate(response);
    }

    /**
     * 批量导入数据标准
     *
     * 通过上传 Excel 文件批量导入数据标准，按照六步校验规则进行校验和过滤。
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果
     * @throws IOException 文件读取异常
     */
    @Operation(summary = "批量导入数据标准", description = "通过上传 Excel 文件批量导入数据标准，按六步规则校验过滤。仅支持 .xlsx 格式，最大 50MB")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DataStandardImportResultVo> importStandards(@RequestParam("file") MultipartFile file) throws IOException {
        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            return Result.error(100400, "上传文件不能为空");
        }
        // 校验文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
            return Result.error(100406, "仅支持 .xlsx 格式的 Excel 文件");
        }
        // 校验文件大小（最大 50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            return Result.error(100418, "文件大小超出限制，最大 50MB");
        }

        DataStandardImportResultVo result = dataStandardService.importStandards(file);
        return Result.success(result);
    }
}
