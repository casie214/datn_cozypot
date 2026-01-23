import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
<<<<<<< HEAD
      '@': fileURLToPath(new URL('./src', import.meta.url))
=======
      // Cách viết chuẩn cho dự án Vite hiện đại thay vì dùng path.resolve(__dirname)
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
    }
  }
})