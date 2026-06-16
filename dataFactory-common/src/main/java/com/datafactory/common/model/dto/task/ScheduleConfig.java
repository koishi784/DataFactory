package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 定时调度配置
 *
 * 定时任务的 Cron 表达式配置，支持三种模式：
 * - RANGE（范围）：从 fromValue 到 toValue，每个时间单位执行一次
 * - INTERVAL（间隔）：从 fromValue 开始，每 intervalValue 个时间单位执行一次
 * - SPECIFY（指定）：在 specifiedValues 中指定的时间点执行
 */
@Schema(description = "定时调度配置")
@Data
public class ScheduleConfig {

    @Schema(description = "时间单位：SECOND / MINUTE / HOUR / DAY / MONTH / WEEK / YEAR", example = "SECOND")
    private String timeUnit;

    @Schema(description = "配置模式：RANGE（范围）/ INTERVAL（间隔）/ SPECIFY（指定）", example = "INTERVAL")
    private String configType;

    @Schema(description = "起始值（RANGE / INTERVAL 模式使用）", example = "5")
    private Integer fromValue;

    @Schema(description = "结束值（RANGE 模式使用）", example = "55")
    private Integer toValue;

    @Schema(description = "间隔值（INTERVAL 模式使用）", example = "10")
    private Integer intervalValue;

    @Schema(description = "指定值列表（SPECIFY 模式使用），秒0-59，分0-59，时0-23，日1-31，月1-12，周1-7", example = "[0, 15, 30, 45]")
    private List<Integer> specifiedValues;
}
