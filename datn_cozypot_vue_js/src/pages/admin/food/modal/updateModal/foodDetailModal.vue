<script setup>
import { useFoodDetailUpdate } from '../../../../../services/foodFunction';
import GlobalDialogue from '../../../../../components/globalDialogue.vue'; 

const {
    formData, listMonAn, originalInfo, parentName, isLoading,
    searchQuery, sortOption, filteredMonAnList, selectParentFood,   
    handleUpdate, goBack, handleFileUpload, goToEdit,
    isViewMode, getPriceRange,
    dialogVisible, dialogConfig, handleDialogConfirm, handleDialogClose
} = useFoodDetailUpdate();

const getImg = (url) => {
    return (url && (url.startsWith('http') || url.startsWith('data:'))) 
        ? url : 'https://placehold.co/100x100?text=No+Img';
}
</script>

<template>
  <div class="main-content">
    <GlobalDialogue 
        :show="dialogVisible" :type="dialogConfig?.type" :variant="dialogConfig?.variant"
        :title="dialogConfig?.title" :message="dialogConfig?.message"
        @close="handleDialogClose" @confirm="handleDialogConfirm"
    />

    <div class="page-header">
        <div class="header-title">
            <h1>{{ isViewMode ? 'Chi tiết món ăn' : 'Cập nhật chi tiết món ăn' }}</h1>
            <p class="subtitle">{{ isViewMode ? 'Xem thông tin chi tiết' : 'Chỉnh sửa thông tin chi tiết món ăn' }}</p>
        </div>
        <div class="header-actions">
            <button class="btn-back" @click="goBack">← Quay lại</button>
        </div>
    </div>

    <div v-if="isLoading" class="loading-state">Đang tải dữ liệu...</div>

    <div v-else class="page-content-wrapper">
        <div class="info-hero-card" v-if="originalInfo">
            <div class="hero-image">
                <img :src="getImg(originalInfo.hinhAnh)" alt="Ảnh chi tiết">
            </div>
            <div class="hero-details">
                <div class="hero-header">
                    <h2 class="hero-title">
                        {{ originalInfo.tenChiTietMonAn }}
                        <span class="code-badge">#{{ originalInfo.maChiTietMonAn }}</span>
                    </h2>
                    <span :class="['status-badge', originalInfo.trangThai === 1 ? 'active' : 'inactive']">
                        {{ originalInfo.trangThaiKinhDoanh === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}
                    </span>
                </div>
                <div class="hero-meta-grid">
                    <div class="meta-item">
                        <span class="label">Thuộc món:</span>
                        <span class="value" style="color: #0d6efd; font-weight: 600;">{{ parentName }}</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-content" :class="{ 'view-mode': isViewMode }">
            
            <div class="section-left">
                <div class="card">
                    <h3>Thông tin chi tiết</h3>
                    <div class="form-container">
                        
                        <div class="form-group">
                            <label>Mã chi tiết</label>
                            <input v-model="formData.maChiTietMonAn" type="text" disabled style="background: #f0f0f0;">
                        </div>

                        <div class="form-group">
                            <label>Thuộc món ăn</label>
                            <div class="selected-display" :class="{ 'has-data': formData.idMonAnDiKem, 'disabled': isViewMode }">
                                <span v-if="formData.idMonAnDiKem">{{ parentName }}</span>
                                <span v-else class="placeholder-text">Chưa chọn món cha</span>
                                <i v-if="formData.idMonAnDiKem" class="fas fa-check-circle check-icon"></i>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Tên chi tiết <span class="required" v-if="!isViewMode">*</span></label>
                            <input v-model="formData.tenChiTietMonAn" type="text" :disabled="isViewMode">
                        </div>

                        <div class="form-row-2">
                            <div class="form-group">
                                <label>Giá vốn</label>
                                <input v-model="formData.giaVon" type="number" :disabled="isViewMode">
                            </div>
                            <div class="form-group">
                                <label>Giá bán <span class="required" v-if="!isViewMode">*</span></label>
                                <input v-model="formData.giaBan" type="number" :disabled="isViewMode">
                            </div>
                        </div>

                        <div class="form-row-2">
                            <div class="form-group">
                                <label>Kích cỡ</label>
                                <input v-model="formData.kichCo" type="text" :disabled="isViewMode">
                            </div>
                            <div class="form-group">
                                <label>Đơn vị tính</label>
                                <input v-model="formData.donVi" type="text" :disabled="isViewMode">
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Trạng thái</label>
                             <div class="toggle-wrapper" :class="{ 'disabled': isViewMode }" 
                                  @click="!isViewMode && (formData.trangThai = formData.trangThai === 1 ? 0 : 1)">
                                <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }">
                                    <div class="toggle-knob"></div>
                                </div>
                                <span>{{ formData.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}</span>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Hình ảnh</label>
                            <div class="upload-box-wrapper">
                                <div class="upload-container" v-if="!isViewMode">
                                    <label class="custom-file-upload">
                                        <input type="file" accept="image/*" @change="handleFileUpload" />
                                        <i class="fas fa-cloud-upload-alt"></i> Thay đổi ảnh
                                    </label>
                                    <button v-if="formData.hinhAnh" class="btn-clear-img" @click="formData.hinhAnh = ''">
                                        <i class="fas fa-trash"></i> Xóa
                                    </button>
                                </div>
                                <div class="large-preview-container" v-if="formData.hinhAnh">
                                    <img :src="formData.hinhAnh" class="large-preview-img">
                                </div>
                                <div v-else-if="isViewMode" class="text-gray italic">Không có hình ảnh</div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>

            <div class="section-right">
                <div class="card full-height-card" :class="{ 'disabled-overlay': isViewMode }">
                    <h3>{{ isViewMode ? 'Món Ăn Gốc' : 'Chọn Món Ăn Gốc' }}</h3>

                    <div class="filter-tools">
                        <input v-model="searchQuery" type="text" class="search-input" placeholder="🔍 Tìm món..." :disabled="isViewMode">
                        <select v-model="sortOption" class="sort-select" :disabled="isViewMode">
                            <option value="name_asc">A-Z</option>
                            <option value="price_asc">Giá tăng</option>
                            <option value="price_desc">Giá giảm</option>
                        </select>
                    </div>

                    <div class="scroll-list-container">
                        <div v-for="item in filteredMonAnList" :key="item.id" class="food-item-card"
                            :class="{ 'active': formData.idMonAnDiKem === item.id }"
                            @click="selectParentFood(item)">
                            
                            <img :src="getImg(item.hinhAnh)" class="food-thumb" />
                            
                            <div class="food-info">
                                <div class="food-name">{{ item.tenMonAn }}</div>
                                <div class="food-meta">
                                    <span class="food-price-range">
                                        {{ getPriceRange(item) }}
                                    </span>
                                </div>
                            </div>
                            
                            <div class="selection-indicator" v-if="formData.idMonAnDiKem === item.id">
                                <i class="fas fa-check"></i>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="action-footer" v-if="!isViewMode" style="display: flex; align-items: end; justify-self: end;">
                    <button class="btn-large btn-save full-width" @click="handleUpdate">Cập nhật thay đổi</button>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");

/* CSS bổ sung để làm mờ cột phải khi ở chế độ xem */
.disabled-overlay {
    opacity: 0.8;
    pointer-events: none; /* Chặn click */
    background-color: #fafafa;
}

.view-mode .toggle-wrapper.disabled {
    cursor: default;
    opacity: 0.7;
}

.text-gray { color: #888; }
.italic { font-style: italic; }

.btn-primary {
    background-color: #007bff;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: 500;
}
.btn-primary:hover { background-color: #0056b3; }

.food-price-range {
    color: #d32f2f;
    font-weight: bold;
    font-size: 0.95em;
    background: #ffebee;
    padding: 2px 6px;
    border-radius: 4px;
}
</style>