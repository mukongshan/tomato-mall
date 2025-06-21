<script setup lang="ts">
import { getOrder, getOrderItems, Order, OrderItem, payOrder, cancelOrder } from "@/api/order.ts";
import { ref, nextTick, onMounted } from "vue";
import { ElCard, ElTag, ElDivider, ElButton, ElMessage, ElMessageBox } from "element-plus";
import { ArrowRight, Timer, Money, ShoppingCart, Close, Box, Calendar, Check, Warning } from '@element-plus/icons-vue';
import { Product, getProduct } from "@/api/product.ts";

const orders = ref<Order[]>([]);
const orderItems = ref<{ order: Order; items: OrderItem[] }[]>([]);
const productDetails = ref<Record<number, Product>>({}); // 存储商品详情
const isPageLoaded = ref(false);
const loading = ref(true);

const loadOrders = async () => {
    try {
        const response = await getOrder(Number(sessionStorage.getItem("id")));
        orders.value = response.data.data;
    } catch (error) {
        console.error('加载失败:', error);
    }
}

const loadOrderItems = async () => {
    try {
        for (const order of orders.value) {
            const response = await getOrderItems(order.orderId);
            orderItems.value.push({
                order: order,
                items: response.data.data,
            });
        }

    } catch (error) {
        console.error('加载失败:', error);
    }
}

const loadProductDetails = async () => {
    try {
        const productIds = new Set<number>();
        orderItems.value.forEach(orderItem => {
            orderItem.items.forEach(item => {
                if (!productDetails.value[item.productId]) {
                    productIds.add(item.productId);
                }
            });
        });

        // 并行加载所有商品详情
        const productPromises = Array.from(productIds).map(productId =>
            getProduct(productId).then(res => {
                productDetails.value[productId] = res.data.data;
            })
        );

        await Promise.all(productPromises);
    } catch (error) {
        console.error('加载商品详情失败:', error);
    }
}

const pageInit = async () => {
    loading.value = true;
    await loadOrders();
    await loadOrderItems();
    await loadProductDetails();
    loading.value = false;

    // 延迟设置页面加载完成
    await nextTick();
    setTimeout(() => {
        isPageLoaded.value = true;
    }, 500);
};

// 格式化日期
const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
}

// 格式化订单状态
const formatStatus = (status: string) => {
    const statusMap: Record<string, string> = {
        'PENDING': '待支付',
        'SUCCESS': '已支付',
        'TIMEOUT': '超时',
        'FAILED': '已取消'
    };
    return statusMap[status] || status;
}

// 状态标签类型
const getStatusTagType = (status: string): "success" | "warning" | "info" | "danger" => {
    const typeMap: Record<string, "success" | "warning" | "info" | "danger"> = {
        'PENDING': 'warning',
        'SUCCESS': 'success',
        'TIMEOUT': 'info',
        'FAILED': 'danger'
    };
    return typeMap[status] || 'info';
}

// 获取状态图标
const getStatusIcon = (status: string) => {
    const iconMap: Record<string, any> = {
        'PENDING': Timer,
        'SUCCESS': Check,
        'TIMEOUT': Warning,
        'FAILED': Close
    };
    return iconMap[status] || Timer;
}

// 格式化支付方式
const formatPaymentMethod = (method: string) => {
    const methodMap: Record<string, string> = {
        'ALIPAY': '支付宝'
    };
    return methodMap[method] || method;
}

// 计算订单统计
const orderStats = ref({
    total: 0,
    pending: 0,
    success: 0,
    failed: 0
});

const calculateStats = () => {
    orderStats.value.total = orderItems.value.length;
    orderStats.value.pending = orderItems.value.filter(item => item.order.status === 'PENDING').length;
    orderStats.value.success = orderItems.value.filter(item => item.order.status === 'SUCCESS').length;
    orderStats.value.failed = orderItems.value.filter(item => ['FAILED', 'TIMEOUT'].includes(item.order.status)).length;
};

const handlePay = async (orderId: number) => {
    try {
        const res = await payOrder(orderId);
        if (res.data.code === '200') {
            submitAlipayForm(res.data.data.paymentForm);
            ElMessage.success('正在跳转支付页面...');
        } else {
            ElMessage({
                message: res.data.msg || '支付请求失败',
                type: 'error',
                center: true,
            });
        }
    } catch (error) {
        ElMessage.error('支付处理失败');
        console.error('失败', error);
    }
}

