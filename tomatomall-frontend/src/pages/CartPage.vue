<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Cart, CartItem, deleteCartProduct, updateCartItem } from "@/api/cart.js";
import { getCartProducts } from "@/api/cart.js";
import { getProduct, getStockpile, Product, Stockpile } from "@/api/product.ts";
import { checkout, OrderInfo } from "@/api/order.ts";
import { getCouponsByAccountId, getCouponById, CouponVO, AccountCouponsRelationVO } from "@/api/coupon.ts";
import router from "../router/index.ts";
import { getUserDetails } from "@/api/account.ts";
import { Picture, Ticket, Present, ShoppingBag, Delete, Edit, Check, Warning, Clock } from '@element-plus/icons-vue';

// 购物车数据
const cart = ref<Cart>()
const cartItems = ref<Array<CartItem & { selected: boolean }>>([]);
const products = ref<Product[]>([]);
const stockpiles = ref<Stockpile[]>([]);
const loading = ref(false)
const selectedItems = ref<Array<CartItem & { selected: boolean }>>([]);
const showConfirmDialog = ref(false)
const orderInfo = ref<OrderInfo>({
    name: '',
    location: '',
    telephone: '',
    email: '',
})

// 优惠券相关数据
const allUserCoupons = ref<(CouponVO & { relationId?: number, availableQuantity?: number })[]>([]);
const selectedCoupon = ref<(CouponVO & { relationId?: number, availableQuantity?: number }) | null>(null);
const showCouponDialog = ref(false);

// 获取用户拥有的所有优惠券
const fetchUserCoupons = async () => {
    try {
        const accountId = Number(sessionStorage.getItem("id"));
        console.log('开始获取用户优惠券，accountId:', accountId);

        // 第一步：获取用户的优惠券关联关系
        const relationResponse = await getCouponsByAccountId(accountId);

        if (relationResponse.data.code === '200') {
            const relations: AccountCouponsRelationVO[] = relationResponse.data.data || [];
            console.log('获取到的优惠券关联关系:', relations);

            // 第二步：根据每个关联关系的couponId获取详细优惠券信息
            const couponPromises = relations.map(async (relation) => {
                try {
                    const couponResponse = await getCouponById(relation.couponId);
                    if (couponResponse.data.code === '200') {
                        const coupon: CouponVO = couponResponse.data.data;
                        // 合并关联关系信息到优惠券对象中
                        return {
                            ...coupon,
                            relationId: relation.id,
                            availableQuantity: relation.quantity // 用户拥有的该优惠券数量
                        };
                    }
                    return null;
                } catch (error) {
                    console.error(`获取优惠券详情失败，couponId: ${relation.couponId}`, error);
                    return null;
                }
            });

            // 等待所有优惠券详情获取完成
            const couponsWithDetails = await Promise.all(couponPromises);

            // 过滤掉获取失败的优惠券
            allUserCoupons.value = couponsWithDetails.filter(coupon => coupon !== null);

            console.log('获取到的完整优惠券信息:', allUserCoupons.value);
            console.log('可用优惠券数量:', usableCoupons.value.length);
            console.log('不可用优惠券数量:', unusableCoupons.value.length);

            // 自动选择最优可用优惠券
            selectBestCoupon();
        }
    } catch (error) {
        console.error('获取用户优惠券失败:', error);
        ElMessage.error('获取优惠券信息失败');
    }
};

// 检查优惠券是否可用
const isCouponUsable = (coupon: CouponVO & { relationId?: number, availableQuantity?: number }) => {
    // 首先检查优惠券是否有效
    if (coupon.isValid !== 1) {
        console.log(`优惠券 ${coupon.name} 不可用: isValid = ${coupon.isValid}`);
        return false;
    }

    // 检查用户是否还有可用数量
    if (coupon.availableQuantity && coupon.availableQuantity <= 0) {
        console.log(`优惠券 ${coupon.name} 不可用: 用户可用数量为0`);
        return false;
    }

    // 获取当前订单总额
    const currentTotalAmount = totalPrice.value;

    // 检查订单总额是否满足优惠券使用条件
    let minRequiredAmount = 0;

    if (coupon.discountType === 1) {
        // 百分比折扣类型：需要有一定金额才有意义
        minRequiredAmount = 0.01;
    } else {
        // 固定金额折扣类型：订单总额必须超过优惠券面额
        minRequiredAmount = coupon.discountValue;
    }

    const canUse = currentTotalAmount > minRequiredAmount;

    console.log('优惠券可用性检查:', {
        couponId: coupon.id,
        couponName: coupon.name,
        discountType: coupon.discountType,
        discountValue: coupon.discountValue,
        currentTotalAmount: currentTotalAmount,
        minRequiredAmount: minRequiredAmount,
        isValid: coupon.isValid,
        availableQuantity: coupon.availableQuantity,
        canUse: canUse
    });

    return canUse;
};

