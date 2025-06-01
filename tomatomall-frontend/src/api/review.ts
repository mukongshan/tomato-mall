import { axios } from "@/utils/request";

export interface Review {
    id: number;
    accountId: number;
    content: string;
    rate: number;
    type: reviewType;
    productId: number;
    shopId: number;
    createdAt: string;
}

export type reviewType = 'PRODUCT' | 'SHOP';


export const addProductReview = async (review: Review) => {
    return await axios.post(`/api/reviews/product`, review, {
        headers: { "Content-Type": "application/json" },
    });
}

export const addShopReview = async (review: Review) => {
    return await axios.post(`/api/reviews/shop`, review, {
        headers: { "Content-Type": "application/json" },
    });
}

export const getProductReviews = async (productId: number) => {
    return await axios.get(`/api/reviews/product/${productId}`);
}

export const getShopReviews = async (shopId: number) => {
    return await axios.get(`/api/reviews/shop/${shopId}`);
}

export const deleteReview = async (reviewId: number) => {
    return await axios.delete(`/api/reviews/${reviewId}`);
}