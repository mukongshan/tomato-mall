<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { ComponentSize, ElLoading, ElMessage, FormInstance, FormRules } from 'element-plus'
import router from "../router/index.ts";
import { getUserDetails, LoginCredentials, UserDetail } from "@/api/account";
import { login } from "@/api/account";
import { isLogin, checkRole, messageLoad } from '@/components/LoginEvent.ts';
import { User, Lock, Right, UserFilled } from '@element-plus/icons-vue'

// 表单尺寸
const formSize = ref<ComponentSize>('default')
// 表单引用(调用表单方法)
const ruleFormRef = ref<FormInstance>()
// 表单数据对象
const ruleForm = reactive<LoginCredentials>({
    username: '',
    password: ''
})

const rules = reactive<FormRules<typeof ruleForm>>({
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 10, message: '用户长度需在2-10字符之间', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
})

const handleLogin = async () => {
    if (!ruleFormRef.value) return

    try {
        // 表单验证
        const isValid = await ruleFormRef.value.validate()
        if (!isValid) return
        const loading = ElLoading.service({
            lock: true,
            text: '登录中...'
        })
        try {
            const response = await login(ruleForm);
            if (response.data.code === '200') {
                ElMessage.success({
                    message: "登录成功",
                    duration: 500  // 0.5 秒后自动关闭
                });
                sessionStorage.setItem('token', response.data.data);
                try {
                    const response = await getUserDetails(ruleForm.username); // 等待 Promise 解析
                    const userDetail: UserDetail = response.data.data; // 提取 Axios 返回的实际数据
                    sessionStorage.setItem('username', String(userDetail.username));
                    sessionStorage.setItem('role', String(userDetail.role));
                    sessionStorage.setItem('id', String(userDetail.id));
                    sessionStorage.setItem('shopId', String(userDetail.shopId));
                    sessionStorage.setItem('isValidStaff', String(userDetail.isValidStaff));
                    isLogin.value = true; // 更新登录状态
                    await checkRole(); // 检查用户角色
                    await messageLoad(); // 重新加载消息
                    await router.push("/") // 跳转到首页
                } catch (error) {
                    console.error("获取用户详情失败:", error);
                }
            } else {
                ElMessage.error(response.data.message || "登录失败");
            }
        } catch (err: any) {
            ElMessage.error("登录失败");
        } finally {
            loading.close()
        }
    } catch (e: any) {
        ElMessage.error("登录失败")
        console.error(e)
    }
}
</script>

<template>
    <div class="login-container">
        <!-- 主表单区域 -->
        <div class="form-wrapper animate-fade-in">
            <div class="form-header animate-slide-down">
                <div class="header-icon pulse-icon">
                    <el-icon size="48">
                        <UserFilled />
                    </el-icon>
                </div>
                <h2 class="form-title">
                    <span class="title-char" v-for="(char, index) in '用户登录'" :key="index"
                        :style="{ animationDelay: `${index * 0.1}s` }">
                        {{ char }}
                    </span>
                </h2>
                <p class="form-subtitle animate-slide-up">欢迎回来，请登录您的账户</p>
                <div class="title-decoration">
                    <div class="decoration-line"></div>
                    <div class="decoration-dot"></div>
                    <div class="decoration-line"></div>
                </div>
            </div>

            <div class="login-form animate-scale-in">
                <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" :size="formSize" class="form-content"
                    label-width="0" status-icon>

                    <!-- 用户名输入 -->
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

                    <!-- 密码输入 -->
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

                    <!-- 登录按钮 -->
                    <div class="button-container animate-form-item">
                        <el-button type="primary" size="large" @click="handleLogin" class="login-btn pulse-on-hover">
                            <el-icon>
                                <Right />
                            </el-icon>
                            登录
                        </el-button>
                    </div>

                    <!-- 注册链接 -->
                    <div class="form-footer animate-form-item">
                        <div class="register-link">
                            <span class="link-text">还没有账号？</span>
                            <el-button type="text" @click="router.push('/register')"
                                class="register-btn wobble-on-hover">
                                <el-icon>
                                    <UserFilled />
                                </el-icon>
                                立即注册
                            </el-button>
                        </div>
                    </div>
                </el-form>
            </div>
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

