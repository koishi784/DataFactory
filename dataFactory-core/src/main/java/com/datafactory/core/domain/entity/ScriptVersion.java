package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 脚本版本历史实体类
 */

@Data
@TableName("script_version")
public class ScriptVersion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scriptId;
    private String version;
    private String scriptContent;
    private String changeLog;
    private LocalDateTime createTime;
    private String createBy;
}
