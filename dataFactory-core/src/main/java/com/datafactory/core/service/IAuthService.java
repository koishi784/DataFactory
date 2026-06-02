package com.datafactory.core.service;

import com.datafactory.common.model.dto.LoginRequest;
import com.datafactory.common.model.vo.LoginVo;

/**
 * 登录服务
 */

public interface IAuthService {
    LoginVo login(LoginRequest loginRequest);
    void logout(String token);
}
