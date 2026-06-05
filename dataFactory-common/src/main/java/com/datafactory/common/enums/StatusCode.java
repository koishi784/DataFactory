package com.datafactory.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务状态码枚举
 * 格式: 100XXX
 *   1002XX - 成功
 *   1004XX - 客户端错误
 *   1005XX - 服务端错误
 *   1006XX - 认证授权错误
 *   1007XX - 业务通用错误
 */

@Getter
@AllArgsConstructor
public enum StatusCode {

    // ========== 成功 ==========
    SUCCESS(100200, "操作成功"),

    // ========== 客户端错误 (1004XX) ==========
    BAD_REQUEST(100400, "请求参数错误"),
    UNAUTHORIZED(100401, "未授权，请先登录"),
    FORBIDDEN(100403, "禁止访问，权限不足"),
    NOT_FOUND(100404, "资源不存在"),
    METHOD_NOT_ALLOWED(100405, "请求方法不支持"),
    REQUEST_TIMEOUT(100408, "请求超时"),
    UNSUPPORTED_MEDIA_TYPE(100415, "不支持的媒体类型"),
    VALIDATION_FAILED(100422, "参数校验失败"),

    // ========== 服务端错误 (1005XX) ==========
    SERVER_ERROR(100500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(100503, "服务暂不可用"),
    GATEWAY_TIMEOUT(100504, "网关超时"),

    // ========== 认证授权 (1006XX) ==========
    TOKEN_EXPIRED(100601, "令牌已过期，请重新登录"),
    INVALID_TOKEN(100602, "无效的令牌"),
    INVALID_CREDENTIALS(100603, "用户名或密码错误"),
    USER_DISABLED(100604, "账号已被停用"),
    USER_LOCKED(100605, "账号已被锁定"),
    PERMISSION_DENIED(100606, "无操作权限"),
    PASSWORD_INCORRECT(100607, "原密码错误"),

    // ========== 刷新令牌 (1006XX) ==========
    REFRESH_TOKEN_INVALID(100415, "刷新令牌无效或已过期，请重新登录"),

    // ========== 密码相关 ==========
    PASSWORD_SAME_AS_OLD(100414, "新密码不能与原密码相同"),

    // ========== 业务通用 (1007XX) ==========
    BUSINESS_ERROR(100700, "业务处理异常"),
    DATA_EXISTS(100701, "数据已存在"),
    DATA_NOT_EXISTS(100702, "数据不存在"),
    DATA_CONFLICT(100703, "数据冲突"),
    OPERATION_FAILED(100704, "操作失败"),
    FILE_TOO_LARGE(100705, "文件大小超出限制"),
    FILE_TYPE_NOT_ALLOWED(100706, "文件类型不允许"),
    DATABASE_CONNECTION_FAILED(100707, "数据库连接测试失败"),
    API_CALL_FAILED(100708, "接口调用失败"),
    TASK_EXECUTION_FAILED(100709, "任务执行失败"),
    DEPENDENCY_NOT_MET(100710, "依赖条件不满足"),
    ;

    private final Integer code;
    private final String message;
}
