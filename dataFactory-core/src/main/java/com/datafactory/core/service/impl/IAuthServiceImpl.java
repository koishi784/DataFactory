package com.datafactory.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datafactory.common.model.dto.LoginRequest;
import com.datafactory.common.model.vo.LoginVo;
import com.datafactory.core.domain.entity.SysUser;
import com.datafactory.core.domain.mapper.SysUserMapper;
import com.datafactory.core.service.IAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IAuthServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements IAuthService {
    
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    @Override
    public LoginVo login(LoginRequest loginRequest) {
        // 1. 根据用户名查询用户
        SysUser user = lambdaQuery()
                .eq(SysUser::getUsername, loginRequest.getUsername())
                .one();
        
        // 2. 校验用户是否存在
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 3. BCrypt 密码校验
        if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 4. 返回登录信息
        LoginVo loginVo = new LoginVo();
        loginVo.setUsername(user.getUsername());
        loginVo.setPassword("");
        
        log.info("用户登录成功：{}", user.getUsername());
        return loginVo;
    }

    @Override
    public void logout(String token) {
        log.info("用户登出，token: {}", token);
    }
}
