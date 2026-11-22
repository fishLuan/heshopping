package com.youkeda.hashopping.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 接口统一返回结果，包含操作是否成功、提示级别、业务码和返回数据。
 *
 * @param <D> 返回数据的类型
 */
public class Result<D> implements Serializable {
    @JsonProperty("isSuccess")
    private boolean success = false;

    /** 前端展示操作消息时使用的提示级别。 */
    public enum Level {
        /** 操作正常完成。 */
        SUCCESS,
        /** 操作需要提示，是否完成以 isSuccess 为准。 */
        WARNING,
        /** 操作失败。 */
        ERROR
    }

    private Level level;

    private String code;
    private String message;

    private D data;

    /** 创建空结果，供调用方逐个设置字段。 */
    public static <T> Result<T> create() {
        return new Result<T>();
    }

    /** 返回操作成功的结果，不携带数据。 */
    public static <T> Result<T> success() {
        return success("OK", "操作成功", null);
    }

    /** 返回操作成功的结果，并携带业务数据。 */
    public static <T> Result<T> success(T data) {
        return success("OK", "操作成功", data);
    }

    /** 使用指定的业务码、提示消息和数据返回成功结果。 */
    public static <T> Result<T> success(String code, String message, T data) {
        return of(Level.SUCCESS, true, code, message, data);
    }

    /** 返回操作未完成的警告，例如重复提交。 */
    public static <T> Result<T> warning(String code, String message) {
        return of(Level.WARNING, false, code, message, null);
    }

    /**
     * 操作已经执行，但需要提示部分成功或降级等情况。
     */
    public static <T> Result<T> successWithWarning(String code, String message, T data) {
        return of(Level.WARNING, true, code, message, data);
    }

    /** 返回通用系统错误，避免将内部异常细节暴露给前端。 */
    public static <T> Result<T> error() {
        return error("INTERNAL_ERROR", "操作失败，请稍后再试");
    }

    /** 使用指定的业务码和提示消息返回失败结果。 */
    public static <T> Result<T> error(String code, String message) {
        return of(Level.ERROR, false, code, message, null);
    }

    /** 按给定的级别、成功标记和业务字段组装结果。 */
    private static <T> Result<T> of(Level level, boolean success, String code, String message, T data) {
        Result<T> result = new Result<>();
        result.level = level;
        result.success = success;
        result.code = code;
        result.message = message;
        result.data = data;
        return result;
    }

    /** 获取提示级别。旧代码未设置级别时，根据成功标记推导。 */
    public Level getLevel() {
        if (level != null) {
            return level;
        }
        return success ? Level.SUCCESS : Level.ERROR;
    }

    /** 返回操作是否成功，接口中的字段名为 isSuccess。 */
    public boolean isSuccess() {
        return success;
    }

    /** 设置操作是否成功。标记变化时清除旧级别，避免提示与操作状态不一致。 */
    public Result<D> setSuccess(boolean success) {
        if (this.success != success) {
            this.level = null;
        }
        this.success = success;
        return this;
    }

    /** 获取业务码，供前端识别具体场景。 */
    public String getCode() {
        return code;
    }

    /** 设置业务码，并返回当前结果以便链式调用。 */
    public Result<D> setCode(String code) {
        this.code = code;
        return this;
    }

    /** 获取可展示给用户的提示消息。 */
    public String getMessage() {
        return message;
    }

    /** 设置提示消息，并返回当前结果。 */
    public Result<D> setMessage(String message) {
        this.message = message;
        return this;
    }

    /** 获取业务数据，没有数据时返回 null。 */
    public D getData() {
        return data;
    }

    /** 设置业务数据，并返回当前结果。 */
    public Result<D> setData(D data) {
        this.data = data;
        return this;
    }
}
