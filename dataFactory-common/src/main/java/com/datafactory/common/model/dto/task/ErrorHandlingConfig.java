package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 异常处理配置
 *
 * API 节点的异常处理配置，包括错误码映射和重试机制
 */
@Schema(description = "异常处理配置（API 节点）")
@Data
public class ErrorHandlingConfig {

    @Schema(description = "错误码参数字段名", example = "errorCode")
    private String errorCodeParam;

    @Schema(description = "错误码映射列表")
    private List<ErrorCodeMapping> errorCodeMappings;

    @Schema(description = "是否启用重试机制", example = "true")
    private Boolean retryEnabled;

    @Schema(description = "重试次数", example = "3")
    private Integer retryCount;

    @Schema(description = "重试间隔（秒）", example = "5")
    private Integer retryInterval;
}
