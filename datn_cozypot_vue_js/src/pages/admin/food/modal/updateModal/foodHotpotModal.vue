<script setup>
import { useRouter } from 'vue-router';
import { useHotpotForm } from '../../../../../services/foodFunction';
import { ref, computed } from 'vue';

import FoodDetailAddModal from '../addModal/FoodDetailAddModal.vue';
// 1. IMPORT MODAL LOẠI SET LẨU TỪ ADDMODAL
import CategoryHotpotAddModal from '@/pages/admin/category/modal/addModal/CategoryHotpotAddModal.vue';
import Swal from 'sweetalert2';
import Multiselect from '@vueform/multiselect';

const router = useRouter();

const {
    formData, selectedIngredients, listLoaiSet, filteredFoodList, searchQuery, errors, totalComponentsPrice, isViewMode,
    addIngredient, removeIngredient, handleFileUpload, handleUpdate, goBack,
    fetchInitialData 
} = useHotpotForm(true);

const isFoodModalOpen = ref(false);
// ==========================================
// 2. BIẾN VÀ LOGIC TẠO NHANH CHO "MÓN ĂN"
// ==========================================
const prefilledFoodName = ref('');

const handleFoodEnter = (event) => {
    // Chỉ cho phép nếu KHÔNG phải chế độ xem
    if (isViewMode.value) return;

    if (event.key === 'Enter' && searchQuery.value.trim() !== '') {
        if (filteredFoodList.value.length === 0) {
            event.preventDefault();
            prefilledFoodName.value = searchQuery.value.trim();
            isFoodModalOpen.value = true;
            searchQuery.value = ''; 
        }
    }
};

const validateForm = () => {
    errors.value = {}; 
    let isValid = true;

    const name = (formData.value.tenSetLau || '').trim();
    const norm = (formData.value.moTaChiTiet || '').trim(); // Định mức / Ghi chú
    const desc = (formData.value.moTa || '').trim();       // Mô tả chung

    // 1. Validate Tên Set Lẩu (5 - 100 ký tự)
    if (!name) {
        errors.value.tenSetLau = 'Tên set lẩu không được để trống';
        isValid = false;
    } else if (name.length < 5) {
        errors.value.tenSetLau = 'Tên set lẩu phải chứa ít nhất 5 kí tự';
        isValid = false;
    } else if (name.length > 100) {
        errors.value.tenSetLau = 'Tên set lẩu không được vượt quá 100 ký tự';
        isValid = false;
    }

    // 2. Validate Loại Set
    if (!formData.value.idLoaiSet) {
        errors.value.idLoaiSet = 'Vui lòng chọn loại set lẩu';
        isValid = false;
    }

    // 3. Validate Giá bán (> 0)
    if (!formData.value.giaBan || formData.value.giaBan <= 0) {
        errors.value.giaBan = 'Giá bán phải lớn hơn 0';
        isValid = false;
    }

    // 4. Validate Định mức / Ghi chú (5 - 100 ký tự)
    if (!norm) {
        errors.value.moTaChiTiet = 'Vui lòng nhập định mức (VD: Phù hợp cho 4-5 người)';
        isValid = false;
    } else if (norm.length < 5) {
        errors.value.moTaChiTiet = 'Định mức phải chứa ít nhất 5 kí tự';
        isValid = false;
    } else if (norm.length > 100) {
        errors.value.moTaChiTiet = 'Định mức không được vượt quá 100 ký tự';
        isValid = false;
    }

    // 5. Validate Mô tả chung (Tối đa 255 ký tự)
    if (desc.length > 255) {
        errors.value.moTa = 'Mô tả không được vượt quá 255 ký tự';
        isValid = false;
    }

    // 6. Validate Hình ảnh
    if (!formData.value.hinhAnh) {
        errors.value.hinhAnh = 'Vui lòng chọn ảnh đại diện cho set lẩu';
        isValid = false;
    }

    // 7. Validate Thành phần (Phải có ít nhất 1 món)
    if (selectedIngredients.value.length === 0) {
        errors.value.ingredients = 'Vui lòng chọn ít nhất 1 món ăn vào set lẩu';
        isValid = false;
    }

    // 🔥 HIỂN THỊ TOAST NẾU CÓ LỖI (Giống màn Check-in)
    if (!isValid) {
        Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'error',
            title: 'Dữ liệu không hợp lệ',
            text: 'Vui lòng kiểm tra các trường báo đỏ',
            showConfirmButton: false,
            timer: 3000
        });
    } else {
        // Gán lại dữ liệu đã trim trước khi gửi đi
        formData.value.tenSetLau = name;
        formData.value.moTaChiTiet = norm;
        formData.value.moTa = desc;
    }

    return isValid;
};

