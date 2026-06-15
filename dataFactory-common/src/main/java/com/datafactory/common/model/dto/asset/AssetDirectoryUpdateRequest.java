package com.datafactory.common.model.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑资产目录请求参数
 */
@Data
@Schema(description = "编辑资产目录请求参数")
public class AssetDirectoryUpdateRequest {

    @Schema(description = "目录名称", example = "企业数据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目录名称不能为空")
    @Size(max = 50, message = "目录名称最大 50 字符")
    private String name;

    @Schema(description = "排序号", example = "2")
    private Integer sortOrder;
}
