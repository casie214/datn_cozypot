<script setup>
import { useRouter } from 'vue-router';
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import { useHotpotUpdate } from '../../../../../services/foodFunction';
import { ref } from 'vue';

const router = useRouter();
const {
  formData, listLoaiSet, selectedIngredients, totalComponentsPrice,
  searchQuery, sortOption, filteredFoodList, addIngredient, removeIngredient,
  hotpotInfo, handleUpdate, categoryName, goBack, isLoading, isViewMode, handleFileUpload,
  errors // Lấy biến errors
} = useHotpotUpdate();

const getImg = (url) => {
  if (url && (url.startsWith('http') || url.startsWith('data:image'))) {
    return url;
  }
  return 'https://placehold.co/100x100?text=No+Img';
}

const goToDetailTable = () => {
  const currentSetId = formData.value.id || hotpotInfo.value?.id;

  if (currentSetId) {
    router.push({
      name: 'foodManager',
      query: {
        tab: 'chitietTD',
        preHotpot: currentSetId
      }
    });
  } else {
    alert("Không tìm thấy ID Set Lẩu");
  }
};

const goToVariantDetail = (variant) => {
  router.push({
    name: 'viewFoodDetail',
    params: {
      id: variant.id
    }
  });
};

const hoveredItem = ref(null);
const tooltipStyle = ref({ top: '0px', left: '0px' });
const tooltipPlacement = ref('top');

const showTooltip = (event, item) => {
  const rect = event.currentTarget.getBoundingClientRect();
  hoveredItem.value = item;

  const safeZoneTop = 200;
  const leftPos = rect.left + (rect.width / 2);

  if (rect.top < safeZoneTop) {
    tooltipPlacement.value = 'bottom';
    tooltipStyle.value = {
      top: `${rect.bottom + 10}px`,
      left: `${leftPos}px`
    };
  } else {
    tooltipPlacement.value = 'top';
    tooltipStyle.value = {
      top: `${rect.top - 10}px`,
      left: `${leftPos}px`
    };
  }
};

const hideTooltip = () => {
  hoveredItem.value = null;
};
</script>

<template>
  <div class="main-content">
    <GlobalDialogue :show="dialogVisible" :type="dialogConfig?.type" :variant="dialogConfig?.variant"
      :title="dialogConfig?.title" :message="dialogConfig?.message" @close="handleDialogClose"
      @confirm="handleDialogConfirm" />
    <div class="page-header">
      <div class="header-title">
        <h1>{{ isViewMode ? 'Chi tiết set lẩu' : 'Cập nhật set lẩu' }}</h1>
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
              <input :disabled="isViewMode" v-model="formData.tenSetLau" type="text"
                :class="{ 'invalid-border': errors.tenSetLau }" @input="errors.tenSetLau = ''">
              <span class="error-message" v-if="errors.tenSetLau">{{ errors.tenSetLau }}</span>
            </div>

            <div class="form-group">
              <label>Loại Set</label>
              <select :disabled="isViewMode" v-model="formData.idLoaiSet" class="form-control"
                :class="{ 'invalid-border': errors.idLoaiSet }" @change="errors.idLoaiSet = ''">
                <option v-for="cat in listLoaiSet" :key="cat.id" :value="cat.id">{{ cat.tenLoaiSet }}</option>
              </select>
              <span class="error-message" v-if="errors.idLoaiSet">{{ errors.idLoaiSet }}</span>
            </div>

            <div class="form-group">
              <label>Giá bán</label>
              <input :disabled="isViewMode" v-model="formData.giaBan" type="number"
                :class="{ 'invalid-border': errors.giaBan }" @input="errors.giaBan = ''">
              <span class="error-message" v-if="errors.giaBan">{{ errors.giaBan }}</span>
              <div class="price-hint">Giá thành phần: {{ totalComponentsPrice.toLocaleString() }} đ</div>
            </div>

            <div class="form-group">
              <label>Hình ảnh</label>
              <div class="upload-container" v-if="!isViewMode" :class="{ 'invalid-border': errors.hinhAnh }">
                <label class="custom-file-upload">
                  <input type="file" accept="image/*" @change="handleFileUpload" />
                  <i class="fas fa-cloud-upload-alt"></i> Chọn ảnh từ máy
                </label>
                <button v-if="formData.hinhAnh" class="btn-clear-img" @click="formData.hinhAnh = ''">Xóa ảnh</button>
              </div>
              <div class="image-preview-box" v-if="formData.hinhAnh">
                <img :src="formData.hinhAnh" alt="Preview" class="preview-img">
              </div>
              <span class="error-message" v-if="errors.hinhAnh">{{ errors.hinhAnh }}</span>
            </div>

            <div class="form-group">
              <label>Định lượng <span class="required" v-if="!isViewMode">*</span></label>
              <input :disabled="isViewMode" v-model="formData.moTaChiTiet" type="text"
                :class="{ 'invalid-border': errors.moTaChiTiet }" @input="errors.moTaChiTiet = ''">
              <span class="error-message" v-if="errors.moTaChiTiet">
                {{ errors.moTaChiTiet }}
              </span>
            </div>

            <div class="form-group">
              <label>Mô tả</label>
              <input :disabled="isViewMode" v-model="formData.moTa" type="text">
            </div>

            <div class="form-group">
              <label>Trạng thái</label>
              <div class="toggle-wrapper" :class="{ 'disabled': isViewMode }"
                @click="!isViewMode && (formData.trangThai = formData.trangThai === 1 ? 0 : 1)">
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
          <button class="btn-add" @click="goToDetailTable" title="Xem danh sách chi tiết">
            Xem bảng
          </button>
          <div class="selected-items-container">
            <div v-for="(item, index) in selectedIngredients" :key="item.id" class="selected-item-row"
              @mouseenter="showTooltip($event, item)" @mouseleave="hideTooltip">
              <img :src="getImg(item.hinhAnh)" class="selected-thumb">
              <div class="selected-info">
                <div class="selected-name clickable-name" @click="goToVariantDetail(item)">{{ item.ten }}</div>
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


  <Teleport to="body">
    <div v-if="hoveredItem" class="fixed-tooltip" :class="tooltipPlacement" :style="tooltipStyle">

      <div class="tooltip-header">
        <strong>{{ hoveredItem.ten }}</strong>
        <span class="tooltip-badge">Thành phần</span>
      </div>

      <div class="tooltip-body">
        <div class="tooltip-row">
          <span>Đơn vị tính:</span> <strong>{{ hoveredItem.donVi }}</strong>
        </div>
        <div class="tooltip-row">
          <span>Giá thành phần:</span>
          <strong class="price-text">{{ hoveredItem.giaBan?.toLocaleString() }} đ</strong>
        </div>
        <div class="tooltip-row">
          <span>Số lượng trong set:</span>
          <strong>{{ hoveredItem.soLuong }}</strong>
        </div>
        <div class="tooltip-row total-row">
          <span>Tạm tính:</span>
          <strong class="price-text-lg">{{ (hoveredItem.giaBan * hoveredItem.soLuong)?.toLocaleString() }} đ</strong>
        </div>
        <div class="tooltip-row" v-if="hoveredItem.moTaChiTiet">
          <span>Mô tả:</span> <span class="desc-text">{{ hoveredItem.moTaChiTiet }}</span>
        </div>
      </div>
    </div>
  </Teleport>
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

