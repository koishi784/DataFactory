package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据标准实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_standard")
public class DataStandard extends BaseEntity {

    /**
     * 中文名称
     */
    private String name;

    /**
     * 英文名称
     */
    private String englishName;

    /**
     * 标准编号（系统自动生成，格式 BZ + 5 位数字，全局唯一）
     */
    private String standardCode;

    /**
     * 数据类型：String / Int / Float / Enum
     */
    private String dataType;

    /**
     * 数据长度（仅 String 类型可填）
     */
    private Integer length;

    /**
     * 精度（小数位，仅 Float 类型可填）
     */
    private Integer precision;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 取值范围最小值（Int / Float 类型可填）
     */
    private String rangeMin;

    /**
     * 取值范围最大值（Int / Float 类型可填）
     */
    private String rangeMax;

    /**
     * 枚举范围（引用码表编码，仅 Enum 类型可填）
     */
    private String enumRange;

    /**
     * 来源机构
     */
    private String sourceOrganization;

    /**
     * 是否可为空（0=可为空，1=不可为空）
     */
    private Integer nullable;

    /**
     * 标准说明
     */
    private String description;

    /**
     * 状态：0=未发布，1=已发布，2=已停用
     */
    private Integer status;
}
