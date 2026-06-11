package com.datafactory.core.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datafactory.core.domain.entity.TaskCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务分类 Mapper 接口
 */
@Mapper
public interface TaskCategoryMapper extends BaseMapper<TaskCategory> {
}
