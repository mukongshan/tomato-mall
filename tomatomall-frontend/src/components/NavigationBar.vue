<script setup lang="ts">
import { onMounted } from "vue";
import { ElMenu, ElMenuItem } from "element-plus";
import { HomeFilled, ShoppingCart, Shop, Setting, User, Bell, SwitchButton, Edit } from '@element-plus/icons-vue'
import router from "@/router";
import { isLogin, checkRole, isAdmin, isShopOwner, isStaff } from "./LoginEvent";
import { getUserDetails } from "@/api/account";

const checkLogin = () => {
    const token = sessionStorage.getItem('token');
    if (token) {
        isLogin.value = true;
    } else {
        isLogin.value = false;
    }
};

const Logout = () => {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('role');
    isLogin.value = false;
    router.push("/login"); // 跳转到登录页面
};
const navigateToMyShop = async () => {
    const username = sessionStorage.getItem('username') as string;
    const response = await getUserDetails(username);
    const id = response.data.data.id;
};

onMounted(checkLogin);
onMounted(checkRole);
</script>

<template>
    <div class="nav-bar-container">
        <el-menu mode="horizontal" :router="true" class="nav-bar" background-color="#fff" text-color="#333"
            active-text-color="#409EFF">
            <!-- 中间 -->
            <div class="center-menu">
                <el-menu-item index="/" class="left-item">
                    <el-icon>
                        <HomeFilled />
                    </el-icon>
                    <span>番茄书城</span>
                </el-menu-item>
                <el-menu-item index="/cart">
                    <el-icon>
                        <ShoppingCart />
                    </el-icon>
                    <span>购物车</span>
                </el-menu-item>
                <el-menu-item index="/shops">
                    <el-icon>
                        <Shop />
                    </el-icon>
                    <span>全部店铺</span>
                </el-menu-item>
                <el-sub-menu index="management" v-if="isAdmin || isShopOwner || isStaff">
                    <template #title>
                        <el-icon>
                            <Setting />
                        </el-icon>
                        <span>管理</span>
                    </template>
                    <el-menu-item index="/warehouse">商品管理</el-menu-item>
                    <el-menu-item index="/shop-management" v-if="isAdmin || isShopOwner">商店管理</el-menu-item>
                    <el-menu-item index="/advertisements" v-if="isAdmin || isShopOwner">广告管理</el-menu-item>
                </el-sub-menu>
            </div>

            <!-- 右侧 -->
            <div class="right-menu">
                <template v-if="isLogin">
                    <el-menu-item index="/user">
                        <el-icon>
                            <User />
                        </el-icon>
                        <span>个人中心</span>
                    </el-menu-item>
                    <el-menu-item index="/user">
                        <el-icon>
                            <Bell />
                        </el-icon>
                        <span>消息</span>
                    </el-menu-item>
                    <el-menu-item @click="Logout">
                        <el-icon>
                            <SwitchButton />
                        </el-icon>
                        <span>退出登录</span>
                    </el-menu-item>
                </template>
                <template v-else>
                    <el-menu-item index="/login">
                        <el-icon>
                            <User />
                        </el-icon>
                        <span>登录</span>
                    </el-menu-item>
                    <el-menu-item index="/register">
                        <el-icon>
                            <Edit />
                        </el-icon>
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

/* el-menu整体样式 */
.nav-bar {
    position: relative;
    height: 60px;
    padding: 0 20px;
    white-space: nowrap;
}

/* 左侧按钮正常流 */
.left-item {
    float: left;
}

/* 中间菜单用绝对定位，水平居中 */
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

/* 右侧菜单用绝对定位靠右 */
.right-menu {
    position: absolute;
    right: 20px;
    top: 0;
    height: 60px;
    display: flex;
    align-items: center;
    gap: 10px;
}

/* 统一菜单项高度 */
.el-menu-item,
.el-sub-menu__title {
    height: 60px !important;
    padding: 0 12px !important;
    display: flex;
    align-items: center;
    font-weight: 500;
    font-size: 14px;
}
</style>
