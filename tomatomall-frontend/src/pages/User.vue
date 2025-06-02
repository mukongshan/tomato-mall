<script setup lang="ts">
import { ref, reactive } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue' // Added UploadFilled
import { UploadProps, UploadFile, FormRules, ElMessage } from 'element-plus'
import type { UserDetail, AccountDetail } from '@/api/account.ts'
import { getUserDetails, updateUser } from '@/api/account.ts'
import { UserFilled, EditPen, CircleCheck, CircleClose } from "@element-plus/icons-vue"; // Added more icons
import router from "@/router";
import { uploadImg } from '@/utils/image.ts'

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
    password: [
        // { min: 6, message: '密码长度至少为6位', trigger: 'blur' } // Example if min length is needed
    ],
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
    const isImage = ['image/jpeg', 'image/png', 'image/jpg', 'image/gif'].includes(rawFile.type) // Added GIF
    const isSizeValid = rawFile.size <= 5 * 1024 * 1024 // 5MB

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
        } catch (error) {
            ElMessage.error("图片上传失败：" + (error || '未知错误'));
        }
    }
}

// 获取用户信息
const fetchUserDetails = async () => {
    try {
        const response = await getUserDetails(username)
        Object.assign(userInfo.value, response.data.data)
        resetEditForm()
    } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败')
    }
}
fetchUserDetails()

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
            const payload: Partial<AccountDetail> = { ...editForm };
            if (!editForm.password) {
                delete payload.password;
            }
            delete (payload as any).confirmPassword;

            await updateUser(payload as AccountDetail)
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

</script>

