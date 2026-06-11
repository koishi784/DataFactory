package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务节点实体类（DAG节点）
 */

@Data
@TableName(value = "task_node", autoResultMap = true)
public class TaskNode implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private Double positionX;
    private Double positionY;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object nodeConfig;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