// 获取优惠券不可用的原因
const getCouponDisabledReason = (coupon: CouponVO & { relationId?: number, availableQuantity?: number }) => {
    if (coupon.isValid !== 1) {
        return '优惠券已失效';
    }

    if (coupon.availableQuantity && coupon.availableQuantity <= 0) {
        return '您已用完此优惠券';
    }

    const currentTotalAmount = totalPrice.value;

    if (coupon.discountType === 1) {
        // 百分比折扣
        if (currentTotalAmount <= 0) {
            return '订单金额不足';
        }
    } else {
        // 固定金额折扣
        if (currentTotalAmount <= coupon.discountValue) {
            return `订单需满${coupon.discountValue}元`;
        }
    }

    return '未知原因,请联系管理员';
};

// 分离可用和不可用优惠券
const usableCoupons = computed(() => {
    return allUserCoupons.value.filter(coupon => isCouponUsable(coupon));
});

const unusableCoupons = computed(() => {
    return allUserCoupons.value.filter(coupon => !isCouponUsable(coupon));
});

// 计算使用优惠券后的价格
const calculateDiscountPrice = (originalPrice: number, coupon: CouponVO) => {
    if (coupon.discountType === 1) {
        // 百分比折扣
        const discountAmount = originalPrice * (coupon.discountValue / 100);
        return originalPrice - discountAmount;
    } else {
        // 固定金额折扣
        return Math.max(0, originalPrice - coupon.discountValue);
    }
};

// 自动选择最优可用优惠券
const selectBestCoupon = () => {
    if (usableCoupons.value.length === 0) {
        selectedCoupon.value = null;
        console.log('没有可用优惠券');
        return;
    }

    const originalPrice = totalPrice.value;
    let bestCoupon = null;
    let maxSavings = 0;

    console.log('开始计算最优优惠券，当前总价:', originalPrice);

    for (const coupon of usableCoupons.value) {
        const discountedPrice = calculateDiscountPrice(originalPrice, coupon);
        const savings = originalPrice - discountedPrice;

        console.log(`优惠券计算 - ${coupon.name}:`, {
            type: coupon.discountType === 1 ? '百分比' : '固定金额',
            value: coupon.discountValue,
            originalPrice: originalPrice,
            discountedPrice: discountedPrice,
            savings: savings,
            availableQuantity: coupon.availableQuantity
        });

        if (savings > maxSavings) {
            maxSavings = savings;
            bestCoupon = coupon;
        }
    }

    selectedCoupon.value = bestCoupon;

    if (bestCoupon) {
        console.log('选择的最优优惠券:', {
            name: bestCoupon.name,
            maxSavings: maxSavings,
            finalPrice: originalPrice - maxSavings
        });
    }
};

// 格式化折扣显示
const formatCouponDiscount = (coupon: CouponVO) => {
    return coupon.discountType === 1
        ? `${coupon.discountValue}% OFF`
        : `立减 ¥${coupon.discountValue}`;
};

// 计算优惠券节省金额
const couponSavings = computed(() => {
    if (!selectedCoupon.value) return 0;
    const originalPrice = totalPrice.value;
    const discountedPrice = calculateDiscountPrice(originalPrice, selectedCoupon.value);
    return originalPrice - discountedPrice;
});

// 计算最终价格（使用优惠券后）
const finalPrice = computed(() => {
    if (!selectedCoupon.value) return totalPrice.value;
    return calculateDiscountPrice(totalPrice.value, selectedCoupon.value);
});

// 选择优惠券
const selectCoupon = (coupon: (CouponVO & { relationId?: number, availableQuantity?: number }) | null) => {
    // 检查优惠券是否可用
    if (coupon && !isCouponUsable(coupon)) {
        const reason = getCouponDisabledReason(coupon);
        ElMessage.warning(`该优惠券暂不可用：${reason}`);
        return;
    }

    selectedCoupon.value = coupon;
    showCouponDialog.value = false;
    if (coupon) {
        const savings = totalPrice.value - calculateDiscountPrice(totalPrice.value, coupon);
        ElMessage.success(`已选择优惠券：${coupon.name}，节省¥${savings.toFixed(2)}`);
    } else {
        ElMessage.info('已取消使用优惠券');
    }
};

// 格式化时间显示
const formatDate = (dateString: string) => {
    if (!dateString) {
        return '--';
    }
    try {
        return new Date(dateString).toLocaleDateString('zh-CN');
    } catch {
        return '--';
    }
};

//获取用户信息，赋值给orderInfo
const fetchUserDetails = async () => {
    try {
        const response = await getUserDetails(sessionStorage.getItem('username')!)
        // console.log(response.data.data)
        orderInfo.value.name = response.data.data.name;
        orderInfo.value.location = response.data.data.location;
        orderInfo.value.telephone = response.data.data.telephone;
        orderInfo.value.email = response.data.data.email;
    } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败')
    }
}

