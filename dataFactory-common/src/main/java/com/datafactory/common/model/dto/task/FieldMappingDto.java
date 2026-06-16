package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字段映射项 DTO
 *
 * 用于 MAPPING/OUTPUT 节点的字段映射配置
 */
@Schema(description = "字段映射项")
@Data
public class FieldMappingDto {

    @Schema(description = "上游节点ID", example = "node_3")
    private String sourceNodeId;

    @Schema(description = "上游节点参数", example = "output.data.id")
    private String sourceParam;

    @Schema(description = "目标字段/列名", example = "firm_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目标字段不能为空")
    private String targetField;

    @Schema(description = "转换规则表达式", example = "amount * 100")
    private String transformRule;

    @Schema(description = "默认值（源字段为空时使用）", example = "0")
    private String defaultValue;
}
