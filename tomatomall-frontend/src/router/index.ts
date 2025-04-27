import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '../pages/index.vue';
import CartPage from "../pages/CartPage.vue";
import WarehousePage from "../pages/WarehousePage.vue";
import Register from "@/pages/Register.vue";
import Login from "@/pages/Login.vue";
import User from "@/pages/User.vue";
import Product from "@/pages/ProductDetails.vue";
import Checkout from "@/pages/Checkout.vue";
import Advertisement from '@/pages/advertisement.vue';

const routes = [
    {
        path: '/',
        name: 'index',
        component: HomePage
    },
    {
        path: '/cart',
        name: 'cart',
        component: CartPage,
    },
    {
        path: '/warehouse',
        name: 'warehouse',
        component: WarehousePage,
    },
    {
        path: '/register',
        name: 'register',
        mata: { title: '用户注册' },
        component: Register,
    },
    {
        path: '/login',
        name: 'login',
        mata: { title: '用户登录' },
        component: Login,
    },
    {
        path: '/user',
        name: 'user',
        mata: { title: '用户主页' },
        component: User,
    },
    {
        path: '/product/:id',
        name: 'product',
        mata: { title: '商品详情' },
        component: Product,
        props: true,
    },
    {
        path: '/checkout',
        name: 'checkout',
        mata: { title: '确认订单' },
        component: Checkout,
    },
    {
        path: '/advertisements',
        name: 'advertisements',
        mata: { title: '广告管理' },
        component: Advertisement,
    }
    //其他路由配置
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

// default的作用是 当前模块的主要导出值  当然一个模块只能有一个
// 就是import时无需加大括号 且可以随便起名字
export default router;