// 获取购物车数据
const fetchCart = async () => {
    loading.value = true
    try {
        await getCartProducts().then((res) => {
            if (res.data.code === '200') {
                cart.value = res.data.data
                if (cart.value!.cartItems.length > 0) {
                    cartItems.value = cart.value!.cartItems.map(item => ({
                        ...item,
                        selected: false // 默认未选中
                    }));
                    fetchProductAndStockpile()
                }
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

const fetchProductAndStockpile = async () => {
    for (const item of cart.value!.cartItems) {
        await getProduct(item.productId).then((res) => {
            if (res.data.code === '200') {
                products.value.push(res.data.data)
            }
        })
        await getStockpile(item.productId).then((res) => {
            if (res.data.code === '200') {
                stockpiles.value.push(res.data.data)
            }
        })
    }
}

const getCartProduct = (productId: number): Product => {
    return products.value.find(p => p.id === productId)!
}
const getCartStockpile = (productId: number): Stockpile => {
    return stockpiles.value.find(s => s.productId === productId)!
}

// 初始化加载
onMounted(async () => {
    await Promise.all([fetchUserDetails(), fetchCart()]);
    await fetchUserCoupons();
})

// 数量变化处理
const handleQuantityChange = (item: CartItem, newQuantity: number) => {
    if (newQuantity < 1) {
        ElMessage.warning('数量不能小于1')
        return
    }
    if (newQuantity > getCartStockpile(item.productId).amount) {
        ElMessage.warning(`库存不足，最多可购买${getCartStockpile(item.productId).amount}件`)
        return
    }

    updateCartItem(item.cartItemId, newQuantity).then((res) => {
        if (res.data.code === '200') {
            item.quantity = newQuantity
            ElMessage.success('更新成功');
            // 重新计算最优优惠券
            selectBestCoupon();
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
const handleDeleteItem = (cartItemId: number) => {
    ElMessageBox.confirm('确定要删除该商品吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        deleteCartProduct(cartItemId).then((res) => {
            if (res.data.code === '200') {
                cartItems.value = cartItems.value.filter(item => item.cartItemId !== cartItemId)
                ElMessage.success('删除成功')
                // 重新计算最优优惠券
                selectBestCoupon();
            } else {
                ElMessage({
                    message: res.data.msg,
                    type: 'error',
                    center: true,
                })
            }
        })
    }).catch(() => { })
}

// 更新选中数组
const toggleSelectItem = () => {
    selectedItems.value = cartItems.value.filter(item => item.selected)
    // 重新计算最优优惠券
    selectBestCoupon();
}

// 全选/取消全选
const toggleSelectAll = (isSelectAll: any) => {
    cartItems.value.forEach(item => {
        item.selected = isSelectAll
    })
    selectedItems.value = isSelectAll ? [...cartItems.value] : []
    // 重新计算最优优惠券
    selectBestCoupon();
}

// 计算总价（选中商品的原价）
const totalPrice = computed(() => {
    return selectedItems.value.reduce((sum, item) => {
        return sum + (getCartProduct(item.productId).price * item.quantity)
    }, 0)
})

// 结算
const handleCheckout = () => {
    if (selectedItems.value.length === 0) {
        ElMessage.warning('请至少选择一件商品')
        return
    }
    showConfirmDialog.value = true
}

const onConfirm = async () => {
    const cartItemIds = selectedItems.value
        .filter(item => item.selected)
        .map(item => item.cartItemId)
    // 获取优惠券类型和面值
    const couponType = selectedCoupon.value ? selectedCoupon.value.discountType : null;
    const couponValue = selectedCoupon.value ? selectedCoupon.value.discountValue : null;

    checkout(cartItemIds, orderInfo.value, "ALIPAY", couponType as number, couponValue as number).then((res) => {
        if (res.data.code === '200') {
            sessionStorage.setItem('checkoutData', JSON.stringify(res.data))
            router.push('/checkout')
            ElMessage.success('跳转结算页面')
        } else {
            ElMessage({
                message: res.data.msg,
                type: 'error',
                center: true,
            })
        }
    })
}
</script>

<template>
    <div class="cart-container">
        <!-- 页面头部 -->
        <div class="page-header">
            <div class="header-left">
                <div class="header-icon">
                    <el-icon size="32">
                        <ShoppingBag />
                    </el-icon>
                </div>
                <div class="header-text">
                    <h1>我的购物车</h1>
                    <p>管理您选购的商品</p>
                </div>
            </div>
            <div class="header-stats" v-if="cartItems.length > 0">
                <div class="stat-item">
                    <div class="stat-number">{{ cartItems.length }}</div>
                    <div class="stat-label">商品种类</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">{{ selectedItems.length }}</div>
                    <div class="stat-label">已选商品</div>
                </div>
            </div>
        </div>

        <div class="cart-content">
            <!-- 购物车列表 -->
            <el-card class="cart-list-card" shadow="hover">
                <div class="cart-header">
                    <div class="header-select">
                        <el-checkbox :model-value="selectedItems.length === cartItems.length && cartItems.length > 0"
                            @change="toggleSelectAll" class="select-all-checkbox">
                            全选
                        </el-checkbox>
                    </div>
                    <div class="header-item header-info">商品信息</div>
                    <div class="header-item header-price">单价</div>
                    <div class="header-item header-quantity">数量</div>
                    <div class="header-item header-total">小计</div>
                    <div class="header-item header-actions">操作</div>
                </div>

                <div v-if="loading" class="loading-container">
                    <el-skeleton :rows="5" animated />
                </div>

                <div v-else-if="cartItems.length === 0" class="empty-cart">
                    <el-empty description="购物车空空如也，快去添加心仪的商品吧！" class="empty-state">
                        <el-button type="primary" @click="$router.push('/')" size="large" class="go-shopping-btn">
                            <el-icon>
                                <ShoppingBag />
                            </el-icon>
                            去逛逛
                        </el-button>
                    </el-empty>
                </div>

                <div v-else class="cart-items">
                    <div v-for="item in cartItems" :key="item.cartItemId" class="cart-item">
                        <div class="item-select">
                            <el-checkbox v-model="item.selected" @change="toggleSelectItem()" />
                        </div>
                        <div class="item-info">
                            <div class="product-image-wrapper">
                                <el-image :src="getCartProduct(item.productId).cover" fit="cover" class="product-image">
                                    <template #error>
                                        <div class="image-error">
                                            <el-icon size="24">
                                                <Picture />
                                            </el-icon>
                                            <span>图片加载失败</span>
                                        </div>
                                    </template>
                                </el-image>
                            </div>
                            <div class="product-details">
                                <div class="product-name">{{ getCartProduct(item.productId).title }}</div>
                                <div class="product-spec">规格: 默认</div>
                                <div class="stock-info">
                                    库存: {{ getCartStockpile(item.productId).amount }} 件
                                </div>
                            </div>
                        </div>
                        <div class="item-price">
                            <span class="price-symbol">¥</span>
                            <span class="price-value">{{ getCartProduct(item.productId).price }}</span>
                        </div>
                        <div class="item-quantity">
                            <el-input-number v-model="item.quantity" :min="1"
                                :max="getCartStockpile(item.productId).amount"
                                @change="handleQuantityChange(item, $event!)" size="default"
                                controls-position="right" />
                        </div>
                        <div class="item-total">
                            <span class="total-symbol">¥</span>
                            <span
                                class="total-value">{{ (getCartProduct(item.productId).price * item.quantity).toFixed(2) }}</span>
                        </div>
                        <div class="item-actions">
                            <el-button type="danger" size="small" @click="handleDeleteItem(item.cartItemId)"
                                class="delete-btn">
                                <el-icon>
                                    <Delete />
                                </el-icon>
                                删除
                            </el-button>
                        </div>
                    </div>
                </div>
            </el-card>

            <!-- 优惠券选择区域 -->
            <el-card v-if="selectedItems.length > 0" class="coupon-card" shadow="hover">
                <template #header>
                    <div class="coupon-header">
                        <el-icon class="coupon-icon">
                            <Present />
                        </el-icon>
                        <span>优惠券</span>
                        <div class="coupon-stats">
                            <el-tag size="small" type="success">可用 {{ usableCoupons.length }}</el-tag>
                            <el-tag size="small" type="info">不可用 {{ unusableCoupons.length }}</el-tag>
                        </div>
                    </div>
                </template>

                <div class="coupon-content">
                    <div v-if="selectedCoupon" class="selected-coupon">
                        <div class="coupon-info">
                            <div class="coupon-name">{{ selectedCoupon.name }}</div>
                            <div class="coupon-discount">{{ formatCouponDiscount(selectedCoupon) }}</div>
                        </div>
                        <div class="coupon-savings">
                            <span class="savings-text">已省 ¥{{ couponSavings.toFixed(2) }}</span>
                        </div>
                        <el-button type="text" @click="showCouponDialog = true" class="change-coupon-btn">
                            更换
                        </el-button>
                    </div>
                    <div v-else-if="usableCoupons.length > 0" class="no-coupon">
                        <div class="no-coupon-text">
                            <el-icon>
                                <Ticket />
                            </el-icon>
                            <span>暂未使用优惠券</span>
                        </div>
                        <el-button type="primary" @click="showCouponDialog = true" size="small">
                            选择优惠券
                        </el-button>
                    </div>
                    <div v-else class="no-coupon">
                        <div class="no-coupon-text">
                            <el-icon>
                                <Warning />
                            </el-icon>
                            <span>暂无可用优惠券</span>
                        </div>
                        <el-button type="text" @click="showCouponDialog = true" size="small">
                            查看所有优惠券
                        </el-button>
                    </div>
                </div>
            </el-card>

            <!-- 结算栏 -->
            <el-card class="cart-footer-card" shadow="always">
                <div class="cart-footer">
                    <div class="footer-left">
                        <el-checkbox :model-value="selectedItems.length === cartItems.length && cartItems.length > 0"
                            @change="toggleSelectAll" class="footer-select-all">
                            全选
                        </el-checkbox>
                        <el-button type="text" @click="cartItems.forEach(item => handleDeleteItem(item.cartItemId))"
                            class="delete-selected-btn">
                            <el-icon>
                                <Delete />
                            </el-icon>
                            删除选中
                        </el-button>
                    </div>
                    <div class="footer-right">
                        <div class="total-summary">
                            <div class="summary-line">
                                <span class="summary-label">已选 {{ selectedItems.length }} 件商品</span>
                            </div>
                            <div v-if="selectedCoupon" class="summary-line original-price-line">
                                <span class="summary-label">商品总价:</span>
                                <span class="original-price">¥{{ totalPrice.toFixed(2) }}</span>
                            </div>
                            <div v-if="selectedCoupon" class="summary-line savings-line">
                                <span class="summary-label">优惠减免:</span>
                                <span class="savings-amount">-¥{{ couponSavings.toFixed(2) }}</span>
                            </div>
                            <div class="summary-line final-price-line">
                                <span class="summary-label">{{ selectedCoupon ? '实付金额:' : '合计:' }}</span>
                                <span class="final-price">¥{{ finalPrice.toFixed(2) }}</span>
                            </div>
                        </div>
                        <el-button type="primary" size="large" :disabled="selectedItems.length === 0"
                            @click="handleCheckout" class="checkout-btn">
                            <el-icon>
                                <Check />
                            </el-icon>
                            立即结算
                        </el-button>
                    </div>
                </div>
            </el-card>
        </div>

        <!-- 优惠券选择对话框 -->
        <el-dialog v-model="showCouponDialog" title="🎫 选择优惠券" width="75%" class="coupon-dialog">
            <div class="coupon-dialog-content">
                <!-- 可用优惠券 -->
                <div v-if="usableCoupons.length > 0" class="coupon-section">
                    <div class="section-header">
                        <h3>
                            <el-icon>
                                <Check />
                            </el-icon>
                            可用优惠券 ({{ usableCoupons.length }})
                        </h3>
                    </div>
                    <div class="coupon-list">
                        <div class="coupon-option" @click="selectCoupon(null)">
                            <div class="coupon-card no-coupon-card" :class="{ selected: !selectedCoupon }">
                                <div class="no-coupon-content">
                                    <div class="no-coupon-icon">
                                        <el-icon size="32">
                                            <Ticket />
                                        </el-icon>
                                    </div>
                                    <div class="no-coupon-info">
                                        <div class="no-coupon-title">不使用优惠券</div>
                                        <div class="no-coupon-price">原价: ¥{{ totalPrice.toFixed(2) }}</div>
                                    </div>
                                </div>
                                <div v-if="!selectedCoupon" class="selected-mark">
                                    <el-icon>
                                        <Check />
                                    </el-icon>
                                </div>
                            </div>
                        </div>

                        <div v-for="coupon in usableCoupons" :key="`usable-${coupon.id}`" class="coupon-option"
                            @click="selectCoupon(coupon)">
                            <div class="coupon-card usable-coupon"
                                :class="{ selected: selectedCoupon?.id === coupon.id }">
                                <div class="coupon-left">
                                    <div class="discount-badge">
                                        <div class="discount-value">{{ formatCouponDiscount(coupon) }}</div>
                                    </div>
                                </div>
                                <div class="coupon-body">
                                    <div class="coupon-name">{{ coupon.name }}</div>
                                    <div class="coupon-desc">{{ coupon.description }}</div>
                                    <div class="coupon-quantity" v-if="coupon.availableQuantity">
                                        <el-tag size="small" type="success">拥有 {{ coupon.availableQuantity }} 张</el-tag>
                                    </div>
                                    <div class="coupon-time">
                                        <el-icon>
                                            <Clock />
                                        </el-icon>
                                        <span>{{ formatDate(coupon.startTime) }} -
                                            {{ formatDate(coupon.endTime) }}</span>
                                    </div>
                                    <div class="coupon-result">
                                        <span class="result-price">使用后: ¥{{ calculateDiscountPrice(totalPrice,
                                            coupon).toFixed(2) }}</span>
                                        <span class="result-savings">(省 ¥{{ (totalPrice -
                                            calculateDiscountPrice(totalPrice,
                                                coupon)).toFixed(2) }})</span>
                                    </div>
                                </div>
                                <div v-if="selectedCoupon?.id === coupon.id" class="selected-mark">
                                    <el-icon>
                                        <Check />
                                    </el-icon>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 不可用优惠券 -->
                <div v-if="unusableCoupons.length > 0" class="coupon-section">
                    <div class="section-header">
                        <h3>
                            <el-icon>
                                <Warning />
                            </el-icon>
                            不可用优惠券 ({{ unusableCoupons.length }})
                        </h3>
                    </div>
                    <div class="coupon-list">
                        <div v-for="coupon in unusableCoupons" :key="`unusable-${coupon.id}`" class="coupon-option">
                            <div class="coupon-card unusable-coupon" @click="selectCoupon(coupon)">
                                <div class="coupon-left disabled">
                                    <div class="discount-badge">
                                        <div class="discount-value">{{ formatCouponDiscount(coupon) }}</div>
                                    </div>
                                </div>
                                <div class="coupon-body">
                                    <div class="coupon-name">{{ coupon.name }}</div>
                                    <div class="coupon-desc">{{ coupon.description }}</div>
                                    <div class="coupon-quantity" v-if="coupon.availableQuantity !== undefined">
                                        <el-tag size="small"
                                            :type="coupon.availableQuantity > 0 ? 'success' : 'danger'">
                                            拥有 {{ coupon.availableQuantity }} 张
                                        </el-tag>
                                    </div>
                                    <div class="coupon-time">
                                        <el-icon>
                                            <Clock />
                                        </el-icon>
                                        <span>{{ formatDate(coupon.startTime) }} -
                                            {{ formatDate(coupon.endTime) }}</span>
                                    </div>
                                    <div class="disabled-reason">
                                        <el-tag type="danger"
                                            size="small">{{ getCouponDisabledReason(coupon) }}</el-tag>
                                    </div>
                                </div>
                                <div class="disabled-mask">
                                    <el-icon>
                                        <Warning />
                                    </el-icon>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 无优惠券提示 -->
                <div v-if="allUserCoupons.length === 0" class="no-coupons">
                    <el-empty description="您还没有任何优惠券">
                        <el-button type="primary" @click="$router.push('/')">去首页领取</el-button>
                    </el-empty>
                </div>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="showCouponDialog = false" size="large">取消</el-button>
                    <el-button type="primary" @click="showCouponDialog = false" size="large">确定</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 订单确认对话框 -->
        <el-dialog v-model="showConfirmDialog" title="📋 确认订单信息" width="60%" class="order-dialog">
            <div class="order-form-container">
                <el-form :model="orderInfo" label-width="0" class="order-form">
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label">
                                <el-icon>
                                    <Edit />
                                </el-icon>
                                <span>收货人姓名</span>
                            </label>
                            <el-input v-model="orderInfo.name" placeholder="请输入收货人姓名" size="large" />
                        </div>
                        <div class="form-group">
                            <label class="form-label">
                                <el-icon>
                                    <Edit />
                                </el-icon>
                                <span>手机号码</span>
                            </label>
                            <el-input v-model="orderInfo.telephone" placeholder="请输入手机号码" size="large" />
                        </div>
                    </div>

                    <div class="form-row full-width">
                        <div class="form-group">
                            <label class="form-label">
                                <el-icon>
                                    <Edit />
                                </el-icon>
                                <span>收货地址</span>
                            </label>
                            <el-input v-model="orderInfo.location" placeholder="请输入详细收货地址" size="large" />
                        </div>
                    </div>

                    <div class="form-row full-width">
                        <div class="form-group">
                            <label class="form-label">
                                <el-icon>
                                    <Edit />
                                </el-icon>
                                <span>电子邮箱</span>
                            </label>
                            <el-input v-model="orderInfo.email" placeholder="请输入电子邮箱" size="large" />
                        </div>
                    </div>
                </el-form>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="showConfirmDialog = false" size="large">取消</el-button>
                    <el-button type="primary" @click="onConfirm" size="large" class="confirm-order-btn">
                        <el-icon>
                            <Check />
                        </el-icon>
                        确认下单
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.cart-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 20px;
    background: #f8f9fa;
    min-height: 100vh;
}

/* 页面头部 */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 24px 0;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
}

.header-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    box-shadow: 0 4px 16px rgba(78, 205, 196, 0.3);
}

.header-text h1 {
    margin: 0 0 4px 0;
    font-size: 28px;
    color: #2c3e50;
    font-weight: 600;
}

.header-text p {
    margin: 0;
    color: #7f8c8d;
    font-size: 14px;
}

.header-stats {
    display: flex;
    gap: 24px;
}

.stat-item {
    text-align: center;
    padding: 12px 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-number {
    font-size: 24px;
    font-weight: bold;
    color: #4ecdc4;
    line-height: 1;
}

.stat-label {
    font-size: 12px;
    color: #7f8c8d;
    margin-top: 4px;
}

/* 购物车内容 */
.cart-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

/* 购物车列表样式 */
.cart-list-card {
    border-radius: 16px;
    overflow: hidden;
    border: none;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.cart-header {
    display: flex;
    align-items: center;
    padding: 20px 24px;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
    font-size: 14px;
    color: #64748b;
    font-weight: 500;
}

/* 表头各列的宽度设置 */
.header-select {
    flex: 0 0 60px;
    display: flex;
    align-items: center;
}

.header-info {
    flex: 3;
    text-align: left;
    padding-left: 0;
}

.header-price {
    flex: 1;
    text-align: center;
}

.header-quantity {
    flex: 1;
    text-align: center;
}

.header-total {
    flex: 1;
    text-align: center;
}

.header-actions {
    flex: 1;
    text-align: center;
}

.select-all-checkbox {
    margin: 0;
}

.loading-container {
    padding: 40px 24px;
}

.empty-cart {
    padding: 80px 24px;
}

.empty-state {
    padding: 40px 0;
}

.go-shopping-btn {
    border-radius: 12px;
    padding: 12px 32px;
    font-weight: 500;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    transition: all 0.3s ease;
}

.go-shopping-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(78, 205, 196, 0.4);
}

/* 购物车商品项样式 */
.cart-items {
    background: white;
}

.cart-item {
    display: flex;
    align-items: center;
    padding: 24px;
    border-bottom: 1px solid #f1f5f9;
    transition: all 0.3s ease;
}

.cart-item:hover {
    background: #f8fafc;
    transform: translateX(4px);
}

.cart-item:last-child {
    border-bottom: none;
}

/* 确保每一列的宽度与表头对应 */
.item-select {
    flex: 0 0 60px;
    display: flex;
    align-items: center;
    justify-content: flex-start;
}

.item-info {
    flex: 3;
    display: flex;
    align-items: center;
    text-align: left;
    gap: 16px;
}

.item-price {
    flex: 1;
    display: flex;
    align-items: baseline;
    justify-content: center;
    gap: 2px;
}

.item-quantity {
    flex: 1;
    display: flex;
    justify-content: center;
}

.item-total {
    flex: 1;
    display: flex;
    align-items: baseline;
    justify-content: center;
    gap: 2px;
}

.item-actions {
    flex: 1;
    display: flex;
    justify-content: center;
}

/* 商品信息区域样式 */
.product-image-wrapper {
    position: relative;
    flex-shrink: 0;
}

.product-image {
    width: 80px;
    height: 80px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
}

.cart-item:hover .product-image {
    transform: scale(1.05);
}

.image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #a0aec0;
    background: #f7fafc;
    gap: 4px;
    font-size: 12px;
}

.product-details {
    flex: 1;
    min-width: 0;
    /* 防止内容溢出 */
}

.product-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 8px;
    color: #2d3748;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-spec {
    font-size: 12px;
    color: #a0aec0;
    margin-bottom: 4px;
}

.stock-info {
    font-size: 12px;
    color: #4ecdc4;
}

/* 价格样式 */
.price-symbol,
.total-symbol {
    font-size: 14px;
    color: #e53e3e;
}

.price-value {
    font-size: 18px;
    font-weight: 600;
    color: #e53e3e;
}

.total-value {
    font-size: 20px;
    font-weight: bold;
    color: #e53e3e;
}

/* 数量输入框样式 */
.item-quantity .el-input-number {
    width: 120px;
}

/* 操作按钮样式 */
.delete-btn {
    border-radius: 8px;
    transition: all 0.3s ease;
}

.delete-btn:hover {
    transform: scale(1.05);
}

/* 优惠券卡片 */
.coupon-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.coupon-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 16px;
    font-weight: 600;
    color: #2d3748;
}

.coupon-header>span {
    display: flex;
    align-items: center;
    gap: 8px;
}

.coupon-icon {
    color: #ff6b6b;
    font-size: 18px;
}

.coupon-stats {
    display: flex;
    gap: 8px;
}

.coupon-content {
    padding: 0;
}

.selected-coupon {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
    border-radius: 12px;
    color: white;
}

.coupon-info {
    flex: 1;
}

.coupon-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 4px;
}

