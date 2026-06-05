package com.datafactory.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户基本信息 VO
 */
@Schema(description = "用户基本信息")
@Data
public class UserInfoVo {

    /** 用户ID */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /** 用户名 */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /** 邮箱 */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /** 手机号 */
    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    /** 用户状态（1=启用 / 0=停用） */
    @Schema(description = "用户状态（1=启用 / 0=停用）", example = "1")
    private Integer status;

    /** 最后登录时间 */
    @Schema(description = "最后登录时间", example = "2026-06-02 10:00:00")
    private LocalDateTime lastLoginTime;

    /** 注册时间 */
    @Schema(description = "注册时间", example = "2026-05-01 09:00:00")
    private LocalDateTime createTime;
}
