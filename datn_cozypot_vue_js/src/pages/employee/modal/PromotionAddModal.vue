<template>
  <Transition name="fade">
    <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
      <Transition name="zoom">
        <div class="modal-container">
          <div class="modal-header">
            <h2>Thêm đợt khuyến mãi</h2>
            <button class="close-btn" @click="$emit('close')">&times;</button>
          </div>

          <form @submit.prevent="handleSubmit" class="modal-body" novalidate>

            <div class="form-group">
              <label>Tên đợt khuyến mãi *</label>
              <input v-model="formData.tenDotKhuyenMai" type="text" placeholder="Nhập tên đợt khuyến mãi"
                :class="{ 'input-error': errors.tenDotKhuyenMai }">
              <span v-if="errors.tenDotKhuyenMai" class="error-text">{{ errors.tenDotKhuyenMai }}</span>
            </div>

            <div class="form-group">
              <label>Mô tả</label>
              <textarea v-model="formData.moTa" rows="2" placeholder="Nhập mô tả chi tiết chương trình..."></textarea>
            </div>

            <div class="form-group">
              <label>Loại khuyến mãi *</label>
              <div class="status-group">
                <span class="status-item clickable" @click="formData.loaiKhuyenMai = 1">
                  <i class="fa-solid fa-circle" :class="formData.loaiKhuyenMai === 1 ? 'dot-active' : 'dot-gray'"></i>
                  Phần trăm (%)
                </span>
                <span class="status-item clickable" @click="formData.loaiKhuyenMai = 2">
                  <i class="fa-solid fa-circle" :class="formData.loaiKhuyenMai === 2 ? 'dot-active' : 'dot-gray'"></i>
                  Tiền mặt (VND)
                </span>
              </div>
            </div>

            <div class="form-group">
              <label>Đối tượng áp dụng *</label>
              <div class="select-wrapper">
                <select v-model="formData.doiTuongApDung" :class="{ 'input-error': errors.doiTuongApDung }">
                  <option value="" disabled selected>Chọn đối tượng</option>
                  <option value="Mọi khách hàng">Mọi khách hàng</option>
                  <option value="Khách ăn trưa">Khách ăn trưa</option>
                  <option value="Khách ăn trưa">Khách VIP</option>
                  <option value="Khách ăn trưa">Đoàn trên 4 người</option>
                  <option value="Khách ăn trưa">Gia đình có trẻ em</option>
                  <option value="Tất cả">Gia đình có trẻ em</option>


                </select>
              </div>
              <span v-if="errors.doiTuongApDung" class="error-text">{{ errors.doiTuongApDung }}</span>
            </div>

            <div class="form-group">
              <label>Khung giờ áp dụng</label>
              <input v-model="formData.khungGioApDung" type="text" placeholder="Ví dụ: 08:00 - 22:00">
            </div>

            <div class="form-group">
              <label>Danh sách món ăn áp dụng</label>
              <input v-model="formData.danhSachMonApDung" type="text" placeholder="VD: Tất cả hoặc tên món cụ thể">
            </div>

            <div class="form-group">
              <label>Trạng thái</label>
              <div class="status-group">
                <span class="status-item clickable" @click="formData.trangThai = 1">
                  <i class="fa-solid fa-circle" :class="formData.trangThai === 1 ? 'dot-active' : 'dot-gray'"></i> Hoạt
                  động
                </span>
                <span class="status-item clickable" @click="formData.trangThai = 0">
                  <i class="fa-solid fa-circle" :class="formData.trangThai === 0 ? 'dot-active' : 'dot-gray'"></i> Ngưng
                  hoạt động
                </span>
              </div>
            </div>

            <div class="modal-footer-actions">
              <button type="button" class="btn-huy" @click="$emit('close')">Hủy</button>
              <button type="submit" class="btn-xac-nhan">Xác nhận</button>
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

const props = defineProps({ isOpen: Boolean });
const emit = defineEmits(['close', 'save']);

const formData = reactive({
  tenDotKhuyenMai: '',
  moTa: '',
  loaiKhuyenMai: 1,
  doiTuongApDung: '',
  khungGioApDung: '',
  danhSachMonApDung: '',
  trangThai: 1
});

const errors = reactive({
  tenDotKhuyenMai: '',
  doiTuongApDung: ''
});

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    Object.assign(formData, {
      tenDotKhuyenMai: '', moTa: '', loaiKhuyenMai: 1,
      doiTuongApDung: '', khungGioApDung: '', danhSachMonApDung: '', trangThai: 1
    });
    errors.tenDotKhuyenMai = '';
    errors.doiTuongApDung = '';
  }
});

const validate = () => {
  let isValid = true;
  errors.tenDotKhuyenMai = '';
  errors.doiTuongApDung = '';

  if (!formData.tenDotKhuyenMai.trim()) {
    errors.tenDotKhuyenMai = 'Tên đợt khuyến mãi không được để trống';
    isValid = false;
  }
  if (!formData.doiTuongApDung) {
    errors.doiTuongApDung = 'Vui lòng chọn đối tượng áp dụng';
    isValid = false;
  }
  return isValid;
};

const handleSubmit = async () => {
  if (!validate()) return;

  // XỬ LÝ DỨT ĐIỂM: Ép SweetAlert hiện đè lên Modal bằng target
  const result = await Swal.fire({
    title: 'Xác nhận lưu?',
    text: `Bạn có chắc chắn muốn thêm mới khuyến mãi "${formData.tenDotKhuyenMai}"?`,
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#8B0000',
    cancelButtonColor: '#aaa',
    confirmButtonText: 'Đồng ý',
    cancelButtonText: 'Hủy',
    // Gắn trực tiếp vào overlay của Modal để không bị che khuất
    target: document.querySelector('.modal-overlay') || document.body
  });

  if (result.isConfirmed) {
    emit('save', { ...formData });
  }
};
</script>

<style scoped>
/* GIỮ NGUYÊN CSS GIAO DIỆN CỦA BẠN */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.modal-container {
  background: white;
  width: 550px;
  max-height: 90vh;
  border-radius: 8px;
  overflow-y: auto;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  font-size: 18px;
  margin: 0;
  color: #8B0000;
  font-weight: bold;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 6px;
  font-size: 13px;
  color: #666;
  font-weight: bold;
}

.form-group input,
.form-group textarea,
select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.form-group input:focus,
select:focus {
  border-color: #8B0000;
}

.input-error {
  border-color: #dc3545 !important;
  background-color: #fff8f8;
}

.error-text {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
}

.status-group {
  display: flex;
  gap: 20px;
  margin-top: 5px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
}

.dot-active {
  color: #8B0000;
}

.dot-gray {
  color: #e0e0e0;
}

.modal-footer-actions {
  display: flex;
  gap: 15px;
  margin-top: 25px;
}

.btn-huy {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 6px;
  background: #e8e8e8;
  color: #666;
  font-weight: bold;
  cursor: pointer;
}

.btn-xac-nhan {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 6px;
  background: #8B0000;
  color: white;
  font-weight: bold;
  cursor: pointer;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.zoom-enter-active,
.zoom-leave-active {
  transition: transform 0.3s;
}

.zoom-enter-from,
.zoom-leave-to {
  transform: scale(0.9);
}
</style>