package com.datafactory.common.exception;

import com.datafactory.common.enums.StatusCode;
import lombok.Getter;

/**
 * 自定义业务异常
 */

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = StatusCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(StatusCode statusCode) {
        super(statusCode.getMessage());
        this.code = statusCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(StatusCode statusCode, String message) {
        super(message);
        this.code = statusCode.getCode();
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
