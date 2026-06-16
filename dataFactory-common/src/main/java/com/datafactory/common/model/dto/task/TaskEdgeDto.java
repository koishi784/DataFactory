package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DAG 连线 DTO
 */
@Schema(description = "DAG 连线")
@Data
public class TaskEdgeDto {

    @Schema(description = "连线标识（如 edge_1）", example = "edge_1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "连线标识不能为空")
    private String edgeId;

    @Schema(description = "源节点ID", example = "node_1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "源节点ID不能为空")
    private String sourceNodeId;

    @Schema(description = "目标节点ID", example = "node_2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目标节点ID不能为空")
    private String targetNodeId;

    @Schema(description = "条件表达式", example = "")
    private String condition;
}
