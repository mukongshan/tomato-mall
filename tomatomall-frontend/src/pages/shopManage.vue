<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Shop, getShopList, updateShop, deleteShop } from '@/api/shop';
import router from '@/router';

import { Picture } from '@element-plus/icons-vue';
import { getUserRoleById, updateUserRole } from '@/api/account';
import { checkRole } from '@/components/LoginEvent';
import { Message, sendMessage } from '@/api/message';


// 数据状态
const pendingShops = ref<Shop[]>([]);  // 待审核店铺列表
const approvedShops = ref<Shop[]>([]); // 已通过店铺列表
const loading = ref(false);            // 加载状态

/**
 * 获取店铺列表数据
 */
const fetchShops = async () => {
    try {
        loading.value = true;
        const response = await getShopList();
        const shops = (response.data.data || []) as Shop[];

        // 根据isValid状态筛选店铺
        pendingShops.value = shops.filter(shop => shop.isValid === 0);
        approvedShops.value = shops.filter(shop => shop.isValid === 1);
    } catch (error) {
        ElMessage.error('获取店铺列表失败');
        console.error(error);
    } finally {
        loading.value = false;
    }
};

/**
 * 处理店铺通过审核
 * @param shop 店铺数据对象
 */
const handleApprove = async (shop: Shop) => {
    try {
        // 确认弹窗
        await ElMessageBox.confirm('确定要通过该店铺的申请吗？', '操作确认', {
            type: 'warning'
        });

        // 更新店铺状态为已通过(1)
        const updatedShop = { ...shop, isValid: 1 };
        // 把用户身份更新为店主
        const userRole = await getUserRoleById(shop.ownerId);
        console.log('User Role:', userRole.data.data);
        if (userRole.data.data == "CUSTOMER") {
            await updateUserRole(shop.ownerId, 'SHOPKEEPER');
        }
        const message: Message = {
            id: 0,
            content: "APPLICATION_APPROVED",
            isRead: false,
            fromUser: Number(sessionStorage.getItem('id')),
            toUser: shop.ownerId,
            createdTime: new Date().toISOString()
        };
        await sendMessage(message);
        // 更新店铺信息
        await updateShop(updatedShop);
        ElMessage.success('店铺已成功通过审核');
        fetchShops(); // 刷新列表
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('店铺审核操作失败');
        }
    }
};

/**
 * 处理店铺拒绝申请
 * @param shopId 店铺ID
 */
const handleReject = async (shopId: number) => {
    try {
        // 确认弹窗
        await ElMessageBox.confirm('确定要拒绝该店铺的申请吗？', '操作确认', {
            type: 'warning'
        });
        const message: Message = {
            id: 0,
            content: "APPLICATION_REJECTED",
            isRead: false,
            fromUser: Number(sessionStorage.getItem('id')),
            toUser: shopId,
            createdTime: new Date().toISOString()
        };
        await sendMessage(message);
        // 删除店铺记录
        await deleteShop(shopId);
        ElMessage.success('店铺已成功拒绝');
        fetchShops(); // 刷新列表
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('店铺拒绝操作失败');
        }
    }
};

/**
 * 跳转到店铺详情页
 * @param shopId 店铺ID
 */
const navigateToDetail = (shopId: number) => {
    router.push(`/shop/detail/${shopId}`);
};

// 组件挂载时获取数据
onMounted(() => {
    fetchShops();
});
</script>

<template>
    <div class="shop-management-container">
        <h2>店铺管理</h2>

        <!-- 待审核店铺区域 -->
        <div class="section">
            <h3>待审核店铺</h3>
            <el-empty v-if="pendingShops.length === 0 && !loading" description="暂无待审核店铺" />
            <div v-else class="shop-list">
                <el-card v-for="shop in pendingShops" :key="shop.shopId" class="shop-card">
                    <div class="shop-content" @click="navigateToDetail(shop.shopId)">
                        <!-- 店铺图片 -->
                        <el-image :src="shop.iconUrl" v-if="shop.iconUrl" fit="cover" class="shop-image">
                            <template #error>
                                <div class="image-error">
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
                                    <span>图片加载失败</span>
                                </div>
                            </template>
                        </el-image>
                        <div class="shop-info">
                            <h4>{{ shop.name }}</h4>
                            <p class="description">{{ shop.description }}</p>
                            <p class="owner">店主ID: {{ shop.ownerId }}</p>
                        </div>
                    </div>
                    <!-- 操作按钮 -->
                    <div class="action-buttons">
                        <el-button type="success" @click.stop="handleApprove(shop)">通过</el-button>
                        <el-button type="danger" @click.stop="handleReject(shop.shopId)">拒绝</el-button>
                    </div>
                </el-card>
            </div>
        </div>

        <!-- 已通过店铺区域 -->
        <div class="section">
            <h3>已通过店铺</h3>
            <el-empty v-if="approvedShops.length === 0 && !loading" description="暂无已通过店铺" />
            <div v-else class="shop-list">
                <el-card v-for="shop in approvedShops" :key="shop.shopId" class="shop-card approved"
                    @click="navigateToDetail(shop.shopId)">
                    <div class="shop-content">
                        <!-- 店铺图片 -->
                        <el-image :src="shop.iconUrl" v-if="shop.iconUrl" fit="cover" class="shop-image">
                            <template #error>
                                <div class="image-error">
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
                                    <span>图片加载失败</span>
                                </div>
                            </template>
                        </el-image>
                        <div class="shop-info">
                            <h4>{{ shop.name }}</h4>
                            <p class="description">{{ shop.description }}</p>
                            <div class="meta">
                                <span>评分: {{ shop.rate || '暂无评分' }}</span>
                                <span>店主ID: {{ shop.ownerId }}</span>
                            </div>
                        </div>
                    </div>
                </el-card>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 店铺管理容器 */
.shop-management-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

/* 标题样式 */
h2 {
    margin-bottom: 30px;
    text-align: center;
}

/* 分区标题样式 */
h3 {
    margin: 20px 0;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;
}

/* 分区样式 */
.section {
    margin-bottom: 40px;
}

/* 店铺列表网格布局 */
.shop-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 20px;
}

/* 店铺卡片基础样式 */
.shop-card {
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;
}

/* 卡片悬停效果 */
.shop-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

/* 已通过店铺特殊标识 */
.shop-card.approved {
    border-left: 4px solid #67c23a;
}

/* 店铺内容区域布局 */
.shop-content {
    display: flex;
    gap: 15px;
}

/* 店铺图片样式 */
.shop-image {
    width: 100px;
    height: 100px;
    border-radius: 4px;
}

/* 店铺信息区域 */
.shop-info {
    flex: 1;
}

/* 店铺名称 */
.shop-info h4 {
    margin: 0 0 8px 0;
    font-size: 16px;
}

/* 店铺描述 */
.description {
    color: #666;
    font-size: 14px;
    margin: 8px 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

/* 元信息布局 */
.meta {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #888;
}

/* 店主ID样式 */
.owner {
    font-size: 12px;
    color: #888;
    margin: 8px 0 0 0;
}

/* 操作按钮区域 */
.action-buttons {
    display: flex;
    justify-content: space-between;
    margin-top: 15px;
}

/* 空状态提示 */
.el-empty {
    margin: 20px 0;
}
</style>