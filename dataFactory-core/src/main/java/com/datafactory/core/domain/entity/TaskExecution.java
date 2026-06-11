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
 * 任务执行历史实体类
 */

@Data
@TableName(value = "task_execution", autoResultMap = true)
public class TaskExecution implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Integer status;
    private String triggerType;
    private String triggerBy;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object taskParams;

    private Integer debugMode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long totalDuration;
    private LocalDateTime createTime;
}
