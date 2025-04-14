import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 更简单的路径处理方式
// Element UI 自动导入支持
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    })],
  server: {
    port: 3000,   //设定前端运行的端口
    open: true,
  },
  resolve: {
    alias: {
      // 这里使用pathToFileURL也不生效
      // 使用这个也会显示报错 但是能运行
      /*
      问题在于vite是在运行时生效  TypeScript是在编译时生效
      所以报错是因为编译时无法识别到  因此在tsconfig里面配置会生效
       */
      '@': path.resolve(__dirname, './src') // 更稳定的路径写法
    }
  },
  base: './'
})
