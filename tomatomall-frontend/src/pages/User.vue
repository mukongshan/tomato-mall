<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { UploadProps, UploadFile, FormRules, ElMessage } from 'element-plus'
import type { UserDetail, AccountDetail } from '@/api/account.ts'
import { getUserDetails, updateUser, updateUserInfo } from '@/api/account.ts'
import { UserFilled, EditPen, CircleCheck, CircleClose, Camera, Lock, User, Phone, Message, Location } from "@element-plus/icons-vue"
import router from "@/router"
import { uploadImg } from '@/utils/image'

// 用户信息
const userInfo = ref<UserDetail>({
    id: 0,
    username: '',
    name: '',
    role: '',
    avatar: '',
    telephone: '',
    email: '',
    location: '',
    shopId: 0,
    isValidStaff: 0
})

const username = sessionStorage.getItem('username') as string

// 编辑表单数据
const editForm = reactive({
    ...<AccountDetail>{
        username: '',
        name: '',
        role: '',
        avatar: '',
        telephone: '',
        email: '',
        location: '',
        password: ''
    },
    confirmPassword: '',
})

const editMode = ref(false)
const formRef = ref()
const isLoading = ref(true)
const isSaving = ref(false)

// 表单验证规则
const rules = reactive<FormRules<typeof editForm>>({
    name: [
        { required: true, message: '请输入姓名', trigger: 'blur' },
        { min: 2, max: 10, message: '姓名长度需在2-10字符之间', trigger: 'blur' }
    ],
    telephone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1\d{10}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    role: [
        { required: true, message: '请选择用户角色', trigger: 'change' }
    ],
    location: [
        { required: true, message: '请输入地址', trigger: 'blur' }
    ],
    password: [],
    confirmPassword: [
        {
            validator: (_, value, callback) => {
                if (editForm.password && !value) {
                    callback(new Error('请再次输入新密码进行确认'));
                } else if (value && value !== editForm.password) {
                    callback(new Error('两次输入密码不一致'));
                } else {
                    callback();
                }
            },
            trigger: 'blur'
        }
    ]
})

// 头像上传处理
const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
    const isImage = ['image/jpeg', 'image/png', 'image/jpg', 'image/gif'].includes(rawFile.type)
    const isSizeValid = rawFile.size <= 5 * 1024 * 1024

    if (!isImage) {
        ElMessage.error('头像必须为 JPG/PNG/GIF 格式')
        return false
    }
    if (!isSizeValid) {
        ElMessage.error('头像大小不能超过 5MB')
        return false
    }
    return true
}

const handleAvatarChange: UploadProps['onChange'] = async (uploadFile: UploadFile) => {
    if (uploadFile.raw && editMode.value) {
        try {
            const formData = new FormData()
            formData.append('file', uploadFile.raw!)
            const response = await uploadImg(formData)
            editForm.avatar = response.data.data;
            ElMessage.success('头像上传成功！')
        } catch (error) {
            ElMessage.error("图片上传失败：" + (error || '未知错误'));
        }
    }
}

// 获取用户信息
const fetchUserDetails = async () => {
    try {
        isLoading.value = true
        const response = await getUserDetails(username)
        Object.assign(userInfo.value, response.data.data)
        resetEditForm()
    } catch (error) {
        ElMessage.error('获取用户信息失败')
    } finally {
        setTimeout(() => {
            isLoading.value = false
        }, 800)
    }
}

const resetEditForm = () => {
    const { ...restOfUserInfo } = userInfo.value;
    Object.assign(editForm, JSON.parse(JSON.stringify(restOfUserInfo)));
    editForm.password = '';
    editForm.confirmPassword = '';
    if (formRef.value) {
        formRef.value.clearValidate();
    }
}

const handleUpdate = () => {
    formRef.value?.validate(async (valid: boolean) => {
        if (!valid) return
        try {
            isSaving.value = true
            const payload: Partial<AccountDetail> = { ...editForm };
            if (!editForm.password) {
                delete payload.password;
                await updateUserInfo(payload as AccountDetail);
            } else {
                delete (payload as any).confirmPassword;
                await updateUser(payload as AccountDetail)
            }

            await fetchUserDetails();
            editMode.value = false

            ElMessage({
                message: '更新成功！',
                type: 'success',
                icon: CircleCheck,
            });

            if (payload.password) {
                ElMessage.info('密码已更新，请重新登录');
                setTimeout(() => goToLogin(), 1500);
            }
        } catch (error) {
            console.error('更新失败:', error)
            ElMessage({
                message: '更新失败，请稍后重试。',
                type: 'error',
                icon: CircleClose,
            });
        } finally {
            isSaving.value = false
        }
    })
}

