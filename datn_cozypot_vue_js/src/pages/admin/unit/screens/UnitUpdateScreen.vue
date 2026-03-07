<script setup>
import { ref, watch, nextTick } from 'vue'; // THÊM nextTick VÀO ĐÂY
import { foodApi } from '@/services/foodFunction';
import Swal from 'sweetalert2';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
import CategoryAddModal from '../../category/modal/addModal/CategoryAddModal.vue';

const props = defineProps({
    isOpen: Boolean,
    unitItem: Object,
    categories: Array,
    isQuickAddMode: { type: Boolean, default: false }
});
const emit = defineEmits(['close', 'refresh']);

const formData = ref({
    id: null,
    tenDonVi: '',
    moTa: '',
    listIdDanhMuc: [],
    values: []
});

const newValueInput = ref('');
const categoriesList = ref([]);

// 1. LUÔN LẮNG NGHE CHA TRUYỀN VÀO NHƯNG DÙNG BIẾN CỤC BỘ ĐỂ HIỂN THỊ
watch(() => props.categories, (newVal) => {
    categoriesList.value = newVal ? [...newVal] : [];
}, { immediate: true, deep: true });

const isCategoryModalOpen = ref(false);
const prefilledCategoryName = ref('');
const categoryMultiselectRef = ref(null);
const currentCategorySearch = ref('');

const handleCategorySearchChange = (query) => {
    currentCategorySearch.value = query;
};

const handleCategoryKeydown = (event) => {
    if (props.isQuickAddMode) return; 

    if (event.key === 'Enter' && currentCategorySearch.value.trim() !== '') {
        const text = currentCategorySearch.value.trim();
        const exists = categoriesList.value.some(c => c.tenDanhMuc?.toLowerCase() === text.toLowerCase());

        if (!exists) {
            event.preventDefault();
            event.stopPropagation();
            
            if (categoryMultiselectRef.value) {
                categoryMultiselectRef.value.close();
                categoryMultiselectRef.value.clearSearch();
            }

            prefilledCategoryName.value = text;
            isCategoryModalOpen.value = true;
        }
    }
};

// 2. XỬ LÝ ĐỒNG BỘ TRIỆT ĐỂ BẰNG NEXTTICK
const handleCategoryAdded = async (newCategoryData) => {
    isCategoryModalOpen.value = false;
    
    try {
        // Tự tải API và gán ngay lập tức vào options
        const res = await foodApi.getCategories();
        categoriesList.value = res.data ? [...res.data] : [];
        
        // CHỜ VUE CẬP NHẬT DANH SÁCH MỚI VÀO TRONG DOM CỦA MULTISELECT
        await nextTick();
        
        let idToSelect = null;
        if (newCategoryData) {
            idToSelect = typeof newCategoryData === 'object' ? newCategoryData.id : newCategoryData;
        }
        
        if (!idToSelect && prefilledCategoryName.value) {
            const newlyAdded = categoriesList.value.find(
                (c) => c.tenDanhMuc?.toLowerCase() === prefilledCategoryName.value.toLowerCase()
            );
            if (newlyAdded) idToSelect = newlyAdded.id;
        }

        // TẠO MẢNG MỚI ĐỂ ÉP VUE NHẬN DIỆN SỰ THAY ĐỔI CỦA V-MODEL
        if (idToSelect) {
            const currentList = [...formData.value.listIdDanhMuc];
            if (!currentList.includes(idToSelect)) {
                currentList.push(idToSelect);
                formData.value.listIdDanhMuc = currentList;
            }
        }
    } catch (e) {
        console.error("Lỗi lấy danh mục:", e);
    } finally {
        prefilledCategoryName.value = '';
        // Phát tín hiệu cho Component cha ngầm tải lại mà không ảnh hưởng UI hiện tại
    }
};

