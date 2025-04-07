<script setup lang="ts">
import { ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import {addProduct, deleteProduct, Product, updateProduct} from "@/api/product.ts"
import { getProductsList } from "@/api/product.ts"

const products = ref<Product[]>([
  {
    id: 1,
    title: "商品1",
    price: 9.99,
    rate: 9,
    description: "这是商品1",
    cover: "https://via.placeholder.com/150",
    details: "啥1？",
    specification: [{ item: "1", value: "1" }]
  },
  {
    id: 2,
    title: "商品2",
    price: 19.99,
    rate: 8,
    description: "这是商品2",
    cover: "https://via.placeholder.com/150",
    details: "啥2？",
    specification: [{ item: "2", value: "2" }]
  },
]);
const isAdding = ref(false);
const dialogVisible = ref(false);

// 编辑表单的数据结构
const editProduct = ref<Product>({
  id: -1,
  title: '',
  price: 0,
  rate: 0,
  description: '',
  cover: '',
  details: '',
  specification: []
});

// 添加新规格
const addSpecification = () => {
  editProduct.value.specification.push({ item: "", value: ""});
};
// 移除规格
const removeSpecification = (index: number) => {
  editProduct.value.specification.splice(index, 1);
};

const handleAdd = async () => {
  isAdding.value = true;
  // 初始化空表单
  editProduct.value = {
    id: -1,
    title: '',
    price: 0,
    rate: 0,
    description: '',
    cover: '',
    details: '',
    specification: []
  };
  dialogVisible.value = true;
}

const handleUpdate = async (product: Product) => {
  isAdding.value = false;
  // 深拷贝当前商品数据到表单
  editProduct.value = { ...product };
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  try {
    let response;
    if (isAdding.value == true) {
      response = await addProduct(editProduct.value);
    } else {
      response = await updateProduct(editProduct.value as Product);
    }
    ElMessage.success(response.data.data);
    await pageInit();
    dialogVisible.value = false;
  } catch (error: any) {
    ElMessage.error(error.response.msg);
  }
}

const handleDelete = (productId: number) => {
  ElMessageBox.confirm('确定要删除这个商品吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    try {
      deleteProduct(productId);
      ElMessage.success('删除成功');
    } catch(error: any) {
      ElMessage.error(error.response.msg);
    }

  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const pageInit = async () => {
  getProductsList().then(ret => {
    products.value = ret.data;
  })
}
pageInit();
</script>

<template>
  <h1>库存管理</h1>
  <el-button @click="handleAdd">
    增加商品
  </el-button>
  <el-card class="product-list">
    <div class="product-grid">
      <div
          v-for="product in products"
          :key="product.id"
          class="product-item"
      >
        <div class="product-image-container">
          <img :src="product.cover" alt="商品图片" class="product-image" />
        </div>
        <div class="product-info">
          <div class="product-name">{{ product.title }}</div>
          <div class="product-price">¥{{ product.price }}</div>
          <el-button @click="handleUpdate(product)">编辑</el-button>
          <el-button @click="handleDelete(product.id)">删除</el-button>
        </div>
      </div>
    </div>
  </el-card>

  <!-- 商品弹窗 -->
  <el-dialog v-model="dialogVisible" :title="isAdding ? '增加商品' : '编辑商品'" width="50%">
    <el-form label-width="100px">
      <el-form-item label="商品名称">
        <el-input v-model="editProduct.title" />
      </el-form-item>

      <el-form-item label="商品价格">
        <el-input-number v-model="editProduct.price" :min="0" :precision="2" />
      </el-form-item>

      <el-form-item label="商品评分">
        <el-input-number v-model="editProduct.rate" :min="0" :max="10.0" :precision="1" />
      </el-form-item>

      <el-form-item label="商品描述">
        <el-input v-model="editProduct.description" type="textarea" :rows="3" />
      </el-form-item>

      <el-form-item label="商品封面">
        <el-input v-model="editProduct.cover" />
      </el-form-item>

      <el-form-item label="商品细节">
        <el-input v-model="editProduct.details" />
      </el-form-item>

      <el-form-item label="商品规格">
        <div v-for="(spec, index) in editProduct.specification" :key="index" class="spec-item">
          <el-input v-model="spec.item" placeholder="规格名称" style="width: 200px; margin-right: 10px;" />
          <el-input v-model="spec.value" placeholder="规格内容" style="width: 200px; margin-right: 10px;" />
          <el-button
              type="danger"
              circle
              @click="removeSpecification(index)"
          >
            <i class="el-icon-minus" />
          </el-button>
        </div>
        <el-button type="primary" plain @click="addSpecification" icon="el-icon-plus">
          添加规格
        </el-button>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button
          type="primary"
          @click="handleSubmit"
      >
        {{ isAdding ? '添加' : '更新' }}
      </el-button>
    </template>
  </el-dialog>
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