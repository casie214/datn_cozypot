<script setup>
import { useRouter } from 'vue-router';
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import { useFoodUpdate } from '../../../../../services/foodFunction';
import { ref } from 'vue';

const {
  isViewMode, isLoading, formData, foodInfo, variants, categoryName,
  handleUpdate, goBack, goToAddDetail, handleToggleDetailStatus,
  fileInputRef, triggerFileInput, handleFileUpload, removeImage, goToDetailTable, goToFoodListFilter,
  isCatDropdownOpen, catSearchQuery, toggleCatDropdown, filteredCategories, selectCategory, selectedCategoryName,
  isSubCatDropdownOpen, subCatSearchQuery, toggleSubCatDropdown, filteredSubCategories, selectSubCategory, selectedSubCategoryName, closeAllDropdowns,
  errors
} = useFoodUpdate();

const router = useRouter();
const getImg = (url) => {
  if (url && (url.startsWith('http') || url.startsWith('data:image'))) {
    return url;
  }
  return 'https://placehold.co/100x100?text=No+Img';
}

const goToVariantDetail = (variant) => {
  router.push({
    name: 'viewFoodDetail',
    params: {
      id: variant.id
    },
  });
};

const goToEditDetail = (variant) => {
  console.log("Chuyển sang chế độ sửa:", variant);
  
  router.push({
    name: 'updateFoodDetail',
    params: { 
      id: variant.id 
    }
  });
};

const hoveredVariant = ref(null);
const tooltipStyle = ref({ top: '0px', left: '0px' });
const tooltipPlacement = ref('top'); // 'top' hoặc 'bottom' để xoay mũi tên

const showTooltip = (event, variant) => {
  const rect = event.currentTarget.getBoundingClientRect();
  hoveredVariant.value = variant;

  const safeZoneTop = 220;

  const leftPos = rect.left + (rect.width / 2);

  if (rect.top < safeZoneTop) {
    tooltipPlacement.value = 'bottom';
    tooltipStyle.value = {
      top: `${rect.bottom + 12}px`, 
      left: `${leftPos}px`
    };
  } else {
    tooltipPlacement.value = 'top';
    tooltipStyle.value = {
      top: `${rect.top - 12}px`,
      left: `${leftPos}px`
    };
  }
};

const hideTooltip = () => {
  hoveredVariant.value = null;
};

</script>