// Hàm này sẽ được gọi khi bấm nút "Cập nhật"
const onSubmitUpdate = async () => {
    if (validateForm()) {
        await handleUpdate(); // Gọi hàm gốc từ Hook
    }
};

const openFoodModalNormal = () => {
    if (isViewMode.value) return;
    prefilledFoodName.value = '';
    isFoodModalOpen.value = true;
};

const handleFoodAdded = async () => {
    isFoodModalOpen.value = false;
    if (typeof fetchInitialData === 'function') {
        await fetchInitialData();
    }
    Swal.fire({
        icon: 'success',
        iconColor: '#7D161A',
        title: 'Đã cập nhật danh sách món!',
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 2000
    });
};

// ==========================================
// 3. BIẾN VÀ LOGIC TẠO NHANH CHO "LOẠI SET LẨU"
// ==========================================
const isLoaiSetModalOpen = ref(false);
const prefilledLoaiSetName = ref('');
const loaiSetMultiselectRef = ref(null); 
const currentLoaiSetSearch = ref('');

const handleLoaiSetSearchChange = (query) => {
    currentLoaiSetSearch.value = query;
};

const handleLoaiSetKeydown = async (event) => {
    if (isViewMode.value) return;

    if (event.key === 'Enter' && currentLoaiSetSearch.value.trim() !== '') {
        const text = currentLoaiSetSearch.value.trim();
        const exists = listLoaiSet.value.some(ls => ls.tenLoaiSet?.toLowerCase() === text.toLowerCase());

        if (!exists) {
            event.preventDefault();
            event.stopPropagation();
            
            if (loaiSetMultiselectRef.value) {
                loaiSetMultiselectRef.value.close();
                loaiSetMultiselectRef.value.clearSearch();
            }

            prefilledLoaiSetName.value = text;
            isLoaiSetModalOpen.value = true;
        }
    }
};

const handleLoaiSetAdded = async (newData) => {
    isLoaiSetModalOpen.value = false;
    await fetchInitialData(); 
    
    let idToSelect = null;
    if (newData) {
        idToSelect = typeof newData === 'object' ? newData.id : newData;
    }
    
    if (!idToSelect && prefilledLoaiSetName.value) {
        const newlyAdded = listLoaiSet.value.find(
            (ls) => ls.tenLoaiSet?.toLowerCase() === prefilledLoaiSetName.value.toLowerCase()
        );
        if (newlyAdded) idToSelect = newlyAdded.id;
    }

    if (idToSelect) formData.value.idLoaiSet = idToSelect;
    prefilledLoaiSetName.value = '';
};
// ==========================================

const getImg = (url) => {
    if (url && (url.startsWith('http') || url.startsWith('data:image'))) {
        return url;
    }
    return 'https://placehold.co/100x100?text=No+Img';
}

const categoryName = computed(() => {
    if (!formData.value.idLoaiSet) return 'Chưa phân loại';
    const cat = listLoaiSet.value.find(c => c.id === formData.value.idLoaiSet);
    return cat ? cat.tenLoaiSet : 'Không xác định';
});

