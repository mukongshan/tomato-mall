<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Shop, getShopList } from '@/api/shop';
import router from '@/router';
import { Picture, Star, StarFilled } from '@element-plus/icons-vue';

// 数据状态
const approvedShops = ref<Shop[]>([]); // 已通过店铺列表
const loading = ref(false);            // 加载状态

/**
 * 获取已通过店铺列表
 */
const fetchApprovedShops = async () => {

    loading.value = true;
    const response = await getShopList();
    const shops = (response.data.data || []) as Shop[];

    // 筛选已通过审核的店铺 (isValid === 1)
    approvedShops.value = shops.filter(shop => shop.isValid === 1);
    loading.value = false;

};

/**
 * 跳转到店铺详情页
 * @param shopId 店铺ID
 */
const gotoDetails = (shopId: number) => {
    router.push(`/shop/detail/${shopId}`);
};

// 组件挂载时获取数据
onMounted(() => {
    fetchApprovedShops();
});
</script>

<template>
    <div class="shops-container">
        <!-- 页面头部 -->
        <div class="page-header animate-fade-in">
            <h2 class="page-title">
                <span class="title-char" v-for="(char, index) in '所有店铺'" :key="index"
                    :style="{ animationDelay: `${index * 0.1}s` }">
                    {{ char }}
                </span>
            </h2>
            <p class="page-subtitle animate-slide-up">发现更多精彩店铺</p>
            <div class="title-decoration">
                <div class="decoration-line"></div>
                <div class="decoration-dot"></div>
                <div class="decoration-line"></div>
            </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container animate-pulse">
            <el-skeleton :rows="6" animated class="skeleton-item" />
            <div class="loading-text">
                <div class="loading-dots">
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
                </div>
                <p>正在为您寻找店铺...</p>
            </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="approvedShops.length === 0" class="empty-container animate-scale-in">
            <el-empty description="暂无店铺" class="custom-empty">
                <template #image>
                    <div class="empty-image">
                        <el-icon size="80" class="pulse-icon">
                            <Picture />
                        </el-icon>
                    </div>
                </template>
                <template #description>
                    <p class="empty-text">暂时没有找到店铺</p>
                    <p class="empty-subtext">请稍后再来看看</p>
                </template>
            </el-empty>
        </div>

        <!-- 店铺列表 -->
        <div v-else class="shop-list animate-fade-in-up">
            <div v-for="(shop, index) in approvedShops" :key="shop.shopId" class="shop-card animate-card-in"
                :style="{ animationDelay: `${index * 0.1}s` }" @click="gotoDetails(shop.shopId)">

                <!-- 店铺图片容器 -->
                <div class="shop-image-container">
                    <el-image :src="shop.iconUrl" :alt="shop.name" fit="cover" class="shop-image">
                        <template #error>
                            <div class="image-error">
                                <el-icon class="pulse-icon">
                                    <Picture />
                                </el-icon>
                                <span>图片加载失败</span>
                            </div>
                        </template>
                    </el-image>
                    <div v-if="!shop.iconUrl" class="image-placeholder">
                        <el-icon class="bounce-icon">
                            <Picture />
                        </el-icon>
                        <span>暂无图片</span>
                    </div>

                    <!-- 悬停遮罩 -->
                    <div class="shop-overlay">
                        <div class="overlay-content">
                            <span>点击进入店铺</span>
                            <div class="overlay-arrow">→</div>
                        </div>
                    </div>

                    <!-- 光泽效果 -->
                    <div class="shop-shine"></div>
                </div>

                <!-- 店铺信息 -->
                <div class="shop-info">
                    <h3 class="shop-title">{{ shop.name }}</h3>
                    <p class="shop-description">{{ shop.description }}</p>
                    <div class="shop-meta">
                        <div class="rate-container">
                            <el-rate v-model="shop.rate" disabled text-color="#ff9900" class="shop-rate"
                                :icon-classes="['custom-star', 'custom-star-filled', 'custom-star-half']" />
                            <span class="rate-text">{{ shop.rate || 0 }}</span>
                        </div>
                        <div class="shop-badge">
                            <span class="verified-badge">
                                <el-icon>
                                    <StarFilled />
                                </el-icon>
                                已认证
                            </span>
                        </div>
                    </div>
                </div>

                <!-- 卡片边框光效 -->
                <div class="card-glow"></div>
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
        transform: scale(0.8);
    }

    to {
        opacity: 1;
        transform: scale(1);
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(50px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes cardIn {
    from {
        opacity: 0;
        transform: translateY(30px) scale(0.95);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
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

@keyframes shimmer {
    0% {
        transform: translateX(-100%);
    }

    100% {
        transform: translateX(100%);
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

@keyframes loadingDots {

    0%,
    80%,
    100% {
        transform: scale(0);
    }

    40% {
        transform: scale(1);
    }
}

@keyframes glow {

    0%,
    100% {
        opacity: 0;
    }

    50% {
        opacity: 1;
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

/* 动画类 */
.animate-fade-in {
    animation: fadeIn 0.8s ease-out;
}

.animate-slide-up {
    animation: slideUp 0.8s ease-out 0.2s both;
}

.animate-scale-in {
    animation: scaleIn 0.8s ease-out 0.4s both;
}

.animate-fade-in-up {
    animation: fadeInUp 0.8s ease-out 0.6s both;
}

.animate-card-in {
    animation: cardIn 0.6s ease-out both;
}

.animate-pulse {
    animation: pulse 1.5s infinite;
}

.title-char {
    display: inline-block;
    animation: bounce 0.6s ease-out both;
}

.bounce-icon {
    animation: bounce 2s infinite;
}

.pulse-icon {
    animation: pulse 2s infinite;
}

.shops-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    background: #f8f9fa;
    min-height: 100vh;
}

/* 页面头部 */
.page-header {
    text-align: center;
    margin-bottom: 40px;
    padding: 40px 0;
}

.page-title {
    font-size: 42px;
    font-weight: bold;
    color: #2c3e50;
    margin-bottom: 12px;
    font-family: "华文中宋", serif;
    letter-spacing: 2px;
}

.page-subtitle {
    font-size: 16px;
    color: #6c757d;
    margin: 0 0 20px 0;
    font-weight: 300;
}

.title-decoration {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    margin-top: 20px;
}

.decoration-line {
    width: 50px;
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

/* 加载状态 */
.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 30px;
    padding: 60px 20px;
}

.skeleton-item {
    width: 100%;
    max-width: 600px;
}

.loading-text {
    text-align: center;
}

.loading-dots {
    display: flex;
    gap: 8px;
    justify-content: center;
    margin-bottom: 16px;
}

.dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #4ecdc4;
    animation: loadingDots 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
    animation-delay: -0.32s;
}

.dot:nth-child(2) {
    animation-delay: -0.16s;
}

.loading-text p {
    color: #6c757d;
    font-size: 16px;
    margin: 0;
}

/* 空状态 */
.empty-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
}

.custom-empty {
    background: white;
    border-radius: 20px;
    padding: 60px 40px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.empty-image {
    margin-bottom: 20px;
}

.empty-text {
    font-size: 18px;
    color: #2c3e50;
    margin: 0 0 8px 0;
    font-weight: 600;
}

.empty-subtext {
    font-size: 14px;
    color: #6c757d;
    margin: 0;
}

/* 店铺列表 */
.shop-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 30px;
    padding: 20px 0;
}

.shop-card {
    cursor: pointer;
    background: white;
    border-radius: 20px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    transition: all 0.4s ease;
    overflow: hidden;
    position: relative;
    border: 2px solid transparent;
}

.shop-card:hover {
    transform: translateY(-12px) scale(1.02);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    border-color: #4ecdc4;
}

.shop-image-container {
    position: relative;
    width: 100%;
    height: 220px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    overflow: hidden;
}

.shop-image {
    width: 100%;
    height: 100%;
    transition: transform 0.4s ease;
}

.shop-card:hover .shop-image {
    transform: scale(1.1);
}

.image-error,
.image-placeholder {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    color: #6c757d;
    font-size: 14px;
}

.image-error .el-icon,
.image-placeholder .el-icon {
    font-size: 48px;
    margin-bottom: 12px;
}

/* 悬停遮罩 */
.shop-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(78, 205, 196, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: all 0.3s ease;
}

.shop-card:hover .shop-overlay {
    opacity: 1;
}

.overlay-content {
    text-align: center;
    color: white;
    font-weight: 600;
    font-size: 16px;
}

.overlay-arrow {
    font-size: 24px;
    margin-top: 8px;
    animation: float 2s ease-in-out infinite;
}

/* 光泽效果 */
.shop-shine {
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transform: rotate(45deg);
    opacity: 0;
    transition: opacity 0.3s ease;
}

.shop-card:hover .shop-shine {
    opacity: 1;
    animation: shimmer 1.5s ease-out;
}

/* 卡片边框光效 */
.card-glow {
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    background: linear-gradient(45deg, #4ecdc4, #44a08d, #4ecdc4);
    border-radius: 22px;
    opacity: 0;
    z-index: -1;
    animation: rotate 3s linear infinite;
}

.shop-card:hover .card-glow {
    opacity: 0.6;
    animation: glow 2s ease-in-out infinite;
}

/* 店铺信息 */
.shop-info {
    padding: 24px;
    background: white;
}

.shop-title {
    margin: 0 0 12px 0;
    font-size: 20px;
    color: #2c3e50;
    font-weight: 600;
    line-height: 1.3;
}

.shop-description {
    font-size: 14px;
    color: #6c757d;
    margin: 0 0 20px 0;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    min-height: 40px;
}

.shop-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
}

.rate-container {
    display: flex;
    align-items: center;
    gap: 8px;
}

.shop-rate {
    flex-shrink: 0;
}

.rate-text {
    font-size: 14px;
    color: #f39c12;
    font-weight: 600;
}

.shop-badge {
    flex-shrink: 0;
}

.verified-badge {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
}

.verified-badge .el-icon {
    font-size: 14px;
}

/* 自定义星星图标 */
.custom-star {
    color: #dcdfe6;
}

.custom-star-filled {
    color: #f39c12;
}

.custom-star-half {
    color: #f39c12;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .shops-container {
        padding: 16px;
    }

    .page-title {
        font-size: 32px;
    }

    .shop-list {
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 20px;
    }

    .shop-image-container {
        height: 180px;
    }

    .shop-info {
        padding: 20px;
    }

    .shop-meta {
        flex-direction: column;
        align-items: flex-start;
    }
}

@media (max-width: 480px) {
    .shop-list {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    .shop-card {
        margin: 0 auto;
        max-width: 400px;
    }

    .shop-image-container {
        height: 160px;
    }

    .overlay-content {
        font-size: 14px;
    }
}

/* 全局优化 */
* {
    box-sizing: border-box;
}

/* 滚动条美化 */
::-webkit-scrollbar {
    width: 8px;
}

::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
}

::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}
</style>