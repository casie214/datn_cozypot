<script setup>
import { defineProps, defineEmits, watch, ref } from 'vue';
import { useHotpotCategoryAddModal, foodApi } from '../../../../../services/foodFunction';
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import Swal from 'sweetalert2';

const props = defineProps({
  isOpen: Boolean,
  formData: Object,
  initialName: {
    type: String,
    default: ''
  }
});
const emit = defineEmits(['close', 'save', 'refresh']);

const { 
    formData, 
    handleSave,
    dialogVisible,
    dialogConfig,
    handleDialogConfirm,
    handleDialogClose
} = useHotpotCategoryAddModal(props, emit);

// ==========================================
// 🚨 LOGIC VALIDATE TẠI CHỖ CHO LOẠI SET LẨU
// ==========================================
const existingHotpotTypes = ref([]);
const formErrors = ref({
  tenLoaiSet: '',
  moTa: ''
});

// Mỗi khi mở modal -> reset lỗi và lấy data để check trùng
watch(() => props.isOpen, async (newVal) => {
  if (newVal) {
    if (props.initialName && formData.value) {
      formData.value.tenLoaiSet = props.initialName;
    }
    
    // Reset lỗi
    formErrors.value = { tenLoaiSet: '', moTa: '' };

    try {
      const res = await foodApi.getHotpotTypes();
      existingHotpotTypes.value = res.data || [];
    } catch (e) {
      console.error(e);
    }
  }
});

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
    // Check trùng tên
    const isDuplicate = existingHotpotTypes.value.some(
      c => c.tenLoaiSet.toLowerCase() === tenLoai.toLowerCase()
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

  // 3. Hiển thị Toast thông báo lỗi chung
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

// Đánh chặn sự kiện click lưu
const submitForm = () => {
  if (validateForm()) {
    handleSave(); // Vượt qua kiểm tra mới gọi hàm save gốc của Hook
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
        <h2>Thêm Loại Set Lẩu Mới</h2>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
            
            <div class="form-group full-width">
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
        <button class="btn-confirm" @click="submitForm">Thêm mới</button>
      </div>
    </div>
  </div>
</template>

<style scoped src="/src/assets/foodModalManager.css"></style>

<style scoped>
/* Thêm CSS cho lỗi */
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