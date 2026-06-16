package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * START 节点配置
 *
 * 任务流程的起始节点，每个任务有且仅有一个 START 节点
 */
@Schema(description = "START 节点配置")
@Data
public class StartNodeConfig {

    @Schema(description = "START 节点输出参数列表（自动解析下游节点参数后合并的结果）")
    private List<ParamMappingDto> outputParams;
}
