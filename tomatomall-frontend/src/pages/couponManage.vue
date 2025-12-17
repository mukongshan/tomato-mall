<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import {
    getAllCoupons,
    createCoupon,
    updateCoupon,
    deleteCoupon,
    CouponVO
} from '@/api/coupon';
import {
    ElMessage,
    ElMessageBox,
    ElLoading
} from 'element-plus';

import {
    Edit, Delete, Plus, Ticket, Calendar, Warning, Check,
    Money, Discount, TrendCharts, Present
} from '@element-plus/icons-vue';

// 当前优惠券列表
const couponList = ref<CouponVO[]>([]);

// 统一表单数据  新建无id 编辑有id
const editForm = ref<CouponVO>({
    id: 0,
    name: '',
    description: '',
    discountType: 1, // 1: 百分比折扣, 2: 固定金额折扣
    discountValue: 0,
    startTime: '',
    endTime: '',
    quantity: 0,
    usedQuantity: 0,
    isValid: 1 // 默认值，后端会自动判断
});

// 当前操作模式 ('create' | 'edit')
const formMode = ref<'create' | 'edit'>('create');
const showFormModal = ref(false);
const loading = ref(false);

// 折扣类型选项
const discountTypeOptions = [
    {
        label: '百分比折扣',
        value: 1,
        icon: TrendCharts,
        color: '#67c23a',
        description: '按比例减免金额'
    },
    {
        label: '固定金额折扣',
        value: 2,
        icon: Money,
        color: '#409eff',
        description: '减免固定金额'
    }
];

// 分离有效和无效优惠券
const validCoupons = computed(() => {
    return couponList.value.filter(coupon => coupon.isValid === 1);
});

const invalidCoupons = computed(() => {
    return couponList.value.filter(coupon => coupon.isValid === 0);
});

// 统计信息
const couponStats = computed(() => {
    const total = couponList.value.length;
    const valid = validCoupons.value.length;
    const invalid = invalidCoupons.value.length;
    const totalQuantity = couponList.value.reduce((sum, coupon) => sum + coupon.quantity, 0);
    const usedQuantity = couponList.value.reduce((sum, coupon) => sum + coupon.usedQuantity, 0);

    return {
        total,
        valid,
        invalid,
        totalQuantity,
        usedQuantity,
        remainingQuantity: totalQuantity - usedQuantity
    };
});

// 计算剩余数量
const getRemainingQuantity = (coupon: CouponVO) => {
    return coupon.quantity - coupon.usedQuantity;
};

// 获取折扣类型标签
const getDiscountTypeTag = (type: number) => {
    return type === 1 ? 'success' : 'primary';
};

// 获取折扣类型文本
const getDiscountTypeText = (type: number) => {
    return type === 1 ? '百分比折扣' : '固定金额折扣';
};

// 格式化折扣值显示 - 支持小数点的"几折"显示
const formatDiscountValue = (type: number, value: number) => {
    if (type === 1) {
        // 百分比折扣：后端 0.1 -> 前端显示为"几折"
        const discount = Math.round(value);
        const fold = discount / 10;

        // 处理特殊情况
        if (discount >= 100) {
            return '免费';
        } else if (fold <= 0) {
            return '原价';
        } else if (fold % 1 === 0) {
            // 整数折扣
            return `${fold}折`;
        } else {
            // 小数折扣，保留一位小数
            return `${fold.toFixed(1)}折`;
        }
    } else {
        // 固定金额折扣：直接显示
        return `¥${value}`;
    }
};

// 检查优惠券是否已过期（前端显示用）
const isExpired = (endTime: string) => {
    return new Date(endTime) < new Date();
};

// 检查优惠券是否已售罄
const isSoldOut = (coupon: CouponVO) => {
    return coupon.usedQuantity >= coupon.quantity;
};

