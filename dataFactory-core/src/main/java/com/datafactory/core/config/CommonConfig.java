package com.datafactory.core.config;

import com.datafactory.common.config.AesProperties;
import com.datafactory.common.config.CozeProperties;
import com.datafactory.common.config.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 通用配置 - 启用 JwtProperties、AesProperties、CozeProperties 等配置绑定
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, AesProperties.class, CozeProperties.class})
public class CommonConfig {

    /**
     * HTTP 客户端 Bean，用于调用第三方 REST API
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
