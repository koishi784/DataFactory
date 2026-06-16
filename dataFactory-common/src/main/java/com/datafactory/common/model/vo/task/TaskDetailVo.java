package com.datafactory.common.model.vo.task;

import com.datafactory.common.model.dto.task.TaskEdgeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 任务详情 VO
 *
 * 包含任务基本信息、DAG 节点和连线、触发配置等完整信息
 */
@Schema(description = "任务详情")
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDetailVo extends TaskListVo {

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "DAG 节点列表")
    private List<NodeDetailVo> nodes;

    @Schema(description = "DAG 连线列表")
    private List<TaskEdgeDto> edges;

    @Schema(description = "触发配置信息")
    private TriggerConfigVo triggerConfig;

    /**
     * 节点详情（含 config 解析后的节点配置）
     */
    @Schema(description = "DAG 节点详情")
    @Data
    public static class NodeDetailVo {

        @Schema(description = "节点ID")
        private Long id;

        @Schema(description = "节点标识（DAG图中唯一）")
        private String nodeId;

        @Schema(description = "节点名称")
        private String nodeName;

        @Schema(description = "节点类型：START/API/SCRIPT/MAPPING/OUTPUT/END")
        private String nodeType;

        @Schema(description = "画布 X 坐标")
        private Double positionX;

        @Schema(description = "画布 Y 坐标")
        private Double positionY;

        @Schema(description = "节点配置（已解析为具体类型对象）")
        private Object nodeConfig;

        @Schema(description = "创建时间")
        private String createTime;

        @Schema(description = "更新时间")
        private String updateTime;
    }

    /**
     * 触发配置 VO
     */
    @Schema(description = "触发配置信息")
    @Data
    public static class TriggerConfigVo {

        @Schema(description = "调度类型：API / CRON")
        private String scheduleType;

        @Schema(description = "API 接口名称（API 模式）")
        private String apiName;

        @Schema(description = "API 访问路径（API 模式）")
        private String apiPath;

        @Schema(description = "Cron 表达式（CRON 模式）")
        private String cronExpression;

        @Schema(description = "任务默认参数（CRON 模式）")
        private Map<String, Object> taskParams;

        @Schema(description = "生效日期")
        private String effectiveDate;

        @Schema(description = "失效日期")
        private String expireDate;
    }
}
