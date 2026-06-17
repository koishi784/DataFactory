package com.datafactory.api.controller;

import com.datafactory.common.response.Result;
import com.datafactory.core.service.ICozeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Coze AI 聊天小助手控制器
 *
 * 提供与"数据工厂聊天小助手"对话的接口
 */
@Tag(name = "聊天小助手", description = "数据工厂 AI 聊天助手，支持平台使用问题解答")
@RestController
@RequestMapping("/api/v1/coze")
@RequiredArgsConstructor
public class CozeController {

    private final ICozeService cozeService;

    @Operation(summary = "发送消息给 AI 聊天助手", description = "向数据工厂聊天小助手发送消息，返回 AI 回复内容")
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String reply = cozeService.chat(request.getUserId(), request.getMessage());
        return Result.success(new ChatResponse(reply));
    }

    @Data
    public static class ChatRequest {
        @NotBlank(message = "用户标识不能为空")
        private String userId;

        @NotBlank(message = "消息内容不能为空")
        private String message;
    }

    @Data
    @AllArgsConstructor
    public static class ChatResponse {
        private String reply;
    }
}
