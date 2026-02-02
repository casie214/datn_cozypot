<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-box shadow-lg">
      <div class="modal-header-custom">
        <h5 class="m-0 title-modal">
          <i class="fas fa-address-card me-2"></i>
          CHI TIẾT HỒ SƠ KHÁCH HÀNG
        </h5>
        <span class="close-x" @click="$emit('close')">&times;</span>
      </div>

      <div class="modal-body-custom">
        <div class="container-fluid">
          <div class="row">
            <div class="col-md-4 text-center border-end section-left">
              <label class="form-label-custom d-block mb-3">Ảnh khách hàng</label>
              <div class="avatar-wrapper mb-3">
                <img :src="getAvatarUrl(formData.anhDaiDien) || 'https://via.placeholder.com/150'" 
                     alt="Avatar" class="avatar-img" />
              </div>
              
              <div class="staff-meta mt-3">
                <div class="status-label mb-2">
                   <span class="badge" :class="statusClass">{{ statusText }}</span>
                </div>
                <small class="text-muted">Mã khách hàng:</small>
                <p class="fw-bold mb-1">{{ formData.maKhachHang || 'N/A' }}</p>
                <small class="text-muted">ID hệ thống: #{{ formData.id }}</small>
              </div>
            </div>

            <div class="col-md-8 info-content">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label-custom">Họ và tên</label>
                  <input type="text" class="form-control custom-input readonly-style" :value="formData.tenKhachHang" readonly>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Số điện thoại</label>
                  <input type="text" class="form-control custom-input readonly-style" :value="formData.soDienThoai" readonly>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Email</label>
                  <input type="text" class="form-control custom-input readonly-style" :value="formData.email || '---'" readonly>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Ngày sinh</label>
                  <input type="text" class="form-control custom-input readonly-style" :value="formatDate(formData.ngaySinh)" readonly>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Giới tính</label>
                  <input type="text" class="form-control custom-input readonly-style" :value="formData.gioiTinh ? 'Nam' : 'Nữ'" readonly>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Tên đăng nhập</label>
                  <input type="text" class="form-control custom-input readonly-style" :value="formData.tenDangNhap" readonly>
                </div>

                <div class="col-md-12">
                  <label class="form-label-custom">Địa chỉ thường trú</label>
                  <textarea class="form-control custom-input readonly-style" rows="2" readonly>{{ formData.diaChi || 'Chưa cập nhật' }}</textarea>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer-custom">
        <button class="btn btn-main-custom" @click="$emit('close')">Đóng cửa sổ</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, computed } from 'vue';
import clientService from '@/services/clientService';
import dayjs from 'dayjs';

const props = defineProps({
  clientId: { type: [Number, String], required: true }
});

const formData = reactive({});

const getAvatarUrl = (fileName) => {
  if (!fileName) return null;
  return `http://localhost:8080/uploads/customers/${fileName}`;
};

const formatDate = (date) => {
  return date ? dayjs(date).format('DD/MM/YYYY') : '---';
};

const statusText = computed(() => {
  return formData.trangThai === 1 ? 'Đang hoạt động' : 'Ngừng hoạt động';
});

const statusClass = computed(() => {
  return formData.trangThai === 1 ? 'bg-success' : 'bg-danger';
});

onMounted(async () => {
  try {
    const res = await clientService.getDetail(props.clientId);
    Object.assign(formData, res.data);
  } catch (error) {
    console.error("Lỗi tải chi tiết khách hàng:", error);
  }
});
</script>

<style scoped>
/* Đồng nhất hoàn toàn style với bản Nhân viên của bạn */
.modal-overlay { 
  position: fixed; inset: 0; background: rgba(0,0,0,0.6); 
  display: flex; justify-content: center; align-items: center; 
  z-index: 9999; backdrop-filter: blur(4px); 
}

.modal-box { 
  background: #fff; width: 1000px; border-radius: 20px; 
  overflow: hidden; 
}

.modal-header-custom { 
  padding: 20px 30px; border-bottom: 1px solid #f0f0f0; 
  display: flex; justify-content: space-between; align-items: center; 
}

.title-modal { color: #800000; font-weight: 800; font-size: 1.2rem; }
.close-x { font-size: 30px; cursor: pointer; color: #bbb; }

.modal-body-custom { padding: 30px; max-height: 80vh; overflow-y: auto; }

.avatar-wrapper { 
  width: 180px; height: 180px; border-radius: 50%; border: 4px solid #f8f9fa; 
  margin: auto; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }

.form-label-custom { 
  font-weight: 700; font-size: 11px; color: #555; 
  text-transform: uppercase; margin-bottom: 5px; 
}

.custom-input { 
  border-radius: 8px; padding: 10px; font-size: 14px; 
  border: 1px solid #ddd; width: 100%; 
}

/* Style cho trạng thái chỉ đọc giống hệt bản nhân viên */
.readonly-style {
  background-color: #f8f9fa !important;
  color: #495057 !important;
  border-color: #e9ecef !important;
  cursor: default;
  box-shadow: none !important;
}

.modal-footer-custom { 
  padding: 20px 30px; border-top: 1px solid #f0f0f0; 
  display: flex; justify-content: flex-end; 
}

.btn-main-custom { 
  background: #800000; color: #fff; border: none; 
  padding: 10px 30px; border-radius: 8px; font-weight: 700; 
}

.badge { padding: 5px 12px; border-radius: 4px; font-size: 11px; }
</style>