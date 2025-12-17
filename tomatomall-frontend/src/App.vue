<template>
  <div class="app-container">
    <NavigationBar />
    <div class="main-container">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import NavigationBar from "@/components/NavigationBar.vue";
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-container {
  width: 100%;
  flex: 1;
  overflow: auto;
  padding-top: 60px;
}
</style>