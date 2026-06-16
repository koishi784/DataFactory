package com.datafactory.common.model.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 节点执行结果 VO
 */
@Schema(description = "节点执行结果")
@Data
public class NodeResultVo {

    @Schema(description = "节点标识")
    private String nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点类型")
    private String nodeType;

    @Schema(description = "执行状态：2=成功 / 3=失败 / 5=跳过")
    private Integer status;

    @Schema(description = "节点开始时间", example = "2026-06-02 14:30:00")
    private String startTime;

    @Schema(description = "节点结束时间", example = "2026-06-02 14:30:12")
    private String endTime;

    @Schema(description = "节点耗时（毫秒）")
    private Long duration;

    @Schema(description = "节点输入数据摘要")
    private String inputData;

    @Schema(description = "节点输出数据摘要")
    private String outputData;

    @Schema(description = "错误信息（失败时返回）")
    private String errorMessage;

    @Schema(description = "节点执行日志")
    private String logs;
}
