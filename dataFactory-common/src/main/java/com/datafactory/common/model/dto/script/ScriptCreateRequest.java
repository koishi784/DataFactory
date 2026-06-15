package com.datafactory.common.model.dto.script;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增脚本请求参数 DTO
 *
 * 支持两种模式：
 * - 文件上传模式：fileId 必填，scriptContent 不填（适用于 PYTHON/GROOVY 文件上传）
 * - 在线编辑模式：scriptContent 必填，fileId 不填（适用于 GROOVY 文本编辑）
 */
@Data
@Schema(description = "新增脚本请求参数")
public class ScriptCreateRequest {

    @Schema(description = "脚本名称（全局唯一，仅支持中文和英文大小写）", example = "工商信息清洗脚本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "脚本名称不能为空")
    @Size(max = 50, message = "脚本名称最大 50 字符")
    @Pattern(regexp = "^[a-zA-Z\\u4e00-\\u9fa5]+$", message = "脚本名称仅支持中文和英文大小写")
    private String scriptName;

    @Schema(description = "脚本类型：GROOVY / PYTHON", example = "GROOVY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "脚本类型不能为空")
    private String scriptType;

    @Schema(description = "所属分类ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属分类不能为空")
    private Long categoryId;

    @Schema(description = "上传的脚本文件ID（文件上传模式时必填，通过文件上传接口获取）", example = "1001")
    private Long fileId;

    @Schema(description = "脚本源代码（在线编辑模式时必填，fileId 和 scriptContent 至少填一个）", example = "println jdbcTemplate.queryForList(\"SELECT * FROM table\")")
    private String scriptContent;

    @Schema(description = "说明", example = "清洗企业工商信息数据")
    @Size(max = 500, message = "说明最大 500 字符")
    private String description;

    @Schema(description = "输入参数定义列表")
    @Valid
    private List<ScriptParamDTO> inputParams;

    @Schema(description = "输出参数定义列表")
    @Valid
    private List<ScriptParamDTO> outputParams;
}
