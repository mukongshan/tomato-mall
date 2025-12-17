<script setup lang="ts">
import { ref } from "vue";
import { ElMessageBox, ElMessage, UploadProps, UploadFile } from "element-plus";
import { getProductsByShopId, Product, Stockpile } from "@/api/product.ts"
import { addProduct, deleteProduct, updateProduct } from "@/api/product.ts"
import { updateStockpile, getStockpile } from "@/api/product.ts"
import router from "@/router/index.ts"
import { uploadImg } from "@/utils/image.ts";
import { Plus, Picture, Edit, Delete, Setting, ShoppingBag, TrendCharts } from '@element-plus/icons-vue';
import { useRoute } from "vue-router";

const route = useRoute();
const shopId = ref<number>(Number(route.params.id)); // 获取路由参数中的 shopId
// 1. 商品规格表
// 2. 图片上传

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
    detail: '',
    specifications: [],
    shopId: shopId.value // 使用路由参数中的 shopId
});

// 添加新规格
const addSpecification = () => {
    if (!editProduct.value.specifications) {
        editProduct.value.specifications = []; // 如果未定义则初始化
    }
    editProduct.value.specifications.push({
        specificationId: '', // 新增时ID为空，由后端生成
        productId: editProduct.value.id.toString(), // 关联当前商品ID
        item: "",
        value: ""
    });
};
// 移除规格
const removeSpecification = (index: number) => {
    editProduct.value.specifications.splice(index, 1);// 从index位置 删除一个元素
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
        detail: '',
        specifications: [],
        shopId: shopId.value // 使用路由参数中的 shopId
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
/*
async 表示函数内部包含异步操作
await 表示等待异步操作完成 再继续执行后面的代码
*/
const handleSubmit = async () => {
    // 新增数据
    if (isAdding.value == true) {
        if (editProduct.value.title == "") {
            ElMessage.error("请填写标题")
            return
        }
        const { id: _, ...productWithoutId } = editProduct.value; // 解构赋值，去掉 id 属性
        await addProduct(productWithoutId).then((res) => {
            if (res.data.code === '200') {
                ElMessage.success('添加成功');
            }
        });
    } else { // 编辑数据
        console.log(editProduct.value.cover)
        // 编辑数据的id是隐藏的 应该默认为该有的id 不能是-1
        await updateProduct(editProduct.value).then((res) => {
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
                pageInit(); // 刷新页面
            } else {
                ElMessage.error(res.data.msg);
            }
        });
    }).catch(() => {
        ElMessage.info('已取消删除');
    });
};
const handleStockpile = async (productId: number) => {
    try {
        const { value } = await ElMessageBox.prompt('请输入库存数量', '调整库存', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputType: 'number',
            inputPattern: /^-?\d+$/,
            inputErrorMessage: '请输入有效的整数',
        });

        const amount = Number(value);
        if (isNaN(amount) || amount < 0) {
            ElMessage.error('请输入有效的数字');
            return;
        }

        // 直接 await 异步操作
        const res = await updateStockpile(productId, amount);
        if (res.data.code === '200') {
            ElMessage.success('调整成功');
            await pageInit(); // 确保刷新
        } else {
            ElMessage.error(res.data.msg);
        }
        await pageInit(); // 确保刷新
    } catch (error: any) {
        if (error === 'cancel') {
            ElMessage.info('已取消调整');
        }
    }
};

const getStockpileAmount = (productId: number): number => {
    const stock = stockpiles.value.find(s => s.productId === productId);
    return stock?.amount || 0;
}

