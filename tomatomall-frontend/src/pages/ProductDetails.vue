<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getProduct } from '@/api/product.ts';
import { ElMessage } from 'element-plus';
import type { Product } from '@/api/product.ts';
import { Picture } from '@element-plus/icons-vue';

const route = useRoute();
const product = ref<Product | null>(null);

onMounted(async () => {
    try {
        const productId = Number(route.params.id);
        const res = await getProduct(productId);
        if (res.data.code === '200') {
            product.value = res.data.data;
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
        <el-card v-if="product" class="product-card" shadow="hover">
            <div class="product-header">
                <h1>{{ product.title }}</h1>
                <div class="meta-info">
                    <span class="price">¥{{ product.price }}</span>
                    <span class="rate">评分：{{ product.rate }}/10</span>
                </div>
            </div>

            <div class="product-content">
                <div class="image-section">
                    <el-image :src="product.cover" :alt="product.title" class="main-image" fit="cover">
                        <template #error>
                            <div class="image-error">
                                <el-icon><Picture /></el-icon>
                                <span>图片加载失败</span>
                            </div>
                        </template>
                    </el-image>
                </div>

                <div class="info-section">
                    <h3>商品描述</h3>
                    <p class="text">{{ product.description }}</p>

                    <h3>商品细节</h3>
                    <p class="text">{{ product.detail }}</p>

                    <h3>商品规格</h3>
                    <div class="specifications">
                        <div v-for="(spec, index) in product.specifications" :key="index" class="spec-item">
                            <span class="spec-name">{{ spec.item }}：</span>
                            <span class="spec-value">{{ spec.value }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </el-card>

        <div v-else class="empty-state">
            <el-empty description="商品不存在或已下架" />
        </div>
    </div>
</template>

<style scoped>
.product-detail-container {
    max-width: 1200px;
    margin: 30px auto;
    padding: 0 20px;
    box-sizing: border-box;
}

.product-card {
    border-radius: 16px;
    overflow: hidden;
    padding: 30px;
}

.product-header h1 {
    font-size: 26px;
    font-weight: bold;
    color: #2c3e50;
}

.meta-info {
    margin-top: 15px;
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
}

.price {
    font-size: 24px;
    font-weight: bold;
    color: #e63946;
}

.rate {
    font-size: 16px;
    color: #666;
}

.product-content {
    display: flex;
    flex-wrap: wrap;
    gap: 40px;
    margin-top: 30px;
}

.image-section {
    flex: 1 1 400px;
    max-width: 480px;
}

.main-image {
    width: 100%;
    height: 100%;
    max-height: 480px;
    object-fit: cover;
    border-radius: 8px;
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.1);
}

.image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #999;
    font-size: 14px;
    padding: 20px;
}

.image-error .el-icon {
    font-size: 48px;
    margin-bottom: 8px;
}

.info-section {
    flex: 1;
    min-width: 280px;
}

.info-section h3 {
    margin: 20px 0 10px;
    font-size: 18px;
    font-weight: 600;
    color: #34495e;
}

.text {
    font-size: 15px;
    line-height: 1.8;
    color: #555;
}

.specifications {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 10px;
    margin-top: 10px;
}

.spec-item {
    background-color: #f9f9f9;
    padding: 10px 14px;
    border-radius: 8px;
    font-size: 14px;
    color: #333;
    display: flex;
    justify-content: space-between;
}

.spec-name {
    font-weight: 600;
}

.empty-state {
    display: flex;
    justify-content: center;
    margin-top: 100px;
}
</style>
