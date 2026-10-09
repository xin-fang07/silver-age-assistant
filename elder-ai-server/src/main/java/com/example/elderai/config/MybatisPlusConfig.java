package com.example.elderai.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 *
 * @author elder-ai-team
 */
@Configuration
@MapperScan("com.example.elderai.mapper")
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器
     * <p>
     * 注册分页内部拦截器，指定数据库类型为 MySQL。
     * 使用时只需在 Mapper 方法参数中传入 Page 对象即可自动分页。
     * </p>
     *
     * 使用示例：
     * <pre>{@code
     * // 在 Service 或 Controller 中调用：
     * Page<User> page = new Page<>(pageNum, pageSize);
     * userMapper.selectPage(page, queryWrapper);
     * }</pre>
     *
     * @return MybatisPlusInterceptor 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 1. 创建 MyBatis-Plus 总拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 2. 创建分页内部拦截器，指定数据库类型为 MySQL
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);

        // 设置合理的单页最大条数限制，防止一次查询过多数据导致内存溢出
        paginationInterceptor.setMaxLimit(100L);

        // 3. 将分页拦截器添加到总拦截器中
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}
