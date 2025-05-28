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
import java.util.List;


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
    @PostMapping()
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
    @PutMapping()
    public Response<String> updateAccount(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.updateAccount(accountVO));
    }

    /**
     * 更新用户信息（不需要密码）（需要 token）
     */
    @PutMapping("/info")
    public Response<String> updateAccountWithoutPassword(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.updateAccountWithoutPassword(accountVO));
    }

    // 根据id更新用户身份
    @PutMapping("/role/{id}")
    public Response<String> updateRole(@PathVariable Integer id, @RequestBody String role) {
        return Response.buildSuccess(accountService.updateRole(id, role.substring(1, role.length()-1)));
    }
    // 根据id获取用户role
    @GetMapping("/role/{id}")
    public Response<String> getRole(@PathVariable Integer id) {
        return Response.buildSuccess(accountService.getRole(id));
    }

    // 根据shopId获取店铺员工
    @GetMapping("/shop/{shopId}")
    public Response<List<AccountVO>> getShopStaff(@PathVariable Integer shopId) {
        return Response.buildSuccess(accountService.getShopStaff(shopId));
    }


    @PostMapping("/image")
    public Response<String> upload(@RequestParam MultipartFile file){
        return Response.buildSuccess(accountService.uploadImg(file));
    }

}