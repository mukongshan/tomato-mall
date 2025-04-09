import { ACCOUNT_MODULE } from "./_prefix"
import { axios } from "../utils/request"

// 上传图片 返回图片URL
export const imageUpload = async (image: FormData) => {
    return await axios.post(`${ACCOUNT_MODULE}/image`, image, {
        headers: { "Content-Type": "multipart/form-data" },
    })
}