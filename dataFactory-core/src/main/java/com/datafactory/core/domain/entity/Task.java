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

    /**
     * 任务名称（全局唯一，仅支持中文和英文大小写）
     */
    private String taskName;

    /**
     * 任务说明
     */
    private String taskDescription;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 调度类型：API（生成API触发）/ CRON（定时任务触发）
     */
    private String scheduleType;

    /**
     * Cron 表达式（scheduleType=CRON 时有效）
     */
    private String cronExpression;

    /**
     * 生效日期
     */
    private LocalDateTime effectiveDate;

    /**
     * 失效日期
     */
    private LocalDateTime expireDate;

    /**
     * 失败后暂停调度：0=否，1=是
     */
    private Integer pauseOnFailure;

    /**
     * 任务超时时间（分钟）
     */
    private Integer taskTimeout;

    /**
     * 失败重试次数
     */
    private Integer retryCount;

    /**
     * 重试间隔（分钟）
     */
    private Integer retryInterval;

    /**
     * 告警邮箱
     */
    private String alertEmail;

    /**
     * 发布状态：0=未发布，1=已发布，2=已停用
     */
    private Integer status;

    /**
     * 最近执行状态：0=等待，1=执行中，2=成功，3=失败，4=已取消
     */
    private Integer executeStatus;

    /**
     * 最近执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;
}
