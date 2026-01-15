<script setup>
import { defineProps, defineEmits } from 'vue';
import { useHotpotModal } from '../screens/foodFunction';

const props = defineProps({
  isOpen: Boolean,
  hotpotItem: Object
});

const emit = defineEmits(['close', 'save']);

// Gọi logic form
const { 
  formData, 
  listLoaiSet, 
  handleSave, 
  closeModal
} = useHotpotModal(props, emit);
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      
      <div class="modal-header">
        <h2>{{ hotpotItem ? 'Cập nhật Set Lẩu' : 'Thêm Set Lẩu Mới' }}</h2>
        <button class="btn-close" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
          
          <div class="form-row">
            <div class="form-group">
              <label>Mã Set Lẩu <span class="required">*</span></label>
              <input v-model="formData.maSetLau" type="text" placeholder="VD: SL-THAI-01">
            </div>
            <div class="form-group">
              <label>Tên Set Lẩu <span class="required">*</span></label>
              <input v-model="formData.tenSetLau" type="text" placeholder="VD: Set Lẩu Thái Hải Sản">
            </div>
          </div>

          <div class="form-row">
             <div class="form-group">
              <label>Loại Set Lẩu</label>
              <select v-model="formData.idLoaiSet" class="form-control">
                <option value="">-- Chọn loại --</option>
                <option v-for="type in listLoaiSet" :key="type.id" :value="type.id">
                  {{ type.name }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>Giá bán (VNĐ) <span class="required">*</span></label>
              <input v-model="formData.giaBan" type="number" placeholder="Nhập giá bán">
            </div>
          </div>

          <div class="form-group full-width">
             <label>Link Hình ảnh</label>
             <input v-model="formData.hinhAnh" type="text" placeholder="https://example.com/image.jpg">
          </div>

          <div class="form-group full-width">
             <label>Mô tả ngắn</label>
             <textarea v-model="formData.moTa" rows="2" placeholder="Mô tả sơ lược về set lẩu..."></textarea>
          </div>

          <div class="form-group full-width">
             <label>Mô tả chi tiết (Thành phần)</label>
             <textarea v-model="formData.moTaChiTiet" rows="3" placeholder="Chi tiết các thành phần trong set (tôm, mực, rau, nấm...)"></textarea>
          </div>

          <div class="form-group full-width">
            <label>Trạng thái kinh doanh</label>
             <div class="toggle-wrapper">
                <span :class="formData.trangThai === 1 ? 'text-active' : 'text-inactive'">
                    {{ formData.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
                </span>
                <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }" @click="formData.trangThai = (formData.trangThai === 1 ? 0 : 1)">
                  <div class="toggle-knob"></div>
                </div>
             </div>
          </div>

        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="closeModal">Hủy</button>
        <button class="btn-confirm" @click="handleSave">
            {{ hotpotItem ? 'Lưu thay đổi' : 'Thêm mới' }}
        </button>
      </div>

    </div>
  </div>
</template>

<style scoped src="../modal/foodModalManager.css">

</style>