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

    /**
     * 连接名称（全局唯一，仅支持中英文、数字、下划线，不支持特殊符号及空格）
     */
    private String connectionName;

    /**
     * 数据库类型
     */
    private String dbType;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 连接用户名
     */
    private String username;

    /**
     * 连接密码（加密存储）
     */
    private String password;

    /**
     * JDBC 额外连接参数
     */
    private String jdbcParams;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 状态：0=未发布，1=已发布，2=已停用
     */
    private Integer status;

    /**
     * 最近测试连接时间
     */
    private LocalDateTime lastTestTime;

    /**
     * 最近测试结果：1=成功，0=失败
     */
    private Integer lastTestResult;
}
