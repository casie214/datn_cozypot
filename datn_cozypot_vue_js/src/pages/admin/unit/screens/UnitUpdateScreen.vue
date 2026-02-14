<script setup>
import { ref, watch } from 'vue';
import { foodApi } from '@/services/foodFunction';
import Swal from 'sweetalert2';

const props = defineProps({
    isOpen: Boolean,
    itemList: Object // Nhận item được chọn từ bảng
});
const emit = defineEmits(['close', 'refresh']);

const formData = ref({
    id: null,
    kichCo: '',
    dinhLuong: '',
    tenHienThi: ''
});

// Load dữ liệu khi nhấn sửa
watch(() => props.itemList, (newVal) => {
    if (newVal) {
        formData.value = { ...newVal };
    }
}, { immediate: true });

const handleUpdate = async () => {
    // Trim dữ liệu
    formData.value.kichCo = (formData.value.kichCo || '').trim();
    formData.value.dinhLuong = (formData.value.dinhLuong || '').trim();
    formData.value.tenHienThi = (formData.value.tenHienThi || '').trim();

    if (!formData.value.kichCo || !formData.value.tenHienThi) {
        return Swal.fire('Lỗi', 'Không được để trống các trường bắt buộc!', 'error');
    }

    try {
        await foodApi.updateUnit(formData.value.id, formData.value);
        Swal.fire({ icon: 'success', title: 'Đã cập nhật!', timer: 1200, showConfirmButton: false });
        emit('refresh');
        emit('close');
    } catch (error) {
        Swal.fire('Lỗi', 'Cập nhật thất bại!', 'error');
    }
};
</script>

<template>
    <div v-if="isOpen" class="modal-overlay">
        <div class="modal-container">
            <div class="modal-header">
                <h3 style="color: white !important;">Chỉnh sửa định lượng</h3>
                <button @click="emit('close')" class="close-btn">&times;</button>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label>Kích cỡ / Đơn vị</label>
                    <input v-model="formData.kichCo" type="text" class="form-control">
                </div>
                <div class="form-group">
                    <label>Giá trị định lượng</label>
                    <input v-model="formData.dinhLuong" type="text" class="form-control">
                </div>
                <div class="form-group">
                    <label>Tên hiển thị Menu</label>
                    <input v-model="formData.tenHienThi" type="text" class="form-control">
                </div>
            </div>
            <div class="modal-footer">
                <button @click="emit('close')" class="btn-secondary">Hủy</button>
                <button @click="handleUpdate" class="btn-save">Cập nhật thay đổi</button>
            </div>
        </div>
    </div>
</template>

<style scoped src="/src/assets/foodModalManager.css"></style>