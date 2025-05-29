<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox, ElTag, } from 'element-plus';
import { Shop, getShopDetail } from '@/api/shop';
import { UserDetail, getUserListByShopId, updateUserInfo } from '@/api/account';
import { Picture, Clock, User } from '@element-plus/icons-vue';
import router from '@/router';
import { Message, sendMessage } from '@/api/message';

const route = useRoute();
const shopId = ref<number>(Number(route.params.id));
console.log('Shop ID:', shopId.value);
const shopInfo = ref<Shop>({
    shopId: 0,
    name: '',
    ownerId: 0,
    iconUrl: '',
    description: '',
    rate: 0,
    isValid: 0
});
const staffList = ref<UserDetail[]>([]);
const pendingStaffList = ref<UserDetail[]>([]);
const loading = ref(true);

// 获取店铺详情
const fetchShopDetail = async () => {
    try {
        const response = await getShopDetail(shopId.value);
        shopInfo.value = response.data.data;
    } catch (error) {
        ElMessage.error('获取店铺信息失败');
        console.error(error);
    }
};

// 获取员工列表
const fetchStaffList = async () => {
    try {
        const response = await getUserListByShopId(shopId.value);
        const allStaff = response.data.data || [];

        // 分离已通过和待审核员工
        staffList.value = allStaff.filter((user: UserDetail) => user.isValidStaff === 1);
        pendingStaffList.value = allStaff.filter((user: UserDetail) => user.isValidStaff === 0);
    } catch (error) {
        ElMessage.error('获取员工列表失败');
        console.error(error);
    } finally {
        loading.value = false;
    }
};
// 处理员工通过审核
const handleStaffApprove = async (userId: number) => {
    try {
        // 调用API更新员工状态
        // 确认弹窗
        await ElMessageBox.confirm('确定要通过该员工的申请吗？', '操作确认', {
            type: 'warning'
        });
        // 更新用户状态为已通过(1)
        const userDetail = pendingStaffList.value.find(user => user.id === userId)

        const updatedUser = { ...userDetail, role: 'STAFF', isValidStaff: 1 } as UserDetail;

        const message: Message = {
            id: 0,
            content: "APPLICATION_APPROVED",
            isRead: false,
            fromUser: Number(sessionStorage.getItem('id')),
            toUser: userId,
            createdTime: new Date().toISOString()
        };
        await sendMessage(message);
        await updateUserInfo(updatedUser);
        ElMessage.success('员工已通过审核')
        fetchStaffList(); // 刷新员工列表
    } catch (error) {
        ElMessage.error('通过审核失败');
        console.error(error);
    }
};

// 处理员工拒绝申请
const handleStaffReject = async (userId: number) => {
    try {
        // 确认弹窗
        await ElMessageBox.confirm('确定要拒绝该员工的申请吗？', '操作确认', {
            type: 'warning'
        });

        // 更新用户状态为已拒绝(0)
        const userDetail = pendingStaffList.value.find(user => user.id === userId)
        const updatedUser = { ...userDetail, role: 'CUSTOMER', shopId: null, isValidStaff: 0 } as unknown as UserDetail;

        const message: Message = {
            id: 0,
            content: "APPLICATION_REJECTED",
            isRead: false,
            fromUser: Number(sessionStorage.getItem('id')),
            toUser: userId,
            createdTime: new Date().toISOString()
        };
        await sendMessage(message);
        await updateUserInfo(updatedUser);
        ElMessage.success('已拒绝员工申请')
        fetchStaffList(); // 刷新员工列表
    } catch (error) {
        ElMessage.error('拒绝申请失败');
        console.error(error);
    }
};

// 处理员工解雇
const handleStaffFire = async (userId: number) => {
    try {
        // 确认弹窗
        await ElMessageBox.confirm('确定要解雇该员工吗？', '操作确认', {
            type: 'warning'
        });

        // 更新用户状态为已解雇(0)
        const userDetail = staffList.value.find(user => user.id === userId)
        const updatedUser = { ...userDetail, role: 'CUSTOMER', shopId: null, isValidStaff: 0 } as unknown as UserDetail;

        const message: Message = {
            id: 0,
            content: "YOU_ARE_FIRED",
            isRead: false,
            fromUser: Number(sessionStorage.getItem('id')),
            toUser: userId,
            createdTime: new Date().toISOString()
        };
        await sendMessage(message);
        await updateUserInfo(updatedUser);
        ElMessage.success('已解雇员工')
        fetchStaffList(); // 刷新员工列表
    } catch (error) {
        ElMessage.error('解雇员工失败');
        console.error(error);
    }
};