// 取消订单
const handleCancelOrder = async (orderId: number) => {
    try {
        await ElMessageBox.confirm('确定要取消该订单吗？取消后将无法恢复。', '取消订单', {
            confirmButtonText: '确定取消',
            cancelButtonText: '我再想想',
            type: 'warning',
            center: true,
        });

        console.log('正在取消订单:', orderId);
        const res = await cancelOrder(orderId);

        if (res.data.code === '200') {
            // 更新本地订单状态
            const orderIndex = orderItems.value.findIndex(item => item.order.orderId === orderId);
            if (orderIndex !== -1) {
                orderItems.value[orderIndex].order.status = 'FAILED';
            }
            calculateStats(); // 重新计算统计
            ElMessage.success('订单已成功取消');
        } else {
            ElMessage.error(res.data.msg || '取消订单失败，请稍后重试');
        }

    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('取消订单失败，请检查网络连接');
            console.error('取消订单失败:', error);
        }
        // 如果是用户点击了"我再想想"，error 为 'cancel'，不显示错误信息
    }
}

const submitAlipayForm = (formHtml: string) => {
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = formHtml;
    const form = tempDiv.querySelector('form[name="punchout_form"]');

    if (!form) throw new Error('支付宝表单解析失败');

    const newForm = document.createElement('form');
    newForm.method = 'post';
    newForm.action = (form as HTMLFormElement).action;
    newForm.style.display = 'none';

    const inputs = form.querySelectorAll('input');
    inputs.forEach(input => {
        const newInput = document.createElement('input');
        newInput.type = 'hidden';
        newInput.name = input.name;
        newInput.value = input.value;
        newForm.appendChild(newInput);
    });

    document.body.appendChild(newForm);
    newForm.submit();
    document.body.removeChild(newForm);
};

onMounted(async () => {
    await pageInit();
    calculateStats();
});
</script>

