<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getProduct } from '@/api/product.ts';
import { ElMessage } from 'element-plus';
import type { Product } from '@/api/product.ts';

const route = useRoute();
const product = ref<Product | null>(null);
import { Picture } from '@element-plus/icons-vue';

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

    <el-card v-if="product" class="product-card">
        <div class="product-header">
            <h1>{{ product.title }}</h1>
            <div class="meta-info">
                <span class="price">¥{{ product.price }}</span>
                <span class="rate">评分: {{ product.rate }}/10</span>
            </div>
        </div>

        <div class="product-content">
            <div class="image-section">
                <el-image :src="product.cover" :alt="product.title" class="main-image">
                    <template #error>
                        <div class="image-error">
                            <el-icon>
                                <Picture />
                            </el-icon>
                            <span>图片加载失败</span>
                        </div>
                    </template>
                </el-image>
            </div>

            <div class="info-section">
                <h3>商品描述</h3>
                <p>{{ product.description }}</p>

                <h3>商品细节</h3>
                <p>{{ product.detail }}</p>

                <h3>商品规格</h3>
                <div class="specifications">
                    <div v-for="(spec, index) in product.specifications" :key="index" class="spec-item">
                        <span class="spec-name">{{ spec.item }}:</span>
                        <span class="spec-value">{{ spec.value }}</span>
                    </div>
                </div>
            </div>
        </div>
    </el-card>

    <div v-else class="empty-state">
        <el-empty description="商品不存在或已下架" />
    </div>

</template>

<style scoped>
.product-detail-container {
    max-width: 1200px;
    margin: 20px auto;
    padding: 0 20px;
}

.product-card {
    margin-bottom: 20px;
}

.product-header {
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;
}

.meta-info {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-top: 10px;
}

.price {
    font-size: 24px;
    color: #f56c6c;
    font-weight: bold;
}

.rate {
    color: #666;
}

.product-content {
    display: flex;
    gap: 30px;
}

.image-section {
    flex: 0 0 400px;
}

.main-image {
    width: 100%;
    border-radius: 4px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.info-section {
    flex: 1;
}

.info-section h3 {
    margin: 20px 0 10px;
    color: #333;
}

.specifications {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 10px;
    margin-top: 10px;
}

.spec-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background-color: #f5f7fa;
    border-radius: 4px;
}

.spec-name {
    font-weight: bold;
    margin-right: 8px;
}

.empty-state {
    display: flex;
    justify-content: center;
    margin-top: 100px;
}

.image-error .el-icon {
    font-size: 40px;
    margin-bottom: 10px;
}

.image-error {
    text-align: center;
    color: red;
}
</style>