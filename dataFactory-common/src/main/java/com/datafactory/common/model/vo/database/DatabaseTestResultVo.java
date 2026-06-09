package com.datafactory.common.model.vo.database;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据库连接测试结果 VO
 */
@Schema(description = "数据库连接测试结果")
@Data
public class DatabaseTestResultVo {

    @Schema(description = "连接是否成功")
    private Boolean success;

    @Schema(description = "响应耗时（毫秒）")
    private Long responseTime;

    @Schema(description = "错误信息（连接失败时返回）")
    private String errorMessage;
}