@keyframes glow {

    0%,
    100% {
        box-shadow: 0 4px 16px rgba(78, 205, 196, 0.3);
    }

    50% {
        box-shadow: 0 8px 25px rgba(78, 205, 196, 0.5);
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

.animate-form-item {
    animation: formItemIn 0.5s ease-out both;
}

.animate-form-item:nth-child(1) {
    animation-delay: 0.8s;
}

.animate-form-item:nth-child(2) {
    animation-delay: 1.0s;
}

.animate-form-item:nth-child(3) {
    animation-delay: 1.2s;
}

.animate-form-item:nth-child(4) {
    animation-delay: 1.4s;
}

.title-char {
    display: inline-block;
    animation: bounce 0.6s ease-out both;
}

.pulse-icon {
    animation: pulse 2s infinite;
}

.pulse-on-hover:hover {
    animation: glow 0.8s ease-in-out;
}

.wobble-on-hover:hover {
    animation: wobble 0.5s ease-in-out;
}

/* 主容器 */
.login-container {
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
    max-width: 450px;
    width: 100%;
    transition: transform 0.3s ease;
}

.form-wrapper:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
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
.login-form {
    width: 100%;
}

.form-content {
    width: 100%;
}

/* 重写表单项样式，消除默认间距和对齐问题 */
.form-item {
    margin-bottom: 24px;
    width: 100%;
}

.form-item :deep(.el-form-item__content) {
    margin-left: 0 !important;
    width: 100%;
    line-height: normal;
}

.form-item :deep(.el-form-item__error) {
    padding-top: 4px;
}

/* 确保所有输入框宽度一致 */
.input-wrapper {
    width: 100%;
    position: relative;
    display: flex;
    align-items: center;
    background: white;
    border-radius: 12px;
    border: 2px solid #e2e8f0;
    overflow: hidden;
    transition: all 0.3s ease;
    box-sizing: border-box;
}

.input-wrapper:hover {
    border-color: #cbd5e0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    transform: translateY(-2px);
}

.input-wrapper:focus-within {
    border-color: #4ecdc4;
    box-shadow: 0 0 0 3px rgba(78, 205, 196, 0.1);
    transform: translateY(-2px);
}

.input-icon {
    width: 50px;
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f8fafc;
    color: #4ecdc4;
    border-right: 1px solid #e2e8f0;
    transition: all 0.3s ease;
    flex-shrink: 0;
}

.input-wrapper:focus-within .input-icon {
    background: rgba(78, 205, 196, 0.1);
    color: #44a08d;
}

.form-input {
    flex: 1;
    border: none;
    min-width: 0;
    /* 防止flex子项收缩问题 */
}

.form-input :deep(.el-input__wrapper) {
    box-shadow: none !important;
    border-radius: 0;
    padding: 0 16px;
    background: transparent;
    width: 100%;
}

.form-input :deep(.el-input__inner) {
    height: 50px;
    line-height: 50px;
    font-size: 15px;
    color: #2c3e50;
    width: 100%;
    border: none;
    outline: none;
}

.form-input :deep(.el-input__inner::placeholder) {
    color: #a0aec0;
}

/* 按钮容器和样式 */
.button-container {
    width: 100%;
    margin: 32px 0 24px 0;
}

.login-btn {
    width: 100%;
    height: 52px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    color: white;
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.3);
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    box-sizing: border-box;
}

.login-btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(78, 205, 196, 0.4);
}

.login-btn:active {
    transform: translateY(-1px);
}

/* 底部链接 */
.form-footer {
    text-align: center;
    padding-top: 24px;
    border-top: 1px solid #f0f0f0;
    width: 100%;
}

.register-link {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.link-text {
    font-size: 14px;
    color: #7f8c8d;
}

.register-btn {
    font-size: 14px;
    font-weight: 600;
    color: #4ecdc4;
    padding: 8px 16px;
    border-radius: 8px;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 4px;
}

.register-btn:hover {
    background: rgba(78, 205, 196, 0.1);
    color: #44a08d;
    transform: scale(1.05);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .login-container {
        padding: 16px;
    }

    .form-wrapper {
        padding: 30px 24px;
        margin: 20px 0;
    }

    .form-title {
        font-size: 26px;
    }

    .form-subtitle {
        font-size: 14px;
    }

    .header-icon {
        width: 60px;
        height: 60px;
    }

    .input-wrapper {
        border-radius: 10px;
    }

    .input-icon {
        width: 45px;
        height: 45px;
    }

    .form-input :deep(.el-input__inner) {
        height: 45px;
        line-height: 45px;
        font-size: 14px;
    }

    .login-btn {
        height: 48px;
        font-size: 15px;
    }

    .register-link {
        flex-direction: column;
        gap: 12px;
    }
}

@media (max-width: 480px) {
    .form-wrapper {
        padding: 24px 20px;
    }

    .form-title {
        font-size: 22px;
    }

    .input-icon {
        width: 40px;
        height: 40px;
    }

    .form-input :deep(.el-input__inner) {
        height: 40px;
        line-height: 40px;
    }

    .login-btn {
        height: 44px;
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