<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { payOrder } from "@/api/order.ts";
import { getUserDetails, UserDetail } from "@/api/account.ts"

const checkoutData = JSON.parse(sessionStorage.getItem('checkoutData') || '{}')
const orderId = checkoutData.data.orderId
const totalAmount = checkoutData.data.totalAmount
const accountInfo = ref<UserDetail>({
    username: '',
    name: '',
    role: '',
    avatar: '',
    telephone: '',
    email: '',
    location: ''
})

const fetchUserDetails = async () => {
    try {
        const response = await getUserDetails(sessionStorage.getItem('username')!)
        Object.assign(accountInfo.value, response.data.data)
    } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败')
    }
}
fetchUserDetails()

const handleCheckout = async () => {
    payOrder(orderId).then((res) => {
        if (res.data.code === '200') {
            console.log(res.data)

            submitAlipayForm(res.data.data.paymentForm)

            ElMessage.success('跳转支付页面')
        } else {
            ElMessage({
                message: res.data.msg,
                type: 'error',
                center: true,
            })
        }
    })
}

const submitAlipayForm = (formHtml: string) => {
    console.log(formHtml)
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
</script>

<template>
    <div class="checkout-container">
        <h1 class="checkout-title">结账</h1>

        <div class="checkout-grid">
            <!-- 客户信息部分 -->
            <div class="checkout-section">
                <h2 class="section-title">基本信息</h2>
                <div class="form-group">
                    <label for="name">收货人姓名</label>
                    <input type="text" id="name" v-model="accountInfo.name">
                </div>
                <div class="form-group">
                    <label for="address">收货地址</label>
                    <input type="text" id="address" v-model:="accountInfo.location">
                </div>
                <div class="form-group">
                    <label for="phone">手机号码</label>
                    <input type="tel" id="phone" v-model:="accountInfo.telephone">
                </div>
                <div class="form-group">
                    <label for="email">电子邮箱</label>
                    <input type="email" id="email" v-model:="accountInfo.email">
                </div>
            </div>

            <div class="checkout-section">
                <h2 class="section-title">支付方式</h2>
                <div class="payment-methods">
                    <div class="payment-method">
                        <input type="radio" id="alipay" name="payment">
                        <label for="alipay">支付宝</label>
                    </div>
                </div>
            </div>

            <!-- 订单摘要部分 -->
            <div class="order-summary">
                <h2 class="section-title">订单摘要</h2>
                <div class="summary-item">
                    <span>商品小计</span>
                    <span>¥{{ totalAmount }}</span>
                </div>
                <div class="summary-item">
                    <span>运费</span>
                    <span>¥0.00</span>
                </div>
                <div class="summary-item">
                    <span>优惠</span>
                    <span>¥0.00</span>
                </div>
                <div class="summary-item total">
                    <span>总计</span>
                    <span>¥{{ totalAmount }}</span>
                </div>

                <button class="checkout-button" @click="handleCheckout">确认支付</button>

                <div class="secure-checkout">
                    <svg class="lock-icon" viewBox="0 0 24 24">
                        <path
                            d="M12 1C8.676 1 6 3.676 6 7v1H4v14h16V8h-2V7c0-3.324-2.676-6-6-6zm0 2c2.276 0 4 1.724 4 4v1H8V7c0-2.276 1.724-4 4-4zm0 10c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2z" />
                    </svg>
                    <span>安全支付</span>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.checkout-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
    font-family: 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    color: #333;
}

.checkout-title {
    font-size: 2rem;
    font-weight: 600;
    margin-bottom: 2rem;
    color: #2c3e50;
    text-align: center;
}

.checkout-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
}

.checkout-section {
    background: #fff;
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    margin-bottom: 1.5rem;
}

.section-title {
    font-size: 1.25rem;
    font-weight: 600;
    margin-bottom: 1.5rem;
    color: #2c3e50;
    border-bottom: 1px solid #eee;
    padding-bottom: 0.75rem;
}

.form-group {
    margin-bottom: 1.25rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-size: 0.875rem;
    font-weight: 500;
    color: #555;
}

.form-group input,
.form-group select {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
    transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus {
    outline: none;
    border-color: #3498db;
    box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.form-row {
    display: flex;
    gap: 1rem;
}

.half-width {
    flex: 1;
}

.payment-methods {
    margin-bottom: 1.5rem;
}

.payment-method {
    display: flex;
    align-items: center;
    margin-bottom: 0.75rem;
    padding: 0.75rem;
    border: 1px solid #eee;
    border-radius: 4px;
    transition: all 0.3s;
}

.payment-method:hover {
    border-color: #3498db;
    background-color: #f8fafc;
}

.payment-method input {
    margin-right: 0.75rem;
}

.payment-method label {
    font-size: 0.9375rem;
    cursor: pointer;
}

.order-summary {
    background: #fff;
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    grid-column: 2;
    grid-row: 1 / span 2;
    position: sticky;
    top: 2rem;
}

.summary-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #eee;
}

.summary-item.total {
    font-weight: 600;
    font-size: 1.125rem;
    color: #2c3e50;
    border-bottom: none;
    margin-top: 1.5rem;
}

.checkout-button {
    width: 100%;
    padding: 1rem;
    background-color: #3498db;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.3s;
    margin-top: 1.5rem;
}

.checkout-button:hover {
    background-color: #2980b9;
}

.secure-checkout {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 1rem;
    font-size: 0.875rem;
    color: #7f8c8d;
}

.lock-icon {
    width: 16px;
    height: 16px;
    fill: currentColor;
    margin-right: 0.5rem;
}

@media (max-width: 768px) {
    .checkout-grid {
        grid-template-columns: 1fr;
    }

    .order-summary {
        grid-column: 1;
        grid-row: auto;
        position: static;
    }
}
</style>