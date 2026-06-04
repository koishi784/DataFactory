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

    private Long assetId;
    private String fieldName;
    private String fieldType;
    private Integer fieldLength;
    private Integer fieldPrecision;
    private Integer isPrimaryKey;
    private Integer isNullable;
    private String defaultValue;
    private String description;
    private Long standardId;
    private Integer sortOrder;
}
