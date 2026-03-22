<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import { foodApi } from '../../../../../services/foodFunction';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';

const route = useRoute();
const router = useRouter();

// ==========================================
// 1. TRẠNG THÁI GIAO DIỆN
// ==========================================
const isViewMode = computed(() => route.name === 'viewFood' || route.query.mode === 'view');
const isLoading = ref(true);
const errors = ref({});

// ==========================================
// 2. DỮ LIỆU FORM & API
// ==========================================
const originalInfo = ref(null);
const listCategories = ref([]);
const listDinhLuongDB = ref([]); // Chứa danh sách định lượng gốc từ DB

const formData = ref({
    id: null,
    maMon: '',
    tenMon: '',
    giaVon: 0,
    giaBan: 0,
    moTa: '',
    idDanhMuc: '',
    trangThai: 1,
    hinhAnh: ''
});

// Biến riêng cho Combobox
const selectedUnit = ref('');   // Chọn 1 đơn vị
const selectedWeight = ref(''); // Chọn 1 khối lượng
const unitOptions = ref([]);    // Option cho combobox Đơn vị
const weightOptions = ref([]);  // Option cho combobox Khối lượng

// ==========================================
// 3. LẤY DỮ LIỆU BAN ĐẦU
// ==========================================

const existingFoods = ref([]);

onMounted(async () => {
    const id = route.params.id;
    if (!id) return goBack();

    try {
        isLoading.value = true;

        // Gọi API lấy Chi tiết món và Danh sách Danh mục
        const [resFood, resCat, resFoods] = await Promise.all([
            foodApi.getFoodById(id),
            foodApi.getCategories(),
            foodApi.getFoods()
        ]);

        listCategories.value = resCat.data || [];
        existingFoods.value = resFoods.data?.content || resFoods.data || []; // Gán data vào biến
        const data = resFood.data;
        originalInfo.value = { ...data };

        // Đổ dữ liệu vào Form
        formData.value = {
            id: data.id,
            maMon: data.maMon || '',
            tenMon: data.tenMon || '',
            giaVon: data.giaVon || 0,
            
            // 🚨 ĐÃ SỬA: Lấy giaGoc làm chuẩn để hiển thị trong form cập nhật
            // Vì nhân viên chỉnh sửa giá là chỉnh sửa giá gốc, không phải giá đang khuyến mãi
            giaBan: data.giaGoc || data.giaBan || 0, 
            
            moTa: data.moTa || '',
            idDanhMuc: data.idDanhMuc || '',
            trangThai: data.trangThai !== undefined ? data.trangThai : 1,
            hinhAnh: data.hinhAnh || ''
        };

        // GÁN GIÁ TRỊ CHO COMBOBOX (CHÚ Ý SỬA TÊN TRƯỜNG Ở ĐÂY NẾU BACKEND TRẢ VỀ KHÁC)
        // Mình đang dự phòng các tên trường phổ biến: tenDonVi/kichCo và giaTri/giaTriDinhLuong
        selectedUnit.value = data.tenDonVi || data.kichCo || '';
        selectedWeight.value = data.giaTri || data.giaTriDinhLuong || '';

        // Tải danh sách Kích cỡ / Đơn vị (isResetSelection = false để không xóa mất giá trị vừa gán)
        if (formData.value.idDanhMuc) {
            await loadDataByCategory(formData.value.idDanhMuc, false);
        }

    } catch (error) {
        console.error("Lỗi tải dữ liệu:", error);
        Swal.fire('Lỗi', 'Không thể tải dữ liệu món ăn', 'error');
    } finally {
        isLoading.value = false;
    }
});