// Map dữ liệu khi form sửa được mở lên
watch(() => props.isOpen, (newVal) => {
    if (newVal && props.unitItem) {
        formData.value = {
            id: props.unitItem.id,
            tenDonVi: props.unitItem.tenDonVi || '',
            moTa: props.unitItem.moTa || '',
            listIdDanhMuc: props.unitItem.listIdDanhMuc 
                ? [...props.unitItem.listIdDanhMuc] 
                : [],
            values: props.unitItem.values 
                ? props.unitItem.values.map(v => ({ id: v.id, giaTri: v.giaTri }))
                : []
        };
        newValueInput.value = '';
    }
}, { immediate: true });

const addValue = () => {
    const val = newValueInput.value.trim();
    if (!val) return;

    if (formData.value.values.some(v => v.giaTri === val)) {
        return Swal.fire('Thông báo', 'Giá trị này đã tồn tại!', 'info');
    }

    formData.value.values.push({ id: null, giaTri: val });
    newValueInput.value = '';
};

const removeValue = (index) => {
    formData.value.values.splice(index, 1);
};

const handleUpdate = async () => {
    if (!formData.value.tenDonVi.trim()) {
        return Swal.fire('Lỗi', 'Tên đơn vị không được để trống!', 'warning');
    }
    if (formData.value.listIdDanhMuc.length === 0) {
        return Swal.fire('Lỗi', 'Vui lòng chọn ít nhất 1 danh mục!', 'warning');
    }
    if (formData.value.values.length === 0) {
        return Swal.fire('Lỗi', 'Phải có ít nhất 1 giá trị định lượng!', 'warning');
    }

    try {
        await foodApi.updateUnitType(formData.value.id, formData.value);

        Swal.fire({
            icon: 'success',
            iconColor: '#7D161A',
            title: props.isQuickAddMode ? 'Đã thêm giá trị mới!' : 'Cập nhật thành công!',
            timer: 1200,
            showConfirmButton: false
        });
        emit('refresh');
        emit('close');
    } catch (error) {
        console.error(error);
        const msg = error.response?.data?.message || 'Cập nhật thất bại. Vui lòng thử lại!';
        Swal.fire('Lỗi', msg, 'error');
    }
};
</script>
<template>
    <div v-if="isOpen" class="modal-overlay">
        <div class="modal-container">
            <div class="modal-header">
                <h3>{{ isQuickAddMode ? 'Thêm nhanh giá trị định lượng' : 'Cập nhật đơn vị tính' }}</h3>
                <button @click="emit('close')" class="close-btn">&times;</button>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <label>Tên đơn vị tính <span class="required">*</span></label>
                    <input v-model="formData.tenDonVi" type="text" class="form-control" placeholder="VD: ml, gram..." :disabled="isQuickAddMode">
                </div>

                <div class="form-group">
                    <label>Áp dụng cho danh mục <span class="required">*</span></label>
                    <div class="multiselect-container">
                        <Multiselect 
                            ref="categoryMultiselectRef"
                            v-model="formData.listIdDanhMuc" 
                            mode="tags" 
                            :options="categoriesList"
                            label="tenDanhMuc" 
                            valueProp="id" 
                            placeholder="-- Chọn danh mục --" 
                            :searchable="true" 
                            :disabled="isQuickAddMode"
                            @search-change="handleCategorySearchChange"
                            @keydown="handleCategoryKeydown"
                        >
                            <template v-slot:noresults>
                                <div style="padding: 5px 10px; color: #8B0000; font-size: 0.9rem;" v-if="!isQuickAddMode">
                                    Không có sẵn. Nhấn <kbd style="background: #eee; padding: 2px 5px; border-radius: 4px;">Enter ↵</kbd> để tạo nhanh "<b>{{ currentCategorySearch }}</b>"
                                </div>
                            </template>
                        </Multiselect>
                    </div>
                </div>

                <div class="form-group">
                    <label>Các giá trị định lượng (Thêm/Xóa)</label>

                    <div class="input-with-btn">
                        <input v-model="newValueInput" type="text" class="form-control"
                            placeholder="Nhập số và nhấn Enter..." @keyup.enter="addValue">
                        <button @click="addValue" class="btn-add-val" title="Thêm"><i class="fas fa-plus"></i></button>
                    </div>

                    <div class="values-list mt-2">
                        <div v-for="(v, index) in formData.values" :key="index" class="value-tag"
                            :class="{ 'is-new': !v.id }">
                            {{ v.giaTri }}
                            <span v-if="!(isQuickAddMode && v.id)" @click="removeValue(index)" class="remove-tag">&times;</span>
                        </div>
                        <div v-if="formData.values.length === 0" class="empty-hint">
                            Danh sách trống. Vui lòng thêm giá trị.
                        </div>
                    </div>
                    
                </div>

                <div class="form-group">
                    <label>Mô tả</label>
                    <textarea v-model="formData.moTa" class="form-control" rows="2" :disabled="isQuickAddMode"></textarea>
                </div>
            </div>

            <div class="modal-footer">
                <button @click="emit('close')" class="btn-secondary">Hủy</button>
                <button @click="handleUpdate" class="btn-primary">Lưu thay đổi</button>
            </div>
        </div>
        <CategoryAddModal 
            :isOpen="isCategoryModalOpen" 
            :initialName="prefilledCategoryName"
            @close="isCategoryModalOpen = false"
            @refresh="handleCategoryAdded" 
        />
    </div>
