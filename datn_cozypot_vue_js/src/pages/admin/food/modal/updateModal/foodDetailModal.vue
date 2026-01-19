<script setup>
import { useFoodDetailUpdate } from '../../../../../services/foodFunction';

const {
    formData,
    listMonAn,
    originalInfo,
    parentName,
    isLoading,
    searchQuery,        // Mới
    sortOption,         // Mới
    filteredMonAnList,  // Mới
    selectParentFood,   // Mới
    handleUpdate,
    goBack,
    handleFileUpload
} = useFoodDetailUpdate();

const getImg = (url) => {
    return (url && (url.startsWith('http') || url.startsWith('data:'))) 
        ? url 
        : 'https://placehold.co/100x100?text=No+Img';
}
</script>

<template>
  <div class="main-content">
    <div class="page-header">
        <div class="header-title">
            <h1>Cập nhật Chi Tiết Món</h1>
        </div>
        <button class="btn-back" @click="goBack">← Quay lại</button>
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
                        {{ originalInfo.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}
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

        <div class="page-content">
            
            <div class="section-left">
                <div class="card">
                    <h3>Thông tin chỉnh sửa</h3>
                    <div class="form-container">
                        
                        <div class="form-group">
                            <label>Mã chi tiết (Read-only)</label>
                            <input v-model="formData.maChiTietMonAn" type="text" disabled style="background: #f0f0f0;">
                        </div>

                        <div class="form-group">
                            <label>Thuộc món ăn <span class="required">*</span></label>
                            <div class="selected-display" :class="{ 'has-data': formData.idMonAnDiKem }">
                                <span v-if="formData.idMonAnDiKem">
                                    {{ parentName }}
                                </span>
                                <span v-else class="placeholder-text">
                                    <i class="fas fa-arrow-right"></i> Chọn món từ danh sách bên phải
                                </span>
                                <i v-if="formData.idMonAnDiKem" class="fas fa-check-circle check-icon"></i>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Tên chi tiết <span class="required">*</span></label>
                            <input v-model="formData.tenChiTietMonAn" type="text">
                        </div>

                        <div class="form-row-2">
                            <div class="form-group">
                                <label>Giá vốn</label>
                                <input v-model="formData.giaVon" type="number">
                            </div>
                            <div class="form-group">
                                <label>Giá bán</label>
                                <input v-model="formData.giaBan" type="number">
                            </div>
                        </div>

                        <div class="form-row-2">
                            <div class="form-group">
                                <label>Kích cỡ</label>
                                <input v-model="formData.kichCo" type="text">
                            </div>
                            <div class="form-group">
                                <label>Đơn vị tính</label>
                                <input v-model="formData.donVi" type="text">
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Trạng thái</label>
                             <div class="toggle-wrapper" @click="formData.trangThai = formData.trangThai === 1 ? 0 : 1">
                                <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }">
                                    <div class="toggle-knob"></div>
                                </div>
                                <span>{{ formData.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}</span>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Hình ảnh</label>
                            <div class="upload-box-wrapper">
                                <div class="upload-container">
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
                            </div>
                        </div>

                    </div>
                </div>

                <div class="action-footer">
                    <button class="btn-large btn-save full-width" @click="handleUpdate">Cập nhật thay đổi</button>
                </div>
            </div>

            <div class="section-right">
                <div class="card full-height-card">
                    <h3>Chọn Món Ăn Gốc</h3>

                    <div class="filter-tools">
                        <input v-model="searchQuery" type="text" class="search-input" placeholder="🔍 Tìm món...">
                        <select v-model="sortOption" class="sort-select">
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
                                    <span class="food-price">{{ item.giaBan?.toLocaleString() }}đ</span>
                                </div>
                            </div>
                            
                            <div class="selection-indicator" v-if="formData.idMonAnDiKem === item.id">
                                <i class="fas fa-check"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");
</style>