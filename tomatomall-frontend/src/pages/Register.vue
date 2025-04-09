<script setup lang="ts">
import { AccountDetail } from "@/api/account.ts";
import { reactive, ref } from "vue";
import {ComponentSize, ElLoading, ElMessage, FormInstance, FormRules, UploadFile, UploadProps} from 'element-plus'
import router from "../router/index.ts";
import { createAccount } from "@/api/account";  //不能用@ 为什么不生效
import { Plus } from '@element-plus/icons-vue'
import { imageProcess } from "@/utils/UploadImage.ts";

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
    role: '',
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
  role: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
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
const handleAvatarChange: UploadProps['onChange'] = async(uploadFile:UploadFile) => {
  if (uploadFile.raw) {
    // 生成预览URL
        ruleForm.avatar = await imageProcess(uploadFile.raw)
        console.log(ruleForm.avatar)
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
    try{
      const response = await createAccount(submitData);
      console.log(response)
      if (response.data.code === '200') {
        ElMessage.success("注册成功")
        await router.push("/login")
      }else{
        ElMessage.warning(response.data.message || "注册未完成");
      }
    }catch(error:any){
      ElMessage.warning(error)
    }finally{
      loading.close()
    }
  } catch (e:any) {
    ElMessage.error("表单验证失败")
    console.error(e)
  }
}
</script>

<template >
  <div class="form-container">
    <el-form
        class="centered-form"
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="rules"
        :size="formSize"
        label-width="auto"
        status-icon
    >
      <h2 class="form-title">用户注册</h2>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="ruleForm.username" />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
            v-model="ruleForm.password"
            placeholder="••••••••"
            show-password
            type="password"
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
            v-model="ruleForm.confirmPassword"
            placeholder="••••••••"
            show-password
            type="password"
        />
      </el-form-item>

      <el-form-item label="真实姓名" prop="name">
        <el-input v-model="ruleForm.name" />
      </el-form-item>

      <el-form-item label="用户角色" prop="role">
        <el-select v-model="ruleForm.role">
          <el-option label="顾客" value="CUSTOMER" />
          <el-option label="员工" value="STAFF" />
          <el-option label="店主" value="SHOPKEEPER" />
          <el-option label="管理员" value="ADMINISTRATOR" />
        </el-select>
      </el-form-item>

      <el-form-item label="手机号" prop="telephone">
        <el-input v-model="ruleForm.telephone" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="ruleForm.email" />
      </el-form-item>

      <el-form-item label="位置" prop="location">
        <el-input v-model="ruleForm.location" />
      </el-form-item>

      <el-form-item label="头像" prop="avatar">
        <!-- 只接受图片
             关闭自动上传
        -->
        <!-- on-change 图片上传触发 这里是转为url -->
         <!-- on-remove 删除 -->
        <!-- before-upload 上传前验证 -->
        <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :on-change="handleAvatarChange" 
            :on-remove="handleAvatarRemove"
            :before-upload = "beforeLogoUpload"
            accept="image/*"
            :auto-upload="false"
        >
          <!--图片上传模式-->
          <!-- 有头像是显示预览图-->
          <el-image
              v-if="ruleForm.avatar"
              :src="ruleForm.avatar"
              class="avatar"
              fit="cover"
          />
          <!-- 无图像时候显示上传图标-->
          <el-icon v-else class="avatar-uploader-icon">
            <Plus/>
          </el-icon>
        </el-upload>
        <div class="el-upload__tip">
          支持JPG/PNG格式，且不超过5MB
        </div>
      </el-form-item>

      <el-form-item class="button-group">
        <el-button plain type="primary" @click="handleRegister">
          创建账户
        </el-button>
        <el-button plain type="primary" @click="router.push('/login')">
          去登录
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
  margin: 40px auto; /* 上下40px，左右自动居中 */
}

.avatar-uploader {
  border: 1px dashed var(--el-border-color);  /* 虚线边框，使用Element Plus的边框颜色变量 */
  border-radius: 6px;                        /* 6像素圆角 */
  cursor: pointer;                           /* 鼠标悬停时显示手型指针（可点击提示） */
  position: relative;                        /* 相对定位（为子元素绝对定位提供基准） */
  overflow: hidden;                          /* 隐藏超出容器的内容（如图片溢出部分） */
  width: 150px;                              /* 固定宽度150px */
  height: 150px;                             /* 固定高度150px */
  display: flex;                             /* 弹性布局 */
  align-items: center;                       /* 垂直居中 */
  justify-content: center;                   /* 水平居中 */
  transition: var(--el-transition-duration-fast); /* 使用Element Plus的快速过渡时间 */
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);     /* 悬停时边框变为Element Plus主题色 */
}

.avatar {
  width: 100%;                              /* 宽度撑满容器 */
  height: 100%;                             /* 高度撑满容器 */
  object-fit: cover;                        /* 保持比例填充，可能裁剪图片 */
}

.avatar-uploader-icon {
  font-size: 28px;                          /* 图标大小28px */
  color: #8c939d;                           /* 中性灰色（#8c939d） */
  text-align: center;                       /* 文字居中（影响图标位置） */
}

.el-upload__tip {
  margin-top: 8px;                          /* 上边距8px（与上传区域间隔） */
  font-size: 12px;                          /* 小号字体 */
  color: var(--el-text-color-secondary);    /* 使用Element Plus的次要文本色 */
}
</style>