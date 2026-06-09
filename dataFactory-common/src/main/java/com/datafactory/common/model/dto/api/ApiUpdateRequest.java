package com.datafactory.common.model.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 编辑注册接口请求参数
 *
 * 同新增接口请求参数结构，用于修改 DRAFT 状态的接口信息
 */
@Schema(description = "编辑注册接口请求参数")
@Data
public class ApiUpdateRequest {

    @Schema(description = "接口名称（最大 100 字符，全局唯一，不允许空格）", example = "获取订单数据接口", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "接口名称不能为空")
    @Size(max = 100, message = "接口名称最大 100 字符")
    private String apiName;

    @Schema(description = "接口说明（最大 1000 字符）", example = "根据订单ID获取订单详细信息")
    @Size(max = 1000, message = "接口说明最大 1000 字符")
    private String apiDescription;

    @Schema(description = "所属分类ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属分类ID不能为空")
    private Long categoryId;

    @Schema(description = "接口来源", example = "订单系统", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "接口来源不能为空")
    private String source;

    @Schema(description = "协议类型：HTTP / HTTPS", example = "HTTPS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "协议类型不能为空")
    private String protocol;

    @Schema(description = "请求方法：GET / POST", example = "GET", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请求方法不能为空")
    @Pattern(regexp = "^(GET|POST)$", message = "请求方法仅支持 GET 和 POST")
    private String method;

    @Schema(description = "接口URL路径", example = "https://order-system.example.com/api/v1/orders/{orderId}", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "接口URL不能为空")
    @Size(max = 500, message = "接口URL最大 500 字符")
    private String url;

    @Schema(description = "超时时间（毫秒，范围 1~1800000，默认 30000 = 30 秒）", example = "30000")
    @Min(value = 1, message = "超时时间最小为 1 毫秒")
    @Max(value = 1800000, message = "超时时间最大为 1800000 毫秒")
    private Integer timeout;

    @Schema(description = "重试次数，默认 0，最大 3", example = "1")
    @Max(value = 3, message = "重试次数最大为 3")
    private Integer retryCount;

    @Schema(description = "请求头配置列表")
    @Valid
    private List<ApiCreateRequest.HeaderConfigRequest> headers;

    @Schema(description = "请求参数配置列表")
    @Valid
    private List<ApiCreateRequest.ParamConfigRequest> requestParams;

    @Schema(description = "响应示例（JSON 字符串）")
    @Size(max = 5000, message = "响应示例最大 5000 字符")
    private String responseExample;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注最大 500 字符")
    private String remark;
}
