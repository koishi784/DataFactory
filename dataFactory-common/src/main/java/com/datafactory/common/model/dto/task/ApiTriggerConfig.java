package com.datafactory.common.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * API 触发配置
 *
 * scheduleType=API 时的配置，定义生成的 API 接口信息
 */
@Schema(description = "API 触发配置")
@Data
public class ApiTriggerConfig {

    @Schema(description = "API 接口名称", example = "工商信息查询任务API", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "API 名称不能为空")
    @Size(max = 100, message = "API 名称最大 100 字符")
    private String apiName;

    @Schema(description = "API 访问路径", example = "/api/task/commercial-info", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "API Path 不能为空")
    @Size(max = 500, message = "API Path 最大 500 字符")
    private String apiPath;

    @Schema(description = "API 接口描述", example = "通过API触发工商信息查询任务")
    @Size(max = 1000, message = "API 描述最大 1000 字符")
    private String apiDescription;
}
