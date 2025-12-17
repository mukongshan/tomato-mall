package com.example.tomatomall.configure;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 在SpringBoot过滤器中配置跨域，
 * 跨域配置不能和拦截器写一起，
 * 会造成冲突，
 * 需要在过滤器中配置跨域，
 * 过滤器在拦截器前进行。
 * 在“Access-Control-Allow-Headers”中，
 * 需要添加上token，
 * 因为前端要传输token到后端，不能过滤掉。
*/
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 设置过滤器优先级为最高，确保在其他过滤器之前执行
@WebFilter("/*") // 配置过滤器拦截所有请求
public class CorsFilter implements Filter {

    /**
     * 过滤器核心方法，处理跨域请求
     * @param req 请求对象
     * @param res 响应对象
     * @param chain 过滤器链
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        // 将ServletResponse转换为HttpServletResponse，以便设置响应头
        HttpServletResponse response = (HttpServletResponse) res;
        
        // 设置允许所有域名访问，解决跨域问题
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        // 设置允许的HTTP方法
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, PATCH");
        
        // 设置允许的请求头，包括token（用于身份验证）
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, token");
        
        // 设置预检请求的缓存时间，单位为秒
        response.setHeader("Access-Control-Max-Age", "3600");

        // 处理预检请求（OPTIONS请求）
        if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) req).getMethod())){
            // 对于OPTIONS请求，直接返回200状态码，不继续执行后续过滤器
            response.setStatus(200);
        } else {
            // 对于非OPTIONS请求，继续执行过滤器链
            chain.doFilter(req, res);
        }
    }

    /**
     * 过滤器初始化方法
     * @param filterConfig 过滤器配置对象
     */
    @Override
    public void init(FilterConfig filterConfig) {
        // 初始化时不需要特殊处理
    }

}