const pageInit = async () => {
    // 这里是异步请求 但是for是同步的 所以会先执行完for循环 再执行异步请求 所以导致无法获取库存数据
    await getProductsByShopId(shopId.value).then(res => {
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
    stockpiles.value = [];// 清空库存数据
    for (const product of products.value) {
        await getStockpile(product.id).then(res => {
            if (res.data.code === '200') {
                stockpiles.value.push(res.data.data);
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
};

// 图片验证
const beforeLogoUpload: UploadProps['beforeUpload'] = (rawFile) => {
    const isImage = ['image/jpg', 'image/png'].includes(rawFile.type)
    const isSizeValid = rawFile.size <= 5 * 1024 * 1024

    if (!isImage) {
        ElMessage.error('Logo必须为JPG/PNG格式')
        return false
    }
    if (!isSizeValid) {
        ElMessage.error('Logo大小不能超过5MB')
        return false
    }
    return true
}
// 图片上传处理
const handleAvatarChange: UploadProps['onChange'] = async (uploadFile: UploadFile) => {
    if (uploadFile.raw) {
        try {
            const formData = new FormData()
            formData.append('file', uploadFile.raw!)
            const response = await uploadImg(formData)
            editProduct.value.cover = response.data.data;
        } catch (error) {
            ElMessage.error("图片上传失败：" + (error || '未知错误'));
        }
    }
}

// 删除头像
const handleAvatarRemove = () => {
    editProduct.value.cover = ''
}

const gotoDetails = (productId: number) => {
    router.push({ path: `/product/${productId}` });
};
pageInit();
</script>

<template>
    <div class="product-management-container">
        <!-- 页面头部 -->
        <div class="page-header animate-fade-in">
            <div class="header-content">
                <div class="header-left">
                    <div class="header-icon pulse-icon">
                        <el-icon size="36">
                            <ShoppingBag />
                        </el-icon>
                    </div>
                    <div class="header-text">
                        <h1 class="page-title">
                            <span class="title-char" v-for="(char, index) in '商品管理'" :key="index"
                                :style="{ animationDelay: `${index * 0.1}s` }">
                                {{ char }}
                            </span>
                        </h1>
                        <p class="page-subtitle animate-slide-up">管理您的商品信息和库存</p>
                    </div>
                </div>
                <div class="header-right">
                    <div class="stats-card animate-scale-in">
                        <div class="stat-item">
                            <div class="stat-icon">
                                <el-icon>
                                    <ShoppingBag />
                                </el-icon>
                            </div>
                            <div class="stat-info">
                                <div class="stat-number counter-animation">{{ products.length }}</div>
                                <div class="stat-label">商品总数</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="action-bar animate-slide-in">
                <el-button type="primary" size="large" @click="handleAdd" class="add-product-btn wobble-on-hover">
                    <el-icon>
                        <Plus />
                    </el-icon>
                    增加商品
                </el-button>
            </div>
        </div>

        <!-- 商品列表 -->
        <el-card class="product-list animate-fade-in-up" shadow="hover">
            <div v-if="products.length === 0" class="empty-state animate-bounce-in">
                <div class="empty-icon float-animation">
                    <el-icon size="80">
                        <ShoppingBag />
                    </el-icon>
                </div>
                <h3>暂无商品</h3>
                <p>点击上方按钮开始添加您的第一个商品</p>
                <el-button type="primary" @click="handleAdd" class="add-first-btn pulse-on-hover">
                    <el-icon>
                        <Plus />
                    </el-icon>
                    添加商品
                </el-button>
            </div>

            <div v-else class="product-grid">
                <div v-for="(product, index) in products" :key="product.id" class="product-item animate-product-in"
                    :style="{ animationDelay: `${index * 0.1}s` }">

                    <!-- 商品图片区域 -->
                    <div class="product-image-container" @click="gotoDetails(product.id)">
                        <el-image :src="product.cover" alt="商品图片" class="product-image hover-scale" fit="cover">
                            <template #error>
                                <div class="image-error pulse-icon">
                                    <el-icon size="40">
                                        <Picture />
                                    </el-icon>
                                    <span>图片加载失败</span>
                                </div>
                            </template>
                        </el-image>

                        <!-- 悬停遮罩 -->
                        <div class="image-overlay">
                            <span>点击查看详情</span>
                        </div>

                        <!-- 库存标签 -->
                        <div class="stock-badge" :class="{ 'low-stock': getStockpileAmount(product.id) < 10 }">
                            <el-icon>
                                <TrendCharts />
                            </el-icon>
                            <span>{{ getStockpileAmount(product.id) }}</span>
                        </div>

                        <!-- 光泽效果 -->
                        <div class="image-shine"></div>
                    </div>

                    <!-- 商品信息 -->
                    <div class="product-info">
                        <div class="product-name" :title="product.title">{{ product.title }}</div>
                        <div class="product-price price-glow">¥{{ product.price }}</div>
                        <div class="product-stockpile"
                            :class="{ 'low-stock-text': getStockpileAmount(product.id) < 10 }">
                            库存数量: {{ getStockpileAmount(product.id) }}
                        </div>

                        <!-- 操作按钮组 -->
                        <div class="product-actions">
                            <el-button type="primary" size="small" @click="handleUpdate(product)"
                                class="action-btn edit-btn">
                                <el-icon>
                                    <Edit />
                                </el-icon>
                                编辑
                            </el-button>

                            <el-button type="danger" size="small" @click="handleDelete(product.id)"
                                class="action-btn delete-btn shake-on-hover">
                                <el-icon>
                                    <Delete />
                                </el-icon>
                                删除
                            </el-button>

                            <el-button type="warning" size="small" @click="handleStockpile(product.id)"
                                class="action-btn stock-btn bounce-on-hover">
                                <el-icon>
                                    <Setting />
                                </el-icon>
                                调整库存
                            </el-button>
                        </div>
                    </div>

                    <!-- 卡片边框光效 -->
                    <div class="card-glow"></div>
                </div>
            </div>
        </el-card>

        <!-- 商品弹窗 -->
        <el-dialog v-model="dialogProductVisible" :title="isAdding ? '✨ 增加商品' : '📝 编辑商品'" width="60%"
            class="product-dialog animate-dialog" :close-on-click-modal="false">

            <div class="dialog-content">
                <el-form label-width="120px" class="product-form">
                    <div class="form-section animate-form-section">
                        <h3 class="section-title">
                            <el-icon>
                                <Edit />
                            </el-icon>
                            基本信息
                        </h3>

                        <el-form-item label="商品名称" class="form-item">
                            <el-input v-model="editProduct.title" placeholder="请输入商品名称" class="form-input" />
                        </el-form-item>

                        <el-form-item label="商品价格" class="form-item">
                            <el-input-number v-model="editProduct.price" :min="0" :precision="2" class="form-number"
                                placeholder="0.00" />
                        </el-form-item>

                        <el-form-item label="商品描述" class="form-item">
                            <el-input v-model="editProduct.description" type="textarea" :rows="3"
                                placeholder="请描述您的商品特色" class="form-textarea" />
                        </el-form-item>
                    </div>

                    <div class="form-section animate-form-section">
                        <h3 class="section-title">
                            <el-icon>
                                <Picture />
                            </el-icon>
                            商品图片
                        </h3>

                        <el-form-item label="商品封面" class="form-item">
                            <div class="upload-container">
                                <el-upload class="avatar-uploader" :show-file-list="false"
                                    :on-change="handleAvatarChange" :on-remove="handleAvatarRemove"
                                    :before-upload="beforeLogoUpload" accept="image/*" :auto-upload="false">

                                    <div class="upload-area" :class="{ 'has-image': editProduct.cover }">
                                        <el-image v-if="editProduct.cover" :src="editProduct.cover"
                                            class="uploaded-image hover-scale" fit="cover">
                                            <template #error>
                                                <div class="image-error">
                                                    <el-icon>
                                                        <Picture />
                                                    </el-icon>
                                                    <span>图片加载失败</span>
                                                </div>
                                            </template>
                                        </el-image>

                                        <div v-else class="upload-placeholder">
                                            <el-icon size="40" class="upload-icon bounce-icon">
                                                <Plus />
                                            </el-icon>
                                            <div class="upload-text">点击上传商品图片</div>
                                        </div>

                                        <div class="upload-overlay">
                                            <el-icon>
                                                <Edit />
                                            </el-icon>
                                            <span>更换图片</span>
                                        </div>
                                    </div>
                                </el-upload>

                                <div class="upload-tip">
                                    <el-icon>
                                        <Picture />
                                    </el-icon>
                                    <span>支持JPG/PNG格式，且不超过5MB</span>
                                </div>
                            </div>
                        </el-form-item>
                    </div>

                    <div class="form-section animate-form-section">
                        <h3 class="section-title">
                            <el-icon>
                                <Setting />
                            </el-icon>
                            详细信息
                        </h3>

                        <el-form-item label="商品细节" class="form-item">
                            <el-input v-model="editProduct.detail" type="textarea" :rows="4" placeholder="详细描述商品的功能、用途等"
                                class="form-textarea" />
                        </el-form-item>
                    </div>

                    <div class="form-section animate-form-section">
                        <h3 class="section-title">
                            <el-icon>
                                <TrendCharts />
                            </el-icon>
                            商品规格
                        </h3>

                        <el-form-item label="商品规格" class="form-item">
                            <div class="specifications-container">
                                <div v-for="(spec, index) in editProduct.specifications" :key="index"
                                    class="spec-item animate-spec-item" :style="{ animationDelay: `${index * 0.1}s` }">

                                    <div class="spec-inputs">
                                        <el-input v-model="spec.item" placeholder="规格名称（如：颜色、尺寸）" class="spec-input" />

                                        <el-input v-model="spec.value" placeholder="规格内容（如：红色、XL）" class="spec-input" />
                                    </div>

                                    <el-button type="danger" circle @click="removeSpecification(index)"
                                        class="remove-spec-btn shake-on-hover">
                                        <el-icon>
                                            <Delete />
                                        </el-icon>
                                    </el-button>
                                </div>

                                <el-button type="primary" plain @click="addSpecification"
                                    class="add-spec-btn pulse-on-hover">
                                    <el-icon>
                                        <Plus />
                                    </el-icon>
                                    添加规格
                                </el-button>
                            </div>
                        </el-form-item>
                    </div>
                </el-form>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="dialogProductVisible = false" size="large" class="cancel-btn">
                        取消
                    </el-button>
                    <el-button type="primary" @click="handleSubmit" size="large" class="submit-btn pulse-on-hover">
                        <el-icon>
                            <component :is="isAdding ? Plus : Edit" />
                        </el-icon>
                        {{ isAdding ? '添加' : '更新' }}
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
/* 基础动画关键帧 */
@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateX(-50px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes scaleIn {
    from {
        opacity: 0;
        transform: scale(0.8);
    }

    to {
        opacity: 1;
        transform: scale(1);
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(50px);
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
        transform: scale(1.05);
    }

    70% {
        transform: scale(0.9);
    }

    100% {
        opacity: 1;
        transform: scale(1);
    }
}

@keyframes productIn {
    from {
        opacity: 0;
        transform: translateY(40px) rotateX(45deg);
    }

    to {
        opacity: 1;
        transform: translateY(0) rotateX(0deg);
    }
}

@keyframes specIn {
    from {
        opacity: 0;
        transform: translateX(-20px) scale(0.95);
    }

    to {
        opacity: 1;
        transform: translateX(0) scale(1);
    }
}

@keyframes bounce {

    0%,
    20%,
    50%,
    80%,
    100% {
        transform: translateY(0);
    }

    40% {
        transform: translateY(-10px);
    }

    60% {
        transform: translateY(-5px);
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

@keyframes float {

    0%,
    100% {
        transform: translateY(0px);
    }

    50% {
        transform: translateY(-15px);
    }
}

@keyframes shake {

    0%,
    100% {
        transform: translateX(0);
    }

    25% {
        transform: translateX(-3px);
    }

    75% {
        transform: translateX(3px);
    }
}

@keyframes wobble {

    0%,
    100% {
        transform: rotate(0deg);
    }

    25% {
        transform: rotate(-5deg);
    }

    75% {
        transform: rotate(5deg);
    }
}

@keyframes shimmer {
    0% {
        transform: translateX(-100%);
    }

    100% {
        transform: translateX(100%);
    }
}

@keyframes glow {

    0%,
    100% {
        box-shadow: 0 0 5px rgba(78, 205, 196, 0.5);
        text-shadow: 0 0 10px rgba(78, 205, 196, 0.5);
    }

    50% {
        box-shadow: 0 0 20px rgba(78, 205, 196, 0.8);
        text-shadow: 0 0 20px rgba(78, 205, 196, 0.8);
    }
}

@keyframes priceGlow {

    0%,
    100% {
        color: #f56c6c;
        text-shadow: none;
    }

    50% {
        color: #ff8a8a;
        text-shadow: 0 0 10px rgba(245, 108, 108, 0.5);
    }
}

@keyframes counterUp {
    from {
        transform: translateY(20px);
        opacity: 0;
    }

    to {
        transform: translateY(0);
        opacity: 1;
    }
}

@keyframes cardGlow {

    0%,
    100% {
        opacity: 0;
    }

    50% {
        opacity: 0.6;
    }
}

/* 动画类 */
.animate-fade-in {
    animation: fadeIn 0.8s ease-out;
}

.animate-slide-up {
    animation: slideUp 0.8s ease-out 0.2s both;
}

.animate-slide-in {
    animation: slideIn 0.8s ease-out 0.4s both;
}

.animate-scale-in {
    animation: scaleIn 0.8s ease-out 0.6s both;
}

.animate-fade-in-up {
    animation: fadeInUp 0.8s ease-out 0.8s both;
}

.animate-bounce-in {
    animation: bounceIn 0.8s ease-out;
}

.animate-product-in {
    animation: productIn 0.6s ease-out both;
}

.animate-spec-item {
    animation: specIn 0.5s ease-out both;
}

.animate-dialog {
    animation: scaleIn 0.3s ease-out;
}

.animate-form-section {
    animation: slideUp 0.5s ease-out both;
}

.title-char {
    display: inline-block;
    animation: bounce 0.6s ease-out both;
}

.pulse-icon {
    animation: pulse 2s infinite;
}

.bounce-icon {
    animation: bounce 2s infinite;
}

.float-animation {
    animation: float 3s ease-in-out infinite;
}

.counter-animation {
    animation: counterUp 0.8s ease-out;
}

.price-glow {
    animation: priceGlow 2s infinite;
}

.hover-scale:hover {
    transform: scale(1.1);
    transition: transform 0.3s ease;
}

.wobble-on-hover:hover {
    animation: wobble 0.5s ease-in-out;
}

.shake-on-hover:hover {
    animation: shake 0.5s ease-in-out;
}

.pulse-on-hover:hover {
    animation: pulse 0.8s ease-in-out;
}

.bounce-on-hover:hover {
    animation: bounce 0.8s ease-in-out;
}

/* 主容器 */
.product-management-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 20px;
    background: #f8f9fa;
    min-height: 100vh;
}

/* 页面头部 */
.page-header {
    margin-bottom: 30px;
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
}

.header-icon {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.3);
}

.header-text .page-title {
    margin: 0 0 8px 0;
    font-size: 32px;
    color: #2c3e50;
    font-weight: 600;
}

.page-subtitle {
    margin: 0;
    color: #7f8c8d;
    font-size: 16px;
}

.header-right {
    display: flex;
    gap: 20px;
}

.stats-card {
    background: white;
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transition: transform 0.3s ease;
}

.stats-card:hover {
    transform: translateY(-5px);
}

.stat-item {
    display: flex;
    align-items: center;
    gap: 12px;
}

.stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
}

.stat-number {
    font-size: 24px;
    font-weight: bold;
    color: #2c3e50;
}

.stat-label {
    font-size: 14px;
    color: #7f8c8d;
}

.action-bar {
    text-align: center;
}

.add-product-btn {
    padding: 16px 32px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.3);
    transition: all 0.3s ease;
}

.add-product-btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(78, 205, 196, 0.4);
}

/* 商品列表 */
.product-list {
    border-radius: 20px;
    border: none;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.empty-state {
    text-align: center;
    padding: 80px 20px;
    color: #7f8c8d;
}

.empty-icon {
    color: #d1d5db;
    margin-bottom: 24px;
}

.empty-state h3 {
    margin: 0 0 12px 0;
    font-size: 24px;
    color: #2c3e50;
}

.empty-state p {
    margin: 0 0 32px 0;
    font-size: 16px;
}

.add-first-btn {
    padding: 12px 24px;
    border-radius: 12px;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 24px;
    padding: 24px;
}

.product-item {
    display: flex;
    flex-direction: column;
    background: white;
    border-radius: 16px;
    overflow: hidden;
    transition: all 0.3s ease;
    position: relative;
    border: 2px solid transparent;
}

.product-item:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
    border-color: #4ecdc4;
}

.product-image-container {
    position: relative;
    aspect-ratio: 1;
    overflow: hidden;
    cursor: pointer;
}

.product-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
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
    background: rgba(78, 205, 196, 0.9);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 600;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.product-item:hover .image-overlay {
    opacity: 1;
}

.stock-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    background: linear-gradient(135deg, #38a169 0%, #4fd1c7 100%);
    color: white;
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.stock-badge.low-stock {
    background: linear-gradient(135deg, #f56565 0%, #ff8a80 100%);
    animation: pulse 2s infinite;
}

.image-shine {
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transform: rotate(45deg);
    opacity: 0;
    transition: opacity 0.3s ease;
}

.product-item:hover .image-shine {
    opacity: 1;
    animation: shimmer 1.5s ease-out;
}

.image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #a0aec0;
    background: #f7fafc;
    gap: 8px;
    font-size: 14px;
}

.product-info {
    padding: 20px;
    background: #f8fafc;
}

.product-name {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 8px;
    color: #2c3e50;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-price {
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 8px;
    color: #f56c6c;
}

.product-stockpile {
    font-size: 14px;
    color: #38a169;
    margin-bottom: 16px;
}

.low-stock-text {
    color: #f56565;
    font-weight: 600;
}

.product-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.action-btn {
    flex: 1;
    min-width: 70px;
    border-radius: 8px;
    font-size: 12px;
    padding: 8px 12px;
    transition: all 0.3s ease;
}

.edit-btn {
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
}

.delete-btn:hover {
    transform: scale(1.05);
}

.stock-btn {
    background: linear-gradient(135deg, #f6ad55 0%, #ed8936 100%);
    border: none;
    color: white;
}

.card-glow {
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    background: linear-gradient(45deg, #4ecdc4, #44a08d, #4ecdc4);
    border-radius: 18px;
    opacity: 0;
    z-index: -1;
    animation: cardGlow 3s infinite;
}

.product-item:hover .card-glow {
    opacity: 0.6;
}

/* 对话框样式 */
.product-dialog {
    border-radius: 20px;
}

.product-dialog .el-dialog__header {
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
    padding: 24px 30px;
    margin: 0;
}

.product-dialog .el-dialog__title {
    font-size: 20px;
    font-weight: 600;
}

.product-dialog .el-dialog__headerbtn .el-dialog__close {
    color: white;
    font-size: 20px;
}

.dialog-content {
    max-height: 70vh;
    overflow-y: auto;
    padding: 0;
}

.product-form {
    padding: 30px;
}

.form-section {
    margin-bottom: 40px;
    padding: 24px;
    background: #f8fafc;
    border-radius: 16px;
    border: 1px solid #e2e8f0;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0 0 20px 0;
    font-size: 18px;
    color: #2c3e50;
    font-weight: 600;
}

.section-title .el-icon {
    color: #4ecdc4;
}

.form-item {
    margin-bottom: 20px;
}

.form-input,
.form-number,
.form-textarea {
    border-radius: 8px;
    transition: all 0.3s ease;
}

.form-input:hover,
.form-number:hover,
.form-textarea:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 上传区域 */
.upload-container {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.upload-area {
    position: relative;
    width: 200px;
    height: 200px;
    border: 2px dashed #d1d5db;
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s ease;
    cursor: pointer;
}

.upload-area:hover {
    border-color: #4ecdc4;
    box-shadow: 0 4px 12px rgba(78, 205, 196, 0.2);
}

.upload-area.has-image {
    border-style: solid;
    border-color: #4ecdc4;
}

.uploaded-image {
    width: 100%;
    height: 100%;
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #9ca3af;
}

.upload-icon {
    margin-bottom: 8px;
    color: #4ecdc4;
}

.upload-text {
    font-size: 14px;
    font-weight: 500;
}

.upload-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(78, 205, 196, 0.9);
    color: white;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    opacity: 0;
    transition: opacity 0.3s ease;
    font-size: 14px;
    font-weight: 500;
}

.upload-area:hover .upload-overlay {
    opacity: 1;
}

.upload-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #6b7280;
}

/* 规格管理 */
.specifications-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.spec-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: white;
    border-radius: 12px;
    border: 1px solid #e2e8f0;
    transition: all 0.3s ease;
}

.spec-item:hover {
    border-color: #4ecdc4;
    box-shadow: 0 2px 8px rgba(78, 205, 196, 0.1);
}

.spec-inputs {
    display: flex;
    gap: 12px;
    flex: 1;
}

.spec-input {
    flex: 1;
}

.remove-spec-btn {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    transition: all 0.3s ease;
}

.add-spec-btn {
    padding: 12px 24px;
    border-radius: 10px;
    border: 2px dashed #4ecdc4;
    background: transparent;
    color: #4ecdc4;
    transition: all 0.3s ease;
}

.add-spec-btn:hover {
    background: #4ecdc4;
    color: white;
    border-style: solid;
}

/* 对话框底部 */
.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 24px 30px;
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;
}

.cancel-btn {
    padding: 12px 24px;
    border-radius: 8px;
}

.submit-btn {
    padding: 12px 24px;
    border-radius: 8px;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    font-weight: 600;
    transition: all 0.3s ease;
}

.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(78, 205, 196, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .product-management-container {
        padding: 16px;
    }

    .header-content {
        flex-direction: column;
        gap: 20px;
        text-align: center;
    }

    .header-left {
        flex-direction: column;
        text-align: center;
    }

    .page-title {
        font-size: 24px;
    }

    .product-grid {
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 16px;
        padding: 16px;
    }

    .product-actions {
        flex-direction: column;
    }

    .action-btn {
        min-width: auto;
    }

    .spec-inputs {
        flex-direction: column;
    }

    .spec-item {
        flex-direction: column;
        align-items: stretch;
    }

    .remove-spec-btn {
        align-self: center;
    }
}

@media (max-width: 480px) {
    .product-grid {
        grid-template-columns: 1fr;
    }

    .upload-area {
        width: 150px;
        height: 150px;
    }

    .dialog-content {
        max-height: 60vh;
    }
}
</style>