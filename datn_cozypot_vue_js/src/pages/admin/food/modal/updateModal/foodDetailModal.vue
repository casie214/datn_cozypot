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
onMounted(async () => {
    const id = route.params.id;
    if (!id) return goBack();

    try {
        isLoading.value = true;
        
        // Gọi API lấy Chi tiết món và Danh sách Danh mục
        const [resFood, resCat] = await Promise.all([
            foodApi.getFoodById(id),
            foodApi.getCategories()
        ]);

        listCategories.value = resCat.data || [];
        const data = resFood.data;
        originalInfo.value = { ...data };

        // Đổ dữ liệu vào Form
        formData.value = {
            id: data.id,
            maMon: data.maMon || '',
            tenMon: data.tenMon || '',
            giaVon: data.giaVon || 0,
            giaBan: data.giaBan || 0,
            moTa: data.moTa || '',
            idDanhMuc: data.idDanhMuc || '',
            trangThai: data.trangThai !== undefined ? data.trangThai : 1,
            hinhAnh: data.hinhAnh || ''
        };

        // Gán giá trị đang có cho Combobox
        selectedUnit.value = data.kichCo || ''; 
        selectedWeight.value = data.giaTriDinhLuong || ''; 

        // Nếu món ăn đã có idDanhMuc, tự động load danh sách Kích cỡ / Đơn vị
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

    if (!idDanhMuc) return;

    try {
        const res = await foodApi.getUnitsByCategory(idDanhMuc);
        listDinhLuongDB.value = res.data || [];

        const units = new Set();
        const weights = new Set();

        listDinhLuongDB.value.forEach(item => {
            // Dựa vào JSON DB thực tế: kichCo là Đơn vị (Lon, Chai...), dinhLuong là Khối lượng (330ml...)
            if (item.kichCo) units.add(item.kichCo.trim());
            if (item.dinhLuong) weights.add(item.dinhLuong.trim());
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
    } catch (e) {}
    event.target.value = '';
};

// ==========================================
// 6. VALIDATE & CẬP NHẬT DỮ LIỆU
// ==========================================
const validateForm = () => {
    errors.value = {};
    let isValid = true;

    if (!formData.value.tenMon || formData.value.tenMon.trim() === '') {
        errors.value.tenMon = 'Vui lòng nhập tên món ăn'; isValid = false;
    }
    if (!formData.value.idDanhMuc) {
        errors.value.idDanhMuc = 'Vui lòng chọn danh mục'; isValid = false;
    }
    if (formData.value.giaBan <= 0) {
        errors.value.giaBan = 'Giá bán phải lớn hơn 0'; isValid = false;
    }

    return isValid;
};

const handleUpdate = async () => {
    if (!validateForm()) {
        return Swal.fire({ icon: 'error', text: 'Vui lòng kiểm tra lại các ô báo đỏ!', toast: true, position: 'top-end', timer: 2000 });
    }

    // TÌM idDinhLuong TỪ 2 COMBOBOX
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
            text: 'Tổ hợp Đơn vị và Kích cỡ này chưa tồn tại trong danh mục đã chọn. Vui lòng chọn tổ hợp khác hoặc tạo mới trong bảng Định Lượng.' 
        });
    }

    // TẠO PAYLOAD MAP ĐÚNG VỚI MonAnRequest
    const payload = {
        tenMon: formData.value.tenMon,
        maMon: formData.value.maMon, // Có thể null để backend sinh
        giaBan: formData.value.giaBan,
        giaVon: formData.value.giaVon,
        moTa: formData.value.moTa,
        hinhAnh: formData.value.hinhAnh,
        trangThai: formData.value.trangThai,
        idDanhMuc: formData.value.idDanhMuc,
        idDinhLuong: matchedDB.id // RẤT QUAN TRỌNG: ID lấy từ DB
    };

    Swal.fire({
        title: 'Xác nhận cập nhật?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        confirmButtonText: 'Đồng ý'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await foodApi.updateFood(formData.value.id, payload);
                Swal.fire({ icon: 'success', title: 'Thành công', showConfirmButton: false, timer: 1500 });
                setTimeout(() => goBack(), 1500);
            } catch (error) {
                console.error("Lỗi cập nhật:", error);
                Swal.fire('Lỗi', 'Không thể cập nhật món ăn, vui lòng thử lại!', 'error');
            }
        }
    });
};

const goBack = () => router.back();
</script>

