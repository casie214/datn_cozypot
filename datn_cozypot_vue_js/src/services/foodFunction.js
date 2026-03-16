import { computed, nextTick, onMounted, ref, watch, shallowRef } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import * as XLSX from 'xlsx';
import axiosClient from './axiosClient';

const API_BASE = "/manage/food";

// ============================================================================
// 1. API SERVICES (CHUẨN RESTful)
// ============================================================================

export const foodApi = {
    getCategories: () => axiosClient.get(`${API_BASE}/category`),
    getCategoryById: (id) => axiosClient.get(`${API_BASE}/category/${id}`),
    createCategory: (data) => axiosClient.post(`${API_BASE}/category`, data),
    updateCategory: (id, data) => axiosClient.put(`${API_BASE}/category/${id}`, data),
    
    getUnits: () => axiosClient.get(`${API_BASE}/unit`),
    getUnitTypes: () => axiosClient.get(`${API_BASE}/unit-types`),
    createUnitType: (data) => axiosClient.post(`${API_BASE}/unit-types`, data),
    updateUnitType: (id, data) => axiosClient.put(`${API_BASE}/unit-types/${id}`, data),
    updateSingleUnit: (id, data) => axiosClient.put(`/manage/food/quantitative/${id}`, data),
    getUnitTypesByCategory: (id) => axiosClient.get(`${API_BASE}/unit-types/by-category/${id}`),

    getUnitsByCategory: (id) => axiosClient.get(`${API_BASE}/unit/by-category/${id}`),

    getFoods: () => axiosClient.get(`${API_BASE}`),
    getFoodById: (id) => axiosClient.get(`${API_BASE}/${id}`),
    createFood: (data) => axiosClient.post(`${API_BASE}`, data),
    updateFood: (id, data) => axiosClient.put(`${API_BASE}/${id}`, data),

    getHotpotTypes: () => axiosClient.get(`${API_BASE}/category/hotpotType`),
    createHotpotType: (data) => axiosClient.post(`${API_BASE}/category/hotpotType`, data),
    updateHotpotType: (id, data) => axiosClient.put(`${API_BASE}/category/hotpotType/${id}`, data),

    getHotpots: () => axiosClient.get(`${API_BASE}/hotpot`),
    getHotpotById: (id) => axiosClient.get(`${API_BASE}/hotpot/${id}`),
    createHotpot: (data) => axiosClient.post(`${API_BASE}/hotpot`, data),
    updateHotpot: (id, data) => axiosClient.put(`${API_BASE}/hotpot/${id}`, data),

    createUnit: (data) => axiosClient.post(`${API_BASE}/dinh-luong`, data),
    updateUnit: (id, data) => axiosClient.put(`${API_BASE}/dinh-luong/${id}`, data)
    
};

// ============================================================================
// 2. HELPER COMPONENTS (Phân trang, Slider Giá, Tabs)
// ============================================================================
export function usePriceFilter() {
    const globalMinPrice = ref(0);
    const globalMaxPrice = ref(1000000); 
    const selectedPriceRange = ref([0, 1000000]);

    const calculatePriceLimits = (dataList) => {
        if (!dataList || dataList.length === 0) {
            globalMinPrice.value = 0;
            globalMaxPrice.value = 0;
            selectedPriceRange.value = [0, 0];
            return;
        }

        const prices = dataList.map(item => item.giaBan || 0);
        const rawMin = Math.min(...prices);
        const rawMax = Math.max(...prices);

        // LẤY ĐÚNG GIÁ TRỊ THỰC TẾ TRONG DATA (Ví dụ: 399000 và 599000)
        // Dùng Math.floor và Math.ceil chia 1000 để đảm bảo số luôn tròn nghìn đồng
        globalMinPrice.value = Math.floor(rawMin / 1000) * 1000; 
        globalMaxPrice.value = Math.ceil(rawMax / 1000) * 1000; 

        // Gán lại giá trị mặc định cho 2 đầu thanh trượt
        selectedPriceRange.value = [globalMinPrice.value, globalMaxPrice.value];
    };

    const isPriceInRange = (item) => {
        const p = parseFloat(item.giaBan) || 0;
        return p >= selectedPriceRange.value[0] && p <= selectedPriceRange.value[1];
    };

    const resetPriceFilter = () => { selectedPriceRange.value = [globalMinPrice.value, globalMaxPrice.value]; };
    return { globalMinPrice, globalMaxPrice, selectedPriceRange, calculatePriceLimits, isPriceInRange, resetPriceFilter };
}

