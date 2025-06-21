<script setup lang="ts">
import { AccountDetail } from "@/api/account.ts";
import { reactive, ref } from "vue";
import { ComponentSize, ElLoading, ElMessage, FormInstance, FormRules, UploadFile, UploadProps } from 'element-plus'
import router from "../router/index.ts";
import { createAccount } from "@/api/account";  //不能用@ 为什么不生效
import { Plus, User, Lock, Phone, Message, Location, Camera, UserFilled } from '@element-plus/icons-vue'
import { uploadImg } from "@/utils/image.ts";

// 表单尺寸
const formSize = ref<ComponentSize>('default')
// 表单引用(调用表单方法)
const ruleFormRef = ref<FormInstance>()
// 表单数据对象
const ruleForm = reactive({
    ...<AccountDetail>{
        username: '',
        password: '',
        name: '',
        role: "CUSTOMER",
        avatar: '',
        telephone: '',
        email: '',
        location: ''
    },
    confirmPassword: ''
})

const rules = reactive<FormRules<typeof ruleForm>>({
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 10, message: '用户长度需在2-10字符之间', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ],
    name: [
        { required: true, message: '请输入姓名', trigger: 'blur' },
        { min: 2, max: 10, message: '姓名长度需在2-10字符之间', trigger: 'blur' }
    ],
    telephone: [
        { required: true, message: '请输入手机号', trigger: 'change' },
        { pattern: /^1\d{10}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'change' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    location: [
        { required: true, message: '请输入地址', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        {
            validator: (_, value, callback) => {
                if (value !== ruleForm.password) {
                    callback(new Error('两次输入密码不一致!'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ]
})
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
            ruleForm.avatar = response.data.data;
        } catch (error) {
            ElMessage.error("图片上传失败：" + (error || '未知错误'));
        }
    }
}

// 删除头像
const handleAvatarRemove = () => {
    ruleForm.avatar = ''
}

const handleRegister = async () => {
    if (!ruleFormRef.value) return
    try {
        // 表单验证
        const isValid = await ruleFormRef.value.validate()
        if (!isValid) return

        const { confirmPassword, ...submitData } = ruleForm

        const loading = ElLoading.service({
            lock: true,
            text: '注册中...'
        })
        try {
            const response = await createAccount(submitData);
            console.log(response)
            if (response.data.code === '200') {
                // 设置停留时间500ms
                ElMessage.success({ message: "注册成功", duration: 500 })
                await router.push("/login")
            } else {
                ElMessage.warning(response.data.message || "注册未完成");
            }
        } catch (error: any) {
            ElMessage.warning(error)
        } finally {
            loading.close()
        }
    } catch (e: any) {
        ElMessage.error("表单验证失败")
        console.error(e)
    }
}
</script>

<template>
    <div class="register-container">
        <!-- 主表单区域 -->
        <div class="form-wrapper animate-fade-in">
            <div class="form-header animate-slide-down">
                <div class="header-icon pulse-icon">
                    <el-icon size="48">
                        <UserFilled />
                    </el-icon>
                </div>
                <h2 class="form-title">
                    <span class="title-char" v-for="(char, index) in '用户注册'" :key="index"
                        :style="{ animationDelay: `${index * 0.1}s` }">
                        {{ char }}
                    </span>
                </h2>
                <p class="form-subtitle animate-slide-up">创建您的账户，开启精彩之旅</p>
                <div class="title-decoration">
                    <div class="decoration-line"></div>
                    <div class="decoration-dot"></div>
                    <div class="decoration-line"></div>
                </div>
            </div>

            <el-form class="register-form animate-scale-in" ref="ruleFormRef" :model="ruleForm" :rules="rules"
                :size="formSize" label-width="0" status-icon>

                <!-- 基本信息区域 -->
                <div class="form-section animate-form-section">
                    <h3 class="section-title">
                        <el-icon>
                            <User />
                        </el-icon>
                        基本信息
                    </h3>

                    <el-form-item prop="username" class="form-item animate-form-item">
                        <div class="input-wrapper">
                            <div class="input-icon">
                                <el-icon>
                                    <User />
                                </el-icon>
                            </div>
                            <el-input v-model="ruleForm.username" placeholder="请输入用户名" class="form-input" clearable />
                        </div>
                    </el-form-item>

                    <el-form-item prop="name" class="form-item animate-form-item">
                        <div class="input-wrapper">
                            <div class="input-icon">
                                <el-icon>
                                    <UserFilled />
                                </el-icon>
                            </div>
                            <el-input v-model="ruleForm.name" placeholder="请输入真实姓名" class="form-input" clearable />
                        </div>
                    </el-form-item>
                </div>

                <!-- 安全信息区域 -->
                <div class="form-section animate-form-section">
                    <h3 class="section-title">
                        <el-icon>
                            <Lock />
                        </el-icon>
                        安全设置
                    </h3>

                    <el-form-item prop="password" class="form-item animate-form-item">
                        <div class="input-wrapper">
                            <div class="input-icon">
                                <el-icon>
                                    <Lock />
                                </el-icon>
                            </div>
                            <el-input v-model="ruleForm.password" placeholder="请输入密码" show-password type="password"
                                class="form-input" />
                        </div>
                    </el-form-item>

                    <el-form-item prop="confirmPassword" class="form-item animate-form-item">
                        <div class="input-wrapper">
                            <div class="input-icon">
                                <el-icon>
                                    <Lock />
                                </el-icon>
                            </div>
                            <el-input v-model="ruleForm.confirmPassword" placeholder="请确认密码" show-password
                                type="password" class="form-input" />
                        </div>
                    </el-form-item>
                </div>

                <!-- 联系信息区域 -->
                <div class="form-section animate-form-section">
                    <h3 class="section-title">
                        <el-icon>
                            <Phone />
                        </el-icon>
                        联系方式
                    </h3>

                    <el-form-item prop="telephone" class="form-item animate-form-item">
                        <div class="input-wrapper">
                            <div class="input-icon">
                                <el-icon>
                                    <Phone />
                                </el-icon>
                            </div>
                            <el-input v-model="ruleForm.telephone" placeholder="请输入手机号码" class="form-input" clearable />
                        </div>
                    </el-form-item>

                    <el-form-item prop="email" class="form-item animate-form-item">
                        <div class="input-wrapper">
                            <div class="input-icon">
                                <el-icon>
                                    <Message />
                                </el-icon>
                            </div>
                            <el-input v-model="ruleForm.email" placeholder="请输入电子邮箱" class="form-input" clearable />
                        </div>
                    </el-form-item>

                    <el-form-item prop="location" class="form-item animate-form-item">
                        <div class="input-wrapper">
                            <div class="input-icon">
                                <el-icon>
                                    <Location />
                                </el-icon>
                            </div>
                            <el-input v-model="ruleForm.location" placeholder="请输入所在地址" class="form-input" clearable />
                        </div>
                    </el-form-item>
                </div>

                <!-- 头像上传区域 -->
                <div class="form-section animate-form-section">
                    <h3 class="section-title">
                        <el-icon>
                            <Camera />
                        </el-icon>
                        个人头像
                    </h3>

                    <el-form-item prop="avatar" class="form-item animate-form-item">
                        <div class="avatar-upload-container">
                            <el-upload class="avatar-uploader" :show-file-list="false" :on-change="handleAvatarChange"
                                :on-remove="handleAvatarRemove" :before-upload="beforeLogoUpload" accept="image/*"
                                :auto-upload="false">

                                <div class="avatar-upload-area" :class="{ 'has-avatar': ruleForm.avatar }">
                                    <el-image v-if="ruleForm.avatar" :src="ruleForm.avatar" class="avatar hover-scale"
                                        fit="cover" />

                                    <div v-else class="avatar-placeholder">
                                        <el-icon class="avatar-icon bounce-icon">
                                            <Plus />
                                        </el-icon>
                                        <div class="avatar-text">点击上传头像</div>
                                    </div>

                                    <div class="avatar-overlay">
                                        <el-icon>
                                            <Camera />
                                        </el-icon>
                                        <span>更换头像</span>
                                    </div>
                                </div>
                            </el-upload>

                            <div class="upload-tip">
                                <el-icon>
                                    <Camera />
                                </el-icon>
                                <span>支持JPG/PNG格式，且不超过5MB</span>
                            </div>
                        </div>
                    </el-form-item>
                </div>

                <!-- 操作按钮 -->
                <el-form-item class="button-group animate-form-item">
                    <div class="buttons-container">
                        <el-button type="primary" size="large" @click="handleRegister"
                            class="register-btn pulse-on-hover">
                            <el-icon>
                                <UserFilled />
                            </el-icon>
                            创建账户
                        </el-button>

                        <el-button plain size="large" @click="router.push('/login')" class="login-btn wobble-on-hover">
                            <el-icon>
                                <User />
                            </el-icon>
                            去登录
                        </el-button>
                    </div>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<style scoped>
/* 基础动画关键帧 */
@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
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

@keyframes scaleIn {
    from {
        opacity: 0;
        transform: scale(0.9);
    }

    to {
        opacity: 1;
        transform: scale(1);
    }
}

@keyframes formItemIn {
    from {
        opacity: 0;
        transform: translateX(-20px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes formSectionIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
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

@keyframes wobble {

    0%,
    100% {
        transform: rotate(0deg);
    }

    25% {
        transform: rotate(-3deg);
    }

    75% {
        transform: rotate(3deg);
    }
}

/* 动画类 */
.animate-fade-in {
    animation: fadeIn 0.8s ease-out;
}

.animate-slide-down {
    animation: slideDown 0.8s ease-out 0.2s both;
}

.animate-slide-up {
    animation: slideUp 0.8s ease-out 0.4s both;
}

.animate-scale-in {
    animation: scaleIn 0.8s ease-out 0.6s both;
}

.animate-form-section {
    animation: formSectionIn 0.6s ease-out both;
}

.animate-form-item {
    animation: formItemIn 0.5s ease-out both;
}

.title-char {
    display: inline-block;
    animation: bounce 0.6s ease-out both;
}

.pulse-icon {
    animation: pulse 2s infinite;
}

.bounce-icon {
    animation: bounce 2s infinite;
}

.hover-scale:hover {
    transform: scale(1.1);
    transition: transform 0.3s ease;
}

.pulse-on-hover:hover {
    animation: pulse 0.8s ease-in-out;
}

.wobble-on-hover:hover {
    animation: wobble 0.5s ease-in-out;
}

/* 主容器 */
.register-container {
    min-height: 100vh;
    background: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
}

/* 表单包装器 */
.form-wrapper {
    background: white;
    border-radius: 24px;
    padding: 40px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    max-width: 600px;
    width: 100%;
}

/* 表单头部 */
.form-header {
    text-align: center;
    margin-bottom: 40px;
}

.header-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    margin: 0 auto 20px;
    box-shadow: 0 8px 24px rgba(78, 205, 196, 0.3);
}

.form-title {
    font-size: 32px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 12px 0;
    font-family: "华文中宋", serif;
    letter-spacing: 1px;
}

.form-subtitle {
    font-size: 16px;
    color: #7f8c8d;
    margin: 0 0 24px 0;
    font-weight: 300;
}

.title-decoration {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
}

.decoration-line {
    width: 40px;
    height: 2px;
    background: linear-gradient(90deg, transparent, #4ecdc4, transparent);
}

.decoration-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #4ecdc4;
    animation: pulse 2s infinite;
}

/* 表单样式 */
.register-form {
    width: 100%;
}

.form-section {
    margin-bottom: 32px;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0 0 20px 0;
    font-size: 18px;
    color: #2c3e50;
    font-weight: 600;
}

.section-title .el-icon {
    color: #4ecdc4;
    font-size: 20px;
}

.form-item {
    margin-bottom: 20px;
}

.input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
    background: white;
    border-radius: 12px;
    border: 2px solid #e2e8f0;
    overflow: hidden;
    transition: all 0.3s ease;
}

.input-wrapper:hover {
    border-color: #cbd5e0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.input-wrapper:focus-within {
    border-color: #4ecdc4;
    box-shadow: 0 0 0 3px rgba(78, 205, 196, 0.1);
}

.input-icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f8fafc;
    color: #4ecdc4;
    border-right: 1px solid #e2e8f0;
}

.form-input {
    flex: 1;
    border: none;
}

.form-input :deep(.el-input__wrapper) {
    box-shadow: none;
    border-radius: 0;
    padding: 0 16px;
    background: transparent;
}

.form-input :deep(.el-input__inner) {
    height: 48px;
    line-height: 48px;
    font-size: 15px;
}

/* 头像上传 */
.avatar-upload-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
}

.avatar-uploader {
    display: flex;
    justify-content: center;
}

.avatar-upload-area {
    position: relative;
    width: 150px;
    height: 150px;
    border: 3px dashed #d1d5db;
    border-radius: 50%;
    overflow: hidden;
    transition: all 0.3s ease;
    cursor: pointer;
    background: white;
}

.avatar-upload-area:hover {
    border-color: #4ecdc4;
    box-shadow: 0 8px 24px rgba(78, 205, 196, 0.2);
}

.avatar-upload-area.has-avatar {
    border-style: solid;
    border-color: #4ecdc4;
}

.avatar {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #9ca3af;
}

.avatar-icon {
    font-size: 32px;
    color: #4ecdc4;
    margin-bottom: 8px;
}

.avatar-text {
    font-size: 14px;
    font-weight: 500;
}

.avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(78, 205, 196, 0.9);
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

.avatar-upload-area:hover .avatar-overlay {
    opacity: 1;
}

.upload-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #6b7280;
    text-align: center;
}

/* 按钮组 */
.button-group {
    margin-bottom: 0;
    margin-top: 32px;
}

.buttons-container {
    display: flex;
    gap: 16px;
    width: 100%;
}

.register-btn,
.login-btn {
    flex: 1;
    height: 50px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.register-btn {
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    color: white;
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.3);
}

.register-btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(78, 205, 196, 0.4);
}

.login-btn {
    background: transparent;
    border: 2px solid #4ecdc4;
    color: #4ecdc4;
}

.login-btn:hover {
    background: #4ecdc4;
    color: white;
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(78, 205, 196, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .register-container {
        padding: 16px;
    }

    .form-wrapper {
        padding: 24px;
        margin: 20px 0;
    }

    .form-title {
        font-size: 24px;
    }

    .form-subtitle {
        font-size: 14px;
    }

    .header-icon {
        width: 60px;
        height: 60px;
    }

    .form-section {
        margin-bottom: 20px;
    }

    .section-title {
        font-size: 16px;
    }

    .buttons-container {
        flex-direction: column;
    }

    .avatar-upload-area {
        width: 120px;
        height: 120px;
    }
}

@media (max-width: 480px) {
    .form-wrapper {
        padding: 20px;
    }

    .input-wrapper {
        flex-direction: row;
    }

    .input-icon {
        width: 40px;
        height: 40px;
    }

    .form-input :deep(.el-input__inner) {
        height: 40px;
        line-height: 40px;
        font-size: 14px;
    }
}

/* 滚动条美化 */
::-webkit-scrollbar {
    width: 6px;
}

::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}
</style>