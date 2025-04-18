import {CART_MODULE, ORDER_MODULE} from "./_prefix"
import { axios } from "../utils/request"

export interface OrderInfo {
    name: string
    location: string
    telephone: string
    email: string
}

//提交订单
export const checkout = async (cartItemIds: number[], orderInfo: OrderInfo, paymentMethod: string) => {
    return await axios.post(`${CART_MODULE}/checkout`, {cartItemIds, orderInfo, paymentMethod},
        {headers: { "Content-Type": "application/json" }})
}
//发起支付
export const payOrder = async (orderId: number) => {
    return await axios.post(`${ORDER_MODULE}/${orderId}/pay`, {orderId: orderId},
        {headers: { "Content-Type": "application/json" }})
}
