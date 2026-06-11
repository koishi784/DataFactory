package com.datafactory.common.model.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑接口分类请求参数
 */
@Schema(description = "编辑接口分类请求参数")
@Data
public class ApiCategoryUpdateRequest {

    @Schema(description = "分类名称", example = "电商数据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最大 50 字符")
    private String name;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;
}
