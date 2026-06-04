package com.datafactory.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 刷新令牌实体类
 */

@Data
@TableName("sys_refresh_token")
public class SysRefreshToken implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createTime;
}
