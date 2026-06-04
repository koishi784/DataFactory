package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 码值实体类
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("code_item")
public class CodeItem extends BaseEntity {

    private Long tableId;
    private String code;
    private String name;
    private String value;
    private Integer sortOrder;
    private String parentCode;
    private Integer status;
    private String description;
}
