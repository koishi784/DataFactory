package com.datafactory.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求参数
 */
@Schema(description = "刷新令牌请求参数")
@Data
public class RefreshTokenRequest {

    @Schema(description = "刷新令牌（登录或注册时获取）", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4gZXhhbXBsZQ", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
