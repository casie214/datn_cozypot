<script setup>
import { ref, onMounted } from 'vue'
import axiosClient from '../services/axiosClient'

const serverStatus = ref('Checking connection...')
const isConnected = ref(false)

const checkServer = async () => {
  try {
    const response = await axiosClient.get('/status')
    serverStatus.value = response.data.message
    isConnected.value = true
  } catch (error) {
    serverStatus.value = 'Connection Failed: ' + error.message
    isConnected.value = false
  }
}

onMounted(() => {
  checkServer()
})
</script>

<template>
  <div class="container">
    <h1>Project Base Setup</h1>
    <div :class="['status-box', isConnected ? 'online' : 'offline']">
      <h2>Server Status</h2>
      <p>{{ serverStatus }}</p>
    </div>
  </div>
</template>

<style scoped>
.container { font-family: Arial, sans-serif; text-align: center; margin-top: 50px; }
.status-box { padding: 20px; border-radius: 8px; display: inline-block; color: white; }
.online { background-color: #2ecc71; } /* Màu xanh */
.offline { background-color: #e74c3c; } /* Màu đỏ */
</style>