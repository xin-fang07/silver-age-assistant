// ============================================================
// Vite 构建配置文件
// 银发智能生活助手 - 前端工程化构建配置
// ============================================================

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

export default defineConfig({
  // 注册 Vue 插件
  plugins: [
    vue(),
    Components({
      resolvers: [ElementPlusResolver({ importStyle: false })]
    })
  ],

  // 路径解析配置
  resolve: {
    alias: {
      // 将 @ 映射到 src 目录，方便模块导入
      '@': resolve(__dirname, 'src')
    }
  },

  // ECharts 已独立为懒加载路由块；其压缩后约 180KB，600KB 为未压缩告警阈值。
  build: {
    chunkSizeWarningLimit: 600,
    cssCodeSplit: true
  },

  optimizeDeps: {
    esbuildOptions: {
      target: 'es2020'
    }
  },

  // 开发服务器配置
  server: {
    // 开发服务器端口
    port: 5173,
    // 代理配置：将 /api 开头的请求转发到后端服务器
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true
      }
    }
  }
})
