<script setup lang="ts">
import { ref, onMounted } from 'vue';
import {
    getAdvertisements,
    addAdvertisement,
    updateAdvertisement as updateAdAPI,
    deleteAdvertisement as deleteAdAPI,
    Advertisement,
    AdvertisementUpdate
} from '@/api/advertisements';
import {
    ElMessage,
    ElMessageBox,
    ElLoading
} from 'element-plus';

// 当前广告列表
const advertisementList = ref<AdvertisementUpdate[]>([]);

// 当前编辑的广告
const currentAdvertisement = ref<AdvertisementUpdate>({
    id: 0,
    title: '',
    content: '',
    image_url: '',
    product_id: 0
});

// 新建广告表单
const newAdvertisement = ref<Advertisement>({
    title: '',
    content: '',
    image_url: '',
    product_id: 0
});

const showCreateModal = ref(false);
const showEditModal = ref(false);
const loading = ref(false);

// 加载广告列表
const loadAdvertisements = async () => {
    try {
        loading.value = true;
        const response = await getAdvertisements();
        advertisementList.value = response.data;
        ElMessage.success('广告列表加载成功');
    } catch (error) {
        console.error('加载广告失败:', error);
        ElMessage.error('加载广告失败，请稍后重试');
    } finally {
        loading.value = false;
    }
};

// 初始化加载数据
onMounted(() => {
    loadAdvertisements();
});

// 打开创建广告模态框
const openCreateModal = () => {
    resetNewAdvertisement();
    showCreateModal.value = true;
};

// 打开编辑广告模态框
const openEditModal = (ad: AdvertisementUpdate) => {
    // 拷贝广告数据，避免直接修改原数据
    currentAdvertisement.value = { ...ad };
    showEditModal.value = true;
};

// 重置新建广告表单
const resetNewAdvertisement = () => {
    newAdvertisement.value = {
        title: '',
        content: '',
        image_url: '',
        product_id: 0
    };
};

// 验证广告数据
const validateAdvertisement = (ad: Advertisement): boolean => {
    if (!ad.title.trim()) {
        ElMessage.warning('广告标题不能为空');
        return false;
    }
    if (ad.product_id <= 0) {
        ElMessage.warning('商品ID必须大于0');
        return false;
    }
    return true;
};

// 创建广告
const createAdvertisement = async () => {
    if (!validateAdvertisement(newAdvertisement.value)) return;

    const loadingInstance = ElLoading.service({
        lock: true,
        text: '正在创建广告...',
        background: 'rgba(0, 0, 0, 0.7)'
    });

    try {
        const response = await addAdvertisement(newAdvertisement.value);
        advertisementList.value.push(response.data);
        showCreateModal.value = false;
        resetNewAdvertisement();
        ElMessage.success('广告创建成功');
    } catch (error) {
        console.error('创建广告失败:', error);
        ElMessage.error('创建广告失败，请稍后重试');
    } finally {
        loadingInstance.close();
    }
};

// 更新广告
const updateAdvertisement = async () => {
    if (!validateAdvertisement(currentAdvertisement.value)) return;

    const loadingInstance = ElLoading.service({
        lock: true,
        text: '正在更新广告...',
        background: 'rgba(0, 0, 0, 0.7)'
    });

    try {
        await updateAdAPI(currentAdvertisement.value);
        const index = advertisementList.value.findIndex(
            a => a.id === currentAdvertisement.value.id
        );
        if (index !== -1) {
            advertisementList.value[index] = { ...currentAdvertisement.value };
        }
        showEditModal.value = false;
        ElMessage.success('广告更新成功');
    } catch (error) {
        console.error('更新广告失败:', error);
        ElMessage.error('更新广告失败，请稍后重试');
    } finally {
        loadingInstance.close();
    }
};

// 删除广告
const deleteAdvertisement = async (id: number) => {
    try {
        await ElMessageBox.confirm(
            '确定要删除这个广告吗？此操作不可恢复',
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }
        );

        const loadingInstance = ElLoading.service({
            lock: true,
            text: '正在删除广告...',
            background: 'rgba(0, 0, 0, 0.7)'
        });

        try {
            await deleteAdAPI(id);
            advertisementList.value = advertisementList.value.filter(a => a.id !== id);
            ElMessage.success('广告删除成功');
        } catch (error) {
            console.error('删除广告失败:', error);
            ElMessage.error('删除广告失败，请稍后重试');
        } finally {
            loadingInstance.close();
        }
    } catch (cancel) {
        ElMessage.info('已取消删除操作');
    }
};

// 关闭所有模态框
const closeAllModals = () => {
    showCreateModal.value = false;
    showEditModal.value = false;
};
</script>

