import { CART_MODULE, ORDER_MODULE } from "./_prefix"
import { axios } from "../utils/request"

export interface OrderInfo {
    name: string
    location: string
    telephone: string
    email: string
}

export interface Order {
    orderId: number;
    accountId: number;
    totalAmount: number;
    paymentMethod: string;
    status: string;
    createTime: string;
}

export interface OrderItem {
    orderId: number;
    productId: number;
    quantity: number;
    price: number;
}

//提交订单
export const checkout = async (cartItemIds: number[], orderInfo: OrderInfo, paymentMethod: string, couponType: number, couponValue: number) => {
    return await axios.post(`${CART_MODULE}/checkout`, { cartItemIds, orderInfo, paymentMethod },
        {
            headers: {
                "Content-Type": "application/json"
            },
            params: {
                couponType,
                couponValue
            }
        })
}
//发起支付
export const payOrder = async (orderId: number) => {
    return await axios.post(`${ORDER_MODULE}/${orderId}/pay`, { orderId: orderId },
        { headers: { "Content-Type": "application/json" } })
}
//获取订单详情
export const getOrderItems = async (orderId: number) => {
    return await axios.get(`${ORDER_MODULE}/${orderId}`);
}
//获取用户所有订单
export const getOrder = async (accountId: number) => {
    return await axios.get(`${ORDER_MODULE}/account/${accountId}`);
}

// 取消订单
export const cancelOrder = async (orderId: number) => {
    return await axios.post(`${ORDER_MODULE}/cancel/${orderId}`, { orderId: orderId },
        { headers: { "Content-Type": "application/json" } })
}