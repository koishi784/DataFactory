package com.datafactory.common.enums;

/**
 * 状态码常量类
 */

public class StatusCode {
    // 成功状态码
    public static final Integer SUCCESS = 100200;

    // 客户端错误状态码 (100400 - 100499)
    public static final Integer UNAUTHORIZED = 100401;  // 未授权
    public static final Integer FORBIDDEN = 100403;     // 禁止访问
    public static final Integer NOT_FOUND = 100404;     // 资源不存在
    public static final Integer BAD_REQUEST = 100400;   // 请求参数错误

    // 服务端错误状态码 (100500 - 100599)
    public static final Integer SERVER_ERROR = 100500;  // 服务器内部错误
    public static final Integer SERVICE_UNAVAILABLE = 100503;  // 服务不可用

    // 业务错误状态码 (100600 - 100699)
    public static final Integer BUSINESS_ERROR = 100600;  // 业务错误
    public static final Integer USER_EXISTS = 100601;     // 用户已存在
    public static final Integer MOBILE_EXISTS = 100602;   // 手机号已存在

    private StatusCode() {
        // 私有构造函数，防止实例化
    }
}
