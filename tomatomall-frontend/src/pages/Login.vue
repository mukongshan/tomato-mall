<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { ComponentSize, ElLoading, ElMessage, FormInstance, FormRules } from 'element-plus'
import router from "../router/index.ts";
import { getUserDetails, LoginCredentials, UserDetail } from "@/api/account";
import { login } from "@/api/account";
import { isLogin, checkRole, messageLoad } from '@/components/LoginEvent.ts';


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
                ElMessage.success("登录成功");
                sessionStorage.setItem('token', response.data.data);
                sessionStorage.setItem('username', ruleForm.username);
                try {
                    const response = await getUserDetails(ruleForm.username); // 等待 Promise 解析
                    const userDetail: UserDetail = response.data.data; // 提取 Axios 返回的实际数据
                    sessionStorage.setItem('role', userDetail.role);
                    sessionStorage.setItem('id', String(userDetail.id));
                    sessionStorage.setItem('shopId', String(userDetail.shopId));
                    sessionStorage.setItem('isValidStaff', String(userDetail.isValidStaff));
                } catch (error) {
                    console.error("获取用户详情失败:", error);
                }
                isLogin.value = true; // 更新登录状态
                checkRole(); // 检查用户角色
                messageLoad(); // 重新加载消息
                await router.push("/user") // 跳转到首页或其他页面
            } else {
                ElMessage.warning(response.data.message || "登录失败");
            }
        } catch (err: any) {
            ElMessage.warning(err || "登录失败");
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
    <div class="form-container">
        <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" :size="formSize" class="centered-form"
            label-width="auto" status-icon>
            <h2 class="form-title">用户登录</h2>

            <el-form-item label="用户名" prop="username">
                <el-input v-model="ruleForm.username" placeholder="请输入用户名" />
            </el-form-item>

            <el-form-item label="密码" prop="password">
                <el-input v-model="ruleForm.password" placeholder="请输入密码" show-password type="password" />
            </el-form-item>

            <el-form-item class="button-group">
                <el-button style="width: 100%" type="primary" @click="handleLogin">
                    登录
                </el-button>
            </el-form-item>

            <div class="form-footer">
                <el-link type="primary" @click="router.push('/register')">没有账号？立即注册</el-link>
            </div>
        </el-form>
    </div>
</template>

<style scoped>
.form-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
}

.centered-form {
    width: 400px;
    padding: 30px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.form-title {
    text-align: center;
    margin-bottom: 30px;
    color: #333;
}

.button-group {
    margin-top: 30px;
}

.form-footer {
    text-align: center;
    margin-top: 20px;
}
</style>