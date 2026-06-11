package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 脚本分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("script_category")
public class ScriptCategory extends BaseEntity {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父级分类ID
     */
    private Long parentId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
