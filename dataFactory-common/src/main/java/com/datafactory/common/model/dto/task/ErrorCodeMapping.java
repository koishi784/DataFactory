package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 错误码映射项
 */
@Schema(description = "错误码映射项")
@Data
public class ErrorCodeMapping {

    @Schema(description = "编码取值", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "编码取值不能为空")
    private String code;

    @Schema(description = "编码名称", example = "调用成功", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "编码名称不能为空")
    private String name;

    @Schema(description = "编码含义", example = "接口调用成功")
    private String meaning;
}