// ==========================================
// 4. LOGIC COMBOBOX (LẤY TỪ BẢNG ĐỊNH LƯỢNG)
// ==========================================
const loadDataByCategory = async (idDanhMuc, isResetSelection = true) => {
    errors.value.idDanhMuc = '';

    // Nếu user chủ động đổi danh mục khác, reset trắng 2 ô combobox
    if (isResetSelection) {
        selectedUnit.value = '';
        selectedWeight.value = '';
    }

    unitOptions.value = [];
    weightOptions.value = [];
    listDinhLuongDB.value = []; // Chứa dữ liệu phẳng để tra cứu ID

    if (!idDanhMuc) return;

    try {
        // Đảm bảo tên hàm gọi API đúng với cấu hình của bạn (ví dụ: getUnitTypesByCategory)
        const res = await foodApi.getUnitTypesByCategory(idDanhMuc);
        const rawUnits = res.data || [];

        const units = new Set();
        const weights = new Set();

        // Xử lý dữ liệu Cha - Con từ API
        rawUnits.forEach(unit => {
            if (unit.values && unit.values.length > 0) {
                unit.values.forEach(val => {
                    // 1. Ép phẳng để dễ lấy ID (rất quan trọng cho hàm handleUpdate)
                    listDinhLuongDB.value.push({
                        id: val.id,             // ID định lượng con
                        kichCo: unit.tenDonVi,  // VD: ml, gram, lon...
                        dinhLuong: val.giaTri   // VD: 330, 500, 1.5...
                    });

                    // 2. Gom các tùy chọn không trùng lặp cho Combobox
                    if (unit.tenDonVi) units.add(unit.tenDonVi.trim());
                    if (val.giaTri) weights.add(val.giaTri.trim());
                });
            }
        });

        unitOptions.value = Array.from(units);
        weightOptions.value = Array.from(weights);

    } catch (error) {
        console.error("Lỗi lấy định lượng:", error);
    }
};

const categoryName = computed(() => {
    if (!formData.value.idDanhMuc) return 'Chưa phân loại';
    const cat = listCategories.value.find(c => c.id === formData.value.idDanhMuc);
    return cat ? cat.tenDanhMuc : 'Không xác định';
});

const formatDisplayPrice = (value) => {
    if (!value && value !== 0) return '';
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

// Hàm 2: Xử lý khi người dùng nhập liệu
const handlePriceInput = (field, event) => {
    // Xóa mọi ký tự không phải số
    const rawString = event.target.value.replace(/[^0-9]/g, '');
    
    // Ép kiểu về số nguyên và lưu vào formData
    formData.value[field] = rawString ? parseInt(rawString, 10) : 0;
    
    // Cập nhật lại giao diện bằng chuỗi có dấu phẩy
    event.target.value = formatDisplayPrice(formData.value[field]);
    
    // Xóa cảnh báo lỗi nếu có
    errors.value[field] = '';
};

// ==========================================
// 5. UPLOAD ẢNH & TIỆN ÍCH
// ==========================================
const getImg = (url) => (url && (url.startsWith('http') || url.startsWith('data:'))) ? url : 'https://placehold.co/100x100?text=No+Img';

const resizeImage = (file, maxWidth = 800) => {
    return new Promise((resolve) => {
        const reader = new FileReader(); reader.readAsDataURL(file);
        reader.onload = (e) => {
            const img = new Image(); img.src = e.target.result;
            img.onload = () => {
                const canvas = document.createElement('canvas'); let w = img.width, h = img.height;
                if (w > maxWidth) { h *= maxWidth / w; w = maxWidth; }
                canvas.width = w; canvas.height = h; canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                resolve(canvas.toDataURL('image/jpeg', 0.8));
            };
        };
    });
};

const handleFileUpload = async (event) => {
    if (isViewMode.value) return;
    const file = event.target.files[0];
    if (!file || !file.type.match('image.*')) return Swal.fire('Lỗi', 'Chỉ chấp nhận file ảnh!', 'error');
    try {
        formData.value.hinhAnh = await resizeImage(file);
        errors.value.hinhAnh = '';
    } catch (e) { }
    event.target.value = '';
};

// ==========================================
// 6. VALIDATE & CẬP NHẬT DỮ LIỆU
// ==========================================
const validateForm = () => {
    errors.value = {};
    let isValid = true;

    const name = (formData.value.tenMon || '').trim();
    const desc = (formData.value.moTa || '').trim();

    // 1. Validate Tên món ăn (5 - 100 ký tự)
    if (!name) {
        errors.value.tenMon = 'Vui lòng nhập tên món ăn'; 
        isValid = false;
    } else if (name.length < 5) {
        errors.value.tenMon = 'Tên món ăn phải chứa ít nhất 5 kí tự';
        isValid = false;
    } else if (name.length > 100) {
        errors.value.tenMon = 'Tên món ăn không được vượt quá 100 ký tự';
        isValid = false;
    } else {
        // Check trùng tên (loại trừ chính ID đang sửa)
        const isDuplicate = existingFoods.value.some(
            food => food.tenMon.toLowerCase() === name.toLowerCase() && food.id !== formData.value.id
        );
        if (isDuplicate) {
            errors.value.tenMon = 'Tên món ăn này đã tồn tại trong hệ thống!'; 
            isValid = false;
        }
    }

    // 2. Validate Danh mục
    if (!formData.value.idDanhMuc) {
        errors.value.idDanhMuc = 'Vui lòng chọn danh mục cho món ăn'; 
        isValid = false;
    }

    // 3. Validate Giá bán (> 0)
    if (!formData.value.giaBan || formData.value.giaBan <= 0) {
        errors.value.giaBan = 'Giá bán phải lớn hơn 0'; 
        isValid = false;
    }

    // 4. Validate Mô tả (Max 255)
    if (desc.length > 255) {
        errors.value.moTa = 'Mô tả không được vượt quá 255 ký tự';
        isValid = false;
    }

    // 🔥 BẮN TOAST NẾU CÓ LỖI (Giống màn Check-in)
    if (!isValid) {
        Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'error',
            title: 'Dữ liệu không hợp lệ',
            text: 'Vui lòng kiểm tra lại các trường báo đỏ',
            showConfirmButton: false,
            timer: 3000
        });
    } else {
        // Gán lại dữ liệu đã trim
        formData.value.tenMon = name;
        formData.value.moTa = desc;
    }

    return isValid;
};

