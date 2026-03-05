<script setup>
import { ref } from 'vue';
import { useHotpotCategoryAddModal, useHotpotForm } from '../../../../../services/foodFunction';
import FoodDetailAddModal from './FoodDetailAddModal.vue';
import Swal from 'sweetalert2';
import Multiselect from '@vueform/multiselect';
import CategoryHotpotAddModal from '@/pages/admin/category/modal/addModal/CategoryHotpotAddModal.vue';

const {
    formData, selectedIngredients, listLoaiSet, filteredFoodList, searchQuery, errors, totalComponentsPrice, isViewMode,
    addIngredient, removeIngredient, handleFileUpload, handleSave, goBack, fetchInitialData
} = useHotpotForm(false);
const loaiSetMultiselectRef = ref(null);
const currentLoaiSetSearch = ref('');
const isFoodModalOpen = ref(false);

const handleFoodAdded = async () => {
    isFoodModalOpen.value = false;
    if (typeof fetchInitialData === 'function') {
        await fetchInitialData();
    }
    Swal.fire({
        icon: 'success',
        title: 'Đã cập nhật danh sách món!',
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 2000
    });
};

const getImg = (url) => {
    if (url && (url.startsWith('http') || url.startsWith('data:image'))) {
        return url;
    }
    return 'https://placehold.co/100x100?text=No+Img';
}

// ==========================================
const isLoaiSetModalOpen = ref(false);
const prefilledLoaiSetName = ref('');

const handleLoaiSetSearchChange = (query) => {
    currentLoaiSetSearch.value = query;
};

const handleLoaiSetKeydown = async (event) => {
    if (event.key === 'Enter' && currentLoaiSetSearch.value.trim() !== '') {
        const text = currentLoaiSetSearch.value.trim();
        const exists = listLoaiSet.value.some(ls => ls.tenLoaiSet?.toLowerCase() === text.toLowerCase());

        if (!exists) {
            event.preventDefault();
            event.stopPropagation();
            
            // Đóng dropdown hiện tại
            if (loaiSetMultiselectRef.value) {
                loaiSetMultiselectRef.value.close();
                loaiSetMultiselectRef.value.clearSearch();
            }

            // Mở Modal và truyền chữ sang
            prefilledLoaiSetName.value = text;
            isLoaiSetModalOpen.value = true;
        }
    }
};

// Hàm xử lý sau khi Modal lưu thành công
const handleLoaiSetAdded = async (newData) => {
    isLoaiSetModalOpen.value = false;
    
    // Tải lại danh sách
    await fetchInitialData(); 
    
    let idToSelect = null;
    if (newData) {
        idToSelect = typeof newData === 'object' ? newData.id : newData;
    }
    
    // Tự động tìm ID nếu component con không trả về
    if (!idToSelect && prefilledLoaiSetName.value) {
        const newlyAdded = listLoaiSet.value.find(
            (ls) => ls.tenLoaiSet?.toLowerCase() === prefilledLoaiSetName.value.toLowerCase()
        );
        if (newlyAdded) idToSelect = newlyAdded.id;
    }

    // Tự động chọn trong Multiselect
    if (idToSelect) formData.value.idLoaiSet = idToSelect;
    
    prefilledLoaiSetName.value = '';
};

// ==========================================
// THÊM LOGIC TẠO NHANH CHO "MÓN ĂN"
// ==========================================
const prefilledFoodName = ref('');

