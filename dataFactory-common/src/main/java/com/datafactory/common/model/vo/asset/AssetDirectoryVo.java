package com.datafactory.common.model.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产目录树节点 VO
 */
@Data
@Schema(description = "资产目录树节点")
public class AssetDirectoryVo {

    @Schema(description = "目录ID")
    private Long id;

    @Schema(description = "目录名称")
    private String name;

    @Schema(description = "父节点ID，顶级为 0")
    private Long parentId;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "子目录列表")
    private List<AssetDirectoryVo> children;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
