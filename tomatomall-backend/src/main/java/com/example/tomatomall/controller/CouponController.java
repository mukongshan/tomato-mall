package com.example.tomatomall.controller;

import com.example.tomatomall.service.CouponService;
import com.example.tomatomall.vo.CouponVO;
import com.example.tomatomall.vo.Response;
import com.example.tomatomall.vo.AccountCouponsRelationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券管理控制器
 * 提供优惠券的增删改查、领取、使用等功能
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    @Autowired
    private CouponService couponService;

    /**
     * 获取所有优惠券
     * 
     * @return 所有优惠券列表
     */
    @GetMapping("/all")
    public Response<List<CouponVO>> getAllCoupons() {
        return Response.buildSuccess(couponService.getAllCoupons());
    }

    /**
     * 根据ID获取优惠券
     * 
     * @param id 优惠券ID
     * @return 优惠券详细信息
     */
    @GetMapping("/{id}")
    public Response<CouponVO> getCouponById(@PathVariable int id) {
        return Response.buildSuccess(couponService.getCouponVOById(id));
    }

    /**
     * 创建优惠券
     * 
     * @param couponVO 优惠券信息
     * @return 创建结果
     */
    @PostMapping()
    public Response<String> createCoupon(@RequestBody CouponVO couponVO) {
        return Response.buildSuccess(couponService.createCoupon(couponVO));
    }

    /**
     * 更新优惠券
     * 
     * @param id 优惠券ID
     * @param couponVO 更新的优惠券信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Response<String> updateCoupon(@PathVariable int id, @RequestBody CouponVO couponVO) {
        return Response.buildSuccess(couponService.updateCoupon(id, couponVO));
    }

    /**
     * 删除优惠券
     * 
     * @param id 优惠券ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Response<String> deleteCoupon(@PathVariable int id) {
        return Response.buildSuccess(couponService.deleteCoupon(id));
    }

    /**
     * 使用优惠券
     * 
     * @param accountCouponsRelationVO 用户优惠券关系信息
     * @return 使用结果
     */
    @PostMapping("/use")
    public Response<String> useCoupon(@RequestBody  AccountCouponsRelationVO accountCouponsRelationVO) {
        return Response.buildSuccess(couponService.userUseCoupon(accountCouponsRelationVO));
    }

    /**
     * 获取用户拥有的优惠券
     * 
     * @param accountId 用户ID
     * @return 用户优惠券列表
     */
    @GetMapping("/account/{accountId}")
    public Response<List<AccountCouponsRelationVO>> getCouponsByAccountId(@PathVariable int accountId) {
        return Response.buildSuccess(couponService.getCouponsForAccount(accountId));
    }

    /**
     * 用户领取优惠券
     * 
     * @param accountCouponsRelationVO 用户优惠券关系信息
     * @return 领取结果
     */
    @PostMapping("/receive")
    public Response<String> userReceiveCoupon(@RequestBody  AccountCouponsRelationVO accountCouponsRelationVO) {
        return Response.buildSuccess(couponService.userReceiveCoupon(accountCouponsRelationVO));
    }

}