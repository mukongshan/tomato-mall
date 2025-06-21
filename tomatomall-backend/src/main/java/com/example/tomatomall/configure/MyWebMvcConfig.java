package com.example.tomatomall.configure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 *
 * 这个类实现了WebMvcConfigurer接口，
 * 表示会被SpringBoot接受，
 * 这个类的作用是配置拦截器。
 * addInterceptors方法配置了拦截器，
 * 添加了loginInterceptor作为拦截器，
 * 并且设置除了register和login的所有接口都需要通过该拦截器。
*/
@Configuration
public class MyWebMvcConfig implements WebMvcConfigurer {

    // 注入登录拦截器
    @Autowired
    LoginInterceptor loginInterceptor;

    /**
     * 添加拦截器配置
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册登录拦截器
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/accounts/login")
                .excludePathPatterns("/api/orders/alipay/notify")
                .excludePathPatterns("/api/accounts/image")
                .excludePathPatterns("/api/advertisements")
                .excludePathPatterns("/api/products")
                .order(1);
    }

}
