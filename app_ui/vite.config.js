import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Pages from 'vite-plugin-pages'
// https://vitejs.dev/config/
export default defineConfig({
  server: {
    host: '0.0.0.0',
    port: 80, // 可以选择你希望的端口号
    cors: true, // 如果需要支持跨域请求，可以设置为true
    proxy: {
      '/druid': {
        target: 'http://localhost:9090/api/druid',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/druid/, '')
      }
    },
  },

  base:'/',
  plugins: [
    vue(),


  ],

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),

    }
  }
})