// ============================================================================
// 3. DANH MỤC (CATEGORY)
// ============================================================================
export function useCategoryManager() {
    const categoryData = ref([]);
    const isLoading = ref(false);
    const searchQuery = ref('');
    const statusSort = ref('default'); 
    const sortOption = ref('id_desc');
    const currentPage = ref(1);
    const itemsPerPage = ref(5);

    const filteredData = computed(() => {
        let result = [...categoryData.value];
        
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenDanhMuc && item.tenDanhMuc.toLowerCase().includes(query)) ||
                (item.maDanhMuc && item.maDanhMuc.toLowerCase().includes(query))
            );
        }
        
        return result.sort((a, b) => {
            if (statusSort.value !== 'default') {
                const statusA = Number(a.trangThai) === 1 ? 1 : 0;
                const statusB = Number(b.trangThai) === 1 ? 1 : 0;
                
                if (statusSort.value === 'active_first') {
                    if (statusA !== statusB) return statusB - statusA;
                } else if (statusSort.value === 'inactive_first') {
                    if (statusA !== statusB) return statusA - statusB;
                }
            }
            switch (sortOption.value) {
                case 'name_asc': return (a.tenDanhMuc || '').localeCompare(b.tenDanhMuc || '');
                case 'id_asc': return a.id - b.id;
                default: return b.id - a.id;
            }
        });
    });

    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        return filteredData.value.slice(start, start + itemsPerPage.value);
    });

    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value) || 1);
    const totalElements = computed(() => filteredData.value.length);
    
    const visiblePages = computed(() => {
        const total = totalPages.value, current = currentPage.value, delta = 2, range = [], rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i); l = i;
        }
        return rangeWithDots;
    });

    const changePage = (page) => { if (page >= 1 && page <= totalPages.value) currentPage.value = page; };
    watch([searchQuery, sortOption, statusSort], () => currentPage.value = 1);

    function getAllCategories() {
        isLoading.value = true;
        foodApi.getCategories().then(res => { categoryData.value = res.data; })
        .catch(console.error).finally(() => isLoading.value = false);
    }

    const isModalOpen = ref(false), isModalUpdateOpen = ref(false), selectedItem = ref(null);
    const openModal = (item = null) => { selectedItem.value = item; isModalUpdateOpen.value = true; };

    const handleToggleStatus = async (item) => {
        const newStatus = Number(item.trangThai) === 1 ? 0 : 1;
        const payload = {
            maDanhMuc: item.maDanhMuc,
            tenDanhMuc: item.tenDanhMuc,
            moTa: item.moTa || '',
            trangThai: newStatus
        };

        try {
            await foodApi.updateCategory(item.id, payload);
            item.trangThai = newStatus; 
            Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', text: 'Đã thay đổi trạng thái', timer: 1500, showConfirmButton: false, toast: true, position: 'top-end' });
        } catch (error) {
            console.error("Lỗi API Toggle Category:", error.response?.data);
            Swal.fire({ icon: 'error', title: 'Lỗi', text: error.response?.data?.message || 'Không thể cập nhật trạng thái!' });
        }
    };

    onMounted(() => getAllCategories());

    const exportToExcel = () => {
        if (filteredData.value.length === 0) {
            Swal.fire({ icon: 'warning', iconColor: '#7D161A', title: 'Trống', text: 'Không có dữ liệu nào phù hợp để xuất Excel!' });
            return;
        }

        const dataToExport = filteredData.value.map((item, index) => ({
            'STT': index + 1,
            'Mã Danh Mục': item.maDanhMuc || '',
            'Tên Danh Mục': item.tenDanhMuc || '',
            'Mô Tả': item.moTa || '',
            'Số Lượng Món': item.soLuongMon || 0,
            'Trạng Thái': Number(item.trangThai) === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh'
        }));

        const ws = XLSX.utils.json_to_sheet(dataToExport);
        const wscols = [ { wch: 5 }, { wch: 15 }, { wch: 30 }, { wch: 40 }, { wch: 15 }, { wch: 20 } ];
        ws['!cols'] = wscols;

        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "DanhSachDanhMuc");
        XLSX.writeFile(wb, "Danh_Sach_Danh_Muc.xlsx");
    };

    return {
        categoryData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllCategories, exportToExcel,
        paginatedData, searchQuery, totalElements, sortOption, statusSort, currentPage, totalPages, visiblePages, itemsPerPage, changePage
    };
}


// ============================================================================
// HỖ TRỢ HIỂN THỊ LỖI (TOAST & ERROR FIELD) CHO THÊM MỚI DANH MỤC
// ============================================================================
export function useCategoryAddModal(props, emit) {
    const formData = ref({ 
        tenDanhMuc: '', 
        moTa: '', 
        trangThai: 1,
        phanLoaiMayIn: 1, 
        apDungLoaiVat: 1,
        listIdDonVi: []
    });

    // State lưu lỗi của từng trường để bôi đỏ Input
    const formErrors = ref({
        tenDanhMuc: '',
        listIdDonVi: '',
        moTa: ''
    });

    // Danh sách lưu Category cũ để check trùng lặp tên
    const existingCategories = ref([]);

    watch(() => props.isOpen, async (val) => {
        if (val) {
            formData.value = { tenDanhMuc: '', moTa: '', trangThai: 1, phanLoaiMayIn: 1, apDungLoaiVat: 1, listIdDonVi: [] };
            formErrors.value = { tenDanhMuc: '', listIdDonVi: '', moTa: '' };
            
            // Lấy danh sách cũ để check trùng khi Modal mở lên
            try {
                const res = await foodApi.getCategories();
                existingCategories.value = res.data || [];
            } catch (e) { console.error(e); }
        }
    });

    // Hàm Validate Local theo Test Report
    const validateForm = () => {
        let isValid = true;
        formErrors.value = { tenDanhMuc: '', listIdDonVi: '', moTa: '' };

        const tenDM = (formData.value.tenDanhMuc || '').trim();
        const moTa = (formData.value.moTa || '').trim();
        const lstDonVi = formData.value.listIdDonVi || [];

        // 1. Validate Tên danh mục (Trống, Độ dài 5 - 100)
        if (!tenDM) {
            formErrors.value.tenDanhMuc = "Tên danh mục không được để trống";
            isValid = false;
        } else if (tenDM.length < 5) {
            formErrors.value.tenDanhMuc = "Tên danh mục phải chứa ít nhất 5 kí tự";
            isValid = false;
        } else if (tenDM.length > 100) {
            formErrors.value.tenDanhMuc = "Tên danh mục không được vượt quá 100 ký tự";
            isValid = false;
        } else {
            // Check trùng tên (Bỏ qua hoa thường)
            const isDuplicate = existingCategories.value.some(c => c.tenDanhMuc.toLowerCase() === tenDM.toLowerCase());
            if (isDuplicate) {
                formErrors.value.tenDanhMuc = "Danh mục đã tồn tại trong hệ thống";
                isValid = false;
            }
        }

        // 2. Validate Định lượng (Phải chọn ít nhất 1)
        if (lstDonVi.length === 0) {
            formErrors.value.listIdDonVi = "Vui lòng chọn ít nhất 1 Định lượng áp dụng";
            isValid = false;
        }

        // 3. Validate Mô tả (0 - 255 ký tự)
        if (moTa.length > 255) {
            formErrors.value.moTa = "Mô tả không được vượt quá 255 ký tự";
            isValid = false;
        }

        if (!isValid) {
            Swal.fire({
                toast: true, position: 'top-end', icon: 'error',
                title: 'Dữ liệu không hợp lệ',
                text: 'Vui lòng kiểm tra lại các trường báo đỏ',
                showConfirmButton: false, timer: 3000
            });
        } else {
            formData.value.tenDanhMuc = tenDM;
            formData.value.moTa = moTa;
        }

        return isValid;
    };

    const handleSave = () => {
        if (!validateForm()) return;

        Swal.fire({
            title: 'Xác nhận', text: 'Bạn có chắc chắn muốn thêm danh mục mới?', icon: 'question', iconColor: '#7D161A',
            showCancelButton: true, confirmButtonColor: '#7D161A', cancelButtonColor: '#d33', confirmButtonText: 'Lưu thay đổi',
            cancelButtonText: 'Hủy',
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    await foodApi.createCategory(formData.value);
                    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công!', timer: 1500, showConfirmButton: false });
                    emit('refresh');
                    setTimeout(() => emit('close'), 1000);
                } catch (error) { 
                    Swal.fire({ icon: 'error', title: 'Lỗi', text: error.response?.data?.message || 'Đã xảy ra lỗi khi thêm mới!' }); 
                }
            }
        });
    };

    return { formData, formErrors, handleSave, closeModal: () => emit('close') };
}