<template>
    <div class="page-container">
        <!-- 页面加载动画 -->
        <div v-if="loading && !isPageLoaded" class="page-loading">
            <div class="loading-content">
                <div class="loading-icon">
                    <el-icon size="60" class="rotate-icon">
                        <ShoppingCart />
                    </el-icon>
                </div>
                <div class="loading-text">
                    <span class="loading-char" v-for="(char, index) in '加载订单信息'" :key="index"
                        :style="{ animationDelay: `${index * 0.1}s` }">
                        {{ char }}
                    </span>
                </div>
            </div>
        </div>

        <!-- 主要内容 -->
        <div v-else class="order-container animate-page-enter">
            <!-- 面包屑导航 -->
            <div class="breadcrumb animate-slide-down">
                <span class="breadcrumb-item">首页</span>
                <span class="breadcrumb-separator">/</span>
                <span class="breadcrumb-item">个人中心</span>
                <span class="breadcrumb-separator">/</span>
                <span class="breadcrumb-current">我的订单</span>
            </div>

            <!-- 页面头部 -->
            <div class="page-header animate-slide-down" style="--delay: 0.2s">
                <div class="header-decoration">
                    <div class="decoration-line left-line"></div>
                    <div class="header-icon animate-bounce-in">
                        <el-icon size="32">
                            <ShoppingCart />
                        </el-icon>
                    </div>
                    <div class="decoration-line right-line"></div>
                </div>
                <h1 class="page-title animate-title-reveal">
                    <span v-for="(char, index) in '我的订单'" :key="index" class="title-char"
                        :style="{ animationDelay: `${index * 0.1}s` }">
                        {{ char }}
                    </span>
                </h1>
                <p class="page-subtitle animate-fade-in-up">管理您的购买订单和支付状态</p>
            </div>

            <!-- 统计信息 -->
            <div class="stats-overview animate-slide-up-stagger">
                <div class="stat-item animate-stat-appear" style="--delay: 0.4s">
                    <div class="stat-icon total">
                        <el-icon>
                            <Box />
                        </el-icon>
                    </div>
                    <div class="stat-info">
                        <div class="stat-number">{{ orderStats.total }}</div>
                        <div class="stat-label">全部订单</div>
                    </div>
                </div>
                <div class="stat-item animate-stat-appear" style="--delay: 0.5s">
                    <div class="stat-icon pending">
                        <el-icon>
                            <Timer />
                        </el-icon>
                    </div>
                    <div class="stat-info">
                        <div class="stat-number">{{ orderStats.pending }}</div>
                        <div class="stat-label">待支付</div>
                    </div>
                </div>
                <div class="stat-item animate-stat-appear" style="--delay: 0.6s">
                    <div class="stat-icon success">
                        <el-icon>
                            <Check />
                        </el-icon>
                    </div>
                    <div class="stat-info">
                        <div class="stat-number">{{ orderStats.success }}</div>
                        <div class="stat-label">已支付</div>
                    </div>
                </div>
                <div class="stat-item animate-stat-appear" style="--delay: 0.7s">
                    <div class="stat-icon failed">
                        <el-icon>
                            <Close />
                        </el-icon>
                    </div>
                    <div class="stat-info">
                        <div class="stat-number">{{ orderStats.failed }}</div>
                        <div class="stat-label">已取消</div>
                    </div>
                </div>
            </div>

            <!-- 订单列表 -->
            <div class="order-list">
                <el-card class="order-card animate-card-appear" v-for="(orderData, index) in orderItems"
                    :key="orderData.order.orderId" shadow="hover" :style="{ animationDelay: `${index * 0.1 + 0.8}s` }">
                    <template #header>
                        <div class="order-header">
                            <div class="order-left">
                                <div class="order-number">
                                    <el-icon class="order-icon">
                                        <Box />
                                    </el-icon>
                                    <span class="label">订单编号:</span>
                                    <span class="value">{{ orderData.order.orderId }}</span>
                                </div>
                            </div>
                            <div class="order-right">
                                <el-tag :type="getStatusTagType(orderData.order.status)" size="large" effect="dark"
                                    class="status-tag">
                                    <el-icon class="status-icon">
                                        <component :is="getStatusIcon(orderData.order.status)" />
                                    </el-icon>
                                    {{ formatStatus(orderData.order.status) }}
                                </el-tag>
                            </div>
                        </div>
                    </template>

                    <div class="order-content">
                        <div class="order-meta">
                            <div class="meta-item animate-meta-appear" style="--delay: 0.1s">
                                <div class="meta-icon">
                                    <el-icon>
                                        <Calendar />
                                    </el-icon>
                                </div>
                                <div class="meta-content">
                                    <span class="meta-label">创建时间</span>
                                    <span class="meta-value">{{ formatDate(orderData.order.createTime) }}</span>
                                </div>
                            </div>
                            <div class="meta-item animate-meta-appear" style="--delay: 0.2s">
                                <div class="meta-icon">
                                    <el-icon>
                                        <Money />
                                    </el-icon>
                                </div>
                                <div class="meta-content">
                                    <span class="meta-label">支付方式</span>
                                    <span
                                        class="meta-value">{{ formatPaymentMethod(orderData.order.paymentMethod) }}</span>
                                </div>
                            </div>
                            <div class="meta-item animate-meta-appear" style="--delay: 0.3s">
                                <div class="meta-icon total-amount">
                                    <el-icon>
                                        <Money />
                                    </el-icon>
                                </div>
                                <div class="meta-content">
                                    <span class="meta-label">订单总额</span>
                                    <span class="total-amount">¥{{ orderData.order.totalAmount.toFixed(2) }}</span>
                                </div>
                            </div>
                        </div>

                        <el-divider border-style="dashed" class="section-divider" />

                        <div class="product-section">
                            <div class="section-header">
                                <div class="section-title">
                                    <el-icon class="section-icon">
                                        <ShoppingCart />
                                    </el-icon>
                                    <h3>商品清单</h3>
                                    <div class="item-count">{{ orderData.items.length }} 件商品</div>
                                </div>
                            </div>

                            <div class="products-container">
                                <div v-for="(item, itemIndex) in orderData.items" :key="item.productId"
                                    class="product-item animate-product-appear"
                                    :style="{ animationDelay: `${itemIndex * 0.1}s` }">
                                    <div class="product-index">{{ itemIndex + 1 }}</div>
                                    <div class="product-image-container">
                                        <el-image
                                            :src="productDetails[item.productId]?.cover || 'https://via.placeholder.com/80'"
                                            fit="cover" class="product-image" lazy>
                                            <template #error>
                                                <div class="image-error">
                                                    <el-icon>
                                                        <Box />
                                                    </el-icon>
                                                </div>
                                            </template>
                                        </el-image>
                                        <div class="image-overlay">
                                            <span>查看详情</span>
                                        </div>
                                    </div>
                                    <div class="product-details">
                                        <div class="product-name">{{ productDetails[item.productId]?.title }}</div>
                                        <div class="product-spec">数量: ×{{ item.quantity }}</div>
                                    </div>
                                    <div class="product-pricing">
                                        <div class="product-price">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                                        <div class="product-unit-price">¥{{ item.price.toFixed(2) }}/件</div>
                                        <div class="product-original-price"
                                            v-if="productDetails[item.productId]?.price">
                                            原价: ¥{{ productDetails[item.productId]?.price.toFixed(2) }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="order-footer" v-if="orderData.order.status === 'PENDING'">
                            <div class="footer-content">
                                <div class="footer-text">
                                    <el-icon>
                                        <Timer />
                                    </el-icon>
                                    <span>请尽快完成支付，避免订单超时</span>
                                </div>
                                <div class="footer-buttons">
                                    <el-button type="info" @click="handleCancelOrder(orderData.order.orderId)"
                                        size="large" :icon="Close" plain class="cancel-btn">
                                        取消订单
                                    </el-button>
                                    <el-button type="primary" @click="handlePay(orderData.order.orderId)" size="large"
                                        :icon="ArrowRight" class="pay-btn animate-pulse-btn">
                                        立即支付
                                    </el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                </el-card>

                <div v-if="orderItems.length === 0" class="empty-orders animate-empty-appear">
                    <div class="empty-content">
                        <div class="empty-icon">🛒</div>
                        <h3>暂无订单记录</h3>
                        <p>您还没有任何订单，快去购买商品吧！</p>
                        <el-button type="primary" @click="$router.push('/')" size="large" class="shop-now-btn">
                            <el-icon>
                                <ShoppingCart />
                            </el-icon>
                            立即购物
                        </el-button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 基础动画关键帧 */
@keyframes pageEnter {
    from {
        opacity: 0;
        transform: translateY(30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-30px);
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

@keyframes bounceIn {
    0% {
        opacity: 0;
        transform: scale(0.3);
    }

    50% {
        opacity: 1;
        transform: scale(1.1);
    }

    70% {
        transform: scale(0.9);
    }

    100% {
        opacity: 1;
        transform: scale(1);
    }
}

@keyframes titleReveal {
    from {
        opacity: 0;
        transform: translateY(20px) rotateX(90deg);
    }

    to {
        opacity: 1;
        transform: translateY(0) rotateX(0deg);
    }
}

@keyframes charAppear {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes cardAppear {
    from {
        opacity: 0;
        transform: translateY(40px) scale(0.95);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

@keyframes statAppear {
    from {
        opacity: 0;
        transform: translateY(30px) scale(0.9);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

@keyframes metaAppear {
    from {
        opacity: 0;
        transform: translateX(-20px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes productAppear {
    from {
        opacity: 0;
        transform: translateY(20px) scale(0.95);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

@keyframes emptyAppear {
    from {
        opacity: 0;
        transform: scale(0.8);
    }

    to {
        opacity: 1;
        transform: scale(1);
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

@keyframes loadingChar {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes lineGrow {
    from {
        width: 0;
    }

    to {
        width: 60px;
    }
}

/* 动画类 */
.animate-page-enter {
    animation: pageEnter 0.8s ease-out;
}

.animate-slide-down {
    animation: slideDown 0.6s ease-out calc(0.2s + var(--delay, 0s)) both;
}

.animate-bounce-in {
    animation: bounceIn 1s ease-out 0.6s both;
}

.animate-title-reveal {
    animation: titleReveal 0.8s ease-out 0.8s both;
}

.title-char {
    display: inline-block;
    animation: charAppear 0.6s ease-out both;
}

.animate-fade-in-up {
    animation: fadeInUp 0.6s ease-out 1s both;
}

.animate-slide-up-stagger {
    animation: fadeInUp 0.8s ease-out 1.2s both;
}

.animate-stat-appear {
    animation: statAppear 0.6s ease-out calc(1.4s + var(--delay)) both;
}

.animate-card-appear {
    animation: cardAppear 0.6s ease-out both;
}

.animate-meta-appear {
    animation: metaAppear 0.5s ease-out calc(0.2s + var(--delay)) both;
}

.animate-product-appear {
    animation: productAppear 0.5s ease-out both;
}

.animate-empty-appear {
    animation: emptyAppear 0.8s ease-out 1.5s both;
}

.animate-pulse-btn {
    animation: pulse 2s ease-in-out infinite;
}

.rotate-icon {
    animation: rotate 2s linear infinite;
}

.loading-char {
    animation: loadingChar 0.6s ease-out both;
}

.left-line {
    animation: lineGrow 0.6s ease-out 1.2s both;
}

.right-line {
    animation: lineGrow 0.6s ease-out 1.4s both;
}

/* 页面加载动画 */
.page-loading {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
}

.loading-content {
    text-align: center;
    color: #2c3e50;
}

.loading-icon {
    margin-bottom: 20px;
}

.loading-icon .el-icon {
    color: #4ecdc4;
}

.loading-text {
    font-size: 18px;
    font-weight: 500;
    letter-spacing: 2px;
}

/* 主容器 */
.page-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #f8fafc 0%, #e8f4f8 100%);
    padding: 20px;
}

.order-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0;
}

/* 面包屑 */
.breadcrumb {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 20px;
    padding: 12px 16px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 8px;
    font-size: 14px;
    backdrop-filter: blur(10px);
}

.breadcrumb-item {
    color: #6b7280;
    transition: color 0.3s ease;
}

.breadcrumb-item:hover {
    color: #3b82f6;
    cursor: pointer;
}

.breadcrumb-separator {
    color: #d1d5db;
}

.breadcrumb-current {
    color: #3b82f6;
    font-weight: 500;
}

/* 页面头部 */
.page-header {
    text-align: center;
    margin-bottom: 40px;
}

.header-decoration {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 20px;
    margin-bottom: 20px;
}

.decoration-line {
    height: 2px;
    background: linear-gradient(90deg, transparent, #4ecdc4, transparent);
}

.header-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: 0 8px 24px rgba(78, 205, 196, 0.3);
}

.page-title {
    font-size: 36px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0 0 12px 0;
    font-family: "华文中宋", serif;
}

.page-subtitle {
    font-size: 16px;
    color: #6b7280;
    margin: 0;
    font-weight: 400;
}

/* 统计概览 */
.stats-overview {
    display: flex;
    justify-content: center;
    gap: 30px;
    margin-bottom: 40px;
}

.stat-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px 24px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 16px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
    min-width: 140px;
}

.stat-item:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 20px;
}

.stat-icon.total {
    background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
}

.stat-icon.pending {
    background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
}

.stat-icon.success {
    background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
}

.stat-icon.failed {
    background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
}

.stat-info {
    text-align: left;
}

.stat-number {
    font-size: 28px;
    font-weight: 700;
    color: #2c3e50;
    line-height: 1;
}

.stat-label {
    font-size: 14px;
    color: #6b7280;
    font-weight: 500;
    margin-top: 4px;
}

/* 订单列表 */
.order-list {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.order-card {
    border-radius: 20px;
    transition: all 0.3s ease;
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    overflow: hidden;
}

.order-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.15);
}

.order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    background: linear-gradient(135deg, rgba(78, 205, 196, 0.1) 0%, rgba(255, 255, 255, 0.8) 100%);
}

.order-left {
    display: flex;
    align-items: center;
}

.order-number {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
}

.order-icon {
    color: #4ecdc4;
    font-size: 18px;
}

.label {
    color: #6b7280;
    font-weight: 500;
}

.value {
    font-weight: 600;
    color: #2c3e50;
}

.status-tag {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 20px;
    font-weight: 600;
}

.status-icon {
    font-size: 14px;
}

.order-content {
    padding: 24px;
}

.order-meta {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
}

.meta-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: rgba(248, 250, 252, 0.8);
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    transition: all 0.3s ease;
}

.meta-item:hover {
    background: white;
    border-color: #4ecdc4;
    transform: translateX(4px);
}

.meta-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;
}

.meta-icon.total-amount {
    background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
}

.meta-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.meta-label {
    font-size: 12px;
    color: #9ca3af;
    font-weight: 500;
}

.meta-value {
    font-size: 14px;
    color: #2c3e50;
    font-weight: 500;
}

.total-amount {
    font-size: 18px;
    font-weight: 700;
    color: #ef4444;
}

.section-divider {
    margin: 24px 0;
}

.product-section {
    margin-bottom: 24px;
}

.section-header {
    margin-bottom: 20px;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 0;
}

.section-icon {
    color: #4ecdc4;
    font-size: 18px;
}

.section-title h3 {
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
}

.item-count {
    padding: 4px 12px;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    margin-left: auto;
}

.products-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.product-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    transition: all 0.3s ease;
}

.product-item:hover {
    background: white;
    border-color: #4ecdc4;
    transform: translateX(8px);
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.1);
}

.product-index {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
    flex-shrink: 0;
}

.product-image-container {
    position: relative;
    width: 80px;
    height: 80px;
    flex-shrink: 0;
    border-radius: 8px;
    overflow: hidden;
}

.product-image {
    width: 100%;
    height: 100%;
    transition: transform 0.3s ease;
}

.product-item:hover .product-image {
    transform: scale(1.1);
}

.image-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.product-item:hover .image-overlay {
    opacity: 1;
}

.image-error {
    width: 100%;
    height: 100%;
    background: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #9ca3af;
}

.product-details {
    flex: 1;
    min-width: 0;
}

.product-name {
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 4px;
    line-height: 1.4;
}

.product-spec {
    font-size: 14px;
    color: #6b7280;
}

.product-pricing {
    text-align: right;
    flex-shrink: 0;
}

.product-price {
    font-size: 18px;
    font-weight: 700;
    color: #ef4444;
    margin-bottom: 4px;
}

.product-unit-price {
    font-size: 12px;
    color: #9ca3af;
    margin-bottom: 2px;
}

.product-original-price {
    font-size: 12px;
    color: #d1d5db;
    text-decoration: line-through;
}

.order-footer {
    margin-top: 24px;
    padding-top: 20px;
    border-top: 2px dashed rgba(78, 205, 196, 0.2);
}

.footer-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.footer-text {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #f39c12;
    font-size: 14px;
    font-weight: 500;
}

.footer-buttons {
    display: flex;
    gap: 12px;
}

.cancel-btn,
.pay-btn {
    padding: 12px 24px;
    border-radius: 25px;
    font-weight: 600;
    transition: all 0.3s ease;
}

.pay-btn {
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.3);
}

.pay-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(78, 205, 196, 0.4);
}

.cancel-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

/* 空状态 */
.empty-orders {
    text-align: center;
    padding: 80px 20px;
}

.empty-content {
    max-width: 400px;
    margin: 0 auto;
}

.empty-icon {
    font-size: 100px;
    margin-bottom: 24px;
}

.empty-content h3 {
    font-size: 24px;
    color: #2c3e50;
    margin: 0 0 12px 0;
    font-weight: 600;
}

.empty-content p {
    font-size: 16px;
    color: #6b7280;
    margin: 0 0 32px 0;
}

.shop-now-btn {
    padding: 16px 32px;
    border-radius: 25px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    box-shadow: 0 8px 24px rgba(78, 205, 196, 0.3);
    transition: all 0.3s ease;
}

.shop-now-btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 32px rgba(78, 205, 196, 0.4);
}

/* 响应式设计 */
@media (max-width: 1024px) {
    .stats-overview {
        grid-template-columns: repeat(2, 1fr);
        gap: 20px;
    }

    .order-meta {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 768px) {
    .page-container {
        padding: 16px;
    }

    .order-container {
        padding: 0;
    }

    .page-title {
        font-size: 28px;
    }

    .stats-overview {
        flex-direction: column;
        align-items: center;
        gap: 16px;
    }

    .stat-item {
        width: 100%;
        max-width: 300px;
    }

    .order-header {
        flex-direction: column;
        gap: 16px;
        align-items: stretch;
    }

    .order-content {
        padding: 20px 16px;
    }

    .footer-content {
        flex-direction: column;
        gap: 16px;
        align-items: stretch;
    }

    .footer-buttons {
        flex-direction: column;
    }

    .cancel-btn,
    .pay-btn {
        width: 100%;
    }

    .product-item {
        flex-direction: column;
        text-align: center;
        gap: 12px;
    }

    .product-pricing {
        text-align: center;
    }
}

@media (max-width: 480px) {
    .breadcrumb {
        font-size: 12px;
        padding: 8px 12px;
    }

    .page-title {
        font-size: 24px;
    }

    .order-card {
        margin: 0 -4px;
    }

    .order-header {
        padding: 16px;
    }

    .order-content {
        padding: 16px;
    }
}
</style>