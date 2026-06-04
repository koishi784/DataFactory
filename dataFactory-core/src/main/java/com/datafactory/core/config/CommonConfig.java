package com.datafactory.core.config;

import com.datafactory.common.config.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 通用配置 - 启用 JwtProperties 配置绑定
 */

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class CommonConfig {
}
