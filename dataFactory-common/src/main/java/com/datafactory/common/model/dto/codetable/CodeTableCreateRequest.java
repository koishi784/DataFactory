package com.datafactory.common.model.dto.codetable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增码表请求参数
 */
@Data
@Schema(description = "新增码表请求参数")
public class CodeTableCreateRequest {

    @Schema(description = "码表名称", example = "企业类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "码表名称不能为空")
    @Size(max = 50, message = "码表名称最大 50 字符")
    private String tableName;

    @Schema(description = "说明", example = "企业类型枚举码表")
    @Size(max = 200, message = "说明最大 200 字符")
    private String description;

    @Schema(description = "初始码值列表（可选）")
    private List<CodeItemCreateRequest> items;
}
