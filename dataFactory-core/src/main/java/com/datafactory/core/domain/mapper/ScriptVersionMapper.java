package com.datafactory.core.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datafactory.core.domain.entity.ScriptVersion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScriptVersionMapper extends BaseMapper<ScriptVersion> {
}
