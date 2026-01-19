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

    // --- CÁC BIẾN FILTER ---
    const searchQuery = ref('');
    const currentPage = ref(1);
    const itemsPerPage = ref(5);
    const sortOption = ref('id_asc');
    const statusFilter = ref('all'); 

    // 1. TÍCH HỢP HOOK GIÁ
    const { 
        selectedPriceRange, 
        globalMinPrice, 
        globalMaxPrice, 
        calculatePriceLimits, 
        resetPriceFilter 
    } = usePriceFilter();

    // 2. LOGIC LỌC & SẮP XẾP
    const filteredData = computed(() => {
        let result = [...detailData.value];

        // A. Lọc Tìm kiếm
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenChiTietMonAn || '').toLowerCase().includes(query) ||
                (item.maChiTietMonAn || '').toLowerCase().includes(query)
            );
        }

        // B. Lọc Trạng thái
        if (statusFilter.value !== 'all') {
            const statusValue = Number(statusFilter.value); 
            result = result.filter(item => item.trangThai === statusValue);
        }

        // C. Lọc Khoảng giá (THÊM MỚI)
        if (selectedPriceRange.value && selectedPriceRange.value.length === 2) {
            const [min, max] = selectedPriceRange.value;
            result = result.filter(item => {
                // Chi tiết món ăn dùng giaBan
                const price = parseFloat(item.giaBan) || 0;
                return price >= min && price <= max;
            });
        }

        // D. Sắp xếp
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

    // Reset trang về 1 khi bất kỳ điều kiện lọc nào thay đổi
    watch([searchQuery, statusFilter, sortOption, selectedPriceRange], () => {
        currentPage.value = 1;
    });

    // --- PHÂN TRANG ---
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

    // --- API & ACTIONS ---
    function getAllFoodDetails() {
        getAllFoodDetail()
            .then(async res => {
                detailData.value = res.data;
                // Tính toán lại giá Max cho thanh trượt
                calculatePriceLimits(detailData.value);
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => { getAllFoodDetails(); });

    // Các hàm modal/status giữ nguyên
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
            // Fix lỗi backend nếu cần idMonAnDiKem phẳng
            if (item.monAnDiKem && item.monAnDiKem.id) {
                payload.idMonAnDiKem = item.monAnDiKem.id;
            }
            await putNewFoodDetail(item.id, payload);
        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };

    // Thêm hàm xóa bộ lọc
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
        
        // Return các biến slider
        selectedPriceRange, globalMinPrice, globalMaxPrice, clearFilters
    };
}

