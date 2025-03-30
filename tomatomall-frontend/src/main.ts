import './assets/main.css'
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import { createApp } from 'vue'
import App from './App.vue'
import router from './router';

//创建一个新的Vue应用实例，使用ElementPlus插件和路由，然后挂载到页面上id为'app'的元素上。
createApp(App).use(ElementPlus).use(router).mount('#app')
