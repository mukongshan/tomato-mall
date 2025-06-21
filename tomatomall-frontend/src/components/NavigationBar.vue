<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMenu, ElMenuItem, ElPopover, ElTabPane, ElTabs, ElBadge, ElMessage } from "element-plus";
import { HomeFilled, ShoppingCart, Shop, Setting, User, Bell, SwitchButton, Edit, CirclePlus } from '@element-plus/icons-vue'
import router from "@/router";
import { isLogin, checkRole, isAdmin, isShopOwner, isStaff, isCustomer, messageLoad, unreadCount, receivedMessages, sentMessages } from "./LoginEvent";
import { getUserDetails, getUserRoleById } from "@/api/account";
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
    const shopId = Number(sessionStorage.getItem('shopId'));
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
            <!-- 中间 -->
            <div class="center-menu">
                <el-menu-item index="/" class="left-item">
                    <el-icon> <HomeFilled /> </el-icon>
                    <span>番茄书城</span>
                </el-menu-item>
                <el-menu-item index="/cart">
                    <el-icon> <ShoppingCart /> </el-icon>
                    <span>购物车</span>
                </el-menu-item>
                <el-menu-item index="/shops">
                    <el-icon> <Shop /> </el-icon>
                    <span>全部店铺</span>
                </el-menu-item>
                <el-sub-menu index="management" v-if="isAdmin || isShopOwner || isStaff">
                    <template #title>
                        <el-icon> <Setting /> </el-icon>
                        <span>管理</span>
                    </template>
                    <el-menu-item v-if="isShopOwner || isStaff" @click="navigateToWarehouse">商品管理</el-menu-item>
                    <el-menu-item index="/shopManage" v-if="isAdmin">商店管理</el-menu-item>
                    <el-menu-item index="/advertisements" v-if="isAdmin || isShopOwner">广告管理</el-menu-item>
                </el-sub-menu>
            </div>

            <!-- 右侧 -->
            <div class="right-menu">
                <template v-if="isLogin">
                    <el-menu-item index="/shopCreate" v-if="isCustomer">
                        <el-icon> <CirclePlus /> </el-icon>
                        <span>我要开店</span>
                    </el-menu-item>

                    <el-sub-menu index="mine">
                        <template #title>
                            <el-icon> <Setting /> </el-icon>
                            <span>我的</span>
                        </template>
                        <el-menu-item index="/user">个人中心</el-menu-item>
                        <el-menu-item index="/order">全部订单</el-menu-item>
                        <el-menu-item @click="navigateToMyShop" v-if="isShopOwner">我的店铺</el-menu-item>
                    </el-sub-menu>

                    <!-- 消息通知改为Popover -->
                    <el-popover v-model:visible="messagePopoverVisible" placement="bottom-end" :width="400"
                        trigger="hover">
                        <template #reference>
                            <div class="message-trigger">
                                <el-badge
                                    :value="unreadCount"
                                    :max="99"
                                    :hidden="unreadCount === 0"
                                    class="badge-item"
                                >
                                    <el-icon :size="20">
                                        <Bell />
                                    </el-icon>
                                </el-badge>
                            </div>
                        </template>

                        <el-tabs type="border-card" stretch>
                            <el-tab-pane label="收到的消息">
                                <div class="message-list">
                                    <div v-for="message in receivedMessages" :key="'received-' + message.id"
                                        class="message-item" :class="{ 'unread': !message.isRead }"
                                        @click="!message.isRead && markAsRead(message.id)">
                                        <div class="message-content" @click="NavigateToMessage(message)">
                                            {{ message.content }}
                                        </div>
                                        <div class="message-time">{{ message.createdTime }}</div>
                                    </div>
                                    <div v-if="receivedMessages.length === 0" class="empty-message">
                                        暂无消息
                                    </div>
                                </div>
                            </el-tab-pane>
                            <el-tab-pane label="发出的消息">
                                <div class="message-list">
                                    <div v-for="message in sentMessages" :key="'sent-' + message.id"
                                        class="message-item">
                                        <div class="message-content">{{ message.content }}</div>
                                        <div class="message-time">{{ message.createdTime }}</div>
                                    </div>
                                    <div v-if="sentMessages.length === 0" class="empty-message">
                                        暂无消息
                                    </div>
                                </div>
                            </el-tab-pane>
                        </el-tabs>
                    </el-popover>

                    <el-menu-item @click="Logout">
                        <el-icon> <SwitchButton /> </el-icon>
                        <span>退出登录</span>
                    </el-menu-item>
                </template>
                <template v-else>
                    <el-menu-item index="/login">
                        <el-icon> <User /> </el-icon>
                        <span>登录</span>
                    </el-menu-item>
                    <el-menu-item index="/register">
                        <el-icon> <Edit /> </el-icon>
                        <span>注册</span>
                    </el-menu-item>
                </template>
            </div>
        </el-menu>
    </div>
</template>

<style scoped>
.nav-bar-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 60px;
    z-index: 1000;
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-bar {
    position: relative;
    height: 60px;
    padding: 0 20px;
    white-space: nowrap;
}

.left-item {
    float: left;
}

.center-menu {
    position: absolute;
    left: 50%;
    top: 0;
    height: 60px;
    display: flex;
    align-items: center;
    transform: translateX(-50%);
    white-space: nowrap;
    gap: 10px;
}

.right-menu {
    position: absolute;
    right: 20px;
    top: 0;
    height: 60px;
    display: flex;
    align-items: center;
    gap: 10px;
}

.el-menu-item,
.el-sub-menu__title {
    height: 60px !important;
    padding: 0 12px !important;
    display: flex;
    align-items: center;
    font-weight: 500;
    font-size: 14px;
}

/* 消息触发器样式 */
.message-trigger {
    display: flex;
    align-items: center;
    height: 60px;
    padding: 0 12px;
    cursor: pointer;
    transition: all 0.3s;
}

.message-trigger:hover {
    color: var(--el-color-primary);
}

.badge-item {
    margin-top: 4px;
}

/* 消息列表样式 */
.message-list {
    max-height: 400px;
    overflow-y: auto;
    padding: 8px 0;
}

.message-item {
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color-light);
    cursor: pointer;
    transition: background-color 0.3s;
}

.message-item:hover {
    background-color: var(--el-color-primary-light-9);
}

.message-item.unread {
    background-color: var(--el-color-primary-light-9);
    font-weight: 500;
}

.message-content {
    margin-bottom: 4px;
    line-height: 1.5;
}

.message-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
}

.empty-message {
    padding: 16px;
    text-align: center;
    color: var(--el-text-color-secondary);
}
</style>