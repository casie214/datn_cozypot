<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import { foodApi } from '../../../../../services/foodFunction';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';

// IMPORT MODALS
import UnitUpdateScreen from '@/pages/admin/unit/screens/UnitUpdateScreen.vue';
import CategoryAddModal from '@/pages/admin/category/modal/addModal/CategoryAddModal.vue';
import UnitAddScreen from '@/pages/admin/unit/screens/UnitAddScreen.vue';

const router = useRouter();

// ==========================================
// 1. DATA STATE
// ==========================================
const formData = ref({
    baseName: '',
    moTa: '',
    hinhAnh: '',
    idDanhMuc: '',
});
const props = defineProps({ 
    isModal: { type: Boolean, default: false },
    initialName: { type: String, default: '' } // Thêm dòng này để hứng tên món
});
const emit = defineEmits(['saved', 'cancel']);
const errors = ref({});
const listCategories = ref([]);
const existingFoods = ref([]);
const availableUnits = ref([]);
const selectedDinhLuongIds = ref([]);
const generatedVariants = ref([]);
const fileInputRef = ref(null);

const isCategoryModalOpen = ref(false);
const isUnitModalOpen = ref(false);
const isQuickAddUnitValueOpen = ref(false);
const selectedUnitForQuickAdd = ref(null);

const categoryMultiselectRef = ref(null); // Để móc vào DOM của Multiselect
const currentCategorySearchText = ref('');
const prefilledCategoryName = ref('');

// Theo dõi text đang gõ
const handleCategorySearchChange = (query) => {
    currentCategorySearchText.value = query;
};

// Bắt phím Enter
const handleCategoryKeydown = (event) => {
    if (event.key === 'Enter' && currentCategorySearchText.value.trim() !== '') {
        const text = currentCategorySearchText.value.trim();

        // Kiểm tra trùng
        const exists = listCategories.value.some(
            (cat) => cat.tenDanhMuc.toLowerCase() === text.toLowerCase()
        );

        // Nếu chưa có -> Mở Modal tạo danh mục
        if (!exists) {
            event.preventDefault();
            event.stopPropagation();
            
            prefilledCategoryName.value = text;
            isCategoryModalOpen.value = true;

            // Đóng danh sách và xóa text
            if (categoryMultiselectRef.value) {
                categoryMultiselectRef.value.close();
                categoryMultiselectRef.value.clearSearch();
            }
        }
    }
};

// Hàm mở Modal Danh mục bằng tay (nút "Thêm nhanh")
const openCategoryModalNormal = () => { 
    prefilledCategoryName.value = '';
    isCategoryModalOpen.value = true; 
};

// Cập nhật hàm xử lý sau khi tạo thành công Danh mục
// Cập nhật hàm xử lý sau khi tạo thành công Danh mục
const handleCategoryAdded = async (newCategoryData) => { 
    isCategoryModalOpen.value = false; 
    
    // 1. Chờ tải lại danh sách danh mục mới nhất từ DB
    await fetchInitialData(); 
    
    let idToSelect = null;

    // Trường hợp 1: Nếu Modal con có trả về ID hoặc Object
    if (newCategoryData) {
        idToSelect = typeof newCategoryData === 'object' ? newCategoryData.id : newCategoryData;
    }
    
    // Trường hợp 2: Nếu Modal con KHÔNG trả về gì, ta quét trong danh sách mới tải về
    // Tìm cái danh mục có tên khớp với tên mình vừa gõ (prefilledCategoryName)
    if (!idToSelect && prefilledCategoryName.value) {
        const newlyAdded = listCategories.value.find(
            (cat) => cat.tenDanhMuc.toLowerCase() === prefilledCategoryName.value.toLowerCase()
        );
        if (newlyAdded) {
            idToSelect = newlyAdded.id;
        }
    }

    // 2. Tự động gán ID tìm được vào Multiselect
    if (idToSelect) {
        formData.value.idDanhMuc = idToSelect;
    }
    
    // Xóa bộ nhớ đệm text
    prefilledCategoryName.value = '';
};

