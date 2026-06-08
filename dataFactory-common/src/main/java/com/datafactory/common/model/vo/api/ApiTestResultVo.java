package com.datafactory.common.model.vo.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 接口测试调用结果 VO
 */
@Schema(description = "接口测试调用结果")
@Data
public class ApiTestResultVo {

    @Schema(description = "调用是否成功", example = "true")
    private Boolean success;

    @Schema(description = "HTTP 状态码", example = "200")
    private Integer statusCode;

    @Schema(description = "响应耗时（毫秒）", example = "856")
    private Long responseTime;

    @Schema(description = "响应内容", example = "{\"orderId\":\"ORD20260602001\",\"amount\":299.00}")
    private String responseBody;

    @Schema(description = "响应头")
    private Map<String, String> responseHeaders;

    @Schema(description = "错误信息（调用失败时返回）", example = "连接超时")
    private String errorMessage;
}
