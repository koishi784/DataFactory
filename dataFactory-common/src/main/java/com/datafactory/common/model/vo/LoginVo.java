package com.datafactory.common.model.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录参数
 */

@Data
public class LoginVo {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