// 获取优惠券状态描述
const getCouponStatusDescription = (coupon: CouponVO) => {
    if (coupon.isValid === 0) {
        if (isExpired(coupon.endTime)) {
            return '已过期';
        }
        if (isSoldOut(coupon)) {
            return '已售罄';
        }
        return '已停用';
    }

    if (isExpired(coupon.endTime)) {
        return '即将失效';
    }
    if (isSoldOut(coupon)) {
        return '库存不足';
    }
    return '正常';
};

// 处理API响应消息
const handleApiResponse = (response: any, fallbackMessage: string) => {
    // 获取后端返回的消息
    const message = response?.data?.data || response?.data?.msg || fallbackMessage;
    return message;
};

// 数据转换函数：从后端数据转换为前端表单数据
const convertFromBackend = (coupon: CouponVO): CouponVO => {
    return {
        ...coupon,
        // 百分比折扣：后端 0.1 -> 前端 10
        discountValue: coupon.discountType === 1 ? coupon.discountValue * 100 : coupon.discountValue
    };
};

// 数据转换函数：从前端表单数据转换为后端数据
const convertToBackend = (coupon: CouponVO): CouponVO => {
    return {
        ...coupon,
        // 百分比折扣：前端 10 -> 后端 0.1
        discountValue: coupon.discountType === 1 ? coupon.discountValue / 100 : coupon.discountValue
    };
};

// 加载优惠券列表
const loadCoupons = async () => {
    try {
        loading.value = true;
        const response = await getAllCoupons();
        const rawData = response.data.data || response.data;
        // 转换后端数据为前端显示格式
        couponList.value = rawData.map((coupon: CouponVO) => convertFromBackend(coupon));
        console.log('转换后的优惠券列表:', couponList.value);
    } catch (error: any) {
        console.error('加载优惠券失败:', error);
        const errorMessage = error?.response?.data?.data ||
            error?.response?.data?.msg ||
            '加载优惠券失败，请稍后重试';
        ElMessage.error({
            message: errorMessage,
            duration: 1000
        });
    } finally {
        loading.value = false;
    }
};

// 初始化加载数据
onMounted(() => {
    loadCoupons();
});

// 打开创建优惠券模态框
const openCreateModal = () => {
    resetFormData();
    formMode.value = 'create';
    showFormModal.value = true;
};

// 打开编辑优惠券模态框
const openEditModal = (coupon: CouponVO) => {
    // 编辑时数据已经是前端格式，直接复制
    editForm.value = { ...coupon };
    formMode.value = 'edit';
    showFormModal.value = true;
};

// 重置表单数据
const resetFormData = () => {
    editForm.value = {
        id: 0,
        name: '',
        description: '',
        discountType: 1,
        discountValue: 0, // 前端格式：百分比折扣为 1-100，固定金额为实际金额
        startTime: '',
        endTime: '',
        quantity: 0,
        usedQuantity: 0,
        isValid: 1 // 设为默认值，后端会自动判断
    };
};

// 验证优惠券数据
const validateCoupon = (coupon: CouponVO): boolean => {
    if (!coupon.name.trim()) {
        ElMessage.warning({
            message: '优惠券名称不能为空',
            duration: 1000
        });
        return false;
    }
    if (!coupon.description.trim()) {
        ElMessage.warning({
            message: '优惠券描述不能为空',
            duration: 1000
        });
        return false;
    }
    if (coupon.discountValue <= 0) {
        ElMessage.warning({
            message: '折扣值必须大于0',
            duration: 1000
        });
        return false;
    }
    if (coupon.discountType === 1 && coupon.discountValue > 100) {
        ElMessage.warning({
            message: '百分比折扣不能超过100%',
            duration: 1000
        });
        return false;
    }
    if (coupon.discountType === 1 && coupon.discountValue < 1) {
        ElMessage.warning({
            message: '百分比折扣不能小于1%',
            duration: 1000
        });
        return false;
    }
    if (!coupon.startTime || !coupon.endTime) {
        ElMessage.warning({
            message: '请选择有效的开始和结束时间',
            duration: 1000
        });
        return false;
    }
    if (new Date(coupon.startTime) >= new Date(coupon.endTime)) {
        ElMessage.warning({
            message: '开始时间必须早于结束时间',
            duration: 1000
        });
        return false;
    }
    if (coupon.quantity <= 0) {
        ElMessage.warning({
            message: '优惠券数量必须大于0',
            duration: 1000
        });
        return false;
    }
    return true;
};