<template>
    <el-main class="profile-main-container fade-in">
        <!-- 左侧信息展示卡 -->
        <el-card class="profile-aside-card slide-left" shadow="hover">
            <div class="avatar-section">
                <el-avatar :size="120" :src="userInfo.avatar || ''" class="profile-avatar">
                    <UserFilled />
                </el-avatar>
                <h2 class="user-name-display">{{ userInfo.name || userInfo.username }}</h2>
                <p class="user-role-display">{{ userInfo.role }}</p>
            </div>

            <el-divider class="stylish-divider"></el-divider>

            <el-descriptions :column="1" border class="profile-descriptions" title="详细信息">
                <template #extra>
                    <el-button type="primary" @click="editMode = true" v-if="!editMode" class="edit-info-button"
                        :icon="EditPen" round>
                        编辑资料
                    </el-button>
                </template>
                <el-descriptions-item label-class-name="desc-label" class-name="desc-content">
                    <template #label><i class="el-icon-user" /> 用户名</template>
                    {{ userInfo.username }}
                </el-descriptions-item>
                <el-descriptions-item label-class-name="desc-label" class-name="desc-content">
                    <template #label><i class="el-icon-phone-outline" /> 电话</template>
                    {{ userInfo.telephone || '未设置' }}
                </el-descriptions-item>
                <el-descriptions-item label-class-name="desc-label" class-name="desc-content">
                    <template #label><i class="el-icon-message" /> 邮箱</template>
                    {{ userInfo.email || '未设置' }}
                </el-descriptions-item>
                <el-descriptions-item label-class-name="desc-label" class-name="desc-content">
                    <template #label><i class="el-icon-location-outline" /> 地址</template>
                    {{ userInfo.location || '未设置' }}
                </el-descriptions-item>
            </el-descriptions>
        </el-card>

        <!-- 右侧编辑面板 -->
        <el-card class="profile-change-card slide-right" shadow="hover">
            <template #header>
                <div class="card-header">
                    <span class="header-title">编辑个人信息</span>
                    <div v-if="editMode" class="action-buttons">
                        <el-button @click="cancelEdit" class="cancel-btn" round>取消</el-button>
                        <el-button type="primary" @click="handleUpdate" class="save-btn" round
                            :icon="CircleCheck">保存</el-button>
                    </div>
                </div>
            </template>

            <div v-if="!editMode" class="edit-placeholder fade-in-up">
                <el-icon :size="60" color="#a0cfff">
                    <EditPen />
                </el-icon>
                <p>点击“编辑资料”按钮以修改您的个人信息。</p>
                <span>更改将在此处显示。</span>
            </div>

            <el-form v-else ref="formRef" :model="editForm" :rules="rules" label-position="top"
                class="edit-form styled-scrollbar fade-in-up">
                <el-form-item label="更换头像" prop="avatar" class="avatar-form-item">
                    <el-upload class="avatar-uploader-enhanced" action="#" :show-file-list="false"
                        :before-upload="beforeAvatarUpload" :on-change="handleAvatarChange" accept="image/*"
                        :auto-upload="false">
                        <el-image v-if="editForm.avatar" :src="editForm.avatar" class="uploaded-avatar-preview"
                            fit="cover" />
                        <div v-else class="avatar-uploader-placeholder">
                            <el-icon :size="32">
                                <UploadFilled />
                            </el-icon>
                            <span>点击或拖拽上传</span>
                        </div>
                        <div class="avatar-overlay">
                            <el-icon :size="24">
                                <EditPen />
                            </el-icon>
                        </div>
                    </el-upload>
                    <div class="upload-tip-enhanced">推荐方形图片，JPG/PNG/GIF，小于 5MB。</div>
                </el-form-item>

                <el-row :gutter="24">
                    <el-col :xs="24" :sm="12">
                        <el-form-item label="用户名" prop="username">
                            <el-input v-model="editForm.username" disabled />
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12">
                        <el-form-item label="姓名" prop="name">
                            <el-input v-model="editForm.name" placeholder="您的真实姓名或昵称" />
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-form-item label="用户角色" prop="role">
                    <el-select v-model="editForm.role" placeholder="选择您的账户角色" style="width: 100%;">
                        <el-option label="顾客 (Customer)" value="CUSTOMER" />
                        <el-option label="员工 (Staff)" value="STAFF" />
                        <el-option label="店主 (Shopkeeper)" value="SHOPKEEPER" />
                        <el-option label="管理员 (Admin)" value="admin" />
                    </el-select>
                </el-form-item>

                <el-row :gutter="24">
                    <el-col :xs="24" :sm="12">
                        <el-form-item label="电话号码" prop="telephone">
                            <el-input v-model="editForm.telephone" placeholder="您的联系电话" />
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12">
                        <el-form-item label="电子邮箱" prop="email">
                            <el-input v-model="editForm.email" placeholder="您的电子邮箱地址" />
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-form-item label="通讯地址" prop="location">
                    <el-input v-model="editForm.location" type="textarea" :rows="3" placeholder="您的详细通讯地址" />
                </el-form-item>

                <el-divider content-position="center" class="stylish-divider section-divider">
                    <el-icon><svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" data-v-029747aa="">
                            <path fill="currentColor"
                                d="M800 416a288 288 0 1 0-576 0c0 118.144 95.872 214.784 216 237.248V800h144V653.248A288 288 0 0 0 800 416zM512 960a32 32 0 0 1-32-32V800a32 32 0 0 1 64 0v128a32 32 0 0 1-32 32zm0-704a224 224 0 1 1 0 448 224 224 0 0 1 0-448z">
                            </path>
                        </svg></el-icon>
                    安全设置 (选填)
                </el-divider>

                <el-row :gutter="24">
                    <el-col :xs="24" :sm="12">
                        <el-form-item label="设置新密码" prop="password">
                            <el-input v-model="editForm.password" type="password" placeholder="留空则不修改密码" show-password
                                autocomplete="new-password" />
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12">
                        <el-form-item label="确认新密码" prop="confirmPassword">
                            <el-input v-model="editForm.confirmPassword" type="password" placeholder="再次输入以确认"
                                show-password autocomplete="new-password" />
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
        </el-card>
    </el-main>
</template>

