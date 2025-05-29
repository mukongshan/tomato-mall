<script setup lang="ts">
import { ref } from 'vue';
import { componentSizes, ElMessage } from 'element-plus';
import { Shop, createShop } from '@/api/shop';
import { UploadFile, UploadProps, ElLoading } from 'element-plus';
import { imageProcess } from "@/utils/UploadImage.ts";
import router from "../router/index.ts";
import { Plus } from "@element-plus/icons-vue";
import { getAdmin } from '@/api/account.ts';
import { Message, sendMessage } from '@/api/message.ts';

// 表单数据（根据你的接口调整）
const form = ref<Shop>({
    shopId: 0,
    name: '',
    description: '',
    ownerId: 0,
    iconUrl: '',
    rate: 0,
    isValid: 0,
});


// 图片验证
const beforeLogoUpload: UploadProps['beforeUpload'] = (rawFile) => {
    const isImage = ['image/jpg', 'image/png'].includes(rawFile.type)
    const isSizeValid = rawFile.size <= 5 * 1024 * 1024

    if (!isImage) {
        ElMessage.error('Logo必须为JPG/PNG格式')
        return false
    }
    if (!isSizeValid) {
        ElMessage.error('Logo大小不能超过5MB')
        return false
    }
    return true
}
// 图片上传处理
const handleAvatarChange: UploadProps['onChange'] = async (uploadFile: UploadFile) => {
    if (uploadFile.raw) {
        // 生成预览URL
        form.value.iconUrl = await imageProcess(uploadFile.raw)
        console.log(form.value.iconUrl)
    }
}

// 删除头像
const handleAvatarRemove = () => {
    form.value.iconUrl = ''
}

const handleCreate = async () => {
    if (!form.value) return
    form.value.ownerId = Number(sessionStorage.getItem('id'))
    const loading = ElLoading.service({
        lock: true,
        text: '创建中...'
    })
    try {
        const response = await createShop(form.value);
        const adminId = (await getAdmin()).data.data;
        const message: Message = {
            id: 0,
            content: "NEW_STORE_APPLICATION",
            isRead: false,
            fromUser: adminId,
            toUser: form.value.ownerId,
            createdTime: new Date().toISOString()
        };
        await sendMessage(message);
        console.log(response)
        if (response.data.code === '200') {
            ElMessage.success("创建成功 请等待审核")
            await router.push("/")
        } else {
            ElMessage.warning(response.data.data);
        }
    } catch (error: any) {
        ElMessage.warning(error)
    } finally {
        loading.close()
    }
}
</script>

<template>
    <div class="form-container">
        <el-form class="centered-form" ref="formRef" :model="form" :size=componentSizes[3] label-width="auto"
            status-icon>
            <h2 class="form-title">创建新商店</h2>
            <!--prop是和验证规则绑定的 这里没有验证规则 故而没有-->
            <el-form-item label="商店名称">
                <el-input v-model="form.name" />
            </el-form-item>

            <el-form-item label="商店描述">
                <el-input v-model="form.description" />
            </el-form-item>

            <el-form-item label="商店图标" prop="iconUrl">
                <!-- 只接受图片
             关闭自动上传-->
                <!-- on-change 图片上传触发 这里是转为url -->
                <!-- on-remove 删除 -->
                <!-- before-upload 上传前验证 -->
                <el-upload class="avatar-uploader" :show-file-list="false" :on-change="handleAvatarChange"
                    :on-remove="handleAvatarRemove" :before-upload="beforeLogoUpload" accept="image/*"
                    :auto-upload="false">
                    <!--图片上传模式-->
                    <!-- 有图标是显示预览图-->
                    <el-image v-if="form.iconUrl" :src="form.iconUrl" class="avatar" fit="cover" />
                    <!-- 无图像时候显示上传图标-->
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
                <div class="el-upload__tip">
                    支持JPG/PNG格式，且不超过5MB
                </div>
            </el-form-item>

            <el-form-item class="button-group">
                <el-button plain type="primary" @click="handleCreate">
                    创建商店
                </el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<style scoped>
.form-container {
    display: grid;
    place-items: center;
    padding: 20px;
    width: 100%;
}

.centered-form {
    width: 80%;
    max-width: 600px;
    border-radius: 8px;
    margin: 40px auto;
}

.avatar-uploader {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;

    cursor: pointer;

    position: relative;

    overflow: hidden;

    width: 150px;

    height: 150px;

    display: flex;

    align-items: center;

    justify-content: center;

    transition: var(--el-transition-duration-fast);

}

.avatar-uploader:hover {
    border-color: var(--el-color-primary);

}

.avatar {
    width: 100%;

    height: 100%;

    object-fit: cover;

}

.avatar-uploader-icon {
    font-size: 28px;

    color: #8c939d;

    text-align: center;

}

.el-upload__tip {
    margin-top: 8px;

    font-size: 12px;

    color: var(--el-text-color-secondary);

}
</style>