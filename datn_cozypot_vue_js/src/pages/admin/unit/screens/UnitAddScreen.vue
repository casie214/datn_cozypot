<script setup>
import { ref, watch } from 'vue';
import { foodApi } from '@/services/foodFunction';
import Swal from 'sweetalert2';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';

const props = defineProps({
    isOpen: Boolean,
    categories: Array
});
const emit = defineEmits(['close', 'refresh']);

// Data cấu trúc mới theo DonViRequest
const formData = ref({
    tenDonVi: '',
    moTa: '',
    listIdDanhMuc: [],
    values: [] // Chứa danh sách { giaTri: '...' }
});

const newValueInput = ref(''); // Biến tạm để nhập từng giá trị định lượng

// Reset form khi mở modal
watch(() => props.isOpen, (newVal) => {
    if (newVal) {
        formData.value = {
            tenDonVi: '',
            moTa: '',
            listIdDanhMuc: [],
            values: []
        };
        newValueInput.value = '';
    }
});

// Thêm một giá trị định lượng vào danh sách (VD: gõ 200 rồi Enter)
const addValue = () => {
    const val = newValueInput.value.trim();
    if (!val) return;

    // Kiểm tra trùng lặp trong mảng hiện tại
    if (formData.value.values.some(v => v.giaTri === val)) {
        return Swal.fire('Chú ý', 'Giá trị này đã có trong danh sách!', 'info');
    }

    formData.value.values.push({ giaTri: val });
    newValueInput.value = ''; // Clear input
};

// Xóa một giá trị định lượng khỏi danh sách
const removeValue = (index) => {
    formData.value.values.splice(index, 1);
};

const handleSave = async () => {
    // 1. Validate
    if (!formData.value.tenDonVi.trim()) {
        return Swal.fire('Chú ý', 'Vui lòng nhập tên đơn vị (VD: ml, gram)!', 'warning');
    }

    if (formData.value.listIdDanhMuc.length === 0) {
        return Swal.fire('Chú ý', 'Vui lòng chọn ít nhất một danh mục áp dụng!', 'warning');
    }

    if (formData.value.values.length === 0) {
        return Swal.fire('Chú ý', 'Vui lòng thêm ít nhất một giá trị định lượng!', 'warning');
    }

    // 2. Xác nhận lưu
    const result = await Swal.fire({
        title: 'Xác nhận',
        text: `Tạo đơn vị "${formData.value.tenDonVi}" với ${formData.value.values.length} giá trị?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#8B0000',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        try {
            // Backend nhận DonViRequest { tenDonVi, moTa, values: [{giaTri},...], listIdDanhMuc: [...] }
            await foodApi.createUnitType(formData.value);

            Swal.fire({ icon: 'success', title: 'Thành công!', timer: 1500, showConfirmButton: false });
            emit('refresh');
            emit('close');
        } catch (error) {
            console.error(error);
            const msg = error.response?.data?.message || 'Không thể thêm đơn vị. Vui lòng thử lại!';
            Swal.fire('Lỗi', msg, 'error');
        }
    }
};
</script>

<template>
    <div v-if="isOpen" class="modal-overlay">
        <div class="modal-container">
            <div class="modal-header">
                <h3 style="color: white !important;">Thêm đơn vị tính & định lượng</h3>
                <button @click="emit('close')" class="close-btn">&times;</button>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <label>Tên đơn vị tính (VD: ml, gram, đĩa) <span class="required">*</span></label>
                    <input v-model="formData.tenDonVi" type="text" class="form-control"
                        placeholder="Nhập tên đơn vị...">
                </div>

                <div class="form-group">
                    <label>Áp dụng cho danh mục <span class="required">*</span></label>
                    <Multiselect v-model="formData.listIdDanhMuc" :options="categories" label="tenDanhMuc"
                        valueProp="id" mode="tags" placeholder="-- Chọn danh mục --" :searchable="true" />
                </div>

                <div class="form-group">
                    <label>Giá trị định lượng con (VD: 200, 500, 1.5) <span class="required">*</span></label>
                    <div class="input-with-btn">
                        <input v-model="newValueInput" type="text" class="form-control"
                            placeholder="Nhập số và nhấn Enter..." @keyup.enter="addValue">
                        <button @click="addValue" class="btn-add-val"><i class="fas fa-plus"></i></button>
                    </div>

                    <div class="values-list mt-2">
                        <div v-for="(v, index) in formData.values" :key="index" class="value-tag">
                            {{ v.giaTri }}
                            <span @click="removeValue(index)" class="remove-tag">&times;</span>
                        </div>
                        <div v-if="formData.values.length === 0" class="empty-hint">
                            Chưa có giá trị nào được thêm.
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <label>Mô tả đơn vị</label>
                    <textarea v-model="formData.moTa" class="form-control" rows="2"
                        placeholder="Nhập mô tả ngắn..."></textarea>
                </div>
            </div>

            <div class="modal-footer">
                <button @click="emit('close')" class="btn-secondary">Hủy bỏ</button>
                <button @click="handleSave" class="btn-primary">Lưu đơn vị</button>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* Giữ nguyên các style Modal cũ của bạn và thêm các style mới bên dưới */
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
}

.modal-body {
    padding: 20px 24px;
    max-height: 70vh;
    overflow-y: auto;
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
    text-align: left;
}

.required {
    color: #d32f2f;
}

.form-control {
    width: 100%;
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 6px;
}

/* Style cho phần nhập giá trị con */
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

.remove-tag {
    cursor: pointer;
    font-size: 1.2rem;
    line-height: 1;
}

.empty-hint {
    color: #999;
    font-size: 0.85rem;
    font-style: italic;
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
</style>