.coupon-discount {
    font-size: 14px;
    opacity: 0.9;
}

.coupon-savings {
    margin-right: 16px;
}

.savings-text {
    font-size: 16px;
    font-weight: 600;
}

.change-coupon-btn {
    color: white;
    font-size: 14px;
    padding: 8px 16px;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
}

.change-coupon-btn:hover {
    background: rgba(255, 255, 255, 0.3);
}

.no-coupon {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border: 2px dashed #e2e8f0;
    border-radius: 12px;
    color: #64748b;
}

.no-coupon-text {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
}

/* 结算栏 */
.cart-footer-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    background: linear-gradient(135deg, #f8faff 0%, #e8f4f8 100%);
}

.cart-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
}

.footer-left {
    display: flex;
    align-items: center;
    gap: 24px;
}

.footer-select-all {
    font-weight: 500;
}

.delete-selected-btn {
    color: #e53e3e;
    font-size: 14px;
    transition: all 0.3s ease;
}

.delete-selected-btn:hover {
    color: #c53030;
    transform: scale(1.05);
}

.footer-right {
    display: flex;
    align-items: center;
    gap: 32px;
}

.total-summary {
    text-align: right;
}

.summary-line {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    gap: 24px;
}

.summary-line:last-child {
    margin-bottom: 0;
}

