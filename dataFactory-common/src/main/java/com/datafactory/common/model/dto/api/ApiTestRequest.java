package com.datafactory.common.model.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 接口测试调用请求参数
 */
@Schema(description = "接口测试调用请求参数")
@Data
public class ApiTestRequest {

    @Schema(description = "临时覆盖的参数值，Key 为参数名，Value 为参数值",
            example = "{\"orderId\": \"ORD20260602001\"}")
    private Map<String, Object> paramValues;
}
