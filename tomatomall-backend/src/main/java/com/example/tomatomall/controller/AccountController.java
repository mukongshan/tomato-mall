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

/**
 * 用户账户管理控制器
 * 提供用户注册、登录、信息管理等功能
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Resource
    private AccountService accountService;

    @Autowired
    TokenUtil tokenUtil;

    /**
     * 获取用户详情（需要 token）
     * 
     * @param username 用户名
     * @return 用户详细信息
     */
    @GetMapping("/{username}")
    public Response<AccountVO> getAccountInfo(@PathVariable String username) {
        return Response.buildSuccess(accountService.getAccountInfo(username));
    }

    /**
     * 创建用户（注册）
     * 
     * @param accountVO 用户注册信息
     * @return 注册结果
     */
    @PostMapping()
    public Response<String> createAccount(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.createAccount(accountVO));
    }

    /**
     * 用户登录
     * 
     * @param accountVO 用户登录信息
     * @return 登录结果（包含token）
     */
    @PostMapping("/login")
    public Response<String> login(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.login(accountVO));
    }

    /**
     * 更新用户信息（需要 token）
     * 
     * @param accountVO 更新的用户信息
     * @return 更新结果
     */
    @PutMapping()
    public Response<String> updateAccount(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.updateAccount(accountVO));
    }

    /**
     * 更新用户信息（不需要密码）（需要 token）
     * 
     * @param accountVO 更新的用户信息（不包含密码）
     * @return 更新结果
     */
    @PutMapping("/info")
    public Response<String> updateAccountWithoutPassword(@RequestBody AccountVO accountVO) {
        return Response.buildSuccess(accountService.updateAccountWithoutPassword(accountVO));
    }

    /**
     * 根据id更新用户身份
     * 
     * @param id 用户ID
     * @param role 新的角色
     * @return 更新结果
     */
    @PutMapping("/role/{id}")
    public Response<String> updateRole(@PathVariable Integer id, @RequestBody String role) {
        return Response.buildSuccess(accountService.updateRole(id, role.substring(1, role.length()-1)));
    }
    
    /**
     * 根据id获取用户role
     * 
     * @param id 用户ID
     * @return 用户角色
     */
    @GetMapping("/role/{id}")
    public Response<String> getRole(@PathVariable Integer id) {
        return Response.buildSuccess(accountService.getRole(id));
    }

    /**
     * 根据shopId获取店铺员工
     * 
     * @param shopId 店铺ID
     * @return 店铺员工列表
     */
    @GetMapping("/shop/{shopId}")
    public Response<List<AccountVO>> getShopStaff(@PathVariable Integer shopId) {
        return Response.buildSuccess(accountService.getShopStaff(shopId));
    }

    /**
     * 上传用户头像
     * 
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping("/image")
    public Response<String> upload(@RequestParam MultipartFile file){
        return Response.buildSuccess(accountService.uploadImg(file));
    }

    /**
     * 获取管理员ID
     * 
     * @return 管理员ID
     */
    @GetMapping("/fetchAdmin")
    public Response<Integer> fetchAdminId() {
        return Response.buildSuccess(accountService.fetchAdminId());
    }

}