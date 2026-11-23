package com.youkeda.hashopping.common.exception;

import org.springframework.http.HttpStatus;

/** 表示可以向用户说明的业务失败，例如订单不存在。 */
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    /** 创建业务异常，提示消息应适合直接展示给用户。 */
    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    /** 获取供前端识别失败场景的业务码。 */
    public String getCode() {
        return code;
    }

    /** 获取应返回给客户端的 HTTP 状态。 */
    public HttpStatus getStatus() {
        return status;
    }
}
