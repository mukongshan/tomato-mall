<script setup lang="ts">
import { ref, onMounted } from 'vue';
import {
    getAdvertisements,
    addAdvertisement,
    updateAdvertisement as updateAdAPI,
    deleteAdvertisement as deleteAdAPI,
    Advertisement,
    AdvertisementUpdate
} from '@/api/advertisements';
import {
    ElMessage,
    ElMessageBox,
    ElLoading,
    UploadFile,
    UploadProps
} from 'element-plus';
import router from '@/router/index';

import { Edit, Delete, Plus, Picture } from '@element-plus/icons-vue';
import { uploadImg } from "@/utils/image.ts";
import { getProductList } from '@/api/product';

// 当前广告列表
const advertisementList = ref<AdvertisementUpdate[]>([]);

// 统一表单数据  新建无id 编辑有id
const editForm = ref<AdvertisementUpdate>({
    id: 0,
    title: '',
    content: '',
    imgUrl: '',
    productId: 0
});

// 当前操作模式 ('create' | 'edit')
const formMode = ref<'create' | 'edit'>('create');
const showFormModal = ref(false);
const loading = ref(false);

// 点击跳转到商品详情页
const gotoDetails = (productId: number) => {
    router.push({ path: `/product/${productId}` });
};

// 加载广告列表
const loadAdvertisements = async () => {
    try {
        loading.value = true;
        const response = await getAdvertisements();
        advertisementList.value = response.data.data;
        console.log(advertisementList.value)
    } catch (error) {
        console.error('加载广告失败:', error);
        ElMessage.error({
            message: '加载广告失败，请稍后重试',
            duration: 1000  // 修改这里
        });
    } finally {
        loading.value = false;
    }
};

// 初始化加载数据
onMounted(() => {
    loadAdvertisements();
});

// 打开创建广告模态框
const openCreateModal = () => {
    resetFormData();
    formMode.value = 'create';
    showFormModal.value = true;
};

// 打开编辑广告模态框
const openEditModal = (ad: AdvertisementUpdate) => {
    editForm.value = { ...ad };
    formMode.value = 'edit';
    showFormModal.value = true;
};

// 重置表单数据
const resetFormData = () => {
    editForm.value = {
        id: 0,
        title: '',
        content: '',
        imgUrl: '',
        productId: 0
    };
};

// 验证广告数据
const validateAdvertisement = (ad: Advertisement): boolean => {
    if (!ad.title.trim()) {
        ElMessage.warning({
            message: '广告标题不能为空',
            duration: 1000
        });
        return false;
    }
    if (ad.productId <= 0) {
        ElMessage.warning({
            message: '商品ID必须大于0',
            duration: 1000
        });
        return false;
    }
    return true;
};

// 提交表单 (根据模式创建或更新)
const submitForm = async () => {
    if (!validateAdvertisement(editForm.value)) return;
    const productList = (await getProductList()).data.data;
    const productId = editForm.value.productId;
    const product = productList.find((item: { id: number; }) => item.id === productId)
    if (!product) {
        ElMessage.error('商品ID不存在,请检查');
        return;
    }

    const loadingInstance = ElLoading.service({
        lock: true,
        text: formMode.value === 'create' ? '正在创建广告...' : '正在更新广告...',
        background: 'rgba(0, 0, 0, 0.7)'
    });

    try {
        if (formMode.value === 'create') {
            const { id, ...dataWithoutId } = editForm.value;
            const response = await addAdvertisement(dataWithoutId);
            advertisementList.value.push(response.data);
            ElMessage.success({
                message: '广告创建成功',
                duration: 1000
            });
        } else {
            await updateAdAPI(editForm.value);
            const index = advertisementList.value.findIndex(a => a.id === editForm.value.id);
            if (index !== -1) {
                advertisementList.value[index] = { ...editForm.value };
            }
            ElMessage.success({
                message: '广告更新成功',
                duration: 1000
            });
        }
        showFormModal.value = false;
        await loadAdvertisements();
    } catch (error) {
        console.error(`${formMode.value === 'create' ? '创建' : '更新'}广告失败:`, error);
        ElMessage.error({
            message: `${formMode.value === 'create' ? '创建' : '更新'}广告失败，请稍后重试`,
            duration: 1000
        });
    } finally {
        loadingInstance.close();
    }
};

// 删除广告
const deleteAdvertisement = async (id: number) => {
    try {
        await ElMessageBox.confirm(
            '确定要删除这个广告吗？此操作不可恢复',
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }
        );

        const loadingInstance = ElLoading.service({
            lock: true,
            text: '正在删除广告...',
            background: 'rgba(0, 0, 0, 0.7)'
        });

        try {
            await deleteAdAPI(id);
            advertisementList.value = advertisementList.value.filter(a => a.id !== id);
            ElMessage.success({
                message: '广告删除成功',
                duration: 1000
            });
        } catch (error) {
            console.error('删除广告失败:', error);
            ElMessage.error({
                message: '删除广告失败，请稍后重试',
                duration: 1000
            });
        } finally {
            loadingInstance.close();
        }
    } catch (cancel) {
        ElMessage.info('已取消删除操作');
    }
};

