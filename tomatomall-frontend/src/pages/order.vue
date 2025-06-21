<script setup lang="ts">
import { getOrder, getOrderItems, Order, OrderItem, payOrder, cancelOrder } from "@/api/order.ts";
import { ref } from "vue";
import { ElCard, ElTag, ElTable, ElTableColumn, ElDivider, ElButton, ElMessage, ElMessageBox } from "element-plus";
import { ArrowRight, Timer, Money, ShoppingCart, Close } from '@element-plus/icons-vue';
import { Product, getProduct } from "@/api/product.ts";

const orders = ref<Order[]>([]);
const orderItems = ref<{ order: Order; items: OrderItem[] }[]>([]);
const productDetails = ref<Record<number, Product>>({}); // 存储商品详情

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
    await loadOrders();
    await loadOrderItems();
    await loadProductDetails();
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

// 格式化支付方式
const formatPaymentMethod = (method: string) => {
    const methodMap: Record<string, string> = {
        'ALIPAY': '支付宝'
    };
    return methodMap[method] || method;
}

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

pageInit();
</script>

<template>
    <div class="order-container">
        <h1 class="page-title">我的订单</h1>

        <div class="order-list">
            <el-card class="order-card" v-for="orderData in orderItems" :key="orderData.order.orderId" shadow="hover">
                <template #header>
                    <div class="order-header">
                        <div class="order-number">
                            <span class="label">订单编号:</span>
                            <span class="value">{{ orderData.order.orderId }}</span>
                        </div>
                        <el-tag :type="getStatusTagType(orderData.order.status)" size="default" effect="dark">
                            {{ formatStatus(orderData.order.status) }}
                        </el-tag>
                    </div>
                </template>

                <div class="order-content">
                    <div class="order-meta">
                        <div class="meta-item">
                            <el-icon>
                                <Timer />
                            </el-icon>
                            <span class="meta-label">创建时间:</span>
                            <span>{{ formatDate(orderData.order.createTime) }}</span>
                        </div>
                        <div class="meta-item">
                            <el-icon>
                                <Money />
                            </el-icon>
                            <span class="meta-label">支付方式:</span>
                            <span>{{ formatPaymentMethod(orderData.order.paymentMethod) }}</span>
                        </div>
                        <div class="meta-item">
                            <el-icon>
                                <ShoppingCart />
                            </el-icon>
                            <span class="meta-label">总金额:</span>
                            <span class="total-amount">¥{{ orderData.order.totalAmount.toFixed(2) }}</span>
                        </div>
                    </div>

                    <el-divider border-style="dashed" />

                    <div class="product-section">
                        <h3 class="section-title">商品清单</h3>
                        <el-table :data="orderData.items" style="width: 100%" :show-header="false">
                            <el-table-column width="50">
                                <template #default="{ $index }">
                                    <span class="product-index">{{ $index + 1 }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column>
                                <template #default="{ row }">
                                    <div class="product-info-wrapper">
                                        <el-image
                                            :src="productDetails[row.productId]?.cover || 'https://via.placeholder.com/80'"
                                            fit="cover" class="product-image" lazy />
                                        <div class="product-info">
                                            <div class="product-name">{{ productDetails[row.productId]?.title }}</div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column width="80" align="center">
                                <template #default="{ row }">
                                    <span class="product-quantity">×{{ row.quantity }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column width="120" align="right">
                                <template #default="{ row }">
                                    <span class="product-price">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
                                    <div class="product-unit-price">¥{{ row.price.toFixed(2) }}/件</div>
                                    <div class="product-original-price" v-if="productDetails[row.productId]?.price">
                                        原价: ¥{{ productDetails[row.productId]?.price.toFixed(2) }}
                                    </div>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>

                    <div class="order-footer" v-if="orderData.order.status === 'PENDING'">
                        <div class="footer-buttons">
                            <el-button type="info" @click="handleCancelOrder(orderData.order.orderId)" size="large"
                                :icon="Close" plain>
                                取消订单
                            </el-button>
                            <el-button type="primary" @click="handlePay(orderData.order.orderId)" size="large"
                                :icon="ArrowRight">
                                立即支付
                            </el-button>
                        </div>
                    </div>
                </div>
            </el-card>

            <div v-if="orderItems.length === 0" class="empty-orders">
                <el-empty description="暂无订单记录" />
            </div>
        </div>
    </div>
</template>

<style scoped lang="css">
.order-container {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
    background-color: #f5f7fa;
    min-height: calc(100vh - 40px);

    .page-title {
        font-size: 24px;
        font-weight: 500;
        color: #333;
        margin-bottom: 24px;
        padding-bottom: 16px;
        border-bottom: 1px solid #ebeef5;
    }

    .order-list {
        display: flex;
        flex-direction: column;
        gap: 20px;
    }

    .order-card {
        border-radius: 8px;
        transition: all 0.3s ease;
        border: none;

        &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        .order-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 16px;

            .order-number {
                display: flex;
                align-items: center;
                gap: 8px;

                .label {
                    color: #909399;
                    font-size: 14px;
                }

                .value {
                    font-weight: 500;
                    color: #303133;
                }
            }
        }

        .order-content {
            padding: 0 16px 16px;

            .order-meta {
                display: flex;
                flex-wrap: wrap;
                gap: 24px;
                margin-bottom: 16px;

                .meta-item {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    color: #606266;
                    font-size: 14px;

                    .el-icon {
                        color: #909399;
                    }

                    .meta-label {
                        color: #909399;
                    }

                    .total-amount {
                        font-weight: bold;
                        color: #f56c6c;
                        font-size: 16px;
                    }
                }
            }

            .product-section {
                margin-top: 16px;

                .section-title {
                    font-size: 16px;
                    font-weight: 500;
                    margin-bottom: 12px;
                    color: #303133;
                }

                .product-index {
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    width: 24px;
                    height: 24px;
                    background-color: #f5f7fa;
                    border-radius: 50%;
                    color: #909399;
                    font-size: 12px;
                }

                .product-info-wrapper {
                    display: flex;
                    align-items: center;
                    gap: 12px;

                    .product-image {
                        width: 60px;
                        height: 60px;
                        border-radius: 4px;
                        flex-shrink: 0;
                    }
                }

                .product-info {
                    display: flex;
                    flex-direction: column;

                    .product-name {
                        font-size: 14px;
                        color: #303133;
                        line-height: 1.4;
                    }
                }

                .product-quantity {
                    color: #606266;
                    font-weight: 500;
                }

                .product-price {
                    font-weight: 500;
                    color: #f56c6c;
                    font-size: 16px;
                }

                .product-unit-price {
                    font-size: 12px;
                    color: #909399;
                    margin-top: 2px;
                }

                .product-original-price {
                    font-size: 12px;
                    color: #c0c4cc;
                    text-decoration: line-through;
                    margin-top: 2px;
                }
            }

            .order-footer {
                display: flex;
                justify-content: flex-end;
                margin-top: 20px;
                padding-top: 16px;
                border-top: 1px dashed #ebeef5;

                .footer-buttons {
                    display: flex;
                    gap: 12px;
                }
            }
        }
    }

    .empty-orders {
        margin-top: 40px;
    }
}


@media (max-width: 768px) {
    .order-container {
        padding: 16px;

        .order-card {
            .order-content {
                .order-meta {
                    flex-direction: column;
                    gap: 12px;
                }

                .order-footer {
                    .footer-buttons {
                        flex-direction: column;
                        width: 100%;

                        .el-button {
                            width: 100%;
                        }
                    }
                }
            }
        }
    }
}
</style>