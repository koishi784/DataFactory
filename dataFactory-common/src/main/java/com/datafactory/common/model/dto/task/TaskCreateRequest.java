package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增任务请求参数
 *
 * 创建任务基本信息（三步向导第一步），创建后状态为 DRAFT
 */
@Schema(description = "新增任务请求参数")
@Data
public class TaskCreateRequest {

    @Schema(description = "任务名称，全局唯一，仅支持中文和英文大小写", example = "工商信息清洗任务", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 50, message = "任务名称最大 50 字符")
    private String taskName;

    @Schema(description = "所属分类ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属分类不能为空")
    private Long categoryId;

    @Schema(description = "任务说明", example = "清洗工商企业基本信息数据")
    @Size(max = 500, message = "任务说明最大 500 字符")
    private String taskDescription;
}
