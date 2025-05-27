import { imageUpload } from "@/api/image";
import { ElMessage, UploadRawFile } from "element-plus";

export const imageProcess = async (uploadFile: UploadRawFile) => {
    const formData = new FormData();
    formData.append("file", uploadFile);
    try {
        const res = await imageUpload(formData);
        ElMessage.success("上传成功！");
        return res.data.data as string;
    } catch (error) {
        console.error(error);
        return "";
    }
}
