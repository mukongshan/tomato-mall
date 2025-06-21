<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Shop, getShopList, updateShop, deleteShop } from '@/api/shop';
import router from '@/router';
import { Picture, Shop as ShopIcon, Check, Close, Star, User, Calendar, DataAnalysis } from '@element-plus/icons-vue';
import { getUserRoleById, updateUserRole } from '@/api/account';
import { Message, sendMessage } from '@/api/message';

// 数据状态
const pendingShops = ref<Shop[]>([]);  // 待审核店铺列表
const approvedShops = ref<Shop[]>([]); // 已通过店铺列表
const loading = ref(false);            // 加载状态
const isPageLoaded = ref(false);       // 页面加载状态

/**
 * 获取店铺列表数据
 */
const fetchShops = async () => {
    loading.value = true;
    try {
        const response = await getShopList();
        const shops = (response.data.data || []) as Shop[];

        // 根据isValid状态筛选店铺
        pendingShops.value = shops.filter(shop => shop.isValid === 0);
        approvedShops.value = shops.filter(shop => shop.isValid === 1);
    } catch (error) {
        ElMessage.error('获取店铺列表失败');
    } finally {
        loading.value = false;

        // 延迟设置页面加载完成，让动画更自然
        await nextTick();
        setTimeout(() => {
            isPageLoaded.value = true;
        }, 300);
    }
};

/**
 * 处理店铺通过审核
 * @param shop 店铺数据对象
 */
const handleApprove = async (shop: Shop) => {
    try {
        // 确认弹窗
        await ElMessageBox.confirm('确定要通过该店铺的申请吗？', '审核确认', {
            type: 'warning',
            confirmButtonText: '通过申请',
            cancelButtonText: '取消'
        });

        // 更新店铺状态为已通过(1)
        const updatedShop = { ...shop, isValid: 1 };
        // 把用户身份更新为店主
        const userRole = await getUserRoleById(shop.ownerId);
        console.log('User Role:', userRole.data.data);
        if (userRole.data.data == "CUSTOMER") {
            await updateUserRole(shop.ownerId, 'SHOPKEEPER');
        }
        const message: Message = {
            id: 0,
            content: "APPLICATION_APPROVED",
            isRead: false,
            fromUser: Number(sessionStorage.getItem('id')),
            toUser: shop.ownerId,
            createdTime: new Date().toISOString()
        };
        await sendMessage(message);
        // 更新店铺信息
        await updateShop(updatedShop);
        ElMessage.success('店铺已成功通过审核');
        fetchShops(); // 刷新列表
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('店铺审核操作失败');
        }
    }
};

/**
 * 处理店铺拒绝申请
 * @param shopId 店铺ID
 */
const handleReject = async (shop: Shop) => {
    try {
        // 确认弹窗
        await ElMessageBox.confirm('确定要拒绝该店铺的申请吗？', '拒绝确认', {
            type: 'warning',
            confirmButtonText: '拒绝申请',
            cancelButtonText: '取消'
        });
        const message: Message = {
            id: 0,
            content: "APPLICATION_REJECTED",
            isRead: false,
            fromUser: Number(sessionStorage.getItem('id')),
            toUser: shop.ownerId,
            createdTime: new Date().toISOString()
        };
        await sendMessage(message);
        // 删除店铺记录
        await deleteShop(shop.shopId);
        ElMessage.success('店铺申请已拒绝');
        fetchShops(); // 刷新列表
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('店铺拒绝操作失败');
        }
    }
};

/**
 * 跳转到店铺详情页
 * @param shopId 店铺ID
 */
const navigateToDetail = (shopId: number) => {
    router.push(`/shop/detail/${shopId}`);
};

// 组件挂载时获取数据
onMounted(() => {
    fetchShops();
});
</script>