.summary-label {
    font-size: 14px;
    color: #64748b;
}

.original-price {
    font-size: 14px;
    color: #a0aec0;
    text-decoration: line-through;
}

.savings-amount {
    font-size: 14px;
    color: #38a169;
    font-weight: 500;
}

.final-price-line .summary-label {
    font-size: 16px;
    font-weight: 600;
    color: #2d3748;
}

.final-price {
    font-size: 24px;
    font-weight: bold;
    color: #e53e3e;
}

.checkout-btn {
    padding: 16px 32px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    transition: all 0.3s ease;
}

.checkout-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(78, 205, 196, 0.4);
}

/* 优惠券选择对话框 */
.coupon-dialog {
    border-radius: 16px;
}

.coupon-dialog .el-dialog__header {
    background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
    color: white;
    padding: 20px 30px;
    margin: 0;
}

.coupon-dialog .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
}

.coupon-dialog .el-dialog__headerbtn .el-dialog__close {
    color: white;
    font-size: 18px;
}

.coupon-dialog-content {
    max-height: 600px;
    overflow-y: auto;
    padding: 0;
}

.coupon-section {
    margin-bottom: 32px;
}

.coupon-section:last-child {
    margin-bottom: 0;
}

.section-header {
    padding: 16px 24px;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
}

