<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { ElMessage } from "element-plus";
import { Product } from "@/api/product.ts";
import { getProductList } from "@/api/product.ts";
import router from "@/router/index.ts";
import { addCartProduct } from "@/api/cart.ts";
import { getAdvertisements, AdvertisementUpdate } from "@/api/advertisements";
import { getAllCoupons, userReceiveCoupon, CouponVO, AccountCouponsRelationVO } from "@/api/coupon";
import { Picture, Ticket, Present, ShoppingBag, Clock } from '@element-plus/icons-vue';

const productList = ref<Product[]>([]);
const advertisementList = ref<AdvertisementUpdate[]>([]);
const couponList = ref<CouponVO[]>([]);
const receivingCoupons = ref<Set<number>>(new Set());

// 获取有效优惠券（新增功能，不影响原有逻辑）
const validCoupons = computed(() => {
    return couponList.value.filter(coupon => {
        const now = new Date();
        const endTime = new Date(coupon.endTime);
        return coupon.isValid === 1 &&
            endTime > now &&
            coupon.usedQuantity < coupon.quantity;
    });
});

// 格式化折扣值显示
const formatDiscountValue = (type: number, value: number) => {
    return type === 1 ? `${value}%` : `¥${value}`;
};

// 计算剩余数量
const getRemainingQuantity = (coupon: CouponVO) => {
    return coupon.quantity - coupon.usedQuantity;
};

// 检查优惠券是否即将过期（7天内）
const isExpiringSoon = (endTime: string) => {
    const now = new Date();
    const end = new Date(endTime);
    const diffTime = end.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 7 && diffDays > 0;
};

// 加载优惠券列表（新增功能）
const loadCoupons = async () => {
    try {
        const response = await getAllCoupons();
        couponList.value = response.data.data || response.data;
    } catch (error) {
        console.error('加载优惠券失败:', error);
    }
};

// 领取优惠券（新增功能）
const receiveCoupon = async (couponId: number) => {
    try {
        receivingCoupons.value.add(couponId);

        const relationData: AccountCouponsRelationVO = {
            accountId: Number(sessionStorage.getItem("id")), // 这里应该使用实际的用户ID
            couponId: couponId,
            quantity: 1
        };

        const response = await userReceiveCoupon(relationData);

        if (response.data.code === '200') {
            ElMessage.success({
                message: '耶！优惠券到手了～',
                duration: 2000
            });
            // 重新加载优惠券列表以更新数量
            await loadCoupons();
        } else {
            ElMessage.error({
                message: response.data.msg || '哎呀，领取失败了',
                duration: 2000
            });
        }
    } catch (error: any) {
        console.error('领取优惠券失败:', error);
        const errorMessage = error?.response?.data?.msg || '网络开小差了，请稍后再试';
        ElMessage.error({
            message: errorMessage,
            duration: 2000
        });
    } finally {
        receivingCoupons.value.delete(couponId);
    }
};

// 原有函数保持不变
const loadAdvertisements = async () => {
    try {
        const response = await getAdvertisements();
        advertisementList.value = response.data.data;
    } catch (error) {
        console.error('加载广告失败:', error);
        // ElMessage.error({ message: '加载广告失败，请稍后重试', duration: 1000 });
    }
};

const loadProducts = async () => {
    try {
        const response = await getProductList();
        productList.value = response.data.data;
    } catch (error) {
        console.error('加载商品列表失败:', error);
        // ElMessage.error({ message: '加载广告失败，请稍后重试', duration: 1000 });
    }
}

const handleAdd = (productId: number) => {
    addCartProduct(productId, 1).then((res) => {
        if (res.data.code === '200') {
            ElMessage.success({ message: '成功加入购物车！', duration: 1000 });
        }
    }).catch((error) => {
        ElMessage.error({ message: '库存不足', duration: 1000 });
    });
}

const pageInit = async () => {
    const res = await getProductList();
    if (res.data.code === '200') {
        productList.value = res.data.data;
    } else if (res.data.code === '401') {
        return;
    }
    await loadProducts();
    await loadAdvertisements();
    await loadCoupons(); // 新增：加载优惠券
};

const gotoDetails = (productId: number) => {
    router.push({ path: `/product/${productId}` });
};

onMounted(pageInit);
</script>

