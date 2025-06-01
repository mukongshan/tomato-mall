<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElSkeleton, ElDialog, ElForm, ElFormItem, ElInput, ElRate, ElButton } from 'element-plus';
import { Picture, Star } from '@element-plus/icons-vue'; // Added Star for consistency
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

// 添加评论相关的响应式数据
const reviewDialogVisible = ref(false);
const reviewForm = ref({
    content: '',
    rate: 5
});
const submitLoading = ref(false);

const checkEmployeeStatus = async () => {
    const response = await getMessageByFromUserAndContent(Number(sessionStorage.getItem('id')), "NEW_EMPLOYEE_APPLICATION");
    return response.data.data; // Assuming this returns a boolean or truthy/falsy value
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
            productId: 0,
            shopId: shopId.value,
            createdAt: new Date().toISOString()
        };

        await addShopReview(reviewData);
        ElMessage.success('评论添加成功');

        reviewForm.value = { content: '', rate: 5 };
        reviewDialogVisible.value = false;
        await fetchShopReviews();
    } catch (error) {
        ElMessage.error('评论添加失败');
        console.error(error);
    } finally {
        submitLoading.value = false;
    }
};

const openReviewDialog = () => {
    if (!isCustomer) {
        ElMessage.warning('请先登录');
        return;
    }
    reviewDialogVisible.value = true;
};

const gotoProductDetail = (productId: number) => {
    router.push(`/product/${productId}`);
};

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
    if (sessionStorage.getItem('id')) { // Ensure user is logged in before checking status
        applied.value = await checkEmployeeStatus();
    }
});

const handleApplyForStaff = async () => {
    const username = sessionStorage.getItem('username');
    if (!username || !shopInfo.value) {
        ElMessage.error('无法获取用户信息或店铺信息');
        return;
    }
    try {
        const userDetailResponse = await getUserDetails(username);
        const userDetails = userDetailResponse.data.data;

        if (!userDetails) {
            ElMessage.error('无法获取用户详细信息');
            return;
        }

        const updateUser: Partial<UserDetail> = { // Use Partial for update
            id: userDetails.id,
            isValidStaff: 0, // 设置为待审核状态
            shopId: shopInfo.value.shopId
        };

        const message: Message = {
            id: 0,
            isRead: false,
            fromUser: userDetails.id,
            toUser: shopInfo.value.ownerId,
            content: "NEW_EMPLOYEE_APPLICATION",
            createdTime: new Date().toISOString()
        };
        await sendMessage(message);
        await updateUserInfo(updateUser as UserDetail); // Cast to UserDetail if API expects full object
        ElMessage.success('申请已提交，请等待审核');
        applied.value = true; // Update applied status locally
    } catch (error) {
        ElMessage.error('申请提交失败，请稍后重试');
        console.error('申请成为店员失败:', error);
    }
};
</script>

