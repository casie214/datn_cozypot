<script setup>
import { ref, watch, defineProps, defineEmits, onMounted } from 'vue';
import { useCategoryAddModal, foodApi } from '../../../../../services/foodFunction';
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
import UnitAddScreen from '@/pages/admin/unit/screens/UnitAddScreen.vue';
import Swal from 'sweetalert2';

const props = defineProps({
  isOpen: Boolean,
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
} = useCategoryAddModal(props, emit);

const printerOptions = [
  { value: 1, label: 'Máy in Bếp (Đồ ăn)' },
  { value: 2, label: 'Máy in Quầy Bar (Nước)' },
  { value: 3, label: 'Không in phiếu' }
];

const vatOptions = [
  { value: 1, label: 'Áp dụng VAT Nhà Hàng' },
  { value: 2, label: 'Áp dụng VAT Đóng Gói' }
];

const listUnits = ref([]);
const isUnitModalOpen = ref(false);

if (formData.value && !formData.value.listIdDonVi) {
  formData.value.listIdDonVi = [];
}

const multiselectRef = ref(null); 
const currentSearchText = ref(''); 
const prefilledUnitName = ref(''); 

const handleSearchChange = (query) => {
  currentSearchText.value = query;
};

const handleKeydown = (event) => {
  if (event.key === 'Enter' && currentSearchText.value.trim() !== '') {
    const text = currentSearchText.value.trim();
    
    const exists = listUnits.value.some(
      (unit) => unit.tenDonVi.toLowerCase() === text.toLowerCase()
    );

    if (!exists) {
      event.preventDefault(); 
      event.stopPropagation();
      
      prefilledUnitName.value = text;
      isUnitModalOpen.value = true;

      if (multiselectRef.value) {
        multiselectRef.value.close();
        multiselectRef.value.clearSearch();
      }
    }
  }
};

const openUnitModalNormal = () => {
  prefilledUnitName.value = '';
  isUnitModalOpen.value = true;
};

const fetchAllUnits = async () => {
  try {
    const res = await foodApi.getUnitTypes();
    listUnits.value = res.data || [];
  } catch (error) {
    console.error("Lỗi lấy danh sách định lượng:", error);
  }
};

const handleUnitAdded = async (newUnitId) => {
  isUnitModalOpen.value = false;
  await fetchAllUnits();
  if (newUnitId) {
    if (!formData.value.listIdDonVi) {
      formData.value.listIdDonVi = [];
    }
    if (!formData.value.listIdDonVi.includes(newUnitId)) {
      formData.value.listIdDonVi.push(newUnitId);
    }
  }
};

// ==========================================
// 🚨 LOGIC VALIDATE MỚI 
// ==========================================
const existingCategories = ref([]);

watch(() => props.isOpen, async (val) => {
  if (val) {
    fetchAllUnits();
    if (formData.value) formData.value.listIdDonVi = [];
    if (props.initialName && formData.value) {
      formData.value.tenDanhMuc = props.initialName;
    }
    
    // Lấy danh sách danh mục về để check trùng khi mở modal
    try {
      const res = await foodApi.getCategories();
      existingCategories.value = res.data || [];
    } catch (e) {
      console.error(e);
    }
    
    // Reset lỗi mỗi khi mở lại modal
    formErrors.value = { tenDanhMuc: '', listIdDonVi: '', moTa: '', phanLoaiMayIn: '', apDungLoaiVat: '' };
  }
});

const formErrors = ref({
  tenDanhMuc: '',
  listIdDonVi: '',
  moTa: '',
  phanLoaiMayIn: '',
  apDungLoaiVat: ''
});

const validateForm = () => {
  let isValid = true;
  formErrors.value = { tenDanhMuc: '', listIdDonVi: '', moTa: '', phanLoaiMayIn: '', apDungLoaiVat: '' };

  const tenDM = (formData.value.tenDanhMuc || '').trim();
  const moTa = (formData.value.moTa || '').trim();
  const lstDonVi = formData.value.listIdDonVi || [];

  // 1. Validate Tên danh mục (Độ dài từ 5 - 100)
  if (!tenDM) {
    formErrors.value.tenDanhMuc = "Tên danh mục không được để trống";
    isValid = false;
  } else if (tenDM.length < 5) {
    formErrors.value.tenDanhMuc = "Tên danh mục phải chứa ít nhất 5 kí tự";
    isValid = false;
  } else if (tenDM.length > 100) {
    formErrors.value.tenDanhMuc = "Tên danh mục không được vượt quá 100 ký tự";
    isValid = false;
  } else {
    // Check trùng tên
    const isDuplicate = existingCategories.value.some(
      c => c.tenDanhMuc.toLowerCase() === tenDM.toLowerCase()
    );
    if (isDuplicate) {
      formErrors.value.tenDanhMuc = "Danh mục đã tồn tại trong hệ thống";
      isValid = false;
    }
  }

  // 2. Validate Máy In
  if (!formData.value.phanLoaiMayIn) {
    formErrors.value.phanLoaiMayIn = "Vui lòng chọn Khu vực máy in";
    isValid = false;
  }

  // 3. Validate Loại VAT
  if (!formData.value.apDungLoaiVat) {
    formErrors.value.apDungLoaiVat = "Vui lòng chọn Loại VAT áp dụng";
    isValid = false;
  }

  // 4. Validate Định lượng (Phải chọn ít nhất 1)
  if (lstDonVi.length === 0) {
    formErrors.value.listIdDonVi = "Vui lòng chọn ít nhất 1 Định lượng";
    isValid = false;
  }

  // 5. Validate Mô tả (0 - 255 ký tự)
  if (moTa.length > 255) {
    formErrors.value.moTa = "Mô tả không được vượt quá 255 ký tự";
    isValid = false;
  }

  // 🔥 HIỂN THỊ TOAST NẾU CÓ LỖI (Giống màn Check-in)
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
    formData.value.tenDanhMuc = tenDM;
    formData.value.moTa = moTa;
  }

  return isValid;
};

