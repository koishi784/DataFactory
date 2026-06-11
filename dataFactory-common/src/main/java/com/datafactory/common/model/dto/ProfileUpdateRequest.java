package com.datafactory.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人信息请求参数
 */
@Schema(description = "修改个人信息请求参数")
@Data
public class ProfileUpdateRequest {

    @Schema(description = "昵称，2-20 字符", example = "张三")
    @Size(min = 2, max = 20, message = "昵称长度需为2-20个字符")
    private String nickname;

    @Schema(description = "电子邮箱", example = "zhangsan@example.com")
    @Size(max = 64, message = "邮箱长度不能超过64个字符")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号码", example = "13900139000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @Schema(description = "备注信息", example = "数据工程师")
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;
}
