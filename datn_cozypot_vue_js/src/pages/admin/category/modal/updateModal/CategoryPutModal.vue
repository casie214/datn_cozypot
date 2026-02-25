<script setup>
import { useCategoryPutModal, foodApi } from '../../../../../services/foodFunction';
import { defineProps, defineEmits, ref, watch } from 'vue';
import GlobalDialogue from '../../../../../components/globalDialogue.vue';

// IMPORT THÊM 2 THƯ VIỆN NÀY
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
import UnitAddScreen from '@/pages/admin/unit/screens/UnitAddScreen.vue';

const props = defineProps({
  isOpen: Boolean,
  itemList: Object
});
const emit = defineEmits(['close', 'save', 'refresh']);

const {
    formData,
    handleSave,
    closeModal,
    dialogVisible,
    dialogConfig,
    handleDialogConfirm,
    handleDialogClose
} = useCategoryPutModal(props, emit);

// ==========================================
// THÊM LOGIC QUẢN LÝ ĐỊNH LƯỢNG
// ==========================================
const listUnits = ref([]);
const isUnitModalOpen = ref(false);

const fetchAllUnits = async () => {
  try {
    const res = await foodApi.getUnitTypes();
    listUnits.value = res.data || [];
  } catch (error) {
    console.error("Lỗi lấy danh sách định lượng:", error);
  }
};

// Theo dõi khi Modal mở lên thì load danh sách Unit
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    fetchAllUnits();
    // Đảm bảo listIdDonVi luôn là mảng để multiselect không bị lỗi
    if (formData.value && !formData.value.listIdDonVi) {
      formData.value.listIdDonVi = [];
    }
  }
});

// Xử lý khi thêm nhanh 1 đơn vị mới thành công
const handleUnitAdded = async (newUnitData) => {
  isUnitModalOpen.value = false;

  await fetchAllUnits();
  listUnits.value = [...listUnits.value];

  const idToSelect = typeof newUnitData === 'object' ? newUnitData?.id : newUnitData;

  if (idToSelect) {
    if (!formData.value.listIdDonVi) {
      formData.value.listIdDonVi = [];
    }
    if (!formData.value.listIdDonVi.includes(idToSelect)) {
      formData.value.listIdDonVi = [...formData.value.listIdDonVi, idToSelect];
    }
  }
};
// ==========================================
</script>

<template>
    <div v-if="isOpen" class="modal-overlay" @click.self="closeModal">
        <div class="modal-content" style="max-height: 90vh; overflow-y: auto;">
            
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
                <h2>Cập Nhật Danh Mục</h2>
                <button class="btn-close" @click="closeModal">✕</button>
            </div>

            <div class="modal-body custom-scrollbar">
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

        <UnitAddScreen :isOpen="isUnitModalOpen" :categories="[]" :isQuickAddMode="true" @close="isUnitModalOpen = false"
            @refresh="handleUnitAdded" />
    </div>
</template>

<style scoped src="/src/assets/foodModalManager.css"></style>

<style scoped>
/* SAO CHÉP CSS TỪ BÊN ADD SANG */
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
  border-color: #8B0000 !important;
  box-shadow: 0 0 0 4px rgba(139, 0, 0, 0.2) !important;
  outline: none !important;
}
</style>