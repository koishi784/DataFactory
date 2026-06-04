package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据标准实体类（支持目录树和标准项）
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_standard")
public class DataStandard extends BaseEntity {

    private String name;
    private Long parentId;
    private Integer level;
    private String type;
    private String standardCode;
    private String dataType;
    private Integer length;
    private Integer precision;
    private String defaultValue;
    private String valueRange;
    private String description;
    private Integer status;
}
