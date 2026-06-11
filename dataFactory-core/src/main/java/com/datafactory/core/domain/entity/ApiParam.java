package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 接口请求参数配置实体类
 */

@Data
@TableName("api_param")
public class ApiParam implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long apiId;
    private String paramName;
    private String paramType;
    private String dataType;
    private Integer required;
    private String description;
    private String defaultValue;
    private String exampleValue;
    private Integer sortOrder;
    private String validationRule;
    private String minValue;
    private String maxValue;
}
