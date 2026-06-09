package com.datafactory.common.model.dto.codetable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑码表请求参数
 *
 * 与新增请求参数相同，但 tableCode 不接受修改（忽略传入值）。
 */
@Data
@Schema(description = "编辑码表请求参数")
public class CodeTableUpdateRequest {

    @Schema(description = "码表名称", example = "企业类型更新", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "码表名称不能为空")
    @Size(max = 50, message = "码表名称最大 50 字符")
    private String tableName;

    @Schema(description = "说明", example = "企业类型枚举码表")
    @Size(max = 200, message = "说明最大 200 字符")
    private String description;
}
