<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  foodItem: Object 
});

const emit = defineEmits(['close']);

const currentView = ref('list'); 
const selectedVariant = ref(null); 

const variants = ref([
  { id: 1, name: 'Chai 1.5 L', price: '18.000 VNĐ', rawPrice: 18000, code: 'CCLA-11-15' },
  { id: 2, name: 'Chai 1.8 L', price: '20.000 VNĐ', rawPrice: 20000, code: 'CCLA-11-18' },
  { id: 3, name: 'Chai 2 L', price: '23.000 VNĐ', rawPrice: 23000, code: 'CCLA-11-20' },
  { id: 4, name: 'Chai 2.5 L', price: '26.000 VNĐ', rawPrice: 26000, code: 'CCLA-11-25' },
]);

const openEditMode = (variant) => {
  selectedVariant.value = variant;
  currentView.value = 'update';
};

const backToList = () => {
  selectedVariant.value = null;
  currentView.value = 'list';
};

const closeModal = () => {
  backToList();
  emit('close');
};
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      
      <div class="modal-header">
        <div class="header-left">
          <button v-if="currentView === 'update'" @click="backToList" class="btn-back">←</button>
          <h2>{{ currentView === 'list' ? 'Chi tiết' : 'Cập nhật loại chi tiết' }}</h2>
        </div>
        <button class="btn-close" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">
        
        <div v-if="currentView === 'list'">
          <div class="info-grid">
            <div class="info-item"><span>Món ăn</span> <b>{{ foodItem?.ten || 'N/A' }}</b></div>
            <div class="info-item"><span>Mã món ăn</span> <b>{{ foodItem?.ma || 'N/A' }}</b></div>
            <div class="info-item"><span>Mô tả</span> <span class="text-gray">Lorem ipsum</span></div>
            <div class="info-item"><span>Danh mục</span> <b>{{ foodItem?.danhmuc || 'N/A' }}</b></div>
            <div class="info-item"><span>Chi tiết danh mục</span> <b>{{ foodItem?.chitiet || 'N/A' }}</b></div>
          </div>

          <hr class="divider">

          <div class="variants-grid">
            <div 
              v-for="v in variants" 
              :key="v.id" 
              class="variant-card"
              @click="openEditMode(v)"
            >
              <div class="v-header">
                <b>{{ v.name }}</b>
                <span class="icon-edit">🏷️</span>
              </div>
              <div class="v-price">{{ v.price }}</div>
            </div>

            <div class="variant-card add-card">
              <div class="icon-plus">+</div>
              <div>Thêm loại</div>
            </div>
          </div>
        </div>

        <div v-else class="update-view">
          <div class="info-grid">
            <div class="info-item"><span>Món ăn</span> <b>{{ foodItem?.ten }}</b></div>
            <div class="info-item"><span>Mã món ăn</span> <b>{{ foodItem?.ma }}</b></div>
            <div class="info-item"><span>Chi tiết món</span> <b>{{ selectedVariant?.name }}</b></div>
            <div class="info-item"><span>Danh mục</span> <b>{{ foodItem?.danhmuc }}</b></div>
            <div class="info-item"><span>Chi tiết danh mục</span> <b>{{ foodItem?.chitiet }}</b></div>
            <div class="info-item"><span>Mã chi tiết</span> <b>{{ selectedVariant?.code }}</b></div>
          </div>
          
          <hr class="divider">
        </div>

        <div class="form-container">
          <div class="form-row">
            <div class="form-group">
              <label>Tên chi tiết</label>
              <input type="text" :placeholder="currentView === 'update' ? selectedVariant.name : 'Placeholder'">
            </div>
            <div class="form-group">
              <label>Kích cỡ</label>
              <input type="text" placeholder="Placeholder">
            </div>
          </div>
          <div class="form-row">
             <div class="form-group">
              <label>Mã chi tiết</label>
              <input type="text" :placeholder="currentView === 'update' ? selectedVariant.code : 'Placeholder'">
            </div>
            <div class="form-group">
              <label>Đơn vị</label>
              <input type="text" placeholder="Placeholder">
            </div>
          </div>
          <div class="form-group full-width">
             <label>Giá bán</label>
             <input type="number" :placeholder="currentView === 'update' ? selectedVariant.price : 'Placeholder'">
          </div>
        </div>

      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="closeModal">Hủy</button>
        <button class="btn-confirm">Xác nhận thay đổi</button>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* Overlay nền tối */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
}

/* Khung modal */
.modal-content {
  background: white;
  width: 600px;
  max-height: 90vh;
  border-radius: 12px;
  overflow-y: auto;
  display: flex; flex-direction: column;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
  animation: slideDown 0.3s ease;
}

/* Header */
.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex; justify-content: space-between; align-items: center;
}
.header-left { display: flex; align-items: center; gap: 10px; }
.modal-header h2 { margin: 0; font-size: 18px; color: #333; }
.btn-back, .btn-close {
  background: none; border: none; font-size: 18px; cursor: pointer; color: #666;
}
.btn-back:hover, .btn-close:hover { color: #8B0000; }

/* Body */
.modal-body { padding: 20px; }

/* Info Grid */
.info-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 15px;
}
.info-item { display: flex; flex-direction: column; font-size: 13px; }
.info-item span { color: #888; margin-bottom: 4px; }
.info-item b { color: #333; }
.text-gray { color: #666; font-style: italic; }

.divider { border: 0; border-top: 1px solid #eee; margin: 15px 0; }

/* Variants Grid (Các thẻ giá) */
.variants-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 20px;
}
.variant-card {
  border: 1px solid #ddd; border-radius: 8px; padding: 12px;
  cursor: pointer; transition: 0.2s; position: relative;
}
.variant-card:hover { border-color: #8B0000; background: #fff5f5; }
.v-header { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 5px; }
.v-price { font-weight: bold; color: #333; }
.add-card {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  border: 1px dashed #8B0000; color: #8B0000; font-weight: bold;
}
.icon-plus { font-size: 20px; margin-bottom: 5px; }

/* Form */
.form-container { margin-top: 10px; }
.form-row { display: flex; gap: 15px; margin-bottom: 15px; }
.form-group { flex: 1; display: flex; flex-direction: column; }
.form-group.full-width { width: 100%; }
.form-group label { font-size: 12px; color: #888; margin-bottom: 5px; }
.form-group input {
  padding: 10px; border: 1px solid #ddd; border-radius: 6px; outline: none; background: #f9f9f9;
}
.form-group input:disabled { background: #fff; border: 1px solid #eee; color: #ccc; cursor: not-allowed; }
.form-group input:focus { border-color: #8B0000; background: white; }

/* Footer */
.modal-footer {
  padding: 15px 20px; border-top: 1px solid #eee;
  display: flex; justify-content: flex-end; gap: 10px;
}
.btn-cancel {
  background: #eee; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; color: #666;
}
.btn-confirm {
  background: #8B0000; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; color: white; font-weight: bold;
}

@keyframes slideDown {
  from { transform: translateY(-20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>