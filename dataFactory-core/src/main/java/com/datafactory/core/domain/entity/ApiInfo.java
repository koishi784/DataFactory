package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 注册接口实体类
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_info")
public class ApiInfo extends BaseEntity {

    private String apiName;
    private String apiDescription;
    private Long categoryId;
    private String source;
    private String protocol;
    private String method;
    private String url;
    private Integer timeout;
    private Integer retryCount;
    private Integer status;
    private String version;
    private String responseExample;
    private String remark;
}
