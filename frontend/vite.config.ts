import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发和本地预览共用 API 代理，浏览器使用同域会话 Cookie。
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')
  const proxy = { '/api': env.VITE_API_TARGET || 'http://127.0.0.1:8080' }
  return { plugins: [vue()], server: { proxy }, preview: { proxy } }
})