<template>
    <div class="container">
        <el-card shadow="hover">
            <template #header>
                <div class="card-header">
                    <h1>广告管理</h1>
                    <el-button type="primary" @click="openCreateModal" :loading="loading">
                        <el-icon>
                            <plus />
                        </el-icon> 新增广告
                    </el-button>
                </div>
            </template>

            <el-empty v-if="!loading && advertisementList.length === 0" description="暂无广告数据" />

            <el-skeleton v-else-if="loading && advertisementList.length === 0" :rows="5" animated />

            <el-row v-else :gutter="20">
                <el-col v-for="ad in advertisementList" :key="ad.id" :xs="24" :sm="12" :md="8" :lg="6">
                    <el-card shadow="hover" class="ad-card">
                        <template #header>
                            <div class="ad-header">
                                <span class="ad-title">{{ ad.title }}</span>
                                <div class="ad-actions">
                                    <el-button type="primary" size="small" @click="openEditModal(ad)"
                                        :loading="loading">
                                        <el-icon>
                                            <edit />
                                        </el-icon>
                                    </el-button>
                                    <el-button type="danger" size="small" @click="deleteAdvertisement(ad.id)"
                                        :loading="loading">
                                        <el-icon>
                                            <delete />
                                        </el-icon>
                                    </el-button>
                                </div>
                            </div>
                        </template>

                        <div class="ad-image-container">
                            <el-image v-if="ad.image_url" :src="ad.image_url" :alt="ad.title" fit="cover"
                                class="ad-image">
                                <template #error>
                                    <div class="image-error">
                                        <el-icon>
                                            <picture />
                                        </el-icon>
                                        <span>图片加载失败</span>
                                    </div>
                                </template>
                            </el-image>
                            <div v-else class="ad-image-placeholder">
                                <el-icon>
                                    <picture />
                                </el-icon>
                                <span>暂无图片</span>
                            </div>
                        </div>

                        <div class="ad-content">
                            <p class="ad-description">{{ ad.content }}</p>
                            <div class="ad-meta">
                                <el-tag size="small">商品ID: {{ ad.product_id }}</el-tag>
                                <el-tag size="small" type="info">广告ID: {{ ad.id }}</el-tag>
                            </div>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </el-card>

        <!-- 创建广告模态框 -->
        <el-dialog v-model="showCreateModal" title="创建新广告" width="50%" @close="closeAllModals">
            <el-form :model="newAdvertisement" label-width="100px" label-position="top">
                <el-form-item label="标题" prop="title" required>
                    <el-input v-model="newAdvertisement.title" placeholder="请输入广告标题" />
                </el-form-item>
                <el-form-item label="内容" prop="content">
                    <el-input v-model="newAdvertisement.content" type="textarea" :rows="4" placeholder="请输入广告内容" />
                </el-form-item>
                <el-form-item label="图片URL" prop="image_url">
                    <el-input v-model="newAdvertisement.image_url" placeholder="https://example.com/image.jpg" />
                </el-form-item>
                <el-form-item label="商品ID" prop="product_id" required>
                    <el-input-number v-model="newAdvertisement.product_id" :min="1" controls-position="right" />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="closeAllModals">取消</el-button>
                    <el-button type="primary" @click="createAdvertisement" :loading="loading">
                        提交
                    </el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 编辑广告模态框 -->
        <el-dialog v-model="showEditModal" :title="`编辑广告 (ID: ${currentAdvertisement.id})`" width="50%"
            @close="closeAllModals">
            <el-form :model="currentAdvertisement" label-width="100px" label-position="top">
                <el-form-item label="标题" prop="title" required>
                    <el-input v-model="currentAdvertisement.title" placeholder="请输入广告标题" />
                </el-form-item>
                <el-form-item label="内容" prop="content">
                    <el-input v-model="currentAdvertisement.content" type="textarea" :rows="4" placeholder="请输入广告内容" />
                </el-form-item>
                <el-form-item label="图片URL" prop="image_url">
                    <el-input v-model="currentAdvertisement.image_url" placeholder="https://example.com/image.jpg" />
                </el-form-item>
                <el-form-item label="商品ID" prop="product_id" required>
                    <el-input-number v-model="currentAdvertisement.product_id" :min="1" controls-position="right" />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="closeAllModals">取消</el-button>
                    <el-button type="primary" @click="updateAdvertisement" :loading="loading">
                        更新
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.container {
    padding: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.ad-card {
    margin-bottom: 20px;
    transition: all 0.3s;
}

.ad-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.ad-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.ad-title {
    font-weight: bold;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.ad-image-container {
    height: 200px;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    border-radius: 4px;
    overflow: hidden;
}

.ad-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.ad-image-placeholder,
.image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #909399;
}

.ad-image-placeholder .el-icon,
.image-error .el-icon {
    font-size: 40px;
    margin-bottom: 10px;
}

.ad-content {
    padding: 0 10px;
}

.ad-description {
    color: #606266;
    font-size: 14px;
    line-height: 1.5;
    margin-bottom: 15px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}

.ad-meta {
    display: flex;
    gap: 8px;
    margin-top: 10px;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
}
</style>