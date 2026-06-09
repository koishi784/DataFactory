package com.datafactory.common.model.dto.database;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑数据库连接请求参数
 *
 * 密码字段可选，不传则不修改已有密码。
 */
@Schema(description = "编辑数据库连接请求参数")
@Data
public class DatabaseUpdateRequest {

    @Schema(description = "连接名称", example = "生产订单库", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "连接名称不能为空")
    @Size(max = 50, message = "连接名称最大 50 字符")
    private String connectionName;

    @Schema(description = "数据库类型：MYSQL/POSTGRESQL/ORACLE/SQLSERVER/HIVE/CLICKHOUSE", example = "MYSQL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "数据库类型不能为空")
    private String dbType;

    @Schema(description = "主机地址", example = "192.168.1.100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "主机地址不能为空")
    private String host;

    @Schema(description = "端口号", example = "3306", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "端口号不能为空")
    @Min(value = 1, message = "端口号范围：1-65535")
    @Max(value = 65535, message = "端口号范围：1-65535")
    private Integer port;

    @Schema(description = "数据库名称", example = "order_db", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "数据库名称不能为空")
    private String databaseName;

    @Schema(description = "连接用户名", example = "data_readonly", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "连接用户名不能为空")
    private String username;

    @Schema(description = "连接密码（明文，不传则不修改）", example = "new_password")
    private String password;

    @Schema(description = "JDBC 额外连接参数", example = "useSSL=true&serverTimezone=Asia/Shanghai")
    private String jdbcParams;

    @Schema(description = "描述说明", example = "订单系统生产数据库只读连接")
    @Size(max = 200, message = "描述说明最大 200 字符")
    private String description;
}
