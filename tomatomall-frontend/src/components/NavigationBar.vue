<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMenu, ElMenuItem, ElPopover, ElTabPane, ElTabs, ElBadge, ElMessage } from "element-plus";
import { ShoppingCart, Shop, Setting, User, Bell, SwitchButton, Edit, CirclePlus, ArrowDown } from '@element-plus/icons-vue'
import router from "@/router";
import { isLogin, checkRole, isAdmin, isShopOwner, isStaff, isCustomer, messageLoad, unreadCount, receivedMessages, sentMessages } from "./LoginEvent";
import { getUserRoleById } from "@/api/account";
import { getShopIdByOwnerId } from "@/api/shop";
import { markMessageAsRead, Message } from "@/api/message";

const messagePopoverVisible = ref(false);

const NavigateToMessage = (message: Message) => {
    if (message.content === "NEW_EMPLOYEE_APPLICATION") {
        navigateToMyShop();
    } else if (message.content === "NEW_STORE_APPLICATION") {
        router.push("/shopManage");
    } else if (message.content === "LOW_INVENTORY") {
        navigateToWarehouse();
    } else if (message.content === "APPLICATION_APPROVED") {
        ElMessage.success({
            'message': "恭喜,申请已通过",
            'duration': 2000,
        });
        navigateToWarehouse();
    } else if (message.content === "APPLICATION_REJECTED") {
        ElMessage.info("申请未通过");
    } else if (message.content === "YOU_ARE_FIRED") {
        ElMessage.error("您已被解雇,请不要灰心");
    } else {
        ElMessage.warning("未知消息类型");
    }
};

const checkLogin = () => {
    const token = sessionStorage.getItem('token');
    isLogin.value = !!token;
};

const Logout = () => {
    sessionStorage.clear();
    ElMessage.success({
        message: '已退出登录',
        duration: 500
    })
    isLogin.value = false;
    router.push("/");
};

const navigateToMyShop = async () => {
    const id = sessionStorage.getItem('id');
    const shopId = await getShopIdByOwnerId(Number(id));
    router.push(`/myshop/${shopId.data.data}`);
};

const checkChange = async () => {
    if (!isLogin.value) return;
    const role = sessionStorage.getItem('role');
    const trueRole = await getUserRoleById(Number(sessionStorage.getItem('id')));
    if (role !== trueRole.data.data) {
        sessionStorage.setItem('role', trueRole.data.data);
        checkRole();
    }
};

const navigateToWarehouse = async () => {
    if (!isLogin.value) return;
    let shopId = 0;
    if (isStaff.value) {
        shopId = Number(sessionStorage.getItem('shopId'));
    } else {
        const id = sessionStorage.getItem('id');
        console.log('店主ID:', id);
        const response = await getShopIdByOwnerId(Number(id));
        shopId = response.data.data;
    }
    router.push(`/warehouse/${shopId}`);
};

const markAsRead = async (messageId: number) => {
    await markMessageAsRead(messageId);
    // 重新加载消息
    await messageLoad();
};

onMounted(checkLogin);
onMounted(checkRole);
onMounted(checkChange);
onMounted(messageLoad);
</script>

