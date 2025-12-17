import { axios } from "@/utils/request";
import { SHOP_MODULE } from "./_prefix";

export interface Shop {
    shopId: number;
    name: string;
    ownerId: number;
    iconUrl: string;
    description: string;
    rate: number;
    isValid: number;
}

export const getShopList = async () => {
    return await axios.get(`${SHOP_MODULE}/all`);
};

export const createShop = async (shop: Shop) => {
    return await axios.post(`${SHOP_MODULE}/create`, shop, {
        headers: { "Content-Type": "application/json" },
    });
};

export const getShopDetail = async (shopId: number) => {
    return await axios.get(`${SHOP_MODULE}/detail/${shopId}`);
}

export const updateShop = async (shop: Shop) => {
    return await axios.put(`${SHOP_MODULE}/update/${shop.shopId}`, shop, {
        headers: { "Content-Type": "application/json" },
    });
};
export const deleteShop = async (shopId: number) => {
    return await axios.delete(`${SHOP_MODULE}/delete/${shopId}`);
};

// 根据ownerId获取对应的店铺
export const getShopIdByOwnerId = async (ownerId: number) => {
    return await axios.get(`${SHOP_MODULE}/owner/${ownerId}`);
};