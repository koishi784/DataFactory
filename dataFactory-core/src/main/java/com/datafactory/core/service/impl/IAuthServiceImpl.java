package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.exception.BusinessException;
import com.datafactory.common.model.dto.LoginRequest;
import com.datafactory.common.model.dto.PasswordUpdateRequest;
import com.datafactory.common.model.dto.ProfileUpdateRequest;
import com.datafactory.common.model.dto.RefreshTokenRequest;
import com.datafactory.common.model.dto.RegisterRequest;
import com.datafactory.common.model.vo.CurrentUserVo;
import com.datafactory.common.model.vo.LoginVo;
import com.datafactory.common.model.vo.RefreshTokenVo;
import com.datafactory.common.model.vo.UserInfoVo;
import com.datafactory.common.utils.JwtUtils;
import com.datafactory.core.domain.entity.SysPermission;
import com.datafactory.core.domain.entity.SysRefreshToken;
import com.datafactory.core.domain.entity.SysRole;
import com.datafactory.core.domain.entity.SysRolePermission;
import com.datafactory.core.domain.entity.SysUser;
import com.datafactory.core.domain.entity.SysUserRole;
import com.datafactory.core.domain.mapper.SysPermissionMapper;
import com.datafactory.core.domain.mapper.SysRefreshTokenMapper;
import com.datafactory.core.domain.mapper.SysRoleMapper;
import com.datafactory.core.domain.mapper.SysRolePermissionMapper;
import com.datafactory.core.domain.mapper.SysUserMapper;
import com.datafactory.core.domain.mapper.SysUserRoleMapper;
import com.datafactory.core.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 * 实现用户注册、登录、登出等业务逻辑，包括密码加密校验、JWT 令牌生成、刷新令牌管理等。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IAuthServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements IAuthService {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final JwtUtils jwtUtils;
    private final SysRefreshTokenMapper refreshTokenMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;

    /**
     * 用户登录
     * 根据账号（用户名/邮箱/手机号）查询用户，校验密码和状态，生成令牌并更新最后登录时间。
     *
     * @param loginRequest 登录请求参数
     * @return 登录响应，包含访问令牌、刷新令牌和用户基本信息
     * @throws BusinessException 用户名或密码错误、账号已停用
     */
    @Override
    public LoginVo login(LoginRequest loginRequest) {
        // 1. 根据账号（用户名/邮箱/手机号）查询用户
        SysUser user = lambdaQuery()
                .and(w -> w.eq(SysUser::getUsername, loginRequest.getAccount())
                        .or().eq(SysUser::getEmail, loginRequest.getAccount())
                        .or().eq(SysUser::getMobile, loginRequest.getAccount()))
                .one();

        // 2. 校验用户是否存在
        if (user == null) {
            throw new BusinessException(StatusCode.INVALID_CREDENTIALS);
        }

        // 3. BCrypt 密码校验
        if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException(StatusCode.INVALID_CREDENTIALS);
        }

        // 4. 校验用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(StatusCode.USER_DISABLED);
        }

        // 5. 生成令牌
        String userId = String.valueOf(user.getId());
        String accessToken = jwtUtils.generateToken(userId);
        String refreshToken = createRefreshToken(user.getId());

        // 6. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        lambdaUpdate()
                .eq(SysUser::getId, user.getId())
                .update(user);

        // 7. 构建返回
        log.info("用户登录成功：{}", user.getUsername());
        return buildLoginVo(accessToken, refreshToken, user);
    }

    /**
     * 用户注册
     * 校验参数合法性（密码一致性、用户名/邮箱/手机号唯一性），BCrypt 加密密码后创建用户，
     * 注册成功后自动登录并返回令牌。
     *
     * @param registerRequest 注册请求参数
     * @return 登录响应，包含访问令牌、刷新令牌和用户基本信息
     * @throws BusinessException 用户名/邮箱/手机号已存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVo register(RegisterRequest registerRequest) {
        // 1. 校验确认密码
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "两次输入的密码不一致");
        }

        // 2. 校验用户名唯一性
        Long usernameCount = lambdaQuery()
                .eq(SysUser::getUsername, registerRequest.getUsername())
                .count();
        if (usernameCount > 0) {
            throw new BusinessException(100409, "用户名已存在");
        }

        // 3. 校验邮箱唯一性
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isBlank()) {
            Long emailCount = lambdaQuery()
                    .eq(SysUser::getEmail, registerRequest.getEmail())
                    .count();
            if (emailCount > 0) {
                throw new BusinessException(100410, "邮箱已被注册");
            }
        }

        // 4. 校验手机号唯一性
        if (registerRequest.getMobile() != null && !registerRequest.getMobile().isBlank()) {
            Long mobileCount = lambdaQuery()
                    .eq(SysUser::getMobile, registerRequest.getMobile())
                    .count();
            if (mobileCount > 0) {
                throw new BusinessException(100411, "手机号已被注册");
            }
        }

        // 5. 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setNickname(registerRequest.getNickname());
        user.setEmail(registerRequest.getEmail());
        user.setMobile(registerRequest.getMobile());
        user.setRemark(registerRequest.getRemark());
        user.setStatus(1); // 默认启用

        save(user);

        // 6. 注册成功后自动登录，返回令牌
        String userIdStr = String.valueOf(user.getId());
        String accessToken = jwtUtils.generateToken(userIdStr);
        String refreshToken = createRefreshToken(user.getId());

        log.info("用户注册成功：{}", user.getUsername());
        return buildLoginVo(accessToken, refreshToken, user);
    }

    /**
     * 用户登出
     * 解析令牌获取用户ID，删除该用户的所有刷新令牌，使当前令牌失效。
     *
     * @param token 当前访问令牌（Bearer Token）
     * @throws BusinessException 令牌无效或已过期
     */
    @Override
    public void logout(String token) {
        // 1. 校验令牌有效性
        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException(StatusCode.INVALID_TOKEN);
        }

        // 2. 从令牌中解析用户ID
        String userIdStr = jwtUtils.getUserIdFromToken(token);
        Long userId = Long.valueOf(userIdStr);

        // 3. 删除该用户的所有刷新令牌
        refreshTokenMapper.delete(
                new LambdaQueryWrapper<SysRefreshToken>()
                        .eq(SysRefreshToken::getUserId, userId)
        );

        log.info("用户登出成功，userId: {}", userId);
    }

    /**
     * 获取当前用户信息
     * 根据令牌获取用户信息，并查询该用户的角色列表和权限标识列表。
     *
     * @param token 当前访问令牌
     * @return 当前用户信息（含角色和权限）
     * @throws BusinessException 令牌无效或已过期
     */
    @Override
    public CurrentUserVo getCurrentUserInfo(String token) {
        // 1. 校验令牌有效性
        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException(StatusCode.INVALID_TOKEN);
        }

        // 2. 从令牌中解析用户ID
        String userIdStr = jwtUtils.getUserIdFromToken(token);
        Long userId = Long.valueOf(userIdStr);

        // 3. 查询用户信息
        SysUser user = lambdaQuery()
                .eq(SysUser::getId, userId)
                .one();
        if (user == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "用户不存在");
        }

        // 4. 查询用户角色列表
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        List<String> roleCodes = List.of();
        List<Long> permissionIds = List.of();
        if (!roleIds.isEmpty()) {
            // 查询角色编码
            List<SysRole> roles = roleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>()
                            .in(SysRole::getId, roleIds)
            );
            roleCodes = roles.stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.toList());

            // 查询角色关联的权限ID
            List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
                    new LambdaQueryWrapper<SysRolePermission>()
                            .in(SysRolePermission::getRoleId, roleIds)
            );
            permissionIds = rolePermissions.stream()
                    .map(SysRolePermission::getPermissionId)
                    .collect(Collectors.toList());
        }

        // 5. 查询权限标识
        List<String> permissionCodes = List.of();
        if (!permissionIds.isEmpty()) {
            List<SysPermission> permissions = permissionMapper.selectList(
                    new LambdaQueryWrapper<SysPermission>()
                            .in(SysPermission::getId, permissionIds)
            );
            permissionCodes = permissions.stream()
                    .map(SysPermission::getPermissionCode)
                    .collect(Collectors.toList());
        }

        // 6. 构建返回
        CurrentUserVo currentUserVo = new CurrentUserVo();
        currentUserVo.setId(user.getId());
        currentUserVo.setUsername(user.getUsername());
        currentUserVo.setNickname(user.getNickname());
        currentUserVo.setEmail(user.getEmail());
        currentUserVo.setMobile(user.getMobile());
        currentUserVo.setStatus(user.getStatus());
        currentUserVo.setRoles(roleCodes);
        currentUserVo.setPermissions(permissionCodes);
        currentUserVo.setLastLoginTime(user.getLastLoginTime());
        currentUserVo.setCreateTime(user.getCreateTime());

        log.info("获取用户信息成功，userId: {}", userId);
        return currentUserVo;
    }

    /**
     * 刷新访问令牌
     * 校验刷新令牌有效性，生成新的访问令牌和刷新令牌，旧刷新令牌立即失效。
     *
     * @param refreshTokenRequest 刷新令牌请求参数
     * @return 新的令牌信息
     * @throws BusinessException 刷新令牌无效或已过期
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefreshTokenVo refreshToken(RefreshTokenRequest refreshTokenRequest) {
        // 1. 查询刷新令牌
        SysRefreshToken oldToken = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<SysRefreshToken>()
                        .eq(SysRefreshToken::getToken, refreshTokenRequest.getRefreshToken())
        );

        // 2. 校验令牌是否存在
        if (oldToken == null) {
            throw new BusinessException(StatusCode.REFRESH_TOKEN_INVALID);
        }

        // 3. 校验令牌是否过期
        if (oldToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 删除已过期的令牌记录
            refreshTokenMapper.deleteById(oldToken.getId());
            throw new BusinessException(StatusCode.REFRESH_TOKEN_INVALID);
        }

        // 4. 查询用户并校验状态
        SysUser user = lambdaQuery()
                .eq(SysUser::getId, oldToken.getUserId())
                .one();
        if (user == null) {
            // 用户已被删除，清理令牌
            refreshTokenMapper.deleteById(oldToken.getId());
            throw new BusinessException(StatusCode.REFRESH_TOKEN_INVALID);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            // 用户已被停用，清理令牌
            refreshTokenMapper.deleteById(oldToken.getId());
            throw new BusinessException(StatusCode.USER_DISABLED);
        }

        // 5. 生成新的令牌
        String newAccessToken = jwtUtils.generateToken(String.valueOf(user.getId()));
        String newRefreshToken = createRefreshToken(user.getId());

        // 6. 删除旧的刷新令牌
        refreshTokenMapper.deleteById(oldToken.getId());

        log.info("令牌刷新成功，userId: {}", user.getId());
        return RefreshTokenVo.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(7200L)
                .build();
    }

    /**
     * 修改密码
     * 校验原密码、新密码合法性，BCrypt 加密后更新密码。
     *
     * @param token                  当前访问令牌
     * @param passwordUpdateRequest  修改密码请求参数
     * @throws BusinessException 令牌无效、原密码错误、新旧密码相同
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String token, PasswordUpdateRequest passwordUpdateRequest) {
        // 1. 校验令牌有效性
        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException(StatusCode.INVALID_TOKEN);
        }

        // 2. 校验两次密码一致性
        if (!passwordUpdateRequest.getNewPassword().equals(passwordUpdateRequest.getConfirmPassword())) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "两次输入的新密码不一致");
        }

        // 3. 解析用户
        String userIdStr = jwtUtils.getUserIdFromToken(token);
        Long userId = Long.valueOf(userIdStr);
        SysUser user = lambdaQuery()
                .eq(SysUser::getId, userId)
                .one();
        if (user == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "用户不存在");
        }

        // 4. 校验原密码
        if (!encoder.matches(passwordUpdateRequest.getOldPassword(), user.getPassword())) {
            throw new BusinessException(StatusCode.PASSWORD_INCORRECT);
        }

        // 5. 校验新旧密码不能相同
        if (passwordUpdateRequest.getOldPassword().equals(passwordUpdateRequest.getNewPassword())) {
            throw new BusinessException(StatusCode.PASSWORD_SAME_AS_OLD);
        }

        // 6. 更新密码
        user.setPassword(encoder.encode(passwordUpdateRequest.getNewPassword()));
        lambdaUpdate()
                .eq(SysUser::getId, userId)
                .update(user);

        // 7. 删除该用户的所有刷新令牌，强制重新登录
        refreshTokenMapper.delete(
                new LambdaQueryWrapper<SysRefreshToken>()
                        .eq(SysRefreshToken::getUserId, userId)
        );

        log.info("密码修改成功，userId: {}", userId);
    }

    /**
     * 修改个人信息
     * 更新昵称、邮箱、手机号、备注，校验邮箱和手机号唯一性（排除当前用户）。
     *
     * @param token                当前访问令牌
     * @param profileUpdateRequest 个人信息请求参数
     * @return 更新后的用户信息
     * @throws BusinessException 令牌无效、邮箱或手机号已被占用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVo updateProfile(String token, ProfileUpdateRequest profileUpdateRequest) {
        // 1. 校验令牌有效性
        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException(StatusCode.INVALID_TOKEN);
        }

        // 2. 解析用户
        String userIdStr = jwtUtils.getUserIdFromToken(token);
        Long userId = Long.valueOf(userIdStr);
        SysUser user = lambdaQuery()
                .eq(SysUser::getId, userId)
                .one();
        if (user == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "用户不存在");
        }

        // 3. 校验邮箱唯一性（排除当前用户）
        if (profileUpdateRequest.getEmail() != null && !profileUpdateRequest.getEmail().isBlank()
                && !profileUpdateRequest.getEmail().equals(user.getEmail())) {
            Long emailCount = lambdaQuery()
                    .eq(SysUser::getEmail, profileUpdateRequest.getEmail())
                    .ne(SysUser::getId, userId)
                    .count();
            if (emailCount > 0) {
                throw new BusinessException(100409, "该邮箱已被其他用户使用");
            }
        }

        // 4. 校验手机号唯一性（排除当前用户）
        if (profileUpdateRequest.getMobile() != null && !profileUpdateRequest.getMobile().isBlank()
                && !profileUpdateRequest.getMobile().equals(user.getMobile())) {
            Long mobileCount = lambdaQuery()
                    .eq(SysUser::getMobile, profileUpdateRequest.getMobile())
                    .ne(SysUser::getId, userId)
                    .count();
            if (mobileCount > 0) {
                throw new BusinessException(100410, "该手机号已被其他用户使用");
            }
        }

        // 5. 仅更新非空字段
        boolean updated = false;
        if (profileUpdateRequest.getNickname() != null && !profileUpdateRequest.getNickname().isBlank()) {
            user.setNickname(profileUpdateRequest.getNickname());
            updated = true;
        }
        if (profileUpdateRequest.getEmail() != null && !profileUpdateRequest.getEmail().isBlank()) {
            user.setEmail(profileUpdateRequest.getEmail());
            updated = true;
        }
        if (profileUpdateRequest.getMobile() != null && !profileUpdateRequest.getMobile().isBlank()) {
            user.setMobile(profileUpdateRequest.getMobile());
            updated = true;
        }
        if (profileUpdateRequest.getRemark() != null) {
            user.setRemark(profileUpdateRequest.getRemark());
            updated = true;
        }

        if (updated) {
            lambdaUpdate()
                    .eq(SysUser::getId, userId)
                    .update(user);
        }

        // 6. 构建返回
        UserInfoVo userInfo = new UserInfoVo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setMobile(user.getMobile());
        userInfo.setStatus(user.getStatus());
        userInfo.setLastLoginTime(user.getLastLoginTime());
        userInfo.setCreateTime(user.getCreateTime());

        log.info("个人信息修改成功，userId: {}", userId);
        return userInfo;
    }

    /**
     * 创建刷新令牌并保存到数据库
     *
     * @param userId 用户ID
     * @return 刷新令牌字符串
     */
    private String createRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        SysRefreshToken refreshToken = new SysRefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setCreateTime(LocalDateTime.now());
        refreshTokenMapper.insert(refreshToken);
        return token;
    }

    /**
     * 构建登录/注册响应 VO
     *
     * @param accessToken  访问令牌
     * @param refreshToken 刷新令牌
     * @param user         用户实体
     * @return 登录响应 VO
     */
    private LoginVo buildLoginVo(String accessToken, String refreshToken, SysUser user) {
        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(accessToken);
        loginVo.setRefreshToken(refreshToken);
        loginVo.setTokenType("Bearer");
        loginVo.setExpiresIn(7200L);

        UserInfoVo userInfo = new UserInfoVo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setMobile(user.getMobile());
        userInfo.setStatus(user.getStatus());
        userInfo.setLastLoginTime(user.getLastLoginTime());
        userInfo.setCreateTime(user.getCreateTime());
        loginVo.setUserInfo(userInfo);

        return loginVo;
    }
}
