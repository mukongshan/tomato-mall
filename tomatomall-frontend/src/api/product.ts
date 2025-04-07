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

export const getProductsList = async () =>{
    return await axios.get(`${PRODUCT_MODULE}`,
        {headers:{'Content-Type':'application/json'}});
}

export const getProduct = async (id: number)=>{
    return await axios.get(`${PRODUCT_MODULE}/${id}`,
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

export const deleteProduct = async (id: number)=>{
    return await axios.delete(`${PRODUCT_MODULE}/${id}`,
        {headers: {'Content-Type': 'application/json'}});
}