<template>
    <div class="page-container">
        <!-- 页面加载动画 -->
        <div v-if="loading && !isPageLoaded" class="page-loading">
            <div class="loading-content">
                <div class="loading-icon">
                    <el-icon size="60" class="rotate-icon">
                        <ShopIcon />
                    </el-icon>
                </div>
                <div class="loading-text">
                    <span class="loading-char" v-for="(char, index) in '加载店铺管理'" :key="index"
                        :style="{ animationDelay: `${index * 0.1}s` }">
                        {{ char }}
                    </span>
                </div>
            </div>
        </div>

        <!-- 主要内容 -->
        <div v-else class="shop-management-container animate-page-enter">
            <!-- 页面头部 -->
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
                    <span v-for="(char, index) in '店铺管理中心'" :key="index" class="title-char"
                        :style="{ animationDelay: `${index * 0.05}s` }">
                        {{ char }}
                    </span>
                </h1>
                <p class="page-subtitle animate-fade-in-up">管理和审核商家店铺申请</p>

                <!-- 统计信息 -->
                <div class="stats-overview animate-slide-up-stagger">
                    <div class="stat-item animate-stat-appear" style="--delay: 0.1s">
                        <div class="stat-icon pending">
                            <el-icon>
                                <Calendar />
                            </el-icon>
                        </div>
                        <div class="stat-info">
                            <div class="stat-number">{{ pendingShops.length }}</div>
                            <div class="stat-label">待审核</div>
                        </div>
                    </div>
                    <div class="stat-item animate-stat-appear" style="--delay: 0.2s">
                        <div class="stat-icon approved">
                            <el-icon>
                                <Check />
                            </el-icon>
                        </div>
                        <div class="stat-info">
                            <div class="stat-number">{{ approvedShops.length }}</div>
                            <div class="stat-label">已通过</div>
                        </div>
                    </div>
                    <div class="stat-item animate-stat-appear" style="--delay: 0.3s">
                        <div class="stat-icon total">
                            <el-icon>
                                <DataAnalysis />
                            </el-icon>
                        </div>
                        <div class="stat-info">
                            <div class="stat-number">{{ pendingShops.length + approvedShops.length }}</div>
                            <div class="stat-label">总计</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 待审核店铺区域 -->
            <div class="section animate-section-appear" style="--delay: 0.5s">
                <div class="section-header">
                    <div class="section-title">
                        <div class="title-icon pending">
                            <el-icon>
                                <Calendar />
                            </el-icon>
                        </div>
                        <h3>待审核店铺</h3>
                        <div class="count-badge pending">{{ pendingShops.length }}</div>
                    </div>
                </div>

                <div v-if="pendingShops.length === 0" class="empty-state animate-empty-appear">
                    <div class="empty-content">
                        <div class="empty-icon">📋</div>
                        <h4>暂无待审核店铺</h4>
                        <p>所有店铺申请都已处理完毕</p>
                    </div>
                </div>

                <div v-else class="shop-list">
                    <div v-for="(shop, index) in pendingShops" :key="shop.shopId"
                        class="shop-card pending animate-card-appear" :style="{ animationDelay: `${index * 0.1}s` }">
                        <div class="card-header">
                            <div class="status-badge pending">待审核</div>
                            <div class="shop-id">ID: {{ shop.shopId }}</div>
                        </div>

                        <div class="shop-content" @click="navigateToDetail(shop.shopId)">
                            <!-- 店铺图片 -->
                            <div class="image-container">
                                <el-image :src="shop.iconUrl" v-if="shop.iconUrl" fit="cover" class="shop-image">
                                    <template #error>
                                        <div class="image-error animate-pulse">
                                            <el-icon size="30">
                                                <Picture />
                                            </el-icon>
                                            <span>图片加载失败</span>
                                        </div>
                                    </template>
                                </el-image>
                                <div v-else class="image-placeholder">
                                    <el-icon size="40">
                                        <ShopIcon />
                                    </el-icon>
                                </div>
                                <div class="image-overlay">
                                    <span>点击查看详情</span>
                                </div>
                            </div>

                            <div class="shop-info">
                                <h4 class="shop-name">{{ shop.name }}</h4>
                                <p class="description">{{ shop.description }}</p>
                                <div class="owner-info">
                                    <el-icon>
                                        <User />
                                    </el-icon>
                                    <span>店主ID: {{ shop.ownerId }}</span>
                                </div>
                            </div>
                        </div>

                        <!-- 操作按钮 -->
                        <div class="action-buttons">
                            <el-button type="success" @click.stop="handleApprove(shop)" class="approve-btn"
                                :icon="Check">
                                通过申请
                            </el-button>
                            <el-button type="danger" @click.stop="handleReject(shop)" class="reject-btn" :icon="Close">
                                拒绝申请
                            </el-button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 已通过店铺区域 -->
            <div class="section animate-section-appear" style="--delay: 0.7s">
                <div class="section-header">
                    <div class="section-title">
                        <div class="title-icon approved">
                            <el-icon>
                                <Check />
                            </el-icon>
                        </div>
                        <h3>已通过店铺</h3>
                        <div class="count-badge approved">{{ approvedShops.length }}</div>
                    </div>
                </div>

                <div v-if="approvedShops.length === 0" class="empty-state animate-empty-appear">
                    <div class="empty-content">
                        <div class="empty-icon">🏪</div>
                        <h4>暂无已通过店铺</h4>
                        <p>还没有审核通过的店铺</p>
                    </div>
                </div>

                <div v-else class="shop-list">
                    <div v-for="(shop, index) in approvedShops" :key="shop.shopId"
                        class="shop-card approved animate-card-appear" :style="{ animationDelay: `${index * 0.1}s` }"
                        @click="navigateToDetail(shop.shopId)">
                        <div class="card-header">
                            <div class="status-badge approved">已通过</div>
                            <div class="shop-rating">
                                <el-icon>
                                    <Star />
                                </el-icon>
                                <span>{{ shop.rate || '暂无评分' }}</span>
                            </div>
                        </div>

                        <div class="shop-content">
                            <!-- 店铺图片 -->
                            <div class="image-container">
                                <el-image :src="shop.iconUrl" v-if="shop.iconUrl" fit="cover" class="shop-image">
                                    <template #error>
                                        <div class="image-error animate-pulse">
                                            <el-icon size="30">
                                                <Picture />
                                            </el-icon>
                                            <span>图片加载失败</span>
                                        </div>
                                    </template>
                                </el-image>
                                <div v-else class="image-placeholder">
                                    <el-icon size="40">
                                        <ShopIcon />
                                    </el-icon>
                                </div>
                                <div class="image-overlay">
                                    <span>点击查看详情</span>
                                </div>
                            </div>

                            <div class="shop-info">
                                <h4 class="shop-name">{{ shop.name }}</h4>
                                <p class="description">{{ shop.description }}</p>
                                <div class="meta-info">
                                    <div class="owner-info">
                                        <el-icon>
                                            <User />
                                        </el-icon>
                                        <span>店主ID: {{ shop.ownerId }}</span>
                                    </div>
                                </div>
                            </div>
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

