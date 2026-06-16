package com.datafactory.common.model.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 脚本分类树节点 VO
 */
@Data
@Schema(description = "脚本分类树节点")
public class ScriptCategoryVo {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父级分类ID，顶级为 0")
    private Long parentId;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "子分类列表")
    private List<ScriptCategoryVo> children;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
