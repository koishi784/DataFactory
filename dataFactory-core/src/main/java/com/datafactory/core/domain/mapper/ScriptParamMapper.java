package com.datafactory.core.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datafactory.core.domain.entity.ScriptParam;
import org.apache.ibatis.annotations.Mapper;

/**
 * 脚本参数 Mapper 接口
 */
@Mapper
public interface ScriptParamMapper extends BaseMapper<ScriptParam> {
}