// 提交表单 (根据模式创建或更新)
const submitForm = async () => {
    if (!validateCoupon(editForm.value)) return;

    const loadingInstance = ElLoading.service({
        lock: true,
        text: formMode.value === 'create' ? '正在创建优惠券...' : '正在更新优惠券...',
        background: 'rgba(0, 0, 0, 0.7)'
    });

    try {
        // 转换前端数据为后端格式
        const backendData = convertToBackend({
            ...editForm.value,
            isValid: 1, // 强制设为默认值，让后端判断
            startTime: editForm.value.startTime.replace(' ', 'T'), // 转换为ISO格式
            endTime: editForm.value.endTime.replace(' ', 'T') // 转换为ISO格式
        });

        console.log('发送给后端的数据:', backendData);

        let response;
        if (formMode.value === 'create') {
            const { id, ...dataWithoutId } = backendData;
            response = await createCoupon(dataWithoutId);
            const successMessage = handleApiResponse(response, '优惠券创建成功');
            ElMessage.success({
                message: successMessage,
                duration: 1000
            });
        } else {
            response = await updateCoupon(backendData.id!, backendData);
            const successMessage = handleApiResponse(response, '优惠券更新成功');
            ElMessage.success({
                message: successMessage,
                duration: 1000
            });
        }
        showFormModal.value = false;
        await loadCoupons();
    } catch (error: any) {
        console.error(`${formMode.value === 'create' ? '创建' : '更新'}优惠券失败:`, error);
        const errorMessage = error?.response?.data?.data ||
            error?.response?.data?.msg ||
            `${formMode.value === 'create' ? '创建' : '更新'}优惠券失败，请稍后重试`;
        ElMessage.error({
            message: errorMessage,
            duration: 1000
        });
    } finally {
        loadingInstance.close();
    }
};

// 删除优惠券
const deleteCouponItem = async (id: number) => {
    try {
        await ElMessageBox.confirm(
            '确定要删除这个优惠券吗？此操作不可恢复',
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }
        );

        const loadingInstance = ElLoading.service({
            lock: true,
            text: '正在删除优惠券...',
            background: 'rgba(0, 0, 0, 0.7)'
        });

        try {
            const response = await deleteCoupon(id);
            couponList.value = couponList.value.filter(c => c.id !== id);
            const successMessage = handleApiResponse(response, '优惠券删除成功');
            ElMessage.success({
                message: successMessage,
                duration: 1000
            });
        } catch (error: any) {
            console.error('删除优惠券失败:', error);
            const errorMessage = error?.response?.data?.data ||
                error?.response?.data?.msg ||
                '删除优惠券失败，请稍后重试';
            ElMessage.error({
                message: errorMessage,
                duration: 1000
            });
        } finally {
            loadingInstance.close();
        }
    } catch (cancel) {
        ElMessage.info('已取消删除操作');
    }
};

// 关闭所有模态框
const closeAllModals = () => {
    showFormModal.value = false;
};
</script>

