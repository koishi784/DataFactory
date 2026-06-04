package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 资产标签实体类
 */

@Data
@TableName("asset_tag")
public class AssetTag implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long assetId;
    private String tag;
}
