<script setup>
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import { useFoodUpdate } from '../../../../../services/foodFunction';

const {
  isViewMode,
  isLoading,
  formData,
  foodInfo,
  variants,
  listDanhMuc,
  filteredSubCategories,
  categoryName,
  handleUpdate,
  goBack,
  goToAddDetail,
  handleToggleDetailStatus,
  
  // Dialog Variables
  dialogVisible,
  dialogConfig,
  handleDialogConfirm,
  handleDialogClose
} = useFoodUpdate();

// Helper hiển thị ảnh
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
        <h1>{{ isViewMode ? 'Chi Tiết Món Ăn' : 'Cập Nhật Món Ăn' }}</h1>
        <p class="subtitle">{{ isViewMode ? 'Xem thông tin và các biến thể' : 'Chỉnh sửa thông tin chung' }}</p>
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
              <span class="value">{{ categoryName }}</span>
            </div>
            <div class="meta-item">
              <span class="label">Số lượng biến thể:</span>
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
                <input :disabled="isViewMode" v-model="formData.tenMonAn" type="text">
              </div>

              <div class="form-row-2">
                <div class="form-group">
                  <label>Danh mục gốc</label>
                  <select :disabled="isViewMode" v-model="formData.idDanhMuc" class="form-control">
                    <option v-for="c in listDanhMuc" :key="c.id" :value="c.id">{{ c.tenDanhMuc }}</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>Chi tiết</label>
                  <select :disabled="isViewMode" v-model="formData.idDanhMucChiTiet" class="form-control">
                    <option v-for="s in filteredSubCategories" :key="s.id" :value="s.id">{{ s.tenDanhMucChiTiet }}
                    </option>
                  </select>
                </div>
              </div>

              <div class="form-group">
                <label>Mô tả</label>
                <textarea :disabled="isViewMode" v-model="formData.moTa" rows="3" class="form-control"></textarea>
              </div>

              <div class="form-group">
                <label>Trạng thái</label>
                <div class="toggle-wrapper" :class="{ 'disabled': isViewMode }"
                  @click="!isViewMode && (formData.trangThaiKinhDoanh = formData.trangThaiKinhDoanh === 1 ? 0 : 1)">
                  
                  <div class="toggle-switch" :class="{ 'on': formData.trangThaiKinhDoanh === 1 }">
                    <div class="toggle-knob"></div>
                  </div>
                  
                  <span>{{ formData.trangThaiKinhDoanh === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}</span>
                </div>
              </div>

            </div>
          </div>
        </div>

        <div class="section-right">
          <div class="card">
            <div class="card-header-row">
              <h3>Các biến thể ({{ variants.length }})</h3>
              <button v-if="!isViewMode" class="btn-add" @click="goToAddDetail">
                + Thêm loại
              </button>
            </div>

            <div class="variants-list">
              <div v-for="v in variants" :key="v.id" class="variant-item">
                <div class="v-thumb">
                  <img :src="getImg(v.hinhAnh)" alt="Ảnh" class="img-fit">
                </div>

                <div class="v-info">
                  <div class="v-name">{{ v.tenChiTietMonAn }}</div>
                  <div class="v-meta">
                    <span class="badge-size">{{ v.kichCo }}</span>
                    <span class="text-unit">{{ v.donVi }}</span>
                  </div>
                </div>
                <div class="v-price">
                  {{ v.giaBan?.toLocaleString() }} đ
                </div>

                <div class="action-col">
                  <div v-if="!isViewMode" class="toggle-switch small" :class="{ 'on': v.trangThai === 1 }"
                    @click.stop="handleToggleDetailStatus(v)" title="Bật/Tắt kinh doanh biến thể này">
                    <div class="toggle-knob"></div>
                  </div>

                  <span v-else class="status-dot" :class="v.trangThai === 1 ? 'green' : 'red'">
                    ●
                  </span>
                </div>
              </div>

              <div v-if="variants.length === 0" class="empty-text">Chưa có biến thể nào.</div>
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
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");

@media (max-width: 900px) {
  .page-content {
    grid-template-columns: 1fr;
  }
}
</style>