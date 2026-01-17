<template>
  <Transition name="fade">
    <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
      <Transition name="zoom">
        <div v-if="isOpen" class="modal-container">
          <div class="modal-header">
            <h2>Cập nhật phiếu giảm giá</h2>
            <button class="close-btn" @click="$emit('close')">&times;</button>
          </div>

          <form @submit.prevent="handleSubmit" class="modal-body">
            <div class="info-grid">
              <div class="form-group">
                <label>Đợt khuyến mãi *</label>
                <select v-model="formData.idDotKhuyenMai" required>
                  <option value="" disabled>Chọn đợt khuyến mãi</option>
                  <option v-for="dot in danhSachDotKM" :key="dot.id" :value="dot.id">
                    {{ dot.ten }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label>Code giảm giá *</label>
                <input 
                  v-model="formData.codeGiamGia" 
                  type="text" 
                  required 
                  class="voucher-input"
                >
              </div>
            </div>

            <div class="form-group">
              <label>Tên phiếu giảm giá *</label>
              <input 
                v-model="formData.tenPhieuGiamGia" 
                type="text" 
                required
              >
            </div>

            <div class="form-group">
              <label>Loại giảm giá *</label>
              <div class="status-group">
                <div class="status-item" @click="formData.loaiGiamGia = 1">
                  <span class="custom-dot" :class="{ active: formData.loaiGiamGia === 1 }"></span>
                  <span class="label-text">Phần trăm (%)</span>
                </div>
                <div class="status-item" @click="formData.loaiGiamGia = 2">
                  <span class="custom-dot" :class="{ active: formData.loaiGiamGia === 2 }"></span>
                  <span class="label-text">Tiền mặt (VNĐ)</span>
                </div>
              </div>
            </div>

            <div class="info-grid">
              <div class="form-group">
                <label>Giá trị giảm *</label>
                <input v-model.number="formData.giaTriGiam" type="number" required>
              </div>

              <div class="form-group">
                <label>Giảm tối đa *</label>
                <input v-model.number="formData.giaTriGiamToiDa" type="number" required>
              </div>
            </div>

            <div class="info-grid">
              <div class="form-group">
                <label>Đơn tối thiểu *</label>
                <input v-model.number="formData.donHangToiThieu" type="number" required>
              </div>

              <div class="form-group">
                <label>Số lượng phát hành *</label>
                <input v-model.number="formData.soLuongPhatHanh" type="number" required>
              </div>
            </div>

            <div class="info-grid">
              <div class="form-group">
                <label>Ngày bắt đầu *</label>
                <input v-model="formData.ngayBatDau" type="datetime-local" required>
              </div>

              <div class="form-group">
                <label>Ngày kết thúc *</label>
                <input v-model="formData.ngayKetThuc" type="datetime-local" required>
              </div>
            </div>

            <div class="form-group">
              <label>Trạng thái phiếu</label>
              <div class="status-group">
                <div class="status-item" @click="formData.trangThai = 1">
                  <span class="custom-dot" :class="{ active: formData.trangThai === 1 }"></span>
                  <span class="label-text">Đang hoạt động</span>
                </div>

                <div class="status-item" @click="formData.trangThai = 0">
                  <span class="custom-dot" :class="{ active: formData.trangThai === 0 }"></span>
                  <span class="label-text">Ngưng hoạt động</span>
                </div>
              </div>
            </div>

            <div class="modal-footer-btns">
              <button type="button" class="btn-huy" @click="$emit('close')">
                Hủy
              </button>

              <button type="submit" class="btn-xac-nhan">
                Cập nhật
              </button>
            </div>

          </form>
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<script setup>
import { reactive, watch } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  data: Object,
  danhSachDotKM: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['close', 'save']);

const formData = reactive({
  id: null,
  idDotKhuyenMai: '',
  codeGiamGia: '',
  tenPhieuGiamGia: '',
  loaiGiamGia: 1,
  giaTriGiam: 0,
  giaTriGiamToiDa: 0,
  donHangToiThieu: 0,
  soLuongPhatHanh: 0,
  ngayBatDau: '',
  ngayKetThuc: '',
  trangThai: 1
});

// Load dữ liệu cần update vào form
watch(
  () => props.data,
  (newVal) => {
    if (newVal) {
      Object.assign(formData, newVal);
    }
  },
  { immediate: true }
);

const handleSubmit = () => {
  emit('save', { ...formData });
};
</script>

<style scoped>
/* giữ nguyên toàn bộ CSS cũ của bạn */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex; justify-content: center; align-items: center;
  z-index: 9999;
}
.modal-container {
  background: white; width: 600px; max-height: 95vh;
  border-radius: 12px; overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 15px 25px; border-bottom: 1px solid #eee;
  position: sticky; top: 0; background: white; z-index: 10;
}
.modal-header h2 { font-size: 18px; margin: 0; color: #8B0000; font-weight: bold; }
.close-btn { background: none; border: none; font-size: 28px; cursor: pointer; color: #999; line-height: 1; }

.modal-body { padding: 25px; }

.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 13px; color: #555; font-weight: bold; text-transform: uppercase; }
.form-group input, .form-group select {
  width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px;
}

.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

.status-group { display: flex; gap: 35px; margin-top: 8px; }
.status-item { display: flex; align-items: center; gap: 10px; cursor: pointer; }

.custom-dot {
  width: 22px; height: 22px; border-radius: 50%;
  background-color: #e0e0e0;
}
.custom-dot.active { background-color: #8B0000; }

.modal-footer-btns { display: flex; gap: 20px; margin-top: 35px; }

.btn-huy { flex: 1; padding: 12px; background: #e8e8e8; border-radius: 6px; }
.btn-xac-nhan { flex: 1; padding: 12px; background: #8B0000; color: white; border-radius: 6px; }
</style>