<template>
    <div class="container">
        <!-- 页面标题 -->
        <div class="page-header animate-fade-in">
            <h1 class="page-title">
                <span class="title-char" v-for="(char, index) in '番茄书城'" :key="index"
                    :style="{ animationDelay: `${index * 0.1}s` }">
                    {{ char }}
                </span>
            </h1>
            <p class="page-subtitle animate-slide-up">好书推荐，阅读无限</p>
            <div class="title-decoration">
                <div class="decoration-line"></div>
                <div class="decoration-dot"></div>
                <div class="decoration-line"></div>
            </div>
        </div>

        <!-- 广告轮播 -->
        <el-carousel v-if="advertisementList.length > 0" class="ad-carousel" :interval="3000" height="320px"
            indicator-position="outside" arrow="hover">
            <el-carousel-item v-for="ad in advertisementList" :key="ad.id" @click="gotoDetails(ad.productId)">
                <div class="ad-item">
                    <el-image :src="ad.imgUrl" :alt="ad.title" class="ad-image" fit="cover">
                        <template #error>
                            <div class="image-error">
                                <el-icon size="48" class="pulse-icon">
                                    <Picture />
                                </el-icon>
                                <span>图片走丢了</span>
                            </div>
                        </template>
                    </el-image>
                    <div class="ad-overlay">
                        <div class="ad-content">
                            <h3 class="ad-title">{{ ad.title }}</h3>
                            <p class="ad-description">{{ ad.content }}</p>
                            <div class="ad-action">
                                <span>点击探索更多</span>
                                <el-icon class="bounce-icon">
                                    <ShoppingBag />
                                </el-icon>
                            </div>
                        </div>
                    </div>
                    <div class="shimmer-effect"></div>
                </div>
            </el-carousel-item>
        </el-carousel>

        <!-- 优惠券领取区域 -->
        <div v-if="validCoupons.length > 0" class="coupon-section animate-slide-in">
            <div class="section-header">
                <div class="section-title">
                    <el-icon class="title-icon rotating-icon">
                        <Present />
                    </el-icon>
                    <h2>今日福利</h2>
                    <div class="sparkle-effect">
                        <div class="sparkle"></div>
                        <div class="sparkle"></div>
                        <div class="sparkle"></div>
                    </div>
                </div>
                <div class="section-subtitle">
                    限时优惠券，手慢无～
                </div>
            </div>

            <div class="coupon-container">
                <div class="coupon-scroll">
                    <div v-for="(coupon, index) in validCoupons" :key="coupon.id" class="coupon-card animate-card-in"
                        :style="{ animationDelay: `${index * 0.1}s` }">
                        <div class="coupon-left">
                            <div class="discount-circle">
                                <div class="discount-value pulse-text">
                                    {{ formatDiscountValue(coupon.discountType, coupon.discountValue) }}
                                </div>
                                <div class="discount-label">
                                    {{ coupon.discountType === 1 ? 'OFF' : '减' }}
                                </div>
                            </div>
                            <div class="coupon-glow"></div>
                        </div>

                        <div class="coupon-middle">
                            <div class="coupon-name">{{ coupon.name }}</div>
                            <div class="coupon-desc">{{ coupon.description }}</div>
                            <div class="coupon-time">
                                <el-icon class="tick-icon">
                                    <Clock />
                                </el-icon>
                                <span>{{ new Date(coupon.endTime).toLocaleDateString() }} 截止</span>
                                <el-tag v-if="isExpiringSoon(coupon.endTime)" type="warning" size="small"
                                    class="blink-tag">
                                    即将过期
                                </el-tag>
                            </div>
                        </div>

                        <div class="coupon-right">
                            <div class="remaining-count">
                                还剩 {{ getRemainingQuantity(coupon) }} 张
                            </div>
                            <el-button type="primary" size="small" :loading="receivingCoupons.has(coupon.id!)"
                                @click="receiveCoupon(coupon.id!)" class="receive-btn shake-on-hover">
                                <el-icon>
                                    <Ticket />
                                </el-icon>
                                {{ receivingCoupons.has(coupon.id!) ? '领取中...' : '秒杀' }}
                            </el-button>
                        </div>
                        <div class="coupon-shine"></div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 商品列表 -->
        <div class="product-section animate-fade-in-up">
            <div class="section-header">
                <div class="section-title">
                    <el-icon class="title-icon zoom-icon">
                        <ShoppingBag />
                    </el-icon>
                    <h2>热门推荐</h2>
                    <div class="fire-effect">🔥</div>
                </div>
                <div class="section-subtitle">
                    每一本都值得拥有
                </div>
            </div>

            <el-card class="product-list">
                <div class="product-grid">
                    <div v-for="(product, index) in productList" :key="product.id"
                        class="product-item animate-product-in" :style="{ animationDelay: `${index * 0.05}s` }">
                        <div class="product-image-container" @click="gotoDetails(product.id)">
                            <el-image :src="product.cover" alt="商品图片" class="product-image" fit="cover">
                                <template #error>
                                    <div class="image-error">
                                        <el-icon size="32" class="pulse-icon">
                                            <Picture />
                                        </el-icon>
                                        <span>图片加载中</span>
                                    </div>
                                </template>
                            </el-image>
                            <div class="product-overlay">
                                <span>点击查看</span>
                            </div>
                            <div class="product-shine"></div>
                        </div>
                        <div class="product-info">
                            <div class="product-name" :title="product.title">{{ product.title }}</div>
                            <div class="product-price animate-price">¥{{ product.price }}</div>
                            <el-button type="primary" @click="handleAdd(product.id)"
                                class="add-cart-btn wobble-on-hover" size="default">
                                <el-icon>
                                    <ShoppingBag />
                                </el-icon>
                                加入购物车
                            </el-button>
                        </div>
                    </div>
                </div>
            </el-card>
        </div>
    </div>
