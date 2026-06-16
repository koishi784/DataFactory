package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 脚本组件节点配置
 *
 * 执行数据处理脚本的节点配置
 */
@Schema(description = "脚本组件节点配置")
@Data
public class ScriptNodeConfig {

    @Schema(description = "关联的脚本ID", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "脚本ID不能为空")
    private Long scriptId;

    @Schema(description = "指定脚本版本，不传则使用最新发布版本", example = "1.2.0")
    private String scriptVersion;

    @Schema(description = "参数映射列表")
    private List<ParamMappingDto> paramMapping;

    @Schema(description = "执行脚本时连接的数据源ID", example = "1")
    private Long dataSourceId;
}
