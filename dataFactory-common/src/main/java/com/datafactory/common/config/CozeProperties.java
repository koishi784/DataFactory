package com.datafactory.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Coze API 配置属性
 *
 * 从 application.yaml 中读取 coze 开头的配置项
 */
@Data
@ConfigurationProperties(prefix = "coze")
public class CozeProperties {

    /** Coze API 基础地址，默认 https://api.coze.cn */
    private String baseUrl = "https://api.coze.cn";

    /** 个人访问令牌（PAT） */
    private String apiKey;

    /** 对话流 ID（workflow_id） */
    private String workflowId;

    /** Bot ID（如果发布为 Bot 时使用） */
    private String botId;

    /** Coze 中的用户标识 */
    private String userId;

    /** 请求超时时间（秒），默认 60 */
    private int timeout = 60;
}
