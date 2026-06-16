package com.datafactory.common.model.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资产字段定义 VO
 */
@Data
@Schema(description = "资产字段定义")
public class AssetFieldVo {

    @Schema(description = "字段ID")
    private Long id;

    @Schema(description = "字段英文名称")
    private String englishFieldName;

    @Schema(description = "字段中文名称")
    private String chineseFieldName;

    @Schema(description = "字段说明")
    private String description;

    @Schema(description = "关联数据标准ID")
    private Long standardId;

    @Schema(description = "排序号")
    private Integer sortOrder;
}
