package com.datafactory.common.model.dto.database;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增数据库连接请求参数
 *
 * 连接密码使用明文传入，后端存储时使用 AES 加密。
 */
@Schema(description = "新增数据库连接请求参数")
@Data
public class DatabaseCreateRequest {

    @Schema(description = "连接名称，仅支持中英文、数字、下划线，最大 50 字符，全局唯一", example = "生产订单库", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "连接名称不能为空")
    @Size(max = 50, message = "连接名称最大 50 字符")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$", message = "连接名称仅支持中英文、数字、下划线")
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

    @Schema(description = "连接密码（明文）", example = "encrypted_password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "连接密码不能为空")
    private String password;

    @Schema(description = "JDBC 额外连接参数", example = "useSSL=true&serverTimezone=Asia/Shanghai")
    private String jdbcParams;

    @Schema(description = "描述说明", example = "订单系统生产数据库只读连接")
    @Size(max = 200, message = "描述说明最大 200 字符")
    private String description;
}
