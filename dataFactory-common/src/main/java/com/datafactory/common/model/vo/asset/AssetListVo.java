package com.datafactory.common.model.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产列表项 VO
 */
@Data
@Schema(description = "资产列表项")
public class AssetListVo {

    @Schema(description = "资产ID")
    private Long id;

    @Schema(description = "中文名称")
    private String assetName;

    @Schema(description = "英文名称")
    private String englishName;

    @Schema(description = "数据资产表描述")
    private String description;

    @Schema(description = "状态：0=未发布 / 1=已发布 / 2=已停用")
    private Integer status;

    @Schema(description = "关联的目录ID列表")
    private List<Long> directoryIds;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
