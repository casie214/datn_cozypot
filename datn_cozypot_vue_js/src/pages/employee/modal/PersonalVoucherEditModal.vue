<template>
  <Transition name="fade">
    <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
      <Transition name="zoom">
        <div class="modal-container">
          <div class="modal-header">
            <h2>Cập nhật phiếu cá nhân</h2>
            <button class="close-btn" @click="$emit('close')">&times;</button>
          </div>

          <form @submit.prevent="handleSubmit" class="modal-body" v-if="formData.id" novalidate>
            
            <div class="form-row">
              <div class="form-group">
                <label>Mã phiếu</label>
                <input :value="formData.maGiamGiaCaNhan" type="text" readonly style="background: #f5f5f5; color: #666;">
              </div>
              <div class="form-group">
                <label>Khách hàng</label>
                <input :value="formData.tenKhachHang" type="text" readonly style="background: #f5f5f5; color: #666;">
              </div>
            </div>

            <div class="form-group">
              <label>Loại phiếu giảm giá gốc</label>
              <input :value="formData.tenPhieuGiamGiaGoc" type="text" readonly style="background: #f5f5f5; color: #666;">
            </div>

            <div class="form-group">
              <label>Nguồn gốc / Lý do tặng</label>
              <input v-model="formData.nguonGoc" type="text" placeholder="Ví dụ: Tri ân khách hàng, Sinh nhật...">
            </div>

            <div class="form-group">
              <label>Ngày hết hạn *</label>
              <input v-model="formData.ngayHetHan" type="datetime-local" :class="{ 'input-error': errors.ngayHetHan }">
              <span v-if="errors.ngayHetHan" class="error-text">{{ errors.ngayHetHan }}</span>
            </div>

            <div class="form-group">
              <label>Ghi chú thêm</label>
              <textarea v-model="formData.ghiChu" rows="2" placeholder="Nhập ghi chú cập nhật nếu có..."></textarea>
            </div>

            <div class="form-group">
              <label>Trạng thái phiếu</label>
              <div class="status-group">
                <span class="status-item clickable" @click="formData.trangThaiSuDung = 0">
                  <i class="fa-solid fa-circle" :class="formData.trangThaiSuDung === 0 ? 'dot-active' : 'dot-gray'"></i>
                  Chưa sử dụng
                </span>
                <span class="status-item clickable" @click="formData.trangThaiSuDung = 1">
                  <i class="fa-solid fa-circle" :class="formData.trangThaiSuDung === 1 ? 'dot-active' : 'dot-gray'"></i>
                  Đã sử dụng
                </span>
              </div>
            </div>

            <div class="modal-footer-actions">
              <button type="button" class="btn-huy" @click="$emit('close')">Hủy bỏ</button>
              <button type="submit" class="btn-xac-nhan">Lưu thay đổi</button>
            </div>
          </form>
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<script setup>
import { reactive, watch } from 'vue';
import Swal from 'sweetalert2';

const props = defineProps({ 
  isOpen: Boolean, 
  data: Object 
});
const emit = defineEmits(['close', 'save']);

const formData = reactive({});
const errors = reactive({ ngayHetHan: '' });

// Theo dõi khi mở Modal hoặc dữ liệu thay đổi
watch(() => [props.isOpen, props.data], ([newOpen, newData]) => {
  if (newOpen && newData) {
    // Clone dữ liệu từ prop vào formData
    Object.assign(formData, { ...newData });
    
    // Định dạng lại ngày hết hạn cho input datetime-local (YYYY-MM-DDThh:mm)
    if (formData.ngayHetHan) {
      const date = new Date(formData.ngayHetHan);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      formData.ngayHetHan = `${year}-${month}-${day}T${hours}:${minutes}`;
    }
    
    errors.ngayHetHan = '';
  }
}, { immediate: true });

const validate = () => {
  let isValid = true;
  errors.ngayHetHan = '';
  if (!formData.ngayHetHan) {
    errors.ngayHetHan = 'Vui lòng chọn ngày hết hạn';
    isValid = false;
  }
  return isValid;
};

const handleSubmit = async () => {
  if (!validate()) return;

  const result = await Swal.fire({
    title: 'Xác nhận lưu thay đổi?',
    text: "Mọi thông tin phiếu sẽ được cập nhật lại trên hệ thống.",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#8B0000',
    cancelButtonColor: '#aaa',
    confirmButtonText: 'Đồng ý',
    cancelButtonText: 'Hủy',
    target: document.querySelector('.modal-overlay') || document.body
  });

  if (result.isConfirmed) {
    emit('save', { ...formData });
  }
};
</script>

<style scoped>
@import "./personalVoucherModal.css";

/* Bổ sung để chia cột Mã phiếu và Tên khách hàng */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}
</style>