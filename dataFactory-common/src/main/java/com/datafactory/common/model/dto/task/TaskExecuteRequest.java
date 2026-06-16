package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 手动执行任务请求参数
 *
 * 立即执行指定任务（仅已发布状态的任务可执行）
 */
@Schema(description = "手动执行任务请求参数")
@Data
public class TaskExecuteRequest {

    @Schema(description = "任务参数，Key 为参数名，Value 为参数值")
    private Map<String, Object> taskParams;
}