</template>

<style scoped>
/* 基础动画关键帧 */
@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateX(-50px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes scaleIn {
    from {
        opacity: 0;
        transform: scale(0.8);
    }

    to {
        opacity: 1;
        transform: scale(1);
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(50px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes cardIn {
    from {
        opacity: 0;
        transform: translateY(30px) scale(0.95);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

@keyframes productIn {
    from {
        opacity: 0;
        transform: translateY(40px) rotateX(45deg);
    }

    to {
        opacity: 1;
        transform: translateY(0) rotateX(0deg);
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

@keyframes pulse {

    0%,
    100% {
        transform: scale(1);
    }

    50% {
        transform: scale(1.05);
    }
}

@keyframes rotate {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}

@keyframes shimmer {
    0% {
        transform: translateX(-100%);
    }

    100% {
        transform: translateX(100%);
    }
}

@keyframes sparkle {

    0%,
    100% {
        opacity: 0;
        transform: scale(0);
    }

    50% {
        opacity: 1;
        transform: scale(1);
    }
}

@keyframes blink {

    0%,
    50% {
        opacity: 1;
    }

    51%,
    100% {
        opacity: 0.3;
    }
}

@keyframes shake {

    0%,
    100% {
        transform: translateX(0);
    }

    25% {
        transform: translateX(-2px);
    }

    75% {
        transform: translateX(2px);
    }
}

@keyframes zoom {

    0%,
    100% {
        transform: scale(1);
    }

    50% {
        transform: scale(1.1);
    }
}

@keyframes wobble {

    0%,
    100% {
        transform: rotate(0deg);
    }

    25% {
        transform: rotate(-5deg);
    }

    75% {
        transform: rotate(5deg);
    }
}

@keyframes glow {

    0%,
    100% {
        box-shadow: 0 0 5px rgba(255, 107, 107, 0.5);
    }

    50% {
        box-shadow: 0 0 20px rgba(255, 107, 107, 0.8);
    }
}

@keyframes priceAnimation {
    0% {
        transform: scale(1);
        color: #e53e3e;
    }

    50% {
        transform: scale(1.1);
        color: #ff6b6b;
    }

    100% {
        transform: scale(1);
        color: #e53e3e;
    }
}

/* 动画类 */
.animate-fade-in {
    animation: fadeIn 0.8s ease-out;
}

.animate-slide-up {
    animation: slideUp 0.8s ease-out 0.2s both;
}

.animate-slide-in {
    animation: slideIn 0.8s ease-out 0.4s both;
}

.animate-scale-in {
    animation: scaleIn 0.8s ease-out 0.6s both;
}

.animate-fade-in-up {
    animation: fadeInUp 0.8s ease-out 0.8s both;
}

.animate-card-in {
    animation: cardIn 0.6s ease-out both;
}

.animate-product-in {
    animation: productIn 0.6s ease-out both;
}

.title-char {
    display: inline-block;
    animation: bounce 0.6s ease-out both;
}

.bounce-icon {
    animation: bounce 2s infinite;
}

.pulse-icon {
    animation: pulse 2s infinite;
}

.pulse-text {
    animation: pulse 2s infinite;
}

.rotating-icon {
    animation: rotate 3s linear infinite;
}

.tick-icon {
    animation: pulse 1.5s infinite;
}

.blink-tag {
    animation: blink 1s infinite;
}

.shake-on-hover:hover {
    animation: shake 0.5s ease-in-out;
}

.zoom-icon {
    animation: zoom 2s infinite;
}

.wobble-on-hover:hover {
    animation: wobble 0.5s ease-in-out;
}

.animate-price {
    animation: priceAnimation 2s infinite;
}

.container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 20px;
    background: #f8f9fa;
    min-height: 100vh;
}

/* 页面头部 */
.page-header {
    text-align: center;
    margin-bottom: 40px;
    padding: 40px 0;
}

.page-title {
    font-size: 48px;
    font-weight: bold;
    color: #2c3e50;
    margin-bottom: 12px;
    font-family: "华文中宋", serif;
    letter-spacing: 2px;
}

.page-subtitle {
    font-size: 18px;
    color: #6c757d;
    margin: 0 0 20px 0;
    font-weight: 300;
}

.title-decoration {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    margin-top: 20px;
}

.decoration-line {
    width: 50px;
    height: 2px;
    background: linear-gradient(90deg, transparent, #ff6b6b, transparent);
}

.decoration-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #ff6b6b;
    animation: pulse 2s infinite;
}

/* 广告轮播美化 */
.ad-carousel {
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
    margin-bottom: 50px;
    position: relative;
}

.ad-item {
    position: relative;
    height: 100%;
    cursor: pointer;
    overflow: hidden;
}

.ad-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
}

.ad-item:hover .ad-image {
    transform: scale(1.05);
}

.ad-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
    color: white;
    padding: 40px;
    transform: translateY(20px);
    transition: transform 0.3s ease;
}

.ad-item:hover .ad-overlay {
    transform: translateY(0);
}

.ad-title {
    font-size: 28px;
    font-weight: bold;
    margin: 0 0 8px 0;
}

.ad-description {
    font-size: 16px;
    margin: 0 0 16px 0;
    opacity: 0.9;
    line-height: 1.5;
}

.ad-action {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 500;
}

.shimmer-effect {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    animation: shimmer 3s infinite;
}

/* 区域标题样式 */
.section-header {
    margin-bottom: 30px;
    text-align: center;
}

.section-title {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-bottom: 8px;
    position: relative;
}

.title-icon {
    font-size: 28px;
    color: #ff6b6b;
}

.section-title h2 {
    font-size: 32px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
}

.section-subtitle {
    font-size: 16px;
    color: #6c757d;
    font-weight: 300;
}

.sparkle-effect {
    position: absolute;
    top: -10px;
    right: -30px;
}

.sparkle {
    position: absolute;
    width: 4px;
    height: 4px;
    background: #ff6b6b;
    border-radius: 50%;
    animation: sparkle 1.5s infinite;
}

.sparkle:nth-child(1) {
    top: 0;
    left: 0;
    animation-delay: 0s;
}

.sparkle:nth-child(2) {
    top: -5px;
    left: 10px;
    animation-delay: 0.5s;
}

.sparkle:nth-child(3) {
    top: 5px;
    left: 15px;
    animation-delay: 1s;
}

.fire-effect {
    font-size: 24px;
    margin-left: 10px;
    animation: bounce 1s infinite;
}

/* 优惠券区域 */
.coupon-section {
    margin-bottom: 50px;
}

.coupon-container {
    background: white;
    border-radius: 20px;
    padding: 24px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.coupon-scroll {
    display: flex;
    gap: 20px;
    overflow-x: auto;
    padding: 8px;
    scrollbar-width: none;
    -ms-overflow-style: none;
}

.coupon-scroll::-webkit-scrollbar {
    display: none;
}

.coupon-card {
    display: flex;
    min-width: 380px;
    height: 120px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    transition: all 0.3s ease;
    border: 2px solid #f0f0f0;
    position: relative;
}

.coupon-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(255, 107, 107, 0.2);
    border-color: #ff6b6b;
    animation: glow 2s infinite;
}

.coupon-left {
    width: 100px;
    background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
}

.coupon-left::after {
    content: '';
    position: absolute;
    right: -6px;
    top: 50%;
    transform: translateY(-50%);
    width: 12px;
    height: 12px;
    background: white;
    border-radius: 50%;
}

.coupon-glow {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    animation: shimmer 2s infinite;
}

.discount-circle {
    text-align: center;
    color: white;
    z-index: 2;
    position: relative;
}

.discount-value {
    font-size: 20px;
    font-weight: bold;
    line-height: 1;
    margin-bottom: 4px;
}

.discount-label {
    font-size: 12px;
    opacity: 0.9;
}

.coupon-middle {
    flex: 1;
    padding: 16px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.coupon-name {
    font-size: 16px;
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 4px;
}

.coupon-desc {
    font-size: 13px;
    color: #718096;
    line-height: 1.4;
    margin-bottom: 8px;
}

.coupon-time {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #a0aec0;
}

.coupon-right {
    width: 100px;
    padding: 16px 12px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    background: #f7fafc;
}

.remaining-count {
    font-size: 12px;
    color: #718096;
    text-align: center;
    margin-bottom: 8px;
}

.receive-btn {
    width: 100%;
    border-radius: 8px;
    font-size: 12px;
    height: 32px;
    background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
    border: none;
    transition: all 0.3s ease;
    font-weight: 600;
}

.receive-btn:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

.coupon-shine {
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
    transform: rotate(45deg);
    animation: shimmer 3s infinite;
}

/* 商品区域 */
.product-section {
    margin-bottom: 40px;
}

.product-list {
    background: white;
    border: none;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border-radius: 20px;
    padding: 24px;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 24px;
}

.product-item {
    display: flex;
    flex-direction: column;
    border-radius: 16px;
    overflow: hidden;
    background: white;
    transition: all 0.3s ease;
    border: 2px solid #f0f0f0;
    position: relative;
}

.product-item:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.15);
    border-color: #4ecdc4;
}

.product-image-container {
    position: relative;
    aspect-ratio: 3/4;
    overflow: hidden;
    cursor: pointer;
}

.product-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.product-item:hover .product-image {
    transform: scale(1.1);
}

.product-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(78, 205, 196, 0.85);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 500;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.product-item:hover .product-overlay {
    opacity: 1;
}

.product-shine {
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transform: rotate(45deg);
    opacity: 0;
    transition: opacity 0.3s ease;
}

.product-item:hover .product-shine {
    opacity: 1;
    animation: shimmer 1s ease-out;
}

.product-info {
    padding: 20px;
    text-align: center;
    background: #f7fafc;
}

.product-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 8px;
    color: #2d3748;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    line-height: 1.4;
}

.product-price {
    font-size: 20px;
    color: #e53e3e;
    font-weight: bold;
    margin-bottom: 16px;
}

.add-cart-btn {
    width: 100%;
    border-radius: 10px;
    height: 40px;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    font-weight: 500;
    transition: all 0.3s ease;
}

.add-cart-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(78, 205, 196, 0.4);
}

/* 错误图片样式 */
.image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #a0aec0;
    background: #f7fafc;
    gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .container {
        padding: 16px;
    }

    .page-title {
        font-size: 36px;
    }

    .ad-carousel {
        height: 240px;
    }

    .coupon-scroll {
        gap: 16px;
    }

    .coupon-card {
        min-width: 320px;
        height: 100px;
    }

    .product-grid {
        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
        gap: 16px;
    }

    .section-title h2 {
        font-size: 24px;
    }
}

@media (max-width: 480px) {
    .coupon-card {
        min-width: 280px;
        height: 90px;
    }

    .coupon-left {
        width: 80px;
    }

    .discount-value {
        font-size: 16px;
    }

    .product-grid {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    }
}

/* 全局优化 */
* {
    transition: all 0.3s ease;
}

/* 滚动条美化 */
::-webkit-scrollbar {
    width: 8px;
}

::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
}

::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}
</style>