<template>
    <div class="shop-detail-container">
        <!-- 店铺信息 -->
        <div v-if="shopInfo" class="shop-info-section fade-in">
            <div class="shop-header">
                <div class="shop-avatar slide-left">
                    <el-image :src="shopInfo.iconUrl" fit="cover" class="avatar-image">
                        <template #error>
                            <div class="image-error-global">
                                <el-icon class="bounce">
                                    <Picture />
                                </el-icon>
                                <span>店铺图片加载失败</span>
                            </div>
                        </template>
                        <div v-if="!shopInfo.iconUrl" class="image-placeholder-global">
                            <el-icon>
                                <Picture />
                            </el-icon>
                            <span>暂无店铺图片</span>
                        </div>
                    </el-image>
                </div>
                <div class="shop-meta slide-right">
                    <h1 class="shop-name slide-up">{{ shopInfo.name }}</h1>
                    <div class="shop-rating-display slide-up-delay">
                        <el-rate v-model="shopInfo.rate" disabled show-score text-color="#ff9900"
                            score-template="{value} 分" size="large" />
                    </div>
                    <p class="shop-description fade-in-text">{{ shopInfo.description }}</p>

                    <div class="apply-section fade-in-up" v-if="isCustomer">
                        <el-button v-if="applied" disabled type="info" class="action-button">
                            您已有店员申请
                        </el-button>
                        <el-button v-else type="primary" @click="handleApplyForStaff"
                            class="action-button apply-button">
                            申请成为店员
                        </el-button>
                        <p class="apply-tip">审核通过后即可参与店铺管理</p>
                    </div>
                </div>
            </div>
        </div>
        <div v-else class="empty-state fade-in">
            <el-skeleton :rows="5" animated />
        </div>


        <!-- 店铺评论区域 -->
        <div class="reviews-section fade-in-up">
            <div class="section-header-global">
                <h2 class="section-title-global">店铺评价</h2>
                <el-button v-if="isCustomer" type="primary" @click="openReviewDialog"
                    class="action-button write-review-button">
                    写评价
                </el-button>
            </div>

            <el-skeleton v-if="reviewsLoading" :rows="3" animated />

            <div v-else-if="reviews.length === 0" class="empty-reviews fade-in">
                <el-empty description="暂无评价，快来写下第一条吧！" />
            </div>

            <div v-else class="reviews-list">
                <div v-for="(review, index) in reviews" :key="review.id" class="review-item scale-in"
                    :style="{ animationDelay: `${index * 0.1}s` }">
                    <div class="review-header">
                        <div class="reviewer-info">
                            <span class="reviewer-name">用户{{ review.accountId }}</span>
                            <el-rate v-model="review.rate" disabled text-color="#ff9900" class="review-rating-stars"
                                size="small" />
                        </div>
                        <span class="review-date fade-in-text">{{ formatDate(review.createdAt) }}</span>
                    </div>
                    <div class="review-content">
                        {{ review.content }}
                    </div>
                </div>
            </div>
        </div>

        <!-- 商品列表 -->
        <div class="product-list-section fade-in-up">
            <h2 class="section-title-global">店铺商品</h2>

            <el-skeleton v-if="loading" :rows="6" animated />

            <div v-else-if="products.length === 0" class="empty-products fade-in">
                <el-empty description="该店铺暂无商品" />
            </div>

            <div v-else class="product-grid">
                <div v-for="(product, index) in products" :key="product.id" class="product-card-item scale-in"
                    @click="gotoProductDetail(product.id)" :style="{ animationDelay: `${index * 0.05}s` }">
                    <div class="product-image-container">
                        <el-image :src="product.cover" fit="cover" class="product-image">
                            <template #error>
                                <div class="image-error-global product-image-error">
                                    <el-icon class="bounce">
                                        <Picture />
                                    </el-icon>
                                    <span>图片加载失败</span>
                                </div>
                            </template>
                            <div v-if="!product.cover" class="image-placeholder-global product-image-error">
                                <el-icon>
                                    <Picture />
                                </el-icon>
                                <span>暂无图片</span>
                            </div>
                        </el-image>
                    </div>

                    <div class="product-info">
                        <h3 class="product-title">{{ product.title }}</h3>
                        <p class="product-price">¥ {{ product.price.toFixed(2) }}</p>
                        <div class="product-meta-info">
                            <el-rate v-model="product.rate" disabled text-color="#ff9900" class="product-rate-stars"
                                size="small" />
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 添加评论对话框 -->
        <el-dialog v-model="reviewDialogVisible" title="添加店铺评价" width="500px" class="review-dialog" :before-close="() => {
            reviewForm = { content: '', rate: 5 };
            reviewDialogVisible = false;
        }">
            <el-form :model="reviewForm" label-width="80px" class="review-form">
                <el-form-item label="评分" prop="rate">
                    <div class="dialog-rating">
                        <el-rate v-model="reviewForm.rate" show-text text-color="#ff9900" size="large" />
                    </div>
                </el-form-item>
                <el-form-item label="评价内容" prop="content">
                    <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请输入您对这家店铺的评价..."
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
/* Animation Keyframes (reused from previous example) */
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

/* Animation Classes */
.fade-in {
    animation: fadeIn 0.8s ease-out both;
}

.fade-in-up {
    animation: fadeInUp 0.9s ease-out both;
}

.slide-up {
    animation: slideUp 0.7s ease-out both;
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
    animation: fadeInText 0.5s ease-out both;
}

/* Base Container */
.shop-detail-container {
    max-width: 1200px;
    margin: 30px auto;
    padding: 20px;
    background-color: #f8f9fa;
    /* Light gray background */
    min-height: 100vh;
}

