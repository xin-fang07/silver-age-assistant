package com.example.elderai.common;

import lombok.Getter;

/**
 * 自定义业务异常类
 * <p>
 * 用于在业务逻辑中主动抛出异常，由全局异常处理器统一捕获并转换为 Result 格式返回给前端。
 * 继承自 RuntimeException，无需在方法签名上声明 throws。
 * </p>
 *
 * <pre>
 * 使用示例：
 *   // 抛出"用户不存在"业务异常
 *   throw new BusinessException(404, "用户不存在");
 *
 *   // 快捷构造（默认 code = 400）
 *   throw new BusinessException("操作失败");
 * </pre>
 *
 * @author elder-ai-team
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码，默认 400 */
    private final int code;

    /**
     * 构造业务异常（使用默认错误码 400）
     *
     * @param message 错误提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 构造业务异常（自定义错误码）
     *
     * @param code    业务错误码
     * @param message 错误提示信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
