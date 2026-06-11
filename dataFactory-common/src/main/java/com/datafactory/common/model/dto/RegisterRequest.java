package com.datafactory.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求参数
 */
@Schema(description = "注册请求参数")
@Data
public class RegisterRequest {

    /** 用户名，仅支持字母、数字、下划线，以字母开头 */
    @Schema(description = "用户名，仅支持字母、数字、下划线，以字母开头", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度需为4-20个字符")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{3,19}$", message = "用户名仅支持字母、数字、下划线，以字母开头")
    private String username;

    /** 密码，须包含大写字母、小写字母、数字 */
    @Schema(description = "密码，须包含大写字母、小写字母、数字", example = "Abc@123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需为8-32个字符")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,32}$",
            message = "密码须包含大写字母、小写字母、数字")
    private String password;

    /** 确认密码，须与 password 一致 */
    @Schema(description = "确认密码，须与 password 一致", example = "Abc@123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    @Size(min = 8, max = 32, message = "确认密码长度需为8-32个字符")
    private String confirmPassword;

    /** 昵称，支持中文、字母、数字 */
    @Schema(description = "昵称，支持中文、字母、数字", example = "张三")
    @Size(min = 2, max = 20, message = "昵称长度需为2-20个字符")
    private String nickname;

    /** 电子邮箱 */
    @Schema(description = "电子邮箱", example = "zhangsan@example.com")
    @Size(max = 64, message = "邮箱长度不能超过64个字符")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    /** 手机号码 */
    @Schema(description = "手机号码", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /** 备注信息 */
    @Schema(description = "备注信息", example = "数据开发工程师")
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;
}
