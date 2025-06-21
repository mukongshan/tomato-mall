<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox, ElTag, } from 'element-plus';
import { Shop, getShopDetail } from '@/api/shop';
import { UserDetail, getUserListByShopId, updateUserInfo } from '@/api/account';
import { Picture, Clock, User, Shop as ShopIcon, Check, Close, ShoppingCart, DataAnalysis } from '@element-plus/icons-vue';
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
const isPageLoaded = ref(false);

// 获取店铺详情
const fetchShopDetail = async () => {
    const response = await getShopDetail(shopId.value);
    shopInfo.value = response.data.data;
};

// 获取员工列表
const fetchStaffList = async () => {
    const response = await getUserListByShopId(shopId.value);
    const allStaff = response.data.data || [];

    // 分离已通过和待审核员工
    staffList.value = allStaff.filter((user: UserDetail) => user.isValidStaff === 1);
    pendingStaffList.value = allStaff.filter((user: UserDetail) => user.isValidStaff === 0);

    loading.value = false;
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

    // 延迟设置页面加载完成
    await nextTick();
    setTimeout(() => {
        isPageLoaded.value = true;
    }, 500);
});
</script>

<template>
    <div class="page-container">
        <!-- 页面加载动画 -->
        <div v-if="!isPageLoaded" class="page-loading">
            <div class="loading-content">
                <div class="loading-icon">
                    <el-icon size="60" class="rotate-icon">
                        <ShopIcon />
                    </el-icon>
                </div>
                <div class="loading-text">
                    <span class="loading-char" v-for="(char, index) in '加载我的店铺'" :key="index"
                        :style="{ animationDelay: `${index * 0.1}s` }">
                        {{ char }}
                    </span>
                </div>
            </div>
        </div>

        <!-- 主要内容 -->
        <div v-else class="my-shop-container animate-page-enter">
            <!-- 面包屑导航 -->
            <div class="breadcrumb animate-slide-down">
                <span class="breadcrumb-item">首页</span>
                <span class="breadcrumb-separator">/</span>
                <span class="breadcrumb-item">我的店铺</span>
                <span class="breadcrumb-separator">/</span>
                <span class="breadcrumb-current">{{ shopInfo.name }}</span>
            </div>

            <!-- 页面标题 -->
            <div class="page-header animate-slide-down" style="--delay: 0.2s">
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
                    <span v-for="(char, index) in '我的店铺管理'" :key="index" class="title-char"
                        :style="{ animationDelay: `${index * 0.05}s` }">
                        {{ char }}
                    </span>
                </h1>
                <p class="page-subtitle animate-fade-in-up">管理您的店铺信息和员工团队</p>
            </div>

            <!-- 统计信息 -->
            <div class="stats-overview animate-slide-up-stagger">
                <div class="stat-item animate-stat-appear" style="--delay: 0.4s">
                    <div class="stat-icon pending">
                        <el-icon>
                            <Clock />
                        </el-icon>
                    </div>
                    <div class="stat-info">
                        <div class="stat-number">{{ pendingStaffList.length }}</div>
                        <div class="stat-label">待审核员工</div>
                    </div>
                </div>
                <div class="stat-item animate-stat-appear" style="--delay: 0.5s">
                    <div class="stat-icon approved">
                        <el-icon>
                            <User />
                        </el-icon>
                    </div>
                    <div class="stat-info">
                        <div class="stat-number">{{ staffList.length }}</div>
                        <div class="stat-label">在职员工</div>
                    </div>
                </div>
                <div class="stat-item animate-stat-appear" style="--delay: 0.6s">
                    <div class="stat-icon total">
                        <el-icon>
                            <DataAnalysis />
                        </el-icon>
                    </div>
                    <div class="stat-info">
                        <div class="stat-number">{{ shopInfo.rate?.toFixed(1) || '0.0' }}</div>
                        <div class="stat-label">店铺评分</div>
                    </div>
                </div>
            </div>

            <!-- 店铺信息 -->
            <div class="shop-info-section animate-card-enter" style="--delay: 0.8s">
                <div class="card-header">
                    <div class="section-title">
                        <div class="title-icon shop">
                            <el-icon>
                                <ShopIcon />
                            </el-icon>
                        </div>
                        <h3>店铺信息</h3>
                    </div>
                </div>

                <div class="shop-header">
                    <div class="shop-avatar-container animate-avatar-appear">
                        <el-image :src="shopInfo?.iconUrl" fit="cover" class="shop-avatar">
                            <template #error>
                                <div class="image-error animate-pulse">
                                    <el-icon size="40">
                                        <Picture />
                                    </el-icon>
                                    <span>店铺图片</span>
                                </div>
                            </template>
                        </el-image>
                        <div class="avatar-overlay">
                            <span>店铺头像</span>
                        </div>
                    </div>

                    <div class="shop-details animate-slide-in-right">
                        <h1 class="shop-name">{{ shopInfo.name }}</h1>
                        <div class="shop-rating">
                            <el-rate :model-value="shopInfo.rate" disabled show-score text-color="#ff9900"
                                score-template="{value} 分" class="rate-display" />
                        </div>
                        <p class="shop-description">{{ shopInfo.description }}</p>
                        <div class="shop-meta">
                            <el-tag type="info" class="meta-tag animate-tag-appear" style="--delay: 0.1s">
                                店铺ID: {{ shopInfo.shopId }}
                            </el-tag>
                            <el-tag type="success" class="meta-tag animate-tag-appear" style="--delay: 0.2s">
                                店主ID: {{ shopInfo.ownerId }}
                            </el-tag>
                        </div>
                        <div class="apply-section">
                            <el-button type="primary" @click="router.push(`/warehouse/${shopId}`)"
                                class="manage-btn animate-pulse-btn" :icon="ShoppingCart">
                                商品管理
                            </el-button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 员工管理 -->
            <div class="staff-management animate-section-appear" style="--delay: 1s">
                <!-- 待审核员工 -->
                <div class="staff-section animate-table-appear" style="--delay: 1.2s">
                    <div class="section-header">
                        <div class="section-title">
                            <div class="title-icon pending">
                                <el-icon>
                                    <Clock />
                                </el-icon>
                            </div>
                            <h3>待审核员工</h3>
                            <div class="count-badge pending">{{ pendingStaffList.length }}</div>
                        </div>
                    </div>

                    <div class="table-container">
                        <el-table :data="pendingStaffList" v-loading="loading" class="enhanced-table pending-table"
                            border stripe>
                            <el-table-column prop="id" label="用户ID" width="100" align="center" />
                            <el-table-column prop="name" label="姓名" min-width="120" />
                            <el-table-column prop="username" label="用户名" min-width="120" />
                            <el-table-column prop="telephone" label="电话" width="140" />
                            <el-table-column prop="email" label="邮箱" min-width="180" />
                            <el-table-column prop="location" label="地址" min-width="150" />
                            <el-table-column label="操作" width="200" align="center">
                                <template #default="scope">
                                    <div class="action-buttons">
                                        <el-button size="small" type="success" @click="handleStaffApprove(scope.row.id)"
                                            class="approve-btn" :icon="Check">
                                            通过
                                        </el-button>
                                        <el-button size="small" type="danger" @click="handleStaffReject(scope.row.id)"
                                            class="reject-btn" :icon="Close">
                                            拒绝
                                        </el-button>
                                    </div>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </div>

                <!-- 已雇佣员工 -->
                <div class="staff-section animate-table-appear" style="--delay: 1.4s">
                    <div class="section-header">
                        <div class="section-title">
                            <div class="title-icon approved">
                                <el-icon>
                                    <User />
                                </el-icon>
                            </div>
                            <h3>已雇佣员工</h3>
                            <div class="count-badge approved">{{ staffList.length }}</div>
                        </div>
                    </div>

                    <div class="table-container">
                        <el-table :data="staffList" v-loading="loading" class="enhanced-table approved-table" border
                            stripe>
                            <el-table-column prop="id" label="用户ID" width="100" align="center" />
                            <el-table-column prop="name" label="姓名" min-width="120" />
                            <el-table-column prop="username" label="用户名" min-width="120" />
                            <el-table-column prop="email" label="邮箱" min-width="180" />
                            <el-table-column prop="location" label="地址" min-width="150" />
                            <el-table-column prop="telephone" label="电话" width="140" />
                            <el-table-column label="操作" width="120" align="center">
                                <template #default="scope">
                                    <el-button size="small" type="danger" @click="handleStaffFire(scope.row.id)"
                                        class="fire-btn" :icon="Close">
                                        解雇
                                    </el-button>
                                </template>
                            </el-table-column>
                        </el-table>
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
        transform: translateY(40px) scale(0.95);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

