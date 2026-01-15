<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import axios from 'axios'; 

const props = defineProps({
  isOpen: Boolean,
  foodId: Number 
});

const emit = defineEmits(['close', 'refresh']);

const newItem = ref({
  tenChiTietMonAn: '',
  maChiTietMonAn: '',
  giaBan: 0,
  kichCo: '',
  donVi: '',
  trangThai: 1
});

// const handleSave = async () => {
//     try {
        

//         console.log("ok, id:", props.foodId);
        
//         emit('refresh'); 
//         emit('close'); 
//     } catch (e) {
//         console.error(e);
//         alert("Lỗi");
//     }
// };
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <button class="btn-back" @click="$emit('close')">←</button>
        <h2>Thêm Chi Tiết Mới</h2>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
            <div class="form-group">
                <label>Tên chi tiết</label>
                <input v-model="newItem.tenChiTietMonAn" type="text" placeholder="VD: Size L">
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Mã chi tiết</label>
                    <input v-model="newItem.maChiTietMonAn" type="text">
                </div>
                <div class="form-group">
                    <label>Giá bán</label>
                    <input v-model="newItem.giaBan" type="number">
                </div>
            </div>
             <div class="form-row">
                <div class="form-group">
                    <label>Kích cỡ</label>
                    <input v-model="newItem.kichCo" type="text">
                </div>
                <div class="form-group">
                    <label>Đơn vị</label>
                    <input v-model="newItem.donVi" type="text">
                </div>
            </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="$emit('close')">Hủy</button>
        <button class="btn-confirm" @click="handleSave">Lưu lại</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Copy CSS modal cũ sang, nhưng chỉnh z-index cao hơn để nó hiện đè lên */
.modal-overlay { 
    background: rgba(0, 0, 0, 0.6); 
    z-index: 2000; /* Cao hơn modal cha (thường là 1000) */
}
.modal-content { width: 500px; /* Nhỏ hơn modal cha chút cho đẹp */ }
/* ... Các CSS form khác ... */
</style>