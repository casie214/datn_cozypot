<template>
  <Transition name="fade">
    <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
      <Transition name="zoom">
        <div class="modal-container">
          <div class="modal-header">
            <h2>Tặng phiếu giảm giá cá nhân</h2>
            <button class="close-btn" @click="$emit('close')">&times;</button>
          </div>

          <form @submit.prevent="handleSubmit" class="modal-body" novalidate>
            
            <div class="form-group">
              <label>Khách hàng nhận phiếu *</label>
              <select v-model="formData.idKhachHang" :class="{ 'input-error': errors.idKhachHang }">
                <option value="" disabled>Chọn khách hàng</option>
                <option v-for="kh in dskh" :key="kh.id" :value="kh.id">
                  {{ kh.tenKhachHang }} - {{ kh.soDienThoai }}
                </option>
              </select>
              <span v-if="errors.idKhachHang" class="error-text">{{ errors.idKhachHang }}</span>
            </div>

            <div class="form-group">
              <label>Loại phiếu giảm giá gốc *</label>
              <select v-model="formData.idPhieuGiamGia" :class="{ 'input-error': errors.idPhieuGiamGia }">
                <option value="" disabled>Chọn loại phiếu gốc</option>
                <option v-for="pgg in dspgg" :key="pgg.id" :value="pgg.id">
                  {{ pgg.tenPhieu }} ({{ pgg.hinhThuc === 1 ? 'Giảm %' : 'Giảm tiền' }})
                </option>
              </select>
              <span v-if="errors.idPhieuGiamGia" class="error-text">{{ errors.idPhieuGiamGia }}</span>
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
              <textarea v-model="formData.ghiChu" rows="2" placeholder="Nhập ghi chú nếu có..."></textarea>
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

const props = defineProps({ 
  isOpen: Boolean,
  dskh: Array, // Danh sách khách hàng truyền từ ngoài vào
  dspgg: Array // Danh sách phiếu gốc truyền từ ngoài vào
});
const emit = defineEmits(['close', 'save']);

const formData = reactive({
  idKhachHang: '',
  idPhieuGiamGia: '',
  nguonGoc: '',
  ngayHetHan: '',
  ghiChu: '',
  trangThaiSuDung: 0
});

const errors = reactive({
  idKhachHang: '',
  idPhieuGiamGia: '',
  ngayHetHan: ''
});

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    Object.assign(formData, {
      idKhachHang: '', idPhieuGiamGia: '', nguonGoc: '', 
      ngayHetHan: '', ghiChu: '', trangThaiSuDung: 0
    });
    Object.keys(errors).forEach(k => errors[k] = '');
  }
});

const validate = () => {
  let isValid = true;
  Object.keys(errors).forEach(k => errors[k] = '');

  if (!formData.idKhachHang) {
    errors.idKhachHang = 'Vui lòng chọn khách hàng';
    isValid = false;
  }
  if (!formData.idPhieuGiamGia) {
    errors.idPhieuGiamGia = 'Vui lòng chọn loại phiếu gốc';
    isValid = false;
  }
  if (!formData.ngayHetHan) {
    errors.ngayHetHan = 'Vui lòng chọn ngày hết hạn';
    isValid = false;
  }
  return isValid;
};

const handleSubmit = async () => {
  if (!validate()) return;

  const result = await Swal.fire({
    title: 'Xác nhận tặng phiếu?',
    text: "Hệ thống sẽ gửi phiếu giảm giá này đến tài khoản khách hàng.",
    icon: 'question',
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
/* Copy phần CSS modal của bạn vào đây */
@import "./personalVoucherModal.css"; 
</style>