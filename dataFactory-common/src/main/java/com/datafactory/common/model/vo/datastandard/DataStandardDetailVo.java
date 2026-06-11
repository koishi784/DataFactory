package com.datafactory.common.model.vo.datastandard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据标准详情 VO
 */
@Data
@Schema(description = "数据标准详情")
public class DataStandardDetailVo {

    @Schema(description = "标准ID")
    private Long id;

    @Schema(description = "中文名称")
    private String name;

    @Schema(description = "英文名称")
    private String englishName;

    @Schema(description = "标准编号（系统自动生成，格式 BZ + 5 位数字）")
    private String standardCode;

    @Schema(description = "数据类型：String / Int / Float / Enum")
    private String dataType;

    @Schema(description = "数据长度")
    private Integer length;

    @Schema(description = "精度")
    private Integer precision;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "取值范围最小值")
    private String rangeMin;

    @Schema(description = "取值范围最大值")
    private String rangeMax;

    @Schema(description = "枚举范围（引用码表编码）")
    private String enumRange;

    @Schema(description = "来源机构")
    private String sourceOrganization;

    @Schema(description = "是否可为空：0=可为空 / 1=不可为空")
    private Integer nullable;

    @Schema(description = "标准说明")
    private String description;

    @Schema(description = "状态：0=未发布 / 1=已发布 / 2=已停用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
