package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 资产字段定义实体类
 */
@Data
@TableName("asset_field")
public class AssetField implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属资产ID
     */
    private Long assetId;

    /**
     * 字段英文名称（仅支持英文大小写、数字及下划线，英文开头）
     */
    private String englishFieldName;

    /**
     * 字段中文名称（仅支持中文及英文大小写）
     */
    private String chineseFieldName;

    /**
     * 字段说明
     */
    private String description;

    /**
     * 关联数据标准ID
     */
    private Long standardId;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
