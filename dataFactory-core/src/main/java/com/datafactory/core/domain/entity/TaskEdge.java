package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务边实体类（DAG连线）
 */

@Data
@TableName("task_edge")
public class TaskEdge implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private String edgeId;
    private String sourceNodeId;
    private String targetNodeId;
    @TableField("`condition`")
    private String condition;
}
