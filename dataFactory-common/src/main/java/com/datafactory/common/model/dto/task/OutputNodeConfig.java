package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 输出组件节点配置
 *
 * 数据输出/写入目标节点的配置
 */
@Schema(description = "输出组件节点配置")
@Data
public class OutputNodeConfig {

    @Schema(description = "输出类型：DATABASE / FILE / API_PUSH", example = "DATABASE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "输出类型不能为空")
    private String outputType;

    @Schema(description = "目标数据源ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标数据源不能为空")
    private Long dataSourceId;

    @Schema(description = "目标表名", example = "dwd_order_summary", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目标表名不能为空")
    private String tableName;

    @Schema(description = "写入模式：INSERT / UPSERT / OVERWRITE / APPEND", example = "INSERT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "写入模式不能为空")
    private String writeMode;

    @Schema(description = "输出字段映射列表")
    private List<FieldMappingDto> fieldMappings;
}
