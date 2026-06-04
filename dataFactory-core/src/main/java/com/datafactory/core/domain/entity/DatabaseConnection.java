package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据库连接实体类
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("database_connection")
public class DatabaseConnection extends BaseEntity {

    private String connectionName;
    private String dbType;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String password;
    private String jdbcParams;
    private String description;
    private Integer status;
    private LocalDateTime lastTestTime;
    private Integer lastTestResult;
}