const handleUpdate = async () => {
    if (!validateForm()) return;

    // Tìm idDinhLuong từ 2 combobox
    const u = selectedUnit.value || '';
    const w = selectedWeight.value || '';

    const matchedDB = listDinhLuongDB.value.find(dbItem =>
        (dbItem.kichCo || '').toLowerCase() === u.toLowerCase() &&
        (dbItem.dinhLuong || '').toLowerCase() === w.toLowerCase()
    );

    if (!matchedDB) {
        return Swal.fire({
            icon: 'error',
            title: 'Lỗi Kích cỡ / Đơn vị',
            text: 'Tổ hợp Đơn vị và Kích cỡ này chưa tồn tại trong danh mục. Vui lòng kiểm tra lại cấu hình định lượng.'
        });
    }

    const payload = {
        ...formData.value,
        idDinhLuong: matchedDB.id
    };

    const result = await Swal.fire({
        title: 'Xác nhận lưu?',
        text: 'Mọi thay đổi sẽ được cập nhật vào hệ thống.',
        icon: 'question',
        iconColor: '#7D161A',
        showCancelButton: true,
        confirmButtonColor: '#7D161A',
        confirmButtonText: 'Lưu thay đổi',
        cancelButtonText: 'Hủy',
    });

    if (result.isConfirmed) {
        try {
            await foodApi.updateFood(formData.value.id, payload);
            Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', showConfirmButton: false, timer: 1500 });
            setTimeout(() => goBack(), 1500);
        } catch (error) {
            Swal.fire({
                icon: 'error',
                title: 'Thất bại',
                text: error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật.'
            });
        }
    }
};

const goBack = () => router.back();
</script>

