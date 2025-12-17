// 优惠券相关API
import { COUPON_MODULE } from "./_prefix";
import { axios } from "../utils/request";

export interface CouponVO {
    id?: number;
    name: string;
    description: string;
    discountType: number; // 1: 百分比折扣, 2: 固定金额折扣
    discountValue: number;
    startTime: string; // 推荐用 ISO 字符串与后端兼容
    endTime: string;
    quantity: number;
    usedQuantity: number;
    isValid: number;
}

export interface AccountCouponsRelationVO {
    id?: number;
    accountId: number;
    couponId: number;
    quantity: number;
}

// 获取所有优惠券
export const getAllCoupons = () => {
    return axios.get(`${COUPON_MODULE}/all`);
};

// 根据ID获取优惠券
export const getCouponById = (id: number) => {
    return axios.get(`${COUPON_MODULE}/${id}`);
};

// 创建优惠券
export const createCoupon = (couponVO: CouponVO) => {
    return axios.post(`${COUPON_MODULE}`, couponVO,
        { headers: { 'Content-Type': 'application/json' } });
}

// 更新优惠券
export const updateCoupon = (id: number, couponVO: any) => {
    return axios.put(`${COUPON_MODULE}/${id}`, couponVO,
        { headers: { 'Content-Type': 'application/json' } });
};

// 删除优惠券
export const deleteCoupon = (id: number) => {
    return axios.delete(`${COUPON_MODULE}/${id}`);
};

// 使用优惠券
export const useCoupon = (accountCouponsRelationVO: AccountCouponsRelationVO) => {
    return axios.post(`${COUPON_MODULE}/use`, accountCouponsRelationVO,
        { headers: { 'Content-Type': 'application/json' } });
};

// 获取用户拥有的优惠券
export const getCouponsByAccountId = (accountId: number) => {
    return axios.get(`${COUPON_MODULE}/account/${accountId}`);
};

// 用户领取优惠券
export const userReceiveCoupon = (accountCouponsRelationVO: any) => {
    return axios.post(`${COUPON_MODULE}/receive`, accountCouponsRelationVO,
        { headers: { 'Content-Type': 'application/json' } });
};