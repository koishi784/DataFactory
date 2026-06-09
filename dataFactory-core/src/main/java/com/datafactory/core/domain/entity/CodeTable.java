package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 码表实体类
 *
 * 存储码表基本信息，码表编号由系统自动生成（格式 MZB + 5 位数字），一旦生成不可更改。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("code_table")
public class CodeTable extends BaseEntity {

    /**
     * 码表名称（最大 50 字符，全局唯一，仅支持中文及大小写英文）
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 码表编号（系统自动生成，格式 MZB + 5 位数字，全局唯一）
     */
    @TableField("table_code")
    private String tableCode;

    /**
     * 码表说明（最大 200 字符）
     */
    @TableField("description")
    private String description;

    /**
     * 状态：0=未发布 / 1=已发布 / 2=已停用
     */
    @TableField("status")
    private Integer status;
}
