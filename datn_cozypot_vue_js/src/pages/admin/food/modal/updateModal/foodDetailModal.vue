<script setup>
import { useFoodDetailUpdate } from '../../../../../services/foodFunction';

const {
    formData,
    listMonAn,
    originalInfo,
    parentName,
    isLoading,
    handleUpdate,
    goBack,
    handleFileUpload
} = useFoodDetailUpdate();

const getImg = (url) => {
    if (url && (url.startsWith('http') || url.startsWith('data:image'))) {
        return url;
    }
    return 'https://placehold.co/100x100?text=No+Img';
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
                    <div class="meta-item">
                        <span class="label">Giá bán:</span>
                        <span class="value price">{{ originalInfo.giaBan?.toLocaleString() }} đ</span>
                    </div>
                    <div class="meta-item">
                        <span class="label">Kích cỡ:</span>
                        <span class="value">{{ originalInfo.kichCo || '---' }}</span>
                    </div>
                     <div class="meta-item">
                        <span class="label">Đơn vị:</span>
                        <span class="value">{{ originalInfo.donVi || '---' }}</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-content">
            <div class="section-left">
                <div class="card">
                    <h3>Thông tin cơ bản</h3>
                    <div class="form-container">
                        
                        <div class="form-group">
                            <label>Mã chi tiết (Read-only)</label>
                            <input v-model="formData.maChiTietMonAn" type="text" disabled style="background: #f0f0f0;">
                        </div>

                        <div class="form-group">
                            <label>Tên chi tiết <span class="required">*</span></label>
                            <input v-model="formData.tenChiTietMonAn" type="text">
                        </div>

                        <div class="form-group">
                            <label>Thuộc món ăn <span class="required">*</span></label>
                            <select v-model="formData.idMonAnDiKem" class="form-control">
                                <option value="">-- Chọn món ăn --</option>
                                <option v-for="mon in listMonAn" :key="mon.id" :value="mon.id">
                                    {{ mon.tenMonAn }}
                                </option>
                            </select>
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
                    </div>
                </div>
            </div>

            <div class="section-right">
                <div class="card">
                    <h3>Quy cách & Hình ảnh</h3>
                    <div class="form-container">
                        <div class="form-row-2">
                            <div class="form-group">
                                <label>Kích cỡ</label>
                                <input v-model="formData.kichCo" type="text" placeholder="VD: L, XL">
                            </div>
                            <div class="form-group">
                                <label>Đơn vị tính</label>
                                <input v-model="formData.donVi" type="text" placeholder="VD: Cái, Ly">
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Hình ảnh</label>
                            <div class="upload-container">
                                <label class="custom-file-upload">
                                    <input type="file" accept="image/*" @change="handleFileUpload" />
                                    <i class="fas fa-cloud-upload-alt"></i> Chọn ảnh
                                </label>
                                <button v-if="formData.hinhAnh" class="btn-clear-img" @click="formData.hinhAnh = ''">Xóa</button>
                            </div>
                            <div class="image-preview-box" v-if="formData.hinhAnh">
                                <img :src="formData.hinhAnh" class="preview-img">
                            </div>
                            <div class="image-preview-box empty" v-else>
                                <span>Chưa có ảnh</span>
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

                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="page-footer">
        <button class="btn-large btn-cancel" @click="goBack">Hủy bỏ</button>
        <button class="btn-large btn-save" @click="handleUpdate">Cập nhật</button>
    </div>
  </div>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");
</style>