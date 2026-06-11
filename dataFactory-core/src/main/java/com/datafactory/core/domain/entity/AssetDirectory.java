package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资产目录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_directory")
public class AssetDirectory extends BaseEntity {

    /**
     * 目录名称
     */
    private String name;

    /**
     * 父级目录ID
     */
    private Long parentId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
