package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 脚本实体类
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("script")
public class Script extends BaseEntity {

    private String scriptName;
    private String scriptType;
    private String scriptContent;
    private String description;
    private String version;
    private Integer status;
}