// ============================================================================
// HỖ TRỢ HIỂN THỊ LỖI (TOAST & ERROR FIELD) CHO CẬP NHẬT DANH MỤC
// ============================================================================
export function useCategoryPutModal(props, emit) {
    const formData = ref({ 
        id: null, maDanhMuc: '', tenDanhMuc: '', moTa: '', trangThai: 1,
        phanLoaiMayIn: 1, apDungLoaiVat: 1, listIdDonVi: [] 
    });

    const formErrors = ref({ tenDanhMuc: '', listIdDonVi: '', moTa: '' });
    const existingCategories = ref([]);

    watch(() => props.itemList, async (newItem) => {
        formErrors.value = { tenDanhMuc: '', listIdDonVi: '', moTa: '' };
        
        if (newItem && newItem.id) {
            formData.value = { 
                ...newItem, 
                phanLoaiMayIn: newItem.phanLoaiMayIn || 1, 
                apDungLoaiVat: newItem.apDungLoaiVat || 1,
                listIdDonVi: [] 
            };

            try {
                const [resUnits, resCats] = await Promise.all([
                    foodApi.getUnitTypesByCategory(newItem.id),
                    foodApi.getCategories()
                ]);
                
                const arrDonVi = resUnits.data || [];
                if (arrDonVi.length > 0) {
                    if (typeof arrDonVi[0] === 'object') formData.value.listIdDonVi = arrDonVi.map(item => item.id);
                    else formData.value.listIdDonVi = [...arrDonVi];
                }

                existingCategories.value = resCats.data || [];
            } catch (e) {
                console.error("Lỗi khởi tạo modal Cập nhật:", e);
            }
        }
    }, { immediate: true });

    const validateForm = () => {
        let isValid = true;
        formErrors.value = { tenDanhMuc: '', listIdDonVi: '', moTa: '' };

        const tenDM = (formData.value.tenDanhMuc || '').trim();
        const moTa = (formData.value.moTa || '').trim();
        const lstDonVi = formData.value.listIdDonVi || [];

        // 1. Validate Tên danh mục (Trống, Độ dài 5 - 100)
        if (!tenDM) {
            formErrors.value.tenDanhMuc = "Tên danh mục không được để trống";
            isValid = false;
        } else if (tenDM.length < 5) {
            formErrors.value.tenDanhMuc = "Tên danh mục phải chứa ít nhất 5 kí tự";
            isValid = false;
        } else if (tenDM.length > 100) {
            formErrors.value.tenDanhMuc = "Tên danh mục không được vượt quá 100 ký tự";
            isValid = false;
        } else {
            // Check trùng tên (Loại trừ ID của chính nó đang cập nhật)
            const currentId = formData.value.id;
            const isDuplicate = existingCategories.value.some(
                c => c.tenDanhMuc.toLowerCase() === tenDM.toLowerCase() && c.id !== currentId
            );
            if (isDuplicate) {
                formErrors.value.tenDanhMuc = "Đã tồn tại danh mục này";
                isValid = false;
            }
        }

        // 2. Validate Định lượng (Phải chọn ít nhất 1)
        if (lstDonVi.length === 0) {
            formErrors.value.listIdDonVi = "Vui lòng chọn ít nhất 1 Định lượng áp dụng";
            isValid = false;
        }

        // 3. Validate Mô tả (0 - 255 ký tự)
        if (moTa.length > 255) {
            formErrors.value.moTa = "Mô tả không được vượt quá 255 ký tự";
            isValid = false;
        }

        if (!isValid) {
            Swal.fire({
                toast: true, position: 'top-end', icon: 'error',
                title: 'Dữ liệu không hợp lệ',
                text: 'Vui lòng kiểm tra lại các trường báo đỏ',
                showConfirmButton: false, timer: 3000
            });
        } else {
            formData.value.tenDanhMuc = tenDM;
            formData.value.moTa = moTa;
        }

        return isValid;
    };

    const handleSave = () => {
        if (!validateForm()) return;
        
        Swal.fire({
            title: 'Cập nhật', text: 'Lưu các thay đổi này?', icon: 'question', iconColor: '#7D161A',
            showCancelButton: true, confirmButtonColor: '#8B0000', confirmButtonText: 'Lưu thay đổi', cancelButtonText: 'Hủy',
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    const payload = {
                        maDanhMuc: formData.value.maDanhMuc,
                        tenDanhMuc: formData.value.tenDanhMuc,
                        moTa: formData.value.moTa || '',
                        trangThai: formData.value.trangThai,
                        phanLoaiMayIn: formData.value.phanLoaiMayIn, 
                        apDungLoaiVat: formData.value.apDungLoaiVat, 
                        listIdDonVi: formData.value.listIdDonVi || []
                    };

                    await foodApi.updateCategory(formData.value.id, payload);
                    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Cập nhật thành công', timer: 1500, showConfirmButton: false });
                    
                    emit('refresh');
                    setTimeout(() => emit('close'), 1000);
                } catch (error) { 
                    Swal.fire({ icon: 'error', title: 'Lỗi', text: error.response?.data?.message || 'Cập nhật danh mục mới không thành công' }); 
                }
            }
        });
    };

    return { formData, formErrors, handleSave, closeModal: () => emit('close') };
}

