package com.datafactory.common.model.dto.script;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑脚本分类请求参数 DTO
 */
@Data
@Schema(description = "编辑脚本分类请求参数")
public class ScriptCategoryUpdateRequest {

    @Schema(description = "分类名称", example = "数据清洗", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最大 50 字符")
    private String name;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;
}