.section-header h3 {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
    font-size: 16px;
    color: #2d3748;
    font-weight: 600;
}

.coupon-list {
    padding: 20px 24px;
}

.coupon-option {
    margin-bottom: 16px;
    cursor: pointer;
}

.coupon-option .coupon-card {
    display: flex;
    align-items: stretch;
    border: 2px solid #e2e8f0;
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s ease;
    position: relative;
    background: white;
}

.coupon-option .coupon-card:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
}

.coupon-option .coupon-card.selected {
    border-color: #ff6b6b;
    background: #fff5f5;
    box-shadow: 0 4px 16px rgba(255, 107, 107, 0.3);
}

.usable-coupon:hover {
    border-color: #ff6b6b;
    box-shadow: 0 4px 16px rgba(255, 107, 107, 0.2);
}

.unusable-coupon {
    opacity: 0.6;
    position: relative;
    cursor: not-allowed;
}

.unusable-coupon:hover {
    border-color: #cbd5e0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.no-coupon-card .no-coupon-content {
    display: flex;
    align-items: center;
    padding: 24px;
    width: 100%;
    gap: 16px;
}

.no-coupon-icon {
    color: #a0aec0;
}

.no-coupon-info {
    flex: 1;
}

.no-coupon-title {
    font-size: 16px;
    color: #64748b;
    margin-bottom: 4px;
    font-weight: 500;
}