// ============================================================================
// 4. LOẠI LẨU (HOTPOT TYPE)
// ============================================================================
export function useHotpotSetTypeManager() {
    const hotpotTypeData = ref([]);
    const isLoading = ref(false);
    const searchQuery = ref('');
    const statusFilter = ref('all');
    const sortOption = ref('id_desc');
    const currentPage = ref(1);
    const itemsPerPage = ref(5);

    const filteredData = computed(() => {
        let result = [...hotpotTypeData.value];
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => (item.tenLoaiSet && item.tenLoaiSet.toLowerCase().includes(query)) || (item.maLoaiSet && item.maLoaiSet.toLowerCase().includes(query)));
        }
        if (statusFilter.value !== 'all') {
            result = result.filter(item => item.trangThai === Number(statusFilter.value));
        }
        return result.sort((a, b) => {
            if (sortOption.value === 'name_asc') return (a.tenLoaiSet || '').localeCompare(b.tenLoaiSet || '');
            return sortOption.value === 'id_asc' ? a.id - b.id : b.id - a.id;
        });
    });

    const paginatedData = computed(() => filteredData.value.slice((currentPage.value - 1) * itemsPerPage.value, currentPage.value * itemsPerPage.value));
    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value) || 1);
    const totalElements = computed(() => filteredData.value.length);
    
    const visiblePages = computed(() => {
        const total = totalPages.value, current = currentPage.value, delta = 2, range = [], rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i); l = i;
        }
        return rangeWithDots;
    });

    const changePage = (page) => { if (page >= 1 && page <= totalPages.value) currentPage.value = page; };
    watch([searchQuery, statusFilter, sortOption], () => currentPage.value = 1);

    function getAllHotpotType() {
        isLoading.value = true;
        foodApi.getHotpotTypes().then(res => hotpotTypeData.value = res.data).finally(() => isLoading.value = false);
    }
    onMounted(() => getAllHotpotType());

    const isModalOpen = ref(false), isModalUpdateOpen = ref(false), selectedItem = ref(null);
    const openModal = (item = null) => { selectedItem.value = item; isModalUpdateOpen.value = true; };

    const handleToggleStatus = async(item) => {
        const newStatus = item.trangThai === 1 ? 0 : 1;
        try {
            await foodApi.updateHotpotType(item.id, { ...item, trangThai: newStatus });
            item.trangThai = newStatus;
        } catch (error) { Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Cập nhật trạng thái thất bại' }); }
    };

    const exportToExcel = () => {
        // 1. Kiểm tra có dữ liệu không
        if (filteredData.value.length === 0) {
            Swal.fire({ icon: 'warning', iconColor: '#7D161A', title: 'Trống', text: 'Không có dữ liệu để xuất!' });
            return;
        }

        // 2. Chuẩn bị dữ liệu để xuất (Map theo đúng tiêu đề bảng)
        const dataToExport = filteredData.value.map((item, index) => ({
            'STT': index + 1,
            'Mã Loại': item.maLoaiSet || '',
            'Tên Loại Set': item.tenLoaiSet || '',
            'Mô Tả': item.moTa || '',
            'Trạng Thái': Number(item.trangThai) === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh'
        }));

        // 3. Tạo bảng tính
        const ws = XLSX.utils.json_to_sheet(dataToExport);

        // Định dạng độ rộng cột (tùy chọn)
        ws['!cols'] = [
            { wch: 5 },  // STT
            { wch: 15 }, // Mã
            { wch: 30 }, // Tên
            { wch: 40 }, // Mô tả
            { wch: 20 }  // Trạng thái
        ];

        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "LoaiSetLau");

        // 4. Tải file về máy
        XLSX.writeFile(wb, "Danh_Sach_Loai_Set_Lau.xlsx");
    };



    return { 
        hotpotTypeData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllHotpotType, exportToExcel,
        paginatedData, searchQuery, statusFilter, sortOption, currentPage, totalPages, visiblePages, itemsPerPage, changePage, totalElements 
    };
}

export function useHotpotCategoryAddModal(props, emit) {
    const formData = ref({ tenLoaiSet: '', moTa: '', trangThai: 1 });
    watch(() => props.isOpen, (val) => { if (val) formData.value = { tenLoaiSet: '', moTa: '', trangThai: 1 }; });

    const handleSave = () => {
        // 🚨 ĐÃ BỎ QUA CHECK Ở ĐÂY VÌ ĐÃ CÓ HÀM validateForm LÀM VIỆC NÀY
        Swal.fire({
            title: 'Thêm mới?', icon: 'question', iconColor: '#7D161A', showCancelButton: true, confirmButtonColor: '#7D161A', confirmButtonText: 'Lưu thay đổi',
            cancelButtonText: 'Hủy'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    await foodApi.createHotpotType(formData.value);
                    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', timer: 1500, showConfirmButton: false });
                    emit('refresh'); setTimeout(() => emit('close'), 1000);
                } catch (e) { 
                    Swal.fire({ icon: 'error', title: 'Lỗi thêm mới!', text: e.response?.data?.message }); 
                }
            }
        });
    };
    return { formData, handleSave, closeModal: () => emit('close') };
}

export function useCategoryHotpotPutModal(props, emit) {
    const formData = ref({ id: null, maLoaiSet: '', tenLoaiSet: '', moTa: '', trangThai: 1 });
    watch(() => props.itemList, (newItem) => { if (newItem) formData.value = { ...newItem }; }, { immediate: true });

    const handleSave = () => {
        if (!formData.value.tenLoaiSet || formData.value.tenLoaiSet.length < 5) return Swal.fire({ icon: 'warning', iconColor: '#7D161A', text: 'Tên không hợp lệ' });
        Swal.fire({
            title: 'Cập nhật?', icon: 'question', iconColor: '#7D161A', showCancelButton: true, confirmButtonColor: '#7D161A', confirmButtonText: 'Lưu thay đổi',
    cancelButtonText: 'Hủy'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    await foodApi.updateHotpotType(formData.value.id, formData.value);
                    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Cập nhật thành công', timer: 1500, showConfirmButton: false });
                    emit('refresh'); setTimeout(() => emit('close'), 1000);
                } catch (e) { Swal.fire({ icon: 'error', title: 'Lỗi cập nhật!' }); }
            }
        });
    };
    return { formData, handleSave, closeModal: () => emit('close') };
}

