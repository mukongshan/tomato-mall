import {PRODUCT_MODULE} from "./_prefix.js";
import {axios} from "../utils/request";

export interface Product {
    id: number;
    title: string;
    price: number;
    rate: number;
    description: string;
    cover: string;
    details: string;
    specification: Specification[];
}

export interface Specification {
    item: string;
    value: string;
}

export interface Stockpile {
    id: number;
    productId: number;
    amount: number;
    frozen: number;
}

export const getProductsList = async () =>{
    return await axios.get(`${PRODUCT_MODULE}`,
        {headers:{'Content-Type':'application/json'}});
}

export const getProduct = async (productId: number)=>{
    return await axios.get(`${PRODUCT_MODULE}/${productId}`,
        {headers: {'Content-Type': 'application/json'}});
}

export const updateProduct = async (product: Product)=>{
    return await axios.put(`${PRODUCT_MODULE}`, product,
        {headers: {'Content-Type': 'application/json'}});
}

export const addProduct = async (product: Product)=>{
    return await axios.post(`${PRODUCT_MODULE}`, product,
        {headers: {'Content-Type': 'application/json'}});
}

export const deleteProduct = async (productId: number)=>{
    return await axios.delete(`${PRODUCT_MODULE}/stockpile/${productId}`,
        {headers: {'Content-Type': 'application/json'}});
}

export const updateStockpile = async (productId: number, amount: number)=>{
    return await axios.patch(`${PRODUCT_MODULE}/stockpile/${productId}`, amount,
        {headers: {'Content-Type': 'application/json'}});
}

export const getStockpile = async (productId: number)=>{
    return await axios.get(`${PRODUCT_MODULE}/stockpile/${productId}`,
        {headers: {'Content-Type': 'application/json'}});
}