<template>
    <div class="nav-bar-container">
        <el-menu mode="horizontal" :router="true" class="nav-bar" background-color="#fff" text-color="#333"
            active-text-color="#409EFF">
            <el-menu-item index="/" class="logo-item">
                <img src="/src/assets/logo.jpeg" alt="Logo" class="logo-image" />
            </el-menu-item>

            <!-- 中间 -->
            <div class="center-menu">
                <el-menu-item index="/cart" class="nav-item">
                    <div class="nav-content">
                        <el-icon class="nav-icon">
                            <ShoppingCart />
                        </el-icon>
                        <span>购物车</span>
                    </div>
                </el-menu-item>

                <el-menu-item index="/shops" class="nav-item">
                    <div class="nav-content">
                        <el-icon class="nav-icon">
                            <Shop />
                        </el-icon>
                        <span>全部店铺</span>
                    </div>
                </el-menu-item>

                <el-sub-menu index="management" v-if="isAdmin || isShopOwner || isStaff" class="nav-submenu">
                    <template #title>
                        <div class="nav-content with-submenu">
                            <el-icon class="nav-icon">
                                <Setting />
                            </el-icon>
                            <span>管理</span>
                            <el-icon class="expand-icon">
                                <ArrowDown />
                            </el-icon>
                        </div>
                    </template>
                    <el-menu-item v-if="isShopOwner || isStaff" @click="navigateToWarehouse" class="submenu-item">
                        商品管理
                    </el-menu-item>
                    <el-menu-item index="/shopManage" v-if="isAdmin" class="submenu-item">
                        商店管理
                    </el-menu-item>
                    <el-menu-item index="/advertisements" v-if="isAdmin || isShopOwner" class="submenu-item">
                        广告管理
                    </el-menu-item>
                    <el-menu-item index="/couponManage" v-if="isAdmin" class="submenu-item">
                        优惠券管理
                    </el-menu-item>
                </el-sub-menu>
            </div>

            <!-- 右侧菜单 -->
            <div class="right-menu">
                <template v-if="isLogin">
                    <!-- 我要开店 -->
                    <el-menu-item index="/shopCreate" v-if="isCustomer" class="nav-item create-shop-btn">
                        <div class="nav-content">
                            <el-icon class="nav-icon">
                                <CirclePlus />
                            </el-icon>
                            <span>我要开店</span>
                        </div>
                    </el-menu-item>

                    <!-- 我的菜单 -->
                    <el-sub-menu index="mine" class="nav-submenu">
                        <template #title>
                            <div class="nav-content with-submenu">
                                <el-icon class="nav-icon">
                                    <User />
                                </el-icon>
                                <span>我的</span>
                                <el-icon class="expand-icon">
                                    <ArrowDown />
                                </el-icon>
                            </div>
                        </template>
                        <el-menu-item index="/user" class="submenu-item">个人中心</el-menu-item>
                        <el-menu-item index="/order" class="submenu-item">全部订单</el-menu-item>
                        <el-menu-item @click="navigateToMyShop" v-if="isShopOwner"
                            class="submenu-item">我的店铺</el-menu-item>
                    </el-sub-menu>

                    <!-- 消息通知 -->
                    <el-popover v-model:visible="messagePopoverVisible" placement="bottom-end" :width="420"
                        trigger="hover" class="message-popover">
                        <template #reference>
                            <div class="message-trigger">
                                <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0"
                                    class="message-badge">
                                    <div class="notification-icon" :class="{ 'has-notification': unreadCount > 0 }">
                                        <el-icon size="20">
                                            <Bell />
                                        </el-icon>
                                    </div>
                                </el-badge>
                            </div>
                        </template>

                        <div class="message-popup">
                            <div class="popup-header">
                                <h3>消息中心</h3>
                            </div>

                            <el-tabs type="border-card" stretch class="message-tabs">
                                <el-tab-pane label="📨 收到的消息" class="message-tab">
                                    <div class="message-list">
                                        <div v-for="message in receivedMessages" :key="'received-' + message.id"
                                            class="message-item" :class="{ 'unread': !message.isRead }"
                                            @click="!message.isRead && markAsRead(message.id)">
                                            <div class="message-indicator" v-if="!message.isRead"></div>
                                            <div class="message-content" @click="NavigateToMessage(message)">
                                                {{ message.content }}
                                            </div>
                                            <div class="message-time">
                                                <el-icon size="12">
                                                    <Bell />
                                                </el-icon>
                                                {{ message.createdTime }}
                                            </div>
                                        </div>
                                        <div v-if="receivedMessages.length === 0" class="empty-message">
                                            <div class="empty-icon">📭</div>
                                            <p>暂无消息</p>
                                        </div>
                                    </div>
                                </el-tab-pane>

                                <el-tab-pane label="📤 发出的消息" class="message-tab">
                                    <div class="message-list">
                                        <div v-for="message in sentMessages" :key="'sent-' + message.id"
                                            class="message-item sent-message">
                                            <div class="message-content">{{ message.content }}</div>
                                            <div class="message-time">
                                                <el-icon size="12">
                                                    <Bell />
                                                </el-icon>
                                                {{ message.createdTime }}
                                            </div>
                                        </div>
                                        <div v-if="sentMessages.length === 0" class="empty-message">
                                            <div class="empty-icon">📤</div>
                                            <p>暂无消息</p>
                                        </div>
                                    </div>
                                </el-tab-pane>
                            </el-tabs>
                        </div>
                    </el-popover>

                    <!-- 退出登录 -->
                    <div class="logout-item nav-item" @click="Logout">
                        <div class="nav-content">
                            <el-icon class="nav-icon">
                                <SwitchButton />
                            </el-icon>
                            <span>退出登录</span>
                        </div>
                    </div>
                </template>

                <template v-else>
                    <!-- 未登录状态 -->
                    <el-menu-item index="/login" class="nav-item auth-btn login-btn">
                        <div class="nav-content">
                            <el-icon class="nav-icon">
                                <User />
                            </el-icon>
                            <span>登录</span>
                        </div>
                    </el-menu-item>

                    <el-menu-item index="/register" class="nav-item auth-btn register-btn">
                        <div class="nav-content">
                            <el-icon class="nav-icon">
                                <Edit />
                            </el-icon>
                            <span>注册</span>
                        </div>
                    </el-menu-item>
                </template>
            </div>
        </el-menu>
    </div>
