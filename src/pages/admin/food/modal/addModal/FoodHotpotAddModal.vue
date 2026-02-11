<script setup>
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import { useHotpotAdd } from '../../../../../services/foodFunction';

const {
    formData, listLoaiSet, selectedIngredients, totalComponentsPrice,
    searchQuery, sortOption, filteredFoodList,
    addIngredient, removeIngredient, handleSave, goBack, handleFileUpload,
    errors
} = useHotpotAdd();

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
                <h1>Thêm set lẩu mới</h1>
            </div>
            <button class="btn-back" @click="goBack">← Quay lại</button>
        </div>

        <div class="page-content">
            <div class="section-left">
                <div class="card">
                    <h3>Thông tin chung</h3>
                    <div class="form-container">

                        <div class="form-group">
                            <label>Tên Set Lẩu <span class="required">*</span></label>
                            <input v-model="formData.tenSetLau" type="text" placeholder="VD: Combo Lẩu Thái"
                                :class="{ 'invalid-border': errors.tenSetLau }" @input="errors.tenSetLau = ''">
                            <span class="error-message" v-if="errors.tenSetLau">{{ errors.tenSetLau }}</span>
                        </div>

                        <div class="form-group">
                            <label>Loại Set <span class="required">*</span></label>
                            <select v-model="formData.idLoaiSet" class="form-control"
                                :class="{ 'invalid-border': errors.idLoaiSet }" @change="errors.idLoaiSet = ''">
                                <option value="">-- Chọn loại --</option>
                                <option v-for="cat in listLoaiSet" :key="cat.id" :value="cat.id">{{ cat.tenLoaiSet }}
                                </option>
                            </select>
                            <span class="error-message" v-if="errors.idLoaiSet">{{ errors.idLoaiSet }}</span>
                        </div>

                        <div class="form-group">
                            <label>Giá bán (VNĐ) <span class="required">*</span></label>
                            <input v-model="formData.giaBan" type="number" placeholder="0"
                                :class="{ 'invalid-border': errors.giaBan }" @input="errors.giaBan = ''">
                            <span class="error-message" v-if="errors.giaBan">{{ errors.giaBan }}</span>
                            <div class="price-hint" v-if="totalComponentsPrice > 0">
                                Giá vốn linh kiện: <b>{{ totalComponentsPrice.toLocaleString() }} đ</b>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>Hình ảnh</label>
                            <div class="upload-container" :class="{ 'invalid-border': errors.hinhAnh }">
                                <label class="custom-file-upload">
                                    <input type="file" accept="image/*" @change="handleFileUpload" />
                                    <i class="fas fa-cloud-upload-alt"></i> Chọn ảnh từ máy
                                </label>
                                <button v-if="formData.hinhAnh" class="btn-clear-img" @click="formData.hinhAnh = ''">Xóa
                                    ảnh</button>
                            </div>
                            <div class="image-preview-box" v-if="formData.hinhAnh"><img :src="formData.hinhAnh"
                                    class="preview-img"></div>
                            <div class="image-preview-box empty" v-else><span>Chưa có ảnh</span></div>
                            <span class="error-message" v-if="errors.hinhAnh">{{ errors.hinhAnh }}</span>
                        </div>

                        <div class="form-group">

                            <label>Định mức <span class="required">*</span></label>
                            <input v-model="formData.moTaChiTiet" type="text" placeholder="VD: Phù hợp cho nhóm 4-5 người"
                                :class="{ 'invalid-border': errors.tenSetLau }" @input="errors.tenSetLau = ''">
                            <span class="error-message" v-if="errors.moTaChiTiet">{{ errors.moTaChiTiet }}</span>
                        </div>

                        <div class="form-group">
                            <label>Mô tả</label>
                            <input v-model="formData.moTa" type="text" placeholder="VD: Combo Lẩu Thái">
                        </div>

                        <div class="form-group">
                            <label>Trạng thái</label>
                            <div class="toggle-wrapper" @click="formData.trangThai = formData.trangThai === 1 ? 0 : 1">
                                <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }">
                                    <div class="toggle-knob"></div>
                                </div>
                                <span>{{ formData.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="section-right">

                <div class="card ingredient-selector">
                    <h3>Danh sách món ăn</h3>

                    <div class="filter-tools" style="margin-bottom: 1.4em;">
                        <input v-model="searchQuery" type="text" class="search-input" placeholder="🔍 Tìm tên món...">
                        <select v-model="sortOption" class="sort-select">
                            <option value="name_asc">Tên A-Z</option>
                            <option value="price_asc">Giá tăng dần</option>
                            <option value="price_desc">Giá giảm dần</option>
                        </select>
                    </div>

                    <div class="scroll-list-container">
                        <div v-for="item in filteredFoodList" :key="item.id" class="food-item-card"
                            @click="addIngredient(item)">
                            <img :src="getImg(item.hinhAnh)" alt="" class="food-thumb">
                            <div class="food-info">
                                <div class="food-name">{{ item.tenChiTietMonAn || item.tenDanhMucChiTiet }}</div>
                                <div class="food-meta">
                                    <span class="food-price">{{ item.giaBan?.toLocaleString() }}đ</span>
                                    <span class="food-unit">/ {{ item.kichCo }}</span>
                                </div>
                            </div>
                            <button class="btn-add-mini">+</button>
                        </div>

                        <div v-if="filteredFoodList.length === 0" class="empty-state">
                            Không tìm thấy món nào
                        </div>
                    </div>
                </div>

                <div class="card selected-list-card">
                    <h3>Thành phần đã chọn ({{ selectedIngredients.length }})</h3>

                    <div class="selected-items-container">
                        <div v-if="selectedIngredients.length === 0" class="empty-text">
                            Chưa có thành phần nào. Hãy chọn từ danh sách trên.
                        </div>

                        <div v-for="(item, index) in selectedIngredients" :key="item.id" class="selected-item-row">
                            <img :src="getImg(item.hinhAnh)" alt="" class="selected-thumb">

                            <div class="selected-info">
                                <div class="selected-name">{{ item.ten }}</div>
                                <div class="selected-unit">{{ item.donVi }}</div>
                            </div>

                            <div class="qty-control">
                                <label>SL:</label>
                                <input type="number" v-model="item.soLuong" min="1" class="qty-input-small">
                            </div>

                            <button class="btn-remove-icon" @click="removeIngredient(index)">✕</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-footer">
            <button class="btn-large btn-cancel" @click="goBack">Hủy bỏ</button>
            <button class="btn-large btn-save" @click="handleSave">Lưu Set Lẩu</button>
        </div>
    </div>
</template>

<style scoped src="/src/assets/foodModalManager.css">
/* Import CSS cơ bản của bạn */
@import url("/src/assets/foodModalManager.css");
</style>

<style>
.invalid-border {
    border: 1px solid #dc3545 !important;
}

.error-message {
    color: #dc3545;
    font-size: 0.85em;
    margin-top: 4px;
    display: block;
}
</style>