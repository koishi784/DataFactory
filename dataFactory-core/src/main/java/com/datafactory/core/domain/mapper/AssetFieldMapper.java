package com.datafactory.core.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datafactory.core.domain.entity.AssetField;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产字段定义 Mapper 接口
 */
@Mapper
public interface AssetFieldMapper extends BaseMapper<AssetField> {
}
