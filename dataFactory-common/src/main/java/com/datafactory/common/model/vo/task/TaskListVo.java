package com.datafactory.common.model.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 任务列表项 VO
 */
@Schema(description = "任务列表项")
@Data
public class TaskListVo {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "任务说明")
    private String taskDescription;

    @Schema(description = "所属分类ID")
    private Long categoryId;

    @Schema(description = "发布状态：0=未发布 / 1=已发布 / 2=已停用")
    private Integer status;

    @Schema(description = "最近执行状态：0=等待 / 1=执行中 / 2=成功 / 3=失败 / 4=已取消")
    private Integer executeStatus;

    @Schema(description = "调度类型：API / CRON")
    private String scheduleType;

    @Schema(description = "最近执行时间", example = "2026-06-02 14:30:00")
    private String lastExecuteTime;

    @Schema(description = "下次执行时间", example = "2026-06-03 02:00:00")
    private String nextExecuteTime;

    @Schema(description = "创建时间", example = "2026-06-01 10:00:00")
    private String createTime;

    @Schema(description = "更新时间", example = "2026-06-02 14:30:00")
    private String updateTime;
}