</template>

<style scoped>
/* Reuse style từ UnitAddScreen để đồng bộ */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
}

.modal-container {
    background: white;
    width: 550px;
    border-radius: 12px;
    overflow: hidden;
    animation: slideDown 0.3s;
}

.modal-header {
    background-color: #8B0000;
    color: white;
    padding: 16px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.modal-header h3 {
    margin: 0;
    font-size: 1.3rem;
    color: white !important;
}

.modal-body {
    padding: 20px 24px;
    max-height: 70vh;
    overflow-y: auto;
}

.close-btn {
    background: none;
    border: none;
    color: white;
    font-size: 1.8rem;
    cursor: pointer;
}

.form-group {
    margin-bottom: 15px;
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.form-group label {
    font-weight: 600;
    color: #444;
}

.required {
    color: #d32f2f;
}

.form-control {
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 6px;
    width: 100%;
}

.form-control:disabled {
    background-color: #f5f5f5;
    cursor: not-allowed;
}

/* Style input thêm giá trị */
.input-with-btn {
    display: flex;
    gap: 8px;
}

.btn-add-val {
    background: #444;
    color: white;
    border: none;
    padding: 0 15px;
    border-radius: 6px;
    cursor: pointer;
}

/* Style danh sách tags */
.values-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 10px;
    background: #f8f9fa;
    border: 1px dashed #ccc;
    border-radius: 8px;
    min-height: 45px;
}

.value-tag {
    background: #8B0000;
    color: white;
    padding: 4px 12px;
    border-radius: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    font-size: 0.9rem;
}

/* Highlight item mới thêm */
.value-tag.is-new {
    background: #28a745;
}

.remove-tag {
    cursor: pointer;
    font-size: 1.2rem;
    line-height: 1;
}

.remove-tag:hover {
    color: #ffcccc;
}

.modal-footer {
    padding: 16px 24px;
    border-top: 1px solid #eee;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

.btn-secondary {
    padding: 10px 20px;
    background: #f1f3f5;
    border: 1px solid #dee2e6;
    border-radius: 6px;
    cursor: pointer;
}

.btn-primary {
    padding: 10px 24px;
    background: #8B0000;
    color: white;
    border: none;
    border-radius: 6px;
    font-weight: 600;
    cursor: pointer;
}

:deep(.multiselect-tag) {
    background: #8B0000 !important;
}

:deep(.multiselect-container.is-disabled) {
    opacity: 0.7;
    cursor: not-allowed;
}
</style>