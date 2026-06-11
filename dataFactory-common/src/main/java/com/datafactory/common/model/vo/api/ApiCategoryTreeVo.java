package com.datafactory.common.model.vo.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口分类树节点 VO
 */
@Schema(description = "接口分类树节点")
@Data
public class ApiCategoryTreeVo {

    @Schema(description = "分类ID", example = "1")
    private Long id;

    @Schema(description = "分类名称", example = "电商数据")
    private String name;

    @Schema(description = "父级分类ID，顶级为 0", example = "0")
    private Long parentId;

    @Schema(description = "层级，从 1 开始", example = "1")
    private Integer level;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "子分类列表")
    private List<ApiCategoryTreeVo> children;

    @Schema(description = "创建时间", example = "2026-06-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
