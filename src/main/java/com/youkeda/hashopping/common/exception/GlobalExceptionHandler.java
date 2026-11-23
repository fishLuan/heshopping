package com.youkeda.hashopping.common.exception;

import com.youkeda.hashopping.common.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** 将 Controller 层的异常转换为统一的 Result 响应。 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 返回业务层明确指定的状态、业务码和提示。 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Result.error(ex.getCode(), ex.getMessage()));
    }

    /** 保留框架异常的 HTTP 状态，并隐藏服务端错误细节。 */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> handleStatus(ResponseStatusException ex) {
        HttpStatus status = ex.getStatus();
        if (status.is5xxServerError()) {
            LOG.error("请求处理失败", ex);
            return ResponseEntity.status(status).body(Result.error());
        }
        String message = ex.getReason() == null ? "请求无法处理" : ex.getReason();
        return ResponseEntity.status(status).body(Result.error(status.name(), message));
    }

    /** 封装 Spring MVC 参数和请求格式异常，保留框架设置的响应头。 */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        Result<Void> result;
        if (status.is5xxServerError()) {
            LOG.error("Spring MVC 处理请求失败", ex);
            result = Result.error();
        } else if (ex instanceof HttpRequestMethodNotSupportedException) {
            result = Result.error("METHOD_NOT_ALLOWED", "该接口不支持此请求方法");
        } else if (ex instanceof HttpMediaTypeNotSupportedException) {
            result = Result.error("UNSUPPORTED_MEDIA_TYPE", "不支持该请求内容类型");
        } else if (status == HttpStatus.BAD_REQUEST) {
            result = Result.error("INVALID_ARGUMENT", "请求参数缺失或格式不正确");
        } else {
            result = Result.error(status.name(), "请求无法处理");
        }
        return super.handleExceptionInternal(ex, result, headers, status, request);
    }

    /** 记录未预期异常，认证和授权异常继续交给 Spring Security。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpected(Exception ex) throws Exception {
        if (ex instanceof AuthenticationException || ex instanceof AccessDeniedException) {
            throw ex;
        }
        LOG.error("未预期的接口异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error());
    }
}
