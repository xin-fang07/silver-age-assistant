package com.example.elderai.common;

import com.example.elderai.observability.TraceContext;

/**
 * 统一返回结果类
 * <p>
 * 封装所有 API 接口的返回格式。
 * </p>
 *
 * @param <T> 携带的数据类型
 * @author elder-ai-team
 */
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private String requestId;

    public Result() {
        this.requestId = TraceContext.currentTraceId();
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = TraceContext.currentTraceId();
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    // ==================== 静态工厂方法 ====================

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> success(String message) {
        return (Result<T>) new Result<>(200, message, null);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "请先登录", null);
    }

    public static <T> Result<T> forbidden() {
        return new Result<>(403, "没有访问权限", null);
    }
}
