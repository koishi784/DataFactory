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

    private String assetName;
    private String assetCode;
    private Long parentId;
    private String type;
    private String sourceType;
    private Long sourceId;
    private String sourceTable;
    private String description;
    private Integer status;
}