const handleFoodEnter = (event) => {
    // Nếu bấm Enter, ô tìm kiếm có chữ, và không tìm thấy món nào trong list
    if (event.key === 'Enter' && searchQuery.value.trim() !== '') {
        if (filteredFoodList.value.length === 0) {
            event.preventDefault();
            
            // Mở modal tạo món ăn và truyền chữ đang gõ vào
            prefilledFoodName.value = searchQuery.value.trim();
            isFoodModalOpen.value = true;
            
            // Xóa bộ lọc đi cho sạch sẽ
            searchQuery.value = ''; 
        }
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

// Hàm mở Modal Món ăn bằng nút bấm thủ công
const openFoodModalNormal = () => {
    prefilledFoodName.value = '';
    isFoodModalOpen.value = true;
};
</script>

<template>
    <div class="main-content d-flex flex-column h-100">
        <div class="page-header">
            <div class="header-title">
                <h1>Thêm set lẩu mới</h1>
                <p class="subtitle">Tạo combo/set lẩu bao gồm nhiều món ăn</p>
            </div>
            <button class="btn-back" @click="goBack">← Quay lại</button>
        </div>

        <div class="content-scroll-wrapper" style="flex: 1; overflow-y: auto; padding-bottom: 20px;">
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
                                    @search-change="handleLoaiSetSearchChange"
                                    @keydown="handleLoaiSetKeydown"
                                >
                                    <template v-slot:noresults>
                                        <div style="padding: 5px 10px; color: #8B0000; font-size: 0.9rem;">
                                            Không có sẵn. Nhấn <kbd style="background: #eee; padding: 2px 5px; border-radius: 4px;">Enter ↵</kbd> để tạo nhanh "<b>{{ currentLoaiSetSearch }}</b>"
                                        </div>
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
                                <label>Hình ảnh đại diện</label>
                                <div class="upload-container text-center p-3"
                                    style="border: 2px dashed #ddd; border-radius: 8px;"
                                    :class="{ 'invalid-border': errors.hinhAnh }">
                                    <label class="custom-file-upload btn btn-outline-danger mb-2"
                                        style="cursor: pointer;">
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
                                <span class="error-message" v-if="errors.hinhAnh">{{ errors.hinhAnh }}</span>
                            </div>

                            <div class="form-group">
                                <label>Định mức / Ghi chú <span class="required">*</span></label>
                                <input v-model.trim="formData.moTaChiTiet" type="text"
                                    placeholder="VD: Phù hợp cho nhóm 4-5 người"
                                    :class="{ 'invalid-border': errors.moTaChiTiet }" @input="errors.moTaChiTiet = ''">
                                <span class="error-message" v-if="errors.moTaChiTiet">{{ errors.moTaChiTiet }}</span>
                            </div>

                            <div class="form-group">
                                <label>Mô tả chung</label>
                                <textarea v-model.trim="formData.moTa" rows="3" class="form-control"
                                    placeholder="Mô tả hương vị, thành phần..."></textarea>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="section-right">
                    <div class="card ingredient-selector mb-3">
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
                                        <span class="food-price text-danger">{{ item.giaBan?.toLocaleString() }}
                                            ₫</span>
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
                                Chưa có thành phần nào.<br>Hãy bấm dấu <i class="fas fa-plus text-danger"></i> ở trên để
                                thêm món.
                            </div>

                            <div v-for="(item, index) in selectedIngredients" :key="item.idMonAn"
                                class="selected-item-row"
                                style="display: flex; align-items: center; padding: 10px; background: #f8f9fa; border-radius: 8px; margin-bottom: 10px;">

                                <img :src="getImg(item.hinhAnh)" alt="" class="selected-thumb"
                                    style="width: 50px; height: 50px; border-radius: 5px; object-fit: cover; margin-right: 15px;">

                                <div class="selected-info" style="flex: 1;">
                                    <div class="selected-name fw-bold">{{ item.tenMon }}</div>
                                    <div class="selected-unit text-muted" style="font-size: 0.85rem;">{{ item.dinhLuong
                                        || '---' }}</div>
                                </div>

                                <div class="qty-control d-flex align-items-center me-3">
                                    <label class="me-2 mb-0">SL:</label>
                                    <input type="number" v-model="item.soLuong" min="1"
                                        class="form-control form-control-sm text-center" style="width: 60px;">
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
        </div>

        <div class="page-footer-sticky"
            style="border-top: 1px solid #ddd; background: #fff; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; padding: 15px 20px; display: flex; gap: 15px; width: 100%;">
            <button class="btn-large btn-cancel" @click="goBack" style="flex: 1;">Hủy bỏ</button>
            <button class="btn-large btn-save bg-danger text-white border-0" @click="handleSave" style="flex: 3;">
                <i class="fas fa-save me-1"></i> Lưu Set Lẩu
            </button>
        </div>

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
</style>