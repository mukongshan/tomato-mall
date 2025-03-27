package com.example.tomatomall.controller;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Resource
    private AccountService accountService;

    @Autowired
    TokenUtil tokenUtil;

    /**
     * 获取用户详情（需要 token）
     */
    @GetMapping("/{username}")
    public Response<AccountVO> getAccountInfo(@RequestHeader(value = "token", required = false) String token, @PathVariable String username) {

        // 1. 先检查 token 是否为空
        if (token == null || token.isEmpty()) {
            return Response.buildFailure("未授权", "401");
        }

        // 2. 验证 token 是否有效
        if (!tokenUtil.verifyToken(token)) {
            return Response.buildFailure("Token 无效", "401");
        }

        // 3. 从 token 中获取当前登录的用户
        Account currentUser = tokenUtil.getAccount(token);
        if (currentUser == null) {
            return Response.buildFailure("用户不存在", "404");
        }

        // 4. 确保当前用户只能查询自己的信息，或有管理员权限
        if (!currentUser.getUsername().equals(username) && !currentUser.getRole().equals("ADMINISTRATOR")) {
            return Response.buildFailure("无权限访问", "403");
        }

        // 5. 通过用户名查询目标用户的信息
        return Response.buildSuccess(accountService.getAccountInfo());
    }


    /**
     * 创建用户（注册）
     */
    @PostMapping()
    public Response<String> createAccount(@RequestBody AccountVO accountVO) {
        System.out.println("createAccount");
        return Response.buildSuccess(accountService.createAccount(accountVO));
    }

    /**
     * 更新用户信息（需要 token）
     */
    @PutMapping()
    public Response<Boolean> updateAccount(@RequestHeader(value = "token", required = false) String token, @RequestBody AccountVO accountVO) {

        // 1. 先检查 token 是否为空
        if (token == null || token.isEmpty()) {
            return Response.buildFailure("未授权", "401");
        }

        // 2. 验证 token 是否有效
        if (!tokenUtil.verifyToken(token)) {
            return Response.buildFailure("Token 无效", "401");
        }

        // 3. 从 token 中获取当前登录的用户
        Account currentUser = tokenUtil.getAccount(token);
        if (currentUser == null) {
            return Response.buildFailure("用户不存在", "404");
        }
        return Response.buildSuccess(accountService.updateAccount(accountVO));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Response<String> login(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.login(accountVO));
    }
}