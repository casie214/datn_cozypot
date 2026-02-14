<script setup>
import { useRouter } from 'vue-router';
import { useHotpotForm } from '../../../../../services/foodFunction';
import { ref, computed } from 'vue';

const router = useRouter();

// Gọi useHotpotForm(true) để báo hiệu đây là chế độ Edit/View
const {
    formData, selectedIngredients, listLoaiSet, filteredFoodList, searchQuery, errors, totalComponentsPrice, isViewMode,
    addIngredient, removeIngredient, handleFileUpload, handleUpdate, goBack
} = useHotpotForm(true);

const getImg = (url) => {
    if (url && (url.startsWith('http') || url.startsWith('data:image'))) {
        return url;
    }
    return 'https://placehold.co/100x100?text=No+Img';
}

// Lấy tên Loại Set Lẩu để hiển thị trên Banner
const categoryName = computed(() => {
    if (!formData.value.idLoaiSet) return 'Chưa phân loại';
    const cat = listLoaiSet.value.find(c => c.id === formData.value.idLoaiSet);
    return cat ? cat.tenLoaiSet : 'Không xác định';
});

// Chuyển hướng xem chi tiết món ăn con
const goToVariantDetail = (variant) => {
    router.push({
        name: 'viewFood', // Điều hướng đến trang xem chi tiết món ăn (thay đổi name route nếu cần)
        params: { id: variant.idMonAn }
    });
};

