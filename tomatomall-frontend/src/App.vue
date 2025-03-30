<template>
  <div class="app-container">
    <NavigationBar />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import NavigationBar from "@/components/NavigationBar.vue";
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh; /* 关键：确保容器至少填满整个视口高度 */
}

.main-content {
  flex: 1; /* 关键：使内容区域填满剩余空间 */
  overflow: auto; /* 可选：内容超出时添加滚动条 */
}
</style>