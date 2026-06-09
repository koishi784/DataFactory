package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 资产目录关联实体类
 */
@Data
@TableName("asset_directory_rel")
public class AssetDirectoryRel implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 目录ID
     */
    private Long directoryId;
}