.no-coupon-price {
    font-size: 14px;
    color: #2d3748;
    font-weight: 600;
}

.coupon-left {
    width: 120px;
    background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
}

.coupon-left.disabled {
    background: linear-gradient(135deg, #cbd5e0 0%, #a0aec0 100%);
}

.discount-badge {
    text-align: center;
}

.discount-value {
    font-size: 16px;
    font-weight: bold;
}

.coupon-body {
    flex: 1;
    padding: 20px;
}

.coupon-body .coupon-name {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 8px;
    color: #2d3748;
}

.coupon-desc {
    font-size: 14px;
    color: #64748b;
    margin-bottom: 12px;
    line-height: 1.4;
}

.coupon-quantity {
    margin: 8px 0;
}

.coupon-time {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #a0aec0;
    margin-bottom: 12px;
}

.coupon-result {
    display: flex;
    align-items: center;
    gap: 8px;
}

.result-price {
    font-size: 16px;
    color: #e53e3e;
    font-weight: 600;
}

.result-savings {
    font-size: 14px;
    color: #38a169;
    font-weight: 500;
}

.disabled-reason {
    margin-top: 8px;
}

.disabled-mask {
    position: absolute;
    top: 12px;
    right: 12px;
    width: 32px;
    height: 32px;
    background: #f56c6c;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 16px;
}

.selected-mark {
    position: absolute;
    top: 12px;
    right: 12px;
    width: 28px;
    height: 28px;
    background: #ff6b6b;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 14px;
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

.no-coupons {
    padding: 60px 24px;
}

/* 订单确认对话框 */
.order-dialog {
    border-radius: 16px;
}

.order-dialog .el-dialog__header {
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
    padding: 20px 30px;
    margin: 0;
}

.order-dialog .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
}

.order-dialog .el-dialog__headerbtn .el-dialog__close {
    color: white;
    font-size: 18px;
}

.order-form-container {
    padding: 30px;
    background: #f8fafc;
}

.order-form {
    background: white;
    padding: 32px;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.form-row {
    display: flex;
    gap: 24px;
    margin-bottom: 24px;
}

.form-row.full-width {
    flex-direction: column;
}

.form-group {
    flex: 1;
}

.form-label {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    font-size: 14px;
    color: #2d3748;
    font-weight: 500;
    gap: 6px;
}

.form-label .el-icon {
    color: #4ecdc4;
}

.order-form .el-input {
    border-radius: 8px;
}

.order-form .el-input__wrapper {
    border: 2px solid #e2e8f0;
    border-radius: 8px;
    transition: all 0.3s ease;
}

.order-form .el-input__wrapper:hover {
    border-color: #cbd5e0;
}

.order-form .el-input__wrapper.is-focus {
    border-color: #4ecdc4;
    box-shadow: 0 0 0 3px rgba(78, 205, 196, 0.1);
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 20px 30px;
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;
}

.confirm-order-btn {
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    border: none;
    border-radius: 8px;
    padding: 12px 24px;
    font-weight: 600;
    transition: all 0.3s ease;
}

.confirm-order-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(78, 205, 196, 0.4);
}

/* 响应式设计 - 移动端隐藏表头，调整布局 */
@media (max-width: 768px) {
    .cart-container {
        padding: 16px;
    }

    .page-header {
        flex-direction: column;
        gap: 16px;
        text-align: center;
    }

    .header-stats {
        justify-content: center;
    }

    .cart-header {
        display: none;
    }

    .cart-item {
        flex-wrap: wrap;
        position: relative;
        padding: 20px 16px;
        gap: 12px;
    }

    .item-select {
        position: absolute;
        left: 16px;
        top: 20px;
    }

    .item-info {
        flex: 1 0 100%;
        margin-bottom: 12px;
        padding-left: 40px;
    }

    .item-price,
    .item-quantity,
    .item-total,
    .item-actions {
        flex: 1;
        margin-bottom: 12px;
    }

    .item-price::before {
        content: "单价: ";
        color: #64748b;
        font-size: 12px;
    }

    .item-total::before {
        content: "小计: ";
        color: #64748b;
        font-size: 12px;
    }

    .selected-coupon,
    .no-coupon {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
    }

    .cart-footer {
        flex-direction: column;
        gap: 16px;
    }

    .footer-left {
        width: 100%;
        justify-content: space-between;
    }

    .footer-right {
        width: 100%;
        flex-direction: column;
        gap: 16px;
    }

    .total-summary {
        text-align: center;
    }

    .form-row {
        flex-direction: column;
        gap: 16px;
    }

    .coupon-dialog-content {
        max-height: 500px;
    }

    .coupon-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }

    .coupon-stats {
        align-self: flex-end;
    }
}
</style>