.view-mode .toggle-wrapper.disabled {
  opacity: 0.6;
  pointer-events: none;
}

.qty-text {
  font-weight: bold;
  color: #d32f2f;
  font-size: 1.1rem;
}

.selected-price-mini {
  font-size: 0.8rem;
  color: #666;
}

.total-summary {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ddd;
  display: flex;
  justify-content: space-between;
  font-size: 1.1rem;
}

.highlight {
  color: #d32f2f;
  font-weight: bold;
}

.fixed-tooltip {
  position: fixed;
  /* Cố định theo màn hình, thoát khỏi thẻ cha */
  width: 260px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 5px 30px rgba(0, 0, 0, 0.25);
  border: 1px solid #ddd;
  z-index: 99999;
  /* Luôn nằm trên cùng */
  pointer-events: none;
  /* Chuột xuyên qua */
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

/* HIỆN Ở TRÊN (TOP) */
.fixed-tooltip.top {
  transform: translate(-50%, -100%);
  /* Đẩy lên trên */
}

.fixed-tooltip.top::after {
  top: 100%;
  border-color: white transparent transparent transparent;
}

/* HIỆN Ở DƯỚI (BOTTOM) */
.fixed-tooltip.bottom {
  transform: translate(-50%, 0);
}

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
}

/* --- NỘI DUNG TOOLTIP --- */
.tooltip-header {
  background-color: #8B0000;
  color: white;
  padding: 8px 12px;
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
  font-size: 10px;
}

.tooltip-body {
  padding: 10px 12px;
  font-size: 13px;
  color: #333;
}

.tooltip-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  border-bottom: 1px dashed #f0f0f0;
  padding-bottom: 4px;
}

.tooltip-row.total-row {
  border-top: 1px solid #eee;
  border-bottom: none;
  margin-top: 8px;
  padding-top: 8px;
  background-color: #fcfcfc;
}

.price-text {
  color: #d32f2f;
  font-weight: 600;
}

.price-text-lg {
  color: #d32f2f;
  font-weight: bold;
  font-size: 14px;
}

.desc-text {
  max-width: 140px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #666;
  font-style: italic;
}
</style>