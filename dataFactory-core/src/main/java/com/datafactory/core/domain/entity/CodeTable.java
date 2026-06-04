package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 码表实体类
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("code_table")
public class CodeTable extends BaseEntity {

    private String tableName;
    private String tableCode;
    private String description;
    private Integer status;
}
