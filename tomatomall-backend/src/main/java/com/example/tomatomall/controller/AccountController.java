package com.example.tomatomall.controller;

import com.example.tomatomall.po.Account;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public Response<AccountVO> getAccountInfo(@PathVariable String username) {
        return Response.buildSuccess(accountService.getAccountInfo(username));
    }

    /**
     * 创建用户（注册）
     */
<<<<<<< HEAD:TomatoMall/src/main/java/com/example/tomatomall/controller/AccountController.java
    @PostMapping()
=======
    @PostMapping("")
>>>>>>> 1212401 (实现了修改密码跳转到登录界面):tomatomall-backend/src/main/java/com/example/tomatomall/controller/AccountController.java
    public Response<String> createAccount(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.createAccount(accountVO));
    }

    /**
<<<<<<< HEAD:TomatoMall/src/main/java/com/example/tomatomall/controller/AccountController.java
=======
     * 更新用户信息（需要 token）
     */
    @PutMapping("")
    public Response<String> updateAccount(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.updateAccount(accountVO));
    }

    /**
>>>>>>> 1212401 (实现了修改密码跳转到登录界面):tomatomall-backend/src/main/java/com/example/tomatomall/controller/AccountController.java
     * 用户登录
     */
    @PostMapping("/login")
    public Response<String> login(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.login(accountVO));
    }

    /**
     * 更新用户信息（需要 token）
     */
    @PutMapping()
    public Response<String> updateAccount(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.updateAccount(accountVO));
    }

    @PostMapping("/image")
    public Response<String> upload(@RequestParam MultipartFile file){
        return Response.buildSuccess(accountService.uploadImg(file));
    }
}