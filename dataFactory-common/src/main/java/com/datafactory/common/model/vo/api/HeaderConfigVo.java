package com.datafactory.common.model.vo.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 请求头配置 VO
 */
@Schema(description = "请求头配置")
@Data
public class HeaderConfigVo {

    @Schema(description = "请求头名称", example = "Content-Type")
    private String key;

    @Schema(description = "请求头值", example = "application/json")
    private String value;

    @Schema(description = "是否必填", example = "true")
    private Boolean required;

    @Schema(description = "说明", example = "请求内容类型")
    private String description;
}
