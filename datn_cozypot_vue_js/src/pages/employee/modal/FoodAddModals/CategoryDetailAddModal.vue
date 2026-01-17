<script setup>
import { useCategoryDetailAddModal, useHotpotCategoryAddModal } from '../../../../services/foodFunction';
import { defineProps, defineEmits } from 'vue';

// Nhận listDanhMucGoc từ cha
const props = defineProps(['isOpen', 'formData', 'listDanhMucGoc']); 
const emit = defineEmits(['close', 'save']);

const { 
  formData, 
  handleSave,
  listDanhMucGoc 
} = useCategoryDetailAddModal(props, emit);
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Thêm Chi Tiết Danh Mục </h2>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
            
            <div class="form-group full-width">
                <label>Tên chi tiết <span class="required">*</span></label>
                <input v-model="formData.tenDanhMucChiTiet" type="text" placeholder="VD: Bò Mỹ, Nấm kim châm...">
            </div>

            <div class="form-group full-width">
                <label>Danh mục gốc <span class="required">*</span></label>
                <select v-model="formData.idDanhMuc" class="form-control">
                    <option value="" disabled>-- Chọn danh mục gốc --</option>
                    <option v-for="dm in listDanhMucGoc" :key="dm.id" :value="dm.id">
                        {{ dm.tenDanhMuc }} 
                    </option>
                </select>
            </div>

            <div class="form-group full-width">
                <label>Mô tả</label>
                <textarea v-model="formData.moTa" rows="3" placeholder="Mô tả..."></textarea>
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
        <button class="btn-confirm" @click="handleSave">Thêm mới</button>
      </div>
    </div>
  </div>
</template>

<style scoped src="../foodModalManager.css">
</style>