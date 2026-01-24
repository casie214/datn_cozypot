import { computed, nextTick, onMounted, ref, shallowRef, watch } from 'vue';

import MenuFood from '../pages/admin/food/screens/FoodManageGeneral.vue';
import MenuHotpot from '../pages/admin/food/screens/FoodHotPotSetGeneral.vue';
import FoodDetailManageGeneral from '../pages/admin/food/screens/FoodDetailManageGeneral.vue';
import axios from 'axios';
import CategoryGeneral from '../pages/admin/category/screens/CategoryGeneral.vue';
import CategoryDetailGeneral from '../pages/admin/category/screens/CategoryDetailGeneral.vue';
import CategoryHotpotGeneral from '../pages/admin/category/screens/CategoryHotpotGeneral.vue';
import { useRoute, useRouter } from 'vue-router';
const API_BASE_EMPLOYEE = "http://localhost:8080/api/manage";

export function getAllFoodGeneral() {
    return axios.get(`${API_BASE_EMPLOYEE}/food`);
}

export function getFoodById(id) {
    return axios.get(`${API_BASE_EMPLOYEE}/food/${id}`);
};

export function getFoodGeneralModalById(id) {
    return axios.get(`${API_BASE_EMPLOYEE}/food/modal/${id}`);
}

export function getAllHotpotGeneral() {
    return axios.get(`${API_BASE_EMPLOYEE}/food/hotpotGeneral`);
}

export function getHotpotById(id) {
    return axios.get(`${API_BASE_EMPLOYEE}/food/hotpotGeneral/${id}`);
}

export function getAllFoodDetail() {
    return axios.get(`${API_BASE_EMPLOYEE}/food/foodDetail`);
}

export function getFoodDetailById(id) {
    return axios.get(`${API_BASE_EMPLOYEE}/food/foodDetail/${id}`);
}

export function getAllCategory() {
    return axios.get(`${API_BASE_EMPLOYEE}/food/category`);
}

export function getAllCategoryDetail() {
    return axios.get(`${API_BASE_EMPLOYEE}/food/category/detail`);
}

export function getAllCategoryHotpot() {
    return axios.get(`${API_BASE_EMPLOYEE}/food/category/hotpotType`);
}

export function postNewFood(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food`, data);
}

export function putNewFood(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/${id}`, data);
}

export function putNewFoodDetail(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/foodDetail/${id}`, data);
}

export function postNewHotpot(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/hotpotGeneral`, data);
}

export function putNewCategory(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/category/${id}`, data);
}

export function postNewFoodDetail(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/foodDetail`, data);
}
export function putNewHotpot(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/hotpotGeneral/${id}`, data);
}
export function postNewCategory(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/category`, data);
}

export function putNewHotpotCategory(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/category/hotpotType/${id}`, data);
}

export function putCategoryDetail(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/category/detail/${id}`, data);
}

export function postCategoryDetail(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/category/detail`, data);
}

export function postNewCategoryDetail(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/category/detail`, data);
}

