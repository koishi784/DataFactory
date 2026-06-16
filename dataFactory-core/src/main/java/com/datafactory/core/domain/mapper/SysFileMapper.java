package com.datafactory.core.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datafactory.core.domain.entity.SysFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统文件记录 Mapper 接口
 */
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {
}
