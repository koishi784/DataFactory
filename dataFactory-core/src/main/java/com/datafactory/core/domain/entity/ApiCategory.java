package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口分类实体类
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_category")
public class ApiCategory extends BaseEntity {

    private String name;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
}
