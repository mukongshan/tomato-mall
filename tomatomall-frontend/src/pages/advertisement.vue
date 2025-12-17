<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
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
const isPageLoaded = ref(false);

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

        // 添加延迟让卡片动画更自然
        await nextTick();
        setTimeout(() => {
            isPageLoaded.value = true;
        }, 300);
    } catch (error) {
        console.error('加载广告失败:', error);
        ElMessage.error({
            message: '加载广告失败，请稍后重试',
            duration: 1000
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
    <div class="container animate-fade-in">
        <el-card shadow="hover" class="main-card animate-slide-up">
            <template #header>
                <div class="card-header">
                    <div class="header-left animate-slide-right">
                        <div class="header-icon animate-bounce-in">
                            <el-icon size="32" class="rotate-on-hover">
                                <Promotion />
                            </el-icon>
                        </div>
                        <div class="header-text">
                            <h1 class="animate-slide-down">广告管理</h1>
                            <p class="animate-slide-down delay-1">管理和配置商品推广广告</p>
                        </div>
                    </div>
                    <el-button type="primary" @click="openCreateModal" :loading="loading" size="large"
                        class="create-btn animate-slide-left pulse-on-hover">
                        <el-icon class="bounce-on-click">
                            <plus />
                        </el-icon>
                        新增广告
                    </el-button>
                </div>
            </template>

            <el-empty v-if="!loading && advertisementList.length === 0" description="暂无广告数据"
                class="empty-state animate-fade-in-up" />

            <el-skeleton v-else-if="loading && advertisementList.length === 0" :rows="5" animated
                class="animate-pulse" />

            <!-- 有数据 且加载完毕-->
            <el-row v-else :gutter="24" class="ad-grid">
                <el-col v-for="(ad, index) in advertisementList" :key="ad.id" :xs="24" :sm="12" :md="8" :lg="6"
                    class="animate-card-appear" :style="{ animationDelay: `${index * 0.1}s` }">
                    <el-card shadow="hover" class="ad-card hover-lift">
                        <!-- 标题-->
                        <template #header>
                            <div class="ad-header">
                                <span class="ad-title text-shimmer">{{ ad.title }}</span>
                                <div class="ad-actions">
                                    <el-button type="primary" size="small" @click="openEditModal(ad)" :loading="loading"
                                        circle class="action-btn scale-on-hover">
                                        <el-icon class="rotate-on-hover">
                                            <edit />
                                        </el-icon>
                                    </el-button>
                                    <el-button type="danger" size="small" @click="deleteAdvertisement(ad.id)"
                                        :loading="loading" circle class="action-btn shake-on-hover">
                                        <el-icon class="wobble-on-hover">
                                            <delete />
                                        </el-icon>
                                    </el-button>
                                </div>
                            </div>
                        </template>

                        <div class="ad-image-container zoom-on-hover" @click="gotoDetails(ad.productId)">
                            <el-image v-if="ad.imgUrl" :src="ad.imgUrl" :alt="ad.title" fit="cover" class="ad-image">
                                <template #error>
                                    <div class="image-error animate-bounce">
                                        <el-icon size="40" class="pulse-icon">
                                            <Picture />
                                        </el-icon>
                                        <span>图片加载失败</span>
                                    </div>
                                </template>
                            </el-image>
                            <div v-else class="ad-image-placeholder animate-float">
                                <el-icon size="40" class="pulse-icon">
                                    <Picture />
                                </el-icon>
                                <span>暂无图片</span>
                            </div>
                            <div class="image-overlay slide-up-on-hover">
                                <span class="glow-text">点击查看商品详情</span>
                            </div>
                        </div>

                        <div class="ad-content">
                            <p class="ad-description fade-in-on-scroll">{{ ad.content }}</p>
                            <div class="ad-meta">
                                <el-tag size="small" type="success" class="tag-bounce">商品ID: {{ ad.productId }}</el-tag>
                                <el-tag size="small" type="info" class="tag-bounce delay-1">广告ID: {{ ad.id }}</el-tag>
                            </div>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </el-card>

        <!-- 美化的对话框 -->
        <el-dialog v-model="showFormModal" :title="formMode === 'create' ? '📢 创建新广告' : `✏️ 编辑广告 (ID: ${editForm.id})`"
            width="65%" @close="closeAllModals" class="beautiful-dialog animate-modal-appear"
            :close-on-click-modal="false" destroy-on-close>

            <!-- 表单头部 -->
            <div class="form-header animate-slide-down">
                <div class="form-header-icon animate-spin-in">
                    <el-icon size="40" class="pulse-icon">
                        <Promotion />
                    </el-icon>
                </div>
                <div class="form-header-text">
                    <h3 class="animate-slide-right">{{ formMode === 'create' ? '创建新广告' : '编辑广告信息' }}</h3>
                    <p class="animate-slide-right delay-1">
                        {{ formMode === 'create' ? '填写广告的详细信息，创建一个新的推广广告' : '修改下方的广告信息并保存更改' }}
                    </p>
                </div>
            </div>

            <el-form :model="editForm" label-width="0" class="beautiful-form">
                <!-- 基本信息 -->
                <div class="form-row animate-form-section" style="--delay: 0.1s">
                    <div class="form-group">
                        <label class="form-label animate-slide-up">
                            <el-icon class="bounce-icon">
                                <Present />
                            </el-icon>
                            <span>广告标题</span>
                            <span class="required pulse-text">*</span>
                        </label>
                        <el-input v-model="editForm.title" placeholder="请输入广告标题" size="large"
                            class="form-input focus-glow" />
                    </div>

                    <div class="form-group">
                        <label class="form-label animate-slide-up delay-1">
                            <el-icon class="bounce-icon">
                                <Present />
                            </el-icon>
                            <span>商品ID</span>
                            <span class="required pulse-text">*</span>
                        </label>
                        <el-input-number v-model="editForm.productId" :min="1" controls-position="right" size="large"
                            class="form-input focus-glow">
                            <template #append>
                                <span class="input-unit glow-on-focus">ID</span>
                            </template>
                        </el-input-number>
                    </div>
                </div>

                <!-- 广告内容 -->
                <div class="form-row full-width animate-form-section" style="--delay: 0.2s">
                    <div class="form-group">
                        <label class="form-label animate-slide-up">
                            <el-icon class="bounce-icon">
                                <Edit />
                            </el-icon>
                            <span>广告内容</span>
                        </label>
                        <el-input v-model="editForm.content" type="textarea" :rows="4" placeholder="请输入广告内容描述..."
                            class="form-textarea focus-glow" />
                    </div>
                </div>

                <!-- 图片上传 -->
                <div class="form-row full-width animate-form-section" style="--delay: 0.3s">
                    <div class="form-group">
                        <label class="form-label animate-slide-up">
                            <el-icon class="bounce-icon">
                                <Picture />
                            </el-icon>
                            <span>广告图片</span>
                        </label>
                        <div class="upload-section">
                            <el-upload class="image-uploader" :show-file-list="false" :on-change="handleAvatarChange"
                                :before-upload="beforeLogoUpload" accept="image/*" :auto-upload="false">
                                <!-- 有图片时显示预览 -->
                                <div v-if="editForm.imgUrl" class="image-preview hover-zoom">
                                    <el-image :src="editForm.imgUrl" class="preview-image" fit="cover" />
                                    <div class="image-mask fade-in-on-hover">
                                        <el-icon size="24" class="bounce-icon">
                                            <Edit />
                                        </el-icon>
                                        <span class="glow-text">点击更换图片</span>
                                    </div>
                                </div>
                                <!-- 无图片时显示上传区域 -->
                                <div v-else class="upload-area hover-lift-slight">
                                    <el-icon size="40" class="upload-icon bounce-icon">
                                        <Plus />
                                    </el-icon>
                                    <div class="upload-text">
                                        <div class="animate-fade-in">点击上传图片</div>
                                        <div class="upload-hint animate-fade-in delay-1">支持 JPG/PNG，不超过 5MB</div>
                                    </div>
                                </div>
                            </el-upload>
                            <div class="upload-tips">
                                <div class="tip-item animate-slide-up" style="--delay: 0.1s">
                                    <el-icon class="tip-icon">
                                        <Picture />
                                    </el-icon>
                                    <span>建议尺寸：800x600 像素</span>
                                </div>
                                <div class="tip-item animate-slide-up" style="--delay: 0.2s">
                                    <el-icon class="tip-icon">
                                        <Picture />
                                    </el-icon>
                                    <span>支持格式：JPG、PNG</span>
                                </div>
                                <div class="tip-item animate-slide-up" style="--delay: 0.3s">
                                    <el-icon class="tip-icon">
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
                <div class="dialog-footer animate-slide-up">
                    <el-button @click="closeAllModals" size="large" class="cancel-button hover-lift">
                        取消
                    </el-button>
                    <el-button type="primary" @click="submitForm" :loading="loading" size="large"
                        class="submit-button pulse-on-hover">
                        <el-icon class="bounce-on-click">
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
/* 基础动画关键帧 */
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideLeft {
    from {
        opacity: 0;
        transform: translateX(30px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes slideRight {
    from {
        opacity: 0;
        transform: translateX(-30px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes bounceIn {
    0% {
        opacity: 0;
        transform: scale(0.3);
    }

    50% {
        opacity: 1;
        transform: scale(1.1);
    }

    70% {
        transform: scale(0.9);
    }

    100% {
        opacity: 1;
        transform: scale(1);
    }
}

@keyframes pulse {

    0%,
    100% {
        transform: scale(1);
    }

    50% {
        transform: scale(1.1);
    }
}

@keyframes bounce {

    0%,
    20%,
    50%,
    80%,
    100% {
        transform: translateY(0);
    }

    40% {
        transform: translateY(-10px);
    }

    60% {
        transform: translateY(-5px);
    }
}

@keyframes rotate {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}

@keyframes wobble {

    0%,
    100% {
        transform: rotate(0deg);
    }

    25% {
        transform: rotate(-5deg);
    }

    75% {
        transform: rotate(5deg);
    }
}

@keyframes shake {

    0%,
    100% {
        transform: translateX(0);
    }

    25% {
        transform: translateX(-3px);
    }

    75% {
        transform: translateX(3px);
    }
}

@keyframes float {

    0%,
    100% {
        transform: translateY(0px);
    }

    50% {
        transform: translateY(-10px);
    }
}

@keyframes glow {

    0%,
    100% {
        box-shadow: 0 0 5px rgba(64, 158, 255, 0.3);
    }

    50% {
        box-shadow: 0 0 20px rgba(64, 158, 255, 0.6);
    }
}

@keyframes shimmer {
    0% {
        background-position: -200% 0;
    }

    100% {
        background-position: 200% 0;
    }
}

@keyframes modalAppear {
    from {
        opacity: 0;
        transform: scale(0.8) translateY(-50px);
    }

    to {
        opacity: 1;
        transform: scale(1) translateY(0);
    }
}

@keyframes cardAppear {
    from {
        opacity: 0;
        transform: translateY(50px) scale(0.9);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

@keyframes spinIn {
    from {
        opacity: 0;
        transform: rotate(-180deg) scale(0.5);
    }

    to {
        opacity: 1;
        transform: rotate(0deg) scale(1);
    }
}

/* 动画类 */
.animate-fade-in {
    animation: fadeIn 0.8s ease-out;
}

.animate-slide-up {
    animation: slideUp 0.6s ease-out calc(0.2s + var(--delay, 0s)) both;
}

.animate-slide-down {
    animation: slideDown 0.6s ease-out calc(0.2s + var(--delay, 0s)) both;
}

.animate-slide-left {
    animation: slideLeft 0.6s ease-out 0.4s both;
}

.animate-slide-right {
    animation: slideRight 0.6s ease-out calc(0.2s + var(--delay, 0s)) both;
}

.animate-bounce-in {
    animation: bounceIn 1s ease-out 0.6s both;
}

.animate-fade-in-up {
    animation: slideUp 0.8s ease-out 0.5s both;
}

.animate-pulse {
    animation: pulse 1.5s infinite;
}

.animate-bounce {
    animation: bounce 2s infinite;
}

.animate-float {
    animation: float 3s ease-in-out infinite;
}

.animate-modal-appear {
    animation: modalAppear 0.5s ease-out;
}

.animate-card-appear {
    animation: cardAppear 0.6s ease-out calc(0.8s + var(--delay, 0s)) both;
}

.animate-spin-in {
    animation: spinIn 0.8s ease-out 0.3s both;
}

.animate-form-section {
    animation: slideUp 0.6s ease-out calc(1.2s + var(--delay, 0s)) both;
}

/* 延迟类 */
.delay-1 {
    --delay: 0.1s;
}

.delay-2 {
    --delay: 0.2s;
}

.delay-3 {
    --delay: 0.3s;
}

/* 悬停动效 */
.hover-lift {
    transition: all 0.3s ease;
}

.hover-lift:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.hover-lift-slight {
    transition: all 0.3s ease;
}

.hover-lift-slight:hover {
    transform: translateY(-3px);
}

.scale-on-hover {
    transition: transform 0.3s ease;
}

.scale-on-hover:hover {
    transform: scale(1.1);
}

.rotate-on-hover:hover {
    animation: rotate 0.6s ease-in-out;
}

.wobble-on-hover:hover {
    animation: wobble 0.6s ease-in-out;
}

.shake-on-hover:hover {
    animation: shake 0.5s ease-in-out;
}

.pulse-on-hover:hover {
    animation: pulse 0.6s ease-in-out;
}

.zoom-on-hover {
    transition: all 0.3s ease;
    overflow: hidden;
}

.zoom-on-hover:hover {
    transform: scale(1.02);
}

.zoom-on-hover:hover .ad-image {
    transform: scale(1.1);
}

.hover-zoom {
    transition: transform 0.3s ease;
}

.hover-zoom:hover {
    transform: scale(1.05);
}

/* 特殊效果 */
.pulse-icon {
    animation: pulse 2s infinite ease-in-out;
}

.bounce-icon {
    animation: bounce 2s infinite ease-in-out;
}

.pulse-text {
    animation: pulse 1.5s infinite;
}

.glow-text {
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
}

.text-shimmer {
    background: linear-gradient(90deg, #333 25%, #666 50%, #333 75%);
    background-size: 200% 100%;
    background-clip: text;
    -webkit-background-clip: text;
    color: transparent;
    animation: shimmer 3s infinite;
}

.bounce-on-click:active {
    animation: bounce 0.6s ease-out;
}

.tag-bounce {
    animation: bounceIn 0.8s ease-out calc(1.5s + var(--delay, 0s)) both;
}

/* 表单动效 */
.focus-glow:focus-within {
    animation: glow 0.5s ease-out;
}

.glow-on-focus:focus-within {
    text-shadow: 0 0 10px rgba(64, 158, 255, 0.6);
}

/* 淡入效果 */
.fade-in-on-hover {
    opacity: 0;
    transition: opacity 0.3s ease;
}

.hover-zoom:hover .fade-in-on-hover,
.image-preview:hover .fade-in-on-hover {
    opacity: 1;
}

.slide-up-on-hover {
    transform: translateY(100%);
    transition: transform 0.3s ease;
}

.zoom-on-hover:hover .slide-up-on-hover {
    transform: translateY(0);
}

.fade-in-on-scroll {
    opacity: 0.8;
    transition: opacity 0.3s ease;
}

.ad-card:hover .fade-in-on-scroll {
    opacity: 1;
}

/* 提示图标动效 */
.tip-icon {
    transition: all 0.3s ease;
}

.tip-item:hover .tip-icon {
    color: #409eff;
    transform: scale(1.2);
}

/* 原有样式保持不变 */
.container {
    padding: 20px;
}

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

.empty-state {
    padding: 60px 0;
}

.ad-grid {
    margin-top: 24px;
}

.ad-card {
    margin-bottom: 24px;
    border-radius: 12px;
    transition: all 0.3s ease;
    height: 100%;
    border: 1px solid #e4e7ed;
    overflow: hidden;
}

.ad-card:hover {
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

.ad-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
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
    transition: all 0.3s ease;
}

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