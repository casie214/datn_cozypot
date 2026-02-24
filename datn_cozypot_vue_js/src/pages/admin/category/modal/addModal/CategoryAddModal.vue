<script setup>
import { defineProps, defineEmits } from 'vue';
import { useCategoryAddModal } from '../../../../../services/foodFunction';
// 1. Import Component Dialog
import GlobalDialogue from '../../../../../components/globalDialogue.vue';

const props = defineProps(['isOpen', 'formData']);
const emit = defineEmits(['close', 'save', 'refresh']); // Thêm 'refresh' vào emits

const { 
  formData, 
  handleSave,
  // 2. Lấy biến Dialog từ composable
  dialogVisible,
  dialogConfig,
  handleDialogConfirm,
  handleDialogClose
} = useCategoryAddModal(props, emit);
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      
      <GlobalDialogue 
        :show="dialogVisible"
        :type="dialogConfig?.type"
        :variant="dialogConfig?.variant"
        :title="dialogConfig?.title"
        :message="dialogConfig?.message"
        @close="handleDialogClose"
        @confirm="handleDialogConfirm"
      />

      <div class="modal-header">
        <h2 style="color: white !important;">Thêm Danh Mục Mới</h2>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
            <div class="form-group full-width">
                <label>Tên danh mục <span class="required">*</span></label>
                <input v-model="formData.tenDanhMuc" type="text" placeholder="Nhập tên danh mục...">
            </div>

            <div class="form-group full-width">
                <label>Mô tả</label>
                <textarea v-model="formData.moTa" rows="3" placeholder="Nhập mô tả..."></textarea>
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

<style scoped src="/src/assets/foodModalManager.css">
</style>