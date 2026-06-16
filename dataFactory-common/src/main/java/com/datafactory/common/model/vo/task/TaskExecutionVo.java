package com.datafactory.common.model.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 任务执行历史列表项 VO
 */
@Schema(description = "任务执行历史列表项")
@Data
public class TaskExecutionVo {

    @Schema(description = "执行记录ID")
    private Long executionId;

    @Schema(description = "执行状态：0=等待 / 1=执行中 / 2=成功 / 3=失败 / 4=已取消")
    private Integer status;

    @Schema(description = "开始时间", example = "2026-06-02 14:30:00")
    private String startTime;

    @Schema(description = "结束时间", example = "2026-06-02 14:30:45")
    private String endTime;

    @Schema(description = "耗时（毫秒）")
    private Long duration;

    @Schema(description = "触发方式：API / CRON / MANUAL")
    private String triggerType;

    @Schema(description = "触发人")
    private String triggerBy;
}
