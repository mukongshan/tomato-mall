<script setup lang="ts">
import { ref, onMounted } from "vue";
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
    } catch (error) {
        console.error('加载广告失败:', error);
        ElMessage.error({ message: '加载广告失败，请稍后重试', duration: 1000 });
    }
};

const handleAdd = (productId: number) => {
    addCartProduct(productId, 1).then((res) => {
        if (res.data.code === '200') {
            ElMessage.success({ message: '添加成功', duration: 1000 });
        }
    }).catch((error) => { });
}

const pageInit = async () => {
    const res = await getProductsList();
    if (res.data.code === '200') {
        products.value = res.data.data;
    } else if (res.data.code === '401') {
        return;
    }
    await loadAdvertisements();
};

const gotoDetails = (productId: number) => {
    router.push({ path: `/product/${productId}` });
};

onMounted(pageInit);
</script>

<template>
    <div class="container">
        <h1 class="page-title">番茄书城</h1>

        <el-carousel v-if="advertisementList.length > 0" class="ad-carousel" :interval="3000" height="300px"
            indicator-position="outside">
            <el-carousel-item v-for="ad in advertisementList" :key="ad.id" @click="gotoDetails(ad.productId)">
                <div class="ad-item">
                    <el-image :src="ad.imgUrl" :alt="ad.title" class="ad-image">
                        <template #error>
                            <div class="image-error">
                                <el-icon>
                                    <Picture />
                                </el-icon>
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
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
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
    </div>
</template>

<style scoped>
.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.page-title {
    text-align: center;
    font-size: 40px;
    font-weight: bold;
    color: black;
    margin-bottom: 30px;
    font-family: "华文中宋", serif;
}

.ad-carousel {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    margin-bottom: 40px;
}

.ad-item {
    position: relative;
    height: 100%;
    cursor: pointer;
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
    padding: 14px;
    background: rgba(0, 0, 0, 0.4);
    color: #fff;
    font-weight: bold;
    text-align: center;
    font-size: 20px;
}

.section-title {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 20px;
    color: #333;
    text-align: left;
}

.product-list {
    background: #fff;
    border: none;
    box-shadow: none;
    padding: 10px;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
}

.product-item {
    display: flex;
    flex-direction: column;
    border-radius: 16px;
    overflow: hidden;
    background: #f9f9f9;
    transition: transform 0.3s, box-shadow 0.3s;
}

.product-item:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.product-image-container {
    aspect-ratio: 1/1;
    overflow: hidden;
}

.product-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
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
    margin-bottom: 6px;
    color: #333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.product-price {
    font-size: 18px;
    color: #d62828;
    font-weight: bold;
    margin-bottom: 10px;
}

.image-error {
    text-align: center;
    color: red;
    padding: 20px;
}

.image-error .el-icon {
    font-size: 32px;
    margin-bottom: 8px;
}

@media (max-width: 768px) {
    .page-title {
        font-size: 28px;
    }

    .ad-carousel {
        height: 200px;
    }

    .product-grid {
        grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    }
}
</style>