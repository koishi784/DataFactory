package com.datafactory.common.response;

import com.datafactory.common.enums.StatusCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 统一返回结果类
 */

@Data
@NoArgsConstructor
public class Result<T> implements Serializable {

    private Integer code;

    private String message;

    private T data;

    // 全参构造函数（显式声明以解决泛型推断问题）
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功静态方法
    public static <T> Result<T> success(T data) {
        return new Result<>(StatusCode.SUCCESS.getCode(), StatusCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(StatusCode.SUCCESS.getCode(), message, data);
    }

    // 失败静态方法
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, (T) null);
    }

    public static <T> Result<T> error(StatusCode statusCode) {
        return new Result<>(statusCode.getCode(), statusCode.getMessage(), (T) null);
    }

    public static <T> Result<T> error(StatusCode statusCode, String message) {
        return new Result<>(statusCode.getCode(), message, (T) null);
    }
}
