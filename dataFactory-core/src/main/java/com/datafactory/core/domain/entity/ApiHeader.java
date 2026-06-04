package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 接口请求头配置实体类
 */

@Data
@TableName("api_header")
public class ApiHeader implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long apiId;
    private String headerKey;
    private String headerValue;
    private Integer required;
    private String description;
    private Integer sortOrder;
}
