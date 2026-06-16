package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 映射组件节点配置
 *
 * 字段映射/数据转换节点的配置
 */
@Schema(description = "映射组件节点配置")
@Data
public class MappingNodeConfig {

    @Schema(description = "关联的数据资产表ID", example = "3")
    private Long assetTableId;

    @Schema(description = "字段映射列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "字段映射列表不能为空")
    @Valid
    private List<FieldMappingDto> mappings;
}
