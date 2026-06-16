package com.datafactory.common.model.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 测试运行结果 VO
 */
@Schema(description = "测试运行结果")
@Data
public class TaskTestRunVo {

    @Schema(description = "执行记录ID")
    private Long executionId;

    @Schema(description = "执行状态：0=等待 / 1=执行中 / 2=成功 / 3=失败 / 4=已取消")
    private Integer status;

    @Schema(description = "开始时间", example = "2026-06-02 14:30:00")
    private String startTime;

    @Schema(description = "结束时间", example = "2026-06-02 14:30:45")
    private String endTime;

    @Schema(description = "总耗时（毫秒）")
    private Long totalDuration;

    @Schema(description = "各节点执行结果列表")
    private List<NodeResultVo> nodeResults;
}
