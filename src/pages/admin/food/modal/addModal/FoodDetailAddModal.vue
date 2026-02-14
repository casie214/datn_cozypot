<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import { foodApi } from '../../../../../services/foodFunction'; // Đảm bảo đường dẫn đúng
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';

const router = useRouter();

// ==========================================
// 1. DATA FORM CHUNG
// ==========================================
const formData = ref({
    baseName: '',
    moTa: '',
    hinhAnh: '',
    idDanhMuc: '',
});

const errors = ref({});
const listCategories = ref([]);

// Dữ liệu Đơn vị (Cha - Con) lấy từ API
const availableUnits = ref([]); 

// Danh sách ID các định lượng con được chọn (VD: [101, 102, 205])
const selectedDinhLuongIds = ref([]);

const generatedVariants = ref([]);
const fileInputRef = ref(null);

// Lấy danh sách Danh mục khi tải trang
onMounted(async () => {
    try {
        const res = await foodApi.getCategories();
        listCategories.value = res.data;
    } catch (error) {
        console.error("Lỗi lấy danh mục:", error);
    }
});

// ==========================================
// 2. LOGIC LẤY ĐỊNH LƯỢNG TỪ DB THEO DANH MỤC
// ==========================================
const loadDataByCategory = async () => {
    // Reset dữ liệu
    availableUnits.value = [];
    selectedDinhLuongIds.value = [];
    generatedVariants.value = [];
    errors.value.idDanhMuc = '';

    if (!formData.value.idDanhMuc) return;

    try {
        // Gọi API mới: Lấy Đơn vị theo Danh mục (đã lọc active)
        // Endpoint: /unit-types/by-category/{id}
        const res = await foodApi.getUnitTypesByCategory(formData.value.idDanhMuc);
        availableUnits.value = res.data || [];
        
        if (availableUnits.value.length === 0) {
            Swal.fire('Thông báo', 'Danh mục này chưa có định lượng nào. Vui lòng sang trang Quản lý Đơn vị để cấu hình trước!', 'info');
        }
    } catch (error) {
        console.error("Lỗi lấy định lượng:", error);
    }
};

// Hàm chọn/bỏ chọn một giá trị định lượng
const toggleDinhLuong = (id) => {
    if (selectedDinhLuongIds.value.includes(id)) {
        selectedDinhLuongIds.value = selectedDinhLuongIds.value.filter(i => i !== id);
    } else {
        selectedDinhLuongIds.value.push(id);
    }
};

// ==========================================
// 3. LOGIC TẠO BẢNG BIẾN THỂ
// ==========================================
const handleGenerateVariants = () => {
    errors.value.baseName = '';
    if (!formData.value.baseName.trim()) {
        errors.value.baseName = 'Vui lòng nhập Tên sản phẩm gốc trước!'; return;
    }

    if (selectedDinhLuongIds.value.length === 0) {
        return Swal.fire('Chú ý', 'Vui lòng chọn ít nhất 1 định lượng!', 'warning');
    }

    const newVariants = [];
    const baseName = formData.value.baseName.trim();

    // Duyệt qua tất cả Đơn vị Cha để tìm các con được chọn
    availableUnits.value.forEach(unit => {
        if (unit.values) {
            unit.values.forEach(val => {
                // Nếu giá trị con này nằm trong danh sách đã tick
                if (selectedDinhLuongIds.value.includes(val.id)) {
                    
                    // Tạo tên món: "Coca Cola - 330 ml" hoặc "Thịt bò - 200 gram"
                    // val.giaTri = "330", unit.tenDonVi = "ml"
                    const variantName = `${baseName} - ${val.giaTri} ${unit.tenDonVi}`;
                    
                    // Kiểm tra trùng trong list hiện tại
                    const existing = generatedVariants.value.find(v => v.idDinhLuong === val.id);

                    newVariants.push({
                        tenMon: variantName,
                        giaVon: existing ? existing.giaVon : 0,
                        giaBan: existing ? existing.giaBan : 0,
                        
                        // Lưu thông tin hiển thị
                        tenHienThiDinhLuong: `${val.giaTri} ${unit.tenDonVi}`,
                        
                        // ID QUAN TRỌNG ĐỂ GỬI BACKEND
                        idDinhLuong: val.id
                    });
                }
            });
        }
    });

    generatedVariants.value = newVariants;
};

