<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElSkeleton } from 'element-plus';
import { Picture } from '@element-plus/icons-vue';
import { Shop, getShopDetail } from '@/api/shop';
import { Product, getProductsByShopId } from '@/api/product';
import router from '@/router';
import { isCustomer } from '@/components/LoginEvent';

const route = useRoute();
const shopId = ref<number>(Number(route.params.shopId));
const shopInfo = ref<Shop | null>(null);
const products = ref<Product[]>([]);
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

// 跳转到商品详情
const gotoProductDetail = (productId: number) => {
    router.push(`/product/detail/${productId}`);
};
onMounted(async () => {
    await Promise.all([fetchShopDetail(), fetchShopProducts()]);
});

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
    </div>
</template>

<style scoped>
.shop-detail-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
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
}

@media (max-width: 480px) {
    .product-grid {
        grid-template-columns: 1fr;
    }
}
</style>