@keyframes slideInRight {
    from {
        opacity: 0;
        transform: translateX(40px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes avatarAppear {
    from {
        opacity: 0;
        transform: scale(0.5) rotate(-180deg);
    }

    to {
        opacity: 1;
        transform: scale(1) rotate(0deg);
    }
}

@keyframes statAppear {
    from {
        opacity: 0;
        transform: translateY(30px) scale(0.9);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

@keyframes sectionAppear {
    from {
        opacity: 0;
        transform: translateY(50px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes tableAppear {
    from {
        opacity: 0;
        transform: translateY(40px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes tagAppear {
    from {
        opacity: 0;
        transform: scale(0.8);
    }

    to {
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

/* 动画类 */
.animate-page-enter {
    animation: pageEnter 0.8s ease-out;
}

.animate-slide-down {
    animation: slideDown 0.6s ease-out calc(0.2s + var(--delay, 0s)) both;
}

.animate-bounce-in {
    animation: bounceIn 1s ease-out 0.6s both;
}

.animate-title-reveal {
    animation: titleReveal 0.8s ease-out 0.8s both;
}

.title-char {
    display: inline-block;
    animation: charAppear 0.5s ease-out both;
}

.animate-fade-in-up {
    animation: fadeInUp 0.6s ease-out 1s both;
}

.animate-slide-up-stagger {
    animation: fadeInUp 0.8s ease-out 1.2s both;
}

.animate-stat-appear {
    animation: statAppear 0.6s ease-out calc(1.4s + var(--delay)) both;
}

.animate-card-enter {
    animation: cardEnter 0.8s ease-out calc(1.6s + var(--delay)) both;
}

.animate-slide-in-right {
    animation: slideInRight 0.8s ease-out 1.8s both;
}

.animate-avatar-appear {
    animation: avatarAppear 1s ease-out 2s both;
}

.animate-section-appear {
    animation: sectionAppear 0.6s ease-out calc(2.2s + var(--delay)) both;
}

.animate-table-appear {
    animation: tableAppear 0.6s ease-out calc(2.4s + var(--delay)) both;
}

.animate-tag-appear {
    animation: tagAppear 0.5s ease-out calc(2.6s + var(--delay)) both;
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
}

/* 主容器 */
.page-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #f8fafc 0%, #e8f4f8 100%);
    padding: 20px;
}

.my-shop-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0;
}

/* 面包屑 */
.breadcrumb {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 20px;
    padding: 12px 16px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 8px;
    font-size: 14px;
    backdrop-filter: blur(10px);
}

.breadcrumb-item {
    color: #6b7280;
    transition: color 0.3s ease;
}

.breadcrumb-item:hover {
    color: #3b82f6;
    cursor: pointer;
}

.breadcrumb-separator {
    color: #d1d5db;
}

.breadcrumb-current {
    color: #3b82f6;
    font-weight: 500;
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
    font-size: 36px;
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

/* 统计概览 */
.stats-overview {
    display: flex;
    justify-content: center;
    gap: 40px;
    margin-bottom: 40px;
}

.stat-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px 24px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 16px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
}

.stat-item:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 20px;
}

.stat-icon.pending {
    background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
}

.stat-icon.approved {
    background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
}

.stat-icon.total {
    background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
}

.stat-info {
    text-align: left;
}

.stat-number {
    font-size: 28px;
    font-weight: 700;
    color: #2c3e50;
    line-height: 1;
}

.stat-label {
    font-size: 14px;
    color: #6b7280;
    font-weight: 500;
    margin-top: 4px;
}

/* 店铺信息卡片 */
.shop-info-section {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    padding: 32px;
    margin-bottom: 30px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
}

.shop-info-section:hover {
    transform: translateY(-5px);
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
}

.card-header {
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 2px solid rgba(78, 205, 196, 0.1);
}

.section-title {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 0;
}

.title-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
}

.title-icon.shop {
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
}

.title-icon.pending {
    background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
}

.title-icon.approved {
    background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
}

.section-title h3 {
    font-size: 20px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
}

.shop-header {
    display: flex;
    gap: 32px;
    align-items: flex-start;
}

.shop-avatar-container {
    position: relative;
    flex-shrink: 0;
}

.shop-avatar {
    width: 150px;
    height: 150px;
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;
}

.shop-avatar:hover {
    transform: scale(1.05);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
}

.avatar-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
    color: white;
    padding: 12px;
    text-align: center;
    font-size: 12px;
    font-weight: 500;
    border-radius: 0 0 16px 16px;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.shop-avatar-container:hover .avatar-overlay {
    opacity: 1;
}

.shop-details {
    flex: 1;
    min-width: 0;
}

.shop-name {
    font-size: 28px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0 0 16px 0;
    line-height: 1.3;
}

.shop-rating {
    margin-bottom: 16px;
}

.rate-display {
    font-size: 16px;
}

.shop-description {
    color: #6b7280;
    font-size: 16px;
    line-height: 1.7;
    margin: 0 0 20px 0;
    text-align: justify;
}

.shop-meta {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;
    flex-wrap: wrap;
}

.meta-tag {
    transition: all 0.3s ease;
}

.meta-tag:hover {
    transform: scale(1.05);
}

.apply-section {
    padding: 20px;
    background: linear-gradient(135deg, rgba(78, 205, 196, 0.1) 0%, rgba(255, 255, 255, 0.8) 100%);
    border-radius: 12px;
    border: 1px solid rgba(78, 205, 196, 0.2);
}

.manage-btn {
    padding: 12px 24px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 25px;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    color: white;
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.3);
    transition: all 0.3s ease;
}

.manage-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(78, 205, 196, 0.4);
}

/* 员工管理 */
.staff-management {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    padding: 32px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
}

.staff-section {
    margin-bottom: 40px;
}

.staff-section:last-child {
    margin-bottom: 0;
}

.section-header {
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid rgba(229, 231, 235, 0.8);
}

.count-badge {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    color: white;
    margin-left: 8px;
}

.count-badge.pending {
    background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
}

.count-badge.approved {
    background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
}

/* 表格样式 */
.table-container {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.enhanced-table {
    border-radius: 12px;
    overflow: hidden;
}

.enhanced-table :deep(.el-table__header) {
    background: linear-gradient(135deg, #f8fafc 0%, #e8f4f8 100%);
}

.enhanced-table :deep(.el-table__header th) {
    background: transparent;
    font-weight: 600;
    color: #2c3e50;
    border-bottom: 2px solid rgba(78, 205, 196, 0.2);
}

.pending-table :deep(.el-table__row:hover) {
    background-color: rgba(243, 156, 18, 0.05);
}

.approved-table :deep(.el-table__row:hover) {
    background-color: rgba(39, 174, 96, 0.05);
}

.enhanced-table :deep(.el-table__row) {
    transition: all 0.3s ease;
}

/* 操作按钮 */
.action-buttons {
    display: flex;
    gap: 8px;
    justify-content: center;
}

.approve-btn,
.reject-btn,
.fire-btn {
    padding: 6px 12px;
    border-radius: 20px;
    font-weight: 500;
    transition: all 0.3s ease;
    border: none;
}

.approve-btn {
    background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
    color: white;
    box-shadow: 0 2px 8px rgba(39, 174, 96, 0.3);
}

.approve-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(39, 174, 96, 0.4);
}

.reject-btn,
.fire-btn {
    background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
    color: white;
    box-shadow: 0 2px 8px rgba(231, 76, 60, 0.3);
}

.reject-btn:hover,
.fire-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(231, 76, 60, 0.4);
}

/* 图片错误状态 */
.image-error {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    color: #6c757d;
    border-radius: 16px;
}

.image-error .el-icon {
    font-size: 40px;
    margin-bottom: 8px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
    .stats-overview {
        flex-direction: column;
        align-items: center;
        gap: 20px;
    }

    .stat-item {
        width: 100%;
        max-width: 300px;
    }

    .shop-header {
        flex-direction: column;
        gap: 24px;
    }

    .shop-avatar {
        width: 120px;
        height: 120px;
        margin: 0 auto;
    }
}

@media (max-width: 768px) {
    .page-container {
        padding: 16px;
    }

    .my-shop-container {
        padding: 0;
    }

    .shop-info-section,
    .staff-management {
        padding: 24px 20px;
    }

    .page-title {
        font-size: 28px;
    }

    .shop-name {
        font-size: 24px;
    }

    .shop-meta {
        flex-direction: column;
        gap: 8px;
    }

    .action-buttons {
        flex-direction: column;
        gap: 4px;
    }

    .enhanced-table :deep(.el-table__cell) {
        padding: 8px 4px;
    }
}

@media (max-width: 480px) {
    .breadcrumb {
        font-size: 12px;
        padding: 8px 12px;
    }

    .page-title {
        font-size: 24px;
    }

    .shop-name {
        font-size: 20px;
    }

    .section-title h3 {
        font-size: 18px;
    }

    .enhanced-table {
        font-size: 12px;
    }
}
</style>