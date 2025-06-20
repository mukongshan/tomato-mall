<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Shop, getShopList } from '@/api/shop';
import router from '@/router';
import { Picture } from '@element-plus/icons-vue';

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
        <h2>所有店铺</h2>

        <!-- 加载状态 -->
        <el-skeleton v-if="loading" :rows="6" animated />

        <!-- 空状态 -->
        <el-empty v-else-if="approvedShops.length === 0" description="暂无店铺" />

        <!-- 店铺列表 -->
        <div v-else class="shop-list">
            <div v-for="shop in approvedShops" :key="shop.shopId" class="shop-card" @click="gotoDetails(shop.shopId)">
                <div class="shop-image-container">
                    <el-image :src="shop.iconUrl" :alt="shop.name" fit="cover" class="shop-image">
                        <template #error>
                            <div class="image-error">
                                <el-icon>
                                    <Picture />
                                </el-icon>
                                <span>图片加载失败</span>
                            </div>
                        </template>
                    </el-image>
                    <div v-if="!shop.iconUrl" class="image-placeholder">
                        <el-icon>
                            <Picture />
                        </el-icon>
                        <span>暂无图片</span>
                    </div>
                </div>

                <div class="shop-info">
                    <h3 class="shop-title">{{ shop.name }}</h3>
                    <p class="shop-description">{{ shop.description }}</p>
                    <div class="shop-meta">
                        <el-rate v-model="shop.rate" disabled text-color="#ff9900" class="shop-rate" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.shops-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

h2 {
    text-align: center;
    margin-bottom: 30px;
    color: #333;
}

.shop-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.shop-card {
    cursor: pointer;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    overflow: hidden;
}

.shop-card:hover {
    transform: translateY(-5px);
}

.shop-image-container {
    position: relative;
    width: 100%;
    height: 200px;
    background-color: #f5f7fa;
}

.shop-image {
    width: 100%;
    height: 100%;
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
    background-color: #f5f7fa;
    color: #909399;
}

.image-error .el-icon,
.image-placeholder .el-icon {
    font-size: 40px;
    margin-bottom: 10px;
}

.shop-info {
    padding: 15px;
}

.shop-title {
    margin: 0 0 10px 0;
    font-size: 16px;
    color: #303133;
}

.shop-description {
    font-size: 14px;
    color: #606266;
    margin: 0 0 10px 0;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.shop-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.shop-rate {
    flex: 1;
}

.shop-owner {
    font-size: 12px;
    color: #909399;
    margin-left: 10px;
}
</style>