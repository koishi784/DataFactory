package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 缓存配置
 *
 * API 节点的存储配置，控制缓存取数规则及有效期
 */
@Schema(description = "缓存配置（API 节点存储配置）")
@Data
public class CacheConfig {

    @Schema(description = "是否启用缓存", example = "true")
    private Boolean enabled;

    @Schema(description = "缓存取数规则：CACHE_FIRST（缓存优先）/ API_FIRST（接口优先）", example = "CACHE_FIRST")
    private String cacheRule;

    @Schema(description = "缓存有效期值", example = "30")
    private Integer cacheTtl;

    @Schema(description = "缓存有效期单位：DAY / HOUR / MINUTE", example = "MINUTE")
    private String cacheTtlUnit;
}
