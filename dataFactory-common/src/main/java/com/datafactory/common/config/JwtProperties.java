package com.datafactory.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类
 * 对应 application.yaml 中 jwt.* 配置项
 */

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 密钥（至少32字节） */
    private String secret;

    /** 访问令牌过期时间（秒） */
    private Long accessTokenExpiration;

    /** 刷新令牌过期时间（秒） */
    private Long refreshTokenExpiration;
}
