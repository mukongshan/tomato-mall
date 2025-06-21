<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElSkeleton, ElDialog, ElForm, ElFormItem, ElInput, ElRate, ElButton } from 'element-plus';
import { Picture, Star, ShoppingCart, User, Shop as ShopIcon, Edit, Clock } from '@element-plus/icons-vue';
import { Shop, getShopDetail } from '@/api/shop';
import { Product, getProductsByShopId } from '@/api/product';
import { Review, getShopReviews, addShopReview } from '@/api/review';
import router from '@/router';
import { isCustomer } from '@/components/LoginEvent';
import { getUserDetails, updateUserInfo, UserDetail } from '@/api/account';
import { getMessageByFromUserAndContent, Message, sendMessage } from '@/api/message';

const route = useRoute();
const shopId = ref<number>(Number(route.params.shopId));
const shopInfo = ref<Shop>();
const products = ref<Product[]>([]);
const reviews = ref<Review[]>([]);
const loading = ref(true);
const reviewsLoading = ref(true);
const applied = ref(false);
const isPageLoaded = ref(false);

// 添加评论相关的响应式数据
const reviewDialogVisible = ref(false);
const reviewForm = ref({
    content: '',
    rate: 5
});
const submitLoading = ref(false);

const checkEmployeeStatus = async () => {
    const response = await getMessageByFromUserAndContent(Number(sessionStorage.getItem('id')), "NEW_EMPLOYEE_APPLICATION");
    return response.data.data;
};

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

// 获取店铺商品
const fetchShopProducts = async () => {
    try {
        const response = await getProductsByShopId(shopId.value);
        products.value = response.data.data || [];
    } catch (error) {
        ElMessage.error('获取商品列表失败');
        console.error(error);
    } finally {
        loading.value = false;
    }
};

// 获取店铺评论
const fetchShopReviews = async () => {
    try {
        reviewsLoading.value = true;
        const response = await getShopReviews(shopId.value);
        reviews.value = response.data.data || [];
    } catch (error) {
        ElMessage.error('获取评论失败');
        console.error(error);
    } finally {
        reviewsLoading.value = false;
    }
};

// 提交评论
const submitReview = async () => {
    try {
        submitLoading.value = true;
        const reviewData: Review = {
            id: 0,
            accountId: Number(sessionStorage.getItem('id')),
            content: reviewForm.value.content,
            rate: reviewForm.value.rate,
            type: 'SHOP',
            productId: 0, // 店铺评论不需要商品ID
            shopId: shopId.value,
            createdAt: new Date().toISOString()
        };

        await addShopReview(reviewData);
        ElMessage.success('评论添加成功');

        // 重置表单并关闭对话框
        reviewForm.value = {
            content: '',
            rate: 5
        };
        reviewDialogVisible.value = false;

        // 重新获取评论列表
        await fetchShopReviews();
    } catch (error) {
        ElMessage.error('评论添加失败');
        console.error(error);
    } finally {
        submitLoading.value = false;
    }
};

// 打开添加评论对话框
const openReviewDialog = () => {
    if (!isCustomer) {
        ElMessage.warning('请先登录');
        return;
    }
    reviewDialogVisible.value = true;
};

// 跳转到商品详情
const gotoProductDetail = (productId: number) => {
    router.push(`/product/${productId}`);
};

// 格式化时间
const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
};

onMounted(async () => {
    await Promise.all([fetchShopDetail(), fetchShopProducts(), fetchShopReviews()]);
    applied.value = await checkEmployeeStatus();

    // 延迟设置页面加载完成
    await nextTick();
    setTimeout(() => {
        isPageLoaded.value = true;
    }, 500);
});

