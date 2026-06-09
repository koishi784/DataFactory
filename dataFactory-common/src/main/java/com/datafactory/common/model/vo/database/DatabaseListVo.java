package com.datafactory.common.model.vo.database;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库连接列表 VO
 *
 * 注：密码字段不在列表中明文返回。
 */
@Schema(description = "数据库连接列表视图")
@Data
public class DatabaseListVo {

    @Schema(description = "连接ID")
    private Long id;

    @Schema(description = "连接名称")
    private String connectionName;

    @Schema(description = "数据库类型")
    private String dbType;

    @Schema(description = "主机地址")
    private String host;

    @Schema(description = "端口号")
    private Integer port;

    @Schema(description = "数据库名称")
    private String databaseName;

    @Schema(description = "连接用户名")
    private String username;

    @Schema(description = "状态：0=未发布, 1=已发布, 2=已停用")
    private Integer status;

    @Schema(description = "描述说明")
    private String description;

    @Schema(description = "最近测试连接时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTestTime;

    @Schema(description = "最近测试结果：1=成功, 0=失败")
    private Integer lastTestResult;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
