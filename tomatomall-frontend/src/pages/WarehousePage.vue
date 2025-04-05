<script setup lang="ts">
import { ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import { Product, Specification } from "@/api/product.ts"
import { getProductsList, getProduct,} from "@/api/product.ts"

const products = ref<Product[]>([
  {
    id: 1,
    title: "商品1",
    price: 9.99,
    rate: 9,
    description: "这是商品1",
    cover: "https://via.placeholder.com/150",
    details: "啥1？",
    specification: {}
  },
  {
    id: 2,
    title: "商品2",
    price: 19.99,
    rate: 8,
    description: "这是商品2",
    cover: "https://via.placeholder.com/150",
    details: "啥2？",
    specification: {}
  },
]);

// 当前激活的下拉菜单
const activeDropdown = ref<number | null>(null);

// 切换下拉菜单显示状态
const toggleDropdown = (productId: number) => {
  activeDropdown.value = activeDropdown.value === productId ? null : productId;
};

// 关闭所有下拉菜单
const closeDropdowns = () => {
  activeDropdown.value = null;
};

const pageInit = async () => {
  getProductsList().then(ret => {
    products.value = ret.data;
  })
}


// 删除商品
const handleDelete = (productId: number) => {
  ElMessageBox.confirm('确定要删除这个商品吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    products.value = products.value.filter(p => p.productId !== productId);
    ElMessage.success('删除成功');
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 更新商品
const handleUpdate = (product: Product) => {
  ElMessageBox.prompt('请输入新的商品名称', '编辑商品', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: product.productName
  }).then(({ value }) => {
    const index = products.value.findIndex(p => p.productId === product.productId);
    if (index !== -1) {
      products.value[index].productName = value;
      ElMessage.success('更新成功');
    }
  }).catch(() => {
    ElMessage.info('已取消编辑');
  });
};

pageInit();
</script>

<template>
  <h1>库存管理</h1>
  <el-card class="product-list">
    <div class="product-grid">
      <div
          v-for="product in products"
          :key="product.id"
          class="product-item"
          @click="closeDropdowns"
      >
        <div class="product-image-container">
          <img :src="product.cover" alt="商品图片" class="product-image" />
        </div>
        <div class="product-info">
          <div class="product-name">{{ product.title }}</div>
          <div class="product-price">¥{{ product.price }}</div>

          <!-- 三点下拉菜单 -->
          <div class="product-actions">
            <el-button
                class="more-button"
                @click.stop="toggleDropdown(product.id)"
            >
              <i class="el-icon-more"></i>
            </el-button>

            <div
                v-show="activeDropdown === product.id"
                class="dropdown-menu"
                @click.stop
            >
              <div class="dropdown-item" @click="handleUpdate(product)">
                <i class="el-icon-edit"></i> 更新
              </div>
              <div class="dropdown-item danger" @click="handleDelete(product.id)">
                <i class="el-icon-delete"></i> 删除
              </div>
            </div>
          </div>
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

/* 三点按钮样式 */
.product-actions {
  position: relative;
  display: flex;
  justify-content: center;
}

.more-button {
  padding: 5px;
  min-width: auto;
  border: none;
  background: transparent;
  color: #666;
}

.more-button:hover {
  color: #409eff;
  background: transparent;
}

/* 下拉菜单样式 */
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: #3ac6cd;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 10;
  min-width: 100px;
}

.dropdown-item {
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.dropdown-item:hover {
  background-color: #4572b6;
}

.dropdown-item i {
  margin-right: 5px;
}

.dropdown-item.danger {
  color: #f56c6c;
}

.dropdown-item.danger:hover {
  background-color: #fef0f0;
}
</style>