// Hàm Submit tự custom để chặn lỗi trước khi gửi request
const submitForm = () => {
  if (validateForm()) {
    handleSave(); // Nếu validate Pass thì gọi hàm handleSave của Hook
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
            <input 
              v-model="formData.tenDanhMuc" 
              type="text" 
              placeholder="Nhập tên danh mục..."
              :class="{ 'input-error': formErrors.tenDanhMuc }"
            >
            <small v-if="formErrors.tenDanhMuc" class="error-text">{{ formErrors.tenDanhMuc }}</small>
          </div>

          <div class="d-flex gap-3 mt-3 mb-1">
            <div class="form-group w-50 m-0">
              <label>Máy in liên đơn <span class="required">*</span></label>
              <div :class="{ 'multiselect-error-border': formErrors.phanLoaiMayIn }">
                <Multiselect
                  v-model="formData.phanLoaiMayIn"
                  :options="printerOptions"
                  valueProp="value"
                  label="label"
                  placeholder="-- Chọn khu vực in --"
                  :searchable="false"
                  :canClear="false"
                  class="custom-multiselect-theme"
                />
              </div>
              <small v-if="formErrors.phanLoaiMayIn" class="error-text">{{ formErrors.phanLoaiMayIn }}</small>
            </div>
            
            <div class="form-group w-50 m-0">
              <label>Loại VAT <span class="required">*</span></label>
              <div :class="{ 'multiselect-error-border': formErrors.apDungLoaiVat }">
                <Multiselect
                  v-model="formData.apDungLoaiVat"
                  :options="vatOptions"
                  valueProp="value"
                  label="label"
                  placeholder="-- Chọn loại VAT --"
                  :searchable="false"
                  :canClear="false"
                  class="custom-multiselect-theme"
                />
              </div>
              <small v-if="formErrors.apDungLoaiVat" class="error-text">{{ formErrors.apDungLoaiVat }}</small>
            </div>
          </div> 

          <div class="form-group full-width" style="margin-top: 5px;">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <label class="m-0">Định lượng / Kích cỡ áp dụng <span class="required">*</span></label>
              <button type="button" class="btn-quick-add" @click="openUnitModalNormal">
                <i class="fas fa-plus-circle"></i> Tạo định lượng mới
              </button>
            </div>
            
            <div :class="{ 'multiselect-error-border': formErrors.listIdDonVi }">
              <Multiselect 
                ref="multiselectRef"
                v-model="formData.listIdDonVi" 
                :options="listUnits" 
                mode="tags" 
                valueProp="id" 
                label="tenDonVi"
                placeholder="-- Chọn các định lượng có sẵn --" 
                :searchable="true" 
                class="custom-multiselect-theme"
                @search-change="handleSearchChange"
                @keydown="handleKeydown"
              >
                <template v-slot:noresults>
                  <div style="padding: 5px 10px; color: #8B0000; font-size: 0.9rem;">
                    Không có sẵn. Nhấn <kbd style="background: #eee; padding: 2px 5px; border-radius: 4px;">Enter ↵</kbd> để tạo nhanh "<b>{{ currentSearchText }}</b>"
                  </div>
                </template>
              </Multiselect>
            </div>
            
            <small v-if="formErrors.listIdDonVi" class="error-text">{{ formErrors.listIdDonVi }}</small>
            <small v-else class="text-muted" style="font-size: 0.8rem; margin-top: 4px; display: block;">
              * Có thể chọn nhiều định lượng cùng lúc (VD: ml, gram, ly...)
            </small>
          </div>

          <div class="form-group full-width" style="margin-top: 10px;">
            <label>Mô tả</label>
            <textarea 
              v-model="formData.moTa" 
              rows="3" 
              placeholder="Nhập mô tả..."
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

    <UnitAddScreen 
      :isOpen="isUnitModalOpen" 
      :categories="[]" 
      :isQuickAddMode="true" 
      :initialName="prefilledUnitName"
      @close="isUnitModalOpen = false"
      @refresh="handleUnitAdded" 
    />
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

.input-error {
  border-color: #dc3545 !important;
  background-color: #fff8f8;
}

/* Bôi đỏ viền của thư viện Multiselect */
.multiselect-error-border {
  border: 1px solid #dc3545;
  border-radius: 8px;
}

/* Hiển thị dòng chữ lỗi nhỏ màu đỏ bên dưới */
.error-text {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #dc3545;
  font-style: italic;
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

/* Đè màu đỏ cho cả input của multiselect nếu nó có viền đỏ */
.multiselect-error-border :deep(.multiselect) {
  border-color: #dc3545 !important;
  background-color: #fff8f8 !important;
}

.custom-multiselect-theme.is-active {
  border-color: #8B0000 !important;
  box-shadow: 0 0 0 4px rgba(139, 0, 0, 0.2) !important;
  outline: none !important;
}
</style>