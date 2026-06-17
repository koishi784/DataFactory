package com.datafactory.core.service.impl;

import com.datafactory.common.config.CozeProperties;
import com.datafactory.core.service.ICozeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * Coze AI 聊天助手服务实现类
 *
 * 调用 Coze Bot Chat API（非流式模式），分三步：
 * 1. POST /v3/chat         -> 创建对话，获取 conversation_id + chat_id
 * 2. GET  /v3/chat/retrieve -> 轮询对话状态直到 completed
 * 3. GET  /v3/chat/message/list -> 获取消息列表，提取 AI 回复
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ICozeServiceImpl implements ICozeService {

    private final RestTemplate restTemplate;
    private final CozeProperties cozeProperties;

    /** 轮询最大等待时间（秒） */
    private static final int POLL_MAX_SECONDS = 30;

    /** 轮询间隔（毫秒） */
    private static final long POLL_INTERVAL_MS = 1000;

    @Override
    public String chat(String userId, String message) {
        try {
            return doChat(userId, message);
        } catch (Exception e) {
            log.error("调用 Coze API 发生异常", e);
            return "抱歉，我暂时无法回答，请稍后再试。";
        }
    }

    /**
     * 完整的对话流程：创建 -> 轮询 -> 获取回复
     */
    private String doChat(String userId, String message) {
        String baseUrl = cozeProperties.getBaseUrl();

        log.info("=== Coze 请求开始 ===");
        log.info("bot_id: {}, message: {}", cozeProperties.getBotId(), message);

        // 1. 创建对话
        CozeChatSession session = createChat(baseUrl, userId, message);
        if (session == null) {
            return "抱歉，我暂时无法回答，请稍后再试。";
        }
        log.info("创建对话成功, conversation_id={}, chat_id={}", session.conversationId, session.chatId);

        // 2. 轮询直到对话完成（使用 GET 请求）
        boolean completed = pollChatStatus(baseUrl, session);
        if (!completed) {
            log.error("轮询超时或对话失败");
            return "抱歉，我暂时无法回答，请稍后再试。";
        }

        // 3. 获取消息列表（使用 GET 请求）
        String reply = retrieveMessages(baseUrl, session);
        log.info("=== Coze 请求结束, reply={} ===", reply);
        return reply != null ? reply : "抱歉，我暂时无法回答，请稍后再试。";
    }

    /**
     * 创建对话（POST /v3/chat）
     */
    @SuppressWarnings("unchecked")
    private CozeChatSession createChat(String baseUrl, String userId, String message) {
        String url = baseUrl + "/v3/chat";

        HttpHeaders headers = buildHeaders();

        Map<String, Object> additionalMessage = new HashMap<>();
        additionalMessage.put("role", "user");
        additionalMessage.put("content_type", "text");
        additionalMessage.put("content", message);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bot_id", cozeProperties.getBotId());
        requestBody.put("user_id", userId);
        requestBody.put("stream", false);
        requestBody.put("auto_save_history", true);
        requestBody.put("additional_messages", Collections.singletonList(additionalMessage));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        log.info("创建对话, POST {}", url);
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, requestEntity, Map.class);
        Map<String, Object> responseBody = responseEntity.getBody();

        if (responseBody == null || !"0".equals(String.valueOf(responseBody.get("code")))) {
            log.error("创建对话失败, response={}", responseBody);
            return null;
        }

        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        if (data == null) return null;

        CozeChatSession session = new CozeChatSession();
        session.conversationId = (String) data.get("conversation_id");
        // chat_id 是 Long 类型，转成字符串
        session.chatId = String.valueOf(data.get("id"));
        return session;
    }

    /**
     * 轮询对话状态（GET /v3/chat/retrieve）
     */
    @SuppressWarnings("unchecked")
    private boolean pollChatStatus(String baseUrl, CozeChatSession session) {
        HttpHeaders headers = buildHeaders();

        long startTime = System.currentTimeMillis();
        int attempt = 0;

        while (System.currentTimeMillis() - startTime < POLL_MAX_SECONDS * 1000L) {
            attempt++;

            try {
                // 使用 GET 请求，参数通过查询字符串传递
                String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/v3/chat/retrieve")
                        .queryParam("conversation_id", session.conversationId)
                        .queryParam("chat_id", session.chatId)
                        .build()
                        .toUriString();

                HttpEntity<?> requestEntity = new HttpEntity<>(headers);

                log.debug("轮询第 {} 次, GET {}", attempt, url);
                ResponseEntity<Map> responseEntity = restTemplate.exchange(
                        url, HttpMethod.GET, requestEntity, Map.class);

                Map<String, Object> responseBody = responseEntity.getBody();

                log.info("轮询第 {} 次, HTTP={}, 响应={}", attempt,
                        responseEntity.getStatusCode(), responseBody);

                if (responseBody == null) {
                    Thread.sleep(POLL_INTERVAL_MS);
                    continue;
                }

                Object code = responseBody.get("code");
                if (!"0".equals(String.valueOf(code))) {
                    log.warn("轮询第 {} 次: code={}, msg={}", attempt, code, responseBody.get("msg"));
                    Thread.sleep(POLL_INTERVAL_MS);
                    continue;
                }

                Object dataRaw = responseBody.get("data");
                if (!(dataRaw instanceof Map)) {
                    log.warn("轮询第 {} 次: data 不是 Map, 跳过", attempt);
                    Thread.sleep(POLL_INTERVAL_MS);
                    continue;
                }

                Map<String, Object> data = (Map<String, Object>) dataRaw;
                String status = (String) data.get("status");
                log.info("轮询第 {} 次, status={}", attempt, status);

                if ("completed".equals(status)) {
                    return true;
                }

                if ("failed".equals(status)) {
                    log.error("对话执行失败, data={}", data);
                    return false;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("轮询第 {} 次异常: {}", attempt, e.toString());
            }

            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.warn("轮询超时，已等待 {} 秒", POLL_MAX_SECONDS);
        return false;
    }

    /**
     * 获取消息列表（GET /v3/chat/message/list）
     */
    @SuppressWarnings("unchecked")
    private String retrieveMessages(String baseUrl, CozeChatSession session) {
        try {
            // 使用 GET 请求，参数通过查询字符串传递
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/v3/chat/message/list")
                    .queryParam("conversation_id", session.conversationId)
                    .queryParam("chat_id", session.chatId)
                    .build()
                    .toUriString();

            HttpHeaders headers = buildHeaders();
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            log.info("获取消息列表, GET {}", url);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, Map.class);
            Map<String, Object> responseBody = responseEntity.getBody();

            if (responseBody == null || !"0".equals(String.valueOf(responseBody.get("code")))) {
                log.error("获取消息列表失败, response={}", responseBody);
                return null;
            }

            // 解析消息列表（data 是一个 List）
            Object dataObj = responseBody.get("data");
            log.info("消息列表原始 data: {}", dataObj);

            if (dataObj instanceof List) {
                List<Map<String, Object>> messages = (List<Map<String, Object>>) dataObj;
                log.info("消息数量: {}", messages.size());

                // 遍历消息列表，寻找真正的 AI 回答（role=assistant, type=answer）
                // 跳过系统内部消息（如 knowledge_recall, function_call, tool_output 等）
                for (Map<String, Object> msg : messages) {
                    String role = (String) msg.get("role");
                    String type = (String) msg.get("type");
                    String msgType = (String) msg.get("msg_type");
                    String content = (String) msg.get("content");

                    log.info("消息: role={}, type={}, msg_type={}", role, type, msgType);

                    // 只提取 assistant 角色的 answer 类型的消息
                    if ("assistant".equals(role) && "answer".equals(type)
                            && content != null && !content.isEmpty()) {
                        log.info("找到 assistant 回复: {}", content);
                        return content;
                    }
                }

                // 没找到 type=answer 时，兜底：取任意 role=assistant 的消息
                for (Map<String, Object> msg : messages) {
                    String role = (String) msg.get("role");
                    String content = (String) msg.get("content");
                    if ("assistant".equals(role) && content != null && !content.isEmpty()) {
                        log.info("兜底取 assistant 消息: {}", content);
                        return content;
                    }
                }

                log.warn("未找到 assistant 角色的消息");
            }

        } catch (Exception e) {
            log.error("获取消息列表异常", e);
        }

        return null;
    }

    /**
     * 构建通用请求头
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(cozeProperties.getApiKey());
        return headers;
    }

    /**
     * Coze 对话会话信息（内部类）
     */
    private static class CozeChatSession {
        String conversationId;
        String chatId;
    }
}
