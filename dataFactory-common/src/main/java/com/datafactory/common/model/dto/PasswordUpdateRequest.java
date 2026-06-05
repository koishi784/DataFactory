package com.datafactory.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求参数
 */
@Schema(description = "修改密码请求参数")
@Data
public class PasswordUpdateRequest {

    @Schema(description = "原密码", example = "Abc@123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码，须包含大写字母、小写字母、数字", example = "Xyz#654321", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需为8-32个字符")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,32}$",
            message = "密码须包含大写字母、小写字母、数字")
    private String newPassword;

    @Schema(description = "确认新密码，须与 newPassword 一致", example = "Xyz#654321", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认新密码不能为空")
    @Size(min = 8, max = 32, message = "确认密码长度需为8-32个字符")
    private String confirmPassword;
}
