package com.datafactory.common.model.vo.codetable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 码表详情 VO（包含码值列表）
 */
@Data
@Schema(description = "码表详情")
public class CodeTableDetailVo {

    @Schema(description = "码表ID")
    private Long id;

    @Schema(description = "码表名称")
    private String tableName;

    @Schema(description = "码表编号（系统自动生成，格式 MZB + 5 位数字）")
    private String tableCode;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "状态：0=未发布 / 1=已发布 / 2=已停用")
    private Integer status;

    @Schema(description = "码值数量")
    private Integer codeItemCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "码值列表")
    private List<CodeItemVo> items;
}
