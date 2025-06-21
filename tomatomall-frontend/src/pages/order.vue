<script setup lang="ts">
import {getOrder, getOrderItems, Order, OrderItem, payOrder} from "@/api/order.ts";
import { ref } from "vue";
import {ElCard, ElTag, ElTable, ElTableColumn, ElDivider, ElMessage} from "element-plus";

const orders = ref<Order[]>([]);
const orderItems = ref<{ order: Order; items: OrderItem[] }[]>([]);

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

const loadOrders = async () => {
    try {
        const response = await getOrder(Number(sessionStorage.getItem("id")));
        orders.value = response.data.data;
    } catch (error) {
        console.error('加载失败:', error);
    }
}

const pageInit = async () => {
    await loadOrders();
    await loadOrderItems();
};

// 格式化日期
const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
}

// 格式化订单状态
const formatStatus = (status: string) => {
    const statusMap: Record<string, string> = {
        'PENDING': '待支付',
        'PAID': '已支付',
        'SHIPPED': '已发货',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
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
    return typeMap[status];
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
            submitAlipayForm(res.data.data.paymentForm)
            ElMessage.success('跳转支付页面')
        } else {
            ElMessage({
                message: res.data.msg,
                type: 'error',
                center: true,
            })
        }
    } catch (error) {
        console.error('失败', error);
    }
}

const submitAlipayForm = (formHtml: string) => {
    // 1. 解析HTML获取form数据
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = formHtml;
    const form = tempDiv.querySelector('form[name="punchout_form"]');

    if (!form) throw new Error('支付宝表单解析失败');

    // 2. 动态创建form
    const newForm = document.createElement('form');
    newForm.method = 'post';
    newForm.action = (form as HTMLFormElement).action; // 提取原form的action
    newForm.style.display = 'none';

    // 3. 复制所有input参数
    const inputs = form.querySelectorAll('input');
    inputs.forEach(input => {
        const newInput = document.createElement('input');
        newInput.type = 'hidden';
        newInput.name = input.name;
        newInput.value = input.value;
        newForm.appendChild(newInput);
    });

    // 4. 提交并清理
    document.body.appendChild(newForm);
    newForm.submit();
    document.body.removeChild(newForm); // 提交后移除
};

pageInit();
</script>

<template>
    <div class="order-container">
        <el-card class="box-card" v-for="orderData in orderItems" :key="orderData.order.orderId">
            <template #header>
                <div class="card-header">
                    <span>订单号: {{ orderData.order.orderId }}</span>
                    <el-tag :type="getStatusTagType(orderData.order.status)" size="small">
                        {{ formatStatus(orderData.order.status) }}
                    </el-tag>
                </div>
            </template>

            <div class="order-info">
                <div class="order-meta">
                    <p><strong>创建时间:</strong> {{ formatDate(orderData.order.createTime) }}</p>
                    <p><strong>支付方式:</strong> {{ formatPaymentMethod(orderData.order.paymentMethod) }}</p>
                    <p><strong>总金额:</strong> ¥{{ orderData.order.totalAmount.toFixed(2) }}</p>
                </div>

                <el-divider />

                <h4>商品清单</h4>
                <el-table :data="orderData.items" style="width: 100%">
                    <el-table-column prop="productId" label="商品ID" width="100" />
                    <el-table-column prop="quantity" label="数量" width="80" />
                    <el-table-column prop="price" label="单价" width="100">
                        <template #default="scope">
                            ¥{{ scope.row.price.toFixed(2) }}
                        </template>
                    </el-table-column>
                    <el-table-column label="小计" width="120">
                        <template #default="scope">
                            ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
                        </template>
                    </el-table-column>
                </el-table>

                <div class="order-actions" v-if="orderData.order.status === 'PENDING'">
                    <el-button type="primary" @click="handlePay(orderData.order.orderId)">
                        去支付
                    </el-button>
                </div>
            </div>
        </el-card>
    </div>
</template>

<style scoped>
.order-container {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
}

.box-card {
    margin-bottom: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.order-meta p {
    margin: 5px 0;
    color: #666;
}

.el-divider {
    margin: 15px 0;
}
</style>