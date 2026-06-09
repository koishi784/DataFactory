package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_category")
public class TaskCategory extends BaseEntity {

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
