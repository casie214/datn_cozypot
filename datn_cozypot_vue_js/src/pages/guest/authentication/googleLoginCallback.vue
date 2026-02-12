<template>
  <div class="processing-login">
    <h2>Đang xử lý đăng nhập Google...</h2>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axiosClient from '@/services/axiosClient';
import { useAuthStore } from './authenticationServices/authenticationService';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
  const code = route.query.code; 

  if (code) {
    try {
      const res = await axiosClient.post('/auth/google', { code });
      
      const { accessToken, refreshToken, role, ...userInfo } = res.data;

      authStore.token = accessToken;
      authStore.role = role;
      authStore.user = userInfo;

      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('role', role);
      localStorage.setItem('user', JSON.stringify(userInfo));

      router.push('/');

    } catch (error) {
      console.error("Lỗi login google:", error);
      router.push('/login'); 
    }
  } else {
    router.push('/login');
  }
});
</script>