const goToLogin = () => {
    sessionStorage.clear();
    router.push({ path: `/login` });
};

const cancelEdit = () => {
    resetEditForm()
    editMode.value = false
}

const enterEditMode = () => {
    editMode.value = true
    nextTick(() => {
        // 编辑模式进入后的动画处理
    })
}

onMounted(() => {
    fetchUserDetails()
})
</script>

<template>
    <div class="profile-container">
        <!-- 加载状态 -->
        <div v-if="isLoading" class="loading-container">
            <div class="loading-content">
                <div class="loading-avatar"></div>
                <div class="loading-text">
                    <div class="loading-bar loading-bar-1"></div>
                    <div class="loading-bar loading-bar-2"></div>
                    <div class="loading-bar loading-bar-3"></div>
                </div>
            </div>
        </div>

        <!-- 主要内容 -->
        <el-main v-else class="profile-main-container animate-fade-in">
            <!-- 左侧信息展示卡 -->
            <el-card class="profile-aside-card animate-slide-left" shadow="hover">
                <div class="avatar-section">
                    <div class="avatar-wrapper animate-bounce-in">
                        <el-avatar :size="120" :src="userInfo.avatar || ''" class="profile-avatar">
                            <UserFilled />
                        </el-avatar>
                        <div class="avatar-status-dot"></div>
                    </div>
                    <h2 class="user-name-display animate-slide-up">{{ userInfo.name || userInfo.username }}</h2>
                    <div class="user-role-badge animate-slide-up">
                        <span class="role-text">{{ userInfo.role }}</span>
                    </div>
                </div>

                <el-divider class="stylish-divider animate-draw-line"></el-divider>

                <div class="profile-info-section animate-fade-in-up">
                    <div class="info-header">
                        <h3 class="info-title">
                            <el-icon>
                                <User />
                            </el-icon>
                            个人信息
                        </h3>
                        <el-button type="primary" @click="enterEditMode" v-if="!editMode"
                            class="edit-info-button animate-pulse-button" :icon="EditPen" round size="small">
                            编辑资料
                        </el-button>
                    </div>

                    <div class="info-grid">
                        <div class="info-item animate-fade-in-item" style="--delay: 0.1s">
                            <div class="info-icon">
                                <el-icon>
                                    <User />
                                </el-icon>
                            </div>
                            <div class="info-content">
                                <div class="info-label">用户名</div>
                                <div class="info-value">{{ userInfo.username }}</div>
                            </div>
                        </div>

                        <div class="info-item animate-fade-in-item" style="--delay: 0.2s">
                            <div class="info-icon">
                                <el-icon>
                                    <Phone />
                                </el-icon>
                            </div>
                            <div class="info-content">
                                <div class="info-label">电话</div>
                                <div class="info-value">{{ userInfo.telephone || '未设置' }}</div>
                            </div>
                        </div>

                        <div class="info-item animate-fade-in-item" style="--delay: 0.3s">
                            <div class="info-icon">
                                <el-icon>
                                    <Message />
                                </el-icon>
                            </div>
                            <div class="info-content">
                                <div class="info-label">邮箱</div>
                                <div class="info-value">{{ userInfo.email || '未设置' }}</div>
                            </div>
                        </div>

                        <div class="info-item animate-fade-in-item" style="--delay: 0.4s">
                            <div class="info-icon">
                                <el-icon>
                                    <Location />
                                </el-icon>
                            </div>
                            <div class="info-content">
                                <div class="info-label">地址</div>
                                <div class="info-value">{{ userInfo.location || '未设置' }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </el-card>

            <!-- 右侧编辑面板 -->
            <el-card class="profile-change-card animate-slide-right" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <div class="header-content">
                            <div class="header-icon animate-rotate">
                                <el-icon>
                                    <EditPen />
                                </el-icon>
                            </div>
                            <span class="header-title">编辑个人信息</span>
                        </div>
                        <div v-if="editMode" class="action-buttons animate-slide-in">
                            <el-button @click="cancelEdit" class="cancel-btn animate-hover-lift" round
                                :disabled="isSaving">
                                取消
                            </el-button>
                            <el-button type="primary" @click="handleUpdate" class="save-btn animate-hover-lift" round
                                :icon="CircleCheck" :loading="isSaving">
                                保存更改
                            </el-button>
                        </div>
                    </div>
                </template>

                <div v-if="!editMode" class="edit-placeholder animate-float">
                    <div class="placeholder-content">
                        <div class="placeholder-icon animate-bounce">
                            <el-icon :size="60">
                                <EditPen />
                            </el-icon>
                        </div>
                        <h3 class="placeholder-title animate-fade-in">开始编辑您的资料</h3>
                        <p class="placeholder-desc animate-fade-in">点击"编辑资料"按钮来修改您的个人信息</p>
                        <div class="placeholder-features animate-slide-up">
                            <div class="feature-item">
                                <el-icon>
                                    <Camera />
                                </el-icon>
                                <span>更换头像</span>
                            </div>
                            <div class="feature-item">
                                <el-icon>
                                    <User />
                                </el-icon>
                                <span>修改资料</span>
                            </div>
                            <div class="feature-item">
                                <el-icon>
                                    <Lock />
                                </el-icon>
                                <span>安全设置</span>
                            </div>
                        </div>
                    </div>
                </div>

                <el-form v-else ref="formRef" :model="editForm" :rules="rules" label-position="top"
                    class="edit-form styled-scrollbar animate-form-appear">

                    <!-- 头像上传区域 -->
                    <div class="form-section animate-section-appear" style="--delay: 0.1s">
                        <div class="section-header">
                            <el-icon>
                                <Camera />
                            </el-icon>
                            <span>头像设置</span>
                        </div>
                        <el-form-item prop="avatar" class="avatar-form-item">
                            <el-upload class="avatar-uploader-enhanced animate-hover-scale" action="#"
                                :show-file-list="false" :before-upload="beforeAvatarUpload"
                                :on-change="handleAvatarChange" accept="image/*" :auto-upload="false">

                                <el-image v-if="editForm.avatar" :src="editForm.avatar" class="uploaded-avatar-preview"
                                    fit="cover" />

                                <div v-else class="avatar-uploader-placeholder">
                                    <el-icon :size="32" class="upload-icon animate-bounce">
                                        <UploadFilled />
                                    </el-icon>
                                    <span>点击或拖拽上传</span>
                                </div>

                                <div class="avatar-overlay">
                                    <el-icon :size="24">
                                        <Camera />
                                    </el-icon>
                                    <span>更换头像</span>
                                </div>
                            </el-upload>
                            <div class="upload-tip-enhanced">
                                推荐方形图片，JPG/PNG/GIF，小于 5MB
                            </div>
                        </el-form-item>
                    </div>

                    <!-- 基本信息 -->
                    <div class="form-section animate-section-appear" style="--delay: 0.2s">
                        <div class="section-header">
                            <el-icon>
                                <User />
                            </el-icon>
                            <span>基本信息</span>
                        </div>
                        <el-row :gutter="24">
                            <el-col :xs="24" :sm="12">
                                <el-form-item label="用户名" prop="username">
                                    <el-input v-model="editForm.username" disabled class="animate-input-focus">
                                        <template #prefix>
                                            <el-icon>
                                                <User />
                                            </el-icon>
                                        </template>
                                    </el-input>
                                </el-form-item>
                            </el-col>
                            <el-col :xs="24" :sm="12">
                                <el-form-item label="姓名" prop="name">
                                    <el-input v-model="editForm.name" placeholder="您的真实姓名或昵称"
                                        class="animate-input-focus" />
                                </el-form-item>
                            </el-col>
                        </el-row>

                        <el-form-item label="用户角色" prop="role">
                            <el-select v-model="editForm.role" placeholder="选择您的账户角色" style="width: 100%;" disabled
                                class="animate-input-focus">
                                <el-option label="顾客 (Customer)" value="CUSTOMER" />
                                <el-option label="员工 (Staff)" value="STAFF" />
                                <el-option label="店主 (Shopkeeper)" value="SHOPKEEPER" />
                                <el-option label="管理员 (Admin)" value="admin" />
                            </el-select>
                        </el-form-item>
                    </div>

                    <!-- 联系信息 -->
                    <div class="form-section animate-section-appear" style="--delay: 0.3s">
                        <div class="section-header">
                            <el-icon>
                                <Phone />
                            </el-icon>
                            <span>联系信息</span>
                        </div>
                        <el-row :gutter="24">
                            <el-col :xs="24" :sm="12">
                                <el-form-item label="电话号码" prop="telephone">
                                    <el-input v-model="editForm.telephone" placeholder="您的联系电话"
                                        class="animate-input-focus">
                                        <template #prefix>
                                            <el-icon>
                                                <Phone />
                                            </el-icon>
                                        </template>
                                    </el-input>
                                </el-form-item>
                            </el-col>
                            <el-col :xs="24" :sm="12">
                                <el-form-item label="电子邮箱" prop="email">
                                    <el-input v-model="editForm.email" placeholder="您的电子邮箱地址"
                                        class="animate-input-focus">
                                        <template #prefix>
                                            <el-icon>
                                                <Message />
                                            </el-icon>
                                        </template>
                                    </el-input>
                                </el-form-item>
                            </el-col>
                        </el-row>

                        <el-form-item label="通讯地址" prop="location">
                            <el-input v-model="editForm.location" type="textarea" :rows="3" placeholder="您的详细通讯地址"
                                class="animate-input-focus" />
                        </el-form-item>
                    </div>

                    <!-- 安全设置 -->
                    <div class="form-section animate-section-appear" style="--delay: 0.4s">
                        <div class="section-header security-header">
                            <el-icon>
                                <Lock />
                            </el-icon>
                            <span>安全设置</span>
                            <div class="security-badge">可选</div>
                        </div>
                        <el-row :gutter="24">
                            <el-col :xs="24" :sm="12">
                                <el-form-item label="设置新密码" prop="password">
                                    <el-input v-model="editForm.password" type="password" placeholder="留空则不修改密码"
                                        show-password autocomplete="new-password" class="animate-input-focus">
                                        <template #prefix>
                                            <el-icon>
                                                <Lock />
                                            </el-icon>
                                        </template>
                                    </el-input>
                                </el-form-item>
                            </el-col>
                            <el-col :xs="24" :sm="12">
                                <el-form-item label="确认新密码" prop="confirmPassword">
                                    <el-input v-model="editForm.confirmPassword" type="password" placeholder="再次输入以确认"
                                        show-password autocomplete="new-password" class="animate-input-focus">
                                        <template #prefix>
                                            <el-icon>
                                                <Lock />
                                            </el-icon>
                                        </template>
                                    </el-input>
                                </el-form-item>
                            </el-col>
                        </el-row>
                    </div>
                </el-form>
            </el-card>
        </el-main>
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

@keyframes slideLeft {
    from {
        opacity: 0;
        transform: translateX(-50px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes slideRight {
    from {
        opacity: 0;
        transform: translateX(50px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
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

@keyframes bounceIn {
    0% {
        opacity: 0;
        transform: scale(0.3);
    }

    50% {
        opacity: 1;
        transform: scale(1.05);
    }

    70% {
        transform: scale(0.9);
    }

    100% {
        opacity: 1;
        transform: scale(1);
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

@keyframes float {

    0%,
    100% {
        transform: translateY(0px);
    }

    50% {
        transform: translateY(-10px);
    }
}

@keyframes drawLine {
    from {
        width: 0;
    }

    to {
        width: 100%;
    }
}

@keyframes shimmer {
    0% {
        background-position: -1000px 0;
    }

    100% {
        background-position: 1000px 0;
    }
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateX(20px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

/* 动画类 */
.animate-fade-in {
    animation: fadeIn 0.8s ease-out;
}

.animate-slide-left {
    animation: slideLeft 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.animate-slide-right {
    animation: slideRight 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.1s both;
}

.animate-slide-up {
    animation: slideUp 0.6s ease-out 0.3s both;
}

.animate-bounce-in {
    animation: bounceIn 1s ease-out 0.2s both;
}

.animate-bounce {
    animation: bounce 2s infinite ease-in-out;
}

.animate-pulse-button {
    animation: pulse 2s infinite ease-in-out;
}

.animate-rotate {
    animation: rotate 2s linear infinite;
}

.animate-float {
    animation: float 3s ease-in-out infinite;
}

.animate-draw-line::after {
    animation: drawLine 1s ease-out 0.5s both;
}

.animate-slide-in {
    animation: slideIn 0.5s ease-out;
}

.animate-fade-in-up {
    animation: slideUp 0.8s ease-out 0.4s both;
}

.animate-fade-in-item {
    animation: fadeIn 0.6s ease-out calc(0.6s + var(--delay)) both;
}

.animate-section-appear {
    animation: slideUp 0.6s ease-out calc(0.8s + var(--delay)) both;
}

.animate-form-appear {
    animation: fadeIn 0.8s ease-out 0.3s both;
}

.animate-hover-lift:hover {
    transform: translateY(-3px);
    transition: transform 0.3s ease;
}

.animate-hover-scale:hover {
    transform: scale(1.05);
    transition: transform 0.3s ease;
}

.animate-input-focus:focus-within {
    transform: translateY(-2px);
    transition: transform 0.3s ease;
}

/* 加载状态 */
.loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 140px);
}

.loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
}

.loading-avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
}

.loading-text {
    display: flex;
    flex-direction: column;
    gap: 8px;
    align-items: center;
}

.loading-bar {
    height: 12px;
    border-radius: 6px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
}

.loading-bar-1 {
    width: 120px;
    animation-delay: 0s;
}

.loading-bar-2 {
    width: 80px;
    animation-delay: 0.2s;
}

.loading-bar-3 {
    width: 100px;
    animation-delay: 0.4s;
}

/* 主容器 */
.profile-container {
    min-height: 100vh;
    background: #f8f9fa;
    padding: 20px;
}

.profile-main-container {
    display: flex;
    gap: 30px;
    max-width: 1400px;
    margin: 0 auto;
    padding: 0;
}

/* 卡片样式 */
.profile-aside-card,
.profile-change-card {
    border-radius: 16px;
    border: 1px solid #e8e9ea;
    background: #ffffff;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
}

.profile-aside-card:hover,
.profile-change-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.profile-aside-card {
    flex: 0 0 400px;
    height: fit-content;
}

.profile-change-card {
    flex: 1;
    min-height: 600px;
}

/* 头像区域 */
.avatar-section {
    padding: 40px 30px;
    text-align: center;
    background: #ffffff;
    border-radius: 16px 16px 0 0;
}

.avatar-wrapper {
    position: relative;
    display: inline-block;
    margin-bottom: 20px;
}

.profile-avatar {
    width: 120px;
    height: 120px;
    border: 4px solid #f8f9fa;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
}

.profile-avatar:hover {
    transform: scale(1.05);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.avatar-status-dot {
    position: absolute;
    bottom: 8px;
    right: 8px;
    width: 24px;
    height: 24px;
    background: #67c23a;
    border: 3px solid white;
    border-radius: 50%;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.user-name-display {
    font-size: 28px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0 0 12px 0;
}

.user-role-badge {
    display: inline-flex;
    align-items: center;
    padding: 6px 16px;
    background: #4ecdc4;
    color: white;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(78, 205, 196, 0.3);
}

/* 分割线 */
.stylish-divider {
    position: relative;
    margin: 0 30px 30px;
    border: none;
    height: 1px;
    background: #e8e9ea;
}

.stylish-divider::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    height: 2px;
    background: #4ecdc4;
    width: 0;
}

/* 信息区域 */
.profile-info-section {
    padding: 0 30px 30px;
}

.info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.info-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
}

.edit-info-button {
    font-size: 14px;
    padding: 8px 16px;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.edit-info-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.info-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.info-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 12px;
    border: 1px solid #e8e9ea;
    transition: all 0.3s ease;
}

.info-item:hover {
    background: #f0fffe;
    border-color: #4ecdc4;
    transform: translateX(4px);
}

.info-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: #4ecdc4;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;
}

.info-content {
    flex: 1;
    min-width: 0;
}

.info-label {
    font-size: 12px;
    color: #6b7280;
    margin-bottom: 2px;
    font-weight: 500;
}

.info-value {
    font-size: 14px;
    color: #2c3e50;
    font-weight: 500;
    word-break: break-all;
}

/* 卡片头部 */
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px 32px;
    border-bottom: 1px solid #e8e9ea;
    background: #f8f9fa;
    border-radius: 16px 16px 0 0;
}

.header-content {
    display: flex;
    align-items: center;
    gap: 12px;
}

.header-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: #4ecdc4;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
}

.header-title {
    font-size: 20px;
    font-weight: 600;
    color: #2c3e50;
}

.action-buttons {
    display: flex;
    gap: 12px;
}

.save-btn,
.cancel-btn {
    padding: 10px 20px;
    font-weight: 500;
    transition: all 0.3s ease;
}

.save-btn {
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.save-btn:hover {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 占位符内容 */
.edit-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    min-height: 500px;
    padding: 40px;
}

.placeholder-content {
    text-align: center;
    max-width: 400px;
}

.placeholder-icon {
    margin-bottom: 24px;
    color: #4ecdc4;
}

.placeholder-title {
    font-size: 24px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 12px 0;
}

.placeholder-desc {
    font-size: 16px;
    color: #6b7280;
    margin: 0 0 32px 0;
    line-height: 1.6;
}

.placeholder-features {
    display: flex;
    justify-content: center;
    gap: 24px;
    flex-wrap: wrap;
}

.feature-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 12px;
    border: 1px solid #e8e9ea;
    min-width: 80px;
    transition: all 0.3s ease;
}

.feature-item:hover {
    background: #f0fffe;
    border-color: #4ecdc4;
    transform: translateY(-4px);
}

.feature-item .el-icon {
    font-size: 24px;
    color: #4ecdc4;
}

.feature-item span {
    font-size: 12px;
    color: #6b7280;
    font-weight: 500;
}

/* 表单样式 */
.edit-form {
    padding: 32px;
    height: calc(100vh - 200px);
    overflow-y: auto;
}

.form-section {
    margin-bottom: 32px;
    padding: 24px;
    background: #f8f9fa;
    border-radius: 12px;
    border: 1px solid #e8e9ea;
    transition: all 0.3s ease;
}

.form-section:hover {
    background: #f0fffe;
    border-color: #4ecdc4;
}

.section-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
}

.section-header .el-icon {
    color: #4ecdc4;
}

.security-header {
    position: relative;
}

.security-badge {
    margin-left: auto;
    padding: 4px 12px;
    background: #e6f7ff;
    color: #1890ff;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    border: 1px solid #b3d8ff;
}

/* 头像上传 */
.avatar-form-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
}

.avatar-uploader-enhanced {
    position: relative;
    width: 150px;
    height: 150px;
    border-radius: 50%;
    border: 3px dashed #d1d5db;
    cursor: pointer;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #ffffff;
    transition: all 0.3s ease;
}

.avatar-uploader-enhanced:hover {
    border-color: #4ecdc4;
    background: #f0fffe;
    transform: scale(1.05);
}

.avatar-uploader-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #6b7280;
}

.upload-icon {
    color: #4ecdc4;
}

.uploaded-avatar-preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(78, 205, 196, 0.8);
    color: white;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 4px;
    opacity: 0;
    transition: opacity 0.3s ease;
    border-radius: 50%;
}

.avatar-uploader-enhanced:hover .avatar-overlay {
    opacity: 1;
}

.upload-tip-enhanced {
    font-size: 13px;
    color: #6b7280;
    text-align: center;
    padding: 8px 16px;
    background: #f8f9fa;
    border-radius: 16px;
    border: 1px solid #e8e9ea;
}

/* 表单项美化 */
.edit-form :deep(.el-form-item__label) {
    font-weight: 500;
    color: #4b5563;
    margin-bottom: 8px;
}

.edit-form :deep(.el-input__wrapper),
.edit-form :deep(.el-select__wrapper),
.edit-form :deep(.el-textarea__inner) {
    border-radius: 8px;
    border: 1px solid #e8e9ea;
    box-shadow: none;
    transition: all 0.3s ease;
}

.edit-form :deep(.el-input__wrapper:hover),
.edit-form :deep(.el-select__wrapper:hover),
.edit-form :deep(.el-textarea__inner:hover) {
    border-color: #4ecdc4;
    transform: translateY(-1px);
}

.edit-form :deep(.el-input__wrapper.is-focus),
.edit-form :deep(.el-select__wrapper.is-focus),
.edit-form :deep(.el-textarea__inner:focus) {
    border-color: #4ecdc4;
    box-shadow: 0 0 0 3px rgba(78, 205, 196, 0.1);
}

/* 滚动条 */
.styled-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.styled-scrollbar::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.styled-scrollbar::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

.styled-scrollbar::-webkit-scrollbar-thumb:hover {
    background: #a1a1a1;
}

/* 响应式设计 */
@media (max-width: 1024px) {
    .profile-main-container {
        flex-direction: column;
        gap: 20px;
    }

    .profile-aside-card {
        flex: none;
    }
}

@media (max-width: 768px) {
    .profile-container {
        padding: 16px;
    }

    .avatar-section {
        padding: 30px 20px;
    }

    .profile-avatar {
        width: 100px;
        height: 100px;
    }

    .user-name-display {
        font-size: 24px;
    }

    .card-header {
        padding: 20px 24px;
        flex-direction: column;
        gap: 16px;
        align-items: stretch;
    }

    .action-buttons {
        justify-content: center;
    }

    .edit-form {
        padding: 24px 20px;
    }

    .form-section {
        padding: 20px 16px;
    }

    .placeholder-features {
        gap: 16px;
    }

    .info-item {
        padding: 12px;
    }
}
</style>