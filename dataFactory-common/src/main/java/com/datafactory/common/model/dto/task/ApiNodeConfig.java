package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * API 节点配置
 *
 * 调用注册接口节点的配置，包含参数映射、缓存配置和异常处理
 */
@Schema(description = "API 节点配置")
@Data
public class ApiNodeConfig {

    @Schema(description = "关联的注册接口ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "API ID 不能为空")
    private Long apiId;

    @Schema(description = "参数映射列表")
    private List<ParamMappingDto> paramMapping;

    @Schema(description = "超时时间（毫秒），默认使用接口配置", example = "60000")
    private Integer timeout;

    @Schema(description = "重试次数，默认使用接口配置", example = "2")
    private Integer retryCount;

    @Schema(description = "存储配置（缓存规则+有效期）")
    private CacheConfig cacheConfig;

    @Schema(description = "异常处理配置（错误码映射+重试机制）")
    private ErrorHandlingConfig errorHandling;
}
