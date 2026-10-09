package com.example.elderai.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文工具类
 * <p>
 * 实现了 {@link ApplicationContextAware} 接口，在 Spring 容器初始化时
 * 自动注入 {@link ApplicationContext} 引用并保存为静态变量。
 * 之后在任意位置（包括非 Spring 管理的类）都可以通过静态方法获取 Bean。
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>在非 Spring 管理的 POJO 中需要调用 Spring Bean 的方法</li>
 *   <li>在静态方法中需要获取 Spring 管理的 Bean 实例</li>
 *   <li>在无法通过 @Autowired 注入的场合获取 Bean</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * JwtUtils jwtUtils = SpringContextUtils.getBean(JwtUtils.class);
 * String token = jwtUtils.generateToken(1L, "admin", "ADMIN");
 * </pre>
 * </p>
 *
 * @author elder-ai-team
 */
@Component // 标记为 Spring 管理的 Bean，确保 ApplicationContextAware 回调被触发
public class SpringContextUtils implements ApplicationContextAware {

    /**
     * 静态变量：持有 Spring 应用上下文引用
     * <p>
     * 设置为 volatile 确保多线程环境下的可见性
     * </p>
     */
    private static volatile ApplicationContext applicationContext;

    /**
     * Spring 容器初始化完成后自动调用，注入 ApplicationContext
     * <p>
     * 此方法是 {@link ApplicationContextAware} 接口的回调方法，
     * 由 Spring 框架在 Bean 初始化阶段自动调用。
     * </p>
     *
     * @param context Spring 应用上下文
     * @throws BeansException 如果获取上下文过程中发生异常
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 获取当前 Spring 应用上下文
     *
     * @return ApplicationContext 实例
     * @throws IllegalStateException 如果 Spring 容器尚未初始化
     */
    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException(
                    "Spring 容器尚未初始化，请确保 SpringContextUtils 已被 Spring 管理"
            );
        }
        return applicationContext;
    }

    /**
     * 根据类型获取 Spring Bean
     * <p>
     * 如果存在多个同类型的 Bean，会抛出异常。
     * 存在多个同类型 Bean 时请使用 {@link #getBean(String, Class)} 方法。
     * </p>
     *
     * @param clazz Bean 的 Class 类型
     * @param <T>   泛型类型
     * @return 指定类型的 Bean 实例
     * @throws IllegalStateException     如果 Spring 容器尚未初始化
     * @throws BeansException            如果指定类型的 Bean 不存在或存在多个
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 根据 Bean 名称和类型获取 Spring Bean
     *
     * @param name  Bean 的名称（即 Spring 容器中的 beanName）
     * @param clazz Bean 的 Class 类型
     * @param <T>   泛型类型
     * @return 指定名称和类型的 Bean 实例
     * @throws IllegalStateException 如果 Spring 容器尚未初始化
     * @throws BeansException        如果指定名称和类型的 Bean 不存在
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }
}
