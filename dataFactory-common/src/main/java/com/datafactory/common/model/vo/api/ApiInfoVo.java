package com.datafactory.common.model.vo.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口列表项 VO
 */
@Schema(description = "接口列表项")
@Data
public class ApiInfoVo {

    @Schema(description = "接口ID", example = "1")
    private Long id;

    @Schema(description = "接口名称", example = "获取订单数据接口")
    private String apiName;

    @Schema(description = "接口说明", example = "根据订单ID获取订单详细信息")
    private String apiDescription;

    @Schema(description = "接口分类（多层级拼接字符串）", example = "电商数据 / 订单接口")
    private String apiCategory;

    @Schema(description = "所属分类ID", example = "2")
    private Long categoryId;

    @Schema(description = "接口来源", example = "订单系统")
    private String source;

    @Schema(description = "协议类型：HTTP / HTTPS", example = "HTTPS")
    private String protocol;

    @Schema(description = "请求方法：GET / POST / PUT / DELETE", example = "GET")
    private String method;

    @Schema(description = "接口URL路径", example = "https://order-system.example.com/api/v1/orders/{orderId}")
    private String url;

    @Schema(description = "状态：0=未发布 / 1=已发布 / 2=已停用", example = "0")
    private Integer status;

    @Schema(description = "超时时间（毫秒）", example = "30000")
    private Integer timeout;

    @Schema(description = "重试次数", example = "1")
    private Integer retryCount;

    @Schema(description = "当前版本号", example = "1.0.0")
    private String version;

    @Schema(description = "创建时间", example = "2026-06-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-06-02 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "创建人", example = "admin")
    private String createBy;

    @Schema(description = "更新人", example = "admin")
    private String updateBy;
}
