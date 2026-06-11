package com.datafactory.core.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datafactory.core.domain.entity.ScriptCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 脚本分类 Mapper 接口
 */
@Mapper
public interface ScriptCategoryMapper extends BaseMapper<ScriptCategory> {
}
