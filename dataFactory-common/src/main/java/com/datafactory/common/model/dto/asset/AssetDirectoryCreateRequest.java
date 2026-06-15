package com.datafactory.common.model.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增资产目录请求参数
 */
@Data
@Schema(description = "新增资产目录请求参数")
public class AssetDirectoryCreateRequest {

    @Schema(description = "目录名称", example = "企业数据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目录名称不能为空")
    @Size(max = 50, message = "目录名称最大 50 字符")
    private String name;

    @Schema(description = "父级目录ID，顶级传 0", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父级目录ID不能为空")
    private Long parentId;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;
}
