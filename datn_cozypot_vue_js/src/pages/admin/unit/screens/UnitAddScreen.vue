<script setup>
import { ref, watch } from 'vue';
import { foodApi } from '@/services/foodFunction'; 
import Swal from 'sweetalert2';
import Multiselect from '@vueform/multiselect';
// Import theme mặc định của multiselect
import '@vueform/multiselect/themes/default.css';

const props = defineProps({
    isOpen: Boolean,
    categories: Array
});
const emit = defineEmits(['close', 'refresh']);

const formData = ref({
    idDanhMuc: null,
    kichCo: '',
    dinhLuong: '',
    tenHienThi: ''
});

// Reset form khi mở modal
watch(() => props.isOpen, (newVal) => {
    if (newVal) {
        formData.value = { 
            idDanhMuc: null, 
            kichCo: '', 
            dinhLuong: '', 
            tenHienThi: '' 
        };
    }
});

const handleSave = async () => {
    // 1. Validate và Trim dữ liệu
    formData.value.kichCo = (formData.value.kichCo || '').trim();
    formData.value.dinhLuong = (formData.value.dinhLuong || '').trim();
    formData.value.tenHienThi = (formData.value.tenHienThi || '').trim();

    if (!formData.value.idDanhMuc) {
        return Swal.fire('Chú ý', 'Vui lòng chọn danh mục áp dụng!', 'warning');
    }

    if (!formData.value.kichCo || !formData.value.tenHienThi) {
        return Swal.fire('Chú ý', 'Vui lòng nhập đầy đủ Kích cỡ và Tên hiển thị!', 'warning');
    }

    // 2. Xác nhận lưu
    const result = await Swal.fire({
        title: 'Xác nhận',
        text: 'Thêm định lượng mới này?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#8B0000',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        try {
            await foodApi.createUnit(formData.value);
            Swal.fire({ icon: 'success', title: 'Thành công!', timer: 1500, showConfirmButton: false });
            emit('refresh');
            emit('close');
        } catch (error) {
            console.error(error);
            Swal.fire('Lỗi', 'Không thể thêm định lượng. Vui lòng thử lại!', 'error');
        }
    }
};
</script>

<template>
    <div v-if="isOpen" class="modal-overlay">
        <div class="modal-container">
            <div class="modal-header">
                <h3>Thêm định lượng mới</h3>
                <button @click="emit('close')" class="close-btn">&times;</button>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <label>Áp dụng cho danh mục <span class="required">*</span></label>
                    <div class="multiselect-container">
                        <Multiselect
                            v-model="formData.idDanhMuc"
                            :options="categories"
                            label="tenDanhMuc"
                            valueProp="id"
                            placeholder="-- Chọn danh mục (VD: Đồ uống, Khai vị) --"
                            :searchable="true"
                            :can-clear="true"
                            noOptionsText="Không có dữ liệu"
                            noResultsText="Không tìm thấy danh mục"
                        />
                    </div>
                </div>
                
                <div class="form-group">
                    <label>Kích cỡ / Đơn vị (VD: Đĩa, Ly, Lon) <span class="required">*</span></label>
                    <input v-model="formData.kichCo" type="text" class="form-control" placeholder="Nhập đơn vị (VD: Đĩa)...">
                </div>

                <div class="form-group">
                    <label>Giá trị định lượng (VD: 200g, 330ml)</label>
                    <input v-model="formData.dinhLuong" type="text" class="form-control" placeholder="Nhập thông số (VD: 200g)...">
                </div>

                <div class="form-group">
                    <label>Tên hiển thị Menu <span class="required">*</span></label>
                    <input v-model="formData.tenHienThi" type="text" class="form-control" placeholder="Tên khách sẽ nhìn thấy (VD: Đĩa lớn)...">
                </div>
            </div>

            <div class="modal-footer">
                <button @click="emit('close')" class="btn-secondary">Hủy bỏ</button>
                <button @click="handleSave" class="btn-primary">Lưu lại</button>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* CSS cho Modal Overlay và Container */
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
    max-width: 95%;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    overflow: hidden;
    animation: slideDown 0.3s ease-out;
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

.close-btn {
    background: none;
    border: none;
    color: white;
    font-size: 1.8rem;
    cursor: pointer;
    line-height: 1;
}

.modal-body {
    padding: 24px;
}

.form-group {
    margin-bottom: 18px;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.form-group label {
    font-weight: 600;
    color: #444;
    font-size: 0.95rem;
    text-align: left;
}

.required {
    color: #d32f2f;
}

.form-control {
    width: 100%;
    padding: 10px 14px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 1rem;
    transition: border-color 0.2s;
}

.form-control:focus {
    border-color: #8B0000;
    outline: none;
    box-shadow: 0 0 0 3px rgba(139, 0, 0, 0.1);
}

.modal-footer {
    padding: 16px 24px;
    border-top: 1px solid #eee;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.btn-secondary {
    padding: 10px 20px;
    background: #f1f3f5;
    border: 1px solid #dee2e6;
    border-radius: 6px;
    font-weight: 500;
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

.btn-primary:hover {
    background: #660000;
}

@keyframes slideDown {
    from { opacity: 0; transform: translateY(-20px); }
    to { opacity: 1; transform: translateY(0); }
}

/* CSS CUSTOM CHO MULTISELECT - SỬA LỖI DẤU CHẤM TRÒN */
.multiselect-container {
    width: 100%;
}

:deep(.multiselect) {
    border: 1px solid #ccc;
    border-radius: 6px;
    min-height: 42px;
}

:deep(.multiselect.is-active) {
    border-color: #8B0000;
    box-shadow: 0 0 0 3px rgba(139, 0, 0, 0.1);
}

/* Triệt tiêu bullet points */
:deep(.multiselect-options) {
    list-style: none !important;
    padding: 0 !important;
    margin: 0 !important;
}

:deep(.multiselect-option) {
    padding: 10px 14px;
}

:deep(.multiselect-option.is-selected) {
    background: #8B0000;
}

:deep(.multiselect-option.is-selected.is-pointed) {
    background: #660000;
}

:deep(.multiselect-placeholder) {
    color: #999;
}
</style>