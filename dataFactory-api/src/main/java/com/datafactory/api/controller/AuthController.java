package com.datafactory.api.controller;

import com.datafactory.common.model.dto.LoginRequest;
import com.datafactory.common.model.dto.PasswordUpdateRequest;
import com.datafactory.common.model.dto.ProfileUpdateRequest;
import com.datafactory.common.model.dto.RefreshTokenRequest;
import com.datafactory.common.model.dto.RegisterRequest;
import com.datafactory.common.model.vo.CurrentUserVo;
import com.datafactory.common.model.vo.LoginVo;
import com.datafactory.common.model.vo.RefreshTokenVo;
import com.datafactory.common.model.vo.UserInfoVo;
import com.datafactory.common.response.Result;
import com.datafactory.core.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * 提供用户注册、登录、登出、刷新令牌等认证相关的 REST 接口。
 */
@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /**
     * 用户登录
     * 用户通过用户名/邮箱/手机号 + 密码进行登录，返回访问令牌和刷新令牌。
     *
     * @param loginRequest 登录请求参数（账号、密码、是否记住登录）
     * @return 统一响应，包含令牌和用户基本信息
     */
    @Operation(summary = "用户登录", description = "用户通过用户名/邮箱/手机号 + 密码进行登录，返回访问令牌和刷新令牌")
    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginVo loginVo = authService.login(loginRequest);
        return Result.success("登录成功", loginVo);
    }

    /**
     * 用户注册
     * 新用户注册账号，注册成功后自动登录并返回令牌。
     *
     * @param registerRequest 注册请求参数（用户名、密码、确认密码、昵称等）
     * @return 统一响应，包含令牌和用户基本信息
     */
    @Operation(summary = "用户注册", description = "新用户注册账号，注册成功后自动登录并返回令牌")
    @PostMapping("/register")
    public Result<LoginVo> register(@Valid @RequestBody RegisterRequest registerRequest) {
        LoginVo loginVo = authService.register(registerRequest);
        return Result.success("注册成功", loginVo);
    }

    /**
     * 用户登出
     * 使当前令牌失效，退出登录。
     *
     * @param authorization 请求头 Authorization（Bearer 令牌）
     * @return 统一响应
     */
    @Operation(summary = "用户登出", description = "使当前令牌失效，退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        // 提取 Bearer 令牌
        String token = authorization.replace("Bearer ", "");
        authService.logout(token);
        return Result.success("登出成功", null);
    }

    /**
     * 获取当前用户信息
     * 根据当前令牌获取登录用户的详细信息，包含角色和权限列表。
     *
     * @param authorization 请求头 Authorization（Bearer 令牌）
     * @return 统一响应，包含用户详细信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据当前令牌获取登录用户的详细信息，包含角色和权限列表")
    @GetMapping("/user-info")
    public Result<CurrentUserVo> getUserInfo(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        CurrentUserVo currentUserVo = authService.getCurrentUserInfo(token);
        return Result.success(currentUserVo);
    }

    /**
     * 刷新访问令牌
     * 使用刷新令牌获取新的访问令牌，避免用户频繁登录。
     *
     * @param refreshTokenRequest 刷新令牌请求参数
     * @return 统一响应，包含新的令牌信息
     */
    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌，避免用户频繁登录")
    @PostMapping("/refresh-token")
    public Result<RefreshTokenVo> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshTokenVo refreshTokenVo = authService.refreshToken(refreshTokenRequest);
        return Result.success("令牌刷新成功", refreshTokenVo);
    }

    /**
     * 修改密码
     * 已登录用户修改自己的密码。
     *
     * @param authorization          请求头 Authorization（Bearer 令牌）
     * @param passwordUpdateRequest  修改密码请求参数
     * @return 统一响应
     */
    @Operation(summary = "修改密码", description = "已登录用户修改自己的密码，修改成功后需重新登录")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        String token = authorization.replace("Bearer ", "");
        authService.updatePassword(token, passwordUpdateRequest);
        return Result.success("密码修改成功，请重新登录", null);
    }

    /**
     * 修改个人信息
     * 已登录用户修改自己的基本信息（不含密码）。
     *
     * @param authorization         请求头 Authorization（Bearer 令牌）
     * @param profileUpdateRequest  个人信息请求参数
     * @return 统一响应，包含更新后的用户信息
     */
    @Operation(summary = "修改个人信息", description = "已登录用户修改自己的基本信息（昵称、邮箱、手机号、备注），所有参数均为可选")
    @PutMapping("/profile")
    public Result<UserInfoVo> updateProfile(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        String token = authorization.replace("Bearer ", "");
        UserInfoVo userInfo = authService.updateProfile(token, profileUpdateRequest);
        return Result.success("个人信息更新成功", userInfo);
    }
}
