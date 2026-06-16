package com.datafactory.common.model.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 任务分类树节点 VO
 */
@Schema(description = "任务分类树节点")
@Data
public class TaskCategoryVo {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父级分类ID，顶级为 0")
    private Long parentId;

    @Schema(description = "层级，从 1 开始")
    private Integer level;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "子分类列表")
    private List<TaskCategoryVo> children;

    @Schema(description = "创建时间", example = "2026-06-01 10:00:00")
    private String createTime;
}
