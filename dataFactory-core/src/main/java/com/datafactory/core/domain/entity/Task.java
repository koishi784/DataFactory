package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务实体类
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task")
public class Task extends BaseEntity {

    private String taskName;
    private String taskDescription;
    private String scheduleType;
    private String cronExpression;
    private String eventType;
    private LocalDateTime effectiveDate;
    private LocalDateTime expireDate;
    private Integer pauseOnFailure;
    private Integer taskTimeout;
    private Integer retryCount;
    private Integer retryInterval;
    private String alertEmail;
    private Integer status;
    private Integer executeStatus;
    private LocalDateTime lastExecuteTime;
    private LocalDateTime nextExecuteTime;
}
