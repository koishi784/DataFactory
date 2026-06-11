package com.datafactory.common.model.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量操作请求参数（通用）
 *
 * 用于批量发布、批量停用等操作
 */
@Schema(description = "批量操作请求参数")
@Data
public class BatchIdsRequest {

    @Schema(description = "ID 列表，至少一个，最多 100 个", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "ID 列表不能为空")
    @Size(max = 100, message = "最多支持 100 个 ID")
    private List<Long> ids;
}
