package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务节点执行结果实体类
 */

@Data
@TableName("task_node_execution")
public class TaskNodeExecution implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long executionId;
    private Long taskId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String inputData;
    private String outputData;
    private String errorMessage;
    private String logs;
}
