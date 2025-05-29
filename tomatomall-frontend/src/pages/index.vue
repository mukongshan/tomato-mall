<script setup lang="ts">
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { Product } from "@/api/product.ts";
import { getProductsList } from "@/api/product.ts";
import router from "@/router/index.ts";
import { addCartProduct } from "@/api/cart.ts";
import { getAdvertisements, AdvertisementUpdate } from "@/api/advertisements";
import { Picture } from '@element-plus/icons-vue';

const products = ref<Product[]>([]);
const advertisementList = ref<AdvertisementUpdate[]>([]);

const loadAdvertisements = async () => {
    try {
        const response = await getAdvertisements();
        advertisementList.value = response.data.data;
        ElMessage.success({ message: '广告列表加载成功', duration: 1000 });
    } catch (error) {
        console.error('加载广告失败:', error);
        ElMessage.error({ message: '加载广告失败，请稍后重试', duration: 1000 });
    }
};

const handleAdd = (productId: number) => {
    addCartProduct(productId, 1).then((res) => {
        if (res.data.code === '200') {
            ElMessage.success('添加成功');
        }
    }).catch((error) => {});
};

const pageInit = async () => {
    const res = await getProductsList();
    if (res.data.code === '200') {
        products.value = res.data.data;
    } else {
        ElMessage({ message: res.data.msg, type: 'error', center: true });
    }
    await loadAdvertisements();
};

const gotoDetails = (productId: number) => {
    router.push({ path: `/product/${productId}` });
};

pageInit();
</script>

<template>
    <h1 class="page-title">番茄书城</h1>

    <el-carousel v-if="advertisementList.length > 0" class="ad-carousel" :interval="3000" height="300px" indicator-position="outside">
        <el-carousel-item v-for="ad in advertisementList" :key="ad.id" @click="gotoDetails(ad.productId)">
            <div class="ad-item">
                <el-image :src="ad.imgUrl" :alt="ad.title" class="ad-image">
                    <template #error>
                        <div class="image-error">
                            <el-icon><Picture /></el-icon>
                            <span>图片加载失败</span>
                        </div>
                    </template>
                </el-image>
                <div class="ad-title">{{ ad.title }}</div>
            </div>
        </el-carousel-item>
    </el-carousel>

    <el-card class="product-list">
        <div class="product-grid">
            <div v-for="product in products" :key="product.id" class="product-item">
                <div class="product-image-container" @click="gotoDetails(product.id)">
                    <el-image :src="product.cover" alt="商品图片" class="product-image">
                        <template #error>
                            <div class="image-error">
                                <el-icon><Picture /></el-icon>
                                <span>图片加载失败</span>
                            </div>
                        </template>
                    </el-image>
                </div>
                <div class="product-info">
                    <div class="product-name">{{ product.title }}</div>
                    <div class="product-price">¥{{ product.price }}</div>
                    <el-button @click="handleAdd(product.id)">加入购物车</el-button>
                </div>
            </div>
        </div>
    </el-card>
</template>

<style scoped>
/* 页面标题 */
.page-title {
    text-align: center;
    font-size: 36px;
    font-weight: bold;
    color: #d62828;
    margin: 30px 0 20px;
    font-family: "华文中宋", serif;
}

/* 广告轮播 */
.ad-carousel {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    margin: 0 auto 30px;
    max-width: 960px;
}

.ad-item {
    position: relative;
    cursor: pointer;
    height: 100%;
}

.ad-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.ad-title {
    position: absolute;
    bottom: 0;
    width: 100%;
    padding: 12px;
    background: rgba(0, 0, 0, 0.5);
    color: white;
    font-weight: bold;
    text-align: center;
    font-size: 18px;
}

/* 商品区域 */
.product-list {
    margin-top: 20px;
    background: #fff;
    border: none;
    box-shadow: none;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
}

.product-item {
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.product-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.product-image-container {
    width: 100%;
    aspect-ratio: 1/1;
    overflow: hidden;
}

.product-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
}

.product-item:hover .product-image {
    transform: scale(1.05);
}

.product-info {
    padding: 16px;
    text-align: center;
}

.product-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.product-price {
    font-size: 16px;
    font-weight: bold;
    color: #d62828;
    margin-bottom: 8px;
}

.el-button {
    background-color: #d62828;
    border: none;
    color: white;
    width: 100%;
}

.el-button:hover {
    background-color: #b71c1c;
}

/* 图片加载失败 */
.image-error {
    text-align: center;
    color: red;
}

.image-error .el-icon {
    font-size: 40px;
    margin-bottom: 10px;
}

/* 响应式优化 */
@media (max-width: 600px) {
    .page-title {
        font-size: 24px;
    }

    .product-grid {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    }

    .ad-carousel {
        height: 200px;
    }
}
</style>
