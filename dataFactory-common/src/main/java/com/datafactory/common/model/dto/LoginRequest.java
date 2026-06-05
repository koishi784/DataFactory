package com.datafactory.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数
 */
@Schema(description = "登录请求参数")
@Data
public class LoginRequest {

    /** 登录账号（支持用户名、邮箱或手机号） */
    @Schema(description = "登录账号（支持用户名、邮箱或手机号）", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "登录账号不能为空")
    private String account;

    /** 登录密码 */
    @Schema(description = "登录密码", example = "Abc@123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 是否记住登录，为 true 时刷新令牌有效期为 7 天 */
    @Schema(description = "是否记住登录，为 true 时刷新令牌有效期为 7 天", example = "true", defaultValue = "false")
    private Boolean rememberMe;
}
