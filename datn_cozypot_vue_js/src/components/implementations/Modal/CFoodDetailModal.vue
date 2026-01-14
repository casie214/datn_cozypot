<script setup>
import { ref, watch, computed } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  detailItem: Object // Dữ liệu dòng được chọn (nếu là sửa)
});

const emit = defineEmits(['close', 'save']);

// Form data reactive
const formData = ref({
  ma: '',
  ten: '',
  monAnGoc: '', // FK: id_mon_an_di_kem
  gia: '',
  kichCo: '',
  donVi: '',
  trangThai: true
});

// Giả lập danh sách món ăn để chọn (Dropdown)
const listMonAn = [
  { id: 1, name: 'Coca-Cola' },
  { id: 2, name: 'Fanta' },
  { id: 3, name: 'Viên thả lẩu' }
];

// Nếu có detailItem truyền vào -> Chế độ Sửa (Điền dữ liệu vào form)
// Nếu detailItem null -> Chế độ Thêm mới (Reset form)
watch(() => props.detailItem, (newVal) => {
  if (newVal) {
    formData.value = { ...newVal }; // Clone dữ liệu
  } else {
    // Reset form
    formData.value = {
      ma: '', ten: '', monAnGoc: '', gia: '', kichCo: '', donVi: '', trangThai: true
    };
  }
});

const handleSave = () => {
  // Validate cơ bản nếu cần
  emit('save', formData.value);
  emit('close');
};
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      
      <div class="modal-header">
        <h2>{{ detailItem ? 'Cập nhật chi tiết món' : 'Thêm chi tiết món mới' }}</h2>
        <button class="btn-close" @click="emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-grid">
          
          <div class="form-group">
            <label>Mã chi tiết <span class="required">*</span></label>
            <input v-model="formData.ma" type="text" placeholder="VD: CCLA-11-15">
          </div>
          <div class="form-group">
            <label>Tên chi tiết <span class="required">*</span></label>
            <input v-model="formData.ten" type="text" placeholder="VD: Chai 1.5 L">
          </div>

          <div class="form-group">
            <label>Thuộc món ăn (FK)</label>
            <select v-model="formData.monAnGoc">
              <option value="">-- Chọn món ăn --</option>
              <option v-for="mon in listMonAn" :key="mon.id" :value="mon.id">{{ mon.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Giá bán</label>
            <input v-model="formData.gia" type="text" placeholder="VD: 18.000">
          </div>

          <div class="form-group">
            <label>Kích cỡ</label>
            <input v-model="formData.kichCo" type="text" placeholder="VD: Lớn, Nhỏ, 1.5L">
          </div>
          <div class="form-group">
            <label>Đơn vị tính</label>
            <input v-model="formData.donVi" type="text" placeholder="VD: Chai, Lon, Đĩa">
          </div>

           <div class="form-group full-width">
            <label>Trạng thái kinh doanh</label>
             <div class="toggle-wrapper">
                <span>{{ formData.trangThai ? 'Đang hoạt động' : 'Ngưng hoạt động' }}</span>
                <div class="toggle-switch" :class="{ 'on': formData.trangThai }" @click="formData.trangThai = !formData.trangThai">
                  <div class="toggle-knob"></div>
                </div>
             </div>
          </div>

        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="emit('close')">Hủy</button>
        <button class="btn-confirm" @click="handleSave">
          {{ detailItem ? 'Lưu thay đổi' : 'Thêm mới' }}
        </button>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* Tái sử dụng style modal cũ để đồng bộ */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex; justify-content: center; align-items: center; z-index: 1000;
}
.modal-content {
  background: white; width: 600px; border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2); overflow: hidden;
  animation: slideDown 0.3s ease;
}
.modal-header {
  padding: 15px 20px; border-bottom: 1px solid #eee;
  display: flex; justify-content: space-between; align-items: center;
}
.modal-header h2 { margin: 0; font-size: 18px; color: #333; }
.btn-close { background: none; border: none; font-size: 20px; cursor: pointer; color: #666; }

.modal-body { padding: 25px; }

/* Form Grid Layout */
.form-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 20px;
}
.form-group { display: flex; flex-direction: column; }
.form-group.full-width { grid-column: span 2; }
.form-group label { font-size: 13px; color: #666; margin-bottom: 6px; font-weight: 500; }
.required { color: red; }

.form-group input, .form-group select {
  padding: 10px; border: 1px solid #ddd; border-radius: 6px; outline: none;
  font-size: 14px;
}
.form-group input:focus, .form-group select:focus { border-color: #8B0000; }

/* Toggle Switch Style */
.toggle-wrapper { display: flex; align-items: center; gap: 10px; margin-top: 5px;}
.toggle-switch { width: 40px; height: 22px; background: #ccc; border-radius: 20px; position: relative; cursor: pointer; transition: 0.3s; }
.toggle-switch.on { background: #2e7d32; }
.toggle-knob { width: 18px; height: 18px; background: white; border-radius: 50%; position: absolute; top: 2px; left: 2px; transition: 0.3s; }
.toggle-switch.on .toggle-knob { left: 20px; }

.modal-footer {
  padding: 15px 20px; border-top: 1px solid #eee; background: #f9f9f9;
  display: flex; justify-content: flex-end; gap: 10px;
}
.btn-cancel { background: #eee; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: 500;}
.btn-confirm { background: #8B0000; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }

@keyframes slideDown { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>