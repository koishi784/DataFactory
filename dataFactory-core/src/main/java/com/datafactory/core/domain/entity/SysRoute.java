package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统路由实体类
 */

@Data
@TableName("sys_route")
public class SysRoute implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String routeName;
    private String routePath;
    private String method;
    private String module;
    private String permission;
    private Integer isAuth;
    private String description;
    private LocalDateTime createTime;
}
