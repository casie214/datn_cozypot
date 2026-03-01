<template>
  <div class="processing-container">
    </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axiosClient from '@/services/axiosClient';
import { useAuthStore } from './authenticationServices/authenticationService';
import Swal from 'sweetalert2';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
  const code = route.query.code;

  if (code) {
    Swal.fire({
      title: 'Đang xác thực Google',
      text: 'Vui lòng đợi trong giây lát...',
      allowOutsideClick: false,
      showConfirmButton: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

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

      Swal.close();
      router.push('/');

    } catch (error) {
      Swal.fire({
        icon: 'error',
        title: 'Đăng nhập thất bại',
        text: error.response?.data || 'Có lỗi xảy ra khi xác thực Google.',
        confirmButtonColor: '#8B1A1A'
      }).then(() => {
        router.push('/login');
      });
    }
  } else {
    router.push('/login');
  }
});
</script>

<style scoped>
.processing-container {
  height: 100vh;
  background-color: #fff;
}
</style>