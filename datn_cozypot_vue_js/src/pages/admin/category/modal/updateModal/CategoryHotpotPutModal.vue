<script setup>
import { ref, watch } from 'vue';
// 🚨 ĐÃ SỬA IMPORT: Gọi đúng Hook dùng cho Update
import { useCategoryHotpotPutModal, foodApi } from '../../../../../services/foodFunction';
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import Swal from 'sweetalert2';

const props = defineProps({
  isOpen: Boolean,
  itemList: Object // 🚨 Dùng itemList để nhận dữ liệu từ bảng truyền vào
});
const emit = defineEmits(['close', 'save', 'refresh']);

const { 
    formData, 
    handleSave,
    dialogVisible,
    dialogConfig,
    handleDialogConfirm,
    handleDialogClose
} = useCategoryHotpotPutModal(props, emit); // 🚨 Sử dụng đúng Hook Put

// ==========================================
// 🚨 LOGIC VALIDATE TẠI CHỖ CHO CẬP NHẬT
// ==========================================
const existingHotpotTypes = ref([]);
const formErrors = ref({
  tenLoaiSet: '',
  moTa: ''
});

// Chạy lại mỗi khi nhận được dữ liệu món cần sửa (itemList)
watch(() => props.itemList, async (newVal) => {
  formErrors.value = { tenLoaiSet: '', moTa: '' }; // Reset lỗi

  if (newVal) {
    try {
      const res = await foodApi.getHotpotTypes();
      existingHotpotTypes.value = res.data || [];
    } catch (e) {
      console.error(e);
    }
  }
}, { immediate: true });

const validateForm = () => {
  let isValid = true;
  formErrors.value = { tenLoaiSet: '', moTa: '' };

  const tenLoai = (formData.value.tenLoaiSet || '').trim();
  const moTa = (formData.value.moTa || '').trim();

  // 1. Validate Tên loại set (Trống, 5-100 ký tự)
  if (!tenLoai) {
    formErrors.value.tenLoaiSet = "Tên loại set không được để trống";
    isValid = false;
  } else if (tenLoai.length < 5) {
    formErrors.value.tenLoaiSet = "Tên loại set phải chứa ít nhất 5 kí tự";
    isValid = false;
  } else if (tenLoai.length > 100) {
    formErrors.value.tenLoaiSet = "Tên loại set không được vượt quá 100 ký tự";
    isValid = false;
  } else {
    // Check trùng tên Loại Set (🚨 Phải loại trừ chính ID đang sửa ra)
    const currentId = formData.value.id;
    const isDuplicate = existingHotpotTypes.value.some(
      c => c.tenLoaiSet.toLowerCase() === tenLoai.toLowerCase() && c.id !== currentId
    );
    if (isDuplicate) {
      formErrors.value.tenLoaiSet = "Loại set đã tồn tại trong hệ thống";
      isValid = false;
    }
  }

  // 2. Validate Mô tả (Tối đa 255 ký tự)
  if (moTa.length > 255) {
    formErrors.value.moTa = "Mô tả không được vượt quá 255 ký tự";
    isValid = false;
  }

  if (!isValid) {
    Swal.fire({ 
        toast: true, 
        position: 'top-end', 
        icon: 'error', 
        title: 'Dữ liệu không hợp lệ', 
        text: 'Vui lòng kiểm tra lại các trường báo đỏ',
        showConfirmButton: false, 
        timer: 3000 
    });
  } else {
    // Update lại value đã trim
    formData.value.tenLoaiSet = tenLoai;
    formData.value.moTa = moTa;
  }

  return isValid;
};

// Gọi hàm này khi bấm Submit
const submitForm = () => {
  if (validateForm()) {
    handleSave(); 
  }
};
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
        <h2>Cập Nhật Loại Set Lẩu</h2>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
            
            <div class="form-group full-width">
                <label>Mã loại set</label>
                <input 
                  :value="formData.maLoaiSet" 
                  type="text" 
                  disabled
                  style="background-color: #f0f0f0; color: #666;"
                >
            </div>

            <div class="form-group full-width" style="margin-top: 10px;">
                <label>Tên loại set <span class="required">*</span></label>
                <input 
                  v-model="formData.tenLoaiSet" 
                  type="text" 
                  placeholder="VD: Buffet, Combo..."
                  :class="{ 'input-error': formErrors.tenLoaiSet }"
                >
                <small v-if="formErrors.tenLoaiSet" class="error-text">{{ formErrors.tenLoaiSet }}</small>
            </div>

            <div class="form-group full-width" style="margin-top: 10px;">
                <label>Mô tả</label>
                <textarea 
                  v-model="formData.moTa" 
                  rows="3" 
                  placeholder="Nhập mô tả chi tiết..."
                  :class="{ 'input-error': formErrors.moTa }"
                ></textarea>
                <small v-if="formErrors.moTa" class="error-text">{{ formErrors.moTa }}</small>
            </div>

        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="$emit('close')">Hủy</button>
        <button class="btn-confirm" @click="submitForm">Lưu thay đổi</button>
      </div>
    </div>
  </div>
</template>

<style scoped src="/src/assets/foodModalManager.css"></style>

<style scoped>
.input-error {
  border-color: #dc3545 !important;
  background-color: #fff8f8;
}

.error-text {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #dc3545;
  font-style: italic;
}
</style>