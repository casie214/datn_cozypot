<script setup>
import { useCategoryDetailPutModal } from '../../../../../services/foodFunction';
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  itemList: Object
});
const emit = defineEmits(['close', 'save']);

const {
    formData,
    handleSave,
    closeModal,
    listDanhMucGoc
} = useCategoryDetailPutModal(props, emit);
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Cập Nhật Chi Tiết</h2>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
            
            <div class="form-group full-width">
                <label>Mã chi tiết</label>
                <input :value="formData.maDanhMucChiTiet" type="text" disabled style="background-color: #f5f5f5; color: #666; cursor: not-allowed;">
            </div>

            <div class="form-group full-width">
                <label>Tên chi tiết <span class="required">*</span></label>
                <input v-model="formData.tenDanhMucChiTiet" type="text">
            </div>

            <div class="form-group full-width">
                <label>Danh mục gốc <span class="required">*</span></label>
                <select v-model="formData.idDanhMuc" class="form-control">
                    <option v-for="dm in listDanhMucGoc" :key="dm.id" :value="dm.id">
                        {{ dm.tenDanhMuc }}
                    </option>
                </select>
            </div>

            <div class="form-group full-width">
                <label>Mô tả</label>
                <textarea v-model="formData.moTa" rows="3"></textarea>
            </div>

            <div class="form-group full-width">
                <label>Trạng thái</label>
                <div class="toggle-wrapper" @click="formData.trangThai = formData.trangThai === 1 ? 0 : 1">
                    <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }">
                        <div class="toggle-knob"></div>
                    </div>
                    <span :class="{'text-active': formData.trangThai === 1}">
                        {{ formData.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}
                    </span>
                </div>
            </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="$emit('close')">Hủy</button>
        <button class="btn-confirm" @click="handleSave">Lưu thay đổi</button>
      </div>
    </div>
  </div>
</template>

<style scoped src="/src/assets/foodModalManager.css">
</style>