/* Card-like Sections (Shop Info, Reviews, Products) */
.shop-info-section,
.reviews-section,
.product-list-section {
    background: #ffffff;
    border-radius: 16px;
    padding: 24px 32px;
    margin-bottom: 24px;
    box-shadow: 0 6px 25px rgba(0, 0, 0, 0.07);
    border: 1px solid #e9ecef;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.shop-info-section:hover,
.reviews-section:hover,
.product-list-section:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

/* Global Section Header */
.section-header-global {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #dee2e6;
}

.section-title-global {
    font-size: 22px;
    font-weight: 600;
    color: #343a40;
    position: relative;
}

.section-title-global::after {
    content: '';
    position: absolute;
    bottom: -17px;
    /* Adjusted for border */
    left: 0;
    width: 0;
    height: 3px;
    background-color: #007bff;
    /* Primary blue */
    transition: width 0.5s ease;
}

.section-title-global:hover::after {
    width: 70px;
}

/* Shop Info Section */
.shop-header {
    display: flex;
    gap: 32px;
    /* Increased gap */
    align-items: flex-start;
    /* Align items to the top */
}

.shop-avatar {
    width: 220px;
    /* Slightly larger avatar */
    height: 220px;
    flex-shrink: 0;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.avatar-image {
    width: 100%;
    height: 100%;
    transition: transform 0.4s ease;
}

.avatar-image:hover {
    transform: scale(1.05);
}

.shop-meta {
    flex: 1;
    padding-top: 10px;
    /* Add some padding to align text better if avatar is large */
}

.shop-name {
    margin: 0 0 12px 0;
    font-size: 28px;
    font-weight: 700;
    color: #0056b3;
    /* Darker blue for shop name */
    line-height: 1.3;
}

.shop-rating-display {
    margin-bottom: 16px;
}

.shop-rating-display .el-rate__icon {
    /* Target stars inside el-rate */
    font-size: 24px !important;
    /* Make shop rating stars larger */
}

.shop-rating-display .el-rate__text {
    font-size: 18px !important;
    margin-left: 8px;
}


.shop-description {
    color: #495057;
    /* Slightly darker text for description */
    line-height: 1.7;
    margin: 16px 0;
    font-size: 15px;
    background-color: #f8f9fa;
    padding: 12px;
    border-radius: 8px;
}

.apply-section {
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #e9ecef;
}

.apply-tip {
    font-size: 13px;
    color: #6c757d;
    margin-top: 10px;
}

/* Reviews Section */
.reviews-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
    /* Increased gap between review items */
}

.review-item {
    padding: 20px;
    border: 1px solid #e0e0e0;
    border-radius: 12px;
    background-color: #fdfdfd;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.review-item:hover {
    transform: translateY(-3px) scale(1.01);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
    border-color: #007bff;
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
    gap: 10px;
}

.reviewer-name {
    font-weight: 600;
    color: #0056b3;
    /* Consistent blue for names */
}

.review-item:hover .reviewer-name {
    color: #007bff;
}

.review-rating-stars .el-rate__icon {
    margin-right: 1px !important;
    /* Adjust spacing for small stars */
}

.review-date {
    color: #6c757d;
    font-size: 12px;
}

.review-content {
    color: #495057;
    line-height: 1.6;
    font-size: 14px;
}

.review-item:hover .review-content {
    color: #343a40;
}

/* Product List Section */
.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    /* Slightly larger minmax */
    gap: 24px;
}

.product-card-item {
    /* Renamed from product-card to avoid conflict */
    background: #fff;
    border-radius: 12px;
    /* Softer radius */
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    border: 1px solid #e9ecef;
    display: flex;
    flex-direction: column;
}

.product-card-item:hover {
    transform: translateY(-8px);
    /* More pronounced hover effect */
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.12);
}

.product-image-container {
    width: 100%;
    height: 220px;
    /* Adjusted height */
    background: #f8f9fa;
    overflow: hidden;
    /* Ensure image hover effect is contained */
}

.product-image {
    width: 100%;
    height: 100%;
    transition: transform 0.4s ease;
}

.product-card-item:hover .product-image {
    transform: scale(1.08);
    /* Zoom effect on image */
}

