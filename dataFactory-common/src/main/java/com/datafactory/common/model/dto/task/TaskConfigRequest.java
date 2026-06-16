package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 更新任务 DAG 配置请求参数
 *
 * 配置任务 DAG 流程（三步向导第二步），包含节点和连线
 */
@Schema(description = "更新任务 DAG 配置请求参数")
@Data
public class TaskConfigRequest {

    @Schema(description = "DAG 节点列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "节点列表不能为空")
    @Valid
    private List<TaskNodeDto> nodes;

    @Schema(description = "DAG 连线列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<TaskEdgeDto> edges;
}