<template>
    <div class="main-content d-flex flex-column h-100">
        <div class="page-header">
            <div class="header-title">
                <h1>{{ isViewMode ? 'Chi tiết món ăn' : 'Cập nhật món ăn' }}</h1>
                <p class="subtitle">{{ isViewMode ? 'Xem thông tin món ăn' : 'Chỉnh sửa thông tin món ăn' }}</p>
            </div>
            <div class="header-actions">
                <button class="btn-back" @click="goBack">← Quay lại</button>
            </div>
        </div>

        <div v-if="isLoading" class="loading-state text-center mt-5">
            <i class="fas fa-spinner fa-spin fa-2x"></i>
            <p class="mt-2">Đang tải dữ liệu...</p>
        </div>

        <div v-else class="page-content-wrapper d-flex flex-column" style="flex: 1; overflow: hidden;">

            <div class="content-scroll-wrapper" style="flex: 1; overflow-y: auto; padding-bottom: 20px;">
                <div class="info-hero-card" v-if="originalInfo" style="margin-bottom: 20px;">
                    <div class="hero-image">
                        <img :src="getImg(originalInfo.hinhAnh)" alt="Ảnh món ăn">
                    </div>
                    <div class="hero-details">
                        <div class="hero-header">
                            <h2 class="hero-title">
                                {{ originalInfo.tenMon }}
                                <span class="code-badge">#{{ originalInfo.maMon }}</span>
                            </h2>
                            <span :class="['status-badge', originalInfo.trangThai === 1 ? 'active' : 'inactive']">
                                {{ originalInfo.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
                            </span>
                        </div>
                        <div class="hero-meta-grid">
                            <div class="meta-item">
                                <span class="label">Danh mục:</span>
                                <span class="value" style="color: #d32f2f; font-weight: bold;">
                                    <i class="fas fa-tags me-1"></i> {{ categoryName }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="page-content" :class="{ 'view-mode': isViewMode }">
                    <div class="section-left" style="flex: 1.5;">
                        <div class="card">
                            <h3>Thông tin cơ bản</h3>
                            <div class="form-container">

                                <div class="form-group">
                                    <label>Mã hệ thống</label>
                                    <input v-model="formData.maMon" type="text" disabled
                                        style="background: #f8f9fa; font-weight: bold; color: #555;">
                                </div>

                                <div class="form-group">
                                    <label>Tên món ăn <span class="required" v-if="!isViewMode">*</span></label>
                                    <input v-model="formData.tenMon" type="text" :disabled="isViewMode"
                                        :class="{ 'input-error': errors.tenMon }" 
                                        placeholder="VD: Ba chỉ bò Mỹ...">
                                    <small class="error-text" v-if="errors.tenMon">{{ errors.tenMon }}</small>
                                </div>

                                <div class="form-row-2">
                                    <div class="form-group">
                                        <label>Giá bán (VNĐ) <span class="required" v-if="!isViewMode">*</span></label>
                                        <div class="input-group input-group-sm flex-nowrap">
                                            <input 
                                                :value="formatDisplayPrice(formData.giaBan)" 
                                                @input="handlePriceInput('giaBan', $event)" 
                                                type="text" 
                                                class="form-control text-end fw-bold" 
                                                :disabled="isViewMode"
                                                :class="{ 'is-invalid': errors.giaBan }" 
                                                placeholder="0"
                                                style="padding-right: 12px;" 
                                            >
                                            <span class="input-group-text bg-light text-secondary fw-bold border-start-0">đ</span>
                                        </div>
                                        <span class="error-message" v-if="errors.giaBan">{{ errors.giaBan }}</span>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label>Mô tả chi tiết</label>
                                    <textarea v-model="formData.moTa" rows="4" :disabled="isViewMode"
                                        :class="{ 'input-error': errors.moTa }"
                                        placeholder="Thành phần, hương vị..."></textarea>
                                    <small class="error-text" v-if="errors.moTa">{{ errors.moTa }}</small>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="section-right" style="flex: 1;">
                        <div class="card full-height-card">
                            <h3>Phân loại & Hình ảnh</h3>
                            <div class="form-container">

                                <div class="form-group">
                                    <label>Thuộc danh mục <span class="required" v-if="!isViewMode">*</span></label>
                                    <div :class="{ 'multiselect-error-border': errors.idDanhMuc }">
                                        <Multiselect v-model="formData.idDanhMuc" :options="listCategories" 
                                            valueProp="id" label="tenDanhMuc" :disabled="isViewMode" :searchable="true"
                                            class="custom-multiselect-theme"
                                            @change="(val) => loadDataByCategory(val, true)"
                                            placeholder="-- Chọn danh mục --" />
                                    </div>
                                    <small class="error-text" v-if="errors.idDanhMuc">{{ errors.idDanhMuc }}</small>
                                </div>

                                <div class="form-row-2 mt-3">
                                    <div class="form-group">
                                        <label>Đơn vị tính</label>
                                        <Multiselect v-model="selectedUnit" :options="unitOptions"
                                            :disabled="isViewMode || !formData.idDanhMuc" :searchable="true"
                                            :create-option="false" noResultsText="Không tìm thấy kết quả nào"
                                            placeholder="Chọn đơn vị..." noOptionsText="Không có dữ liệu" />
                                        <small v-if="!formData.idDanhMuc" class="text-muted mt-1 d-block">Vui lòng chọn
                                            Danh mục</small>
                                    </div>
                                    <div class="form-group">
                                        <label>Khối lượng / Kích cỡ</label>
                                        <Multiselect v-model="selectedWeight" :options="weightOptions"
                                            :disabled="isViewMode || !formData.idDanhMuc" :searchable="true"
                                            :create-option="false" noResultsText="Không tìm thấy kết quả nào"
                                            placeholder="Chọn kích cỡ..." noOptionsText="Không có dữ liệu" />
                                    </div>
                                </div>

                                <div class="form-group mt-4">
                                    <label>Hình ảnh đại diện</label>
                                    <div class="upload-box-wrapper text-center">
                                        <div class="upload-container" v-if="!isViewMode"
                                            :class="{ 'invalid-border': errors.hinhAnh }">
                                            <input type="file" accept="image/*" @change="handleFileUpload"
                                                style="display: none;" id="file-upload" />

                                            <div v-if="!formData.hinhAnh" class="empty-upload"
                                                onclick="document.getElementById('file-upload').click()"
                                                style="padding: 20px; border: 2px dashed #ccc; cursor: pointer; border-radius: 8px;">
                                                <i class="fas fa-image" style="font-size: 2rem; color: #888;"></i>
                                                <p class="mt-2 mb-0">Bấm để tải ảnh lên</p>
                                            </div>

                                            <div v-else class="preview-mode">
                                                <img :src="formData.hinhAnh"
                                                    style="width: 100%; height: 200px; object-fit: cover; border-radius: 8px;">
                                                <div class="mt-2 d-flex justify-content-between">
                                                    <button class="btn btn-sm btn-outline-primary"
                                                        onclick="document.getElementById('file-upload').click()">Đổi
                                                        ảnh</button>
                                                    <button class="btn btn-sm btn-outline-danger"
                                                        @click="formData.hinhAnh = ''">Xóa ảnh</button>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="large-preview-container" v-else>
                                            <img :src="getImg(formData.hinhAnh)"
                                                style="width: 100%; max-height: 250px; object-fit: cover; border-radius: 8px; border: 1px solid #eee;">
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-footer-sticky" v-if="!isViewMode"
                style="border-top: 1px solid #ddd; background: #fff; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; padding: 15px 20px; display: flex; justify-content: flex-end; align-items: center; width: 100%; gap: 15px;">
                <button class="btn btn-secondary" @click="goBack"
                    style="min-width: 120px; padding: 10px 20px; border-radius: 6px;">Hủy bỏ</button>
                <button class="btn btn-danger" @click="handleUpdate"
                    style="min-width: 180px; padding: 10px 20px; background-color: #d32f2f; border: none; color: white; border-radius: 6px;">
                    <i class="fas fa-save me-1"></i> Lưu thông tin món
                </button>
            </div>

            <div class="page-footer-sticky" v-else
                style="border-top: 1px solid #ddd; background: #fff; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; padding: 15px 20px; display: flex; justify-content: center; width: 100%;">
                <button class="btn btn-secondary w-100" @click="goBack" style="padding: 10px; border-radius: 6px;">Quay
                    lại danh sách</button>
            </div>

        </div>
    </div>
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

.text-gray {
    color: #888;
}

.italic {
    font-style: italic;
}

.view-mode .toggle-wrapper.disabled {
    cursor: default;
    opacity: 0.7;
}

.btn-primary {
    background-color: #007bff;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: 500;
}

.btn-primary:hover {
    background-color: #0056b3;
}

textarea {
    width: 100%;
    padding: 10px;
    border: 1px solid #ced4da;
    border-radius: 4px;
    resize: vertical;
}

textarea:disabled {
    background-color: #f8f9fa;
    color: #555;
}

/* Custom CSS cho Combobox hiển thị dạng thẻ đơn giản */
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
    /* Đổi viền thành màu đỏ rượu */
    border-color: #8B0000 !important;

    /* Tạo hiệu ứng phát sáng (glow) viền ngoài màu đỏ trong suốt 20% */
    box-shadow: 0 0 0 4px rgba(139, 0, 0, 0.2) !important;

    /* Xóa viền xanh dương mặc định của trình duyệt */
    outline: none !important;
}

/* Kèm thêm class custom của bạn cho chắc chắn (nếu cần) */
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

.input-error {
    border-color: #dc3545 !important;
    background-color: #fff8f8 !important;
}

/* Bôi đỏ viền cho Multiselect */
.multiselect-error-border :deep(.multiselect) {
    border-color: #dc3545 !important;
    background-color: #fff8f8 !important;
}

/* Định dạng text lỗi */
.error-text {
    display: block;
    margin-top: 4px;
    font-size: 12px;
    color: #dc3545;
    font-style: italic;
}

/* Đảm bảo input group giá không bị vỡ dòng (chữ đ nằm cùng hàng) */
.input-group {
    display: flex !important;
    flex-wrap: nowrap !important;
}

.input-group .form-control {
    flex: 1;
}
</style>