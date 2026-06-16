package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.datafactory.common.enums.ParamDirection;
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

    /**
     * 脚本ID
     */
    private Long scriptId;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 数据类型
     */
    private String paramType;

    /**
     * 参数方向
     */
    private ParamDirection paramDirection;

    /**
     * 参数描述
     */
    private String description;
}