// ============================================================================
// 5. MÓN ĂN (FOOD / DANH_MUC_CHI_TIET) - ĐÃ LÀM PHẲNG
// ============================================================================
export function useFoodManager() {
    const router = useRouter();
    const route = useRoute();
    const mockData = ref([]);
    const searchQuery = ref('');
    const sortOption = ref('newest'); 
    const statusFilter = ref('all');
    const categoryFilter = ref('');
    const listCategories = ref([]);

    const currentPage = ref(1);
    const itemsPerPage = ref(5);
    const isCategoryLocked = ref(false);

    const { selectedPriceRange, globalMinPrice, globalMaxPrice, calculatePriceLimits, isPriceInRange, resetPriceFilter } = usePriceFilter();

    const fetchInitialData = async () => {
        try {
            const [resFood, resCat] = await Promise.all([ foodApi.getFoods(), foodApi.getCategories() ]);
            
            const categories = resCat.data || [];
            listCategories.value = categories;

            mockData.value = (resFood.data || []).map(food => {
                const category = categories.find(c => c.id === food.idDanhMuc);
                return {
                    ...food,
                    maDanhMuc: category ? category.maDanhMuc : 'N/A',
                    tenDanhMuc: category ? category.tenDanhMuc : 'Chưa phân loại'
                };
            });

            calculatePriceLimits(mockData.value);
        } catch (e) { 
            console.error("Lỗi fetch dữ liệu:", e); 
        }
    }
    
    onMounted(async () => {
        await fetchInitialData();
        if (route.query.preRoot) {
            categoryFilter.value = route.query.preRoot;
            isCategoryLocked.value = route.query.locked === 'true';
        }
    });

    const filteredAndSortedData = computed(() => {
        let result = [...mockData.value];
        if (searchQuery.value) {
            const q = searchQuery.value.toLowerCase().trim();
            result = result.filter(i => (i.tenMon && i.tenMon.toLowerCase().includes(q)) || (i.maMon && i.maMon.toLowerCase().includes(q)));
        }
        if (statusFilter.value !== 'all') result = result.filter(i => i.trangThai === Number(statusFilter.value));
        if (categoryFilter.value) result = result.filter(i => i.idDanhMuc == categoryFilter.value);
        
        result = result.filter(item => isPriceInRange(item));

        return result.sort((a, b) => {
            if (sortOption.value === 'price_asc') return a.giaBan - b.giaBan;
            if (sortOption.value === 'price_desc') return b.giaBan - a.giaBan;
            if (sortOption.value === 'name_asc') return (a.tenMon || '').localeCompare(b.tenMon || '');
            return b.id - a.id; 
        });
    });

    const paginatedData = computed(() => filteredAndSortedData.value.slice((currentPage.value - 1) * itemsPerPage.value, currentPage.value * itemsPerPage.value));
    const totalPages = computed(() => Math.ceil(filteredAndSortedData.value.length / itemsPerPage.value) || 1);
    const totalElements = computed(() => filteredAndSortedData.value.length);
    
    const visiblePages = computed(() => {
        const total = totalPages.value, current = currentPage.value, delta = 1, range = [], rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i); l = i;
        }
        return rangeWithDots;
    });

    const goToPage = (page) => { if (page >= 1 && page <= totalPages.value) currentPage.value = page; };
    watch([searchQuery, sortOption, statusFilter, categoryFilter, selectedPriceRange], () => currentPage.value = 1);

    const handleToggleStatus = async(item) => {
        const newStatus = item.trangThai === 1 ? 0 : 1;
        
        const payload = {
            tenMon: item.tenMon,
            maMon: item.maMon,
            giaBan: item.giaBan,
            giaVon: item.giaVon || 0,
            moTa: item.moTa || '',
            hinhAnh: item.hinhAnh || '',
            idDanhMuc: item.idDanhMuc,
            idDinhLuong: item.idDinhLuong,
            trangThai: newStatus
        };

        try {
            await foodApi.updateFood(item.id, payload);

            item.trangThai = newStatus;
            
            Swal.fire({ 
                icon: 'success', 
                iconColor: '#7D161A',
                title: 'Thành công', 
                text: 'Đã thay đổi trạng thái', 
                timer: 1500, 
                showConfirmButton: false,
                toast: true,
                position: 'top-end'
            });

        } catch (error) { 
            console.error("Lỗi API Toggle Status:", error.response?.data);
            
            Swal.fire({ 
                icon: 'error', 
                title: 'Lỗi', 
                text: error.response?.data?.message || 'Cập nhật trạng thái thất bại' 
            }); 
        }
    };

    const exportToExcel = () => {
        const dataToExport = filteredAndSortedData.value.map((item, index) => ({
            'STT': index + 1,
            'Mã Món': item.maMon,
            'Tên Món Ăn': item.tenMon,
            'Định lượng': item.tenDinhLuong || '',
            'Giá Bán (VNĐ)': item.giaBan,
            'Danh Mục': item.tenDanhMuc || '',
            'Trạng Thái': item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh',
        }));
        const ws = XLSX.utils.json_to_sheet(dataToExport);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "DanhSachMonAn");
        XLSX.writeFile(wb, "Danh_Sach_Mon_An.xlsx");
    };

    function getAllFoods() {
        isLoading.value = true;
        foodApi.getFoods().then(res => { mockData.value = res.data; })
        .catch(console.error).finally(() => isLoading.value = false);
    }

    return { 
        mockData, paginatedData, currentPage, itemsPerPage, totalPages, totalElements, visiblePages, goToPage,
        searchQuery, sortOption, statusFilter, categoryFilter, listCategories, isCategoryLocked,
        globalMinPrice, globalMaxPrice, selectedPriceRange, getAllFoods,
        handleToggleStatus, exportToExcel,
        goToAdd: () => router.push({ name: 'addFood' }),
        goToEdit: (item) => router.push({ name: 'updateFood', params: { id: item.id } }),
        handleViewDetails: (item) => router.push({ name: 'viewFood', params: { id: item.id } }),
        clearFilters: () => { 
            searchQuery.value=''; statusFilter.value='all'; 
            if(!isCategoryLocked.value) categoryFilter.value=''; 
            resetPriceFilter(); 
        }
    };
}

