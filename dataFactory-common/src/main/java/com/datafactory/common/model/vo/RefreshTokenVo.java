package com.datafactory.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 刷新令牌响应 VO
 */
@Schema(description = "刷新令牌响应")
@Data
@Builder
public class RefreshTokenVo {

    @Schema(description = "新的访问令牌（JWT）", example = "eyJhbGciOiJIUzI1NiJ9.xxx_NEW_TOKEN")
    private String accessToken;

    @Schema(description = "新的刷新令牌（旧令牌立即失效）", example = "NEW_REFRESH_TOKEN_STRING")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer", defaultValue = "Bearer")
    private String tokenType;

    @Schema(description = "访问令牌有效期（秒）", example = "7200")
    private Long expiresIn;
}