const handleApplyForStaff = async () => {
    const username = sessionStorage.getItem('username') as string;
    const userDetail = await getUserDetails(username);
    const updateUser = {
        ...userDetail.data.data,
        isValidStaff: 0, // 设置为待审核状态
        shopId: (shopInfo.value as Shop).shopId
    } as unknown as UserDetail;
    // 发送申请成为店员的消息
    const message: Message = {
        id: 0,
        isRead: false,
        fromUser: userDetail.data.data.id,
        toUser: (shopInfo.value as Shop).ownerId,
        content: "NEW_EMPLOYEE_APPLICATION",
        createdTime: new Date().toISOString()
    };
    await sendMessage(message);

    console.log('申请成为店员的用户信息:', updateUser);
    try {
        await updateUserInfo(updateUser);
        ElMessage.success('申请已提交，请等待审核');
    } catch (error) {
        ElMessage.error('申请提交失败，请稍后重试');
        console.error(error);
    }
};
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
                    <span class="loading-char" v-for="(char, index) in '加载店铺详情'" :key="index"
                        :style="{ animationDelay: `${index * 0.1}s` }">
                        {{ char }}
                    </span>
                </div>
            </div>
        </div>

        <!-- 主要内容 -->
        <div v-else class="shop-detail-container animate-page-enter">
            <!-- 面包屑导航 -->
            <div class="breadcrumb animate-slide-down">
                <span class="breadcrumb-item">首页</span>
                <span class="breadcrumb-separator">/</span>
                <span class="breadcrumb-item">店铺</span>
                <span class="breadcrumb-separator">/</span>
                <span class="breadcrumb-current">{{ shopInfo?.name }}</span>
            </div>

            <!-- 店铺信息 -->
            <div v-if="shopInfo" class="shop-info-section animate-card-enter">
                <div class="shop-header">
                    <div class="shop-avatar animate-avatar-appear">
                        <el-image :src="shopInfo.iconUrl" fit="cover" class="avatar-image">
                            <template #error>
                                <div class="image-error animate-pulse">
                                    <el-icon size="40">
                                        <Picture />
                                    </el-icon>
                                    <span>店铺图片加载失败</span>
                                </div>
                            </template>
                            <div v-if="!shopInfo.iconUrl" class="image-placeholder">
                                <el-icon size="40">
                                    <ShopIcon />
                                </el-icon>
                                <span>暂无店铺图片</span>
                            </div>
                        </el-image>
                        <div class="avatar-overlay">
                            <span>店铺头像</span>
                        </div>
                    </div>

                    <div class="shop-meta animate-slide-in-right">
                        <h1 class="shop-name animate-title-reveal">
                            <span v-for="(char, index) in shopInfo.name" :key="index" class="title-char"
                                :style="{ animationDelay: `${index * 0.05}s` }">
                                {{ char }}
                            </span>
                        </h1>

                        <div class="shop-rating animate-fade-in-up" style="--delay: 0.2s">
                            <el-rate v-model="shopInfo.rate" disabled show-score text-color="#ff9900"
                                score-template="{value} 分" class="shop-rate" />
                        </div>

                        <p class="shop-description animate-fade-in-up" style="--delay: 0.4s">
                            {{ shopInfo.description }}
                        </p>

                        <!-- 申请成为店员按钮 -->
                        <div class="apply-section animate-fade-in-up" style="--delay: 0.6s" v-if="isCustomer">
                            <el-button v-if="applied" disabled type="info" class="apply-button applied-button"
                                :icon="Clock">
                                您已有店员申请
                            </el-button>

                            <el-button v-else type="primary" @click="handleApplyForStaff"
                                class="apply-button animate-pulse-btn" :icon="User">
                                申请成为店员
                            </el-button>
                            <p class="apply-tip">审核通过后即可参与店铺管理</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 店铺评论区域 -->
            <div class="reviews-section animate-section-appear" style="--delay: 0.8s">
                <div class="section-header">
                    <div class="section-title">
                        <div class="title-icon">
                            <el-icon>
                                <Star />
                            </el-icon>
                        </div>
                        <h2>店铺评价</h2>
                        <div class="count-badge">{{ reviews.length }}</div>
                    </div>
                    <el-button v-if="isCustomer" type="primary" @click="openReviewDialog"
                        class="write-review-btn animate-hover-lift" :icon="Edit">
                        写评价
                    </el-button>
                </div>

                <el-skeleton v-if="reviewsLoading" :rows="3" animated class="animate-skeleton" />

                <div v-else-if="reviews.length === 0" class="empty-reviews animate-empty-appear">
                    <div class="empty-content">
                        <div class="empty-icon">💬</div>
                        <h4>暂无评价</h4>
                        <p>成为第一个评价这家店铺的顾客吧！</p>
                    </div>
                </div>

                <div v-else class="reviews-list">
                    <div v-for="(review, index) in reviews" :key="review.id" class="review-item animate-review-appear"
                        :style="{ animationDelay: `${index * 0.1}s` }">
                        <div class="review-header">
                            <div class="reviewer-info">
                                <div class="reviewer-avatar">
                                    {{ String(review.accountId).slice(-1) }}
                                </div>
                                <div class="reviewer-details">
                                    <span class="reviewer-name">用户{{ review.accountId }}</span>
                                    <el-rate v-model="review.rate" disabled text-color="#ff9900"
                                        class="review-rating" />
                                </div>
                            </div>
                            <span class="review-date">{{ formatDate(review.createdAt) }}</span>
                        </div>
                        <div class="review-content">
                            {{ review.content }}
                        </div>
                    </div>
                </div>
            </div>

            <!-- 商品列表 -->
            <div class="product-list-section animate-section-appear" style="--delay: 1s">
                <div class="section-header">
                    <div class="section-title">
                        <div class="title-icon">
                            <el-icon>
                                <ShoppingCart />
                            </el-icon>
                        </div>
                        <h2>店铺商品</h2>
                        <div class="count-badge">{{ products.length }}</div>
                    </div>
                </div>

                <el-skeleton v-if="loading" :rows="6" animated class="animate-skeleton" />

                <div v-else-if="products.length === 0" class="empty-products animate-empty-appear">
                    <div class="empty-content">
                        <div class="empty-icon">📦</div>
                        <h4>该店铺暂无商品</h4>
                        <p>店主正在努力上架商品中...</p>
                    </div>
                </div>

                <div v-else class="product-grid">
                    <div v-for="(product, index) in products" :key="product.id"
                        class="product-card animate-product-appear" :style="{ animationDelay: `${index * 0.1}s` }"
                        @click="gotoProductDetail(product.id)">
                        <div class="product-image-container">
                            <el-image :src="product.cover" fit="cover" class="product-image">
                                <template #error>
                                    <div class="image-error animate-pulse">
                                        <el-icon size="30">
                                            <Picture />
                                        </el-icon>
                                        <span>图片加载失败</span>
                                    </div>
                                </template>
                                <div v-if="!product.cover" class="image-placeholder">
                                    <el-icon size="30">
                                        <Picture />
                                    </el-icon>
                                    <span>暂无商品图片</span>
                                </div>
                            </el-image>
                            <div class="product-overlay">
                                <span>点击查看详情</span>
                            </div>
                        </div>

                        <div class="product-info">
                            <h3 class="product-title">{{ product.title }}</h3>
                            <p class="product-price">¥ {{ product.price.toFixed(2) }}</p>
                            <div class="product-meta">
                                <el-rate v-model="product.rate" disabled text-color="#ff9900" class="product-rate" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 添加评论对话框 -->
        <el-dialog v-model="reviewDialogVisible" title="✨ 添加店铺评价" width="500px"
            class="review-dialog animate-modal-appear" :before-close="() => {
                reviewForm = { content: '', rate: 5 };
                reviewDialogVisible = false;
            }">
            <div class="dialog-content">
                <el-form :model="reviewForm" label-width="80px" class="review-form">
                    <el-form-item label="评分" prop="rate">
                        <div class="rating-section">
                            <el-rate v-model="reviewForm.rate" show-text text-color="#ff9900" class="dialog-rating" />
                        </div>
                    </el-form-item>
                    <el-form-item label="评价内容" prop="content">
                        <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请输入您对这家店铺的评价..."
                            maxlength="500" show-word-limit class="review-textarea" />
                    </el-form-item>
                </el-form>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="reviewDialogVisible = false" class="cancel-btn">
                        取消
                    </el-button>
                    <el-button type="primary" @click="submitReview" :loading="submitLoading" class="submit-btn">
                        <el-icon>
                            <Star />
                        </el-icon>
                        提交评价
                    </el-button>
                </div>
            </template>
        </el-dialog>
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
        transform: translateY(-20px);
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