export function useFoodForm(isEditMode = false) {
    const router = useRouter();
    const route = useRoute();
    const foodId = route.params.id;

    const isViewMode = computed(() => route.name === 'viewFood');

    const formData = ref({ tenMon: '', giaBan: 0, giaVon: 0, moTa: '', hinhAnh: '', idDanhMuc: '', idDinhLuong: '', trangThai: 1 });
    const errors = ref({});
    const listCategories = ref([]);
    const listUnits = ref([]);
    const fileInputRef = ref(null);

    const fetchFormData = async () => {
        try {
            const resCat = await foodApi.getCategories();
            listCategories.value = resCat.data;
            if ((isEditMode || isViewMode.value) && foodId) {
                const resFood = await foodApi.getFoodById(foodId);
                formData.value = { ...resFood.data, trangThai: resFood.data.trangThai || 1 };
                if (formData.value.idDanhMuc) fetchUnitsByCategory(formData.value.idDanhMuc);
            }
        } catch (e) { console.error(e); }
    };

    const fetchUnitsByCategory = async (catId) => {
        if(!catId) { listUnits.value = []; return; }
        try {
            const res = await foodApi.getUnitsByCategory(catId);
            listUnits.value = res.data;
        } catch (e) { console.error(e); }
    };

    watch(() => formData.value.idDanhMuc, (newVal) => {
        if (!isEditMode || newVal) fetchUnitsByCategory(newVal);
    });

    onMounted(() => fetchFormData());

    const triggerFileInput = () => { if(!isViewMode.value) fileInputRef.value.click(); };

    const resizeImage = (file, maxWidth = 800) => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                const img = new Image(); img.src = e.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    let w = img.width, h = img.height;
                    if (w > maxWidth) { h *= maxWidth / w; w = maxWidth; }
                    canvas.width = w; canvas.height = h;
                    canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                    resolve(canvas.toDataURL('image/jpeg', 0.8));
                };
            };
        });
    };

    const handleFileUpload = async (event) => {
        if (isViewMode.value) return;
        const file = event.target.files[0];
        if (!file || !file.type.match('image.*')) return Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Chỉ chấp nhận file ảnh!'});
        try {
            formData.value.hinhAnh = await resizeImage(file);
            errors.value.hinhAnh = '';
        } catch (e) { console.error(e); }
        event.target.value = '';
    };

    const removeImage = () => {
        if (isViewMode.value) return;
        Swal.fire({
            title: 'Xóa ảnh?', text: "Bạn muốn xóa ảnh này?", icon: 'warning', iconColor: '#7D161A',
            showCancelButton: true, confirmButtonColor: '#d33', confirmButtonText: 'Xóa'
        }).then((result) => { if (result.isConfirmed) formData.value.hinhAnh = ''; });
    };

    const validateForm = () => {
        errors.value = {}; let isValid = true;
        if (!formData.value.tenMon || formData.value.tenMon.length < 3) { errors.value.tenMon = 'Tên không hợp lệ'; isValid = false; }
        if (!formData.value.idDanhMuc) { errors.value.idDanhMuc = 'Chọn danh mục'; isValid = false; }
        if (!formData.value.idDinhLuong) { errors.value.idDinhLuong = 'Chọn định lượng'; isValid = false; }
        if (formData.value.giaBan <= 0) { errors.value.giaBan = 'Giá không hợp lệ'; isValid = false; }
        if (!formData.value.hinhAnh) { errors.value.hinhAnh = 'Chọn ảnh'; isValid = false; }
        return isValid;
    };

    const handleSave = async () => {
        if (!validateForm()) return Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Vui lòng kiểm tra form', toast: true, position: 'top-end', timer: 2000 });
        
        Swal.fire({
            title: 'Xác nhận', text: isEditMode ? 'Cập nhật món ăn này?' : 'Thêm mới món ăn này?', icon: 'question', iconColor: '#7D161A',
            showCancelButton: true, confirmButtonColor: '#7D161A', confirmButtonText: 'Lưu thay đổi',
    cancelButtonText: 'Hủy'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    if (isEditMode) await foodApi.updateFood(foodId, formData.value);
                    else await foodApi.createFood(formData.value);
                    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', timer: 1500, showConfirmButton: false });
                    setTimeout(() => router.back(), 1500);
                } catch (e) { Swal.fire({ icon: 'error', title: 'Lỗi lưu dữ liệu' }); }
            }
        });
    };

    return { 
        formData, errors, listCategories, listUnits, isViewMode,
        fileInputRef, triggerFileInput, handleFileUpload, removeImage, 
        handleUpdate: handleSave, handleSave, goBack: () => router.back() 
    };
}

// ============================================================================
// 7. SET LẨU (HOTPOT SET)
// ============================================================================
export function useHotpotManager() {
    const router = useRouter();
    const route = useRoute();
    const hotpotData = ref([]);
    const listLoaiSet = ref([]);
    const searchQuery = ref('');
    const typeFilter = ref('all');
    const statusFilter = ref('all');
    const sortOption = ref('newest');
    const currentPage = ref(1);
    const itemsPerPage = ref(5);
    const isTypeLocked = ref(false);

    const { selectedPriceRange, globalMinPrice, globalMaxPrice, calculatePriceLimits, isPriceInRange, resetPriceFilter } = usePriceFilter();

    const fetchInitialData = async () => {
        try {
            const [resHotpots, resTypes] = await Promise.all([ 
                foodApi.getHotpots(), 
                foodApi.getHotpotTypes() 
            ]);
            hotpotData.value = resHotpots.data;
            listLoaiSet.value = resTypes.data || []; // Đảm bảo không bị null
            calculatePriceLimits(hotpotData.value);
            
            // Log thử để kiểm tra dữ liệu thực tế từ API
            console.log("Dữ liệu Loại Set Lẩu:", listLoaiSet.value);
        } catch (e) { console.error(e); }
    }
    
    onMounted(async () => {
        await fetchInitialData();
        if (route.query.preType) {
            typeFilter.value = Number(route.query.preType) || route.query.preType;
            
            isTypeLocked.value = route.query.locked === 'true';
        }
    });

    const filteredAndSortedData = computed(() => {
        let result = [...hotpotData.value];
        if (searchQuery.value) {
            const q = searchQuery.value.toLowerCase().trim();
            result = result.filter(i => (i.tenSetLau || '').toLowerCase().includes(q) || (i.maSetLau || '').toLowerCase().includes(q));
        }
        
        // 1. CHUẨN HÓA TRẠNG THÁI (Chống lỗi undefined/null)
        if (statusFilter.value !== 'all') {
            const targetStatus = Number(statusFilter.value);
            result = result.filter(i => (Number(i.trangThai) === 1 ? 1 : 0) === targetStatus);
        }

        if (typeFilter.value && typeFilter.value !== 'all') {
            result = result.filter(i => i.idLoaiSet == typeFilter.value);
        }
        result = result.filter(item => isPriceInRange(item)); // Lọc giá

        return result.sort((a, b) => {
            if (sortOption.value === 'price_asc') return a.giaBan - b.giaBan;
            if (sortOption.value === 'price_desc') return b.giaBan - a.giaBan;
            if (sortOption.value === 'name_asc') return (a.tenSetLau || '').localeCompare(b.tenSetLau || '');
            return b.id - a.id; 
        });
    });

    const paginatedData = computed(() => filteredAndSortedData.value.slice((currentPage.value - 1) * itemsPerPage.value, currentPage.value * itemsPerPage.value));
    const totalPages = computed(() => Math.ceil(filteredAndSortedData.value.length / itemsPerPage.value) || 1);
    const totalElements = computed(() => filteredAndSortedData.value.length);
    
    const visiblePages = computed(() => {
        const total = totalPages.value, current = currentPage.value, delta = 1, range = [], rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i); l = i;
        }
        return rangeWithDots;
    });

    const goToPage = (page) => { if (page >= 1 && page <= totalPages.value) currentPage.value = page; };
    watch([searchQuery, typeFilter, statusFilter, sortOption, selectedPriceRange], () => currentPage.value = 1);

    // 2. SỬA LỖI ĐỔI TRẠNG THÁI (Gửi đúng Payload)
    const handleToggleStatus = async(item) => {
        const newStatus = Number(item.trangThai) === 1 ? 0 : 1;
        
        // Tạo Payload chuẩn xác cho Backend
        const payload = {
            tenSetLau: item.tenSetLau,
            maSetLau: item.maSetLau,
            idLoaiSet: item.idLoaiSet,
            moTaChiTiet: item.moTaChiTiet || '',
            moTa: item.moTa || '',
            giaBan: item.giaBan,
            hinhAnh: item.hinhAnh || '',
            trangThai: newStatus,
            // Cần list món con nếu Backend yêu cầu Update toàn phần, nếu không có thể truyền mảng rỗng
            chiTietMonAn: item.danhSachMon ? item.danhSachMon.map(i => ({ idMonAn: i.idMon, soLuong: i.soLuong })) : []
        };

        try {
            await foodApi.updateHotpot(item.id, payload);
            item.trangThai = newStatus;
            Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', text: 'Đã thay đổi trạng thái', timer: 1500, showConfirmButton: false, toast: true, position: 'top-end' });
        } catch (error) { 
            console.error("Lỗi Toggle Status Hotpot:", error.response?.data);
            Swal.fire({ icon: 'error', title: 'Lỗi', text: error.response?.data?.message || 'Cập nhật trạng thái thất bại' }); 
        }
    };

    const exportToExcel = () => {
        if (filteredAndSortedData.value.length === 0) return Swal.fire('Trống', 'Không có dữ liệu', 'warning');
        
        const dataToExport = filteredAndSortedData.value.map((item, index) => ({
            'STT': index + 1,
            'Mã Set': item.maSetLau || '',
            'Tên Set Lẩu': item.tenSetLau || '',
            'Giá Bán (VNĐ)': item.giaBan || 0,
            'Loại Lẩu': item.tenLoaiSet || '',
            'Trạng Thái': Number(item.trangThai) === 1 ? 'Kinh doanh' : 'Ngưng'
        }));
        const ws = XLSX.utils.json_to_sheet(dataToExport);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "SetLau");
        XLSX.writeFile(wb, "Danh_Sach_Set_Lau.xlsx");
    };

    function getAllHotpot() {
        isLoading.value = true;
        foodApi.getHotpots().then(res => { hotpotData.value = res.data; })
        .catch(console.error).finally(() => isLoading.value = false);
    }

    const uniqueTypes = computed(() => {
        // Nếu API trả về listLoaiSet có dạng [{id, tenLoaiSet, ...}]
        // Chúng ta map nó về [{id, name}] để khớp với label="name" trong template
        return listLoaiSet.value.map(type => ({
            id: type.id,
            name: type.tenLoaiSet || type.name // Ưu tiên tenLoaiSet nếu có
        }));
    });

    return { 
        hotpotData, paginatedData, listLoaiSet, searchQuery, typeFilter, statusFilter, sortOption, 
        currentPage, totalPages, totalElements, visiblePages, goToPage, isTypeLocked,
        globalMinPrice, globalMaxPrice, selectedPriceRange, itemsPerPage, getAllHotpot, uniqueTypes,
        handleToggleStatus, exportToExcel,
        goToAdd: () => router.push({ name: 'addHotpotSet' }),
        goToEdit: (item) => router.push({ name: 'updateHotpotSet', params: { id: item.id } }),
        handleViewDetails: (item) => router.push({ name: 'viewHotpotSet', params: { id: item.id } }),
        clearFilters: () => { 
            searchQuery.value=''; statusFilter.value='all'; sortOption.value='newest';
            if(!isTypeLocked.value) typeFilter.value='all'; 
            resetPriceFilter(); 
        }
    };
}

