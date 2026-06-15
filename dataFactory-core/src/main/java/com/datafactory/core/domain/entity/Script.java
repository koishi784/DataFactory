package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 脚本实体类
 *
 * 支持两种脚本来源：
 * 1. 文件上传：通过 fileId + fileName 关联上传的文件（适用于 PYTHON）
 * 2. 在线编辑：直接通过 scriptContent 存储脚本源代码（适用于 GROOVY）
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
     * 脚本类型：GROOVY / PYTHON
     */
    private String scriptType;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 上传文件ID（文件上传模式时使用）
     */
    private Long fileId;

    /**
     * 脚本文件名（如 xxx.py，文件上传模式时使用）
     */
    private String fileName;

    /**
     * 脚本源代码（在线编辑模式时使用）
     */
    private String scriptContent;

    /**
     * 说明
     */
    private String description;

    /**
     * 状态：0=未发布，1=已发布，2=已停用
     */
    private Integer status;
}
