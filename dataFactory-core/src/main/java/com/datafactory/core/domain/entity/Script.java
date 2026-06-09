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

    /**
     * 脚本名称（全局唯一，仅支持中文和英文大小写）
     */
    private String scriptName;

    /**
     * 脚本类型：PYTHON
     */
    private String scriptType;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 上传文件ID
     */
    private Long fileId;

    /**
     * 脚本文件名（如 xxx.py）
     */
    private String fileName;

    /**
     * 说明
     */
    private String description;

    /**
     * 状态：0=未发布，1=已发布，2=已停用
     */
    private Integer status;
}
