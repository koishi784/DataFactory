package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务触发设置请求参数
 *
 * 配置任务触发方式（三步向导第三步），支持 API 触发和定时任务两种方式
 */
@Schema(description = "任务触发设置请求参数")
@Data
public class TaskTriggerConfigRequest {

    @Schema(description = "调度类型：API（生成API触发）/ CRON（定时任务触发）", example = "API", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "调度类型不能为空")
    private String scheduleType;

    // ===== API 模式专属 =====

    @Schema(description = "API 触发配置（scheduleType=API 时必填）")
    private ApiTriggerConfig apiConfig;

    // ===== CRON 模式专属 =====

    @Schema(description = "任务默认参数（CRON 模式时使用）")
    private Map<String, Object> taskParams;

    @Schema(description = "定时调度配置（CRON 模式时使用）")
    private ScheduleConfig scheduleConfig;

    @Schema(description = "生效日期", example = "2026-06-15T00:00:00")
    private LocalDateTime effectiveDate;

    @Schema(description = "失效日期", example = "2027-06-15T00:00:00")
    private LocalDateTime expireDate;
}
