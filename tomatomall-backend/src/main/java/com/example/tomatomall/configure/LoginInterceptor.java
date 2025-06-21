package com.example.tomatomall.configure;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 这个类定制了一个登录的拦截器，
 * SpringBoot的拦截器标准为HandlerInterceptor接口，
 * 这个类实现了这个接口，表示是SpringBoot标准下的，
 * 在preHandle方法中，通过获取请求头Header中的token，
 * 判断了token是否合法，如果不合法则抛异常，
 * 合法则将用户信息存储到request的session中。
*/
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    TokenUtil tokenUtil;

    /**
     * 请求预处理方法，在Controller方法执行前调用
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象
     * @return true表示继续执行，false表示中断请求
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 打印当前访问的请求方法和URI路径，用于调试
        System.out.println("访问：" + request.getMethod() + " " + request.getRequestURI());

        // 允许注册请求POST /api/accounts
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/api/accounts".equals(request.getRequestURI())) {
            return true;
        }
        // 允许上传图片请求通过，不需要验证token
        // 检查请求方法是否为POST且路径为/api/image（图片上传接口）
        else if("POST".equalsIgnoreCase(request.getMethod()) && "/api/image".equals(request.getRequestURI())) {
            return true;
        }

        // 对于其他请求，需要验证token
        // 从请求头中获取token
        String token = request.getHeader("token");
        
        // 验证token是否存在且有效
        if (token != null && tokenUtil.verifyToken(token)) {
            // token有效，获取用户信息
            Account account = tokenUtil.getAccount(token);
            // 将用户信息存储到session中，供后续使用
            request.getSession().setAttribute("currentAccount", account);
            return true;
        } else {
            // token无效或不存在，抛出未登录异常
            throw TomatoMallException.notLogin();
        }
    }
}
