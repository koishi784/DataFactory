package com.datafactory.core.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datafactory.core.domain.entity.DataStandard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据标准 Mapper 接口
 */
@Mapper
public interface DataStandardMapper extends BaseMapper<DataStandard> {
}
