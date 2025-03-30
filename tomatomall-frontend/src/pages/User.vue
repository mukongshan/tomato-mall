<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import {UploadProps, UploadFile, FormRules, ElMessage} from 'element-plus'
import type { UserDetail, AccountDetail } from '@/api/account.ts'
import { getUserDetails, updateUser } from '@/api/account.ts'
import {UserFilled} from "@element-plus/icons-vue";

// 用户信息
const userInfo = ref<UserDetail>({
  username: '',
  name: '',
  role: '',
  avatar: '',
  telephone: '',
  email: '',
  location: ''
})

const username = sessionStorage.getItem('username') as string

// 编辑表单数据（包含密码字段）
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
  password: [],
  confirmPassword: [
    {
      validator: (_, value, callback) => {
        if (value && value !==editForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})


// 头像上传处理
const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(rawFile.type)
  const isSizeValid = rawFile.size <= 5 * 1024 * 1024 // 5MB

  if (!isImage) {
    ElMessage.error('头像必须为JPG/PNG格式')
    return false
  }
  if (!isSizeValid) {
    ElMessage.error('头像大小不能超过5MB')
    return false
  }
  return true
}

const handleAvatarChange: UploadProps['onChange'] = (uploadFile: UploadFile) => {
  if (uploadFile.raw) {
    editForm.avatar = URL.createObjectURL(uploadFile.raw)
    // 实际项目中这里应该调用API上传图片到服务器，并返回图片URL
  }
}

const handleAvatarRemove = () => {
  editForm.avatar = ''
}

// 获取用户信息（不包含密码）
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
// 重置编辑表单（保留密码字段为空）
const resetEditForm = () => {
  Object.assign(editForm, {
    ...JSON.parse(JSON.stringify(userInfo.value)),
    password: ''
  })
  editForm.confirmPassword = ''
}

// 提交更新（传递所有字段）
const handleUpdate = () => {
  formRef.value?.validate(async (valid:boolean) => {
    if (!valid) return
    try {
      // 注意：这里会提交所有字段，包括空的密码字段
      await updateUser({ ...editForm })
      await fetchUserDetails() // 重新获取最新数据
      editMode.value = false
      ElMessage.success('更新成功')
    } catch (error) {
      console.error('更新失败:', error)
      ElMessage.error('更新失败')
    }
  })
}

// 取消编辑
const cancelEdit = () => {
  resetEditForm()
  editMode.value = false
  formRef.value?.clearValidate()
}


</script>

<template>
  <el-main class="main-container">
    <!-- 左侧信息展示卡 -->
    <el-card class="aside-card">
      <div class="avatar-area">
        <el-avatar :size="80" :src="userInfo.avatar || ''" :icon="UserFilled">
        </el-avatar>
        <span class="avatar-text">欢迎您，{{ userInfo.name }}</span>
      </div>
      <el-divider></el-divider>
      <el-descriptions
          :column="1" border
          title="个人信息">
        <template #extra>
          <el-button
              type="primary"
              @click="editMode = true"
              v-if="!editMode">
            编辑个人信息
          </el-button>
        </template>
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="角色"><el-tag>
          {{ userInfo.role === 'CUSTOMER' ? '顾客' :
            userInfo.role === 'STAFF' ? '员工' :
                userInfo.role === 'SHOPKEEPER' ? '店主' :
                    userInfo.role === 'ADMINISTRATOR' ? '管理员' : userInfo.role }}
        </el-tag></el-descriptions-item>
        <el-descriptions-item label="电话">{{ userInfo.telephone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ userInfo.location }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 右侧编辑面板 -->
    <el-card class="change-card">
      <template #header>
        <div class="card-header">
          <span>编辑个人信息</span>
          <div v-if="editMode">
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" @click="handleUpdate">保存</el-button>
          </div>
        </div>
      </template>

      <el-form
          ref="formRef"
          :model="editForm"
          :rules="rules"
          >

        <!-- 头像上传 -->
        <el-form-item label="头像">
          <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :on-change="handleAvatarChange"
              :on-remove="handleAvatarRemove"
              accept="image/*"
              :auto-upload="false"
              :disabled="!editMode">
            <el-image v-if="editForm.avatar" :src="editForm.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <span v-if="editMode">支持JPG/PNG格式，大小不超过5MB</span>
        </el-form-item>

        <!-- 基本信息 -->
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" :disabled="!editMode" />
        </el-form-item>

        <el-form-item label="用户角色" prop="role">
          <el-select v-model="editForm.role" :disabled="!editMode">
            <el-option label="顾客" value="CUSTOMER" />
            <el-option label="员工" value="STAFF" />
            <el-option label="店主" value="SHOPKEEPER" />
            <el-option label="管理员" value="ADMINISTRATOR" />
          </el-select>
        </el-form-item>

        <el-form-item label="电话" prop="telephone">
          <el-input v-model="editForm.telephone" :disabled="!editMode" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" :disabled="!editMode" />
        </el-form-item>

        <el-form-item label="地址" prop="location">
          <el-input v-model="editForm.location" :disabled="!editMode" />
        </el-form-item>

        <!-- 密码修改 -->
        <el-form-item label="新密码" prop="password">
          <el-input
              v-model="editForm.password"
              type="password"
              :disabled="!editMode"
              placeholder="留空则不修改密码"
              show-password />
        </el-form-item>

        <el-form-item
            label="确认密码"
            prop="confirmPassword"
            :rules="rules.confirmPassword">
          <el-input
              v-model="editForm.confirmPassword"
              type="password"
              :disabled="!editMode"
              placeholder="再次输入新密码"
              show-password />
        </el-form-item>
      </el-form>
    </el-card>
  </el-main>
</template>

<style scoped>

/* 在全局样式或App.vue中添加 */
.main-container {
  display: flex;
  flex-direction: row;
  gap: 5px;
  justify-content: flex-start; /* 改为从左侧开始 */
  min-height: calc(100vh - 60px); /* 添加这行，确保至少占满整个视口高度 */
  min-width: 100vw;
  box-sizing: border-box; /* 防止padding影响高度 */
  padding-left: 0; /* 左侧不留白 */
}


.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.aside-card {
  height: 100%; /* 继承父容器高度 */
  width: 34%;
  overflow-y: auto; /* 内容过多时滚动 */
}
.change-card {
  height: 100%; /* 继承父容器高度 */
  width: 66%;
  overflow-y: auto; /* 内容过多时滚动 */
}

.avatar-area {
  display: flex;
  justify-content: space-around;
  align-items: center;
  gap: 30px;
}
.avatar-text {
  font-size: x-large;
  font-weight: bolder;
  padding-right: 40px;
}

</style>