<script setup lang="ts">
import { onMounted, } from "vue";
import { ElMenu, ElMenuItem } from "element-plus";
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
        <el-menu class="nav-bar" mode="horizontal" :router="true">
            <div class="mid-items">
                <el-menu-item index="/">番茄书城</el-menu-item>
                <el-menu-item index="/cart">购物车</el-menu-item>
                <el-menu-item index="/shops">全部店铺</el-menu-item>
                <el-menu-item v-if="isAdmin || isShopOwner || isStaff" index="/warehouse">商品管理</el-menu-item>
                <el-menu-item v-if="isAdmin || isShopOwner" index="/advertisements">广告管理</el-menu-item>
            </div>


            <div class="right-items">
                <template v-if="isLogin">
                    <el-menu-item index="/user">个人中心</el-menu-item>
                    <el-menu-item @click="Logout">退出登录</el-menu-item>
                    <el-menu-item index="/shopManage" v-if="isAdmin">商店管理</el-menu-item>
                    <el-menu-item @click="navigateToMyShop" v-else-if="isShopOwner">我的店铺</el-menu-item>
                    <el-menu-item index="/shopCreate" v-else>开店</el-menu-item>
                    <el-menu-item>消息</el-menu-item>

                </template>

                <template v-else>
                    <el-menu-item index="/login">登录</el-menu-item>
                    <el-menu-item index="/register">注册</el-menu-item>
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
    z-index: 1000;
    height: 60px;
}

.nav-bar {
    display: grid;
    grid-template-columns: 40% 20% 40%;
    width: 100%;
    height: 100%;
    position: relative;
}

.mid-items {
    grid-column: 2;
    display: flex;
    justify-content: center;
    gap: 10px;
}

.right-items {
    position: absolute;
    right: 10%;
    top: 0;
    display: flex;
}

.el-menu-item {
    height: 100%;
    display: flex;
    align-items: center;
}
</style>