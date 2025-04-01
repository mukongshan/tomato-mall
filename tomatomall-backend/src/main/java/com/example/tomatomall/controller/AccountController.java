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
    @PostMapping("")
    public Response<String> createAccount(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.createAccount(accountVO));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Response<String> login(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.login(accountVO));
    }

    /**
     * 更新用户信息（需要 token）
     */
    @PutMapping("")
    public Response<String> updateAccount(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.updateAccount(accountVO));
    }

    @PostMapping("/image")
    public Response<String> upload(@RequestParam MultipartFile file){
        return Response.buildSuccess(accountService.uploadImg(file));
    }
}
