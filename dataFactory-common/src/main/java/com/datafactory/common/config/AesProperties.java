package com.datafactory.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AES 加密配置属性类
 * 对应 application.yaml 中 aes.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "aes")
public class AesProperties {

    /**
     * AES 密钥（必须为 32 字节，对应 AES-256）
     */
    private String secret;
}
