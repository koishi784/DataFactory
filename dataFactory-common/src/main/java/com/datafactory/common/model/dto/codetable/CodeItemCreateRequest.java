package com.datafactory.common.model.dto.codetable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增码值请求参数
 */
@Data
@Schema(description = "新增码值请求参数")
public class CodeItemCreateRequest {

    @Schema(description = "编码取值（同一码表内唯一）", example = "01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "编码取值不能为空")
    private String code;

    @Schema(description = "编码中文名称（同一码表内唯一，仅支持中文及大小写英文）", example = "国有企业", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "编码中文名称不能为空")
    private String name;

    @Schema(description = "编码值", example = "STATED_OWNED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "编码值不能为空")
    private String value;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "父级编码（层级码表）", example = "ROOT")
    private String parentCode;

    @Schema(description = "说明", example = "国有企业编码")
    private String description;
}
