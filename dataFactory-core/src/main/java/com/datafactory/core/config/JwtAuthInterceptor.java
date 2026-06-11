package com.datafactory.core.config;

import com.datafactory.common.enums.StatusCode;
import com.datafactory.common.response.Result;
import com.datafactory.common.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证拦截器
 * 拦截所有 /api/v1/** 请求（公开接口除外），验证 Access Token 的有效性，
 * 并将用户 ID 存入请求属性 {@link #USER_ID_ATTR} 中，供后续业务逻辑使用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    /** 请求属性中存放用户 ID 的 key */
    public static final String USER_ID_ATTR = "_userId";

    /** 不需要认证的路径前缀 */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token"
    );

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {
        // 放行 OPTIONS 预检请求（CORS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 放行公开接口
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            return true;
        }

        // 只拦截 /api/v1/ 开头的请求
        if (!path.startsWith("/api/v1/")) {
            return true;
        }

        // 获取 Authorization 头
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("缺少有效的 Authorization 头, path={}", path);
            writeUnauthorized(response, StatusCode.UNAUTHORIZED.getCode(), "缺少认证令牌，请先登录");
            return false;
        }

        String token = authHeader.substring(7);

        // 验证令牌
        if (!jwtUtils.validateToken(token)) {
            log.warn("无效的令牌, path={}", path);
            writeUnauthorized(response, StatusCode.INVALID_TOKEN.getCode(), "无效的令牌，请重新登录");
            return false;
        }

        // 将用户 ID 存入请求属性
        String userId = jwtUtils.getUserIdFromToken(token);
        request.setAttribute(USER_ID_ATTR, userId);
        return true;
    }

    /**
     * 判断是否为公开路径
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::equals);
    }

    /**
     * 写入 401 未授权响应
     */
    private void writeUnauthorized(HttpServletResponse response,
                                   int code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result<Void> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