<style scoped>
/* Animation Keyframes */
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(15px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(25px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideLeft {
    from {
        opacity: 0;
        transform: translateX(-40px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes slideRight {
    from {
        opacity: 0;
        transform: translateX(40px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes subtleShine {
    0% {
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.05);
    }

    50% {
        box-shadow: 0 10px 30px rgba(64, 158, 255, 0.1);
    }

    100% {
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.05);
    }
}

/* Animation Classes */
.fade-in {
    animation: fadeIn 0.8s ease-out both;
}

.fade-in-up {
    animation: fadeInUp 0.8s ease-out 0.2s both;
}

.slide-left {
    animation: slideLeft 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

.slide-right {
    animation: slideRight 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.1s both;
}

/* Main Container */
.profile-main-container {
    display: flex;
    gap: 28px;
    padding: 28px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e9edf2 100%);
    /* Subtle gradient background */
    min-height: calc(100vh - 80px);
    /* Adjust based on your header/footer */
    overflow-x: hidden;
    /* Prevent horizontal scroll from animations */
}

/* Cards Styling */
.profile-aside-card,
.profile-change-card {
    border-radius: 16px;
    /* Softer radius */
    border: none;
    /* Remove default border if using prominent shadow */
    background-color: #ffffff;
    display: flex;
    flex-direction: column;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    animation: subtleShine 5s infinite ease-in-out;
    /* Subtle shadow animation */
}

.profile-aside-card:hover,
.profile-change-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 35px rgba(0, 0, 0, 0.08) !important;
    /* Enhanced hover shadow */
}

.profile-aside-card {
    flex: 0 0 380px;
    /* Slightly wider aside */
}

.profile-change-card {
    flex: 1;
    min-width: 0;
    /* Prevent overflow issues in flex child */
}

/* Card Header (Edit Panel) */
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 28px;
    border-bottom: 1px solid #e9eef3;
    background-color: #fdfdfe;
    /* Slightly off-white header */
    border-top-left-radius: 16px;
    border-top-right-radius: 16px;
}

.header-title {
    font-size: 20px;
    font-weight: 600;
    color: #2c3e50;
    /* Darker, sophisticated blue-gray */
}

.action-buttons .el-button {
    margin-left: 14px;
    font-weight: 500;
}

.save-btn {
    background-color: #409EFF;
    /* Element Plus primary */
    border-color: #409EFF;
    color: #fff;
}

.save-btn:hover {
    background-color: #66b1ff;
    border-color: #66b1ff;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.cancel-btn:hover {
    color: #409EFF;
    border-color: #a0cfff;
    background-color: #ecf5ff;
    transform: translateY(-2px);
}

/* Aside Card Content */
.avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 35px 20px;
    text-align: center;
    background: linear-gradient(to bottom, #f0f5ff, #ffffff);
    /* Gradient for avatar section */
    border-top-left-radius: 16px;
    border-top-right-radius: 16px;
}

.profile-avatar {
    width: 120px;
    height: 120px;
    margin-bottom: 18px;
    border: 4px solid #fff;
    /* White border for avatar */
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
    /* More prominent avatar shadow */
    transition: transform 0.3s ease;
}

.profile-avatar:hover {
    transform: scale(1.05);
}

.user-name-display {
    font-size: 24px;
    font-weight: 600;
    color: #1c2b3a;
    margin: 0 0 6px 0;
}

.user-role-display {
    font-size: 15px;
    color: #5a6876;
    font-style: italic;
}

.stylish-divider {
    margin: 0px 28px 24px;
    /* Adjusted margin */
    border-top: 1px dashed #ced4da;
    /* Dashed divider */
}

.section-divider .el-divider__text {
    background-color: #ffffff;
    padding: 0 20px;
    font-weight: 500;
    color: #5a6876;
    font-size: 15px;
}

.section-divider .el-icon {
    margin-right: 8px;
    color: #409EFF;
}

.profile-descriptions {
    padding: 0 28px 28px;
}

.profile-descriptions .el-descriptions__title {
    /* Target title slot */
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 18px;
    padding-bottom: 8px;
    border-bottom: 2px solid #409EFF;
    display: inline-block;
}

.edit-info-button {
    font-weight: 500;
    transition: all 0.3s ease;
}

.edit-info-button:hover {
    transform: translateY(-2px) scale(1.03);
    box-shadow: 0 5px 15px rgba(64, 158, 255, 0.25);
}

:deep(.desc-label) {
    font-weight: 500 !important;
    color: #495057 !important;
    background-color: #f8f9fa !important;
    width: 110px !important;
    padding: 12px 15px !important;
    border-right: 1px solid #e9ecef;
}

:deep(.desc-label) i {
    margin-right: 8px;
    color: #007bff;
    vertical-align: middle;
}

:deep(.desc-content) {
    color: #212529 !important;
    padding: 12px 15px !important;
    font-size: 14px;
    background-color: #fff !important;
}

:deep(.el-descriptions__body) {
    border-radius: 8px;
    overflow: hidden;
    /* Ensure border radius clips content */
}

/* Edit Panel Content */
.edit-placeholder {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: calc(100% - 77px);
    /* Adjust based on header height */
    padding: 30px;
    text-align: center;
    color: #777d84;
}

.edit-placeholder p {
    font-size: 16px;
    margin-top: 15px;
    margin-bottom: 8px;
    font-weight: 500;
}

.edit-placeholder span {
    font-size: 14px;
}

.edit-form {
    padding: 28px;
    overflow-y: auto;
    max-height: calc(100vh - 80px - 77px - 56px);
    /* Viewport - container padding - header - margins */
}

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


/* Enhanced Avatar Uploader */
.avatar-form-item {
    margin-bottom: 28px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

.avatar-uploader-enhanced {
    width: 150px;
    height: 150px;
    border-radius: 50%;
    /* Circular uploader */
    border: 2px dashed #b8c2cc;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f0f4f8;
    transition: all 0.3s ease;
}

.avatar-uploader-enhanced:hover {
    border-color: #409EFF;
    background-color: #eaf4ff;
}

.avatar-uploader-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #8c939d;
}

.avatar-uploader-placeholder span {
    font-size: 13px;
    margin-top: 8px;
}

.uploaded-avatar-preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
    /* Ensures the image covers the circle */
}

.avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.3);
    color: white;
    display: flex;
    justify-content: center;
    align-items: center;
    opacity: 0;
    transition: opacity 0.3s ease;
    border-radius: 50%;
}

.avatar-uploader-enhanced:hover .avatar-overlay {
    opacity: 1;
}

.upload-tip-enhanced {
    font-size: 13px;
    color: #777d84;
    margin-top: 12px;
}


/* Form Inputs and Select */
.edit-form .el-form-item {
    margin-bottom: 22px;
    /* Consistent spacing */
}

.edit-form .el-form-item__label {
    font-weight: 500;
    color: #4a5568;
    /* Softer label color */
    padding-bottom: 6px;
    /* Space between label and input */
}

.edit-form .el-input__wrapper,
/* Target new Element Plus structure for inputs */
.edit-form .el-select__wrapper {
    border-radius: 8px !important;
    /* Rounded inputs */
    box-shadow: none !important;
    /* Remove default focus shadow if adding custom */
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.edit-form .el-input__wrapper:hover,
.edit-form .el-select__wrapper:hover {
    border-color: #a0cfff !important;
}

.edit-form .el-input__wrapper.is-focus,
.edit-form .el-select__wrapper.is-focus {
    border-color: #409EFF !important;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2) !important;
    /* Custom focus ring */
}

.edit-form .el-textarea__inner {
    border-radius: 8px;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.edit-form .el-textarea__inner:hover {
    border-color: #a0cfff;
}

.edit-form .el-textarea__inner:focus {
    border-color: #409EFF;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}


/* Responsive Adjustments */
@media (max-width: 1024px) {

    /* Tablet and below */
    .profile-main-container {
        flex-direction: column;
        padding: 20px;
    }

    .profile-aside-card {
        flex: 0 0 auto;
        margin-bottom: 24px;
    }

    .edit-form {
        max-height: none;
        /* Allow form to scroll with page on mobile */
    }
}

@media (max-width: 768px) {

    /* Mobile */
    .avatar-section {
        padding: 25px 15px;
    }

    .profile-avatar {
        width: 100px;
        height: 100px;
    }

    .user-name-display {
        font-size: 22px;
    }

    .card-header {
        padding: 18px 20px;
    }

    .header-title {
        font-size: 18px;
    }

    .edit-form {
        padding: 20px;
    }

    .el-row {
        margin-left: 0 !important;
        margin-right: 0 !important;
    }

    /* Reset gutter for stacked */
    .el-col {
        padding-left: 0 !important;
        padding-right: 0 !important;
    }

    /* Reset gutter for stacked */
    .el-col:not(:last-child) .el-form-item {
        margin-bottom: 20px;
    }

    /* Spacing for stacked items */
}
</style>