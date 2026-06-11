package com.datafactory.common.model.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增接口分类请求参数
 */
@Schema(description = "新增接口分类请求参数")
@Data
public class ApiCategoryCreateRequest {

    @Schema(description = "分类名称", example = "电商数据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最大 50 字符")
    private String name;

    @Schema(description = "父级分类ID，顶级传 0", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父级分类ID不能为空")
    private Long parentId;

    @Schema(description = "排序号", example = "1", defaultValue = "0")
    private Integer sortOrder;
}
