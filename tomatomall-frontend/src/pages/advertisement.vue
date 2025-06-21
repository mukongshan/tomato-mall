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

import { Edit, Delete, Plus, Picture, Present, Promotion } from '@element-plus/icons-vue';
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
        <el-card shadow="hover" class="main-card">
            <template #header>
                <div class="card-header">
                    <div class="header-left">
                        <div class="header-icon">
                            <el-icon size="32">
                                <Promotion />
                            </el-icon>
                        </div>
                        <div class="header-text">
                            <h1>广告管理</h1>
                            <p>管理和配置商品推广广告</p>
                        </div>
                    </div>
                    <el-button type="primary" @click="openCreateModal" :loading="loading" size="large"
                        class="create-btn">
                        <el-icon>
                            <plus />
                        </el-icon> 新增广告
                    </el-button>
                </div>
            </template>

            <el-empty v-if="!loading && advertisementList.length === 0" description="暂无广告数据" class="empty-state" />

            <el-skeleton v-else-if="loading && advertisementList.length === 0" :rows="5" animated />

            <!-- 有数据 且加载完毕-->
            <el-row v-else :gutter="24" class="ad-grid">
                <el-col v-for="ad in advertisementList" :key="ad.id" :xs="24" :sm="12" :md="8" :lg="6">
                    <el-card shadow="hover" class="ad-card">
                        <!-- 标题-->
                        <template #header>
                            <div class="ad-header">
                                <span class="ad-title">{{ ad.title }}</span>
                                <div class="ad-actions">
                                    <el-button type="primary" size="small" @click="openEditModal(ad)" :loading="loading"
                                        circle class="action-btn">
                                        <el-icon>
                                            <edit />
                                        </el-icon>
                                    </el-button>
                                    <el-button type="danger" size="small" @click="deleteAdvertisement(ad.id)"
                                        :loading="loading" circle class="action-btn">
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
                                        <el-icon size="40">
                                            <Picture />
                                        </el-icon>
                                        <span>图片加载失败</span>
                                    </div>
                                </template>
                            </el-image>
                            <div v-else class="ad-image-placeholder">
                                <el-icon size="40">
                                    <Picture />
                                </el-icon>
                                <span>暂无图片</span>
                            </div>
                            <div class="image-overlay">
                                <span>点击查看商品详情</span>
                            </div>
                        </div>

                        <div class="ad-content">
                            <p class="ad-description">{{ ad.content }}</p>
                            <div class="ad-meta">
                                <el-tag size="small" type="success">商品ID: {{ ad.productId }}</el-tag>
                                <el-tag size="small" type="info">广告ID: {{ ad.id }}</el-tag>
                            </div>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </el-card>

        <!-- 美化的对话框 -->
        <el-dialog v-model="showFormModal" :title="formMode === 'create' ? '📢 创建新广告' : `✏️ 编辑广告 (ID: ${editForm.id})`"
            width="65%" @close="closeAllModals" class="beautiful-dialog" :close-on-click-modal="false" destroy-on-close>

            <!-- 表单头部 -->
            <div class="form-header">
                <div class="form-header-icon">
                    <el-icon size="40">
                        <Promotion />
                    </el-icon>
                </div>
                <div class="form-header-text">
                    <h3>{{ formMode === 'create' ? '创建新广告' : '编辑广告信息' }}</h3>
                    <p>{{ formMode === 'create' ? '填写广告的详细信息，创建一个新的推广广告' : '修改下方的广告信息并保存更改' }}</p>
                </div>
            </div>

            <el-form :model="editForm" label-width="0" class="beautiful-form">
                <!-- 基本信息 -->
                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label">
                            <el-icon>
                                <Present />
                            </el-icon>
                            <span>广告标题</span>
                            <span class="required">*</span>
                        </label>
                        <el-input v-model="editForm.title" placeholder="请输入广告标题" size="large" class="form-input" />
                    </div>

                    <div class="form-group">
                        <label class="form-label">
                            <el-icon>
                                <Present />
                            </el-icon>
                            <span>商品ID</span>
                            <span class="required">*</span>
                        </label>
                        <el-input-number v-model="editForm.productId" :min="1" controls-position="right" size="large"
                            class="form-input">
                            <template #append>
                                <span class="input-unit">ID</span>
                            </template>
                        </el-input-number>
                    </div>
                </div>

                <!-- 广告内容 -->
                <div class="form-row full-width">
                    <div class="form-group">
                        <label class="form-label">
                            <el-icon>
                                <Edit />
                            </el-icon>
                            <span>广告内容</span>
                        </label>
                        <el-input v-model="editForm.content" type="textarea" :rows="4" placeholder="请输入广告内容描述..."
                            class="form-textarea" />
                    </div>
                </div>

                <!-- 图片上传 -->
                <div class="form-row full-width">
                    <div class="form-group">
                        <label class="form-label">
                            <el-icon>
                                <Picture />
                            </el-icon>
                            <span>广告图片</span>
                        </label>
                        <div class="upload-section">
                            <el-upload class="image-uploader" :show-file-list="false" :on-change="handleAvatarChange"
                                :before-upload="beforeLogoUpload" accept="image/*" :auto-upload="false">
                                <!-- 有图片时显示预览 -->
                                <div v-if="editForm.imgUrl" class="image-preview">
                                    <el-image :src="editForm.imgUrl" class="preview-image" fit="cover" />
                                    <div class="image-mask">
                                        <el-icon size="24">
                                            <Edit />
                                        </el-icon>
                                        <span>点击更换图片</span>
                                    </div>
                                </div>
                                <!-- 无图片时显示上传区域 -->
                                <div v-else class="upload-area">
                                    <el-icon size="40" class="upload-icon">
                                        <Plus />
                                    </el-icon>
                                    <div class="upload-text">
                                        <div>点击上传图片</div>
                                        <div class="upload-hint">支持 JPG/PNG，不超过 5MB</div>
                                    </div>
                                </div>
                            </el-upload>
                            <div class="upload-tips">
                                <div class="tip-item">
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
                                    <span>建议尺寸：800x600 像素</span>
                                </div>
                                <div class="tip-item">
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
                                    <span>支持格式：JPG、PNG</span>
                                </div>
                                <div class="tip-item">
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
                                    <span>文件大小：不超过 5MB</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </el-form>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="closeAllModals" size="large" class="cancel-button">
                        取消
                    </el-button>
                    <el-button type="primary" @click="submitForm" :loading="loading" size="large" class="submit-button">
                        <el-icon>
                            <Plus />
                        </el-icon>
                        {{ formMode === 'create' ? '创建广告' : '保存修改' }}
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.container {
    padding: 20px;
}

