package com.datafactory.core.service;

/**
 * Coze AI 聊天助手服务接口
 *
 * 提供与数据工厂聊天小助手交互的能力
 */
public interface ICozeService {

    /**
     * 发送消息给 Coze AI 聊天助手
     *
     * @param userId  用户标识（前端传入的用户ID）
     * @param message 用户发送的消息内容
     * @return AI 助手的回复内容
     */
    String chat(String userId, String message);
}
