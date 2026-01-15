<script setup>
import { defineProps, defineEmits } from 'vue';
import { useHotpotModal } from '../screens/foodFunction';

const props = defineProps({
  isOpen: Boolean,
  hotpotItem: Object
});

const emit = defineEmits(['close']);

const { 
  currentView, 
  selectedVariant, 
  variants, 
  openEditMode, 
  backToList,
  closeModal
} = useHotpotModal(props, emit);
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      
      <div class="modal-header">
        <div class="header-left">
          <button v-if="currentView === 'update'" @click="backToList" class="btn-back">←</button>
          <h2>{{ currentView === 'list' ? 'Chi tiết Set Lẩu' : 'Cập nhật Set Lẩu' }}</h2>
        </div>
        <button class="btn-close" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">
        
        <div v-if="currentView === 'list'">
          <div class="info-grid">
            <div class="info-item"><span>Tên Set Lẩu</span> <b>{{ hotpotItem?.ten || 'N/A' }}</b></div>
            <div class="info-item"><span>Mã Set</span> <b>{{ hotpotItem?.ma || 'N/A' }}</b></div>
            <div class="info-item"><span>Loại lẩu</span> <b>{{ hotpotItem?.loailau || 'N/A' }}</b></div>
            <div class="info-item full-width"><span>Mô tả</span> <span class="text-gray">Nước lẩu đậm đà, bao gồm tôm, mực, ngao...</span></div>
          </div>

          <hr class="divider">

          <h3 class="section-title">Các kích cỡ hiện có</h3>
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
              <div>Thêm kích cỡ</div>
            </div>
          </div>
        </div>

        <div v-else class="update-view">
          <div class="info-grid">
            <div class="info-item"><span>Set Lẩu</span> <b>{{ hotpotItem?.ten }}</b></div>
            <div class="info-item"><span>Loại lẩu</span> <b>{{ hotpotItem?.loailau }}</b></div>
            <div class="info-item"><span>Kích cỡ đang sửa</span> <b style="color: #8B0000;">{{ selectedVariant?.name }}</b></div>
          </div>
          
          <hr class="divider">
        </div>

        <div class="form-container">
          <div class="form-row">
            <div class="form-group">
              <label>Tên kích cỡ (VD: Set Nhỏ)</label>
              <input type="text" :placeholder="currentView === 'update' ? selectedVariant.name : 'Placeholder'" :disabled="currentView === 'list'">
            </div>
            <div class="form-group">
              <label>Khẩu phần (VD: 2-3 người)</label>
              <input type="text" placeholder="Nhập khẩu phần ăn" :disabled="currentView === 'list'">
            </div>
          </div>
          <div class="form-row">
             <div class="form-group">
              <label>Mã chi tiết</label>
              <input type="text" :placeholder="currentView === 'update' ? selectedVariant.code : 'Placeholder'" :disabled="currentView === 'list'">
            </div>
            <div class="form-group">
              <label>Đơn vị tính</label>
              <input type="text" value="Set" disabled>
            </div>
          </div>
          <div class="form-group full-width">
             <label>Giá bán</label>
             <input type="number" :placeholder="currentView === 'update' ? selectedVariant.price : 'Placeholder'" :disabled="currentView === 'list'">
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
/* CSS giữ nguyên style chuẩn của dự án */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; width: 650px; border-radius: 12px; overflow: hidden; display: flex; flex-direction: column; box-shadow: 0 4px 20px rgba(0,0,0,0.2); animation: slideDown 0.3s ease; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; background: #fff; }
.header-left { display: flex; align-items: center; gap: 10px; }
.modal-header h2 { margin: 0; font-size: 18px; color: #333; }
.btn-back, .btn-close { background: none; border: none; font-size: 18px; cursor: pointer; color: #666; }
.modal-body { padding: 25px; }

/* Info Grid */
.info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 15px; }
.info-item { display: flex; flex-direction: column; font-size: 13px; }
.info-item.full-width { grid-column: span 3; }
.info-item span { color: #888; margin-bottom: 4px; }
.text-gray { color: #666; font-style: italic; }
.divider { border: 0; border-top: 1px solid #eee; margin: 20px 0; }

/* Variants Grid */
.section-title { font-size: 14px; margin-bottom: 10px; color: #333; }
.variants-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 20px; }
.variant-card { border: 1px solid #ddd; border-radius: 8px; padding: 15px; cursor: pointer; transition: 0.2s; position: relative; background: #f9f9f9; }
.variant-card:hover { border-color: #8B0000; background: #fff5f5; }
.v-header { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 8px; }
.v-price { font-weight: bold; color: #d32f2f; font-size: 15px; }
.add-card { display: flex; flex-direction: column; align-items: center; justify-content: center; border: 1px dashed #8B0000; color: #8B0000; font-weight: bold; background: white; }

/* Form */
.form-row { display: flex; gap: 15px; margin-bottom: 15px; }
.form-group { flex: 1; display: flex; flex-direction: column; }
.form-group.full-width { width: 100%; }
.form-group label { font-size: 12px; color: #888; margin-bottom: 5px; }
.form-group input { padding: 10px; border: 1px solid #ddd; border-radius: 6px; outline: none; background: #fff; }
.form-group input:disabled { background: #f2f2f2; color: #999; border-color: #eee; }
.form-group input:focus { border-color: #8B0000; }

/* Footer */
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; background: #f9f9f9; }
.btn-cancel { background: #eee; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; color: #666; font-weight: 500;}
.btn-confirm { background: #8B0000; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; color: white; font-weight: bold; }

@keyframes slideDown { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>