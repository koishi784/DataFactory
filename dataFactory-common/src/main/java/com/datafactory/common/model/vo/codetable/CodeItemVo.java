package com.datafactory.common.model.vo.codetable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 码值 VO
 */
@Data
@Schema(description = "码值项")
public class CodeItemVo {

    @Schema(description = "码值ID")
    private Long id;

    @Schema(description = "编码取值")
    private String code;

    @Schema(description = "编码中文名称")
    private String name;

    @Schema(description = "编码值")
    private String value;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "父级编码（层级码表）")
    private String parentCode;

    @Schema(description = "码值状态：1=启用 / 0=停用")
    private Integer status;

    @Schema(description = "说明")
    private String description;
}
