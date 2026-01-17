<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content-custom shadow">
      <div class="modal-header-custom border-bottom pb-2 mb-4 d-flex justify-content-between align-items-center">
        <h5 class="fw-bold text-dark mb-0">
          <i class="fas" :class="clientId ? 'fa-pen' : 'fa-plus'"></i> 
          {{ clientId ? 'Cập nhật khách hàng' : 'Thêm khách hàng mới' }}
        </h5>
        <button class="btn-close shadow-none" @click="$emit('close')"></button>
      </div>

      <div class="modal-body py-2">
        <form @submit.prevent="handleSubmit">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="filter-label">Họ tên khách hàng <span class="text-danger">*</span></label>
              <input v-model="formData.tenKhachHang" type="text" class="form-control custom-input" required placeholder="Nguyễn Văn A">
            </div>
            <div class="col-md-6">
              <label class="filter-label">Số điện thoại <span class="text-danger">*</span></label>
              <input v-model="formData.soDienThoai" type="text" class="form-control custom-input" required placeholder="09xxxxxxxx">
            </div>
            <div class="col-md-6">
              <label class="filter-label">Email</label>
              <input v-model="formData.email" type="email" class="form-control custom-input" placeholder="example@gmail.com">
            </div>
            <div class="col-md-6">
              <label class="filter-label">Ngày sinh</label>
              <input v-model="formData.ngaySinh" type="date" class="form-control custom-input">
            </div>
            <div class="col-md-6">
              <label class="filter-label">Giới tính</label>
              <div class="d-flex gap-3 mt-2">
                <label><input type="radio" v-model="formData.gioiTinh" :value="true"> Nam</label>
                <label><input type="radio" v-model="formData.gioiTinh" :value="false"> Nữ</label>
              </div>
            </div>
            <div class="col-md-6">
              <label class="filter-label">Trạng thái</label>
              <select v-model="formData.trangThai" class="form-select custom-input">
                <option :value="1">Hoạt động</option>
                <option :value="0">Ngừng hoạt động</option>
              </select>
            </div>
            <div class="col-12">
              <label class="filter-label">Địa chỉ</label>
              <textarea v-model="formData.diaChi" class="form-control custom-input" rows="2" placeholder="Số nhà, tên đường..."></textarea>
            </div>
          </div>

          <div class="modal-footer border-top pt-3 mt-4 text-end">
            <button type="button" class="btn btn-secondary me-2 px-4" @click="$emit('close')">Hủy</button>
            <button type="submit" class="btn-red-dark shadow-sm">
              <i class="fas fa-save me-1"></i> Lưu dữ liệu
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import clientService from '@/services/clientService';

const props = defineProps(['clientId']);
const emit = defineEmits(['close', 'refresh']);

const formData = ref({
  tenKhachHang: '',
  soDienThoai: '',
  email: '',
  ngaySinh: '',
  gioiTinh: true,
  diaChi: '',
  trangThai: 1
});

// Nếu có clientId (chế độ sửa), lấy dữ liệu cũ đổ vào form
onMounted(async () => {
  if (props.clientId) {
    try {
      const res = await clientService.getDetail(props.clientId);
      formData.value = res.data;
    } catch (error) {
      console.error("Lỗi lấy chi tiết khách hàng:", error);
    }
  }
});

const handleSubmit = async () => {
  try {
    if (props.clientId) {
      await clientService.update(props.clientId, formData.value);
      alert('Cập nhật khách hàng thành công!');
    } else {
      await clientService.create(formData.value);
      alert('Thêm khách hàng thành công!');
    }
    emit('refresh'); // Load lại bảng ở trang chủ
    emit('close');   // Đóng modal
  } catch (error) {
    alert('Có lỗi xảy ra, vui lòng kiểm tra lại!');
    console.error(error);
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
.modal-content-custom {
  background: white;
  padding: 25px;
  border-radius: 15px;
  width: 100%;
  max-width: 700px;
}
</style>