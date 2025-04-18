<script setup lang="ts">
import { ref } from 'vue';
import {ElMessage} from "element-plus";
import {payOrder} from "../api/order.ts"

// 定义订单ID（假设从父组件传入或从路由参数获取）
const orderId = ref(1); // 示例订单ID，你可以根据实际情况动态设置


// 定义处理支付的方法
const handlePayOrder = async () => {
  try {
    const response = await payOrder(orderId.value); // 调用 payOrder 函数
    if (response.data.code === '200') {
      ElMessage.success('支付成功');
    } else {
      ElMessage({
        message: response.data.msg || '支付失败，请稍后再试',
        type: 'error',
        center: true,
      });
    }
  } catch (error) {
    ElMessage.error('支付失败，请稍后再试');
    console.error('支付失败:', error);
  }
};
</script>

<template>
  <div class="checkout-container">
    <h1 class="checkout-title">结账</h1>

    <div class="checkout-grid">
      <!-- 客户信息部分 -->
      <div class="checkout-section">
        <h2 class="section-title">客户信息</h2>
        <div class="form-group">
          <label for="email">电子邮箱</label>
          <input type="email" id="email" placeholder="example@email.com">
        </div>
        <div class="form-group">
          <label for="phone">手机号码</label>
          <input type="tel" id="phone" placeholder="+86 138 0000 0000">
        </div>
      </div>

      <!-- 配送地址部分 -->
      <div class="checkout-section">
        <h2 class="section-title">配送地址</h2>
        <div class="form-row">
          <div class="form-group half-width">
            <label for="first-name">名字</label>
            <input type="text" id="first-name" placeholder="张三">
          </div>
          <div class="form-group half-width">
            <label for="last-name">姓氏</label>
            <input type="text" id="last-name" placeholder="李">
          </div>
        </div>

        <div class="form-group">
          <label for="address">详细地址</label>
          <input type="text" id="address" placeholder="街道地址、公司名称等">
        </div>

        <div class="form-row">
          <div class="form-group half-width">
            <label for="city">城市</label>
            <input type="text" id="city" placeholder="例如：北京市">
          </div>
          <div class="form-group half-width">
            <label for="postal-code">邮政编码</label>
            <input type="text" id="postal-code" placeholder="100000">
          </div>
        </div>

        <div class="form-group">
          <label for="country">国家/地区</label>
          <select id="country">
            <option value="">请选择</option>
            <option value="china">中国</option>
            <option value="usa">美国</option>
            <option value="uk">英国</option>
          </select>
        </div>
      </div>

      <!-- 支付方式部分 -->
      <div class="checkout-section">
        <h2 class="section-title">支付方式</h2>
        <div class="payment-methods">
          <div class="payment-method">
            <input type="radio" id="credit-card" name="payment" checked>
            <label for="credit-card">信用卡/借记卡</label>
          </div>
          <div class="payment-method">
            <input type="radio" id="paypal" name="payment">
            <label for="paypal">PayPal</label>
          </div>
          <div class="payment-method">
            <input type="radio" id="alipay" name="payment">
            <label for="alipay">支付宝</label>
          </div>
          <div class="payment-method">
            <input type="radio" id="wechat-pay" name="payment">
            <label for="wechat-pay">微信支付</label>
          </div>
        </div>

        <div class="credit-card-form">
          <div class="form-group">
            <label for="card-number">卡号</label>
            <input type="text" id="card-number" placeholder="1234 5678 9012 3456">
          </div>

          <div class="form-row">
            <div class="form-group half-width">
              <label for="expiry-date">有效期</label>
              <input type="text" id="expiry-date" placeholder="MM/YY">
            </div>
            <div class="form-group half-width">
              <label for="cvv">安全码</label>
              <input type="text" id="cvv" placeholder="CVV">
            </div>
          </div>

          <div class="form-group">
            <label for="card-name">持卡人姓名</label>
            <input type="text" id="card-name" placeholder="与卡面一致">
          </div>
        </div>
      </div>

      <!-- 订单摘要部分 -->
      <div class="order-summary">
        <h2 class="section-title">订单摘要</h2>
        <div class="summary-item">
          <span>商品小计</span>
          <span>¥1,299.00</span>
        </div>
        <div class="summary-item">
          <span>运费</span>
          <span>¥0.00</span>
        </div>
        <div class="summary-item">
          <span>优惠</span>
          <span>-¥100.00</span>
        </div>
        <div class="summary-item total">
          <span>总计</span>
          <span>¥1,199.00</span>
        </div>

        <!-- 绑定点击事件 -->
        <button class="checkout-button" @click="handlePayOrder(orderId)">确认支付</button>

        <div class="secure-checkout">
          <svg class="lock-icon" viewBox="0 0 24 24">
            <path d="M12 1C8.676 1 6 3.676 6 7v1H4v14h16V8h-2V7c0-3.324-2.676-6-6-6zm0 2c2.276 0 4 1.724 4 4v1H8V7c0-2.276 1.724-4 4-4zm0 10c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2z"/>
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

.credit-card-form {
  margin-top: 1.5rem;
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