<template>
    <div class="container">
        <!-- 统计信息卡片 -->
        <el-row :gutter="20" class="stats-row">
            <el-col :span="8">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon total">
                            <el-icon>
                                <Ticket />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-number">{{ couponStats.total }}</div>
                            <div class="stats-label">总优惠券</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon valid">
                            <el-icon>
                                <Check />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-number">{{ couponStats.valid }}</div>
                            <div class="stats-label">有效优惠券</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon invalid">
                            <el-icon>
                                <Warning />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-number">{{ couponStats.invalid }}</div>
                            <div class="stats-label">无效优惠券</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 有效优惠券区域 -->
        <el-card shadow="hover" class="section-card">
            <template #header>
                <div class="card-header">
                    <div class="section-title">
                        <el-icon class="section-icon valid-icon">
                            <Check />
                        </el-icon>
                        <h2>有效优惠券 ({{ validCoupons.length }})</h2>
                    </div>
                    <el-button type="primary" @click="openCreateModal" :loading="loading" size="large"
                        class="create-btn">
                        <el-icon>
                            <plus />
                        </el-icon> 新增优惠券
                    </el-button>
                </div>
            </template>

            <el-empty v-if="!loading && validCoupons.length === 0" description="暂无有效优惠券" />

            <el-row v-else :gutter="20">
                <el-col v-for="coupon in validCoupons" :key="`valid-${coupon.id}`" :xs="24" :sm="12" :md="8" :lg="6">
                    <el-card shadow="hover" class="coupon-card valid-coupon">
                        <template #header>
                            <div class="coupon-header">
                                <span class="coupon-title">{{ coupon.name }}</span>
                                <div class="coupon-actions">
                                    <el-button type="primary" size="default" @click="openEditModal(coupon)"
                                        :loading="loading">
                                        <el-icon>
                                            <edit />
                                        </el-icon>
                                    </el-button>
                                    <el-button type="danger" size="default" @click="deleteCouponItem(coupon.id!)"
                                        :loading="loading">
                                        <el-icon>
                                            <delete />
                                        </el-icon>
                                    </el-button>
                                </div>
                            </div>
                        </template>

                        <div class="coupon-content">
                            <!-- 折扣值显示 -->
                            <div class="discount-display valid-discount">
                                <el-icon class="discount-icon">
                                    <Ticket />
                                </el-icon>
                                <span
                                    class="discount-value">{{ formatDiscountValue(coupon.discountType, coupon.discountValue) }}</span>
                            </div>

                            <!-- 描述 -->
                            <p class="coupon-description">{{ coupon.description }}</p>

                            <!-- 时间信息 -->
                            <div class="time-info">
                                <div class="time-item">
                                    <el-icon>
                                        <Calendar />
                                    </el-icon>
                                    <span>{{ new Date(coupon.startTime).toLocaleDateString() }}</span>
                                </div>
                                <span class="time-separator">至</span>
                                <div class="time-item">
                                    <el-icon>
                                        <Calendar />
                                    </el-icon>
                                    <span>{{ new Date(coupon.endTime).toLocaleDateString() }}</span>
                                </div>
                            </div>

                            <!-- 标签信息 -->
                            <div class="coupon-meta">
                                <el-tag size="small" :type="getDiscountTypeTag(coupon.discountType)">
                                    {{ getDiscountTypeText(coupon.discountType) }}
                                </el-tag>
                                <el-tag size="small" type="success">
                                    {{ getCouponStatusDescription(coupon) }}
                                </el-tag>
                                <el-tag size="small" type="info">
                                    剩余: {{ getRemainingQuantity(coupon) }}/{{ coupon.quantity }}
                                </el-tag>
                            </div>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </el-card>

        <!-- 无效优惠券区域 -->
        <el-card shadow="hover" class="section-card" v-if="invalidCoupons.length > 0">
            <template #header>
                <div class="card-header">
                    <div class="section-title">
                        <el-icon class="section-icon invalid-icon">
                            <Warning />
                        </el-icon>
                        <h2>无效优惠券 ({{ invalidCoupons.length }})</h2>
                    </div>
                </div>
            </template>

            <el-row :gutter="20">
                <el-col v-for="coupon in invalidCoupons" :key="`invalid-${coupon.id}`" :xs="24" :sm="12" :md="8"
                    :lg="6">
                    <el-card shadow="hover" class="coupon-card invalid-coupon">
                        <template #header>
                            <div class="coupon-header">
                                <span class="coupon-title">{{ coupon.name }}</span>
                                <div class="coupon-actions">
                                    <el-button type="primary" size="default" @click="openEditModal(coupon)"
                                        :loading="loading">
                                        <el-icon>
                                            <edit />
                                        </el-icon>
                                    </el-button>
                                    <el-button type="danger" size="default" @click="deleteCouponItem(coupon.id!)"
                                        :loading="loading">
                                        <el-icon>
                                            <delete />
                                        </el-icon>
                                    </el-button>
                                </div>
                            </div>
                        </template>

                        <div class="coupon-content">
                            <!-- 折扣值显示 -->
                            <div class="discount-display invalid-discount">
                                <el-icon class="discount-icon">
                                    <Ticket />
                                </el-icon>
                                <span
                                    class="discount-value">{{ formatDiscountValue(coupon.discountType, coupon.discountValue) }}</span>
                            </div>

                            <!-- 描述 -->
                            <p class="coupon-description">{{ coupon.description }}</p>

                            <!-- 时间信息 -->
                            <div class="time-info">
                                <div class="time-item">
                                    <el-icon>
                                        <Calendar />
                                    </el-icon>
                                    <span>{{ new Date(coupon.startTime).toLocaleDateString() }}</span>
                                </div>
                                <span class="time-separator">至</span>
                                <div class="time-item">
                                    <el-icon>
                                        <Calendar />
                                    </el-icon>
                                    <span>{{ new Date(coupon.endTime).toLocaleDateString() }}</span>
                                </div>
                            </div>

                            <!-- 标签信息 -->
                            <div class="coupon-meta">
                                <el-tag size="small" :type="getDiscountTypeTag(coupon.discountType)">
                                    {{ getDiscountTypeText(coupon.discountType) }}
                                </el-tag>
                                <el-tag size="small" type="danger">
                                    {{ getCouponStatusDescription(coupon) }}
                                </el-tag>
                                <el-tag size="small" type="info">
                                    剩余: {{ getRemainingQuantity(coupon) }}/{{ coupon.quantity }}
                                </el-tag>
                            </div>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </el-card>

        <!-- 加载状态 -->
        <el-skeleton v-if="loading && couponList.length === 0" :rows="5" animated />

        <!-- 创建/编辑优惠券对话框 -->
        <el-dialog v-model="showFormModal"
            :title="formMode === 'create' ? '🎫 创建新优惠券' : `✏️ 编辑优惠券 (ID: ${editForm.id})`" width="75%"
            @close="closeAllModals" class="unified-form-dialog" :close-on-click-modal="false" destroy-on-close>

            <!-- 表单内容 -->
            <div class="unified-form-container">
                <!-- 表单头部信息 -->
                <div class="form-header-section">
                    <div class="header-icon">
                        <el-icon size="48">
                            <Present />
                        </el-icon>
                    </div>
                    <div class="header-content">
                        <h2>{{ formMode === 'create' ? '创建新优惠券' : '编辑优惠券信息' }}</h2>
                        <p>{{ formMode === 'create' ? '填写优惠券的详细信息，创建一张新的优惠券' : '修改下方的优惠券信息并保存更改' }}</p>
                    </div>
                </div>

                <!-- 表单主体 -->
                <el-form :model="editForm" label-width="0" class="unified-form" ref="formRef">

                    <!-- 第一行：基本信息 -->
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label">
                                <el-icon>
                                    <Present />
                                </el-icon>
                                <span>优惠券名称</span>
                                <span class="required">*</span>
                            </label>
                            <el-input v-model="editForm.name" placeholder="请输入优惠券名称，如：春节特惠券" size="large"
                                class="form-input" />
                        </div>

                        <div class="form-group">
                            <label class="form-label">
                                <el-icon>
                                    <Discount />
                                </el-icon>
                                <span>折扣类型</span>
                                <span class="required">*</span>
                            </label>
                            <el-select v-model="editForm.discountType" placeholder="请选择折扣类型" size="large"
                                class="form-input">
                                <el-option v-for="option in discountTypeOptions" :key="option.value"
                                    :label="option.label" :value="option.value">
                                    <div class="select-option">
                                        <el-icon :style="{ color: option.color }">
                                            <component :is="option.icon" />
                                        </el-icon>
                                        <div class="option-text">
                                            <div class="option-title">{{ option.label }}</div>
                                            <div class="option-subtitle">{{ option.description }}</div>
                                        </div>
                                    </div>
                                </el-option>
                            </el-select>
                        </div>
                    </div>

                    <!-- 第二行：描述 -->
                    <div class="form-row full-width">
                        <div class="form-group">
                            <label class="form-label">
                                <el-icon>
                                    <Edit />
                                </el-icon>
                                <span>优惠券描述</span>
                                <span class="required">*</span>
                            </label>
                            <el-input v-model="editForm.description" type="textarea" :rows="3"
                                placeholder="请详细描述优惠券的使用说明和适用范围..." class="form-textarea" />
                        </div>
                    </div>

                    <!-- 第三行：折扣设置 -->
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label">
                                <el-icon v-if="editForm.discountType === 1">
                                    <TrendCharts />
                                </el-icon>
                                <el-icon v-else>
                                    <Money />
                                </el-icon>
                                <span>折扣值</span>
                                <span class="required">*</span>
                            </label>
                            <el-input-number v-model="editForm.discountValue" :min="editForm.discountType === 1 ? 1 : 0"
                                :max="editForm.discountType === 1 ? 100 : undefined"
                                :precision="editForm.discountType === 1 ? 0 : 2" controls-position="right" size="large"
                                class="form-input">
                                <template #append>
                                    <span class="input-unit">
                                        {{ editForm.discountType === 1 ? '%' : '元' }}
                                    </span>
                                </template>
                            </el-input-number>
                            <div class="field-tip">
                                {{ editForm.discountType === 1 ? '输入百分比数值 (1-100)' : '输入具体金额 (元)' }}
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label">
                                <el-icon>
                                    <Ticket />
                                </el-icon>
                                <span>发放数量</span>
                                <span class="required">*</span>
                            </label>
                            <el-input-number v-model="editForm.quantity" :min="1" controls-position="right" size="large"
                                class="form-input">
                                <template #append>
                                    <span class="input-unit">张</span>
                                </template>
                            </el-input-number>
                            <div class="field-tip">
                                设置优惠券的发放总数量
                            </div>
                        </div>
                    </div>

                    <!-- 第四行：时间设置 -->
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label">
                                <el-icon>
                                    <Calendar />
                                </el-icon>
                                <span>开始时间</span>
                                <span class="required">*</span>
                            </label>
                            <el-date-picker v-model="editForm.startTime" type="datetime" placeholder="选择优惠券生效时间"
                                size="large" class="form-input" format="YYYY-MM-DD HH:mm:ss"
                                value-format="YYYY-MM-DD HH:mm:ss" />
                        </div>

                        <div class="form-group">
                            <label class="form-label">
                                <el-icon>
                                    <Calendar />
                                </el-icon>
                                <span>结束时间</span>
                                <span class="required">*</span>
                            </label>
                            <el-date-picker v-model="editForm.endTime" type="datetime" placeholder="选择优惠券失效时间"
                                size="large" class="form-input" format="YYYY-MM-DD HH:mm:ss"
                                value-format="YYYY-MM-DD HH:mm:ss" />
                        </div>
                    </div>

                    <!-- 使用情况（仅编辑时显示） -->
                    <div v-if="formMode === 'edit'" class="usage-section">
                        <div class="usage-header">
                            <el-icon>
                                <Check />
                            </el-icon>
                            <span>使用情况统计</span>
                        </div>
                        <div class="usage-stats">
                            <div class="stat-item">
                                <div class="stat-label">已使用</div>
                                <div class="stat-value used">{{ editForm.usedQuantity }}</div>
                                <div class="stat-unit">张</div>
                            </div>
                            <div class="stat-divider"></div>
                            <div class="stat-item">
                                <div class="stat-label">剩余</div>
                                <div class="stat-value remaining">{{ editForm.quantity - editForm.usedQuantity }}</div>
                                <div class="stat-unit">张</div>
                            </div>
                            <div class="stat-divider"></div>
                            <div class="stat-item">
                                <div class="stat-label">使用率</div>
                                <div class="stat-value rate">
                                    {{ editForm.quantity > 0 ? Math.round((editForm.usedQuantity / editForm.quantity) * 100) : 0 }}
                                </div>
                                <div class="stat-unit">%</div>
                            </div>
                        </div>
                    </div>

                    <!-- 调试信息（开发环境可以显示） -->
                    <div v-if="formMode === 'edit'" class="debug-section"
                        style="margin-top: 20px; padding: 16px; background: #f8f9fa; border-radius: 8px; font-size: 12px; color: #666;">
                        <strong>调试信息：</strong><br>
                        前端显示值: {{ editForm.discountValue }}{{ editForm.discountType === 1 ? '%' : '元' }}<br>
                        将发送给后端:
                        {{ editForm.discountType === 1 ? (editForm.discountValue / 100) : editForm.discountValue }}
                    </div>
                </el-form>
            </div>

            <template #footer>
                <div class="unified-footer">
                    <el-button @click="closeAllModals" size="large" class="cancel-button">
                        取消
                    </el-button>
                    <el-button type="primary" @click="submitForm" :loading="loading" size="large" class="submit-button">
                        <el-icon>
                            <Check />
                        </el-icon>
                        {{ formMode === 'create' ? '创建优惠券' : '保存修改' }}
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
/* 原有样式保持不变 */
.container {
    padding: 20px;
}

