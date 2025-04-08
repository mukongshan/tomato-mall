<script setup lang="ts">
import { ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import { Product, Stockpile } from "@/api/product.ts"
import { getProductsList, addProduct, deleteProduct, updateProduct } from "@/api/product.ts"
import { updateStockpile, getStockpile } from "@/api/product.ts"
// 1. 更新库存不生效 后端数据库是0 显示商品不存在
// 2. 删除商品不生效
// 3. 更新数据时后端报错商品不存在前端500错误 但是能正确更新
// 4. 编辑表单里面的id是不可编辑的  如果设置为-1 那么传入后端也是-1  应该把编辑表单的id隐藏掉 并且默认值变成被编辑的商品的id

// 存储商品列表数据
const products = ref<Product[]>([]);
// 存储库存列表数据
const stockpiles = ref<Stockpile[]>([]);
// 当前是 ​​新增商品​​ 还是 ​​编辑商品​​
const isAdding = ref(false);
//控制商品弹窗的显示/隐藏
const dialogProductVisible = ref(false);

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
    editProduct.value.specification.push({ item: "", value: "" });
};
// 移除规格
const removeSpecification = (index: number) => {
    editProduct.value.specification.splice(index, 1);// 从index位置 删除一个元素
};
// 处理添加商品逻辑
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
    dialogProductVisible.value = true;
}
// 处理编辑商品逻辑
const handleUpdate = async (product: Product) => {
    isAdding.value = false;
    // 深拷贝当前商品数据到表单
    editProduct.value = { ...product };
    dialogProductVisible.value = true;
};
// 提交表单
const handleSubmit = async () => {
    // 新增数据
    if (isAdding.value == true) {
        if (editProduct.value.title == "") {
            ElMessage.error("请填写标题")
            return
        }
        const { id: _, ...productWithoutId } = editProduct.value; // 解构赋值，去掉 id 属性
        addProduct(productWithoutId).then((res) => {
            if (res.data.code === '200') {
                ElMessage.success('添加成功');
            } else {
                ElMessage.error(res.data.msg);
            }
        });
    } else { // 编辑数据
        // 编辑数据的id是隐藏的 应该默认为该有的id 不能是-1
        updateProduct(editProduct.value).then((res) => {
            if (res.data.code === '200') {
                ElMessage.success('修改成功');
            } else {
                ElMessage.error(res.data.msg);
            }
        });
    }
    // 处理完成隐藏弹窗
    dialogProductVisible.value = false;
    await pageInit();
}
// 删除商品
const handleDelete = (productId: number) => {
    ElMessageBox.confirm('确定要删除这个商品吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        deleteProduct(productId).then((res) => {
            if (res.data.code === '200') {
                ElMessage.success('删除成功');
            } else {
                ElMessage.error(res.data.msg);
            }
        });
    }).catch(() => {
        ElMessage.info('已取消删除');
    });
};
// 调整库存数量
const handleStockpile = async (productId: number) => {
    ElMessageBox.prompt('请输入库存数量', '调整库存', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'number',
        inputPattern: /^-?\d+$/,  // 只允许整数
        inputErrorMessage: '请输入有效的整数',
    }).then(({ value }) => {
        const amount = Number(value);

        if (isNaN(amount) || amount < 0) {
            ElMessage.error('请输入有效的数字');
            return;
        }

        updateStockpile(productId, amount).then((res) => {
            if (res.data.code === '200') {
                ElMessage.success('调整成功');
            } else {
                ElMessage.error(res.data.msg);
            }
        });
    }).catch(() => {
        ElMessage.info('已取消调整');
    });
}

const getStockpileAmount = (productId: number): number => {
    const stock = stockpiles.value.find(s => s.productId === productId);
    return stock?.amount || 0;
}

const pageInit = async () => {
    // 这里是异步请求 但是for是同步的 所以会先执行完for循环 再执行异步请求 所以导致无法获取库存数据
    getProductsList().then(res => {
        if (res.data.code === '200') {
            products.value = res.data.data;
            for (const product of products.value) {
                getStockpile(product.id).then(res => {
                    if (res.data.code === '200') {
                        stockpiles.value.push(res.data.data);
                        console.log("成功")
                    } else {
                        ElMessage({
                            message: res.data.msg,
                            type: 'error',
                            center: true,
                        })
                        console.log("失败")
                    }
                })
            }
        } else {
            ElMessage({
                message: res.data.msg,
                type: 'error',
                center: true,
            })
        }
    })
    
};
pageInit();
</script>

<template>
    <h1>商品管理</h1>
    <el-button @click="handleAdd">
        增加商品
    </el-button>
    <el-card class="product-list">
        <!--Grid布局-->
        <div class="product-grid">
            <!--商品列表-->
            <div v-for="product in products" :key="product.id" class="product-item">
                <!-- 图片-->
                <div class="product-image-container">

                    <img :src="product.cover" alt="商品图片" class="product-image" />
                </div>
                <!-- 商品信息-->
                <div class="product-info">
                    <div class="product-name">{{ product.title }}</div>
                    <div class="product-price">¥{{ product.price }}</div>
                    <div class="product-stockpile">库存数量: {{ getStockpileAmount(product.id) }}</div>

                    <el-button @click="handleUpdate(product)">编辑</el-button>

                    <el-button @click="handleDelete(product.id)">删除</el-button>

                    <el-button @click="handleStockpile(product.id)">调整库存</el-button>
                </div>
            </div>
        </div>
    </el-card>

    <!-- 商品弹窗 -->
    <el-dialog v-model="dialogProductVisible" :title="isAdding ? '增加商品' : '编辑商品'" width="50%">
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
                    <el-button type="danger" circle @click="removeSpecification(index)">
                        <i class="el-icon-minus" />
                    </el-button>
                </div>
                <el-button type="primary" plain @click="addSpecification" icon="el-icon-plus">
                    添加规格
                </el-button>
            </el-form-item>
        </el-form>

        <template #footer>
            <el-button @click="dialogProductVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">
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

.product-stockpile {
    font-size: 12px;
    color: #666;
    margin-top: 4px;
}
</style>