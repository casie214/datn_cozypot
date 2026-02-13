<script setup>
import { useCategoryHotpotPutModal } from '../../../../../services/foodFunction';
import { defineProps, defineEmits } from 'vue';
// 1. Import GlobalDialogue
import GlobalDialogue from '../../../../../components/globalDialogue.vue';

const props = defineProps({
  isOpen: Boolean,
  itemList: Object
});
const emit = defineEmits(['close', 'save', 'refresh']);

const {
    formData,
    handleSave,
    closeModal,
    // 2. Lấy biến Dialog
    dialogVisible,
    dialogConfig,
    handleDialogConfirm,
    handleDialogClose
} = useCategoryHotpotPutModal(props, emit);
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="closeModal">
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
        <h2 style="color: white !important;">Cập Nhật Loại Set</h2>
        <button class="btn-close" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
            
            <div class="form-group full-width">
                <label>Mã loại</label>
                <input :value="formData.maLoaiSet" type="text" disabled 
                    style="background-color: #f5f5f5; color: #666; cursor: not-allowed;">
            </div>

            <div class="form-group full-width">
                <label>Tên loại set <span class="required">*</span></label>
                <input v-model="formData.tenLoaiSet" type="text">
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
        <button class="btn-cancel" @click="closeModal">Hủy</button>
        <button class="btn-confirm" @click="handleSave">Lưu thay đổi</button>
      </div>
    </div>
  </div>
</template>

<style scoped src="/src/assets/foodModalManager.css">
</style>