/* 统计卡片样式 */
.stats-row {
    margin-bottom: 20px;
}

.stats-card {
    border-radius: 8px;
    transition: all 0.3s;
}

.stats-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stats-content {
    display: flex;
    align-items: center;
    padding: 10px;
}

.stats-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15px;
    font-size: 24px;
    color: white;
}

.stats-icon.total {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.valid {
    background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
}

.stats-icon.invalid {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stats-info {
    flex: 1;
}

.stats-number {
    font-size: 28px;
    font-weight: bold;
    color: #303133;
    line-height: 1;
}

.stats-label {
    font-size: 14px;
    color: #909399;
    margin-top: 5px;
}

/* 区域卡片样式 */
.section-card {
    margin-bottom: 30px;
    border-radius: 12px;
    overflow: hidden;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.section-title {
    display: flex;
    align-items: center;
}

.section-icon {
    font-size: 24px;
    margin-right: 10px;
}

.valid-icon {
    color: #67c23a;
}

.invalid-icon {
    color: #f56c6c;
}

.section-title h2 {
    margin: 0;
    font-size: 20px;
    color: #303133;
}

.create-btn {
    border-radius: 8px;
    padding: 12px 24px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    transition: all 0.3s;
}

.create-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* 优惠券卡片样式 */
.coupon-card {
    margin-bottom: 20px;
    border-radius: 12px;
    transition: all 0.3s;
    height: 100%;
    overflow: hidden;
}

.valid-coupon {
    border-left: 4px solid #67c23a;
}

.invalid-coupon {
    border-left: 4px solid #f56c6c;
    opacity: 0.8;
}

.coupon-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.coupon-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.coupon-title {
    font-weight: bold;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.valid-coupon .coupon-title {
    color: #67c23a;
}

.invalid-coupon .coupon-title {
    color: #f56c6c;
}

.coupon-content {
    padding: 0 10px;
}

.discount-display {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 15px;
    padding: 15px;
    border-radius: 8px;
    color: white;
}

.valid-discount {
    background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.invalid-discount {
    background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.discount-icon {
    font-size: 24px;
    margin-right: 10px;
}

.discount-value {
    font-size: 28px;
    font-weight: bold;
}

.coupon-description {
    color: #606266;
    font-size: 14px;
    line-height: 1.5;
    margin-bottom: 15px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}

.time-info {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 15px;
    font-size: 12px;
    color: #909399;
}

.time-item {
    display: flex;
    align-items: center;
    gap: 4px;
}

.time-separator {
    margin: 0 10px;
}

.coupon-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 10px;
}

/* 统一表单样式 */
.unified-form-dialog {
    border-radius: 16px;
    overflow: hidden;
}

.unified-form-dialog :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px 30px;
    margin: 0;
}

.unified-form-dialog :deep(.el-dialog__title) {
    font-size: 20px;
    font-weight: 600;
}

.unified-form-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: white;
    font-size: 20px;
}

.unified-form-dialog :deep(.el-dialog__body) {
    padding: 0;
}

.unified-form-container {
    background: #ffffff;
}

.form-header-section {
    display: flex;
    align-items: center;
    padding: 30px 40px;
    background: linear-gradient(135deg, #f8faff 0%, #e8f4f8 100%);
    border-bottom: 1px solid #e4e7ed;
}

.header-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    margin-right: 24px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.header-content h2 {
    margin: 0 0 8px 0;
    font-size: 24px;
    color: #303133;
    font-weight: 600;
}

.header-content p {
    margin: 0;
    color: #606266;
    font-size: 14px;
    line-height: 1.5;
}

.unified-form {
    padding: 40px;
}

.form-row {
    display: flex;
    gap: 32px;
    margin-bottom: 32px;
}

.form-row.full-width {
    flex-direction: column;
}

.form-group {
    flex: 1;
    min-width: 0;
}

.form-label {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    font-size: 15px;
    color: #303133;
    font-weight: 500;
    gap: 8px;
}

.form-label .el-icon {
    color: #409eff;
    font-size: 16px;
}

.required {
    color: #f56c6c;
    font-weight: 600;
}

.form-input,
.form-textarea {
    width: 100%;
}

.form-input :deep(.el-input__wrapper),
.form-input :deep(.el-select__wrapper),
.form-input :deep(.el-input-number),
.form-textarea :deep(.el-textarea__inner) {
    border-radius: 8px;
    border: 2px solid #e4e7ed;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.form-input :deep(.el-input__wrapper):hover,
.form-input :deep(.el-select__wrapper):hover,
.form-input :deep(.el-input-number):hover,
.form-textarea :deep(.el-textarea__inner):hover {
    border-color: #c6d9f7;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.form-input :deep(.el-input__wrapper.is-focus),
.form-input :deep(.el-select__wrapper.is-focused),
.form-input :deep(.el-input-number.is-focus),
.form-textarea :deep(.el-textarea__inner):focus {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.select-option {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 0;
}

.option-text {
    flex: 1;
}

.option-title {
    font-weight: 500;
    color: #303133;
}

.option-subtitle {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
}

.input-unit {
    color: #409eff;
    font-weight: 500;
    padding: 0 12px;
    background: #f0f9ff;
    border-left: 1px solid #e4e7ed;
}

.field-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
    line-height: 1.4;
}

.usage-section {
    margin-top: 40px;
    padding: 24px;
    background: linear-gradient(135deg, #f8faff 0%, #e8f4f8 100%);
    border-radius: 12px;
    border: 1px solid #e4e7ed;
}

.usage-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    gap: 8px;
}

.usage-header .el-icon {
    color: #409eff;
    font-size: 18px;
}

.usage-stats {
    display: flex;
    align-items: center;
    gap: 24px;
}

.stat-item {
    flex: 1;
    text-align: center;
    padding: 16px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-label {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
}

.stat-value {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 4px;
}

.stat-value.used {
    color: #f56c6c;
}

.stat-value.remaining {
    color: #67c23a;
}

.stat-value.rate {
    color: #409eff;
}

.stat-unit {
    font-size: 12px;
    color: #909399;
}

.stat-divider {
    width: 1px;
    height: 40px;
    background: #e4e7ed;
}

.unified-footer {
    display: flex;
    justify-content: flex-end;
    gap: 16px;
    padding: 24px 40px;
    background: #fafbfc;
    border-top: 1px solid #e4e7ed;
}

.cancel-button {
    padding: 12px 32px;
    border-radius: 8px;
    font-weight: 500;
}

.submit-button {
    padding: 12px 32px;
    border-radius: 8px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    transition: all 0.3s ease;
}

.submit-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.debug-section {
    margin-top: 20px;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 8px;
    font-size: 12px;
    color: #666;
    border: 1px solid #e9ecef;
}
</style>