const goToVariantDetail = (variant) => {
    router.push({
        name: 'viewFood',
        params: { id: variant.idMonAn }
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

const formatDisplayPrice = (value) => {
    if (!value && value !== 0) return '';
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

const handlePriceInput = (event) => {
    // Xóa mọi ký tự không phải số
    const rawString = event.target.value.replace(/[^0-9]/g, '');
    
    // Ép kiểu về số nguyên và lưu trực tiếp vào formData (để gửi API)
    formData.value.giaBan = rawString ? parseInt(rawString, 10) : 0;
    
    // Cập nhật lại giao diện bằng chuỗi có dấu phẩy
    event.target.value = formatDisplayPrice(formData.value.giaBan);
    
    // Xóa lỗi nếu đang có lỗi
    errors.value.giaBan = '';
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
                        <span class="value price" style="color: #d32f2f; font-weight: bold;">{{
                            formData.giaBan?.toLocaleString() }} ₫</span>
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
                            <Multiselect 
                                ref="loaiSetMultiselectRef"
                                v-model="formData.idLoaiSet" 
                                :options="listLoaiSet"
                                mode="single"
                                valueProp="id" 
                                label="tenLoaiSet"
                                :searchable="true"
                                :class="{ 'invalid-border': errors.idLoaiSet }"
                                class="custom-multiselect-theme"
                                @change="errors.idLoaiSet = ''"
                                placeholder="-- Chọn loại lẩu --" 
                                :disabled="isViewMode"
                                @search-change="handleLoaiSetSearchChange"
                                @keydown="handleLoaiSetKeydown"
                            >
                                <template v-slot:noresults>
                                    <div style="padding: 5px 10px; color: #8B0000; font-size: 0.9rem;" v-if="!isViewMode">
                                        Không có sẵn. Nhấn <kbd style="background: #eee; padding: 2px 5px; border-radius: 4px;">Enter ↵</kbd> để tạo nhanh "<b>{{ currentLoaiSetSearch }}</b>"
                                    </div>
                                    <div v-else>Không tìm thấy loại lẩu nào</div>
                                </template>
                            </Multiselect>
                            <span class="error-message" v-if="errors.idLoaiSet">{{ errors.idLoaiSet }}</span>
                        </div>

                        <div class="form-row-2">
                            <div class="form-group">
                                    <label>Giá bán (VNĐ) <span class="required">*</span></label>
                                    <div class="input-group flex-nowrap">
                                        <input 
                                            :value="formatDisplayPrice(formData.giaBan)"
                                            @input="handlePriceInput"
                                            type="text" 
                                            class="form-control text-end fw-bold" 
                                            placeholder="0"
                                            style="padding-right: 12px;"
                                            :class="{ 'invalid-border': errors.giaBan }" 
                                        >
                                        <span class="input-group-text bg-light text-secondary fw-bold border-start-0" 
                                            :class="{ 'invalid-border': errors.giaBan }">
                                            đ
                                        </span>
                                    </div>
                                <span class="error-message" v-if="errors.giaBan">{{ errors.giaBan }}</span>
                            </div>

                            <div class="form-group"
                                style="display: flex; flex-direction: column; justify-content: center;">
                                <label class="text-muted">Tổng giá bán lẻ tham khảo:</label>
                                <div class="price-hint" style="font-size: 1.1rem; font-weight: bold;">
                                    {{ totalComponentsPrice.toLocaleString() }} ₫
                                </div>
                            </div>
                        </div>

                        <div class="form-group mt-3">
                            <label>Hình ảnh đại diện <span class="required" v-if="!isViewMode">*</span></label>
                            <div class="upload-container text-center p-3" v-if="!isViewMode"
                                style="border: 2px dashed #ddd; border-radius: 8px;"
                                :class="{ 'invalid-border': errors.hinhAnh }">
                                <label class="custom-file-upload btn btn-outline-danger mb-2" style="cursor: pointer;">
                                    <input type="file" accept="image/*" @change="handleFileUpload"
                                        style="display: none;" />
                                    <i class="fas fa-cloud-upload-alt me-1"></i> Chọn ảnh từ máy
                                </label>
                                <br>
                                <button v-if="formData.hinhAnh" class="btn btn-sm btn-outline-danger"
                                    @click="formData.hinhAnh = ''">
                                    <i class="fas fa-trash me-1"></i> Xóa ảnh
                                </button>
                            </div>
                            <div class="image-preview-box mt-2 text-center" v-if="formData.hinhAnh">
                                <img :src="formData.hinhAnh"
                                    style="max-height: 200px; border-radius: 8px; border: 1px solid #eee;">
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
                            <textarea :disabled="isViewMode" v-model.trim="formData.moTa" rows="3"
                                class="form-control" :class="{ 'invalid-border': errors.moTa }"></textarea>
                            <small class="error-message" v-if="errors.moTa">{{ errors.moTa }}</small>
                        </div>
                    </div>
                </div>
            </div>

            <div class="section-right">
                <div class="card ingredient-selector mb-3" v-if="!isViewMode">
                    <h3>Thêm món vào Set lẩu</h3>

                    <button class="btn btn-sm btn-outline-danger fw-bold mb-2" @click="openFoodModalNormal">
                        <i class="fas fa-plus me-1"></i> Tạo món mới
                    </button>

                    <div class="filter-tools" style="margin-bottom: 1.4em;">
                        <i class="fas fa-search me-1" style="align-self: center;"></i>
                        <input v-model="searchQuery" type="text" class="search-input w-100"
                            placeholder="Tìm tên món ăn..." @keydown="handleFoodEnter">
                    </div>

                    <div class="scroll-list-container" style="max-height: 300px; overflow-y: auto;">
                        <div v-if="filteredFoodList.length === 0" class="text-center p-3 text-muted">
                            <span v-if="searchQuery">
                                Không tìm thấy món "<b>{{ searchQuery }}</b>".<br>
                                <div class="mt-2">Nhấn <kbd style="background: #eee; padding: 2px 5px; border-radius: 4px; color: #8B0000;">Enter ↵</kbd> để tạo món này.</div>
                            </span>
                            <span v-else>Không tìm thấy món ăn nào.</span>
                        </div>

                        <div v-for="item in filteredFoodList" :key="item.id" class="food-item-card"
                            @click="addIngredient(item)"
                            style="cursor: pointer; display: flex; align-items: center; padding: 10px; border-bottom: 1px solid #eee;">

                            <img :src="getImg(item.hinhAnh)" alt="" class="food-thumb"
                                style="width: 50px; height: 50px; border-radius: 5px; object-fit: cover; margin-right: 15px;">

                            <div class="food-info" style="flex: 1;">
                                <div class="food-name fw-bold">{{ item.tenMon }}</div>
                                <div class="food-meta text-muted" style="font-size: 0.9rem;">
                                    <span class="food-price text-danger">{{ item.giaBan?.toLocaleString() }} ₫</span>
                                    <span class="food-unit ms-1" v-if="item.tenDinhLuong">({{ item.tenDinhLuong
                                        }})</span>
                                </div>
                            </div>
                            <button class="btn btn-sm btn-outline-danger rounded-circle"><i
                                    class="fas fa-plus"></i></button>
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

                            <img :src="getImg(item.hinhAnh)" alt="" class="selected-thumb"
                                style="width: 50px; height: 50px; border-radius: 5px; object-fit: cover; margin-right: 15px;">

                            <div class="selected-info" style="flex: 1;">
                                <div class="selected-name fw-bold" style="cursor: pointer;"
                                    @click="goToVariantDetail(item)">
                                    {{ item.tenMon }}
                                </div>
                                <div class="selected-unit text-muted" style="font-size: 0.85rem;">{{ item.dinhLuong ||
                                    '---' }}</div>

                                <div class="selected-price-mini mt-1 text-danger" v-if="isViewMode">
                                    {{ item.giaBan?.toLocaleString() }} ₫ <span class="text-dark fw-bold">x {{
                                        item.soLuong }}</span>
                                </div>
                            </div>

                            <div class="qty-control d-flex align-items-center me-3" v-if="!isViewMode">
                                <label class="me-2 mb-0">SL:</label>
                                <input type="number" v-model="item.soLuong" min="1"
                                    class="form-control form-control-sm text-center" style="width: 60px;">
                            </div>

                            <button class="btn btn-sm btn-outline-danger" @click.stop="removeIngredient(index)"
                                v-if="!isViewMode">
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
            <button class="btn-large btn-save bg-danger text-white border-0" @click="onSubmitUpdate" style="flex: 3;">
                <i class="fas fa-save me-1"></i> Cập nhật Set Lẩu
            </button>
        </div>

        <div class="page-footer" style="display: flex;" v-else>
            <button class="btn-large btn-cancel w-100" @click="goBack">Quay lại danh sách</button>
        </div>
    </div>

    <div v-if="isFoodModalOpen" class="fullscreen-modal-overlay">
        <div class="fullscreen-modal-container">
            <div class="modal-header-custom">
                <h4><i class="fas fa-magic me-2"></i> Tạo nhiều món ăn cùng lúc</h4>
                <button class="btn-close-modal" @click="isFoodModalOpen = false">&times;</button>
            </div>

            <div class="modal-body-custom">
                <FoodDetailAddModal :is-modal="true" @saved="handleFoodAdded" @cancel="isFoodModalOpen = false" />
            </div>
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
                    <strong class="price-text-lg">{{ (hoveredItem.giaBan * hoveredItem.soLuong)?.toLocaleString() }}
                        ₫</strong>
                </div>
            </div>
        </div>
    </Teleport>

    <div v-if="isFoodModalOpen" class="fullscreen-modal-overlay">
        <div class="fullscreen-modal-container">
            <div class="modal-header-custom">
                <h4><i class="fas fa-magic me-2"></i> Tạo nhiều món ăn cùng lúc</h4>
                <button class="btn-close-modal" @click="isFoodModalOpen = false">&times;</button>
            </div>

            <div class="modal-body-custom">
                <FoodDetailAddModal 
                    :is-modal="true" 
                    :initial-name="prefilledFoodName"
                    @saved="handleFoodAdded" 
                    @cancel="isFoodModalOpen = false" 
                />
            </div>
        </div>
    </div>

    <CategoryHotpotAddModal 
        :isOpen="isLoaiSetModalOpen" 
        :initialName="prefilledLoaiSetName"
        @close="isLoaiSetModalOpen = false" 
        @refresh="handleLoaiSetAdded" 
    />
</template>

<style scoped>
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

.view-mode .toggle-wrapper.disabled {
    opacity: 0.6;
    pointer-events: none;
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

/* Scrollbars */
.scroll-list-container::-webkit-scrollbar,
.selected-items-container::-webkit-scrollbar {
    width: 6px;
}

.scroll-list-container::-webkit-scrollbar-thumb,
.selected-items-container::-webkit-scrollbar-thumb {
    background: #ccc;
    border-radius: 4px;
}

/* 6. THÊM CSS CỦA MODAL THÊM MÓN */
.fullscreen-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.75);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 10000;
    backdrop-filter: blur(4px);
}

.fullscreen-modal-container {
    background: #f8f9fa;
    width: 95%;
    max-width: 1400px;
    height: 92vh;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4);
    animation: slideUp 0.3s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

.modal-header-custom {
    padding: 18px 25px;
    background: #8B0000;
    color: white;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.modal-header-custom h4 {
    margin: 0;
    font-size: 1.25rem;
    font-weight: 600;
}

.btn-close-modal {
    background: none;
    border: none;
    color: white;
    font-size: 2.2rem;
    line-height: 1;
    cursor: pointer;
    transition: 0.2s;
}

.btn-close-modal:hover {
    color: #ffcccc;
    transform: scale(1.1);
}

.modal-body-custom {
    flex: 1;
    overflow-y: auto;
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(40px) scale(0.98);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

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

.fixed-tooltip::after {
    content: "";
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    border-width: 8px;
    border-style: solid;
}

.fixed-tooltip.top {
    transform: translate(-50%, -100%);
}

.fixed-tooltip.top::after {
    top: 100%;
    border-color: white transparent transparent transparent;
}

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
</style>

<style>
div.swal2-container {
    z-index: 99999 !important;
}

:deep(.multiselect) {
    min-height: 40px;
}
:deep(.is-disabled .multiselect-single-label) {
    color: #666;
}

:deep(.multiselect-tag) {
    background: #8B0000 !important;
}

:deep(.multiselect.is-active) {
  border-color: #8B0000 !important;
  box-shadow: 0 0 0 4px rgba(139, 0, 0, 0.2) !important;
  outline: none !important;
}

.custom-multiselect-theme.is-active {
  border-color: #8B0000 !important;
  box-shadow: 0 0 0 4px rgba(139, 0, 0, 0.2) !important;
  outline: none !important;
}

:deep(.multiselect-option.is-selected) {
    background: #8B0000 !important;
    color: #ffffff !important;
}

/* Hiệu ứng khi di chuột vào option đang được chọn */
:deep(.multiselect-option.is-selected.is-pointed) {
    background: #a00000 !important;
}

div.swal2-container {
    z-index: 99999 !important;
}

/* Định nghĩa lại TOÀN BỘ biến màu của Multiselect thành màu đỏ rượu */
.custom-multiselect-theme {
    --ms-border-color: #ccc;
    --ms-border-color-active: #8B0000;
    --ms-radius: 8px;
    --ms-ring-color: rgba(139, 0, 0, 0.15); /* Màu viền phát sáng khi focus */
    --ms-placeholder-color: #999;
    
    /* Màu khi trỏ chuột vào một lựa chọn */
    --ms-option-bg-pointed: #fdf2f2;
    --ms-option-color-pointed: #8B0000;
    
    /* MÀU KHI MỘT LỰA CHỌN ĐÃ ĐƯỢC CHỌN (Xóa sổ màu xanh lá ở đây) */
    --ms-option-bg-selected: #8B0000;
    --ms-option-color-selected: #ffffff;
    
    /* Màu khi trỏ chuột vào lựa chọn đã được chọn */
    --ms-option-bg-selected-pointed: #600000; 
    
    /* Màu của dấu X để xóa và nút mũi tên */
    --ms-clear-color: #8B0000;
    --ms-clear-color-hover: #600000;
    --ms-caret-color: #8B0000;
    
    /* Màu nền của nút Tag (nếu dùng chế độ tags) */
    --ms-tag-bg: #8B0000;
    --ms-tag-color: #ffffff;

    min-height: 40px;
}

/* Ghi đè cứng bằng :global nếu các biến trên bị chặn */
.custom-multiselect-theme :global(.multiselect-option.is-selected) {
    background-color: #8B0000 !important;
    color: white !important;
}

.custom-multiselect-theme :global(.multiselect-option.is-selected.is-pointed) {
    background-color: #600000 !important;
}

/* Ép viền đỏ khi click vào ô select */
.custom-multiselect-theme :global(.is-active) {
    box-shadow: 0 0 0 3px rgba(139, 0, 0, 0.15) !important;
    border-color: #8B0000 !important;
}
</style>