onMounted(async () => {
    await Promise.all([fetchShopDetail(), fetchStaffList()]);
});
</script>

<template>
    <div class="my-shop-container">
        <!-- 店铺信息 -->
        <div class="shop-info-section">
            <div class="shop-header">
                <el-image :src="shopInfo?.iconUrl" fit="cover" class="shop-avatar">
                    <template #error>
                        <div class="image-error">
                            <el-icon>
                                <Picture />
                            </el-icon>
                            <span>店铺图片</span>
                        </div>
                    </template>
                </el-image>

                <div class="shop-details">
                    <h1 class="shop-name">{{ shopInfo.name }}</h1>
                    <el-rate :model-value="shopInfo.rate" disabled show-score text-color="#ff9900"
                        score-template="{value} 分" />
                    <p class="shop-description">{{ shopInfo.description }}</p>
                    <div class="shop-meta">
                        <el-tag type="info">店铺ID: {{ shopInfo.shopId }}</el-tag>
                        <el-tag type="success">店主ID: {{ shopInfo.ownerId }}</el-tag>
                    </div>
                    <div class="apply-section">
                        <el-button type="primary" @click="router.push(`/warehouse/${shopId}`)">
                            商品管理
                        </el-button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 员工管理 -->
        <div class="staff-management">
            <!-- 待审核员工 -->
            <div class="staff-section">
                <h3 class="section-title">
                    <el-icon>
                        <Clock />
                    </el-icon>
                    待审核员工 ({{ pendingStaffList.length }})
                </h3>

                <el-table :data="pendingStaffList" v-loading="loading" border>
                    <el-table-column prop="id" label="用户ID" width="100" />
                    <el-table-column prop="name" label="姓名" />
                    <el-table-column prop="username" label="用户名" />
                    <el-table-column prop="telephone" label="电话" width="120" />
                    <el-table-column prop="email" label="邮箱" />
                    <el-table-column prop="location" label="地址" />
                    <el-table-column label="操作" width="200">
                        <template #default="scope">
                            <el-button size="small" type="success" @click="handleStaffApprove(scope.row.id)">
                                通过
                            </el-button>
                            <el-button size="small" type="danger" @click="handleStaffReject(scope.row.id)">
                                拒绝
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>

            <!-- 已雇佣员工 -->
            <div class="staff-section">
                <h3 class="section-title">
                    <el-icon>
                        <User />
                    </el-icon>
                    已雇佣员工 ({{ staffList.length }} )
                </h3>

                <el-table :data="staffList" v-loading="loading" border>
                    <el-table-column prop="id" label="用户ID" width="100" />
                    <el-table-column prop="name" label="姓名" />
                    <el-table-column prop="username" label="用户名" />
                    <el-table-column prop="email" label="邮箱" />
                    <el-table-column prop="location" label="地址" />
                    <el-table-column prop="telephone" label="电话" width="120" />
                    <el-table-column label="操作" width="200">
                        <template #default="scope">
                            <el-button size="small" type="success" @click="handleStaffFire(scope.row.id)">
                                解雇
                            </el-button>
                        </template>
                    </el-table-column>

                </el-table>
            </div>
        </div>
    </div>
</template>

<style scoped>
.apply-section {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #eee;
}

.my-shop-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.shop-info-section {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.shop-header {
    display: flex;
    gap: 24px;
    align-items: center;
}

.shop-avatar {
    width: 150px;
    height: 150px;
    border-radius: 8px;
    flex-shrink: 0;
}

.shop-details {
    flex: 1;
}

.shop-name {
    margin: 0 0 16px 0;
    font-size: 24px;
    color: #303133;
}

.shop-description {
    color: #606266;
    line-height: 1.6;
    margin: 16px 0;
}

.shop-meta {
    display: flex;
    gap: 10px;
    margin-top: 16px;
}

.staff-management {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.staff-section {
    margin-bottom: 30px;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0 0 16px 0;
    font-size: 18px;
    color: #303133;
    padding-bottom: 8px;
    border-bottom: 1px solid #eee;
}

.image-error {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #909399;
}

.image-error .el-icon {
    font-size: 40px;
    margin-bottom: 8px;
}
</style>