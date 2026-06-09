package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据标准实体类
 *
 * 存储数据标准定义信息，包含中文名称、英文名称、数据类型、取值范围等。
 * 标准编号由系统自动生成（格式 BZ + 5 位数字），一旦生成不可更改。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_standard")
public class DataStandard extends BaseEntity {

    /**
     * 中文名称（最大 50 字符，仅支持中文及英文大小写）
     */
    @TableField("name")
    private String name;

    /**
     * 英文名称（最大 100 字符，仅支持英文大小写、数字及下划线，英文字母开头）
     */
    @TableField("english_name")
    private String englishName;

    /**
     * 标准编号（系统自动生成，格式 BZ + 5 位数字，全局唯一）
     */
    @TableField("standard_code")
    private String standardCode;

    /**
     * 数据类型：String / Int / Float / Enum
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 数据长度（仅 String 类型可填，须为正整数）
     */
    @TableField("length")
    private Integer length;

    /**
     * 精度（仅 Float 类型可填，须为非负整数）
     */
    @TableField("`precision`")
    private Integer precision;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 取值范围最小值（仅 Int / Float 类型可填）
     */
    @TableField("range_min")
    private String rangeMin;

    /**
     * 取值范围最大值（仅 Int / Float 类型可填）
     */
    @TableField("range_max")
    private String rangeMax;

    /**
     * 枚举范围（引用码表编码，仅 Enum 类型可填）
     */
    @TableField("enum_range")
    private String enumRange;

    /**
     * 来源机构
     */
    @TableField("source_organization")
    private String sourceOrganization;

    /**
     * 是否可为空：0=可为空 / 1=不可为空
     */
    @TableField("nullable")
    private Integer nullable;

    /**
     * 标准说明（最大 500 字符）
     */
    @TableField("description")
    private String description;

    /**
     * 状态：0=未发布 / 1=已发布 / 2=已停用
     */
    @TableField("status")
    private Integer status;
}