@keyframes reviewAppear {
    from {
        opacity: 0;
        transform: translateX(-30px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes productAppear {
    from {
        opacity: 0;
        transform: translateY(40px) scale(0.9);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

@keyframes emptyAppear {
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

@keyframes modalAppear {
    from {
        opacity: 0;
        transform: scale(0.8) translateY(-50px);
    }

    to {
        opacity: 1;
        transform: scale(1) translateY(0);
    }
}

/* 动画类 */
.animate-page-enter {
    animation: pageEnter 0.8s ease-out;
}

.animate-slide-down {
    animation: slideDown 0.6s ease-out 0.2s both;
}

.animate-card-enter {
    animation: cardEnter 0.8s ease-out 0.4s both;
}

.animate-slide-in-right {
    animation: slideInRight 0.8s ease-out 0.6s both;
}

.animate-title-reveal {
    animation: titleReveal 0.8s ease-out 0.8s both;
}

.title-char {
    display: inline-block;
    animation: charAppear 0.5s ease-out both;
}

.animate-fade-in-up {
    animation: fadeInUp 0.6s ease-out calc(1s + var(--delay)) both;
}

.animate-avatar-appear {
    animation: avatarAppear 1s ease-out 1s both;
}

.animate-section-appear {
    animation: sectionAppear 0.6s ease-out calc(1.2s + var(--delay)) both;
}

.animate-review-appear {
    animation: reviewAppear 0.6s ease-out calc(1.4s + var(--delay)) both;
}

.animate-product-appear {
    animation: productAppear 0.6s ease-out calc(1.6s + var(--delay)) both;
}

.animate-empty-appear {
    animation: emptyAppear 0.8s ease-out 1.8s both;
}

.animate-pulse {
    animation: pulse 2s ease-in-out infinite;
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

.animate-hover-lift:hover {
    transform: translateY(-3px);
    transition: transform 0.3s ease;
}

.animate-modal-appear {
    animation: modalAppear 0.5s ease-out;
}

.animate-skeleton {
    animation: pulse 1.5s ease-in-out infinite;
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

.shop-detail-container {
    max-width: 1200px;
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

/* 店铺信息样式 */
.shop-info-section {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    padding: 40px;
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

.shop-header {
    display: flex;
    gap: 40px;
    align-items: flex-start;
}

.shop-avatar {
    position: relative;
    width: 200px;
    height: 200px;
    flex-shrink: 0;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;
}

.shop-avatar:hover {
    transform: scale(1.05);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
}

.avatar-image {
    width: 100%;
    height: 100%;
}

.avatar-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
    color: white;
    padding: 16px;
    text-align: center;
    font-size: 14px;
    font-weight: 500;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.shop-avatar:hover .avatar-overlay {
    opacity: 1;
}

.shop-meta {
    flex: 1;
    min-width: 0;
}

.shop-name {
    font-size: 32px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0 0 20px 0;
    line-height: 1.3;
}

.shop-rating {
    margin-bottom: 20px;
}

.shop-rate {
    font-size: 16px;
}

.shop-description {
    color: #6b7280;
    font-size: 16px;
    line-height: 1.7;
    margin: 0 0 30px 0;
    text-align: justify;
}

/* 申请区域 */
.apply-section {
    padding: 24px;
    background: linear-gradient(135deg, rgba(78, 205, 196, 0.1) 0%, rgba(255, 255, 255, 0.8) 100%);
    border-radius: 16px;
    border: 1px solid rgba(78, 205, 196, 0.2);
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.1);
}

.apply-button {
    padding: 12px 24px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 25px;
    transition: all 0.3s ease;
    border: none;
}

.apply-button:not(.applied-button) {
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.3);
}

.apply-button:not(.applied-button):hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(78, 205, 196, 0.4);
}

.applied-button {
    background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
    color: white;
}

.apply-tip {
    font-size: 14px;
    color: #6b7280;
    margin-top: 12px;
    margin-bottom: 0;
}

/* 分区样式 */
.reviews-section,
.product-list-section {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    padding: 32px;
    margin-bottom: 30px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
}

.reviews-section:hover,
.product-list-section:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
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
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
}

.section-title h2 {
    font-size: 24px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
}

.count-badge {
    padding: 4px 12px;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    margin-left: 8px;
}

.write-review-btn {
    border-radius: 25px;
    padding: 12px 20px;
    font-weight: 500;
    box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
}

/* 评论列表 */
.reviews-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.review-item {
    padding: 24px;
    border: 1px solid #e5e7eb;
    border-radius: 16px;
    background: rgba(248, 250, 252, 0.8);
    transition: all 0.3s ease;
    cursor: pointer;
}

.review-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    background: white;
    border-color: #4ecdc4;
}

.review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.reviewer-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.reviewer-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 16px;
}

.reviewer-details {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.reviewer-name {
    font-weight: 600;
    color: #2c3e50;
    font-size: 16px;
}

.review-rating {
    font-size: 14px;
}

.review-date {
    color: #9ca3af;
    font-size: 14px;
}

.review-content {
    color: #4b5563;
    line-height: 1.7;
    font-size: 15px;
}

/* 商品网格 */
.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 24px;
}

.product-card {
    background: rgba(255, 255, 255, 0.9);
    border-radius: 16px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.product-card:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.15);
}

.product-image-container {
    position: relative;
    width: 100%;
    height: 240px;
    background: #f5f7fa;
    overflow: hidden;
}

.product-image {
    width: 100%;
    height: 100%;
    transition: transform 0.3s ease;
}

.product-card:hover .product-image {
    transform: scale(1.1);
}

.product-overlay {
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
    font-size: 14px;
    font-weight: 500;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
    opacity: 1;
}

.image-error,
.image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #f8f9fa;
    color: #9ca3af;
    gap: 8px;
}

.product-info {
    padding: 20px;
}

.product-title {
    margin: 0 0 12px 0;
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.product-price {
    color: #ef4444;
    font-size: 20px;
    font-weight: 700;
    margin: 0 0 12px 0;
}

.product-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.product-rate {
    font-size: 14px;
}

/* 空状态 */
.empty-reviews,
.empty-products {
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

/* 对话框样式 */
.review-dialog {
    border-radius: 20px;
    overflow: hidden;
}

.review-dialog :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
    padding: 24px 32px;
}

.review-dialog :deep(.el-dialog__title) {
    font-size: 20px;
    font-weight: 600;
}

.dialog-content {
    padding: 32px;
}

.rating-section {
    padding: 16px;
    background: linear-gradient(135deg, rgba(78, 205, 196, 0.1) 0%, rgba(255, 255, 255, 0.9) 100%);
    border-radius: 12px;
    display: inline-block;
    border: 1px solid rgba(78, 205, 196, 0.2);
}

.dialog-rating {
    margin: 0;
}

.review-textarea {
    margin-top: 16px;
}

.review-textarea :deep(.el-textarea__inner) {
    border-radius: 12px;
    border: 2px solid #e5e7eb;
    transition: all 0.3s ease;
}

.review-textarea :deep(.el-textarea__inner):focus {
    border-color: #4ecdc4;
    box-shadow: 0 0 0 3px rgba(78, 205, 196, 0.1);
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 16px;
    padding: 24px 32px;
    background: #f8fafc;
    border-top: 1px solid #e5e7eb;
}

.cancel-btn,
.submit-btn {
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: 600;
    transition: all 0.3s ease;
}

.submit-btn {
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.3);
}

.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(78, 205, 196, 0.4);
}

/* 响应式设计 */
@media (max-width: 1024px) {
    .shop-header {
        flex-direction: column;
        gap: 30px;
    }

    .shop-avatar {
        width: 100%;
        max-width: 300px;
        height: auto;
        aspect-ratio: 1/1;
        margin: 0 auto;
    }

    .product-grid {
        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
        gap: 20px;
    }
}

@media (max-width: 768px) {
    .page-container {
        padding: 16px;
    }

    .shop-info-section,
    .reviews-section,
    .product-list-section {
        padding: 24px 20px;
    }

    .shop-name {
        font-size: 24px;
    }

    .section-header {
        flex-direction: column;
        gap: 16px;
        align-items: stretch;
    }

    .reviewer-info {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }

    .product-grid {
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 16px;
    }

    .dialog-content {
        padding: 24px 20px;
    }
}

@media (max-width: 480px) {
    .product-grid {
        grid-template-columns: 1fr;
    }

    .breadcrumb {
        font-size: 12px;
        padding: 8px 12px;
    }

    .shop-name {
        font-size: 20px;
    }

    .section-title h2 {
        font-size: 20px;
    }
}
</style>