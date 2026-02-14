<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import { foodApi } from '../../../../../services/foodFunction';
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

// Danh sách Định lượng thật từ Database (Để lấy ID gửi lên API tránh lỗi 400)
const listDinhLuongDB = ref([]);

// Dữ liệu cho Combobox (Sẽ được đổ tự động từ DB khi chọn Danh mục)
const unitOptions = ref([]);
const weightOptions = ref([]);

// Giá trị user chọn trong Combobox
const selectedUnits = ref([]);
const selectedWeights = ref([]);

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
    // Reset dữ liệu cũ
    selectedUnits.value = [];
    selectedWeights.value = [];
    generatedVariants.value = [];
    unitOptions.value = [];
    weightOptions.value = [];
    errors.value.idDanhMuc = '';

    if (!formData.value.idDanhMuc) return;

    try {
        const res = await foodApi.getUnitsByCategory(formData.value.idDanhMuc);
        listDinhLuongDB.value = res.data || [];

        const units = new Set();
        const weights = new Set();

        // CHỈ LẤY DỮ LIỆU CÓ SẴN TỪ DATABASE DỰA VÀO JSON
        listDinhLuongDB.value.forEach(item => {
            // Combobox 1: Đẩy dữ liệu từ trường "kichCo"
            if (item.kichCo) units.add(item.kichCo.trim());

            // Combobox 2: Đẩy dữ liệu từ trường "dinhLuong"
            if (item.dinhLuong) weights.add(item.dinhLuong.trim());
        });

        unitOptions.value = Array.from(units);
        weightOptions.value = Array.from(weights);

    } catch (error) {
        console.error("Lỗi lấy định lượng:", error);
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

    const newVariants = [];
    const name = formData.value.baseName.trim();
    const uList = selectedUnits.value.length > 0 ? selectedUnits.value : [''];
    const wList = selectedWeights.value.length > 0 ? selectedWeights.value : [''];

    uList.forEach(u => {
        wList.forEach(w => {
            const parts = [name, u, w].filter(p => p !== '');
            const variantName = parts.join(' - ');

            // KHỚP VỚI JSON CỦA BẠN: u = kichCo (VD: S), w = dinhLuong (VD: 100g)
            const matchedDB = listDinhLuongDB.value.find(dbItem => 
                (dbItem.kichCo || '').toLowerCase() === u.toLowerCase() && 
                (dbItem.dinhLuong || '').toLowerCase() === w.toLowerCase()
            );

            const existing = generatedVariants.value.find(v => v.tenMon === variantName);

            newVariants.push({
                tenMon: variantName,
                giaVon: existing ? existing.giaVon : 0,
                giaBan: existing ? existing.giaBan : 0,
                tenDinhLuong: matchedDB ? matchedDB.tenHienThi : `${u} ${w}`.trim(),
                idDinhLuong: matchedDB ? matchedDB.id : null, 
                
                // --- THÊM 2 TRƯỜNG NÀY ĐỂ ĐÁNH DẤU TẠO MỚI ---
                isNewUnit: !matchedDB, 
                newUnitData: !matchedDB ? { kichCo: u, dinhLuong: w } : null,
                
                trangThai: existing ? existing.trangThai : 1
            });
        });
    });
    generatedVariants.value = newVariants;
};

const removeVariant = (index) => generatedVariants.value.splice(index, 1);

