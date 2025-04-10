import { CART_MODULE } from "./_prefix"
import { axios } from "../utils/request"

export interface CartItem {
    cartItemId: number,
    accountId: number,
    productId: number,
    quantity: number,
}

//添加商品至购物车
export const addProductToCart = async (productId: number, quantity: number) => {
    return await axios.post(`${CART_MODULE}`, {productId, amount},
        {headers: { "Content-Type": "application/json" },
    })
}

//修改购物车商品数量
export const updateCartItem = async (cartItemId: number, quantity: number)=> {
    return await axios.patch(`${CART_MODULE}/${cartItemId}`, quantity,
        {headers: { "Content-Type": "application/json" },
        })
}

//获取购物车商品列表
export const getCartProducts = async () => {
    return await axios.get(`${CART_MODULE}`,
        {headers: { "Content-Type": "application/json" }})
}