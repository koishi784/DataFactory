package com.datafactory.common.model.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产详情 VO
 */
@Data
@Schema(description = "资产详情")
public class AssetDetailVo {

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

    @Schema(description = "关联的目录列表")
    private List<AssetDirectorySimpleVo> directories;

    @Schema(description = "字段定义列表")
    private List<AssetFieldVo> fields;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 精简版目录信息
     */
    @Data
    @Schema(description = "关联目录简单信息")
    public static class AssetDirectorySimpleVo {
        @Schema(description = "目录ID")
        private Long id;

        @Schema(description = "目录名称")
        private String name;
    }
}