export function useFoodManager() {
    const mockData = ref([]);
    const activeTab = ref('thucdon');
    const isModalOpen = ref(false);
    const selectedItem = ref(null);
    const isAddFoodModalOpen = ref(false);

    // --- CÁC BIẾN FILTER & SORT ---
    const searchQuery = ref('');
    const sortOption = ref('newest'); 
    const statusFilter = ref('all');
    
    const currentPage = ref(1);
    const itemsPerPage = 5;

    // 1. SỬ DỤNG LOGIC TỪ FILE USEPRICEFILTER
    const { 
        globalMinPrice, 
        globalMaxPrice, 
        selectedPriceRange, 
        calculatePriceLimits, 
        isPriceInRange,
        resetPriceFilter
    } = usePriceFilter();

    // 2. LOGIC LỌC & SẮP XẾP CHÍNH (SỬA LẠI PHẦN NÀY)
    const filteredAndSortedData = computed(() => {
        // 1. Copy mảng để tránh lỗi reference
        let result = [...mockData.value];

        // ----------------------------------------------------
        // A. CÁC BỘ LỌC (GIỮ NGUYÊN)
        // ----------------------------------------------------
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenMonAn && item.tenMonAn.toLowerCase().includes(query)) ||
                (item.maMonAn && item.maMonAn.toLowerCase().includes(query))
            );
        }

        if (statusFilter.value !== 'all') {
            const statusValue = Number(statusFilter.value);
            result = result.filter(item => item.trangThaiKinhDoanh === statusValue);
        }

        // Lọc theo slider (Quan trọng: Slider trả về số, nên so sánh số ok)
        result = result.filter(item => isPriceInRange(item));

        // ----------------------------------------------------
        // B. SẮP XẾP (PHẦN BẠN ĐANG BỊ LỖI)
        // ----------------------------------------------------
        
        // Hàm này chuyên trị các loại giá null, undefined, string, số 0...
        const getSafePrice = (item) => {
            // Lấy giá trị thô
            let min = item.giaThapNhat; 
            let base = item.giaBan;

            // Nếu null/undefined -> Gán bằng 0 ngay
            if (min === null || min === undefined) min = 0;
            if (base === null || base === undefined) base = 0;

            // Ép về kiểu số thực (Float) để tránh lỗi so sánh chuỗi ("100" < "20")
            const valMin = parseFloat(min);
            const valBase = parseFloat(base);

            // Logic: Nếu có giá thấp nhất > 0 thì dùng, không thì dùng giá gốc
            // Dùng || 0 để chặn trường hợp NaN
            return (valMin > 0 ? valMin : valBase) || 0;
        };

        // Debug: Bật F12 -> Console xem dòng này có hiện ra khi chọn sort không
        // console.log("Sort Option đang chọn:", sortOption.value);

        switch (sortOption.value) {
            case 'price_asc': // Giá tăng dần
                result.sort((a, b) => getSafePrice(a) - getSafePrice(b));
                break;
                
            case 'price_desc': // Giá giảm dần
                result.sort((a, b) => getSafePrice(b) - getSafePrice(a));
                break;
                
            case 'name_asc': // Tên A-Z
                result.sort((a, b) => {
                    const nameA = a.tenMonAn ? String(a.tenMonAn).toLowerCase() : '';
                    const nameB = b.tenMonAn ? String(b.tenMonAn).toLowerCase() : '';
                    return nameA.localeCompare(nameB);
                });
                break;
                
            case 'newest': // Mới nhất
            default:
                // Sắp xếp theo ID giảm dần (người mới tạo ID sẽ lớn hơn)
                result.sort((a, b) => Number(b.id) - Number(a.id));
                break;
        }

        return result;
    });

    // Reset về trang 1 khi bất kỳ bộ lọc nào thay đổi
    watch([searchQuery, sortOption, statusFilter, selectedPriceRange], () => {
        currentPage.value = 1;
    });

    // --- PHÂN TRANG ---
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

    // --- API ---
    function getAllFood() {
        getAllFoodGeneral()
            .then(async res => {
                mockData.value = res.data;
                // Tính toán lại thanh kéo giá dựa trên dữ liệu mới tải về
                calculatePriceLimits(mockData.value);
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => { getAllFood(); });

    const handleViewDetails = (item) => { selectedItem.value = item; isModalOpen.value = true; };
    
    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThaiKinhDoanh;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThaiKinhDoanh = newStatus;
        try {
            const payload = {...item, trangThaiKinhDoanh: newStatus };
            if (item.danhMucChiTiet?.id) payload.idDanhMucChiTiet = item.danhMucChiTiet.id;
            await putNewFood(item.id, payload);
        } catch (error) {
            item.trangThaiKinhDoanh = oldStatus;
        }
    };

    const clearFilters = () => {
        searchQuery.value = '';
        statusFilter.value = 'all';
        sortOption.value = 'newest';
        resetPriceFilter(); // Reset thanh kéo
    };

    return {
        mockData, paginatedData, currentPage, itemsPerPage, totalPages, visiblePages, goToPage,
        handleViewDetails, getAllFood, handleToggleStatus,
        activeTab, isModalOpen, selectedItem, isAddFoodModalOpen,
        
        searchQuery, sortOption, statusFilter, clearFilters,
        
        // Return biến từ usePriceFilter để Component dùng
        globalMinPrice,
        globalMaxPrice,
        selectedPriceRange
    };
}
export function usePriceFilter() {
    const globalMinPrice = ref(0);
    const globalMaxPrice = ref(1000000); // Mặc định
    const selectedPriceRange = ref([0, 1000000]);

    /**
     * Hàm tính toán lại Max Price dựa trên dữ liệu tải về
     * @param {Array} dataList - Danh sách món ăn
     */
    const calculatePriceLimits = (dataList) => {
        if (!dataList || dataList.length === 0) return;

        // 1. Tìm giá cao nhất thực tế trong DB
        const maxInDb = Math.max(...dataList.map(item => {
            // Lấy tất cả các trường giá có thể có
            return parseFloat(item.giaCaoNhat) || parseFloat(item.giaBan) || parseFloat(item.giaThapNhat) || 0;
        }));
        
        // 2. GÁN TRỰC TIẾP GIÁ TRỊ THỰC TẾ (Không làm tròn lên 100k nữa)
        // Nếu muốn làm tròn nhẹ cho đẹp (ví dụ tròn chục nghìn) thì dùng dòng dưới, còn không thì để nguyên maxInDb
        // const exactMax = Math.ceil(maxInDb / 10000) * 10000; // Làm tròn lên 10k gần nhất
        
        globalMaxPrice.value = maxInDb > 0 ? maxInDb : 1000000;
        
        // 3. Reset thanh kéo về full range
        selectedPriceRange.value = [0, globalMaxPrice.value];
    };

    /**
     * Hàm kiểm tra xem 1 món có nằm trong khoảng giá chọn không
     * Dùng để filter bên useFoodManager
     */
    const isPriceInRange = (item) => {
        // Lấy giá thấp nhất để so sánh (logic kinh doanh: khách tìm món rẻ nhất trong khoảng giá)
        const priceToCheck = item.giaThapNhat || item.giaBan || 0;
        const [min, max] = selectedPriceRange.value;
        return priceToCheck >= min && priceToCheck <= max;
    };

    const resetPriceFilter = () => {
        selectedPriceRange.value = [0, globalMaxPrice.value];
    };

    return {
        globalMinPrice,
        globalMaxPrice,
        selectedPriceRange,
        calculatePriceLimits,
        isPriceInRange,
        resetPriceFilter
    };
}

