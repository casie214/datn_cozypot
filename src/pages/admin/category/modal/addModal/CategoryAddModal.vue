<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue';
import { useCategoryAddModal, foodApi } from '../../../../../services/foodFunction';
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
import UnitAddScreen from '@/pages/admin/unit/screens/UnitAddScreen.vue';

const props = defineProps(['isOpen', 'formData']);
const emit = defineEmits(['close', 'save', 'refresh']);

const {
  formData,
  handleSave,
  dialogVisible,
  dialogConfig,
  handleDialogConfirm,
  handleDialogClose
} = useCategoryAddModal(props, emit);

const listUnits = ref([]);
const isUnitModalOpen = ref(false);

if (formData.value && !formData.value.listIdDonVi) {
  formData.value.listIdDonVi = [];
}

const fetchAllUnits = async () => {
  try {
    const res = await foodApi.getUnitTypes();
    listUnits.value = res.data || [];
  } catch (error) {
    console.error("Lỗi lấy danh sách định lượng:", error);
  }
};

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    fetchAllUnits();
    if (formData.value) formData.value.listIdDonVi = [];
  }
});

// XỬ LÝ KHI ĐƠN VỊ TẠO THÀNH CÔNG (NHẬN ID TỪ COMPONENT CON)
const handleUnitAdded = async (newUnitId) => {
  isUnitModalOpen.value = false;

  // Tải lại danh sách để Multiselect có lựa chọn mới hiển thị
  await fetchAllUnits();

  // NẾU CÓ ID MỚI TRẢ VỀ -> TỰ ĐỘNG CHỌN NÓ VÀO MULTISELECT
  if (newUnitId) {
    if (!formData.value.listIdDonVi) {
      formData.value.listIdDonVi = [];
    }
    // Kiểm tra xem đã có trong mảng chưa để tránh trùng lặp
    if (!formData.value.listIdDonVi.includes(newUnitId)) {
      formData.value.listIdDonVi.push(newUnitId);
    }
  }
};
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content" style="max-height: 90vh; overflow-y: auto;">

      <GlobalDialogue :show="dialogVisible" :type="dialogConfig?.type" :variant="dialogConfig?.variant"
        :title="dialogConfig?.title" :message="dialogConfig?.message" @close="handleDialogClose"
        @confirm="handleDialogConfirm" />

      <div class="modal-header">
        <h2>Thêm Danh Mục Mới</h2>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body custom-scrollbar">
        <div class="form-container">
          <div class="form-group full-width">
            <label>Tên danh mục <span class="required">*</span></label>
            <input v-model="formData.tenDanhMuc" type="text" placeholder="Nhập tên danh mục...">
          </div>

          <div class="form-group full-width" style="margin-top: 5px;">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <label class="m-0">Định lượng / Kích cỡ áp dụng</label>
              <button type="button" class="btn-quick-add" @click="isUnitModalOpen = true">
                <i class="fas fa-plus-circle"></i> Tạo định lượng mới
              </button>
            </div>
            <Multiselect v-model="formData.listIdDonVi" :options="listUnits" mode="tags" valueProp="id" label="tenDonVi"
              placeholder="-- Chọn các định lượng có sẵn --" :searchable="true" class="custom-multiselect-theme" />
            <small class="text-muted" style="font-size: 0.8rem; margin-top: 4px; display: block;">
              * Có thể chọn nhiều định lượng cùng lúc (VD: ml, gram, ly...)
            </small>
          </div>

          <div class="form-group full-width" style="margin-top: 10px;">
            <label>Mô tả</label>
            <textarea v-model="formData.moTa" rows="3" placeholder="Nhập mô tả..."></textarea>
          </div>

          <!-- <div class="form-group full-width">
            <label>Trạng thái</label>
            <div class="toggle-wrapper" @click="formData.trangThai = formData.trangThai === 1 ? 0 : 1">
              <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }">
                <div class="toggle-knob"></div>
              </div>
              <span :class="{ 'text-active': formData.trangThai === 1 }">
                {{ formData.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}
              </span>
            </div>
          </div> -->
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="$emit('close')">Hủy</button>
        <button class="btn-confirm" @click="handleSave">Thêm mới</button>
      </div>
    </div>

    <UnitAddScreen :isOpen="isUnitModalOpen" :categories="[]" :isQuickAddMode="true" @close="isUnitModalOpen = false"
      @refresh="handleUnitAdded" />
  </div>
</template>

<style scoped src="/src/assets/foodModalManager.css"></style>

<style scoped>
/* Giữ nguyên toàn bộ CSS cũ của bạn */
.btn-quick-add {
  background: none;
  border: none;
  color: #7b121c;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 2px 5px;
  border-radius: 4px;
}

.btn-quick-add:hover {
  background-color: #fdf2f2;
  text-decoration: underline;
  color: #b01a2b;
}

.custom-multiselect-theme {
  --ms-border-color: #ccc;
  --ms-border-color-active: #7b121c;
  --ms-radius: 8px;
  --ms-ring-color: rgba(123, 18, 28, 0.1);
  --ms-placeholder-color: #999;
  --ms-option-bg-pointed: #fdf2f2;
  --ms-option-color-pointed: #7b121c;
  --ms-option-bg-selected: #7b121c;
  --ms-option-color-selected: #ffffff;
  --ms-option-bg-selected-pointed: #600000;
  min-height: 44px;
}

.custom-multiselect-theme :global(.multiselect-is-active) {
  box-shadow: 0 0 0 3px rgba(123, 18, 28, 0.15) !important;
  border-color: #7b121c !important;
}

.custom-multiselect-theme :global(.multiselect-tag) {
  background: #8B0000 !important;
  color: white !important;
  font-weight: 600;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}

:deep(.multiselect.is-active) {
  /* Đổi viền thành màu đỏ rượu */
  border-color: #8B0000 !important;
  
  /* Tạo hiệu ứng phát sáng (glow) viền ngoài màu đỏ trong suốt 20% */
  box-shadow: 0 0 0 4px rgba(139, 0, 0, 0.2) !important;
  
  /* Xóa viền xanh dương mặc định của trình duyệt */
  outline: none !important;
}

/* Kèm thêm class custom của bạn cho chắc chắn (nếu cần) */
.custom-multiselect-theme.is-active {
  border-color: #8B0000 !important;
  box-shadow: 0 0 0 4px rgba(139, 0, 0, 0.2) !important;
  outline: none !important;
}
</style>