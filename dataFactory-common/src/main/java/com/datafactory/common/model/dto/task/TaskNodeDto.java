package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DAG 节点通用 DTO
 */
@Schema(description = "DAG 节点")
@Data
public class TaskNodeDto {

    @Schema(description = "节点标识（DAG图中唯一，如 node_1）", example = "node_1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "节点标识不能为空")
    private String nodeId;

    @Schema(description = "节点名称", example = "工商基本信息接口", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    @Schema(description = "节点类型：START/API/SCRIPT/MAPPING/OUTPUT/END", example = "API", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "节点类型不能为空")
    private String nodeType;

    @Schema(description = "画布 X 坐标", example = "100.0")
    private Double positionX;

    @Schema(description = "画布 Y 坐标", example = "200.0")
    private Double positionY;

    @Schema(description = "节点配置（JSON 对象，按 nodeType 不同结构不同）")
    private Object config;
}
