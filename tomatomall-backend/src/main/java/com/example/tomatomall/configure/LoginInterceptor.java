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
<<<<<<< HEAD:TomatoMall/src/main/java/com/example/tomatomall/configure/LoginInterceptor.java
        //对创建用户的请求放行
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if ("/api/accounts".equals(uri) && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        //对其余请求鉴权
=======
        // 允许注册请求（POST /api/accounts）不检查 token
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/api/accounts".equals(request.getRequestURI())) {
            return true;
        }
>>>>>>> 1212401 (实现了修改密码跳转到登录界面):tomatomall-backend/src/main/java/com/example/tomatomall/configure/LoginInterceptor.java
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
