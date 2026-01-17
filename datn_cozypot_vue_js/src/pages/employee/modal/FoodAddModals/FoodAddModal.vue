<script setup>
import { defineProps, defineEmits } from 'vue';
// Import file logic vừa tạo (nhớ chỉnh đúng đường dẫn)
import { useFoodAddModal } from '../../../../services/foodFunction'; 

const props = defineProps({
  isOpen: Boolean
});

const emit = defineEmits(['close', 'refresh']);

const { 
  formData, 
  listDanhMuc, 
  filteredSubCategories, 
  handleSave 
} = useFoodAddModal(props, emit);

</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      
      <div class="modal-header">
        <h2>Thêm Món Ăn Mới</h2>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
            
            <div class="form-row">
                <div class="form-group">
                    <label>Tên món ăn <span class="required">*</span></label>
                    <input v-model="formData.tenMonAn" type="text" placeholder="Tên món ăn">
                </div>
                <div class="form-group">
                    <label>Giá bán</label>
                    <input v-model="formData.giaBan" type="text" placeholder="Giá bán">
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>Danh mục gốc <span class="required">*</span></label>
                    <select v-model="formData.idDanhMuc" class="form-control">
                        <option value="">-- Chọn danh mục --</option>
                        <option v-for="dm in listDanhMuc" :key="dm.id" :value="dm.id">
                            {{ dm.tenDanhMuc }}
                        </option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Danh mục chi tiết <span class="required">*</span></label>
                    <select v-model="formData.idDanhMucChiTiet" :disabled="!formData.idDanhMuc" class="form-control">
                        <option value="">-- Chọn chi tiết --</option>
                        <option v-for="sub in filteredSubCategories" :key="sub.id" :value="sub.id">
                            {{ sub.tenDanhMucChiTiet }}
                        </option>
                    </select>
                </div>
            </div>

            <div class="form-group full-width">
                <label>Link hình ảnh</label>
                <input v-model="formData.hinhAnh" type="text" placeholder="https://...">
            </div>

            <div class="form-group full-width">
                <label>Mô tả</label>
                <textarea v-model="formData.moTa" rows="3"></textarea>
            </div>
             
             <div class="form-group">
                <label>Trạng thái</label>
                <div class="toggle-switch" :class="{ 'on': formData.trangThaiKinhDoanh === 1 }" @click="formData.trangThaiKinhDoanh = (formData.trangThaiKinhDoanh === 1 ? 0 : 1)">
                  <div class="toggle-knob"></div>
                </div>
             </div>

        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="$emit('close')">Hủy</button>
        <button class="btn-confirm" @click="handleSave">Thêm mới</button>
      </div>

    </div>
  </div>
</template>

<style scoped src="../../modal/foodModalManager.css">
</style>