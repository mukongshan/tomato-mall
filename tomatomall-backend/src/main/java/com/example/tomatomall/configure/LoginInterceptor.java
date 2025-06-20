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

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        System.out.println("访问：" + request.getMethod() + " " + request.getRequestURI());

        // 允许注册请求POST /api/accounts
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/api/accounts".equals(request.getRequestURI())) {
            return true;
        }
        // 允许上传图片
        else if("POST".equalsIgnoreCase(request.getMethod()) && "/api/image".equals(request.getRequestURI())) {
            return true;
        }

        // 验证Token
        String token = request.getHeader("token");
        if (token != null && tokenUtil.verifyToken(token)) {
            Account account = tokenUtil.getAccount(token);
            request.getSession().setAttribute("currentAccount", account);
            return true;
        } else {
            throw TomatoMallException.notLogin();
        }
    }
}