export function postNewHotpotCategory(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/category/hotpotType`, data);
}



//Trang chính food manager
export function useTabManager() {
    const tabs = {
        'thucdon': MenuFood,
        'setlau': MenuHotpot,
        'chitietTD': FoodDetailManageGeneral
    };

    const currentTabName = ref('thucdon');
    const currentComponent = shallowRef(MenuFood);

    const changeTab = (tabName) => {
        currentTabName.value = tabName;
        currentComponent.value = tabs[tabName];
    };

    return {
        currentTabName,
        currentComponent,
        changeTab
    };
}

export function useFoodDetailManager() {
    const detailData = ref([]);

    const searchQuery = ref('');
    const currentPage = ref(1);
    const itemsPerPage = ref(5);
    const sortOption = ref('id_asc');
    const statusFilter = ref('all'); 

    const { 
        selectedPriceRange, 
        globalMinPrice, 
        globalMaxPrice, 
        calculatePriceLimits, 
        resetPriceFilter 
    } = usePriceFilter();

    const handleMinChange = () => {
  if (selectedPriceRange.value[0] < globalMinPrice.value) {
    selectedPriceRange.value[0] = globalMinPrice.value;
  }
  if (selectedPriceRange.value[0] > selectedPriceRange.value[1]) {
    selectedPriceRange.value[0] = selectedPriceRange.value[1];
  }
};

const handleMaxChange = () => {
  if (selectedPriceRange.value[1] > globalMaxPrice.value) {
    selectedPriceRange.value[1] = globalMaxPrice.value;
  }
  if (selectedPriceRange.value[1] < selectedPriceRange.value[0]) {
    selectedPriceRange.value[1] = selectedPriceRange.value[0];
  }
};
    const filteredData = computed(() => {
        let result = [...detailData.value];

        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenChiTietMonAn || '').toLowerCase().includes(query) ||
                (item.maChiTietMonAn || '').toLowerCase().includes(query)
            );
        }

        if (statusFilter.value !== 'all') {
            const statusValue = Number(statusFilter.value); 
            result = result.filter(item => item.trangThai === statusValue);
        }

        if (selectedPriceRange.value && selectedPriceRange.value.length === 2) {
            const [min, max] = selectedPriceRange.value;
            result = result.filter(item => {
                const price = parseFloat(item.giaBan) || 0;
                return price >= min && price <= max;
            });
        }

        return result.sort((a, b) => {
            const priceA = parseFloat(a.giaBan) || 0;
            const priceB = parseFloat(b.giaBan) || 0;
            const nameA = (a.tenChiTietMonAn || '').toLowerCase();
            const nameB = (b.tenChiTietMonAn || '').toLowerCase();

            switch (sortOption.value) {
                case 'price_asc': return priceA - priceB;
                case 'price_desc': return priceB - priceA;
                case 'name_asc': return nameA.localeCompare(nameB);
                case 'id_desc': return b.id - a.id;
                case 'id_asc': default: return a.id - b.id;
            }
        });
    });

    watch([searchQuery, statusFilter, sortOption, selectedPriceRange], () => {
        currentPage.value = 1;
    });

    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value) || 1);
    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        const end = start + itemsPerPage.value;
        return filteredData.value.slice(start, end);
    });

    const visiblePages = computed(() => {
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 2;
        const range = [], rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    const changePage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    function getAllFoodDetails() {
        getAllFoodDetail()
            .then(async res => {
                detailData.value = res.data;
                calculatePriceLimits(detailData.value);
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => { getAllFoodDetails(); });

    const isModalOpen = ref(false);
    const selectedDetail = ref(null);
    const isAddModalOpen = ref(false);

    const openEditModal = (item) => { selectedDetail.value = item; isModalOpen.value = true; };
    const handleSaveData = () => { isModalOpen.value = false; };

    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThai;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThai = newStatus;
        try {
            const payload = {...item, trangThai: newStatus };
            if (item.monAnDiKem && item.monAnDiKem.id) {
                payload.idMonAnDiKem = item.monAnDiKem.id;
            }
            await putNewFoodDetail(item.id, payload);
        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };

    const clearFilters = () => {
        searchQuery.value = '';
        statusFilter.value = 'all';
        sortOption.value = 'id_asc';
        resetPriceFilter();
    };

    return {
        detailData, searchQuery, paginatedData, currentPage, totalPages, visiblePages, itemsPerPage, changePage,
        isModalOpen, isAddModalOpen, selectedDetail, getAllFoodDetails, openEditModal, handleSaveData, handleToggleStatus,
        sortOption, statusFilter,
        
        selectedPriceRange, globalMinPrice, globalMaxPrice, clearFilters
    };
}

export function usePriceFilter() {
    const globalMinPrice = ref(0);
    const globalMaxPrice = ref(1000000); // Mặc định
    const selectedPriceRange = ref([0, 1000000]);

    const calculatePriceLimits = (dataList) => {
        if (!dataList || dataList.length === 0) return;
        let min = Infinity;
        let max = -Infinity;

        dataList.forEach(item => {
            const pMin = item.giaThapNhat > 0 ? item.giaThapNhat : item.giaBan;
            const pMax = item.giaCaoNhat > 0 ? item.giaCaoNhat : item.giaBan;
            if (pMin < min) min = pMin;
            if (pMax > max) max = pMax;
        });

        if (min === Infinity) min = 0;
        if (max === -Infinity) max = 0;

        // Làm tròn cho đẹp (xuống 0, lên hàng chục nghìn)
        globalMinPrice.value = Math.floor(min / 10000) * 10000;
        globalMaxPrice.value = Math.ceil(max / 10000) * 10000 + 50000; // Cộng thêm chút dư
        selectedPriceRange.value = [globalMinPrice.value, globalMaxPrice.value];
    };

    const isPriceInRange = (item) => {
        const pMin = item.giaThapNhat > 0 ? item.giaThapNhat : item.giaBan;
        const pMax = item.giaCaoNhat > 0 ? item.giaCaoNhat : item.giaBan;
        return pMax >= selectedPriceRange.value[0] && pMin <= selectedPriceRange.value[1];
    };

    const resetPriceFilter = () => {
        selectedPriceRange.value = [globalMinPrice.value, globalMaxPrice.value];
    };

    return { globalMinPrice, globalMaxPrice, selectedPriceRange, calculatePriceLimits, isPriceInRange, resetPriceFilter };
}

// 2. LOGIC QUẢN LÝ MÓN ĂN (CHÍNH)
export function useFoodManager() {
    const router = useRouter();
    const mockData = ref([]);
    const activeTab = ref('thucdon');
    const isModalOpen = ref(false);
    const selectedItem = ref(null);
    const isAddFoodModalOpen = ref(false);

    // --- Filter & Sort ---
    const searchQuery = ref('');
    const sortOption = ref('newest'); 
    const statusFilter = ref('all');
    
    // --- Pagination ---
    const currentPage = ref(1);
    const itemsPerPage = 5;

    // --- [MỚI] BIẾN CHO LỌC DANH MỤC ---
    const isCategoryFilterOpen = ref(false); // Đóng/Mở Modal
    const listRootCategories = ref([]);      // Danh sách gốc
    const listSubCategories = ref([]);       // Danh sách chi tiết
    const selectedRootCate = ref('');        // ID danh mục gốc đang chọn
    const selectedSubCate = ref('');         // ID danh mục con đang chọn

    // Sử dụng logic giá
    const { 
        globalMinPrice, globalMaxPrice, selectedPriceRange, 
        calculatePriceLimits, isPriceInRange, resetPriceFilter 
    } = usePriceFilter();

    // --- [MỚI] HÀM LẤY DANH MỤC TỪ API ---
    const fetchCategories = async () => {
        try {
            // Thay bằng hàm gọi API thực tế của bạn
            if (typeof getAllCategory === 'function') {
                const resRoot = await getAllCategory();
                listRootCategories.value = resRoot.data;
            }
            if (typeof getAllCategoryDetail === 'function') {
                const resSub = await getAllCategoryDetail();
                listSubCategories.value = resSub.data;
            }
        } catch (e) {
            console.error("Lỗi tải danh mục:", e);
        }
    };

    // --- [MỚI] Computed: Lọc danh mục con theo danh mục cha ---
    const availableSubCategories = computed(() => {
        if (!selectedRootCate.value) return [];
        // Lọc listSubCategories có idDanhMuc trùng với selectedRootCate
        return listSubCategories.value.filter(sub => sub.idDanhMuc == selectedRootCate.value);
    });

    // --- COMPUTED: LỌC & SẮP XẾP DỮ LIỆU ---
    const filteredAndSortedData = computed(() => {
        let result = [...mockData.value];

        // 1. Search
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenMonAn && item.tenMonAn.toLowerCase().includes(query)) ||
                (item.maMonAn && item.maMonAn.toLowerCase().includes(query))
            );
        }

        // 2. Status
        if (statusFilter.value !== 'all') {
            const statusValue = Number(statusFilter.value);
            result = result.filter(item => item.trangThaiKinhDoanh === statusValue);
        }

        // 3. Price Slider
        result = result.filter(item => isPriceInRange(item));

        // 4. [MỚI] Filter Category (Root)
        if (selectedRootCate.value) {
            // Tìm object danh mục gốc để lấy tên (vì bảng món ăn lưu tên danh mục)
            const rootCat = listRootCategories.value.find(c => c.id == selectedRootCate.value);
            if (rootCat) {
                // So sánh tên danh mục (hoặc ID nếu backend trả về ID trong list món ăn)
                result = result.filter(item => item.tenDanhMuc === rootCat.tenDanhMuc);
            }
        }

        // 5. [MỚI] Filter Category (Sub)
        if (selectedSubCate.value) {
             const subCat = listSubCategories.value.find(c => c.id == selectedSubCate.value);
             if (subCat) {
                 result = result.filter(item => item.tenDanhMucChiTiet === subCat.tenDanhMucChiTiet);
             }
        }

        // 6. Sort
        const getSafePrice = (item) => {
            let min = item.giaThapNhat; 
            let base = item.giaBan;
            if (min === null || min === undefined) min = 0;
            if (base === null || base === undefined) base = 0;
            const valMin = parseFloat(min);
            const valBase = parseFloat(base);
            return (valMin > 0 ? valMin : valBase) || 0;
        };

        switch (sortOption.value) {
            case 'price_asc': result.sort((a, b) => getSafePrice(a) - getSafePrice(b)); break;
            case 'price_desc': result.sort((a, b) => getSafePrice(b) - getSafePrice(a)); break;
            case 'name_asc': 
                result.sort((a, b) => {
                    const nameA = a.tenMonAn ? String(a.tenMonAn).toLowerCase() : '';
                    const nameB = b.tenMonAn ? String(b.tenMonAn).toLowerCase() : '';
                    return nameA.localeCompare(nameB);
                });
                break;
            case 'newest':
            default: result.sort((a, b) => Number(b.id) - Number(a.id)); break;
        }

        return result;
    });

    // Reset trang về 1 khi filter đổi
    watch([searchQuery, sortOption, statusFilter, selectedPriceRange, selectedRootCate, selectedSubCate], () => {
        currentPage.value = 1;
    });

    const paginatedData = computed(() => {
        if (filteredAndSortedData.value.length === 0) return [];
        const start = (currentPage.value - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        return filteredAndSortedData.value.slice(start, end);
    });

    const totalPages = computed(() => Math.ceil(filteredAndSortedData.value.length / itemsPerPage) || 1);

    const goToPage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    const visiblePages = computed(() => {
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 1;
        const range = [], rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    // --- API & ACTIONS ---
    function getAllFood() {
        if (typeof getAllFoodGeneral !== 'function') return; // Safety check
        getAllFoodGeneral()
            .then(async res => {
                mockData.value = res.data;
                calculatePriceLimits(mockData.value);
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => { 
        getAllFood(); 
        fetchCategories(); 
    });

    const handleViewDetails = (item) => {
        router.push({ 
            name: 'viewFood', 
            params: { id: item.id } 
        });
    };

    const handleEditFood = (item) => {
        router.push({ 
            name: 'updateFood',
            params: { id: item.id } 
        });
    };    
    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThaiKinhDoanh;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThaiKinhDoanh = newStatus;
        try {
            const payload = {...item, trangThaiKinhDoanh: newStatus };
            if (item.danhMucChiTiet?.id) payload.idDanhMucChiTiet = item.danhMucChiTiet.id;
            // Gọi API Update (nếu có)
             if (typeof putNewFood === 'function') await putNewFood(item.id, payload);
        } catch (error) {
            item.trangThaiKinhDoanh = oldStatus;
        }
    };

    const clearFilters = () => {
        searchQuery.value = '';
        statusFilter.value = 'all';
        sortOption.value = 'newest';
        selectedRootCate.value = ''; // Reset root category
        selectedSubCate.value = '';  // Reset sub category
        resetPriceFilter(); 
    };

    return {
        mockData, paginatedData, currentPage, itemsPerPage, totalPages, visiblePages, goToPage,
        handleViewDetails,
        handleEditFood, getAllFood, handleToggleStatus, activeTab, isModalOpen, selectedItem, isAddFoodModalOpen,
        searchQuery, sortOption, statusFilter, clearFilters,
        globalMinPrice, globalMaxPrice, selectedPriceRange,
        
        // EXPORT CÁC BIẾN MỚI
        isCategoryFilterOpen,
        listRootCategories,
        selectedRootCate,
        selectedSubCate,
        availableSubCategories
    };
}

export function useFoodUpdate() {
    const route = useRoute();
    const router = useRouter();
    const foodId = route.params.id;

    // --- DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    // --- STATE ---
    const isViewMode = computed(() => route.name === 'viewFood');
    const fileInputRef = ref(null); // Ref để kích hoạt input file

    const formData = ref({
        id: '',
        tenMonAn: '',
        idDanhMuc: '',
        idDanhMucChiTiet: '',
        moTa: '',
        giaBan: 0,
        hinhAnh: '',
        trangThaiKinhDoanh: 1 
    });

    const foodInfo = ref(null);
    const variants = ref([]);
    const listDanhMuc = ref([]);
    const listDanhMucChiTiet = ref([]);
    const isLoading = ref(false);

    // --- XỬ LÝ ẢNH (Logic Mới) ---
    
    // 1. Hàm nén ảnh và chuyển Base64
    const resizeImage = (file, maxWidth = 800) => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (event) => {
                const img = new Image();
                img.src = event.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    const ctx = canvas.getContext('2d');
                    let width = img.width;
                    let height = img.height;
                    
                    // Tính toán tỷ lệ
                    if (width > maxWidth) {
                        height *= maxWidth / width;
                        width = maxWidth;
                    }
                    
                    canvas.width = width;
                    canvas.height = height;
                    ctx.drawImage(img, 0, 0, width, height);
                    // Xuất ra base64 chất lượng 0.8
                    resolve(canvas.toDataURL('image/jpeg', 0.8)); 
                };
            };
        });
    };

    // 2. Hàm kích hoạt input file ẩn
    const triggerFileInput = () => {
        fileInputRef.value.click();
    };

    // Hàm xử lý khi chọn file (Kèm Resize ảnh để tránh lỗi Database)
    const handleFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        if (!file.type.match('image.*')) {
            alert("Vui lòng chọn file hình ảnh!"); 
            return;
        }

        try {
            // Resize ảnh và chuyển sang Base64
            const resizedBase64 = await resizeImage(file); 
            formData.value.hinhAnh = resizedBase64; 
        } catch (e) {
            console.error(e);
        } finally {
            // Reset input để có thể chọn lại cùng 1 file
            event.target.value = '';
        }
    };

    // Hàm xóa ảnh
    const removeImage = () => {
        showConfirm(
            "Bạn có chắc muốn xóa ảnh này không?",
            () => {
                formData.value.hinhAnh = '';
            },
            "Xác nhận xóa",
            "warning"
        );
    };


    // --- FETCH DATA (Giữ nguyên logic cũ) ---
    const fetchData = async () => {
        try {
            isLoading.value = true;
            const [resParent, resVariants, resCat, resSubCat] = await Promise.all([
                getFoodById(foodId),
                getFoodGeneralModalById(foodId),
                getAllCategory(),
                getAllCategoryDetail()
            ]);

            listDanhMuc.value = resCat.data;
            listDanhMucChiTiet.value = resSubCat.data;

            const parentData = resParent.data;
            if (parentData) {
                foodInfo.value = { ...parentData };
                
                // Logic tìm danh mục cha/con (Giữ nguyên)
                const subCatId = parentData.idDanhMucChiTiet || (parentData.danhMucChiTiet ? parentData.danhMucChiTiet.id : '');
                let rootCatId = parentData.idDanhMuc || (parentData.danhMuc ? parentData.danhMuc.id : '');

                if (!rootCatId && subCatId) {
                    const foundSub = listDanhMucChiTiet.value.find(sub => sub.id === subCatId);
                    if (foundSub) rootCatId = foundSub.idDanhMuc;
                }

                formData.value = {
                    id: parentData.id,
                    tenMonAn: parentData.tenMonAn,
                    idDanhMuc: rootCatId,
                    idDanhMucChiTiet: subCatId,
                    moTa: parentData.moTa,
                    giaBan: parentData.giaBan,
                    hinhAnh: parentData.hinhAnh,
                    trangThaiKinhDoanh: (parentData.trangThaiKinhDoanh == 1 || parentData.trangThaiKinhDoanh === true) ? 1 : 0
                };
            }

            if (Array.isArray(resVariants.data)) {
                variants.value = resVariants.data;
                // Nếu món gốc chưa có ảnh, lấy ảnh biến thể đầu tiên
                if (!formData.value.hinhAnh && variants.value.length > 0) {
                    formData.value.hinhAnh = variants.value[0].hinhAnh;
                    if(foodInfo.value) foodInfo.value.hinhAnh = variants.value[0].hinhAnh;
                }
            }

        } catch (e) {
            console.error("Lỗi fetchData:", e);
            showError("Không thể tải thông tin món ăn!");
        } finally {
            isLoading.value = false;
        }
    };

    onMounted(() => {
        if (foodId) fetchData();
    });

    // --- COMPUTED & VALIDATE (Giữ nguyên) ---
    const filteredSubCategories = computed(() => {
        if (!formData.value.idDanhMuc) return [];
        return listDanhMucChiTiet.value.filter(item => String(item.idDanhMuc) === String(formData.value.idDanhMuc));
    });

    const categoryName = computed(() => {
        const found = listDanhMuc.value.find(c => c.id == formData.value.idDanhMuc);
        return found ? found.tenDanhMuc : '---';
    });

    const validateForm = () => {
        if (!formData.value.tenMonAn || !formData.value.tenMonAn.trim()) {
            showAlert("Vui lòng nhập tên món ăn!", "Thiếu thông tin"); return false;
        }
        if (!formData.value.hinhAnh) {
            showAlert("Vui lòng chọn hình ảnh!", "Thiếu thông tin"); return false;
        }
        return true;
    };

    // --- UPDATE ---
    const handleUpdate = async () => {
        if (!validateForm()) return;

        showConfirm(
            "Bạn có chắc chắn muốn cập nhật thông tin?",
            async () => {
                try {
                    isLoading.value = true;
                    // formData đã chứa hinhAnh mới (Base64) nếu người dùng có chọn lại
                    const payload = {
                        ...formData.value,
                        trangThaiKinhDoanh: Number(formData.value.trangThaiKinhDoanh)
                    };
                    
                    await putNewFood(formData.value.id, payload);
                    
                    showSuccess("Cập nhật thành công!");
                    setTimeout(() => goBack(), 1500); 
                } catch (e) {
                    console.error(e);
                    showError("Đã xảy ra lỗi khi cập nhật!");
                } finally {
                    isLoading.value = false;
                }
            },
            "Xác nhận cập nhật"
        );
    };

    // ... (Giữ nguyên handleToggleDetailStatus, goBack, goToAddDetail) ...
    const handleToggleDetailStatus = async (variant) => { /* logic cũ */ };
    const goBack = () => router.back();
    const goToAddDetail = () => { router.push({ name: 'addFoodDetail', query: { parentId: foodId } }); };

    return {
        isViewMode, isLoading, formData, foodInfo, variants, listDanhMuc, filteredSubCategories, categoryName,
        handleUpdate, goBack, goToAddDetail, handleToggleDetailStatus,
        dialogVisible: isVisible, dialogConfig, handleDialogConfirm: handleConfirm, handleDialogClose: handleClose,
        
        // Return các hàm xử lý ảnh
        fileInputRef,
        triggerFileInput,
        handleFileUpload,
        removeImage
    };
}

export function useFoodDetailUpdate() {
    const router = useRouter();
    const route = useRoute();
    const detailId = route.params.id;

    // --- 2. GỌI DIALOG (DÒNG NÀY ĐANG BỊ THIẾU) ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    // --- STATE ---
    const isViewMode = computed(() => route.name === 'viewFoodDetail'); // Route xem
    
    const formData = ref({
        id: null,
        maChiTietMonAn: '',
        tenChiTietMonAn: '',
        idMonAnDiKem: '',
        giaBan: 0,
        giaVon: 0,
        kichCo: '',
        donVi: '',
        hinhAnh: '',
        trangThai: 1
    });

    const listMonAn = ref([]); 
    const originalInfo = ref(null);
    const isLoading = ref(true);
    const searchQuery = ref('');
    const sortOption = ref('name_asc');

    const { selectedPriceRange, globalMinPrice, globalMaxPrice, calculatePriceLimits } = usePriceFilter();

    // --- COMPUTED FILTER ---
    const filteredMonAnList = computed(() => {
        let result = [...listMonAn.value];
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenMonAn && item.tenMonAn.toLowerCase().includes(query)) ||
                (item.maMonAn && item.maMonAn.toLowerCase().includes(query))
            );
        }
        if (selectedPriceRange.value && selectedPriceRange.value.length === 2) {
            const [min, max] = selectedPriceRange.value;
            result = result.filter(item => {
                const price = parseFloat(item.giaThapNhat) || parseFloat(item.giaBan) || 0;
                return price >= min && price <= max;
            });
        }
        return result.sort((a, b) => {
            const nameA = (a.tenMonAn || '').toLowerCase();
            const nameB = (b.tenMonAn || '').toLowerCase();
            const priceA = parseFloat(a.giaThapNhat) || parseFloat(a.giaBan) || 0;
            const priceB = parseFloat(b.giaThapNhat) || parseFloat(b.giaBan) || 0;

            switch (sortOption.value) {
                case 'name_asc': return nameA.localeCompare(nameB);
                case 'price_asc': return priceA - priceB;
                case 'price_desc': return priceB - priceA;
                default: return 0;
            }
        });
    });

    const selectParentFood = (food) => {
        if (isViewMode.value) return;
        formData.value.idMonAnDiKem = food.id;
    };

    const parentName = computed(() => {
        const found = listMonAn.value.find(m => m.id === formData.value.idMonAnDiKem);
        if (found) return found.tenMonAn;
        if (originalInfo.value && originalInfo.value.monAnDiKem) {
            return originalInfo.value.monAnDiKem.tenMonAn;
        }
        return '---';
    });

    // --- FETCH DATA ---
    const fetchDetailData = async () => {
        try {
            isLoading.value = true;
            const [resDetail, resListFood] = await Promise.all([
                getFoodDetailById(detailId),
                getAllFoodGeneral()
            ]);

            listMonAn.value = resListFood.data;
            calculatePriceLimits(listMonAn.value);

            const data = resDetail.data;
            originalInfo.value = data; 

            // Debug: xem dữ liệu trả về thực tế
            console.log("Chi tiết món API:", data);

            formData.value = {
                id: data.id,
                maChiTietMonAn: data.maChiTietMonAn,
                tenChiTietMonAn: data.tenChiTietMonAn,
                idMonAnDiKem: data.monAnDiKem ? data.monAnDiKem.id : data.idMonAnDiKem,
                giaBan: data.giaBan,
                giaVon: data.giaVon,
                kichCo: data.kichCo,
                donVi: data.donVi,
                hinhAnh: data.hinhAnh,
                
                // --- SỬA QUAN TRỌNG: Ép kiểu về Số (Number) ---
                // Nếu API trả về "1", 1, true -> thành 1
                // Nếu API trả về "0", 0, false, null -> thành 0
                trangThai: Number(data.trangThai) === 1 ? 1 : 0 
            };
        } catch (e) {
            console.error("Lỗi:", e);
            showError("Không tìm thấy dữ liệu!");
            setTimeout(() => router.back(), 1500);
        } finally {
            isLoading.value = false;
        }
    };

    onMounted(() => { if (detailId) fetchDetailData(); });

    // --- UPLOAD & VALIDATE ---
    const handleFileUpload = async (event) => {
        if (isViewMode.value) return;
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            showAlert("Vui lòng chọn file hình ảnh!", "Sai định dạng"); return;
        }
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = (e) => { formData.value.hinhAnh = e.target.result; };
    };

    const validateForm = () => {
        if (!formData.value.tenChiTietMonAn?.trim()) {
            showAlert("Vui lòng nhập tên chi tiết!", "Thiếu thông tin"); return false;
        }
        if (!formData.value.idMonAnDiKem) {
            showAlert("Vui lòng chọn món ăn gốc!", "Thiếu thông tin"); return false;
        }
        if (!formData.value.donVi || !formData.value.kichCo.trim()) {
            showAlert("Vui lòng nhập kích cỡ và đơn vị chi tiết món ăn!", "Thiếu thông tin");
            return false;
        }
        if (formData.value.giaBan < 0) {
            showAlert("Giá bán không hợp lệ!", "Lỗi nhập liệu"); return false;
        }
        if (!formData.value.hinhAnh) {
            showAlert("Vui lòng chọn hình ảnh!", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- UPDATE ---
    const handleUpdate = async () => {
        if (!validateForm()) return;

        showConfirm(
            "Bạn có chắc chắn muốn cập nhật thay đổi?",
            async () => {
                try {
                    isLoading.value = true;
                    // Gọi API
                    await putNewFoodDetail(detailId, formData.value);
                    
                    showSuccess("Cập nhật thành công!");
                    setTimeout(() => goBack(), 1500);
                } catch (e) {
                    console.error(e);
                    showError("Lỗi cập nhật!");
                } finally {
                    isLoading.value = false;
                }
            },
            "Xác nhận cập nhật"
        );
    };
    
    const goBack = () => router.back();
    
    const goToEdit = () => {
        router.push({ name: 'updateFoodDetail', params: { id: detailId } });
    };

    return {
        formData, listMonAn, originalInfo, parentName, isLoading,
        isViewMode,
        searchQuery, sortOption, filteredMonAnList, 
        selectParentFood, selectedPriceRange, globalMinPrice, globalMaxPrice,
        handleUpdate, goBack, handleFileUpload, goToEdit,

        // Return biến Dialog (Bây giờ return cái này mới không bị lỗi)
        dialogVisible: isVisible, 
        dialogConfig, 
        handleDialogConfirm: handleConfirm, 
        handleDialogClose: handleClose
    };
}

export function useHotpotManager() {
    const hotpotData = ref([]);

    // --- CÁC BIẾN FILTER ---
    const searchQuery = ref('');
    const sortOption = ref('newest');
    const currentPage = ref(1);
    const itemsPerPage = 5;
    const statusFilter = ref('all'); 
    const typeFilter = ref('all');   

    // 1. GỌI HOOK XỬ LÝ GIÁ
    const { 
        selectedPriceRange, 
        globalMinPrice, 
        globalMaxPrice, 
        calculatePriceLimits,
        resetPriceFilter
    } = usePriceFilter();

    // 2. TẠO DANH SÁCH LOẠI LẨU
    const uniqueTypes = computed(() => {
        const types = new Map();
        hotpotData.value.forEach(item => {
            if (item.tenLoaiSet && item.idLoaiSet) {
                types.set(item.idLoaiSet, item.tenLoaiSet);
            }
        });
        return Array.from(types, ([id, name]) => ({ id, name }));
    });

    // 3. LOGIC LỌC & SẮP XẾP CHÍNH
    const filteredAndSortedData = computed(() => {
        let result = [...hotpotData.value];

        // A. Lọc theo Tên/Mã
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item =>
                (item.tenSetLau && item.tenSetLau.toLowerCase().includes(query)) ||
                (item.maSetLau && item.maSetLau.toLowerCase().includes(query))
            );
        }

        // B. Lọc theo Khoảng giá (THÊM MỚI)
        // Set lẩu thường chỉ có 'giaBan', không có 'giaThapNhat' phức tạp như món ăn
        if (selectedPriceRange.value && selectedPriceRange.value.length === 2) {
            const [min, max] = selectedPriceRange.value;
            result = result.filter(item => {
                const price = parseFloat(item.giaBan) || 0;
                return price >= min && price <= max;
            });
        }

        // C. Lọc theo Trạng thái
        if (statusFilter.value !== 'all') {
            const statusValue = Number(statusFilter.value);
            result = result.filter(item => item.trangThai === statusValue);
        }

        // D. Lọc theo Loại Set Lẩu
        if (typeFilter.value !== 'all') {
            result = result.filter(item => item.idLoaiSet == typeFilter.value);
        }

        // E. Sắp xếp
        switch (sortOption.value) {
            case 'price_asc': result.sort((a, b) => (a.giaBan || 0) - (b.giaBan || 0)); break;
            case 'price_desc': result.sort((a, b) => (b.giaBan || 0) - (a.giaBan || 0)); break;
            case 'name_asc': 
                result.sort((a, b) => (a.tenSetLau || '').localeCompare(b.tenSetLau || '')); 
                break;
            case 'newest': 
            default: 
                result.sort((a, b) => b.id - a.id); 
                break;
        }

        return result;
    });

    // Reset phân trang khi filter thay đổi
    watch([searchQuery, sortOption, statusFilter, typeFilter, selectedPriceRange], () => {
        currentPage.value = 1;
    });

    // --- PHÂN TRANG ---
    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        return filteredAndSortedData.value.slice(start, end);
    });

    const totalPages = computed(() => Math.ceil(filteredAndSortedData.value.length / itemsPerPage) || 1);

    const goToPage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    const visiblePages = computed(() => {
        // ... (Giữ nguyên logic phân trang cũ của bạn) ...
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 1;
        const range = [], rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    // --- API & ACTIONS ---
    function getAllHotpot() {
        getAllHotpotGeneral().then(async res => {
            hotpotData.value = res.data;
            
            // TÍNH TOÁN GIÁ MAX CHO SLIDER
            calculatePriceLimits(hotpotData.value);
            
            await nextTick();
        }).catch(console.error);
    }
    
    onMounted(() => { getAllHotpot(); });
    
    const isModalOpen = ref(false);
    const selectedHotpot = ref(null);
    const handleViewDetails = (item) => { selectedHotpot.value = item; isModalOpen.value = true; };
    
    const handleToggleStatus = async (item) => {
    const oldStatus = item.trangThai;
    const newStatus = oldStatus === 1 ? 0 : 1;
    
    // Cập nhật giao diện trước cho mượt (Optimistic UI)
    item.trangThai = newStatus;

    try {
        // --- SỬA LẠI ĐOẠN NÀY ---
        // Thay vì gửi {...item}, hãy tạo object mới chỉ chứa thông tin cần thiết.
        // Tùy vào Backend của bạn yêu cầu gì (PUT thường cần đủ trường, PATCH chỉ cần trường thay đổi).
        // Đây là cách an toàn nhất: Copy các trường cơ bản, bỏ qua các object lồng nhau.
        
        const payload = {
            id: item.id,
            maSetLau: item.maSetLau,
            tenSetLau: item.tenSetLau,
            giaBan: item.giaBan,
            idLoaiSet: item.idLoaiSet, // Gửi ID thay vì cả object loaiSet
            hinhAnh: item.hinhAnh,
            mota: item.mota,
            trangThai: newStatus // Quan trọng nhất
        };

        // Nếu Backend hỗ trợ PATCH (cập nhật 1 phần), bạn chỉ cần gửi:
        // const payload = { id: item.id, trangThai: newStatus };

        await putNewHotpot(item.id, payload);
        console.log("Cập nhật trạng thái thành công");

    } catch (error) {
        console.error("Lỗi cập nhật:", error);
        // Hoàn tác nếu lỗi
        item.trangThai = oldStatus;
        
        // Kiểm tra lỗi cụ thể để báo người dùng
        if (error.response && error.response.status === 403) {
            alert("Bạn không có quyền chỉnh sửa mục này!");
        } else {
            alert("Lỗi kết nối, vui lòng thử lại.");
        }
    }
};

    // Hàm Reset bộ lọc
    const clearFilters = () => {
        searchQuery.value = '';
        sortOption.value = 'newest';
        statusFilter.value = 'all';
        typeFilter.value = 'all';
        
        // RESET SLIDER
        resetPriceFilter();
    };

    return {
        hotpotData, getAllHotpot, isModalOpen, selectedHotpot, handleViewDetails, handleToggleStatus,
        searchQuery, sortOption, paginatedData, currentPage, totalPages, visiblePages, itemsPerPage, goToPage,
        statusFilter, typeFilter, uniqueTypes, clearFilters,
        
        // RETURN CÁC BIẾN SLIDER
        selectedPriceRange,
        globalMinPrice,
        globalMaxPrice
    };
}

export function useHotpotUpdate() {
    const router = useRouter();
    const route = useRoute();
    const hotpotId = route.params.id;
    
    // --- GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    // --- STATE ---
    const hotpotInfo = ref(null);
    const listLoaiSet = ref([]);
    const listFoodDetails = ref([]);
    const selectedIngredients = ref([]);
    const isLoading = ref(true);
    const searchQuery = ref("");
    const sortOption = ref("name_asc");

    const formData = ref({
        id: null,
        tenSetLau: '',
        idLoaiSet: '',
        giaBan: 0,
        hinhAnh: '',
        moTa: '',
        trangThai: 1
    });

    // --- COMPUTED ---
    const isViewMode = computed(() => route.name === 'viewHotpotSet');

    const totalComponentsPrice = computed(() => {
        return selectedIngredients.value.reduce((sum, item) => sum + (item.giaBan * item.soLuong), 0);
    });

    const categoryName = computed(() => {
        if (!hotpotInfo.value) return 'Đang tải...';
        // Ưu tiên lấy từ object loaiSet trả về, nếu không có thì tìm trong list danh mục
        if (hotpotInfo.value.loaiSet?.tenLoaiSet) return hotpotInfo.value.loaiSet.tenLoaiSet;
        
        const idToFind = hotpotInfo.value.idLoaiSet || (hotpotInfo.value.loaiSet ? hotpotInfo.value.loaiSet.id : null);
        const found = listLoaiSet.value.find(cat => cat.id === idToFind);
        return found ? found.tenLoaiSet : 'Chưa phân loại';
    });

    const filteredFoodList = computed(() => {
        let result = [...listFoodDetails.value];
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase();
            result = result.filter(item =>
                (item.tenChiTietMonAn || item.tenDanhMucChiTiet || '').toLowerCase().includes(query)
            );
        }
        return result.sort((a, b) => {
            const nameA = (a.tenChiTietMonAn || a.tenDanhMucChiTiet || '').toLowerCase();
            const nameB = (b.tenChiTietMonAn || b.tenDanhMucChiTiet || '').toLowerCase();
            const priceA = a.giaBan || 0;
            const priceB = b.giaBan || 0;

            switch (sortOption.value) {
                case 'name_asc': return nameA.localeCompare(nameB);
                case 'price_asc': return priceA - priceB;
                case 'price_desc': return priceB - priceA;
                default: return 0;
            }
        });
    });

    // --- FETCH DATA ---
    const fetchDetailData = async() => {
        try {
            isLoading.value = true;
            // Gọi song song các API
            const [resDetail, resLoaiSet, resFoodDetail] = await Promise.all([
                getHotpotById(hotpotId),
                getAllCategoryHotpot(),
                getAllFoodDetail()
            ]);

            listLoaiSet.value = resLoaiSet.data;
            listFoodDetails.value = resFoodDetail.data;

            const data = resDetail.data;
            hotpotInfo.value = data;

            // Map data vào Form
            formData.value = {
                id: data.id,
                tenSetLau: data.tenSetLau,
                idLoaiSet: data.loaiSet ? data.loaiSet.id : data.idLoaiSet,
                giaBan: data.giaBan,
                hinhAnh: data.hinhAnh,
                moTa: data.moTa,
                trangThai: data.trangThai
            };

            // Map thành phần món ăn
            if (data.listChiTietSetLau && data.listChiTietSetLau.length > 0) {
                selectedIngredients.value = data.listChiTietSetLau.map(item => {
                    if (!item.chiTietMonAn) return null;
                    return {
                        id: item.chiTietMonAn.id,
                        ten: item.chiTietMonAn.tenChiTietMonAn,
                        donVi: item.chiTietMonAn.donVi,
                        giaBan: item.chiTietMonAn.giaBan,
                        hinhAnh: item.chiTietMonAn.hinhAnh,
                        soLuong: item.soLuong
                    };
                }).filter(i => i !== null);
            }
        } catch (e) {
            console.error(e);
            showError("Không thể tải dữ liệu Set Lẩu!");
            setTimeout(() => router.back(), 1500);
        } finally {
            isLoading.value = false;
        }
    };

    onMounted(() => {
        if (hotpotId) fetchDetailData();
    });

    // --- ACTIONS ---
    const addIngredient = (foodItem) => {
        const exists = selectedIngredients.value.find(item => item.id === foodItem.id);
        if (exists) {
            exists.soLuong += 1;
        } else {
            selectedIngredients.value.push({
                id: foodItem.id,
                ten: foodItem.tenChiTietMonAn || foodItem.tenDanhMucChiTiet,
                donVi: foodItem.donVi || 'Phần',
                giaBan: foodItem.giaBan,
                hinhAnh: foodItem.hinhAnh,
                soLuong: 1
            });
        }
    };

    const removeIngredient = (index) => {
        // Confirm trước khi xóa thành phần
        showConfirm(
            "Bạn muốn loại bỏ món này khỏi Set Lẩu?",
            () => { selectedIngredients.value.splice(index, 1); },
            "Xóa thành phần",
            "warning"
        );
    };

    // --- VALIDATE ---
    const validateForm = () => {
        if (!formData.value.tenSetLau || !formData.value.tenSetLau.trim()) {
            showAlert("Vui lòng nhập tên Set Lẩu!", "Thiếu thông tin");
            return false;
        }
        if (!formData.value.idLoaiSet) {
            showAlert("Vui lòng chọn Loại Set!", "Thiếu thông tin");
            return false;
        }
        if (formData.value.giaBan < 0 || formData.value.giaBan === "") {
            showAlert("Giá bán không hợp lệ!", "Lỗi nhập liệu");
            return false;
        }
        if (selectedIngredients.value.length === 0) {
            showAlert("Set lẩu cần ít nhất 1 món ăn thành phần!", "Thiếu thành phần");
            return false;
        }
        if (!formData.value.hinhAnh) {
            showAlert("Vui lòng chọn hình ảnh!", "Thiếu thông tin");
            return;
        }
        return true;
    };

    // --- UPDATE ---
    const handleUpdate = async() => {
        // 1. Validate
        if (!validateForm()) return;

        // 2. Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn cập nhật thông tin Set Lẩu này?",
            async () => {
                try {
                    isLoading.value = true;
                    const payload = {
                        ...formData.value,
                        // Map lại list thành phần theo cấu trúc backend cần
                        listChiTietSetLau: selectedIngredients.value.map(item => ({
                            idChiTietMonAn: item.id,
                            soLuong: item.soLuong
                        }))
                    };
                    
                    await putNewHotpot(hotpotId, payload);
                    
                    showSuccess("Cập nhật thành công!");
                    // Chờ chút rồi quay lại
                    setTimeout(() => goBack(), 1500);
                } catch (e) {
                    console.error(e);
                    showError("Lỗi cập nhật! Vui lòng thử lại.");
                } finally {
                    isLoading.value = false;
                }
            },
            "Xác nhận cập nhật"
        );
    };

    const handleFileUpload = (event) => {
        const file = event.target.files[0];
        if (!file || !file.type.match('image.*')) return;
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = (e) => { formData.value.hinhAnh = e.target.result; };
    };

    const goBack = () => router.back();

    return {
        // Data
        formData, listLoaiSet, selectedIngredients, totalComponentsPrice,
        searchQuery, sortOption, filteredFoodList, hotpotInfo, categoryName, 
        isViewMode, isLoading,

        // Actions
        handleFileUpload, addIngredient, removeIngredient, handleUpdate, goBack,

        // Dialog Variables (Trả về để template dùng)
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useCategoryTabManager() {
    const tabs = {
        'danhmuc': CategoryGeneral,
        'chitietDM': CategoryDetailGeneral,
        'loaiset': CategoryHotpotGeneral
    };

    const currentTabName = ref('danhmuc');
    const currentComponent = shallowRef(CategoryGeneral);

    const changeTab = (tabName) => {
        currentTabName.value = tabName;
        currentComponent.value = tabs[tabName];
    };

    return {
        currentTabName,
        currentComponent,
        changeTab
    };
}

export function useCategoryManager() {
    const categoryData = ref([]);
    const isLoading = ref(false);

    // --- STATE ---
    const searchQuery = ref('');
    
    // 1. Biến Sắp xếp Trạng thái (Riêng biệt)
    const statusSort = ref('default'); // default, active_first, inactive_first
    
    // 2. Biến Sắp xếp Chính (Tên, ID)
    const sortOption = ref('id_desc');
    
    const currentPage = ref(1);
    const itemsPerPage = ref(10); // Số lượng/trang

    // --- COMPUTED: LỌC & SẮP XẾP ---
    const filteredData = computed(() => {
        let result = [...categoryData.value];

        // A. Tìm kiếm
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenDanhMuc && item.tenDanhMuc.toLowerCase().includes(query)) ||
                (item.maDanhMuc && item.maDanhMuc.toLowerCase().includes(query))
            );
        }

        // B. Sắp xếp (Logic kết hợp)
        return result.sort((a, b) => {
            // --- Ưu tiên 1: Sắp xếp theo Trạng thái (nếu user chọn) ---
            if (statusSort.value !== 'default') {
                const statusA = Number(a.trangThai);
                const statusB = Number(b.trangThai);
                
                if (statusSort.value === 'active_first') {
                    // Đang hoạt động (1) lên đầu
                    if (statusA !== statusB) return statusB - statusA;
                } 
                else if (statusSort.value === 'inactive_first') {
                    // Ngưng hoạt động (0) lên đầu
                    if (statusA !== statusB) return statusA - statusB;
                }
            }

            // --- Ưu tiên 2: Sắp xếp theo Tiêu chí chính ---
            switch (sortOption.value) {
                case 'name_asc': 
                    return (a.tenDanhMuc || '').localeCompare(b.tenDanhMuc || '');
                case 'id_asc': 
                    return a.id - b.id;
                case 'id_desc': 
                default:
                    return b.id - a.id;
            }
        });
    });

    // --- PHÂN TRANG ---
    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        const end = start + itemsPerPage.value;
        return filteredData.value.slice(start, end);
    });

    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value));

    const visiblePages = computed(() => {
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 2;
        const range = [];
        const rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    // --- ACTIONS ---
    const changePage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    // Reset trang khi thay đổi filter/sort
    watch([searchQuery, sortOption, statusSort], () => {
        currentPage.value = 1;
    });

    // --- API & LOGIC KHÁC ---
    function getAllCategories() {
        isLoading.value = true;
        getAllCategory()
            .then(async res => {
                categoryData.value = res.data;
                await nextTick();
            })
            .catch(console.error)
            .finally(() => isLoading.value = false);
    }

    onMounted(() => { getAllCategories(); });

    const isModalOpen = ref(false);
    const isModalUpdateOpen = ref(false);
    const selectedItem = ref(null);

    const openModal = (item = null) => {
        selectedItem.value = item;
        isModalUpdateOpen.value = true;
    };

    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThai;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThai = newStatus;
        try {
            const payload = {
                tenDanhMuc: item.tenDanhMuc,
                moTa: item.moTa || "",
                trangThai: newStatus
            };
            await putNewCategory(item.id, payload);
        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
            alert("Lỗi cập nhật trạng thái!");
        }
    };

    return {
        categoryData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllCategories,
        
        // Return các biến mới
        paginatedData, searchQuery, 
        sortOption, statusSort, // 2 biến sort riêng biệt
        currentPage, totalPages, visiblePages, itemsPerPage, changePage
    };
}

export function useCategoryDetailManager() {
    const detailData = ref([]);
    const parentCategories = ref([]); 
    const isLoading = ref(false);

    // --- STATE FILTER & SORT ---
    const searchQuery = ref('');
    const categoryFilter = ref('all'); 
    
    // 1. Tách riêng biến Sắp xếp Trạng thái
    const statusSort = ref('default'); // default, active_first, inactive_first
    
    // 2. Biến Sắp xếp chính (Tên, Mã...)
    const sortOption = ref('id_desc');
    
    const currentPage = ref(1);
    const itemsPerPage = ref(10);

    // --- COMPUTED: LỌC & SẮP XẾP ---
    const filteredData = computed(() => {
        let result = [...detailData.value];

        // A. Lọc Tìm kiếm
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenDanhMucChiTiet && item.tenDanhMucChiTiet.toLowerCase().includes(query)) ||
                (item.maDanhMucChiTiet && item.maDanhMucChiTiet.toLowerCase().includes(query))
            );
        }

        // B. Lọc Danh mục gốc
        if (categoryFilter.value !== 'all') {
            result = result.filter(item => item.tenDanhMuc === categoryFilter.value);
        }

        // C. LOGIC SẮP XẾP KẾT HỢP (QUAN TRỌNG)
        return result.sort((a, b) => {
            // --- Ưu tiên 1: Sắp xếp theo Trạng thái trước (nếu có chọn) ---
            if (statusSort.value !== 'default') {
                const statusA = Number(a.trangThai);
                const statusB = Number(b.trangThai);
                
                if (statusSort.value === 'active_first') {
                    // Đang kinh doanh (1) lên đầu -> Ngưng (0) xuống dưới
                    if (statusA !== statusB) return statusB - statusA;
                } 
                else if (statusSort.value === 'inactive_first') {
                    // Ngưng kinh doanh (0) lên đầu
                    if (statusA !== statusB) return statusA - statusB;
                }
            }

            // --- Ưu tiên 2: Sắp xếp theo Tiêu chí chính (Tên, Mã...) ---
            // Nếu trạng thái giống nhau (hoặc không sort trạng thái), thì sort tiếp theo cái này
            switch (sortOption.value) {
                case 'name_asc': 
                    return (a.tenDanhMucChiTiet || '').localeCompare(b.tenDanhMucChiTiet || '');
                case 'parent_asc': 
                    return (a.tenDanhMuc || '').localeCompare(b.tenDanhMuc || '');
                case 'id_asc': 
                    return a.id - b.id;
                case 'id_desc': 
                default:
                    return b.id - a.id;
            }
        });
    });

    // --- PHÂN TRANG (Giữ nguyên) ---
    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        const end = start + itemsPerPage.value;
        return filteredData.value.slice(start, end);
    });

    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value));

    const visiblePages = computed(() => {
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 2;
        const range = [];
        const rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    const changePage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    // Reset trang khi filter thay đổi (Thêm statusSort vào watcher)
    watch([searchQuery, sortOption, categoryFilter, statusSort], () => {
        currentPage.value = 1;
    });

    // --- API & MODAL (Giữ nguyên) ---
    function getAllData() {
        isLoading.value = true;
        Promise.all([getAllCategoryDetail(), getAllCategory()])
            .then(async ([resDetail, resParent]) => {
                detailData.value = resDetail.data;
                parentCategories.value = resParent.data;
                await nextTick();
            })
            .catch(console.error)
            .finally(() => isLoading.value = false);
    }

    onMounted(() => { getAllData(); });

    const isModalOpen = ref(false);
    const isModalUpdateOpen = ref(false);
    const selectedItem = ref(null);

    const openModal = (item = null) => {
        selectedItem.value = item;
        isModalUpdateOpen.value = true;
    };

    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThai;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThai = newStatus;
        try {
            const payload = { ...item, trangThai: newStatus };
            await putCategoryDetail(item.id, payload);
        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
            alert("Lỗi cập nhật trạng thái!");
        }
    };

    return {
        detailData, parentCategories, 
        isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, 
        getAllCategoriesDetail: getAllData,

        // Pagination & Filter Variables
        paginatedData, 
        searchQuery, 
        categoryFilter,
        
        sortOption, // Sort chính
        statusSort, // Sort trạng thái (Mới)
        
        currentPage, totalPages, visiblePages, itemsPerPage, changePage
    };
}

export function useHotpotSetTypeManager() {
    const hotpotTypeData = ref([]);
    const isLoading = ref(false);

    // --- STATE FILTER & PAGINATION ---
    const searchQuery = ref('');
    const statusFilter = ref('all'); // Lọc theo trạng thái
    const sortOption = ref('id_desc'); // Sắp xếp
    const currentPage = ref(1);
    const itemsPerPage = ref(5); // Số dòng mỗi trang

    // --- 1. COMPUTED: LỌC & SẮP XẾP ---
    const filteredData = computed(() => {
        let result = [...hotpotTypeData.value];

        // A. Tìm kiếm (Mã hoặc Tên)
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenLoaiSet && item.tenLoaiSet.toLowerCase().includes(query)) ||
                (item.maLoaiSet && item.maLoaiSet.toLowerCase().includes(query))
            );
        }

        // B. Lọc theo Trạng thái
        if (statusFilter.value !== 'all') {
            const statusValue = parseInt(statusFilter.value);
            result = result.filter(item => item.trangThai === statusValue);
        }

        // C. Sắp xếp
        return result.sort((a, b) => {
            const statusA = Number(a.trangThai);
            const statusB = Number(b.trangThai);

            switch (sortOption.value) {
                case 'name_asc': 
                    return (a.tenLoaiSet || '').localeCompare(b.tenLoaiSet || '');
                
                case 'status_active': // Đang hoạt động lên đầu
                    return statusB - statusA;
                case 'status_inactive': // Ngưng hoạt động lên đầu
                    return statusA - statusB;

                case 'id_asc': 
                    return a.id - b.id;
                case 'id_desc': 
                default:
                    return b.id - a.id;
            }
        });
    });

    // --- 2. PHÂN TRANG ---
    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        const end = start + itemsPerPage.value;
        return filteredData.value.slice(start, end);
    });

    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value));

    const visiblePages = computed(() => {
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 2;
        const range = [];
        const rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) {
                range.push(i);
            }
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    // --- ACTIONS ---
    const changePage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    // Reset trang khi filter thay đổi
    watch([searchQuery, statusFilter, sortOption], () => {
        currentPage.value = 1;
    });

    // --- API CALLS ---
    function getAllHotpotType() {
        isLoading.value = true;
        getAllCategoryHotpot()
            .then(async res => {
                hotpotTypeData.value = res.data;
                await nextTick();
            })
            .catch(console.error)
            .finally(() => isLoading.value = false);
    }

    onMounted(() => { getAllHotpotType(); });

    // --- MODAL & TOGGLE ---
    const isModalOpen = ref(false);
    const isModalUpdateOpen = ref(false);
    const selectedItem = ref(null);

    const openModal = (item = null) => {
        selectedItem.value = item;
        isModalUpdateOpen.value = true;
    };

    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThai;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThai = newStatus;
        try {
            const payload = {...item, trangThai: newStatus };
            await putNewHotpotCategory(item.id, payload);
        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
            alert("Lỗi cập nhật trạng thái!");
        }
    };

    return {
        // Data gốc
        hotpotTypeData,
        
        // Modal
        isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllHotpotType,

        // Pagination & Filter (Mới)
        paginatedData,
        searchQuery,
        statusFilter,
        sortOption,
        currentPage,
        totalPages,
        visiblePages,
        itemsPerPage,
        changePage
    };
}


export function useFoodAddScreen() {
    const router = useRouter();
    const { isVisible, dialogConfig, showAlert, showError, showSuccess, showConfirm, handleConfirm, handleClose } = useDialog();

    // --- 1. HÀM TIỆN ÍCH RESIZE ẢNH (Dùng chung cho cả món chính và biến thể) ---
    const resizeImage = (file, maxWidth = 800) => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (event) => {
                const img = new Image();
                img.src = event.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    const ctx = canvas.getContext('2d');
                    let width = img.width;
                    let height = img.height;
                    if (width > maxWidth) {
                        height *= maxWidth / width;
                        width = maxWidth;
                    }
                    canvas.width = width;
                    canvas.height = height;
                    ctx.drawImage(img, 0, 0, width, height);
                    resolve(canvas.toDataURL('image/jpeg', 0.8));
                };
            };
        });
    };

    // --- 2. THÔNG TIN CHUNG ---
    const formData = ref({
        tenMonAn: '',
        idDanhMuc: '',
        idDanhMucChiTiet: '',
        moTa: '',
        giaBan: 0,
        hinhAnh: '',
        trangThaiKinhDoanh: 1
    });

    // --- 3. QUẢN LÝ DANH SÁCH CHI TIẾT ---
    const listChiTiet = ref([]);
    const detailFileInput = ref(null); // Ref cho input file ẩn của biến thể

    const newDetail = ref({
        tenChiTietMonAn: '',
        giaBan: 0,
        giaVon: 0,
        kichCo: '',
        donVi: 'Cốc',
        trangThai: 1,
        hinhAnh: '' // Thêm trường hình ảnh cho biến thể
    });

    // --- 4. HÀM XỬ LÝ ẢNH BIẾN THỂ ---
    const triggerDetailImageUpload = () => {
        if (detailFileInput.value) detailFileInput.value.click();
    };

    const handleDetailImageUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            showAlert("Vui lòng chọn file hình ảnh!", "Lỗi định dạng"); return;
        }
        try {
            const resizedBase64 = await resizeImage(file);
            newDetail.value.hinhAnh = resizedBase64;
        } catch (e) { console.error(e); }
        event.target.value = ''; // Reset input
    };

    // --- 5. HÀM THÊM CHI TIẾT (Kèm Validate Trim & Ảnh) ---
    const addDetailToList = () => {
        // Validate tên biến thể (Trim space)
        if (!newDetail.value.tenChiTietMonAn || !newDetail.value.tenChiTietMonAn.trim()) {
            showAlert("Vui lòng nhập tên chi tiết (Ví dụ: Size L)!", "Thiếu thông tin");
            return;
        }
        
        // Validate kích cỡ (Trim space)
        if (!newDetail.value.kichCo || !newDetail.value.kichCo.trim()) {
            showAlert("Vui lòng nhập kích cỡ!", "Thiếu thông tin");
            return;
        }

        // Validate giá
        if (newDetail.value.giaBan === "" || newDetail.value.giaBan < 0) {
            showAlert("Giá bán không hợp lệ!", "Lỗi nhập liệu", "error");
            return;
        }

        // Validate ảnh biến thể (Bắt buộc)
        if (!newDetail.value.hinhAnh) {
            showAlert("Vui lòng chọn ảnh cho biến thể này!", "Thiếu thông tin");
            return;
        }

        // Tự động trim dữ liệu trước khi đẩy vào list
        listChiTiet.value.push({
            ...newDetail.value,
            tenChiTietMonAn: newDetail.value.tenChiTietMonAn.trim(),
            kichCo: newDetail.value.kichCo.trim(),
            donVi: newDetail.value.donVi.trim()
        });

        // Reset form
        newDetail.value = {
            tenChiTietMonAn: '',
            giaBan: 0,
            giaVon: 0,
            kichCo: '',
            donVi: 'Cốc',
            trangThai: 1,
            hinhAnh: ''
        };
    };

    const removeDetailFromList = (index) => {
        showConfirm("Bạn muốn xóa chi tiết này khỏi danh sách?", () => { listChiTiet.value.splice(index, 1); }, "Xóa chi tiết", "error");
    };

    // --- 6. XỬ LÝ ẢNH MÓN CHÍNH ---
    const mainFileInput = ref(null);
    const triggerMainImageUpload = () => { if(mainFileInput.value) mainFileInput.value.click(); };
    
    const handleMainFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) { showAlert("Sai định dạng ảnh!", "Lỗi"); return; }
        try {
            const resizedBase64 = await resizeImage(file);
            formData.value.hinhAnh = resizedBase64;
        } catch (e) { console.error(e); }
        event.target.value = '';
    };

    // --- 7. DANH MỤC (API) ---
    const listDanhMuc = ref([]);
    const listDanhMucChiTiet = ref([]);
    function getAllCategories() { getAllCategory().then(res => listDanhMuc.value = res.data); }
    function getAllCategoriesDetail() { getAllCategoryDetail().then(res => listDanhMucChiTiet.value = res.data); }
    onMounted(() => { getAllCategories(); getAllCategoriesDetail(); });
    const filteredSubCategories = computed(() => {
        if (!formData.value.idDanhMuc) return [];
        return listDanhMucChiTiet.value.filter(item => String(item.idDanhMuc) === String(formData.value.idDanhMuc));
    });

    const handleSave = async () => {
        if (!formData.value.tenMonAn || !formData.value.tenMonAn.trim()) {
            showAlert("Vui lòng nhập tên món ăn!", "Thiếu thông tin"); return;
        }
        if (!formData.value.idDanhMucChiTiet) {
            showAlert("Vui lòng chọn danh mục chi tiết!", "Thiếu thông tin"); return;
        }
        if (!formData.value.hinhAnh) {
            showAlert("Vui lòng chọn hình ảnh món ăn!", "Thiếu thông tin"); return;
        }
        if (listChiTiet.value.length === 0) {
            showAlert("Vui lòng thêm ít nhất một biến thể (chi tiết)!", "Cảnh báo"); return;
        }

        showConfirm(
            "Bạn có chắc chắn muốn thêm món ăn này không?",
            async () => {
                try {
                    // Chuẩn hóa dữ liệu trước khi gửi (Trim lần cuối)
                    const payload = {
                        ...formData.value,
                        tenMonAn: formData.value.tenMonAn.trim(),
                        moTa: formData.value.moTa ? formData.value.moTa.trim() : '',
                        listChiTiet: listChiTiet.value // List này đã được trim lúc add
                    };
                    await postNewFood(payload);
                    showSuccess("Thêm mới thành công!");
                    setTimeout(() => router.back(), 1500);
                } catch (e) {
                    console.error(e);
                    showError("Đã xảy ra lỗi khi thêm mới!");
                }
            },
            "Xác nhận thêm mới", "success"
        );
    };

    const goBack = () => router.back();

    return {
        formData, listDanhMuc, filteredSubCategories, handleSave, goBack,
        listChiTiet, newDetail, addDetailToList, removeDetailFromList,
        
        // Return các hàm/ref ảnh
        mainFileInput, handleMainFileUpload, triggerMainImageUpload,
        detailFileInput, handleDetailImageUpload, triggerDetailImageUpload,

        dialogVisible: isVisible, dialogConfig, handleDialogConfirm: handleConfirm, handleDialogClose: handleClose
    };
}


export function useHotpotAdd() {
    const router = useRouter();

    // --- 2. GỌI DIALOG (KHỞI TẠO BIẾN) ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    // --- STATE ---
    const formData = ref({
        tenSetLau: '',
        idLoaiSet: '',
        giaBan: 0,
        hinhAnh: '',
        moTa: '',
        trangThai: 1
    });

    const listLoaiSet = ref([]);
    const listFoodDetails = ref([]);
    const selectedIngredients = ref([]);
    
    // State tìm kiếm & sắp xếp
    const searchQuery = ref("");
    const sortOption = ref("name_asc");

    // --- COMPUTED ---
    const filteredFoodList = computed(() => {
        let result = listFoodDetails.value;

        // Lọc theo tên
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase();
            result = result.filter(item => 
                (item.tenChiTietMonAn || item.tenDanhMucChiTiet || '').toLowerCase().includes(query)
            );
        }

        // Sắp xếp
        return result.sort((a, b) => {
            const nameA = (a.tenChiTietMonAn || a.tenDanhMucChiTiet || '').toLowerCase();
            const nameB = (b.tenChiTietMonAn || b.tenDanhMucChiTiet || '').toLowerCase();
            
            if (sortOption.value === 'name_asc') return nameA.localeCompare(nameB);
            if (sortOption.value === 'price_asc') return a.giaBan - b.giaBan;
            if (sortOption.value === 'price_desc') return b.giaBan - a.giaBan;
            return 0;
        });
    });

    const totalComponentsPrice = computed(() => {
        return selectedIngredients.value.reduce((sum, item) => sum + (item.giaBan * item.soLuong), 0);
    });

    // --- FETCH DATA ---
    const fetchInitialData = async() => {
        try {
            const [resLoaiSet, resFoodDetail] = await Promise.all([
                getAllCategoryHotpot(),
                getAllFoodDetail()
            ]);
            listLoaiSet.value = resLoaiSet.data;
            listFoodDetails.value = resFoodDetail.data;
        } catch (e) {
            console.error("Lỗi load dữ liệu:", e);
            showError("Không thể tải danh sách món ăn/danh mục!");
        }
    };

    onMounted(() => {
        fetchInitialData();
    });

    // --- ACTIONS ---
    const addIngredient = (foodItem) => {
        const exists = selectedIngredients.value.find(item => item.id === foodItem.id);
        if (exists) {
            exists.soLuong += 1;
        } else {
            selectedIngredients.value.push({
                id: foodItem.id,
                ten: foodItem.tenChiTietMonAn || foodItem.tenDanhMucChiTiet,
                donVi: foodItem.donVi || 'Phần',
                giaBan: foodItem.giaBan,
                hinhAnh: foodItem.hinhAnh,
                soLuong: 1
            });
        }
    };

    const removeIngredient = (index) => {
        selectedIngredients.value.splice(index, 1);
    };

    const handleFileUpload = (event) => {
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            showAlert("Vui lòng chọn file hình ảnh!", "Sai định dạng");
            return;
        }
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = (e) => { formData.value.hinhAnh = e.target.result; };
    };

    const goBack = () => router.back();

    // --- SAVE (VALIDATE & CONFIRM) ---
    const handleSave = async() => {
        // 1. Validate Form
        if (!formData.value.tenSetLau || !formData.value.tenSetLau.trim()) {
            showAlert("Vui lòng nhập tên Set Lẩu!", "Thiếu thông tin");
            return;
        }
        if (!formData.value.idLoaiSet) {
            showAlert("Vui lòng chọn Loại Set!", "Thiếu thông tin");
            return;
        }
        if (!formData.value.giaBan || formData.value.giaBan < 0) {
            showAlert("Giá bán không hợp lệ!", "Lỗi nhập liệu");
            return;
        }
        if (selectedIngredients.value.length === 0) {
            showAlert("Vui lòng thêm ít nhất 1 món ăn vào Set!", "Thiếu thành phần");
            return;
        }
        if (!formData.value.hinhAnh) {
            showAlert("Vui lòng chọn hình ảnh!", "Thiếu thông tin");
            return;
        }

        // 2. Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn thêm Set Lẩu mới này?",
            async () => {
                try {
                    // Tạo payload đúng chuẩn Backend yêu cầu
                    const payload = {
                        ...formData.value,
                        listChiTietSetLau: selectedIngredients.value.map(item => ({
                            idChiTietMonAn: item.id,
                            soLuong: item.soLuong
                        }))
                    };

                    // Gọi API
                    await postNewHotpot(payload);
                    
                    showSuccess("Thêm mới thành công!");
                    
                    // Chuyển trang sau 1.5 giây
                    setTimeout(() => {
                        router.push({ name: 'foodManager', query: { tab: 'setlau' } });
                    }, 1500);

                } catch (e) {
                    console.error(e);
                    showError("Lỗi khi thêm mới! Vui lòng thử lại.");
                }
            },
            "Xác nhận thêm mới"
        );
    };

    return {
        // Data
        formData, listLoaiSet, selectedIngredients, totalComponentsPrice,
        searchQuery, sortOption, filteredFoodList,
        
        // Actions
        addIngredient, removeIngredient, handleSave, goBack, handleFileUpload,
        
        // Dialog Variables (Trả về để Template sử dụng)
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useFoodDetailAdd() {
    const router = useRouter();
    const route = useRoute();
    const parentIdFromUrl = route.query.parentId; // ID món cha nếu truyền từ trang cha

    // --- 2. GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    // --- STATE ---
    const formData = ref({
        tenChiTietMonAn: '',
        idMonAnDiKem: '', 
        moTaChiTiet: '',
        giaBan: 0,
        giaVon: 0,
        kichCo: '',
        donVi: '',
        hinhAnh: '',
        trangThai: 1
    });

    const listMonAn = ref([]); 
    const isParentLocked = ref(false); 
    const parentFoodName = ref('');

    // State tìm kiếm & sắp xếp
    const searchQuery = ref('');
    const sortOption = ref('name_asc');

    // --- COMPUTED: LỌC DANH SÁCH MÓN GỐC ---
    const filteredMonAnList = computed(() => {
        let result = [...listMonAn.value];

        // Lọc theo tên
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenMonAn && item.tenMonAn.toLowerCase().includes(query)) ||
                (item.maMonAn && item.maMonAn.toLowerCase().includes(query))
            );
        }

        // Sắp xếp
        return result.sort((a, b) => {
            const nameA = (a.tenMonAn || '').toLowerCase();
            const nameB = (b.tenMonAn || '').toLowerCase();
            const priceA = parseFloat(a.giaThapNhat) || parseFloat(a.giaBan) || 0;
            const priceB = parseFloat(b.giaThapNhat) || parseFloat(b.giaBan) || 0;

            switch (sortOption.value) {
                case 'name_asc': return nameA.localeCompare(nameB);
                case 'price_asc': return priceA - priceB;
                case 'price_desc': return priceB - priceA;
                default: return 0;
            }
        });
    });

    // --- HÀM CHỌN MÓN ---
    const selectParentFood = (food) => {
        if (isParentLocked.value) return; 
        formData.value.idMonAnDiKem = food.id;
    };

    // --- FETCH DATA ---
    const fetchInitialData = async() => {
        try {
            const resAllFood = await getAllFoodGeneral();
            listMonAn.value = resAllFood.data;

            // Nếu có parentId trên URL -> Tự động chọn và khóa
            if (parentIdFromUrl) {
                const pId = parseInt(parentIdFromUrl);
                const parent = listMonAn.value.find(f => f.id === pId);
                if (parent) {
                    formData.value.idMonAnDiKem = pId;
                    isParentLocked.value = true;
                    parentFoodName.value = parent.tenMonAn;
                }
            }
        } catch (e) {
            console.error("Lỗi load dữ liệu:", e);
            showError("Không thể tải danh sách món ăn gốc!");
        }
    };

    onMounted(() => {
        fetchInitialData();
    });

    // --- UPLOAD ẢNH ---
    const handleFileUpload = (event) => {
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            showAlert("Vui lòng chọn file hình ảnh!", "Sai định dạng");
            return;
        }
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = (e) => { formData.value.hinhAnh = e.target.result; };
    };

    // --- VALIDATE & SAVE ---
    const handleSave = async() => {
        // 1. Validate
        if (!formData.value.idMonAnDiKem) {
            showAlert("Vui lòng chọn Món ăn gốc (cột bên phải)!", "Thiếu thông tin");
            return;
        }
        if (!formData.value.tenChiTietMonAn || !formData.value.tenChiTietMonAn.trim()) {
            showAlert("Vui lòng nhập Tên chi tiết món ăn!", "Thiếu thông tin");
            return;
        }
        if (!formData.value.donVi || !formData.value.kichCo.trim()) {
            showAlert("Vui lòng nhập kích cỡ và đơn vị chi tiết món ăn!", "Thiếu thông tin");
            return;
        }
        if (formData.value.giaBan < 0 || formData.value.giaVon < 0) {
            showAlert("Giá tiền không được nhỏ hơn 0!", "Lỗi nhập liệu");
            return;
        }
        if (!formData.value.hinhAnh) {
            showAlert("Vui lòng chọn hình ảnh!", "Thiếu thông tin");
            return;
        }

        // 2. Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn thêm chi tiết món ăn này?",
            async () => {
                try {
                    // Chuẩn hóa dữ liệu trước khi gửi
                    const payload = {
                        ...formData.value,
                        // Đảm bảo kiểu số cho trangThai
                        trangThai: Number(formData.value.trangThai) 
                    };

                    await postNewFoodDetail(payload);
                    
                    showSuccess("Thêm chi tiết thành công!");
                    
                    // Quay lại trang trước sau 1.5s
                    setTimeout(() => router.back(), 1500);

                } catch (e) {
                    console.error("Lỗi thêm mới:", e);
                    showError("Đã xảy ra lỗi khi lưu dữ liệu!");
                }
            },
            "Xác nhận thêm mới"
        );
    };

    const goBack = () => router.back();

    return {
        formData, listMonAn, isParentLocked, parentFoodName,
        searchQuery, sortOption, filteredMonAnList, selectParentFood,
        handleSave, goBack, handleFileUpload,

        // Return Dialog Variables
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useCategoryAddModal(props, emit) {

    // --- 2. GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    const formData = ref({
        tenDanhMuc: '',
        moTa: '',
        trangThai: 1
    });

    // Reset form khi mở modal
    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenDanhMuc: '',
                moTa: '',
                trangThai: 1
            };
        }
    });

    // --- 3. VALIDATE FORM ---
    const validateForm = () => {
        if (!formData.value.tenDanhMuc || !formData.value.tenDanhMuc.trim()) {
            showAlert("Vui lòng nhập tên danh mục!", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- 4. HANDLE SAVE VỚI CONFIRM ---
    const handleSave = async() => {
        // Bước 1: Validate
        if (!validateForm()) return;

        // Bước 2: Hiện Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn thêm danh mục mới này?",
            async () => {
                try {
                    // Gọi API Thêm mới
                    await postNewCategory(formData.value);

                    showSuccess("Thêm danh mục thành công!");

                    // Refresh danh sách cha
                    emit('refresh');

                    // Đóng modal sau một chút để người dùng kịp đọc thông báo thành công
                    setTimeout(() => {
                        emit('close');
                    }, 1000);

                } catch (e) {
                    console.error("Lỗi thêm danh mục:", e);
                    showError("Đã xảy ra lỗi khi thêm mới!");
                }
            },
            "Xác nhận thêm mới"
        );
    };

    return {
        formData,
        handleSave,
        
        // Return các biến của Dialog để template sử dụng
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useCategoryPutModal(props, emit) {

    // --- 2. GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    const formData = ref({
        id: null,
        maDanhMuc: '',
        tenDanhMuc: '',
        moTa: '',
        trangThai: 1
    });

    // Theo dõi props để đổ dữ liệu vào form khi mở modal
    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            // Clone object để tránh tham chiếu ngược
            formData.value = { ...newItem };
        } else {
            formData.value = {
                id: null,
                maDanhMuc: '',
                tenDanhMuc: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    // --- 3. VALIDATE ---
    const validateForm = () => {
        if (!formData.value.tenDanhMuc || !formData.value.tenDanhMuc.trim()) {
            showAlert("Vui lòng nhập tên danh mục!", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- 4. SAVE VỚI CONFIRM ---
    const handleSave = async() => {
        // Bước 1: Validate
        if (!validateForm()) return;

        // Bước 2: Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn lưu các thay đổi này?",
            async () => {
                try {
                    // Gọi API Cập nhật
                    await putNewCategory(formData.value.id, formData.value);

                    showSuccess("Cập nhật thành công!");

                    // Refresh danh sách cha
                    emit('refresh');
                    
                    // Đợi 1s để người dùng thấy thông báo thành công rồi mới đóng
                    setTimeout(() => {
                        closeModal();
                    }, 1000);

                } catch (e) {
                    console.error("Lỗi cập nhật:", e);
                    showError("Đã xảy ra lỗi khi cập nhật!");
                }
            },
            "Xác nhận cập nhật"
        );
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal,

        // Return biến Dialog
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useCategoryHotpotPutModal(props, emit) {

    // --- 2. GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    const formData = ref({
        id: null,
        maLoaiSet: '',
        tenLoaiSet: '',
        moTa: '',
        trangThai: 1
    });

    // Reset/Fill form khi props thay đổi
    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            formData.value = { ...newItem };
        } else {
            formData.value = {
                id: null,
                maLoaiSet: '',
                tenLoaiSet: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    // --- 3. VALIDATE ---
    const validateForm = () => {
        if (!formData.value.tenLoaiSet || !formData.value.tenLoaiSet.trim()) {
            showAlert("Vui lòng nhập tên loại set!", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- 4. HANDLE SAVE VỚI CONFIRM ---
    const handleSave = async() => {
        // Bước 1: Validate
        if (!validateForm()) return;

        // Bước 2: Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn cập nhật loại set này?",
            async () => {
                try {
                    // Gọi API
                    await putNewHotpotCategory(formData.value.id, formData.value);

                    showSuccess("Cập nhật thành công!");

                    // Refresh danh sách cha
                    emit('refresh');
                    
                    // Đóng modal sau 1 giây để người dùng kịp đọc thông báo
                    setTimeout(() => {
                        closeModal();
                    }, 1000);

                } catch (e) {
                    console.error("Lỗi cập nhật:", e);
                    showError("Đã xảy ra lỗi khi cập nhật!");
                }
            },
            "Xác nhận cập nhật"
        );
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal,

        // Return biến Dialog
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}


export function useHotpotCategoryAddModal(props, emit) {

    // --- 2. GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    const formData = ref({
        tenLoaiSet: '',
        moTa: '',
        trangThai: 1
    });

    // Reset form khi mở modal
    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenLoaiSet: '',
                moTa: '',
                trangThai: 1
            };
        }
    });

    // --- 3. VALIDATE ---
    const validateForm = () => {
        if (!formData.value.tenLoaiSet || !formData.value.tenLoaiSet.trim()) {
            showAlert("Vui lòng nhập tên loại set!", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- 4. HANDLE SAVE VỚI CONFIRM ---
    const handleSave = async() => {
        // Bước 1: Validate
        if (!validateForm()) return;

        // Bước 2: Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn thêm loại set lẩu mới này?",
            async () => {
                try {
                    // Gọi API
                    await postNewHotpotCategory(formData.value);

                    showSuccess("Thêm mới thành công!");

                    // Refresh danh sách cha
                    emit('refresh');
                    
                    // Đóng modal sau 1 giây
                    setTimeout(() => {
                        emit('close');
                    }, 1000);

                } catch (e) {
                    console.error("Lỗi thêm mới:", e);
                    showError("Đã xảy ra lỗi khi thêm mới!");
                }
            },
            "Xác nhận thêm mới"
        );
    };

    return {
        formData,
        handleSave,

        // Return biến Dialog
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useCategoryDetailAddModal(props, emit) {

    const listDanhMucGoc = ref([]);

    function getAllCategories() {
        getAllCategory()
            .then(async res => {
                listDanhMucGoc.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }
    onMounted(() => {
        getAllCategories();
    })

    const formData = ref({
        idDanhMuc: '',
        tenDanhMucChiTiet: '',
        moTa: '',
        trangThai: 1
    });


    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                idDanhMuc: '',
                tenDanhMucChiTiet: '',
                moTa: '',
                trangThai: 1
            };
        }
    });

    const handleSave = async() => {
        try {
            // Gọi API Thêm mới (Example)
            // await axios.post('http://localhost:8080/api/manage/food', formData.value);
            postNewCategoryDetail(formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    return {
        formData,
        handleSave,
        listDanhMucGoc
    };
}

export function useCategoryDetailPutModal(props, emit) {

    // --- 2. GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    const listDanhMucGoc = ref([]);

    function getAllCategories() {
        getAllCategory()
            .then(async res => {
                listDanhMucGoc.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }
    
    onMounted(() => {
        getAllCategories();
    });

    const formData = ref({
        id: null,
        maDanhMucChiTiet: '',
        idDanhMuc: '',
        tenDanhMucChiTiet: '',
        moTa: '',
        trangThai: 1
    });

    // Reset/Fill form khi props thay đổi
    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            // Clone object để tránh thay đổi trực tiếp props
            formData.value = { ...newItem };
        } else {
            formData.value = {
                id: null,
                maDanhMucChiTiet: '',
                idDanhMuc: '',
                tenDanhMucChiTiet: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    // --- 3. VALIDATE ---
    const validateForm = () => {
        if (!formData.value.tenDanhMucChiTiet || !formData.value.tenDanhMucChiTiet.trim()) {
            showAlert("Vui lòng nhập tên chi tiết!", "Thiếu thông tin");
            return false;
        }
        if (!formData.value.idDanhMuc) {
            showAlert("Vui lòng chọn danh mục gốc!", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- 4. HANDLE SAVE VỚI CONFIRM ---
    const handleSave = async() => {
        // Bước 1: Validate
        if (!validateForm()) return;

        // Bước 2: Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn cập nhật thông tin này?",
            async () => {
                try {
                    // Gọi API
                    await putCategoryDetail(formData.value.id, formData.value);

                    showSuccess("Cập nhật thành công!");

                    // Refresh danh sách cha
                    emit('refresh');
                    
                    // Đóng modal sau 1 giây
                    setTimeout(() => {
                        closeModal();
                    }, 1000);

                } catch (e) {
                    console.error("Lỗi cập nhật:", e);
                    showError("Đã xảy ra lỗi khi cập nhật!");
                }
            },
            "Xác nhận cập nhật"
        );
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal,
        listDanhMucGoc,

        // Return biến Dialog
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useDialog() {
    const isVisible = ref(false);
    const dialogConfig = ref({
        type: 'alert',      
        variant: 'warning', 
        title: '',
        message: '',
        onConfirm: null 
    });

    const showAlert = (message, title = "Thông báo", variant = "warning") => {
        dialogConfig.value = {
            type: 'alert',
            variant,
            title,
            message,
            onConfirm: null
        };
        isVisible.value = true;
    };

    const showSuccess = (message) => showAlert(message, "Thành công", "success");
    
    const showError = (message) => showAlert(message, "Lỗi", "error");

    const showConfirm = (message, onConfirmCallback, title = "Xác nhận", variant = "warning") => {
        dialogConfig.value = {
            type: 'confirm',
            variant,
            title,
            message,
            onConfirm: onConfirmCallback
        };
        isVisible.value = true;
    };

    const handleConfirm = () => {
        if (dialogConfig.value.onConfirm) {
            dialogConfig.value.onConfirm();
        }
        isVisible.value = false;
    };

    const handleClose = () => {
        isVisible.value = false;
    };

    return {
        isVisible,
        dialogConfig,
        showAlert,
        showSuccess,
        showError,
        showConfirm,
        handleConfirm,
        handleClose
    };
}