// ==========================================
// 2. KHỞI TẠO & TẢI DỮ LIỆU
// ==========================================
const fetchInitialData = async () => {
    try {
        const [resCat, resFoods] = await Promise.all([
            foodApi.getCategories(),
            foodApi.getFoods()
        ]);
        listCategories.value = resCat.data || [];
        existingFoods.value = resFoods.data?.content || resFoods.data || [];
    } catch (error) {
        console.error("Lỗi khởi tạo dữ liệu:", error);
    }
};

onMounted(fetchInitialData);

const loadDataByCategory = async () => {
    availableUnits.value = [];
    selectedDinhLuongIds.value = [];
    generatedVariants.value = [];
    errors.value.idDanhMuc = '';
    if (!formData.value.idDanhMuc) return;

    try {
        const res = await foodApi.getUnitTypesByCategory(formData.value.idDanhMuc);
        availableUnits.value = res.data || [];
    } catch (error) {
        console.error("Lỗi lấy định lượng:", error);
    }
};

watch(() => formData.value.idDanhMuc, (newVal, oldVal) => {
    if (newVal && newVal !== oldVal) loadDataByCategory();
    else if (!newVal) {
        availableUnits.value = [];
        selectedDinhLuongIds.value = [];
        generatedVariants.value = [];
    }
}); // Bỏ chữ {immediate: true} ở đây đi cho an toàn

// THÊM ĐOẠN NÀY VÀO DƯỚI ĐỂ HỨNG TÊN MÓN:
watch(() => props.initialName, (newVal) => {
    if (newVal) {
        formData.value.baseName = newVal;
    }
}, { immediate: true });

const toggleDinhLuong = (id) => {
    if (selectedDinhLuongIds.value.includes(id)) {
        selectedDinhLuongIds.value = selectedDinhLuongIds.value.filter(i => i !== id);
    } else {
        selectedDinhLuongIds.value.push(id);
    }
};

// ==========================================
// 3. LOGIC TẠO BIẾN THỂ & CHECK TRÙNG
// ==========================================
const handleGenerateVariants = () => {
    if (!formData.value.baseName.trim()) {
        errors.value.baseName = 'Vui lòng nhập tên sản phẩm gốc!';
        return;
    }
    if (selectedDinhLuongIds.value.length === 0) {
        return Swal.fire({ icon: 'warning', text: 'Vui lòng chọn ít nhất 1 định lượng!', confirmButtonColor: '#8B0000' });
    }

    const newVariants = [];
    const baseName = formData.value.baseName.trim();
    let duplicateCount = 0;

    availableUnits.value.forEach(unit => {
        unit.values?.forEach(val => {
            if (selectedDinhLuongIds.value.includes(val.id)) {
                const variantName = `${baseName} - ${val.giaTri} ${unit.tenDonVi}`;
                const isDuplicate = existingFoods.value.some(f =>
                    f.tenMon?.toLowerCase() === variantName.toLowerCase() && f.idDinhLuong === val.id
                );

                if (isDuplicate) {
                    duplicateCount++;
                    return;
                }

                const existingInTable = generatedVariants.value.find(v => v.idDinhLuong === val.id);
                newVariants.push({
                    tenMon: variantName,
                    giaVon: existingInTable ? existingInTable.giaVon : 0,
                    giaBan: existingInTable ? existingInTable.giaBan : 0,
                    tenHienThiDinhLuong: `${val.giaTri} ${unit.tenDonVi}`,
                    idDinhLuong: val.id
                });
            }
        });
    });

    if (duplicateCount > 0) {
        Swal.fire({
            icon: 'info',
            title: 'Lọc trùng lặp',
            text: `Đã bỏ qua ${duplicateCount} định lượng vì đã tồn tại trong hệ thống.`,
            confirmButtonColor: '#8B0000'
        });
    }
    generatedVariants.value = newVariants;
};

const openCategoryModal = () => { isCategoryModalOpen.value = true; };
const openUnitModal = () => { isUnitModalOpen.value = true; };

