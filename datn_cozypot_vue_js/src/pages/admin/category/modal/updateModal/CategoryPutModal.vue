<script setup>
import { useCategoryAddModal, useCategoryPutModal } from '../../../../../services/foodFunction';
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  itemList: Object
});
const emit = defineEmits(['close', 'save']);

const {
    formData,
    handleSave,
    closeModal
} = useCategoryPutModal(props, emit);
</script>

<template>
    <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Cập Nhật Danh Mục</h2>
                <button class="btn-close" @click="$emit('close')">✕</button>
            </div>

            <div class="modal-body">
                <div class="form-container">

                    <div class="form-group full-width">
                        <label>Mã danh mục</label>
                        <input :value="formData.maDanhMuc" type="text" disabled
                            style="background-color: #f0f0f0; color: #666;">
                    </div>

                    <div class="form-group full-width">
                        <label>Tên danh mục <span class="required">*</span></label>
                        <input v-model="formData.tenDanhMuc" type="text">
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
                            <span :class="{ 'text-active': formData.trangThai === 1 }">
                                {{ formData.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn-cancel" @click="closeModal">Hủy</button>
                <button class="btn-confirm" @click="handleSave">Lưu thay đổi</button>
            </div>
        </div>
    </div>
</template>

<style scoped src="/src/assets/foodModalManager.css"></style>