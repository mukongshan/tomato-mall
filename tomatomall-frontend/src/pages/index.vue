<script setup lang="ts">
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { Product } from "@/api/product.ts"
import { getProductsList } from "@/api/product.ts"
import router from "@/router/index.ts"
import {addProductToCart} from "@/api/cart.ts";

// 存储商品列表数据
const products = ref<Product[]>([]);

const handleAdd = (productId: number) => {
  const amount = 1;
  addProductToCart(productId, amount).then((res) => {
    if (res.data.code === '200') {
      ElMessage.success('添加成功');
    } else {
      ElMessage({
        message: res.data.msg,
        type: 'error',
        center: true,
      })
    }
  })
}

const pageInit = async () => {
  // 这里是异步请求 但是for是同步的 所以会先执行完for循环 再执行异步请求 所以导致无法获取库存数据
  await getProductsList().then(res => {
    if (res.data.code === '200') {
      products.value = res.data.data;

    } else {
      ElMessage({
        message: res.data.msg,
        type: 'error',
        center: true,
      })
    }
  })
};

const gotoDetails = (productId: number) => {
  router.push({ path: `/product/${productId}` });
};
pageInit();
</script>

<template>
  <h1>番茄书城</h1>
  <el-card class="product-list">
    <!--Grid布局-->
    <div class="product-grid">
      <!--商品列表-->
      <div v-for="product in products" :key="product.id" class="product-item">
        <!-- 图片-->
        <div class="product-image-container" @click="gotoDetails(product.id)">

          <img :src=" product.cover" alt="商品图片" class="product-image" />
        </div>
        <!-- 商品信息-->
        <div class="product-info">
          <div class="product-name">{{ product.title }}</div>
          <div class="product-price">¥{{ product.price }}</div>

          <el-button @click="handleAdd(product.id)">加入购物车</el-button>
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.product-list {
  margin-top: 20px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.product-item {
  display: flex;
  flex-direction: column;
  background-color: #ebeef5;
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  position: relative;
}

.product-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.product-image-container {
  width: 100%;
  aspect-ratio: 1/1;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-item:hover .product-image {
  transform: scale(1.05);
}

.product-info {
  padding: 12px;
  text-align: center;
  position: relative;
}

.product-name {
  font-weight: bold;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  color: #f56c6c;
  font-size: 14px;
  margin-bottom: 8px;
}
</style>