<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from './authenticationServices/authenticationService'; // Vì cùng nằm trong authentication nên dùng ./
import axios from 'axios';
import Swal from 'sweetalert2';
import axiosClient from '@/services/axiosClient';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const confirmPassword = ref('');

const form = ref({
  matKhauCu: '',
  matKhauMoi: ''
});

const handleUpdate = async () => {
  const { matKhauCu, matKhauMoi } = form.value;
  const confirmPass = confirmPassword.value;

  // Cấu hình dùng chung cho Swal báo lỗi
  const errorConfig = {
    icon: 'error',
    iconColor: '#7D161A',
    confirmButtonColor: '#800000',
    title: 'Thông báo lỗi'
  };

  // 1. Kiểm tra trống hoặc chỉ toàn khoảng trắng
  if (!matKhauCu?.trim() || !matKhauMoi?.trim() || !confirmPass?.trim()) {
    Swal.fire({
      ...errorConfig,
      text: 'Vui lòng điền đầy đủ thông tin vào các trường!'
    });
    return;
  }

  // 2. Kiểm tra khoảng trắng ở giữa mật khẩu
  if (/\s/.test(matKhauMoi)) {
    Swal.fire({
      ...errorConfig,
      icon: 'warning',
      text: 'Mật khẩu mới không được chứa khoảng trắng!'
    });
    return;
  }

  // 3. Kiểm tra mật khẩu mới không được trùng mật khẩu cũ
  if (matKhauMoi === matKhauCu) {
    Swal.fire({
      ...errorConfig,
      icon: 'warning',
      text: 'Mật khẩu mới phải khác mật khẩu hiện tại!'
    });
    return;
  }

  // 4. Kiểm tra độ dài tối thiểu
  if (matKhauMoi.length < 6 || matKhauMoi.length > 50) {
    Swal.fire({
      ...errorConfig,
      icon: 'warning',
      text: 'Mật khẩu mới phải có độ dài từ 6 đến 50 ký tự!'
    });
    return;
  }

  // 5. Kiểm tra mật khẩu xác nhận
  if (matKhauMoi !== confirmPass) {
    Swal.fire({
      ...errorConfig,
      text: 'Xác nhận mật khẩu mới không khớp!'
    });
    return;
  }

  // --- Gọi API ---
  loading.value = true;
  try {
    const response = await axiosClient.post('/tai-khoan/doi-mat-khau', {
        matKhauCu: matKhauCu.trim(),
        matKhauMoi: matKhauMoi.trim()
    });
    
    // Thông báo thành công
    await Swal.fire({
        icon: 'success',
        iconColor: '#7D161A',
        title: 'Thành công',
        text: 'Mật khẩu đã được thay đổi! Hệ thống sẽ đăng xuất.',
        showConfirmButton: false,
        timer: 2000
    });
    
    authStore.logout();
    router.push('/login');

  } catch (error) {
    console.error("Lỗi đổi mật khẩu:", error);
    loading.value = false;
    
    // Lấy message từ backend trả về (ví dụ: "Mật khẩu cũ không đúng")
    let msg = "Kết nối máy chủ thất bại, vui lòng thử lại sau!";
    if (error.response) {
        msg = error.response.data?.message || error.response.data || "Mật khẩu hiện tại không chính xác!";
    }
    
    Swal.fire({
        ...errorConfig,
        title: 'Đổi mật khẩu thất bại',
        text: msg
    });
  }
};
</script>

<template>
  <div class="change-password-page">
    <div class="form-container">
      <div class="form-header">
        <div class="icon-badge">
          <i class="fa-solid fa-user-shield"></i>
        </div>
        <h2>THAY ĐỔI MẬT KHẨU</h2>
        <p>Cập nhật mật khẩu để bảo vệ tài khoản của bạn</p>
      </div>

      <form @submit.prevent="handleUpdate" class="password-form">
        <div class="input-group">
          <label>Mật khẩu hiện tại</label>
          <div class="input-wrapper">
            <i class="fa-solid fa-lock icon-left"></i>
            <input type="password" v-model="form.matKhauCu" placeholder="Nhập mật khẩu cũ"  />
          </div>
        </div>

        <div class="input-group">
          <label>Mật khẩu mới</label>
          <div class="input-wrapper">
            <i class="fa-solid fa-key icon-left"></i>
            <input type="password" v-model="form.matKhauMoi" placeholder="Tối thiểu 6 ký tự"  />
          </div>
        </div>

        <div class="input-group">
          <label>Xác nhận mật khẩu</label>
          <div class="input-wrapper">
            <i class="fa-solid fa-shield-check icon-left"></i>
            <input type="password" v-model="confirmPassword" placeholder="Nhập lại mật khẩu mới"  />
          </div>
        </div>

        <button type="submit" class="btn-update" :disabled="loading">
          <span v-if="loading"><i class="fas fa-circle-notch fa-spin"></i> ĐANG XỬ LÝ...</span>
          <span v-else>CẬP NHẬT NGAY</span>
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.change-password-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 80px);
  background: #f0f2f5;
  padding: 20px;
}

.form-container {
  background: #ffffff;
  width: 100%;
  max-width: 450px; /* Độ rộng vừa phải cho Form */
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.08);
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.icon-badge {
  font-size: 40px;
  color: #800000;
  margin-bottom: 10px;
}

.form-header h2 {
  font-size: 22px;
  font-weight: 800;
  color: #333;
  margin-bottom: 5px;
  letter-spacing: 0.5px;
}

.form-header p {
  color: #777;
  font-size: 14px;
}

/* Group nhập liệu */
.input-group {
  margin-bottom: 20px;
  width: 100%;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 13px;
  color: #555;
  text-transform: uppercase;
}

.input-wrapper {
  position: relative;
  width: 100%; /* Đảm bảo wrapper chiếm hết chiều rộng */
}

.icon-left {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #800000;
  font-size: 18px;
}

/* Ô nhập liệu dài bằng nút bấm */
.input-wrapper input {
  width: 100%;
  padding: 15px 15px 15px 50px; /* Padding trái rộng để chừa chỗ cho icon */
  border: 1.5px solid #e1e1e1;
  border-radius: 12px; /* Bo góc hiện đại */
  font-size: 15px;
  transition: all 0.3s ease;
  box-sizing: border-box; /* Quan trọng: Để padding không làm tăng chiều rộng thực tế */
  background-color: #f9f9f9;
}

.input-wrapper input:focus {
  background-color: #fff;
  border-color: #800000;
  box-shadow: 0 0 0 4px rgba(128, 0, 0, 0.1);
  outline: none;
}

/* Nút bấm dài bằng ô nhập liệu */
.btn-update {
  width: 100%;
  padding: 16px;
  background-color: #800000;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
  box-shadow: 0 4px 12px rgba(128, 0, 0, 0.2);
}

.btn-update:hover {
  background-color: #600000;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(128, 0, 0, 0.3);
}

.btn-update:active {
  transform: translateY(0);
}

.btn-update:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}

/* Responsive cho mobile */
@media (max-width: 480px) {
  .form-container {
    padding: 30px 20px;
  }
}
</style>