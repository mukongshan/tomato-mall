<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { getProduct } from '@/api/product.ts';
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElRate, ElButton, ElSkeleton } from 'element-plus';
import type { Product } from '@/api/product.ts';
import { Review, getProductReviews, addProductReview } from '@/api/review';
import { Picture, Star } from '@element-plus/icons-vue';
import { isCustomer } from '@/components/LoginEvent';

const route = useRoute();
const product = ref<Product | null>(null);
const reviews = ref<Review[]>([]);
const reviewsLoading = ref(true);

// 添加评论相关数据
const reviewDialogVisible = ref(false);
const reviewForm = ref({
    content: '',
    rate: 5
});
const submitLoading = ref(false);

// 评分动画相关
const displayRate = ref(0);
const displayPrice = ref(0);
const averageReviewRate = ref(0);

// 计算评分统计
const ratingStats = computed(() => {
    const total = reviews.value.length;

    if (total === 0) {
        return {
            average: 0,
            total: 0,
            distribution: [0, 0, 0, 0, 0]
        };
    }

    const sum = reviews.value.reduce((acc, review) => acc + review.rate, 0);
    const average = Number((sum / total).toFixed(1));
    const distribution = [5, 4, 3, 2, 1].map(star =>
        reviews.value.filter(review => review.rate === star).length
    );

    return { average, total, distribution };
});

// 修正：计算精确星星填充百分比
const getStarFillPercentage = (starIndex: number, rating: number) => {
    const starPosition = starIndex - 1;
    const diff = rating - starPosition;

    if (diff >= 1) return 100;
    if (diff <= 0) return 0;
    return Math.round(diff * 100);
};

// 数字动画函数
const animateNumber = (target: number, callback: (value: number) => void, duration = 1000) => {
    const start = 0;
    const startTime = performance.now();

    const animate = (currentTime: number) => {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);

        const easeOutQuart = 1 - Math.pow(1 - progress, 4);
        const current = start + (target - start) * easeOutQuart;

        callback(current);

        if (progress < 1) {
            requestAnimationFrame(animate);
        }
    };

    requestAnimationFrame(animate);
};

// 获取商品评论
const fetchProductReviews = async (productId: number) => {
    try {
        reviewsLoading.value = true;
        const response = await getProductReviews(productId);
        reviews.value = response.data.data || [];

        // 启动动画
        setTimeout(() => {
            if (product.value) {
                animateNumber(product.value.rate, (value) => {
                    displayRate.value = value;
                });

                animateNumber(product.value.price, (value) => {
                    displayPrice.value = value;
                });

                animateNumber(ratingStats.value.average, (value) => {
                    averageReviewRate.value = value;
                });
            }
        }, 500);

    } catch (error) {
        ElMessage.error('获取评论失败');
        console.error(error);
    } finally {
        reviewsLoading.value = false;
    }
};

// 提交评论
const submitReview = async () => {
    if (!product.value) return;

    try {
        submitLoading.value = true;
        const reviewData: Review = {
            id: 0,
            accountId: Number(sessionStorage.getItem('id')),
            content: reviewForm.value.content,
            rate: reviewForm.value.rate,
            type: 'PRODUCT',
            productId: product.value.id,
            shopId: product.value.shopId || 0,
            createdAt: new Date().toISOString()
        };

        await addProductReview(reviewData);

        // 重置表单并关闭对话框
        reviewForm.value = { content: '', rate: 5 };
        reviewDialogVisible.value = false;

        // 重新获取评论列表
        await fetchProductReviews(product.value.id);
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
    try {
        const productId = Number(route.params.id);
        const res = await getProduct(productId);
        if (res.data.code === '200') {
            product.value = res.data.data;
            await fetchProductReviews(productId);
        } else {
            ElMessage.error(res.data.msg);
        }
    } catch (error) {
        ElMessage.error('获取商品详情失败');
    }
});
</script>

