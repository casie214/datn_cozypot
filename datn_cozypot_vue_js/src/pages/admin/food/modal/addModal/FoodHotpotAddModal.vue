<script setup>
import { useHotpotForm } from '../../../../../services/foodFunction';

const {
    formData, selectedIngredients, listLoaiSet, filteredFoodList, searchQuery, errors, totalComponentsPrice, isViewMode,
    addIngredient, removeIngredient, handleFileUpload, handleSave, goBack
} = useHotpotForm(false);

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
                <p class="subtitle">Tạo combo/set lẩu bao gồm nhiều món ăn</p>
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
                            <input v-model.trim="formData.tenSetLau" type="text" placeholder="VD: Combo Lẩu Thái"
                                :class="{ 'invalid-border': errors.tenSetLau }" @input="errors.tenSetLau = ''">
                            <span class="error-message" v-if="errors.tenSetLau">{{ errors.tenSetLau }}</span>
                        </div>

                        <div class="form-group">
                            <label>Loại Set <span class="required">*</span></label>
                            <select v-model="formData.idLoaiSet" class="form-control"
                                :class="{ 'invalid-border': errors.idLoaiSet }" @change="errors.idLoaiSet = ''">
                                <option value="">-- Chọn loại lẩu --</option>
                                <option v-for="cat in listLoaiSet" :key="cat.id" :value="cat.id">
                                    {{ cat.tenLoaiSet }}
                                </option>
                            </select>
                            <span class="error-message" v-if="errors.idLoaiSet">{{ errors.idLoaiSet }}</span>
                        </div>

                        <div class="form-row-2">
                            <div class="form-group">
                                <label>Giá bán (VNĐ) <span class="required">*</span></label>
                                <input v-model="formData.giaBan" type="number" placeholder="0"
                                    :class="{ 'invalid-border': errors.giaBan }" @input="errors.giaBan = ''">
                                <span class="error-message" v-if="errors.giaBan">{{ errors.giaBan }}</span>
                            </div>
                            
                            <div class="form-group" style="display: flex; flex-direction: column; justify-content: center;">
                                <label class="text-muted">Tổng giá bán lẻ tham khảo:</label>
                                <div class="price-hint" style="font-size: 1.1rem; color: #28a745; font-weight: bold;">
                                    {{ totalComponentsPrice.toLocaleString() }} ₫
                                </div>
                            </div>
                        </div>

                        <div class="form-group mt-3">
                            <label>Hình ảnh đại diện</label>
                            <div class="upload-container text-center p-3" style="border: 2px dashed #ddd; border-radius: 8px;" :class="{ 'invalid-border': errors.hinhAnh }">
                                <label class="custom-file-upload btn btn-outline-primary mb-2" style="cursor: pointer;">
                                    <input type="file" accept="image/*" @change="handleFileUpload" style="display: none;" />
                                    <i class="fas fa-cloud-upload-alt me-1"></i> Chọn ảnh từ máy
                                </label>
                                <br>
                                <button v-if="formData.hinhAnh" class="btn btn-sm btn-outline-danger" @click="formData.hinhAnh = ''">
                                    <i class="fas fa-trash me-1"></i> Xóa ảnh
                                </button>
                            </div>
                            <div class="image-preview-box mt-2 text-center" v-if="formData.hinhAnh">
                                <img :src="formData.hinhAnh" style="max-height: 200px; border-radius: 8px; border: 1px solid #eee;">
                            </div>
                            <span class="error-message" v-if="errors.hinhAnh">{{ errors.hinhAnh }}</span>
                        </div>

                        <div class="form-group">
                            <label>Định mức / Ghi chú <span class="required">*</span></label>
                            <input v-model.trim="formData.moTaChiTiet" type="text" placeholder="VD: Phù hợp cho nhóm 4-5 người"
                                :class="{ 'invalid-border': errors.moTaChiTiet }" @input="errors.moTaChiTiet = ''">
                            <span class="error-message" v-if="errors.moTaChiTiet">{{ errors.moTaChiTiet }}</span>
                        </div>

                        <div class="form-group">
                            <label>Mô tả chung</label>
                            <textarea v-model.trim="formData.moTa" rows="3" class="form-control" placeholder="Mô tả hương vị, thành phần..."></textarea>
                        </div>

                        <div class="form-group">
                            <label>Trạng thái kinh doanh</label>
                            <div class="toggle-wrapper" @click="formData.trangThai = formData.trangThai === 1 ? 0 : 1">
                                <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }">
                                    <div class="toggle-knob"></div>
                                </div>
                                <span :class="formData.trangThai === 1 ? 'text-success fw-bold' : 'text-danger fw-bold'">
                                    {{ formData.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="section-right">

                <div class="card ingredient-selector mb-3">
                    <h3>Thêm món vào Set lẩu</h3>

                    <div class="filter-tools" style="margin-bottom: 1.4em;">
                        <input v-model="searchQuery" type="text" class="search-input w-100" placeholder="🔍 Tìm tên món ăn...">
                    </div>

                    <div class="scroll-list-container" style="max-height: 300px; overflow-y: auto;">
                        <div v-for="item in filteredFoodList" :key="item.id" class="food-item-card"
                            @click="addIngredient(item)" style="cursor: pointer; display: flex; align-items: center; padding: 10px; border-bottom: 1px solid #eee;">
                            
                            <img :src="getImg(item.hinhAnh)" alt="" class="food-thumb" style="width: 50px; height: 50px; border-radius: 5px; object-fit: cover; margin-right: 15px;">
                            
                            <div class="food-info" style="flex: 1;">
                                <div class="food-name fw-bold">{{ item.tenMon }}</div>
                                <div class="food-meta text-muted" style="font-size: 0.9rem;">
                                    <span class="food-price text-danger">{{ item.giaBan?.toLocaleString() }} ₫</span>
                                    <span class="food-unit ms-1" v-if="item.tenDinhLuong">({{ item.tenDinhLuong }})</span>
                                </div>
                            </div>
                            <button class="btn btn-sm btn-outline-primary rounded-circle"><i class="fas fa-plus"></i></button>
                        </div>

                        <div v-if="filteredFoodList.length === 0" class="text-center p-3 text-muted">
                            Không tìm thấy món ăn nào.
                        </div>
                    </div>
                </div>

                <div class="card selected-list-card" :class="{ 'invalid-border': errors.ingredients }">
                    <h3>Thành phần đã chọn ({{ selectedIngredients.length }})</h3>

                    <div class="selected-items-container" style="max-height: 400px; overflow-y: auto;">
                        <div v-if="selectedIngredients.length === 0" class="text-center p-4 text-muted">
                            <i class="fas fa-box-open fa-2x mb-2 text-black-50"></i><br>
                            Chưa có thành phần nào.<br>Hãy bấm dấu <i class="fas fa-plus text-primary"></i> ở trên để thêm món.
                        </div>

                        <div v-for="(item, index) in selectedIngredients" :key="item.idMonAn" class="selected-item-row"
                             style="display: flex; align-items: center; padding: 10px; background: #f8f9fa; border-radius: 8px; margin-bottom: 10px;">
                            
                            <img :src="getImg(item.hinhAnh)" alt="" class="selected-thumb" style="width: 50px; height: 50px; border-radius: 5px; object-fit: cover; margin-right: 15px;">

                            <div class="selected-info" style="flex: 1;">
                                <div class="selected-name fw-bold">{{ item.tenMon }}</div>
                                <div class="selected-unit text-muted" style="font-size: 0.85rem;">{{ item.dinhLuong || '---' }}</div>
                            </div>

                            <div class="qty-control d-flex align-items-center me-3">
                                <label class="me-2 mb-0">SL:</label>
                                <input type="number" v-model="item.soLuong" min="1" class="form-control form-control-sm text-center" style="width: 60px;">
                            </div>

                            <button class="btn btn-sm btn-outline-danger" @click.stop="removeIngredient(index)">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                    </div>
                    <span class="error-message p-2" v-if="errors.ingredients">{{ errors.ingredients }}</span>
                </div>
            </div>
        </div>

        <div class="page-footer" style="display: flex; gap: 15px;">
            <button class="btn-large btn-cancel" @click="goBack" style="flex: 1;">Hủy bỏ</button>
            <button class="btn-large btn-save bg-danger text-white border-0" @click="handleSave" style="flex: 3;">
                <i class="fas fa-save me-1"></i> Lưu Set Lẩu
            </button>
        </div>
    </div>
</template>

<style scoped>
/* Có thể dùng đường dẫn CSS cũ của bạn nếu nó thiết kế chuẩn Layout */
@import url("/src/assets/foodModalManager.css");

.invalid-border {
    border: 1px solid #dc3545 !important;
}

.error-message {
    color: #dc3545;
    font-size: 0.85em;
    margin-top: 4px;
    display: block;
}

textarea.form-control {
    width: 100%;
    padding: 10px;
    border: 1px solid #ced4da;
    border-radius: 4px;
    resize: vertical;
}

/* Đảm bảo khung scroll đẹp mắt */
.scroll-list-container::-webkit-scrollbar,
.selected-items-container::-webkit-scrollbar {
    width: 6px;
}
.scroll-list-container::-webkit-scrollbar-thumb,
.selected-items-container::-webkit-scrollbar-thumb {
    background: #ccc; 
    border-radius: 4px;
}
</style>