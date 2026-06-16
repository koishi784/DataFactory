package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 测试运行任务请求参数
 *
 * 在线测试运行指定任务的 DAG 流程，可传入任务全局参数和调试模式标识
 */
@Schema(description = "测试运行任务请求参数")
@Data
public class TaskTestRunRequest {

    @Schema(description = "任务全局参数，Key 为参数名，Value 为参数值")
    private Map<String, Object> taskParams;

    @Schema(description = "是否调试模式，默认 false。调试模式下将输出更详细的执行日志", example = "true")
    private Boolean debugMode;
}