/* 主卡片样式 */
.main-card {
    border-radius: 16px;
    overflow: hidden;
    border: none;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
}

.header-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.header-text h1 {
    margin: 0 0 4px 0;
    font-size: 24px;
    color: #303133;
    font-weight: 600;
}

.header-text p {
    margin: 0;
    color: #909399;
    font-size: 14px;
}

.create-btn {
    border-radius: 8px;
    padding: 12px 24px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    transition: all 0.3s ease;
}

.create-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* 空状态样式 */
.empty-state {
    padding: 60px 0;
}

/* 广告网格 */
.ad-grid {
    margin-top: 24px;
}

/* 广告卡片样式 */
.ad-card {
    margin-bottom: 24px;
    border-radius: 12px;
    transition: all 0.3s ease;
    height: 100%;
    border: 1px solid #e4e7ed;
    overflow: hidden;
}

.ad-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
    border-color: #409eff;
}

.ad-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.ad-title {
    font-weight: 600;
    font-size: 16px;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
    margin-right: 12px;
}

.ad-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
}

.action-btn {
    width: 32px;
    height: 32px;
    transition: all 0.3s ease;
}

.action-btn:hover {
    transform: scale(1.1);
}

/* 图片容器样式 */
.ad-image-container {
    position: relative;
    cursor: pointer;
    height: 200px;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    border-radius: 8px;
    overflow: hidden;
    transition: all 0.3s ease;
}

.ad-image-container:hover {
    transform: scale(1.02);
}

.ad-image-container:hover .image-overlay {
    opacity: 1;
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
    gap: 12px;
}

.image-error {
    color: #f56c6c;
}

.image-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 500;
    opacity: 0;
    transition: opacity 0.3s ease;
}