</template>

<style scoped>
/* 主容器 */
.nav-bar-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 70px;
    z-index: 1000;
    background: #ffffff;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border-bottom: 1px solid #e8e8e8;
}

/* 导航栏 */
.nav-bar {
    position: relative;
    height: 70px;
    padding: 0 30px;
    border: none;
    background: #ffffff;
}

/* 品牌区域 */
.brand-section {
    position: absolute;
    left: 30px;
    top: 0;
    height: 70px;
    display: flex;
    align-items: center;
}

/* 中间导航 */
.center-menu {
    position: absolute;
    left: 50%;
    top: 0;
    height: 70px;
    display: flex;
    align-items: center;
    transform: translateX(-50%);
    gap: 8px;
}

/* 右侧菜单 */
.right-menu {
    position: absolute;
    right: 30px;
    top: 0;
    height: 70px;
    display: flex;
    align-items: center;
    gap: 8px;
}

/* 统一所有导航项的高度 */
.brand-item,
.nav-item,
.nav-submenu,
.logout-item {
    height: 70px !important;
    line-height: 70px !important;
    padding: 0 !important;
    border: none !important;
    background: transparent !important;
    margin: 0 4px;
    display: flex !important;
    align-items: center !important;
}

/* 修复子菜单标题的高度 */
.nav-submenu :deep(.el-sub-menu__title) {
    height: 70px !important;
    line-height: 70px !important;
    padding: 0 !important;
    display: flex !important;
    align-items: center !important;
}

/* 隐藏 Element Plus 默认的箭头图标 */
.nav-submenu :deep(.el-sub-menu__icon-arrow) {
    display: none !important;
}

/* 统一导航内容样式 */
.nav-content {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    border-radius: 10px;
    transition: all 0.3s ease;
    cursor: pointer;
    font-weight: 500;
    color: #2c3e50;
    height: 50px;
    box-sizing: border-box;
    position: relative;
}

/* 有子菜单的导航项样式 */
.nav-content.with-submenu {
    padding-right: 12px;
    /* 为展开图标留出空间 */
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border: 1px solid #dee2e6;
}

.nav-content:hover {
    background: #f0fffe;
    color: #4ecdc4;
}

.nav-content.with-submenu:hover {
    background: linear-gradient(135deg, #e8f8f7 0%, #d1f2eb 100%);
    border-color: #4ecdc4;
}

.nav-icon {
    font-size: 18px;
    flex-shrink: 0;
}

/* 展开图标样式 */
.expand-icon {
    font-size: 14px;
    margin-left: auto;
    transition: transform 0.3s ease;
    color: #6c757d;
}

/* 子菜单展开时旋转图标 */
.nav-submenu.is-opened .expand-icon {
    transform: rotate(180deg);
}

.nav-content.with-submenu:hover .expand-icon {
    color: #4ecdc4;
}

.brand-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;
}

.brand-text {
    font-size: 20px;
    font-weight: 700;
    color: #2c3e50;
    font-family: "华文中宋", serif;
    white-space: nowrap;
}

/* 特殊按钮样式 */
.create-shop-btn .nav-content {
    background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
    color: white;
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.2);
    border: none;
}

