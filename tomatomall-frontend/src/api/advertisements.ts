import { axios } from "../utils/request"
import { AD_MODULE } from './_prefix'

export interface Advertisement {
    
    title: string
    content: string
    image_url: string
    product_id: number
}
export interface AdvertisementUpdate extends Advertisement {
    id: number
}

export const getAdvertisements = async() => {
    return await axios.get(`${AD_MODULE}`,
        { headers: { "Content-Type": "application/json" } })
}

export const updateAdvertisement = async (ad: AdvertisementUpdate) => {
    return await axios.patch(`${AD_MODULE}`, ad,
        { headers: { "Content-Type": "application/json" } })
}
// 添加时不带id 后端自动生成
export const addAdvertisement = async (ad: Advertisement) => {
    return await axios.post(`${AD_MODULE}`, ad,
        { headers: { "Content-Type": "application/json" } })
}
// 根据id删除广告
export const deleteAdvertisement = async (id: number) => {
    return await axios.delete(`${AD_MODULE}/${id}`,
        { headers: { "Content-Type": "application/json" } })
}