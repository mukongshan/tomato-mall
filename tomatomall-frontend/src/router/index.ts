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
import Shops from '@/pages/shops.vue';
import ShopCreate from '@/pages/shopCreate.vue';
import shopManage from '@/pages/shopManage.vue';
import shopDetail from '@/pages/shopDetail.vue';
import myShop from '@/pages/myShop.vue';
import order from '@/pages/order.vue';
import couponManage from '@/pages/couponManage.vue';

const routes = [
    {
        path: '/',
        name: 'index',
        meta: { title: '首页' },
        component: HomePage
    },
    {
        path: '/cart',
        name: 'cart',
        meta: { title: '购物车' },
        component: CartPage,
    },
    {
        path: '/warehouse/:id',
        name: 'warehouse',
        meta: { title: '商品管理' },
        component: WarehousePage,
        props: true,
    },
    {
        path: '/register',
        name: 'register',
        meta: { title: '用户注册' },
        component: Register,
    },
    {
        path: '/login',
        name: 'login',
        meta: { title: '用户登录' },
        component: Login,
    },
    {
        path: '/user',
        name: 'user',
        meta: { title: '用户主页' },
        component: User,
    },
    {
        path: '/product/:id',
        name: 'product',
        meta: { title: '商品详情' },
        component: Product,
        props: true,
    },
    {
        path: '/checkout',
        name: 'checkout',
        meta: { title: '确认订单' },
        component: Checkout,
    },
    {
        path: '/advertisements',
        name: 'advertisements',
        meta: { title: '广告管理' },
        component: Advertisement,
    },
    {
        path: '/shops',
        name: 'shops',
        meta: { title: '全部商店' },
        component: Shops,
    },
    {
        path: '/shopCreate',
        name: 'shopCreate',
        meta: { title: '创建商店' },
        component: ShopCreate,
    },
    {
        path: '/shopManage',
        name: 'shopManage',
        meta: { title: '店铺管理' },
        component: shopManage,
    },
    {
        path: '/shop/detail/:shopId',
        name: 'shopDetail',
        meta: { title: '店铺详情' },
        component: shopDetail,
        props: true,
    },
    {
        path: '/myshop/:id',
        name: 'myshop',
        component: myShop,
        meta: { title: '我的店铺' },
        props: true,
    },
    {
        path: '/order',
        name: 'order',
        component: order,
        meta: { title: '全部订单' },
    },
    {
        path: '/couponManage',
        name: 'couponManage',
        component: couponManage,
        meta: { title: '优惠券管理' },
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