<template>
    <div class="main-content">
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

        <div v-else class="page-content-wrapper">
            <div class="info-hero-card" v-if="originalInfo">
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
                                <input v-model="formData.maMon" type="text" disabled style="background: #f8f9fa; font-weight: bold; color: #555;">
                            </div>

                            <div class="form-group">
                                <label>Tên món ăn <span class="required" v-if="!isViewMode">*</span></label>
                                <input v-model="formData.tenMon" type="text" :disabled="isViewMode"
                                    :class="{ 'invalid-border': errors.tenMon }"
                                    @input="errors.tenMon = ''" placeholder="VD: Ba chỉ bò Mỹ...">
                                <span class="error-message" v-if="errors.tenMon">{{ errors.tenMon }}</span>
                            </div>

                            <div class="form-row-2">
                                <div class="form-group">
                                    <label>Giá vốn (VNĐ)</label>
                                    <input v-model="formData.giaVon" type="number" :disabled="isViewMode">
                                </div>
                                <div class="form-group">
                                    <label>Giá bán (VNĐ) <span class="required" v-if="!isViewMode">*</span></label>
                                    <input v-model="formData.giaBan" type="number" :disabled="isViewMode"
                                        :class="{ 'invalid-border': errors.giaBan }" @input="errors.giaBan = ''">
                                    <span class="error-message" v-if="errors.giaBan">{{ errors.giaBan }}</span>
                                </div>
                            </div>

                            <div class="form-group">
                                <label>Mô tả chi tiết</label>
                                <textarea v-model="formData.moTa" rows="4" :disabled="isViewMode" 
                                    placeholder="Thành phần, hương vị..."></textarea>
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

                <div class="section-right" style="flex: 1;">
                    <div class="card full-height-card">
                        <h3>Phân loại & Hình ảnh</h3>
                        <div class="form-container">

                            <div class="form-group">
                                <label>Thuộc danh mục <span class="required" v-if="!isViewMode">*</span></label>
                                <select v-model="formData.idDanhMuc" class="form-control" :disabled="isViewMode"
                                    :class="{ 'invalid-border': errors.idDanhMuc }" 
                                    @change="loadDataByCategory(formData.idDanhMuc, true)">
                                    <option value="" disabled>-- Chọn danh mục --</option>
                                    <option v-for="cat in listCategories" :key="cat.id" :value="cat.id">
                                        {{ cat.tenDanhMuc }}
                                    </option>
                                </select>
                                <span class="error-message" v-if="errors.idDanhMuc">{{ errors.idDanhMuc }}</span>
                            </div>

                            <div class="form-row-2 mt-3">
                                <div class="form-group">
                                    <label>Đơn vị tính</label>
                                    <Multiselect 
                                        v-model="selectedUnit" 
                                        :options="unitOptions"
                                        :disabled="isViewMode || !formData.idDanhMuc" 
                                        :searchable="true" 
                                        :create-option="false"
                                        placeholder="Chọn đơn vị..." 
                                        noOptionsText="Không có dữ liệu" 
                                    />
                                    <small v-if="!formData.idDanhMuc" class="text-muted mt-1 d-block">Vui lòng chọn Danh mục</small>
                                </div>
                                <div class="form-group">
                                    <label>Khối lượng / Kích cỡ</label>
                                    <Multiselect 
                                        v-model="selectedWeight" 
                                        :options="weightOptions"
                                        :disabled="isViewMode || !formData.idDanhMuc" 
                                        :searchable="true" 
                                        :create-option="false"
                                        placeholder="Chọn kích cỡ..." 
                                        noOptionsText="Không có dữ liệu" 
                                    />
                                </div>
                            </div>

                            <div class="form-group mt-4">
                                <label>Hình ảnh đại diện</label>
                                <div class="upload-box-wrapper text-center">
                                    <div class="upload-container" v-if="!isViewMode"
                                        :class="{ 'invalid-border': errors.hinhAnh }">
                                        <input type="file" accept="image/*" @change="handleFileUpload" style="display: none;" id="file-upload" />
                                        
                                        <div v-if="!formData.hinhAnh" class="empty-upload" onclick="document.getElementById('file-upload').click()" 
                                             style="padding: 20px; border: 2px dashed #ccc; cursor: pointer; border-radius: 8px;">
                                            <i class="fas fa-image" style="font-size: 2rem; color: #888;"></i>
                                            <p class="mt-2 mb-0">Bấm để tải ảnh lên</p>
                                        </div>

                                        <div v-else class="preview-mode">
                                            <img :src="formData.hinhAnh" style="width: 100%; height: 200px; object-fit: cover; border-radius: 8px;">
                                            <div class="mt-2 d-flex justify-content-between">
                                                <button class="btn btn-sm btn-outline-primary" onclick="document.getElementById('file-upload').click()">Đổi ảnh</button>
                                                <button class="btn btn-sm btn-outline-danger" @click="formData.hinhAnh = ''">Xóa ảnh</button>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="large-preview-container" v-else>
                                        <img :src="getImg(formData.hinhAnh)" style="width: 100%; max-height: 250px; object-fit: cover; border-radius: 8px; border: 1px solid #eee;">
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>

            </div>

            <div class="page-footer" v-if="!isViewMode" style="display: flex;">
                <button class="btn-large btn-cancel" @click="goBack">Hủy bỏ</button>
                <button class="btn-large btn-save full-width bg-danger text-white border-0" @click="handleUpdate">
                    <i class="fas fa-save me-1"></i> Lưu thông tin món
                </button>
            </div>

            <div class="page-footer" v-else style="display: flex;">
                <button class="btn-large btn-cancel w-100" @click="goBack">Quay lại danh sách</button>
            </div>
        </div>

    </div>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");

.invalid-border { border: 1px solid #dc3545 !important; }
.error-message { color: #dc3545; font-size: 0.85em; margin-top: 4px; display: block; }
.text-gray { color: #888; }
.italic { font-style: italic; }

.view-mode .toggle-wrapper.disabled { cursor: default; opacity: 0.7; }
.btn-primary { background-color: #007bff; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-weight: 500; }
.btn-primary:hover { background-color: #0056b3; }

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
</style>