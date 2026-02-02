<script setup>
import GlobalDialogue from '@/components/globalDialogue.vue';
import { useCategoryDetailAddModal } from '../../../../../services/foodFunction';
import { defineProps, defineEmits } from 'vue';

// Nhận props từ cha
const props = defineProps(['isOpen', 'formData', 'listDanhMucGoc']); 
const emit = defineEmits(['close', 'save', 'refresh']);

const { 
  formData, 
  handleSave,
  
  // New variables
  searchQuery,
  filteredCategories,
  isDropdownOpen,
  toggleDropdown,
  selectCategory,
  selectedCategoryName,
  closeDropdown,

  dialogVisible, 
  dialogConfig, 
  handleDialogConfirm, 
  handleDialogClose
} = useCategoryDetailAddModal(props, emit);
</script>

<template>
  <GlobalDialogue 
        :show="dialogVisible"
        :type="dialogConfig?.type"
        :variant="dialogConfig?.variant"
        :title="dialogConfig?.title"
        :message="dialogConfig?.message"
        @close="handleDialogClose"
        @confirm="handleDialogConfirm"
    />
    
  <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content" @click="closeDropdown"> <div class="modal-header">
        <h2>Thêm Chi Tiết Danh Mục </h2>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
            
            <div class="form-group full-width">
                <label>Tên chi tiết <span class="required">*</span></label>
                <input v-model="formData.tenDanhMucChiTiet" type="text" placeholder="VD: Bò Mỹ, Nấm kim châm...">
            </div>

            <div class="form-group full-width relative-container">
                <label>Danh mục gốc <span class="required">*</span></label>
                
                <div class="custom-select-box" @click.stop="toggleDropdown">
                    <span :class="{ 'placeholder': !formData.idDanhMuc }">
                        {{ selectedCategoryName || '-- Chọn danh mục gốc --' }}
                    </span>
                    <i class="fas" :class="isDropdownOpen ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
                </div>

                <div v-if="isDropdownOpen" class="dropdown-list-container" @click.stop>
                    <div class="search-box-wrapper">
                        <input 
                            v-model="searchQuery" 
                            type="text" 
                            class="dropdown-search-input" 
                            placeholder="🔍 Tìm kiếm..."
                            autofocus
                        >
                    </div>
                    
                    <ul class="options-list">
                        <li 
                            v-for="dm in filteredCategories" 
                            :key="dm.id" 
                            @click="selectCategory(dm)"
                            :class="{ 'selected': formData.idDanhMuc === dm.id }"
                        >
                            {{ dm.tenDanhMuc }}
                            <i v-if="formData.idDanhMuc === dm.id" class="fas fa-check check-icon"></i>
                        </li>
                        
                        <li v-if="filteredCategories.length === 0" class="no-result">
                            Không tìm thấy kết quả.
                        </li>
                    </ul>
                </div>
            </div>
            <div class="form-group full-width">
                <label>Mô tả</label>
                <textarea v-model="formData.moTa" rows="3" placeholder="Mô tả..."></textarea>
            </div>

            <div class="form-group full-width">
                <label>Trạng thái</label>
                <div class="toggle-wrapper" @click="formData.trangThai = formData.trangThai === 1 ? 0 : 1">
                    <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }">
                        <div class="toggle-knob"></div>
                    </div>
                    <span :class="{'text-active': formData.trangThai === 1}">
                        {{ formData.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}
                    </span>
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

<style scoped>
@import url("/src/assets/foodModalManager.css");

/* --- Custom Dropdown Styles --- */
.relative-container {
    position: relative; /* Anchor for absolute dropdown */
}

.custom-select-box {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #ccc;
    border-radius: 6px;
    background: #fff;
    cursor: pointer;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    user-select: none;
    transition: border-color 0.2s;
}

.custom-select-box:hover {
    border-color: #8B0000;
}

.placeholder {
    color: #888;
}

.dropdown-list-container {
    position: absolute;
    top: 100%; /* Below the input */
    left: 0;
    width: 100%;
    max-height: 250px;
    background: white;
    border: 1px solid #ccc;
    border-radius: 6px;
    box-shadow: 0 4px 10px rgba(0,0,0,0.1);
    z-index: 100;
    margin-top: 5px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.search-box-wrapper {
    padding: 8px;
    border-bottom: 1px solid #eee;
    background: #f9f9f9;
}

.dropdown-search-input {
    width: 100%;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
    outline: none;
    font-size: 13px;
}

.dropdown-search-input:focus {
    border-color: #8B0000;
}

.options-list {
    list-style: none;
    padding: 0;
    margin: 0;
    overflow-y: auto;
    flex: 1;
}

.options-list li {
    padding: 10px 12px;
    cursor: pointer;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: background 0.2s;
    font-size: 14px;
}

.options-list li:hover {
    background-color: #fce8e8; /* Light red hover */
    color: #8B0000;
}

.options-list li.selected {
    background-color: #fdecec;
    color: #8B0000;
    font-weight: bold;
}

.check-icon {
    color: #8B0000;
    font-size: 12px;
}

.no-result {
    padding: 15px;
    text-align: center;
    color: #888;
    font-style: italic;
    cursor: default;
}
</style>