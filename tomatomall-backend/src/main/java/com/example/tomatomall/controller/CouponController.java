package com.example.tomatomall.controller;

import com.example.tomatomall.service.CouponService;
import com.example.tomatomall.vo.CouponVO;
import com.example.tomatomall.vo.Response;
import com.example.tomatomall.vo.AccountCouponsRelationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    @Autowired
    private CouponService couponService;

    /**
     * 获取所有优惠券
     */
    @GetMapping("/all")
    public Response<List<CouponVO>> getAllCoupons() {
        return Response.buildSuccess(couponService.getAllCoupons());
    }

    /**
     * 根据ID获取优惠券
     */
    @GetMapping("/{id}")
    public Response<CouponVO> getCouponById(@PathVariable int id) {
        return Response.buildSuccess(couponService.getCouponVOById(id));
    }

    /**
     * 创建优惠券
     */
    @PostMapping()
    public Response<String> createCoupon(@RequestBody CouponVO couponVO) {
        return Response.buildSuccess(couponService.createCoupon(couponVO));
    }

    /**
     * 更新优惠券
     */
    @PutMapping("/{id}")
    public Response<String> updateCoupon(@PathVariable int id, @RequestBody CouponVO couponVO) {
        return Response.buildSuccess(couponService.updateCoupon(id, couponVO));
    }

    /**
     * 删除优惠券
     */
    @DeleteMapping("/{id}")
    public Response<String> deleteCoupon(@PathVariable int id) {
        return Response.buildSuccess(couponService.deleteCoupon(id));
    }

    /**
     * 使用优惠券
     */
    @PostMapping("/use")
    public Response<String> useCoupon(@RequestBody  AccountCouponsRelationVO accountCouponsRelationVO) {
        return Response.buildSuccess(couponService.userUseCoupon(accountCouponsRelationVO));
    }

    /**
     * 获取用户拥有的优惠券
     */
    @GetMapping("/account/{accountId}")
    public Response<List<AccountCouponsRelationVO>> getCouponsByAccountId(@PathVariable int accountId) {
        return Response.buildSuccess(couponService.getCouponsForAccount(accountId));
    }

    /**
     * 用户领取优惠券
     */
    @PostMapping("/receive")
    public Response<String> userReceiveCoupon(@RequestBody  AccountCouponsRelationVO accountCouponsRelationVO) {
        return Response.buildSuccess(couponService.userReceiveCoupon(accountCouponsRelationVO));
    }

}