<template>
    <div class="product-detail-container">
        <div v-if="product" class="product-card fade-in">
            <div class="product-header">
                <h1 class="slide-up">{{ product.title }}</h1>

                <!-- 规范化的价格和评分信息 -->
                <div class="product-meta slide-up-delay">
                    <div class="meta-row">
                        <div class="meta-item price-meta">
                            <div class="meta-label">
                                <span class="label-text">商品价格</span>
                            </div>
                            <div class="meta-content">
                                <div class="price-wrapper">
                                    <span class="currency">￥</span>
                                    <span class="price-number">{{ displayPrice.toFixed(2) }}</span>
                                </div>
                            </div>
                        </div>

                        <div class="meta-divider"></div>

                        <div class="meta-item rating-meta">
                            <div class="meta-label">
                                <span class="label-text">商品评分</span>
                            </div>
                            <div class="meta-content">
                                <div class="rating-wrapper">
                                    <div class="rating-score">
                                        <span class="score-number">{{ displayRate.toFixed(1) }}</span>
                                        <span class="score-max">/ 5</span>
                                    </div>
                                    <!-- MODIFIED SECTION FOR PRODUCT RATING STARS -->
                                    <div class="rating-stars precise-stars product-main-rating-stars">
                                        <div v-for="n in 5" :key="n" class="precise-star-wrapper">
                                            <div class="precise-star">
                                                <el-icon class="star-bg">
                                                    <Star />
                                                </el-icon>
                                                <el-icon class="star-fill" :style="{
                                                    clipPath: `inset(0 ${100 - getStarFillPercentage(n, displayRate)}% 0 0)`
                                                }" v-if="getStarFillPercentage(n, displayRate) > 0">
                                                    <Star />
                                                </el-icon>
                                            </div>
                                        </div>
                                    </div>
                                    <!-- END OF MODIFIED SECTION -->
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="product-content">
                <div class="image-section slide-left">
                    <el-image :src="product.cover" :alt="product.title" class="main-image" fit="cover">
                        <template #error>
                            <div class="image-error">
                                <el-icon class="bounce">
                                    <Picture />
                                </el-icon>
                                <span>图片加载失败</span>
                            </div>
                        </template>
                    </el-image>
                </div>

                <div class="info-section slide-right">
                    <h3 class="section-title">商品描述</h3>
                    <p class="text">{{ product.description }}</p>

                    <h3 class="section-title">商品细节</h3>
                    <p class="text">{{ product.detail }}</p>

                    <h3 class="section-title">商品规格</h3>
                    <div class="specifications">
                        <div v-for="(spec, index) in product.specifications" :key="index" class="spec-item"
                            :style="{ animationDelay: `${index * 0.1}s` }">
                            <span class="spec-name">{{ spec.item }}：</span>
                            <span class="spec-value">{{ spec.value }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 商品评论区域 -->
        <div v-if="product" class="reviews-card fade-in-up">
            <div class="section-header">
                <div class="reviews-title-area">
                    <h2 class="section-title">用户评价</h2>

                    <!-- 规范化的评价统计 -->
                    <div class="review-stats">
                        <div class="stats-container">
                            <div class="overall-rating">
                                <div class="rating-summary">
                                    <div class="summary-score">
                                        <span class="big-score">{{ averageReviewRate.toFixed(1) }}</span>
                                        <div class="score-info">
                                            <div class="precise-stars">
                                                <div v-for="n in 5" :key="n" class="precise-star-wrapper">
                                                    <div class="precise-star">
                                                        <el-icon class="star-bg">
                                                            <Star />
                                                        </el-icon>
                                                        <el-icon class="star-fill" :style="{
                                                            clipPath: `inset(0 ${100 - getStarFillPercentage(n, averageReviewRate)}% 0 0)`
                                                        }" v-if="getStarFillPercentage(n, averageReviewRate) > 0">
                                                            <Star />
                                                        </el-icon>
                                                    </div>
                                                </div>
                                            </div>
                                            <span class="review-count">基于 {{ ratingStats.total }} 条评价</span>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="rating-breakdown">
                                <div class="breakdown-title">评分分布</div>
                                <div class="breakdown-list">
                                    <div v-for="(count, index) in ratingStats.distribution" :key="index"
                                        class="breakdown-row">
                                        <span class="star-count">{{ 5 - index }} 星</span>
                                        <div class="progress-container">
                                            <div class="progress-track">
                                                <div class="progress-bar"
                                                    :style="{ width: ratingStats.total > 0 ? (count / ratingStats.total * 100) + '%' : '0%' }">
                                                </div>
                                            </div>
                                        </div>
                                        <span class="review-num">{{ count }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <el-button v-if="isCustomer" type="primary" class="write-btn" @click="openReviewDialog">
                    写评价
                </el-button>
            </div>

            <el-skeleton v-if="reviewsLoading" :rows="3" animated />

            <div v-else-if="reviews.length === 0" class="empty-reviews fade-in">
                <el-empty description="暂无评价，快来写下第一条评价吧！" />
            </div>

            <div v-else class="reviews-list">
                <div v-for="(review, index) in reviews" :key="review.id" class="review-item scale-in"
                    :style="{ animationDelay: `${index * 0.15}s` }">
                    <div class="review-header">
                        <div class="reviewer-info">
                            <span class="reviewer-name">用户{{ review.accountId }}</span>
                            <div class="review-rating-display">
                                <div v-for="n in 5" :key="n" class="review-star"
                                    :class="{ 'filled': n <= review.rate }">
                                    <el-icon>
                                        <Star />
                                    </el-icon>
                                </div>
                            </div>
                        </div>
                        <span class="review-date fade-in-text">{{ formatDate(review.createdAt) }}</span>
                    </div>
                    <div class="review-content">
                        {{ review.content }}
                    </div>
                </div>
            </div>
        </div>

        <div v-else class="empty-state fade-in">
            <el-empty description="商品不存在或已下架" />
        </div>

        <!-- 添加评论对话框 -->
        <el-dialog v-model="reviewDialogVisible" title="添加商品评价" width="500px" class="review-dialog" :before-close="() => {
            reviewForm = { content: '', rate: 5 };
            reviewDialogVisible = false;
        }">
            <el-form :model="reviewForm" label-width="80px" class="review-form">
                <el-form-item label="评分">
                    <div class="dialog-rating">
                        <el-rate v-model="reviewForm.rate" show-text text-color="#ff9900" size="large" />
                    </div>
                </el-form-item>
                <el-form-item label="评价内容">
                    <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请输入您对这个商品的评价..."
                        maxlength="500" show-word-limit class="review-textarea" />
                </el-form-item>
            </el-form>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="reviewDialogVisible = false" class="cancel-btn">取消</el-button>
                    <el-button type="primary" @click="submitReview" :loading="submitLoading" class="submit-btn">
                        提交评价
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
/* 动效关键帧 */
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
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

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(40px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideLeft {
    from {
        opacity: 0;
        transform: translateX(-40px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes slideRight {
    from {
        opacity: 0;
        transform: translateX(40px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
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

@keyframes fadeInText {
    from {
        opacity: 0;
        filter: blur(2px);
    }

    to {
        opacity: 1;
        filter: blur(0);
    }
}

@keyframes gradientShift {
    0% {
        background-position: 0% 50%;
    }

    50% {
        background-position: 100% 50%;
    }

    100% {
        background-position: 0% 50%;
    }
}

@keyframes shimmer {
    0% {
        left: -100%;
    }

    100% {
        left: 100%;
    }
}

@keyframes softGlow {
    0% {
        box-shadow: 0 0 10px rgba(59, 130, 246, 0.2);
    }

    50% {
        box-shadow: 0 0 20px rgba(59, 130, 246, 0.4);
    }

    100% {
        box-shadow: 0 0 10px rgba(59, 130, 246, 0.2);
    }
}

/* 动效类 */
.fade-in {
    animation: fadeIn 0.8s ease-out;
}

.fade-in-up {
    animation: fadeInUp 0.9s ease-out;
}

.slide-up {
    animation: slideUp 0.7s ease-out;
}

.slide-up-delay {
    animation: slideUp 0.7s ease-out 0.2s both;
}

.slide-left {
    animation: slideLeft 0.8s ease-out 0.3s both;
}

.slide-right {
    animation: slideRight 0.8s ease-out 0.4s both;
}

.scale-in {
    animation: scaleIn 0.6s ease-out both;
}

.bounce {
    animation: bounce 1s ease-in-out infinite;
}

.fade-in-text {
    animation: fadeInText 0.5s ease-out;
}

/* 基础样式 */
.product-detail-container {
    max-width: 1200px;
    margin: 30px auto;
    padding: 0 20px;
    background-color: #fafafa;
    min-height: 100vh;
    padding-top: 30px;
    padding-bottom: 30px;
}

.product-card {
    background: #ffffff;
    border-radius: 16px;
    padding: 32px;
    margin-bottom: 24px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.product-header h1 {
    font-size: 28px;
    font-weight: 600;
    color: #2563eb;
    /* New blue color for product title */
    margin-bottom: 32px;
    line-height: 1.3;
}

/* 规范化的商品元信息区域 */
.product-meta {
    background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
    border-radius: 12px;
    padding: 24px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.meta-row {
    display: flex;
    align-items: center;
    justify-content: space-around;
    gap: 32px;
}

.meta-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
}

.meta-divider {
    width: 1px;
    height: 60px;
    background: linear-gradient(to bottom, transparent, #e5e7eb, transparent);
}

.meta-label {
    margin-bottom: 12px;
}

.label-text {
    font-size: 14px;
    color: #6b7280;
    font-weight: 500;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.meta-content {
    display: flex;
    flex-direction: column;
    align-items: center;
}

/* 价格样式 */
.price-wrapper {
    display: flex;
    align-items: baseline;
    gap: 4px;
}

.currency {
    font-size: 20px;
    font-weight: 600;
    color: #dc2626;
}

.price-number {
    font-size: 32px;
    font-weight: 700;
    background: linear-gradient(45deg, #dc2626, #ef4444);
    background-size: 200% 200%;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: gradientShift 3s ease-in-out infinite;
}

/* 评分样式 */
.rating-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.rating-score {
    display: flex;
    align-items: baseline;
    gap: 4px;
}

.score-number {
    font-size: 32px;
    font-weight: 700;
    background: linear-gradient(45deg, #3b82f6, #1d4ed8);
    background-size: 200% 200%;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: gradientShift 3s ease-in-out infinite;
}

.score-max {
    font-size: 16px;
    color: #6b7280;
    font-weight: 500;
}

.rating-stars {
    display: flex;
    gap: 3px;
}

/* 商品内容区域 */
.product-content {
    display: flex;
    gap: 40px;
    margin-top: 32px;
}

.image-section {
    flex: 1 1 400px;
    max-width: 480px;
}

.main-image {
    width: 100%;
    height: 480px;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    cursor: pointer;
}

.main-image:hover {
    transform: scale(1.02);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 480px;
    color: #9ca3af;
    font-size: 14px;
    background-color: #f9fafb;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
}

.image-error .el-icon {
    font-size: 48px;
    margin-bottom: 12px;
}

.info-section {
    flex: 1;
    min-width: 320px;
}

.section-title {
    margin: 24px 0 12px;
    font-size: 18px;
    font-weight: 600;
    color: #374151;
    padding-bottom: 8px;
    border-bottom: 1px solid #e5e7eb;
    position: relative;
    transition: color 0.3s ease;
}

.section-title::after {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 0;
    width: 0;
    height: 2px;
    background-color: #3b82f6;
    transition: width 0.5s ease;
}

.section-title:hover::after {
    width: 60px;
}

.text {
    font-size: 15px;
    line-height: 1.6;
    color: #6b7280;
    margin-bottom: 16px;
    padding: 12px;
    background-color: #f9fafb;
    border-radius: 8px;
    transition: all 0.3s ease;
}

.text:hover {
    background-color: #f3f4f6;
    transform: translateX(4px);
}

.specifications {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 12px;
    margin-top: 16px;
}

.spec-item {
    background-color: #ffffff;
    padding: 12px 16px;
    border-radius: 8px;
    border: 1px solid #e5e7eb;
    font-size: 14px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    cursor: pointer;
    animation: scaleIn 0.6s ease-out both;
}

.spec-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-color: #3b82f6;
}

.spec-name {
    font-weight: 500;
    color: #374151;
    transition: color 0.3s ease;
}

.spec-value {
    color: #6b7280;
    transition: color 0.3s ease;
}

.spec-item:hover .spec-name,
.spec-item:hover .spec-value {
    color: #3b82f6;
}

/* 评论区域 */
.reviews-card {
    background: #ffffff;
    border-radius: 16px;
    padding: 32px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.reviews-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e5e7eb;
}

.reviews-title-area {
    flex: 1;
}

/* 规范化的评价统计区域 */
.review-stats {
    margin-top: 20px;
}

.stats-container {
    display: flex;
    gap: 40px;
    align-items: flex-start;
}

.overall-rating {
    flex-shrink: 0;
}

.rating-summary {
    background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
    border-radius: 12px;
    padding: 24px;
    text-align: center;
    border: 1px solid #e5e7eb;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    min-width: 160px;
}

.summary-score {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
}

.big-score {
    font-size: 36px;
    font-weight: 700;
    background: linear-gradient(45deg, #3b82f6, #1d4ed8);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    line-height: 1;
}

.score-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

/* 精确星星显示 */
.precise-stars {
    display: flex;
    gap: 3px;
}

.precise-star-wrapper {
    position: relative;
}

.precise-star {
    position: relative;
    width: 18px;
    height: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.precise-star .star-bg {
    color: #d1d5db;
    position: absolute;
    font-size: 18px;
}

/* Default star fill color (yellow for average review rating) */
.precise-star .star-fill {
    color: #fbbf24;
    filter: drop-shadow(0 0 4px rgba(251, 191, 36, 0.5));
    position: absolute;
    font-size: 18px;
}

/* Specific star fill color for product's main rating (green) */
.product-main-rating-stars .precise-star .star-fill {
    color: #22c55e;
    /* Green color */
    filter: drop-shadow(0 0 4px rgba(34, 197, 94, 0.6));
    /* Adjusted green shadow */
}


.review-count {
    font-size: 12px;
    color: #6b7280;
    font-weight: 500;
}

/* 评分分布 */
.rating-breakdown {
    flex: 1;
    max-width: 280px;
}

.breakdown-title {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 16px;
}

.breakdown-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.breakdown-row {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 13px;
}

.star-count {
    width: 40px;
    color: #6b7280;
    font-weight: 500;
    text-align: right;
}

.progress-container {
    flex: 1;
}

.progress-track {
    height: 8px;
    background-color: #f3f4f6;
    border-radius: 4px;
    overflow: hidden;
}

.progress-bar {
    height: 100%;
    background: linear-gradient(90deg, #3b82f6, #1d4ed8);
    border-radius: 4px;
    transition: width 1.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.review-num {
    width: 30px;
    text-align: right;
    color: #6b7280;
    font-weight: 500;
}

.reviews-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.review-item {
    padding: 20px;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    background-color: #fafafa;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    cursor: pointer;
}

.review-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    background-color: #ffffff;
    border-color: #3b82f6;
}

.review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.reviewer-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.reviewer-name {
    font-weight: 500;
    color: #374151;
    transition: color 0.3s ease;
}

.review-item:hover .reviewer-name {
    color: #3b82f6;
}

.review-rating-display {
    display: flex;
    gap: 2px;
}

.review-star {
    font-size: 14px;
    color: #d1d5db;
    transition: all 0.3s ease;
}

.review-star.filled {
    color: #fbbf24;
}

.review-date {
    color: #9ca3af;
    font-size: 12px;
    transition: color 0.3s ease;
}

.review-content {
    color: #6b7280;
    line-height: 1.6;
    font-size: 14px;
    transition: color 0.3s ease;
}

.review-item:hover .review-content {
    color: #374151;
}

.empty-reviews,
.empty-state {
    text-align: center;
    padding: 48px 0;
}

/* 按钮和对话框 */
.write-btn,
.submit-btn {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.write-btn:hover,
.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.3);
}

.cancel-btn {
    transition: all 0.3s ease;
}

.cancel-btn:hover {
    transform: translateX(-2px);
}

.review-dialog {
    animation: fadeIn 0.4s ease-out;
}

.dialog-rating {
    padding: 12px;
    background: linear-gradient(135deg, #f8fafc, #ffffff);
    border-radius: 8px;
    display: inline-block;
    border: 1px solid rgba(59, 130, 246, 0.1);
}

.review-textarea {
    transition: all 0.3s ease;
}

.review-textarea:focus-within {
    transform: scale(1.01);
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}
</style>