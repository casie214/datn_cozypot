<script setup>
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import { useHotpotUpdate } from '../../../../../services/foodFunction';

const {
  formData, listLoaiSet, selectedIngredients, totalComponentsPrice,
  searchQuery, sortOption, filteredFoodList, addIngredient, removeIngredient,
  hotpotInfo, handleUpdate, categoryName, goBack, isLoading, isViewMode, handleFileUpload,
  dialogVisible,
    dialogConfig,
    handleDialogConfirm,
    handleDialogClose
} = useHotpotUpdate();

const getImg = (url) => {
    if (url && (url.startsWith('http') || url.startsWith('data:image'))) {
        return url;
    }
    return 'https://placehold.co/100x100?text=No+Img';
}
</script>

<template>
  <div class="main-content">
    <GlobalDialogue 
            :show="dialogVisible"
            :type="dialogConfig?.type"
            :variant="dialogConfig?.variant"
            :title="dialogConfig?.title"
            :message="dialogConfig?.message"
            @close="handleDialogClose"
            @confirm="handleDialogConfirm"
        />
    <div class="page-header">
      <div class="header-title">
        <h1>{{ isViewMode ? 'Chi Tiết Set Lẩu' : 'Cập nhật Set Lẩu' }}</h1>
        <p class="subtitle">{{ isViewMode ? 'Xem thông tin chi tiết set lẩu' : 'Chỉnh sửa thông tin và thành phần món ăn' }}</p>
      </div>
      <button class="btn-back" @click="goBack">← Quay lại</button>
    </div>

    <div class="info-hero-card" v-if="hotpotInfo" style="margin-bottom: 2em;">
      <div class="hero-image">
        <img :src="getImg(hotpotInfo.hinhAnh)" alt="Ảnh Set Lẩu">
      </div>
      
      <div class="hero-details">
        <div class="hero-header">
          <h2 class="hero-title">
            {{ hotpotInfo.tenSetLau }} 
            <span class="code-badge">#{{ hotpotInfo.maSetLau }}</span>
          </h2>
          <span :class="['status-badge', hotpotInfo.trangThai === 1 ? 'active' : 'inactive']">
            {{ hotpotInfo.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
          </span>
        </div>

        <div class="hero-meta-grid">
          <div class="meta-item">
            <span class="label">Loại Set:</span>
            <span class="value">{{ categoryName }}</span>
          </div>
          <div class="meta-item">
            <span class="label">Giá bán:</span>
            <span class="value price">{{ hotpotInfo.giaBan?.toLocaleString() }} đ</span>
          </div>
          <div class="meta-item">
            <span class="label">Người tạo:</span>
            <span class="value">{{ hotpotInfo.nguoiTao || 'Admin' }}</span>
          </div>
          <div class="meta-item">
            <span class="label">Ngày tạo:</span>
            <span class="value">{{ hotpotInfo.ngayTao || '---' }}</span>
          </div>
        </div>

        <div class="hero-description">
          <span class="label">Mô tả:</span>
          <p>{{ hotpotInfo.moTa || 'Chưa có mô tả cho set lẩu này.' }}</p>
        </div>
      </div>
    </div>

    <div v-if="isLoading" class="loading-state">Đang tải dữ liệu...</div>

    <div v-else class="page-content" :class="{ 'view-mode': isViewMode }">
      
      <div class="section-left">
        <div class="card">
          <h3>Thông tin chung</h3>
          <div class="form-container">
            <div class="form-group">
              <label>Tên Set Lẩu <span class="required" v-if="!isViewMode">*</span></label>
              <input :disabled="isViewMode" v-model="formData.tenSetLau" type="text">
            </div>
            <div class="form-group">
              <label>Loại Set</label>
              <select :disabled="isViewMode" v-model="formData.idLoaiSet" class="form-control">
                <option v-for="cat in listLoaiSet" :key="cat.id" :value="cat.id">{{ cat.tenLoaiSet }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Giá bán</label>
              <input :disabled="isViewMode" v-model="formData.giaBan" type="number">
              <div class="price-hint">Giá thành phần: {{ totalComponentsPrice.toLocaleString() }} đ</div>
            </div>
            
            <div class="form-group">
              <label>Hình ảnh</label>
              <div class="upload-container" v-if="!isViewMode">
                <label class="custom-file-upload">
                  <input type="file" accept="image/*" @change="handleFileUpload" />
                  <i class="fas fa-cloud-upload-alt"></i> Chọn ảnh từ máy
                </label>
                <button v-if="formData.hinhAnh" class="btn-clear-img" @click="formData.hinhAnh = ''">Xóa ảnh</button>
              </div>
              <div class="image-preview-box" v-if="formData.hinhAnh">
                <img :src="formData.hinhAnh" alt="Preview" class="preview-img">
              </div>
            </div>

            <div class="form-group">
              <label>Trạng thái</label>
              <div class="toggle-wrapper" :class="{ 'disabled': isViewMode }" @click="!isViewMode && (formData.trangThai = formData.trangThai === 1 ? 0 : 1)">
                <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }">
                  <div class="toggle-knob"></div>
                </div>
                <span>{{ formData.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="section-right">
        
        <div class="card ingredient-selector" style="margin-bottom: 1.4em;" v-if="!isViewMode">
          <h3>Thêm món vào Set</h3>
          <div class="filter-tools">
            <input v-model="searchQuery" type="text" class="search-input" placeholder="Tìm món...">
            </div>
          <div class="scroll-list-container">
            <div v-for="item in filteredFoodList" :key="item.id" class="food-item-card" @click="addIngredient(item)">
               <img :src="getImg(item.hinhAnh)" class="food-thumb">
               <div class="food-info">
                  <div class="food-name">{{ item.tenChiTietMonAn || item.tenDanhMucChiTiet }}</div>
                  <span class="food-price">{{ item.giaBan?.toLocaleString() }}đ</span>
               </div>
               <button class="btn-add-mini">+</button>
            </div>
          </div>
        </div>

        <div class="card selected-list-card">
          <h3>Thành phần ({{ selectedIngredients.length }})</h3>
          <div class="selected-items-container">
            <div v-for="(item, index) in selectedIngredients" :key="item.id" class="selected-item-row">
              <img :src="getImg(item.hinhAnh)" class="selected-thumb">
              <div class="selected-info">
                <div class="selected-name">{{ item.ten }}</div>
                <div class="selected-unit">{{ item.donVi }}</div>
                <div class="selected-price-mini" v-if="isViewMode">
                   {{ item.giaBan?.toLocaleString() }}đ x {{ item.soLuong }}
                </div>
              </div>
              <div class="qty-control">
                <span v-if="isViewMode" class="qty-text">x{{ item.soLuong }}</span>
                <input v-else type="number" v-model="item.soLuong" min="1" class="qty-input-small">
              </div>
              <button class="btn-remove-icon" @click="removeIngredient(index)" v-if="!isViewMode">✕</button>
            </div>
          </div>
          
          <div class="total-summary" v-if="isViewMode">
             <span>Tổng giá trị thực:</span>
             <span class="highlight">{{ totalComponentsPrice.toLocaleString() }} đ</span>
          </div>
        </div>
      </div>
    </div>

    <div class="page-footer">
      <button class="btn-large btn-cancel" @click="goBack">
        {{ isViewMode ? 'Quay lại danh sách' : 'Hủy bỏ' }}
      </button>
      <button class="btn-large btn-save" @click="handleUpdate" v-if="!isViewMode">Cập nhật</button>
    </div>
  </div>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");

.loading-state {
  text-align: center;
  padding: 50px;
  font-size: 1.2rem;
  color: #666;
}

.preview-img-small {
  height: 60px;
  margin-top: 5px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.view-mode .toggle-wrapper.disabled { opacity: 0.6; pointer-events: none; }
.qty-text { font-weight: bold; color: #d32f2f; font-size: 1.1rem; }
.selected-price-mini { font-size: 0.8rem; color: #666; }
.total-summary {
    margin-top: 15px; padding-top: 15px; border-top: 1px dashed #ddd;
    display: flex; justify-content: space-between; font-size: 1.1rem;
}
.highlight { color: #d32f2f; font-weight: bold; }
</style>