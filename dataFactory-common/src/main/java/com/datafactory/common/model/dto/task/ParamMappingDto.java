package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 参数映射项 DTO
 *
 * 用于 START/API/SCRIPT 节点的参数映射配置
 */
@Schema(description = "参数映射项")
@Data
public class ParamMappingDto {

    @Schema(description = "参数名称", example = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数名称不能为空")
    private String paramName;

    @Schema(description = "参数类型", example = "int")
    private String paramType;

    @Schema(description = "对应上游节点ID", example = "node_1")
    private String sourceNodeId;

    @Schema(description = "上游节点参数", example = "output.data.id")
    private String sourceParam;

    @Schema(description = "参数描述", example = "企业唯一标识")
    private String description;

    @Schema(description = "测试值", example = "12345")
    private String testValue;
}