export function useFoodModal(itemSource) {
    const currentView = ref('list');
    const selectedVariant = ref(null);
    const variants = ref([]);
    const listDanhMuc = ref([]);
    const listDanhMucChiTiet = ref([]);

    const parentFormData = ref({
        id: '',
        tenMonAn: '',
        idDanhMuc: '',
        idDanhMucChiTiet: '',
        moTa: '',
        giaBan: 0,
        hinhAnh: '',
        trangThai: 1
    });


    function getAllCategories() {
        getAllCategory()
            .then(async res => {
                listDanhMuc.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    function getAllCategoriesDetail() {
        getAllCategoryDetail()
            .then(async res => {
                listDanhMucChiTiet.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    const fetchVariants = () => {
        const item = typeof itemSource === 'function' ? itemSource() : itemSource;
        if (!item || !item.id) return;

        getFoodGeneralModalById(item.id)
            .then(async res => {
                variants.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    const openEditMode = (variant) => {
        selectedVariant.value = {...variant };
        currentView.value = 'update';
    };

    const backToList = () => {
        selectedVariant.value = null;
        currentView.value = 'list';
    };

    const filteredSubCategories = computed(() => {
        if (!parentFormData.value || !parentFormData.value.idDanhMuc) {
            return [];
        }
        return listDanhMucChiTiet.value.filter(item => {
            return String(item.idDanhMuc) === String(parentFormData.value.idDanhMuc);
        });
    });

    onMounted(() => {
        getAllCategories();
        getAllCategoriesDetail();
    });

    watch(
        () => (typeof itemSource === 'function' ? itemSource() : itemSource),
        (newVal) => {
            if (newVal) {
                parentFormData.value = {...newVal };
                if (newVal.idDanhMuc) {
                    parentFormData.value.idDanhMuc = newVal.danhMuc.id;
                }

                if (newVal.idDanhMucChiTiet) {
                    parentFormData.value.idDanhMucChiTiet = newVal.idDanhMucChiTiet;
                }
                fetchVariants();
                currentView.value = 'list';
            } else {
                parentFormData.value = {
                    id: '',
                    tenMonAn: '',
                    idDanhMuc: '',
                    idDanhMucChiTiet: '',
                    moTa: '',
                    giaBan: 0,
                    hinhAnh: '',
                    trangThai: 1
                };
                variants.value = [];
            }
        }, { immediate: true, deep: true }
    );

    const handleSave = async(emit) => {
        try {
            const data = parentFormData.value;
            console.log("Dữ liệu cập nhật Variant:", data);
            await putNewFood(data.id, data);

            if (typeof emit === 'function') {
                emit('refresh');
                emit('close');
            }

            fetchVariants();
            backToList();

        } catch (e) {
            console.error("Lỗi cập nhật:", e);
        }
    };

    // 6. RETURN
    return {
        currentView,
        selectedVariant,
        variants,
        parentFormData,
        listDanhMuc,
        filteredSubCategories,

        openEditMode,
        backToList,
        fetchVariants,
        handleSave
    };
}

export function useFoodDetailUpdate() {
    const router = useRouter();
    const route = useRoute();
    const detailId = route.params.id;

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

    // --- 1. BIẾN MỚI CHO TÌM KIẾM & SORT ---
    const searchQuery = ref('');
    const sortOption = ref('name_asc');

    // Lấy biến slider từ hook usePriceFilter
    const { 
        selectedPriceRange, 
        globalMinPrice, 
        globalMaxPrice, 
        calculatePriceLimits 
    } = usePriceFilter();

    // --- 2. COMPUTED: LỌC DANH SÁCH MÓN GỐC (ĐÃ SỬA) ---
    const filteredMonAnList = computed(() => {
        let result = [...listMonAn.value];

        // A. Lọc theo Search (Tên/Mã)
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenMonAn && item.tenMonAn.toLowerCase().includes(query)) ||
                (item.maMonAn && item.maMonAn.toLowerCase().includes(query))
            );
        }

        // B. Lọc theo Khoảng giá (Đã tách riêng ra khỏi Search)
        // Logic: Lấy giá nhỏ nhất của món để so sánh với slider
        if (selectedPriceRange.value && selectedPriceRange.value.length === 2) {
            const [min, max] = selectedPriceRange.value;
            result = result.filter(item => {
                // Ưu tiên giaThapNhat, nếu không có thì lấy giaBan
                const price = parseFloat(item.giaThapNhat) || parseFloat(item.giaBan) || 0;
                return price >= min && price <= max;
            });
        }

        // C. Sắp xếp
        return result.sort((a, b) => {
            const nameA = (a.tenMonAn || '').toLowerCase();
            const nameB = (b.tenMonAn || '').toLowerCase();
            
            // Lấy giá để sort (ưu tiên giá thấp nhất)
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

    // --- 3. HÀM CHỌN MÓN ---
    const selectParentFood = (food) => {
        formData.value.idMonAnDiKem = food.id;
    };

    // --- 4. COMPUTED: Tên món cha ---
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
            
            // Tính toán lại khoảng giá Min/Max cho slider
            calculatePriceLimits(listMonAn.value);

            const data = resDetail.data;
            originalInfo.value = data; 

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
                trangThai: data.trangThai
            };

        } catch (e) {
            console.error("Lỗi load dữ liệu:", e);
            alert("Không tìm thấy chi tiết món ăn!");
            router.back();
        } finally {
            isLoading.value = false;
        }
    };

    onMounted(() => {
        if (detailId) fetchDetailData();
    });

    // --- XỬ LÝ ẢNH & UPDATE ---
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

    const handleFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            alert("Vui lòng chọn file hình ảnh!"); return;
        }
        try {
            const resizedBase64 = await resizeImage(file);
            formData.value.hinhAnh = resizedBase64; 
        } catch (e) { console.error(e); }
    };

    const handleUpdate = async () => {
        try {
            if (!formData.value.tenChiTietMonAn || !formData.value.idMonAnDiKem) {
                alert("Vui lòng nhập đủ thông tin!"); return;
            }
            await putNewFoodDetail(detailId, formData.value);
            alert("Cập nhật thành công!");
            router.push({ name: 'foodManager', query: { tab: 'chitietTD' } });
        } catch (e) {
            console.error("Lỗi cập nhật:", e);
            alert("Đã xảy ra lỗi khi cập nhật!");
        }
    };
    
    const goBack = () => router.back();

    return {
        formData,
        listMonAn,
        originalInfo,
        parentName,
        isLoading,
        
        // Return các biến lọc/sort cho giao diện
        searchQuery,
        sortOption,
        filteredMonAnList,
        selectParentFood,
        
        // Return biến slider
        selectedPriceRange,
        globalMinPrice,
        globalMaxPrice,

        handleUpdate,
        goBack,
        handleFileUpload
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
    const hotpotInfo = ref(null);

    const isViewMode = computed(() => {
        return route.name === 'viewHotpot';
    });

    const formData = ref({
        id: null,
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
    const currentIngredientId = ref('');
    const isLoading = ref(true);

    const searchQuery = ref("");
    const sortOption = ref("name_asc");

    const filteredFoodList = computed(() => {
        // Sao chép mảng để tránh mutation trực tiếp (quan trọng khi sort)
        let result = [...listFoodDetails.value];

        // --- BƯỚC 1: LỌC (SEARCH) ---
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase();
            result = result.filter(item =>
                (item.tenChiTietMonAn || item.tenDanhMucChiTiet || '').toLowerCase().includes(query)
            );
        }

        // --- BƯỚC 2: SẮP XẾP (SORT) ---
        return result.sort((a, b) => {
            // Lấy tên an toàn (tránh null)
            const nameA = (a.tenChiTietMonAn || a.tenDanhMucChiTiet || '').toLowerCase();
            const nameB = (b.tenChiTietMonAn || b.tenDanhMucChiTiet || '').toLowerCase();

            // Lấy giá an toàn
            const priceA = a.giaBan || 0;
            const priceB = b.giaBan || 0;

            switch (sortOption.value) {
                case 'name_asc': // Tên A -> Z
                    return nameA.localeCompare(nameB);

                case 'price_asc': // Giá Thấp -> Cao
                    return priceA - priceB;

                case 'price_desc': // Giá Cao -> Thấp
                    return priceB - priceA;

                default:
                    return 0;
            }
        });
    });

    const fetchDetailData = async() => {
        try {
            isLoading.value = true;
            const [resDetail, resLoaiSet, resFoodDetail] = await Promise.all([
                getHotpotById(hotpotId),
                getAllCategoryHotpot(),
                getAllFoodDetail()
            ]);

            listLoaiSet.value = resLoaiSet.data;
            listFoodDetails.value = resFoodDetail.data;

            const data = resDetail.data;

            // 2. Lưu dữ liệu gốc vào biến này để hiển thị ở Header
            hotpotInfo.value = data;

            // Map vào FormData (để sửa)
            formData.value = {
                id: data.id,
                tenSetLau: data.tenSetLau,
                idLoaiSet: data.loaiSet ? data.loaiSet.id : data.idLoaiSet,
                giaBan: data.giaBan,
                hinhAnh: data.hinhAnh,
                moTa: data.moTa,
                trangThai: data.trangThai
            };

            // Map danh sách chi tiết (giữ nguyên code cũ của bạn)
            if (data.listChiTietSetLau && data.listChiTietSetLau.length > 0) {
                selectedIngredients.value = data.listChiTietSetLau.map(item => {
                    // ... logic map giữ nguyên ...
                    // Nhớ check null item.chiTietMonAn như bài trước
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
            router.back();
        } finally {
            isLoading.value = false;
        }
    };

    onMounted(() => {
        if (hotpotId) {
            fetchDetailData();
        }
    });

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

    const totalComponentsPrice = computed(() => {
        return selectedIngredients.value.reduce((sum, item) => sum + (item.giaBan * item.soLuong), 0);
    });

    // --- HÀM CẬP NHẬT (PUT) ---
    const handleUpdate = async() => {
        try {
            if (!formData.value.tenSetLau || !formData.value.idLoaiSet) {
                alert("Vui lòng điền đủ thông tin!");
                return;
            }

            const payload = {
                ...formData.value,
                // Map lại danh sách món để gửi về Backend
                listChiTietSetLau: selectedIngredients.value.map(item => ({
                    idChiTietMonAn: item.id,
                    soLuong: item.soLuong
                }))
            };

            await putNewHotpot(hotpotId, payload); // Gọi API PUT

            alert("Cập nhật thành công!");
            router.push({ name: 'foodManager', query: { tab: 'setlau' } });

        } catch (e) {
            console.error(e);
            alert("Lỗi khi cập nhật!");
        }
    };

    const goBack = () => router.back();

    const categoryName = computed(() => {
        if (!hotpotInfo.value) return 'Đang tải...';

        if (hotpotInfo.value.loaiSet && hotpotInfo.value.loaiSet.tenLoaiSet) {
            return hotpotInfo.value.loaiSet.tenLoaiSet;
        }

        const idToFind = hotpotInfo.value.idLoaiSet || (hotpotInfo.value.loaiSet ? hotpotInfo.value.loaiSet.id : null);

        if (idToFind && listLoaiSet.value.length > 0) {
            const found = listLoaiSet.value.find(cat => cat.id === idToFind);
            if (found) return found.tenLoaiSet;
        }

        return 'Chưa phân loại';
    });

    const handleFileUpload = (event) => {
        const file = event.target.files[0];
        if (!file) return;

        if (!file.type.match('image.*')) {
            alert("Vui lòng chọn file hình ảnh!");
            return;
        }

        const reader = new FileReader();
        reader.readAsDataURL(file);

        reader.onload = (e) => {
            formData.value.hinhAnh = e.target.result;
        };

        reader.onerror = (error) => {
            console.error('Lỗi đọc file:', error);
        };
    };

    return {
        formData,
        listLoaiSet,
        selectedIngredients,
        totalComponentsPrice,
        searchQuery,
        sortOption,
        filteredFoodList,
        hotpotInfo,
        categoryName,
        handleFileUpload,
        addIngredient,
        removeIngredient,
        handleUpdate,
        goBack,
        isViewMode,
        isLoading
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

    function getAllCategories() {
        getAllCategory()
            .then(async res => {
                categoryData.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => {
        getAllCategories();
    })

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
            console.log(payload);
            console.log(item.id);
            await putNewCategory(item.id, payload);

        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };

    return {
        categoryData,
        isModalOpen,
        isModalUpdateOpen,
        selectedItem,
        openModal,
        getAllCategories,
        handleToggleStatus
    };
}

export function useCategoryDetailManager() {
    const detailData = ref([]);

    function getAllCategoriesDetail() {
        getAllCategoryDetail()
            .then(async res => {
                detailData.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => {
        getAllCategoriesDetail();
    })

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
            console.log(payload);
            console.log(item.id);
            await putCategoryDetail(item.id, payload);

        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };

    return {
        detailData,
        isModalOpen,
        isModalUpdateOpen,
        selectedItem,
        openModal,
        handleToggleStatus,
        getAllCategoriesDetail
    };
}

export function useHotpotSetTypeManager() {
    const hotpotTypeData = ref([]);

    function getAllHotpotType() {
        getAllCategoryHotpot()
            .then(async res => {
                hotpotTypeData.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => {
        getAllHotpotType();
    })

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
            console.log(payload);
            console.log(item.id);
            await putNewHotpotCategory(item.id, payload);

        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };


    return {
        hotpotTypeData,
        isModalOpen,
        isModalUpdateOpen,
        selectedItem,
        openModal,
        handleToggleStatus,
        getAllHotpotType
    };
}

export function useFoodAddModal(props, emit) {

    const formData = ref({
        tenMonAn: '',
        idDanhMuc: '',
        idDanhMucChiTiet: '',
        giaBan: 0,
        moTa: '',
        hinhAnh: '',
        trangThaiKinhDoanh: 1
    });

    const listDanhMuc = ref([]);
    const listDanhMucChiTiet = ref([]);

    function getAllCategories() {
        getAllCategory()
            .then(async res => {
                listDanhMuc.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    function getAllCategoriesDetail() {
        getAllCategoryDetail()
            .then(async res => {
                listDanhMucChiTiet.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => {
        getAllCategories();
        getAllCategoriesDetail();
    })

    const filteredSubCategories = computed(() => {
        if (!formData.value.idDanhMuc) return [];
        return listDanhMucChiTiet.value.filter(item => item.idDanhMuc === formData.value.idDanhMuc);
    });

    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenMonAn: '',
                idDanhMuc: '',
                idDanhMucChiTiet: '',
                moTa: '',
                giaBan: 0,
                hinhAnh: '',
                trangThaiKinhDoanh: 1
            };
        }
    });

    const handleSave = async() => {
        try {
            if (!formData.value.tenMonAn || !formData.value.idDanhMucChiTiet) {
                alert("Vui lòng nhập tên món và chọn danh mục chi tiết!");
                return;
            }

            // Gọi API Thêm mới (Example)
            // await axios.post('http://localhost:8080/api/manage/food', formData.value);
            postNewFood(formData.value);

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
        listDanhMuc,
        filteredSubCategories,
        handleSave
    };
}

export function useHotpotAdd() {
    const router = useRouter();

    // ... (Giữ nguyên phần formData, listLoaiSet...) ...
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

    // --- MỚI: State cho tìm kiếm và sắp xếp ---
    const searchQuery = ref("");
    const sortOption = ref("name_asc"); // name_asc, price_asc, price_desc

    // --- MỚI: Computed để lọc danh sách món ăn hiển thị ---
    const filteredFoodList = computed(() => {
        let result = listFoodDetails.value;

        // 1. Lọc theo tên tìm kiếm
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase();
            result = result.filter(item =>
                (item.tenChiTietMonAn || item.tenDanhMucChiTiet).toLowerCase().includes(query)
            );
        }

        // 2. Sắp xếp
        return result.sort((a, b) => {
            const nameA = (a.tenChiTietMonAn || a.tenDanhMucChiTiet).toLowerCase();
            const nameB = (b.tenChiTietMonAn || b.tenDanhMucChiTiet).toLowerCase();

            if (sortOption.value === 'name_asc') return nameA.localeCompare(nameB);
            if (sortOption.value === 'price_asc') return a.giaBan - b.giaBan;
            if (sortOption.value === 'price_desc') return b.giaBan - a.giaBan;
            return 0;
        });
    });

    // --- CẬP NHẬT: Hàm thêm món (nhận object item thay vì ID từ dropdown) ---
    const addIngredient = (foodItem) => {
        // Kiểm tra đã có trong danh sách chọn chưa
        const exists = selectedIngredients.value.find(item => item.id === foodItem.id);

        if (exists) {
            // Nếu có rồi thì tăng số lượng lên 1 (UX mượt hơn là báo lỗi)
            exists.soLuong += 1;
        } else {
            // Nếu chưa có thì thêm mới
            selectedIngredients.value.push({
                id: foodItem.id,
                ten: foodItem.tenChiTietMonAn || foodItem.tenDanhMucChiTiet,
                donVi: foodItem.donVi || 'Phần',
                giaBan: foodItem.giaBan,
                hinhAnh: foodItem.hinhAnh, // Lưu thêm hình ảnh để hiển thị
                soLuong: 1
            });
        }
    };

    // ... (Giữ nguyên các hàm fetchInitialData, removeIngredient, handleSave...) ...

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
        }
    };

    onMounted(() => {
        fetchInitialData();
    });

    const removeIngredient = (index) => {
        selectedIngredients.value.splice(index, 1);
    };

    const totalComponentsPrice = computed(() => {
        return selectedIngredients.value.reduce((sum, item) => sum + (item.giaBan * item.soLuong), 0);
    });

    const handleSave = async() => {
        // ... (Giữ nguyên logic save cũ của bạn) ...
        try {
            if (!formData.value.tenSetLau || !formData.value.idLoaiSet || !formData.value.giaBan) {
                alert("Vui lòng nhập đủ: Tên set, Loại set và Giá bán!");
                return;
            }
            if (selectedIngredients.value.length === 0) {
                alert("Vui lòng thêm ít nhất 1 món ăn vào Set!");
                return;
            }
            const payload = {
                ...formData.value,
                listChiTietSetLau: selectedIngredients.value.map(item => ({
                    idChiTietMonAn: item.id,
                    soLuong: item.soLuong
                }))
            };
            await postNewHotpot(payload);
            alert("Thêm mới thành công!");
            router.push({ name: 'foodManager', query: { tab: 'setlau' } });
        } catch (e) {
            console.error(e);
            alert("Lỗi khi thêm mới!");
        }
    };

    const goBack = () => router.back();

    const handleFileUpload = (event) => {
        const file = event.target.files[0];
        if (!file) return;

        if (!file.type.match('image.*')) {
            alert("Vui lòng chọn file hình ảnh!");
            return;
        }

        const reader = new FileReader();
        reader.readAsDataURL(file);

        reader.onload = (e) => {
            formData.value.hinhAnh = e.target.result;
        };

        reader.onerror = (error) => {
            console.error('Lỗi đọc file:', error);
        };
    };

    return {
        formData,
        listLoaiSet,
        selectedIngredients,
        totalComponentsPrice,
        // Các biến mới return ra
        searchQuery,
        sortOption,
        filteredFoodList,
        addIngredient,
        removeIngredient,
        handleSave,
        goBack,
        handleFileUpload
    };
}

export function useFoodDetailAdd() {
    const router = useRouter();
    const route = useRoute();
    const parentIdFromUrl = route.query.parentId;

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

    // --- 1. BIẾN MỚI CHO TÌM KIẾM & SORT ---
    const searchQuery = ref('');
    const sortOption = ref('name_asc');

    // --- 2. COMPUTED: LỌC DANH SÁCH MÓN GỐC ---
    const filteredMonAnList = computed(() => {
        let result = [...listMonAn.value];

        // Lọc theo tên
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase();
            result = result.filter(item => 
                (item.tenMonAn || '').toLowerCase().includes(query)
            );
        }

        // Sắp xếp
        return result.sort((a, b) => {
            const nameA = (a.tenMonAn || '').toLowerCase();
            const nameB = (b.tenMonAn || '').toLowerCase();
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

    // --- 3. HÀM CHỌN MÓN ---
    const selectParentFood = (food) => {
        if (isParentLocked.value) return; // Nếu bị khóa (do truyền ID từ URL) thì không cho chọn lại
        formData.value.idMonAnDiKem = food.id;
    };

    // --- FETCH DATA ---
    const fetchInitialData = async() => {
        try {
            const resAllFood = await getAllFoodGeneral();
            listMonAn.value = resAllFood.data;

            if (parentIdFromUrl) {
                formData.value.idMonAnDiKem = parseInt(parentIdFromUrl);
                isParentLocked.value = true;
                const parent = listMonAn.value.find(f => f.id === parseInt(parentIdFromUrl));
                if (parent) parentFoodName.value = parent.tenMonAn;
            }
        } catch (e) {
            console.error("Lỗi load dữ liệu:", e);
        }
    };

    onMounted(() => {
        fetchInitialData();
    });

    const handleFileUpload = (event) => {
        const file = event.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = (e) => { formData.value.hinhAnh = e.target.result; };
    };

    const handleSave = async() => {
        try {
            if (!formData.value.tenChiTietMonAn || !formData.value.idMonAnDiKem) {
                alert("Vui lòng nhập Tên chi tiết và chọn Món ăn cha!");
                return;
            }
            // Code nén ảnh nên thêm vào đây nếu cần
            await postNewFoodDetail(formData.value);
            alert("Thêm chi tiết thành công!");
            router.back();
        } catch (e) {
            console.error("Lỗi thêm mới:", e);
            alert("Đã xảy ra lỗi khi lưu dữ liệu!");
        }
    };

    const goBack = () => router.back();

    return {
        formData,
        listMonAn,
        isParentLocked,
        parentFoodName,
        // Return thêm các biến mới
        searchQuery,
        sortOption,
        filteredMonAnList,
        selectParentFood,
        // ...
        handleSave,
        goBack,
        handleFileUpload
    };
}

export function useCategoryAddModal(props, emit) {

    const formData = ref({
        tenDanhMuc: '',
        moTa: '',
        trangThai: 1
    });


    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenDanhMuc: '',
                moTa: '',
                trangThai: 1
            };
        }
    });

    const handleSave = async() => {
        try {
            // Gọi API Thêm mới (Example)
            // await axios.post('http://localhost:8080/api/manage/food', formData.value);
            postNewCategory(formData.value);

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
        handleSave
    };
}

export function useCategoryPutModal(props, emit) {

    const formData = ref({
        tenDanhMuc: '',
        moTa: '',
        trangThai: 1
    });

    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            formData.value = {...newItem };
        } else {
            formData.value = {
                tenDanhMuc: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    const handleSave = async() => {
        try {
            await putNewCategory(formData.value.id, formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal
    };
}

export function useCategoryHotpotPutModal(props, emit) {

    const formData = ref({
        tenLoaiSet: '',
        moTa: '',
        trangThai: 1
    });

    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            formData.value = {...newItem };
        } else {
            formData.value = {
                tenLoaiSet: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    const handleSave = async() => {
        try {
            await putNewHotpotCategory(formData.value.id, formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal
    };
}


export function useHotpotCategoryAddModal(props, emit) {

    const formData = ref({
        tenLoaiSet: '',
        moTa: '',
        trangThai: 1
    });


    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenLoaiSet: '',
                moTa: '',
                trangThai: 1
            };
        }
    });

    const handleSave = async() => {
        try {
            // Gọi API Thêm mới (Example)
            // await axios.post('http://localhost:8080/api/manage/food', formData.value);
            postNewHotpotCategory(formData.value);

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
        handleSave
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

    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            formData.value = {...newItem };
        } else {
            formData.value = {
                idDanhMuc: '',
                tenDanhMucChiTiet: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    const handleSave = async() => {
        try {
            await putCategoryDetail(formData.value.id, formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal,
        listDanhMucGoc
    };
}