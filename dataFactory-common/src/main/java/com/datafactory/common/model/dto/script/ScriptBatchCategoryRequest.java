package com.datafactory.common.model.dto.script;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量修改脚本分类请求参数
 */
@Schema(description = "批量修改脚本分类请求参数")
@Data
public class ScriptBatchCategoryRequest {

    @Schema(description = "脚本ID列表", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "脚本ID列表不能为空")
    @Size(max = 100, message = "最多支持 100 个 ID")
    private List<Long> ids;

    @Schema(description = "目标分类ID", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标分类ID不能为空")
    private Long categoryId;
}
