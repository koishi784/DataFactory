package com.datafactory.core.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datafactory.core.domain.entity.AssetDirectory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产目录 Mapper 接口
 */
@Mapper
public interface AssetDirectoryMapper extends BaseMapper<AssetDirectory> {
}
