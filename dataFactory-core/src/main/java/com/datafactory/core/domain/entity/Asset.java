package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据资产实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset")
public class Asset extends BaseEntity {

    /**
     * 中文名称（全局唯一，仅支持中英文）
     */
    private String assetName;

    /**
     * 英文名称（全局唯一，仅支持英文大小写、数字及下划线，英文开头）
     */
    private String englishName;

    /**
     * 数据资产表描述
     */
    private String description;

    /**
     * 状态：0=未发布，1=已发布，2=已停用
     */
    private Integer status;
}
