package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 脚本参数定义实体类
 */

@Data
@TableName("script_param")
public class ScriptParam implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scriptId;
    private String paramName;
    private String paramType;
    private Integer required;
    private String defaultValue;
    private String description;
}