// ==========================================
// 4. LƯU DỮ LIỆU HÀNG LOẠT
// ==========================================
const handleSave = async () => {
    if (!formData.value.idDanhMuc || !formData.value.baseName || !formData.value.hinhAnh) {
        return Swal.fire({ icon: 'warning', title: 'Thiếu thông tin', text: 'Vui lòng nhập đủ Tên món, Danh mục và Ảnh!' });
    }
    if (generatedVariants.value.length === 0) {
        return Swal.fire({ icon: 'warning', text: 'Vui lòng chọn định lượng và nhấn "Tạo biến thể"!' });
    }

    console.log("Danh sách trước khi lưu:", generatedVariants.value); // In ra để xem

    const invalidPrice = generatedVariants.value.some(v => {
        const gia = Number(v.giaBan);
        console.log(`Đang check giá: ${gia}`); // Soi từng giá một
        return isNaN(gia) || gia <= 0;
    });

    // NẾU CÓ GIÁ <= 0, BẮT BUỘC DỪNG LẠI
    if (invalidPrice) {
        console.log("Phát hiện lỗi giá! Đang chặn lại...");
        return Swal.fire({
            icon: 'error',
            title: 'Lỗi giá bán',
            text: 'Vui lòng nhập giá bán lớn hơn 0 cho TẤT CẢ các món!',
            customClass: { container: 'swal2-container' } // Đảm bảo không bị đè
        });
    }

    const result = await Swal.fire({
        title: 'Xác nhận lưu?',
        text: `Tạo ${generatedVariants.value.length} món ăn mới.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#8B0000',
        confirmButtonText: 'Đồng ý'
    });

    if (result.isConfirmed) {
        Swal.fire({ title: 'Đang xử lý...', didOpen: () => Swal.showLoading() });
        try {
            const savePromises = generatedVariants.value.map(v => {
                return foodApi.createFood({
                    ...formData.value,
                    tenMon: v.tenMon,
                    giaVon: v.giaVon,
                    giaBan: v.giaBan,
                    idDinhLuong: v.idDinhLuong,
                    trangThai: 1
                });
            });
            await Promise.all(savePromises);
            await Swal.fire({ icon: 'success', title: 'Thành công!', timer: 1500, showConfirmButton: false });

            if (props.isModal) {
                emit('saved'); // Báo cho trang Set lẩu biết đã lưu xong để nó tự đóng Modal và tải lại dữ liệu
            } else {
                router.push({ name: 'foodManager', query: { tab: 'thucdon' } });
            }
        } catch (error) {
            Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Có lỗi xảy ra khi lưu dữ liệu.' });
        }
    }
};

const triggerFileInput = () => fileInputRef.value.click();
const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (e) => { formData.value.hinhAnh = e.target.result; errors.value.hinhAnh = ''; };
    reader.readAsDataURL(file);
    event.target.value = '';
};

const handleUnitAdded = async () => { isUnitModalOpen.value = false; await loadDataByCategory(); };
const openQuickAddUnitValueModal = (unit) => { selectedUnitForQuickAdd.value = unit; isQuickAddUnitValueOpen.value = true; };
const handleQuickAddUnitValueSuccess = async () => { isQuickAddUnitValueOpen.value = false; await loadDataByCategory(); };

const goBack = () => {
    if (props.isModal) {
        emit('cancel'); // Đóng modal nếu đang ở trong HotpotAdd
    } else {
        router.back();  // Lùi trang nếu đang đứng độc lập
    }
};
</script>

<template>
    <div class="main-content d-flex flex-column h-100">
        <div class="page-header">
            <div class="header-title">
                <h1>Thêm Món Ăn Mới</h1>
            </div>
            <button class="btn-back" @click="goBack"><i class="fas fa-arrow-left"></i> Quay lại</button>
        </div>

        <div class="content-scroll-wrapper" style="flex: 1; overflow-y: auto;">
            <div class="page-content" style="display: flex; gap: 20px; padding-bottom: 20px;">
                <div class="section-left" style="flex: 2;">
                    <div class="card mb-3" style="background-color: #fcfcfc; border: 1px solid #eee;">
                        <h3 class="text-danger"><i class="fas fa-filter"></i> Bước 1: Chọn Danh mục & Sản phẩm</h3>
                        <div class="form-container form-row-2">
                            <div class="form-group">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <label class="m-0">Danh mục <span class="required">*</span></label>

                                    <div class="d-flex align-items-center">
                                        <button class="btn-quick-add me-2" @click="openCategoryModal"
                                            title="Thêm danh mục mới">
                                            <i class="fas fa-plus-circle"></i> Thêm nhanh
                                        </button>
                                        <i class="fas fa-sync-alt cursor-pointer text-muted" @click="fetchCategories"
                                            title="Tải lại danh sách" style="font-size: 0.9rem;"></i>
                                    </div>
                                </div>
                                <Multiselect 
                                    ref="categoryMultiselectRef"
                                    v-model="formData.idDanhMuc" 
                                    :options="listCategories" 
                                    mode="single"
                                    valueProp="id" 
                                    label="tenDanhMuc" 
                                    placeholder="-- Bắt buộc chọn --" 
                                    :searchable="true"
                                    :canClear="true" 
                                    class="custom-multiselect-theme" 
                                    @search-change="handleCategorySearchChange"
                                    @keydown="handleCategoryKeydown"
                                >
                                    <template v-slot:noresults>
                                        <div style="padding: 5px 10px; color: #8B0000; font-size: 0.9rem;">
                                            Không có sẵn. Nhấn <kbd style="background: #eee; padding: 2px 5px; border-radius: 4px;">Enter ↵</kbd> để tạo nhanh "<b>{{ currentCategorySearchText }}</b>"
                                        </div>
                                    </template>
                                </Multiselect>
                            </div>
                            <div class="form-group" style="margin-top: 5.5px;">
                                <label>Tên sản phẩm gốc <span class="required">*</span></label>
                                <input v-model="formData.baseName" type="text" placeholder="VD: Nước ngọt Coca, Bò Mỹ..."
                                    :class="{ 'invalid-border': errors.baseName }">
                                <span class="error-message" v-if="errors.baseName">{{ errors.baseName }}</span>
                            </div>
                        </div>
                    </div>

                    <div class="card">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h3 class="m-0"><i class="fas fa-layer-group"></i> Bước 2: Chọn Định Lượng / Kích Cỡ</h3>
                            <button v-if="formData.idDanhMuc" class="btn-quick-add" @click="openUnitModal"
                                title="Tạo mới 1 đơn vị cho danh mục này">
                                <i class="fas fa-cog"></i> Cấu hình định lượng mới
                            </button>
                        </div>

                        <div v-if="!formData.idDanhMuc" class="text-muted text-center p-4">
                            Vui lòng chọn Danh mục ở Bước 1 trước.
                        </div>

                        <div v-else class="unit-selection-container">
                            <div v-if="availableUnits.length === 0" class="empty-units">
                                <i class="fas fa-box-open"></i> Không tìm thấy định lượng nào cho danh mục này.
                            </div>

                            <div v-for="unit in availableUnits" :key="unit.id" class="unit-group">
                                <div class="unit-header d-flex align-items-center mb-2">
                                    <span class="unit-name">{{ unit.tenDonVi }}</span>

                                    <button class="btn btn-sm btn-outline-danger ms-3 py-0 px-2 rounded-pill"
                                        style="font-size: 0.75rem;" @click="openQuickAddUnitValueModal(unit)">
                                        <i class="fas fa-plus"></i> Thêm giá trị
                                    </button>

                                    <span class="unit-desc text-muted ms-2" style="font-size: 0.8rem; font-weight: normal;">
                                        {{ unit.moTa }}
                                    </span>
                                </div>

                                <div class="unit-values">
                                    <button v-for="val in unit.values" :key="val.id" class="btn-option"
                                        :class="{ 'active': selectedDinhLuongIds.includes(val.id) }"
                                        @click="toggleDinhLuong(val.id)">
                                        {{ val.giaTri }}
                                        <i v-if="selectedDinhLuongIds.includes(val.id)"
                                            class="fas fa-check-circle ms-1"></i>
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
                                        <td class="fw-bold">{{ v.tenMon }}</td>
                                        <td>
                                            <span class="badge bg-light text-dark border">
                                                {{ v.tenHienThiDinhLuong }}
                                            </span>
                                        </td>
                                        <td>
                                            <div class="input-group input-group-sm" style="flex-direction: row !important;">
                                                <input v-model="v.giaBan" type="number" class="form-control"
                                                    style="width: 80% !important;" placeholder="0">
                                                <span class="input-group-text">đ</span>
                                            </div>
                                        </td>
                                        <td class="text-center">
                                            <button class="btn-clear-img text-danger"
                                                @click="generatedVariants.splice(idx, 1)">
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
                </div>
            </div>
        </div>

        <div class="page-footer-sticky"
            style="border-top: 1px solid #ddd; background: #fff; border-bottom-left-radius: 8px; border-bottom-right-radius: 8px; padding: 15px 20px; display: flex; justify-content: flex-end; align-items: center; width: 100%;">
            <button class="btn btn-secondary me-2" @click="goBack" style="min-width: 120px;">Hủy bỏ</button>
            <button class="btn btn-danger" @click="handleSave" style="min-width: 120px; background-color: #d32f2f;">
                Lưu hàng loạt
            </button>
        </div>

        <CategoryAddModal 
            :isOpen="isCategoryModalOpen" 
            :initialName="prefilledCategoryName"
            @close="isCategoryModalOpen = false"
            @refresh="handleCategoryAdded" 
        />

        <UnitAddScreen :isOpen="isUnitModalOpen" :categories="listCategories" @close="isUnitModalOpen = false"
            @refresh="handleUnitAdded" />

        <UnitUpdateScreen :isOpen="isQuickAddUnitValueOpen" :unit-item="selectedUnitForQuickAdd"
            :categories="listCategories" :isQuickAddMode="true" @close="isQuickAddUnitValueOpen = false"
            @refresh="handleQuickAddUnitValueSuccess" />
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

.variants-table {
    width: 100%;
    border-collapse: collapse;
}

.variants-table th,
.variants-table td {
    border: 1px solid #eee;
    padding: 10px;
    vertical-align: middle;
}

.variants-table th {
    background-color: #f8f9fa;
    font-weight: 600;
}

.btn-quick-add {
    background: none;
    border: none;
    color: #7b121c;
    font-size: 0.85rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 2px 5px;
    border-radius: 4px;
}

.btn-quick-add:hover {
    background-color: #fdf2f2;
    text-decoration: underline;
    color: #b01a2b;
}

.btn-quick-add i {
    font-size: 1rem;
}

.unit-group {
    margin-bottom: 20px;
    border-bottom: 1px dashed #eee;
    padding-bottom: 15px;
}

.unit-group:last-child {
    border-bottom: none;
}

.unit-header {
    margin-bottom: 10px;
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

/* Ghi đè CSS toàn diện cho Multiselect */
.custom-multiselect-theme {
    --ms-border-color: #7b121c;
    --ms-border-color-active: #7b121c;
    --ms-radius: 10px;
    --ms-ring-color: rgba(123, 18, 28, 0.1);
    --ms-placeholder-color: #999;
    --ms-option-bg-pointed: #fdf2f2;
    --ms-option-color-pointed: #7b121c;
    --ms-option-bg-selected: #7b121c;
    --ms-option-color-selected: #ffffff;
    --ms-option-bg-selected-pointed: #600000;
    min-height: 48px;
}

.custom-multiselect-theme :global(.multiselect-is-active) {
    box-shadow: 0 0 0 3px rgba(123, 18, 28, 0.15) !important;
    border-color: #7b121c !important;
}

.custom-multiselect-theme :global(.multiselect-search) {
    background-color: transparent;
}

.custom-multiselect-theme :global(.multiselect-caret) {
    mask-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 320 512'%3E%3Cpath d='M31.3 192h257.3c17.8 0 26.7 21.5 14.1 34.1L174.1 354.8c-7.8 7.8-20.5 7.8-28.3 0L17.2 226.1C4.6 213.5 13.5 192 31.3 192z'/%3E%3C/svg%3E");
    background-color: #7b121c !important;
}

.custom-multiselect-theme:focus {
    outline: none;
    border-color: #7b121c;
}

.card h3 {
    margin-bottom: 0;
}
</style>