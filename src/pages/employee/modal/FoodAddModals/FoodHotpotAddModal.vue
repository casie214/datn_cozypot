<script setup>
import { defineProps, defineEmits } from 'vue';
import { useHotpotAddModal } from '../../screens/foodFunction'; 

const props = defineProps({
  isOpen: Boolean
});

const emit = defineEmits(['close', 'refresh']);

const { 
  formData, 
  listDanhMuc, 
  handleSave 
} = useHotpotAddModal(props, emit);

</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      
      <div class="modal-header">
        <h3>Thêm Set Lẩu Mới</h3>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
            
            <div class="form-row">
                <div class="form-group">
                    <label>Tên Set Lẩu <span class="required">*</span></label>
                    <input v-model="formData.tenSetLau" type="text" placeholder="VD: Set Lẩu Thái Hải Sản">
                </div>
                <div class="form-group">
                    <label>Loại Set <span class="required">*</span></label>
                    <select v-model="formData.tenMonAn" class="form-control">
                        <option value="">-- Chọn loại set --</option>
                        <option v-for="item in listDanhMuc" :key="item.id" :value="item.id">
                            {{ item.tenLoaiSet || item.ten }}
                        </option>
                    </select>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>Giá bán (VNĐ) <span class="required">*</span></label>
                    <input v-model="formData.giaBan" type="number" placeholder="Nhập giá bán">
                </div>
                 <div class="form-group">
                    <label>Link hình ảnh</label>
                    <input v-model="formData.hinhAnh" type="text" placeholder="https://example.com/anh.jpg">
                </div>
            </div>

            <div class="form-group full-width">
                <label>Mô tả ngắn</label>
                <textarea v-model="formData.moTa" rows="2" placeholder="Mô tả sơ lược..."></textarea>
            </div>

            <div class="form-group full-width">
                <label>Mô tả chi tiết (Thành phần set)</label>
                <textarea v-model="formData.moTaChiTiet" rows="4" placeholder="VD: 300g bò, 200g tôm, rau, nấm..."></textarea>
            </div>
             
             <div class="form-group">
                <label>Trạng thái kinh doanh</label>
                <div class="toggle-wrapper">
                    <div class="toggle-switch" 
                         :class="{ 'on': formData.trangThai === 1 }" 
                         @click="formData.trangThai = (formData.trangThai === 1 ? 0 : 1)">
                      <div class="toggle-knob"></div>
                    </div>
                    <span class="status-text">
                        {{ formData.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}
                    </span>
                </div>
             </div>

        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="$emit('close')">Hủy</button>
        <button class="btn-confirm" @click="handleSave">Thêm mới</button>
      </div>

    </div>
  </div>
</template>

<style scoped src="../foodModalManager.css">
</style>