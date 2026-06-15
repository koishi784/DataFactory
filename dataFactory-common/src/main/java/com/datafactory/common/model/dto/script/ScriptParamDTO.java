package com.datafactory.common.model.dto.script;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 脚本参数定义 DTO
 */
@Data
@Schema(description = "脚本参数定义")
public class ScriptParamDTO {

    @Schema(description = "参数名称", example = "企业唯一id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数名称不能为空")
    @Size(max = 100, message = "参数名称最大 100 字符")
    private String paramName;

    @Schema(description = "参数数据类型", example = "Int", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数类型不能为空")
    @Size(max = 16, message = "参数类型最大 16 字符")
    private String paramType;

    @Schema(description = "参数描述", example = "企业唯一标识")
    @Size(max = 200, message = "参数描述最大 200 字符")
    private String description;
}
