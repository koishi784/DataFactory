package com.datafactory.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录/注册响应 VO
 * 登录和注册接口统一使用此结构返回，包含访问令牌、刷新令牌及用户基本信息。
 */
@Schema(description = "登录/注册响应")
@Data
public class LoginVo {

    /** 访问令牌（JWT） */
    @Schema(description = "访问令牌（JWT）", example = "eyJhbGciOiJIUzI1NiJ9.xxx")
    private String accessToken;

    /** 刷新令牌，用于获取新的访问令牌 */
    @Schema(description = "刷新令牌", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4gZXhhbXBsZQ")
    private String refreshToken;

    /** 令牌类型，固定值 Bearer */
    @Schema(description = "令牌类型", example = "Bearer", defaultValue = "Bearer")
    private String tokenType;

    /** 访问令牌有效期（秒） */
    @Schema(description = "访问令牌有效期（秒）", example = "7200")
    private Long expiresIn;

    /** 用户基本信息 */
    @Schema(description = "用户基本信息")
    private UserInfoVo userInfo;
}