/* 广告内容样式 */
.ad-content {
    padding: 0 16px 16px;
}

.ad-description {
    color: #606266;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 16px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
    min-height: 60px;
}

.ad-meta {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

/* 美化对话框样式 */
.beautiful-dialog {
    border-radius: 16px;
    overflow: hidden;
}

.beautiful-dialog :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px 30px;
    margin: 0;
}

.beautiful-dialog :deep(.el-dialog__title) {
    font-size: 20px;
    font-weight: 600;
}

.beautiful-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: white;
    font-size: 20px;
}

.beautiful-dialog :deep(.el-dialog__body) {
    padding: 0;
}

/* 表单头部 */
.form-header {
    display: flex;
    align-items: center;
    padding: 30px 40px;
    background: linear-gradient(135deg, #f8faff 0%, #e8f4f8 100%);
    border-bottom: 1px solid #e4e7ed;
}

.form-header-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    margin-right: 24px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.form-header-text h3 {
    margin: 0 0 8px 0;
    font-size: 24px;
    color: #303133;
    font-weight: 600;
}

.form-header-text p {
    margin: 0;
    color: #606266;
    font-size: 14px;
    line-height: 1.5;
}

/* 表单样式 */
.beautiful-form {
    padding: 40px;
}

.form-row {
    display: flex;
    gap: 32px;
    margin-bottom: 32px;
}

.form-row.full-width {
    flex-direction: column;
}

.form-group {
    flex: 1;
    min-width: 0;
}

.form-label {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    font-size: 15px;
    color: #303133;
    font-weight: 500;
    gap: 8px;
}

.form-label .el-icon {
    color: #409eff;
    font-size: 16px;
}

.required {
    color: #f56c6c;
    font-weight: 600;
}

.form-input,
.form-textarea {
    width: 100%;
}

.form-input :deep(.el-input__wrapper),
.form-input :deep(.el-input-number),
.form-textarea :deep(.el-textarea__inner) {
    border-radius: 8px;
    border: 2px solid #e4e7ed;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.form-input :deep(.el-input__wrapper):hover,
.form-input :deep(.el-input-number):hover,
.form-textarea :deep(.el-textarea__inner):hover {
    border-color: #c6d9f7;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.form-input :deep(.el-input__wrapper.is-focus),
.form-input :deep(.el-input-number.is-focus),
.form-textarea :deep(.el-textarea__inner):focus {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.input-unit {
    color: #409eff;
    font-weight: 500;
    padding: 0 12px;
    background: #f0f9ff;
    border-left: 1px solid #e4e7ed;
}

/* 上传区域样式 */
.upload-section {
    display: flex;
    gap: 24px;
    align-items: flex-start;
}

.image-uploader {
    flex-shrink: 0;
}

.image-preview {
    position: relative;
    width: 200px;
    height: 150px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
}

.image-preview:hover {
    transform: scale(1.02);
}

.image-preview:hover .image-mask {
    opacity: 1;
}

.preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.image-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    opacity: 0;
    transition: opacity 0.3s ease;
    font-size: 14px;
}

.upload-area {
    width: 200px;
    height: 150px;
    border: 2px dashed #e4e7ed;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    background: #fafbfc;
}

.upload-area:hover {
    border-color: #409eff;
    background: #f0f9ff;
}

.upload-icon {
    color: #c0c4cc;
    margin-bottom: 12px;
}

.upload-text {
    text-align: center;
}

.upload-text>div:first-child {
    color: #606266;
    font-size: 14px;
    margin-bottom: 4px;
}

.upload-hint {
    color: #909399;
    font-size: 12px;
}

.upload-tips {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.tip-item {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #909399;
    font-size: 13px;
}

.tip-item .el-icon {
    color: #c0c4cc;
    font-size: 14px;
}

/* 对话框底部 */
.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 16px;
    padding: 24px 40px;
    background: #fafbfc;
    border-top: 1px solid #e4e7ed;
}

.cancel-button {
    padding: 12px 32px;
    border-radius: 8px;
    font-weight: 500;
}

.submit-button {
    padding: 12px 32px;
    border-radius: 8px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    transition: all 0.3s ease;
}

.submit-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}
</style>