package com.datafactory.common.model.dto.script;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 编辑脚本请求参数 DTO
 *
 * 支持两种模式：
 * - 文件上传模式：fileId 可选（不传则不更新文件）
 * - 在线编辑模式：scriptContent 可选（不传则不更新脚本源代码）
 */
@Data
@Schema(description = "编辑脚本请求参数")
public class ScriptUpdateRequest {

    @Schema(description = "脚本名称（全局唯一，仅支持中文和英文大小写）", example = "工商信息清洗脚本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "脚本名称不能为空")
    @Size(max = 50, message = "脚本名称最大 50 字符")
    @Pattern(regexp = "^[a-zA-Z\\u4e00-\\u9fa5]+$", message = "脚本名称仅支持中文和英文大小写")
    private String scriptName;

    @Schema(description = "脚本类型：GROOVY / PYTHON", example = "GROOVY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "脚本类型不能为空")
    private String scriptType;

    @Schema(description = "所属分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "上传的脚本文件ID（不传则不更新文件）", example = "1001")
    private Long fileId;

    @Schema(description = "脚本源代码（在线编辑模式时使用，不传则不更新源代码）", example = "println jdbcTemplate.queryForList(\"SELECT * FROM table\")")
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
