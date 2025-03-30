<script setup>
import { ElMenu, ElMenuItem } from "element-plus";
</script>

<template>
  <div class="nav-bar-container">
    <el-menu
        class="nav-bar"
        mode="horizontal"
        :router="true"
    >
      <div class="mid-items">
        <el-menu-item index="/">番茄书城</el-menu-item>
        <el-menu-item index="/cart">购物车</el-menu-item>
        <el-menu-item index="/warehouse">库存管理</el-menu-item>
      </div>

      <div class="right-items">
        <el-menu-item index="/login">登录</el-menu-item>
        <el-menu-item index="/register">注册</el-menu-item>
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