const removeVariant = (index) => {
    // Khi xóa dòng bảng, ta cũng bỏ tick chọn ở trên cho đồng bộ
    const idToRemove = generatedVariants.value[index].idDinhLuong;
    selectedDinhLuongIds.value = selectedDinhLuongIds.value.filter(id => id !== idToRemove);
    
    generatedVariants.value.splice(index, 1);
};

// ==========================================
// 4. LOGIC UPLOAD ẢNH (Giữ nguyên)
// ==========================================
const triggerFileInput = () => fileInputRef.value.click();
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
    const file = event.target.files[0];
    if (!file || !file.type.match('image.*')) return;
    try { formData.value.hinhAnh = await resizeImage(file); errors.value.hinhAnh = ''; } catch (e) { }
    event.target.value = '';
};

// ==========================================
// 5. LƯU DỮ LIỆU
// ==========================================
const handleSave = async () => {
    // 1. Validate
    errors.value = {};
    if (!formData.value.idDanhMuc || !formData.value.baseName || !formData.value.hinhAnh) {
        return Swal.fire({ icon: 'warning', title: 'Thiếu thông tin', text: 'Vui lòng nhập đủ Tên món, Danh mục và Ảnh!' });
    }
    if (generatedVariants.value.length === 0) {
        return Swal.fire({ icon: 'warning', text: 'Vui lòng chọn định lượng và nhấn "Tạo biến thể"!' });
    }

    // Check giá bán
    const invalidPrice = generatedVariants.value.some(v => !v.giaBan || v.giaBan < 0);
    if (invalidPrice) {
        return Swal.fire({ icon: 'warning', text: 'Vui lòng nhập giá bán hợp lệ cho tất cả các món!' });
    }

    // 2. Xác nhận
    const result = await Swal.fire({
        title: 'Xác nhận lưu?',
        text: `Hệ thống sẽ tạo ${generatedVariants.value.length} món ăn mới.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#8B0000',
        confirmButtonText: 'Đồng ý'
    });

    if (result.isConfirmed) {
        Swal.fire({ title: 'Đang xử lý...', didOpen: () => Swal.showLoading() });

        try {
            // Chuẩn bị mảng Promises gọi API Create MonAn
            const savePromises = generatedVariants.value.map(v => {
                return foodApi.createFood({
                    tenMon: v.tenMon,
                    giaVon: v.giaVon || 0,
                    giaBan: v.giaBan,
                    moTa: formData.value.moTa || '',
                    hinhAnh: formData.value.hinhAnh,
                    idDanhMuc: formData.value.idDanhMuc,
                    idDinhLuong: v.idDinhLuong, // ID định lượng lấy từ DB
                    trangThai: 1
                });
            });

            await Promise.all(savePromises);
            
            await Swal.fire({ icon: 'success', title: 'Thành công!', timer: 1500, showConfirmButton: false });
            router.push({ name: 'foodManager', query: { tab: 'thucdon' } });
            
        } catch (error) {
            console.error("Lỗi lưu:", error);
            const msg = error.response?.data?.message || error.message || 'Lỗi hệ thống';
            Swal.fire({ icon: 'error', title: 'Lưu thất bại', text: msg });
        }
    }
};
</script>

<template>
    <div class="main-content">
        <div class="page-header">
            <div class="header-title">
                <h1>Thêm Món Ăn Mới (Gen Biến Thể)</h1>
            </div>
            <button class="btn-back" @click="router.back()"><i class="fas fa-arrow-left"></i> Quay lại</button>
        </div>

        <div class="page-content">
            <div class="section-left" style="flex: 2;">

                <div class="card mb-3" style="background-color: #fcfcfc; border: 1px solid #eee;">
                    <h3 class="text-danger"><i class="fas fa-filter"></i> Bước 1: Chọn Danh mục & Sản phẩm</h3>
                    <div class="form-container form-row-2">
                        <div class="form-group">
                            <label>Danh mục <span class="required">*</span></label>
                            <select v-model="formData.idDanhMuc" class="form-control" @change="loadDataByCategory"
                                style="border: 2px solid #7b121c;">
                                <option value="" disabled>-- Bắt buộc chọn --</option>
                                <option v-for="cat in listCategories" :key="cat.id" :value="cat.id">
                                    {{ cat.tenDanhMuc }}
                                </option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Tên sản phẩm gốc <span class="required">*</span></label>
                            <input v-model="formData.baseName" type="text" placeholder="VD: Nước ngọt Coca, Bò Mỹ..."
                                :disabled="!formData.idDanhMuc" :class="{ 'invalid-border': errors.baseName }">
                            <span class="error-message" v-if="errors.baseName">{{ errors.baseName }}</span>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <h3><i class="fas fa-layer-group"></i> Bước 2: Chọn Định Lượng / Kích Cỡ</h3>
                    
                    <div v-if="!formData.idDanhMuc" class="text-muted text-center p-4">
                        Vui lòng chọn Danh mục ở Bước 1 trước.
                    </div>

                    <div v-else class="unit-selection-container">
                        <div v-if="availableUnits.length === 0" class="empty-units">
                            <i class="fas fa-box-open"></i> Không tìm thấy định lượng nào cho danh mục này.
                        </div>

                        <div v-for="unit in availableUnits" :key="unit.id" class="unit-group">
                            <div class="unit-header">
                                <span class="unit-name">{{ unit.tenDonVi }}</span>
                                <span class="unit-desc text-muted ms-2" style="font-size: 0.8rem; font-weight: normal;">
                                    {{ unit.moTa }}
                                </span>
                            </div>
                            
                            <div class="unit-values">
                                <button 
                                    v-for="val in unit.values" 
                                    :key="val.id"
                                    class="btn-option"
                                    :class="{ 'active': selectedDinhLuongIds.includes(val.id) }"
                                    @click="toggleDinhLuong(val.id)"
                                >
                                    {{ val.giaTri }}
                                    <i v-if="selectedDinhLuongIds.includes(val.id)" class="fas fa-check-circle ms-1"></i>
                                </button>
                            </div>
                        </div>

                        <div class="text-left mt-4 border-top pt-3">
                            <button class="btn btn-danger fw-bold" @click="handleGenerateVariants"
                                :disabled="selectedDinhLuongIds.length === 0">
                                <i class="fas fa-magic"></i> Tạo {{ selectedDinhLuongIds.length }} biến thể
                            </button>
                        </div>
                    </div>
                </div>

                <div class="card mt-3" v-if="generatedVariants.length > 0">
                    <h3>Bước 3: Chi tiết món ăn (Nhập giá)</h3>
                    <div class="table-responsive">
                        <table class="table variants-table">
                            <thead>
                                <tr>
                                    <th style="width: 50px;">STT</th>
                                    <th>Tên món sẽ tạo</th>
                                    <th>Kích cỡ</th>
                                    <th style="width: 150px;">Giá bán <span class="text-danger">*</span></th>
                                    <th style="width: 50px;">Xóa</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(v, idx) in generatedVariants" :key="idx">
                                    <td class="text-center">{{ idx + 1 }}</td>
                                    <td class="fw-bold text-primary">{{ v.tenMon }}</td>
                                    <td>
                                        <span class="badge bg-light text-dark border">
                                            {{ v.tenHienThiDinhLuong }}
                                        </span>
                                    </td>
                                    <td>
                                        <div class="input-group input-group-sm">
                                            <input v-model="v.giaBan" type="number" class="form-control fw-bold" placeholder="0">
                                            <span class="input-group-text">đ</span>
                                        </div>
                                    </td>
                                    <td class="text-center">
                                        <button class="btn-clear-img text-danger" @click="removeVariant(idx)">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="section-right" style="flex: 1;">
                <div class="card full-height-card">
                    <h3>Thông tin phụ</h3>
                    <div class="form-container">
                        <div class="form-group">
                            <label>Mô tả chung</label>
                            <textarea v-model="formData.moTa" rows="3" class="form-control"
                                placeholder="Áp dụng cho mọi biến thể..."></textarea>
                        </div>
                        <div class="form-group mt-3">
                            <label>Hình đại diện chung <span class="required">*</span></label>
                            <div class="upload-box-wrapper text-center">
                                <div class="upload-container" :class="{ 'invalid-border': errors.hinhAnh }">
                                    <input type="file" accept="image/*" ref="fileInputRef" style="display: none"
                                        @change="handleFileUpload" />
                                    <div v-if="!formData.hinhAnh" class="empty-upload" @click="triggerFileInput"
                                        style="padding: 20px; border: 2px dashed #ccc; cursor: pointer; border-radius: 8px;">
                                        <i class="fas fa-image" style="font-size: 2rem; color: #888;"></i>
                                        <p class="mt-2 mb-0">Bấm để tải ảnh lên</p>
                                    </div>
                                    <div v-else class="preview-mode">
                                        <img :src="formData.hinhAnh"
                                            style="width: 100%; height: 150px; object-fit: cover; border-radius: 8px;">
                                        <div class="mt-2 d-flex justify-content-between">
                                            <button class="btn btn-sm btn-outline-primary" @click="triggerFileInput">Đổi ảnh</button>
                                            <button class="btn btn-sm btn-outline-danger" @click="formData.hinhAnh = ''">Xóa</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="page-footer mt-4 text-center">
                    <button class="btn btn-secondary me-2" @click="router.back()" style="min-width: 120px;">Hủy bỏ</button>
                    <button class="btn btn-danger" @click="handleSave" style="min-width: 120px; background-color: #d32f2f;">
                        Lưu hàng loạt
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");

.invalid-border { border: 1px solid #dc3545 !important; }
.error-message { color: #dc3545; font-size: 0.85em; margin-top: 4px; display: block; }

.variants-table { width: 100%; border-collapse: collapse; }
.variants-table th, .variants-table td { border: 1px solid #eee; padding: 10px; vertical-align: middle; }
.variants-table th { background-color: #f8f9fa; font-weight: 600; }

/* STYLE CHO PHẦN CHỌN UNIT (MỚI) */
.unit-group {
    margin-bottom: 20px;
    border-bottom: 1px dashed #eee;
    padding-bottom: 15px;
}
.unit-group:last-child { border-bottom: none; }

.unit-header {
    margin-bottom: 10px;
    display: flex;
    align-items: baseline;
}
.unit-name {
    font-weight: 700;
    color: #444;
    font-size: 1.1rem;
    text-transform: uppercase;
}

.unit-values {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.btn-option {
    background-color: white;
    border: 1px solid #ccc;
    color: #333;
    padding: 6px 16px;
    border-radius: 20px;
    cursor: pointer;
    font-weight: 500;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
}

.btn-option:hover {
    background-color: #f1f1f1;
    border-color: #bbb;
}

.btn-option.active {
    background-color: #8B0000;
    color: white;
    border-color: #8B0000;
    box-shadow: 0 2px 5px rgba(139, 0, 0, 0.3);
}

.empty-units {
    text-align: center;
    padding: 30px;
    color: #999;
    font-style: italic;
    background: #f9f9f9;
    border-radius: 8px;
}
</style>