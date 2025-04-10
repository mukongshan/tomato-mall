<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { router } from "@/router/index.ts"
import {CartItem, updateCartItem} from "@/api/cart.js";
import { getCartProducts } from "@/api/cart.js";

// 购物车数据
const cartItems = ref<CartItem[]>([]);
const loading = ref(false)
const selectedItems = ref([])

// 获取购物车数据
const fetchCartItems = async () => {
  loading.value = true
  try {
    await getCartProducts().then((res) => {
      if (res.data.code === 200) {
        cartItems.value = res.data.data;
      } else {
        ElMessage({
          message: res.data.msg,
          type: 'error',
          center: true,
        })
      }
    })
  } catch (error) {
    ElMessage.error('获取购物车数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 初始化加载
onMounted(() => { fetchCartItems() })

// 数量变化处理
const handleQuantityChange = (item, newQuantity) => {
  if (newQuantity < 1) {
    ElMessage.warning('数量不能小于1')
    return
  }
  if (newQuantity > item.quantity) {
    ElMessage.warning(`库存不足，最多可购买${item.amount}件`)
    return
  }

  // 这里调用更新购物车数量的API
  updateCartItem(item.cartItemId, newQuantity).then((res) => {
    if (res.data.code === 200) {
      item.quantity = newQuantity
      ElMessage.success('更新成功');
      console.log(`更新商品 ${item.id} 数量为 ${newQuantity}`)
    } else {
      ElMessage({
        message: res.data.msg,
        type: 'error',
        center: true,
      })
    }
  })
}

// 删除商品
const handleDeleteItem = (id) => {
  ElMessageBox.confirm('确定要删除该商品吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 这里调用删除API
    cartItems.value = cartItems.value.filter(item => item.id !== id)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 切换选中状态
const toggleSelectItem = (item) => {
  item.selected = !item.selected
  selectedItems.value = cartItems.value.filter(item => item.selected)
}

// 全选/取消全选
const toggleSelectAll = (isSelectAll) => {
  cartItems.value.forEach(item => {
    item.selected = isSelectAll
  })
  selectedItems.value = isSelectAll ? [...cartItems.value] : []
}

// 计算总价
const totalPrice = computed(() => {
  return 0
  // return selectedItems.value.reduce((sum, item) => {
  //   return sum + (item.price * item.quantity)
  // }, 0)
})

// 结算
const handleCheckout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请至少选择一件商品')
    return
  }

  // 这里跳转到结算页面或调用结算API
  console.log('结算商品:', selectedItems.value)
  ElMessage.success('跳转结算页面')
}
</script>

<template>
  <div class="cart-container">
    <h1 class="cart-title">我的购物车</h1>

    <div class="cart-content">
      <!-- 购物车列表 -->
      <div class="cart-list">
        <div class="cart-header">
          <el-checkbox
              :model-value="selectedItems.length === cartItems.length && cartItems.length > 0"
              @change="toggleSelectAll"
          >
            全选
          </el-checkbox>
          <span class="header-item">商品信息</span>
          <span class="header-item">单价</span>
          <span class="header-item">数量</span>
          <span class="header-item">小计</span>
          <span class="header-item">操作</span>
        </div>

        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="cartItems.length === 0" class="empty-cart">
          <el-empty description="购物车空空如也">
            <el-button type="primary" @click="$router.push('/index')">去逛逛</el-button>
          </el-empty>
        </div>

        <div v-else class="cart-items">
          <div v-for="item in cartItems" :key="item.cartItemId" class="cart-item">
            <div class="item-select">
              <el-checkbox
                  v-model="item.selected"
                  @change="toggleSelectItem(item)"
              />
            </div>
            <div class="item-info">
              <el-image
                  :src="item.image"
                  fit="cover"
                  class="product-image"
              />
              <div class="product-info">
                <div class="product-name">{{ item.productId }}</div>
                <div class="product-spec">规格: 默认</div>
              </div>
            </div>
            <div class="item-price">¥{{ item.productId }}</div>
            <div class="item-quantity">
              <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="item.productId"
                  @change="handleQuantityChange(item, $event)"
              />
            </div>
            <div class="item-total">¥{{ (item.productId * item.quantity).toFixed(2) }}</div>
            <div class="item-actions">
              <el-button
                  type="danger"
                  size="small"
                  @click="handleDeleteItem(item.cartItemId)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox
              :model-value="selectedItems.length === cartItems.length && cartItems.length > 0"
              @change="toggleSelectAll"
          >
            全选
          </el-checkbox>
          <el-button
              type="text"
              @click="cartItems.forEach(item => handleDeleteItem(item.cartItemId))"
          >
            删除选中
          </el-button>
        </div>
        <div class="footer-right">
          <div class="total-info">
            <span>已选 {{ selectedItems.length }} 件</span>
            <span class="total-price">合计: ¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <el-button
              type="primary"
              size="large"
              :disabled="selectedItems.length === 0"
              @click="handleCheckout"
          >
            去结算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .cart-title {
    font-size: 24px;
    font-weight: 500;
    margin-bottom: 20px;
    color: #333;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;
  }

  .cart-content {
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  }

  .cart-header {
    display: flex;
    align-items: center;
    padding: 15px 20px;
    background: #f5f5f5;
    border-bottom: 1px solid #eee;
    font-size: 14px;
    color: #666;

    .el-checkbox {
      margin-right: 20px;
    }

    .header-item {
      flex: 1;
      text-align: center;

      &:first-of-type {
        flex: 3;
        text-align: left;
        padding-left: 40px;
      }
    }
  }

  .loading-container {
    padding: 20px;
  }

  .empty-cart {
    padding: 60px 0;
  }

  .cart-items {
    .cart-item {
      display: flex;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #f5f5f5;

      &:hover {
        background: #fafafa;
      }

      > div {
        flex: 1;
        text-align: center;
      }

      .item-select {
        flex: 0 0 50px;
        text-align: left;
      }

      .item-info {
        flex: 3;
        display: flex;
        align-items: center;
        text-align: left;

        .product-image {
          width: 80px;
          height: 80px;
          margin-right: 15px;
          border: 1px solid #f5f5f5;
          border-radius: 4px;
        }

        .product-name {
          font-size: 16px;
          margin-bottom: 5px;
        }

        .product-spec {
          font-size: 12px;
          color: #999;
        }
      }

      .item-price, .item-total {
        color: #f56c6c;
        font-weight: 500;
      }

      .item-quantity {
        :deep(.el-input-number) {
          width: 120px;
        }
      }
    }
  }

  .cart-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    background: #f5f5f5;
    border-top: 1px solid #eee;

    .footer-left {
      display: flex;
      align-items: center;

      .el-checkbox {
        margin-right: 20px;
      }
    }

    .footer-right {
      display: flex;
      align-items: center;

      .total-info {
        margin-right: 30px;
        text-align: right;

        span {
          display: block;
          line-height: 1.5;
        }

        .total-price {
          font-size: 18px;
          color: #f56c6c;
          font-weight: 500;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .cart-container {
    padding: 10px;

    .cart-header {
      display: none;
    }

    .cart-item {
      flex-wrap: wrap;
      position: relative;
      padding: 15px 10px !important;

      .item-select {
        position: absolute;
        left: 10px;
        top: 15px;
      }

      .item-info {
        flex: 1 0 100% !important;
        margin-bottom: 10px;
        padding-left: 30px;
      }

      .item-price, .item-quantity, .item-total, .item-actions {
        flex: 1;
        margin-bottom: 10px;
      }

      .item-price::before {
        content: "单价: ";
        color: #999;
      }

      .item-total::before {
        content: "小计: ";
        color: #999;
      }
    }

    .cart-footer {
      flex-direction: column;

      .footer-left {
        width: 100%;
        justify-content: space-between;
        margin-bottom: 15px;
      }

      .footer-right {
        width: 100%;
        justify-content: space-between;

        .total-info {
          margin-right: 0;
          text-align: left;
        }
      }
    }
  }
}
</style>