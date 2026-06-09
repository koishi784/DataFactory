package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 码表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("code_table")
public class CodeTable extends BaseEntity {

    /**
     * 码表名称（全局唯一）
     */
    private String tableName;

    /**
     * 码表编号（系统自动生成，格式 MZB + 5 位数字，全局唯一）
     */
    private String tableCode;

    /**
     * 码表说明
     */
    private String description;

    /**
     * 状态：0=未发布，1=已发布，2=已停用
     */
    private Integer status;
}