// 关闭所有模态框
const closeAllModals = () => {
    showFormModal.value = false;
};

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
        try {
            const formData = new FormData()
            formData.append('file', uploadFile.raw!)
            const response = await uploadImg(formData)
            editForm.value.imgUrl = response.data.data;
        } catch (error) {
            ElMessage.error("图片上传失败：" + (error || '未知错误'));
        }
    }
}
</script>

<template>
    <div class="container">
        <el-card shadow="hover">
            <template #header>
                <div class="card-header">
                    <h1>广告管理</h1>
                    <el-button type="primary" @click="openCreateModal" :loading="loading">
                        <el-icon>
                            <plus />
                        </el-icon> 新增广告
                    </el-button>
                </div>
            </template>

            <el-empty v-if="!loading && advertisementList.length === 0" description="暂无广告数据" />

            <el-skeleton v-else-if="loading && advertisementList.length === 0" :rows="5" animated />
            <!-- 有数据 且加载完毕-->
            <el-row v-else :gutter="20">
                <el-col v-for="ad in advertisementList" :key="ad.id" :xs="24" :sm="12" :md="8" :lg="6">
                    <el-card shadow="hover" class="ad-card">
                        <!-- 标题-->
                        <template #header>
                            <div class="ad-header">
                                <span class="ad-title">{{ ad.title }}</span>
                                <div class="ad-actions">
                                    <el-button type="primary" size="default" @click="openEditModal(ad)"
                                        :loading="loading">
                                        <el-icon>
                                            <edit />
                                        </el-icon>
                                    </el-button>
                                    <el-button type="danger" size="default" @click="deleteAdvertisement(ad.id)"
                                        :loading="loading">
                                        <el-icon>
                                            <delete />
                                        </el-icon>
                                    </el-button>
                                </div>
                            </div>
                        </template>

                        <div class="ad-image-container" @click="gotoDetails(ad.productId)">
                            <el-image v-if="ad.imgUrl" :src="ad.imgUrl" :alt="ad.title" fit="cover" class="ad-image">
                                <template #error>
                                    <div class="image-error">
                                        <el-icon>
                                            <Picture />
                                        </el-icon>
                                        <span>图片加载失败</span>
                                    </div>
                                </template>
                            </el-image>
                            <div v-else class="ad-image-placeholder">
                                <el-icon>
                                    <Picture />
                                </el-icon>
                                <span>暂无图片</span>
                            </div>
                        </div>

                        <div class="ad-content">
                            <p class="ad-description">{{ ad.content }}</p>
                            <div class="ad-meta">
                                <el-tag size="small">商品ID: {{ ad.productId }}</el-tag>
                                <el-tag size="small" type="info">广告ID: {{ ad.id }}</el-tag>
                            </div>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </el-card>

        <el-dialog v-model="showFormModal" :title="formMode === 'create' ? '创建新广告' : `编辑广告 (ID: ${editForm.id})`"
            width="50%" @close="closeAllModals">
            <el-form :model="editForm" label-width="100px" label-position="top">
                <el-form-item label="标题" prop="title" required>
                    <el-input v-model="editForm.title" placeholder="请输入广告标题" />
                </el-form-item>
                <el-form-item label="内容" prop="content">
                    <el-input v-model="editForm.content" type="textarea" :rows="4" placeholder="请输入广告内容" />
                </el-form-item>
                <el-form-item label="图片URL" prop="imgUrl">
                    <el-upload class="avatar-uploader" :show-file-list="false" :on-change="handleAvatarChange"
                        :before-upload="beforeLogoUpload" accept="image/*" :auto-upload="false">
                        <!--图片上传模式-->
                        <!-- 有头像是显示预览图-->
                        <el-image v-if="editForm.imgUrl" :src="editForm.imgUrl" class="avatar" fit="cover" />
                        <!-- 无图像时候显示上传图标-->
                        <el-icon v-else class="avatar-uploader-icon">
                            <Plus />
                        </el-icon>
                    </el-upload>
                    <div class="el-upload__tip">
                        支持JPG/PNG格式,且不超过5MB
                    </div>
                </el-form-item>
                <el-form-item label="商品ID" prop="productId" required>
                    <el-input-number v-model="editForm.productId" :min="1" controls-position="right" />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="closeAllModals">取消</el-button>
                    <el-button type="primary" @click="submitForm" :loading="loading">
                        {{ formMode === 'create' ? '提交' : '更新' }}
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.container {
    padding: 20px;
    /* 内边距20px */
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.ad-card {
    margin-bottom: 20px;
    border-radius: 8px;
    transition: all 0.3s;
    height: 100%;
}

.ad-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.ad-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.ad-title {
    font-weight: bold;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.ad-image-container {
    cursor: pointer;
    height: 200px;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    border-radius: 4px;
    overflow: hidden;
}

.ad-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.ad-image-placeholder,
.image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #909399;
}

.ad-image-placeholder .el-icon,
.image-error .el-icon {
    font-size: 40px;
    margin-bottom: 10px;
}

.image-error {
    text-align: center;
    color: red;
}

.ad-content {
    padding: 0 10px;
}

.ad-description {
    color: #606266;
    font-size: 14px;
    line-height: 1.5;
    margin-bottom: 15px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}

.ad-meta {
    display: flex;
    gap: 8px;
    margin-top: 10px;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
}

.avatar-uploader {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
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