.create-shop-btn .nav-content:hover {
    background: linear-gradient(135deg, #ff5252 0%, #ff7979 100%);
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.auth-btn .nav-content {
    border: 2px solid #4ecdc4;
    background: transparent;
}

.login-btn .nav-content:hover {
    background: #4ecdc4;
    color: white;
}

.register-btn .nav-content {
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
    border: none;
    box-shadow: 0 2px 8px rgba(78, 205, 196, 0.2);
}

.register-btn .nav-content:hover {
    box-shadow: 0 4px 12px rgba(78, 205, 196, 0.3);
}

.logout-item .nav-content {
    color: #f56c6c;
}

.logout-item .nav-content:hover {
    background: #fef0f0;
    color: #e53e3e;
}

/* 子菜单下拉框美化 */
.nav-submenu :deep(.el-menu--popup) {
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid #e8e8e8;
    padding: 8px 0;
    margin-top: 8px;
}

/* 子菜单项样式 */
.submenu-item {
    padding: 12px 20px !important;
    margin: 4px 8px !important;
    border-radius: 8px !important;
    transition: all 0.3s ease !important;
    height: auto !important;
    line-height: normal !important;
    font-weight: 500 !important;
}

.submenu-item:hover {
    background: #f0fffe !important;
    color: #4ecdc4 !important;
    transform: translateX(4px) !important;
}

/* 为子菜单项添加图标 */
.submenu-item:before {
    content: "•";
    color: #4ecdc4;
    font-weight: bold;
    margin-right: 8px;
}

/* 消息通知样式 */
.message-trigger {
    display: flex;
    align-items: center;
    height: 70px;
    padding: 0 16px;
    cursor: pointer;
    border-radius: 10px;
    transition: all 0.3s ease;
}

.message-trigger:hover {
    background: #f0fffe;
}

.message-badge {
    position: relative;
}

.notification-icon {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    border: 2px solid #e8e8e8;
}

.notification-icon.has-notification {
    background: #fff0f0;
    border-color: #ffcccb;
}

.notification-icon .el-icon {
    color: #4ecdc4;
}

.has-notification .el-icon {
    color: #f56c6c;
}

/* 消息弹窗样式 */
.message-popup {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.popup-header {
    padding: 20px 24px;
    background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
    color: white;
}

.popup-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
}

.message-tabs {
    border: none;
    background: white;
}

.message-tabs :deep(.el-tabs__header) {
    margin: 0;
    background: #f8f9fa;
}

.message-tabs :deep(.el-tabs__item) {
    font-weight: 500;
}

.message-list {
    max-height: 400px;
    overflow-y: auto;
    padding: 0;
}

.message-item {
    position: relative;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.message-item:hover {
    background: #f8f9fa;
}

.message-item.unread {
    background: #f0fffe;
    border-left: 4px solid #4ecdc4;
}

.message-item.sent-message {
    border-left: 4px solid #409eff;
}

.message-indicator {
    position: absolute;
    top: 18px;
    left: 8px;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #f56c6c;
}

.message-content {
    margin-bottom: 8px;
    line-height: 1.5;
    font-weight: 500;
    color: #2c3e50;
}

.message-time {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #9ca3af;
}

.empty-message {
    padding: 40px 20px;
    text-align: center;
    color: #9ca3af;
}

.empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
    .center-menu {
        position: static;
        transform: none;
        margin: 0 auto;
    }

    .brand-section {
        position: static;
    }

    .right-menu {
        position: static;
    }

    .nav-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
}

@media (max-width: 768px) {
    .nav-bar-container {
        height: 60px;
    }

    .nav-bar {
        height: 60px;
        padding: 0 16px;
    }

    .brand-item,
    .nav-item,
    .nav-submenu,
    .logout-item {
        height: 60px !important;
        line-height: 60px !important;
    }

    .nav-submenu :deep(.el-sub-menu__title) {
        height: 60px !important;
        line-height: 60px !important;
    }

    .message-trigger {
        height: 60px;
    }

    .brand-text {
        font-size: 16px;
    }

    .nav-content {
        padding: 8px 12px;
        height: 44px;
    }

    .nav-content span {
        display: none;
    }

    .center-menu {
        gap: 4px;
    }

    .right-menu {
        gap: 4px;
    }

    /* 移动端显示展开图标 */
    .expand-icon {
        display: block !important;
    }
}

/* 滚动条美化 */
.message-list::-webkit-scrollbar {
    width: 6px;
}

.message-list::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

.logo-item {
    padding: 0 10px !important;
    height: 60px !important;
    display: flex;
    align-items: center;
}

.logo-image {
    height: 36px;
    /* 根据实际图标调整 */
    width: auto;
    object-fit: contain;
}
</style>