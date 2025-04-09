import {PRODUCT_MODULE} from "./_prefix.js";
import {axios} from "../utils/request";
// 商品属性
export interface Product {
    id: number;
    title: string;
    price: number;
    rate: number;
    description: string;
    cover: string;
    detail: string;
    specifications: Specification[];
}
// 商品规格说明
export interface Specification {
    id: string; // 规格ID
    productId: string; // 商品ID
    item: string; // 规格名称
    value: string; // 规格内容
}
// 商品库存表
export interface Stockpile {
    id: number; // 库存ID
    productId: number; // 商品ID
    amount: number; // 库存数
    frozen: number;// 不可卖的商品数
}
// 获取商品列表
export const getProductsList = async () =>{
    return await axios.get(`${PRODUCT_MODULE}`);
}
// 通过商品ID获取单个商品
export const getProduct = async (productId: number)=>{
    return await axios.get(`${PRODUCT_MODULE}/${productId}`);
}
// 更新商品
export const updateProduct = async (product: Product)=>{
    return await axios.put(`${PRODUCT_MODULE}`, product,
        {headers: {'Content-Type': 'application/json'}});
}
// 添加商品
export const addProduct = async (product: Omit<Product,"id">)=>{
    return await axios.post(`${PRODUCT_MODULE}`, product,
        {headers: {'Content-Type': 'application/json'}});
}
// 删除商品
export const deleteProduct = async (productId: number)=>{
    return await axios.delete(`${PRODUCT_MODULE}/${productId}` );
}
// 调整商品库存
export const updateStockpile = async (productId: number, amount: number)=>{
    return await axios.patch(`${PRODUCT_MODULE}/stockpile/${productId}`, {amount: amount },
        {headers: {'Content-Type': 'application/json'}}
    );
}
// 获取商品库存
export const getStockpile = async (productId: number)=>{
    return await axios.get(`${PRODUCT_MODULE}/stockpile/${productId}`,
        { headers: { 'Content-Type': 'application/json' } });
}
//PATCH方法用于资源的部分内容的更新；
//PUT用于更新某个较为完整的资源