export function useHotpotForm(isEditMode = false) {
    const router = useRouter();
    const route = useRoute();
    const hotpotId = route.params.id;
    const isViewMode = computed(() => route.name === 'viewHotpotSet');

    const formData = ref({ tenSetLau: '', idLoaiSet: '', moTaChiTiet: '', giaBan: 0, hinhAnh: '', moTa: '', trangThai: 1 });
    const selectedIngredients = ref([]);
    const errors = ref({});
    const listLoaiSet = ref([]);
    const listFoods = ref([]); 
    const searchQuery = ref('');

    const fetchInitialData = async () => {
        try {
            const [resTypes, resFoods] = await Promise.all([ foodApi.getHotpotTypes(), foodApi.getFoods() ]);
            listLoaiSet.value = resTypes.data;
            listFoods.value = [...resFoods.data.filter(f => Number(f.trangThai) === 1)];

            if ((isEditMode || isViewMode.value) && hotpotId) {
                const resHotpot = await foodApi.getHotpotById(hotpotId);
                const data = resHotpot.data;
                formData.value = { ...data, trangThai: data.trangThai !== undefined ? data.trangThai : 1 };
                
                if (data.danhSachMon || data.chiTietMonAn) {
                    const listChiTiet = data.danhSachMon || data.chiTietMonAn;
                    
                    selectedIngredients.value = listChiTiet.map(item => ({
                        idMonAn: item.idMon || item.idMonAn, // Quét 2 trường hợp tên biến
                        tenMon: item.tenMon || item.tenMonAn,
                        hinhAnh: item.hinhAnhMon || item.hinhAnh,
                        soLuong: item.soLuong,
                        
                        // SỬA CHÍNH LÀ Ở ĐÂY: Quét tìm giá bán
                        giaBan: item.giaBan || item.giaBanLe || 0, 
                        
                        dinhLuong: item.dinhLuong || item.tenDinhLuong || ''
                    }));
                }
            }
        } catch (e) { console.error(e); }
    };
    onMounted(() => fetchInitialData());

    const filteredFoodList = computed(() => {
        if (!searchQuery.value) return listFoods.value;
        const q = searchQuery.value.toLowerCase().trim();
        return listFoods.value.filter(f => (f.tenMon || '').toLowerCase().includes(q));
    });

    const totalComponentsPrice = computed(() => selectedIngredients.value.reduce((sum, item) => sum + ((item.giaBan||0) * item.soLuong), 0));

    const addIngredient = (food) => {
        if (isViewMode.value) return;
        const exist = selectedIngredients.value.find(i => i.idMonAn === food.id);
        if (exist) exist.soLuong++;
        else selectedIngredients.value.push({ 
            idMonAn: food.id, 
            tenMon: food.tenMon, 
            hinhAnh: food.hinhAnh, 
            soLuong: 1, 
            giaBan: food.giaBan, 
            dinhLuong: food.tenDinhLuong 
        });
        errors.value.ingredients = '';
    };

    const removeIngredient = (index) => {
        if (isViewMode.value) return;
        selectedIngredients.value.splice(index, 1);
    };

    const resizeImage = (file, maxWidth = 800) => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                const img = new Image(); img.src = e.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    let w = img.width, h = img.height;
                    if (w > maxWidth) { h *= maxWidth / w; w = maxWidth; }
                    canvas.width = w; canvas.height = h;
                    canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                    resolve(canvas.toDataURL('image/jpeg', 0.8));
                };
            };
        });
    };

    const handleFileUpload = async (event) => {
        if (isViewMode.value) return;
        const file = event.target.files[0];
        if (!file || !file.type.match('image.*')) return Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Chỉ chấp nhận file ảnh!'});
        try {
            formData.value.hinhAnh = await resizeImage(file);
            errors.value.hinhAnh = '';
        } catch (e) { console.error(e); }
        event.target.value = '';
    };

    const validateForm = () => {
        errors.value = {}; 
        let isValid = true;

        const name = (formData.value.tenSetLau || '').trim();
        const norm = (formData.value.moTaChiTiet || '').trim(); // Định mức
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

        // 2. Validate Loại Set (Combobox)
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
            errors.value.moTaChiTiet = 'Vui lòng nhập định mức (VD: Cho 4-5 người)';
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
            errors.value.hinhAnh = 'Vui lòng tải ảnh đại diện cho set lẩu';
            isValid = false;
        }

        // 7. Validate Thành phần (Ít nhất 1 món)
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
                text: 'Vui lòng kiểm tra lại các trường báo đỏ',
                showConfirmButton: false,
                timer: 3000
            });
        } else {
            // Gán lại dữ liệu đã trim để gửi API sạch đẹp
            formData.value.tenSetLau = name;
            formData.value.moTaChiTiet = norm;
            formData.value.moTa = desc;
        }

        return isValid;
    };

    const handleSave = async () => {
        if (!validateForm()) return;

        Swal.fire({
            title: 'Xác nhận lưu?',
            text: isEditMode ? 'Lưu các thay đổi cho set lẩu này?' : 'Thêm set lẩu mới vào hệ thống?',
            icon: 'question',
            iconColor: '#7D161A',
            showCancelButton: true,
            confirmButtonColor: '#7D161A',
            confirmButtonText: 'Đồng ý',
            cancelButtonText: 'Hủy'
        }).then(async (result) => {
            if (result.isConfirmed) {
                Swal.fire({ title: 'Đang xử lý...', didOpen: () => Swal.showLoading() });
                try {
                    const payload = { 
                        ...formData.value, 
                        chiTietMonAn: selectedIngredients.value.map(i => ({ idMonAn: i.idMonAn, soLuong: i.soLuong })) 
                    };
                    if (isEditMode) await foodApi.updateHotpot(hotpotId, payload);
                    else await foodApi.createHotpot(payload);
                    
                    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', timer: 1500, showConfirmButton: false });
                    setTimeout(() => router.back(), 1500);
                } catch (error) { 
                    Swal.fire({ icon: 'error', title: 'Lỗi', text: error.response?.data?.message || 'Không thể lưu dữ liệu' }); 
                }
            }
        });
    };

    return { 
        formData, selectedIngredients, listLoaiSet, filteredFoodList, searchQuery, errors, totalComponentsPrice, isViewMode, fetchInitialData,
        addIngredient, removeIngredient, handleFileUpload, handleUpdate: handleSave, handleSave, goBack: () => router.back() 
    };
}



