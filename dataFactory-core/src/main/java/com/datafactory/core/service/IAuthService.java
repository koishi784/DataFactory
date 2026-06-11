package com.datafactory.core.service;

import com.datafactory.common.model.dto.LoginRequest;
import com.datafactory.common.model.dto.PasswordUpdateRequest;
import com.datafactory.common.model.dto.ProfileUpdateRequest;
import com.datafactory.common.model.dto.RefreshTokenRequest;
import com.datafactory.common.model.dto.RegisterRequest;
import com.datafactory.common.model.vo.CurrentUserVo;
import com.datafactory.common.model.vo.LoginVo;
import com.datafactory.common.model.vo.RefreshTokenVo;
import com.datafactory.common.model.vo.UserInfoVo;

/**
 * 认证服务接口
 * 定义用户注册、登录、登出、获取当前用户信息等认证相关的业务方法。
 */
public interface IAuthService {

    /**
     * 用户登录
     * 根据账号（用户名/邮箱/手机号）和密码进行登录校验，成功后返回令牌和用户信息。
     *
     * @param loginRequest 登录请求参数
     * @return 登录响应，包含访问令牌、刷新令牌和用户基本信息
     */
    LoginVo login(LoginRequest loginRequest);

    /**
     * 用户注册
     * 创建新用户账号，注册成功后自动登录并返回令牌。
     *
     * @param registerRequest 注册请求参数
     * @return 登录响应，包含访问令牌、刷新令牌和用户基本信息
     */
    LoginVo register(RegisterRequest registerRequest);

    /**
     * 用户登出
     * 使当前令牌失效，退出登录。
     *
     * @param token 当前访问令牌
     */
    void logout(String token);

    /**
     * 获取当前用户信息
     * 根据令牌获取当前登录用户的详细信息，包含角色和权限列表。
     *
     * @param token 当前访问令牌
     * @return 当前用户信息（含角色和权限）
     */
    CurrentUserVo getCurrentUserInfo(String token);

    /**
     * 刷新访问令牌
     * 使用刷新令牌获取新的访问令牌，旧刷新令牌立即失效。
     *
     * @param refreshTokenRequest 刷新令牌请求参数
     * @return 新的令牌信息（访问令牌、刷新令牌、令牌类型、有效期）
     */
    RefreshTokenVo refreshToken(RefreshTokenRequest refreshTokenRequest);

    /**
     * 修改密码
     * 已登录用户修改自己的密码，修改成功后需重新登录。
     *
     * @param token                  当前访问令牌
     * @param passwordUpdateRequest  修改密码请求参数
     */
    void updatePassword(String token, PasswordUpdateRequest passwordUpdateRequest);

    /**
     * 修改个人信息
     * 已登录用户修改自己的基本信息（昵称、邮箱、手机号、备注）。
     *
     * @param token                当前访问令牌
     * @param profileUpdateRequest 个人信息请求参数
     * @return 更新后的用户信息
     */
    UserInfoVo updateProfile(String token, ProfileUpdateRequest profileUpdateRequest);
}
