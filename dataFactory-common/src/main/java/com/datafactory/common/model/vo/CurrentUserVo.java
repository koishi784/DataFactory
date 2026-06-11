package com.datafactory.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前用户信息 VO
 * 用于获取当前登录用户的详细信息，包含角色和权限列表。
 */
@Schema(description = "当前用户信息")
@Data
public class CurrentUserVo {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "用户状态（1=启用 / 0=停用）", example = "1")
    private Integer status;

    @Schema(description = "角色列表", example = "[\"DATA_ENGINEER\"]")
    private List<String> roles;

    @Schema(description = "权限标识列表", example = "[\"api:read\", \"api:write\"]")
    private List<String> permissions;

    @Schema(description = "最后登录时间", example = "2026-06-02 10:00:00")
    private LocalDateTime lastLoginTime;

    @Schema(description = "注册时间", example = "2026-05-01 09:00:00")
    private LocalDateTime createTime;
}
