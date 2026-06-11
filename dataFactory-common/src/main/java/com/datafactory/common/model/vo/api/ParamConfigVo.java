package com.datafactory.common.model.vo.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 请求参数配置 VO
 */
@Schema(description = "请求参数配置")
@Data
public class ParamConfigVo {

    @Schema(description = "参数ID", example = "1")
    private Long id;

    @Schema(description = "参数名称", example = "orderId")
    private String paramName;

    @Schema(description = "参数类型：QUERY / PATH / HEADER / BODY", example = "PATH")
    private String paramType;

    @Schema(description = "数据类型：STRING / INTEGER / LONG / DOUBLE / BOOLEAN / DATE / DATETIME / OBJECT / ARRAY", example = "STRING")
    private String dataType;

    @Schema(description = "是否必填", example = "true")
    private Boolean required;

    @Schema(description = "参数说明", example = "订单ID")
    private String description;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "示例值", example = "ORD20260602001")
    private String exampleValue;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "校验规则（正则表达式）")
    private String validationRule;

    @Schema(description = "最小值（数值类型）")
    private String minValue;

    @Schema(description = "最大值（数值类型）")
    private String maxValue;
}
