<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElSkeleton, ElDialog, ElForm, ElFormItem, ElInput, ElRate, ElButton } from 'element-plus';
import { Picture } from '@element-plus/icons-vue';
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
    router.push(`/product/detail/${productId}`);
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
    <div class="shop-detail-container">
        <!-- 店铺信息 -->
        <div v-if="shopInfo" class="shop-info-section">
            <div class="shop-header">
                <div class="shop-avatar">
                    <el-image :src="shopInfo.iconUrl" fit="cover" class="avatar-image">
                        <template #error>
                            <div class="image-error">
                                <el-icon>
                                    <Picture />
                                </el-icon>
                                <span>店铺图片加载失败</span>
                            </div>
                        </template>
                        <div v-if="!shopInfo.iconUrl" class="image-placeholder">
                            <el-icon>
                                <Picture />
                            </el-icon>
                            <span>暂无店铺图片</span>
                        </div>
                    </el-image>
                </div>
                <div class="shop-meta">
                    <h1 class="shop-name">{{ shopInfo.name }}</h1>
                    <el-rate v-model="shopInfo.rate" disabled show-score text-color="#ff9900"
                        score-template="{value} 分" />
                    <p class="shop-description">{{ shopInfo.description }}</p>

                    <!-- 新增：申请成为店员按钮 -->
                    <div class="apply-section" v-if="isCustomer">
                        <el-button v-if="applied" disabled type="info">
                            您已有店员申请
                        </el-button>

                        <el-button v-else type="primary" @click="handleApplyForStaff">
                            申请成为店员
                        </el-button>
                        <p class="apply-tip">审核通过后即可参与店铺管理</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- 店铺评论区域 -->
        <div class="reviews-section">
            <div class="section-header">
                <h2 class="section-title">店铺评价</h2>
                <el-button v-if="isCustomer" type="primary" @click="openReviewDialog">
                    写评价
                </el-button>
            </div>

            <el-skeleton v-if="reviewsLoading" :rows="3" animated />

            <div v-else-if="reviews.length === 0" class="empty-reviews">
                <el-empty description="暂无评价" />
            </div>

            <div v-else class="reviews-list">
                <div v-for="review in reviews" :key="review.id" class="review-item">
                    <div class="review-header">
                        <div class="reviewer-info">
                            <span class="reviewer-name">用户{{ review.accountId }}</span>
                            <el-rate v-model="review.rate" disabled text-color="#ff9900" class="review-rating" />
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
        <div class="product-list-section">
            <h2 class="section-title">店铺商品</h2>

            <el-skeleton v-if="loading" :rows="6" animated />

            <div v-else-if="products.length === 0" class="empty-products">
                <el-empty description="该店铺暂无商品" />
            </div>

            <div v-else class="product-grid">
                <div v-for="product in products" :key="product.id" class="product-card"
                    @click="gotoProductDetail(product.id)">
                    <div class="product-image-container">
                        <el-image :src="product.cover" fit="cover" class="product-image">
                            <template #error>
                                <div class="image-error">
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
                                    <span>图片加载失败</span>
                                </div>
                            </template>
                            <div v-if="!product.cover" class="image-placeholder">
                                <el-icon>
                                    <Picture />
                                </el-icon>
                                <span>暂无商品图片</span>
                            </div>
                        </el-image>
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

        <!-- 添加评论对话框 -->
        <el-dialog v-model="reviewDialogVisible" title="添加店铺评价" width="500px" :before-close="() => {
            reviewForm = { content: '', rate: 5 };
            reviewDialogVisible = false;
        }">
            <el-form :model="reviewForm" label-width="80px">
                <el-form-item label="评分" prop="rate">
                    <el-rate v-model="reviewForm.rate" show-text text-color="#ff9900" />
                </el-form-item>
                <el-form-item label="评价内容" prop="content">
                    <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请输入您对这家店铺的评价..."
                        maxlength="500" show-word-limit />
                </el-form-item>
            </el-form>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="reviewDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitReview" :loading="submitLoading">
                        提交评价
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.shop-detail-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

/* 新增样式 */
.apply-section {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #eee;
}

.apply-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
}

/* 店铺信息样式 */
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
}

.shop-avatar {
    width: 200px;
    height: 200px;
    flex-shrink: 0;
}

.avatar-image {
    width: 100%;
    height: 100%;
    border-radius: 8px;
}

.shop-meta {
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

.shop-stats {
    margin-top: 16px;
    color: #909399;
}

.stat-item {
    margin-right: 20px;
}

/* 评论区域样式 */
.reviews-section {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.reviews-list {
    space-y: 16px;
}

.review-item {
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    margin-bottom: 16px;
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
    font-weight: 600;
    color: #303133;
}

.review-rating {
    font-size: 14px;
}

.review-date {
    color: #909399;
    font-size: 12px;
}

.review-content {
    color: #606266;
    line-height: 1.6;
}

.empty-reviews {
    text-align: center;
    padding: 40px 0;
}

/* 商品列表样式 */
.section-title {
    font-size: 20px;
    color: #303133;
    margin-bottom: 24px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eee;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 24px;
}

.product-card {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;
}

.product-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image-container {
    width: 100%;
    height: 240px;
    background: #f5f7fa;
}

.product-image {
    width: 100%;
    height: 100%;
}

.image-error,
.image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #909399;
}

.image-error .el-icon,
.image-placeholder .el-icon {
    font-size: 40px;
    margin-bottom: 8px;
}

.product-info {
    padding: 16px;
}

.product-title {
    margin: 0 0 8px 0;
    font-size: 16px;
    color: #303133;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.product-price {
    color: #f56c6c;
    font-size: 18px;
    font-weight: 600;
    margin: 8px 0;
}

.product-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.product-rate {
    flex: 1;
}

/* 对话框样式 */
.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .shop-header {
        flex-direction: column;
    }

    .shop-avatar {
        width: 100%;
        height: auto;
        aspect-ratio: 1/1;
    }

    .product-grid {
        grid-template-columns: 1fr 1fr;
    }

    .section-header {
        flex-direction: column;
        gap: 16px;
        align-items: flex-start;
    }

    .reviewer-info {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }
}

@media (max-width: 480px) {
    .product-grid {
        grid-template-columns: 1fr;
    }
}
</style>