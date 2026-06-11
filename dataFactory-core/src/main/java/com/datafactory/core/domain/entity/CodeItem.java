package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 码值实体类
 *
 * 存储码表中每一个具体的枚举取值。
 * 注意：此表有 create_time、update_time、deleted，但没有 create_by、update_by，因此不继承 BaseEntity。
 */
@Data
@TableName("code_item")
public class CodeItem {

    /**
     * 码值ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属码表ID
     */
    @TableField("table_id")
    private Long tableId;

    /**
     * 编码取值（同一码表内唯一）
     */
    @TableField("code")
    private String code;

    /**
     * 编码中文名称（同一码表内唯一，仅支持中文及大小写英文）
     */
    @TableField("name")
    private String name;

    /**
     * 编码值
     */
    @TableField("value")
    private String value;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 父级编码（层级码表）
     */
    @TableField("parent_code")
    private String parentCode;

    /**
     * 码值状态：1=启用 / 0=停用
     */
    @TableField("status")
    private Integer status;

    /**
     * 说明
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