export function useUnitManager() {
    const unitData = ref([]); // Danh sách DonViResponse từ Backend
    const isLoading = ref(false);
    const searchQuery = ref('');
    const sortOption = ref('id_desc');
    const currentPage = ref(1);
    const itemsPerPage = ref(8);
    const listCategories = ref([]);
    const categoryFilter = ref(null);

    const fetchAllData = async () => {
        isLoading.value = true;
        try {
            // Gọi đồng thời lấy danh sách Đơn vị (mới) và Danh mục
            const [resUnits, resCats] = await Promise.all([
                foodApi.getUnitTypes(), // Endpoint: /api/manage/food/unit-types
                foodApi.getCategories()
            ]);
            
            unitData.value = resUnits.data || [];
            listCategories.value = resCats.data || [];
            return resUnits.data; 
        } catch (e) {
            console.error("Lỗi tải dữ liệu:", e);
            return [];
        } finally {
            isLoading.value = false;
        }
    };

    // LOGIC LỌC VÀ TÌM KIẾM MỚI
    const filteredData = computed(() => {
        let result = [...unitData.value];

        // 1. Lọc theo Danh mục (Quan hệ N-N ở cấp Đơn vị)
        if (categoryFilter.value) {
            const selectedId = Number(categoryFilter.value);
            result = result.filter(unit => 
                unit.listIdDanhMuc && unit.listIdDanhMuc.includes(selectedId)
            );
        }

        // 2. Tìm kiếm (Tìm trong tên Đơn vị HOẶC các giá trị con bên trong)
        if (searchQuery.value) {
            const q = searchQuery.value.toLowerCase().trim();
            result = result.filter(unit => {
                const matchUnitName = unit.tenDonVi?.toLowerCase().includes(q);
                const matchChildValues = unit.values?.some(v => v.giaTri.toLowerCase().includes(q));
                return matchUnitName || matchChildValues;
            });
        }

        // 3. Sắp xếp
        switch (sortOption.value) {
            case 'name_asc':
                result.sort((a, b) => a.tenDonVi.localeCompare(b.tenDonVi, 'vi'));
                break;
            case 'id_asc':
                result.sort((a, b) => a.id - b.id);
                break;
            default: // id_desc
                result.sort((a, b) => b.id - a.id);
        }

        return result;
    });

    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        return filteredData.value.slice(start, start + itemsPerPage.value);
    });

    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value) || 1);
    const totalElements = computed(() => filteredData.value.length);

    const exportToExcel = () => {
        if (filteredData.value.length === 0) return Swal.fire('Thông báo', 'Không có dữ liệu để xuất', 'warning');
        
        const data = filteredData.value.map((unit, index) => ({
            'STT': index + 1,
            'Đơn vị tính': unit.tenDonVi,
            'Các kích cỡ/giá trị': unit.values?.map(v => v.giaTri).join(', '),
            'Mô tả': unit.moTa || '',
            'Danh mục áp dụng': unit.listIdDanhMuc?.map(id => 
                listCategories.value.find(c => c.id === id)?.tenDanhMuc
            ).join(', ')
        }));

        const ws = XLSX.utils.json_to_sheet(data);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "DonViDinhLuong");
        XLSX.writeFile(wb, "Quan_Ly_Don_Vi_Tinh.xlsx");
    };

    onMounted(() => fetchAllData());

    return {
        unitData, isLoading, searchQuery, sortOption, currentPage, itemsPerPage, categoryFilter,
        totalElements, totalPages, paginatedData, fetchAllData, exportToExcel, listCategories
    };
}