// ==========================================
// TOOLTIP CHO MÓN ĂN TRONG SET LẨU
// ==========================================
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
        <div class="page-header">
            <div class="header-title">
                <h1>{{ isViewMode ? 'Chi tiết set lẩu' : 'Cập nhật set lẩu' }}</h1>
                <p class="subtitle">{{ isViewMode ? 'Xem thông tin chi tiết set lẩu' : 'Chỉnh sửa thông tin và thành phần món ăn' }}</p>
            </div>
            <button class="btn-back" @click="goBack">← Quay lại</button>
        </div>

        <div class="info-hero-card" v-if="formData.id" style="margin-bottom: 2em;">
            <div class="hero-image">
                <img :src="getImg(formData.hinhAnh)" alt="Ảnh Set Lẩu">
            </div>

            <div class="hero-details">
                <div class="hero-header">
                    <h2 class="hero-title">
                        {{ formData.tenSetLau }}
                        <span class="code-badge">#{{ formData.maSetLau || 'Chưa có mã' }}</span>
                    </h2>
                    <span :class="['status-badge', formData.trangThai === 1 ? 'active' : 'inactive']">
                        {{ formData.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
                    </span>
                </div>

                <div class="hero-meta-grid">
                    <div class="meta-item">
                        <span class="label">Loại Set:</span>
                        <span class="value">{{ categoryName }}</span>
                    </div>
                    <div class="meta-item">
                        <span class="label">Giá bán:</span>
                        <span class="value price" style="color: #d32f2f; font-weight: bold;">{{ formData.giaBan?.toLocaleString() }} ₫</span>
                    </div>
                    <div class="meta-item">
                        <span class="label">Người tạo:</span>
                        <span class="value">{{ formData.nguoiTao || 'Hệ thống' }}</span>
                    </div>
                    <div class="meta-item">
                        <span class="label">Ngày tạo:</span>
                        <span class="value">{{ formData.ngayTao || '---' }}</span>
                    </div>
                </div>

                <div class="hero-description">
                    <span class="label">Mô tả:</span>
                    <p>{{ formData.moTa || 'Chưa có mô tả cho set lẩu này.' }}</p>
                </div>
            </div>
        </div>

        <div class="page-content" :class="{ 'view-mode': isViewMode }">

            <div class="section-left">
                <div class="card">
                    <h3>Thông tin chung</h3>
                    <div class="form-container">

                        <div class="form-group">
                            <label>Tên Set Lẩu <span class="required" v-if="!isViewMode">*</span></label>
                            <input :disabled="isViewMode" v-model.trim="formData.tenSetLau" type="text"
                                :class="{ 'invalid-border': errors.tenSetLau }" @input="errors.tenSetLau = ''">
                            <span class="error-message" v-if="errors.tenSetLau">{{ errors.tenSetLau }}</span>
                        </div>

                        <div class="form-group">
                            <label>Loại Set <span class="required" v-if="!isViewMode">*</span></label>
                            <select :disabled="isViewMode" v-model="formData.idLoaiSet" class="form-control"
                                :class="{ 'invalid-border': errors.idLoaiSet }" @change="errors.idLoaiSet = ''">
                                <option value="">-- Chọn loại lẩu --</option>
                                <option v-for="cat in listLoaiSet" :key="cat.id" :value="cat.id">{{ cat.tenLoaiSet }}</option>
                            </select>
                            <span class="error-message" v-if="errors.idLoaiSet">{{ errors.idLoaiSet }}</span>
                        </div>

                        <div class="form-row-2">
                            <div class="form-group">
                                <label>Giá bán (VNĐ) <span class="required" v-if="!isViewMode">*</span></label>
                                <input :disabled="isViewMode" v-model="formData.giaBan" type="number"
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
                            <div class="upload-container text-center p-3" v-if="!isViewMode" style="border: 2px dashed #ddd; border-radius: 8px;" :class="{ 'invalid-border': errors.hinhAnh }">
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
                            <div v-else-if="isViewMode" class="text-muted fst-italic mt-2">Không có hình ảnh</div>
                            <span class="error-message" v-if="errors.hinhAnh">{{ errors.hinhAnh }}</span>
                        </div>

                        <div class="form-group">
                            <label>Định mức / Ghi chú <span class="required" v-if="!isViewMode">*</span></label>
                            <input :disabled="isViewMode" v-model.trim="formData.moTaChiTiet" type="text"
                                :class="{ 'invalid-border': errors.moTaChiTiet }" @input="errors.moTaChiTiet = ''">
                            <span class="error-message" v-if="errors.moTaChiTiet">{{ errors.moTaChiTiet }}</span>
                        </div>

                        <div class="form-group">
                            <label>Mô tả chung</label>
                            <textarea :disabled="isViewMode" v-model.trim="formData.moTa" rows="3" class="form-control"></textarea>
                        </div>

                        <div class="form-group">
                            <label>Trạng thái kinh doanh</label>
                            <div class="toggle-wrapper" :class="{ 'disabled': isViewMode }"
                                @click="!isViewMode && (formData.trangThai = formData.trangThai === 1 ? 0 : 1)">
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

                <div class="card ingredient-selector mb-3" v-if="!isViewMode">
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
                            Chưa có thành phần nào.
                        </div>

                        <div v-for="(item, index) in selectedIngredients" :key="item.idMonAn" class="selected-item-row"
                             @mouseenter="showTooltip($event, item)" @mouseleave="hideTooltip"
                             style="display: flex; align-items: center; padding: 10px; background: #f8f9fa; border-radius: 8px; margin-bottom: 10px;">
                            
                            <img :src="getImg(item.hinhAnh)" alt="" class="selected-thumb" style="width: 50px; height: 50px; border-radius: 5px; object-fit: cover; margin-right: 15px;">

                            <div class="selected-info" style="flex: 1;">
                                <div class="selected-name fw-bold text-primary" style="cursor: pointer;" @click="goToVariantDetail(item)">
                                    {{ item.tenMon }}
                                </div>
                                <div class="selected-unit text-muted" style="font-size: 0.85rem;">{{ item.dinhLuong || '---' }}</div>
                                
                                <div class="selected-price-mini mt-1 text-danger" v-if="isViewMode">
                                    {{ item.giaBan?.toLocaleString() }} ₫ <span class="text-dark fw-bold">x {{ item.soLuong }}</span>
                                </div>
                            </div>

                            <div class="qty-control d-flex align-items-center me-3" v-if="!isViewMode">
                                <label class="me-2 mb-0">SL:</label>
                                <input type="number" v-model="item.soLuong" min="1" class="form-control form-control-sm text-center" style="width: 60px;">
                            </div>

                            <button class="btn btn-sm btn-outline-danger" @click.stop="removeIngredient(index)" v-if="!isViewMode">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                    </div>
                    <span class="error-message p-2" v-if="errors.ingredients">{{ errors.ingredients }}</span>

                    <div class="total-summary" v-if="isViewMode">
                        <span>Tổng giá trị tham khảo:</span>
                        <span class="highlight">{{ totalComponentsPrice.toLocaleString() }} ₫</span>
                    </div>
                </div>

            </div>
        </div>

        <div class="page-footer" style="display: flex; gap: 15px;" v-if="!isViewMode">
            <button class="btn-large btn-cancel" @click="goBack" style="flex: 1;">Hủy bỏ</button>
            <button class="btn-large btn-save bg-danger text-white border-0" @click="handleUpdate" style="flex: 3;">
                <i class="fas fa-save me-1"></i> Cập nhật Set Lẩu
            </button>
        </div>

        <div class="page-footer" style="display: flex;" v-else>
            <button class="btn-large btn-cancel w-100" @click="goBack">Quay lại danh sách</button>
        </div>
    </div>

    <Teleport to="body">
        <div v-if="hoveredItem" class="fixed-tooltip" :class="tooltipPlacement" :style="tooltipStyle">
            <div class="tooltip-header">
                <strong>{{ hoveredItem.tenMon }}</strong>
                <span class="tooltip-badge">Thành phần</span>
            </div>
            <div class="tooltip-body">
                <div class="tooltip-row">
                    <span>Định lượng:</span> <strong>{{ hoveredItem.dinhLuong || '---' }}</strong>
                </div>
                <div class="tooltip-row">
                    <span>Giá bán lẻ:</span>
                    <strong class="price-text">{{ hoveredItem.giaBan?.toLocaleString() }} ₫</strong>
                </div>
                <div class="tooltip-row">
                    <span>Số lượng trong set:</span>
                    <strong>{{ hoveredItem.soLuong }}</strong>
                </div>
                <div class="tooltip-row total-row">
                    <span>Tạm tính:</span>
                    <strong class="price-text-lg">{{ (hoveredItem.giaBan * hoveredItem.soLuong)?.toLocaleString() }} ₫</strong>
                </div>
            </div>
        </div>
    </Teleport>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");

.invalid-border { border: 1px solid #dc3545 !important; }
.error-message { color: #dc3545; font-size: 0.85em; margin-top: 4px; display: block; }
textarea.form-control { width: 100%; padding: 10px; border: 1px solid #ced4da; border-radius: 4px; resize: vertical; }

.view-mode .toggle-wrapper.disabled { opacity: 0.6; pointer-events: none; }
.total-summary { margin-top: 15px; padding-top: 15px; border-top: 1px dashed #ddd; display: flex; justify-content: space-between; font-size: 1.1rem; }
.highlight { color: #d32f2f; font-weight: bold; }

/* Scrollbars */
.scroll-list-container::-webkit-scrollbar, .selected-items-container::-webkit-scrollbar { width: 6px; }
.scroll-list-container::-webkit-scrollbar-thumb, .selected-items-container::-webkit-scrollbar-thumb { background: #ccc; border-radius: 4px; }

/* TOOLTIP STYLES */
.fixed-tooltip {
    position: fixed;
    width: 260px;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 5px 30px rgba(0, 0, 0, 0.25);
    border: 1px solid #ddd;
    z-index: 99999;
    pointer-events: none;
    animation: fadeInTooltip 0.15s ease-out;
}
.fixed-tooltip::after { content: ""; position: absolute; left: 50%; transform: translateX(-50%); border-width: 8px; border-style: solid; }
.fixed-tooltip.top { transform: translate(-50%, -100%); }
.fixed-tooltip.top::after { top: 100%; border-color: white transparent transparent transparent; }
.fixed-tooltip.bottom { transform: translate(-50%, 0); }
.fixed-tooltip.bottom::after { bottom: 100%; border-color: transparent transparent white transparent; }

@keyframes fadeInTooltip {
    from { opacity: 0; transform: translate(-50%, -90%) scale(0.95); }
    to { opacity: 1; }
}

.tooltip-header { background-color: #8B0000; color: white; padding: 8px 12px; border-top-left-radius: 8px; border-top-right-radius: 8px; display: flex; justify-content: space-between; align-items: center; font-size: 13px; }
.tooltip-badge { background-color: rgba(255, 255, 255, 0.2); padding: 2px 8px; border-radius: 10px; font-size: 10px; }
.tooltip-body { padding: 10px 12px; font-size: 13px; color: #333; }
.tooltip-row { display: flex; justify-content: space-between; margin-bottom: 6px; border-bottom: 1px dashed #f0f0f0; padding-bottom: 4px; }
.tooltip-row.total-row { border-top: 1px solid #eee; border-bottom: none; margin-top: 8px; padding-top: 8px; background-color: #fcfcfc; }
.price-text { color: #d32f2f; font-weight: 600; }
.price-text-lg { color: #d32f2f; font-weight: bold; font-size: 14px; }
</style>