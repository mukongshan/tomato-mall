package com.example.tomatomall.service;

import com.example.tomatomall.vo.AccountCouponsRelationVO;
import com.example.tomatomall.vo.CouponVO;
import java.util.List;

public interface CouponService {
    List<CouponVO> getAllCoupons();
    List<AccountCouponsRelationVO> getCouponsForAccount(Integer accountId);
    CouponVO getCouponVOById(int couponId);
    String createCoupon(CouponVO couponVO);
    String userReceiveCoupon(AccountCouponsRelationVO accountCouponsRelationVO);
    String userUseCoupon(AccountCouponsRelationVO accountCouponsRelationVO);
    String deleteCoupon(int couponId);
    String updateCoupon(int id, CouponVO couponVO);
}