.product-info {
    padding: 16px;
    flex-grow: 1;
    /* Allow info to take remaining space */
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    /* Distribute content within info */
}

.product-title {
    margin: 0 0 8px 0;
    font-size: 17px;
    /* Slightly larger title */
    font-weight: 600;
    /* Bolder title */
    color: #343a40;
    line-height: 1.4;
    height: 2.8em;
    /* Limit to 2 lines */
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}

.product-price {
    color: #e63946;
    /* Vibrant red for price */
    font-size: 19px;
    font-weight: 700;
    /* Bolder price */
    margin: 8px 0;
}

.product-meta-info {
    display: flex;
    justify-content: flex-start;
    /* Align stars to the left */
    align-items: center;
    margin-top: auto;
    /* Push to bottom */
}

.product-rate-stars .el-rate__icon {
    margin-right: 1px !important;
}

.product-rate-stars .el-rate__text {
    display: none;
    /* Hide score text for product cards if desired */
}


/* Global Image Error/Placeholder */
.image-error-global,
.image-placeholder-global {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #e9ecef;
    /* Lighter background */
    color: #6c757d;
    border-radius: inherit;
    /* Inherit border-radius from parent */
}

.image-error-global .el-icon,
.image-placeholder-global .el-icon {
    font-size: 48px;
    margin-bottom: 12px;
}

.product-image-error {
    /* Specific for product card image placeholders */
    font-size: 13px;
}

.product-image-error .el-icon {
    font-size: 32px;
    margin-bottom: 8px;
}


/* Buttons */
.action-button {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    padding: 10px 20px;
    font-size: 15px;
}

.action-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(0, 123, 255, 0.3);
}

.apply-button {
    background-color: #28a745;
    /* Green for apply */
    border-color: #28a745;
}

.apply-button:hover {
    background-color: #218838;
    border-color: #1e7e34;
    box-shadow: 0 6px 15px rgba(40, 167, 69, 0.4);
}

.write-review-button {
    background-color: #007bff;
    border-color: #007bff;
}

.write-review-button:hover {
    background-color: #0069d9;
    border-color: #0062cc;
}


/* Dialog Styles (reused) */
.review-dialog {
    animation: fadeIn 0.4s ease-out;
}

.dialog-rating {
    padding: 12px;
    background: linear-gradient(135deg, #f8f9fa, #ffffff);
    border-radius: 8px;
    display: inline-block;
    border: 1px solid #dee2e6;
}

.review-textarea {
    transition: all 0.3s ease;
}

.review-textarea:focus-within {
    transform: scale(1.01);
    border-color: #007bff !important;
    box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, .25)
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.cancel-btn:hover {
    transform: translateX(-2px);
}

.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(0, 123, 255, 0.3);
}


/* Empty States */
.empty-reviews,
.empty-products,
.empty-state {
    text-align: center;
    padding: 48px 0;
    color: #6c757d;
}

.empty-state .el-skeleton {
    padding: 20px;
}


/* Responsive Adjustments */
@media (max-width: 992px) {
    .product-grid {
        grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    }
}

@media (max-width: 768px) {
    .shop-header {
        flex-direction: column;
        align-items: center;
        /* Center avatar when stacked */
    }

    .shop-avatar {
        width: 200px;
        /* Maintain a good size */
        height: 200px;
        margin-bottom: 20px;
    }

    .shop-meta {
        text-align: center;
        /* Center text when stacked */
    }

    .section-header-global {
        flex-direction: column;
        gap: 16px;
        align-items: stretch;
        /* Stretch items */
    }

    .section-header-global .action-button {
        width: 100%;
        /* Full width button */
    }

    .product-grid {
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    }
}

@media (max-width: 480px) {
    .shop-detail-container {
        padding: 15px;
    }

    .shop-info-section,
    .reviews-section,
    .product-list-section {
        padding: 20px;
    }

    .shop-name {
        font-size: 24px;
    }

    .product-grid {
        grid-template-columns: 1fr;
        /* Single column */
    }

    .product-title {
        font-size: 16px;
    }

    .product-price {
        font-size: 18px;
    }

    .dialog-rating .el-rate {
        /* Ensure rate component is not too large on mobile */
        transform: scale(0.9);
        transform-origin: left;
    }
}
</style>