// ==========================================
// 4. LOGIC UPLOAD ẢNH (Rút gọn)
// ==========================================
const triggerFileInput = () => fileInputRef.value.click();
const resizeImage = (file, maxWidth = 800) => { /* Giữ nguyên hàm của bạn */
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
    // Validate cơ bản
    if (!formData.value.idDanhMuc || !formData.value.baseName || !formData.value.hinhAnh) {
        return Swal.fire({ icon: 'warning', text: 'Vui lòng nhập đủ thông tin và ảnh!' });
    }
    if (generatedVariants.value.length === 0) return;

    const result = await Swal.fire({
        title: 'Xác nhận lưu?',
        text: "Hệ thống sẽ tự động tạo các định lượng mới và lưu món ăn.",
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý'
    });

    if (result.isConfirmed) {
        try {
            // --- BƯỚC 1: TẠO ĐỊNH LƯỢNG MỚI VÀ CẬP NHẬT ID ---
            // Dùng for...of để đợi từng API chạy xong rồi mới đi tiếp
            for (let variant of generatedVariants.value) {
                if (variant.isNewUnit || !variant.idDinhLuong) {
                    const unitPayload = {
                        idDanhMuc: formData.value.idDanhMuc,
                        kichCo: variant.newUnitData.kichCo,
                        dinhLuong: variant.newUnitData.dinhLuong,
                        tenHienThi: variant.tenDinhLuong
                    };
                    
                    // GỌI API TẠO ĐỊNH LƯỢNG
                    const resUnit = await foodApi.createUnit(unitPayload);
                    
                    // LẤY ID TRẢ VỀ TỪ BACKEND GÁN VÀO BIẾN THỂ
                    if (resUnit.data && resUnit.data.id) {
                        variant.idDinhLuong = resUnit.data.id;
                        variant.isNewUnit = false; 
                    } else {
                        throw new Error(`Không lấy được ID cho định lượng: ${variant.tenDinhLuong}`);
                    }
                }
            }

            // --- BƯỚC 2: KIỂM TRA LẠI MỘT LẦN NỮA ---
            const stillMissingId = generatedVariants.value.some(v => !v.idDinhLuong);
            if (stillMissingId) {
                throw new Error("Vẫn còn món chưa có ID định lượng!");
            }

            // --- BƯỚC 3: LƯU HÀNG LOẠT MÓN ĂN ---
            const savePromises = generatedVariants.value.map(v => {
                return foodApi.createFood({
                    tenMon: v.tenMon,
                    giaVon: v.giaVon,
                    giaBan: v.giaBan,
                    moTa: formData.value.moTa,
                    hinhAnh: formData.value.hinhAnh,
                    idDanhMuc: formData.value.idDanhMuc,
                    idDinhLuong: v.idDinhLuong, // BÂY GIỜ CHẮC CHẮN ĐÃ CÓ ID
                    trangThai: 1
                });
            });

            await Promise.all(savePromises);
            
            Swal.fire({ icon: 'success', title: 'Thành công!', showConfirmButton: false, timer: 1500 });
            router.push({ name: 'foodManager', query: { tab: 'thucdon' } });
            
        } catch (error) {
            console.error("Lỗi quá trình lưu:", error);
            Swal.fire({ 
                icon: 'error', 
                title: 'Lưu thất bại', 
                text: error.response?.data?.errors?.idDinhLuong || "Vui lòng kiểm tra lại dữ liệu định lượng!" 
            });
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
                                <option v-for="cat in listCategories" :key="cat.id" :value="cat.id">{{ cat.tenDanhMuc }}
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
                    <h3><i class="fas fa-layer-group"></i> Bước 2: Tạo các tùy chọn (Size/Unit)</h3>
                    <div class="form-container">
                        <div class="form-row-2">
                            <div class="form-group">
                                <label>Đơn vị tính</label>
                                <Multiselect v-model="selectedUnits" mode="tags" :options="unitOptions"
                                    :disabled="!formData.idDanhMuc" :searchable="true" :create-option="false"
                                    placeholder="Chọn đơn vị..." noOptionsText="Không có dữ liệu từ DB" />
                                <small v-if="!formData.idDanhMuc" class="text-muted mt-1 d-block">Vui lòng chọn Danh mục
                                    trước</small>
                            </div>

                            <div class="form-group">
                                <label>Khối lượng / Kích cỡ</label>
                                <Multiselect v-model="selectedWeights" mode="tags" :options="weightOptions"
                                    :disabled="!formData.idDanhMuc" :searchable="true" :create-option="false"
                                    placeholder="Chọn khối lượng..." noOptionsText="Không có dữ liệu từ DB" />
                            </div>
                        </div>

                        <div class="text-center mt-3">
                            <button class="btn btn-warning fw-bold" @click="handleGenerateVariants"
                                :disabled="!formData.idDanhMuc">
                                <i class="fas fa-magic"></i> Tạo Bảng Biến Thể
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
                                    <th>Trạng thái CSDL</th>
                                    <th style="width: 130px;">Giá bán <span class="text-danger">*</span></th>
                                    <th style="width: 50px;">Xóa</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(v, idx) in generatedVariants" :key="idx">
                                    <td class="text-center">{{ idx + 1 }}</td>
                                    <td class="fw-bold">{{ v.tenMon }}</td>

                                    <td>
                                        <span v-if="!v.isNewUnit" class="badge bg-success">
                                            <i class="fas fa-check"></i> Có sẵn trong DB
                                        </span>
                                        <span v-else class="badge bg-warning text-dark"
                                            title="Hệ thống sẽ tự động tạo định lượng này">
                                            <i class="fas fa-plus"></i> Sẽ tạo mới ngầm
                                        </span>
                                    </td>

                                    <td><input v-model="v.giaBan" type="number" class="form-control form-control-sm"
                                            placeholder="0"></td>
                                    <td class="text-center">
                                        <button class="btn-clear-img text-danger" @click="removeVariant(idx)"><i
                                                class="fas fa-trash"></i></button>
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
                                            <button class="btn btn-sm btn-outline-primary" @click="triggerFileInput">Đổi
                                                ảnh</button>
                                            <button class="btn btn-sm btn-outline-danger"
                                                @click="formData.hinhAnh = ''">Xóa</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="page-footer mt-4 text-center">
                    <button class="btn btn-secondary me-2" @click="router.back()" style="min-width: 120px;">Hủy
                        bỏ</button>
                    <button class="btn btn-danger" @click="handleSave"
                        style="min-width: 120px; background-color: #d32f2f;">Lưu hàng loạt</button>
                </div>
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

:deep(.multiselect-tag) {
    background-color: #7b121c;
    color: white;
    font-weight: 500;
}

:deep(.multiselect-tag i:before) {
    color: white;
}

:deep(.multiselect.is-disabled) {
    background-color: #e9ecef;
    cursor: not-allowed;
}

.variants-table {
    width: 100%;
    border-collapse: collapse;
}

.variants-table th,
.variants-table td {
    border: 1px solid #eee;
    padding: 8px;
    vertical-align: middle;
}

.variants-table th {
    background-color: #f8f9fa;
    font-weight: 600;
}
</style>