@keyframes cardAppear {
    from {
        opacity: 0;
        transform: translateY(40px) scale(0.95);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
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

@keyframes emptyAppear {
    from {
        opacity: 0;
        transform: scale(0.9);
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

@keyframes numberCount {
    from {
        transform: scale(0.5);
        opacity: 0;
    }

    to {
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

.animate-slide-up-stagger {
    animation: fadeInUp 0.8s ease-out 1s both;
}

.animate-stat-appear {
    animation: statAppear 0.6s ease-out calc(1.2s + var(--delay)) both;
}

.animate-section-appear {
    animation: sectionAppear 0.6s ease-out calc(1.4s + var(--delay)) both;
}

.animate-card-appear {
    animation: cardAppear 0.6s ease-out calc(1.6s + var(--delay)) both;
}

.animate-empty-appear {
    animation: emptyAppear 0.8s ease-out 1.8s both;
}

.animate-pulse {
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

.shop-management-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 40px 20px;
}

/* 页面头部 */
.page-header {
    text-align: center;
    margin-bottom: 50px;
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
    margin: 0 0 40px 0;
    font-weight: 400;
}

/* 统计概览 */
.stats-overview {
    display: flex;
    justify-content: center;
    gap: 40px;
    margin-bottom: 20px;
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
    animation: numberCount 0.8s ease-out;
}

.stat-label {
    font-size: 14px;
    color: #6b7280;
    font-weight: 500;
    margin-top: 4px;
}

/* 分区样式 */
.section {
    margin-bottom: 50px;
}

.section-header {
    margin-bottom: 30px;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
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

.title-icon.pending {
    background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
}

.title-icon.approved {
    background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
}

.section-title h3 {
    font-size: 24px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
}

.count-badge {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    color: white;
    margin-left: auto;
}

.count-badge.pending {
    background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
}

.count-badge.approved {
    background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
}

/* 店铺列表网格 */
.shop-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 24px;
}

/* 店铺卡片 */
.shop-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    padding: 24px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
}

.shop-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    transition: all 0.3s ease;
}

.shop-card.pending::before {
    background: linear-gradient(90deg, #f39c12, #e67e22);
}

.shop-card.approved::before {
    background: linear-gradient(90deg, #27ae60, #2ecc71);
}

.shop-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.15);
}

.shop-card:hover::before {
    height: 6px;
}

/* 卡片头部 */
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.status-badge {
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    color: white;
}

.status-badge.pending {
    background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
}

.status-badge.approved {
    background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
}

.shop-id,
.shop-rating {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #6b7280;
    font-weight: 500;
}

.shop-rating .el-icon {
    color: #f1c40f;
}

/* 店铺内容 */
.shop-content {
    display: flex;
    gap: 16px;
    margin-bottom: 20px;
}

/* 图片容器 */
.image-container {
    position: relative;
    width: 100px;
    height: 100px;
    flex-shrink: 0;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.shop-image {
    width: 100%;
    height: 100%;
    transition: transform 0.3s ease;
}

.shop-card:hover .shop-image {
    transform: scale(1.1);
}

.image-placeholder {
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #6c757d;
}

.image-error {
    width: 100%;
    height: 100%;
    background: #f8f9fa;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #9ca3af;
    font-size: 12px;
    gap: 4px;
}

.image-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 500;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.shop-card:hover .image-overlay {
    opacity: 1;
}

/* 店铺信息 */
.shop-info {
    flex: 1;
    min-width: 0;
}

.shop-name {
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 8px 0;
    line-height: 1.3;
}

.description {
    color: #6b7280;
    font-size: 14px;
    line-height: 1.5;
    margin: 0 0 12px 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.owner-info,
.meta-info {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #9ca3af;
}

.owner-info .el-icon {
    color: #4ecdc4;
}

/* 操作按钮 */
.action-buttons {
    display: flex;
    gap: 12px;
}

.approve-btn,
.reject-btn {
    flex: 1;
    border-radius: 25px;
    font-weight: 500;
    transition: all 0.3s ease;
    border: none;
}

.approve-btn {
    background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
    color: white;
    box-shadow: 0 4px 12px rgba(39, 174, 96, 0.3);
}

.approve-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(39, 174, 96, 0.4);
}

.reject-btn {
    background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
    color: white;
    box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
}

.reject-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(231, 76, 60, 0.4);
}

/* 空状态 */
.empty-state {
    text-align: center;
    padding: 60px 20px;
}

.empty-content {
    max-width: 300px;
    margin: 0 auto;
}

.empty-icon {
    font-size: 80px;
    margin-bottom: 20px;
}

.empty-content h4 {
    font-size: 18px;
    color: #2c3e50;
    margin: 0 0 8px 0;
    font-weight: 600;
}

.empty-content p {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
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

    .shop-list {
        grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
        gap: 20px;
    }
}

@media (max-width: 768px) {
    .page-container {
        padding: 16px;
    }

    .shop-management-container {
        padding: 20px 16px;
    }

    .page-title {
        font-size: 28px;
    }

    .shop-list {
        grid-template-columns: 1fr;
    }

    .shop-content {
        flex-direction: column;
        align-items: center;
        text-align: center;
    }

    .image-container {
        width: 80px;
        height: 80px;
    }

    .action-buttons {
        flex-direction: column;
    }

    .section-title {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }

    .count-badge {
        margin-left: 0;
        align-self: flex-start;
    }
}
</style>