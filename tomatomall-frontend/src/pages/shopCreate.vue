<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { componentSizes, ElMessage } from 'element-plus';
import { Shop, createShop } from '@/api/shop';
import { UploadFile, UploadProps, ElLoading } from 'element-plus';
import { uploadImg } from "@/utils/image.ts";
import router from "../router/index.ts";
import { Plus, Shop as ShopIcon, Edit, Picture, Check, Clock } from "@element-plus/icons-vue";
import { getAdmin } from '@/api/account.ts';
import { getMessageByFromUserAndContent, Message, sendMessage } from '@/api/message.ts';

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

const applied = ref(false);
const isLoading = ref(true);

const checkEmployeeStatus = async () => {
    const response = await getMessageByFromUserAndContent(Number(sessionStorage.getItem('id')), "NEW_STORE_APPLICATION");
    return response.data.data;
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
            form.value.iconUrl = response.data.data;
            ElMessage.success('图片上传成功！');
        } catch (error) {
            ElMessage.error("图片上传失败：" + (error || '未知错误'));
        }
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
        text: '创建中...',
        background: 'rgba(0, 0, 0, 0.7)'
    })
    try {
        const response = await createShop(form.value);
        const adminId = (await getAdmin()).data.data;
        const message: Message = {
            id: 0,
            content: "NEW_STORE_APPLICATION",
            isRead: false,
            fromUser: form.value.ownerId,
            toUser: adminId,
            createdTime: new Date().toISOString()
        };
        await sendMessage(message);
        console.log(response)
        if (response.data.code === '200') {
            ElMessage.success({ message: "创建成功 请等待审核", duration: 500 })
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

onMounted(async () => {
    try {
        applied.value = await checkEmployeeStatus();
    } finally {
        setTimeout(() => {
            isLoading.value = false;
        }, 800);
    }
});
</script>

<template>
    <div class="page-container">
        <!-- 页面加载动画 -->
        <div v-if="isLoading" class="page-loading">
            <div class="loading-content">
                <div class="loading-icon">
                    <el-icon size="60" class="rotate-icon">
                        <ShopIcon />
                    </el-icon>
                </div>
                <div class="loading-text">
                    <span class="loading-char" v-for="(char, index) in '准备创建商店'" :key="index"
                        :style="{ animationDelay: `${index * 0.1}s` }">
                        {{ char }}
                    </span>
                </div>
                <div class="loading-dots">
                    <span class="dot" style="--delay: 0s">.</span>
                    <span class="dot" style="--delay: 0.3s">.</span>
                    <span class="dot" style="--delay: 0.6s">.</span>
                </div>
            </div>
        </div>

        <!-- 主要内容 -->
        <div v-else class="form-container animate-page-enter">
            <!-- 页面标题区域 -->
            <div class="page-header animate-slide-down">
                <div class="header-decoration">
                    <div class="decoration-line left-line"></div>
                    <div class="header-icon animate-bounce-in">
                        <el-icon size="32">
                            <ShopIcon />
                        </el-icon>
                    </div>
                    <div class="decoration-line right-line"></div>
                </div>
                <h1 class="page-title animate-title-reveal">
                    <span v-for="(char, index) in '创建您的专属商店'" :key="index" class="title-char"
                        :style="{ animationDelay: `${index * 0.05}s` }">
                        {{ char }}
                    </span>
                </h1>
                <p class="page-subtitle animate-fade-in-up">填写店铺信息，开启您的商业之旅</p>
            </div>

            <!-- 表单卡片 -->
            <div class="form-card animate-card-enter">
                <el-form class="centered-form" ref="formRef" :model="form" :size="componentSizes[3]" label-width="auto"
                    status-icon>

                    <!-- 商店名称 -->
                    <el-form-item label="商店名称" class="form-item animate-form-item" style="--delay: 0.1s">
                        <div class="input-wrapper">
                            <div class="input-icon">
                                <el-icon>
                                    <ShopIcon />
                                </el-icon>
                            </div>
                            <div class="input-content">
                                <el-input v-model="form.name" placeholder="请输入您的商店名称" class="enhanced-input" />
                            </div>
                        </div>
                    </el-form-item>

                    <!-- 商店描述 -->
                    <el-form-item label="商店描述" class="form-item animate-form-item" style="--delay: 0.2s">
                        <div class="input-wrapper">
                            <div class="input-icon">
                                <el-icon>
                                    <Edit />
                                </el-icon>
                            </div>
                            <div class="input-content">
                                <el-input v-model="form.description" type="textarea" :rows="4"
                                    placeholder="请描述您的商店特色和经营理念..." maxlength="200" show-word-limit
                                    class="enhanced-textarea" />
                            </div>
                        </div>
                    </el-form-item>

                    <!-- 商店图标 -->
                    <el-form-item label="商店图标" prop="iconUrl" class="form-item animate-form-item" style="--delay: 0.3s">
                        <div class="upload-section">
                            <el-upload class="avatar-uploader enhanced-uploader" :show-file-list="false"
                                :on-change="handleAvatarChange" :on-remove="handleAvatarRemove"
                                :before-upload="beforeLogoUpload" accept="image/*" :auto-upload="false">

                                <!-- 有图标时显示预览 -->
                                <div v-if="form.iconUrl" class="avatar-preview">
                                    <el-image :src="form.iconUrl" class="avatar-image" fit="cover" />
                                    <div class="avatar-overlay">
                                        <el-icon size="24">
                                            <Edit />
                                        </el-icon>
                                        <span>更换图标</span>
                                    </div>
                                </div>

                                <!-- 无图标时显示上传区域 -->
                                <div v-else class="upload-placeholder">
                                    <el-icon class="upload-icon animate-bounce">
                                        <Plus />
                                    </el-icon>
                                    <div class="upload-text">
                                        <div class="upload-main-text">点击上传商店图标</div>
                                        <div class="upload-sub-text">建议尺寸 150x150</div>
                                    </div>
                                </div>
                            </el-upload>

                            <div class="upload-tips">
                                <div class="tip-item">
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
                                    <span>支持 JPG/PNG 格式</span>
                                </div>
                                <div class="tip-item">
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
                                    <span>文件大小不超过 5MB</span>
                                </div>
                                <div class="tip-item">
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
                                    <span>建议使用方形图片</span>
                                </div>
                            </div>
                        </div>
                    </el-form-item>

                    <!-- 提交按钮 -->
                    <el-form-item class="button-group animate-form-item" style="--delay: 0.4s">
                        <div class="button-container">
                            <el-button v-if="applied" disabled type="info" size="large"
                                class="status-button applied-button">
                                <el-icon>
                                    <Clock />
                                </el-icon>
                                您已有商店申请，请耐心等待审核
                            </el-button>
                            <el-button v-else type="primary" size="large" @click="handleCreate"
                                class="create-button animate-pulse-btn">
                                <el-icon class="button-icon">
                                    <Check />
                                </el-icon>
                                创建商店
                            </el-button>
                        </div>
                    </el-form-item>
                </el-form>
            </div>

            <!-- 帮助提示 -->
            <div class="help-section animate-slide-up-late">
                <div class="help-card">
                    <div class="help-header">
                        <el-icon size="20">
                            <Picture />
                        </el-icon>
                        <span>创建须知</span>
                    </div>
                    <div class="help-content">
                        <div class="help-item">
                            <span class="help-step">1</span>
                            <span>填写完整的商店信息</span>
                        </div>
                        <div class="help-item">
                            <span class="help-step">2</span>
                            <span>上传清晰的商店图标</span>
                        </div>
                        <div class="help-item">
                            <span class="help-step">3</span>
                            <span>提交申请等待管理员审核</span>
                        </div>
                        <div class="help-item">
                            <span class="help-step">4</span>
                            <span>审核通过后即可开始经营</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 基础动画关键帧 */
@keyframes pageEnter {
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

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
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

@keyframes titleReveal {
    from {
        opacity: 0;
        transform: translateY(20px) rotateX(90deg);
    }

    to {
        opacity: 1;
        transform: translateY(0) rotateX(0deg);
    }
}

@keyframes charAppear {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes cardEnter {
    from {
        opacity: 0;
        transform: translateY(50px) scale(0.95);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

@keyframes formItemSlide {
    from {
        opacity: 0;
        transform: translateX(-30px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
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

@keyframes pulse {

    0%,
    100% {
        transform: scale(1);
    }

    50% {
        transform: scale(1.05);
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

@keyframes loadingChar {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes lineGrow {
    from {
        width: 0;
    }

    to {
        width: 60px;
    }
}

@keyframes dotBounce {

    0%,
    80%,
    100% {
        transform: scale(0);
        opacity: 0.3;
    }

    40% {
        transform: scale(1);
        opacity: 1;
    }
}

/* 动画类 */
.animate-page-enter {
    animation: pageEnter 0.8s ease-out;
}

.animate-slide-down {
    animation: slideDown 0.6s ease-out 0.2s both;
}

.animate-bounce-in {
    animation: bounceIn 1s ease-out 0.4s both;
}

.animate-title-reveal {
    animation: titleReveal 0.8s ease-out 0.6s both;
}

.title-char {
    display: inline-block;
    animation: charAppear 0.5s ease-out both;
}

.animate-fade-in-up {
    animation: fadeInUp 0.6s ease-out 0.8s both;
}

.animate-card-enter {
    animation: cardEnter 0.8s ease-out 1s both;
}

.animate-form-item {
    animation: formItemSlide 0.6s ease-out calc(1.2s + var(--delay)) both;
}

.animate-slide-up-late {
    animation: fadeInUp 0.8s ease-out 2s both;
}

.animate-bounce {
    animation: bounce 2s ease-in-out infinite;
}

.animate-pulse-btn {
    animation: pulse 2s ease-in-out infinite;
}

.rotate-icon {
    animation: rotate 2s linear infinite;
}

.loading-char {
    animation: loadingChar 0.6s ease-out both;
}

.left-line {
    animation: lineGrow 0.6s ease-out 1.2s both;
}

.right-line {
    animation: lineGrow 0.6s ease-out 1.4s both;
}

/* 页面加载动画 */
.page-loading {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
}

.loading-content {
    text-align: center;
    color: #2c3e50;
}

.loading-icon {
    margin-bottom: 20px;
}

.loading-icon .el-icon {
    color: #4ecdc4;
}

.loading-text {
    font-size: 18px;
    font-weight: 500;
    letter-spacing: 2px;
    margin-bottom: 16px;
}

.loading-dots {
    display: flex;
    justify-content: center;
    gap: 4px;
}

.dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #4ecdc4;
    animation: dotBounce 1.4s ease-in-out infinite;
    animation-delay: var(--delay);
}

/* 主容器 */
.page-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #f8fafc 0%, #e8f4f8 100%);
    padding: 20px;
}

.form-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 40px 20px;
}

/* 页面头部 */
.page-header {
    text-align: center;
    margin-bottom: 40px;
}

.header-decoration {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 20px;
    margin-bottom: 20px;
}

.decoration-line {
    height: 2px;
    background: linear-gradient(90deg, transparent, #4ecdc4, transparent);
}

.header-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: 0 8px 24px rgba(78, 205, 196, 0.3);
}

.page-title {
    font-size: 32px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0 0 12px 0;
    font-family: "华文中宋", serif;
}

.page-subtitle {
    font-size: 16px;
    color: #6b7280;
    margin: 0;
    font-weight: 400;
}

/* 表单卡片 */
.form-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    padding: 40px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    margin-bottom: 30px;
}

.centered-form {
    width: 100%;
}

/* 表单项样式 */
.form-item {
    margin-bottom: 32px;
}

.form-item :deep(.el-form-item__label) {
    font-weight: 600;
    color: #2c3e50;
    font-size: 16px;
    margin-bottom: 12px;
}

/* 关键修复：统一的输入框包装器 */
.input-wrapper {
    display: flex;
    width: 100%;
    /* 确保宽度一致 */
    background: white;
    border-radius: 12px;
    border: 2px solid #e8e9ea;
    overflow: hidden;
    transition: all 0.3s ease;
}

.input-wrapper:hover {
    border-color: #4ecdc4;
    box-shadow: 0 4px 12px rgba(78, 205, 196, 0.1);
}

.input-wrapper:focus-within {
    border-color: #4ecdc4;
    box-shadow: 0 0 0 3px rgba(78, 205, 196, 0.1);
}

/* 统一的图标区域 */
.input-icon {
    width: 54px;
    min-width: 54px;
    /* 防止收缩 */
    height: auto;
    min-height: 54px;
    /* 最小高度 */
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f8f9fa;
    color: #4ecdc4;
    border-right: 1px solid #e8e9ea;
    flex-shrink: 0;
}

/* 统一的内容区域 */
.input-content {
    flex: 1;
    width: 100%;
    display: flex;
    align-items: stretch;
}

.enhanced-input,
.enhanced-textarea {
    width: 100%;
    border: none;
}

.enhanced-input :deep(.el-input__wrapper),
.enhanced-textarea :deep(.el-textarea__inner) {
    box-shadow: none !important;
    border: none !important;
    border-radius: 0;
    background: transparent;
    width: 100%;
}

.enhanced-input :deep(.el-input__inner) {
    padding: 0 16px;
    height: 54px;
    line-height: 54px;
    width: 100%;
}

.enhanced-textarea :deep(.el-textarea__inner) {
    padding: 16px;
    resize: none;
    background: transparent;
    min-height: 102px;
    /* 调整为合适的高度 */
    line-height: 1.5;
    width: 100%;
}

/* 上传区域 */
.upload-section {
    display: flex;
    gap: 30px;
    align-items: flex-start;
    width: 100%;
}

.enhanced-uploader {
    position: relative;
    border: 3px dashed #d1d5db;
    border-radius: 16px;
    cursor: pointer;
    overflow: hidden;
    width: 180px;
    height: 180px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    background: #fafbfc;
    flex-shrink: 0;
}

.enhanced-uploader:hover {
    border-color: #4ecdc4;
    background: #f0fffe;
    transform: scale(1.02);
}

.avatar-preview {
    position: relative;
    width: 100%;
    height: 100%;
}

.avatar-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-overlay {
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
    font-weight: 500;
}

.enhanced-uploader:hover .avatar-overlay {
    opacity: 1;
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    text-align: center;
}

.upload-icon {
    font-size: 40px;
    color: #4ecdc4;
}

.upload-text {
    color: #6b7280;
}

.upload-main-text {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 4px;
}

.upload-sub-text {
    font-size: 12px;
    color: #9ca3af;
}

/* 上传提示 */
.upload-tips {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding-top: 10px;
}

.tip-item {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #6b7280;
    font-size: 14px;
    padding: 8px 12px;
    background: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e8e9ea;
    transition: all 0.3s ease;
}

.tip-item:hover {
    background: #f0fffe;
    border-color: #4ecdc4;
    color: #4ecdc4;
}

.tip-item .el-icon {
    color: #4ecdc4;
    font-size: 16px;
}

/* 按钮组 */
.button-group {
    margin-top: 40px;
    margin-bottom: 0;
}

.button-container {
    width: 100%;
    display: flex;
    justify-content: center;
}

.create-button,
.status-button {
    padding: 16px 40px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 25px;
    transition: all 0.3s ease;
    min-width: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.create-button {
    background: linear-gradient(45deg, #4ecdc4, #44a08d);
    border: none;
    box-shadow: 0 8px 24px rgba(78, 205, 196, 0.3);
}

.create-button:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 32px rgba(78, 205, 196, 0.4);
}

.applied-button {
    background: linear-gradient(45deg, #9ca3af, #6b7280);
    border: none;
    color: white;
}

.button-icon {
    font-size: 18px;
}

/* 帮助区域 */
.help-section {
    margin-top: 40px;
}

.help-card {
    background: rgba(255, 255, 255, 0.9);
    border-radius: 16px;
    padding: 24px;
    border: 1px solid rgba(78, 205, 196, 0.2);
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.1);
}

.help-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 20px;
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
}

.help-content {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
}

.help-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 8px;
    transition: all 0.3s ease;
}

.help-item:hover {
    background: #f0fffe;
    transform: translateX(4px);
}

.help-step {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #4ecdc4;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
    flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .form-container {
        padding: 20px 16px;
    }

    .form-card {
        padding: 24px 20px;
    }

    .page-title {
        font-size: 24px;
    }

    .upload-section {
        flex-direction: column;
        gap: 20px;
        align-items: center;
    }

    .enhanced-uploader {
        width: 150px;
        height: 150px;
    }

    .upload-tips {
        width: 100%;
    }

    .help-content {
        grid-template-columns: 1fr;
    }

    .header-decoration {
        gap: 12px;
    }

    .decoration-line {
        width: 40px;
    }
}

@media (max-width: 480px) {
    .input-wrapper {
        flex-direction: column;
    }

    .input-icon {
        width: 100%;
        height: 44px;
        border-right: none;
        border-bottom: 1px solid #e8e9ea;
    }

    .enhanced-input :deep(.el-input__inner) {
        height: 50px;
        line-height: 50px;
    }

    .enhanced-textarea :deep(.el-textarea__inner) {
        min-height: 80px;
    }

    .create-button,
    .status-button {
        padding: 14px 32px;
        min-width: 160px;
        font-size: 15px;
    }
}
</style>