<template>
  <div class="main-content">

    <GlobalDialogue :show="dialogVisible" :type="dialogConfig?.type" :variant="dialogConfig?.variant"
      :title="dialogConfig?.title" :message="dialogConfig?.message" @close="handleDialogClose"
      @confirm="handleDialogConfirm" />

    <div class="page-header">
      <div class="header-title">
        <h1>{{ isViewMode ? 'Chi tiết món ăn' : 'Cập nhật món ăn' }}</h1>
        <p class="subtitle">{{ isViewMode ? 'Xem thông tin và các chi tiết món ăn' : 'Chỉnh sửa thông tin chung' }}</p>
      </div>
      <button class="btn-back" @click="goBack">← Quay lại</button>
    </div>

    <div v-if="isLoading" class="loading-state">Đang tải dữ liệu...</div>

    <div v-else>

      <div class="info-hero-card" v-if="foodInfo" style="margin-bottom: 30px;">
        <div class="hero-image">
          <img :src="getImg(foodInfo.hinhAnh)" alt="Food Img">
        </div>
        <div class="hero-details">
          <div class="hero-header">
            <h2 class="hero-title">
              {{ foodInfo.tenMonAn || formData.tenMonAn }}
              <span class="code-badge">#{{ foodInfo.maMonAn }}</span>
            </h2>
            <span :class="['status-badge', formData.trangThaiKinhDoanh === 1 ? 'active' : 'inactive']">
              {{ formData.trangThaiKinhDoanh === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </span>
          </div>
          <div class="hero-meta-grid">
            <div class="meta-item">
              <span class="label">Danh mục:</span>
              <span style="width: 60%; cursor: pointer;" class="value clickable-link"
                @click="goToFoodListFilter('root')" title="Lọc món ăn theo danh mục này">
                {{ selectedCategoryName }} <i class="fas fa-filter small-icon"></i>
              </span>
            </div>
            <div class="meta-item">
              <span class="label">Danh mục chi tiết:</span>
              <span style="width: 60%; cursor: pointer;" class="value clickable-link" @click="goToFoodListFilter('sub')"
                title="Lọc món ăn theo chi tiết này">
                {{ selectedSubCategoryName }} <i class="fas fa-filter small-icon"></i>
              </span>
            </div>
            <div class="meta-item">
              <span class="label">Số lượng chi tiết món:</span>
              <span class="value">{{ variants.length }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="page-content" :class="{ 'view-mode': isViewMode }">

        <div class="section-left">
          <div class="card">
            <h3>Thông tin chung</h3>
            <div class="form-container">

              <div class="form-group">
                <label>Tên món ăn <span class="required" v-if="!isViewMode">*</span></label>
                <input :disabled="isViewMode" v-model="formData.tenMonAn" type="text"
                  :class="{ 'invalid-border': errors.tenMonAn }" @input="errors.tenMonAn = ''">
                <span class="error-message" v-if="errors.tenMonAn">{{ errors.tenMonAn }}</span>
              </div>

              <div class="form-row-2">
                <div class="form-group relative-container">
                  <label>Danh mục gốc <span class="required">*</span></label>
                  <div class="custom-select-box" :class="{ 'invalid-border': errors.idDanhMuc }"
                    @click.stop="toggleCatDropdown">
                    <span :class="{ 'placeholder': !formData.idDanhMuc }">
                      {{ selectedCategoryName || '-- Chọn danh mục --' }}
                    </span>
                    <i class="fas" :class="isCatDropdownOpen ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
                  </div>
                  <span class="error-message" v-if="errors.idDanhMuc">{{ errors.idDanhMuc }}</span>

                  <div v-if="isCatDropdownOpen" class="dropdown-list-container" @click.stop>
                    <div class="search-box-wrapper">
                      <input v-model="catSearchQuery" type="text" class="dropdown-search-input"
                        placeholder="🔍 Tìm kiếm..." autofocus>
                    </div>
                    <ul class="options-list">
                      <li v-for="dm in filteredCategories" :key="dm.id" @click="selectCategory(dm)"
                        :class="{ 'selected': formData.idDanhMuc === dm.id }">
                        {{ dm.tenDanhMuc }} <i v-if="formData.idDanhMuc === dm.id" class="fas fa-check check-icon"></i>
                      </li>
                      <li v-if="filteredCategories.length === 0" class="no-result">Không tìm thấy kết quả.
                      </li>
                    </ul>
                  </div>
                </div>

                <div class="form-group relative-container">
                  <label>Chi tiết <span class="required">*</span></label>
                  <div class="custom-select-box"
                    :class="{ 'disabled': !formData.idDanhMuc, 'invalid-border': errors.idDanhMucChiTiet }"
                    @click.stop="toggleSubCatDropdown">
                    <span :class="{ 'placeholder': !formData.idDanhMucChiTiet }">
                      {{ selectedSubCategoryName || '-- Chọn chi tiết --' }}
                    </span>
                    <i class="fas" :class="isSubCatDropdownOpen ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
                  </div>
                  <span class="error-message" v-if="errors.idDanhMucChiTiet">{{ errors.idDanhMucChiTiet }}</span>

                  <div v-if="isSubCatDropdownOpen" class="dropdown-list-container" @click.stop>
                    <div class="search-box-wrapper">
                      <input v-model="subCatSearchQuery" type="text" class="dropdown-search-input"
                        placeholder="🔍 Tìm kiếm..." autofocus>
                    </div>
                    <ul class="options-list">
                      <li v-for="sub in filteredSubCategories" :key="sub.id" @click="selectSubCategory(sub)"
                        :class="{ 'selected': formData.idDanhMucChiTiet === sub.id }">
                        {{ sub.tenDanhMucChiTiet }} <i v-if="formData.idDanhMucChiTiet === sub.id"
                          class="fas fa-check check-icon"></i>
                      </li>
                      <li v-if="filteredSubCategories.length === 0" class="no-result">Không tìm thấy kết quả.
                      </li>
                    </ul>
                  </div>
                </div>
              </div>

              <div class="form-group">
                <label>Hình ảnh</label>
                <div class="image-action-bar" v-if="!isViewMode">
                  <input type="file" ref="fileInputRef" accept="image/*" class="hidden-input"
                    @change="handleFileUpload" />
                  <button type="button" class="btn-action btn-upload" :class="{ 'invalid-border': errors.hinhAnh }"
                    @click="triggerFileInput">
                    <i class="fas fa-cloud-upload-alt"></i> Thay đổi ảnh
                  </button>
                </div>
                <div class="image-preview-container" :class="{ 'has-image': formData.hinhAnh }">
                  <img v-if="formData.hinhAnh" :src="getImg(formData.hinhAnh)">
                </div>
                <span class="error-message" v-if="errors.hinhAnh">{{ errors.hinhAnh }}</span>
              </div>

            </div>
          </div>
        </div>

        <div class="section-right">
          <div class="card">
            <div class="card-header-row">
              <h3>Các chi tiết món ({{ variants.length }})</h3>
              <button class="btn-show-table" @click="goToDetailTable" title="Xem danh sách chi tiết">
                Xem bảng
              </button>
              <button v-if="!isViewMode" class="btn-show-table" @click="goToAddDetail">
                + Thêm loại
              </button>
            </div>

            <div class="variants-list">
              <div v-for="v in variants" :key="v.id" class="variant-item" @mouseenter="showTooltip($event, v)"
                @mouseleave="hideTooltip">
                <div class="v-thumb">
                  <img :src="getImg(v.hinhAnh)" alt="Ảnh" class="img-fit">
                </div>

                <div class="v-info">
                  <div class="v-name clickable-name" @click="goToVariantDetail(v)">{{ v.tenChiTietMonAn }}</div>
                  <div class="v-meta">
                    <span class="badge-size">{{ v.kichCo }}</span>
                    <span class="text-unit">{{ v.donVi }}</span>
                  </div>
                </div>
                <div class="v-price">
                  {{ v.giaBan?.toLocaleString() }} đ
                </div>

                 <i style="cursor:pointer" class="fas fa-pen edit-icon me-2" title="Chỉnh sửa chi tiết món"
                  @click="goToEditDetail(v)"></i>

                
              </div>

              <div v-if="variants.length === 0" class="empty-text">Chưa có chi tiết món nào.</div>
            </div>
          </div>
        </div>

      </div>

      <div class="page-footer">
        <button class="btn-large btn-cancel" @click="goBack">
          {{ isViewMode ? 'Quay lại danh sách' : 'Hủy bỏ' }}
        </button>
        <button v-if="!isViewMode" class="btn-large btn-save" @click="handleUpdate">Cập nhật</button>
      </div>
    </div>
  </div>

  <Teleport to="body">
    <div v-if="hoveredVariant" class="fixed-tooltip" :class="tooltipPlacement" :style="tooltipStyle">

      <div class="tooltip-header">
        <strong>{{ hoveredVariant.tenChiTietMonAn }}</strong>
        <span class="tooltip-badge">{{ hoveredVariant.trangThai === 1 ? 'Đang bán' : 'Ngưng bán' }}</span>
      </div>

      <div class="tooltip-body">
        <div class="tooltip-row">
          <span>Mã:</span> <strong>{{ hoveredVariant.maChiTietMonAn || '---' }}</strong>
        </div>
        <div class="tooltip-row">
          <span>Kích cỡ:</span> <strong>{{ hoveredVariant.kichCo || 'Tiêu chuẩn' }}</strong>
        </div>
        <div class="tooltip-row">
          <span>Đơn vị:</span> <strong>{{ hoveredVariant.donVi }}</strong>
        </div>
        <div class="tooltip-row">
          <span>Giá bán:</span> <strong class="price-text">{{ hoveredVariant.giaBan?.toLocaleString() }} đ</strong>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");

@media (max-width: 900px) {
  .page-content {
    grid-template-columns: 1fr;
  }
}

.relative-container {
  position: relative;
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
  height: 42px;
}

.custom-select-box:hover {
  border-color: #8B0000;
}

.custom-select-box.disabled {
  background-color: #f5f5f5;
  color: #aaa;
  cursor: not-allowed;
  border-color: #ddd;
  pointer-events: none;
}

.placeholder {
  color: #888;
}

.dropdown-list-container {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  max-height: 250px;
  background: white;
  border: 1px solid #ccc;
  border-radius: 6px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
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
  background-color: #fce8e8;
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

.invalid-border {
  border: 1px solid #dc3545 !important;
}

.error-message {
  color: #dc3545;
  font-size: 0.85em;
  margin-top: 4px;
  display: block;
}

.fixed-tooltip {
  position: fixed;
  /* Cố định theo màn hình */
  width: 280px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 5px 30px rgba(0, 0, 0, 0.25);
  /* Bóng đổ đậm hơn để nổi bật */
  border: 1px solid #ddd;
  z-index: 99999;
  /* Luôn nằm trên cùng (trên cả Modal/Header) */
  pointer-events: none;
  /* Chuột xuyên qua */

  /* Hiệu ứng xuất hiện */
  animation: fadeInTooltip 0.15s ease-out;
}

/* Định vị mũi tên chung */
.fixed-tooltip::after {
  content: "";
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  border-width: 8px;
  border-style: solid;
}

/* --- TRƯỜNG HỢP 1: HIỆN Ở TRÊN (PLACEMENT = TOP) --- */
.fixed-tooltip.top {
  transform: translate(-50%, -100%);
  /* Đẩy lên trên */
}

/* Mũi tên chĩa xuống */
.fixed-tooltip.top::after {
  top: 100%;
  border-color: white transparent transparent transparent;
}

/* --- TRƯỜNG HỢP 2: HIỆN Ở DƯỚI (PLACEMENT = BOTTOM) --- */
.fixed-tooltip.bottom {
  transform: translate(-50%, 0);
  /* Giữ nguyên vị trí tính toán */
}

/* Mũi tên chĩa lên */
.fixed-tooltip.bottom::after {
  bottom: 100%;
  border-color: transparent transparent white transparent;
}

@keyframes fadeInTooltip {
  from {
    opacity: 0;
    transform: translate(-50%, -90%) scale(0.95);
  }

  to {
    opacity: 1;
  }

  /* Transform đích được set bởi class .top/.bottom */
}

/* --- STYLES NỘI DUNG (Giữ nguyên cho đẹp) --- */
.tooltip-header {
  background-color: #8B0000;
  color: white;
  padding: 10px 15px;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.tooltip-badge {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
}

.tooltip-body {
  padding: 12px 15px;
  font-size: 13px;
  color: #333;
}

.tooltip-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  border-bottom: 1px dashed #f0f0f0;
  padding-bottom: 6px;
}

.tooltip-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.price-text {
  color: #d32f2f;
  font-weight: bold;
}

.desc-text {
  max-width: 150px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #666;
  font-style: italic;
}
</style>