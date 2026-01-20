<script setup>
import { useFoodDetailAdd } from '../../../../../services/foodFunction';
import GlobalDialogue from '../../../../../components/globalDialogue.vue'; // Import Dialog

const {
    formData,
    listMonAn,
    isParentLocked,
    parentFoodName,
    searchQuery,
    sortOption,
    filteredMonAnList,
    selectParentFood,
    handleSave,
    goBack,
    handleFileUpload,

    // Lấy biến Dialog
    dialogVisible,
    dialogConfig,
    handleDialogConfirm,
    handleDialogClose
} = useFoodDetailAdd();

const getImg = (url) => {
    return (url && (url.startsWith('http') || url.startsWith('data:')))
        ? url
        : 'https://placehold.co/100x100?text=No+Img';
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
                <h1>Thêm Chi Tiết Món Ăn</h1>
            </div>
            <button class="btn-back" @click="goBack">
                <i class="fas fa-arrow-left"></i> Quay lại
            </button>
        </div>

        <div class="page-content">

            <div class="section-left">
                <div class="card">
                    <h3>Thông tin cơ bản</h3>
                    <div class="form-container">

                        <div class="form-group">
                            <label>Thuộc món ăn <span class="required">*</span></label>
                            <div class="selected-display"
                                :class="{ 'locked': isParentLocked, 'has-data': formData.idMonAnDiKem }">
                                <span v-if="formData.idMonAnDiKem">
                                    {{ listMonAn.find(f => f.id === formData.idMonAnDiKem)?.tenMonAn || parentFoodName || 'Đang tải...' }}
                                </span>
                                <span v-else class="placeholder-text">
                                    <i class="fas fa-arrow-right"></i> Chọn món từ danh sách bên phải
                                </span>
                                <i v-if="formData.idMonAnDiKem" class="fas fa-check-circle check-icon"></i>
                            </div>
                            <small v-if="isParentLocked" class="hint-text">
                                *Đang thêm chi tiết cho món: <b>{{ parentFoodName }}</b> (Đã khóa)
                            </small>
                        </div>

                        <div class="form-group">
                            <label>Tên chi tiết <span class="required">*</span></label>
                            <input v-model="formData.tenChiTietMonAn" type="text" placeholder="VD: Size L, Thêm trứng...">
                        </div>

                        <div class="form-row-2">
                            <div class="form-group">
                                <label>Giá vốn (VNĐ)</label>
                                <input v-model="formData.giaVon" type="number" placeholder="0">
                            </div>
                            <div class="form-group">
                                <label>Giá bán (VNĐ)</label>
                                <input v-model="formData.giaBan" type="number" placeholder="0">
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Mô tả chi tiết</label>
                            <textarea v-model="formData.moTaChiTiet" rows="3"></textarea>
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
                            <div class="toggle-wrapper" 
                                 @click="formData.trangThai = formData.trangThai === 1 ? 0 : 1">
                                <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }">
                                    <div class="toggle-knob"></div>
                                </div>
                                <span>{{ formData.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}</span>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Hình ảnh minh họa</label>
                            <div class="upload-box-wrapper">
                                <div class="upload-container">
                                    <label class="custom-file-upload">
                                        <input type="file" accept="image/*" @change="handleFileUpload" />
                                        <i class="fas fa-cloud-upload-alt"></i> Chọn ảnh
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
                            :class="{ 'active': formData.idMonAnDiKem === item.id, 'disabled': isParentLocked && formData.idMonAnDiKem !== item.id }"
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

                        <div v-if="filteredMonAnList.length === 0" class="empty-state">
                            Không tìm thấy món nào
                        </div>
                    </div>
                </div>

                <div class="page-footer">
                    <button class="btn-cancel-large" @click="goBack">Hủy bỏ</button>    
                    <button class="btn-large btn-save full-width" @click="handleSave">Lưu chi tiết</button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");
</style>