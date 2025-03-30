/**
 * @description 扩展Vue-Router-Meta的类型，防止Typescript报错
 */
//让 TypeScript 能够识别你在路由配置中自定义的 meta 字段
declare module 'vue-router' {
    interface RouteMeta {
        permission?: Array<string>
        title?: string
    }
}

export {}