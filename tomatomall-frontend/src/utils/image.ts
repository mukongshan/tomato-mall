import { IMAGE_MODULE} from "@/api/_prefix"
import { axios } from "../utils/request"

// 上传图片 返回图片URL
export const uploadImg = async (image: FormData) => {
    return await axios.post(`${IMAGE_MODULE}`, image, {
        headers: { "Content-Type": "multipart/form-data" },
    })
}